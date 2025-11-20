@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.batiment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayoutMediator
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import tg.univlome.epl.R
import tg.univlome.epl.adapter.ImageAdapter
import tg.univlome.epl.adapter.SalleBatimentAdapter
import tg.univlome.epl.databinding.ActivityBatimentBinding
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.models.Salle
import tg.univlome.epl.services.SalleService
import tg.univlome.epl.ui.home.ViewAllSalleFragment
import tg.univlome.epl.ui.maps.MapsActivity
import tg.univlome.epl.utils.MapsUtils

/**
 * Activité BatimentActivity : Activité de détail d’un bâtiment
 *
 * Description :
 * Cette activité affiche les détails complets d’un bâtiment sélectionné, y compris :
 *  - Le nom, la situation géographique, la distance, et la description
 *  - Un carrousel d’images si plusieurs photos sont disponibles
 *  - Une carte miniature affichant la position du bâtiment et celle de l’utilisateur
 *  - Un bouton de navigation qui redirige vers la carte principale (`MapsActivity`)
 *
 * Elle gère également la récupération de la position de l'utilisateur en temps réel à l'aide de
 * l'API `FusedLocationProviderClient`, ainsi que les permissions de localisation.
 *
 * Composants UI :
 *  - `ActivityBatimentBinding` pour accéder aux vues via ViewBinding
 *  - `MapView` (OSMDroid) pour l'affichage de la carte
 *  - `TabLayout` + `ViewPager2` pour les images
 *
 * Composants techniques :
 *  - Glide pour le chargement dynamique des images
 *  - OSMDroid pour la carte miniature statique
 *  - `MapsUtils` pour configurer la carte et calculer les itinéraires
 *
 * Permissions requises :
 *  - `ACCESS_FINE_LOCATION` pour afficher la position de l’utilisateur
 *
 * @see MapsUtils.setMiniMap pour la configuration de la carte miniature
 * @see MapsActivity pour la navigation complète
 * @see ImageAdapter pour le carrousel d’images
 */
class BatimentActivity : AppCompatActivity() {

    lateinit var ui: ActivityBatimentBinding
    private lateinit var miniMap: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Point d’entrée de l’activité. Initialise l’interface utilisateur,
     * applique les paramètres OSMDroid, extrait l’objet `Batiment`
     * depuis l’intent, configure l’affichage des images, les informations textuelles,
     * la carte miniature, et le bouton de navigation.
     *
     * Gère également les permissions de localisation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityBatimentBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = packageName

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        val batiment = intent.getSerializableExtra("batiment") as? Batiment

        val recyclerSalles = findViewById<RecyclerView>(R.id.recyclerSallesDuBatiment)
        val salles = mutableListOf<Salle>()
        val adapter = SalleBatimentAdapter(salles, supportFragmentManager, ViewAllSalleFragment())

        recyclerSalles.adapter = adapter
        recyclerSalles.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(
                this,
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                false
            )

        if (batiment != null) {
            val salleService = SalleService(this)
            salleService.getSalles().observe(this) { allSalles ->
                val sallesDuBat = allSalles.filter { it.infrastructureId == batiment.id }

                salles.clear()
                salles.addAll(sallesDuBat)
                adapter.notifyDataSetChanged()
            }
        }

        // Mettre à jour l'interface utilisateur avec les données reçues

        val images = batiment?.images ?: emptyList()

        if (images.size <= 1) {
            ui.imageContainer.visibility = View.GONE
        } else {
            ui.imageContainer.visibility = View.VISIBLE
            val viewPager = ui.imagePager
            val tabLayout = ui.imageIndicator

            // Déplacer la première image à la fin de la liste
            val mainImage = batiment?.image
            val reorderedImages = (images.filter { it != mainImage }) + mainImage

            viewPager.adapter = ImageAdapter(reorderedImages as List<String>)

            TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

            for (i in 0 until tabLayout.tabCount) {
                val tabView = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
                val params = tabView.layoutParams as ViewGroup.MarginLayoutParams

                // Définition de la taille des points
                params.width = resources.getDimensionPixelSize(R.dimen.custom_dot_width)
                params.height = resources.getDimensionPixelSize(R.dimen.custom_dot_height)

                // Définition des marges entre les points
                params.setMargins(
                    resources.getDimensionPixelSize(R.dimen.custom_dot_margin_horizontal),
                    0,
                    resources.getDimensionPixelSize(R.dimen.custom_dot_margin_horizontal),
                    0
                )

                tabView.layoutParams = params
            }
        }

        if (batiment != null) {
            ui.txtNomBatiment.text = batiment.nom
            ui.situationBat.text = batiment.situation
            ui.txtDistance.text = batiment.distance
            ui.desc.text = batiment.description
            if (!batiment.image.isNullOrEmpty()) {
                Glide.with(this)
                    .asBitmap()
                    .load(batiment.image)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            val drawable =
                                BitmapDrawable(resources, resource)
                            ui.imgBatiment.setImageDrawable(drawable)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            } else {
                ui.imgBatiment.setImageResource(batiment.icon)
            }
            if (!batiment.latitude.isNullOrBlank() && !batiment.longitude.isNullOrBlank()) {
                val latitude = batiment.latitude.toDouble()
                val longitude = batiment.longitude.toDouble()

                ui.aller.setOnClickListener {
                    MapsUtils.saveDestination(this, GeoPoint(latitude, longitude))
                    val intent = Intent(this, MapsActivity::class.java)
                    ui.aller.context.startActivity(intent)
                }

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {

                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
                    return
                }

                miniMap = ui.miniMap

                // Fallback si la localisation n'est pas disponible
                val destination = GeoPoint(latitude, longitude)
                var userLocation: GeoPoint? = null

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {

                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            userLocation = GeoPoint(it.latitude, it.longitude)
                            setupMiniMap(userLocation!!, destination)
                        } ?: run {
                            // Si la localisation n'est pas disponible, utiliser seulement la destination
                            setupMiniMap(destination, destination)
                        }
                    }.addOnFailureListener {
                        // En cas d'échec, utiliser seulement la destination
                        setupMiniMap(destination, destination)
                    }
                } else {
                    // Sans permission, utiliser seulement la destination
                    setupMiniMap(destination, destination)
                }
            } else {
                // Affiche un message ou fais une action par défaut
                ui.aller.visibility = View.GONE
                ui.miniMapLayout.visibility = View.GONE
            }

        }

        ui.btnRetour.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Configure et affiche une carte miniature (`miniMap`) montrant l’itinéraire
     * entre deux points : la position de l’utilisateur et celle du bâtiment.
     *
     * @param start Position de départ (utilisateur)
     * @param end Position d’arrivée (bâtiment)
     *
     * @requiresPermission Manifest.permission.ACCESS_FINE_LOCATION
     * @requiresPermission Manifest.permission.ACCESS_COARSE_LOCATION
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun setupMiniMap(start: GeoPoint, end: GeoPoint) {
        // Postpone pour s'assurer que la vue est bien layoutée
        ui.miniMapLayout.visibility = View.VISIBLE
        ui.miniMap.post {
            MapsUtils.setMiniMap(miniMap, start, end, this, this, resources)
            // Forcer un recalcul des dimensions
            miniMap.invalidate()
            miniMap.requestLayout()
        }
    }
}