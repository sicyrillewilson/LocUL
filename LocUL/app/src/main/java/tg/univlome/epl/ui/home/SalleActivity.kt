@file:Suppress("DEPRECATION")

package tg.univlome.epl.ui.home

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import tg.univlome.epl.R
import tg.univlome.epl.databinding.ActivitySalleBinding

class SalleActivity : AppCompatActivity() {
    lateinit var ui: ActivitySalleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivitySalleBinding.inflate(layoutInflater)
        setContentView(ui.root)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        val nom = intent.getStringExtra("nom")
        val situation = intent.getStringExtra("situation")
        val distance = intent.getStringExtra("distance")
        val icon = intent.getIntExtra("icon", 0)
        val latitude = intent.getStringExtra("latitude")
        val longitude = intent.getStringExtra("longitude")
        val images = intent.getStringArrayListExtra("images")
    }
}