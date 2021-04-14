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
package net.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.w3c.dom.Document;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import xml.XMLTools;

/**
 * Liefert eine Antwort, die über den Webserver zum Anfrager zurückgeschickt werden kann.
 * @author Alexander Herzog
 * @see WebServerHandler
 */
public class WebServerResponse {
	/**
	 * Mime-Typen für die Antworten
	 * @author Alexander Herzog
	 */
	public enum Mime {
		/**
		 * Einfacher Text
		 */
		TEXT("text/plain; charset=utf-8"),

		/**
		 * HTML-Dateien (Texte)
		 */
		HTML("text/html; charset=utf-8"),

		/**
		 * CSS-Dateien (Texte)
		 */
		CSS("text/css; charset=utf-8"),

		/**
		 * JS-Dateien (Texte)
		 */
		JS("text/javascript; charset=utf-8"),

		/**
		 * XML-Dateien (Texte)
		 */
		XML("text/xml; charset=utf-8"),

		/**
		 * JSON-Dateien (Texte)
		 */
		JSON("application/json; charset=utf-8"),

		/**
		 * Icon (Binärdaten)
		 */
		ICON("image/x-icon"),

		/**
		 * png-Grafik (Binärdatei)
		 */
		PNG("image/png"),

		/**
		 * zip-Datei (Binärdatei)
		 */
		ZIP("application/zip"),

		/**
		 * tar.gz-Datei (Binärdatei)
		 */
		TARGZ("application/gzip");

		/**
		 * Bezeichner des jeweiligen Mime-Typs in Textform
		 */
		public final String text;

		/**
		 * Konstruktor der Klasse
		 * @param text	Bezeichner des jeweiligen Mime-Typs in Textform
		 */
		Mime(final String text) {
			this.text=text;
		}
	}

	/** Zu verwendender Mime-Typ */
	private Mime mime;
	/** Auszugebende Daten */
	private byte[] data;
	/** Länge der auszugebenden Daten */
	private int length;
	/** Steht hier ein nicht-leerer String, so wird dem Browser mitgeteilt, dass das Dokument zum Speichern und nicht zum Anzeigen bestimmt ist und der hier angegebene Name wird als Vorschlag verwendet. */
	private String downloadName;
	/** Rückgabecode */
	private IStatus returnCode;
	/** Optionale benutzerdefinierte HTTP-Header */
	private Map<String,String> userHeaders;

	/**
	 * Konstruktor der Klasse<br>
	 * Wird die Antwort direkt nach der Initialisierung ohne weitere Anpassungen versendet wird, so liefert Sie den http-Code 404.
	 */
	public WebServerResponse() {
		mime=null;
		returnCode=NanoHTTPD.Response.Status.OK;
		userHeaders=new HashMap<>();
	}

	/**
	 * Versucht eine Zeichenkette für die Ausgabe UTF8 zu encodieren
	 * @param text	Zeichenkette
	 * @return	UTF8-Zeichenkette
	 */
	private String utf8encode(final String text) {
		return new String(StandardCharsets.UTF_8.encode(text).array()).trim();
	}

	/**
	 * Stellt einen Text als Antwort ein.
	 * @param mime	Zu verwendendes Mime-Format
	 * @param text	Antworttext
	 * @param utf8encode	Umwandlung des Textes nach UTF8 notwendig?
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	private boolean setTextFormat(final Mime mime, final String text, final boolean utf8encode) {
		if (mime==null) return false;
		if (text==null) return false;
		this.mime=mime;
		if (utf8encode) {
			String s=utf8encode(text);
			s=s.trim();
			data=s.getBytes();
		} else {
			data=text.getBytes();
		}
		length=text.length();
		return true;
	}

	/**
	 * Stellt einen neuen Rückgabecode für die HTTP-Anfragen ein (Vorgabe ist "OK" 200).
	 * @param returnCode	Neuer Rückgabecode
	 */
	public void setReturnCode(final NanoHTTPD.Response.Status returnCode) {
		if (returnCode!=null) this.returnCode=returnCode;
	}

	/**
	 * Liefert die Zuordnung der optionalen benutzerdefinierten HTTP-Header.
	 * @return	Benutzerdefinierte HTTP-Header
	 */
	public Map<String,String> getUserHeaders() {
		return userHeaders;
	}

