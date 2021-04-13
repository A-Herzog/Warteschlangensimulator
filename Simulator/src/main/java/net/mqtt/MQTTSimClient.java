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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import net.webcalc.CalcFuture;
import simulator.editmodel.EditModel;

/**
 * Diese Klasse stellt die Simulator-Serverdienste über einen
 * MQTT-Klienten zur Verfügung.
 * @author Alexander Herzog
 */
public class MQTTSimClient extends MQTTSimClientBase {
	/**
	 * Anfrage-Thema auf das mit einem Echo an das Antwort-Thema geantwortet wird (kann <code>null</code> sein)<br>
	 * (wird von {@link #start(MQTTBrokerURL, String, String, String)} bzw. {@link #start(MQTTBrokerURL, String, String, String, String, String)} gesetzt)
	 */
	private String echoTopic;

	/**
	 * Anfrage-Thema über das Aufträge an den Simulator übergeben werden können (kann <code>null</code> sein)<br>
	 * (wird von {@link #start(MQTTBrokerURL, String, String, String)} bzw. {@link #start(MQTTBrokerURL, String, String, String, String, String)} gesetzt)
	 */
	private String workTopic;

	/**
	 * Festgelegtes Modell, welches nur noch parametrisiert werden soll.<br>
	 * Ist dieses Feld <code>null</code>, so können beliebige Modelle simuliert werden.
	 */
	private EditModel fixedModel;

	/**
	 * Optionaler Thread zur Ausgabe der Systemauslastung
	 */
	private LoadInfoThread loadInfoThread;

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
	 * @param loadTopic	Thema über das regelmäßig Informationen zur Systemauslastung angegeben werden sollen (kann <code>null</code> sein)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #start(MQTTBrokerURL, String, String, String)
	 * @see #stop()
	 */
	public String start(final MQTTBrokerURL broker, final String echoTopic, final String workTopic, final String loadTopic) {
		return start(broker,echoTopic,workTopic,loadTopic,null,null);
	}

	/**
	 * Startet den MQTT-Client.
	 * @param broker	Netzwerkname des MQTT-Brokers (inkl. Protokoll und evtl. Port)
	 * @param echoTopic	Anfrage-Thema auf das mit einem Echo an das Antwort-Thema geantwortet wird (kann <code>null</code> sein)
	 * @param workTopic	Anfrage-Thema über das Aufträge an den Simulator übergeben werden können (kann <code>null</code> sein)
	 * @param loadTopic	Thema über das regelmäßig Informationen zur Systemauslastung angegeben werden sollen (kann <code>null</code> sein)
	 * @param username	Nutzername zur Authentifizierung gegenüber dem Broker (kann <code>null</code> sein; nur wenn Name und Password nicht leer sind, werden diese übermittelt)
	 * @param password	Passwort zur Authentifizierung gegenüber dem Broker (kann <code>null</code> sein; nur wenn Name und Password nicht leer sind, werden diese übermittelt)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #start(MQTTBrokerURL, String, String, String)
	 * @see #stop()
	 */
	public String start(final MQTTBrokerURL broker, final String echoTopic, final String workTopic, final String loadTopic, final String username, final String password) {
		this.echoTopic=echoTopic;
		this.workTopic=workTopic;

		if (loadTopic!=null && !loadTopic.trim().isEmpty()) {
			loadInfoThread=new LoadInfoThread(loadTopic);
			loadInfoThread.start();
		}

		return start(broker,new String[]{echoTopic,workTopic},username,password);
	}

