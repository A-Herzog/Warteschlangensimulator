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
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import tools.JTableExt;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementOutputLog}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementOutputLog
 */
public class ModelElementOutputLogDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=6523288780221631523L;

	/**
	 * Tabelle zur Konfiguration der auszugebenden Daten
	 */
	private OutputLogTableModel tableModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementOutputLog}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementOutputLogDialog(final Component owner, final ModelElementOutputLog element, final boolean readOnly) {
		super(owner,Language.tr("Surface.OutputLog.Dialog.Title"),element,"ModelElementOutputLog",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationOutput;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		if (element instanceof ModelElementOutputLog) {
			final JPanel upperPanel=new JPanel();
			upperPanel.setLayout(new BoxLayout(upperPanel,BoxLayout.PAGE_AXIS));

			final JTableExt table=new JTableExt();
			table.setModel(tableModel=new OutputLogTableModel(table,element.getModel(),((ModelElementOutputLog)element).getModes(),((ModelElementOutputLog)element).getData(),element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true),readOnly));
			table.setIsPanelCellTable(0);
			table.setIsPanelCellTable(1);
			table.getColumnModel().getColumn(1).setMaxWidth(300);
			table.getColumnModel().getColumn(1).setMinWidth(300);
			table.setEnabled(!readOnly);
			content.add(new JScrollPane(table),BorderLayout.CENTER);
		}

		return content;
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
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

		if (element instanceof ModelElementOutputLog) {
			final List<ModelElementOutputLog.OutputMode> modes=((ModelElementOutputLog)element).getModes();
			modes.clear();
			modes.addAll(tableModel.getModes());
			List<String> data=((ModelElementOutputLog)element).getData();
			data.clear();
			data.addAll(tableModel.getData());
			while (data.size()<modes.size()) data.add("");
		}
	}
}
