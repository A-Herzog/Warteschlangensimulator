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

import javax.swing.JComponent;
import javax.swing.JPanel;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementTransportDestination}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementTransportDestination
 */
public class ModelElementTransportDestinationDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -8403825762415357794L;

	/**
	 * Name der Station beim Aufrufen des Dialogs<br>
	 * (um ggf. beim Schließen des Dialogs das Modell zu benachrichtigen, dass sich der Signalname verändert hat)
	 */
	private final String oldName;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementTransportDestination}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTransportDestinationDialog(final Component owner, final ModelElementTransportDestination element, final boolean readOnly) {
		super(owner,Language.tr("Surface.TransportDestination.Dialog.Title"),element,"ModelElementTransportDestination",readOnly,false);
		oldName=element.getName();
		setVisible(true);
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
		return InfoPanel.stationTransportDestination;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		return new JPanel();
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
			element.getSurface().objectRenamed(oldName,element.getName(),ModelDataRenameListener.RenameType.RENAME_TYPE_TRANSPORT_DESTINATION,true);
		}
	}
}
