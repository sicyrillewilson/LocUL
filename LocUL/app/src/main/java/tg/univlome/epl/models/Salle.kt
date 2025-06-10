package tg.univlome.epl.models

import java.io.Serializable


/**
 * Modèle de données Salle : Représentation d’une salle géolocalisable sur le campus
 *
 * Description :
 * Ce modèle hérite de la classe [Lieu] et représente une salle spécifique appartenant à une infrastructure.
 * Il contient des informations précises comme la capacité d’accueil et son rattachement à un bâtiment ou une autre entité.
 *
 * Hérite de :
 * - [Lieu] : modèle de base contenant les propriétés communes (id, nom, description, coordonnées, etc.)
 *
 * Champs supplémentaires :
 * @property infrastructureId Identifiant de l’infrastructure parente (ex : bâtiment)
 * @property capacite Capacité d’accueil de la salle
 *
 * Constructeur secondaire :
 * Permet de créer une salle avec toutes ses propriétés en une seule instanciation.
 *
 * Utilisation :
 * - Affichage dans [tg.univlome.epl.adapter.SalleAdapter], [tg.univlome.epl.adapter.SalleViewAllAdapter]
 * - Récupération des données depuis Firebase via [tg.univlome.epl.services.SalleService]
 * - Visualisation détaillée dans [tg.univlome.epl.ui.home.SalleActivity]
 *
 * @see tg.univlome.epl.models.Lieu
 * @see tg.univlome.epl.services.SalleService
 * @see tg.univlome.epl.adapter.SalleAdapter
 * @see tg.univlome.epl.ui.home.SalleActivity
 */
data class Salle(
    var infrastructureId: String = "",
    var capacite: String = ""
) : Lieu(), Serializable {

    /**
     * Constructeur secondaire pour initialiser une salle avec toutes ses propriétés.
     *
     * @param id Identifiant unique de la salle
     * @param infrastructureId Identifiant de l’infrastructure parente (bâtiment, etc.)
     * @param nom Nom de la salle
     * @param description Description détaillée
     * @param capacite Capacité d’accueil de la salle
     * @param longitude Coordonnée GPS - longitude
     * @param latitude Coordonnée GPS - latitude
     * @param image URL de l’image principale
     * @param situation Zone ou localisation dans le campus (ex : Bloc A, étage 1)
     * @param type Type de la salle (ex : cours, labo)
     * @param images Liste des images secondaires
     * @param distance Distance calculée depuis la position de l’utilisateur
     */
    constructor(
        id: String,
        infrastructureId: String,
        nom: String,
        description: String,
        capacite: String,
        longitude: String,
        latitude: String,
        image: String,
        situation: String,
        type: String,
        images: List<String> = emptyList(),
        distance: String = ""
    ) : this(infrastructureId, capacite) {
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