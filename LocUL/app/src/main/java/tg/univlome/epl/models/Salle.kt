package tg.univlome.epl.models

import java.io.Serializable

data class Salle(
    override var id: String,
    var infrastructureId: String,
    override var nom: String,
    override var description: String,
    var capacite: String,
    override var longitude: String,
    override var latitude: String,
    override var image: String
) : Lieu(id, nom, description, longitude, latitude, image), Serializable
