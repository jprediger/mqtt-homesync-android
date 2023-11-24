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

class Quarto2Fragment : BaseFragment() {

    override fun onResume() {
        super.onResume()
        requireActivity().title = "Sala de Estar" // Atualize o título aqui

        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        seekBarPersianaIntProgresso = sharedPreferences.getInt("seekBarPersianaIntProgress", 0)

        ledMode = sharedPreferences.getBoolean("ledmode", false)
        ledStatus = sharedPreferences.getString("ledstatus", "OFF").toString()


        seekBarPersianaInt.progress = seekBarPersianaIntProgresso
        ledSwitch.isChecked = ledMode
        ledOnOff.text = ledStatus


    } // Carregar os dados salvos quando o fragmento e reaberto

    override fun onPause() {
        super.onPause()

        // Save the progress in SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("seekBarPersianaIntProgress", seekBarPersianaInt.progress)
        editor.putBoolean("ledmode", ledMode)
        editor.putString("ledstatus", ledStatus)
        editor.apply()
    } // Salva os dados quando o fragmento e fechado

    private lateinit var sTemp: TextView
    private lateinit var sHum: TextView

    private lateinit var ledSwitch: SwitchCompat
    private lateinit var ledOnOff: TextView

    private lateinit var seekBarPersianaInt: SeekBar
    private lateinit var persianaIntProgresso: TextView

    private var ledMode: Boolean = false
    private var ledStatus: String = "OFF"

    private var seekBarPersianaIntProgresso: Int = 0

    override val layoutResId: Int
        get() = R.layout.fragment_quarto2

    override fun setUpView(view: View) {

        sTemp = view.findViewById(R.id.statustemp)
        sHum = view.findViewById(R.id.statushum)

        ledSwitch = view.findViewById(R.id.ledSwitch)
        ledOnOff = view.findViewById(R.id.ledOnOff)

        seekBarPersianaInt = view.findViewById(R.id.seekBarPersianaInt)
        persianaIntProgresso = view.findViewById(R.id.persianaIntProgresso)

        // SWITCH LED
        ledSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Crie um objeto Publish com os dados necessários
                val publish = Publish("64c2c909ce81b/esp32/casa/quarto2/ledquarto2", "1", 0, false)

                // Chame a função onMqttPublish na MainActivity (ou na classe que contém a função)
                val mainActivity = activity as? MainActivity
                mainActivity?.onMqttPublish(publish, null)
                ledMode = true
                ledStatus = "ON"
                ledOnOff.text = ledStatus
            } else {
                val publish = Publish("64c2c909ce81b/esp32/casa/quarto2/ledquarto2", "0", 0, false)
                val mainActivity = activity as? MainActivity
                mainActivity?.onMqttPublish(publish, null)
                ledMode = false
                ledStatus = "OFF"
                ledOnOff.text = ledStatus
            }
        }

        // SEEKBAR PERSIANA INTENSIDADE
        seekBarPersianaInt.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                var temp = "$progress"
                persianaIntProgresso.text = temp
                seekBarPersianaIntProgresso = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Este método é chamado quando o usuário começa a arrastar a SeekBar.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Crie um objeto Publish com os dados necessários
                val publish = Publish(
                    "64c2c909ce81b/esp32/casa/quarto2/persianaquarto2",
                    seekBarPersianaIntProgresso.toString(),
                    0,
                    false
                )

                // Chame a função onMqttPublish na MainActivity (ou na classe que contém a função)
                val mainActivity = activity as? MainActivity
                mainActivity?.onMqttPublish(publish, null)
            }
        })
    }


    fun updateTempQuarto2(message: String?) {
        val tempConc = "$message°C" // Concatenando o valor de temp recebido com °C
        sTemp.text = tempConc // Mostrando o valor concatenado no textview
    }

    fun updateHumQuarto2(message: String?) {
        val humConc = "$message%" // Concatenando o valor de temp recebido com %
        sHum.text = humConc // Mostrando o valor concatenado no textview
    }

    companion object {
        fun newInstance(): Quarto2Fragment {
            return Quarto2Fragment()
        }
    }
}