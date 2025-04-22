@file:Suppress("DEPRECATION")

package tg.univlome.epl.utils

import android.Manifest
import android.app.Activity
import org.osmdroid.util.GeoPoint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import tg.univlome.epl.R
import java.io.IOException
import org.osmdroid.util.BoundingBox
import kotlin.math.*

object MapsUtils {

    private val client = OkHttpClient()
    private val iconCache = mutableMapOf<Int, BitmapDrawable>()

    fun calculateDistance(start: GeoPoint, end: GeoPoint): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0].toDouble() // Retourne la distance en mètres
    }

    fun saveUserLocation(context: Context, location: GeoPoint) {
        val sharedPreferences = context.getSharedPreferences("UserLocationPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(location)
        editor.putString("userLocation", json)
    }

    fun loadUserLocation(context: Context): GeoPoint {
        val sharedPreferences = context.getSharedPreferences("UserLocationPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("userLocation", null)
        val type = object : TypeToken<GeoPoint>() {}.type
        return gson.fromJson(json, type) ?: GeoPoint(0.0, 0.0)
    }

    fun saveDestination(context: Context, destination: GeoPoint) {
        val sharedPreferences = context.getSharedPreferences("DestinationPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(destination)
        editor.putString("destination", json)
        editor.apply()
    }


    fun loadDestination(context: Context): GeoPoint {
        val sharedPreferences = context.getSharedPreferences("DestinationPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("destination", null)
        val type = object : TypeToken<GeoPoint>() {}.type
        return gson.fromJson(json, type) ?: GeoPoint(0.0, 0.0)
    }

    fun clearDestination(context: Context) {
        val sharedPreferences = context.getSharedPreferences("DestinationPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("destination")
        editor.apply()
    }

    fun saveMarkerList(context: Context, markerList: MutableList<Marker>) {
        val sharedPreferences = context.getSharedPreferences("MarkerListPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(markerList)
        editor.putString("markerList", json)
        editor.apply()
    }

    fun loadMarkerList(context: Context): MutableList<Marker> {
        val sharedPreferences = context.getSharedPreferences("MarkerListPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("markerList", null)
        val type = object : TypeToken<MutableList<Marker>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun clearMarkerList(context: Context) {
        val sharedPreferences = context.getSharedPreferences("MarkerListPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("markerList")
        editor.apply()
    }

    fun saveMapState(context: Context, latitude: Double, longitude: Double, zoom: Float, isNightMode: Boolean) {

    }

    fun resizeIcon(icon: Int = R.drawable.default_marker, resources: Resources): BitmapDrawable? {
        return iconCache[icon] ?: run {
            val drawable = ResourcesCompat.getDrawable(resources, icon, null) ?: return null
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 75, 75, false)
            val scaledDrawable = BitmapDrawable(resources, scaledBitmap)
            iconCache[icon] = scaledDrawable
            scaledDrawable
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun setMiniMap(miniMap: MapView, userLocation: GeoPoint = GeoPoint(0, 0), destination: GeoPoint = GeoPoint(0, 0), context: Context, activity: Activity, resources: Resources) {
        var start = userLocation
        var end = destination
        // Si la localisation utilisateur n'est pas valide, utiliser la destination comme point de départ
        if (start.latitude == 0.0 && start.longitude == 0.0) {
            start = end
        }
        miniMap.setTileSource(TileSourceFactory.MAPNIK)
        miniMap.setMultiTouchControls(false)
        // Rendre la carte totalement statique
        miniMap.setBuiltInZoomControls(false) // désactiver les boutons zoom
        miniMap.isTilesScaledToDpi = false

        val latPadding = 0.001
        val lonPadding = 0.001
        val boundingBox = BoundingBox(
            maxOf(start.latitude, end.latitude) + latPadding,
            maxOf(start.longitude, end.longitude) + lonPadding,
            minOf(start.latitude, end.latitude) - latPadding,
            minOf(start.longitude, end.longitude) - lonPadding
        )

        // Ajuster la vue pour montrer les deux points
        miniMap.post {
            miniMap.zoomToBoundingBox(boundingBox, false, 50, 14.0, 14.0.toLong())


            // Marqueur de départ
            val startMarker = Marker(miniMap).apply {
                position = start
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = resizeIcon(R.drawable.maps_and_flags, resources)
            }

            // Marqueur de destination
            val endMarker = Marker(miniMap).apply {
                position = end
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = resizeIcon(R.drawable.destination, resources)
            }

            miniMap.overlays.clear()
            miniMap.overlays.add(startMarker)
            miniMap.overlays.add(endMarker)

            // Désactiver le toucher utilisateur
            miniMap.isFocusable = false
            miniMap.isEnabled = false

            // Bloquer tous les gestes de l'utilisateur
            val touchInterceptor = object : Overlay() {
                override fun onTouchEvent(event: MotionEvent?, mapView: MapView?): Boolean {
                    return true // bloque tous les touch events
                }
            }
            miniMap.overlays.add(touchInterceptor)

            // Obtenir l'itinéraire
            getRoute(start, end, activity, miniMap)

            miniMap.invalidate()
        }
    }

    private fun getRoute(start: GeoPoint, end: GeoPoint, activity: Activity, mapView: MapView) {
        val apiKey = "5b3ce3597851110001cf62480894b05967b24b268cf8fa5b6a5166f7"
        val url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=$apiKey&start=${start.longitude},${start.latitude}&end=${end.longitude},${end.latitude}"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MapsActivity", "Erreur lors de la récupération de l'itinéraire : ${e.message}")
                activity.runOnUiThread {
                    Toast.makeText(activity, "Impossible de récupérer l'itinéraire", Toast.LENGTH_LONG).show()
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

                                activity.runOnUiThread {
                                    var currentPolyline = Polyline()
                                    currentPolyline!!.setPoints(geoPoints)
                                    currentPolyline!!.outlinePaint.color = ContextCompat.getColor(activity, R.color.mainColor)
                                    currentPolyline!!.outlinePaint.strokeWidth = 5f

                                    mapView.overlays.add(currentPolyline)
                                    mapView.invalidate()
                                }
                            }
                        } catch (e: JSONException) {
                            Log.e("MapsUtils", "Erreur JSON: ${e.message}")
                        }
                    }
                } else {
                    Log.e("MapsUtils", "Erreur dans la réponse de l'API de routage")
                }
            }
        })
    }
}