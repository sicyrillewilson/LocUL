package tg.univlome.epl.utils

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.R
import tg.univlome.epl.adapter.InfraAdapter
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.models.modelsfragments.HomeFragmentModel
import tg.univlome.epl.services.InfrastructureService
import tg.univlome.epl.ui.maps.MapsFragment

/**
 * Objet HomeInfraUtils : Fournit des utilitaires pour la gestion des infrastructures
 * sur l’écran d’accueil de l’application.
 *
 * Description :
 * Cet objet permet d'afficher dynamiquement les infrastructures filtrées par type
 * ou situation géographique dans un `RecyclerView` horizontal sur la page d'accueil.
 * Il calcule également la distance entre l'utilisateur et chaque infrastructure,
 * et permet d'ouvrir une carte localisant l'infrastructure sélectionnée.
 *
 * Composants principaux :
 *  - InfrastructureService : service de récupération des infrastructures depuis Firestore
 *  - InfraAdapter : adaptateur pour afficher les infrastructures horizontalement
 *  - MapsFragment : fragment de carte centré sur une infrastructure
 *
 * Bibliothèques utilisées :
 *  - OSMDroid pour les coordonnées géographiques
 *  - AndroidX RecyclerView et Fragment
 *
 * @see InfrastructureService pour la gestion des données
 * @see MapsFragment pour l'affichage de la carte
 */
object HomeInfraUtils {

    private lateinit var infrastructureService: InfrastructureService
    private lateinit var filteredList: MutableList<Infrastructure>
    private lateinit var infrastructures: MutableList<Infrastructure>
    private lateinit var adapter: InfraAdapter

    /**
     * Ouvre un fragment de carte centré sur la position de l’infrastructure sélectionnée.
     *
     * @param infrastructure Infrastructure à afficher sur la carte.
     * @param fragmentActivity Activité hôte dans laquelle le fragment sera affiché.
     */
    fun ouvrirMapsFragment(infrastructure: Infrastructure, fragmentActivity: FragmentActivity) {
        val fragment = MapsFragment()
        val bundle = Bundle()
        bundle.putDouble("latitude", infrastructure.latitude.toDouble())
        bundle.putDouble("longitude", infrastructure.longitude.toDouble())
        fragment.arguments = bundle

        fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Met à jour dynamiquement la liste des infrastructures à afficher sur l’écran d’accueil :
     *  - Récupère les données depuis Firestore
     *  - Calcule la distance entre l’utilisateur et chaque infrastructure
     *  - Met à jour la RecyclerView avec les éléments chargés
     *
     * @param userLocation Position géographique actuelle de l'utilisateur.
     * @param infrastructures Liste à remplir avec les infrastructures récupérées.
     * @param filteredList Liste utilisée pour l’affichage après filtrage.
     * @param adapter Adaptateur pour la RecyclerView (sera remplacé après chargement).
     * @param homeFragmentModel Modèle contenant le contexte et les vues du fragment appelant.
     * @param onDataLoaded Callback optionnel exécuté après chargement.
     */
    fun updateInfrastructures(
        userLocation: GeoPoint,
        infrastructures: MutableList<Infrastructure>,
        filteredList: MutableList<Infrastructure>,
        adapter: InfraAdapter,
        homeFragmentModel: HomeFragmentModel,
        onDataLoaded: () -> Unit = {}
    ) {
        infrastructureService = InfrastructureService(homeFragmentModel.fragmentContext)
        this.filteredList = filteredList
        this.infrastructures = infrastructures
        this.adapter = adapter

        Log.d("HomeInfraUtils", "updateInfrastructures appelée avec : $userLocation")
        // Charger les bâtiments
        infrastructureService.getInfrastructures()
            .observe(homeFragmentModel.viewLifecycleOwner, Observer { infras ->
                if (infras != null) {
                    for (infrastructure in infras) {
                        try {
                            val infrastructureLocation = GeoPoint(
                                infrastructure.latitude.toDouble(),
                                infrastructure.longitude.toDouble()
                            )
                            val distance =
                                MapsUtils.calculateDistance(userLocation, infrastructureLocation)
                            Log.d(
                                "HomeInfraUtils",
                                "Distance calculée pour ${infrastructure.nom}: $distance mètres"
                            )

                            // Conversion en km si la distance dépasse 1000 m
                            val formattedDistance = if (distance >= 1000) {
                                String.format("%.2f km", distance / 1000)
                            } else {
                                String.format("%.2f m", distance)
                            }

                            infrastructure.distance = formattedDistance

                            Log.d(
                                "HomeInfraUtils",
                                "Distance mise à jour pour ${infrastructure.nom}: ${infrastructure.distance}"
                            )

                        } catch (e: Exception) {
                            Log.e(
                                "HomeInfraUtils",
                                "Erreur lors de la mise à jour de la distance pour ${infrastructure.nom}",
                                e
                            )
                        }
                        infrastructures.add(infrastructure)
                    }
                }
                // Mise à jour de l’affichage
                this.filteredList = infrastructures.toMutableList()

                this.adapter = InfraAdapter(
                    infrastructures,
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