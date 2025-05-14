package tg.univlome.epl.services

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import tg.univlome.epl.models.Salle
import tg.univlome.epl.utils.SalleUtils

class SalleService(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val sallesCollection = db.collection("salles")
    private val batimentsCollection = db.collection("batiments")

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

                            // Sauvegarde UNE SEULE FOIS après avoir tout ajouté
                            SalleUtils.saveSalles(context, sallesList)

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

                            // Sauvegarde UNE SEULE FOIS après avoir tout ajouté
                            SalleUtils.saveSalles(context, sallesList)

                            sallesLiveData.value = sallesList
                            //sallesLiveData.postValue(sallesList)


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
