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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import language.Language;
import simulator.elements.RunElementTeleportSource;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementDecide}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementDecide
 */
public final class ModelElementDecideDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1706008891307759220L;

	/** Konfigurationsseite für die Verzweigungsregeln */
	private DecideDataPanel decideDataPanel;

	/** Konfigurationbereich für die neuen Kundentypen pro Ausgang */
	private NewClientTypesPanel newClientTypes;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementDecide}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementDecideDialog(final Component owner, final ModelElementDecide element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Decide.Dialog.Title"),element,"ModelElementDecide",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		pack();
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	/**
	 * Liefert den Namen des Ziels, das über eine auslaufende Kante erreicht werden kann
	 * @param edge	Auslaufende Kante
	 * @return	Name des Ziels
	 */
	private String getDestination(ModelElementEdge edge) {
		while (true) {
			if (edge==null) return null;
			final ModelElement edgeEnd=edge.getConnectionEnd();
			if (edgeEnd==null) return null;

			if (edgeEnd instanceof ModelElementVertex) {
				edge=((ModelElementVertex)edgeEnd).getEdgeOut();
				continue;
			}

			if (edgeEnd instanceof ModelElementTeleportSource) {
				final ModelElementTeleportDestination destination=RunElementTeleportSource.getDestination(edgeEnd.getModel(),((ModelElementTeleportSource)edgeEnd).getDestination());
				if (destination==null) return null;
				edge=destination.getEdgeOut();
				continue;
			}


			String name;
			if (edgeEnd instanceof ModelElementBox) {
				name=((ModelElementBox)edgeEnd).getTypeName();
			} else {
				name=edgeEnd.getName();
			}

			return name+" (id="+edgeEnd.getId()+")";
		}
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationDecide;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JTabbedPane tabs=new JTabbedPane();

		if (!(element instanceof ModelElementDecide)) return tabs;
		final ModelElementDecide decide=(ModelElementDecide)element;

		/* Verzweigungsrichtungen */

		final JPanel main=new JPanel(new BorderLayout());
		tabs.addTab(Language.tr("Surface.Decide.Dialog.Directions"),main);

		final ModelElementEdge[] edges=decide.getEdgesOut();
		final List<String> destinations=new ArrayList<>();
		for (int i=0;i<edges.length;i++) {
			String name=Language.tr("Surface.Decide.Dialog.OutgoingEdge")+" "+(i+1);
			final String destination=getDestination(edges[i]);
			if (destination!=null) name+=" zu "+destination;
			destinations.add(name);
		}

		main.add(decideDataPanel=new DecideDataPanel(element,readOnly) {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=6614230151945947397L;

			@Override protected List<String> getDestinations() {return destinations;}
		});

		/* Neue Kundentypen zuweisen */

		tabs.addTab(Language.tr("Surface.Decide.Dialog.NewClientTypes"),newClientTypes=new NewClientTypesPanel(decide.getChangedClientTypes(),readOnly));

		/* Icons für Seiten */

		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_DECIDE.getIcon());
		tabs.setIconAt(1,Images.MODELPROPERTIES_CLIENTS.getIcon());

		return tabs;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return decideDataPanel.checkDataWithErrorMessage();
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementDecide) {
			final ModelElementDecide decide=(ModelElementDecide)element;

			decideDataPanel.storeData();

			decide.setChangedClientTypes(newClientTypes.getNewClientTypeNames());
		}
	}
}
