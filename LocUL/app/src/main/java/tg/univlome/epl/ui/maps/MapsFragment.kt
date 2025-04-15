@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.maps

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tg.univlome.epl.R
import tg.univlome.epl.databinding.FragmentMapsBinding
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import okhttp3.*
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
import java.io.IOException
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import tg.univlome.epl.services.BatimentService
import tg.univlome.epl.services.InfrastructureService
import tg.univlome.epl.services.SalleService
import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import tg.univlome.epl.MainActivity
import tg.univlome.epl.models.Lieu
import tg.univlome.epl.models.Salle
import tg.univlome.epl.ui.SearchBarFragment
import tg.univlome.epl.utils.MapsUtils

class MapsFragment : Fragment(), SearchBarFragment.SearchListener , LocationListener  {
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

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
    private var isOtherMarkersHidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMapData(view)
    }

    private fun loadMapData(view: View){
        // Initialiser mapView en premier
        mapView = binding.mapView

        Configuration.getInstance().load(requireContext(), requireActivity().getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = requireActivity().packageName

        destination = MapsUtils.loadDestination(requireContext())

        // Initialisation du service Firebase
        batimentService = BatimentService()
        infrastructureService = InfrastructureService()
        salleService = SalleService()

        //mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val prefs = requireActivity().getSharedPreferences("map_state", 0)
        val latitude = prefs.getString("latitude", "6.1375")!!.toDouble()
        val longitude = prefs.getString("longitude", "1.2123")!!.toDouble()
        val zoom = prefs.getFloat("zoom", 17.0f)
        isNightMode = prefs.getBoolean("isNightMode", false)

        val savedPoint = GeoPoint(latitude, longitude)
        mapView.controller.setZoom(zoom.toDouble())
        mapView.controller.setCenter(savedPoint)

        // Ajouter une boussole
        val compassOverlay = CompassOverlay(requireActivity(), mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        // Ajouter une barre d'échelle
        val scaleBarOverlay = ScaleBarOverlay(mapView)
        scaleBarOverlay.setScaleBarOffset(130, 20) // Ajuster la position sur l'écran
        mapView.overlays.add(scaleBarOverlay)

        // Charger les données depuis Firebase et les afficher sur la carte
        loadLieux()

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            view.post {
                initLocationOverlay()
                initLocationTracking()
            }
        }

        binding.focusLocation.setOnClickListener {
            userLocation?.let {
                mapView.controller.animateTo(it)
            } ?: Toast.makeText(requireContext(), "Position actuelle inconnue", Toast.LENGTH_SHORT).show()
        }

        binding.focusDestination.setOnClickListener {
            if (destination != null && destination != GeoPoint(0.0, 0.0)) {
                destination?.let {
                    mapView.controller.animateTo(it)
                } ?: Toast.makeText(requireContext(), "Destination inconnue", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Destination inconnue", Toast.LENGTH_SHORT).show()
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
            MapsUtils.clearDestination(requireContext())
            currentPolyline?.let { mapView.overlays.remove(it) }
            currentPolyline = null
            mapView.controller.setCenter(userLocation ?: GeoPoint(6.1375, 1.2123))
            mapView.invalidate()
            loadMapData(view)
        }


        arguments?.let {
            val lat = it.getDouble("latitude", 0.0)
            val lon = it.getDouble("longitude", 0.0)
            if (lat != 0.0 && lon != 0.0) {
                destination = GeoPoint(lat, lon)
                MapsUtils.saveDestination(requireContext(), destination!!)
                Log.e("MapsFragment", "Destination changée: $destination")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationTracking() {
        if (isAdded){
            locationManager = requireActivity().getSystemService(LocationManager::class.java)
            val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!isLocationEnabled) {
                Toast.makeText(requireContext(), "Veuillez activer la localisation", Toast.LENGTH_SHORT).show()
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10f, this) // Maj toutes les 3 sec, 10m de différence
            }
        }
    }

    override fun onLocationChanged(location: Location) {

        // Vérification si MapView est bien initialisé
        if (!isAdded || mapView == null) {
            Log.w("MapsFragment", "Fragment non attaché ou MapView null, ignore la mise à jour")
            return
        }

        activity?.runOnUiThread {
            try {
                userLocation = GeoPoint(location.latitude, location.longitude)
                //addMarker(userLocation!!, "Ma position actuelle")
                addMarkerUserLocation()

                if (userLocation != null && (lastLocation == null || location.distanceTo(lastLocation!!) > 3)) {
                    lastLocation = location
                    if (destination != null && destination != GeoPoint(0.0, 0.0)) {
                        if (markerList.isNotEmpty()) {
                            updateRoute(userLocation!!, destination!!)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MapsFragment", "Erreur dans onLocationChanged", e)
            }
        }
    }

    private fun loadLieux() {
        removeAllMarkers() // Évite les doublons de marqueurs

        // Charger les bâtiments
        batimentService.getBatiments().observe(viewLifecycleOwner, Observer { batiments ->
            if (batiments != null) {
                for (batiment in batiments) {
                    ajouterLieuSurCarte(batiment)
                }
            }
        })

        // Charger les infrastructures
        infrastructureService.getInfrastructures().observe(viewLifecycleOwner, Observer { infrastructures ->
            if (infrastructures != null) {
                for (infrastructure in infrastructures) {
                    ajouterLieuSurCarte(infrastructure)
                }
            }
        })

        // Charger les salles
        salleService.getSalles().observe(viewLifecycleOwner, Observer { salles ->
            if (salles != null) {
                for (salle in salles) {
                    ajouterLieuSurCarte(salle)
                }
            }
        })
    }

    // Nouvelle méthode pour ajouter un lieu sur la carte
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
                    else -> R.drawable.maps_and_flags
                }

                addMarker(position, lieu.nom, icon, lieu.image)  // Store the marker in the list
            } catch (e: NumberFormatException) {
                Log.e("MapsFragment", "Coordonnées invalides pour ${lieu.nom}")
            }
        }
    }

    private fun initLocationOverlay() {
        locationOverlay = MyLocationNewOverlay(mapView)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation() // Pour suivre la position en temps réel
        mapView.overlays.add(locationOverlay)

        locationOverlay.runOnFirstFix {
            userLocation = locationOverlay.myLocation
            if (isAdded) {
                requireActivity().runOnUiThread {
                    if (userLocation != null) {
                        mapView.controller.setCenter(userLocation)
                        //addMarker(userLocation!!, "Ma position actuelle")
                        addMarkerUserLocation()

                        destination = MapsUtils.loadDestination(requireContext())
                        // Définition de la destination
                        if (!(destination == null || destination == GeoPoint(0.0, 0.0))) {
                            updateRoute(userLocation!!, destination!!)
                        }

                    } else {
                        Toast.makeText(requireContext(), "Localisation non trouvée !", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        mapView.overlays.add(locationOverlay)
    }

    private fun updateRoute(userLocation: GeoPoint, userDestination: GeoPoint ) {

        //addMarker(userLocation!!, "Ma position actuelle")
        addMarkerUserLocation(userLocation!!)
        //addMarker(userDestination!!, "Ma destination")

        if (userDestination == null || userDestination == GeoPoint(0.0, 0.0)) {
            Log.e("MapsFragment", "Destination is null or {0,0} , cannot update route")
            return
        }

        // Vérifier currentPolyline
        currentPolyline?.let { mapView.overlays.remove(it) }

        updateDestination(userDestination!!)
        // Récupérer le nouvel itinéraire
        getRoute(userLocation, userDestination)
    }

    private fun addMarker(position: GeoPoint, title: String, icon: Int = R.drawable.maps_and_flags, imageUrl: String? = null): Marker? {

        if (!isAdded || mapView == null) {
            Log.w("MapsFragment", "Fragment non attaché ou MapView null, impossible d'ajouter le marqueur")
            return null
        }

        if (mapView == null) {
            Log.w("MapsFragment", "mapView n'est pas encore initialisé. Nouvelle tentative dans 200 ms...")
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
        if (oldMarker != null){
            markerList.remove(oldMarker)
        }

        if (mapView == null) {
            Log.e("MapsFragment", "MapView est nul. Impossible d'ajouter un marqueur.");
            return null
        }
        Log.d("MapsFragment", "Ajout du marqueur: $title à $position")
        val marker = Marker(mapView)
        marker.position = position
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        if (isAdded) {
                            val drawable = BitmapDrawable(resources, resource)
                            marker.image = drawable
                            mapView.invalidate() // Rafraîchir la carte après chargement
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        val resizedDrawable = resizeIcon(icon)

        marker.icon = resizedDrawable

        mapView.overlays.add(marker)
        mapView.invalidate()

        markerList.add(marker)
        return marker
    }

    private fun resizeIcon(icon: Int = R.drawable.maps_and_flags): BitmapDrawable? {
        /*val drawable = resources.getDrawable(R.drawable.maps_and_flags, null)
        val bitmap = (drawable as BitmapDrawable).bitmap*/

        val drawable = ResourcesCompat.getDrawable(resources, icon, null)

        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        // Redimensionner l'image
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 75, 75, false) // Modifier la taille selon le besoin
        val resizedDrawable = BitmapDrawable(resources, scaledBitmap)

        return resizedDrawable
    }

    private fun removeAllMarkers() {
        for (marker in markerList) {
            mapView.overlays.remove(marker)
        }
        markerList.clear()  // Optionally, clear the list after removal
        mapView.invalidate()  // Refresh the map
    }

    private fun hideOtherMarkers() {
        for (marker in markerList) {
            if (marker.position != userLocation && marker.position != destination){
                mapView.overlays.remove(marker)
            }
        }
        mapView.invalidate()  // Refresh the map
        isOtherMarkersHidden = true
        binding.hide.setImageResource(R.drawable.no_hide)
    }

    private fun reloadOtherMarker() {
        for (marker in markerList) {
            if (marker.position != userLocation && marker.position != destination){
                mapView.overlays.add(marker)
            }
        }
        mapView.invalidate()  // Refresh the map
        isOtherMarkersHidden = false
        binding.hide.setImageResource(R.drawable.hide)
    }

    private fun getRoute(start: GeoPoint, end: GeoPoint) {
        val apiKey = "5b3ce3597851110001cf62480894b05967b24b268cf8fa5b6a5166f7"
        val url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=$apiKey&start=${start.longitude},${start.latitude}&end=${end.longitude},${end.latitude}"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MapsFragment", "Erreur lors de la récupération de l'itinéraire : ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Impossible de récupérer l'itinéraire", Toast.LENGTH_LONG).show()
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

                                requireActivity().runOnUiThread {
                                    currentPolyline = Polyline()
                                    currentPolyline!!.setPoints(geoPoints)
                                    currentPolyline!!.outlinePaint.color = resources.getColor(R.color.mainColor, null)
                                    currentPolyline!!.outlinePaint.strokeWidth = 5f

                                    mapView.overlays.add(currentPolyline)
                                    mapView.invalidate()
                                }
                            }
                        } catch (e: JSONException) {
                            Log.e("MapsFragment", "Erreur JSON: ${e.message}")
                        }
                    }
                } else {
                    Log.e("MapsFragment", "Erreur dans la réponse de l'API de routage")
                }
            }
        })
    }

    private fun filterMarkers(query: String?) {
        removeAllMarkers()

        if (query.isNullOrEmpty()) {
            loadLieux()
            if (userLocation != null){
                //addMarker(userLocation!!, "Ma position actuelle")
                addMarkerUserLocation()
            }
            return
        }


        val lowerCaseQuery = query.lowercase()

        batimentService.getBatiments().observe(viewLifecycleOwner, Observer { batiments ->
            if (batiments != null) {
                val batiments = batiments.filter { batiment ->
                    val name = batiment.nom?.lowercase() ?: ""
                    return@filter name.contains(lowerCaseQuery) || isSubsequence(lowerCaseQuery, name)
                }
                for (batiment in batiments) {
                    ajouterLieuSurCarte(batiment)
                }
            }
        })

        infrastructureService.getInfrastructures().observe(viewLifecycleOwner, Observer { infrastructures ->
            if (infrastructures != null) {
                val infrastructures = infrastructures.filter { infrastructure ->
                    val name = infrastructure.nom?.lowercase() ?: ""
                    return@filter name.contains(lowerCaseQuery) || isSubsequence(lowerCaseQuery, name)
                }
                for (infrastructure in infrastructures) {
                    ajouterLieuSurCarte(infrastructure)
                }
            }
        })

        salleService.getSalles().observe(viewLifecycleOwner, Observer { salles ->
            if (salles != null) {
                val salles = salles.filter { salle ->
                    val name = salle.nom?.lowercase() ?: ""
                    return@filter name.contains(lowerCaseQuery) || isSubsequence(lowerCaseQuery, name)
                }
                for (salle in salles) {
                    ajouterLieuSurCarte(salle)
                }
            }
        })

        removeAllMarkers()
        if (userLocation != null){
            //addMarker(userLocation!!, "Ma position actuelle")
            addMarkerUserLocation()
        }
        mapView.invalidate()
    }

    private fun isSubsequence(sub: String, word: String): Boolean {
        var i = 0
        var j = 0
        while (i < sub.length && j < word.length) {
            if (sub[i] == word[j]) i++
            j++
        }
        return i == sub.length
    }

    private fun addMarkerUserLocation(userLocation: GeoPoint? = this.userLocation){
        if (!isAdded || mapView == null || userLocation == null) {
            Log.w("MapsFragment", "Conditions non remplies pour ajouter le marqueur de position")
            return
        }

        // Supprimer l'ancien marqueur s'il existe
        currentUserMarker?.let {
            mapView.overlays.remove(it)
            markerList.remove(it)
        }

        currentUserMarker = addMarker(userLocation!!, "Ma position actuelle")
    }

    private fun updateDestination(destination: GeoPoint) {
        var find = false
        reloadPreDestinationIcon()
        if (markerList.isNotEmpty()) {
            for (marker in markerList) {
                if (marker.position == destination) {
                    find = true
                    preDestinationIcon = marker.icon
                    marker.icon = resizeIcon(R.drawable.maps_and_flags)
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


    override fun onResume() {
        super.onResume()
        mapView.onResume()
        (activity as MainActivity).showSearchBarFragment(this)
    }

    override fun onPause() {
        super.onPause()
        try {
            locationManager.removeUpdates(this)
        } catch (e: Exception) {
            Log.w("MapsFragment", "Erreur lors de l'arrêt des mises à jour de localisation", e)
        }

        val prefs = requireActivity().getSharedPreferences("map_state", 0)
        val editor = prefs.edit()

        val mapCenter = mapView.mapCenter as GeoPoint
        val zoomLevel = mapView.zoomLevelDouble

        editor.putString("latitude", mapCenter.latitude.toString())
        editor.putString("longitude", mapCenter.longitude.toString())
        editor.putFloat("zoom", zoomLevel.toFloat())
        editor.putBoolean("isNightMode", isNightMode)

        editor.apply()
        mapView.onPause()
        (activity as MainActivity).showSearchBarFragment(null) // Cacher la barre si on quitte
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeAllMarkers()  // Supprime tous les marqueurs
        _binding = null
    }

    override fun onSearch(query: String) {
        filterMarkers(query)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocationOverlay()
                initLocationTracking()
            } else {
                Toast.makeText(requireContext(), "Permission de localisation refusée.", Toast.LENGTH_LONG).show()
            }
        }
    }

}