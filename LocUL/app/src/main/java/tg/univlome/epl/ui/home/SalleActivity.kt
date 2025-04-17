@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.home

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import tg.univlome.epl.R
import tg.univlome.epl.databinding.ActivitySalleBinding
import tg.univlome.epl.models.Salle

class ImageAdapter(private val images: List<Int>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_XY
        }
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageResource(images[position])
    }

    override fun getItemCount(): Int = images.size
}

class SalleActivity : AppCompatActivity() {
    lateinit var ui: ActivitySalleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivitySalleBinding.inflate(layoutInflater)
        setContentView(ui.root)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        val salle = intent.getSerializableExtra("salle") as? Salle

//        if (salle != null) {
//            ui.txtNomSalle.text = salle.nom
//            ui.situationSalle.text = salle.situation
//            ui.txtDistance.text = salle.distance
//            ui.desc.text = salle.description
//            val images = salle.images
//
//        }

        val images = listOf(
            R.drawable.mini_map,
            R.drawable.img,
            R.drawable.mini_map
        )

        val viewPager = findViewById<ViewPager2>(R.id.imagePager)
        val tabLayout = findViewById<TabLayout>(R.id.imageIndicator)

        viewPager.adapter = ImageAdapter(images)

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

        ui.btnRetour.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}