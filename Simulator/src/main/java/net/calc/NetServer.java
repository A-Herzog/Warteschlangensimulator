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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

/**
 * Stellt eine Server-Klasse, die auf Verbindungen �ber einen Socket wartet,
 * bereit. Als Gegenst�ck kann die {@link NetClient}-Klasse verwendet werden.
 * @author Alexander Herzog
 * @see NetClient
 */
public abstract class NetServer {
	/** Bezeichner f�r Fehlermeldung "Der Serverdienst konnte auf Port %d nicht gestartet werden." */
	public static String ERROT_START="Der Serverdienst konnte auf Port %d nicht gestartet werden.";
	/** Bezeichner f�r Logging-Meldung "Der Serverdienst wurde auf Port %d gestartet." */
	public static String LOG_START="Der Serverdienst wurde auf Port %d gestartet.";
	/** Bezeichner f�r Logging-Meldung "Der Serverdienst wurde auf Port %d beendet." */
	public static String LOG_STOP="Der Serverdienst wurde auf Port %d beendet.";
	/** Bezeichner f�r Logging-Meldung "[%06d] Verbindung hergestellt mit %s." */
	public static String LOG_CONNECTION_START="[%06d] Verbindung hergestellt mit %s.";
	/** Bezeichner f�r Logging-Meldung "[%06d] Verbindung mit %s beendet." */
	public static String LOG_CONNECTION_STOP="[%06d] Verbindung mit %s beendet.";

	/** Callback �ber das Statusmeldungen angegeben werden k�nnen (darf <code>null</code> sein). */
	private final Consumer<String> output;
	/** Port auf dem der Server auf Anfragen warten soll */
	private final int port;
	/** Sollen die Daten komprimiert �bertragen werden? */
	private final boolean compress;
	/** Optionales Passwort zum Verschl�sseln der Daten. Wird hier <code>null</code> �bergeben, so erfolgt die �bertragung unverschl�sselt. */
	private final String key;
	/** Maximale Gr��e von empfangbaren Datenbl�cken (zur Vermeidung von externen Angreifern induzierten Out-of-Memory-Fehlern) */
	private final int maxTransferSize;
	/** Socket der auf eingehende Verbindungen wartet */
	private ServerSocket listenSocket;
	/** Thread in dem {@link #listenSocket} arbeitet */
	private Thread listenThread;
	/** Z�hlt die Anfragen */
	private long runnerCounter;

