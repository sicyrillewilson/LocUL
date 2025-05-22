package tg.univlome.epl.models

import java.io.Serializable

/**
 * Modèle de données Infrastructure : Représentation d’une infrastructure présente sur le campus
 *
 * Description :
 * Cette classe hérite de [Lieu] et décrit une infrastructure physique telle qu’un parking, une cafétéria,
 * un laboratoire ou tout autre équipement important. Elle contient les informations nécessaires à son affichage,
 * sa localisation, et son identification.
 *
 * Hérite de :
 * - [Lieu] : modèle commun regroupant les propriétés génériques comme l’identifiant, le nom, les coordonnées, etc.
 *
 * Champs supplémentaires :
 * @property extraProp Propriété personnalisée permettant d’ajouter des métadonnées spécifiques à une infrastructure
 *
 * Constructeur secondaire :
 * Permet l’instanciation complète de l’objet avec tous les attributs pertinents pour l’affichage ou la géolocalisation.
 *
 * Utilisation :
 * - Gestion et affichage via [tg.univlome.epl.adapter.InfraAdapter], [tg.univlome.epl.adapter.InfraFragmentAdapter]
 * - Chargement depuis Firestore via [tg.univlome.epl.services.InfrastructureService]
 * - Consultation détaillée dans [tg.univlome.epl.ui.infrastructure.InfraActivity]
 *
 * @see tg.univlome.epl.models.Lieu
 * @see tg.univlome.epl.services.InfrastructureService
 * @see tg.univlome.epl.adapter.InfraAdapter
 * @see tg.univlome.epl.ui.infrastructure.InfraActivity
 */
data class Infrastructure(
    val extraProp: String = "" // uniquement les ajouts spécifiques
) : Lieu(), Serializable {

    /**
     * Constructeur secondaire permettant d’initialiser une infrastructure avec toutes ses propriétés.
     *
     * @param id Identifiant unique de l’infrastructure
     * @param nom Nom de l’infrastructure
     * @param description Description de l’infrastructure
     * @param longitude Coordonnée GPS - longitude
     * @param latitude Coordonnée GPS - latitude
     * @param image Image principale de l’infrastructure
     * @param situation Position ou zone (Nord, Sud...) sur le campus
     * @param type Type ou catégorie de l’infrastructure
     * @param images Liste des images supplémentaires
     * @param distance Distance depuis l'utilisateur (calculée dynamiquement)
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