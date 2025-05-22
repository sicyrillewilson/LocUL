package tg.univlome.epl.utils

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.R
import tg.univlome.epl.adapter.BatimentAdapter
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.models.modelsfragments.HomeFragmentModel
import tg.univlome.epl.services.BatimentService
import tg.univlome.epl.ui.maps.MapsFragment

/**
 * Objet HomeBatimentUtils : Fournit des fonctions utilitaires spécifiques à l'affichage
 * des bâtiments sur l'écran d'accueil de l'application.
 *
 * Description :
 * Cet objet permet de :
 *  - Charger dynamiquement la liste des bâtiments filtrés par type (enseignement, administratif, etc.)
 *  - Calculer et afficher la distance entre chaque bâtiment et l'utilisateur
 *  - Afficher les bâtiments sous forme de liste horizontale dans un `RecyclerView`
 *  - Ouvrir une carte localisant un bâtiment sélectionné
 *
 * Il est utilisé principalement par le `HomeFragment` via un modèle `HomeFragmentModel` qui encapsule
 * le contexte et les vues nécessaires.
 *
 * Composants principaux :
 *  - BatimentService : service de récupération des données Firestore
 *  - BatimentAdapter : adaptateur RecyclerView pour afficher les bâtiments sur l’écran d’accueil
 *  - MapsFragment : fragment de carte pour visualiser la localisation d’un bâtiment
 *
 * Bibliothèques utilisées :
 *  - OSMDroid pour la manipulation de géopoints
 *  - Gson (via MapsUtils si extension)
 *  - AndroidX Fragment/RecyclerView
 *
 * @see BatimentService pour l'accès aux données des bâtiments
 * @see MapsFragment pour la localisation sur carte
 */
object HomeBatimentUtils {

    private lateinit var batimentService: BatimentService
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var batiments: MutableList<Batiment>
    private lateinit var adapter: BatimentAdapter

    /**
     * Ouvre un `MapsFragment` centré sur la position du bâtiment sélectionné.
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
     * Met à jour la liste des bâtiments affichés sur l'écran d'accueil :
     *  - Applique un filtre par type (défini dans `HomeFragmentModel`)
     *  - Calcule la distance entre chaque bâtiment et l'utilisateur
     *  - Met à jour le `RecyclerView` horizontal avec l'adaptateur
     *
     * @param userLocation Position géographique actuelle de l'utilisateur.
     * @param batiments Liste cible contenant les bâtiments chargés.
     * @param filteredList Liste filtrée utilisée pour l'affichage.
     * @param adapter Adaptateur initial (sera remplacé).
     * @param homeFragmentModel Modèle encapsulant le contexte et les vues de HomeFragment.
     * @param onDataLoaded Callback optionnel exécuté après le chargement.
     */
    fun updateBatiments(
        userLocation: GeoPoint,
        batiments: MutableList<Batiment>,
        filteredList: MutableList<Batiment>,
        adapter: BatimentAdapter,
        homeFragmentModel: HomeFragmentModel,
        onDataLoaded: () -> Unit = {}
    ) {
        batimentService = BatimentService(homeFragmentModel.fragmentContext)
        this.filteredList = filteredList
        this.batiments = batiments
        this.adapter = adapter

        Log.d("HomeBatimentUtils", "updateBatiments appelée avec : $userLocation")
        // Charger les bâtiments
        batimentService.getBatiments()
            .observe(homeFragmentModel.viewLifecycleOwner, Observer { bats ->
                if (bats != null) {
                    for (batiment in bats) {
                        if (batiment.type.lowercase() == homeFragmentModel.type.lowercase()) {
                            try {
                                val batimentLocation = GeoPoint(
                                    batiment.latitude.toDouble(),
                                    batiment.longitude.toDouble()
                                )
                                val distance =
                                    MapsUtils.calculateDistance(userLocation, batimentLocation)
                                Log.d(
                                    "HomeBatimentUtils",
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
                                    "HomeBatimentUtils",
                                    "Distance mise à jour pour ${batiment.nom}: ${batiment.distance}"
                                )

                            } catch (e: Exception) {
                                Log.e(
                                    "HomeBatimentUtils",
                                    "Erreur lors de la mise à jour de la distance pour ${batiment.nom}",
                                    e
                                )
                            }
                            batiments.add(batiment)
                        }
                    }
                }
                this.filteredList = batiments.toMutableList()

                this.adapter = BatimentAdapter(
                    batiments,
                    homeFragmentModel.fragmentManager,
                    homeFragmentModel.newFragment
                )

                val recyclerBatiments =
                    homeFragmentModel.view?.findViewById<RecyclerView>(homeFragmentModel.recyclerViewId)
                recyclerBatiments?.layoutManager =
                    LinearLayoutManager(
                        homeFragmentModel.fragmentContext,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                recyclerBatiments?.adapter = adapter
            })
        onDataLoaded()
    }
}