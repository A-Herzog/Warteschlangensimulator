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
package ui.modeleditor.elements;

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JPanel;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorPanel;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementDecideJS}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementDecideJS
 */
public class ModelElementDecideJSDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4081622299729502435L;

	private ScriptEditorPanel editor;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementDecideJS}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementDecideJSDialog(final Component owner, final ModelElementDecideJS element, final boolean readOnly) {
		super(owner,Language.tr("Surface.DecideJS.Dialog.Title"),element,"ModelElementDecideJS",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationDecideJS;
	}

	@Override
	protected JComponent getContentPanel() {
		if (element instanceof ModelElementDecideJS) {
			final String script=((ModelElementDecideJS)element).getScript();
			ScriptEditorPanel.ScriptMode mode;
			switch (((ModelElementDecideJS)element).getMode()) {
			case Javascript: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			case Java: mode=ScriptEditorPanel.ScriptMode.Java; break;
			default: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			}
			return editor=new ScriptEditorPanel(script,mode,readOnly,Language.tr("Surface.SetJS.Dialog.Script"),element.getModel(),helpRunnable,ScriptEditorPanel.featuresClientStationOutput);
		} else {
			return new JPanel();
		}
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
		pack();
	}

	@Override
	protected boolean checkData() {
		return editor.checkData();
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementDecideJS) {
			((ModelElementDecideJS)element).setScript(editor.getScript());
			switch (editor.getMode()) {
			case Javascript: ((ModelElementDecideJS)element).setMode(ModelElementDecideJS.ScriptMode.Javascript); break;
			case Java: ((ModelElementDecideJS)element).setMode(ModelElementDecideJS.ScriptMode.Java); break;
			}
		}
	}
}
