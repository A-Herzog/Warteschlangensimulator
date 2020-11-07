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

import language.Language;
import simulator.editmodel.EditModel;
import ui.AnimationPanel;
import ui.EditorPanel;
import ui.MainPanel;
import ui.statistics.StatisticsPanel;
import ui.tools.WaitPanel;

/**
 * Liefert den Betriebsstatus des Simulators als HTML-Schnippselt zurück.
 * @author Alexander Herzog
 * @see WebServerHandler
 */
public class HandlerStatus extends HandlerText {
	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param mainPanel	Hauptpanel des Simulators
	 */
	public HandlerStatus(final String serverURL, final MainPanel mainPanel) {
		super(serverURL,()->getStatus(mainPanel),WebServerResponse.Mime.JSON,true);
	}

	/**
	 * Escaped doppelte Anführungszeichen
	 * @param text	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private static String escapeString(final String text) {
		return text.replace("\"","\\\"");
	}

	/**
	 * Liefert ein json-Objekt mit Daten zu Modell und Simulator zurück.
	 * @param mainPanel	Hauptpanel des Simulators
	 * @return	json-Objekt mit Daten zu Modell und Simulator zurück
	 */
	private static String getStatus(final MainPanel mainPanel) {
		final EditorPanel editorPanel=mainPanel.editorPanel;
		final EditModel model=editorPanel.getModel();

		final StringBuilder sb=new StringBuilder();
		sb.append("{\n");

		sb.append("  \"version\": \""+MainPanel.VERSION+"\",\n");
		sb.append("  \"model\": \""+(model.name.trim().isEmpty()?Language.tr("WebServer.Status.CurrentModel.NoName"):escapeString(model.name))+"\",\n");
		sb.append("  \"changed\": \""+(editorPanel.isModelChanged()?"1":"0")+"\",\n");

		String s=Language.tr("WebServer.Status.Mode.Special");
		if (mainPanel.currentPanel instanceof EditorPanel) s=Language.tr("WebServer.Status.Mode.Editor");
		if (mainPanel.currentPanel instanceof WaitPanel) s=Language.tr("WebServer.Status.Mode.Simulation");
		if (mainPanel.currentPanel instanceof StatisticsPanel) s=Language.tr("WebServer.Status.Mode.Statistics");
		if (mainPanel.currentPanel instanceof AnimationPanel) s=Language.tr("WebServer.Status.Mode.Animation");
		sb.append("  \"mode\": \""+s+"\"\n");

		sb.append("}\n");

		return sb.toString();
	}
}