	/**
	 * Stellt einen Plain-Text als Antwort ein.
	 * @param text	Antworttext
	 * @param utf8encode	Umwandlung des Textes nach UTF8 notwendig?
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setText(final String text, final boolean utf8encode) {
		return setTextFormat(Mime.TEXT,text,utf8encode);
	}

	/**
	 * Stellt einen Text in einem definierbaren Format als Antwort ein.
	 * @param text	Antworttext
	 * @param mime	Zu verwendender Mime-Typ
	 * @param utf8encode	Umwandlung des Textes nach UTF8 notwendig?
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 * @see Mime
	 */
	public boolean setText(final String text, final Mime mime, final boolean utf8encode) {
		return setTextFormat(mime,text,utf8encode);
	}

	/**
	 * Stellt einen Text in einem definierbaren Format als Antwort ein.
	 * @param stream	Stream aus dem der Antworttext ausgelesen werden soll
	 * @param mime	Zu verwendender Mime-Typ
	 * @param utf8encode	Umwandlung des Textes nach UTF8 notwendig?
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 * @throws IOException	Wird ausgelöst, wenn ein Fehler beim Laden der Daten aus dem Stream aufgetreten ist.
	 */
	public boolean setText(final InputStream stream, final Mime mime, final boolean utf8encode) throws IOException {
		if (stream==null) return false;
		final int size=stream.available();
		byte[] data=new byte[size];
		int read=0;
		while (read<size) read+=stream.read(data,read,size-read);
		final String s=new String(data);
		if (utf8encode) data=utf8encode(s).getBytes();

		this.data=data;
		this.mime=mime;
		length=s.length();
		return true;
	}

