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
package ui.script;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;

/**
 * Dialog zur Auswahl einer im Modell vorhandenen Variable
 * @author Alexander Herzog
 * @see ScriptPopupItemCommandVariable
 */
public class SelectVariableDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5308507430316777428L;

	/** Auswahlfeld für die verfügbaren Variablen */
	private final JComboBox<String> combo;
	/** Liste der Variablennamen */
	private final String[] variableNames;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param variableNames	Liste der Variablennamen
	 * @param help	Hilfe-Runnable
	 */
	public SelectVariableDialog(final Component owner, final String[] variableNames, final Runnable help) {
		super(owner,Language.tr("ScriptPopup.SelectVariableDialog.Title"));
		this.variableNames=variableNames;
		if (variableNames==null || variableNames.length==0) {
			MsgBox.error(owner,Language.tr("ScriptPopup.SelectVariableDialog.ErrorNoVariables.Title"),Language.tr("ScriptPopup.SelectVariableDialog.ErrorNoVariables.Info"));
			combo=null;
			return;
		}

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());
		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		setup.add(line);

		final JLabel label=new JLabel(Language.tr("ScriptPopup.SelectVariableDialog.Variable"));
		line.add(label);
		line.add(combo=new JComboBox<>(variableNames));
		combo.setSelectedIndex(0);
		label.setLabelFor(combo);

		setMinSizeRespectingScreensize(500,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Modell aus dem die Variablennamen ausgelesen werden sollen
	 * @param help	Hilfe-Runnable
	 */
	public SelectVariableDialog(final Component owner, final EditModel model, final Runnable help) {
		this(owner,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),help);
	}

	/**
	 * Liefert den gewählten Variablename.
	 * @return	Gewählter Variablenname oder <code>null</code> wenn keine Auswahl erfolgte
	 */
	public String getVariableName() {
		if (combo==null) return null;
		if (getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
		return variableNames[combo.getSelectedIndex()];
	}
}
