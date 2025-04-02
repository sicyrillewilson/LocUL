package tg.univlome.epl.models.modelsfragments

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import tg.univlome.epl.adapter.BatimentFragmentAdapter
import tg.univlome.epl.models.Batiment
import java.io.Serializable

data class BatimentFragmentModel(
    var view: View,
    var fragmentContext: Context,
    var fragmentActivity: FragmentActivity,
    var viewLifecycleOwner: LifecycleOwner,
    var recyclerViewId: Int,
    var type: String = ""
) : Serializable