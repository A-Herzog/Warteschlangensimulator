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
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;
import ui.tutorial.TutorialPage;
import ui.tutorial.TutorialTools;
import ui.tutorial.TutorialWindow;

/**
 * Tutorial-Seite: Abschluss
 * @author Alexander Herzog
 * @see TutorialPage
 * @see TutorialWindow
 */
public class PageDone implements TutorialPage {
	/**
	 * Konstruktor der Klasse
	 */
	public PageDone() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String getPageName() {
		return "Done";
	}

	@Override
	public String checkNextCondition(MainPanel mainPanel) {
		if (!TutorialTools.hasSource(mainPanel)) return new PageAddSource().getPageName();
		if (!TutorialTools.hasProcess(mainPanel)) return new PageAddProcess().getPageName();
		if (!TutorialTools.hasDispose(mainPanel)) return new PageAddDispose().getPageName();
		if (!TutorialTools.connect1(mainPanel)) return new PageConnect1().getPageName();
		if (!TutorialTools.connect2(mainPanel)) return new PageConnect2().getPageName();

		final ModelElementSource source=TutorialTools.getSource(mainPanel);
		if (!PageSetupSource.testSource(source)) return new PageSetupSource().getPageName();

		final ModelElementProcess process=TutorialTools.getProcess(mainPanel);
		if (!PageSetupProcess.testProcess(process,mainPanel)) return new PageSetupProcess().getPageName();

		return null;
	}
}