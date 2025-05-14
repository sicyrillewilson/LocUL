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
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import tg.univlome.epl.R
import tg.univlome.epl.databinding.ActivityBatimentBinding
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.ui.maps.MapsActivity
import tg.univlome.epl.utils.MapsUtils

class BatimentActivity : AppCompatActivity() {
    lateinit var ui: ActivityBatimentBinding
    private lateinit var miniMap: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityBatimentBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = packageName

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        val batiment = intent.getSerializableExtra("batiment") as? Batiment

        // Mettre à jour l'interface utilisateur avec les données reçues

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
            ui.aller.setOnClickListener {
                MapsUtils.saveDestination(
                    this,
                    GeoPoint(batiment.latitude.toDouble(), batiment.longitude.toDouble())
                )
                val intent = Intent(this, MapsActivity::class.java)
                ui.aller.context.startActivity(intent)
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {

                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
                return
            }

            miniMap = ui.miniMap

            // Fallback si la localisation n'est pas disponible
            val destination = GeoPoint(batiment.latitude.toDouble(), batiment.longitude.toDouble())
            var userLocation: GeoPoint? = null

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
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