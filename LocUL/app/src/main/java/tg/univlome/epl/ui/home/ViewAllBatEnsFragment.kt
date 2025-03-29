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
import tg.univlome.epl.adapter.Batiment
import tg.univlome.epl.adapter.BatimentFragmentAdapter
import tg.univlome.epl.ui.SearchBarFragment

class ViewAllBatEnsFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var batimentsEns: List<Batiment>
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_all_bat_ens, container, false)

        val recyclerBatimentsEns = view.findViewById<RecyclerView>(R.id.recyclerAllBatimentsEns)

        batimentsEns = listOf(
            Batiment("Bâtiment enseignement A", "Campus Nord", "500m", R.drawable.img),
            Batiment("Bâtiment enseignement B", "Campus Sud", "300m", R.drawable.img),
            Batiment("Bâtiment enseignement B", "Campus Sud", "300m", R.drawable.img),
            Batiment("Bâtiment enseignement B", "Campus Sud", "300m", R.drawable.img),
            Batiment("Bâtiment enseignement B", "Campus Sud", "300m", R.drawable.img),
            Batiment("Bâtiment enseignement B", "Campus Sud", "300m", R.drawable.img),
            Batiment("Bâtiment enseignement B", "Campus Sud", "300m", R.drawable.img),
        )
        filteredList = batimentsEns.toMutableList()

        adapter = BatimentFragmentAdapter(batimentsEns)
        recyclerBatimentsEns.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerBatimentsEns.adapter = adapter

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
        filteredList = batimentsEns.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }
}