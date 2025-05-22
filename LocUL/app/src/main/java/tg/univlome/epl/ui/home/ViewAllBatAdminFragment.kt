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

/**
 * Fragment ViewAllBatAdminFragment : Affiche tous les bâtiments à usage administratif
 *
 * Description :
 * Ce fragment est destiné à afficher l’ensemble des bâtiments classés comme *administratifs*
 * sur le campus universitaire. Il exploite :
 *  - la localisation de l’utilisateur pour calculer les distances
 *  - une `RecyclerView` pour afficher la liste complète
 *  - une `SearchBar` intégrée pour filtrer dynamiquement par nom
 *
 * Les données sont récupérées depuis un service distant (`BatimentService`) et
 * traitées via l’objet `BatimentUtils`, qui applique les règles de filtrage selon le type
 * et la situation géographique définis dans le `FragmentModel`.
 *
 * Composants principaux :
 *  - `RecyclerView` : liste scrollable des bâtiments
 *  - `BatimentFragmentAdapter` : adaptateur d’affichage
 *  - `FusedLocationProviderClient` : pour la récupération de la position GPS de l'utilisateur
 *  - `FragmentModel` : encapsule le contexte et les métadonnées nécessaires à l’affichage
 *
 * Bibliothèques utilisées :
 *  - Google Location Services (FusedLocationProviderClient)
 *  - OSMDroid pour la gestion des coordonnées géographiques
 *
 * @see BatimentUtils pour le traitement logique et les calculs de distances
 * @see BatimentFragmentAdapter pour l’affichage des éléments dans la liste
 * @see FragmentModel pour les métadonnées du fragment
 */
class ViewAllBatAdminFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var batimentsAdmin: MutableList<Batiment>
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter

    private lateinit var batimentService: BatimentService

    private lateinit var fragmentModel: FragmentModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Crée et initialise la vue du fragment, configure la `RecyclerView`, instancie le modèle
     * et lance la récupération de la localisation de l'utilisateur.
     *
     * @return Vue complète initialisée
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_all_bat_admin, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        batimentService = BatimentService(requireContext())
        batimentsAdmin = mutableListOf()
        filteredList = mutableListOf()
        adapter = BatimentFragmentAdapter(batimentsAdmin)

        fragmentModel = FragmentModel(
            view,
            requireContext(),
            requireActivity(),
            viewLifecycleOwner,
            R.id.recyclerAllBatimentsAdmin
        )
        fragmentModel.type = "administratif"
        getUserLocation()

        return view
    }

    /**
     * Récupère la localisation actuelle de l'utilisateur (si permission accordée) et
     * déclenche la mise à jour de la liste des bâtiments administratifs via `BatimentUtils`.
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
                BatimentUtils.updateBatiments(
                    userGeoPoint,
                    batimentsAdmin,
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
     * Filtre dynamiquement la liste des bâtiments administratifs en fonction du
     * nom saisi par l’utilisateur dans la barre de recherche.
     *
     * @param query Texte recherché
     */
    override fun onSearch(query: String) {
        filteredList =
            batimentsAdmin.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }
}