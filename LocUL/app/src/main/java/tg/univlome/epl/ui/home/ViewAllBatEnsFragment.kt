@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
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
 * Fragment ViewAllBatEnsFragment : Affiche la liste complète des bâtiments d’enseignement
 *
 * Description :
 * Ce fragment permet la visualisation de tous les bâtiments liés à l’enseignement
 * sur le campus universitaire. Il récupère les données depuis `BatimentService` et
 * les affiche à l’aide d’un `RecyclerView` géré par `BatimentFragmentAdapter`.
 *
 * Il utilise la géolocalisation de l’utilisateur pour calculer la distance avec
 * chaque bâtiment via `MapsUtils`, et permet une recherche dynamique sur le nom
 * des bâtiments grâce à `SearchBarFragment`.
 *
 * Composants principaux :
 * - `RecyclerView` : affiche la liste complète des bâtiments d’enseignement
 * - `FusedLocationProviderClient` : pour localiser l’utilisateur
 * - `BatimentFragmentAdapter` : adaptateur d’affichage des bâtiments
 * - `FragmentModel` : modèle contenant les informations du fragment
 * - `SearchBarFragment` : barre de recherche dynamique
 *
 * Bibliothèques utilisées :
 * - Google Play Services pour la localisation
 * - OSMDroid pour les coordonnées
 *
 * @see BatimentUtils pour le traitement des données de bâtiment
 * @see MapsUtils pour le calcul des distances géographiques
 * @see SearchBarFragment pour la recherche utilisateur
 */
class ViewAllBatEnsFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var batimentsEns: MutableList<Batiment>
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter

    private lateinit var batimentService: BatimentService

    private lateinit var fragmentModel: FragmentModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Initialise la vue du fragment, configure la localisation, le modèle de fragment
     * et déclenche la récupération des données de bâtiments d’enseignement.
     *
     * @return Vue construite pour le fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_all_bat_ens, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        batimentService = BatimentService(requireContext())
        batimentsEns = mutableListOf()
        filteredList = mutableListOf()
        adapter = BatimentFragmentAdapter(batimentsEns)

        fragmentModel = FragmentModel(
            view,
            requireContext(),
            requireActivity(),
            viewLifecycleOwner,
            R.id.recyclerAllBatimentsEns
        )
        fragmentModel.type = "enseignement"
        getUserLocation()

        return view
    }

    /**
     * Récupère la localisation actuelle de l’utilisateur pour permettre
     * le calcul de la distance entre l’utilisateur et chaque bâtiment.
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
            BatimentUtils.updateBatiments(
                userGeoPoint,
                batimentsEns,
                filteredList,
                adapter,
                fragmentModel
            )
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
     * Filtre dynamiquement la liste des bâtiments affichés selon le texte
     * saisi par l’utilisateur dans la barre de recherche.
     *
     * @param query Texte recherché
     */
    override fun onSearch(query: String) {
        filteredList =
            batimentsEns.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }
}