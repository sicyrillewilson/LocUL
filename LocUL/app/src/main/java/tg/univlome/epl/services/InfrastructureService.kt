package tg.univlome.epl.services

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import tg.univlome.epl.models.Batiment

class InfrastructureService {
    private val db = FirebaseFirestore.getInstance()
    private val infrastructuresCollection = db.collection("infrastructures")

    fun getInfrastructures(): LiveData<List<Batiment>> {
        val infrastructuresLiveData = MutableLiveData<List<Batiment>>()

        infrastructuresCollection.get()
            .addOnSuccessListener { result ->
                val infrastructuresList = mutableListOf<Batiment>()
                for (document in result) {
                    val id = document.id
                    val nom = document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""
                    val longitude = document.getString("longitude") ?: ""
                    val latitude = document.getString("latitude") ?: ""
                    val image = document.getString("image") ?: ""

                    infrastructuresList.add(Batiment(id, nom, description, longitude, latitude, image))
                }
                infrastructuresLiveData.value = infrastructuresList
            }
            .addOnFailureListener { exception ->
                Log.e("InfrastructureService", "Erreur lors de la récupération des infrastructures", exception)
            }

        return infrastructuresLiveData
    }
}
