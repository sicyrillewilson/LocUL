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

/**
 * Fragment SudBatimentFragment : Affiche les bâtiments du campus sud
 *
 * Description :
 * Ce fragment est dédié à l'affichage des bâtiments situés sur le **campus sud** de l'université.
 * Il récupère la localisation de l'utilisateur, calcule la distance jusqu'à chaque bâtiment
 * et met à jour dynamiquement une `RecyclerView` avec un indicateur de chargement (shimmer).
 *
 * Une recherche en temps réel permet de filtrer les résultats en fonction du nom du bâtiment.
 *
 * Composants principaux :
 * - `BatimentUtils` : Gère la récupération, le filtrage et la mise à jour des bâtiments
 * - `BatimentFragmentAdapter` : Affiche les bâtiments dans une liste verticale
 * - `FragmentModel` : Fournit le contexte et les paramètres de configuration
 *
 * Bibliothèques utilisées :
 * - **OSMDroid** pour la gestion des coordonnées géographiques
 * - **Google Play Services** (`FusedLocationProviderClient`) pour la géolocalisation
 * - **Facebook Shimmer** pour l'effet de chargement pendant l'attente des données
 *
 * @see BatimentUtils
 * @see SearchBarFragment
 */
class SudBatimentFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var batiments: MutableList<Batiment>
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter
    private lateinit var batimentService: BatimentService
    private lateinit var fragmentModel: FragmentModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var shimmerSudBatiments: ShimmerFrameLayout
    private lateinit var recyclerSudBatiments: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Initialise la vue du fragment et déclenche la localisation pour charger les données.
     *
     * @return La vue du fragment sud contenant la liste des bâtiments filtrés.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sud_batiment, container, false)

        shimmerSudBatiments = view.findViewById(R.id.shimmerSudBatiments)
        recyclerSudBatiments = view.findViewById(R.id.recyclerSudBatiments)
        shimmerSudBatiments.startShimmer()

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
            R.id.recyclerSudBatiments,
            "sud"
        )
        getUserLocation()
        return view
    }

    /**
     * Récupère la position actuelle de l'utilisateur puis met à jour la liste des bâtiments du campus sud.
     * Affiche ensuite la `RecyclerView` et cache le shimmer une fois les données prêtes.
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
            location?.let {
                val userGeoPoint = GeoPoint(it.latitude, it.longitude)
                val onDataLoadedCallback = {
                    shimmerSudBatiments.stopShimmer()
                    shimmerSudBatiments.visibility = View.GONE
                    recyclerSudBatiments.visibility = View.VISIBLE
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
     * Filtre les bâtiments affichés selon le mot-clé saisi dans la barre de recherche.
     * @param query Texte de recherche saisi par l'utilisateur
     */
    override fun onSearch(query: String) {
        filteredList =
            batiments.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}