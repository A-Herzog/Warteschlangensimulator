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

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;

import language.Language;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementDecideAndTeleport}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementDecideAndTeleport
 */
public class ModelElementDecideAndTeleportDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=4344496210544246295L;

	/**
	 * Eingabepanel für die Teleportziele
	 */
	private TeleportDestinationsPanel teleportDestinations;

	/**
	 * Eingabepanel für die Verzweigungsregeln
	 */
	private DecideDataPanel decideDataPanel;

	/**
	 * Anzahl an Zielen
	 */
	private int lastDestinationCount;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementDecide}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementDecideAndTeleportDialog(final Component owner, final ModelElementDecideAndTeleport element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Decide.Dialog.Title"),element,"ModelElementDecideAndTeleport",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
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
		return InfoPanel.stationDecide;
	}

	@Override
	protected JComponent getContentPanel() {


		final ModelElementDecideAndTeleport decide=(ModelElementDecideAndTeleport)element;

		final JPanel content=new JPanel(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tab;

		/* Teleportziele */
		tabs.addTab("Teleportziele",tab=new JPanel(new BorderLayout()));
		tab.add(teleportDestinations=new TeleportDestinationsPanel(decide.getDestinations(),element.getModel(),readOnly));
		teleportDestinations.addDataChangedListener(()->rebuildDecidePanel());

		/* Verzweigungsregeln */
		tabs.addTab("Verzweigungsregeln",tab=new JPanel(new BorderLayout()));
		tab.add(decideDataPanel=new DecideAndTeleportDecideDataPanel());

		/* Icons */
		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_TELEPORT_DESTINATION.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_DECIDE.getIcon());

		lastDestinationCount=teleportDestinations.getDestinations().size();

		return content;
	}

	/**
	 * Cache für die Zuordnung von Teleport-Ziel-Namen zu den zugehörigen Elementen
	 * @see DecideAndTeleportDecideDataPanel#getDestinations()
	 */
	private Map<String,ModelElementTeleportDestination> destinations;

	/**
	 * Verzweigungsregeln-Panel
	 * @see DecideDataPanel
	 */
	private class DecideAndTeleportDecideDataPanel extends DecideDataPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-1776207700552175574L;

		/**
		 * Konstruktor der Klasse
		 * (legt basierend auf dem Element ein neues Panel an)
		 */
		public DecideAndTeleportDecideDataPanel() {
			super(element,readOnly);
		}

		/**
		 * Konstruktor der Klasse
		 * (überträgt die Daten aus einem alten Panel in ein neues)
		 * @param oldDecideDataPanel	Altes Panel aus dem die Daten übernommen werden sollen
		 */
		public DecideAndTeleportDecideDataPanel(final DecideDataPanel oldDecideDataPanel) {
			super(element,oldDecideDataPanel,readOnly);
		}

		@Override
		protected List<String> getDestinations() {
			if (destinations==null) destinations=TeleportDestinationsPanel.getAllDestinations(element.getModel());
			return teleportDestinations.getDestinations().stream().map(destination->{
				final ModelElementTeleportDestination destinationStation=destinations.get(destination);
				return (destinationStation==null)?destination:String.format("%s (id=%d)",destination,destinationStation.getId());
			}).collect(Collectors.toList());
		}
	}

	/**
	 * Aktualisiert das Verzweigungsregeln-Panel vollständig
	 */
	private void rebuildDecidePanelNow() {
		final JPanel tab=(JPanel)decideDataPanel.getParent();
		final DecideDataPanel oldDecideDataPanel=decideDataPanel;
		tab.remove(oldDecideDataPanel);
		tab.add(decideDataPanel=new DecideAndTeleportDecideDataPanel(oldDecideDataPanel));
	}

	/**
	 * Timer zur Verzögerten Aktualisierung des Verzweigungsregeln-Panels
	 * @see #rebuildDecidePanel()
	 */
	private Timer rebuildDecidePanelTimer=null;

	/**
	 * Aktualisiert Anzahl und Bezeichnungen im Verzweigungsregeln-Panel
	 */
	private void rebuildDecidePanel() {
		final int newDestinationCount=teleportDestinations.getDestinations().size();
		if (lastDestinationCount==newDestinationCount) {
			decideDataPanel.updateLabels();
			return;
		}
		lastDestinationCount=newDestinationCount;

		if (rebuildDecidePanelTimer!=null) {
			rebuildDecidePanelTimer.stop();
			rebuildDecidePanelTimer=null;
		}

		rebuildDecidePanelTimer=new Timer(200,e->{
			rebuildDecidePanelTimer.stop();
			rebuildDecidePanelTimer=null;
			rebuildDecidePanelNow();
		});
		rebuildDecidePanelTimer.start();
	}

	@Override
	protected boolean checkData() {
		return decideDataPanel.checkDataWithErrorMessage();
	}

	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementDecideAndTeleport decide=(ModelElementDecideAndTeleport)element;

		/* Teleportziele */
		decide.getDestinations().clear();
		decide.getDestinations().addAll(teleportDestinations.getDestinations());

		/* Verzweigungsregeln */
		decideDataPanel.storeData();
	}
}
