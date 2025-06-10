package tg.univlome.epl.models.modelsfragments

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import java.io.Serializable

/**
 * Classe modèle `HomeFragmentModel` : Encapsule les informations et dépendances
 * nécessaires à la gestion dynamique d’une section de l’écran d’accueil (`HomeFragment`).
 *
 * Description :
 * Ce modèle centralise les éléments indispensables à la configuration d’une section
 * du `HomeFragment`. Il permet de lier une vue spécifique, son `RecyclerView`, son
 * contexte d'exécution, le fragment cible à afficher en vue complète, et les éléments
 * du cycle de vie nécessaires à l'interaction avec les `LiveData` ou les coroutines.
 *
 * Il est principalement utilisé pour alimenter dynamiquement différentes catégories
 * d’entités (bâtiments, salles, infrastructures…) sur la page d’accueil de l’application,
 * tout en assurant une navigation fluide vers leur affichage détaillé.
 *
 * Propriétés :
 * @property view Vue principale du fragment parent contenant le `RecyclerView` ciblé.
 * @property fragmentContext Contexte du fragment (généralement `requireContext()`).
 * @property fragmentActivity Activité hôte du fragment (généralement `requireActivity()`).
 * @property viewLifecycleOwner Propriétaire du cycle de vie, utilisé pour observer les `LiveData`.
 * @property recyclerViewId ID du `RecyclerView` à mettre à jour pour cette section.
 * @property fragmentManager Gestionnaire de fragments utilisé pour les transactions.
 * @property newFragment Fragment à afficher lorsque l'utilisateur veut voir tous les éléments.
 * @property type Chaîne identifiant le type de contenu affiché (ex. : "enseignement", "salles").
 *
 * Implémente :
 * - `Serializable` : permet de sérialiser l'objet si besoin lors de transferts.
 *
 * @see tg.univlome.epl.ui.home.HomeFragment.loadData
 */
data class HomeFragmentModel(
    var view: View,
    var fragmentContext: Context,
    var fragmentActivity: FragmentActivity,
    var viewLifecycleOwner: LifecycleOwner,
    var recyclerViewId: Int,
    var fragmentManager: FragmentManager,
    var newFragment: Fragment,
    var type: String = ""
) : Serializable