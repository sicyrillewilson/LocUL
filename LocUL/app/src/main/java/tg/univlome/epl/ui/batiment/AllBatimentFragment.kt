@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.batiment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.MainActivity
import tg.univlome.epl.R
import tg.univlome.epl.adapter.BatimentFragmentAdapter
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.services.BatimentService
import tg.univlome.epl.ui.SearchBarFragment

class AllBatimentFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var batiments: MutableList<Batiment>
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter

    private lateinit var batimentService: BatimentService

    //Pour la localisation
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_batiment, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getUserLocation()

        batimentService = BatimentService()
        batiments = mutableListOf()
        filteredList = mutableListOf()

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
                updateBatimentsDistances(userGeoPoint)
            }
        }
    }

    private fun updateBatimentsDistances(userLocation: GeoPoint) {
        Log.d("AllBatimentFragement", "updateBatimentsDistances appelée avec : $userLocation")
        // Charger les bâtiments
        batimentService.getBatiments().observe(viewLifecycleOwner, Observer { bats ->
            if (bats != null) {
                for (batiment in bats) {
                    try {
                        val batimentLocation = GeoPoint(batiment.latitude.toDouble(), batiment.longitude.toDouble())
                        val distance = calculateDistance(userLocation, batimentLocation)
                        Log.d("AllBatimentFragement", "Distance calculée pour ${batiment.nom}: $distance mètres")

                        // Mettre à jour l'objet Batiment avec la nouvelle distance
                        batiment.distance = String.format("%.2f m", distance)

                        Log.d("AllBatimentFragement", "Distance mise à jour pour ${batiment.nom}: ${batiment.distance}")

                    } catch (e: Exception) {
                        Log.e("AllBatimentFragement", "Erreur lors de la mise à jour de la distance pour ${batiment.nom}", e)
                    }
                    //ajouterLieuSurCarte(batiment)
                    batiments.add(batiment)
                }
            }
            filteredList = batiments.toMutableList()

            adapter = BatimentFragmentAdapter(batiments)
            val recyclerAllBatiments = view?.findViewById<RecyclerView>(R.id.recyclerAllBatiments)
            recyclerAllBatiments?.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recyclerAllBatiments?.adapter = adapter
        })
    }

    private fun calculateDistance(start: GeoPoint, end: GeoPoint): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0].toDouble() // Retourne la distance en mètres
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