/**
 * Copyright 2020 Alexander Herzog
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
package net.calc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import simulator.AnySimulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;

/**
 * Kapselt eine Netzwerkverbindung zu einem Simulationsserver
 * @author Alexander Herzog
 * @see StartAnySimulator
 * @see SimulationServer
 */
public class SimulationClient implements AnySimulator {
	/** Bezeichner für Fehlermeldung "Es kann keine Verbindung mit %s auf Port %d hergestellt werden." */
	public static String NO_CONNECT="Es kann keine Verbindung mit %s auf Port %d hergestellt werden.";
	/** Bezeichner für Fehlermeldung "Das Modell konnte nicht an %s an Port %d gesendet werden." */
	public static String ERROR_SENDING_MODEL="Das Modell konnte nicht an %s an Port %d gesendet werden.";
	/** Bezeichner für Fehlermeldung "Es ist ein interner Fehler bei der Vorbereitung des Modells auf dem Server aufgetreten." */
	public static String ERROR_ON_REMOTE_PREPARE="Es ist ein interner Fehler bei der Vorbereitung des Modells auf dem Server aufgetreten.";

	/** Zu simulierendes Modell */
	private final EditModel model;
	/** Objekt über das die Verbindung zum Server aufgebaut wird */
	private final NetClient net;
	/** {@link NetTransfer}-Objekt über das die Kommunikation mit dem Server erfolgt */
	private NetTransfer transfer;
	/** Steht während der Simulation auf <code>true</code>. Nach der Simulation (im Erfolgs- und im Fehlerfall) wieder auf <code>false</code>. */
	private boolean started;

	/** Nimmt eine Fehlermeldung auf, wenn bei der Vorbereitung des Modells ein Fehler aufgetreten ist. */
	private String prepareError;
	/** Summe der in allen Threads bisher simulierten Ereignisse */
	private long eventCount;
	/** Wie viele Ereignisse werden pro Sekunde verarbeitet? */
	private int eventsPerSecond;
	/** Nummer des gerade im Simulator in Bearbeitung befindlichen Tages */
	private long simDayCount;
	/** Gesamtanzahl an Wiederholungen in der Simulation */
	private long simDaysCount;
	/** Anzahl an bislang simulierten Kundenankünften */
	private long currentClients;
	/** Aktuelle Anzahl an Kunden im System */
	private int currentWIP;
	/** Gesamtanzahl an zu simulierenden Kundenankünften */
	private long countClients;
	/**
	 * Statistik-Objekt, welches alle Daten des Simulationslaufs enthält (oder <code>null</code>, wenn die Simulation - ggf. auch durch den Server - abgebrochen wurde)
	 * @see #getStatistic()
	 */
	private Statistics statistics;

	/**
	 * Konstruktor der Klasse
	 * @param model	Zu simulierendes Modell
	 * @param host	Adresse des Servers
	 * @param port	Portnummer auf dem der Server auf Anfragen wartet
	 * @param key	Optionales Passwort zum Verschlüsseln der Daten. Wird hier <code>null</code> übergeben, so erfolgt die Übertragung unverschlüsselt.
	 */
	public SimulationClient(final EditModel model, final String host, final int port, final String key) {
		net=new NetClient(host,port,true,key);
		this.model=model;
		started=false;
	}

	/**
	 * Bereitet die Simulation vor
	 * @return	Liefert <code>null</code> zurück, wenn die Simulation erfolgreich vorbereitet werden konnte, sonst eine Fehlermeldung
	 */
	public String prepare() {
		final StartAnySimulator.PrepareError modelError=StartAnySimulator.testModel(model);
		if (modelError!=null) return modelError.error;

		transfer=net.start();
		if (transfer==null) {
			net.stop();
			return String.format(NO_CONNECT,net.getHost(),net.getPort());
		}

		return null;
	}

	/**
	 * Startet die Simulation auf dem Server
	 * @return	Liefert <code>null</code>, wenn die Simulation gestartet werden konnte, sonst eine Fehlermeldung.
	 */
	public String start() {
		if (!transfer.sendByte(0)) {
			net.stop();
			transfer=null;
			return String.format(ERROR_SENDING_MODEL,net.getHost(),net.getPort());
		}

		try (ByteArrayOutputStream data=new ByteArrayOutputStream()) {
			model.saveToStream(data);
			if (!transfer.sendStream(data)) {
				net.stop();
				transfer=null;
				return String.format(ERROR_SENDING_MODEL,net.getHost(),net.getPort());
			}
		} catch (IOException e) {
			net.stop();
			transfer=null;
			return String.format(ERROR_SENDING_MODEL,net.getHost(),net.getPort());
		}

		started=true;
		int count=0;
		while (eventCount==0 && prepareError==null && count<10) {
			updateStatus(true);
			count++;
		}
		if (eventCount==0) {
			if (prepareError!=null) return prepareError;
			return ERROR_ON_REMOTE_PREPARE;
		}
		return null;
	}

	/**
	 * Bricht die Simulation vorzeitig ab.
	 */
	@Override
	public void cancel() {
		if (transfer!=null) transfer.sendString("cancel");
		net.stop();
		transfer=null;
		started=false;
	}

