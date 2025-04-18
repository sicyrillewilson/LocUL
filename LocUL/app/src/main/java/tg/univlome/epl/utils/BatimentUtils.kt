package tg.univlome.epl.utils

import android.content.Context
import androidx.fragment.app.FragmentActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.MainActivity
import tg.univlome.epl.R
import tg.univlome.epl.adapter.BatimentFragmentAdapter
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.models.modelsfragments.FragmentModel
import tg.univlome.epl.services.BatimentService
import tg.univlome.epl.ui.maps.MapsFragment

object BatimentUtils {

    private lateinit var batimentService: BatimentService
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var batiments: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter

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

    fun ouvrirMapsFragment(batiment: Batiment, activity: FragmentActivity, fragmentContext: Context) {
        MapsUtils.saveDestination(fragmentContext, GeoPoint(batiment.latitude.toDouble(), batiment.longitude.toDouble()))
        (activity as? MainActivity)?.loadMapsFragment()
    }

    fun updateBatiments(userLocation: GeoPoint, batiments: MutableList<Batiment>, filteredList: MutableList<Batiment>, adapter: BatimentFragmentAdapter, fragmentModel: FragmentModel) {
        batimentService = BatimentService(fragmentModel.fragmentContext)
        this.filteredList = filteredList
        this.batiments = batiments
        this.adapter = adapter

        Log.d("BatimentUtils", "updateBatiments appelée avec : $userLocation")
        // Charger les bâtiments
        batimentService.getBatiments().observe(fragmentModel.viewLifecycleOwner, Observer { bats ->
            if (bats != null) {
                for (batiment in bats) {
                    try {
                        val batimentLocation = GeoPoint(batiment.latitude.toDouble(), batiment.longitude.toDouble())
                        val distance = MapsUtils.calculateDistance(userLocation, batimentLocation)
                        Log.d("BatimentUtils", "Distance calculée pour ${batiment.nom}: $distance mètres")

                        // Conversion en km si la distance dépasse 1000 m
                        val formattedDistance = if (distance >= 1000) {
                            String.format("%.2f km", distance / 1000)
                        } else {
                            String.format("%.2f m", distance)
                        }

                        batiment.distance = formattedDistance

                        Log.d("BatimentUtils", "Distance mise à jour pour ${batiment.nom}: ${batiment.distance}")

                    } catch (e: Exception) {
                        Log.e("BatimentUtils", "Erreur lors de la mise à jour de la distance pour ${batiment.nom}", e)
                    }
                    when (fragmentModel.situation) {
                        "" -> {
                            if (fragmentModel.type != "" && batiment.type.lowercase() == fragmentModel.type.lowercase()) {
                                batiments.add(batiment)
                            } else {
                                batiments.add(batiment)
                            }
                        }
                        "sud" -> {
                            if (batiment.situation == "Campus sud") {
                                batiments.add(batiment)
                            }
                        }
                        "nord" -> {
                            if (batiment.situation == "Campus nord") {
                                batiments.add(batiment)
                            }
                        }
                    }
                }
            }
            this.filteredList = batiments.toMutableList()

            this.adapter = BatimentFragmentAdapter(batiments)

            val recyclerBatiments = fragmentModel.view?.findViewById<RecyclerView>(fragmentModel.recyclerViewId)
            recyclerBatiments?.layoutManager =
                LinearLayoutManager(fragmentModel.fragmentContext, LinearLayoutManager.VERTICAL, false)
            recyclerBatiments?.adapter = adapter
        })
    }
    
    fun saveBatiments(context: Context, batiments: MutableList<Batiment>) {
        val sharedPreferences = context.getSharedPreferences("BatimentsPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(batiments)
        editor.putString("batiment", json)
        editor.apply()
    }

    fun loadBatiments(context: Context): MutableList<Batiment>? {
        val sharedPreferences = context.getSharedPreferences("BatimentsPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("batiment", null)
        val type = object : TypeToken<MutableList<Batiment>>() {}.type
        return gson.fromJson(json, type)
    }

}