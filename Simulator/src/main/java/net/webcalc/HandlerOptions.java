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
package net.webcalc;

import java.util.function.Function;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.request.Method;

import net.web.WebServerHandler;
import net.web.WebServerResponse;

/**
 * Dieser Handler Verarbeitet eine http-OPTIONS Anfrage.
 * @author Alexander Herzog
 * @see CalcWebServer
 */
public class HandlerOptions implements WebServerHandler {
	/** Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen und auch enden) */
	private final String serverURL;
	/** Verarbeitung in Abhängigkeit von dem Text nach der <code>serverURL</code> */
	private final Function<String,WebServerResponse> process;

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen und auch enden)
	 * @param process	Verarbeitung in Abhängigkeit von dem Text nach der <code>serverURL</code>
	 */
	public HandlerOptions(final String serverURL, final Function<String,WebServerResponse> process) {
		if (serverURL.endsWith("/")) this.serverURL=serverURL; else this.serverURL=serverURL+"/";
		this.process=process;
	}

	@Override
	public WebServerResponse process(IHTTPSession session) {
		if (session.getMethod()!=Method.OPTIONS) return null;
		final String uri=session.getUri();
		if (uri.length()<=serverURL.length()) return null;
		if (!uri.substring(0,serverURL.length()).equalsIgnoreCase(serverURL)) return null;
		final String request=uri.substring(serverURL.length());
		return process.apply(request);
	}

}
