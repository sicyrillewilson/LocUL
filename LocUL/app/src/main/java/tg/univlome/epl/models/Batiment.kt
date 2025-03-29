package tg.univlome.epl.models

import java.io.Serializable

data class Batiment(
    override var id: String,
    override var nom: String,
    override var description: String,
    override var longitude: String,
    override var latitude: String,
    override var image: String
) : Lieu(id, nom, description, longitude, latitude, image), Serializable
