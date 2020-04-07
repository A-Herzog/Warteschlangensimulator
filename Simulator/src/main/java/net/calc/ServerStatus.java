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

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import language.Language;
import systemtools.MsgBox;

/**
 * Liefert Status-Informationen zu einem Server
 * @author Alexander Herzog
 */
public class ServerStatus {
	private final NetClient netClient;

	/**
	 * Konstruktor der Klasse<br>
	 * @param host	Adresse des Servers
	 * @param port	Portnummer auf dem der Server auf Anfragen wartet
	 * @param compress	Sollen die Daten komprimiert übertragen werden?
	 * @param key	Optionales Passwort zum Verschlüsseln der Daten. Wird hier <code>null</code> übergeben, so erfolgt die Übertragung unverschlüsselt.
	 */
	public ServerStatus(final String host, final int port, final boolean compress, final String key) {
		netClient=new NetClient(host,port,compress,key);
	}

	/**
	 * Liefert die Daten zum Server
	 * @return	Daten zum Server
	 */
	public String get() {
		final NetTransfer transfer=netClient.start();

		try {
			if (transfer==null) return String.format(SimulationClient.NO_CONNECT,netClient.getHost(),netClient.getPort());
			if (!transfer.sendByte(1)) return String.format(SimulationClient.NO_CONNECT,netClient.getHost(),netClient.getPort());

			final StringBuilder result=new StringBuilder();
			final ByteArrayInputStream stream=transfer.waitForStream(30_000);
			if (stream==null) return String.format(SimulationClient.NO_CONNECT,netClient.getHost(),netClient.getPort());

			try (final DataInputStream reader=new DataInputStream(stream)) {
				result.append(Language.tr("Server.Status.ServerAddress"));
				result.append(": ");
				result.append(netClient.getHost()+":"+netClient.getPort());
				result.append("\n");

				result.append(Language.tr("Server.Status.UseCompression"));
				result.append(": ");
				result.append(netClient.isCompress()?Language.tr("Server.Status.Yes"):Language.tr("Server.Status.No"));
				result.append("\n");

				result.append(Language.tr("Server.Status.UseEncryption"));
				result.append(": ");
				result.append(netClient.isEncrypted()?Language.tr("Server.Status.Yes"):Language.tr("Server.Status.No"));
				result.append("\n");

				result.append(Language.tr("Server.Status.ServerVersion"));
				result.append(": ");
				result.append(reader.readUTF());
				result.append("\n");

				result.append(Language.tr("Server.Status.ServerJavaVersion"));
				result.append(": ");
				result.append(reader.readUTF());
				result.append("\n");

				result.append(Language.tr("Server.Status.ServerCPUCount"));
				result.append(": ");
				result.append(reader.readInt());
				result.append("\n");

				result.append(Language.tr("Server.Status.MemoryUsage"));
				result.append(": ");
				result.append(reader.readInt());
				result.append(" MB\n");

				result.append(Language.tr("Server.Status.SimulationCount"));
				result.append(": ");
				result.append(reader.readLong());
				result.append("\n");

				result.append(Language.tr("Server.Status.RunningThreads"));
				result.append(": ");
				result.append(reader.readInt());
				result.append("\n");
			} catch (IOException e) {
				return String.format(SimulationClient.NO_CONNECT,netClient.getHost(),netClient.getPort());
			}

			return result.toString();
		} finally {
			netClient.stop();
		}
	}

	/**
	 * Zeigt die Daten über den Server als Meldungsfenster an
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Dialogs)
	 */
	public void showMessage(final Component owner) {
		MsgBox.info(owner,Language.tr("Server.Status.Title"),get());
	}
}
