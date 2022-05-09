/**
 * Copyright 2021 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mqtt;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

/**
 * Standard-Implementierung für {@link MqttCallback} mit
 * leeren Methoden. Klassen, die von dieser Klasse abgeleitet
 * werden, können dann selektiv einzelne Methoden überschreiben,
 * ohne alle Methoden implementieren zu müssen.
 * @author Alexander Herzog
 */
public class MQTTCallbackAdapter implements MqttCallback {
	/**
	 * Konstruktor der Klasse
	 */
	public MQTTCallbackAdapter() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public void disconnected(MqttDisconnectResponse disconnectResponse) {
	}

	@Override
	public void mqttErrorOccurred(MqttException exception) {
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
	}

	@Override
	public void deliveryComplete(IMqttToken token) {
	}

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
	}

	@Override
	public void authPacketArrived(int reasonCode, MqttProperties properties) {
	}
}
