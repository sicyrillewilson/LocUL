package tg.univlome.epl.utils

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.R
import tg.univlome.epl.adapter.SalleViewAllAdapter
import tg.univlome.epl.models.Salle
import tg.univlome.epl.models.modelsfragments.FragmentModel
import tg.univlome.epl.services.SalleService
import tg.univlome.epl.ui.maps.MapsFragment

/**
 * Objet SalleUtils : Fournit des fonctions utilitaires pour la gestion complète
 * des salles de cours ou de réunion sur le campus universitaire.
 *
 * Description :
 * Cet objet permet de :
 *  - Charger dynamiquement les salles depuis Firestore
 *  - Filtrer les salles par situation géographique (sud, nord, etc.)
 *  - Calculer la distance entre l'utilisateur et chaque salle
 *  - Afficher dynamiquement les salles dans un `RecyclerView`
 *  - Ouvrir une carte localisant une salle
 *  - Sauvegarder et charger localement les salles via `SharedPreferences`
 *
 * Composants principaux :
 *  - SalleService : service Firestore pour la récupération des données
 *  - SalleViewAllAdapter : adaptateur utilisé pour l’affichage de toutes les salles
 *  - MapsFragment : fragment de carte pour la localisation
 *
 * Bibliothèques utilisées :
 *  - OSMDroid pour les géopoints
 *  - Gson pour la sérialisation JSON
 *  - AndroidX Fragment, RecyclerView, Lifecycle
 *
 * @see SalleService pour les opérations de récupération de données
 * @see MapsFragment pour la visualisation géographique
 */
object SalleUtils {

    private lateinit var salleService: SalleService
    private lateinit var filteredList: MutableList<Salle>
    private lateinit var salles: MutableList<Salle>
    private lateinit var adapter: SalleViewAllAdapter

    /**
     * Ouvre un `MapsFragment` centré sur la salle sélectionnée.
     *
     * @param salle Salle à afficher sur la carte.
     * @param fragmentActivity Activité dans laquelle le fragment sera affiché.
     */
    fun ouvrirMapsFragment(salle: Salle, fragmentActivity: FragmentActivity) {
        val fragment = MapsFragment()
        val bundle = Bundle()
        bundle.putDouble("latitude", salle.latitude.toDouble())
        bundle.putDouble("longitude", salle.longitude.toDouble())
        fragment.arguments = bundle

        fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Met à jour dynamiquement la liste des salles :
     *  - Calcule la distance avec l'utilisateur
     *  - Applique un filtrage géographique (nord, sud, ou tous)
     *  - Recharge la RecyclerView avec les nouvelles données
     *
     * @param userLocation Position actuelle de l'utilisateur.
     * @param salles Liste à remplir avec les salles récupérées.
     * @param filteredList Liste utilisée pour le rendu visuel.
     * @param adapter Adaptateur initial de la RecyclerView.
     * @param fragmentModel Modèle encapsulant les éléments de contexte et de vue.
     */
    fun updateSalles(
        userLocation: GeoPoint,
        salles: MutableList<Salle>,
        filteredList: MutableList<Salle>,
        adapter: SalleViewAllAdapter,
        fragmentModel: FragmentModel
    ) {
        salleService = SalleService(fragmentModel.fragmentContext)
        this.filteredList = filteredList
        this.salles = salles
        this.adapter = adapter

        Log.d("SalleUtils", "updateSalles appelée avec : $userLocation")
        // Charger les salles
        salleService.getSalles().observe(fragmentModel.viewLifecycleOwner, Observer { sals ->
            if (sals != null) {
                for (salle in sals) {
                    try {
                        val salleLocation =
                            GeoPoint(salle.latitude.toDouble(), salle.longitude.toDouble())
                        val distance = MapsUtils.calculateDistance(userLocation, salleLocation)
                        Log.d("SalleUtils", "Distance calculée pour ${salle.nom}: $distance mètres")

                        // Conversion en km si la distance dépasse 1000 m
                        val formattedDistance = if (distance >= 1000) {
                            String.format("%.2f km", distance / 1000)
                        } else {
                            String.format("%.2f m", distance)
                        }

                        salle.distance = formattedDistance

                        Log.d(
                            "SalleUtils",
                            "Distance mise à jour pour ${salle.nom}: ${salle.distance}"
                        )

                    } catch (e: Exception) {
                        Log.e(
                            "SalleUtils",
                            "Erreur lors de la mise à jour de la distance pour ${salle.nom}",
                            e
                        )
                    }
                    when (fragmentModel.situation) {
                        "" -> {
                            salles.add(salle)
                        }

                        "sud" -> {
                            if (salle.situation == "Campus sud") {
                                salles.add(salle)
                            }
                        }

                        "nord" -> {
                            if (salle.situation == "Campus nord") {
                                salles.add(salle)
                            }
                        }
                    }
                }
            }
            this.filteredList = salles.toMutableList()

            this.adapter = SalleViewAllAdapter(salles)

            val recyclerBatiments =
                fragmentModel.view?.findViewById<RecyclerView>(fragmentModel.recyclerViewId)
            recyclerBatiments?.layoutManager =
                LinearLayoutManager(
                    fragmentModel.fragmentContext,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            recyclerBatiments?.adapter = adapter
        })
    }

    /**
     * Sauvegarde la liste des salles localement dans les préférences partagées.
     *
     * @param context Contexte utilisé pour accéder aux préférences.
     * @param salles Liste des salles à sauvegarder.
     */
    fun saveSalles(context: Context, salles: MutableList<Salle>) {
        val sharedPreferences = context.getSharedPreferences("SallePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(salles)
        editor.putString("salle", json)
        editor.apply()
    }

    /**
     * Charge les salles sauvegardées précédemment depuis les préférences partagées.
     *
     * @param context Contexte utilisé pour accéder aux préférences.
     * @return Liste des salles sauvegardées, ou null si aucune donnée.
     */
    fun loadSalles(context: Context): MutableList<Salle>? {
        val sharedPreferences = context.getSharedPreferences("SallePrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("salle", null)
        val type = object : TypeToken<MutableList<Salle>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Supprime la liste des salles sauvegardée localement.
     *
     * @param context Contexte utilisé pour accéder aux préférences.
     */
    fun clearSalles(context: Context) {
        val sharedPreferences = context.getSharedPreferences("SallePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("salle")
        editor.apply()
    }

}