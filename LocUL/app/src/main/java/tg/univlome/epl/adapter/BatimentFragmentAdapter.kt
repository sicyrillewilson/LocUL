package tg.univlome.epl.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tg.univlome.epl.R
import tg.univlome.epl.models.Batiment

class BatimentFragmentAdapter(private var batiments: List<Batiment>) : RecyclerView.Adapter<BatimentFragmentAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.imgBatiment)
        val nom = view.findViewById<TextView>(R.id.txtNomBatiment)
        val situation = view.findViewById<TextView>(R.id.situationBat)
        val distance = view.findViewById<TextView>(R.id.txtDistanceBatiment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_all_batiment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val batiment = batiments[position]
        holder.img.setImageResource(batiment.icon)
        holder.nom.text = batiment.nom
        holder.situation.text = batiment.situation
        holder.distance.text = batiment.distance
    }

    override fun getItemCount() = batiments.size

    fun updateList(newList: List<Batiment>) {
        batiments = newList
        notifyDataSetChanged()
    }
}