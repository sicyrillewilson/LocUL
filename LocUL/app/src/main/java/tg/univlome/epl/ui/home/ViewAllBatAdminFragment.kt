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
import tg.univlome.epl.models.Batiment
import tg.univlome.epl.adapter.BatimentFragmentAdapter
import tg.univlome.epl.ui.SearchBarFragment

class ViewAllBatAdminFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var batimentsAdmin: List<Batiment>
    private lateinit var filteredList: MutableList<Batiment>
    private lateinit var adapter: BatimentFragmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_all_bat_admin, container, false)

        val recyclerBatimentsAdmin = view.findViewById<RecyclerView>(R.id.recyclerAllBatimentsAdmin)

        batimentsAdmin = listOf(
            Batiment("1", "Bâtiment enseignement A", "Batiment Administratif", "", "", "", "Campus Nord", "type", listOf(), "500m", R.drawable.img),
            Batiment("2", "Bâtiment enseignement B", "Batiment Administratif", "", "", "", "Campus Sud", "type", listOf(), "300m", R.drawable.img),
            Batiment("3", "Bâtiment enseignement B", "Batiment Administratif", "", "", "", "Campus Sud", "type", listOf(), "300m", R.drawable.img),
            Batiment("4", "Bâtiment enseignement B", "Batiment Administratif", "", "", "", "Campus Sud", "type", listOf(), "300m", R.drawable.img),
            Batiment("5", "Bâtiment enseignement B", "Batiment Administratif", "", "", "", "Campus Sud", "type", listOf(), "300m", R.drawable.img),
            Batiment("6", "Bâtiment enseignement B", "Batiment Administratif", "", "", "", "Campus Sud", "type", listOf(), "300m", R.drawable.img),
            Batiment("7", "Bâtiment enseignement B", "Batiment Administratif", "", "", "", "Campus Sud", "type", listOf(), "300m", R.drawable.img),
        )
        filteredList = batimentsAdmin.toMutableList()

        adapter = BatimentFragmentAdapter(batimentsAdmin)  { batiment ->
            //ouvrirMapsFragment(batiment)
        }
        recyclerBatimentsAdmin.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerBatimentsAdmin.adapter = adapter

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
        filteredList = batimentsAdmin.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}