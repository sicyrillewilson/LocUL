package tg.univlome.epl.services

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
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
                    adminsList.add(createAdminFromDocument(document))
                }
                adminsLiveData.value = adminsList
            }
            .addOnFailureListener { exception ->
                Log.e("AdminService", "Erreur lors de la récupération des admins", exception)
            }

        return adminsLiveData
    }

    private fun createAdminFromDocument(document: DocumentSnapshot): Admin {
        val username = document.getString("username") ?: ""
        val password = document.getString("password") ?: ""
        return Admin(username, password)
    }
}
