package tg.univlome.epl.models

import java.io.Serializable

/**
 * Classe Salle : Représentation d'une salle physique sur le campus universitaire.
 *
 * Description :
 * Cette classe hérite de [Lieu] et représente une salle d’enseignement, de réunion ou de laboratoire.
 * Elle contient des informations spécifiques telles que sa capacité d’accueil et l’identifiant du bâtiment
 * auquel elle appartient.
 *
 * Composants spécifiques :
 * @property infrastructureId Identifiant du bâtiment auquel cette salle est rattachée (clé étrangère)
 * @property capacite Capacité d’accueil de la salle (ex : "40", "100", "15")
 *
 * Hérite de :
 * - [Lieu] : fournit les coordonnées, nom, description, image principale, type et autres attributs communs
 *
 * Constructeur secondaire :
 * Permet d’instancier une salle avec toutes ses propriétés héritées et spécifiques
 *
 * Utilisation :
 * - Affichée dans les listes ou détails via les adaptateurs comme [tg.univlome.epl.adapter.SalleAdapter]
 * - Consultée dans [tg.univlome.epl.ui.home.SalleActivity] pour les détails
 * - Chargée dynamiquement via [tg.univlome.epl.services.SalleService]
 *
 * @see Lieu
 * @see Batiment
 * @see Infrastructure
 * @see tg.univlome.epl.services.SalleService
 * @see tg.univlome.epl.ui.home.SalleActivity
 */
data class Salle(
    var infrastructureId: String = "",
    var capacite: String = ""
) : Lieu(), Serializable {

    /**
     * Constructeur secondaire pour instancier une salle avec toutes ses propriétés.
     *
     * @param id Identifiant unique de la salle
     * @param infrastructureId ID du bâtiment parent
     * @param nom Nom de la salle
     * @param description Description textuelle
     * @param capacite Capacité d'accueil
     * @param longitude Coordonnée longitude
     * @param latitude Coordonnée latitude
     * @param image URL de l’image principale
     * @param situation Zone ou localisation (Nord/Sud)
     * @param type Type ou fonction de la salle
     * @param images Liste d’images supplémentaires
     * @param distance Distance par rapport à l'utilisateur
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