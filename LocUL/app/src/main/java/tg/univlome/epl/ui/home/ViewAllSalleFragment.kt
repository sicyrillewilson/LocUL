package tg.univlome.epl.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tg.univlome.epl.MainActivity
import tg.univlome.epl.R
import tg.univlome.epl.adapter.Salle
import tg.univlome.epl.adapter.SalleViewAllAdapter
import tg.univlome.epl.ui.SearchBarFragment

class ViewAllSalleFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var salles: List<Salle>
    private lateinit var filteredList: MutableList<Salle>
    private lateinit var adapter: SalleViewAllAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_all_salle, container, false)

        val recyclerSalle = view.findViewById<RecyclerView>(R.id.recyclerAllSalle)

        salles = listOf(
            Salle("Salle A", "500m", R.drawable.img),
            Salle("Salle B", "300m", R.drawable.img),
            Salle("Salle B", "300m", R.drawable.img),
            Salle("Salle B", "300m", R.drawable.img),
            Salle("Salle B", "300m", R.drawable.img),
            Salle("Salle B", "300m", R.drawable.img),
        )
        filteredList = salles.toMutableList()

        adapter = SalleViewAllAdapter(salles)
        recyclerSalle.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerSalle.adapter = adapter

        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showSearchBarFragment(this)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).showSearchBarFragment(null) // Cacher la barre si on quitte
    }

    override fun onSearch(query: String) {
        filteredList = salles.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}