package tg.univlome.epl.models

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * Classe NavItem : Élément de navigation personnalisé dans l'interface utilisateur.
 *
 * Description :
 * Cette classe encapsule les composants visuels et logiques d’un item de navigation
 * dans l’application, permettant d’associer une vue de navigation (texte + icône + layouts)
 * à un fragment correspondant.
 *
 * Utilisation :
 * - Employé dans les fragments comme `BatimentFragment` ou `InfraFragment`
 *   pour basculer dynamiquement entre différents sous-fragments.
 * - Fournit une structure pratique pour gérer l’état de sélection et les interactions UI.
 *
 * Composants :
 * @property layout Le layout principal de l’item (incluant l’icône et le texte)
 * @property minLayout Layout réduit ou secondaire, souvent utilisé pour l’effet de sélection
 * @property textView Texte affiché pour cet item (ex: "Tous", "Nord", "Sud")
 * @property icon Icône associée à cet item
 * @property fragment Fragment associé à cet item, à afficher lors de la sélection
 *
 * Exemple :
 * ```kotlin
 * val navItem = NavItem(
 *     layout = view.findViewById(R.id.nav_all),
 *     minLayout = view.findViewById(R.id.indicator_all),
 *     textView = view.findViewById(R.id.txt_all),
 *     icon = view.findViewById(R.id.icon_all),
 *     fragment = AllBatimentFragment()
 * )
 * ```
 *
 * @see androidx.fragment.app.Fragment
 * @see tg.univlome.epl.ui.batiment.BatimentFragment
 * @see tg.univlome.epl.ui.infrastructure.InfraFragment
 */
data class NavItem(
    val layout: LinearLayout,
    val minLayout: LinearLayout,
    val textView: TextView,
    val icon: ImageView,
    val fragment: Fragment
)
