package tg.univlome.epl.utils

import org.osmdroid.util.GeoPoint
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
}