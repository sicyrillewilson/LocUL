@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.infrastructure

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.MainActivity
import tg.univlome.epl.R
import tg.univlome.epl.adapter.InfraFragmentAdapter
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.models.modelsfragments.FragmentModel
import tg.univlome.epl.services.InfrastructureService
import tg.univlome.epl.ui.SearchBarFragment
import tg.univlome.epl.utils.InfraUtils
import tg.univlome.epl.utils.MapsUtils

/**
 * Fragment AllInfraFragment : Affiche toutes les infrastructures sans filtrage de situation
 *
 * Description :
 * Ce fragment est chargé d’afficher la liste complète des infrastructures disponibles
 * sur le campus universitaire, quelle que soit leur situation géographique (nord ou sud).
 * Il utilise les services de géolocalisation de l’utilisateur pour afficher la distance
 * entre lui et chaque infrastructure.
 *
 * Les données sont récupérées depuis un service distant via `InfrastructureService`,
 * affichées dans une `RecyclerView`, et mises en forme avec un `Shimmer` de chargement.
 * Un champ de recherche permet un filtrage dynamique par nom d’infrastructure.
 *
 * Composants principaux :
 *  - `ShimmerFrameLayout` : animation de chargement en attendant les données
 *  - `RecyclerView` : affichage de la liste des infrastructures
 *  - `FusedLocationProviderClient` : localisation utilisateur
 *  - `InfraUtils` : traitement des données, distances, filtrage
 *
 * Bibliothèques utilisées :
 *  - Facebook Shimmer pour les effets de chargement
 *  - OSMDroid pour la manipulation des coordonnées
 *  - Google Location Services pour la localisation utilisateur
 *
 * @see InfraUtils pour la mise à jour et la logique métier
 * @see InfraFragmentAdapter pour l’affichage des infrastructures
 * @see SearchBarFragment pour la fonctionnalité de recherche dynamique
 */
class AllInfraFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var infrasAll: MutableList<Infrastructure>
    private lateinit var filteredList: MutableList<Infrastructure>
    private lateinit var adapter: InfraFragmentAdapter
    private lateinit var infraService: InfrastructureService
    private lateinit var fragmentModel: FragmentModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var shimmerAllInfra: ShimmerFrameLayout
    private lateinit var recyclerAllInfra: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Initialise la vue du fragment, configure les composants UI,
     * instancie les listes et adaptateurs, puis déclenche la géolocalisation.
     *
     * @return Vue complète du fragment initialisé
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_infra, container, false)

        shimmerAllInfra = view.findViewById(R.id.shimmerAllInfra)
        recyclerAllInfra = view.findViewById(R.id.recyclerAllInfra)
        shimmerAllInfra.startShimmer()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        infraService = InfrastructureService(requireContext())
        infrasAll = mutableListOf()
        filteredList = mutableListOf()
        adapter = InfraFragmentAdapter(infrasAll)

        fragmentModel = FragmentModel(
            view,
            requireContext(),
            requireActivity(),
            viewLifecycleOwner,
            R.id.recyclerAllInfra
        )
        getUserLocation()

        return view
    }

    /**
     * Récupère la position actuelle de l’utilisateur (avec permission),
     * puis appelle `InfraUtils.updateInfrastructures()` pour charger et filtrer les données.
     */
    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val userGeoPoint = MapsUtils.fusedLocationClient(location, requireContext())
            val onDataLoadedCallback = {
                shimmerAllInfra.stopShimmer()
                shimmerAllInfra.visibility = View.GONE
                recyclerAllInfra.visibility = View.VISIBLE
            }

            InfraUtils.updateInfrastructures(
                userGeoPoint,
                infrasAll,
                filteredList,
                adapter,
                fragmentModel
            ) {
                onDataLoadedCallback()
            }
        }
    }

    /**
     * Affiche la barre de recherche intégrée lorsque ce fragment est actif.
     */
    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showSearchBarFragment(this)
    }

    /**
     * Cache la barre de recherche lorsque l’utilisateur quitte le fragment.
     */
    override fun onPause() {
        super.onPause()
        (activity as MainActivity).showSearchBarFragment(null) // Cacher la barre si on quitte
    }

    /**
     * Effectue une recherche en temps réel dans la liste des infrastructures
     * à partir du nom saisi par l’utilisateur.
     *
     * @param query Chaîne entrée par l’utilisateur dans la barre de recherche
     */
    override fun onSearch(query: String) {
        filteredList =
            infrasAll.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}