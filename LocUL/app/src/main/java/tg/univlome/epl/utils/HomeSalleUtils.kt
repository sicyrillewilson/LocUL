package tg.univlome.epl.utils

import androidx.fragment.app.FragmentActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.R
import tg.univlome.epl.adapter.SalleAdapter
import tg.univlome.epl.models.Salle
import tg.univlome.epl.models.modelsfragments.HomeFragmentModel
import tg.univlome.epl.services.SalleService
import tg.univlome.epl.ui.maps.MapsFragment

object HomeSalleUtils {

    private lateinit var salleService: SalleService
    private lateinit var filteredList: MutableList<Salle>
    private lateinit var salles: MutableList<Salle>
    private lateinit var adapter: SalleAdapter

    fun ouvrirMapsFragment(salle: Salle, fragmentActivity: FragmentActivity) {
        val fragment = MapsFragment()
        val bundle = Bundle()
        bundle.putDouble("latitude", salle.latitude.toDouble())
        bundle.putDouble("longitude", salle.longitude.toDouble())
        fragment.arguments = bundle

        fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    fun updateSalles(userLocation: GeoPoint, salles: MutableList<Salle>, filteredList: MutableList<Salle>, adapter: SalleAdapter, homeFragmentModel: HomeFragmentModel) {
        salleService = SalleService()
        this.filteredList = filteredList
        this.salles = salles
        this.adapter = adapter

        Log.d("HomeSalleUtils", "updateSalles appelée avec : $userLocation")
        // Charger les bâtiments
        salleService.getSalles().observe(homeFragmentModel.viewLifecycleOwner, Observer { sals ->
            if (sals != null) {
                for (salle in sals) {
                    try {
                        val salleLocation = GeoPoint(salle.latitude.toDouble(), salle.longitude.toDouble())
                        val distance = MapsUtils.calculateDistance(userLocation, salleLocation)
                        Log.d("HomeSalleUtils", "Distance calculée pour ${salle.nom}: $distance mètres")

                        // Conversion en km si la distance dépasse 1000 m
                        val formattedDistance = if (distance >= 1000) {
                            String.format("%.2f km", distance / 1000)
                        } else {
                            String.format("%.2f m", distance)
                        }

                        salle.distance = formattedDistance

                        Log.d("HomeSalleUtils", "Distance mise à jour pour ${salle.nom}: ${salle.distance}")

                    } catch (e: Exception) {
                        Log.e("HomeSalleUtils", "Erreur lors de la mise à jour de la distance pour ${salle.nom}", e)
                    }
                    salles.add(salle)
                }
            }
            this.filteredList = salles.toMutableList()

            this.adapter = SalleAdapter(salles, homeFragmentModel.fragmentManager, homeFragmentModel.newFragment) { salle ->
                ouvrirMapsFragment(salle, homeFragmentModel.fragmentActivity)
            }

            val recyclerBatiments = homeFragmentModel.view?.findViewById<RecyclerView>(homeFragmentModel.recyclerViewId)
            recyclerBatiments?.layoutManager =
                LinearLayoutManager(homeFragmentModel.fragmentContext, LinearLayoutManager.HORIZONTAL, false)
            recyclerBatiments?.adapter = adapter
        })
    }
}