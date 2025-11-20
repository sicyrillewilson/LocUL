@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import tg.univlome.epl.R
import tg.univlome.epl.adapter.ImageAdapter
import tg.univlome.epl.databinding.ActivitySalleBinding
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.models.Salle
import tg.univlome.epl.ui.batiment.BatimentActivity
import tg.univlome.epl.ui.maps.MapsActivity
import tg.univlome.epl.utils.MapsUtils

/**
 * Activité SalleActivity : Activité de détail d’une salle
 *
 * Description :
 * Cette activité permet d'afficher les informations complètes d'une salle universitaire :
 *  - Nom, situation géographique, description et distance par rapport à l’utilisateur
 *  - Images supplémentaires affichées dans un carrousel avec indicateurs personnalisés
 *  - Carte miniature (`miniMap`) indiquant la position de la salle et, si possible, celle de l’utilisateur
 *  - Un bouton "Aller" permettant d’ouvrir la carte principale (`MapsActivity`) avec l’itinéraire
 *
 * Elle utilise les bibliothèques `Glide` pour le chargement des images, `OSMDroid` pour la carte
 * et `FusedLocationProviderClient` pour la récupération de la position actuelle.
 *
 * Composants UI :
 *  - `ActivitySalleBinding` : ViewBinding pour accéder aux éléments d’interface
 *  - `ViewPager2` + `TabLayout` : affichage des images avec points personnalisés
 *  - `MapView` : carte statique pour visualisation de l’itinéraire
 *
 * Composants techniques :
 *  - `Glide` pour le chargement des images
 *  - `MapsUtils` pour configurer la carte miniature et enregistrer la destination
 *
 * Permissions requises :
 *  - `ACCESS_FINE_LOCATION` : localisation précise
 *  - `ACCESS_COARSE_LOCATION` : localisation approximative
 *
 * @see MapsUtils.setMiniMap pour l'affichage de la carte
 * @see MapsActivity pour la navigation détaillée
 * @see ImageAdapter pour le carrousel d’images
 */
class SalleActivity : AppCompatActivity() {

    lateinit var ui: ActivitySalleBinding
    private lateinit var miniMap: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Méthode principale appelée à la création de l'activité.
     * Initialise l’interface utilisateur, récupère les données de la salle,
     * configure les images, la carte miniature, les actions, et applique les autorisations.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivitySalleBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = packageName

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        val salle = intent.getSerializableExtra("salle") as? Salle

        val images = salle?.images ?: emptyList()

        if (images.size <= 1) {
            ui.imageContainer.visibility = View.GONE
        } else {
            ui.imageContainer.visibility = View.VISIBLE
            val viewPager = ui.imagePager
            val tabLayout = ui.imageIndicator

            // Déplacer la première image à la fin de la liste
            val mainImage = salle?.image
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

        if (salle != null) {
            ui.txtNomSalle.text = salle.nom
            ui.situationSalle.text = salle.situation
            ui.txtDistance.text = salle.distance
            ui.desc.text = salle.description
            if (!salle.image.isNullOrEmpty()) {
                Glide.with(this)
                    .asBitmap()
                    .load(salle.image)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            val drawable =
                                BitmapDrawable(resources, resource)
                            ui.imgSalle.setImageDrawable(drawable)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            } else {
                ui.imgSalle.setImageResource(salle.icon)
            }
            if (!salle.latitude.isNullOrBlank() && !salle.longitude.isNullOrBlank()) {
                val latitude = salle.latitude.toDouble()
                val longitude = salle.longitude.toDouble()

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

            ui.allerParent.setOnClickListener {

                if (salle.infrastructureId.isNullOrBlank()) {
                    return@setOnClickListener
                }

                val db = FirebaseFirestore.getInstance()
                val batimentsCollection = db.collection("batiments")

                batimentsCollection.document(salle.infrastructureId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {

                            val images = document.get("images") as? List<String> ?: emptyList()
                            val image = images.firstOrNull() ?: ""
                            val type = document.getString("type") ?: ""

                            val batiment = Batiment(
                                document.id,
                                document.getString("nom") ?: "",
                                document.getString("description") ?: "",
                                document.getString("longitude") ?: "",
                                document.getString("latitude") ?: "",
                                image,               // image principale
                                document.getString("situation") ?: "",
                                type,                // <-- manquait ici
                                images               // liste des images
                            )

                            val intent = Intent(this, BatimentActivity::class.java)
                            intent.putExtra("batiment", batiment)
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener {
                        // gestion erreur si besoin
                    }
            }

        }

        ui.btnRetour.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Configure une carte miniature (`miniMap`) pour afficher la position de la salle
     * et celle de l’utilisateur, si disponible.
     *
     * @param start Point de départ (position de l’utilisateur)
     * @param end Point de destination (salle)
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