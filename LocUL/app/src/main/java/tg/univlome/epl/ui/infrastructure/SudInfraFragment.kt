package tg.univlome.epl.ui.infrastructure

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tg.univlome.epl.MainActivity
import tg.univlome.epl.R
import tg.univlome.epl.adapter.Infra
import tg.univlome.epl.adapter.InfraFragmentAdapter
import tg.univlome.epl.ui.SearchBarFragment

class SudInfraFragment : Fragment(), SearchBarFragment.SearchListener {

    private lateinit var infrasSud: List<Infra>
    private lateinit var filteredList: MutableList<Infra>
    private lateinit var adapter: InfraFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sud_infra, container, false)

        val recyclerSudInfra = view.findViewById<RecyclerView>(R.id.recyclerSudInfra)

        infrasSud = listOf(
            Infra("Infra A", "Campus Sud", "500m", R.drawable.img),
            Infra("Infra B", "Campus Sud", "300m", R.drawable.img),
            Infra("Infra B", "Campus Sud", "300m", R.drawable.img),
            Infra("Infra B", "Campus Sud", "300m", R.drawable.img),
            Infra("Infra B", "Campus Sud", "300m", R.drawable.img),
            Infra("Infra B", "Campus Sud", "300m", R.drawable.img),
            Infra("Infra B", "Campus Sud", "300m", R.drawable.img),
            Infra("Infra B", "Campus Sud", "300m", R.drawable.img),
        )
        filteredList = infrasSud.toMutableList()

        adapter = InfraFragmentAdapter(infrasSud)
        recyclerSudInfra.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerSudInfra.adapter = adapter

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
        filteredList = infrasSud.filter { it.nom.contains(query, ignoreCase = true) }.toMutableList()
        adapter.updateList(filteredList)
    }

}