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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementBatchMulti}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementBatchMulti
 */
public class ModelElementBatchMultiDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = -3486370612949864100L;

	private List<String> clientTypes;
	private JComboBox<String> selectClientType;
	private JPanel main;
	private CardLayout mainLayout;
	private List<BatchRecordPanel> batchRecordPanels;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementBatchMulti}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementBatchMultiDialog(final Component owner, final ModelElementBatchMulti element, final boolean readOnly) {
		super(owner,Language.tr("Surface.BatchMulti.Dialog.Title"),element,"ModelElementBatchMulti",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationBatchMulti;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		clientTypes=element.getModel().surface.getClientTypes();

		final JPanel content=new JPanel(new BorderLayout());

		/* Auswahl des Kundentyps */
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line,BorderLayout.NORTH);
		final JLabel label=new JLabel(Language.tr("Surface.BatchMulti.Dialog.ClientType")+":");
		line.add(label);
		line.add(selectClientType=new JComboBox<String>(clientTypes.toArray(new String[0])));
		label.setLabelFor(selectClientType);
		if (clientTypes.size()>0) selectClientType.setSelectedIndex(0);
		selectClientType.addActionListener(e->selectedClientTypeChanged());

		/* Editorbereich */
		content.add(main=new JPanel(mainLayout=new CardLayout()),BorderLayout.CENTER);

		/* Editoren anlegen */
		final Map<String,BatchRecord> map=((ModelElementBatchMulti)element).getBatchRecords();
		batchRecordPanels=new ArrayList<>();
		for (int i=0;i<clientTypes.size();i++) {
			final BatchRecord editRecord=new BatchRecord();
			final BatchRecord record=map.get(clientTypes.get(i));
			if (record!=null) editRecord.copyDataFrom(record);
			final BatchRecordPanel batchRecordPanel=new BatchRecordPanel(editRecord,readOnly,true);
			batchRecordPanel.setActive(record!=null);
			batchRecordPanels.add(batchRecordPanel);
			main.add(batchRecordPanel,""+i);
		}

		return content;
	}

	private void selectedClientTypeChanged() {
		final int index=selectClientType.getSelectedIndex();
		if (index<0) return;

		mainLayout.show(main,""+index);
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		for (int i=0;i<batchRecordPanels.size();i++) {
			if (!batchRecordPanels.get(i).checkData(true)) {
				selectClientType.setSelectedIndex(i);
				selectedClientTypeChanged();
				return false;
			}
		}

		return true;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final Map<String,BatchRecord> map=((ModelElementBatchMulti)element).getBatchRecords();
		map.clear();

		for (int i=0;i<batchRecordPanels.size();i++) if (batchRecordPanels.get(i).isActive()) {
			batchRecordPanels.get(i).storeData();
			final BatchRecord newRecord=batchRecordPanels.get(i).getBatchRecord();
			map.put(clientTypes.get(i),newRecord);
		}
	}
}
