package tg.univlome.epl.adapter

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import tg.univlome.epl.R
import tg.univlome.epl.models.Salle
import tg.univlome.epl.ui.home.SalleActivity

class SalleViewAllAdapter(private var salles: List<Salle>,  private val onItemClick: (Salle) -> Unit) : RecyclerView.Adapter<SalleViewAllAdapter.SalleViewHolder>() {

    class SalleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgSalle)
        val nom: TextView = view.findViewById(R.id.txtNomSalle)
        val situation = view.findViewById<TextView>(R.id.situationSalle)
        val distance: TextView = view.findViewById(R.id.txtDistanceSalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_all_salle, parent, false)
        return SalleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalleViewHolder, position: Int) {
        val salle = salles[position]
        //holder.img.setImageResource(salle.icon)
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

        /*holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, Salle::class.java).apply {
                putExtra("nom", salle.nom)
                putExtra("situation", salle.situation)
                putExtra("distance", salle.distance)
                putExtra("icon", salle.icon)
                putExtra("longitude", salle.longitude)
                putExtra("latitude", salle.latitude)
                putStringArrayListExtra("images", ArrayList(salle.images))
            }
            holder.itemView.context.startActivity(intent)
        }*/
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, SalleActivity::class.java).apply {
                putExtra("salle", salle)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = salles.size

    fun updateList(newList: List<Salle>) {
        salles = newList
        notifyDataSetChanged()
    }
}