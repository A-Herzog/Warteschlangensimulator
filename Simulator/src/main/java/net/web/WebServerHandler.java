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

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;

/**
 * Dieses Interface ermöglicht es, auf eine bestimmte Anfrage an den Webserver zu reagieren.
 * @author Alexander Herzog
 * @see WebServer#getHandlersList()
 */
public interface WebServerHandler {
	/**
	 * Prüft, ob eine bestimmte Anfrage durch diese Klasse bearbeitet werden kann und
	 * liefert, wenn ja, eine Antwort
	 * @param session	Anfragedaten
	 * @return	Antwort oder <code>null</code> wenn dieser Handler für diese Anfrage nicht zuständig ist.
	 * @see WebServerResponse
	 */
	WebServerResponse process(final IHTTPSession session);

	/**
	 * Prüft ob die Anfrage-URL einer behandelbaren URL entspricht
	 * @param url	Anfrage-URL
	 * @param test	URL, die durch den Handler bedient werden kann (startet üblicherweise mit "/")
	 * @return	Gibt <code>true</code> zurück, wenn die Anfrage-URL der bedienbaren URL entspricht
	 */
	default boolean testURL(String url, final String test) {
		if (url==null) return false;
		url=url.trim();
		return url.equalsIgnoreCase(test);
	}

	/**
	 * Prüft ob die Anfrage-URL einer behandelbaren URL entspricht und vom Anfragetyp "Get" ist
	 * @param session	Anfrage
	 * @param test	URL, die durch den Handler bedient werden kann (startet üblicherweise mit "/")
	 * @return	Gibt <code>true</code> zurück, wenn die Anfrage-URL der bedienbaren URL entspricht
	 */
	default boolean testURL(final IHTTPSession session, final String test) {
		if (session.getMethod()!=NanoHTTPD.Method.GET) return false;
		return testURL(session.getUri(),test);
	}

	/**
	 * Prüft ob die Anfrage-URL einer behandelbaren URL entspricht und vom Anfragetyp "Post" ist
	 * @param session	Anfrage
	 * @param test	URL, die durch den Handler bedient werden kann (startet üblicherweise mit "/")
	 * @return	Gibt <code>true</code> zurück, wenn die Anfrage-URL der bedienbaren URL entspricht
	 */
	default boolean testPostURL(final IHTTPSession session, final String test) {
		if (session.getMethod()!=NanoHTTPD.Method.POST) return false;
		return testURL(session.getUri(),test);
	}
}
