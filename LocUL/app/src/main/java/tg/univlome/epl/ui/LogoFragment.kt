package tg.univlome.epl.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tg.univlome.epl.R

/**
 * Fragment LogoFragment : Affichage du logo au lancement
 *
 * Description :
 * Fragment utilisé pour **afficher temporairement un logo ou écran d’accueil** au lancement de l’application.
 * Il peut être utilisé comme un splash screen ou comme une vue d’introduction silencieuse.
 * Cette classe a donc pour but de :
 * - Fournir un écran statique avec le logo de l’application.
 * - Notifier un composant parent via une interface optionnelle (`LogoListener`).
 *
 * Composants :
 * - `fragment_logo.xml` : Mise en page du logo.
 * - `LogoListener` : Interface pour l’interaction optionnelle avec d'autres fragments ou activités.
 *
 * Bibliothèques utilisées :
 * - AndroidX Fragment
 *
 * Exemple d’utilisation :
 * ```
 * val fragment = LogoFragment()
 * fragment.setLogoListener(object : LogoFragment.LogoListener { ... })
 * ```
 */
class LogoFragment : Fragment() {

    /**
     * Interface de rappel optionnelle pour interagir avec des composants parents
     * (comme une activité ou un autre fragment) après l’affichage du logo.
     */
    interface LogoListener

    private var logoListener: LogoListener? = null

    /**
     * Permet d’enregistrer un listener pour réagir à des événements du fragment.
     *
     * @param listener Une implémentation de [LogoListener]
     */
    fun setLogoListener(listener: LogoListener) {
        logoListener = listener
    }

    /**
     * Récupère le listener actuellement assigné au fragment.
     *
     * @return L’instance de [LogoListener] ou `null` si aucun n’a été défini
     */
    fun getLogoListener(): LogoListener? {
        return this.logoListener
    }

    /**
     * Crée la vue du fragment à partir du layout `fragment_logo`.
     *
     * @return La vue représentant le logo
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_logo, container, false)
    }

    /**
     * Méthode appelée après que la vue a été créée. Affiche un log dans la console pour suivi du cycle de vie.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("LOGO_FRAGMENT", "LogoFragment visible")
    }

}