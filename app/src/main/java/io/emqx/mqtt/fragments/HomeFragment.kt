package io.emqx.mqtt.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import io.emqx.mqtt.R

class HomeFragment : BaseFragment() {

    private lateinit var salaTemp: TextView
    private lateinit var cozinhaTemp: TextView
    private lateinit var quarto1Temp: TextView
    private lateinit var quarto2Temp: TextView
    private lateinit var banheiroTemp: TextView

    override val layoutResId: Int
        get() = R.layout.fragment_home

    override fun setUpView(view: View) {

        salaTemp = view.findViewById(R.id.salaTemp)
        cozinhaTemp = view.findViewById(R.id.cozinhaTemp)
        quarto1Temp = view.findViewById(R.id.quarto1Temp)
        quarto2Temp = view.findViewById(R.id.quarto2Temp)
        banheiroTemp = view.findViewById(R.id.banheiroTemp)

        // Setup dos botões para navegação entre fragmentos

        view.findViewById<CardView>(R.id.salaCardView).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_salaFragment)
        }
        view.findViewById<CardView>(R.id.cozinhaCardView).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_cozinhaFragment)
        }
        view.findViewById<CardView>(R.id.quarto1CardView).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_quarto1Fragment)
        }
        view.findViewById<CardView>(R.id.quarto2CardView).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_quarto2Fragment)
        }
        view.findViewById<CardView>(R.id.banheiroCardView).setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_homeFragment_to_banheiroFragment)
        }
    }
    fun updateTempSala(message: String?) {
        val tempConc = "$message°C" // Concatenando o valor de temp recebido com °C
        salaTemp.text = tempConc // Mostrando o valor concatenado no textview
    }

    fun updateTempCozinha(message: String?) {
        val tempConc = "$message°C" // Concatenando o valor de temp recebido com °C
        cozinhaTemp.text = tempConc // Mostrando o valor concatenado no textview
    }

    fun updateTempQuarto1(message: String?) {
        val tempConc = "$message°C" // Concatenando o valor de temp recebido com °C
        quarto1Temp.text = tempConc // Mostrando o valor concatenado no textview
    }

    fun updateTempQuarto2(message: String?) {
        val tempConc = "$message°C" // Concatenando o valor de temp recebido com °C
        quarto2Temp.text = tempConc // Mostrando o valor concatenado no textview
    }

    fun updateTempBanheiro(message: String?) {
        val tempConc = "$message°C" // Concatenando o valor de temp recebido com °C
        banheiroTemp.text = tempConc // Mostrando o valor concatenado no textview
    }



    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}