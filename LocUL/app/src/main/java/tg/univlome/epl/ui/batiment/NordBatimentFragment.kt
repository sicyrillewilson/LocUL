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
 * Fragment NordBatimentFragment : Affiche les bâtiments situés sur le Campus Nord.
 *
 * Description :
 * Ce fragment permet de visualiser dynamiquement les bâtiments du **campus nord**,
 * en se basant sur la localisation actuelle de l’utilisateur.
 * Il calcule les distances entre l’utilisateur et les bâtiments, les affiche dans
 * une `RecyclerView` avec un effet de chargement shimmer, et permet un filtrage
 * en temps réel via la barre de recherche.
 *
 * Composants principaux :
 * - `BatimentUtils` : outil central de récupération, filtrage et affichage
 * - `BatimentFragmentAdapter` : adaptateur de liste pour les bâtiments
 * - `FragmentModel` : encapsule les métadonnées de fragment (vue, contexte, type)
 *
 * Bibliothèques utilisées :
 * - Google Play Services (`FusedLocationProviderClient`) : pour la géolocalisation
 * - OSMDroid : pour manipuler les coordonnées géographiques
 * - Facebook Shimmer : pour l’effet de chargement fluide
 *
 * @see BatimentUtils pour la logique métier de mise à jour
 * @see SearchBarFragment pour le support du filtrage textuel en direct
 */
class NordBatimentFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var batiments: MutableList<Batiment>
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter
    private lateinit var batimentService: BatimentService
    private lateinit var fragmentModel: FragmentModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var shimmerNordBatiments: ShimmerFrameLayout
    private lateinit var recyclerNordBatiments: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Crée et retourne la vue du fragment, initialise les composants UI,
     * démarre l’effet shimmer et déclenche la récupération de la localisation.
     *
     * @return Vue complète du fragment initialisé
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nord_batiment, container, false)

        shimmerNordBatiments = view.findViewById(R.id.shimmerNordBatiments)
        recyclerNordBatiments = view.findViewById(R.id.recyclerNordBatiments)
        shimmerNordBatiments.startShimmer()

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
            R.id.recyclerNordBatiments,
            "nord"
        )
        getUserLocation()
        return view
    }

    /**
     * Récupère la localisation actuelle de l'utilisateur pour filtrer les bâtiments.
     * Utilise `BatimentUtils.updateBatiments` avec le paramètre de situation "nord".
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
                shimmerNordBatiments.stopShimmer()
                shimmerNordBatiments.visibility = View.GONE
                recyclerNordBatiments.visibility = View.VISIBLE
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
     * Filtre dynamiquement la liste des bâtiments selon le texte de recherche saisi.
     *
     * @param query Le mot-clé saisi dans la barre de recherche
     */
    override fun onSearch(query: String) {
        filteredList =
            batiments.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}