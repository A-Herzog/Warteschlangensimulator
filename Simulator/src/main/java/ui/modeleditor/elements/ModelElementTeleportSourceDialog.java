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
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementTeleportSource}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementTeleportSource
 */
public class ModelElementTeleportSourceDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4461520538246262403L;

	/**
	 * Auswahlbox für das Teleport-Ziel
	 */
	private JComboBox<String> destination;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementTeleportSource}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTeleportSourceDialog(final Component owner, final ModelElementTeleportSource element, final boolean readOnly) {
		super(owner,Language.tr("Surface.TeleportSource.Dialog.Title"),element,"ModelElementTeleportSource",readOnly);
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
		return InfoPanel.stationTeleportSource;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());
		final JPanel main=new JPanel();
		content.add(main,BorderLayout.NORTH);
		main.setLayout(new BoxLayout(main,BoxLayout.PAGE_AXIS));

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		main.add(line);

		final JLabel label=new JLabel(Language.tr("Surface.TeleportSource.Dialog.Target")+":");
		line.add(label);
		final List<String> destinations=getTeleportDestinations();
		line.add(destination=new JComboBox<>(destinations.toArray(String[]::new)));
		label.setLabelFor(destination);
		if (element instanceof ModelElementTeleportSource) {
			final String dest=((ModelElementTeleportSource)element).getDestination();
			int index=destinations.indexOf(dest);
			if (index<0 && destinations.size()>0) index=0;
			if (index>=0) destination.setSelectedIndex(index);
		}

		return content;
	}

	/**
	 * Liefert eine Liste mit allen verfügbaren Teleport-Zielen.
	 * @return	Liste mit allen verfügbaren Teleport-Zielen
	 */
	private List<String> getTeleportDestinations() {
		final Set<String> destinations=new HashSet<>();

		for (ModelElement e1: element.getModel().surface.getElements()) {
			if (e1 instanceof ModelElementTeleportDestination && !e1.getName().trim().isEmpty()) destinations.add(e1.getName());
			if (e1 instanceof ModelElementSub) for (ModelElement e2: ((ModelElementSub)e1).getSubSurface().getElements()) {
				if (e2 instanceof ModelElementTeleportDestination && !e2.getName().trim().isEmpty()) destinations.add(e2.getName());
			}
		}

		return destinations.stream().sorted().collect(Collectors.toList());
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();
		if (element instanceof ModelElementTeleportSource) {
			if (destination.getSelectedIndex()<0) {
				((ModelElementTeleportSource)element).setDestination("");
			} else {
				((ModelElementTeleportSource)element).setDestination((String)destination.getSelectedItem());
			}
		}
	}
}
