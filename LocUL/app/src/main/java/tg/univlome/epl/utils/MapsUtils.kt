@file:Suppress("DEPRECATION")

package tg.univlome.epl.utils

import org.osmdroid.util.GeoPoint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import tg.univlome.epl.R

object MapsUtils {
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

    fun setMiniMap(miniMap: MapView, context: Context){
        miniMap.setTileSource(TileSourceFactory.MAPNIK)
        miniMap.setMultiTouchControls(false)
        // Rendre la carte totalement statique
        miniMap.setBuiltInZoomControls(false) // désactiver les boutons zoom
        miniMap.isTilesScaledToDpi = false
        miniMap.controller.setZoom(14.0)
        miniMap.controller.setCenter(GeoPoint(6.1935, 1.2087)) // ou une autre coordonnée

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

        val marker = Marker(miniMap).apply {
            position = GeoPoint(6.1935, 1.2087)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(context, R.drawable.maps_and_flags)
        }
        miniMap.overlays.add(marker)
        miniMap.invalidate()
    }
}