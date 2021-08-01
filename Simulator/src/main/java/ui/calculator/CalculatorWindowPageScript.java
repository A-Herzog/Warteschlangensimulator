/**
 * Copyright 2021 Alexander Herzog
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
package ui.calculator;

import java.awt.Window;
import java.io.Serializable;

import javax.swing.JTabbedPane;

import language.Language;
import tools.SetupData;
import ui.images.Images;
import ui.scriptrunner.JSModelRunnerPanel;

/**
 * Skript-Tab innerhalb des Rechner-Fensters
 * @author Alexander Herzog
 * @see CalculatorWindow
 */
public class CalculatorWindowPageScript extends CalculatorWindowPage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=6164790577785191308L;

	/** Script-Editor */
	private final JSModelRunnerPanel scriptEditor;

	/**
	 * Konstruktor der Klasse
	 * @param window	Gesamtes Fenster
	 * @param tabs	Tabs-Element in das dieses Tab eingefügt werden soll
	 */
	public CalculatorWindowPageScript(final Window window, final JTabbedPane tabs) {
		super(tabs);

		add(scriptEditor=new JSModelRunnerPanel(window,null,null,null,false));
		scriptEditor.setScript(SetupData.getSetup().scriptCalculator);
	}

	@Override
	protected String getTabTitle() {
		return Language.tr("CalculatorDialog.Tab.Skript");
	}

	@Override
	protected Images getTabIcon() {
		return Images.EXTRAS_CALCULATOR_SCRIPT;
	}

	@Override
	public void storeData() {
		final SetupData setup=SetupData.getSetup();
		setup.scriptCalculator=scriptEditor.getScript();
		setup.saveSetup();
	}
}
