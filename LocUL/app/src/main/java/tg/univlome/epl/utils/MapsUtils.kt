package tg.univlome.epl.utils

import org.osmdroid.util.GeoPoint

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
}