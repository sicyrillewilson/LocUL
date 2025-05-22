package tg.univlome.epl.utils

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.R
import tg.univlome.epl.adapter.InfraFragmentAdapter
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.models.modelsfragments.FragmentModel
import tg.univlome.epl.services.InfrastructureService
import tg.univlome.epl.ui.maps.MapsFragment

/**
 * Objet InfraUtils : Fournit des fonctions utilitaires pour la gestion
 * des infrastructures sur le campus universitaire.
 *
 * Description :
 * Cet objet permet d'interagir avec les infrastructures en facilitant :
 *  - Le chargement dynamique des infrastructures à partir d'un service distant
 *  - Le filtrage selon leur situation géographique (Campus sud, nord, etc.)
 *  - L'affichage de la distance par rapport à l'utilisateur
 *  - L'ouverture d'une carte localisant une infrastructure
 *  - La sauvegarde et le chargement local des infrastructures via SharedPreferences
 *
 * Composants principaux :
 *  - InfrastructureService : service de récupération des données
 *  - InfraFragmentAdapter : adaptateur RecyclerView pour afficher les infrastructures
 *  - MapsFragment : fragment de carte utilisé pour localiser une infrastructure
 *
 * Bibliothèques utilisées :
 *  - Gson pour la sérialisation JSON
 *  - OSMDroid pour la manipulation des points géographiques
 *
 * @see InfrastructureService pour l'accès aux données des infrastructures
 * @see MapsFragment pour l'affichage de la carte d'une infrastructure
 */
object InfraUtils {

    private lateinit var infraService: InfrastructureService
    private lateinit var filteredList: MutableList<Infrastructure>
    private lateinit var infras: MutableList<Infrastructure>
    private lateinit var adapter: InfraFragmentAdapter

    /**
     * Ouvre le fragment de carte (MapsFragment) centré sur la position d'une infrastructure donnée.
     *
     * @param infrastructure Infrastructure dont la position est à afficher.
     * @param fragmentActivity L’activité hôte dans laquelle charger le fragment.
     */
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

    /**
     * Met à jour dynamiquement la liste des infrastructures avec la distance depuis l’utilisateur,
     * filtre selon la situation (sud, nord...), et recharge la RecyclerView avec l’adaptateur.
     *
     * @param userLocation Position géographique actuelle de l’utilisateur.
     * @param infrastructures Liste cible à remplir avec les infrastructures chargées.
     * @param filteredList Liste utilisée pour afficher les éléments filtrés.
     * @param adapter Adaptateur lié à la RecyclerView à mettre à jour.
     * @param fragmentModel Modèle contenant le contexte, la vue et les infos du fragment appelant.
     * @param onDataLoaded Callback optionnel exécuté après le chargement.
     */
    fun updateInfrastructures(
        userLocation: GeoPoint,
        infrastructures: MutableList<Infrastructure>,
        filteredList: MutableList<Infrastructure>,
        adapter: InfraFragmentAdapter,
        fragmentModel: FragmentModel,
        onDataLoaded: () -> Unit = {}
    ) {
        infraService = InfrastructureService(fragmentModel.fragmentContext)
        this.filteredList = filteredList
        this.infras = infrastructures
        this.adapter = adapter

        Log.d("InfraUtils", "updateInfrastructures appelée avec : $userLocation")

        // Récupération des infrastructures depuis le service
        infraService.getInfrastructures()
            .observe(fragmentModel.viewLifecycleOwner, Observer { infras ->
                if (infras != null) {
                    for (infrastructure in infras) {
                        try {
                            val infrastructureLocation = GeoPoint(
                                infrastructure.latitude.toDouble(),
                                infrastructure.longitude.toDouble()
                            )
                            val distance =
                                MapsUtils.calculateDistance(userLocation, infrastructureLocation)
                            Log.d(
                                "InfraUtils",
                                "Distance calculée pour ${infrastructure.nom}: $distance mètres"
                            )

                            // Conversion en km si la distance dépasse 1000 m
                            val formattedDistance = if (distance >= 1000) {
                                String.format("%.2f km", distance / 1000)
                            } else {
                                String.format("%.2f m", distance)
                            }

                            infrastructure.distance = formattedDistance

                            Log.d(
                                "InfraUtils",
                                "Distance mise à jour pour ${infrastructure.nom}: ${infrastructure.distance}"
                            )

                        } catch (e: Exception) {
                            Log.e(
                                "InfraUtils",
                                "Erreur lors de la mise à jour de la distance pour ${infrastructure.nom}",
                                e
                            )
                        }
                        // Filtrage selon la situation géographique
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

                // Mise à jour de la liste filtrée et de l'adaptateur
                this.filteredList = infrastructures.toMutableList()
                this.adapter = InfraFragmentAdapter(infrastructures)

                val recyclerBatiments =
                    fragmentModel.view?.findViewById<RecyclerView>(fragmentModel.recyclerViewId)
                recyclerBatiments?.layoutManager =
                    LinearLayoutManager(
                        fragmentModel.fragmentContext,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                recyclerBatiments?.adapter = adapter
            })
        onDataLoaded()
    }

    /**
     * Sauvegarde la liste des infrastructures localement dans les préférences partagées (JSON).
     *
     * @param context Contexte utilisé pour accéder aux préférences.
     * @param infras Liste des infrastructures à sauvegarder.
     */
    fun saveInfras(context: Context, infras: MutableList<Infrastructure>) {
        val sharedPreferences = context.getSharedPreferences("InfrasPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(infras)
        editor.putString("infrastructure", json)
        editor.apply()
    }

    /**
     * Charge les infrastructures précédemment sauvegardées depuis les préférences partagées.
     *
     * @param context Contexte utilisé pour accéder aux préférences.
     * @return Liste des infrastructures sauvegardées ou null si aucune donnée trouvée.
     */
    fun loadInfras(context: Context): MutableList<Infrastructure>? {
        val sharedPreferences = context.getSharedPreferences("InfrasPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("infrastructure", null)
        val type = object : TypeToken<MutableList<Infrastructure>>() {}.type
        return gson.fromJson(json, type)
    }
}