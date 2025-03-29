package tg.univlome.epl.ui.batiment

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

class SudBatimentFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var batiments: List<Batiment>
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sud_batiment, container, false)

        batiments = listOf(
            Batiment("Bâtiment enseignement A", "Campus Nord", "500m", R.drawable.img),
            Batiment("Bâtiment enseignement B", "Campus Sud", "300m", R.drawable.img),
            Batiment("Bâtiment enseignement B", "Campus Sud", "300m", R.drawable.img),
            Batiment("Bâtiment enseignement B", "Campus Sud", "300m", R.drawable.img),
            Batiment("Bâtiment enseignement B", "Campus Sud", "300m", R.drawable.img),
            Batiment("Bâtiment enseignement B", "Campus Sud", "300m", R.drawable.img),
        )
        filteredList = batiments.toMutableList()

        adapter = BatimentFragmentAdapter(batiments)
        val recyclerSudBatiments = view.findViewById<RecyclerView>(R.id.recyclerSudBatiments)
        recyclerSudBatiments.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerSudBatiments.adapter = adapter

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
        filteredList = batiments.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}