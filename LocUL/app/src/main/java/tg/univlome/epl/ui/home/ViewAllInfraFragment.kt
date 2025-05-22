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
import tg.univlome.epl.adapter.InfraFragmentAdapter
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.models.modelsfragments.FragmentModel
import tg.univlome.epl.services.InfrastructureService
import tg.univlome.epl.ui.SearchBarFragment
import tg.univlome.epl.utils.InfraUtils

/**
 * Fragment ViewAllInfraFragment : Affiche la liste complète des infrastructures
 *
 * Description :
 * Ce fragment permet d’afficher toutes les infrastructures présentes sur le campus
 * universitaire. Il récupère les données via `InfrastructureService` et utilise
 * `InfraFragmentAdapter` pour les présenter sous forme de liste.
 *
 * Il calcule dynamiquement la distance entre l’utilisateur et chaque infrastructure,
 * et permet la recherche en temps réel sur le nom des infrastructures.
 *
 * Composants principaux :
 * - RecyclerView : affichage de la liste des infrastructures
 * - FusedLocationProviderClient : localisation de l’utilisateur
 * - InfraFragmentAdapter : adaptateur des infrastructures
 * - FragmentModel : modèle de configuration du fragment
 * - SearchBarFragment : interface de recherche utilisateur
 *
 * Bibliothèques utilisées :
 * - Google Play Services : pour la géolocalisation
 * - OSMDroid : gestion des coordonnées géographiques
 *
 * @see InfraUtils pour les fonctions utilitaires liées aux infrastructures
 * @see SearchBarFragment pour la recherche interactive
 */
class ViewAllInfraFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var infras: MutableList<Infrastructure>
    private lateinit var filteredList: MutableList<Infrastructure>
    private lateinit var adapter: InfraFragmentAdapter

    private lateinit var infraService: InfrastructureService

    private lateinit var fragmentModel: FragmentModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Initialise la vue du fragment et configure les composants nécessaires,
     * y compris la localisation, l’adaptateur et le modèle de fragment.
     *
     * @return Vue racine du fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_all_infra, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        infraService = InfrastructureService(requireContext())
        infras = mutableListOf()
        filteredList = mutableListOf()
        adapter = InfraFragmentAdapter(infras)

        fragmentModel = FragmentModel(
            view,
            requireContext(),
            requireActivity(),
            viewLifecycleOwner,
            R.id.recyclerAllInfraHome
        )
        getUserLocation()

        return view
    }

    /**
     * Récupère la position actuelle de l'utilisateur et met à jour la liste
     * des infrastructures avec la distance calculée.
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
                InfraUtils.updateInfrastructures(
                    userGeoPoint,
                    infras,
                    filteredList,
                    adapter,
                    fragmentModel
                )
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
     * Met à jour dynamiquement la liste affichée en fonction du texte saisi.
     *
     * @param query Texte de recherche fourni par l'utilisateur
     */
    override fun onSearch(query: String) {
        filteredList = infras.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}