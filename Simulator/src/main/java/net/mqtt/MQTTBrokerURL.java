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

import mathtools.NumberTools;

/**
 * Diese Klasse hält Netzwerkname, Port und Verschlüsselungsstatus
 * für eine Verbindung zu einem MQTT-Broker vor.
 * @author Alexander Herzog
 * @see MQTTSimClientBase
 * @see MQTTSimClient
 */
public class MQTTBrokerURL {
	/**
	 * Standardport für unverschlüsselte Verbindungen zum MQTT-Broker
	 */
	public static final int MQTT_DEFAULT_UNSECURED_PORT=1883;

	/**
	 * Standardport für verschlüsselte Verbindungen zum MQTT-Broker
	 */
	public static final int MQTT_DEFAULT_SECURED_PORT=8883;

	/**
	 * Netzwerkname des MQTT-Brokers
	 */
	final String broker;

	/**
	 * Port auf dem Broker (Werte &le;0 bedeuten, dass der Port automatisch bestimmt wird)
	 */
	final int port;

	/**
	 * Soll die Verbindung verschlüsselt erfolgen?
	 */
	final boolean secured;

	/**
	 * Konstruktor der Klasse
	 * @param broker	Netzwerkname des MQTT-Brokers
	 * @param port	Port auf dem Broker (Werte &le;0 bedeuten, dass der Port automatisch bestimmt wird)
	 * @param secured	Soll die Verbindung verschlüsselt erfolgen?
	 */
	public MQTTBrokerURL(final String broker, final int port, final boolean secured) {
		this.broker=broker;
		this.port=port;
		this.secured=secured;
	}

	/**
	 * Konstruktor der Klasse
	 * @param broker	Netzwerkname des MQTT-Brokers
	 * @param secured	Soll die Verbindung verschlüsselt erfolgen?
	 */
	public MQTTBrokerURL(final String broker, final boolean secured) {
		this.broker=broker;
		port=-1;
		this.secured=secured;
	}

	/**
	 * Versucht eine Zeichenkette als Protokoll, Netzwerkname und optional Port zu parsen
	 * @param mqttURL	Zu parsende Zeichenkette
	 * @return	Liefert im Erfolgsfall ein {@link MQTTBrokerURL}-Objekt, sonst <code>null</code>
	 */
	public static MQTTBrokerURL parseString(String mqttURL) {
		if (mqttURL==null) return null;
		mqttURL=mqttURL.trim();
		if (mqttURL.length()<7) return null;

		/* Protokoll */
		final String lower=mqttURL.toLowerCase();
		boolean secured=false;
		if (lower.startsWith("ssl://")) secured=true; else {
			if (!lower.startsWith("tcp://")) return null;
		}
		mqttURL=mqttURL.substring(6);

		/* Port? */
		final int index=mqttURL.lastIndexOf(':');
		if (index<0) {
			/* Kein Port, nur Name */
			return new MQTTBrokerURL(mqttURL,secured);
		} else {
			/* Name und Port */
			if (index==mqttURL.length()-1) return null; /* ":" steht ganz am Ende */
			final String url=mqttURL.substring(0,index).trim();
			final String portString=mqttURL.substring(index+1).trim();
			final Long port=NumberTools.getPositiveLong(portString);
			if (port==null) return null;
			return new MQTTBrokerURL(url,port.intValue(),secured);
		}
	}

	@Override
	public String toString() {
		final StringBuilder brokerURL=new StringBuilder();
		if (secured) brokerURL.append("ssl"); else brokerURL.append("tcp");
		brokerURL.append("://");
		brokerURL.append(broker);
		brokerURL.append(":");
		if (port<=0) {
			if (secured) brokerURL.append(MQTT_DEFAULT_SECURED_PORT); else brokerURL.append(MQTT_DEFAULT_UNSECURED_PORT);
		} else {
			brokerURL.append(port);
		}
		return brokerURL.toString();
	}
}