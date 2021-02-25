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
package tools;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import language.Language;
import mathtools.Table;
import systemtools.MsgBox;
import ui.dialogs.WaitDialog;

/**
 * Diese Klasse interpretiert eine URL-Datei und ermöglicht
 * das Laden der in dieser angegebenen Adresse.
 * @author Alexander Herzog
 */
public class URLLoader {
	/**
	 * Zu verarbeitende URL-Datei
	 */
	private final File urlFile;

	/**
	 * Konstruktor der Klasse
	 * @param urlFile	Zu verarbeitende URL-Datei
	 */
	public URLLoader(final File urlFile) {
		this.urlFile=urlFile;
	}

	/**
	 * Lädt eine URL-Datei und extrahiert die darin enthaltene URL
	 * @param urlFile	URL-Datei
	 * @return	Liefert im Erfolgsfall die enthaltene URL, sonst <code>null</code>
	 */
	private static String getURL(final File urlFile) {
		if (urlFile==null || !urlFile.isFile()) return null;

		final String content=Table.loadTextFromFile(urlFile);
		if (content==null || content.trim().isEmpty()) return null;

		final String[] lines=content.split("\n");

		boolean inInternetShortcutBlock=false;
		for (String line: lines) {
			final String s=line.trim();

			if (s.startsWith("[") && s.endsWith("]")) {
				final String sub=s.substring(1,s.length()-1);
				inInternetShortcutBlock=sub.equalsIgnoreCase("InternetShortcut");
				continue;
			}

			if (inInternetShortcutBlock && s.startsWith("URL=")) {
				return s.substring(4).trim();
			}
		}

		return null;
	}

	/**
	 * Lädt die Daten von einer URL aus dem Netz.
	 * @param url	URL von der die Daten geladen werden soll
	 * @return	Liefert im Erfolgsfall die geladenen Daten, sonst <code>null</code>
	 */
	private static byte[] loadData(final String url) {
		final ByteArrayOutputStream output=new ByteArrayOutputStream();
		try (BufferedInputStream in=new BufferedInputStream(new URL(url).openStream())) {
			final byte dataBuffer[]=new byte[1024*128];
			int bytesRead;
			while ((bytesRead=in.read(dataBuffer,0,dataBuffer.length))!=-1) {
				output.write(dataBuffer,0,bytesRead);
			}
		} catch (IOException e) {
			return null;
		}

		return output.toByteArray();
	}

	/**
	 * Führt die eigentliche Verarbeitung aus.
	 * @param parent	Übergeordnetes Element (zur Ausrichtung von Dialogen)
	 * @param errorMessageOnFail	Soll im Falle eines Fehlers eine Meldung ausgegeben werden?
	 * @return	Liefert im Erfolgsfall die geladenen Daten, sonst <code>null</code>
	 */
	public byte[] process(final Component parent, final boolean errorMessageOnFail) {
		/* URL-Datei interpretieren */
		final String url=getURL(urlFile);
		if (url==null || url.isEmpty()) {
			if (errorMessageOnFail) {
				MsgBox.error(parent,Language.tr("URLLoader.Error.Title"),Language.tr("URLLoader.Error.InfoURLFile"));
			}
			return null;
		}

		/* Sicherheitsfrage */
		if (!MsgBox.confirm(parent,Language.tr("URLLoader.Confirm.Title"),String.format(Language.tr("URLLoader.Confirm.Info"),url),Language.tr("URLLoader.Confirm.InfoYes"),Language.tr("URLLoader.Confirm.InfoNo"))) return null;

		/* Datei von URL laden */
		final byte[] data=WaitDialog.workBytes(parent,()->loadData(url),WaitDialog.Mode.DOWNLOAD_FILE);
		if (data==null) {
			if (errorMessageOnFail) {
				MsgBox.error(parent,Language.tr("URLLoader.Error.Title"),String.format(Language.tr("URLLoader.Error.InfoLoad"),url));
			}
			return null;
		}
		return data;
	}
}
