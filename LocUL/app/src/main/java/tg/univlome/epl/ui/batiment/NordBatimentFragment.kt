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
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.adapter.BatimentFragmentAdapter
import tg.univlome.epl.ui.SearchBarFragment

class NordBatimentFragment : Fragment(), SearchBarFragment.SearchListener {

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
        val view = inflater.inflate(R.layout.fragment_nord_batiment, container, false)

        batiments = listOf(
            Batiment("1", "Bâtiment enseignement A", "Batiment Enseignement", "", "", "", "Campus Nord", "500m", "", R.drawable.img),
            Batiment("2", "Bâtiment enseignement B", "Batiment Enseignement", "", "", "", "Campus Sud", "300m", "", R.drawable.img),
            Batiment("3", "Bâtiment enseignement B", "Batiment Enseignement", "", "", "", "Campus Sud", "300m", "", R.drawable.img),
            Batiment("4", "Bâtiment enseignement B", "Batiment Enseignement", "", "", "", "Campus Sud", "300m", "", R.drawable.img),
            Batiment("5", "Bâtiment enseignement B", "Batiment Enseignement", "", "", "", "Campus Sud", "300m", "", R.drawable.img),
            Batiment("6", "Bâtiment enseignement B", "Batiment Enseignement", "", "", "", "Campus Sud", "300m", "", R.drawable.img),
        )
        filteredList = batiments.toMutableList()

        adapter = BatimentFragmentAdapter(batiments)
        val recyclerNordBatiments = view.findViewById<RecyclerView>(R.id.recyclerNordBatiments)
        recyclerNordBatiments.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerNordBatiments.adapter = adapter

        // Inflate the layout for this fragment
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