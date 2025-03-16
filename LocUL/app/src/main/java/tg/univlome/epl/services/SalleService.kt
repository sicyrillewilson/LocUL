package tg.univlome.epl.services

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import tg.univlome.epl.models.Salle

class SalleService {
    private val db = FirebaseFirestore.getInstance()
    private val sallesCollection = db.collection("salles")

    fun getSalles(): LiveData<List<Salle>> {
        val sallesLiveData = MutableLiveData<List<Salle>>()

        sallesCollection.get()
            .addOnSuccessListener { result ->
                val sallesList = mutableListOf<Salle>()
                for (document in result) {
                    val id = document.id
                    val infrastructureId = document.getString("buildingId") ?: ""
                    val nom = document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""
                    val capacite = document.getString("capacity") ?: ""
                    val longitude = document.getString("longitude") ?: ""
                    val latitude = document.getString("latitude") ?: ""
                    val image = document.getString("image") ?: ""

                    sallesList.add(Salle(id, infrastructureId, nom, description, capacite, longitude, latitude, image))
                }
                sallesLiveData.value = sallesList
            }
            .addOnFailureListener { exception ->
                Log.e("SalleService", "Erreur lors de la récupération des salles", exception)
            }

        return sallesLiveData
    }
}
