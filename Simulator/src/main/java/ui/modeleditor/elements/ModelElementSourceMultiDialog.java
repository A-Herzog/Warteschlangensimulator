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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSourceMulti}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSourceMulti
 */
public class ModelElementSourceMultiDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -8837613134430882888L;

	/**
	 * Tabelle zur Anzeige und Konfiguration der Kunden-Teil-Quellen-Daten
	 */
	private ModelElementSourceMultiTableModel recordsTableModel;

	/**
	 * Combobox zur Auswahl des Modus wie mit den Teil-Ankunftsströmen umgegangen werden soll
	 */
	private JComboBox<?> modeComboBox;

	/**
	 * Option: Globale Maximalanzahl an Kundenankünften aktiv?
	 * @see #maxClientArrivalValue
	 */
	private JCheckBox maxClientArrivalOption;

	/**
	 * Eingabefeld für die Maximalanzahl an Kundenankünften
	 */
	private JTextField maxClientArrivalValue;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSourceMulti}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendatenelement aus dem Modell (zum Auslesen von Icons und ggf. um Kundentypen umzubenennen)
	 */
	public ModelElementSourceMultiDialog(final Component owner, final ModelElementSourceMulti element, final boolean readOnly, final ModelClientData clientData) {
		super(owner,Language.tr("Surface.SourceMulti.Dialog.Title"),element,"ModelElementSourceMulti",readOnly,false);
		recordsTableModel.setClientData(clientData);
		setVisible(true);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(750,500);
		pack();
		setResizable(true);
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSourceMulti;
	}

	@Override
	protected void initUserButtons() {
		addUserButton(Language.tr("ClientTypeLoader.Button"),Images.GENERAL_LOAD.getIcon());
		getUserButton(0).setToolTipText(Language.tr("ClientTypeLoader.Title"));
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementSourceMulti source=(ModelElementSourceMulti)element;

		final JPanel content=new JPanel(new BorderLayout());

		/* Tabelle */
		final JTableExt recordsTable;
		content.add(new JScrollPane(recordsTable=new JTableExt()),BorderLayout.CENTER);
		if (element instanceof ModelElementSourceMulti) {
			recordsTable.setModel(recordsTableModel=new ModelElementSourceMultiTableModel(recordsTable,source.getRecords(),true,true,element,element.getModel(),element.getSurface(),readOnly,helpRunnable,childDialog->getOpenModelSchedulesButton(childDialog)));
		}

		recordsTable.getColumnModel().getColumn(0).setMaxWidth(200);
		recordsTable.getColumnModel().getColumn(0).setMinWidth(200);
		recordsTable.setIsPanelCellTable(0);
		recordsTable.setIsPanelCellTable(1);
		recordsTable.setEnabled(!readOnly);

		/* Eingabebereich unten */
		final JPanel setupArea=new JPanel();
		setupArea.setLayout(new BoxLayout(setupArea,BoxLayout.PAGE_AXIS));
		content.add(setupArea,BorderLayout.SOUTH);

		/* Modus */
		final Object[] data=getComboBoxPanel(Language.tr("Surface.SourceMulti.Dialog.Mode")+":",new String[] {
				Language.tr("Surface.SourceMulti.Dialog.Mode.All"),
				Language.tr("Surface.SourceMulti.Dialog.Mode.InTurn")
		});
		setupArea.add((JPanel)data[0]);
		modeComboBox=(JComboBox<?>)data[1];
		modeComboBox.setEnabled(!readOnly);
		switch (source.getMode()) {
		case ALL: modeComboBox.setSelectedIndex(0); break;
		case IN_TURN: modeComboBox.setSelectedIndex(1); break;
		default: modeComboBox.setSelectedIndex(0); break;
		}

		/* Anzahl an Ankünften */
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		setupArea.add(line);
		line.add(maxClientArrivalOption=new JCheckBox(Language.tr("Surface.SourceMulti.Dialog.Limit")+":"));
		maxClientArrivalOption.addActionListener(e->checkData(false));
		maxClientArrivalOption.setSelected(source.getMaxClientArrival()>0);
		maxClientArrivalOption.setEnabled(!readOnly);
		line.add(maxClientArrivalValue=new JTextField((source.getMaxClientArrival()>0)?(""+source.getMaxClientArrival()):"1000",10));
		maxClientArrivalValue.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				maxClientArrivalOption.setSelected(true);
				checkData(false);
			}
		});
		maxClientArrivalValue.setEnabled(!readOnly);

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (maxClientArrivalOption.isSelected()) {
			final Long L=NumberTools.getPositiveLong(maxClientArrivalValue,true);
			if (L==null) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.SourceMulti.Dialog.Limit.Error.Title"),String.format(Language.tr("Surface.SourceMulti.Dialog.Limit.Error.Info"),maxClientArrivalValue.getText()));
					return false;
				}
				ok=false;
			}
		} else {
			maxClientArrivalValue.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementSourceMulti source=(ModelElementSourceMulti)element;

		recordsTableModel.storeData(source);
		switch (modeComboBox.getSelectedIndex()) {
		case 0: source.setMode(ModelElementSourceMulti.MultiSourceMode.ALL); break;
		case 1: source.setMode(ModelElementSourceMulti.MultiSourceMode.IN_TURN); break;
		}

		if (maxClientArrivalOption.isSelected()) {
			source.setMaxClientArrival(NumberTools.getPositiveLong(maxClientArrivalValue,true).intValue());
		} else {
			source.setMaxClientArrival(-1);
		}
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