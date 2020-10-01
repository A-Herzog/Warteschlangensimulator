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

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import tools.JTableExt;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSplit}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSplit
 */
public class ModelElementSplitDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 26157308123139871L;

	private ModelElementSourceMultiTableModel recordsTableModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSplit}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendatenelement aus dem Modell (zum Auslesen von Icons und ggf. um Kundentypen umzubenennen)
	 */
	public ModelElementSplitDialog(final Component owner, final ModelElementSplit element, final boolean readOnly, final ModelClientData clientData) {
		super(owner,Language.tr("Surface.Split.Dialog.Title"),element,"ModelElementSplit",readOnly,false);
		recordsTableModel.setClientData(clientData);
		setVisible(true);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,500);
		setResizable(true);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSplit;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		final JTableExt recordsTable;
		content.add(new JScrollPane(recordsTable=new JTableExt()),BorderLayout.CENTER);
		if (element instanceof ModelElementSplit) {
			recordsTable.setModel(recordsTableModel=new ModelElementSourceMultiTableModel(recordsTable,((ModelElementSplit)element).getRecords(),false,element,element.getModel(),element.getSurface(),readOnly,helpRunnable));
		}

		recordsTable.getColumnModel().getColumn(0).setMaxWidth(200);
		recordsTable.getColumnModel().getColumn(0).setMinWidth(200);
		recordsTable.setIsPanelCellTable(0);
		recordsTable.setIsPanelCellTable(1);
		recordsTable.setEnabled(!readOnly);

		return content;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();
		recordsTableModel.storeData((ModelElementSplit)element);
	}
}
