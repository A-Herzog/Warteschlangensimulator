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

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import language.Language;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modelproperties.ModelPropertiesDialogPageClients;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSource}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSource
 */
public final class ModelElementSourceDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -86922871601132368L;

	/**
	 * Panel zum Bearbeiten der Kunden-Quelle-Einstellungen
	 */
	private ModelElementSourceRecordPanel recordPanel;

	/**
	 * Name der Station beim Aufrufen des Dialogs<br>
	 * (um ggf. beim Schließen des Dialogs das Modell zu benachrichtigen, dass sich der Signalname verändert hat)
	 */
	private final String oldName;

	/**
	 * Kundendatenelement aus dem Modell (zum Auslesen von Icons und ggf. um Kundentypen umzubenennen)
	 */
	private final ModelClientData clientData;

	/**
	 * Schaltfläche "Kundentypeigenschaften bearbeiten"
	 */
	private JButton editClientDataButton;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSource}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendatenelement aus dem Modell (zum Auslesen von Icons und ggf. um Kundentypen umzubenennen)
	 */
	public ModelElementSourceDialog(final Component owner, final ModelElementSource element, final boolean readOnly, final ModelClientData clientData) {
		super(owner,Language.tr("Surface.Source.Dialog.Title"),element,"ModelElementSource",readOnly,false);
		oldName=element.getName();
		this.clientData=clientData;
		if (owner!=null) setVisible(true);
	}

	@Override
	protected void initUserNameFieldButtons(final JPanel panel) {
		panel.add(editClientDataButton=new JButton());
		setClientIcon(element.getName(),editClientDataButton,element.getModel());
		editClientDataButton.setToolTipText(Language.tr("Surface.Source.Dialog.ClientTypeSettings"));
		editClientDataButton.addActionListener(e->editClientData());
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(700,600);
		pack();
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
		return InfoPanel.stationSource;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		recordPanel=new ModelElementSourceRecordPanel(readOnly,element.getModel(),element.getSurface(),()->getOpenModelSchedulesButton(null),helpRunnable,true,false);
		recordPanel.setData(((ModelElementSource)element).getRecord(),element);
		return recordPanel;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return recordPanel.checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementSourceRecord record=((ModelElementSource)element).getRecord();
		recordPanel.getData(record);

		if (!element.getSurface().getClientTypes().contains(oldName)) {
			ModelElementSourceRecordPanel.renameClients(oldName,element.getName(),clientData,element.getSurface());
		}
	}

	/**
	 * Ruft den Dialog zum Bearbeiten der Kundentypeigenschaften auf.
	 * @see #editClientDataButton
	 */
	private void editClientData() {
		final String name=(oldName.isEmpty())?getElementName():oldName;
		if (name.isEmpty()) {
			MsgBox.error(this,Language.tr("Surface.Source.Dialog.ClientTypeSettings.NoNameError.Title"),Language.tr("Surface.Source.Dialog.ClientTypeSettings.NoNameError.Info"));
			return;
		}

		if (ModelPropertiesDialogPageClients.editClientData(this,helpRunnable,element.getModel(),name,readOnly)) setClientIcon(name,editClientDataButton,element.getModel());
	}
}
