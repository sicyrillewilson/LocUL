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
import tg.univlome.epl.adapter.SalleViewAllAdapter
import tg.univlome.epl.models.Salle
import tg.univlome.epl.models.modelsfragments.FragmentModel
import tg.univlome.epl.services.SalleService
import tg.univlome.epl.ui.maps.MapsFragment

object SalleUtils {

    private lateinit var salleService: SalleService
    private lateinit var filteredList: MutableList<Salle>
    private lateinit var salles: MutableList<Salle>
    private lateinit var adapter: SalleViewAllAdapter

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
    
    fun updateSalles(userLocation: GeoPoint, salles: MutableList<Salle>, filteredList: MutableList<Salle>, adapter: SalleViewAllAdapter, fragmentModel: FragmentModel) {
        //salleService = SalleService()
        salleService = SalleService(fragmentModel.fragmentContext)
        this.filteredList = filteredList
        this.salles = salles
        this.adapter = adapter

        Log.d("SalleUtils", "updateSalles appelée avec : $userLocation")
        // Charger les bâtiments
        salleService.getSalles().observe(fragmentModel.viewLifecycleOwner, Observer { sals ->
            if (sals != null) {
                for (salle in sals) {
                    try {
                        val salleLocation = GeoPoint(salle.latitude.toDouble(), salle.longitude.toDouble())
                        val distance = MapsUtils.calculateDistance(userLocation, salleLocation)
                        Log.d("SalleUtils", "Distance calculée pour ${salle.nom}: $distance mètres")

                        // Conversion en km si la distance dépasse 1000 m
                        val formattedDistance = if (distance >= 1000) {
                            String.format("%.2f km", distance / 1000)
                        } else {
                            String.format("%.2f m", distance)
                        }

                        salle.distance = formattedDistance

                        Log.d("SalleUtils", "Distance mise à jour pour ${salle.nom}: ${salle.distance}")

                    } catch (e: Exception) {
                        Log.e("SalleUtils", "Erreur lors de la mise à jour de la distance pour ${salle.nom}", e)
                    }
                    when (fragmentModel.situation) {
                        "" -> {
                            salles.add(salle)
                        }
                        "sud" -> {
                            if (salle.situation == "Campus sud") {
                                salles.add(salle)
                            }
                        }
                        "nord" -> {
                            if (salle.situation == "Campus nord") {
                                salles.add(salle)
                            }
                        }
                    }
                }
            }
            this.filteredList = salles.toMutableList()

            this.adapter = SalleViewAllAdapter(salles)

            val recyclerBatiments = fragmentModel.view?.findViewById<RecyclerView>(fragmentModel.recyclerViewId)
            recyclerBatiments?.layoutManager =
                LinearLayoutManager(fragmentModel.fragmentContext, LinearLayoutManager.VERTICAL, false)
            recyclerBatiments?.adapter = adapter
        })
    }

    fun saveSalles(context: Context, salles: MutableList<Salle>) {
        val sharedPreferences = context.getSharedPreferences("SallePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(salles)
        editor.putString("salle", json)
        editor.apply()
    }

    fun loadSalles(context: Context): MutableList<Salle>? {
        val sharedPreferences = context.getSharedPreferences("SallePrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("salle", null)
        val type = object : TypeToken<MutableList<Salle>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearSalles(context: Context) {
        val sharedPreferences = context.getSharedPreferences("SallePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("salle")
        editor.apply()
    }

}