	/**
	 * Stellt einen HTML-Text als Antwort ein.
	 * @param text	HTML-Antworttext
	 * @param utf8encode	Umwandlung des Textes nach UTF8 notwendig?
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setHTML(final String text, final boolean utf8encode) {
		return setTextFormat(Mime.HTML,text,utf8encode);
	}

	/**
	 * Stellt ein HTML-Dokument als Antwort ein.
	 * @param data HTML-Dokument als Antwort
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setHTML(final byte[] data) {
		if (data==null) return false;
		mime=Mime.HTML;
		this.data=data;
		length=data.length;
		return true;
	}

	/**
	 * Stellt einen CSS-Text als Antwort ein.
	 * @param text	CSS-Antworttext
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setCSS(final String text) {
		return setTextFormat(Mime.CSS,text,false);
	}

	/**
	 * Stellt einen JS-Text als Antwort ein.
	 * @param text	JS-Antworttext
	 * @param utf8encode	Umwandlung des Textes nach UTF8 notwendig?
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setJS(final String text, final boolean utf8encode) {
		return setTextFormat(Mime.JS,text,utf8encode);
	}

	/**
	 * Stellt einen JS-Text als Antwort ein.
	 * @param text	JS-Antworttext
	 * @param utf8encode	Umwandlung des Textes nach UTF8 notwendig?
	 * @param downloadFileName	Wird hier ein nicht-leerer String übergeben, so wird dem Browser mitgeteilt, dass das Dokument zum Speichern und nicht zum Anzeigen bestimmt ist und der hier angegebene Name wird als Vorschlag verwendet.
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setJS(final String text, final boolean utf8encode, final String downloadFileName) {
		downloadName=downloadFileName;
		return setTextFormat(Mime.JS,text,utf8encode);
	}

	/**
	 * Stellt einen XML-Text als Antwort ein.
	 * @param text	XML-Antworttext
	 * @param downloadFileName	Wird hier ein nicht-leerer String übergeben, so wird dem Browser mitgeteilt, dass das Dokument zum Speichern und nicht zum Anzeigen bestimmt ist und der hier angegebene Name wird als Vorschlag verwendet.
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setXML(final String text, final String downloadFileName) {
		final boolean b=setTextFormat(Mime.XML,text,false);
		if (b) this.downloadName=downloadFileName;
		return b;
	}

	/**
	 * Stellt ein XML-Dokument als Antwort ein.
	 * @param doc	XML-Dokument als Antwort
	 * @param downloadFileName	Wird hier ein nicht-leerer String übergeben, so wird dem Browser mitgeteilt, dass das Dokument zum Speichern und nicht zum Anzeigen bestimmt ist und der hier angegebene Name wird als Vorschlag verwendet.
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setXML(final Document doc, final String downloadFileName) {
		if (doc==null) return false;

		try (ByteArrayOutputStream output=new ByteArrayOutputStream()) {
			final XMLTools xml=new XMLTools(output);
			if (!xml.save(doc)) return false;
			final byte[] data=output.toByteArray();

			mime=Mime.XML;
			this.data=data;
			length=data.length;
			downloadName=downloadFileName;
		} catch (IOException e) {return false;}

		return true;
	}

	/**
	 * Stellt ein XML-Dokument als Antwort ein.
	 * @param data	Bytes des xml-Dokuments
	 * @param downloadFileName	Wird hier ein nicht-leerer String übergeben, so wird dem Browser mitgeteilt, dass das Dokument zum Speichern und nicht zum Anzeigen bestimmt ist und der hier angegebene Name wird als Vorschlag verwendet.
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setXML(final byte[] data, final String downloadFileName) {
		if (data==null) return false;
		mime=Mime.XML;
		this.data=data;
		length=data.length;
		downloadName=downloadFileName;
		return true;
	}

	/**
	 * Stellt ein JSON-Dokument als Antwort ein.
	 * @param data	Bytes des json-Dokuments
	 * @param downloadFileName	Wird hier ein nicht-leerer String übergeben, so wird dem Browser mitgeteilt, dass das Dokument zum Speichern und nicht zum Anzeigen bestimmt ist und der hier angegebene Name wird als Vorschlag verwendet.
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setJSON(final byte[] data, final String downloadFileName) {
		if (data==null) return false;
		mime=Mime.JSON;
		this.data=data;
		length=data.length;
		downloadName=downloadFileName;
		return true;
	}

	/**
	 * Stellt eine zip-Datei als Antwort ein.
	 * @param data	Bytes der zip-Datei
	 * @param downloadFileName	Wird hier ein nicht-leerer String übergeben, so wird dem Browser mitgeteilt, dass das Dokument zum Speichern und nicht zum Anzeigen bestimmt ist und der hier angegebene Name wird als Vorschlag verwendet.
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setZIP(final byte[] data, final String downloadFileName) {
		if (data==null) return false;
		mime=Mime.ZIP;
		this.data=data;
		length=data.length;
		downloadName=downloadFileName;
		return true;
	}

	/**
	 * Stellt eine tar.gz-Datei als Antwort ein.
	 * @param data	Bytes der tar.gz-Datei
	 * @param downloadFileName	Wird hier ein nicht-leerer String übergeben, so wird dem Browser mitgeteilt, dass das Dokument zum Speichern und nicht zum Anzeigen bestimmt ist und der hier angegebene Name wird als Vorschlag verwendet.
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setTARGZ(final byte[] data, final String downloadFileName) {
		if (data==null) return false;
		mime=Mime.TARGZ;
		this.data=data;
		length=data.length;
		downloadName=downloadFileName;
		return true;
	}

	/**
	 * Stellt einen JSON-Text als Antwort ein.
	 * @param text	JSON-Antworttext
	 * @param utf8encode	Umwandlung des Textes nach UTF8 notwendig?
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setJSON(final String text, final boolean utf8encode) {
		return setTextFormat(Mime.JSON,text,utf8encode);
	}

	/**
	 * Stellt einen JSON-Text als Antwort ein.
	 * @param text	JSON-Antworttext
	 * @param utf8encode	Umwandlung des Textes nach UTF8 notwendig?
	 * @param downloadFileName	Wird hier ein nicht-leerer String übergeben, so wird dem Browser mitgeteilt, dass das Dokument zum Speichern und nicht zum Anzeigen bestimmt ist und der hier angegebene Name wird als Vorschlag verwendet.
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setJSON(final String text, final boolean utf8encode, final String downloadFileName) {
		downloadName=downloadFileName;
		return setTextFormat(Mime.JSON,text,utf8encode);
	}

	/**
	 * Stellt ein Icon als Antwort ein.
	 * @param icon	Binärdaten des Icons
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setIcon(final byte[] icon) {
		if (icon==null || icon.length==0) return false;
		mime=Mime.ICON;
		data=Arrays.copyOf(icon,icon.length);
		return true;
	}

	/**
	 * Stellt ein Icon als Antwort ein.
	 * @param stream	Stream der das Icon enthält
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 * @throws IOException	Wird ausgelöst, wenn ein Fehler beim Laden der Daten aus dem Stream aufgetreten ist.
	 */
	public boolean setIcon(final InputStream stream) throws IOException {
		if (stream==null) return false;
		byte[] data=new byte[stream.available()];
		stream.read(data);

		this.data=data;
		mime=Mime.ICON;
		length=data.length;
		return true;
	}

