package tg.univlome.epl.utils

object OthersUtils {
    fun convertDistance(distance: Double): String {
        // Conversion en km si la distance dépasse 1000 m
        val formattedDistance = if (distance >= 1000) {
            String.format("%.2f km", distance / 1000)
        } else {
            String.format("%.2f m", distance)
        }
        return formattedDistance
    }
}