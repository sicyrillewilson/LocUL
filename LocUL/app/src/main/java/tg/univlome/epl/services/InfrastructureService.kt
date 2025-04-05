package tg.univlome.epl.services

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import tg.univlome.epl.models.Infrastructure

class InfrastructureService {
    private val db = FirebaseFirestore.getInstance()
    private val infrastructuresCollection = db.collection("infrastructures")

    fun getInfrastructures(): LiveData<List<Infrastructure>> {
        val infrastructuresLiveData = MutableLiveData<List<Infrastructure>>()

        infrastructuresCollection.get()
            .addOnSuccessListener { result ->
                val infrastructuresList = mutableListOf<Infrastructure>()
                for (document in result) {
                    val id = document.id
                    val nom = document.getString("nom") ?: ""
                    val description = document.getString("description") ?: ""
                    val longitude = document.getString("longitude") ?: ""
                    val latitude = document.getString("latitude") ?: ""
                    val images = document.get("images") as? List<String> ?: emptyList()
                    val image = images.firstOrNull() ?: ""
                    val situation = document.getString("situation") ?: ""
                    val type = document.getString("type") ?: ""

                    infrastructuresList.add(Infrastructure(id, nom, description, longitude, latitude, image, situation, type, images))
                }
                infrastructuresLiveData.value = infrastructuresList
            }
            .addOnFailureListener { exception ->
                Log.e("InfrastructureService", "Erreur lors de la récupération des infrastructures", exception)
            }

        return infrastructuresLiveData
    }
}
