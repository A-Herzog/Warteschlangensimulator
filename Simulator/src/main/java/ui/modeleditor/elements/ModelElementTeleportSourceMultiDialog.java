/**
 * Copyright 2022 Alexander Herzog
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
import java.util.List;

import javax.swing.JComponent;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementTeleportSourceMulti}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementTeleportSourceMulti
 */
public class ModelElementTeleportSourceMultiDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-6829332598061218612L;

	/**
	 * Eingabepanel für die Teleportziele
	 */
	private TeleportDestinationsPanel teleportDestinations;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementTeleportSource}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTeleportSourceMultiDialog(final Component owner, final ModelElementTeleportSourceMulti element, final boolean readOnly) {
		super(owner,Language.tr("Surface.TeleportSourceMulti.Dialog.Title"),element,"ModelElementTeleportSourceMulti",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,400);
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
		return InfoPanel.stationTeleportSourceMulti;
	}

	@Override
	protected JComponent getContentPanel() {
		return teleportDestinations=new TeleportDestinationsPanel(((ModelElementTeleportSourceMulti)element).getDestinations(),element.getModel(),readOnly);
	}

	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementTeleportSourceMulti) {
			final List<String> destinations=((ModelElementTeleportSourceMulti)element).getDestinations();
			destinations.clear();
			destinations.addAll(teleportDestinations.getDestinations());
		}
	}
}
