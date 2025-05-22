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

/**
 * Adapter SalleViewAllAdapter : Affichage détaillé de toutes les salles
 *
 * Description :
 * Cet adaptateur est utilisé dans les fragments de type `ViewAllSalleFragment` pour afficher
 * `la liste complète` des salles disponibles sur le campus avec leurs visuels, bâtiments, et distances.
 * Contrairement à `SalleAdapter`, il n’intègre pas de bouton "Voir Tout" et affiche l’ensemble des salles.
 *
 * Composants :
 * - `SalleViewHolder` : Représente une cellule individuelle de salle dans la RecyclerView.
 *
 * Bibliothèques utilisées :
 * - **Glide** : Pour le chargement asynchrone des images des salles (avec gestion d’erreur)
 *
 * @param salles Liste complète des salles à afficher
 *
 * @see tg.univlome.epl.models.Salle
 * @see tg.univlome.epl.ui.home.SalleActivity
 */
class SalleViewAllAdapter(private var salles: List<Salle>) :
    RecyclerView.Adapter<SalleViewAllAdapter.SalleViewHolder>() {

    /**
     * ViewHolder pour une salle dans la liste.
     * Contient l’image de la salle, son nom, son bâtiment d’appartenance et sa distance.
     */
    class SalleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgSalle)
        val nom: TextView = view.findViewById(R.id.txtNomSalle)
        val situation = view.findViewById<TextView>(R.id.batimentSalle)
        val distance: TextView = view.findViewById(R.id.txtDistanceSalle)
    }

    /**
     * Crée et retourne un nouveau ViewHolder pour chaque élément de la liste.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalleViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_all_salle, parent, false)
        return SalleViewHolder(view)
    }

    /**
     * Lie les données d'une salle à la vue de son ViewHolder.
     */
    override fun onBindViewHolder(holder: SalleViewHolder, position: Int) {
        val salle = salles[position]
        holder.nom.text = salle.nom
        holder.situation.text = salle.situation
        holder.distance.text = salle.distance
        if (!salle.image.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .asBitmap()
                .load(salle.image)
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
            holder.img.setImageResource(salle.icon)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, SalleActivity::class.java).apply {
                putExtra("salle", salle)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    /**
     * Retourne le nombre total d'éléments à afficher dans la liste.
     */
    override fun getItemCount(): Int = salles.size

    /**
     * Met à jour dynamiquement la liste des salles affichées.
     *
     * @param newList Nouvelle liste à utiliser
     */
    fun updateList(newList: List<Salle>) {
        salles = newList
        notifyDataSetChanged()
    }
}