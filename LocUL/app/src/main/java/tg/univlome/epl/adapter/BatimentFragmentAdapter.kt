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
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.ui.batiment.BatimentActivity

/**
 * Adapter BatimentFragmentAdapter : Adaptateur d'affichage des bâtiments (vue étendue)
 *
 * Description :
 * Cet adaptateur est utilisé pour afficher l’ensemble des bâtiments dans les fragments
 * "Voir tout", sans limitation du nombre d’éléments. Chaque élément représente un bâtiment avec
 * son image, son nom, sa situation et sa distance. Un clic sur un bâtiment ouvre
 * l'activité de détail `BatimentActivity`.
 *
 * Composants principaux :
 * - `ViewHolder` : Gère l’affichage des informations d’un bâtiment.
 * - `updateList()` : Met à jour dynamiquement la liste des bâtiments affichés.
 *
 * Bibliothèques utilisées :
 * - `Glide` pour le chargement d'images à partir d’URL
 * - `AndroidX RecyclerView` pour l’affichage efficace d’une liste scrollable
 * - `Intent` pour la navigation vers l’activité de détail `BatimentActivity`
 *
 * @param batiments Liste des bâtiments à afficher dans le RecyclerView
 *
 * @see BatimentActivity
 * @see tg.univlome.epl.utils.BatimentUtils
 */
class BatimentFragmentAdapter(private var batiments: List<Batiment>) :
    RecyclerView.Adapter<BatimentFragmentAdapter.ViewHolder>() {

    /**
     * ViewHolder représentant un bâtiment avec :
     * - Une image (ou icône par défaut)
     * - Son nom
     * - Sa situation géographique (ex: "Campus Nord")
     * - La distance calculée depuis la position de l'utilisateur
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.imgBatiment)
        val nom = view.findViewById<TextView>(R.id.txtNomBatiment)
        val situation = view.findViewById<TextView>(R.id.situationBat)
        val distance = view.findViewById<TextView>(R.id.txtDistanceBatiment)
    }

    /**
     * Crée un ViewHolder pour l’affichage d’un bâtiment.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_all_batiment, parent, false)
        return ViewHolder(view)
    }

    /**
     * Lie les données du bâtiment à la vue correspondante.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val batiment = batiments[position]
        holder.nom.text = batiment.nom
        holder.situation.text = batiment.situation
        holder.distance.text = batiment.distance
        if (!batiment.image.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .asBitmap()
                .load(batiment.image)
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
            holder.img.setImageResource(batiment.icon)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, BatimentActivity::class.java).apply {
                putExtra("batiment", batiment)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    /**
     * Retourne le nombre total de bâtiments à afficher.
     */
    override fun getItemCount(): Int = batiments.size

    /**
     * Met à jour la liste affichée avec de nouvelles données.
     *
     * @param newList Nouvelle liste de bâtiments
     */
    fun updateList(newList: List<Batiment>) {
        batiments = newList
        notifyDataSetChanged()
    }
}