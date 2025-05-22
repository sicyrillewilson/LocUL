package tg.univlome.epl.utils

/**
 * Objet OthersUtils : Contient des fonctions utilitaires générales utilisées dans l'application.
 *
 * Description :
 * Cet objet regroupe des méthodes d'utilité courante ne relevant pas directement
 * d’un domaine spécifique comme les bâtiments ou les infrastructures.
 * Il est destiné à accueillir des fonctions de conversion, de formatage, ou d'aide au calcul.
 *
 * Fonctionnalité actuelle :
 *  - Conversion d’une distance exprimée en mètres vers un format lisible (mètres ou kilomètres)
 *
 * Ce type d’objet permet de centraliser les fonctions utilitaires partagées
 * par plusieurs composants de l’application.
 */
object OthersUtils {

    /**
     * Convertit une distance en mètres vers une représentation lisible :
     * - en mètres (m) si la valeur est inférieure à 1000
     * - en kilomètres (km) avec deux décimales sinon
     *
     * @param distance Distance en mètres à convertir.
     * @return Une chaîne formatée avec l’unité appropriée (par ex. "850.00 m" ou "1.25 km").
     */
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