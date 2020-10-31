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
import java.util.List;
import java.util.Vector;

import fi.iki.elonen.NanoHTTPD;
import simulator.editmodel.EditModel;

/**
 * Diese Klasse kapselt einen Webserver, der
 * über bestimmte Handler auf Anfragen reagieren kann.
 * @author Alexander Herzog
 * @see WebServerHandler
 */
public class WebServer {
	private ServerSystem serverSystem;

	/**
	 * Liste der momentan registrierten Handler
	 * @see #getHandlersList()
	 */
	private final List<WebServerHandler> handlers;

	/**
	 * Konstruktor der Klasse
	 */
	public WebServer() {
		handlers=new Vector<>();
	}

	/**
	 * Liefert die Liste der momentan registrierten Handler.
	 * @return	Liste der momentan registrierten Handler
	 * @see WebServerHandler
	 */
	public List<WebServerHandler> getHandlersList() {
		return handlers;
	}

	private class ServerSystem extends NanoHTTPD {
		public ServerSystem(final int port) {
			super(port);
		}

		public ServerSystem(final int port, final EditModel model) {
			super(port);
		}

		@Override
		public Response serve(IHTTPSession session) {
			for (WebServerHandler handler: handlers) {
				final WebServerResponse response=handler.process(session);
				if (response!=null) return response.getResponse();
			}
			return new WebServerResponse().getResponse();
		}
	}

	/**
	 * Startet den Webserver (sofern er nichts bereits läuft)
	 * @param port	Zu verwendender Port
	 * @return	Liefert <code>true</code>, wenn der Server gestartet werden konnte, oder <code>false</code>, wenn das Starten fehlgeschlagen ist oder der Server bereits läuft.
	 */
	public boolean start(final int port) {
		if (serverSystem!=null) return false;
		serverSystem=new ServerSystem(port);
		try {
			serverSystem.start();
		} catch (IOException e) {
			serverSystem=null;
			return false;
		}
		return true;
	}

	/**
	 * Stoppt den Webserver (sofern er läuft)
	 * @return	Liefert <code>true</code>, wenn der Server gestoppt werden konnte, oder <code>false</code>, wenn der Server nicht lief.
	 */
	public boolean stop() {
		if (serverSystem==null) return false;
		serverSystem.stop();
		serverSystem=null;
		return true;
	}

	/**
	 * Gibt an, ob der Server momentan läuft.
	 * @return	Liefert <code>true</code>, wenn der Server momentan aktiv ist.
	 */
	public boolean isRunning() {
		return serverSystem!=null;
	}

	/**
	 * Liefert, sofern der Server momentan läuft, den Port.
	 * @return	Port des Servers oder -1, wenn der Server nicht läuft.
	 */
	public int getRunningPort() {
		if (serverSystem==null) return -1;
		return serverSystem.getListeningPort();
	}
}
