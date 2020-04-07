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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.ResponseException;
import language.Language;
import simulator.editmodel.EditModel;
import ui.EditorPanel;
import ui.MainPanel;
import ui.statistics.StatisticsPanel;
import xml.XMLTools;

/**
 * Dieser Handler nimmt per http-POST ein Modell entgegen
 * und lädt dieses in den Simulator.
 * @author Alexander Herzog
 * @see WebServerHandler
 */
public class HandlerPostModel implements WebServerHandler {
	private final String serverURL;
	private final MainPanel mainPanel;

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param mainPanel	Hauptpanel des Simulators
	 */
	public HandlerPostModel(final String serverURL, final MainPanel mainPanel) {
		this.serverURL=serverURL;
		this.mainPanel=mainPanel;
	}

	private Object loadModel(final File file) {
		EditModel model;

		/* Aus XML laden */
		model=new EditModel();
		final String errorXML=model.loadFromFile(file,XMLTools.FileType.XML);
		if (errorXML==null) return model;

		/* Aus JSON laden */
		model=new EditModel();
		final String errorJSON=model.loadFromFile(file,XMLTools.FileType.JSON);
		if (errorJSON==null) return model;

		return errorXML; /* Im Zweifelsfall den XML-Fehler zurückmelden. */
	}

	private String processUpload(final IHTTPSession session) {
		if (!(mainPanel.currentPanel instanceof EditorPanel) && !(mainPanel.currentPanel instanceof StatisticsPanel)) return Language.tr("WebServer.Upload.ErrorWrongMode");
		if (mainPanel.editorPanel.isModelChanged()) return Language.tr("WebServer.Upload.ErrorModelChanged");

		try {
			final Map<String,String> files=new HashMap<>();
			session.parseBody(files);
			if (files.size()==0) {
				return Language.tr("WebServer.Upload.ErrorNoData");
			} else {
				final String fileName=files.get(files.keySet().toArray(new String[0])[0]);
				final File file=new File(fileName);
				final Object obj=loadModel(file);
				if (obj instanceof String) return (String)obj;
				mainPanel.editorPanel.setModel((EditModel)obj);
				mainPanel.setCurrentPanel(mainPanel.editorPanel);
			}
		} catch (IOException | ResponseException e) {
			return Language.tr("WebServer.Upload.ErrorInvalidData");
		}

		return Language.tr("WebServer.Upload.Success");
	}

	@Override
	public WebServerResponse process(final IHTTPSession session) {
		if (!testPostURL(session,serverURL)) return null;

		final String status=processUpload(session);

		final WebServerResponse response=new WebServerResponse();
		response.setText(status,true);
		return response;
	}
}
