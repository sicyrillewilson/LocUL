package tg.univlome.epl.utils

import android.content.Context
import androidx.fragment.app.FragmentActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.R
import tg.univlome.epl.adapter.BatimentFragmentAdapter
import tg.univlome.epl.adapter.InfraFragmentAdapter
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.models.Salle
import tg.univlome.epl.models.modelsfragments.FragmentModel
import tg.univlome.epl.services.InfrastructureService
import tg.univlome.epl.ui.maps.MapsFragment

object InfraUtils {

    private lateinit var infraService: InfrastructureService
    private lateinit var filteredList: MutableList<Infrastructure>
    private lateinit var infras: MutableList<Infrastructure>
    private lateinit var adapter: InfraFragmentAdapter

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
    
    fun updateInfrastructures(userLocation: GeoPoint, infrastructures: MutableList<Infrastructure>, filteredList: MutableList<Infrastructure>, adapter: InfraFragmentAdapter, fragmentModel: FragmentModel, onDataLoaded: () -> Unit = {}) {
        infraService = InfrastructureService(fragmentModel.fragmentContext)
        this.filteredList = filteredList
        this.infras = infrastructures
        this.adapter = adapter

        Log.d("InfraUtils", "updateInfrastructures appelée avec : $userLocation")
        // Charger les bâtiments
        infraService.getInfrastructures().observe(fragmentModel.viewLifecycleOwner, Observer { infras ->
            if (infras != null) {
                for (infrastructure in infras) {
                    try {
                        val infrastructureLocation = GeoPoint(infrastructure.latitude.toDouble(), infrastructure.longitude.toDouble())
                        val distance = MapsUtils.calculateDistance(userLocation, infrastructureLocation)
                        Log.d("InfraUtils", "Distance calculée pour ${infrastructure.nom}: $distance mètres")

                        // Conversion en km si la distance dépasse 1000 m
                        val formattedDistance = if (distance >= 1000) {
                            String.format("%.2f km", distance / 1000)
                        } else {
                            String.format("%.2f m", distance)
                        }

                        infrastructure.distance = formattedDistance

                        Log.d("InfraUtils", "Distance mise à jour pour ${infrastructure.nom}: ${infrastructure.distance}")

                    } catch (e: Exception) {
                        Log.e("InfraUtils", "Erreur lors de la mise à jour de la distance pour ${infrastructure.nom}", e)
                    }
                    when (fragmentModel.situation) {
                        "" -> {
                            infrastructures.add(infrastructure)
                        }
                        "sud" -> {
                            if (infrastructure.situation == "Campus sud") {
                                infrastructures.add(infrastructure)
                            }
                        }
                        "nord" -> {
                            if (infrastructure.situation == "Campus nord") {
                                infrastructures.add(infrastructure)
                            }
                        }
                    }
                }
            }
            this.filteredList = infrastructures.toMutableList()

            this.adapter = InfraFragmentAdapter(infrastructures)

            val recyclerBatiments = fragmentModel.view?.findViewById<RecyclerView>(fragmentModel.recyclerViewId)
            recyclerBatiments?.layoutManager =
                LinearLayoutManager(fragmentModel.fragmentContext, LinearLayoutManager.VERTICAL, false)
            recyclerBatiments?.adapter = adapter
        })
        onDataLoaded()
    }

    fun saveInfras(context: Context, infras: MutableList<Infrastructure>) {
        val sharedPreferences = context.getSharedPreferences("InfrasPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(infras)
        editor.putString("infrastructure", json)
        editor.apply()
    }

    fun loadInfras(context: Context): MutableList<Infrastructure>? {
        val sharedPreferences = context.getSharedPreferences("InfrasPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("infrastructure", null)
        val type = object : TypeToken<MutableList<Infrastructure>>() {}.type
        return gson.fromJson(json, type)
    }
}