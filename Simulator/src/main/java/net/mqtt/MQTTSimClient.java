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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import net.webcalc.CalcFuture;

/**
 * Diese Klasse stellt die Simulator-Serverdienste über einen
 * MQTT-Klienten zur Verfügung.
 * @author Alexander Herzog
 */
public class MQTTSimClient extends MQTTSimClientBase {
	/**
	 * Anfrage-Thema auf das mit einem Echo an das Antwort-Thema geantwortet wird (kann <code>null</code> sein)<br>
	 * (wird von {@link #start(MQTTBrokerURL, String, String)} bzw. {@link #start(MQTTBrokerURL, String, String, String, String)} gesetzt)
	 */
	private String echoTopic;

	/**
	 * Anfrage-Thema über das Aufträge an den Simulator übergeben werden können (kann <code>null</code> sein)<br>
	 * (wird von {@link #start(MQTTBrokerURL, String, String)} bzw. {@link #start(MQTTBrokerURL, String, String, String, String)} gesetzt)
	 */
	private String workTopic;

	/**
	 * System zur Ausführung von Aufgaben
	 */
	private final ExecutorService executor;

	/**
	 * Sichert den Zugriff auf die Liste der aktiven Aufgaben ab
	 */
	private final ReentrantLock lock;

	/**
	 * Liste der aktiven Aufgaben
	 */
	private final List<CalcFuture> list;

	/**
	 * Konstruktor der Klasse
	 */
	public MQTTSimClient() {
		lock=new ReentrantLock();
		list=new ArrayList<>();

		executor=new ThreadPoolExecutor(0,1,5000,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>(),new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r,"MQTTSim Request Processor");
			}
		});
	}

	/**
	 * Startet den MQTT-Client.
	 * @param broker	Netzwerkname des MQTT-Brokers (inkl. Protokoll und evtl. Port)
	 * @param echoTopic	Anfrage-Thema auf das mit einem Echo an das Antwort-Thema geantwortet wird (kann <code>null</code> sein)
	 * @param workTopic	Anfrage-Thema über das Aufträge an den Simulator übergeben werden können (kann <code>null</code> sein)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #start(MQTTBrokerURL, String, String)
	 * @see #stop()
	 */
	public String start(final MQTTBrokerURL broker, final String echoTopic, final String workTopic) {
		return start(broker,echoTopic,workTopic,null,null);
	}

	/**
	 * Startet den MQTT-Client.
	 * @param broker	Netzwerkname des MQTT-Brokers (inkl. Protokoll und evtl. Port)
	 * @param echoTopic	Anfrage-Thema auf das mit einem Echo an das Antwort-Thema geantwortet wird (kann <code>null</code> sein)
	 * @param workTopic	Anfrage-Thema über das Aufträge an den Simulator übergeben werden können (kann <code>null</code> sein)
	 * @param username	Nutzername zur Authentifizierung gegenüber dem Broker (kann <code>null</code> sein; nur wenn Name und Password nicht leer sind, werden diese übermittelt)
	 * @param password	Passwort zur Authentifizierung gegenüber dem Broker (kann <code>null</code> sein; nur wenn Name und Password nicht leer sind, werden diese übermittelt)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #start(MQTTBrokerURL, String, String)
	 * @see #stop()
	 */
	public String start(final MQTTBrokerURL broker, final String echoTopic, final String workTopic, final String username, final String password) {
		this.echoTopic=echoTopic;
		this.workTopic=workTopic;

		return start(broker,new String[]{echoTopic,workTopic},username,password);
	}

	@Override
	protected void processRequest(final String requestTopic, final String responseTopic, final byte[] payload, final List<UserProperty> userProperties) {
		if (requestTopic==null || responseTopic.trim().isEmpty()) return;

		if (requestTopic.equals(echoTopic)) {
			send(responseTopic,getEcho(payload),userProperties);
			return;
		}

		if (requestTopic.equals(workTopic)) {
			processJob(responseTopic,payload,userProperties);
			return;
		}
	}

	/**
	 * Verarbeitet empfangene Modelldaten.
	 * @param responseTopic	MQTT-Antwort-Thema
	 * @param input	Modelldaten
	 * @param userProperties	Zusätzlich zu übermittelnde Eigenschaften
	 */
	private void processJob(final String responseTopic, final byte[] input, final List<UserProperty> userProperties) {
		lock.lock();
		try {
			final CalcFuture future=new CalcFuture(input,f->simulationDone(responseTopic,f,userProperties));
			list.add(future);
			executor.submit(()->future.run());
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void stop() {
		lock.lock();
		try {
			for (CalcFuture future: list) future.cancel();
			list.clear();
			super.stop();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Sendet die MQTT-Antwort nach dem Ende der Simulation.
	 * @param responseTopic	MQTT-Antwort-Thema	 *
	 * @param future	Simulationsobjekt (kann auch im Fehler-Ende-Status sein)
	 * @param userProperties	Zusätzlich zu übermittelnde Eigenschaften
	 */
	private void simulationDone(final String responseTopic, final CalcFuture future, final List<UserProperty> userProperties) {
		lock.lock();
		try {
			final byte[] result=future.getBytes();
			if (result!=null) send(responseTopic,result,userProperties);
			list.remove(future);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Singleton-Instanz dieser Klasse
	 * @see #getInstance()
	 */
	private static MQTTSimClient instance;

	/**
	 * Liefert eine Singleton-Instanz dieser Klasse
	 * @return	Instanz dieser Klasse
	 */
	public static synchronized MQTTSimClient getInstance() {
		if (instance==null) instance=new MQTTSimClient();
		return instance;
	}
}
