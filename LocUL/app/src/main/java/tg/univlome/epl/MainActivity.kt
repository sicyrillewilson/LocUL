@file:Suppress("DEPRECATION")

package tg.univlome.epl

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import tg.univlome.epl.fragments.TestFragment
import tg.univlome.epl.fragments.MapsFragment

class MainActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false // Variable pour gérer le double appui
    private var currentFragment: Fragment? = null // Pour suivre quel fragment est affiché

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        setContentView(R.layout.activity_main)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bouton_navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Définir l'action sur les éléments de navigation
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(TestFragment())
                    true
                }
                R.id.nav_maps -> {
                    loadFragment(MapsFragment())
                    true
                }
                else -> false
            }
        }

        // Charger un fragment par défaut
        bottomNavigationView.selectedItemId = R.id.nav_home

    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        currentFragment = fragment
    }
    override fun onBackPressed() {
        if (currentFragment is TestFragment) {
            // Si on est sur HomeFragment, gérer le double appui pour quitter l'application
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed() // Quitter l'application
                return
            }

            this.doubleBackToExitPressedOnce = true
            // Afficher un message à l'utilisateur
            Toast.makeText(this, "Appuyez à nouveau pour quitter", Toast.LENGTH_SHORT).show()

            // Remettre la variable à false après un délai
            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000) // Le délai pour un deuxième appui est de 2 secondes
        } else {
            // Si on n'est pas dans HomeFragment, on retourne au HomeFragment
            loadFragment(TestFragment())
            // Charger un fragment par défaut
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bouton_navigation)
            bottomNavigationView.selectedItemId = R.id.nav_home
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
}