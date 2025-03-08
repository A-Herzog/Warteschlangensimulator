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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import mathtools.Table;
import systemtools.BaseDialog;
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
		if (content==null || content.isBlank()) return null;

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
		try (BufferedInputStream in=new BufferedInputStream(new URI(url).toURL().openStream())) {
			final byte dataBuffer[]=new byte[1024*128];
			int bytesRead;
			while ((bytesRead=in.read(dataBuffer,0,dataBuffer.length))!=-1) {
				output.write(dataBuffer,0,bytesRead);
			}
		} catch (IOException | URISyntaxException e) {
			return null;
		}

		return output.toByteArray();
	}

	/**
	 * Übersetzt eine GitHub-Datei-Webseiten-URL in eine GitHub-Raw-Datei-URL
	 * @param url	Zu prüfende und ggf. zu übersetzende URL
	 * @return	Passt die URL auf das Schema, so wird diese umgebaut und das Ergebnis zurückgeliefert
	 */
	private String processGitHubFile(final String url) {
		/*
		 * Übersetzt eine GitHub-Datei-URL in eine Raw-URL:
		 * https://github.com/<user>/<repo>/blob/<branch>/<path> -> https://raw.githubusercontent.com/<user>/<repo>/<branch>/<path>
		 */

		/* Bisherige URL zerlegen und prüfen */

		final String[] parts=url.toString().split("/");
		if (parts.length<8) return null;

		if (!parts[0].equalsIgnoreCase("https:")) return null;
		if (!parts[1].isEmpty()) return null;
		if (!parts[2].equalsIgnoreCase("github.com")) return null;
		final String user=parts[3]; if (user.isEmpty()) return null;
		final String repo=parts[4]; if (repo.isEmpty()) return null;
		if (!parts[5].equalsIgnoreCase("blob")) return null;
		final String branch=parts[6]; if (branch.isEmpty()) return null;

		/* Neue URL erstellen */

		final List<String> rawURL=new ArrayList<>();
		rawURL.add("https:");
		rawURL.add("");
		rawURL.add("raw.githubusercontent.com");
		rawURL.add(user);
		rawURL.add(repo);
		rawURL.add(branch);

		for (int i=7;i<parts.length;i++) rawURL.add(parts[i]);

		return String.join("/",rawURL);
	}

	/**
	 * Übersetzt eine GitHub-Verzeichnis-Webseiten-URL in eine JSON-Datei
	 * und bietet einen Eintrag daraus zum Download an.
	 * @param parent	Übergeordnetes Element (zur Ausrichtung von Dialogen)
	 * @param errorMessageOnFail	Soll im Falle eines Fehlers eine Meldung ausgegeben werden?
	 * @param url	Zu prüfende und ggf. zu übersetzende URL
	 * @return	Passt die URL auf das Schema, so wird diese umgebaut und das Ergebnis zurückgeliefert
	 */
	private String processGitHubFolder(final Component parent, final boolean errorMessageOnFail, final String url) {
		/*
		 * Übersetzt eine GitHub-Verzeichnis-URL in eine JSON-URL:
		 * https://github.com/<user>/<repo>/tree/<branch>/<path> -> https://api.github.com/repos/<user>/<repo>/contents/<path>
		 */

		/* Bisherige URL zerlegen und prüfen */

		final String[] parts=url.toString().split("/");
		if (parts.length<8) return null;

		if (!parts[0].equalsIgnoreCase("https:")) return null;
		if (!parts[1].isEmpty()) return null;
		if (!parts[2].equalsIgnoreCase("github.com")) return null;
		final String user=parts[3]; if (user.isEmpty()) return null;
		final String repo=parts[4]; if (repo.isEmpty()) return null;
		if (!parts[5].equalsIgnoreCase("tree")) return null;
		final String branch=parts[6]; if (branch.isEmpty()) return null;

		/* Neue URL erstellen */

		final List<String> rawURL=new ArrayList<>();
		rawURL.add("https:");
		rawURL.add("");
		rawURL.add("api.github.com");
		rawURL.add("repos");
		rawURL.add(user);
		rawURL.add(repo);
		rawURL.add("contents");

		for (int i=7;i<parts.length;i++) rawURL.add(parts[i]);

		final String jsonUrl=String.join("/",rawURL);

		/* Sicherheitsfrage */

		if (!confirmAccess(parent,jsonUrl)) return null;

		/* JSON laden */

		final byte[] data=downloadFile(parent,errorMessageOnFail,jsonUrl);
		if (data==null) return null;

		/* Daten verarbeiten */
		final URLLoaderGitHubFolder apiProcessor=new URLLoaderGitHubFolder(data);
		final List<URLLoaderGitHubFolder.FileRecord> records=apiProcessor.process(user,repo,branch);
		if (records==null) return null;

		/* Nur ein einzelner Verzeichniseintrag ? */
		if (records.size()==1) return records.get(0).url;

		/* Auswahldialog anzeigen */
		final URLLoaderGitHubFolderDialog dialog=new URLLoaderGitHubFolderDialog(parent,records);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) return dialog.getSelectedURL();

		return null;
	}

	/**
	 * Fragt, ob ein Internetzugriff in Ordnung ist.
	 * @param parent	Übergeordnetes Element (zur Ausrichtung von Dialogen)
	 * @param url	URL die aufgerufen werden soll
	 * @return	Liefert <code>true</code>, wenn der Aufruf freigegeben wurde
	 */
	private boolean confirmAccess(final Component parent, final String url) {
		return MsgBox.confirm(parent,Language.tr("URLLoader.Confirm.Title"),String.format(Language.tr("URLLoader.Confirm.Info"),url),Language.tr("URLLoader.Confirm.InfoYes"),Language.tr("URLLoader.Confirm.InfoNo"));
	}

	/**
	 * Führt einen Download durch
	 * @param parent	Übergeordnetes Element (zur Ausrichtung von Dialogen)
	 * @param errorMessageOnFail	Soll im Falle eines Fehlers eine Meldung ausgegeben werden?
	 * @param url	Adresse der herunter zu ladenden Datei
	 * @return	Liefert im Erfolgsfall die geladenen Daten, sonst <code>null</code>
	 */
	private byte[] downloadFile(final Component parent, final boolean errorMessageOnFail, final String url) {
		final byte[] data=WaitDialog.workBytes(parent,()->loadData(url),WaitDialog.Mode.DOWNLOAD_FILE);
		if (data==null && errorMessageOnFail) {
			MsgBox.error(parent,Language.tr("URLLoader.Error.Title"),String.format(Language.tr("URLLoader.Error.InfoLoad"),url));
		}
		return data;
	}

	/**
	 * Führt die eigentliche Verarbeitung aus.
	 * @param parent	Übergeordnetes Element (zur Ausrichtung von Dialogen)
	 * @param errorMessageOnFail	Soll im Falle eines Fehlers eine Meldung ausgegeben werden?
	 * @return	Liefert im Erfolgsfall die geladenen Daten, sonst <code>null</code>
	 */
	public byte[] process(final Component parent, final boolean errorMessageOnFail) {
		/* URL-Datei interpretieren */
		String url=getURL(urlFile);
		if (url==null || url.isEmpty()) {
			if (errorMessageOnFail) {
				MsgBox.error(parent,Language.tr("URLLoader.Error.Title"),Language.tr("URLLoader.Error.InfoURLFile"));
			}
			return null;
		}

		/* GitHub-Datei-URL umsetzen */
		final String gitHubURL=processGitHubFile(url);
		if (gitHubURL!=null) url=gitHubURL;

		/* GitHub-Verzeichnis-URL interpretieren */
		final String gitHubFileFromFolder=processGitHubFolder(parent,errorMessageOnFail,url);
		if (gitHubFileFromFolder!=null) {
			/* Sicherheitsfrage ist schon erfolgt - Datei von URL laden */
			return downloadFile(parent,errorMessageOnFail,gitHubFileFromFolder);
		}

		/* Sicherheitsfrage */
		if (!confirmAccess(parent,url)) return null;

		/* Datei von URL laden */
		return downloadFile(parent,errorMessageOnFail,url);
	}
}
