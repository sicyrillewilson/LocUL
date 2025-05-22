package tg.univlome.epl.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tg.univlome.epl.R

/**
 * Fragment LocULFragment : Localisation statique de l'Université de Lomé
 *
 * Description :
 * Ce fragment affiche un écran de **localisation générale ou symbolique** de l’Université de Lomé.
 * Il peut servir à présenter une carte figée, une bannière descriptive ou toute autre vue informative
 * concernant la position ou l'identité géographique de l’UL.
 * Cette classe a donc pour but de :
 * - Fournir un point d’entrée statique à la localisation de l’université.
 * - Affichage complémentaire ou introductif dans le système de navigation de l'application.
 *
 * Composants :
 * - `fragment_loc_ul.xml` : layout associé à la représentation de l’UL.
 *
 * Bibliothèques utilisées :
 * - AndroidX Fragment
 *
 * Exemple d’utilisation :
 * ```
 * val fragment = LocULFragment()
 * supportFragmentManager.beginTransaction()
 *     .replace(R.id.container, fragment)
 *     .commit()
 * ```
 */
class LocULFragment : Fragment() {

    /**
     * Crée la vue du fragment à partir du layout `fragment_loc_ul`.
     *
     * @param inflater Le LayoutInflater utilisé pour gonfler la vue
     * @param container Le ViewGroup parent dans lequel la vue sera insérée
     * @param savedInstanceState État précédemment enregistré du fragment
     * @return La vue représentant la localisation de l’UL
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_loc_ul, container, false)
    }

    /**
     * Appelée après que la vue a été créée. Peut être utilisée pour initialiser
     * les composants d'interface ou déclencher des animations/données.
     *
     * @param view La vue racine du fragment
     * @param savedInstanceState L’état sauvegardé du fragment
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}