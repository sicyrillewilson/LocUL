@file:Suppress("DEPRECATION")

package tg.univlome.epl

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import tg.univlome.epl.databinding.ActivityMainBinding
import tg.univlome.epl.models.NavItem
import tg.univlome.epl.ui.LogoFragment
import tg.univlome.epl.ui.SearchBarFragment
import tg.univlome.epl.ui.batiment.BatimentFragment
import tg.univlome.epl.ui.home.HomeFragment
import tg.univlome.epl.ui.infrastructure.InfraFragment
import tg.univlome.epl.ui.maps.MapsFragment
import java.util.Locale

/**
 * Activité principale de l'application : `MainActivity`
 *
 * Description :
 * Cette activité est le point d’entrée visuel de l’application. Elle centralise :
 * - L’interface de navigation latérale (`DrawerLayout`)
 * - L’affichage des fragments (accueil, bâtiments, salles, carte)
 * - La gestion de la langue, du thème et des permissions de localisation
 * - L’intégration des fragments `LogoFragment` et `SearchBarFragment` pour la barre de recherche dynamique
 *
 * Composants principaux :
 * - `DrawerLayout`, `NavigationView`, `Toolbar` : composants d'interface
 * - `NavItem` : modèle utilisé pour représenter les éléments de navigation
 * - `HomeFragment`, `BatimentFragment`, `InfraFragment`, `MapsFragment` : fragments principaux
 * - `SearchBarFragment`, `LogoFragment` : fragments d’en-tête dynamiques
 *
 * Bibliothèques utilisées :
 * - OSMDroid (via MapsFragment)
 * - Firebase (optionnel, indirect)
 * - AndroidX : AppCompat, RecyclerView, Fragment, Lifecycle
 *
 * Fonctionnalités intégrées :
 * - Gestion du thème (clair, sombre, système)
 * - Gestion multilingue (français/anglais)
 * - Permissions runtime pour la localisation
 * - Comportement personnalisé du bouton retour (double clic pour quitter)
 *
 * @see NavItem
 * @see HomeFragment
 * @see LogoFragment
 * @see SearchBarFragment
 */