	/**
	 * Konstruktor der Klasse
	 * @param output	Callback �ber das Statusmeldungen angegeben werden k�nnen (darf <code>null</code> sein).
	 * @param port	Port auf dem der Server auf Anfragen warten soll
	 * @param compress	Sollen die Daten komprimiert �bertragen werden?
	 * @param key	Optionales Passwort zum Verschl�sseln der Daten. Wird hier <code>null</code> �bergeben, so erfolgt die �bertragung unverschl�sselt.
	 * @param maxTransferSize	Maximale Gr��e von empfangbaren Datenbl�cken (zur Vermeidung von externen Angreifern induzierten Out-of-Memory-Fehlern)
	 */
	public NetServer(final Consumer<String> output, final int port, final boolean compress, final String key, final int maxTransferSize) {
		this.output=output;
		this.port=port;
		this.compress=compress;
		this.key=key;
		this.maxTransferSize=maxTransferSize;

		runnerCounter=0;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es wird angenommen, dass maximal Datenblock der Gr��e {@link NetTransfer#DEFAULT_MAX_TRANSFER_SIZE} empfangen werden d�rfen.
	 * @param output	Callback �ber das Statusmeldungen angegeben werden k�nnen (darf <code>null</code> sein).
	 * @param port	Port auf dem der Server auf Anfragen warten soll
	 * @param compress	Sollen die Daten komprimiert �bertragen werden?
	 * @param key	Optionales Passwort zum Verschl�sseln der Daten. Wird hier <code>null</code> �bergeben, so erfolgt die �bertragung unverschl�sselt.
	 */
	public NetServer(final Consumer<String> output, final int port, final boolean compress, final String key) {
		this(output,port,compress,key,-1);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es wird angenommen, dass maximal Datenblock der Gr��e {@link NetTransfer#DEFAULT_MAX_TRANSFER_SIZE} empfangen werden d�rfen.<br>
	 * Die �bertragung erfolgt unverschl�sselt.
	 * @param output	Callback �ber das Statusmeldungen angegeben werden k�nnen (darf <code>null</code> sein).
	 * @param port	Port auf dem der Server auf Anfragen warten soll
	 * @param compress	Sollen die Daten komprimiert �bertragen werden?
	 */
	public NetServer(final Consumer<String> output, final int port, final boolean compress) {
		this(output,port,compress,null,-1);
	}

	/**
	 * Gibt eine Log-Meldung aus.<br>
	 * Diese Methode ist nicht lokal f�r die einzelnen Rechenthreads.
	 * @param line	Auszugebende Meldung
	 */
	protected synchronized void logGlobal(final String line) {
		if (output!=null) output.accept(line);
	}

	/**
	 * Startet den Server.
	 * @return	Gibt <code>true</code> zur�ck, wenn der Netzwerkserver gestartet werden konnte.
	 * @see NetServer#stop()
	 */
	public boolean start() {
		if (listenSocket!=null) return true;

		try {
			listenSocket=new ServerSocket(port);
		} catch (IOException e) {
			listenSocket=null;
			logGlobal(String.format(ERROT_START,port));
			return false;
		}

		listenThread=new Thread(()->listen(),"ServerListener");
		listenThread.setDaemon(true);
		listenThread.start();

		return true;
	}

	/**
	 * Wartet auf eintreffende Anfragen.<br>
	 * Diese Methode beinhaltet die Hauptschleife
	 * von {@link #listenThread}.
	 * @see #listenThread
	 */
	@SuppressWarnings("resource")
	private void listen() {
		logGlobal(String.format(LOG_START,port));

		try {listenSocket.setSoTimeout(250);} catch (SocketException e1) {}

		while (!Thread.interrupted()) {
			try {
				new ServerThread(runnerCounter,listenSocket.accept());
				runnerCounter++;
			} catch (IOException e) {}
		}
		try {listenSocket.close();} catch (IOException e) {}
		listenSocket=null;

		logGlobal(String.format(LOG_STOP,port));
	}

	/**
	 * Verarbeitungsthread
	 */
	private class ServerThread extends Thread {
		/** ID der Anfrage */
		private final long id;
		/** Verbindungs-Socket zum Anfragesteller */
		private final Socket runSocket;
		/** Ist dies die erste Logging-Ausgabe? (Wenn ja, erst noch Verbindungs-Info ausgeben.) */
		private boolean firstLog=true;

		/**
		 * Konstruktor der Klasse
		 * @param id	ID der Anfrage
		 * @param runSocket	Verbindungs-Socket zum Anfragesteller
		 */
		public ServerThread(final long id, final Socket runSocket) {
			super("ServerWorker");
			this.id=id;
			this.runSocket=runSocket;

			if (runSocket!=null) {
				super.start();
			}
		}

		/**
		 * F�hrt die eigentliche Verarbeitung innerhalb des Threads durch.
		 * @return	Liefert <code>true</code>, wenn die Verarbeitung erfolgreich abgeschlossen werden konnte
		 * @see #run()
		 */
		private boolean processServerTask() {
			try {
				process(new NetTransfer(runSocket,compress,key,maxTransferSize),s->{
					if (firstLog) {
						logGlobal(String.format(LOG_CONNECTION_START,id,runSocket.getInetAddress().getHostAddress()));
						firstLog=false;
					}
					logGlobal(String.format("[%06d] %s",id,s));
				});
			} finally {
				if (!firstLog) logGlobal(String.format(LOG_CONNECTION_STOP,id,runSocket.getInetAddress().getHostAddress()));
				try {
					runSocket.close();
				} catch (IOException e) {
					return false;
				}
			}
			return true;
		}

		@Override
		public void run() {
			processServerTask(); /* Damit die innere Funktion bei Exception false zur�ckgeben kann, damit SpotBugs gl�cklich ist. */
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Verbindung von einem Client aus aufgebaut wurde.<br>
	 * Die Socket-Connection wurde bereits aufgebaut und wird nach dem Verlassen dieser Methode wieder abgebaut.
	 * @param transfer	Objekt, �ber das Daten zum Client gesandt werden k�nnen und von ihm empfangen werden k�nnen.
	 * @param log	Erm�glicht die serverseitige Ausgabe von Statusmeldungen.
	 */
	protected abstract void process(final NetTransfer transfer, final Consumer<String> log);

	/**
	 * Stoppt den Netzwerkserver.
	 * @see NetServer#start()
	 */
	public void stop() {
		if (listenThread!=null) {
			listenThread.interrupt();
			try {listenThread.join(1_000);} catch (InterruptedException e) {}
			listenThread=null;
		}
	}
}