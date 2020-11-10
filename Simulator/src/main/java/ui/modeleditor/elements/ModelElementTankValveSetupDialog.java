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
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import tools.JTableExt;
import ui.help.Help;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementTankValveSetup}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementTankValveSetup
 */
public class ModelElementTankValveSetupDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2318993244622873645L;

	/**
	 * Tabelle zur Konfiguration der Ventil-Einstellungen
	 */
	private ModelElementTankValveSetupTableModel tableModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementTankValveSetup}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTankValveSetupDialog(final Component owner, final ModelElementTankValveSetup element, final boolean readOnly) {
		super(owner,Language.tr("Surface.TankValveSetup.Dialog.Title"),element,"ModelElementTankValveSetup",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationTankValveSetup;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel panel=new JPanel(new BorderLayout());
		final JTableExt table;
		panel.add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
		final ModelSurface surface=(element.getSurface().getParentSurface()==null)?element.getSurface():element.getSurface().getParentSurface();
		table.setModel(tableModel=new ModelElementTankValveSetupTableModel(table,((ModelElementTankValveSetup)element).getValveSetups(),surface,element.getModel(),readOnly,()->Help.topicModal(ModelElementTankValveSetupDialog.this,"ModelElementTankValveSetup")));
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.getColumnModel().getColumn(1).setMaxWidth(125);
		table.getColumnModel().getColumn(1).setMinWidth(125);
		table.setEnabled(!readOnly);
		table.getTableHeader().setReorderingAllowed(false);

		return panel;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();
		tableModel.storeData();
	}
}