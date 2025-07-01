@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.batiment

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
import tg.univlome.epl.adapter.BatimentFragmentAdapter
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.models.modelsfragments.FragmentModel
import tg.univlome.epl.services.BatimentService
import tg.univlome.epl.ui.SearchBarFragment
import tg.univlome.epl.utils.BatimentUtils
import tg.univlome.epl.utils.MapsUtils

/**
 * Fragment AllBatimentFragment : Affiche tous les bâtiments sans filtrage géographique
 *
 * Description :
 * Ce fragment présente la liste complète des bâtiments disponibles sur le campus universitaire,
 * sans distinction de situation (nord/sud). Il utilise :
 *  - un effet de chargement Shimmer pour l’attente des données
 *  - la localisation de l’utilisateur pour calculer les distances par bâtiment
 *  - une interface de recherche (SearchBar) pour filtrer dynamiquement par nom
 *
 * Une fois les données chargées, elles sont affichées dans une `RecyclerView` avec
 * l’adaptateur `BatimentFragmentAdapter`. Ce fragment communique avec `BatimentUtils`
 * pour le traitement de la logique métier et la géolocalisation.
 *
 * Composants principaux :
 *  - `RecyclerView` : liste des bâtiments
 *  - `ShimmerFrameLayout` : animation de chargement
 *  - `FusedLocationProviderClient` : localisation utilisateur
 *  - `SearchBarFragment` : filtrage via une barre de recherche
 *
 * Bibliothèques utilisées :
 *  - Google Location Services
 *  - Facebook Shimmer
 *  - OSMDroid pour la gestion des coordonnées
 *
 * @see BatimentUtils pour la logique de filtrage et calcul de distance
 * @see BatimentFragmentAdapter pour l’affichage des éléments
 * @see SearchBarFragment pour la recherche textuelle
 */
class AllBatimentFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var batiments: MutableList<Batiment>
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter
    private lateinit var batimentService: BatimentService
    private lateinit var fragmentModel: FragmentModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var shimmerAllBatiments: ShimmerFrameLayout
    private lateinit var recyclerAllBatiments: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Initialise le layout du fragment, les vues UI, la localisation,
     * les listes de données et lance le chargement.
     *
     * @return Vue initialisée du fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_batiment, container, false)

        shimmerAllBatiments = view.findViewById(R.id.shimmerAllBatiments)
        recyclerAllBatiments = view.findViewById(R.id.recyclerAllBatiments)
        shimmerAllBatiments.startShimmer()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        batimentService = BatimentService(requireContext())
        batiments = mutableListOf()
        filteredList = mutableListOf()
        adapter = BatimentFragmentAdapter(batiments)

        fragmentModel = FragmentModel(
            view,
            requireContext(),
            requireActivity(),
            viewLifecycleOwner,
            R.id.recyclerAllBatiments
        )
        getUserLocation()
        return view
    }

    /**
     * Récupère la position actuelle de l’utilisateur à l’aide de Google Location Services.
     * Une fois la position obtenue, déclenche le chargement des bâtiments via `BatimentUtils`.
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
            //val userGeoPoint = MapsUtils.fusedLocationClient(location, requireContext())
            val contextSafe = context ?: return@addOnSuccessListener
            val userGeoPoint = MapsUtils.fusedLocationClient(location, contextSafe)
            val onDataLoadedCallback = {
                shimmerAllBatiments.stopShimmer()
                shimmerAllBatiments.visibility = View.GONE
                recyclerAllBatiments.visibility = View.VISIBLE
            }

            BatimentUtils.updateBatiments(
                userGeoPoint,
                batiments,
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
     * Effectue un filtrage dynamique de la liste des bâtiments en fonction du texte recherché.
     * Met à jour l’adaptateur avec les résultats filtrés.
     *
     * @param query Chaîne entrée par l’utilisateur dans la barre de recherche
     */
    override fun onSearch(query: String) {
        filteredList =
            batiments.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}