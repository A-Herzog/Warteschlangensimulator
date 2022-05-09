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
 * Tutorial-Seite: Quelle hinzufügen
 * @author Alexander Herzog
 * @see TutorialPage
 * @see TutorialWindow
 */
public class PageAddSource implements TutorialPage {
	/**
	 * Konstruktor der Klasse
	 */
	public PageAddSource() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String getPageName() {
		return "AddSource";
	}

	@Override
	public String checkNextCondition(MainPanel mainPanel) {
		if (!mainPanel.editorPanel.isTemplatesVisible()) return new PageOpenTemplates().getPageName();
		if (TutorialTools.hasSource(mainPanel)) return new PageAddProcess().getPageName();
		return null;
	}
}
