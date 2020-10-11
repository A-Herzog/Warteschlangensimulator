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
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.function.Consumer;

import simulator.Simulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.editmodel.EditModelBase;
import ui.MainPanel;

/**
 * Diese Klasse kapselt einen vollständigen Simulations-Netzwerk-Server
 * @author Alexander Herzog
 * @see NetServer
 * @see SimulationClient
 */
public class SimulationServer extends NetServer {
	/** Bezeichner für die Meldung "Simulation gestartet" */
	public static String SIMULATION_STARTED="Simulation gestartet";
	/** Bezeichner für die Meldung "Simulation erfolgreich abgeschlossen" */
	public static String SIMULATION_FINISHED="Simulation erfolgreich abgeschlossen";
	/** Bezeichner für die Meldung "Simulation abgeschlossen, Ergebnisse werden gesendet." */
	public static String SIMULATION_FINISHED_SENDING="Simulation abgeschlossen, Ergebnisse werden gesendet.";
	/** Bezeichner für die Meldung "Simulation abgebrochen" */
	public static String SIMULATION_CANCELED="Simulation abgebrochen";
	/** Bezeichner für die Fehlermeldung "Der Server ist bereits maximal ausgelastet." */
	public static String PREPARE_REJECTED_DUE_TO_OVERLOAD="Der Server ist bereits maximal ausgelastet.";
	/** Bezeichner für die Fehlermeldung "Kein Modell empfangen." */
	public static String PREPARE_NO_MODEL="Kein Modell empfangen.";
	/** Bezeichner für die Fehlermeldung "Das Modell kann nicht auf einem externen Rechner simuliert werden." */
	public static String PREPARE_NO_REMOTE_MODEL="Das Modell kann nicht auf einem externen Rechner simuliert werden.";
	/** Bezeichner für die Fehlermeldung "Client- und Serverversion passen nicht zueinander." */
	public static String PREPARE_VERSION_MISMATCH="Client- und Serverversion passen nicht zueinander.";

	/** Wird <code>true</code> übergeben, so werden nicht mehr gleichzeitige Tasks angenommen, als logische CPU-Kerne für die Threads vorhanden sind. */
	private final boolean limitThreadCount;
	private int runningThreads;
	private int simulationCount;

	/**
	 * Konstruktor der Klasse
	 * @param output	Callback über das Statusmeldungen angegeben werden können (darf <code>null</code> sein).
	 * @param port	Port auf dem der Server auf Anfragen warten soll
	 * @param key	Optionales Passwort zum Verschlüsseln der Daten. Wird hier <code>null</code> übergeben, so erfolgt die Übertragung unverschlüsselt.
	 * @param limitThreadCount	Wird <code>true</code> übergeben, so werden nicht mehr gleichzeitige Tasks angenommen, als logische CPU-Kerne für die Threads vorhanden sind.
	 */
	public SimulationServer(final Consumer<String> output, final int port, final String key, final boolean limitThreadCount) {
		super(output,port,true,key);
		this.limitThreadCount=limitThreadCount;
		runningThreads=0;
		simulationCount=0;
	}

	private void returnError(final NetTransfer transfer, final Consumer<String> log, final String message) {
		transfer.sendByte(1);
		transfer.sendString(message);
		log.accept(message);
	}

	private Simulator startSimulator(final NetTransfer transfer, final Consumer<String> log) {
		/* Schon zu viele Threads? */
		if (runningThreads>=Runtime.getRuntime().availableProcessors() && limitThreadCount) {
			returnError(transfer,log,PREPARE_REJECTED_DUE_TO_OVERLOAD);
			return null;
		}

		/* Modell über Netzwerk laden */
		final byte[] input=transfer.waitForBytes(30_000); /* Evtl. werden sehr viele Verbindungen auf einmal aufgemacht. Dann kann das relevant sein. */
		if (input==null) {
			returnError(transfer,log,PREPARE_NO_MODEL);
			return null;
		}

		/* Modell aus Stream laden */
		final EditModel model=new EditModel();
		final String loadError=model.loadFromStream(new ByteArrayInputStream(input));
		if (loadError!=null) {
			returnError(transfer,log,loadError);
			return null;
		}

		/* Kann das Modell überhaupt von einem anderen Rechner simuliert werden? */
		if (!StartAnySimulator.isRemoveSimulateable(model)) {
			returnError(transfer,log,PREPARE_NO_REMOTE_MODEL);
			return null;
		}

		/* Passen die Versionen zusammen? */
		if (EditModelBase.isOtherVersionSystem(model.version,EditModel.systemVersion)) {
			returnError(transfer,log,PREPARE_VERSION_MISMATCH);
			return null;
		}

		/* Starten */
		final Simulator simulator=new Simulator(model,null,null,Simulator.logTypeFull);
		final String prepareError=simulator.prepare();
		if (prepareError!=null) {
			returnError(transfer,log,prepareError);
			return null;
		}

		log.accept(SIMULATION_STARTED);
		simulator.start();
		return simulator;
	}

