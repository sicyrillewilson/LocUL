package tg.univlome.epl.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tg.univlome.epl.R

class SalleViewAllAdapter(private var salles: List<Salle>) : RecyclerView.Adapter<SalleViewAllAdapter.SalleViewHolder>() {

    class SalleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icone: ImageView = view.findViewById(R.id.imgSalle)
        val nom: TextView = view.findViewById(R.id.txtNomSalle)
        val distance: TextView = view.findViewById(R.id.txtDistanceSalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_all_salle, parent, false)
        return SalleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalleViewHolder, position: Int) {
        val salle = salles[position]
        holder.icone.setImageResource(salle.icon)
        holder.nom.text = salle.nom
        holder.distance.text = salle.distance
    }

    override fun getItemCount(): Int = salles.size

    fun updateList(newList: List<Salle>) {
        salles = newList
        notifyDataSetChanged()
    }
}