	/**
	 * Prüft, ob die Simulationsthreads auf dem Server noch laufen.
	 * @return Liefert <code>true</code> wenn mindestens ein Thread noch aktiv ist.
	 */
	@Override
	public boolean isRunning() {
		updateStatus(false);
		return started;
	}

	/**
	 * Aktualisiert die über die verschiedenen Methoden
	 * abrufbaren Datenfelder zum Simulationsfortschritt.
	 * @param wait	Soll auf die Verfügbarkeit von Statusdaten vom Server gewartet werden?
	 */
	private void updateStatus(final boolean wait) {
		if (transfer==null) return;
		byte[] id;

		if (wait) {
			id=transfer.waitForBytes(2_000);
		} else {
			id=transfer.getBytes();
		}
		if (transfer.receivedInvalidData()) {
			prepareError=ERROR_ON_REMOTE_PREPARE;
			cancel();
			return;
		}

		while (id!=null) {
			if (id.length!=1) return;

			ByteArrayInputStream data;

			switch (id[0]) {
			case 1: /* Prepare Error */
				prepareError=transfer.waitForString(5_000);
				if (prepareError==null) prepareError=ERROR_ON_REMOTE_PREPARE;
				cancel();
				return;
			case 2: /* Status */
				data=transfer.waitForStream(10_000);
				if (data!=null) {
					try (final DataInputStream reader=new DataInputStream(data)) {
						currentClients=reader.readLong();
						countClients=reader.readLong();
						currentWIP=reader.readInt();
						simDayCount=reader.readLong();
						simDaysCount=reader.readLong();
						eventCount=reader.readLong();
						eventsPerSecond=reader.readInt();
					} catch (IOException e) {}
				}
				break;
			case 3: /* Finish */
				data=transfer.waitForStream(30_000);
				if (data!=null) {
					statistics=new Statistics(model.collectWaitingTimes,model.useWelford);
					if (statistics.loadFromStream(data)!=null) statistics=null;
				}
				started=false;
				net.stop();
				transfer=null;
				return;
			}

			/* Weitere Daten? */
			id=transfer.getBytes();
		}
	}

	/**
	 * Gibt die Summe der in allen Threads bisher simulierten Ereignisse zurück.
	 * @return Anzahl der simulierten Ereignisse in allen Threads
	 */
	@Override
	public long getEventCount() {
		updateStatus(false);
		return eventCount;
	}

	/**
	 * Gibt zurück, wie viele Ereignisse pro Sekunde verarbeitet werden.
	 * @return	Ereignisse pro Sekunde
	 */
	@Override
	public int getEventsPerSecond() {
		updateStatus(false);
		return eventsPerSecond;
	}

	/**
	 * Liefert die Gesamtanzahl an zu simulierenden Kundenankünften
	 * @return	Gesamtanzahl an zu simulierenden Kundenankünften
	 */
	@Override
	public long getCountClients() {
		updateStatus(false);
		return countClients;
	}

	/**
	 * Liefert die Anzahl an bislang simulierten Kundenankünften
	 * @return	Anzahl an bislang simulierten Kundenankünften
	 */
	@Override
	public long getCurrentClients() {
		updateStatus(false);
		return currentClients;
	}

	/**
	 * Liefert die aktuelle Anzahl an Kunden im System
	 * @return	Aktuelle Anzahl an Kunden im System
	 */
	@Override
	public int getCurrentWIP() {
		updateStatus(false);
		return currentWIP;
	}

	/**
	 * Gibt die Nummer des gerade im Simulator in Bearbeitung befindlichen Tages zurück.
	 * @return Gerade in Arbeit befindlicher Tag
	 */
	@Override
	public long getSimDayCount() {
		updateStatus(false);
		return simDayCount;
	}

	/**
	 * Liefert die Gesamtanzahl an Wiederholungen in der Simulation.
	 * @return	Anzahl an Wiederholungen (über alle Threads) der Simulation.
	 */
	@Override
	public long getSimDaysCount() {
		updateStatus(false);
		return simDaysCount;
	}

	/**
	 * Wartet bis alle Simulationsthreads beendet sind und berechnet dann die gesamte Laufzeit.
	 * @return Liefert immer null zurück.
	 */
	@Override
	public String finalizeRun() {
		while (started) {
			updateStatus(false);
			if (started) {
				try {Thread.sleep(50);} catch (InterruptedException e) {break;}
			}
		}
		return null;
	}

	/**
	 * Liefert nach Abschluss der Simulation die Statistikergebnisse zurück.
	 * @return	Statistik-Objekt, welches alle Daten des Simulationslaufs enthält (oder <code>null</code>, wenn die Simulation - ggf. auch durch den Server - abgebrochen wurde)
	 */
	@Override
	public Statistics getStatistic() {
		if (started) finalizeRun();
		return statistics;
	}

	/**
	 * Liefert den im Konstruktor angegeben Host
	 * @return	Host für die Kommunikation
	 */
	public String getHost() {
		return net.getHost();
	}
}