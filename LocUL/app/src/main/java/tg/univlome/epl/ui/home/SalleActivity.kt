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
import android.widget.ImageView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import tg.univlome.epl.R
import tg.univlome.epl.adapter.ImageAdapter
import tg.univlome.epl.databinding.ActivitySalleBinding
import tg.univlome.epl.models.Salle
import tg.univlome.epl.ui.maps.MapsActivity
import tg.univlome.epl.utils.MapsUtils

class SalleActivity : AppCompatActivity() {
    lateinit var ui: ActivitySalleBinding
    private lateinit var miniMap: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
            findViewById<FrameLayout>(R.id.imageContainer).visibility = View.GONE
        } else {
            val viewPager = findViewById<ViewPager2>(R.id.imagePager)
            val tabLayout = findViewById<TabLayout>(R.id.imageIndicator)

            viewPager.adapter = ImageAdapter(images) // Adapter à créer ou adapter ton code existant

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
            ui.aller.setOnClickListener {
                MapsUtils.saveDestination(this, GeoPoint(salle.latitude.toDouble(), salle.longitude.toDouble()))
                val intent = Intent(this, MapsActivity::class.java)
                ui.aller.context.startActivity(intent)
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
                return
            }

            miniMap = ui.miniMap

            // Fallback si la localisation n'est pas disponible
            val destination = GeoPoint(salle.latitude.toDouble(), salle.longitude.toDouble())
            var userLocation: GeoPoint? = null

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

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

        }

        ui.btnRetour.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

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