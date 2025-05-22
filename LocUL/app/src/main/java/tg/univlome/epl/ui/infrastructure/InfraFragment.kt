package tg.univlome.epl.ui.infrastructure

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
 * Fragment InfraFragment : Fragment de navigation des infrastructures du campus
 *
 * Description :
 * Ce fragment permet à l’utilisateur de filtrer les infrastructures universitaires
 * selon leur position géographique :
 *  - Toutes les infrastructures
 *  - Infrastructures situées au Campus Nord
 *  - Infrastructures situées au Campus Sud
 *
 * Chaque filtre déclenche dynamiquement l’affichage d’un sous-fragment correspondant
 * (ex : `AllInfraFragment`, `NordInfraFragment`, `SudInfraFragment`) dans le conteneur central.
 * La navigation est enrichie par un indicateur visuel animé qui signale l’élément actif.
 *
 * Composants principaux :
 *  - `LinearLayout` pour les onglets de navigation (nav_all, nav_nord, nav_sud)
 *  - `View` servant d’indicateur actif pour chaque onglet
 *  - `Fragment` chargé dynamiquement dans `infra_container`
 *  - Animation `scale` appliquée à l’indicateur actif
 *
 * Bibliothèques utilisées :
 *  - AndroidX Fragment
 *  - Android AnimationUtils (animation personnalisée de l’indicateur)
 *
 * @see AllInfraFragment pour l’affichage global
 * @see NordInfraFragment pour les infrastructures du campus nord
 * @see SudInfraFragment pour les infrastructures du campus sud
 */
class InfraFragment : Fragment() {

    private var currentSelectedNav: View? = null
    private var currentIndicator: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Crée et retourne la vue racine du fragment à partir du fichier `fragment_infra.xml`.
     *
     * @return Vue contenant les boutons de navigation et le conteneur dynamique
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_infra, container, false)
    }

    /**
     * Initialise les éléments de navigation par défaut et configure les écouteurs
     * pour les sections "Tout", "Nord" et "Sud".
     */
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

    /**
     * Initialise un élément de navigation cliquable avec son indicateur et son action associée.
     *
     * @param view Vue racine du fragment
     * @param navItemId ID du bouton de navigation
     * @param indicatorId ID de l’indicateur visuel
     * @param action Action à exécuter lors du clic (chargement du fragment correspondant)
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
     * Lance une animation de type "scale" sur l’indicateur sélectionné
     * pour améliorer la perception visuelle de la navigation.
     *
     * @param indicator Indicateur à animer
     */
    private fun animateIndicator(indicator: View) {
        // Charger l'animation
        val scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.indicator_scale)

        // Appliquer l'animation à l'indicateur
        indicator.startAnimation(scaleAnimation)
    }

    /**
     * Remplace dynamiquement le contenu du conteneur `infra_container` par le fragment fourni.
     *
     * @param fragment Fragment à charger dynamiquement dans l’interface
     */
    private fun loadFragment(fragment: Fragment) {
        (requireActivity() as AppCompatActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.infra_container, fragment)
            .commit()
    }

}