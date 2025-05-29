package tg.univlome.epl.models

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * Classe `NavItem`
 *
 * Représente un élément de navigation personnalisé utilisé dans l'activité principale (`MainActivity`).
 * Chaque instance de `NavItem` correspond à un bouton ou une section du menu latéral ou de l’interface principale
 * qui déclenche l'affichage d'un fragment associé.
 *
 * ## Utilisation :
 * - Gérée par la méthode `chargerItems()` dans `MainActivity`
 * - Permet de centraliser la logique de changement d’apparence et de fragment lors de la sélection d’un onglet
 *
 * ## Propriétés :
 * @property layout `LinearLayout` principal contenant l'icône et le texte de l’élément (utilisé pour détecter les clics)
 * @property minLayout `LinearLayout` secondaire ou réduit
 * @property textView `TextView` affichant le nom de l’élément de navigation
 * @property icon `ImageView` affichant l’icône associée à l’élément
 * @property fragment `Fragment` instance du fragment à afficher lorsque cet élément est sélectionné
 *
 * @see tg.univlome.epl.MainActivity.chargerItems
 */
data class NavItem(
    val layout: LinearLayout,
    val minLayout: LinearLayout,
    val textView: TextView,
    val icon: ImageView,
    val fragment: Fragment
)
