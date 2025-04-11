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
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.adapter.BatimentAdapter
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.adapter.InfraAdapter
import tg.univlome.epl.models.Salle
import tg.univlome.epl.adapter.SalleAdapter
import tg.univlome.epl.models.modelsfragments.HomeFragmentModel
import tg.univlome.epl.utils.HomeBatimentUtils
import tg.univlome.epl.utils.HomeInfraUtils
import tg.univlome.epl.utils.HomeSalleUtils

class HomeFragment : Fragment() {

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


    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val fragmentManager = requireActivity().supportFragmentManager

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        batimentsEns = mutableListOf()
        batimentsEnsFilteredList = mutableListOf()
        batimentsEnsAdapter = BatimentAdapter(batimentsEns, fragmentManager, ViewAllBatEnsFragment())

        batimentsAdmin = mutableListOf()
        batimentsAdminFilteredList = mutableListOf()
        batimentsAdminAdapter = BatimentAdapter(batimentsAdmin, fragmentManager, ViewAllBatAdminFragment())

        infras = mutableListOf()
        infrasFilteredList = mutableListOf()
        infrasAdapter = InfraAdapter(infras, fragmentManager, ViewAllInfraFragment())

        salles = mutableListOf()
        sallesFilteredList = mutableListOf()
        sallesAdapter = SalleAdapter(salles, fragmentManager, ViewAllSalleFragment())

        batsEnsHomeFragmentModel = HomeFragmentModel(view, requireContext(), requireActivity(), viewLifecycleOwner, R.id.recyclerBatiments, fragmentManager, ViewAllBatEnsFragment())
        batsAdminHomeFragmentModel = HomeFragmentModel(view, requireContext(), requireActivity(), viewLifecycleOwner, R.id.recyclerBatimentsAdmin, fragmentManager, ViewAllBatAdminFragment())
        sallesHomeFragmentModel = HomeFragmentModel(view, requireContext(), requireActivity(), viewLifecycleOwner, R.id.recyclerSalles, fragmentManager, ViewAllSalleFragment())
        infrasHomeFragmentModel = HomeFragmentModel(view, requireContext(), requireActivity(), viewLifecycleOwner, R.id.recyclerInfra, fragmentManager, ViewAllInfraFragment())

        getUserLocation()
        return view
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val userGeoPoint = GeoPoint(it.latitude, it.longitude)
                HomeBatimentUtils.updateBatiments(userGeoPoint, batimentsEns, batimentsEnsFilteredList, batimentsEnsAdapter, batsEnsHomeFragmentModel)
                HomeBatimentUtils.updateBatiments(userGeoPoint, batimentsAdmin, batimentsAdminFilteredList, batimentsAdminAdapter, batsAdminHomeFragmentModel)
                HomeInfraUtils.updateInfrastructures(userGeoPoint, infras, infrasFilteredList, infrasAdapter, infrasHomeFragmentModel)
                HomeSalleUtils.updateSalles(userGeoPoint, salles, sallesFilteredList, sallesAdapter, sallesHomeFragmentModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).showSearchBarFragment(null) // Cacher la barre si on quitte
    }
}