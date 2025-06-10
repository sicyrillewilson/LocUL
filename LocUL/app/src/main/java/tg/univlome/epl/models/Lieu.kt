package tg.univlome.epl.models

import tg.univlome.epl.R
import java.io.Serializable

/**
 * Classe abstraite Lieu : Représentation générique d’un emplacement géographique sur le campus
 *
 * Description :
 * Cette classe représente la base commune à tous les éléments géolocalisables de l’application
 * tels que les bâtiments, les infrastructures ou les salles. Elle regroupe les propriétés nécessaires
 * à l'affichage, à la localisation GPS et à la catégorisation.
 *
 * Propriétés principales :
 * @property id Identifiant unique du lieu (généré par Firebase)
 * @property nom Nom du lieu (ex : "Bibliothèque", "Amphi A")
 * @property description Description textuelle du lieu
 * @property longitude Coordonnée GPS - longitude (sous forme de chaîne)
 * @property latitude Coordonnée GPS - latitude (sous forme de chaîne)
 * @property image URL de l’image principale du lieu
 * @property situation Emplacement ou zone (Nord, Sud) sur le campus
 * @property type Catégorie du lieu (enseignement, administratif, détente, etc.)
 * @property images Liste d’URLs vers des images secondaires ou supplémentaires
 * @property distance Distance entre l’utilisateur et ce lieu (calculée dynamiquement)
 * @property icon Ressource par défaut à afficher si aucune image n’est disponible (valeur par défaut : [R.drawable.img])
 *
 * Utilisation :
 * - Superclasse héritée par : [Batiment], [Infrastructure], [Salle]
 * - Affichage et traitement via des adaptateurs spécialisés
 * - Exploitation dans les vues de détail (ex : [tg.univlome.epl.ui.batiment.BatimentActivity])
 *
 * @see Batiment
 * @see Infrastructure
 * @see Salle
 */
open class Lieu(
    open var id: String = "",
    open var nom: String = "",
    open var description: String = "",
    open var longitude: String = "",
    open var latitude: String = "",
    open var image: String = "",
    open var situation: String = "",
    open var type: String = "",
    open var images: List<String> = emptyList(),
    open var distance: String = "",
    open val icon: Int = R.drawable.img
) : Serializable