@file:Suppress("DEPRECATION")

package tg.univlome.epl.fragments

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
import android.os.Handler
import android.os.Looper
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
import tg.univlome.epl.services.BatimentService
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.services.InfrastructureService
import tg.univlome.epl.services.SalleService


class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private val client = OkHttpClient()

    private lateinit var batimentService: BatimentService
    private lateinit var infrastructureService: InfrastructureService
    private lateinit var salleService: SalleService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance().load(requireContext(), requireActivity().getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = requireActivity().packageName

        // Initialisation du service Firebase
        batimentService = BatimentService()
        infrastructureService = InfrastructureService()
        salleService = SalleService()

        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(17.0)

        val defaultPoint = GeoPoint(6.1375, 1.2123)
        mapView.controller.setCenter(defaultPoint)

        // Ajouter une boussole
        val compassOverlay = CompassOverlay(requireActivity(), mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        // Ajouter une barre d'échelle
        val scaleBarOverlay = ScaleBarOverlay(mapView)
        scaleBarOverlay.setScaleBarOffset(130, 20) // Ajuster la position sur l'écran
        mapView.overlays.add(scaleBarOverlay)

        // Charger les bâtiments depuis Firebase et les afficher sur la carte
        loadBatiments()
        loadInfrastructures()
        loadSalles()

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            //Handler(Looper.getMainLooper()).postDelayed({ initLocationOverlay() }, 1000)
            view.post { initLocationOverlay() }
        }
    }

    private fun loadBatiments() {
        batimentService.getBatiments().observe(viewLifecycleOwner, Observer { batiments ->
            if (batiments != null) {
                for (batiment in batiments) {
                    if (batiment.latitude.isNotEmpty() && batiment.longitude.isNotEmpty()) {
                        try {
                            val lat = batiment.latitude.toDouble()
                            val lon = batiment.longitude.toDouble()
                            val position = GeoPoint(lat, lon)
                            addMarker(position, batiment.nom)
                        } catch (e: NumberFormatException) {
                            Log.e("MapsFragment", "Coordonnées invalides pour ${batiment.nom}")
                        }
                    }
                }
            }
        })
    }

    private fun loadInfrastructures() {
        infrastructureService.getInfrastructures().observe(viewLifecycleOwner, Observer { infrastructures ->
            if (infrastructures != null) {
                for (infrastructure in infrastructures) {
                    if (infrastructure.latitude.isNotEmpty() && infrastructure.longitude.isNotEmpty()) {
                        try {
                            val lat = infrastructure.latitude.toDouble()
                            val lon = infrastructure.longitude.toDouble()
                            val position = GeoPoint(lat, lon)
                            addMarker(position, infrastructure.nom)
                        } catch (e: NumberFormatException) {
                            Log.e("MapsFragment", "Coordonnées invalides pour ${infrastructure.nom}")
                        }
                    }
                }
            }
        })
    }

    private fun loadSalles() {
        salleService.getSalles().observe(viewLifecycleOwner, Observer { salles ->
            if (salles != null) {
                for (salle in salles) {
                    if (salle.latitude.isNotEmpty() && salle.longitude.isNotEmpty()) {
                        try {
                            val lat = salle.latitude.toDouble()
                            val lon = salle.longitude.toDouble()
                            val position = GeoPoint(lat, lon)
                            addMarker(position, salle.nom)
                        } catch (e: NumberFormatException) {
                            Log.e("MapsFragment", "Coordonnées invalides pour ${salle.nom}")
                        }
                    }
                }
            }
        })
    }

    private fun initLocationOverlay() {
        locationOverlay = MyLocationNewOverlay(mapView)
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)

        locationOverlay.runOnFirstFix {
            val userLocation = locationOverlay.myLocation
            if (isAdded) {
                requireActivity().runOnUiThread {
                    if (userLocation != null) {
                        mapView.controller.setCenter(userLocation)
                        addMarker(userLocation, "Ma position actuelle")

                        val destination =
                            GeoPoint(userLocation.latitude + 0.009, userLocation.longitude)
                        addMarker(destination, "Destination à 1 km")

                        getRoute(userLocation, destination)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Localisation non trouvée !",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    //mapView.invalidate()
                }
            }
        }
        mapView.overlays.add(locationOverlay)
    }

    private fun addMarker(position: GeoPoint, title: String) {
        val marker = Marker(mapView)
        marker.position = position
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title

        val drawable = resources.getDrawable(R.drawable.maps_and_flags, null)
        val bitmap = (drawable as android.graphics.drawable.BitmapDrawable).bitmap

        // Redimensionner l'image
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 40, 40, false) // Modifier la taille selon le besoin
        val resizedDrawable = android.graphics.drawable.BitmapDrawable(resources, scaledBitmap)

        marker.icon = resizedDrawable

        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    private fun getRoute(start: GeoPoint, end: GeoPoint) {
        val apiKey = "5b3ce3597851110001cf62480894b05967b24b268cf8fa5b6a5166f7"
        val url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=$apiKey&start=${start.longitude},${start.latitude}&end=${end.longitude},${end.latitude}"

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
                                polyline.color = resources.getColor(android.R.color.black, null)
                            }

                            for (i in 0 until coordinates.length()) {
                                val coord = coordinates.getJSONArray(i)
                                val lon = coord.getDouble(0)
                                val lat = coord.getDouble(1)
                                polyline.addPoint(GeoPoint(lat, lon))
                            }

                            if (isAdded) {
                                requireActivity().runOnUiThread {
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

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}