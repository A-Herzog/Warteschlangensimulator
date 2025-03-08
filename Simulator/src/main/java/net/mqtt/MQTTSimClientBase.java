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

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import language.Language;
import ui.MainFrame;

/**
 * Basis-Implementierung für einen MQTT-Klienten,
 * der Anfragen entgegennimmt und über ein Antwort-Topic
 * antworten kann.
 * @author Alexander Herzog
 */
public abstract class MQTTSimClientBase {
	/**
	 * MQTT-Verbindungsobjekt
	 */
	private MqttClient mqtt;

	/**
	 * Konstruktor der Klasse
	 */
	public MQTTSimClientBase() {
	}

	/**
	 * Erzeugt eine Factory-Objekt zur Erzeugung von TLS-gesicherten Sockets
	 * @param tlsVersion	Zu verwendende TLS-Version
	 * @return	Factory-Objekt zur Erzeugung von TLS-gesicherten Sockets
	 * @see #start(MQTTBrokerURL, String[])
	 * @see #start(MQTTBrokerURL, String[], String, String)
	 */
	private static SocketFactory getTLSSocketFactory(final String tlsVersion) {
		final TrustManager[] trustAllCerts=new TrustManager[] {new X509TrustManager() {
			@Override public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}
			@Override public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
			@Override public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
		}};

		final SSLContext sc;
		try {
			sc=SSLContext.getInstance(tlsVersion);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

		try {
			sc.init(null,trustAllCerts,new java.security.SecureRandom());
		} catch (KeyManagementException e) {
			return null;
		}

		return sc.getSocketFactory();
	}

	/**
	 * Startet den MQTT-Client.
	 * @param broker	Netzwerkname des MQTT-Brokers (inkl. Protokoll und evtl. Port)
	 * @param topics	Themen die dieser MQTT-Client abonnieren soll
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #start(MQTTBrokerURL, String[], String, String)
	 * @see #stop()
	 */
	protected String start(final MQTTBrokerURL broker, final String[] topics) {
		return start(broker,topics,null,null);
	}

	/**
	 * Startet den MQTT-Client.
	 * @param broker	Netzwerkname des MQTT-Brokers (inkl. Protokoll und evtl. Port)
	 * @param topics	Themen die dieser MQTT-Client abonnieren soll
	 * @param username	Nutzername zur Authentifizierung gegenüber dem Broker (kann <code>null</code> sein; nur wenn Name und Password nicht leer sind, werden diese übermittelt)
	 * @param password	Passwort zur Authentifizierung gegenüber dem Broker (kann <code>null</code> sein; nur wenn Name und Password nicht leer sind, werden diese übermittelt)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #start(MQTTBrokerURL, String[])
	 * @see #stop()
	 */
	protected String start(final MQTTBrokerURL broker, final String[] topics, final String username, final String password) {
		if (mqtt!=null) return Language.tr("MQTT.Error.AlreadyRunning");

		/* Objekt initialisieren */
		if (broker==null) return Language.tr("MQTT.Error.Init");
		try {
			mqtt=new MqttClient(broker.toString(),MainFrame.PROGRAM_NAME);
		} catch (MqttException e) {
			mqtt=null;
			return Language.tr("MQTT.Error.Init");
		}

		mqtt.setCallback(new MQTTCallbackAdapter() {
			@Override public void messageArrived(final String topic, final MqttMessage message) throws Exception {
				final String responseTopic=message.getProperties().getResponseTopic();
				final byte[] payload=message.getPayload();
				final List<UserProperty> userProperties=message.getProperties().getUserProperties();
				processRequest(topic,responseTopic,payload,userProperties);
			}
		});

		/* Mit Server verbinden */
		try {
			MqttConnectionOptions options=new MqttConnectionOptions();
			options.setAutomaticReconnect(true);

			/* Zugangsdaten */
			if (username!=null && !username.isBlank() && password!=null && !password.isBlank()) {
				options.setUserName(username);
				options.setPassword(password.getBytes(StandardCharsets.UTF_8));
			}

			/* Verschlüsselte Verbindung */
			if (broker.secured!=MQTTBrokerURL.SecurityMode.OFF) {
				options.setSocketFactory(getTLSSocketFactory("TLSv1.3"));
				if (broker.secured!=MQTTBrokerURL.SecurityMode.ON_NO_VALIDATION) {
					options.setHttpsHostnameVerificationEnabled(false);
				}
				options.setSSLHostnameVerifier((hostname, session)->true);
			}

			mqtt.connect(options);
		} catch (MqttException e) {
			mqtt=null;
			return String.format(Language.tr("MQTT.Error.ConnectingToServer"),broker);
		}

		/* Topic abonnieren */
		if (topics!=null) for (String topic: topics) if (topic!=null && !topic.isBlank()) {
			try {
				mqtt.subscribe(topic,2);
			} catch (MqttException e) {
				try {
					mqtt.disconnect();
				} catch (MqttException e1) {}
				mqtt=null;
				return String.format(Language.tr("MQTT.Error.SubscribeTopic"),topic);
			}
		}

		return null;
	}

