package io.emqx.mqtt

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import info.mqtt.android.service.MqttAndroidClient;
import io.emqx.mqtt.fragments.BanheiroFragment
import io.emqx.mqtt.fragments.ConnectionFragment
import io.emqx.mqtt.fragments.CozinhaFragment
import io.emqx.mqtt.fragments.HomeFragment
import io.emqx.mqtt.fragments.Quarto1Fragment
import io.emqx.mqtt.fragments.Quarto2Fragment
import io.emqx.mqtt.fragments.SalaFragment
import org.eclipse.paho.client.mqttv3.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), MqttCallback {

    // Declara mClient como comunicador MQTT
    private var mClient: MqttAndroidClient? = null

    // Declara a lista de fragmentos
    private val mFragmentList: MutableList<Fragment> = ArrayList()

    // Configuração da AppBar
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout

    // Variaveis de notificação
    private var fogoJaDetectado = false
    private var gasJaDetectado = false

    var notificacoesGasCozinha = true
    var notificacoesChamasCozinha = true

    // Tópicos MQTT:

    // Sala:

    private val tempsala: String = "64c2c909ce81b/esp32/casa/sala/tempsala"
    private val humsala: String = "64c2c909ce81b/esp32/casa/sala/humsala"

    // Cozinha:

    private val tempcozinha: String = "64c2c909ce81b/esp32/casa/cozinha/tempcozinha"
    private val humcozinha: String = "64c2c909ce81b/esp32/casa/cozinha/humcozinha"
    private val gascozinha: String = "64c2c909ce81b/esp32/casa/cozinha/gas"
    private val chamascozinha: String = "64c2c909ce81b/esp32/casa/cozinha/chamas"

    // Quarto 1:

    private val tempquarto1: String = "64c2c909ce81b/esp32/casa/quarto1/tempquarto1"
    private val humquarto1: String = "64c2c909ce81b/esp32/casa/quarto1/humquarto1"

    // Quarto 2:

    private val tempquarto2: String = "64c2c909ce81b/esp32/casa/quarto2/tempquarto2"
    private val humquarto2: String = "64c2c909ce81b/esp32/casa/quarto2/humquarto2"

    // Banheiro:

    private val tempbanheiro: String = "64c2c909ce81b/esp32/casa/banheiro/tempbanheiro"
    private val humbanheiro: String = "64c2c909ce81b/esp32/casa/banheiro/humbanheiro"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val notificationManager = NotificationManager(this)
        notificationManager.createNotificationChannel()

        // Declaração da Toolbar
        setSupportActionBar(findViewById(R.id.my_toolbar))

        // Código para configurar o botão de retorno na toolbar
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)

        // Código para setar o título correto de cada fragmento na toolbar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val title = when (destination.id) {
                R.id.homeFragment -> "Menu"
                R.id.salaFragment -> "Sala de Estar"
                R.id.cozinhaFragment -> "Cozinha"
                R.id.quarto1Fragment -> "Quarto 1"
                R.id.quarto2Fragment -> "Quarto 2"
                R.id.banheiroFragment -> "Banheiro"
                R.id.bioFragment -> "Biometria"
                R.id.connectionFragment -> "Login"
                R.id.notifFragment -> "Notificações"

                else -> "IFSUL HomeSync Connect"
            }
            supportActionBar?.title = title
        }

        drawerLayout = findViewById(R.id.drawer_layout)

        // Configuração do ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, findViewById(R.id.my_toolbar),
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Configuração do NavigationView
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_home -> navController.navigate(R.id.homeFragment)
                R.id.nav_notifications -> navController.navigate(R.id.notifFragment)
                R.id.nav_biometrico -> navController.navigate(R.id.bioFragment)

                // Adicione mais casos para outros itens do menu se necessário
            }
            true
        }
    }

    // Função para possibilitar o usuário retornar ao navegar no drawer
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragmentContainerView)
        return NavigationUI.navigateUp(navController, drawerLayout)
                || super.onSupportNavigateUp()
    }


    // Função para conectar o usuário
    fun connect(connection: Connection, listener: IMqttActionListener?) {
        mClient = connection.getMqttAndroidClient(this)
        try {
            mClient!!.connect(connection.mqttConnectOptions, null, listener)
            mClient!!.setCallback(this)
        } catch (e: MqttException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show()
        }
    }

    // Função para desconectar o usuário
    fun disconnect() {
        if (notConnected(true)) {
            return
        }
        try {
            mClient!!.disconnect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    // Função que inscreve o usuário nos tópicos em questão
    fun subscribeToMultipleTopics(topics: List<String>, qos: Int, listener: IMqttActionListener?) {
        if (notConnected(true)) {
            return
        }

        try {
            for (topic in topics) {
                mClient?.subscribe(topic, qos, null, listener)
            }
        } catch (e: MqttException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to subscribe", Toast.LENGTH_SHORT).show()
        }
    }

    // Função que publica as mensagens no tópico MQTT
    fun onMqttPublish(publish: Publish, callback: IMqttActionListener?) {
        if (notConnected(true)) {
            return
        }
        try {
            mClient!!.publish(
                publish.topic,
                publish.payload.toByteArray(),
                publish.qos,
                publish.isRetained,
                null,
                callback
            )
        } catch (e: MqttException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to publish", Toast.LENGTH_SHORT).show()
        }
    }

    // Função para alertar o usuário que ele não está conectado
    fun notConnected(showNotify: Boolean): Boolean {
        if (mClient == null || !mClient!!.isConnected) {
            if (showNotify) {
                Toast.makeText(this, "Client is not connected", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return false
    }

    // Função em caso de conexão perdida
    override fun connectionLost(cause: Throwable?) {
        (mFragmentList[0] as ConnectionFragment).updateButtonText()
    }

    // Função com a lógica para o recebimento de mensagens MQTT em determinados tópicos
    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        runOnUiThread {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

            val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment

            // SALA DE ESTAR
            if (topic == tempsala) {
                message.payload?.toString(Charsets.UTF_8)?.toFloatOrNull()?.let {
                    // Arredonde o valor para o inteiro mais próximo e atualize o fragmento com a string resultante
                    val roundedValue = it.toDouble().roundToInt()
                    currentFragment?.let {
                        if (currentFragment is SalaFragment) {
                            currentFragment.updateTempSala(roundedValue.toString())
                        }
                        if (currentFragment is HomeFragment) {
                            currentFragment.updateTempSala(roundedValue.toString())
                        }
                    }
                }
            }
            if (topic == humsala) {
                val payload = message.payload?.toString(Charsets.UTF_8)?.toFloatOrNull()
                payload?.let {
                    val roundedValue = it.toDouble().roundToInt()
                    currentFragment?.let {
                        if (currentFragment is SalaFragment) {
                            // Arredonde o valor para o inteiro mais próximo e atualize o fragmento com a string resultante
                            currentFragment.updateHumSala(roundedValue.toString())
                        }
                    }
                }
            }

            // COZINHA
            if (topic == tempcozinha) {
                val payload = message.payload?.toString(Charsets.UTF_8)?.toFloatOrNull()
                if (payload != null) {
                    payload.let {
                        // Arredonde o valor para o inteiro mais próximo e atualize o fragmento com a string resultante
                        val roundedValue = it.toDouble().roundToInt()
                        currentFragment?.let {
                            if (currentFragment is CozinhaFragment) {
                                currentFragment.updateTempCozinha(roundedValue.toString())
                            } else if (currentFragment is HomeFragment) {
                                currentFragment.updateTempCozinha(roundedValue.toString())
                            }
                        }
                    }
                }
            }
            if (topic == humcozinha) {
                val payload = message.payload?.toString(Charsets.UTF_8)?.toFloatOrNull()
                payload?.let {
                    val roundedValue = it.toDouble().roundToInt()
                    currentFragment?.let {
                        if (currentFragment is CozinhaFragment) {
                            // Arredonde o valor para o inteiro mais próximo e atualize o fragmento com a string resultante
                            currentFragment.updateHumCozinha(roundedValue.toString())
                        }
                    }
                }
            }
            if (topic == gascozinha) {
                val sharedPreferences = getSharedPreferences("MyNotificationPrefs", Context.MODE_PRIVATE)
                notificacoesGasCozinha = sharedPreferences.getBoolean("gasStatusPref", true)
                val payload = message.payload?.toString(Charsets.UTF_8)?.toIntOrNull()
                if (payload != null) {
                    if (payload == 1) {
                        if (!gasJaDetectado && notificacoesGasCozinha) {
                            // Enviar notificação aqui
                            val notificationManager = NotificationManager(this)
                            notificationManager.notificacaoGasCozinha()
                            gasJaDetectado = true
                        }

                        //Atualizar texto no fragmento cozinha
                    } else if (payload == 0) {
                        gasJaDetectado = false
                    }
                }
            }
            if (topic == chamascozinha) {
                val sharedPreferences = getSharedPreferences("MyNotificationPrefs", Context.MODE_PRIVATE)
                notificacoesChamasCozinha = sharedPreferences.getBoolean("chamasStatusPref", true)
                val payload = message.payload?.toString(Charsets.UTF_8)?.toIntOrNull()
                if (payload != null) {
                    if (payload == 1) {
                        if (!fogoJaDetectado && notificacoesChamasCozinha) {
                            // Enviar notificação aqui
                            val notificationManager = NotificationManager(this)
                            notificationManager.notificacaoChamasCozinha()
                            fogoJaDetectado = true
                        }

                        //Atualizar texto no fragmento cozinha
                    } else if (payload == 0) {
                        fogoJaDetectado = false
                    }
                }
            }

            // QUARTO 1
            if (topic == tempquarto1) {
                val payload = message.payload?.toString(Charsets.UTF_8)?.toFloatOrNull()
                if (payload != null) {
                    payload.let {
                        // Arredonde o valor para o inteiro mais próximo e atualize o fragmento com a string resultante
                        val roundedValue = it.toDouble().roundToInt()
                        currentFragment?.let {
                            if (currentFragment is Quarto1Fragment) {
                                currentFragment.updateTempQuarto1(roundedValue.toString())
                            }
                            if (currentFragment is HomeFragment) {
                                currentFragment.updateTempQuarto1(roundedValue.toString())
                            }
                        }
                    }
                }
            }
            if (topic == humquarto1) {
                val payload = message.payload?.toString(Charsets.UTF_8)?.toFloatOrNull()
                payload?.let {
                    val roundedValue = it.toDouble().roundToInt()
                    currentFragment?.let {
                        if (currentFragment is Quarto1Fragment) {
                            // Arredonde o valor para o inteiro mais próximo e atualize o fragmento com a string resultante
                            currentFragment.updateHumQuarto1(roundedValue.toString())
                        }
                    }
                }
            }

            // QUARTO 2
            if (topic == tempquarto2) {
                val payload = message.payload?.toString(Charsets.UTF_8)?.toFloatOrNull()
                if (payload != null) {
                    payload.let {
                        // Arredonde o valor para o inteiro mais próximo e atualize o fragmento com a string resultante
                        val roundedValue = it.toDouble().roundToInt()
                        currentFragment?.let {
                            if (currentFragment is Quarto2Fragment) {
                                currentFragment.updateTempQuarto2(roundedValue.toString())
                            }
                            if (currentFragment is HomeFragment) {
                                currentFragment.updateTempQuarto2(roundedValue.toString())
                            }
                        }
                    }
                }
            }
            if (topic == humquarto2) {
                val payload = message.payload?.toString(Charsets.UTF_8)?.toFloatOrNull()
                payload?.let {
                    val roundedValue = it.toDouble().roundToInt()
                    currentFragment?.let {
                        if (currentFragment is Quarto2Fragment) {
                            // Arredonde o valor para o inteiro mais próximo e atualize o fragmento com a string resultante
                            currentFragment.updateHumQuarto2(roundedValue.toString())
                        }
                    }
                }
            }

            // BANHEIRO
            if (topic == tempbanheiro) {
                val payload = message.payload?.toString(Charsets.UTF_8)?.toFloatOrNull()
                if (payload != null) {
                    payload.let {
                        // Arredonde o valor para o inteiro mais próximo e atualize o fragmento com a string resultante
                        val roundedValue = it.toDouble().roundToInt()
                        currentFragment?.let {
                            if (currentFragment is BanheiroFragment) {
                                currentFragment.updateTempBanheiro(roundedValue.toString())
                            }
                            if (currentFragment is HomeFragment) {
                                currentFragment.updateTempBanheiro(roundedValue.toString())
                            }
                        }
                    }
                }
            }
            if (topic == humbanheiro) {
                val payload = message.payload?.toString(Charsets.UTF_8)?.toFloatOrNull()
                payload?.let {
                    val roundedValue = it.toDouble().roundToInt()
                    currentFragment?.let {
                        if (currentFragment is BanheiroFragment) {
                            // Arredonde o valor para o inteiro mais próximo e atualize o fragmento com a string resultante
                            currentFragment.updateHumBanheiro(roundedValue.toString())
                        }
                    }
                }
            }
        }
    }

    // Função que altera o valor da variavel responsavel por possibilitar ou não notificações
    fun notificacoesGasCozinha(novoValor: Boolean) {
        notificacoesGasCozinha = novoValor
    }
    // Função que altera o valor da variavel responsavel por possibilitar ou não notificações
    fun notificacoesChamasCozinha(novoValor: Boolean) {
        notificacoesChamasCozinha = novoValor
    }


    override fun deliveryComplete(token: IMqttDeliveryToken) {
    }
}