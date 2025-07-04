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
 * Fragment SudInfraFragment : Affiche les infrastructures du campus sud
 *
 * Description :
 * Ce fragment permet de lister dynamiquement toutes les infrastructures situées sur
 * le **campus sud**. Il s’appuie sur les services de géolocalisation pour calculer
 * la distance entre l’utilisateur et chaque infrastructure, et affiche les résultats
 * dans une `RecyclerView` accompagnée d’un effet Shimmer pendant le chargement.
 *
 * Il intègre également une barre de recherche via `SearchBarFragment` pour permettre
 * un filtrage en temps réel.
 *
 * Composants principaux :
 * - `InfraUtils` : Fournit les méthodes de récupération, filtrage et mise à jour des données
 * - `InfraFragmentAdapter` : Adaptateur pour l’affichage des infrastructures
 * - `FragmentModel` : Modèle contenant le contexte, la vue, l’activité, etc.
 *
 * Bibliothèques utilisées :
 * - Google Play Services (`FusedLocationProviderClient`) : géolocalisation de l’utilisateur
 * - OSMDroid : manipulation des coordonnées géographiques
 * - Facebook Shimmer : effet de chargement visuel avant l’affichage des données
 *
 * @see InfraUtils pour le traitement des infrastructures
 * @see SearchBarFragment pour la gestion de la recherche dynamique
 */
class SudInfraFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var infrasSud: MutableList<Infrastructure>
    private lateinit var filteredList: MutableList<Infrastructure>
    private lateinit var adapter: InfraFragmentAdapter
    private lateinit var infraService: InfrastructureService
    private lateinit var fragmentModel: FragmentModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var shimmerSudInfra: ShimmerFrameLayout
    private lateinit var recyclerSudInfra: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Crée et retourne la vue du fragment.
     * Initialise les composants visuels et déclenche la récupération de la position utilisateur.
     *
     * @return Vue complète du fragment initialisé
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sud_infra, container, false)

        shimmerSudInfra = view.findViewById(R.id.shimmerSudInfra)
        recyclerSudInfra = view.findViewById(R.id.recyclerSudInfra)
        shimmerSudInfra.startShimmer()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        infraService = InfrastructureService(requireContext())
        infrasSud = mutableListOf()
        filteredList = mutableListOf()
        adapter = InfraFragmentAdapter(infrasSud)

        fragmentModel = FragmentModel(
            view,
            requireContext(),
            requireActivity(),
            viewLifecycleOwner,
            R.id.recyclerSudInfra,
            "sud"
        )
        getUserLocation()
        return view
    }

    /**
     * Récupère la localisation de l'utilisateur.
     * Appelle `InfraUtils.updateInfrastructures` pour charger les données du campus sud.
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
                shimmerSudInfra.stopShimmer()
                shimmerSudInfra.visibility = View.GONE
                recyclerSudInfra.visibility = View.VISIBLE
            }

            InfraUtils.updateInfrastructures(
                userGeoPoint,
                infrasSud,
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
     * Filtre les infrastructures affichées selon une recherche textuelle.
     *
     * @param query Texte à rechercher
     */
    override fun onSearch(query: String) {
        filteredList =
            infrasSud.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}