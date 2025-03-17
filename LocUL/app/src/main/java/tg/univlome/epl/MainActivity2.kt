package tg.univlome.epl

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.Marker
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.views.overlay.Polyline
import java.io.IOException

class MainActivity2 : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_main2)

        mapView = findViewById(R.id.mapView2)
        mapView.setTileSource(TileSourceFactory.MAPNIK) // Chargement de la carte OSM
        mapView.setMultiTouchControls(true) // Zoom avec les doigts
        mapView.controller.setZoom(17.0)

        // Définir une position par défaut avant de récupérer la localisation
        val defaultPoint = GeoPoint(6.1375, 1.2123)
        mapView.controller.setCenter(defaultPoint)

        /*// Centrage sur une position (Lomé par exemple)
        val startPoint = GeoPoint(6.1375, 1.2123)
        val mapController = mapView.controller
        mapController.setZoom(17.0)
        mapController.setCenter(startPoint)*/

        // Ajouter une boussole
        val compassOverlay = CompassOverlay(this, mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        // Ajouter une barre d'échelle
        val scaleBarOverlay = ScaleBarOverlay(mapView)
        scaleBarOverlay.setScaleBarOffset(130, 20) // Ajuster la position sur l'écran
        mapView.overlays.add(scaleBarOverlay)

        // Ajouter l'affichage de la position actuelle
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            /*val locationOverlay = MyLocationNewOverlay(mapView)
            locationOverlay.enableMyLocation()
            mapView.overlays.add(locationOverlay)*/
            //initLocationOverlay()
            Handler(Looper.getMainLooper()).postDelayed({
                initLocationOverlay()
            }, 1000) // Attente pour éviter le problème d'affichage
        }
    }

    private fun initLocationOverlay() {
        locationOverlay = MyLocationNewOverlay(mapView)
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)

        locationOverlay.runOnFirstFix {
            val userLocation = locationOverlay.myLocation
            runOnUiThread {
                if (userLocation != null) {
                    mapView.controller.setCenter(userLocation)
                    addMarker(userLocation, "Ma position actuelle")

                    // Calcul du point à 1 km vers le nord
                    val destination = GeoPoint(userLocation.latitude + 0.009, userLocation.longitude)
                    addMarker(destination, "Destination à 1 km")

                    getRoute(userLocation, destination)
                } else {
                    Toast.makeText(this, "Localisation non trouvée !", Toast.LENGTH_LONG).show()
                }
                mapView.invalidate() // Forcer l'affichage de la carte
            }
        }
        mapView.overlays.add(locationOverlay)
    }

    private fun addMarker(position: GeoPoint, title: String) {
        val marker = Marker(mapView)
        marker.position = position
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        mapView.overlays.add(marker)
    }

    private fun addMarker(position: GeoPoint) {
        val marker = Marker(mapView)
        marker.position = position
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Ma position actuelle"
        mapView.overlays.add(marker)
    }

    private fun getRoute(start: GeoPoint, end: GeoPoint) {
        val apiKey = "5b3ce3597851110001cf62480894b05967b24b268cf8fa5b6a5166f7" // Remplace par ta clé OpenRouteService
        val url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=$apiKey&start=${start.longitude},${start.latitude}&end=${end.longitude},${end.latitude}"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity2, "Erreur réseau !", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonResponse ->
                    try {
                        val jsonObject = JSONObject(jsonResponse)
                        // Vérifier si la réponse contient bien les données géométriques
                        if (jsonObject.has("features")) {
                            val features = jsonObject.getJSONArray("features")
                            val geometry = features.getJSONObject(0).getJSONObject("geometry")
                            val coordinates = geometry.getJSONArray("coordinates")

                            val polyline = Polyline()
                            polyline.color = resources.getColor(android.R.color.black, theme)

                            // Ajouter les points de la route dans le Polyline
                            for (i in 0 until coordinates.length()) {
                                val coord = coordinates.getJSONArray(i)
                                val lon = coord.getDouble(0)
                                val lat = coord.getDouble(1)
                                polyline.addPoint(GeoPoint(lat, lon))
                            }

                            // Afficher la polyline sur la carte
                            runOnUiThread {
                                mapView.overlays.add(polyline)
                                mapView.invalidate() // Forcer l'affichage de la carte
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
        mapView.onResume() // Reprendre la carte quand l'activité est active
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause() // Arrêter la carte pour économiser des ressources
    }
}

/*class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}*/