package tg.univlome.epl.services

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import tg.univlome.epl.models.Admin

/**
 * Service AdminService : Récupération des données d’administration
 *
 * Description :
 * Service dédié à la **récupération des informations d’authentification des administrateurs** à partir de Firestore.
 * Il permet de centraliser la gestion des comptes admin dans l'application.
 * Cette classe a donc pour but de :
 * - Accéder à la collection `admins` de Firebase Firestore.
 * - Fournir les données des administrateurs à l’application sous forme de `LiveData`.
 *
 * Composants :
 * - Firebase Firestore
 * - AndroidX LiveData
 *
 * Bibliothèques utilisées :
 * - Firebase Firestore
 * - AndroidX Lifecycle (LiveData)
 *
 * @constructor Initialise la connexion à Firestore pour accéder à la collection `admins`
 *
 * @see tg.univlome.epl.models.Admin
 */
class AdminService {
    private val db = FirebaseFirestore.getInstance()
    private val adminsCollection = db.collection("admins")

    /**
     * Récupère la liste des administrateurs depuis Firestore.
     *
     * @return LiveData contenant une liste de [Admin]
     */
    fun getAdmins(): LiveData<List<Admin>> {
        val adminsLiveData = MutableLiveData<List<Admin>>()

        adminsCollection.get()
            .addOnSuccessListener { result ->
                val adminsList = mutableListOf<Admin>()
                for (document in result) {
                    adminsList.add(createAdminFromDocument(document))
                }
                adminsLiveData.value = adminsList
            }
            .addOnFailureListener { exception ->
                Log.e("AdminService", "Erreur lors de la récupération des admins", exception)
            }

        return adminsLiveData
    }

    /**
     * Construit un objet [Admin] à partir d’un document Firestore.
     *
     * @param document Le document Firestore contenant les informations de l’administrateur
     * @return Une instance de [Admin] avec les champs `username` et `password`
     */
    private fun createAdminFromDocument(document: DocumentSnapshot): Admin {
        val username = document.getString("username") ?: ""
        val password = document.getString("password") ?: ""
        return Admin(username, password)
    }
}
