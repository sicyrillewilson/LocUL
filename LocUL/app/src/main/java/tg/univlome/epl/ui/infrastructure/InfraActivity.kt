@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.infrastructure

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayoutMediator
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import tg.univlome.epl.R
import tg.univlome.epl.adapter.ImageAdapter
import tg.univlome.epl.databinding.ActivityInfraBinding
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.ui.maps.MapsActivity
import tg.univlome.epl.utils.MapsUtils

/**
 * Activité InfraActivity : Activité de visualisation d’une infrastructure
 *
 * Description :
 * Cette activité permet d’afficher les détails complets d’une infrastructure sélectionnée.
 * Elle fournit à l’utilisateur :
 *  - Une interface descriptive contenant le nom, la situation, la distance et la description
 *  - Un affichage d’images multiples dans un carrousel avec des points d’indicateur personnalisés
 *  - Une carte miniature (`miniMap`) représentant la position de l'infrastructure
 *  - Un bouton de navigation permettant de lancer l’itinéraire dans `MapsActivity`
 *
 * Cette activité exploite l’API de localisation (`FusedLocationProviderClient`) pour détecter
 * la position de l’utilisateur, et utilise la bibliothèque OSMDroid pour afficher une carte embarquée.
 *
 * Composants principaux :
 *  - `ImageAdapter` : pour l’affichage des images de l’infrastructure
 *  - `MapsUtils` : pour la configuration de la carte miniature
 *  - `MapsActivity` : pour la navigation complète entre utilisateur et destination
 *
 * Bibliothèques utilisées :
 *  - OSMDroid (cartographie)
 *  - Glide (chargement d’images)
 *  - Google Location Services (géolocalisation)
 *  - Material Components (UI : TabLayout)
 *
 * Permissions requises :
 *  - `ACCESS_FINE_LOCATION`
 *  - `ACCESS_COARSE_LOCATION`
 *
 * @see MapsUtils.setMiniMap pour l'affichage de la carte miniature
 * @see MapsActivity pour la carte principale avec itinéraire
 * @see ImageAdapter pour l’affichage dynamique des images
 */
class InfraActivity : AppCompatActivity() {

    lateinit var ui: ActivityInfraBinding
    private lateinit var miniMap: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Point d’entrée de l’activité. Récupère les données de l’infrastructure envoyées via l’intent,
     * initialise l’interface graphique, gère le carrousel d’images, la carte miniature
     * et le bouton de navigation.
     *
     * Si les données de géolocalisation sont disponibles et les permissions accordées,
     * une mini carte est affichée avec itinéraire. Sinon, seule la destination est affichée.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityInfraBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = packageName

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        val infrastructure = intent.getSerializableExtra("infrastructure") as? Infrastructure

        // Mettre à jour l'interface utilisateur avec les données reçues

        val images = infrastructure?.images ?: emptyList()

        if (images.size <= 1) {
            ui.imageContainer.visibility = View.GONE
        } else {
            ui.imageContainer.visibility = View.VISIBLE
            val viewPager = ui.imagePager
            val tabLayout = ui.imageIndicator

            // Déplacer la première image à la fin de la liste
            val mainImage = infrastructure?.image
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

        if (infrastructure != null) {
            ui.txtNomInfra.text = infrastructure.nom
            ui.situationInfra.text = infrastructure.situation
            ui.txtDistance.text = infrastructure.distance
            ui.desc.text = infrastructure.description
            if (!infrastructure.image.isNullOrEmpty()) {
                Glide.with(this)
                    .asBitmap()
                    .load(infrastructure.image)
                    .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {
                            val drawable =
                                android.graphics.drawable.BitmapDrawable(resources, resource)
                            ui.imgInfra.setImageDrawable(drawable)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            } else {
                ui.imgInfra.setImageResource(infrastructure.icon)
            }
            if (!infrastructure.latitude.isNullOrBlank() && !infrastructure.longitude.isNullOrBlank()) {
                val latitude = infrastructure.latitude.toDouble()
                val longitude = infrastructure.longitude.toDouble()

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
     * Configure la carte miniature (`miniMap`) en affichant l’itinéraire entre la position actuelle
     * de l’utilisateur et l’infrastructure.
     *
     * @param start Position de départ (utilisateur ou fallback)
     * @param end Position de destination (infrastructure)
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