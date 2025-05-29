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

/**
 * Adapter InfraFragmentAdapter : Affichage complet des infrastructures
 *
 * Description :
 * Cet adaptateur est utilisé dans les fragments comme `AllInfraFragment`, `NordInfraFragment`,
 * `SudInfraFragment`, ou `ViewAllInfraFragment` pour afficher toutes les infrastructures
 * disponibles dans un `RecyclerView`, avec une carte miniature, nom, distance et situation.
 *
 * Contrairement à `InfraAdapter`, il ne limite pas le nombre d’éléments ni n’inclut
 * de bouton "Voir tout". Chaque item est cliquable pour ouvrir `InfraActivity`.
 *
 * Composants :
 * - `InfraViewHolder` : Vue de chaque infrastructure avec image, nom, situation, distance
 *
 * Bibliothèques utilisées :
 * - `Glide` pour le chargement efficace des images distantes
 *
 * @param infras Liste complète des infrastructures à afficher
 *
 * @see tg.univlome.epl.ui.infrastructure.InfraActivity
 * @see tg.univlome.epl.ui.infrastructure.AllInfraFragment
 * @see tg.univlome.epl.ui.home.ViewAllInfraFragment
 */
class InfraFragmentAdapter(private var infras: List<Infrastructure>) :
    RecyclerView.Adapter<InfraFragmentAdapter.InfraViewHolder>() {

    /**
     * ViewHolder représentant une infrastructure dans la liste.
     * Contient une image, le nom, la situation et la distance.
     */
    class InfraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgInfra)
        val nom: TextView = view.findViewById(R.id.txtNomInfra)
        val situation = view.findViewById<TextView>(R.id.situationInfra)
        val distance: TextView = view.findViewById(R.id.txtDistanceInfra)
    }

    /**
     * Crée un ViewHolder pour une infrastructure.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfraViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_all_infra, parent, false)
        return InfraViewHolder(view)
    }

    /**
     * Lie les données d'une infrastructure à la vue correspondante.
     */
    override fun onBindViewHolder(holder: InfraViewHolder, position: Int) {
        val infra = infras[position]
        holder.nom.text = infra.nom
        holder.situation.text = infra.situation
        holder.distance.text = infra.distance
        if (!infra.image.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .asBitmap()
                .load(infra.image)
                .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        val drawable = android.graphics.drawable.BitmapDrawable(
                            holder.itemView.resources,
                            resource
                        )
                        holder.img.setImageDrawable(drawable)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        } else {
            holder.img.setImageResource(infra.icon)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, InfraActivity::class.java).apply {
                putExtra("infrastructure", infra)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    /**
     * Retourne le nombre total d'infrastructures affichées.
     */
    override fun getItemCount(): Int = infras.size

    /**
     * Met à jour la liste des infrastructures et notifie l'adaptateur.
     *
     * @param newList Nouvelle liste à afficher dans le RecyclerView
     */
    fun updateList(newList: List<Infrastructure>) {
        infras = newList
        notifyDataSetChanged()
    }
}