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

import java.util.function.Supplier;

import org.w3c.dom.Document;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import xml.XMLTools;

/**
 * Liefert eine JSON-Datei aus.
 * @author Alexander Herzog
 * @see WebServerHandler
 */
public class HandlerJSON implements WebServerHandler {
	private final String serverURL;
	private final Supplier<Document> xmlSupplier;
	private final String downloadFileName;
	private final boolean addVariableName;

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param xmlSupplier	Supplier, der im Anfragefall das XML-Dokument liefert
	 * @param downloadFileName	Wird hier ein nicht-leerer String übergeben, so wird dem Browser mitgeteilt, dass das Dokument zum Speichern und nicht zum Anzeigen bestimmt ist und der hier angegebene Name wird als Vorschlag verwendet.
	 * @param addVariableName	Ausgabe als einfache json-Datei (<code>false</code>) oder mit vorangestellter js-Variablenzuweisung als js-Datei (<code>true</code>)
	 */
	public HandlerJSON(final String serverURL, final Supplier<Document> xmlSupplier, final String downloadFileName, final boolean addVariableName) {
		this.serverURL=serverURL;
		this.xmlSupplier=xmlSupplier;
		this.downloadFileName=downloadFileName;
		this.addVariableName=addVariableName;
	}

	@Override
	public WebServerResponse process(final IHTTPSession session) {
		if (!testURL(session,serverURL)) return null;

		final WebServerResponse response=new WebServerResponse();

		final Document xml=xmlSupplier.get();
		if (xml==null) return null;
		final String json=XMLTools.xmlToJson(xml,false,true);
		if (addVariableName) {
			response.setJS("'use strict';\n\nvar qs_model="+json,false,downloadFileName);
		} else {
			response.setJSON(json,false,downloadFileName);
		}

		return response;
	}
}