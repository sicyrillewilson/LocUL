package tg.univlome.epl.models

import java.io.Serializable

data class Infrastructure(
    var id: String,
    var nom: String,
    var description: String,
    var longitude: String,
    var latitude: String,
    var image: String
) : Serializable