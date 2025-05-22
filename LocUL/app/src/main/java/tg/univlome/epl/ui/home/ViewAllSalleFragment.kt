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
import tg.univlome.epl.adapter.SalleViewAllAdapter
import tg.univlome.epl.models.Salle
import tg.univlome.epl.models.modelsfragments.FragmentModel
import tg.univlome.epl.services.InfrastructureService
import tg.univlome.epl.ui.SearchBarFragment
import tg.univlome.epl.utils.SalleUtils

/**
 * Fragment ViewAllSalleFragment : Affiche la liste complète des salles
 *
 * Description :
 * Ce fragment permet d'afficher toutes les salles présentes sur le campus universitaire,
 * qu'elles soient d'enseignement, d'administration ou polyvalentes. Les données sont
 * récupérées depuis `SalleUtils`, enrichies par la distance depuis l'utilisateur, et
 * affichées grâce à un `SalleViewAllAdapter`.
 *
 * Il offre également une fonctionnalité de recherche en temps réel sur le nom des salles.
 *
 * Composants principaux :
 * - SalleUtils : utilitaire pour le chargement et la mise à jour des salles
 * - SalleViewAllAdapter : adaptateur pour l'affichage des salles dans une RecyclerView
 * - FragmentModel : modèle de configuration du fragment
 * - SearchBarFragment : barre de recherche dynamique
 *
 * Bibliothèques utilisées :
 * - Google Play Services : récupération de la position GPS
 * - OSMDroid : manipulation de coordonnées géographiques
 *
 * @see SalleUtils pour la gestion des données de salle
 * @see SearchBarFragment pour les fonctionnalités de recherche utilisateur
 */
class ViewAllSalleFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var salles: MutableList<Salle>
    private lateinit var filteredList: MutableList<Salle>
    private lateinit var adapter: SalleViewAllAdapter

    private lateinit var infraService: InfrastructureService

    private lateinit var fragmentModel: FragmentModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Initialise la vue du fragment, configure les composants et déclenche
     * la récupération de la localisation de l’utilisateur.
     *
     * @return Vue racine du fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_all_salle, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        infraService = InfrastructureService(requireContext())
        salles = mutableListOf()
        filteredList = mutableListOf()
        adapter = SalleViewAllAdapter(salles)

        fragmentModel = FragmentModel(
            view,
            requireContext(),
            requireActivity(),
            viewLifecycleOwner,
            R.id.recyclerAllSalle
        )
        getUserLocation()

        return view
    }

    /**
     * Récupère la position actuelle de l’utilisateur et met à jour la liste des salles
     * avec les distances correspondantes.
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
                SalleUtils.updateSalles(userGeoPoint, salles, filteredList, adapter, fragmentModel)
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
     * Filtre dynamiquement la liste des salles selon la recherche utilisateur.
     *
     * @param query Texte de recherche fourni par l'utilisateur
     */
    override fun onSearch(query: String) {
        filteredList = salles.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}