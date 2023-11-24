package io.emqx.mqtt.fragments

import io.emqx.mqtt.Publish
import org.eclipse.paho.client.mqttv3.IMqttActionListener

interface MqttCallback {
    fun onMqttPublish(publish: Publish, callback: IMqttActionListener?)
}