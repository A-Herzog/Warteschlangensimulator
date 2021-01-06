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
import java.util.Base64;
import java.util.List;
import java.util.Vector;

import fi.iki.elonen.NanoHTTPD;

/**
 * Diese Klasse kapselt einen Webserver, der
 * über bestimmte Handler auf Anfragen reagieren kann.
 * @author Alexander Herzog
 * @see WebServerHandler
 */
public class WebServer {
	/**
	 * Internes HTTP-Server-Objekt
	 */
	private ServerSystem serverSystem;

	/**
	 * Liste der momentan registrierten Handler
	 * @see #getHandlersList()
	 */
	private final List<WebServerHandler> handlers;

	/**
	 * Hinweistext für notwendige Authentifizierung
	 */
	private String authRequestInfo;

	/**
	 * Notwendiger Benutzername für Authentifizierung
	 */
	private String authName;

	/**
	 * Notwendiges Passwort für Authentifizierung
	 */
	private String authPassword;

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

	/**
	 * Stellt ein, ob eine Authentifizierung notwendig ist.
	 * @param request	Hinweistext zur Anzeige im Browser
	 * @param name	Nutzername (wird ohne Berücksichtigung der Groß- und Kleinschreibung verarbeitet)
	 * @param password	Passwort (wird mit Berücksichtigung der Groß- und Kleinschreibung verarbeitet)
	 * @see #getAuthData()
	 */
	public void setAuthData(final String request, final String name, final String password) {
		authRequestInfo=request;
		authName=name;
		authPassword=password;
	}

	/**
	 * Liefert die bislang eingestellten Authentifizierungsdaten.
	 * @return	Array aus Hinweistext, Nutzername und Passwort. Einzelne Einträge können <code>null</code> sein, das Array aus 3 Elementen ist jedoch immer ungleich <code>null</code>.
	 * @see #setAuthData(String, String, String)
	 */
	public String[] getAuthData() {
		return new String[] {
				authRequestInfo,
				authName,
				authPassword
		};
	}

	/**
	 * Internes HTTP-Server
	 * @see NanoHTTPD
	 */
	private class ServerSystem extends NanoHTTPD {
		/**
		 * Konstruktor der Klasse
		 * @param port	Portnummer auf dem der Server auf Anfragen reagieren soll
		 */
		public ServerSystem(final int port) {
			super(port);
		}

		/**
		 * Liefert eine Server-Antwort, dass Zugangsdaten benötigt werden.
		 * @return	401-Server-Antwort
		 */
		private Response getNeedAuthorizationResponse() {
			final WebServerResponse response=new WebServerResponse();
			response.setText("",true);
			response.setReturnCode(NanoHTTPD.Response.Status.UNAUTHORIZED);
			response.getUserHeaders().put("WWW-Authenticate","Basic realm=\"Access to the staging site\", charset=\"UTF-8\"");
			return response.getResponse();
		}

		/**
		 * Liefert die vom Browser gelieferten Zugangsdaten
		 * @param session	Anfrage
		 * @return	Liefert entweder ein Array aus Nutzername und Passwort oder <code>null</code>, wenn keine Zugangsdaten übermittelt wurden
		 */
		private String[] getAuthorizationData(final IHTTPSession session) {
			final String data=session.getHeaders().get("authorization");
			if (data==null) return null;
			final String[] parts=data.split(" ");
			if (parts.length!=2 || !parts[0].toLowerCase().equals("basic")) return null;
			final byte[] decoded=Base64.getDecoder().decode(parts[1]);

			final String decodedString=new String(decoded);
			final int index=decodedString.indexOf(':');
			if (index<0) return null;

			return new String[] {
					decodedString.substring(0,index),
					decodedString.substring(index+1)
			};
		}

		@Override
		public Response serve(IHTTPSession session) {
			/* Passwortabfrage */
			if (authRequestInfo!=null && authName!=null && authPassword!=null) {
				final String[] data=getAuthorizationData(session);
				if (data==null) return getNeedAuthorizationResponse();
				if (!data[0].equalsIgnoreCase(authName) || !data[1].equalsIgnoreCase(authPassword)) return getNeedAuthorizationResponse();
			}

			/* Eigentliche Verarbeitung */
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
