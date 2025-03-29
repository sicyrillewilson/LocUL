package tg.univlome.epl.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import tg.univlome.epl.R
import tg.univlome.epl.adapter.Batiment
import tg.univlome.epl.adapter.BatimentAdapter
import tg.univlome.epl.adapter.Infra
import tg.univlome.epl.adapter.InfraAdapter
import tg.univlome.epl.adapter.Salle
import tg.univlome.epl.adapter.SalleAdapter

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val recyclerBatiments = view.findViewById<RecyclerView>(R.id.recyclerBatiments)
        val recyclerBatimentsAdmin = view.findViewById<RecyclerView>(R.id.recyclerBatimentsAdmin)
        val recyclerSalles = view.findViewById<RecyclerView>(R.id.recyclerSalles)
        val recyclerInfra = view.findViewById<RecyclerView>(R.id.recyclerInfra)

        val batimentsEns = listOf(
            Batiment("Bâtiment enseignement A", "Campus Nord", "500m", R.drawable.img),
            Batiment("Bâtiment enseignement B", "Campus Sud", "300m", R.drawable.img)
        )

        val batimentsAdmin = listOf(
            Batiment("DAAS", "Campus Nord","500m", R.drawable.img),
            Batiment("Bâtiment admin A", "Campus Sud","300m", R.drawable.img)
        )

        val Infras = listOf(
            Infra("Infra A", "Campus Nord","500m", R.drawable.img),
            Infra("Infra B", "Campus Sud","300m", R.drawable.img),
        )

        val salles = listOf(
            Salle("Salle 101", "200m", R.drawable.img),
            Salle("Salle 202", "100m", R.drawable.img)
        )

        val fragmentManager = requireActivity().supportFragmentManager

        recyclerBatiments.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerBatiments.adapter = BatimentAdapter(batimentsEns, fragmentManager, ViewAllBatEnsFragment())

        recyclerBatimentsAdmin.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerBatimentsAdmin.adapter = BatimentAdapter(batimentsAdmin, fragmentManager, ViewAllBatAdminFragment())

        recyclerSalles.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerSalles.adapter = SalleAdapter(salles, fragmentManager, ViewAllSalleFragment())

        recyclerInfra.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerInfra.adapter = InfraAdapter(Infras, fragmentManager, ViewAllInfraFragment())

        return view
    }
}