	private void processCommandSimulation(final NetTransfer transfer, final Consumer<String> log) {
		final Simulator simulator=startSimulator(transfer,log);
		if (simulator==null) return;
		final long clientCount=simulator.getCountClients();
		final long daysCount=simulator.getSimDaysCount();

		simulationCount++;
		runningThreads+=simulator.threadCount;
		try {

			/* Simulation */
			while (true) {
				/* Abbruch des Threads erkennen */
				if (Thread.interrupted()) {
					simulator.cancel();
					log.accept(SIMULATION_CANCELED);
					return;
				}
				/* Ende erkennen */
				if (!simulator.isRunning()) break;
				/* Abbruchbefehl vom Client erkennen */
				final byte[] abortTest=transfer.getBytes();
				if (abortTest!=null) {
					simulator.cancel();
					log.accept(SIMULATION_CANCELED);
					return;
				}
				/* Info senden */
				sendInfo(transfer,simulator,clientCount,daysCount);

				/* Info-Thread pausieren */
				try {Thread.sleep(100);} catch (InterruptedException e) {
					simulator.cancel();
					log.accept(SIMULATION_CANCELED);
					return;
				}
			}

			/* Ergebnis senden */
			log.accept(SIMULATION_FINISHED_SENDING);
			sendInfo(transfer,simulator,clientCount,daysCount); /* Timeout verhindern */
			final ByteArrayOutputStream output=new ByteArrayOutputStream();
			simulator.getStatistic().saveToStream(output);
			sendInfo(transfer,simulator,clientCount,daysCount); /* Timeout verhindern */
			transfer.sendByte(3);
			transfer.sendStream(output);
			log.accept(SIMULATION_FINISHED);

		} finally {
			runningThreads-=simulator.threadCount;
		}
	}

	private boolean processCommandInfo(final NetTransfer transfer) {
		final ByteArrayOutputStream info=new ByteArrayOutputStream();
		final DataOutputStream writer=new DataOutputStream(info);
		try {
			writer.writeUTF(MainPanel.VERSION);
			writer.writeUTF(System.getProperty("java.version")+" ("+System.getProperty("java.vm.name")+")");
			writer.writeInt(Runtime.getRuntime().availableProcessors());
			final long l1=ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
			final long l2=ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
			writer.writeInt((int)((l1+l2)/1024/1024));
			writer.writeLong(simulationCount);
			writer.writeInt(runningThreads);
		} catch (IOException e) {return false;}
		transfer.sendStream(info);
		return true;
	}

	@Override
	protected void process(final NetTransfer transfer, final Consumer<String> log) {
		byte[] cmd=transfer.waitForBytes(30_000);
		if (cmd==null || cmd.length!=1) {
			transfer.sendByte(0);
			return;
		}

		switch (cmd[0]) {
		case 0: processCommandSimulation(transfer,log); break;
		case 1: processCommandInfo(transfer); break;
		}
	}

	private boolean sendInfo(final NetTransfer transfer, final Simulator simulator, final long clientCount, final long daysCount) {
		transfer.sendByte(2);
		final ByteArrayOutputStream info=new ByteArrayOutputStream();
		final DataOutputStream writer=new DataOutputStream(info);
		try {
			writer.writeLong(simulator.getCurrentClients());
			writer.writeLong(clientCount);
			writer.writeInt(simulator.getCurrentWIP());
			writer.writeLong(simulator.getSimDayCount());
			writer.writeLong(daysCount);
			writer.writeLong(simulator.getEventCount());
			writer.writeInt(simulator.getEventsPerSecond());
		} catch (IOException e) {return false;}
		transfer.sendStream(info);
		return true;
	}
}