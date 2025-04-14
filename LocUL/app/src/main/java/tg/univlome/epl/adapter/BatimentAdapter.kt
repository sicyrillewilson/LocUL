package tg.univlome.epl.adapter

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import tg.univlome.epl.FragmentUtils
import tg.univlome.epl.R
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.ui.batiment.BatimentActivity

//data class Batiment(val nom: String, val situation: String, val distance: String, val icon: Int)

class BatimentAdapter(
    private var batiments: List<Batiment>,
    private val fragmentManager: FragmentManager,
    private val newFragment: Fragment,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_BATIMENT = 0
        private const val VIEW_TYPE_BUTTON = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < batiments.size) VIEW_TYPE_BATIMENT else VIEW_TYPE_BUTTON
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_BATIMENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_batiment, parent, false)
            BatimentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_voir_tout, parent, false)
            ButtonViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BatimentViewHolder) {
            val batiment = batiments[position]
            //holder.img.setImageResource(batiment.icon)
            holder.nom.text = batiment.nom
            holder.situation.text = batiment.situation
            holder.distance.text = batiment.distance
            if (!batiment.image.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .asBitmap()
                    .load(batiment.image)
                    .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                            val drawable = android.graphics.drawable.BitmapDrawable(holder.itemView.resources, resource)
                            holder.img.setImageDrawable(drawable)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            } else {
                holder.img.setImageResource(batiment.icon)
            }

            /*holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, BatimentActivity::class.java).apply {
                    putExtra("nom", batiment.nom)
                    putExtra("situation", batiment.situation)
                    putExtra("distance", batiment.distance)
                    putExtra("icon", batiment.icon)
                    putExtra("longitude", batiment.longitude)
                    putExtra("latitude", batiment.latitude)
                    putStringArrayListExtra("images", ArrayList(batiment.images))
                }
                holder.itemView.context.startActivity(intent)
            }*/

            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, BatimentActivity::class.java).apply {
                    putExtra("batiment", batiment)
                }
                holder.itemView.context.startActivity(intent)
            }
        } else if (holder is ButtonViewHolder) {
            holder.btnVoirTout.setOnClickListener {
                FragmentUtils.ouvrirFragment(fragmentManager, newFragment)
            }
        }
    }

    override fun getItemCount(): Int {
        return batiments.size + 1
    }

    class BatimentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.imgBatiment)
        val nom = view.findViewById<TextView>(R.id.txtNomBatiment)
        val situation = view.findViewById<TextView>(R.id.situationBat)
        val distance = view.findViewById<TextView>(R.id.txtDistanceBatiment)
    }

    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnVoirTout = view.findViewById<ImageView>(R.id.imgVoirTout)
    }

    fun updateList(newList: List<Batiment>) {
        batiments = newList
        notifyDataSetChanged()
    }
}
