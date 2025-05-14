package tg.univlome.epl.services

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.utils.BatimentUtils

class BatimentService(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val batimentsCollection = db.collection("batiments")

    fun getBatiments(): LiveData<List<Batiment>> {
        val batimentsLiveData = MutableLiveData<List<Batiment>>()

        val loadBatiments = BatimentUtils.loadBatiments(context)
        if (loadBatiments != null) {
            batimentsLiveData.value = loadBatiments!!
            batimentsCollection.get()
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
            batimentsCollection.get()
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
