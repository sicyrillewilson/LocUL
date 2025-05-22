package tg.univlome.epl.services

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.utils.InfraUtils

/**
 * Service InfrastructureService : Récupération des données d'infrastructures
 *
 * Description :
 * Cette classe fournit un service permettant de **charger les données d'infrastructures** depuis
 * Firebase Firestore et de les mettre en cache localement via `SharedPreferences`.
 * Cette a pour but de :
 * - Fournir une liste observable d'objets [Infrastructure] via `LiveData`.
 * - Optimiser le temps de chargement avec un système de cache (via `InfraUtils`).
 * - Assurer la synchronisation des données entre Firestore et le cache.
 *
 * Composants :
 * - Firebase Firestore
 * - SharedPreferences (via `InfraUtils`)
 * - AndroidX LiveData
 *
 * Bibliothèques utilisées :
 * - Firebase Firestore
 * - AndroidX Lifecycle (LiveData)
 *
 * @param context Contexte Android utilisé pour accéder au système de stockage local
 *
 * @see tg.univlome.epl.models.Infrastructure
 * @see tg.univlome.epl.utils.InfraUtils
 */
class InfrastructureService(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val infrastructuresCollection = db.collection("infrastructures")

    /**
     * Récupère la liste des infrastructures depuis Firestore.
     * Utilise le cache local s’il est disponible, et met à jour en arrière-plan.
     *
     * @return LiveData contenant la liste des infrastructures
     */
    fun getInfrastructures(): LiveData<List<Infrastructure>> {
        val infrastructuresLiveData = MutableLiveData<List<Infrastructure>>()

        val loadInfrastructures = InfraUtils.loadInfras(context)
        if (loadInfrastructures != null) {
            infrastructuresLiveData.value = loadInfrastructures!!
            infrastructuresCollection.get()
                .addOnSuccessListener { result ->
                    val infrastructuresList = mutableListOf<Infrastructure>()
                    for (document in result) {
                        infrastructuresList.add(createInfrastructureFromDocument(document))
                    }
                    InfraUtils.saveInfras(context, infrastructuresList)
                }
                .addOnFailureListener { exception ->
                    Log.e(
                        "InfrastructureService",
                        "Erreur lors de la récupération des infrastructures",
                        exception
                    )
                }
        } else {
            infrastructuresCollection.get()
                .addOnSuccessListener { result ->
                    val infrastructuresList = mutableListOf<Infrastructure>()
                    for (document in result) {
                        infrastructuresList.add(createInfrastructureFromDocument(document))
                    }
                    InfraUtils.saveInfras(context, infrastructuresList)
                    infrastructuresLiveData.value = infrastructuresList
                }
                .addOnFailureListener { exception ->
                    Log.e(
                        "InfrastructureService",
                        "Erreur lors de la récupération des infrastructures",
                        exception
                    )
                }
        }

        return infrastructuresLiveData
    }

    /**
     * Convertit un document Firestore en objet [Infrastructure].
     *
     * @param document Document Firestore contenant les données de l'infrastructure
     * @return Une instance d’[Infrastructure] initialisée
     */
    private fun createInfrastructureFromDocument(document: DocumentSnapshot): Infrastructure {
        val id = document.id
        val nom = document.getString("nom") ?: ""
        val description = document.getString("description") ?: ""
        val longitude = document.getString("longitude") ?: ""
        val latitude = document.getString("latitude") ?: ""
        val images = document.get("images") as? List<String> ?: emptyList()
        val image = images.firstOrNull() ?: ""
        val situation = document.getString("situation") ?: ""
        val type = document.getString("type") ?: ""

        return Infrastructure(
            id,
            nom,
            description,
            longitude,
            latitude,
            image,
            situation,
            type,
            images
        )
    }
}
