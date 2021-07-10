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

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import simulator.editmodel.EditModel;
import tools.JTableExt;
import ui.modeleditor.ModelTransporters;

/**
 * Dialogseite "Transporter"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageTransporters extends ModelPropertiesDialogPage {
	/** Objekt zum lokalen Speichern der Transporter (außerhalb des Modells, in das sie erst beim Schließen des Dialogs zurückgeschrieben werden */
	private final ModelTransporters localTransporters;

	/** Datenmodell für die Liste der Transporter */
	private TransporterTableModel transportersData;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPageTransporters(ModelPropertiesDialog dialog, EditModel model, boolean readOnly, Runnable help) {
		super(dialog,model,readOnly,help);
		localTransporters=model.transporters.clone();
	}

	@Override
	public void build(JPanel content) {
		final JTableExt table=new JTableExt();
		transportersData=new TransporterTableModel(model,model.surface,localTransporters,table,readOnly,help);
		table.setModel(transportersData);
		table.getColumnModel().getColumn(1).setMaxWidth(75);
		table.getColumnModel().getColumn(1).setMinWidth(75);
		table.getColumnModel().getColumn(2).setMaxWidth(75);
		table.getColumnModel().getColumn(2).setMinWidth(75);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.setIsPanelCellTable(2);
		content.add(new JScrollPane(table));
	}

	@Override
	public void storeData() {
		model.transporters.setDataFrom(transportersData.getTransporters());
	}
}