package io.emqx.mqtt.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import io.emqx.mqtt.MainActivity
import io.emqx.mqtt.Publish
import io.emqx.mqtt.R

class BioFragment : BaseFragment() {

    private lateinit var registroButton: Button

    override val layoutResId: Int
        get() = R.layout.fragment_bio

    override fun setUpView(view: View) {

        registroButton = view.findViewById(R.id.botaoregistro)

        registroButton.setOnClickListener{
            val publish = Publish("64c2c909ce81b/esp32/casa/leitorbio/registroid", "1", 0, false)
            val mainActivity = activity as? MainActivity
            mainActivity?.onMqttPublish(publish, null)
        }
    }

    companion object {
        fun newInstance(): BioFragment {
            return BioFragment()
        }
    }
}