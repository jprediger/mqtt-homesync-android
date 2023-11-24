package io.emqx.mqtt.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import io.emqx.mqtt.MainActivity
import io.emqx.mqtt.NotificationManager
import io.emqx.mqtt.Publish
import io.emqx.mqtt.R
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import kotlin.properties.Delegates

class SalaFragment : BaseFragment() {

    // As funções onResume e onPause são implementadas em conjunto com sharedPreferences,
    // para armazenar os valores dos slides, botões, etc. sempre que o fragmento é fechado.

    // Recupera as variáveis do sharedPreferences "MyPreferences" quando o fragmento é reaberto
    override fun onResume() {
        super.onResume()

        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        seekBarAlvoProgresso = sharedPreferences.getInt("seekBarAlvoProgress", 0)
        seekBarIntProgresso = sharedPreferences.getInt("seekBarIntProgress", 0)
        climaMode = sharedPreferences.getBoolean("climamode", false)
        climaStatus = sharedPreferences.getString("climastatus", "OFF").toString()
        ledMode = sharedPreferences.getBoolean("ledmode", false)
        ledStatus = sharedPreferences.getString("ledstatus", "OFF").toString()

        // Set the SeekBar progress
        seekBarAlvo.progress = seekBarAlvoProgresso
        seekBarInt.progress = seekBarIntProgresso
        climaSwitch.isChecked = climaMode
        climaOnOff.text = climaStatus

        ledSwitch.isChecked = ledMode
        ledOnOff.text = ledStatus

    }

    // Salva as variáveis na sharedPreferences "MyPreferences" quando o fragmento é fechado
    override fun onPause() {
        super.onPause()

        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("seekBarAlvoProgress", seekBarAlvo.progress)
        editor.putInt("seekBarIntProgress", seekBarInt.progress)
        editor.putBoolean("climamode", climaMode)
        editor.putString("climastatus", climaStatus)
        editor.putBoolean("ledmode", ledMode)
        editor.putString("ledstatus", ledStatus)
        editor.apply()
    }

    // São declaradas as variáveis utilizadas no fragmento:

    private lateinit var sTemp: TextView
    private lateinit var sHum: TextView

    private lateinit var ledSwitch: SwitchCompat
    private lateinit var ledOnOff: TextView

    private lateinit var seekBarTextView: TextView
    private lateinit var seekBarAlvo: SeekBar
    private lateinit var seekBarAlvoTextViewProgress: TextView

    private lateinit var climaSwitch: SwitchCompat
    private lateinit var climaOnOff: TextView
    private lateinit var seekBarInt: SeekBar

    // Variáveis auxiliares utilizadas somente para armazenar os valores em sharedPreferences:

    private var seekBarAlvoProgresso: Int = 0
    private var seekBarIntProgresso: Int = 0

    private var climaMode: Boolean = false
    private var climaStatus: String = "OFF"

    private var ledMode: Boolean = false
    private var ledStatus: String = "OFF"



    override val layoutResId: Int
        get() = R.layout.fragment_sala

    @SuppressLint("SetTextI18n")
    override fun setUpView(view: View) {
        sTemp = view.findViewById(R.id.statustemp)
        sHum = view.findViewById(R.id.statushum)

        ledSwitch = view.findViewById(R.id.ledSwitch)
        ledOnOff = view.findViewById(R.id.ledOnOff)

        seekBarAlvo = view.findViewById(R.id.seekBarAlvo)
        seekBarTextView = view.findViewById(R.id.tempAlvo)
        seekBarAlvoTextViewProgress = view.findViewById(R.id.alvoProgresso)
        climaSwitch = view.findViewById(R.id.climaSwitch)
        climaOnOff = view.findViewById(R.id.climaOnOff)
        seekBarInt = view.findViewById(R.id.seekBarTempInt)


        // SWITCH LED
        ledSwitch.setOnCheckedChangeListener { _, isChecked ->
            val valor: String
            if (isChecked) {
                valor = "1"
                ledMode = true
                ledStatus = "ON"
                ledOnOff.text = ledStatus
            }
            else {
                valor = "0"
                ledMode = false
                ledStatus = "OFF"
                ledOnOff.text = ledStatus
            }
            val publish = Publish("64c2c909ce81b/esp32/casa/sala/ledsala", valor, 0, false)
            // Chame a função onMqttPublish na MainActivity (ou na classe que contém a função)
            val mainActivity = activity as? MainActivity
            mainActivity?.onMqttPublish(publish, null)
        }


        // SWITCH CLIMA
        climaSwitch.setOnCheckedChangeListener { _, isChecked ->
            val valor: String
            if (isChecked) {
                valor = "1"
                climaMode = true
                climaStatus = "ON"
                climaOnOff.text = climaStatus
            } else {
                valor = "0"
                climaMode = false
                climaStatus = "OFF"
                climaOnOff.text = climaStatus
            }
            val publish = Publish("64c2c909ce81b/esp32/casa/sala/fanstatussala", valor, 0, false)
            val mainActivity = activity as? MainActivity
            mainActivity?.onMqttPublish(publish, null)
        }

        // SEEKBAR CLIMA INTENSIDADE
        seekBarInt.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Método chamado sempre que o progresso na SeekBar é alterado
                seekBarIntProgresso = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Este método é chamado quando o usuário começa a arrastar a SeekBar.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Método chamado sempre que o usuário solta o slider
                // Cria um objeto Publish com a mensagem
                val publish = Publish(
                    "64c2c909ce81b/esp32/casa/sala/fanintsala", seekBarIntProgresso.toString(), 0, false)
                // Chame a função onMqttPublish na MainActivity
                val mainActivity = activity as? MainActivity
                mainActivity?.onMqttPublish(publish, null)
            }
        })

        // SEEKBAR CLIMA TEMPERATURA ALVO
        seekBarAlvo.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                var temp = "$progress°C"
                seekBarAlvoTextViewProgress.text = temp
                seekBarAlvoProgresso = progress
                }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Este método é chamado quando o usuário começa a arrastar a SeekBar.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Crie um objeto Publish com os dados necessários
                val publish = Publish("64c2c909ce81b/esp32/casa/sala/fanalvosala", seekBarAlvoProgresso.toString(), 0, false)
                // Chame a função onMqttPublish na MainActivity
                val mainActivity = activity as? MainActivity
                mainActivity?.onMqttPublish(publish, null)
            }
        })
    }

        // Função que é chamada no callback da MainActivity sempre que é recebida a temperatura
        fun updateTempSala(message: String?) {
            val tempConc = "$message°C" // Concatenando o valor de temp recebido com °C
            sTemp.text = tempConc // Mostrando o valor concatenado no textview
        }

        // Função que é chamada no callback da MainActivity sempre que é recebida a umidade
        fun updateHumSala(message: String?) {
            val humConc = "$message%" // Concatenando o valor de temp recebido com %
            sHum.text = humConc // Mostrando o valor concatenado no textview
        }

        companion object {
            fun newInstance(): SalaFragment {
                return SalaFragment()
            }
        }
    }
