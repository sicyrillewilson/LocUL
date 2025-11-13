package tg.univlome.epl.services

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.utils.BatimentUtils

/**
 * Service BatimentService : Récupération des données de bâtiments
 *
 * Description :
 * Classe responsable de la **gestion et récupération des bâtiments** depuis la base de données
 * Firestore. Elle propose une approche hybride : d’abord via un cache local (SharedPreferences), puis
 * via le cloud pour assurer des données à jour.
 * Cette classe a pour but de :
 * - Fournir une liste observable de bâtiments via `LiveData`.
 * - Réduire les temps de chargement grâce à la persistance locale.
 * - Assurer la synchronisation automatique des données avec Firebase Firestore.
 *
 * Composants :
 * - Firebase Firestore (Cloud)
 * - SharedPreferences (Stockage local via `BatimentUtils`)
 * - AndroidX LiveData
 *
 * Bibliothèques utilisées :
 * - Firebase Firestore
 * - AndroidX Lifecycle (LiveData)
 *
 * @param context Contexte Android utilisé pour l'accès au cache local
 *
 * @see tg.univlome.epl.models.Batiment
 * @see tg.univlome.epl.utils.BatimentUtils
 */
class BatimentService(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val batimentsCollection = db.collection("batiments")

    /**
     * Récupère la liste des bâtiments de manière asynchrone depuis Firestore ou localement.
     *
     * @return LiveData contenant une liste de [Batiment]
     */
    fun getBatiments(): LiveData<List<Batiment>> {
        val batimentsLiveData = MutableLiveData<List<Batiment>>()

        val loadBatiments = BatimentUtils.loadBatiments(context)
        if (loadBatiments != null) {
            batimentsLiveData.value = loadBatiments!!
            batimentsCollection
                .orderBy("nom") // tri par le champ "nom"
                .get()
                .addOnSuccessListener { result ->
                    val batimentsList = mutableListOf<Batiment>()
                    for (document in result) {
                        batimentsList.add(createBatimentFromDocument(document))
                    }
                    BatimentUtils.saveBatiments(context, batimentsList)
                }
                .addOnFailureListener { exception ->
                    Log.e(
                        "BatimentService",
                        "Erreur lors de la récupération des bâtiments",
                        exception
                    )
                }
        } else {
            batimentsCollection
                .orderBy("nom") // tri par le champ "nom"
                .get()
                .addOnSuccessListener { result ->
                    val batimentsList = mutableListOf<Batiment>()
                    for (document in result) {
                        batimentsList.add(createBatimentFromDocument(document))
                    }
                    BatimentUtils.saveBatiments(context, batimentsList)

                    batimentsLiveData.value = batimentsList
                }
                .addOnFailureListener { exception ->
                    Log.e(
                        "BatimentService",
                        "Erreur lors de la récupération des bâtiments",
                        exception
                    )
                }
        }

        return batimentsLiveData
    }

    /**
     * Transforme un document Firestore en instance de [Batiment].
     *
     * @param document Le document Firestore contenant les données du bâtiment
     * @return Une instance de [Batiment] construite à partir des données du document
     */
    private fun createBatimentFromDocument(document: DocumentSnapshot): Batiment {
        val id = document.id
        val nom = document.getString("nom") ?: ""
        val description = document.getString("description") ?: ""
        val longitude = document.getString("longitude") ?: ""
        val latitude = document.getString("latitude") ?: ""
        val images = document.get("images") as? List<String> ?: emptyList()
        val image = images.firstOrNull() ?: ""
        val situation = document.getString("situation") ?: ""
        val type = document.getString("type") ?: ""

        return Batiment(id, nom, description, longitude, latitude, image, situation, type, images)
    }
}
