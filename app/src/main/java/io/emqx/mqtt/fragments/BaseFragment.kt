package io.emqx.mqtt.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

abstract class BaseFragment : Fragment() {

    // Método para restaurar o estado do fragmento
    open fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        // Aqui, você pode restaurar dados do savedInstanceState, se necessário
        // Implementação padrão não faz nada
    }

    var fragmentActivity: FragmentActivity? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(layoutResId, container, false)
        setUpView(view)
        return view
    }

    protected abstract val layoutResId: Int
    protected abstract fun setUpView(view: View)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentActivity = activity
    }

}