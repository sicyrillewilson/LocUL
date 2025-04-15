package tg.univlome.epl.models.modelsfragments

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import java.io.Serializable

data class FragmentModel(
    var view: View,
    var fragmentContext: Context,
    var fragmentActivity: FragmentActivity,
    var viewLifecycleOwner: LifecycleOwner,
    var recyclerViewId: Int,
    var situation: String = "",
    var type: String = ""
) : Serializable