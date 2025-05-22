package tg.univlome.epl.utils

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.R
import tg.univlome.epl.adapter.SalleAdapter
import tg.univlome.epl.models.Salle
import tg.univlome.epl.models.modelsfragments.HomeFragmentModel
import tg.univlome.epl.services.SalleService
import tg.univlome.epl.ui.maps.MapsFragment

/**
 * Objet HomeSalleUtils : Fournit des utilitaires pour la gestion et l’affichage
 * des salles sur l’écran d’accueil de l’application.
 *
 * Description :
 * Cet objet permet d’afficher dynamiquement les salles dans un `RecyclerView` horizontal,
 * en calculant la distance entre chaque salle et la position actuelle de l’utilisateur.
 * Il offre également la possibilité d’ouvrir une carte localisant une salle sélectionnée.
 *
 * Composants principaux :
 *  - SalleService : service de récupération des données des salles via Firestore
 *  - SalleAdapter : adaptateur utilisé pour afficher les salles dans un `RecyclerView`
 *  - MapsFragment : fragment de carte centré sur une salle
 *
 * Bibliothèques utilisées :
 *  - OSMDroid pour la manipulation géographique
 *  - AndroidX Lifecycle, RecyclerView, Fragment
 *
 * @see SalleService pour la gestion des données Firestore
 * @see MapsFragment pour la localisation sur carte
 */
object HomeSalleUtils {

    private lateinit var salleService: SalleService
    private lateinit var filteredList: MutableList<Salle>
    private lateinit var salles: MutableList<Salle>
    private lateinit var adapter: SalleAdapter

    /**
     * Ouvre un fragment de carte (`MapsFragment`) centré sur la position
     * d’une salle donnée.
     *
     * @param salle Salle à localiser sur la carte.
     * @param fragmentActivity Activité hôte dans laquelle afficher le fragment.
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
     * Met à jour la liste des salles affichées sur l’écran d’accueil :
     *  - Charge les données depuis Firestore via `SalleService`
     *  - Calcule la distance entre l'utilisateur et chaque salle
     *  - Met à jour dynamiquement l'adaptateur et le `RecyclerView`
     *
     * @param userLocation Position actuelle de l'utilisateur.
     * @param salles Liste à remplir avec les salles récupérées.
     * @param filteredList Liste utilisée pour l’affichage après filtrage.
     * @param adapter Adaptateur RecyclerView (sera remplacé).
     * @param homeFragmentModel Modèle contenant les informations de vue et de contexte.
     * @param onDataLoaded Callback exécuté après chargement des données (optionnel).
     */
    fun updateSalles(
        userLocation: GeoPoint,
        salles: MutableList<Salle>,
        filteredList: MutableList<Salle>,
        adapter: SalleAdapter,
        homeFragmentModel: HomeFragmentModel,
        onDataLoaded: () -> Unit = {}
    ) {
        salleService = SalleService(homeFragmentModel.fragmentContext)
        this.filteredList = filteredList
        this.salles = salles
        this.adapter = adapter

        Log.d("HomeSalleUtils", "updateSalles appelée avec : $userLocation")
        // Charger les bâtiments
        salleService.getSalles().observe(homeFragmentModel.viewLifecycleOwner, Observer { sals ->
            if (sals != null) {
                for (salle in sals) {
                    try {
                        val salleLocation =
                            GeoPoint(salle.latitude.toDouble(), salle.longitude.toDouble())
                        val distance = MapsUtils.calculateDistance(userLocation, salleLocation)
                        Log.d(
                            "HomeSalleUtils",
                            "Distance calculée pour ${salle.nom}: $distance mètres"
                        )

                        // Conversion en km si la distance dépasse 1000 m
                        val formattedDistance = if (distance >= 1000) {
                            String.format("%.2f km", distance / 1000)
                        } else {
                            String.format("%.2f m", distance)
                        }

                        salle.distance = formattedDistance

                        Log.d(
                            "HomeSalleUtils",
                            "Distance mise à jour pour ${salle.nom}: ${salle.distance}"
                        )

                    } catch (e: Exception) {
                        Log.e(
                            "HomeSalleUtils",
                            "Erreur lors de la mise à jour de la distance pour ${salle.nom}",
                            e
                        )
                    }
                    salles.add(salle)
                }
            }
            // Mise à jour de l’adaptateur et du RecyclerView
            this.filteredList = salles.toMutableList()

            this.adapter = SalleAdapter(
                salles,
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