class MainActivity : AppCompatActivity(), SearchBarFragment.SearchListener,
    LogoFragment.LogoListener {

    // Liaison de la vue
    lateinit var ui: ActivityMainBinding

    // Navigation
    private var selectedItem: NavItem? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    // Fragments de recherche et logo
    private lateinit var searchBarFragment: SearchBarFragment
    private lateinit var logoFragment: LogoFragment
    private var currentLanguageCode: String? = null

    private var doubleBackToExitPressedOnce = false // Variable pour gérer le double appui
    private var currentFragment: Fragment? = null // Pour suivre quel fragment est affiché
    private var navItems = listOf<NavItem>()

    // Demande de permission de localisation
    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission accordée, on recharge les données si HomeFragment est actif
                (currentFragment as? HomeFragment)?.rechargerDonnees()
            } else {
                Toast.makeText(this, "Permission localisation refusée", Toast.LENGTH_SHORT).show()
            }
        }

    /**
     * Méthode appelée lors de la création de l'activité.
     * Initialise la navigation, le thème, la langue, les fragments et les permissions.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // Vérifie si l'activité a déjà été redémarrée
        val hasRestarted = sharedPreferences.getBoolean("theme_applied_once", false)

        // Appliquer langue
        currentLanguageCode =
            sharedPreferences.getString("language_code", Locale.getDefault().language)
        if (currentLanguageCode != null) {
            setLocale(this, currentLanguageCode!!)
        }

        // Appliquer le thème
        val savedTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(savedTheme)

        if (!hasRestarted) {
            // Marque comme redémarré
            sharedPreferences.edit().putBoolean("theme_applied_once", true).apply()

            // Redémarre l'activité pour appliquer le thème proprement
            finish()
            startActivity(intent)
            return // Stoppe ici le flux du premier démarrage
        }

        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        /*// Charger les paramètres sauvegardés
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // Appliquer la langue
        currentLanguageCode =
            sharedPreferences.getString("language_code", Locale.getDefault().language)
        if (currentLanguageCode != null) {
            setLocale(this, currentLanguageCode!!)
        }

        // Appliquer le thème
        val savedTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(savedTheme)*/

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
        logoFragment = LogoFragment()

        // Ajouter la barre de recherche à l'activité, mais la cacher au début
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.search_bar_container, searchBarFragment)
                .hide(searchBarFragment)
                .add(R.id.search_bar_container, logoFragment)
                .show(logoFragment)
                .commit()
        }

        // Ensuite tu fais registerFragmentLifecycleCallbacks + verification immédiate
        supportFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(fm: FragmentManager, fragment: Fragment) {
                super.onFragmentResumed(fm, fragment)

                when (fragment) {
                    is SearchBarFragment.SearchListener -> {
                        showSearchBarFragment(fragment)
                        showLogo(null)
                    }

                    is LogoFragment.LogoListener -> {
                        showLogo(fragment)
                        showSearchBarFragment(null)
                    }
                }
            }
        }, true)

        chargerItems()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Si déjà accordée, on recharge immédiatement les données du HomeFragment
            (currentFragment as? HomeFragment)?.rechargerDonnees()
        }
    }

    /**
     * Initialise les éléments de navigation (`NavItem`) et configure le comportement de sélection.
     * Chaque item déclenche le chargement de son fragment associé.
     */
    private fun chargerItems() {
        navItems = listOf(
            NavItem(
                findViewById(R.id.container_home),
                findViewById(R.id.nav_home),
                findViewById(R.id.text_home),
                findViewById(R.id.icon_home),
                HomeFragment()
            ),
            NavItem(
                findViewById(R.id.container_batiment),
                findViewById(R.id.nav_batiment),
                findViewById(R.id.text_batiment),
                findViewById(R.id.icon_batiment),
                BatimentFragment()
            ),
            NavItem(
                findViewById(R.id.container_salle),
                findViewById(R.id.nav_salle),
                findViewById(R.id.text_salle),
                findViewById(R.id.icon_salle),
                InfraFragment()
            ),
            NavItem(
                findViewById(R.id.container_maps),
                findViewById(R.id.nav_maps),
                findViewById(R.id.text_maps),
                findViewById(R.id.icon_maps),
                MapsFragment()
            )
        )

        for (item in navItems) {
            item.minLayout.setBackgroundColor(Color.TRANSPARENT)
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
        defaultItem.textView.setTextColor(ContextCompat.getColor(this, R.color.mainColor))
        defaultItem.icon.setColorFilter(ContextCompat.getColor(this, R.color.mainColor))

        animateItemSelection(defaultItem)

        // Charge le fragment par défaut
        loadFragment(defaultItem.fragment)

        if (defaultItem.fragment is HomeFragment) {
            showLogo(defaultItem.fragment as LogoFragment.LogoListener)
            showSearchBarFragment(null)
        } else if (defaultItem.fragment is SearchBarFragment.SearchListener) {
            showSearchBarFragment(defaultItem.fragment as SearchBarFragment.SearchListener)
            showLogo(null)
        }

        for (item in navItems) {
            item.layout.setOnClickListener {
                if (selectedItem == item) {
                    return@setOnClickListener // Ne rien faire si l'item est déjà sélectionné
                }

                setItem(item, navItems)

            }
        }
    }

    /**
     * Applique les effets visuels et la logique de sélection d’un élément du menu.
     *
     * @param item Élément sélectionné dans le menu.
     * @param navItems Liste complète des items pour réinitialisation.
     */
    fun setItem(item: NavItem, navItems: List<NavItem>) {
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
            if (otherItem.textView.text == this.getString(R.string.infrastructures)) {
                item.textView.minWidth = 0
            }
        }

        // Animer l'élément sélectionné
        _animateItemSelection(item)

        // Mettre à jour l'item sélectionné
        selectedItem = item

        // Charger le fragment correspondant
        loadFragment(item.fragment)
    }

    /**
     * Effectue également une animation d'arrière-plan pour l'item sélectionné.
     */
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

    /**
     * Effectue une animation d'arrière-plan pour l'item sélectionné.
     */
    private fun _animateItemSelection(item: NavItem) {
        // Appliquer le style de fond
        item.layout.setBackgroundResource(R.drawable.nav_item_bg)

        // Colorier l'icône
        item.icon.setColorFilter(ContextCompat.getColor(this, R.color.black))

        // Préparer le texte pour l'animation
        item.textView.alpha = 0f
        item.textView.visibility = View.VISIBLE
        item.textView.setTextColor(ContextCompat.getColor(this, R.color.black))

        // Mesurer la largeur que devrait avoir le layout en mode wrap_content
        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

        // Garder la largeur actuelle (avant expansion)
        val startWidth = item.layout.width

        // Mesurer la largeur cible avec le texte visible
        item.layout.measure(widthSpec, heightSpec)
        val targetWidth = item.layout.measuredWidth

        // Animation d'expansion horizontale
        val layoutParams = item.layout.layoutParams
        val animator = ValueAnimator.ofInt(startWidth, targetWidth)
        animator.duration = 500
        animator.interpolator = DecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            layoutParams.width = value
            item.layout.layoutParams = layoutParams
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Quand l'animation est terminée, définir en wrap_content
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                item.layout.layoutParams = layoutParams
            }
        })

        // Démarrer l'animation d'expansion
        animator.start()

        // Animation d'apparition progressive du texte
        item.textView.animate()
            .alpha(1f)
            .setDuration(350)
            .start()
    }

    /**
     * Affiche la barre de recherche (`SearchBarFragment`) si un listener est fourni.
     *
     * @param listener Listener implémenté par le fragment actuel ou `null` pour masquer la barre.
     */
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

    /**
     * Affiche ou masque le logo (`LogoFragment`) en fonction du fragment actif.
     */
    fun showLogo(listener: LogoFragment.LogoListener?) {
        if (listener != null) {
            logoFragment.setLogoListener(listener)
            supportFragmentManager.beginTransaction()
                .show(logoFragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .hide(logoFragment)
                .commit()
        }
    }

    /**
     * Charge dynamiquement un fragment dans le conteneur principal.
     *
     * @param fragment Fragment à afficher.
     */
    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
        currentFragment = fragment
    }

    /**
     * Charge le fragment MapsFragment dans le conteneur principal.
     *
     * @param fragment Fragment à afficher.
     */
    fun loadMapsFragment() {
        for (item in navItems) {
            if (item.fragment is MapsFragment) {
                setItem(item, navItems)
                break
            }
        }
    }

    /**
     * Affiche une boîte de dialogue de paramètres permettant à l'utilisateur
     * de choisir la langue et le thème de l'application.
     *
     * @param activity Activité appelante.
     * @param show Si `true`, la boîte de dialogue est immédiatement affichée.
     */
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
            activity.getSharedPreferences("AppSettings", MODE_PRIVATE)
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
            val appLocale = LocaleListCompat.forLanguageTags(selectedLanguageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)

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
            val fragmentManager = (activity as AppCompatActivity).supportFragmentManager
            fragmentManager.fragments.forEach {
                fragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            }

            //activity.recreate() // Recréer l'activité pour appliquer les changements
            activity.finish()
            activity.startActivity(activity.intent)
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

    /**
     * Applique la langue spécifiée à l'application en mettant à jour la configuration du contexte.
     *
     * @param activity Activité cible.
     * @param languageCode Code de langue (ex: "fr", "en").
     */
    fun setLocale(activity: Activity, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = activity.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    /**
     * Gère le comportement du bouton retour, avec une vérification de double appui
     * pour quitter l'application depuis le `HomeFragment`.
     */
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

    override fun onSearch(query: String) {}
}