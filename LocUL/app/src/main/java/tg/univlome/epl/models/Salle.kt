package tg.univlome.epl.models

import java.io.Serializable

data class Salle(
    var infrastructureId: String = "",
    var capacite: String = ""
) : Lieu(), Serializable {

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