package tg.univlome.epl.models

import tg.univlome.epl.R
import java.io.Serializable

data class Batiment(
    override var id: String,
    override var nom: String,
    override var description: String,
    override var longitude: String,
    override var latitude: String,
    override var image: String,
    override var situation: String = "",
    override var distance: String = "",
    override val type: String = "",
    override val icon: Int  = R.drawable.img
) : Lieu(id, nom, description, longitude, latitude, image, situation, distance, type, icon), Serializable
