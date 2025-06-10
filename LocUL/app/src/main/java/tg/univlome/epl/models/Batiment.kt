package tg.univlome.epl.models

import java.io.Serializable

/**
 * Modèle de données Batiment : Représentation d’un bâtiment géolocalisable sur le campus
 *
 * Description :
 * Ce modèle hérite de la classe [Lieu] et représente un bâtiment spécifique au sein de l’université.
 * Il contient des informations détaillées sur son emplacement, ses images et sa typologie.
 *
 * Hérite de :
 * - [Lieu] : modèle de base contenant les propriétés communes (id, nom, description, coordonnées, etc.)
 *
 * Champs supplémentaires :
 * @property extraProp Propriété étendue (réservée à de futurs besoins ou marquages spécifiques)
 *
 * Constructeur secondaire :
 * Permet de créer un bâtiment avec toutes ses propriétés en une seule instanciation.
 *
 * Utilisation :
 * - Représentation dans les interfaces utilisateurs : [tg.univlome.epl.adapter.BatimentAdapter], [tg.univlome.epl.adapter.BatimentFragmentAdapter]
 * - Chargement depuis Firebase via [tg.univlome.epl.services.BatimentService]
 * - Affichage détaillé dans [tg.univlome.epl.ui.batiment.BatimentActivity]
 *
 * @see tg.univlome.epl.models.Lieu
 * @see tg.univlome.epl.services.BatimentService
 * @see tg.univlome.epl.adapter.BatimentAdapter
 * @see tg.univlome.epl.ui.batiment.BatimentActivity
 */
data class Batiment(
    val extraProp: String = "" // uniquement les ajouts spécifiques
) : Lieu(), Serializable {

    /**
     * Constructeur secondaire pour initialiser un bâtiment avec toutes ses propriétés.
     *
     * @param id Identifiant unique du bâtiment
     * @param nom Nom du bâtiment
     * @param description Description détaillée
     * @param longitude Coordonnée GPS - longitude
     * @param latitude Coordonnée GPS - latitude
     * @param image URL de l’image principale
     * @param situation Zone ou localisation dans le campus (ex : Nord, Sud)
     * @param type Type du bâtiment (ex : enseignement, administratif)
     * @param images Liste des images secondaires
     * @param distance Distance calculée depuis la position de l’utilisateur
     */
    constructor(
        id: String,
        nom: String,
        description: String,
        longitude: String,
        latitude: String,
        image: String,
        situation: String,
        type: String,
        images: List<String> = emptyList(),
        distance: String = ""
    ) : this("") {
        this.id = id
        this.nom = nom
        this.description = description
        this.longitude = longitude
        this.latitude = latitude
        this.image = image
        this.situation = situation
        this.type = type
        this.images = images
        this.distance = distance
    }
}