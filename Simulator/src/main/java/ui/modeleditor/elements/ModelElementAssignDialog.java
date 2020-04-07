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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import language.Language;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modelproperties.ModelPropertiesDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAssign}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAssign
 */
public class ModelElementAssignDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 491234735571463778L;

	private final String oldName;
	private final ModelClientData clientData;
	private JButton editClientDataButton;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAssign}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendatenelement aus dem Modell (zum Auslesen von Icons und ggf. um Kundentypen umzubenennen)
	 */
	public ModelElementAssignDialog(final Component owner, final ModelElementAssign element, final boolean readOnly, final ModelClientData clientData) {
		super(owner,Language.tr("Surface.Assign.Dialog.Title"),element,"ModelElementAssign",readOnly,false);
		oldName=element.getName();
		this.clientData=clientData;
		setVisible(true);
	}

	@Override
	protected void initUserNameFieldButtons(final JPanel panel) {
		panel.add(editClientDataButton=new JButton());
		setClientIcon(element.getName(),editClientDataButton,element.getModel());
		editClientDataButton.setToolTipText(Language.tr("Surface.Source.Dialog.ClientTypeSettings"));
		editClientDataButton.addActionListener(e->editClientData());
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAssign;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		return new JPanel();
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (!oldName.equals(element.getName())) {
			if (clientData!=null) clientData.copyDataIfNotExistent(oldName,element.getName());
			element.getSurface().objectRenamed(oldName,element.getName(),ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE,true);
		}
	}

	private void editClientData() {
		final String name=(oldName.isEmpty())?getElementName():oldName;
		if (name.isEmpty()) {
			MsgBox.error(this,Language.tr("Surface.Source.Dialog.ClientTypeSettings.NoNameError.Title"),Language.tr("Surface.Source.Dialog.ClientTypeSettings.NoNameError.Info"));
			return;
		}

		if (ModelPropertiesDialog.editClientData(this,helpRunnable,element.getModel(),name,readOnly)) setClientIcon(name,editClientDataButton,element.getModel());
	}
}