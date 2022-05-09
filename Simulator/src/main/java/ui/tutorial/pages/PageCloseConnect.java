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
package ui.tutorial.pages;

import ui.MainPanel;
import ui.tutorial.TutorialPage;
import ui.tutorial.TutorialTools;
import ui.tutorial.TutorialWindow;

/**
 * Tutorial-Seite: "Kanten hinzufügen"-Funktion wieder deaktivieren
 * @author Alexander Herzog
 * @see TutorialPage
 * @see TutorialWindow
 */
public class PageCloseConnect implements TutorialPage {
	/**
	 * Konstruktor der Klasse
	 */
	public PageCloseConnect() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String getPageName() {
		return "CloseConnect";
	}

	@Override
	public String checkNextCondition(MainPanel mainPanel) {
		if (!TutorialTools.hasSource(mainPanel)) return new PageAddSource().getPageName();
		if (!TutorialTools.hasProcess(mainPanel)) return new PageAddProcess().getPageName();
		if (!TutorialTools.hasDispose(mainPanel)) return new PageAddDispose().getPageName();
		if (!TutorialTools.connect1(mainPanel)) return new PageConnect1().getPageName();
		if (!TutorialTools.connect2(mainPanel)) return new PageConnect2().getPageName();
		if (!mainPanel.editorPanel.isAddEdgeActive()) return new PageSetupSource().getPageName();
		return null;
	}
}
