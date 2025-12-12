@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import tg.univlome.epl.R
import tg.univlome.epl.databinding.ActivityMapsBinding
import tg.univlome.epl.models.Lieu
import tg.univlome.epl.models.Salle
import tg.univlome.epl.services.BatimentService
import tg.univlome.epl.services.InfrastructureService
import tg.univlome.epl.services.SalleService
import tg.univlome.epl.utils.MapsUtils
import java.io.IOException
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer

class MapsActivity : AppCompatActivity(), LocationListener {

    private lateinit var binding: ActivityMapsBinding

    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private val client = OkHttpClient()

    private lateinit var batimentService: BatimentService
    private lateinit var infrastructureService: InfrastructureService
    private lateinit var salleService: SalleService

    private lateinit var locationManager: LocationManager
    private var currentPolyline: Polyline? = null
    private var lastLocation: Location? = null

    // Déclaration des variables globales
    private var userLocation: GeoPoint? = null
    private var destination: GeoPoint? = null

    private val markerList = mutableListOf<Marker>()
    private var currentUserMarker: Marker? = null
    private var currentDestinationMarker: Marker? = null
    private var preDestinationIcon: Drawable? = null
    private var isNightMode = false
    private var isOtherMarkersHidden = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Clusters pour l'affichage des markeurs
    private lateinit var clusterer: RadiusMarkerClusterer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadMapData()
    }

    private fun loadMapData() {
        // Initialiser mapView en premier
        mapView = binding.mapView

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = packageName

        destination = MapsUtils.loadDestination(this)

        // Initialisation du service Firebase
        batimentService = BatimentService(this)
        infrastructureService = InfrastructureService(this)
        //salleService = SalleService()
        salleService = SalleService(this)

        //mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Initialisation du clusterer
        setupClusterer()

        val prefs = getSharedPreferences("map_state", 0)
        val latitude = prefs.getString("latitude", "6.1375")!!.toDouble()
        val longitude = prefs.getString("longitude", "1.2123")!!.toDouble()
        val zoom = prefs.getFloat("zoom", 17.0f)
        isNightMode = prefs.getBoolean("isNightMode", false)

        val savedPoint = GeoPoint(latitude, longitude)
        mapView.controller.setZoom(zoom.toDouble())
        mapView.controller.setCenter(savedPoint)

        // Ajouter une boussole
        val compassOverlay = CompassOverlay(this, mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        // Ajouter une barre d'échelle
        val scaleBarOverlay = ScaleBarOverlay(mapView)
        scaleBarOverlay.setScaleBarOffset(130, 20) // Ajuster la position sur l'écran
        mapView.overlays.add(scaleBarOverlay)

        // Charger les données depuis Firebase et les afficher sur la carte
        loadLieux()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            binding.root.post {
                initLocationOverlay()
                initLocationTracking()
            }
        }

        binding.focusLocation.setOnClickListener {
            userLocation?.let {
                mapView.controller.animateTo(it)
            } ?: Toast.makeText(this, "Position actuelle inconnue", Toast.LENGTH_SHORT).show()
        }

        binding.focusDestination.setOnClickListener {
            if (destination != null && destination != GeoPoint(0.0, 0.0)) {
                destination?.let {
                    mapView.controller.animateTo(it)
                } ?: Toast.makeText(this, "Destination inconnue", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Destination inconnue", Toast.LENGTH_SHORT).show()
            }
        }

        binding.themeIcon.setOnClickListener {
            isNightMode = !isNightMode
            mapView.setTileSource(
                if (isNightMode) TileSourceFactory.WIKIMEDIA else TileSourceFactory.MAPNIK
            )
            mapView.invalidate()
        }

        binding.hide.setOnClickListener {
            if (isOtherMarkersHidden) {
                reloadOtherMarker()
            } else {
                hideOtherMarkers()
            }
        }

        binding.recharger.setOnClickListener {
            reloadPreDestinationIcon()
            destination = null
            MapsUtils.clearDestination(this)
            currentPolyline?.let { mapView.overlays.remove(it) }
            currentPolyline = null
            mapView.controller.setCenter(userLocation ?: GeoPoint(6.1375, 1.2123))
            mapView.invalidate()
            loadLieux()
        }

        binding.btnRetour.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    @SuppressLint("MissingPermission")
    private fun initLocationTracking() {
        locationManager = getSystemService(LocationManager::class.java)
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isLocationEnabled) {
            Toast.makeText(this, "Veuillez activer la localisation", Toast.LENGTH_SHORT).show()
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        runOnUiThread {
            try {
                userLocation = GeoPoint(location.latitude, location.longitude)
                addMarkerUserLocation()

                if (userLocation != null && (lastLocation == null || location.distanceTo(
                        lastLocation!!
                    ) > 3)
                ) {
                    lastLocation = location
                    if (destination != null && destination != GeoPoint(0.0, 0.0)) {
                        if (markerList.isNotEmpty()) {
                            updateRoute(userLocation!!, destination!!)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MapsActivity", "Erreur dans onLocationChanged", e)
            }
        }
    }

    private fun loadLieux() {
        removeAllMarkers()

        // Charger les bâtiments
        batimentService.getBatiments().observe(this) { batiments ->
            if (batiments != null) {
                for (batiment in batiments) {
                    ajouterLieuSurCarte(batiment)
                }
            }
        }

        // Charger les infrastructures
        infrastructureService.getInfrastructures().observe(this) { infrastructures ->
            if (infrastructures != null) {
                for (infrastructure in infrastructures) {
                    ajouterLieuSurCarte(infrastructure)
                }
            }
        }

        // Charger les salles
        salleService.getSalles().observe(this) { salles ->
            if (salles != null) {
                for (salle in salles) {
                    ajouterLieuSurCarte(salle)
                }
            }
        }
    }

    private fun ajouterLieuSurCarte(lieu: Lieu) {
        if (lieu.latitude.isNotEmpty() && lieu.longitude.isNotEmpty()) {
            try {
                val lat = lieu.latitude.toDouble()
                val lon = lieu.longitude.toDouble()
                val position = GeoPoint(lat, lon)

                // Déterminer l'image à afficher selon le type de lieu
                val icon = when (lieu) {
                    is tg.univlome.epl.models.Batiment -> R.drawable.batiment_nav_icon
                    is tg.univlome.epl.models.Infrastructure -> R.drawable.infra_nav_icon
                    is Salle -> R.drawable.salle_nav_icon
                    else -> R.drawable.default_marker
                }

                //addMarker(position, lieu.nom, icon, lieu.image)
                // CACHER LES MARQUEURS A CE NIVEAU
                //var marker = addMarker(position, lieu.nom, icon, lieu.image)

                var marker = addClusterMarker(position, lieu.nom, icon, lieu.image)
                if (marker!!.position != userLocation && marker!!.position != destination) {
                    //mapView.overlays.remove(marker)
                    clusterer.items.remove(marker)
                }
            } catch (e: NumberFormatException) {
                Log.e("MapsActivity", "Coordonnées invalides pour ${lieu.nom}")
            }
        }
    }

    private fun initLocationOverlay() {
        binding.locationProgressBar.visibility = View.VISIBLE
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Vérification de la permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {

            // Essai rapide via FusedLocationProviderClient
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userGeoPoint = GeoPoint(location.latitude, location.longitude)
                        userLocation = userGeoPoint

                        mapView.controller.setCenter(userLocation)
                        addMarkerUserLocation()

                        destination = MapsUtils.loadDestination(this)
                        if (destination != null && destination != GeoPoint(0.0, 0.0)) {
                            updateRoute(userLocation!!, destination!!)
                        }

                        binding.locationProgressBar.visibility = View.GONE

                    } else {
                        // Fallback GPS si lastLocation est null
                        startGPSLocation()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erreur de localisation : ${e.message}", Toast.LENGTH_LONG)
                        .show()
                    binding.locationProgressBar.visibility = View.GONE
                }

        } else {
            // Demande de permission si elle n'est pas accordée
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startGPSLocation() {
        val locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        val provider = LocationManager.GPS_PROVIDER

        locationManager.requestSingleUpdate(provider, object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val userGeoPoint = GeoPoint(location.latitude, location.longitude)
                userLocation = userGeoPoint

                mapView.controller.setCenter(userLocation)
                addMarkerUserLocation()

                destination = MapsUtils.loadDestination(this@MapsActivity)
                if (destination != null && destination != GeoPoint(0.0, 0.0)) {
                    updateRoute(userLocation!!, destination!!)
                }

                binding.locationProgressBar.visibility = View.GONE
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }, null)
    }

    private fun updateRoute(userLocation: GeoPoint, userDestination: GeoPoint) {
        addMarkerUserLocation(userLocation)

        if (userDestination == null || userDestination == GeoPoint(0.0, 0.0)) {
            Log.e("MapsActivity", "Destination is null or {0,0}, cannot update route")
            return
        }

        currentPolyline?.let { mapView.overlays.remove(it) }

        updateDestination(userDestination)
        getRoute(userLocation, userDestination)
    }

    private fun addMarker(
        position: GeoPoint,
        title: String,
        icon: Int = R.drawable.default_marker,
        imageUrl: String? = null
    ): Marker? {
        if (mapView == null) {
            Log.w(
                "MapsActivity",
                "mapView n'est pas encore initialisé. Nouvelle tentative dans 200 ms..."
            )
            Handler(Looper.getMainLooper()).postDelayed({
                addMarker(position, title, icon, imageUrl)
            }, 200)
            return null
        }

        var oldMarker: Marker? = null

        for (marker in markerList) {
            if (marker.position == position && marker.title == title) {
                oldMarker = marker
                mapView.overlays.remove(oldMarker)
            }
        }
        if (oldMarker != null) {
            markerList.remove(oldMarker)
        }

        if (mapView == null) {
            Log.e("MapsActivity", "MapView est nul. Impossible d'ajouter un marqueur.")
            return null
        }

        Log.d("MapsActivity", "Ajout du marqueur: $title à $position")
        val marker = Marker(mapView)
        marker.position = position
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val drawable = BitmapDrawable(resources, resource)
                        marker.image = drawable
                        mapView.invalidate()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        val resizedDrawable = MapsUtils.resizeIcon(icon, resources)
        marker.icon = resizedDrawable

        mapView.overlays.add(marker)
        mapView.invalidate()

        markerList.add(marker)
        return marker
    }

    private fun removeAllMarkers() {
        for (marker in markerList) {
            if (::clusterer.isInitialized) {
                clusterer.items.remove(marker)
                clusterer.invalidate()
            }
            mapView.overlays.remove(marker)
        }
        markerList.clear()
        mapView.invalidate()
    }

    private fun hideOtherMarkers() {
        if (!::clusterer.isInitialized) return

        val iterator = clusterer.items.iterator()
        while (iterator.hasNext()) {
            val marker = iterator.next()
            if (marker.position != userLocation && marker.position != destination) {
                iterator.remove() // retire du cluster
            }
        }
        clusterer.invalidate()
        mapView.invalidate()

        isOtherMarkersHidden = true
        binding.hide.setImageResource(R.drawable.no_hide)
    }
    private fun hideOtherMarkersOld() {
        for (marker in markerList) {
            if (marker.position != userLocation && marker.position != destination) {
                mapView.overlays.remove(marker)
            }
        }
        mapView.invalidate()  // Refresh the map
        isOtherMarkersHidden = true
        binding.hide.setImageResource(R.drawable.no_hide)
    }

    private fun reloadOtherMarker() {
        if (!::clusterer.isInitialized) return

        for (marker in markerList) {
            if (marker.position != userLocation && marker.position != destination && !clusterer.items.contains(marker)) {
                clusterer.add(marker)
            }
        }
        clusterer.invalidate()
        mapView.invalidate()

        isOtherMarkersHidden = false
        binding.hide.setImageResource(R.drawable.hide)
    }

    private fun reloadOtherMarkerOld() {
        for (marker in markerList) {
            if (marker.position != userLocation && marker.position != destination) {
                mapView.overlays.add(marker)
            }
        }
        mapView.invalidate()  // Refresh the map
        isOtherMarkersHidden = false
        binding.hide.setImageResource(R.drawable.hide)
    }

    private fun getRoute(start: GeoPoint, end: GeoPoint) {
        val apiKey = "5b3ce3597851110001cf62480894b05967b24b268cf8fa5b6a5166f7"
        val url =
            "https://api.openrouteservice.org/v2/directions/driving-car?api_key=$apiKey&start=${start.longitude},${start.latitude}&end=${end.longitude},${end.latitude}"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(
                    "MapsActivity",
                    "Erreur lors de la récupération de l'itinéraire : ${e.message}"
                )
                runOnUiThread {
                    Toast.makeText(
                        this@MapsActivity,
                        "Impossible de récupérer l'itinéraire",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { jsonString ->
                        try {
                            val jsonObject = JSONObject(jsonString)
                            val features = jsonObject.getJSONArray("features")
                            if (features.length() > 0) {
                                val geometry = features.getJSONObject(0).getJSONObject("geometry")
                                val coordinates = geometry.getJSONArray("coordinates")

                                val geoPoints = mutableListOf<GeoPoint>()
                                for (i in 0 until coordinates.length()) {
                                    val coord = coordinates.getJSONArray(i)
                                    val lon = coord.getDouble(0)
                                    val lat = coord.getDouble(1)
                                    geoPoints.add(GeoPoint(lat, lon))
                                }

                                runOnUiThread {
                                    currentPolyline = Polyline()
                                    currentPolyline!!.setPoints(geoPoints)
                                    currentPolyline!!.outlinePaint.color =
                                        resources.getColor(R.color.mainColor, null)
                                    currentPolyline!!.outlinePaint.strokeWidth = 5f

                                    mapView.overlays.add(currentPolyline)
                                    mapView.invalidate()
                                }
                            }
                        } catch (e: JSONException) {
                            Log.e("MapsActivity", "Erreur JSON: ${e.message}")
                        }
                    }
                } else {
                    Log.e("MapsActivity", "Erreur dans la réponse de l'API de routage")
                }
            }
        })
    }

    private fun addMarkerUserLocation(userLocation: GeoPoint? = this.userLocation) {
        if (mapView == null || userLocation == null) {
            Log.w("MapsActivity", "Conditions non remplies pour ajouter le marqueur de position")
            return
        }

        currentUserMarker?.let {
            mapView.overlays.remove(it)
            markerList.remove(it)
        }

        currentUserMarker =
            addMarker(userLocation, "Ma position actuelle", R.drawable.maps_and_flags)
    }

    private fun updateDestination(destination: GeoPoint) {
        var find = false
        reloadPreDestinationIcon()
        if (markerList.isNotEmpty()) {
            for (marker in markerList) {
                if (marker.position == destination) {
                    find = true
                    preDestinationIcon = marker.icon
                    marker.icon = MapsUtils.resizeIcon(R.drawable.destination, resources)
                    currentDestinationMarker = marker
                    mapView.invalidate()
                    break
                }
            }
        }
    }

    private fun reloadPreDestinationIcon() {
        currentDestinationMarker?.icon = preDestinationIcon
        mapView.invalidate()
    }

    private fun setupClusterer() {
        clusterer = RadiusMarkerClusterer(this)
        clusterer.setRadius(100) // rayon du cluster en pixels
        val resizedDrawable = MapsUtils.resizeIcon(R.drawable.cluster_icon, resources)
        val bitmap = (resizedDrawable as BitmapDrawable).bitmap
        clusterer.setIcon(bitmap)
        mapView.overlays.add(clusterer)
    }

    private fun addClusterMarker(
        position: GeoPoint,
        title: String,
        iconRes: Int = R.drawable.default_marker,
        imageUrl: String? = null
    ): Marker? {
        if (mapView == null) {
            Log.w("MapsActivity", "mapView n'est pas encore initialisé.")
            return null
        }

        // Vérifier les doublons
        var oldMarker: Marker? = null
        for (marker in markerList) {
            if (marker.position == position && marker.title == title) {
                oldMarker = marker
                if (::clusterer.isInitialized) {
                    clusterer.items.remove(marker)
                    clusterer.invalidate()
                }
            }
        }
        oldMarker?.let { markerList.remove(it) }

        // Créer le marqueur
        Log.d("MapsActivity", "Ajout du marqueur: $title à $position")
        val marker = Marker(mapView)
        marker.position = position
        marker.title = title
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = MapsUtils.resizeIcon(iconRes, resources)

        // Charger l'image si elle existe
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val drawable = BitmapDrawable(resources, resource)
                        marker.image = drawable
                        mapView.invalidate()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        // Ajouter au clusterer
        if (::clusterer.isInitialized) {
            clusterer.add(marker)
        } else {
            Log.e("MapsActivity", "Clusterer non initialisé !")
        }

        markerList.add(marker)
        mapView.invalidate()
        return marker
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        try {
            locationManager.removeUpdates(this)
        } catch (e: Exception) {
            Log.w("MapsActivity", "Erreur lors de l'arrêt des mises à jour de localisation", e)
        }

        val prefs = getSharedPreferences("map_state", 0)
        val editor = prefs.edit()

        val mapCenter = mapView.mapCenter as GeoPoint
        val zoomLevel = mapView.zoomLevelDouble

        editor.putString("latitude", mapCenter.latitude.toString())
        editor.putString("longitude", mapCenter.longitude.toString())
        editor.putFloat("zoom", zoomLevel.toFloat())
        editor.putBoolean("isNightMode", isNightMode)

        editor.apply()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeAllMarkers()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocationOverlay()
                initLocationTracking()
            } else {
                Toast.makeText(this, "Permission de localisation refusée.", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}