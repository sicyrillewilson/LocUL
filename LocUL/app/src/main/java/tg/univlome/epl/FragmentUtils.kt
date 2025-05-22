package tg.univlome.epl

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Objet FragmentUtils : Fournit une fonction utilitaire simplifiée
 * pour effectuer une transition vers un nouveau fragment.
 *
 * Description :
 * Cette classe utilitaire encapsule la logique standard de remplacement
 * de fragment dans l'application. Elle permet de :
 *  - Remplacer dynamiquement un fragment existant
 *  - Ajouter l'opération à la pile de retour (`BackStack`)
 *
 * Elle est principalement utilisée pour centraliser le code lié
 * aux transactions de fragments, assurant une meilleure maintenabilité.
 *
 * Composants principaux :
 *  - FragmentManager : gestionnaire de fragments fourni par AndroidX
 *  - Fragment : nouveau fragment à afficher
 *
 * @see androidx.fragment.app.Fragment
 * @see androidx.fragment.app.FragmentManager
 */
object FragmentUtils {

    /**
     * Remplace le fragment courant par un nouveau, et ajoute l’opération à la pile de retour.
     *
     * @param fragmentManager Instance de `FragmentManager` utilisée pour exécuter la transaction.
     * @param newFragment Nouveau fragment à afficher dans le conteneur `R.id.container`.
     */
    fun ouvrirFragment(fragmentManager: FragmentManager, newFragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}