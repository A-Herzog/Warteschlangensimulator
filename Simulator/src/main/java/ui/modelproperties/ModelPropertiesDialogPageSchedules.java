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

/**
 * Dialogseite "Zeitpläne"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageSchedules extends ModelPropertiesDialogPage {
	/** Datenmodell für die Liste Zeitpläne */
	private SchedulesTableModel schedulesData;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPageSchedules(ModelPropertiesDialog dialog, EditModel model, boolean readOnly, Runnable help) {
		super(dialog,model,readOnly,help);
	}

	@Override
	public void build(JPanel content) {
		final JTableExt table=new JTableExt();
		schedulesData=new SchedulesTableModel(model.surface,dialog.localResources,dialog.localSchedules,table,readOnly,help);
		schedulesData.addUpdateResourcesListener(()->dialog.updateResourceTable());
		table.setModel(schedulesData);
		table.getColumnModel().getColumn(0).setMaxWidth(225);
		table.getColumnModel().getColumn(0).setMinWidth(225);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		content.add(new JScrollPane(table));
	}

	@Override
	public void storeData() {
		model.schedules.setDataFrom(schedulesData.getSchedules());
	}
}
