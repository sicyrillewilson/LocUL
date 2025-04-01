package tg.univlome.epl.ui.batiment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tg.univlome.epl.MainActivity
import tg.univlome.epl.R
import tg.univlome.epl.adapter.BatimentFragmentAdapter
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.services.BatimentService
import tg.univlome.epl.ui.SearchBarFragment

class AllBatimentFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var batiments: List<Batiment>
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter

    private lateinit var batimentService: BatimentService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_batiment, container, false)

        batimentService = BatimentService()

        // Charger les bâtiments
        batimentService.getBatiments().observe(viewLifecycleOwner, Observer { batiments ->
            if (batiments != null) {
                for (batiment in batiments) {
                    //ajouterLieuSurCarte(batiment)
                }
            }
        })

        batiments = listOf(
            Batiment("1", "Bâtiment enseignement A", "Batiment Enseignement", "", "", "", "Campus Nord", "500m", R.drawable.img),
            Batiment("2", "Qwerty enseignement B", "Batiment Enseignement", "", "", "", "Campus Sud", "300m", R.drawable.img),
            Batiment("3", "Asdfgh enseignement C", "Batiment Enseignement", "", "", "", "Campus Sud", "300m", R.drawable.img),
            Batiment("4", "Zxcvb enseignement D", "Batiment Enseignement", "", "", "", "Campus Sud", "300m", R.drawable.img),
            Batiment("5", "Bâtiment enseignement E", "Batiment Enseignement", "", "", "", "Campus Sud", "300m", R.drawable.img),
            Batiment("6", "Bâtiment enseignement F", "Batiment Enseignement", "", "", "", "Campus Sud", "300m", R.drawable.img),
            Batiment("7", "Bâtiment enseignement G", "Batiment Enseignement", "", "", "", "Campus Sud", "300m", R.drawable.img),
        )
        filteredList = batiments.toMutableList()

        adapter = BatimentFragmentAdapter(batiments)
        val recyclerAllBatiments = view.findViewById<RecyclerView>(R.id.recyclerAllBatiments)
        recyclerAllBatiments.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerAllBatiments.adapter = adapter

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