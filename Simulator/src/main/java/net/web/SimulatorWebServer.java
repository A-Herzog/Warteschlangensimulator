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

import java.util.List;

import tools.SetupData;
import ui.MainPanel;

/**
 * Webserver, der auf konkrete Anfragen an den Simulator reagiert.
 * @author Alexander Herzog
 */
public class SimulatorWebServer extends WebServer {
	/**
	 * Singleton-Instanz dieser Klasse
	 * @see #getInstance(MainPanel)
	 */
	private static SimulatorWebServer instance;

	/**
	 * Referenz auf das Setup-Singleton
	 */
	private final SetupData setup;

	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse als Singleton ausgelegt und kann nicht direkt instanziert werden.
	 * Stattdessen muss die Methode {@link SimulatorWebServer#getInstance(MainPanel)} verwendet werden.
	 * @param mainPanel	Hauptpanel des Simulators
	 */
	private SimulatorWebServer(final MainPanel mainPanel) {
		super();
		setup=SetupData.getSetup();

		final List<WebServerHandler> list=getHandlersList();

		/* Allgemeines */
		list.add(new HandlerFavicon("/ui/res/Symbol.ico"));

		/* Statische Seiten */
		list.add(new HandlerText("/","res/index_%LANG%.html",this,WebServerResponse.Mime.HTML));
		list.add(new HandlerText("/help","res/help_%LANG%.html",this,WebServerResponse.Mime.HTML));
		list.add(new HandlerText("/css.css","res/css.css",this,WebServerResponse.Mime.CSS));
		list.add(new HandlerText("/main.js","res/js_%LANG%.js",this,WebServerResponse.Mime.JS));

		list.add(new HandlerText("/3d/three.min.js","res/three.min.js",this,WebServerResponse.Mime.JS));
		list.add(new HandlerText("/3d/viewer.js","res/qs3d_viewer.js",this,WebServerResponse.Mime.JS));
		list.add(new HandlerText("/3d/binary.js","res/qs3d_binary.js",this,WebServerResponse.Mime.JS));
		list.add(new HandlerText("/3d/model.js","res/qs3d_model.js",this,WebServerResponse.Mime.JS));
		list.add(new HandlerText("/3d/language.js","res/qs3d_language_%LANG%.js",this,WebServerResponse.Mime.JS));
		list.add(new HandlerText("/3d/viewer.js","res/qs3d_viewer.js",this,WebServerResponse.Mime.JS));
		list.add(new HandlerText("/3d/scene.js","res/qs3d_scene.js",this,WebServerResponse.Mime.JS));
		list.add(new HandlerText("/3d","res/qs3d_%LANG%.html",this,WebServerResponse.Mime.HTML));

		/* Status des Simulator */
		list.add(new HandlerStatus("/status",mainPanel));

		/* Modell hoch- oder runterladen */
		list.add(new HandlerXML("/xml",()->mainPanel.editorPanel.getModel().saveToXMLDocument(),"Model.xml"));
		list.add(new HandlerJSON("/json",()->mainPanel.editorPanel.getModel().saveToXMLDocument(),"Model.json",false));
		list.add(new HandlerJSON("/json_variable",()->mainPanel.editorPanel.getModel().saveToXMLDocument(),"Model.json",true));
		list.add(new HandlerPostModel("/upload",mainPanel));

		/* Modell als png-Grafik runterladen */
		list.add(new HandlerModelPNG("/image",mainPanel));

		/* Animation */
		list.add(new HandlerText("/animation.js","res/animation_%LANG%.js",this,WebServerResponse.Mime.JS));
		list.add(new HandlerAnimation("/animation","res/animation_%LANG%.html",mainPanel));
	}

	/**
	 * Liefert die Singleton-Instanz dieser Klasse
	 * @param mainPanel	Hauptpanel des Simulators
	 * @return	Instanz dieser Klasse
	 */
	public static synchronized SimulatorWebServer getInstance(final MainPanel mainPanel) {
		if (instance==null) instance=new SimulatorWebServer(mainPanel);
		return instance;
	}

	@Override
	public String start(final int port) {
		setup.webServerPort=port;
		setup.saveSetup();
		return super.start(port);
	}

	/**
	 * Liefert die beim letzten Serverstart verwendete Serverportnummer
	 * @return	Letzte Serverportnummer
	 */
	public int getLastPort() {
		return setup.webServerPort;
	}

	/**
	 * Trägt ein neues Hauptpanel in die laufenden Server ein.
	 * @param mainPanel	Hauptpanel des Simulators (für den Zugriff durch den Animations-Web-Server)
	 */
	public static synchronized void updatePanel(final MainPanel mainPanel) {
		if (instance==null) return;
		final boolean running=instance.isRunning();
		final String[] authData=instance.getAuthData();
		final String[] tlsData=instance.getTLSData();
		if (running) instance.stop();
		instance=new SimulatorWebServer(mainPanel);
		instance.setAuthData(authData[0],authData[1],authData[2]);
		instance.setTLSData(tlsData[0],tlsData[1]);
		if (running) instance.start(instance.getLastPort());
	}
}
