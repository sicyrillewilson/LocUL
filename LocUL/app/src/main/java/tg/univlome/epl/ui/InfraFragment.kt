package tg.univlome.epl.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import tg.univlome.epl.R

class InfraFragment : Fragment() {
    private var currentSelectedNav: View? = null
    private var currentIndicator: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_infra, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Par défaut, "Tout" est sélectionné
        val allNav = view.findViewById<LinearLayout>(R.id.nav_all)
        val allIndicator = view.findViewById<View>(R.id.indicator_all)
        currentSelectedNav = allNav
        currentIndicator = allIndicator
        loadFragment(AllInfraFragment())

        // Configurer les listeners pour chaque élément de navigation
        setupNavItem(view, R.id.nav_all, R.id.indicator_all) {
            loadFragment(AllInfraFragment())
        }

        setupNavItem(view, R.id.nav_nord, R.id.indicator_nord) {
            loadFragment(NordInfraFragment())
        }

        setupNavItem(view, R.id.nav_sud, R.id.indicator_sud) {
            loadFragment(SudInfraFragment())
        }
    }

    private fun setupNavItem(view: View, navItemId: Int, indicatorId: Int, action: () -> Unit) {
        val navItem = view.findViewById<LinearLayout>(navItemId)
        val indicator = view.findViewById<View>(indicatorId)

        navItem.setOnClickListener {
            // Si c'est déjà l'élément sélectionné, ne rien faire
            if (currentSelectedNav == navItem) return@setOnClickListener

            // Désélectionner l'élément précédent
            currentIndicator?.visibility = View.INVISIBLE

            // Sélectionner le nouvel élément
            indicator.visibility = View.VISIBLE
            animateIndicator(indicator)
            currentSelectedNav = navItem
            currentIndicator = indicator

            // Exécuter l'action associée à cet élément
            action()
        }
    }

    private fun animateIndicator(indicator: View) {
        // Charger l'animation
        val scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.indicator_scale)

        // Appliquer l'animation à l'indicateur
        indicator.startAnimation(scaleAnimation)
    }

    private fun loadFragment(fragment: Fragment) {
        (requireActivity() as AppCompatActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.infra_container, fragment)
            .commit()
    }

}