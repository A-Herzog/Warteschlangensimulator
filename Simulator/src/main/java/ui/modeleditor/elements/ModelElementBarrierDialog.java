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
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementBarrier}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementBarrier
 */
public class ModelElementBarrierDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5734915086647346063L;

	private BarrierSignalTableModel tableSignal;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementBarrier}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementBarrierDialog(final Component owner, final ModelElementBarrier element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Barrier.Dialog.Title"),element,"ModelElementBarrier",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setSizeRespectingScreensize(650,525);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationBarrier;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BorderLayout());

		final JTableExt table;
		content.add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
		table.setModel(tableSignal=new BarrierSignalTableModel(
				table,
				((ModelElementBarrier)element).getOptions(),
				element.getSurface().getAllSignalNames().toArray(new String[0]),
				element.getSurface().getClientTypes().toArray(new String[0]),
				readOnly,
				helpRunnable));
		table.getColumnModel().getColumn(0).setMaxWidth(225);
		table.getColumnModel().getColumn(0).setMinWidth(225);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.setEnabled(!readOnly);

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
		tableSignal.storeData(((ModelElementBarrier)element).getOptions());
	}
}