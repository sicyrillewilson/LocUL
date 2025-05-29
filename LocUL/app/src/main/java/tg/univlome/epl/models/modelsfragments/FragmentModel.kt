package tg.univlome.epl.models.modelsfragments

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import java.io.Serializable

/**
 * Modèle de données pour encapsuler les éléments nécessaires à un fragment affichant une liste.
 *
 * Cette classe sert à regrouper les informations essentielles utilisées lors de la mise à jour
 * dynamique des données dans une `RecyclerView`, typiquement dans le contexte d’un fragment.
 * Elle facilite l’appel de méthodes comme `BatimentUtils.updateBatiments` en centralisant
 * les composants liés à l’interface, au cycle de vie et à la configuration du fragment.
 *
 * @property view Vue racine du fragment (souvent obtenue dans `onCreateView`)
 * @property fragmentContext Contexte de l’application ou de l’activité
 * @property fragmentActivity Activité parente du fragment, utilisée pour accéder à certains services Android
 * @property viewLifecycleOwner Propriétaire du cycle de vie de la vue du fragment (pour les LiveData)
 * @property recyclerViewId ID de la `RecyclerView` utilisée pour afficher la liste dynamique
 * @property situation Contexte ou position géographique associée au fragment (ex : "nord", "sud")
 * @property type Type d’élément ou catégorie affichée (optionnel)
 *
 * @see tg.univlome.epl.ui.batiment.AllBatimentFragment
 * @see tg.univlome.epl.ui.batiment.NordBatimentFragment
 * @see tg.univlome.epl.ui.batiment.SudBatimentFragment
 * @see tg.univlome.epl.ui.infrastructure.AllInfraFragment
 * @see tg.univlome.epl.ui.infrastructure.NordInfraFragment
 * @see tg.univlome.epl.ui.infrastructure.SudInfraFragment
 */
data class FragmentModel(
    var view: View,
    var fragmentContext: Context,
    var fragmentActivity: FragmentActivity,
    var viewLifecycleOwner: LifecycleOwner,
    var recyclerViewId: Int,
    var situation: String = "",
    var type: String = ""
) : Serializable