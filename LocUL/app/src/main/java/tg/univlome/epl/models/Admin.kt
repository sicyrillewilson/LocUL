package tg.univlome.epl.models

import java.io.Serializable

data class Admin(
    var nom: String,
    var motDePasse: String
) : Serializable