	/**
	 * Startet den MQTT-Client.
	 * @param broker	Netzwerkname des MQTT-Brokers (inkl. Protokoll und evtl. Port)
	 * @param echoTopic	Anfrage-Thema auf das mit einem Echo an das Antwort-Thema geantwortet wird (kann <code>null</code> sein)
	 * @param workTopic	Anfrage-Thema über das Aufträge an den Simulator übergeben werden können (kann <code>null</code> sein)
	 * @param loadTopic	Thema über das regelmäßig Informationen zur Systemauslastung angegeben werden sollen (kann <code>null</code> sein)
	 * @param username	Nutzername zur Authentifizierung gegenüber dem Broker (kann <code>null</code> sein; nur wenn Name und Password nicht leer sind, werden diese übermittelt)
	 * @param password	Passwort zur Authentifizierung gegenüber dem Broker (kann <code>null</code> sein; nur wenn Name und Password nicht leer sind, werden diese übermittelt)
	 * @param fixedModel	Festgelegtes Modell, welches nur noch parametrisiert werden soll (kann <code>null</code> sein)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #start(MQTTBrokerURL, String, String, String)
	 * @see #stop()
	 */
	public String start(final MQTTBrokerURL broker, final String echoTopic, final String workTopic, final String loadTopic, final EditModel fixedModel, final String username, final String password) {
		this.echoTopic=echoTopic;
		this.workTopic=workTopic;

		if (loadTopic!=null && !loadTopic.trim().isEmpty()) {
			loadInfoThread=new LoadInfoThread(loadTopic);
			loadInfoThread.start();
		}

		this.fixedModel=fixedModel;

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
			final CalcFuture future;
			if (fixedModel==null) {
				future=new CalcFuture(input,f->simulationDone(responseTopic,f,userProperties));
			} else {
				future=new CalcFuture(fixedModel,input,f->simulationDone(responseTopic,f,userProperties));
			}
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

		if (loadInfoThread!=null) {
			loadInfoThread.interrupt();
			loadInfoThread=null;
		}
	}

	/**
	 * Sendet die MQTT-Antwort nach dem Ende der Simulation.
	 * @param responseTopic	MQTT-Antwort-Thema
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
	 * Sendet einen Text über eine bestehende Verbindung zum MQTT-Broker
	 * @param topic	MQTT-Thema an das die Nachricht gesendet werden soll
	 * @param text	Zu sendender Text
	 * @param qos	MQTT Quality of Service (0 bis 2)
	 * @return	Liefert <code>true</code>, wenn die Nachricht an den MQTT-Broker übermittelt werden konnte
	 */
	public boolean sendText(final String topic, final String text, final int qos) {
		return send(topic,text.getBytes(StandardCharsets.UTF_8),null,qos);
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

	/**
	 * Thread zur Ausgabe der Systemauslastung
	 * @see MQTTSimClient#loadInfoThread
	 */
	private class LoadInfoThread extends Thread {
		/**
		 * Zeitintervall (in MS) in dem Auslastungsdaten ausgegeben werden sollen
		 */
		private static final int INFO_INTERVAL=2_000;

		/**
		 * Anzahl der logischen CPU-Kerne in diesem System
		 */
		private final int cpuCount=Runtime.getRuntime().availableProcessors();

		/**
		 * System zu Ermittlung der Thread-Daten
		 */
		private final ThreadMXBean threads=ManagementFactory.getThreadMXBean();

		/**
		 * Zeitpunkt des letzten Aufrufs von {@link #processInfo()}
		 * @see #processInfo()
		 */
		private long lastTimeStamp;

		/**
		 * CPU-Zeit der Threads beim letzten Aufruf
		 */
		private Map<Long,Long> lastLoad;

		/**
		 * Thema über das regelmäßig Informationen zur Systemauslastung angegeben werden sollen
		 */
		private final String loadTopic;

		/**
		 * Konstruktor der Klasse
		 * @param loadTopic	Thema über das regelmäßig Informationen zur Systemauslastung angegeben werden sollen
		 */
		public LoadInfoThread(final String loadTopic) {
			super("MQTT Load Info Thread");
			this.loadTopic=loadTopic;
		}

		/**
		 * Sammelt die Daten zur Auslastung und gibt diese
		 * über das eingestellte MQTT-Topic aus.
		 */
		private void processInfo() {
			final Map<Long,Long> activeLoad=new HashMap<>();
			final long timeStamp=System.currentTimeMillis();
			long sum=0;
			final Map<Long,Long> load=new HashMap<>();
			for (long id: threads.getAllThreadIds()) {
				final long l=threads.getThreadCpuTime(id);
				load.put(id,l);
				final long threadLoad=(lastLoad==null)?l:(l-lastLoad.getOrDefault(id,0L));
				if (threadLoad>0) {
					activeLoad.put(id,threadLoad);
				}
				sum+=threadLoad;
			}
			lastLoad=load;
			final long delta=(timeStamp-lastTimeStamp)*1_000_000;
			lastTimeStamp=timeStamp;
			final double workLoadSystem=((double)sum)/delta/cpuCount;

			final String result=Math.round(Math.min(1,workLoadSystem)*100)+"%";

			send(loadTopic,result.getBytes(),null,0);
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				try {sleep(INFO_INTERVAL);} catch (InterruptedException e) {}
				processInfo();
			}
		}
	}
}