	/**
	 * Stellt ein png-Bild aus Antwort ein.
	 * @param image	Auszugebendes Bild
	 * @return	Liefert <code>true</code>, wenn die Antwort konfiguriert werden konnte.
	 */
	public boolean setPNG(final BufferedImage image) {
		if (image==null) return false;
		try (final ByteArrayOutputStream output=new ByteArrayOutputStream()) {
			ImageIO.write(image,"png",output);
			final byte[] data=output.toByteArray();

			mime=Mime.PNG;
			this.data=data;
			length=data.length;
			return true;
		} catch (IOException e) {return false;}
	}

	/**
	 * Gibt an, ob eine Antwort eingestellt wurde.
	 * Wenn nicht, ist die Antwort ein http-Code 404.
	 * @return	Liefert <code>true</code>, wenn zuvor eine Antwort eingestellt wurde.
	 */
	public boolean isValid() {
		return mime!=null;
	}

	/**
	 * Liefert den für die Antwort eingestellten Mime-Typ.
	 * @return	Eingestellter Mime-Typ oder <code>null</code>, wenn keine Antwort konfiguriert wurde.
	 * @see Mime
	 */
	public Mime getMime() {
		return mime;
	}

	/**
	 * Liefert die eingestellte Antwort in Binärform.
	 * @return	Eingestellte Antwort in Binärform oder <code>null</code>, wenn keine Antwort konfiguriert wurde.
	 */
	public byte[] getBytes() {
		return data;
	}

	/*
	private final String getChecksum() {
		MessageDigest md;
		try {
			md=MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {return "";}

		md.update(data);
		final byte[] sha=md.digest();
		if (sha.length!=20) return "";
		return HexBin.encode(sha);
	}
	 */

	/**
	 * Formt die Antwort in eine {@link fi.iki.elonen.NanoHTTPD.Response}-Antwort um.
	 * @return	Antwort als {@link fi.iki.elonen.NanoHTTPD.Response}-Objekt
	 */
	public NanoHTTPD.Response getResponse() {
		final NanoHTTPD.Response nanoResponse;

		if (!isValid()) {
			nanoResponse=NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,Mime.TEXT.text,null,0);
			nanoResponse.addHeader("Cache-Control","no-cache, no-store, must-revalidate");
			return nanoResponse;
		}

		/* Das Senden von 304 führt dazu, dass der Client die Verbindung schließt - und NanoHTTP eine Exception schmeißt. */
		/*
		final String oldETag=session.getHeaders().get("if-none-match");
		final String newETag=getChecksum();

		if (oldETag!=null && oldETag.equals(newETag)) {
			nanoResponse=NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED,mime.text,null,0);
			nanoResponse.addHeader("Cache-Control","max-age=0");
			nanoResponse.addHeader("Etag",newETag);
			return nanoResponse;
		}
		 */

		nanoResponse=NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,mime.text,new ByteArrayInputStream(data),length);

		nanoResponse.addHeader("Cache-Control","no-cache, no-store, must-revalidate");
		/*
		nanoResponse.addHeader("Cache-Control","must-revalidate");
		nanoResponse.addHeader("Etag",newETag);
		 */
		nanoResponse.setStatus(returnCode);
		for (Map.Entry<String,String> entry: userHeaders.entrySet()) nanoResponse.addHeader(entry.getKey(),entry.getValue());

		if (downloadName!=null&& !downloadName.trim().isEmpty()) nanoResponse.addHeader("Content-Disposition","attachment; filename="+downloadName);

		return nanoResponse;
	}
}
