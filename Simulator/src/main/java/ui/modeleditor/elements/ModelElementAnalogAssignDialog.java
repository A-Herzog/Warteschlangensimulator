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
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnalogAssign}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnalogAssign
 */
public class ModelElementAnalogAssignDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 5452639514092710192L;

	private ModelElementAnalogAssignTableModel tableData;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnalogAssign}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */

	public ModelElementAnalogAssignDialog(final Component owner, final ModelElementAnalogAssign element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnalogAssign.Dialog.Title"),element,"ModelElementAnalogAssign",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnalogAssign;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		final JTableExt table;
		content.add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
		table.setModel(tableData=new ModelElementAnalogAssignTableModel(table,(ModelElementAnalogAssign)element,readOnly,helpRunnable));
		table.getColumnModel().getColumn(0).setMaxWidth(150);
		table.getColumnModel().getColumn(0).setMinWidth(150);
		table.getColumnModel().getColumn(1).setMaxWidth(150);
		table.getColumnModel().getColumn(1).setMinWidth(150);
		table.getColumnModel().getColumn(3).setMaxWidth(150);
		table.getColumnModel().getColumn(3).setMinWidth(150);
		table.setIsPanelCellTable(3);
		table.setEnabled(!readOnly);

		return content;
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(650,550);
		pack();
		setResizable(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		tableData.storeData();
	}
}
