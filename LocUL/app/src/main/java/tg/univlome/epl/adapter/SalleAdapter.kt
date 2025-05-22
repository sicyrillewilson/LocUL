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

/**
 * Adapter SalleAdapter : Aperçu limité des salles avec navigation
 *
 * Description :
 * Cet adaptateur est utilisé dans des contextes comme `HomeFragment` pour présenter
 * un aperçu succinct des salles disponibles sur le campus, avec un bouton "Voir Tout".
 * Il affiche un nombre limité d’éléments (3 par défaut), et redirige vers `SalleActivity`
 * au clic sur un élément ou vers un `Fragment` complet au clic sur le bouton.
 *
 * Composants :
 * - `SalleViewHolder` : Affichage des informations principales d'une salle
 * - `ButtonViewHolder` : Affichage du bouton de navigation "Voir Tout"
 *
 * Bibliothèques utilisées :
 * - `Glide` pour le chargement asynchrone des images
 *
 * @param salles Liste des salles à afficher
 * @param fragmentManager Pour ouvrir le `newFragment` lorsque l'utilisateur veut voir tout
 * @param newFragment Fragment contenant la vue complète des salles
 *
 * @see tg.univlome.epl.ui.home.SalleActivity
 * @see tg.univlome.epl.ui.home.ViewAllSalleFragment
 * @see tg.univlome.epl.models.Salle
 */
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

    /**
     * Détermine le type de vue : salle ou bouton "Voir Tout".
     */
    override fun getItemViewType(position: Int): Int {
        return if (position < minOf(salles.size, min)) VIEW_TYPE_SALLE else VIEW_TYPE_BUTTON
    }

    /**
     * Crée les différents types de ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SALLE) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_salle, parent, false)
            SalleViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_voir_tout, parent, false)
            ButtonViewHolder(view)
        }
    }

    /**
     * Lie une salle ou un bouton à un ViewHolder.
     */
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
        } else if (holder is ButtonViewHolder) {
            holder.btnVoirTout.setOnClickListener {
                FragmentUtils.ouvrirFragment(fragmentManager, newFragment)
            }
        }
    }

    /**
     * Retourne le nombre d’éléments à afficher (limité + bouton).
     */
    override fun getItemCount(): Int {
        return minOf(salles.size, min) + 1 // +1 pour le bouton "Voir Tout"
    }

    /**
     * ViewHolder représentant une salle avec image, nom, bâtiment, et distance.
     */
    class SalleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgSalle)
        val nom: TextView = view.findViewById(R.id.txtNomSalle)
        val situation = view.findViewById<TextView>(R.id.batimentSalle)
        val distance: TextView = view.findViewById(R.id.txtDistanceSalle)
    }

    /**
     * ViewHolder contenant un bouton permettant d’ouvrir un fragment pour voir toutes les salles.
     */
    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnVoirTout = view.findViewById<ImageView>(R.id.imgVoirTout)
    }

    /**
     * Met à jour la liste des salles affichées et notifie l’adaptateur.
     *
     * @param newList Nouvelle liste des salles à afficher
     */
    fun updateList(newList: List<Salle>) {
        salles = newList
        notifyDataSetChanged()
    }
}
