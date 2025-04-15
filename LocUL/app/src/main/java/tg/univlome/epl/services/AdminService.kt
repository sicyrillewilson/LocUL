// Pourquoi AdminService

package tg.univlome.epl.services

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import tg.univlome.epl.models.Admin

class AdminService {
    private val db = FirebaseFirestore.getInstance()
    private val adminsCollection = db.collection("admins")

    fun getAdmins(): LiveData<List<Admin>> {
        val adminsLiveData = MutableLiveData<List<Admin>>()

        adminsCollection.get()
            .addOnSuccessListener { result ->
                val adminsList = mutableListOf<Admin>()
                for (document in result) {
                    val username = document.getString("username") ?: ""
                    val password = document.getString("password") ?: ""
                    adminsList.add(Admin(username, password))
                }
                adminsLiveData.value = adminsList
            }
            .addOnFailureListener { exception ->
                Log.e("AdminService", "Erreur lors de la récupération des admins", exception)
            }

        return adminsLiveData
    }
}
