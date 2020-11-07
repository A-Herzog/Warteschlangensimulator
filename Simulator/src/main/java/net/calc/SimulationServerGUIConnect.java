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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import tools.SetupData;

/**
 * Diese Klasse stellt ein im Hintergrund arbeitendes Singleton zur Verfügung,
 * welches von GUI-Klassen zum Betrieb eines Simulationsservers, der nicht
 * dauerhaft sichtbar sein muss, verwendet werden kann.
 * @author Alexander Herzog
 * @see SimulationServer
 */
public class SimulationServerGUIConnect {
	/**
	 * Maximale Anzahl an Zeilen, die in der Ausgabe vorgehalten werden sollen
	 */
	private static final int MAX_LINES=1_000;

	/**
	 * Internes Server-Objekt
	 * @see #startServer(int, String, boolean)
	 * @see #stopServer()
	 * @see #isServerRunning()
	 */
	private SimulationServer server;

	/**
	 * Ausgaben des Servers
	 */
	private final List<String> output;

	/**
	 * Absicherung paralleler Zugriffe auf {@link #output}
	 * @see #output
	 */
	private final Semaphore lock;

	/**
	 * Listener, die über Ausgaben des Servers benachrichtigt werden sollen
	 * @see #output(String)
	 * @see #output
	 */
	private final Set<Consumer<List<String>>> listeners;

	/**
	 * Referenz auf das Setup-Singleton
	 */
	private final SetupData setup;

	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse als Singleton ausgelegt und kann nicht direkt instanziert werden.
	 * Stattdessen muss die Methode {@link SimulationServerGUIConnect#getInstance()} verwendet werden.
	 */
	private SimulationServerGUIConnect() {
		server=null;
		output=new ArrayList<>();
		lock=new Semaphore(1);
		listeners=new HashSet<>();
		setup=SetupData.getSetup();
	}

	/**
	 * Instanz dieser Klasse
	 * @see #getInstance()
	 */
	private static SimulationServerGUIConnect instance=null;

	/**
	 * Liefert die Singleton-Instanz dieser Klasse
	 * @return	Instanz dieser Klasse
	 */
	public static synchronized SimulationServerGUIConnect getInstance() {
		if (instance==null) instance=new SimulationServerGUIConnect();
		return instance;
	}

	/**
	 * Startet den Simulationsserver
	 * @param port	Port auf dem der Server auf Anfragen warten soll
	 * @param key	Optionales Passwort zum Verschlüsseln der Daten. Wird hier <code>null</code> oder ein leerer String übergeben, so erfolgt die Übertragung unverschlüsselt.
	 * @param limitThreadCount	Wird <code>true</code> übergeben, so werden nicht mehr gleichzeitige Tasks angenommen, als logische CPU-Kerne für die Threads vorhanden sind.
	 * @return	Liefert <code>true</code>, wenn der Server gestartet werden konnte.
	 */
	public boolean startServer(final int port, final String key, final boolean limitThreadCount) {
		if (server!=null) return false;

		lock.acquireUninterruptibly();
		try {
			output.clear();
		} finally {
			lock.release();
		}

		setup.simulationServerPort=port;
		setup.simulationServerPasswort=(key==null)?"":key;
		setup.simulationServerLimitThreadCount=limitThreadCount;
		setup.saveSetup();

		server=new SimulationServer(s->output(s),port,(key!=null && key.trim().isEmpty())?null:key,limitThreadCount);
		if (server.start()) return true;
		server=null;
		return false;
	}

	/**
	 * Beendet einen ggf. laufenden Server.
	 */
	public void stopServer() {
		if (server==null) return;
		server.stop();
		server=null;
	}

	/**
	 * Gibt an, ob momentan ein Server-Listener-Thread läuft.
	 * @return	Gibt <code>true</code> zurück, wenn momentan ein Server, der auf Verbindungen wartet, läuft.
	 */
	public boolean isServerRunning() {
		return server!=null;
	}

	/**
	 * Liefert die beim letzten Serverstart verwendete Serverportnummer
	 * @return	Letzte Serverportnummer
	 */
	public int getLastPort() {
		return setup.simulationServerPort;
	}

	/**
	 * Liefert das beim letzten Serverstart verwendete Passwort
	 * @return	Letztes Passwort (kann ein leerer String sein, ist aber nie <code>null</code>)
	 */
	public String getLastPasswort() {
		return (setup.simulationServerPasswort==null)?"":setup.simulationServerPasswort;
	}

	/**
	 * Gibt an, ob beim letzten Serverstart die Anzahl an Threads begrenzt wurde
	 * @return	Threadbegrenzung beim letzten Serverstart
	 */
	public boolean getLastLimitThreadCount() {
		return setup.simulationServerLimitThreadCount;
	}

	/**
	 * Fügt einen Listener zu der Liste der Listener, die über Ausgaben des Servers benachrichtigt werden sollen, hinzu.
	 * @param listener	Zusätzlicher zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener zu der Liste hinzugefügt werden konnte.
	 * @see SimulationServerGUIConnect#removeOutputListener(Consumer)
	 * @see SimulationServerGUIConnect#removeAllOutputListeners()
	 */
	public boolean addOutputListener(final Consumer<List<String>> listener) {
		if (listener!=null) {
			lock.acquireUninterruptibly();
			try {
				listener.accept(output);
			} finally {
				lock.release();
			}
		}
		return listeners.add(listener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die über Ausgaben des Servers benachrichtigt werden sollen.
	 * @param listener	Listener der nicht mehr benachrichtigt werden soll
	 * @return	Gibt <code>true</code> zurück, wenn der Listener aus der Liste enfernt werden konnte
	 * @see SimulationServerGUIConnect#addOutputListener(Consumer)
	 * @see SimulationServerGUIConnect#removeAllOutputListeners()
	 */
	public boolean removeOutputListener(final Consumer<List<String>> listener) {
		return listeners.remove(listener);
	}

	/**
	 * Löscht die gesamte Liste der Listener, die über Ausgaben des Servers benachrichtigt werden sollen.
	 * @see SimulationServerGUIConnect#addOutputListener(Consumer)
	 * @see SimulationServerGUIConnect#removeOutputListener(Consumer)
	 */
	public void removeAllOutputListeners() {
		listeners.clear();
	}

	/**
	 * Schreibt eine Zeile in {@link #output}
	 * @param line	Auszugebende Zeile
	 * @see #output
	 */
	private void output(final String line) {
		lock.acquireUninterruptibly();
		try {
			output.add(line);
			while (output.size()>MAX_LINES) output.remove(0);
			for (Consumer<List<String>> listener: listeners) listener.accept(output);
		} finally {
			lock.release();
		}
	}
}