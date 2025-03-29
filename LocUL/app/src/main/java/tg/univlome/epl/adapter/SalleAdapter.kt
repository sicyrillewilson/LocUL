package tg.univlome.epl.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import tg.univlome.epl.R
import tg.univlome.epl.FragmentUtils

data class Salle(val nom: String, val distance: String, val icon: Int)

class SalleAdapter(
    private val salles: List<Salle>,
    private val fragmentManager: FragmentManager,
    private val newFragment: Fragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SALLE = 0
        private const val VIEW_TYPE_BUTTON = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < salles.size) VIEW_TYPE_SALLE else VIEW_TYPE_BUTTON
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
        if (holder is SalleViewHolder) {
            val salle = salles[position]
            holder.icone.setImageResource(salle.icon)
            holder.nom.text = salle.nom
            holder.distance.text = salle.distance
        } else if (holder is ButtonViewHolder) {
            holder.btnVoirTout.setOnClickListener {
                FragmentUtils.ouvrirFragment(fragmentManager, newFragment)
            }
        }
    }

    override fun getItemCount(): Int {
        return salles.size + 1 // +1 pour le bouton "Voir Tout"
    }

    class SalleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icone: ImageView = view.findViewById(R.id.imgSalle)
        val nom: TextView = view.findViewById(R.id.txtNomSalle)
        val distance: TextView = view.findViewById(R.id.txtDistanceSalle)
    }

    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnVoirTout: ImageView = view.findViewById(R.id.imgVoirTout)
    }
}