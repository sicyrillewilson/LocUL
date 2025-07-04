@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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
import tg.univlome.epl.MainActivity
import tg.univlome.epl.R
import tg.univlome.epl.databinding.FragmentMapsBinding
import tg.univlome.epl.models.Lieu
import tg.univlome.epl.services.BatimentService
import tg.univlome.epl.services.InfrastructureService
import tg.univlome.epl.services.SalleService
import tg.univlome.epl.ui.SearchBarFragment
import java.io.IOException

class MapsFragmentOld : Fragment(), SearchBarFragment.SearchListener, LocationListener {

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

        Configuration.getInstance()
            .load(requireContext(), requireActivity().getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = requireActivity().packageName

        // Initialisation du service Firebase
        batimentService = BatimentService(requireContext())
        infrastructureService = InfrastructureService(requireContext())
        //salleService = SalleService()
        salleService = SalleService(requireContext())

        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val prefs = requireActivity().getSharedPreferences("map_state", 0)
        val latitude = prefs.getString("latitude", "6.1375")!!.toDouble()
        val longitude = prefs.getString("longitude", "1.2123")!!.toDouble()
        val zoom = prefs.getFloat("zoom", 17.0f)

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

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            view.post {
                initLocationOverlay()
                initLocationTracking()
            }
        }


