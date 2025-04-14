@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.batiment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import org.osmdroid.util.GeoPoint
import tg.univlome.epl.R
import tg.univlome.epl.databinding.ActivityBatimentBinding
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.ui.maps.MapsActivity
import tg.univlome.epl.utils.MapsUtils

class BatimentActivity : AppCompatActivity() {
    lateinit var ui: ActivityBatimentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityBatimentBinding.inflate(layoutInflater)
        setContentView(ui.root)

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
                    .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {
                            val drawable =
                                android.graphics.drawable.BitmapDrawable(resources, resource)
                            ui.imgBatiment.setImageDrawable(drawable)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            } else {
                ui.imgBatiment.setImageResource(batiment.icon)
            }
            ui.aller.setOnClickListener {
                MapsUtils.saveDestination(this, GeoPoint(batiment.latitude.toDouble(), batiment.longitude.toDouble()))
                val intent = Intent(this, MapsActivity::class.java)
                ui.aller.context.startActivity(intent)
            }
        }

        ui.btnRetour.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}