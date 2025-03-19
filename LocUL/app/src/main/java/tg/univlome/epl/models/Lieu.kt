package tg.univlome.epl.models

import java.io.Serializable

// Superclasse Lieu
open class Lieu(
    open var id: String,
    open var nom: String,
    open var description: String,
    open var longitude: String,
    open var latitude: String,
    open var image: String
) : Serializable