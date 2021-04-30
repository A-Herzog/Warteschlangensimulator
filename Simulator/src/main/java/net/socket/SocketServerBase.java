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
package net.socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Basisklasse für Socket-basierte Server-Dienste.
 * @author Alexander Herzog
 * @see SocketServerCalc
 */
public abstract class SocketServerBase {
	/**
	 * Server-Port
	 */
	private int port;

	/**
	 * Server-Socket
	 */
	private ServerSocket listenSocket;

	/**
	 * Thread in dem auf eingehende Anfragen gewartet wird<br>
	 * (nicht der Thread, in dem die Anfragen bearbeitet werden)
	 */
	private Thread listenThread;

	/**
	 * Konstruktor der Klasse

	 */
	public SocketServerBase() {
		port=-1;
	}

	/**
	 * Liefert den Server-Port.
	 * @return	Server-Port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Läuft der Server gerade?
	 * @return	Liefert <code>true</code>, wenn der Server läuft
	 */
	public boolean isRunning() {
		return listenSocket!=null;
	}

	/**
	 * Startet den Server.
	 * @param port	Server-Port
	 * @return	Gibt <code>true</code> zurück, wenn der Netzwerkserver gestartet werden konnte.
	 * @see #stop()
	 */
	public boolean start(final int port) {
		if (listenSocket!=null) return true;

		this.port=port;

		try {
			listenSocket=new ServerSocket(port);
		} catch (IOException e) {
			listenSocket=null;
			this.port=-1;
			return false;
		}

		listenThread=new Thread(()->listen(),"SocketServer");
		listenThread.setDaemon(true);
		listenThread.start();

		return true;
	}

	/**
	 * Stoppt den Netzwerkserver.
	 * @see #start(int)
	 */
	public void stop() {
		if (listenThread!=null) {
			listenThread.interrupt();
			try {listenThread.join(1_000);} catch (InterruptedException e) {}
			listenThread=null;
			port=-1;
		}
	}

	/**
	 * Wartet auf eintreffende Anfragen.<br>
	 * Diese Methode beinhaltet die Hauptschleife
	 * von {@link #listenThread}.
	 * @see #listenThread
	 */
	@SuppressWarnings("resource")
	private void listen() {
		try {listenSocket.setSoTimeout(250);} catch (SocketException e1) {}

		while (!Thread.interrupted()) {
			try {
				final Socket socket=listenSocket.accept();
				new Thread(()->{
					try {
						try (InputStream input=socket.getInputStream()) {
							try (OutputStream output=socket.getOutputStream()) {
								final ByteArrayOutputStream outputInternal=process(input);
								if (outputInternal!=null) {
									final byte[] result=outputInternal.toByteArray();
									if (result!=null && result.length>0) output.write(result);
								}
							}
						}
					} catch (IOException e) {} finally {
						try {
							socket.close();
						} catch (IOException e) {}
					}

				},"SocketServerTaskProcessor").start();
			} catch (IOException e) {}
		}

		try {listenSocket.close();} catch (IOException e) {}
		listenSocket=null;
	}

	/**
	 * Führt die Verarbeitung einer Anfrage aus.<br>
	 * Diese Methode wird bereits in einem eigenen Thread gestartet.
	 * @param input	Empfangene Daten
	 * @return	Zurück zu liefernde Daten
	 */
	protected abstract ByteArrayOutputStream process(final InputStream input);

	/**
	 * Liest eine festgelegte Anzahl an Bytes aus dem Eingabe-Stream.
	 * @param input	Eingabe-Stream aus dem die Daten geladen werden sollen
	 * @param count	Anzahl an zu lesenden Bytes
	 * @return	Liefert im Erfolgsfall die gelesenen Bytes, sonst <code>null</code>
	 */
	protected static byte[] readData(final InputStream input, final int count) {
		final byte[] result=new byte[count];
		int read=0;
		while (read<count) {
			try {
				read+=input.read(result,read,count-read);
			} catch (IOException e) {
				return null;
			}
		}
		return result;
	}

	/**
	 * Liest Bytes aus dem Eingabe-Stream.
	 * @param input	Eingabe-Stream aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall die gelesenen Bytes, sonst <code>null</code>
	 */
	protected static byte[] readData(final InputStream input) {
		final int size=readInteger(input);
		if (size<0) return null;
		return readData(input,size);
	}

	/**
	 * Liest einen String festgelegter Länge aus einem Eingabe-Stream.
	 * @param input	Eingabe-Stream aus dem die Daten geladen werden sollen
	 * @param count	Anzahl an zu lesenden Bytes
	 * @return	Liefert im Erfolgsfall den String, sonst <code>null</code>
	 */
	protected static String readString(final InputStream input, final int count) {
		final byte[] bytes=readData(input,count);
		if (bytes==null) return null;
		return new String(bytes,StandardCharsets.UTF_8)	;
	}

	/**
	 * Liest einen String aus einem Eingabe-Stream.
	 * @param input	Eingabe-Stream aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall den String, sonst <code>null</code>
	 */
	protected static String readString(final InputStream input) {
		final int size=readInteger(input);
		if (size<0) return null;
		return readString(input,size);
	}

	/**
	 * Liest einen Integer-Wert im Big-Endian-Format aus dem Eingabe-Stream.
	 * @param input	Eingabe-Stream aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall den Zahlenwert, sonst -1
	 */
	protected static int readInteger(final InputStream input) {
		final byte[] bytes=readData(input,4);
		if (bytes==null) return -1;
		return new BigInteger(bytes).intValue();
	}

	/**
	 * Wandelt zerlegt Integer-Wert in einzelne Bytes (im Big-Endian-Format).
	 * @param value	Zu zerlegender Integer-Wert
	 * @return	Bytes, die den Integer-Wert repräsentieren
	 */
	protected static byte[] write(final int value) {
		return new byte[] {
				(byte)(value >>> 24),
				(byte)(value >>> 16),
				(byte)(value >>> 8),
				(byte)value
		};
	}

	/**
	 * Schreibt einen Integer-Wert im Big-Endian-Format in einen Ausgabe-Stream.
	 * @param value	Auszugebender Integer-Wert
	 * @param output	Ausgabe-Stream
	 */
	protected static void write(final int value, final ByteArrayOutputStream output) {
		output.write(write(value),0,4);
	}

	/**
	 * Schreibt eine Zeichenkette in einen Ausgabe-Stream.
	 * @param result	Auszugebende Zeichenkette
	 * @param output	Ausgabe-Stream
	 */
	protected static void write(final String result, final ByteArrayOutputStream output) {
		write(result.getBytes(StandardCharsets.UTF_8),output);
	}

	/**
	 * Schreibt den Inhalt eines Byte-Arrays in einen Ausgabe-Stream.
	 * @param data	Auszugebendes Byte-Array
	 * @param output	Ausgabe-Stream
	 */
	protected static void write(final byte[] data, final ByteArrayOutputStream output) {
		output.write(write(data.length),0,4);
		output.write(data,0,data.length);
	}
}
