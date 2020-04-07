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
import java.util.function.Supplier;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import language.Language;

/**
 * Liefert eine Textdatei aus.
 * @author Alexander Herzog
 * @see WebServerHandler
 */
public class HandlerText implements WebServerHandler {
	private final String serverURL;
	private final String localURL;
	private final Object localURLBaseClass;
	private final Supplier<String> textSupplier;
	private final WebServerResponse.Mime mime;
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
	public HandlerText(final String serverURL, final Supplier<String> textSupplier, final WebServerResponse.Mime mime, final boolean utf8encode) {
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
	public HandlerText(final String serverURL, final Supplier<String> textSupplier, final WebServerResponse.Mime mime) {
		this(serverURL,textSupplier,mime,false);
	}

	@Override
	public WebServerResponse process(final IHTTPSession session) {
		if (!testURL(session,serverURL)) return null;

		WebServerResponse response=new WebServerResponse();

		if (textSupplier!=null) {
			response.setText(textSupplier.get(),mime,utf8encode);
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