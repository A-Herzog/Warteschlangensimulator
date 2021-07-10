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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import simulator.editmodel.EditModel;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.elements.VariablesTableModel;

/**
 * Dialogseite "Initiale Variablenwerte"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageInitialValues extends ModelPropertiesDialogPage {
	/** Datenmodell für die Liste der initialen Variablenwerte */
	private VariablesTableModel variablesTableModel;

	/** Art der Erfassung der Werte der globalen Variablen in der Statistik */
	private JComboBox<String> variableRecording;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPageInitialValues(ModelPropertiesDialog dialog, EditModel model, boolean readOnly, Runnable help) {
		super(dialog,model,readOnly,help);
	}

	@Override
	public void build(JPanel content) {
		final Object[] data=VariablesTableModel.buildTable(model,readOnly,help);
		content.add((JScrollPane)data[0],BorderLayout.CENTER);
		variablesTableModel=(VariablesTableModel)data[1];

		final JPanel setup=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(setup,BorderLayout.SOUTH);
		final JLabel label=new JLabel(Language.tr("Editor.Dialog.Tab.InitialVariableValues.RecordVariables")+":");
		setup.add(label);
		setup.add(variableRecording=new JComboBox<>(new String[] {
				Language.tr("Editor.Dialog.Tab.InitialVariableValues.RecordVariables.Off"),
				Language.tr("Editor.Dialog.Tab.InitialVariableValues.RecordVariables.Variables"),
				Language.tr("Editor.Dialog.Tab.InitialVariableValues.RecordVariables.VariablesAndMaps")
		}));
		variableRecording.setRenderer(new IconListCellRenderer(new Images[] {
				Images.GENERAL_OFF,
				Images.EXPRESSION_BUILDER_VARIABLE,
				Images.SCRIPT_MAP
		}));
		label.setLabelFor(variableRecording);
		switch (model.variableRecord) {
		case OFF: variableRecording.setSelectedIndex(0); break;
		case VARIABLES: variableRecording.setSelectedIndex(1); break;
		case MAPS_VARIABLES: variableRecording.setSelectedIndex(2); break;
		default: variableRecording.setSelectedIndex(0); break;
		}
	}

	@Override
	public void storeData() {
		variablesTableModel.storeData();
		switch (variableRecording.getSelectedIndex()) {
		case 0: model.variableRecord=EditModel.VariableRecord.OFF; break;
		case 1: model.variableRecord=EditModel.VariableRecord.VARIABLES; break;
		case 2: model.variableRecord=EditModel.VariableRecord.MAPS_VARIABLES; break;
		}
	}
}
