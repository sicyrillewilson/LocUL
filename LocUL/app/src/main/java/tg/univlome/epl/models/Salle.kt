package tg.univlome.epl.models

import java.io.Serializable

data class Salle(
    var id: String,
    var infrastructureId: String,
    var nom: String,
    var description: String,
    var capacite: String,
    var longitude: String,
    var latitude: String,
    var image: String
) : Serializable
