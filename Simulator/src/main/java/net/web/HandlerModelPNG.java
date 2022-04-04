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

import org.nanohttpd.protocols.http.IHTTPSession;

import ui.MainPanel;

/**
 * Dieser Handler liefert das Modell als png-Grafik zurück.
 * @author Alexander Herzog
 * @see WebServerHandler
 */
public class HandlerModelPNG implements WebServerHandler {
	/** Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen) */
	private final String serverURL;
	/** Hauptpanel des Simulators */
	private final MainPanel mainPanel;

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param mainPanel	Hauptpanel des Simulators
	 */
	public HandlerModelPNG(final String serverURL, final MainPanel mainPanel) {
		this.serverURL=serverURL;
		this.mainPanel=mainPanel;
	}

	@Override
	public WebServerResponse process(IHTTPSession session) {
		if (!testURL(session,serverURL)) return null;

		final BufferedImage image=mainPanel.editorPanel.getPrintImage(-1);

		final WebServerResponse response=new WebServerResponse();
		response.setPNG(image);
		return response;
	}
}