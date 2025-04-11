@file:Suppress("DEPRECATION")

package tg.univlome.epl

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import tg.univlome.epl.databinding.ActivityMainBinding
import tg.univlome.epl.ui.batiment.BatimentFragment
import tg.univlome.epl.ui.home.HomeFragment
import tg.univlome.epl.ui.infrastructure.InfraFragment
import tg.univlome.epl.ui.SearchBarFragment
import tg.univlome.epl.ui.maps.MapsFragment
import java.util.Locale
import android.os.Handler
import android.os.Looper
import tg.univlome.epl.models.NavItem

class MainActivity : AppCompatActivity(), SearchBarFragment.SearchListener {
    lateinit var ui: ActivityMainBinding
    private var selectedItem: NavItem? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var searchBarFragment: SearchBarFragment

    private var doubleBackToExitPressedOnce = false // Variable pour gérer le double appui
    private var currentFragment: Fragment? = null // Pour suivre quel fragment est affiché
    private var currentSubFragment: Fragment? = null // Pour suivre quel fragment est affiché

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById<NavigationView>(R.id.nav_view)
        toolbar = findViewById(R.id.toolbarHome)

        // Configurer la toolbar
        setSupportActionBar(toolbar)
        setTitle("")

        // Configurer le toggle pour le drawer
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        toolbar.setNavigationIcon(R.drawable.hamburger_menu)

