package tg.univlome.epl.models

import java.io.Serializable

/**
 * Modèle de données Admin : Représentation d’un administrateur de l’application
 *
 * Description :
 * Ce modèle représente les informations d’identification d’un administrateur utilisé
 * pour l’authentification ou la gestion sécurisée dans l’application.
 *
 * Champs :
 * @property nom le nom d’utilisateur ou identifiant unique de l’administrateur
 * @property motDePasse le mot de passe associé à cet administrateur
 *
 * Utilisation :
 * - Employé dans les opérations d’authentification (via Firebase ou locale)
 * - Récupéré via le service [tg.univlome.epl.services.AdminService]
 *
 * Interfaces :
 * - [Serializable] : permet de transférer l’objet entre activités ou fragments via `Intent`
 *
 * @see tg.univlome.epl.services.AdminService
 */
data class Admin(
    var nom: String,
    var motDePasse: String
) : Serializable
