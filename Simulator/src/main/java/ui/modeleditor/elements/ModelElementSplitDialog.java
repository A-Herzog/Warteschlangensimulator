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
import java.awt.FlowLayout;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSplit}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSplit
 */
public class ModelElementSplitDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 26157308123139871L;

	/**
	 * Tabelle zur Anzeige und Konfiguration der Kunden-Teil-Quellen-Daten
	 */
	private ModelElementSourceMultiTableModel recordsTableModel;

	/**
	 * Sollen die Kundendatenfelder auf die neuen Ankunftsdatensätze übertragen werden?
	 */
	private JCheckBox copyClientData;

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
	protected void initUserButtons() {
		addUserButton(Language.tr("ClientTypeLoader.Button"),Images.GENERAL_LOAD.getIcon());
		getUserButton(0).setToolTipText(Language.tr("ClientTypeLoader.Title"));
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementSplit split=(ModelElementSplit)element;

		final JPanel content=new JPanel(new BorderLayout());

		final JTableExt recordsTable;
		content.add(new JScrollPane(recordsTable=new JTableExt()),BorderLayout.CENTER);
		recordsTable.setModel(recordsTableModel=new ModelElementSourceMultiTableModel(recordsTable,split.getRecords(),false,true,element,element.getModel(),element.getSurface(),readOnly,helpRunnable,childDialog->getOpenModelSchedulesButton(childDialog)));
		recordsTable.getColumnModel().getColumn(0).setMaxWidth(200);
		recordsTable.getColumnModel().getColumn(0).setMinWidth(200);
		recordsTable.setIsPanelCellTable(0);
		recordsTable.setIsPanelCellTable(1);
		recordsTable.setEnabled(!readOnly);

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line,BorderLayout.SOUTH);
		line.add(copyClientData=new JCheckBox(Language.tr("Surface.Split.Dialog.CopyClientData"),split.isCopyClientData()));

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

		final ModelElementSplit split=(ModelElementSplit)element;

		recordsTableModel.storeData(split);
		split.setCopyClientData(copyClientData.isSelected());
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		final File file=ClientTypeLoader.selectFile(this);
		if (file==null) return;

		final List<ModelElementSourceRecord> clientTypes=new ClientTypeLoader(file).getArrivalClientTypes();

		if (clientTypes.size()==0) {
			MsgBox.error(this,Language.tr("ClientTypeLoader.Title"),String.format(Language.tr("ClientTypeLoader.LoadError"),file.toString()));
			return;
		}

		final Set<String> currentClientTypes=recordsTableModel.getClientTypeNames();
		if (clientTypes.stream().map(clientType->clientType.getName()).filter(name->currentClientTypes.contains(name)).findFirst().isPresent()) {
			if (!MsgBox.confirm(this,Language.tr("ClientTypeLoader.Title"),Language.tr("ClientTypeLoader.ReplaceWarning"),Language.tr("ClientTypeLoader.ReplaceWarning.YesInfo"),Language.tr("ClientTypeLoader.ReplaceWarning.NoInfo"))) return;
		}

		recordsTableModel.replaceRecords(clientTypes);
	}
}
