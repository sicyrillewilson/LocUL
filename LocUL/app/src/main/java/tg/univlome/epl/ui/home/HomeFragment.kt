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
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.MainActivity
import tg.univlome.epl.R
import tg.univlome.epl.adapter.BatimentAdapter
import tg.univlome.epl.adapter.InfraAdapter
import tg.univlome.epl.adapter.SalleAdapter
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.models.Salle
import tg.univlome.epl.models.modelsfragments.HomeFragmentModel
import tg.univlome.epl.ui.LogoFragment
import tg.univlome.epl.utils.HomeBatimentUtils
import tg.univlome.epl.utils.HomeInfraUtils
import tg.univlome.epl.utils.HomeSalleUtils

class HomeFragment : Fragment(), LogoFragment.LogoListener {

    private lateinit var batimentsEns: MutableList<Batiment>
    private lateinit var batimentsEnsFilteredList: MutableList<Batiment>
    private lateinit var batimentsEnsAdapter: BatimentAdapter

    private lateinit var batimentsAdmin: MutableList<Batiment>
    private lateinit var batimentsAdminFilteredList: MutableList<Batiment>
    private lateinit var batimentsAdminAdapter: BatimentAdapter

    private lateinit var infras: MutableList<Infrastructure>
    private lateinit var infrasFilteredList: MutableList<Infrastructure>
    private lateinit var infrasAdapter: InfraAdapter

    private lateinit var salles: MutableList<Salle>
    private lateinit var sallesFilteredList: MutableList<Salle>
    private lateinit var sallesAdapter: SalleAdapter

    private lateinit var batsEnsHomeFragmentModel: HomeFragmentModel
    private lateinit var batsAdminHomeFragmentModel: HomeFragmentModel
    private lateinit var sallesHomeFragmentModel: HomeFragmentModel
    private lateinit var infrasHomeFragmentModel: HomeFragmentModel

    // Shimmer FrameLayouts
    private lateinit var shimmerBatimentsEns: ShimmerFrameLayout
    private lateinit var shimmerBatimentsAdmin: ShimmerFrameLayout
    private lateinit var shimmerSalles: ShimmerFrameLayout
    private lateinit var shimmerInfra: ShimmerFrameLayout

    // RecyclerViews
    private lateinit var recyclerBatimentsEns: RecyclerView
    private lateinit var recyclerBatimentsAdmin: RecyclerView
    private lateinit var recyclerSalles: RecyclerView
    private lateinit var recyclerInfra: RecyclerView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialiser les shimmer layouts et recyclerviews
        initializeViews(rootView!!)

        // Montrer les shimmer et cacher les recyclerviews
        showShimmer()

        // Charger les données
        loadData(rootView!!)