	/**
	 * Beendet den MQTT-Klienten.
	 * @see #start(MQTTBrokerURL, String[])
	 * @see #start(MQTTBrokerURL, String[], String, String)
	 */
	public void stop() {
		if (mqtt==null) return;

		try {
			mqtt.disconnect();
		} catch (MqttException e) {}

		mqtt=null;
	}

	/**
	 * Gibt an, ob der Server momentan läuft.
	 * @return	Liefert <code>true</code>, wenn der Server momentan aktiv ist.
	 */
	public boolean isRunning() {
		return mqtt!=null;
	}

	/**
	 * Versendet eine Nachricht.
	 * @param topic	Ziel-Thema
	 * @param data	Zu sendende Nachricht
	 * @param userProperties	Zusätzlich zu übermittelnde Eigenschaften
	 * @return	Liefert <code>true</code>, wenn die Nachricht an den MQTT-Broker übermittelt werden konnte
	 */
	protected boolean send(final String topic, final byte[] data, final List<UserProperty> userProperties) {
		return send(topic,data,userProperties,2);
	}

	/**
	 * Versendet eine Nachricht.
	 * @param topic	Ziel-Thema
	 * @param data	Zu sendende Nachricht
	 * @param userProperties	Zusätzlich zu übermittelnde Eigenschaften
	 * @param qos	MQTT Quality of Service (0 bis 2)
	 * @return	Liefert <code>true</code>, wenn die Nachricht an den MQTT-Broker übermittelt werden konnte
	 */
	protected boolean send(final String topic, final byte[] data, final List<UserProperty> userProperties, final int qos) {
		if (mqtt==null) return false;
		if (topic==null || topic.isBlank()) return false;
		if (data==null) return false;

		final MqttMessage message=new MqttMessage(data);
		message.setQos(Math.max(0,Math.min(2,qos)));
		if (userProperties!=null) {
			final MqttProperties properties=new MqttProperties();
			properties.getUserProperties().addAll(userProperties);
			message.setProperties(properties);
		}

		try {
			mqtt.publish(topic,message);
		} catch (MqttException e) {
			return false;
		}

		return true;
	}

	/**
	 * Versendet eine Nachricht.
	 * @param topic	Ziel-Thema
	 * @param data	Zu sendende Nachricht
	 * @return	Liefert <code>true</code>, wenn die Nachricht an den MQTT-Broker übermittelt werden konnte
	 */
	protected boolean send(final String topic, final byte[] data) {
		return send(topic,data,null);
	}

	/**
	 * Verarbeitete eine empfangende Nachricht
	 * @param requestTopic	MQTT-Anfragethema
	 * @param responseTopic	MQTT-Thema das für Antworten verwendet werden soll (kann <code>null</code> sein)
	 * @param payload	Eingetroffene Nachricht
	 * @param userProperties	Zusätzlich übermittelte Eigenschaften
	 */
	protected abstract void processRequest(final String requestTopic, final String responseTopic, final byte[] payload, final List<UserProperty> userProperties);

	/**
	 * Erstellt eine Echo-Nachricht, um die MQTT-Zwei-Wege-Kommunikation zu testen
	 * @param input	Eingetroffene Nachricht
	 * @return	Antwortnachricht
	 */
	protected static byte[] getEcho(final byte[] input) {
		final String msg=new String(input,StandardCharsets.UTF_8);
		final String response="Echo: "+msg;
		return response.getBytes(StandardCharsets.UTF_8);
	}
}
