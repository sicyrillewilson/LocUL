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

class AllInfraFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var infrasAll: MutableList<Infrastructure>
    private lateinit var filteredList: MutableList<Infrastructure>
    private lateinit var adapter: InfraFragmentAdapter
    private lateinit var infraService: InfrastructureService
    private lateinit var fragmentModel: FragmentModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var shimmerAllInfra: ShimmerFrameLayout
    private lateinit var recyclerAllInfra: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_infra, container, false)

        shimmerAllInfra = view.findViewById(R.id.shimmerAllInfra)
        recyclerAllInfra = view.findViewById(R.id.recyclerAllInfra)
        shimmerAllInfra.startShimmer()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        infraService = InfrastructureService(requireContext())
        infrasAll = mutableListOf()
        filteredList = mutableListOf()
        adapter = InfraFragmentAdapter(infrasAll)

        fragmentModel = FragmentModel(view, requireContext(), requireActivity(), viewLifecycleOwner, R.id.recyclerAllInfra)
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
                val onDataLoadedCallback = {
                    shimmerAllInfra.stopShimmer()
                    shimmerAllInfra.visibility = View.GONE
                    recyclerAllInfra.visibility = View.VISIBLE
                }

                InfraUtils.updateInfrastructures(userGeoPoint, infrasAll, filteredList, adapter, fragmentModel) {
                    onDataLoadedCallback()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showSearchBarFragment(this)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).showSearchBarFragment(null) // Cacher la barre si on quitte
    }

    override fun onSearch(query: String) {
        filteredList = infrasAll.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}