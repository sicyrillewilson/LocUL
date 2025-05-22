package tg.univlome.epl.ui.batiment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import tg.univlome.epl.R

/**
 * Fragment BatimentFragment : Fragment de navigation des bâtiments par zone géographique
 *
 * Description :
 * Ce fragment offre une interface utilisateur permettant de filtrer et d'afficher
 * les bâtiments du campus selon leur situation géographique :
 *  - Tous les bâtiments
 *  - Bâtiments situés au Campus Nord
 *  - Bâtiments situés au Campus Sud
 *
 * L’utilisateur peut naviguer entre ces catégories via une barre de navigation personnalisée,
 * avec des indicateurs visuels animés pour refléter la sélection.
 * Chaque filtre déclenche le chargement d’un sous-fragment spécifique (`AllBatimentFragment`, etc.).
 *
 * Composants principaux :
 *  - `LinearLayout` interactif pour chaque onglet de navigation (nav_all, nav_nord, nav_sud)
 *  - `View` utilisé comme indicateur visuel de sélection
 *  - `AnimationUtils` pour une animation d’indicateur fluide
 *  - Sous-fragments affichés dynamiquement dans un conteneur (`R.id.batiment_container`)
 *
 * Bibliothèques utilisées :
 *  - AndroidX Fragment
 *  - Android AnimationUtils (animation de scale)
 *
 * @see AllBatimentFragment pour l’affichage de tous les bâtiments
 * @see NordBatimentFragment pour les bâtiments du Campus Nord
 * @see SudBatimentFragment pour les bâtiments du Campus Sud
 */
class BatimentFragment : Fragment() {

    private var currentSelectedNav: View? = null
    private var currentIndicator: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Crée la vue du fragment à partir de `fragment_batiment.xml`.
     *
     * @return Vue principale contenant la navigation par zone
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_batiment, container, false)
    }

    /**
     * Initialise l’état initial du fragment et configure la navigation
     * entre les différentes zones géographiques des bâtiments.
     *
     * Par défaut, charge `AllBatimentFragment`.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Par défaut, "Tout" est sélectionné
        val allNav = view.findViewById<LinearLayout>(R.id.nav_all)
        val allIndicator = view.findViewById<View>(R.id.indicator_all)
        currentSelectedNav = allNav
        currentIndicator = allIndicator
        loadFragment(AllBatimentFragment())

        // Configurer les listeners pour chaque élément de navigation
        setupNavItem(view, R.id.nav_all, R.id.indicator_all) {
            loadFragment(AllBatimentFragment())
        }

        setupNavItem(view, R.id.nav_nord, R.id.indicator_nord) {
            loadFragment(NordBatimentFragment())
        }

        setupNavItem(view, R.id.nav_sud, R.id.indicator_sud) {
            loadFragment(SudBatimentFragment())
        }
    }

    /**
     * Associe un écouteur à un bouton de navigation et son indicateur.
     * Gère la sélection visuelle, les animations et le changement de fragment.
     *
     * @param view Vue racine contenant l’élément
     * @param navItemId ID de l’élément de navigation cliquable
     * @param indicatorId ID de l’indicateur à afficher
     * @param action Action à exécuter au clic (chargement d’un sous-fragment)
     */
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

    /**
     * Applique une animation de type "scale" à l’indicateur sélectionné.
     *
     * @param indicator Vue représentant l’indicateur actif
     */
    private fun animateIndicator(indicator: View) {
        // Charger l'animation
        val scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.indicator_scale)

        // Appliquer l'animation à l'indicateur
        indicator.startAnimation(scaleAnimation)
    }

    /**
     * Remplace dynamiquement le fragment affiché dans le conteneur central.
     *
     * @param fragment Fragment à afficher dans `batiment_container`
     */
    private fun loadFragment(fragment: Fragment) {
        (requireActivity() as AppCompatActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.batiment_container, fragment)
            .commit()
    }

}