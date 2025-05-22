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

/**
 * Adapter BatimentAdapter : Adaptateur pour l'affichage des bâtiments dans un RecyclerView
 *
 * Description :
 * Cet adaptateur permet d'afficher une liste réduite de bâtiments (jusqu’à 3 éléments) suivie d’un bouton
 * "Voir tout" qui redirige vers un fragment listant l'ensemble des bâtiments disponibles.
 * Il gère le rendu des informations clés d’un bâtiment (nom, situation, distance, image) ainsi que
 * la navigation vers l'activité de détails `BatimentActivity`.
 *
 * Composants principaux :
 * - `BatimentViewHolder` : Gère l’affichage d’un élément de type bâtiment.
 * - `ButtonViewHolder` : Gère l’affichage du bouton "Voir tout".
 * - `updateList()` : Met à jour dynamiquement la liste de bâtiments affichés.
 *
 * Bibliothèques utilisées :
 * - `Glide` pour le chargement et la mise en cache d’images à distance
 * - `AndroidX RecyclerView` pour la gestion optimisée des listes scrollables
 * - `Intent` pour le passage aux détails (`BatimentActivity`)
 * - `FragmentUtils` pour la navigation entre fragments
 *
 * @param batiments Liste initiale de bâtiments à afficher
 * @param fragmentManager Gestionnaire de fragments pour les transitions
 * @param newFragment Fragment à charger lors du clic sur "Voir tout"
 *
 * @see BatimentActivity
 * @see tg.univlome.epl.utils.BatimentUtils
 */
class BatimentAdapter(
    private var batiments: List<Batiment>,
    private val fragmentManager: FragmentManager,
    private val newFragment: Fragment,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var min: Int = 3

    companion object {
        private const val VIEW_TYPE_BATIMENT = 0
        private const val VIEW_TYPE_BUTTON = 1
    }

    /**
     * Définit le type de vue à afficher selon la position (bâtiment ou bouton).
     */
    override fun getItemViewType(position: Int): Int {
        return if (position < minOf(batiments.size, min)) VIEW_TYPE_BATIMENT else VIEW_TYPE_BUTTON
    }

    /**
     * Crée et retourne le bon type de ViewHolder selon le type défini.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_BATIMENT) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_batiment, parent, false)
            BatimentViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_voir_tout, parent, false)
            ButtonViewHolder(view)
        }
    }

    /**
     * Lie les données d’un bâtiment ou configure le bouton "Voir tout".
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BatimentViewHolder && position < minOf(batiments.size, min)) {
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
        } else if (holder is ButtonViewHolder) {
            holder.btnVoirTout.setOnClickListener {
                FragmentUtils.ouvrirFragment(fragmentManager, newFragment)
            }
        }
    }

    /**
     * Retourne le nombre total d’éléments à afficher (3 max + 1 bouton).
     */
    override fun getItemCount(): Int {
        return minOf(batiments.size, min) + 1 // 3 batiments max + 1 bouton "voir tout"
    }

    /**
     * Met à jour dynamiquement la liste de bâtiments affichée.
     * @param newList Nouvelle liste de bâtiments à afficher
     */
    fun updateList(newList: List<Batiment>) {
        batiments = newList
        notifyDataSetChanged()
    }

    /**
     * ViewHolder pour l’affichage d’un bâtiment (image, nom, situation, distance).
     */
    class BatimentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.imgBatiment)
        val nom = view.findViewById<TextView>(R.id.txtNomBatiment)
        val situation = view.findViewById<TextView>(R.id.situationBat)
        val distance = view.findViewById<TextView>(R.id.txtDistanceBatiment)
    }

    /**
     * ViewHolder pour le bouton "Voir tout" (utilise une icône).
     */
    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnVoirTout = view.findViewById<ImageView>(R.id.imgVoirTout)
    }

}
