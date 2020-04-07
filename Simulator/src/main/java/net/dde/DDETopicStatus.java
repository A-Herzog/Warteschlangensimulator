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
package net.dde;

import language.Language;
import ui.AnimationPanel;
import ui.EditorPanel;
import ui.MainPanel;
import ui.statistics.StatisticsPanel;
import ui.tools.WaitPanel;

/**
 * Topic für den DDE-Server zur Abfrage des Status des Simulators
 * @author Alexander Herzog
 * @see SimulationDDEServer
 */
public class DDETopicStatus extends DDETopic {
	/**
	 * Konstruktor der Klasse
	 * @param server	DDE-Server
	 * @param mainPanel	Haupt-Panel des Simulators (aus dem ausgelesen werden kann, in welchem Status sich der Simulator befindet)
	 */
	public DDETopicStatus(final DDEServerSystem server, final MainPanel mainPanel) {
		super(server,"Status");

		final EditorPanel editorPanel=mainPanel.editorPanel;

		addItemStatic("Version",MainPanel.VERSION);
		addItemUpdateable("Model",()->(editorPanel.getModel().name.trim().isEmpty())?Language.tr("WebServer.Status.CurrentModel.NoName"):editorPanel.getModel().name);
		addItemUpdateable("Changed",()->(editorPanel.isModelChanged()?"1":"0"));
		addItemUpdateable("Mode",()->{
			String s=Language.tr("WebServer.Status.Mode.Special");
			if (mainPanel.currentPanel instanceof EditorPanel) s=Language.tr("WebServer.Status.Mode.Editor");
			if (mainPanel.currentPanel instanceof WaitPanel) s=Language.tr("WebServer.Status.Mode.Simulation");
			if (mainPanel.currentPanel instanceof StatisticsPanel) s=Language.tr("WebServer.Status.Mode.Statistics");
			if (mainPanel.currentPanel instanceof AnimationPanel) s=Language.tr("WebServer.Status.Mode.Animation");
			return s;
		});
	}
}
