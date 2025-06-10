@file:Suppress("DEPRECATION")

package tg.univlome.epl

import android.app.Application
import android.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.osmdroid.config.Configuration

/**
 * Classe MyApp : Point d’entrée principal de l’application.
 *
 * Description :
 * Cette classe étend `Application` et est utilisée pour effectuer des configurations
 * globales dès le lancement de l’application Android.
 *
 * Deux initialisations majeures sont effectuées :
 *  - **OSMDroid** : Chargement des préférences partagées pour la configuration de la carte
 *  - **Firebase Firestore** : Activation de la persistance hors-ligne des données
 *
 * Elle est référencée dans le fichier `AndroidManifest.xml` via l’attribut `android:name`.
 *
 * Composants principaux :
 *  - `Configuration.getInstance().load(...)` pour OSMDroid
 *  - `FirebaseFirestoreSettings.setPersistenceEnabled(true)` pour Firestore
 *
 * Bibliothèques utilisées :
 *  - OSMDroid : bibliothèque de cartographie open source
 *  - Firebase Firestore : base de données NoSQL temps réel
 *
 * @see android.app.Application
 * @see org.osmdroid.config.Configuration
 * @see com.google.firebase.firestore.FirebaseFirestore
 */
class MyApp : Application() {

    /**
     * Initialise les composants globaux de l’application.
     * Appelé automatiquement au lancement de l’application.
     */
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