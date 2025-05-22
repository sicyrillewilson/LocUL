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
import tg.univlome.epl.MainActivity
import tg.univlome.epl.R
import tg.univlome.epl.adapter.BatimentFragmentAdapter
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.models.modelsfragments.FragmentModel
import tg.univlome.epl.services.BatimentService
import tg.univlome.epl.ui.maps.MapsFragment

/**
 * Objet BatimentUtils : Fournit des fonctions utilitaires pour la gestion
 * des bâtiments sur le campus universitaire.
 *
 * Description :
 * Cet objet centralise les opérations relatives aux bâtiments :
 *  - Chargement dynamique des bâtiments à partir du service distant (Firestore)
 *  - Filtrage selon la situation géographique (sud, nord, etc.) ou le type (enseignement, administratif...)
 *  - Calcul de la distance entre l'utilisateur et chaque bâtiment
 *  - Ouverture d'une carte localisant un bâtiment spécifique
 *  - Sauvegarde et chargement local de la liste des bâtiments (JSON via SharedPreferences)
 *
 * Composants principaux :
 *  - BatimentService : service de récupération des données depuis Firestore
 *  - BatimentFragmentAdapter : adaptateur RecyclerView pour afficher les bâtiments
 *  - MapsFragment : fragment de carte pour localiser un bâtiment
 *  - FragmentModel : modèle encapsulant le contexte de fragment (UI + logique)
 *
 * Bibliothèques utilisées :
 *  - Gson pour la sérialisation JSON
 *  - OSMDroid pour la manipulation de coordonnées géographiques
 *
 * @see BatimentService pour la gestion distante des bâtiments
 * @see MapsFragment pour la carte centrée sur un bâtiment
 */
object BatimentUtils {

    private lateinit var batimentService: BatimentService
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var batiments: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter

    /**
     * Ouvre un fragment de carte (MapsFragment) centré sur la position du bâtiment donné.
     *
     * @param batiment Bâtiment à localiser sur la carte.
     * @param fragmentActivity Activité hôte dans laquelle le fragment sera affiché.
     */
    fun ouvrirMapsFragment(batiment: Batiment, fragmentActivity: FragmentActivity) {
        val fragment = MapsFragment()
        val bundle = Bundle()
        bundle.putDouble("latitude", batiment.latitude.toDouble())
        bundle.putDouble("longitude", batiment.longitude.toDouble())
        fragment.arguments = bundle

        fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Ouvre le fragment de carte depuis une activité en sauvegardant la destination.
     *
     * @param batiment Bâtiment cible à localiser.
     * @param activity Activité actuelle (doit hériter de MainActivity).
     * @param fragmentContext Contexte utilisé pour accéder aux préférences partagées.
     */
    fun ouvrirMapsFragment(
        batiment: Batiment,
        activity: FragmentActivity,
        fragmentContext: Context
    ) {
        MapsUtils.saveDestination(
            fragmentContext,
            GeoPoint(batiment.latitude.toDouble(), batiment.longitude.toDouble())
        )
        (activity as? MainActivity)?.loadMapsFragment()
    }

    /**
     * Met à jour dynamiquement la liste des bâtiments avec la distance depuis l’utilisateur,
     * applique des filtres par type ou situation (sud, nord, etc.), et recharge la RecyclerView.
     *
     * @param userLocation Position géographique actuelle de l'utilisateur.
     * @param batiments Liste cible à remplir avec les bâtiments chargés.
     * @param filteredList Liste utilisée pour stocker les bâtiments filtrés.
     * @param adapter Adaptateur lié à la RecyclerView pour l'affichage.
     * @param fragmentModel Modèle contenant la vue, le contexte et les métadonnées du fragment.
     * @param onDataLoaded Callback optionnel à exécuter après chargement.
     */
    fun updateBatiments(
        userLocation: GeoPoint,
        batiments: MutableList<Batiment>,
        filteredList: MutableList<Batiment>,
        adapter: BatimentFragmentAdapter,
        fragmentModel: FragmentModel,
        onDataLoaded: () -> Unit = {}
    ) {
        batimentService = BatimentService(fragmentModel.fragmentContext)
        this.filteredList = filteredList
        this.batiments = batiments
        this.adapter = adapter

        Log.d("BatimentUtils", "updateBatiments appelée avec : $userLocation")
        // Charger les bâtiments
        batimentService.getBatiments().observe(fragmentModel.viewLifecycleOwner, Observer { bats ->
            if (bats != null) {
                for (batiment in bats) {
                    try {
                        val batimentLocation =
                            GeoPoint(batiment.latitude.toDouble(), batiment.longitude.toDouble())
                        val distance = MapsUtils.calculateDistance(userLocation, batimentLocation)
                        Log.d(
                            "BatimentUtils",
                            "Distance calculée pour ${batiment.nom}: $distance mètres"
                        )

                        // Conversion en km si la distance dépasse 1000 m
                        val formattedDistance = if (distance >= 1000) {
                            String.format("%.2f km", distance / 1000)
                        } else {
                            String.format("%.2f m", distance)
                        }

                        batiment.distance = formattedDistance

                        Log.d(
                            "BatimentUtils",
                            "Distance mise à jour pour ${batiment.nom}: ${batiment.distance}"
                        )

                    } catch (e: Exception) {
                        Log.e(
                            "BatimentUtils",
                            "Erreur lors de la mise à jour de la distance pour ${batiment.nom}",
                            e
                        )
                    }
                    when (fragmentModel.situation) {
                        "" -> {
                            if (fragmentModel.type != "" && batiment.type.lowercase() == fragmentModel.type.lowercase()) {
                                batiments.add(batiment)
                            } else if (fragmentModel.type == "") {
                                batiments.add(batiment)
                            }
                        }

                        "sud" -> {
                            if (batiment.situation == "Campus sud") {
                                batiments.add(batiment)
                            }
                        }

                        "nord" -> {
                            if (batiment.situation == "Campus nord") {
                                batiments.add(batiment)
                            }
                        }
                    }
                }
            }
            this.filteredList = batiments.toMutableList()

            this.adapter = BatimentFragmentAdapter(batiments)

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
        onDataLoaded()
    }

    /**
     * Sauvegarde la liste des bâtiments localement dans les préférences partagées (JSON).
     *
     * @param context Contexte utilisé pour accéder aux préférences.
     * @param batiments Liste des bâtiments à sauvegarder.
     */
    fun saveBatiments(context: Context, batiments: MutableList<Batiment>) {
        val sharedPreferences = context.getSharedPreferences("BatimentsPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(batiments)
        editor.putString("batiment", json)
        editor.apply()
    }

    /**
     * Charge les bâtiments précédemment sauvegardés depuis les préférences partagées.
     *
     * @param context Contexte utilisé pour accéder aux préférences.
     * @return Liste des bâtiments sauvegardés ou null si aucune donnée.
     */
    fun loadBatiments(context: Context): MutableList<Batiment>? {
        val sharedPreferences = context.getSharedPreferences("BatimentsPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("batiment", null)
        val type = object : TypeToken<MutableList<Batiment>>() {}.type
        return gson.fromJson(json, type)
    }

}