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

/**
 * Fragment NordInfraFragment : Affiche les infrastructures situées au nord du campus
 *
 * Description :
 * Ce fragment permet d’afficher dynamiquement toutes les infrastructures localisées
 * sur le campus nord. Les données sont chargées à partir de `InfraUtils`, enrichies
 * avec la distance depuis la localisation actuelle de l’utilisateur et affichées
 * à l’aide d’un `InfraFragmentAdapter`.
 *
 * Il inclut également une barre de recherche permettant un filtrage en temps réel.
 * Un effet Shimmer est utilisé durant le chargement pour améliorer l’expérience utilisateur.
 *
 * Composants principaux :
 * - InfraUtils : utilitaire de chargement et filtrage des infrastructures
 * - InfraFragmentAdapter : adaptateur pour l’affichage dans la RecyclerView
 * - FragmentModel : modèle d’interaction entre la vue, le contexte et le cycle de vie
 *
 * Bibliothèques utilisées :
 * - Google Play Services : récupération de la géolocalisation
 * - OSMDroid : manipulation de coordonnées GPS
 * - Facebook Shimmer : effet de chargement visuel
 *
 * @see InfraUtils pour la logique métier et la mise à jour des données
 * @see SearchBarFragment pour la gestion de la recherche utilisateur
 */
class NordInfraFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var infrasNord: MutableList<Infrastructure>
    private lateinit var filteredList: MutableList<Infrastructure>
    private lateinit var adapter: InfraFragmentAdapter
    private lateinit var infraService: InfrastructureService
    private lateinit var fragmentModel: FragmentModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var shimmerNordInfra: ShimmerFrameLayout
    private lateinit var recyclerNordInfra: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Initialise et retourne la vue du fragment,
     * prépare les composants et déclenche la localisation utilisateur.
     *
     * @return Vue complète du fragment initialisé
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nord_infra, container, false)

        shimmerNordInfra = view.findViewById(R.id.shimmerNordInfra)
        recyclerNordInfra = view.findViewById(R.id.recyclerNordInfra)
        shimmerNordInfra.startShimmer()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        infraService = InfrastructureService(requireContext())
        infrasNord = mutableListOf()
        filteredList = mutableListOf()
        adapter = InfraFragmentAdapter(infrasNord)

        fragmentModel = FragmentModel(
            view,
            requireContext(),
            requireActivity(),
            viewLifecycleOwner,
            R.id.recyclerNordInfra,
            "nord"
        )
        getUserLocation()
        return view
    }

    /**
     * Récupère la géolocalisation de l’utilisateur et appelle
     * InfraUtils pour charger les infrastructures du campus nord.
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
                    shimmerNordInfra.stopShimmer()
                    shimmerNordInfra.visibility = View.GONE
                    recyclerNordInfra.visibility = View.VISIBLE
                }

                InfraUtils.updateInfrastructures(
                    userGeoPoint,
                    infrasNord,
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
     * Met à jour dynamiquement la liste affichée selon le texte saisi dans la barre de recherche.
     *
     * @param query Chaîne de caractères à rechercher dans les noms d'infrastructures
     */
    override fun onSearch(query: String) {
        filteredList =
            infrasNord.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}