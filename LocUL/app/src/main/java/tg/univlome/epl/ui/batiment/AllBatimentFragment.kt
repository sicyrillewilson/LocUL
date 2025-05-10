@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.batiment

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
import tg.univlome.epl.adapter.BatimentFragmentAdapter
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.models.modelsfragments.FragmentModel
import tg.univlome.epl.services.BatimentService
import tg.univlome.epl.ui.SearchBarFragment
import tg.univlome.epl.utils.BatimentUtils

class AllBatimentFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var batiments: MutableList<Batiment>
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter
    private lateinit var batimentService: BatimentService
    private lateinit var fragmentModel: FragmentModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var shimmerAllBatiments: ShimmerFrameLayout
    private lateinit var recyclerAllBatiments: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_batiment, container, false)

        shimmerAllBatiments = view.findViewById(R.id.shimmerAllBatiments)
        recyclerAllBatiments = view.findViewById(R.id.recyclerAllBatiments)
        shimmerAllBatiments.startShimmer()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        batimentService = BatimentService(requireContext())
        batiments = mutableListOf()
        filteredList = mutableListOf()
        adapter = BatimentFragmentAdapter(batiments)

        fragmentModel = FragmentModel(view, requireContext(), requireActivity(), viewLifecycleOwner, R.id.recyclerAllBatiments)
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
                    shimmerAllBatiments.stopShimmer()
                    shimmerAllBatiments.visibility = View.GONE
                    recyclerAllBatiments.visibility = View.VISIBLE
                }

                BatimentUtils.updateBatiments(userGeoPoint, batiments, filteredList, adapter, fragmentModel) {
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
        filteredList = batiments.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}