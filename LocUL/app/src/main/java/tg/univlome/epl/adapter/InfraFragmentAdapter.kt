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
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.ui.infrastructure.InfraActivity

class InfraFragmentAdapter(private var infras: List<Infrastructure>) : RecyclerView.Adapter<InfraFragmentAdapter.InfraViewHolder>() {

    class InfraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgInfra)
        val nom: TextView = view.findViewById(R.id.txtNomInfra)
        val situation = view.findViewById<TextView>(R.id.situationInfra)
        val distance: TextView = view.findViewById(R.id.txtDistanceInfra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_all_infra, parent, false)
        return InfraViewHolder(view)
    }

    override fun onBindViewHolder(holder: InfraViewHolder, position: Int) {
        val infra = infras[position]
        //holder.img.setImageResource(salle.icon)
        holder.nom.text = infra.nom
        holder.situation.text = infra.situation
        holder.distance.text = infra.distance
        if (!infra.image.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .asBitmap()
                .load(infra.image)
                .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        val drawable = android.graphics.drawable.BitmapDrawable(holder.itemView.resources, resource)
                        holder.img.setImageDrawable(drawable)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        } else {
            holder.img.setImageResource(infra.icon)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, InfraActivity::class.java).apply {
                putExtra("nom", infra.nom)
                putExtra("situation", infra.situation)
                putExtra("distance", infra.distance)
                putExtra("icon", infra.icon)
                putExtra("longitude", infra.longitude)
                putExtra("latitude", infra.latitude)
                putStringArrayListExtra("images", ArrayList(infra.images))
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = infras.size

    fun updateList(newList: List<Infrastructure>) {
        infras = newList
        notifyDataSetChanged()
    }
}