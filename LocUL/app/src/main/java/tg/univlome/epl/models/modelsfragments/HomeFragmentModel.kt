package tg.univlome.epl.models.modelsfragments

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import java.io.Serializable

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