@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import tg.univlome.epl.adapter.SalleViewAllAdapter
import tg.univlome.epl.databinding.ActivityViewAllSalleBinding
import tg.univlome.epl.models.Salle

class ViewAllSalleActivity : AppCompatActivity() {

    private lateinit var ui: ActivityViewAllSalleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityViewAllSalleBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val salles = intent.getSerializableExtra("salles")
                as? ArrayList<Salle> ?: arrayListOf()

        ui.recyclerAllSalles.layoutManager = LinearLayoutManager(this)
        ui.recyclerAllSalles.adapter = SalleViewAllAdapter(salles)
        ui.txtNomBatimentParent.text = salles[0].situation

        ui.btnRetour.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}