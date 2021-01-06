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

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import language.Language;

/**
 * Liefert eine Textdatei aus.
 * @author Alexander Herzog
 * @see WebServerHandler
 */
public class HandlerText implements WebServerHandler {
	/** Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen) */
	private final String serverURL;
	/** Pfad zu dem Dokument aus Java-Ressourcen-Sicht ("%LANG%" wird durch die aktuelle Sprache, also "de" oder "en" ersetzt) */
	private final String localURL;
	/** Basisklasse zu der <code>localURL</code> relativ aufgefasst werden soll */
	private final Object localURLBaseClass;
	/** Supplier, der im Anfragefall den Text liefert */
	private final Function<String,String> textSupplier;
	/** Mime-Typ des Dokuments */
	private final WebServerResponse.Mime mime;
	/** Gibt an, ob der Text noch ins UTF8-Format konvertiert werden muss */
	private final boolean utf8encode;

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param localURL	Pfad zu dem Dokument aus Java-Ressourcen-Sicht ("%LANG%" wird durch die aktuelle Sprache, also "de" oder "en" ersetzt)
	 * @param localURLBaseClass	Basisklasse zu der <code>localURL</code> relativ aufgefasst werden soll
	 * @param mime	Mime-Typ des Dokuments
	 * @param utf8encode	Gibt an, ob der Text noch ins UTF8-Format konvertiert werden muss
	 * @see WebServerResponse.Mime
	 */
	public HandlerText(final String serverURL, final String localURL, final Object localURLBaseClass, final WebServerResponse.Mime mime, final boolean utf8encode) {
		this.serverURL=serverURL;
		this.localURL=localURL;
		this.localURLBaseClass=localURLBaseClass;
		textSupplier=null;
		this.mime=mime;
		this.utf8encode=utf8encode;
	}

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param localURL	Pfad zu dem Dokument aus Java-Ressourcen-Sicht ("%LANG%" wird durch die aktuelle Sprache, also "de" oder "en" ersetzt)
	 * @param localURLBaseClass	Basisklasse zu der <code>localURL</code> relativ aufgefasst werden soll
	 * @param mime	Mime-Typ des Dokuments
	 * @see WebServerResponse.Mime
	 */
	public HandlerText(final String serverURL, final String localURL, final Object localURLBaseClass, final WebServerResponse.Mime mime) {
		this(serverURL,localURL,localURLBaseClass,mime,false);
	}

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param textSupplier	Supplier, der im Anfragefall den Text liefert
	 * @param mime	Mime-Typ des Dokuments
	 * @param utf8encode	Gibt an, ob der Text noch ins UTF8-Format konvertiert werden muss
	 * @see WebServerResponse.Mime
	 */
	public HandlerText(final String serverURL, final Function<String,String> textSupplier, final WebServerResponse.Mime mime, final boolean utf8encode) {
		this.serverURL=serverURL;
		localURL=null;
		localURLBaseClass=null;
		this.textSupplier=textSupplier;
		this.mime=mime;
		this.utf8encode=utf8encode;
	}

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param textSupplier	Supplier, der im Anfragefall den Text liefert
	 * @param mime	Mime-Typ des Dokuments
	 * @see WebServerResponse.Mime
	 */
	public HandlerText(final String serverURL, final Function<String,String> textSupplier, final WebServerResponse.Mime mime) {
		this(serverURL,textSupplier,mime,false);
	}

	@Override
	public WebServerResponse process(final IHTTPSession session) {
		if (!testURL(session,serverURL)) return null;
		final String parameters=session.getQueryParameterString();

		final WebServerResponse response=new WebServerResponse();

		if (textSupplier!=null) {
			response.setText(textSupplier.apply(parameters),mime,utf8encode);
		}

		if (localURL!=null) {
			final String url=localURL.replace("%LANG%",Language.getCurrentLanguage());
			try (final InputStream stream=localURLBaseClass.getClass().getResourceAsStream(url)) {
				response.setText(stream,mime,utf8encode);
			} catch (IOException e) {}
		}

		return response;
	}
}