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
import java.net.Socket;

/**
 * Stellt eine Client-Klasse, �ber die mit einem Netzwerk-Server kommuniziert
 * werden kann, bereit. Als Gegenst�ck kann die {@link NetServer}-Klasse verwendet werden.
 * @author Alexander Herzog
 * @see NetServer
 */
public final class NetClient {
	/** Adresse des Servers */
	private final String host;
	/** Portnummer auf dem der Server auf Anfragen wartet */
	private final int port;
	/** Sollen die Daten komprimiert �bertragen werden? */
	private final boolean compress;
	/** Optionales Passwort zum Verschl�sseln der Daten. Wird hier <code>null</code> �bergeben, so erfolgt die �bertragung unverschl�sselt. */
	private final String key;
	/** Maximale Gr��e von empfangbaren Datenbl�cken (zur Vermeidung von externen Angreifern induzierten Out-of-Memory-Fehlern) */
	private final int maxTransferSize;
	private Socket socket;
	private NetTransfer transfer;

	/**
	 * Konstruktor der Klasse
	 * @param host	Adresse des Servers
	 * @param port	Portnummer auf dem der Server auf Anfragen wartet
	 * @param compress	Sollen die Daten komprimiert �bertragen werden?
	 * @param key	Optionales Passwort zum Verschl�sseln der Daten. Wird hier <code>null</code> �bergeben, so erfolgt die �bertragung unverschl�sselt.
	 * @param maxTransferSize	Maximale Gr��e von empfangbaren Datenbl�cken (zur Vermeidung von externen Angreifern induzierten Out-of-Memory-Fehlern)
	 */
	public NetClient(final String host, final int port, final boolean compress, final String key, final int maxTransferSize) {
		this.host=host;
		this.port=port;
		this.compress=compress;
		this.key=key;
		this.maxTransferSize=maxTransferSize;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es wird angenommen, dass maximal Datenblock der Gr��e {@link NetTransfer#DEFAULT_MAX_TRANSFER_SIZE} empfangen werden d�rfen.
	 * @param host	Adresse des Servers
	 * @param port	Portnummer auf dem der Server auf Anfragen wartet
	 * @param compress	Sollen die Daten komprimiert �bertragen werden?
	 * @param key	Optionales Passwort zum Verschl�sseln der Daten. Wird hier <code>null</code> �bergeben, so erfolgt die �bertragung unverschl�sselt.
	 */
	public NetClient(final String host, final int port, final boolean compress, final String key) {
		this(host,port,compress,key,-1);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es wird angenommen, dass maximal Datenblock der Gr��e {@link NetTransfer#DEFAULT_MAX_TRANSFER_SIZE} empfangen werden d�rfen.<br>
	 * Die �bertragung erfolgt unverschl�sselt.
	 * @param host	Adresse des Servers
	 * @param port	Portnummer auf dem der Server auf Anfragen wartet
	 * @param compress	Sollen die Daten komprimiert �bertragen werden?
	 */
	public NetClient(final String host, final int port, final boolean compress) {
		this(host,port,compress,null,-1);
	}

	/**
	 * Baut die Verbindung zum Server auf.
	 * @return	Gibt im Erfolgsfall ein {@link NetTransfer}-Objekt, �ber das mit dem Server kommuniziert werden kann, zur�ck. Im Fehlerfall wird <code>null</code> zur�ckgeliefert.
	 * @see NetClient#stop()
	 */
	public NetTransfer start() {
		if (transfer!=null) return transfer;

		try {
			socket=new Socket(host,port);
		} catch (IOException e) {
			socket=null;
			return null;
		}

		transfer=new NetTransfer(socket,compress,key,maxTransferSize);
		return transfer;
	}

	/**
	 * Beendet die Kommunikation mit dem Server.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Verbindung geschlossen werden konnte oder bereits geschlossen war.
	 * @see NetClient#start()
	 */
	public boolean stop() {
		boolean ok=true;
		if (socket!=null) {
			try {socket.close();} catch (IOException e) {ok=false;}
			socket=null;
			transfer=null;
		}
		return ok;
	}

	/**
	 * Liefert den im Konstruktor angegeben Host
	 * @return	Host f�r die Kommunikation
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Liefert den im Konstruktor angegeben Hostport
	 * @return	Hostport f�r die Kommunikation
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Gibt an, ob im Konstruktor angegeben wurde, dass die �bertragung komprimiert ablaufen soll
	 * @return	Komprimierte �bertragung
	 */
	public boolean isCompress() {
		return compress;
	}

	/**
	 * Gibt an, ob im Konstruktor angegeben wurde, dass die �bertragung verschl�sselt ablaufen soll
	 * @return	Verschl�sselte �bertragung
	 */
	public boolean isEncrypted() {
		return key!=null;
	}
}