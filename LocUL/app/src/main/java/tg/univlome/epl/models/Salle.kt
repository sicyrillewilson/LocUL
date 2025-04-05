package tg.univlome.epl.models

import tg.univlome.epl.R
import java.io.Serializable

data class Salle(
    override var id: String,
    var infrastructureId: String,
    override var nom: String,
    override var description: String,
    var capacite: String,
    override var longitude: String,
    override var latitude: String,
    override var image: String,
    override var situation: String = "",
    override val type: String = "",
    override var images: List<String> = emptyList(),
    override var distance: String = "",
    override val icon: Int  = R.drawable.img
) : Lieu(id, nom, description, longitude, latitude, image, situation, type, images, distance, icon), Serializable