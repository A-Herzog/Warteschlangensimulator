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

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import language.Language;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modelproperties.SequencesEditPanel;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementTransportSourceRouter}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementTransportSourceRouter
 */
public class ModelElementTransportSourceRouterDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8381498487499996914L;

	/**
	 * Objekt zur Konfiguration der Transportzeiten
	 */
	private TransportTimeRecordPanel transportTimeRecordPanel;

	/**
	 * Liste mit den Namen aller möglichen Zielstationen
	 */
	private String[] stations;

	/**
	 * Auswahlbox für die standardmäßige Zielstation
	 */
	private JComboBox<String> defaultStation;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementTransportSourceRouter}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTransportSourceRouterDialog(final Component owner, final ModelElementTransportSourceRouter element, final boolean readOnly) {
		super(owner,Language.tr("Surface.TransportSourceRouter.Dialog.Title"),element,"ModelElementTransportSourceRouter",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(750,650);
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
	protected JComponent getContentPanel() {
		final ModelElementTransportSourceRouter source=(ModelElementTransportSourceRouter)element;

		final JTabbedPane tabs=new JTabbedPane();
		JPanel tab, sub, line;
		JLabel label;

		/* Daten vorbereiten */
		stations=SequencesEditPanel.getDestinationStations(element.getSurface());

		/* Tab "Transportzeit" */
		tabs.addTab(Language.tr("Surface.TransportSourceRouter.Dialog.Tab.TransportTimes"),tab=new JPanel(new BorderLayout()));
		tab.add(transportTimeRecordPanel=new TransportTimeRecordPanel(source.getTransportTimeRecord(),readOnly,source.getModel(),source.getSurface()),BorderLayout.CENTER);

		/* Tab "Routing-Ziele" */
		tabs.addTab(Language.tr("Surface.TransportSourceRouter.Dialog.Tab.RoutingTargets"),tab=new JPanel(new BorderLayout()));
		tab.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.TransportSourceRouter.Dialog.Info")+"</b></body></html>"));
		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.TransportSourceRouter.Dialog.DefaultStation")+":"));
		line.add(defaultStation=new JComboBox<>(stations));
		label.setLabelFor(defaultStation);

		int index=-1;
		for (int i=0;i<stations.length;i++) if (stations[i].equals(source.getDefaultStation())) {index=i; break;}
		if (index<0 && stations.length>0) index=0;
		if (index>=0) defaultStation.setSelectedIndex(index);

		/* System vorbereiten */
		tabs.setIconAt(0,Images.MODE_DISTRIBUTION.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_TRANSPORT_TARGET.getIcon());

		return tabs;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();
		final ModelElementTransportSourceRouter source=(ModelElementTransportSourceRouter)element;

		transportTimeRecordPanel.storeData();
		if (defaultStation.getSelectedIndex()>=0) source.setDefaultStation(stations[defaultStation.getSelectedIndex()]);
	}
}