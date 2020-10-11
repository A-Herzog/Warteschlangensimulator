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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.JPanel;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;

/**
 * Dialog zum Bearbeiten von einem Eingabeparameter
 * für die Parameter-Vergleichs-Funktion
 * @author Alexander Herzog
 * @see ParameterCompareSetupValueInputListDialog
 */
public class ParameterCompareSetupValueInputDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -112285444307635605L;

	private final ParameterCompareSetupValueInputPanel editor;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param input	Eingabeparameter-Datensatz
	 * @param model	Editor-Modell, welches die Basis für die Parameterstudie darstellt
	 * @param help	Hilfe-Runnable
	 */
	public ParameterCompareSetupValueInputDialog(final Component owner, final ParameterCompareSetupValueInput input, final EditModel model, final Runnable help) {
		super(owner,Language.tr("ParameterCompare.Settings.Input.Title"));

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());
		content.add(editor=new ParameterCompareSetupValueInputPanel(getOwner(),input,model,help,true));

		/* Dialog starten */

		setMinSizeRespectingScreensize(600,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	@Override
	protected boolean checkData() {
		return editor.checkData(true);
	}

	@Override
	protected void storeData() {
		editor.storeData();
	}
}