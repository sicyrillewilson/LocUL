package tg.univlome.epl.models

import java.io.Serializable

data class Infrastructure(
    val extraProp: String = "" // uniquement les ajouts spécifiques
) : Lieu(), Serializable {

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