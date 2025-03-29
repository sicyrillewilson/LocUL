package tg.univlome.epl.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tg.univlome.epl.R

class InfraFragmentAdapter(private var infras: List<Infra>) : RecyclerView.Adapter<InfraFragmentAdapter.InfraViewHolder>() {

    class InfraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icone: ImageView = view.findViewById(R.id.imgInfra)
        val nom: TextView = view.findViewById(R.id.txtNomInfra)
        val distance: TextView = view.findViewById(R.id.txtDistanceInfra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_all_infra, parent, false)
        return InfraViewHolder(view)
    }

    override fun onBindViewHolder(holder: InfraViewHolder, position: Int) {
        val salle = infras[position]
        holder.icone.setImageResource(salle.icon)
        holder.nom.text = salle.nom
        holder.distance.text = salle.distance
    }

    override fun getItemCount(): Int = infras.size

    fun updateList(newList: List<Infra>) {
        infras = newList
        notifyDataSetChanged()
    }
}