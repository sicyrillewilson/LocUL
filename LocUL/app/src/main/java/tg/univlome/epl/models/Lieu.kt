package tg.univlome.epl.models

import tg.univlome.epl.R
import java.io.Serializable

// Superclasse Lieu
open class Lieu(
    open var id: String,
    open var nom: String,
    open var description: String,
    open var longitude: String,
    open var latitude: String,
    open var image: String,
    open var situation: String = "",
    open val type: String = "",
    open var images: List<String> = emptyList(),
    open var distance: String = "",
    open val icon: Int  = R.drawable.img
) : Serializable