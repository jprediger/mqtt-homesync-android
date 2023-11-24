package io.emqx.mqtt.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import io.emqx.mqtt.MainActivity
import io.emqx.mqtt.Publish
import io.emqx.mqtt.R

class Quarto1Fragment : BaseFragment() {

    override fun onResume() {
        super.onResume()
        requireActivity().title = "Sala de Estar" // Atualize o título aqui

        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        seekBarIntProgresso = sharedPreferences.getInt("seekBarIntProgress", 0)

        ledMode = sharedPreferences.getBoolean("ledmode", false)
        ledStatus = sharedPreferences.getString("ledstatus", "OFF").toString()

        led2Mode = sharedPreferences.getBoolean("led2mode", false)
        led2Status = sharedPreferences.getString("led2status", "OFF").toString()


        seekBarLed2Int.progress = seekBarIntProgresso
        ledSwitch.isChecked = ledMode
        ledOnOff.text = ledStatus

        led2Switch.isChecked = led2Mode
        led2OnOff.text = led2Status

    } // Carregar os dados salvos quando o fragmento e reaberto

    override fun onPause() {
        super.onPause()

        // Save the progress in SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("seekBarIntProgress", seekBarLed2Int.progress)
        editor.putBoolean("ledmode", ledMode)
        editor.putString("ledstatus", ledStatus)
        editor.putBoolean("led2mode", led2Mode)
        editor.putString("led2status", led2Status)
        editor.apply()
    } // Salva os dados quando o fragmento e fechado

    private lateinit var sTemp: TextView
    private lateinit var sHum: TextView

    private lateinit var ledSwitch: SwitchCompat
    private lateinit var ledOnOff: TextView

    private lateinit var led2Switch: SwitchCompat
    private lateinit var led2OnOff: TextView
    private lateinit var led2Int: TextView
    private lateinit var seekBarLed2Int: SeekBar
    private lateinit var ledIntProgresso: TextView

    private var ledMode: Boolean = false
    private var ledStatus: String = "OFF"

    private var led2Mode: Boolean = false
    private var led2Status: String = "OFF"

    private var seekBarIntProgresso: Int = 0

    override val layoutResId: Int
        get() = R.layout.fragment_quarto1

    override fun setUpView(view: View) {

        sTemp = view.findViewById(R.id.statustemp)
        sHum = view.findViewById(R.id.statushum)

        ledSwitch = view.findViewById(R.id.ledSwitch)
        ledOnOff = view.findViewById(R.id.ledOnOff)

        led2Switch = view.findViewById(R.id.ledSwitch2)
        led2OnOff = view.findViewById(R.id.ledOnOff2)
        led2Int = view.findViewById(R.id.led2Int)
        seekBarLed2Int = view.findViewById(R.id.seekBarLed2Int)
        ledIntProgresso = view.findViewById(R.id.ledIntProgresso)

        // SWITCH LED
        ledSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Crie um objeto Publish com os dados necessários
                val publish = Publish("64c2c909ce81b/esp32/casa/quarto1/ledquarto1", "1", 0, false)

                // Chame a função onMqttPublish na MainActivity (ou na classe que contém a função)
                val mainActivity = activity as? MainActivity
                mainActivity?.onMqttPublish(publish, null)
                ledMode = true
                ledStatus = "ON"
                ledOnOff.text = ledStatus
            }
            else {
                val publish = Publish("64c2c909ce81b/esp32/casa/quarto1/ledquarto1", "0", 0, false)
                val mainActivity = activity as? MainActivity
                mainActivity?.onMqttPublish(publish, null)
                ledMode = false
                ledStatus = "OFF"
                ledOnOff.text = ledStatus
            }}

        // SWITCH LED2
        led2Switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Crie um objeto Publish com os dados necessários
                val publish = Publish("64c2c909ce81b/esp32/casa/quarto1/dimmerstatusquarto", "1", 0, false)

                // Chame a função onMqttPublish na MainActivity (ou na classe que contém a função)
                val mainActivity = activity as? MainActivity
                mainActivity?.onMqttPublish(publish, null)
                led2Mode = true
                led2Status = "ON"
                led2OnOff.text = led2Status
            }
            else {
                val publish = Publish("64c2c909ce81b/esp32/casa/quarto1/dimmerstatusquarto", "0", 0, false)
                val mainActivity = activity as? MainActivity
                mainActivity?.onMqttPublish(publish, null)
                led2Mode = false
                led2Status = "OFF"
                led2OnOff.text = led2Status
            }}

        // SEEKBAR LED2 INTENSIDADE
        seekBarLed2Int.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                var temp = "$progress"
                ledIntProgresso.text = temp
                seekBarIntProgresso = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Este método é chamado quando o usuário começa a arrastar a SeekBar.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Crie um objeto Publish com os dados necessários
                val publish = Publish(
                    "64c2c909ce81b/esp32/casa/quarto1/dimmerintquarto",
                    seekBarIntProgresso.toString(),
                    0,
                    false
                )

                // Chame a função onMqttPublish na MainActivity (ou na classe que contém a função)
                val mainActivity = activity as? MainActivity
                mainActivity?.onMqttPublish(publish, null)
            }
        })
    }

    fun updateTempQuarto1(message: String?) {
        val tempConc = "$message°C" // Concatenando o valor de temp recebido com °C
        sTemp.text = tempConc // Mostrando o valor concatenado no textview
    }

    fun updateHumQuarto1(message: String?) {
        val humConc = "$message%" // Concatenando o valor de temp recebido com %
        sHum.text = humConc // Mostrando o valor concatenado no textview
    }


    companion object {
        fun newInstance(): Quarto1Fragment {
            return Quarto1Fragment()
        }
    }
}