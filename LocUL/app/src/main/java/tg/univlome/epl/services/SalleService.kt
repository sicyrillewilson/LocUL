package tg.univlome.epl.services

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import tg.univlome.epl.models.Salle
import tg.univlome.epl.utils.SalleUtils

/**
 * Service SalleService : Récupération des données des salles
 *
 * Description :
 * Cette classe gère la **récupération et la mise en cache des données des salles** depuis Firebase Firestore.
 * Elle assure un chargement rapide grâce à un cache local (`SharedPreferences`) et une mise à jour
 * asynchrone en arrière-plan avec les données plus fraîches depuis Firestore.
 * Cette classe a donc pour but de :
 * - Lire les données des salles depuis Firestore.
 * - Associer chaque salle à son bâtiment via `buildingId`.
 * - Fournir un LiveData<List<Salle>> pour l'observation par les vues.
 * - Utiliser le cache local (`SalleUtils.loadSalles`, `SalleUtils.saveSalles`) pour les performances.
 *
 * Composants :
 * - Firebase Firestore
 * - SharedPreferences (via `SalleUtils`)
 * - `LiveData` et `MutableLiveData` pour observer les salles
 *
 * Bibliothèques utilisées :
 * - Firebase Firestore
 * - AndroidX Lifecycle (LiveData)
 *
 * @param context Contexte Android pour accéder au système de fichiers local
 *
 * @see tg.univlome.epl.models.Salle
 * @see tg.univlome.epl.utils.SalleUtils
 */
class SalleService(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val sallesCollection = db.collection("salles")
    private val batimentsCollection = db.collection("batiments")

    /**
     * Récupère la liste des salles, en utilisant d'abord le cache local (SharedPreferences),
     * puis en mettant à jour les données via Firebase Firestore si disponible.
     *
     * @return LiveData contenant la liste des salles
     */
    fun getSalles(): LiveData<List<Salle>> {
        val sallesLiveData = MutableLiveData<List<Salle>>()
        val batimentMap = mutableMapOf<String, String>()

        // 1. Charger les salles depuis SharedPreferences (cache rapide)
        val loadSalles = SalleUtils.loadSalles(context)
        if (loadSalles != null) {
            sallesLiveData.value = loadSalles!!
            // 2. Charger les bâtiments depuis Firebase (plus lent)
            batimentsCollection.get()
                .addOnSuccessListener { batimentResult ->
                    for (document in batimentResult) {
                        val id = document.id
                        val nom = document.getString("nom") ?: ""
                        batimentMap[id] = nom
                    }

                    // 3. Puis les salles
                    sallesCollection.get()
                        .addOnSuccessListener { result ->
                            val sallesList = mutableListOf<Salle>()
                            for (document in result) {
                                sallesList.add(createSalleFromDocument(document, batimentMap))
                            }

                            // Trier les salles : d’abord par nom de bâtiment, puis par nom de salle
                            val sallesTriees = sallesList.sortedWith(
                                compareBy<Salle> { it.situation.lowercase() } // tri par nom de bâtiment
                                    .thenBy { it.nom.lowercase() }             // puis par nom de salle
                            )

                            // Sauvegarde UNE SEULE FOIS après avoir tout ajouté
                            SalleUtils.saveSalles(context, sallesTriees.toMutableList())

                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "SalleService",
                                "Erreur lors de la récupération des salles",
                                exception
                            )
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("SalleService", "Erreur lors de la récupération des bâtiments", exception)
                }
        } else {
            // 2. Charger les bâtiments depuis Firebase (plus lent)
            batimentsCollection.get()
                .addOnSuccessListener { batimentResult ->
                    for (document in batimentResult) {
                        val id = document.id
                        val nom = document.getString("nom") ?: ""
                        batimentMap[id] = nom
                    }

                    // 3. Puis les salles
                    sallesCollection.get()
                        .addOnSuccessListener { result ->
                            val sallesList = mutableListOf<Salle>()
                            for (document in result) {
                                sallesList.add(createSalleFromDocument(document, batimentMap))
                            }

                            // Trier les salles : d’abord par nom de bâtiment, puis par nom de salle
                            val sallesTriees = sallesList.sortedWith(
                                compareBy<Salle> { it.situation.lowercase() } // tri par nom de bâtiment
                                    .thenBy { it.nom.lowercase() }             // puis par nom de salle
                            )

                            // Sauvegarde UNE SEULE FOIS après avoir tout ajouté
                            SalleUtils.saveSalles(context, sallesTriees.toMutableList())
                            sallesLiveData.value = sallesTriees


                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "SalleService",
                                "Erreur lors de la récupération des salles",
                                exception
                            )
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("SalleService", "Erreur lors de la récupération des bâtiments", exception)
                }
        }

        return sallesLiveData
    }

    /**
     * Transforme un document Firestore en instance de [Salle], en liant les ID de bâtiments
     * à leur nom via une map générée au préalable.
     *
     * @param document Document Firestore de la salle
     * @param batimentMap Map des ID de bâtiments vers leurs noms
     * @return Salle initialisée avec ses attributs
     */
    private fun createSalleFromDocument(
        document: DocumentSnapshot,
        batimentMap: Map<String, String>
    ): Salle {
        val id = document.id
        val infrastructureId = document.getString("buildingId") ?: ""
        val nom = document.getString("nom") ?: ""
        val description = document.getString("description") ?: ""
        val capacite = document.getString("capacity") ?: ""
        val longitude = document.getString("longitude") ?: ""
        val latitude = document.getString("latitude") ?: ""
        val images = document.get("images") as? List<String> ?: emptyList()
        val image = images.firstOrNull() ?: ""
        val type = document.getString("type") ?: ""

        val situation = batimentMap[infrastructureId] ?: "Bâtiment inconnu"

        return Salle(
            id,
            infrastructureId,
            nom,
            description,
            capacite,
            longitude,
            latitude,
            image,
            situation,
            type,
            images
        )
    }
}
