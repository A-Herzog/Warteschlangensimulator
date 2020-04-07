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
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import xml.ChiperTools;

/**
 * Diese Klasse erlaubt es, Daten über einen bereits offenen Socket
 * zu übertragen.
 * @author Alexander Herzog
 */
public final class NetTransfer {
	private static final int DEFAULT_MAX_TRANSFER_SIZE=250*1024*1024;

	private final Socket socket;
	private DataOutputStream outputStream;
	private DataInputStream inputStream;
	private final boolean compress;
	private final String key;
	private final int maxTransferSize;
	private byte[] receiveBlock;
	private int received;
	private int skipBytes;
	private boolean decodeOrdecryptError;

	/**
	 * Konstruktor der Klasse
	 * @param socket	Offener Socket (wird von dieser Klasse auch nicht geschlossen)
	 * @param compress	Sollen die Daten komprimiert übertragen werden?
	 * @param key	Optionales Passwort zum Verschlüsseln der Daten. Wird hier <code>null</code> übergeben, so erfolgt die Übertragung unverschlüsselt.
	 * @param maxTransferSize	Maximale Größe von empfangbaren Datenblöcken (zur Vermeidung von externen Angreifern induzierten Out-of-Memory-Fehlern)
	 */
	public NetTransfer(final Socket socket, final boolean compress, final String key, final int maxTransferSize) {
		this.socket=socket;
		this.compress=compress;
		this.key=key;
		this.maxTransferSize=(maxTransferSize>0)?maxTransferSize:DEFAULT_MAX_TRANSFER_SIZE;

		receiveBlock=null;
		skipBytes=0;
		outputStream=null;
		inputStream=null;
		decodeOrdecryptError=false;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es wird angenommen, dass maximal Datenblock der Größe {@link NetTransfer#DEFAULT_MAX_TRANSFER_SIZE} empfangen werden dürfen.
	 * @param socket	Offener Socket (wird von dieser Klasse auch nicht geschlossen)
	 * @param compress	Sollen die Daten komprimiert übertragen werden?
	 * @param key	Optionales Passwort zum Verschlüsseln der Daten. Wird hier <code>null</code> übergeben, so erfolgt die Übertragung unverschlüsselt.
	 */
	public NetTransfer(final Socket socket, final boolean compress, final String key) {
		this(socket,compress,key,DEFAULT_MAX_TRANSFER_SIZE);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es wird angenommen, dass maximal Datenblock der Größe {@link NetTransfer#DEFAULT_MAX_TRANSFER_SIZE} empfangen werden dürfen.<br>
	 * Die Übertragung erfolgt unverschlüsselt.
	 * @param socket	Offener Socket (wird von dieser Klasse auch nicht geschlossen)
	 * @param compress	Sollen die Daten komprimiert übertragen werden?
	 */
	public NetTransfer(final Socket socket, final boolean compress) {
		this(socket,compress,null,DEFAULT_MAX_TRANSFER_SIZE);
	}

	private byte[] compress(byte[] data) {
		final ByteArrayOutputStream result=new ByteArrayOutputStream();

		try {
			try (GZIPOutputStream zip=new GZIPOutputStream(result)) {
				zip.write(data);
				zip.flush();
			}
		} catch (IOException e) {return null;}

		return result.toByteArray();
	}

	private byte[] decompress(byte[] data) {
		final ByteArrayOutputStream bufferStream;
		final byte[] buf=new byte[32768];
		int count;

		try {
			GZIPInputStream zip=new GZIPInputStream(new ByteArrayInputStream(data));
			bufferStream=new ByteArrayOutputStream();
			while ((count=zip.read(buf))!=-1) bufferStream.write(buf,0,count);
		} catch (IOException e) {return null;}

		return bufferStream.toByteArray();
	}

	private byte[] encode(byte[] input) {
		if (compress) input=compress(input);
		if (key!=null) input=ChiperTools.encrypt(input,key);
		return input;
	}

	private byte[] decode(byte[] input) {
		if (input==null) return null;

		if (key!=null) {
			input=ChiperTools.decrypt(input,key);
			if (input==null) {
				decodeOrdecryptError=true;
				return null;
			}
		}

		if (compress) {
			input=decompress(input);
			if (input==null) {
				decodeOrdecryptError=true;
				return null;
			}
		}

		return input;
	}

	/**
	 * Liefert den nächsten Block.<br>
	 * Wurde noch kein Datenblock vollständig übertragen, so liefert die Funktion <code>null</code>.
	 * @return	Nächster vollständiger Datenblock oder <code>null</code> wenn noch kein Datenblock vollständig empfangen wurde.
	 * @see #getString()
	 * @see #getStream()
	 * @see #waitForBytes(int)
	 */
	public byte[] getBytes() {
		if (socket==null) return null;

		try {
			if (inputStream==null) inputStream=new DataInputStream(socket.getInputStream());

			while (inputStream.available()>0) {
				final int available=inputStream.available();

				/* Überspringen wir gerade einen ungültigen Block? */
				if (skipBytes>0) {
					final int skip=Math.min(skipBytes,available);
					inputStream.skip(skip);
					skipBytes-=skip;
					continue;
				}

				/* Block ggf. vorbereiten */
				if (receiveBlock==null) {
					if (available<4) return null;
					final int size=inputStream.readInt();
					if (size>maxTransferSize) {
						skipBytes=size;
						continue;
					}
					receiveBlock=new byte[size];
					received=0;
					continue;
				}

				/* Daten lesen */
				final int size=Math.min(receiveBlock.length-received,available);
				inputStream.read(receiveBlock,received,size);
				received+=size;

				/* Block zu Ende? */
				if (received==receiveBlock.length) {
					final byte[] result=decode(receiveBlock);
					receiveBlock=null;
					received=0;
					return result;
				}

				return null;
			}
		} catch (IOException e) {return null;}
		return null;
	}

	/**
	 * Liefert den nächsten Block.<br>
	 * Wurde noch kein Datenblock vollständig übertragen, so liefert die Funktion <code>null</code>.
	 * @return	Nächster vollständiger Datenblock oder <code>null</code> wenn noch kein Datenblock vollständig empfangen wurde.
	 * @see #getString()
	 * @see #getBytes()
	 * @see #waitForStream(int)
	 */
	public ByteArrayInputStream getStream()  {
		final byte[] data=getBytes();
		if (data==null) return null;
		return new ByteArrayInputStream(data);
	}

	/**
	 * Liefert den nächsten Block in Form einer Zeichenkette.<br>
	 * Wurde noch kein Datenblock vollständig übertragen, so liefert die Funktion <code>null</code>.
	 * @return	Nächster vollständiger Datenblock oder <code>null</code> wenn noch kein Datenblock vollständig empfangen wurde.
	 * @see #getBytes()
	 * @see #getStream()
	 * @see #waitForString(int)
	 */
	public String getString() {
		byte[] bytes=getBytes();
		if (bytes==null) return null;
		return new String(bytes);
	}

	/**
	 * Liefert den nächsten Block (und wartet vorher ggf. auf diesen).
	 * @param timeOutMS	Maximale Wartezeitdauer
	 * @return	Nächster Block oder <code>null</code> wenn die maximale Zeitdauer überschritten wurde
	 * @see NetTransfer#waitForString(int)
	 * @see NetTransfer#getBytes()
	 */
	public byte[] waitForBytes(int timeOutMS) {
		decodeOrdecryptError=false;
		byte[] result=getBytes();
		if (result!=null || decodeOrdecryptError) return result;
		while (true) {
			try {Thread.sleep(50);} catch (InterruptedException e) {Thread.currentThread().interrupt(); return null;}
			result=getBytes();
			if (result!=null || decodeOrdecryptError) return result;
			timeOutMS-=50;
			if (timeOutMS<=0) return null;
		}
	}

	/**
	 * Liefert den nächsten Block (und wartet vorher ggf. auf diesen).
	 * @param timeOutMS	Maximale Wartezeitdauer
	 * @return	Nächster Block oder <code>null</code> wenn die maximale Zeitdauer überschritten wurde
	 * @see NetTransfer#waitForString(int)
	 * @see NetTransfer#getStream()
	 */
	public ByteArrayInputStream waitForStream(int timeOutMS) {
		final byte[] bytes=waitForBytes(timeOutMS);
		if (bytes==null) return null;
		return new ByteArrayInputStream(bytes);
	}

	/**
	 * Liefert den nächsten Block in Form einer Zeichenkette (und wartet vorher ggf. auf diesen).<br>
	 * @param timeOutMS	Maximale Wartezeitdauer
	 * @return	Nächster Block oder <code>null</code> wenn die maximale Zeitdauer überschritten wurde
	 * @see NetTransfer#waitForBytes(int)
	 * @see NetTransfer#getString()
	 */
	public String waitForString(int timeOutMS) {
		decodeOrdecryptError=false;
		String result=getString();
		if (result!=null || decodeOrdecryptError) return result;
		while (true) {
			try {Thread.sleep(50);} catch (InterruptedException e) {Thread.currentThread().interrupt(); return null;}
			result=getString();
			if (result!=null || decodeOrdecryptError) return result;
			timeOutMS-=50;
			if (timeOutMS<=0) return null;
		}
	}

	/**
	 * Versendet einen Datenblock.
	 * @param data	Zu sendender Datenblock
	 * @return	Gibt <code>true</code> zurück, wenn der Block gesendet werden konnte.
	 * @see #sendByte(int)
	 * @see #sendStream(ByteArrayOutputStream)
	 */
	public boolean sendBytes(final byte[] data) {
		if (socket==null) return false;

		try {
			if (outputStream==null) outputStream=new DataOutputStream(socket.getOutputStream());
			final byte[] input=encode(data);
			outputStream.writeInt(input.length);
			outputStream.write(input);
		} catch (IOException e) {return false;}

		return true;
	}

	/**
	 * Versendet einen byte-Wert.
	 * @param data	Zu sendender byte-Wert
	 * @return	Gibt <code>true</code> zurück, wenn der Block gesendet werden konnte.
	 * @see #sendBytes(byte[])
	 * @see #sendStream(ByteArrayOutputStream)
	 */
	public boolean sendByte(final int data) {
		return sendBytes(new byte[] {(byte)data});
	}

	/**
	 * Versendet einen int-Wert.
	 * @param data	Zu sendender int-Wert
	 * @return	Gibt <code>true</code> zurück, wenn der Block gesendet werden konnte.
	 * @see #sendBytes(byte[])
	 * @see #sendStream(ByteArrayOutputStream)
	 */
	public boolean sendInt(final int data) {
		final ByteArrayOutputStream stream=new ByteArrayOutputStream();
		final DataOutputStream writer=new DataOutputStream(stream);
		try {
			writer.writeInt(data);
		} catch (IOException e) {return false;}
		return sendStream(stream);
	}

	/**
	 * Versendet einen Datenblock.
	 * @param stream	Stream der die zu sendender Daten enthält
	 * @return	Gibt <code>true</code> zurück, wenn der Block gesendet werden konnte.
	 * @see #sendByte(int)
	 * @see #sendBytes(byte[])
	 */
	public boolean sendStream(final ByteArrayOutputStream stream) {
		return sendBytes(stream.toByteArray());
	}

	/**
	 * Versender eine Zeichenkette.
	 * @param data	Zu sendende Zeichenkette
	 * @return	Gibt <code>true</code> zurück, wenn die Zeichenkette gesendet werden konnte.
	 */
	public boolean sendString(final String data) {
		return sendBytes(data.getBytes());
	}

	/**
	 * Gibt an, ob die letzte Übertragung aufgrund unpassender Parameter (Verschlüsselung, Kompression, ...) gescheitert ist.
	 * @return	Gibt <code>true</code> an, wenn die letzte Übertragung <code>null</code> lieferte und der Grund nicht ein Timeout, sondern ein Verbindungsfehler war.
	 */
	public boolean receivedInvalidData() {
		return decodeOrdecryptError;
	}
}
