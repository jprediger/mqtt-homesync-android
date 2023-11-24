package io.emqx.mqtt.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import io.emqx.mqtt.MainActivity
import io.emqx.mqtt.Publish
import io.emqx.mqtt.R


class CozinhaFragment : BaseFragment() {

    override fun onResume() {
        super.onResume()
        requireActivity().title = "Sala de Estar" // Atualize o título aqui

        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        ledMode = sharedPreferences.getBoolean("ledmode", false)
        ledStatus = sharedPreferences.getString("ledstatus", "OFF").toString()

        ledSwitch.isChecked = ledMode
        ledOnOff.text = ledStatus

    } // Carregar os dados salvos quando o fragmento e reaberto

    override fun onPause() {
        super.onPause()

        // Save the progress in SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("ledmode", ledMode)
        editor.putString("ledstatus", ledStatus)
        editor.apply()
    } // Salva os dados qua

    override val layoutResId: Int
        get() = R.layout.fragment_cozinha

    private lateinit var sTemp: TextView
    private lateinit var sHum: TextView

    private lateinit var ledSwitch: SwitchCompat
    private lateinit var ledOnOff: TextView

    private var ledMode: Boolean = false
    private var ledStatus: String = "OFF"

    override fun setUpView(view: View) {

        sTemp = view.findViewById(R.id.statustemp)
        sHum = view.findViewById(R.id.statushum)

        ledSwitch = view.findViewById(R.id.ledSwitch)
        ledOnOff = view.findViewById(R.id.ledOnOff)

        // SWITCH LED
        ledSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Crie um objeto Publish com os dados necessários
                val publish = Publish("64c2c909ce81b/esp32/casa/cozinha/ledcozinha", "1", 0, false)

                // Chame a função onMqttPublish na MainActivity (ou na classe que contém a função)
                val mainActivity = activity as? MainActivity
                mainActivity?.onMqttPublish(publish, null)
                ledMode = true
                ledStatus = "ON"
                ledOnOff.text = ledStatus
            }
            else {
                val publish = Publish("64c2c909ce81b/esp32/casa/cozinha/ledcozinha", "0", 0, false)
                val mainActivity = activity as? MainActivity
                mainActivity?.onMqttPublish(publish, null)
                ledMode = false
                ledStatus = "OFF"
                ledOnOff.text = ledStatus
            }}
    }

    fun updateTempCozinha(message: String?) {
        val tempConc = "$message°C" // Concatenando o valor de temp recebido com °C
        sTemp.text = tempConc // Mostrando o valor concatenado no textview
    }

    fun updateHumCozinha(message: String?) {
        val humConc = "$message%" // Concatenando o valor de temp recebido com %
        sHum.text = humConc // Mostrando o valor concatenado no textview
    }


    companion object {
        fun newInstance(): CozinhaFragment {
            return CozinhaFragment()
        }
    }
}