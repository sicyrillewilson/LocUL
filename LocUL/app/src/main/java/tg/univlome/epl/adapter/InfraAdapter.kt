package tg.univlome.epl.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import tg.univlome.epl.FragmentUtils
import tg.univlome.epl.R
import tg.univlome.epl.ui.HomeFragment
import tg.univlome.epl.ui.ViewAllInfraFragment
import tg.univlome.epl.ui.ViewAllSalleFragment

data class Infra(val nom: String, val situation: String, val distance: String, val icon: Int)

class InfraAdapter(
    private val infras: List<Infra>,
    private val fragmentManager: FragmentManager,
    private val newFragment: Fragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_INFRA = 0
        private const val VIEW_TYPE_BUTTON = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < infras.size) VIEW_TYPE_INFRA else VIEW_TYPE_BUTTON
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_INFRA) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_infra, parent, false)
            InfraViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_voir_tout, parent, false)
            ButtonViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is InfraViewHolder) {
            val infra = infras[position]
            holder.img.setImageResource(infra.icon)
            holder.nom.text = infra.nom
            holder.situation.text = infra.situation
            holder.distance.text = infra.distance
        } else if (holder is ButtonViewHolder) {
            holder.btnVoirTout.setOnClickListener {
                FragmentUtils.ouvrirFragment(fragmentManager, newFragment)
            }
        }
    }

    override fun getItemCount(): Int {
        return infras.size + 1 // +1 pour le bouton "Voir Tout"
    }

    class InfraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.imgInfra)
        val nom = view.findViewById<TextView>(R.id.txtNomInfra)
        val situation = view.findViewById<TextView>(R.id.situationInfra)
        val distance = view.findViewById<TextView>(R.id.txtDistanceInfra)
    }

    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnVoirTout = view.findViewById<ImageView>(R.id.imgVoirTout)
    }
}