        arguments?.let {
            val lat = it.getDouble("latitude", 0.0)
            val lon = it.getDouble("longitude", 0.0)
            if (lat != 0.0 && lon != 0.0) {
                destination = GeoPoint(lat, lon)
                Log.e("MapsFragment", "Destination changée: $destination")
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun initLocationTracking() {
        if (isAdded) {
            locationManager = requireActivity().getSystemService(LocationManager::class.java)
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                3000,
                10f,
                this
            ) // Maj toutes les 3 sec, 10m de différence
        }
    }

    override fun onLocationChanged(location: Location) {
        userLocation = GeoPoint(location.latitude, location.longitude)

        if (userLocation != null && (lastLocation == null || location.distanceTo(lastLocation!!) > 3)) {
            lastLocation = location
            updateRoute(userLocation!!, destination!!)
        }
    }

    private fun loadLieux() {

        // Charger les bâtiments
        batimentService.getBatiments().observe(viewLifecycleOwner, Observer { batiments ->
            if (batiments != null) {
                for (batiment in batiments) {
                    ajouterLieuSurCarte(batiment)
                }
            }
        })

        // Charger les infrastructures
        infrastructureService.getInfrastructures()
            .observe(viewLifecycleOwner, Observer { infrastructures ->
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
                markerList.add(
                    addMarker(
                        position,
                        lieu.nom,
                        lieu.image
                    )
                )  // Store the marker in the list
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

                        // Définition de la destination
                        if (destination == null) {
                            destination =
                                GeoPoint(userLocation!!.latitude + 0.009, userLocation!!.longitude)
                        } else {
                            updateRoute(userLocation!!, destination!!)
                        }

                        // getRoute(userLocation!!, destination!!)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Localisation non trouvée !",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        mapView.overlays.add(locationOverlay)
    }

    private fun updateRoute(userLocation: GeoPoint, userDestination: GeoPoint) {

        addMarker(userLocation!!, "Ma position actuelle")
        addMarker(userDestination!!, "Ma destination")

        //val destination = GeoPoint(userLocation.latitude + 0.009, userLocation.longitude) // Ex. Destination fixe

        //if (destination == null) {
        if (userDestination == null) {
            Log.e("MapsFragment", "Destination is null, cannot update route")
            return
        }

        // Vérifier currentPolyline
        currentPolyline?.let { mapView.overlays.remove(it) }

        // Récupérer le nouvel itinéraire
        //getRoute(userLocation, destination!!)
        getRoute(userLocation, userDestination!!)
    }

    private fun addMarker(position: GeoPoint, title: String, imageUrl: String? = null): Marker {
        var oldMarker = Marker(mapView)
        for (marker in markerList) {
            if (marker.position == position && marker.title == title) {
                oldMarker = marker
                mapView.overlays.remove(oldMarker)
                //return marker
            }
        }
        markerList.remove(oldMarker)

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
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val drawable = BitmapDrawable(resources, resource)
                        marker.image = drawable
                        mapView.invalidate() // Rafraîchir la carte après chargement
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        val drawable = resources.getDrawable(R.drawable.default_marker, null)
        val bitmap = (drawable as BitmapDrawable).bitmap

        // Redimensionner l'image
        val scaledBitmap =
            Bitmap.createScaledBitmap(bitmap, 40, 40, false) // Modifier la taille selon le besoin
        val resizedDrawable = BitmapDrawable(resources, scaledBitmap)

        marker.icon = resizedDrawable

        mapView.overlays.add(marker)
        mapView.invalidate()
        return marker
    }

    private fun removeAllMarkers() {
        for (marker in markerList) {
            mapView.overlays.remove(marker)
        }
        markerList.clear()  // Optionally, clear the list after removal
        mapView.invalidate()  // Refresh the map
    }

    private fun getRoute(start: GeoPoint, end: GeoPoint) {
        val apiKey = "5b3ce3597851110001cf62480894b05967b24b268cf8fa5b6a5166f7"
        val url =
            "https://api.openrouteservice.org/v2/directions/driving-car?api_key=$apiKey&start=${start.longitude},${start.latitude}&end=${end.longitude},${end.latitude}"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Erreur réseau !", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonResponse ->
                    try {
                        val jsonObject = JSONObject(jsonResponse)
                        if (jsonObject.has("features")) {
                            val features = jsonObject.getJSONArray("features")
                            val geometry = features.getJSONObject(0).getJSONObject("geometry")
                            val coordinates = geometry.getJSONArray("coordinates")

                            val polyline = Polyline()
                            if (isAdded) {
                                //polyline.color = resources.getColor(android.R.color.holo_blue_dark, null)
                                polyline.color = resources.getColor(R.color.mainColor, null)
                            }

                            for (i in 0 until coordinates.length()) {
                                val coord = coordinates.getJSONArray(i)
                                val lon = coord.getDouble(0)
                                val lat = coord.getDouble(1)
                                polyline.addPoint(GeoPoint(lat, lon))
                            }

                            if (isAdded) {
                                requireActivity().runOnUiThread {
                                    currentPolyline?.let { mapView.overlays.remove(it) }
                                    currentPolyline = polyline
                                    mapView.overlays.add(polyline)
                                    mapView.invalidate()
                                }
                            }
                        } else {
                            Log.e("API_ERROR", "La clé 'features' est absente de la réponse JSON")
                        }
                    } catch (e: JSONException) {
                        Log.e("JSON_ERROR", "Erreur lors du parsing JSON: ${e.message}")
                    }
                } ?: Log.e("API_ERROR", "Réponse vide de l'API")
            }
        })
    }

    private fun filterMarkers(query: String?) {
        removeAllMarkers()

        if (query.isNullOrEmpty()) {
            loadLieux()
            return
        }

        val lowerCaseQuery = query.lowercase()

        batimentService.getBatiments().observe(viewLifecycleOwner, Observer { batiments ->
            if (batiments != null) {
                val batiments = batiments.filter { batiment ->
                    val name = batiment.nom?.lowercase() ?: ""
                    return@filter name.contains(lowerCaseQuery) || isSubsequence(
                        lowerCaseQuery,
                        name
                    )
                }
                for (batiment in batiments) {
                    ajouterLieuSurCarte(batiment)
                }
            }
        })

        infrastructureService.getInfrastructures()
            .observe(viewLifecycleOwner, Observer { infrastructures ->
                if (infrastructures != null) {
                    val infrastructures = infrastructures.filter { infrastructure ->
                        val name = infrastructure.nom?.lowercase() ?: ""
                        return@filter name.contains(lowerCaseQuery) || isSubsequence(
                            lowerCaseQuery,
                            name
                        )
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
                    return@filter name.contains(lowerCaseQuery) || isSubsequence(
                        lowerCaseQuery,
                        name
                    )
                }
                for (salle in salles) {
                    ajouterLieuSurCarte(salle)
                }
            }
        })

        removeAllMarkers()
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

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        (activity as MainActivity).showSearchBarFragment(this)
    }

    override fun onPause() {
        super.onPause()
        val prefs = requireActivity().getSharedPreferences("map_state", 0)
        val editor = prefs.edit()

        val mapCenter = mapView.mapCenter as GeoPoint
        val zoomLevel = mapView.zoomLevelDouble

        editor.putString("latitude", mapCenter.latitude.toString())
        editor.putString("longitude", mapCenter.longitude.toString())
        editor.putFloat("zoom", zoomLevel.toFloat())

        editor.apply()
        mapView.onPause()
        (activity as MainActivity).showSearchBarFragment(null) // Cacher la barre si on quitte
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSearch(query: String) {
        filterMarkers(query)
    }

}