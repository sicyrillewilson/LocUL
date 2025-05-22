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
import tg.univlome.epl.models.Infrastructure
import tg.univlome.epl.ui.infrastructure.InfraActivity

/**
 * Adapter InfraAdapter : Affichage résumé des infrastructures avec option "Voir tout"
 *
 * Description :
 * Cet adaptateur affiche une liste horizontale des infrastructures (bâtiments secondaires)
 * avec un maximum de 3 éléments, suivi d’un bouton "Voir tout". Il est utilisé dans
 * la page d’accueil (`HomeFragment`) pour un affichage synthétique, avec ouverture
 * d’une activité détaillée (`InfraActivity`) ou d’un fragment complet à la demande.
 *
 * Composants :
 * - `InfraViewHolder` : Affiche une infrastructure (image, nom, situation, distance)
 * - `ButtonViewHolder` : Affiche un bouton "Voir tout" pour ouvrir un fragment complet
 *
 * Bibliothèques utilisées :
 * - `Glide` pour le chargement optimisé des images distantes
 * - `FragmentManager` pour la navigation entre fragments
 *
 * @param infras Liste des infrastructures à afficher
 * @param fragmentManager FragmentManager utilisé pour ouvrir le fragment complet
 * @param newFragment Fragment à ouvrir via le bouton "Voir tout"
 *
 * @see tg.univlome.epl.ui.infrastructure.InfraActivity
 * @see tg.univlome.epl.ui.home.HomeFragment
 */
class InfraAdapter(
    private var infras: List<Infrastructure>,
    private val fragmentManager: FragmentManager,
    private val newFragment: Fragment,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var min: Int = 3

    companion object {
        private const val VIEW_TYPE_INFRA = 0
        private const val VIEW_TYPE_BUTTON = 1
    }

    /**
     * Détermine le type de vue : élément d’infrastructure ou bouton "Voir tout"
     */
    override fun getItemViewType(position: Int): Int {
        return if (position < minOf(infras.size, min)) VIEW_TYPE_INFRA else VIEW_TYPE_BUTTON
    }

    /**
     * Crée le ViewHolder adapté au type de vue : item ou bouton
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_INFRA) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_infra, parent, false)
            InfraViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_voir_tout, parent, false)
            ButtonViewHolder(view)
        }
    }

    /**
     * Lie les données d'une infrastructure à la vue, ou configure le bouton "Voir tout"
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is InfraViewHolder && position < minOf(infras.size, min)) {
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
        } else if (holder is ButtonViewHolder) {
            holder.btnVoirTout.setOnClickListener {
                FragmentUtils.ouvrirFragment(fragmentManager, newFragment)
            }
        }
    }

    /**
     * Retourne le nombre total d’éléments à afficher (max 3 infrastructures + bouton)
     */
    override fun getItemCount(): Int {
        return minOf(infras.size, min) + 1 // +1 pour le bouton "Voir Tout"
    }

    /**
     * Met à jour la liste des infrastructures et notifie l’adaptateur.
     *
     * @param newList Nouvelle liste à afficher
     */
    fun updateList(newList: List<Infrastructure>) {
        infras = newList
        notifyDataSetChanged()
    }

    /**
     * ViewHolder pour une infrastructure avec image, nom, situation, distance.
     */
    class InfraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.imgInfra)
        val nom = view.findViewById<TextView>(R.id.txtNomInfra)
        val situation = view.findViewById<TextView>(R.id.situationInfra)
        val distance = view.findViewById<TextView>(R.id.txtDistanceInfra)
    }

    /**
     * ViewHolder pour le bouton "Voir tout".
     */
    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnVoirTout = view.findViewById<ImageView>(R.id.imgVoirTout)
    }
}
