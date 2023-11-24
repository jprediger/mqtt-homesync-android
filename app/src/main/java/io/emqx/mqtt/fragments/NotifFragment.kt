package io.emqx.mqtt.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import io.emqx.mqtt.MainActivity
import io.emqx.mqtt.Publish
import io.emqx.mqtt.R

class NotifFragment : BaseFragment() {

    override fun onResume() {
        super.onResume()

        val sharedPreferences = requireContext().getSharedPreferences("MyNotificationPrefs", Context.MODE_PRIVATE)

        gasStatusPref = sharedPreferences.getBoolean("gasStatusPref", false)
        gasSwitch.isChecked = gasStatusPref
        chamasStatusPref = sharedPreferences.getBoolean("chamasStatusPref", false)
        chamasSwitch.isChecked = chamasStatusPref

    } // Carregar os dados salvos quando o fragmento e reaberto

    override fun onPause() {
        super.onPause()

        // Save the progress in SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyNotificationPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putBoolean("gasStatusPref", gasStatusPref)
        editor.putBoolean("chamasStatusPref", chamasStatusPref)

        editor.apply()
    } // Salva os dados quando o fragmento e fechado

    private lateinit var gasSwitch: SwitchCompat
    private lateinit var chamasSwitch: SwitchCompat

    private var gasStatusPref: Boolean = false
    private var chamasStatusPref: Boolean = false


    override val layoutResId: Int
        get() = R.layout.fragment_notif

    override fun setUpView(view: View) {

        gasSwitch = view.findViewById(R.id.gasSwitch)
        chamasSwitch = view.findViewById(R.id.chamasSwitch)

        // Switch habilitar notificações gás
        gasSwitch.setOnCheckedChangeListener { _, isChecked ->
            gasStatusPref = if (isChecked) {
                (activity as? MainActivity)?.notificacoesGasCozinha(true)
                true
            } else {
                (activity as? MainActivity)?.notificacoesGasCozinha(false)
                false
            }
        }

        // Switch habilitar notificações chamas
        chamasSwitch.setOnCheckedChangeListener { _, isChecked ->
            chamasStatusPref = if (isChecked) {
                (activity as? MainActivity)?.notificacoesChamasCozinha(true)
                true
            } else {
                (activity as? MainActivity)?.notificacoesChamasCozinha(false)
                false
            }
        }
    }





    companion object {
        fun newInstance(): Quarto2Fragment {
            return Quarto2Fragment()
        }
    }
}