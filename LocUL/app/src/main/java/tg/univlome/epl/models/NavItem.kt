package tg.univlome.epl.models

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

data class NavItem(
    val layout: LinearLayout,
    val minLayout: LinearLayout,
    val textView: TextView,
    val icon: ImageView,
    val fragment: Fragment
)