        val menuView = navigationView.getChildAt(0) as RecyclerView
        menuView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position > 0) {
                    outRect.top = 10
                }
            }
        })

        // Gérer les événements de clic sur les items du menu
        navigationView.setNavigationItemSelectedListener { item ->
            // Gérer les clics sur les options du menu
            when (item.itemId) {
                R.id.settings -> {
                    showSettingsDialog(this, true)
                }

                R.id.share -> {

                }

                R.id.contact -> {

                }

                R.id.about -> {

                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        searchBarFragment = SearchBarFragment()
        // Ajouter la barre de recherche à l'activité, mais la cacher au début
        supportFragmentManager.beginTransaction()
            .add(R.id.search_bar_container, searchBarFragment)
            .hide(searchBarFragment)
            .commit()

        supportFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(fm: FragmentManager, fragment: Fragment) {
                super.onFragmentResumed(fm, fragment)
                if (fragment is SearchBarFragment.SearchListener) {
                    showSearchBarFragment(fragment)
                } else {
                    showSearchBarFragment(null)
                }
            }
        }, true) // "true" permet de surveiller même les fragments imbriqués

        chargerItems()
    }

    private fun chargerItems() {
        val navItems = listOf(
            NavItem(
                findViewById(R.id.nav_home),
                findViewById(R.id.text_home),
                findViewById(R.id.icon_home),
                HomeFragment()
            ),
            NavItem(
                findViewById(R.id.nav_batiment),
                findViewById(R.id.text_batiment),
                findViewById(R.id.icon_batiment),
                BatimentFragment()
            ),
            NavItem(
                findViewById(R.id.nav_salle),
                findViewById(R.id.text_salle),
                findViewById(R.id.icon_salle),
                InfraFragment()
            ),
            NavItem(
                findViewById(R.id.nav_maps),
                findViewById(R.id.text_maps),
                findViewById(R.id.icon_maps),
                MapsFragment()
            )
        )

        for (item in navItems) {
            item.layout.setBackgroundColor(Color.TRANSPARENT)
            item.textView.visibility = View.GONE
            item.icon.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.gray
                )
            )
        }

        val defaultItem = navItems[0]
        selectedItem = defaultItem
        defaultItem.layout.setBackgroundResource(R.drawable.nav_item_bg)
        defaultItem.textView.visibility = View.VISIBLE
        defaultItem.textView.setTextColor(ContextCompat.getColor(this, R.color.black))
        defaultItem.icon.setColorFilter(ContextCompat.getColor(this, R.color.black))

        animateItemSelection(defaultItem)

        // Charge le fragment par défaut
        loadFragment(defaultItem.fragment)

        for (item in navItems) {
            item.layout.setOnClickListener {
                if (selectedItem == item) {
                    return@setOnClickListener // Ne rien faire si l'item est déjà sélectionné
                }


                // Réinitialiser tous les items
                for (otherItem in navItems) {
                    otherItem.layout.setBackgroundColor(Color.TRANSPARENT)
                    otherItem.textView.visibility = View.GONE
                    otherItem.icon.setColorFilter(
                        ContextCompat.getColor(
                            this,
                            R.color.gray
                        )
                    ) // Couleur inactive
                }

                // Animer l'élément sélectionné
                animateItemSelection(item)

                // Mettre à jour l'item sélectionné
                selectedItem = item

                // Charger le fragment correspondant
                loadFragment(item.fragment)
            }
        }
    }

    private fun animateItemSelection(item: NavItem) {
        item.layout.setBackgroundResource(R.drawable.nav_item_bg)

        // Animation d'apparition progressive du texte
        item.textView.alpha = 0f
        item.textView.visibility = View.VISIBLE
        item.textView.setTextColor(ContextCompat.getColor(this, R.color.black))
        item.textView.animate()
            .alpha(1f)
            .setDuration(350)
            .start()

        item.icon.setColorFilter(ContextCompat.getColor(this, R.color.black))
    }

    fun showSearchBarFragment(listener: SearchBarFragment.SearchListener?) {
        if (listener != null) {
            // Afficher la barre de recherche et définir le listener du fragment actif
            searchBarFragment.setSearchListener(listener)
            supportFragmentManager.beginTransaction()
                .show(searchBarFragment)
                .commit()
        } else {
            // Cacher la barre de recherche
            supportFragmentManager.beginTransaction()
                .hide(searchBarFragment)
                .commit()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
        currentFragment = fragment
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Si le drawer est ouvert, fermez-le
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (currentFragment is HomeFragment) {
                // Si on est sur HomeFragment, gérer le double appui pour quitter l'application
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed() // Quitter l'application
                    return
                }

                this.doubleBackToExitPressedOnce = true
                // Afficher un message à l'utilisateur
                Toast.makeText(this, "Appuyez à nouveau pour quitter", Toast.LENGTH_SHORT)
                    .show()

                // Remettre la variable à false après un délai
                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000) // Le délai pour un deuxième appui est de 2 secondes
            } else {
                chargerItems()
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    fun showSettingsDialog(activity: Activity, show: Boolean) {
        val dialog = Dialog(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.settings, null)
        dialog.setContentView(view)

        val languageSpinner = view.findViewById<Spinner>(R.id.languageSpinner)
        val themeSpinner = view.findViewById<Spinner>(R.id.themeSpinner)
        val applyButton = view.findViewById<Button>(R.id.appliquer)

        val languageOptions =
            listOf(activity.getString(R.string.francais), activity.getString(R.string.anglais))
        val themeOptions = listOf(
            activity.getString(R.string.clair),
            activity.getString(R.string.sombre),
            activity.getString(R.string.systeme)
        )

        val languageAdapter = ArrayAdapter(activity, R.layout.spinner_item, languageOptions)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = languageAdapter

        val themeAdapter = ArrayAdapter(activity, R.layout.spinner_item, themeOptions)
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = themeAdapter

        // Charger les préférences sauvegardées
        val sharedPreferences =
            activity.getSharedPreferences("AppSettings", AppCompatActivity.MODE_PRIVATE)
        val savedLanguageCode = sharedPreferences.getString(
            "language_code",
            Locale.getDefault().language
        ) // Langue par défaut du système
        val savedTheme = sharedPreferences.getInt(
            "theme_mode",
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ) // Thème par défaut : Système

        // Appliquer les préférences sauvegardées
        setLocale(activity, savedLanguageCode!!)
        //AppCompatDelegate.setDefaultNightMode(savedTheme)

        // Définir les sélections des spinners
        val savedLanguagePosition = when (savedLanguageCode) {
            "fr" -> 0
            "en" -> 1
            else -> 0 // Français par défaut
        }
        languageSpinner.setSelection(savedLanguagePosition)

        val savedThemePosition = when (savedTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> 2 // Système
            else -> 0 // Clair par défaut
        }
        themeSpinner.setSelection(savedThemePosition)

        // Appliquer les choix au clic sur "Appliquer"
        applyButton.setOnClickListener {
            val selectedLanguagePosition = languageSpinner.selectedItemPosition
            val selectedThemePosition = themeSpinner.selectedItemPosition

            // Appliquer la langue
            val selectedLanguageCode = when (selectedLanguagePosition) {
                0 -> "fr" // Français
                1 -> "en" // Anglais
                else -> "fr"
            }
            setLocale(activity, selectedLanguageCode)

            // Appliquer le thème
            val selectedThemeMode = when (selectedThemePosition) {
                0 -> AppCompatDelegate.MODE_NIGHT_NO // Clair
                1 -> AppCompatDelegate.MODE_NIGHT_YES // Sombre
                2 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM // Système
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(selectedThemeMode)

            // Sauvegarder les préférences
            val editor = sharedPreferences.edit()
            editor.putString("language_code", selectedLanguageCode)
            editor.putInt("theme_mode", selectedThemeMode)
            editor.apply()

            dialog.dismiss()
            activity.recreate() // Recréer l'activité pour appliquer les changements
        }

        // Personnaliser l'apparence du Dialog
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_frame)
        dialog.window?.setLayout(
            (activity.resources.displayMetrics.widthPixels * 0.8).toInt(), // 65% de la largeur de l'écran
            (activity.resources.displayMetrics.heightPixels * 0.28).toInt(),
        )
        if (show) {
            dialog.show()
        }
    }

    fun setLocale(activity: Activity, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = activity.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    override fun onSearch(query: String) {

    }
}