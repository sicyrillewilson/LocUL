@file:Suppress("DEPRECATION")

package tg.univlome.epl

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.osmdroid.config.Configuration
import android.preference.PreferenceManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // osmdroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        // Firestore offline
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}