package tg.univlome.epl.utils

import androidx.fragment.app.FragmentActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.R
import tg.univlome.epl.adapter.BatimentAdapter
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.models.modelsfragments.HomeFragmentModel
import tg.univlome.epl.services.BatimentService
import tg.univlome.epl.ui.maps.MapsFragment

object HomeBatimentUtils {

    private lateinit var batimentService: BatimentService
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var batiments: MutableList<Batiment>
    private lateinit var adapter: BatimentAdapter

    fun ouvrirMapsFragment(batiment: Batiment, fragmentActivity: FragmentActivity) {
        val fragment = MapsFragment()
        val bundle = Bundle()
        bundle.putDouble("latitude", batiment.latitude.toDouble())
        bundle.putDouble("longitude", batiment.longitude.toDouble())
        fragment.arguments = bundle

        fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    fun updateBatiments(userLocation: GeoPoint, batiments: MutableList<Batiment>, filteredList: MutableList<Batiment>, adapter: BatimentAdapter, homeFragmentModel: HomeFragmentModel) {
        batimentService = BatimentService()
        this.filteredList = filteredList
        this.batiments = batiments
        this.adapter = adapter

        Log.d("HomeBatimentUtils", "updateBatiments appelée avec : $userLocation")
        // Charger les bâtiments
        batimentService.getBatiments().observe(homeFragmentModel.viewLifecycleOwner, Observer { bats ->
            if (bats != null) {
                for (batiment in bats) {
                    if (batiment.type.lowercase() == homeFragmentModel.type.lowercase()) {
                        try {
                            val batimentLocation = GeoPoint(batiment.latitude.toDouble(), batiment.longitude.toDouble())
                            val distance = MapsUtils.calculateDistance(userLocation, batimentLocation)
                            Log.d("HomeBatimentUtils", "Distance calculée pour ${batiment.nom}: $distance mètres")

                            // Conversion en km si la distance dépasse 1000 m
                            val formattedDistance = if (distance >= 1000) {
                                String.format("%.2f km", distance / 1000)
                            } else {
                                String.format("%.2f m", distance)
                            }

                            batiment.distance = formattedDistance

                            Log.d("HomeBatimentUtils", "Distance mise à jour pour ${batiment.nom}: ${batiment.distance}")

                        } catch (e: Exception) {
                            Log.e("HomeBatimentUtils", "Erreur lors de la mise à jour de la distance pour ${batiment.nom}", e)
                        }
                        batiments.add(batiment)
                    }
                }
            }
            this.filteredList = batiments.toMutableList()

            this.adapter = BatimentAdapter(batiments, homeFragmentModel.fragmentManager, homeFragmentModel.newFragment)

            val recyclerBatiments = homeFragmentModel.view?.findViewById<RecyclerView>(homeFragmentModel.recyclerViewId)
            recyclerBatiments?.layoutManager =
                LinearLayoutManager(homeFragmentModel.fragmentContext, LinearLayoutManager.HORIZONTAL, false)
            recyclerBatiments?.adapter = adapter
        })
    }
}