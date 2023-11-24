package io.emqx.mqtt.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.navigation.Navigation
import io.emqx.mqtt.Connection
import io.emqx.mqtt.MainActivity
import io.emqx.mqtt.R
import io.emqx.mqtt.Subscription
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient

class ConnectionFragment : BaseFragment() {
    private lateinit var mHost: EditText
    private lateinit var mPort: EditText
    private lateinit var mClientId: EditText
    private lateinit var mUsername: EditText
    private lateinit var mPassword: EditText
    private lateinit var mTlsButton: RadioButton
    private lateinit var mButton: Button
    private lateinit var mAvancar: Button
    override val layoutResId: Int
        get() = R.layout.fragment_connection

    override fun setUpView(view: View) {
        mHost = view.findViewById(R.id.host)
        mHost.setText("public.mqtthq.com")
        mPort = view.findViewById(R.id.port)
        mClientId = view.findViewById(R.id.clientid)
        mClientId.setText(MqttAsyncClient.generateClientId())
        mUsername = view.findViewById(R.id.username)
        mUsername.setText("")
        mPassword = view.findViewById(R.id.password)
        mPassword.setText("")
        mTlsButton = view.findViewById(R.id.tls_true)
        mButton = view.findViewById(R.id.btn_connect)
        mAvancar = view.findViewById(R.id.btn_avancar)

        mButton.setOnClickListener {
            if (mButton.text.toString() == getString(R.string.connect)) {
                val connection = Connection(
                    fragmentActivity!!,
                    mHost.text.toString(),
                    mPort.text.toString().toInt(),
                    mClientId.text.toString(),
                    mUsername.text.toString(),
                    mPassword.text.toString(),
                    mTlsButton.isChecked
                )
                (fragmentActivity as MainActivity).connect(
                    connection,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            Toast.makeText(
                                fragmentActivity,
                                "CONECTADO",
                                Toast.LENGTH_LONG
                            ).show()
                            updateButtonText()
                            mAvancar.visibility = View.VISIBLE
                        }

                        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                            Toast.makeText(
                                fragmentActivity,
                                "ERRO NA CONEXÃO",
                                Toast.LENGTH_LONG
                            ).show()
                            exception.printStackTrace()
                        }
                    })
            } else {
                (fragmentActivity as MainActivity).disconnect()
                mAvancar.visibility = View.GONE
            }
        }

        mAvancar.setOnClickListener{
            val topicsToSubscribe = listOf(
                "64c2c909ce81b/esp32/casa/sala/tempsala",
                "64c2c909ce81b/esp32/casa/sala/humsala",
                "64c2c909ce81b/esp32/casa/cozinha/tempcozinha",
                "64c2c909ce81b/esp32/casa/cozinha/humcozinha",
                "64c2c909ce81b/esp32/casa/quarto1/tempquarto1",
                "64c2c909ce81b/esp32/casa/quarto1/humquarto1",
                "64c2c909ce81b/esp32/casa/quarto2/tempquarto2",
                "64c2c909ce81b/esp32/casa/quarto2/humquarto2",
                "64c2c909ce81b/esp32/casa/banheiro/tempbanheiro",
                "64c2c909ce81b/esp32/casa/banheiro/humbanheiro",
                "64c2c909ce81b/esp32/casa/cozinha/gas",
                "64c2c909ce81b/esp32/casa/cozinha/chamas"
            )
            val qos = 0
            (fragmentActivity as MainActivity).subscribeToMultipleTopics(
                topicsToSubscribe, qos,null
            )
            Navigation.findNavController(view).navigate(R.id.action_connectionFragment_to_homeFragment)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Salve os dados do fragmento que você deseja preservar no Bundle
        outState.putString("mqttState", mButton.text.toString())
        outState.putString("host", mHost.text.toString())
        outState.putString("port", mPort.text.toString())
        outState.putString("clientId", mClientId.text.toString())
        outState.putString("username", mUsername.text.toString())
        outState.putString("password", mPassword.text.toString())
        outState.putBoolean("tlsChecked", mTlsButton.isChecked)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restaure os dados específicos deste fragmento a partir do savedInstanceState
        if (savedInstanceState != null) {
            mButton.text = savedInstanceState.getString("mqttState", "")
            mHost.setText(savedInstanceState.getString("host", ""))
            mPort.setText(savedInstanceState.getString("port", ""))
            mClientId.setText(savedInstanceState.getString("clientId", ""))
            mUsername.setText(savedInstanceState.getString("username", ""))
            mPassword.setText(savedInstanceState.getString("password", ""))
            mTlsButton.isChecked = savedInstanceState.getBoolean("tlsChecked", false)
        }
    }


    fun updateButtonText() {
        if ((fragmentActivity as MainActivity).notConnected(false)) {
            mButton.text = getText(R.string.connect)
        } else {
            mButton.text = getString(R.string.disconnect)
        }
    }

    companion object {
        fun newInstance(): ConnectionFragment {
            return ConnectionFragment()
        }
    }
}