        return rootView
    }

    private fun initializeViews(view: View) {
        // Initialiser les shimmer layouts
        shimmerBatimentsEns = view.findViewById(R.id.shimmerBatimentsEnsContainer)
        shimmerBatimentsAdmin = view.findViewById(R.id.shimmerBatimentsAdminContainer)
        shimmerSalles = view.findViewById(R.id.shimmerSalleContainer)
        shimmerInfra = view.findViewById(R.id.shimmerInfraContainer)

        // Initialiser les recyclerviews
        recyclerBatimentsEns = view.findViewById(R.id.recyclerBatiments)
        recyclerBatimentsAdmin = view.findViewById(R.id.recyclerBatimentsAdmin)
        recyclerSalles = view.findViewById(R.id.recyclerSalles)
        recyclerInfra = view.findViewById(R.id.recyclerInfra)
    }

    private fun showShimmer() {
        // Démarrer les animations shimmer
        shimmerBatimentsEns.startShimmer()
        shimmerBatimentsAdmin.startShimmer()
        shimmerSalles.startShimmer()
        shimmerInfra.startShimmer()
    }

    private fun hideShimmer() {
        // Arrêter les animations shimmer
        shimmerBatimentsEns.stopShimmer()
        shimmerBatimentsAdmin.stopShimmer()
        shimmerSalles.stopShimmer()
        shimmerInfra.stopShimmer()

        // Cacher les shimmer layouts
        shimmerBatimentsEns.visibility = View.GONE
        shimmerBatimentsAdmin.visibility = View.GONE
        shimmerSalles.visibility = View.GONE
        shimmerInfra.visibility = View.GONE

        // Montrer les recyclerviews
        recyclerBatimentsEns.visibility = View.VISIBLE
        recyclerBatimentsAdmin.visibility = View.VISIBLE
        recyclerSalles.visibility = View.VISIBLE
        recyclerInfra.visibility = View.VISIBLE
    }

    fun loadData(view: View) {
        val fragmentManager = requireActivity().supportFragmentManager

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        batimentsEns = mutableListOf()
        batimentsEnsFilteredList = mutableListOf()
        batimentsEnsAdapter =
            BatimentAdapter(batimentsEns, fragmentManager, ViewAllBatEnsFragment())

        batimentsAdmin = mutableListOf()
        batimentsAdminFilteredList = mutableListOf()
        batimentsAdminAdapter =
            BatimentAdapter(batimentsAdmin, fragmentManager, ViewAllBatAdminFragment())

        infras = mutableListOf()
        infrasFilteredList = mutableListOf()
        infrasAdapter = InfraAdapter(infras, fragmentManager, ViewAllInfraFragment())

        salles = mutableListOf()
        sallesFilteredList = mutableListOf()
        sallesAdapter = SalleAdapter(salles, fragmentManager, ViewAllSalleFragment())

        batsEnsHomeFragmentModel = HomeFragmentModel(
            view,
            requireContext(),
            requireActivity(),
            viewLifecycleOwner,
            R.id.recyclerBatiments,
            fragmentManager,
            ViewAllBatEnsFragment(),
            "enseignement"
        )
        batsAdminHomeFragmentModel = HomeFragmentModel(
            view,
            requireContext(),
            requireActivity(),
            viewLifecycleOwner,
            R.id.recyclerBatimentsAdmin,
            fragmentManager,
            ViewAllBatAdminFragment(),
            "administratif"
        )
        sallesHomeFragmentModel = HomeFragmentModel(
            view,
            requireContext(),
            requireActivity(),
            viewLifecycleOwner,
            R.id.recyclerSalles,
            fragmentManager,
            ViewAllSalleFragment()
        )
        infrasHomeFragmentModel = HomeFragmentModel(
            view,
            requireContext(),
            requireActivity(),
            viewLifecycleOwner,
            R.id.recyclerInfra,
            fragmentManager,
            ViewAllInfraFragment()
        )

        getUserLocation()
    }

    // Méthode pour recharger les données
    fun rechargerDonnees() {
        // Afficher à nouveau les shimmer pendant le rechargement
        rootView?.let {
            showShimmer()
            loadData(it)
        }
    }

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

                // Créer un compteur pour suivre les chargements terminés
                var loadedSections = 0
                val totalSections = 4 // 4 sections à charger

                val onDataLoadedCallback = {
                    loadedSections++
                    if (loadedSections >= totalSections) {
                        hideShimmer()
                    }
                }

                // Modification pour notifier quand les données sont chargées
                HomeBatimentUtils.updateBatiments(
                    userGeoPoint,
                    batimentsEns,
                    batimentsEnsFilteredList,
                    batimentsEnsAdapter,
                    batsEnsHomeFragmentModel
                ) {
                    onDataLoadedCallback()
                }

                HomeBatimentUtils.updateBatiments(
                    userGeoPoint,
                    batimentsAdmin,
                    batimentsAdminFilteredList,
                    batimentsAdminAdapter,
                    batsAdminHomeFragmentModel
                ) {
                    onDataLoadedCallback()
                }

                HomeInfraUtils.updateInfrastructures(
                    userGeoPoint,
                    infras,
                    infrasFilteredList,
                    infrasAdapter,
                    infrasHomeFragmentModel
                ) {
                    onDataLoadedCallback()
                }

                HomeSalleUtils.updateSalles(
                    userGeoPoint,
                    salles,
                    sallesFilteredList,
                    sallesAdapter,
                    sallesHomeFragmentModel
                ) {
                    onDataLoadedCallback()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).showSearchBarFragment(null) // Cacher la barre si on quitte
    }
}