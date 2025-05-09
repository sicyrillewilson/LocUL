package tg.univlome.epl.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tg.univlome.epl.R

class LogoFragment : Fragment() {

    interface LogoListener {}

    private var logoListener: LogoListener? = null

    fun setLogoListener(listener: LogoListener) {
        logoListener = listener
    }

    fun getLogoListener(): LogoListener? {
        return this.logoListener
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_logo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("LOGO_FRAGMENT", "LogoFragment visible")
    }

}