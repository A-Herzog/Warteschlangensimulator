/**
 * Copyright 2024 Alexander Herzog
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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import tools.JTableExt;

/**
 * Zeigt einen Dialog an, über den vor dem Start einer Simulation oder Animation
 * die initialen Werte der globalen Variablen aktualisiert werden können.
 */
public class AskForVariableValuesDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-7092432601292356045L;

	/**
	 * Datenmodell der Editor-Tabelle für die Variablenwerte
	 */
	private final AskForVariableValuesTableModel tableModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Modell in dem die initialen Variablenwerte aktualisiert werden sollen
	 */
	public AskForVariableValuesDialog(final Component owner, final EditModel model) {
		super(owner,Language.tr("Editor.Dialog.Tab.InitialVariableValues.UpdateValues"));

		/* GUI */
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		/* Tabelle */
		final JTableExt table=new JTableExt();
		content.add(new JScrollPane(table),BorderLayout.CENTER);
		table.setModel(tableModel=new AskForVariableValuesTableModel(table,model));
		table.getColumnModel().getColumn(0).setMaxWidth(125);
		table.getColumnModel().getColumn(0).setMinWidth(125);
		table.getTableHeader().setReorderingAllowed(false);
		table.putClientProperty("terminateEditOnFocusLost",true);

		/* Dialog starten */
		setResizable(true);
		setMinSizeRespectingScreensize(500,400);
		setSizeRespectingScreensize(500,400);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Prüft, ob der Dialog in Bezug auf das aktuelle Modell überhaupt angezeigt werden muss.
	 * @param model	Modell in dem die globalen Variablen untersucht werden sollen
	 * @return	Liefert <code>true</code>, wenn für mindestens eine globale Variable eingestellt ist, dass deren Wert vor dem Start einer Simulation oder Animation aktualisiert werden soll
	 */
	public static boolean dialogNeeded(final EditModel model) {
		return model.globalVariables.stream().filter(variable->variable.isAskForValueOnStart()).findFirst().isPresent();
	}

	@Override
	protected boolean checkData() {
		return tableModel.checkData();
	}

	@Override
	protected void storeData() {
		tableModel.storeData();
	}
}
