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

        fragmentModel = FragmentModel(view, requireContext(), requireActivity(), viewLifecycleOwner, R.id.recyclerSudInfra, "sud")
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
                    shimmerSudInfra.stopShimmer()
                    shimmerSudInfra.visibility = View.GONE
                    recyclerSudInfra.visibility = View.VISIBLE
                }

                InfraUtils.updateInfrastructures(userGeoPoint, infrasSud, filteredList, adapter, fragmentModel) {
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
        filteredList = infrasSud.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }
    
}