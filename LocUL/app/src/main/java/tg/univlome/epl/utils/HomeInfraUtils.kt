package tg.univlome.epl.utils

import androidx.fragment.app.FragmentActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.R
import tg.univlome.epl.adapter.InfraAdapter
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.models.modelsfragments.HomeFragmentModel
import tg.univlome.epl.services.InfrastructureService
import tg.univlome.epl.ui.maps.MapsFragment

object HomeInfraUtils {

    private lateinit var infrastructureService: InfrastructureService
    private lateinit var filteredList: MutableList<Infrastructure>
    private lateinit var infrastructures: MutableList<Infrastructure>
    private lateinit var adapter: InfraAdapter

    fun ouvrirMapsFragment(infrastructure: Infrastructure, fragmentActivity: FragmentActivity) {
        val fragment = MapsFragment()
        val bundle = Bundle()
        bundle.putDouble("latitude", infrastructure.latitude.toDouble())
        bundle.putDouble("longitude", infrastructure.longitude.toDouble())
        fragment.arguments = bundle

        fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    fun updateInfrastructures(userLocation: GeoPoint, infrastructures: MutableList<Infrastructure>, filteredList: MutableList<Infrastructure>, adapter: InfraAdapter, homeFragmentModel: HomeFragmentModel) {
        infrastructureService = InfrastructureService()
        this.filteredList = filteredList
        this.infrastructures = infrastructures
        this.adapter = adapter

        Log.d("HomeInfraUtils", "updateInfrastructures appelée avec : $userLocation")
        // Charger les bâtiments
        infrastructureService.getInfrastructures().observe(homeFragmentModel.viewLifecycleOwner, Observer { infras ->
            if (infras != null) {
                for (infrastructure in infras) {
                    try {
                        val infrastructureLocation = GeoPoint(infrastructure.latitude.toDouble(), infrastructure.longitude.toDouble())
                        val distance = MapsUtils.calculateDistance(userLocation, infrastructureLocation)
                        Log.d("HomeInfraUtils", "Distance calculée pour ${infrastructure.nom}: $distance mètres")

                        // Conversion en km si la distance dépasse 1000 m
                        val formattedDistance = if (distance >= 1000) {
                            String.format("%.2f km", distance / 1000)
                        } else {
                            String.format("%.2f m", distance)
                        }

                        infrastructure.distance = formattedDistance

                        Log.d("HomeInfraUtils", "Distance mise à jour pour ${infrastructure.nom}: ${infrastructure.distance}")

                    } catch (e: Exception) {
                        Log.e("HomeInfraUtils", "Erreur lors de la mise à jour de la distance pour ${infrastructure.nom}", e)
                    }
                    infrastructures.add(infrastructure)
                }
            }
            this.filteredList = infrastructures.toMutableList()

            this.adapter = InfraAdapter(infrastructures, homeFragmentModel.fragmentManager, homeFragmentModel.newFragment)

            val recyclerBatiments = homeFragmentModel.view?.findViewById<RecyclerView>(homeFragmentModel.recyclerViewId)
            recyclerBatiments?.layoutManager =
                LinearLayoutManager(homeFragmentModel.fragmentContext, LinearLayoutManager.HORIZONTAL, false)
            recyclerBatiments?.adapter = adapter
        })
    }
}