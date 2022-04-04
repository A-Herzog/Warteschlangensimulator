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

import org.nanohttpd.protocols.http.IHTTPSession;

/**
 * Liefert das Favicon für die Website
 * @author Alexander Herzog
 * @see WebServerHandler
 */
public class HandlerFavicon implements WebServerHandler {
	/** Adresse der auszuliefernden ico-Datei bezogen auf die Java-Ressourcen-Verzeichnisstruktur */
	private final String iconResourceURL;

	/**
	 * Konstruktor der Klasse
	 * @param iconResourceURL	Adresse der auszuliefernden ico-Datei bezogen auf die Java-Ressourcen-Verzeichnisstruktur
	 */
	public HandlerFavicon(final String iconResourceURL) {
		this.iconResourceURL=iconResourceURL;
	}

	@Override
	public WebServerResponse process(IHTTPSession session) {
		if (!testURL(session,"/favicon.ico")) return null;

		final WebServerResponse response=new WebServerResponse();

		try (final InputStream stream=getClass().getResourceAsStream(iconResourceURL)) {
			response.setIcon(stream);
		} catch (IOException e) {}

		return response;
	}
}