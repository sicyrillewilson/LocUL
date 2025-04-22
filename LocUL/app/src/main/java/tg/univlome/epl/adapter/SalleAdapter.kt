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
import tg.univlome.epl.models.Salle
import tg.univlome.epl.ui.home.SalleActivity

class SalleAdapter(
    private var salles: List<Salle>,
    private val fragmentManager: FragmentManager,
    private val newFragment: Fragment,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var min: Int = 3

    companion object {
        private const val VIEW_TYPE_SALLE = 0
        private const val VIEW_TYPE_BUTTON = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < minOf(salles.size, min)) VIEW_TYPE_SALLE else VIEW_TYPE_BUTTON
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SALLE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_salle, parent, false)
            SalleViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_voir_tout, parent, false)
            ButtonViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SalleViewHolder && position < minOf(salles.size, min)) {
            val salle = salles[position]
            holder.nom.text = salle.nom
            holder.situation.text = salle.situation
            holder.distance.text = salle.distance
            if (!salle.image.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .asBitmap()
                    .load(salle.image)
                    .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                            val drawable = android.graphics.drawable.BitmapDrawable(holder.itemView.resources, resource)
                            holder.img.setImageDrawable(drawable)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            } else {
                holder.img.setImageResource(salle.icon)
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, SalleActivity::class.java).apply {
                    putExtra("salle", salle)
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
        return minOf(salles.size, min) + 1 // +1 pour le bouton "Voir Tout"
    }

    class SalleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgSalle)
        val nom: TextView = view.findViewById(R.id.txtNomSalle)
        val situation = view.findViewById<TextView>(R.id.batimentSalle)
        val distance: TextView = view.findViewById(R.id.txtDistanceSalle)
    }

    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnVoirTout = view.findViewById<ImageView>(R.id.imgVoirTout)
    }

    fun updateList(newList: List<Salle>) {
        salles = newList
        notifyDataSetChanged()
    }
}
