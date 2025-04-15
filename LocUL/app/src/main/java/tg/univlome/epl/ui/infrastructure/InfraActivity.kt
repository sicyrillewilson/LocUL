@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.infrastructure

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.R
import tg.univlome.epl.databinding.ActivityInfraBinding
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.ui.maps.MapsActivity
import tg.univlome.epl.utils.MapsUtils

class InfraActivity : AppCompatActivity() {
    lateinit var ui: ActivityInfraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityInfraBinding.inflate(layoutInflater)
        setContentView(ui.root)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        val infrastructure = intent.getSerializableExtra("infrastructure") as? Infrastructure

        // Mettre à jour l'interface utilisateur avec les données reçues

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
            ui.aller.setOnClickListener {
                MapsUtils.saveDestination(this, GeoPoint(infrastructure.latitude.toDouble(), infrastructure.longitude.toDouble()))
                val intent = Intent(this, MapsActivity::class.java)
                ui.aller.context.startActivity(intent)
            }
        }

        ui.btnRetour.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}