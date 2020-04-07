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
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import simulator.editmodel.EditModel;
import tools.IconListCellRenderer;
import tools.JTableExt;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modelproperties.SequencesEditPanel;

/**
 * Diese Klasse kapselt die Dialogdaten für ein
 * {@link TransportTargetSystem}-Element.
 * @author Alexander Herzog
 * @see TransportTargetSystem
 * @see ModelElementTransportSourceDialog
 */
public class TransportTargetSystemPanel extends JPanel {
	private static final long serialVersionUID = -5911117945099961882L;

	private final TransportTargetSystem data;

	private final String[] stations;

	private final JComboBox<String> mode;
	private final JPanel cardPanel;
	private final CardLayout cardLayout;
	private final TransportRouteTableModel tableRouting;
	private final JTextField propertyName;
	private final JComboBox<String> defaultStation;

	/**
	 * Konstruktor der Klasse
	 * @param data	Daten, die in dem Panel bearbeitet werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param model	Gesamtes Modell (zum Auslesen von Daten für den Expression-Builder)
	 * @param surface	Zeichenoberfläche (zum Auslesen von Daten für den Expression-Builder)
	 * @param help	Hilfe-Runnable
	 */
	public TransportTargetSystemPanel(final TransportTargetSystem data, final boolean readOnly, final EditModel model, final ModelSurface surface, final Runnable help) {
		super();
		this.data=data;
		setLayout(new BorderLayout());

		/* Daten vorbereiten */
		final String[] clientTypes=surface.getClientTypes().toArray(new String[0]);
		final String[] variables=surface.getMainSurfaceVariableNames(model.getModelVariableNames(),true);
		stations=SequencesEditPanel.getDestinationStations(surface);

		JPanel line;
		JLabel label;

		/* Modus */
		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(label=new JLabel(Language.tr("Surface.TransportSource.Dialog.TargetMode")+":"));
		line.add(mode=new JComboBox<>(new String[] {
				Language.tr("Surface.TransportSource.Dialog.TargetMode.Explicite"),
				Language.tr("Surface.TransportSource.Dialog.TargetMode.Sequence"),
				Language.tr("Surface.TransportSource.Dialog.TargetMode.Property")
		}));
		mode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODELEDITOR_ELEMENT_TRANSPORT_TARGET_MODE_EXPLICITE,
				Images.MODELEDITOR_ELEMENT_TRANSPORT_TARGET_MODE_SEQUENCE,
				Images.MODELEDITOR_ELEMENT_TRANSPORT_TARGET_MODE_PROPERTY
		}));
		mode.setEnabled(!readOnly);
		label.setLabelFor(mode);

		add(cardPanel=new JPanel(cardLayout=new CardLayout()),BorderLayout.CENTER);

		cardPanel.add(new JPanel(),"0");

		/* Tabelle */
		final JTableExt table;
		cardPanel.add(new JScrollPane(table=new JTableExt()),"0");
		table.setModel(tableRouting=new TransportRouteTableModel(table,data.getRouting(),clientTypes,stations,variables,model,surface,readOnly,help));
		table.getColumnModel().getColumn(1).setMaxWidth(275);
		table.getColumnModel().getColumn(1).setMinWidth(275);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.setEnabled(!readOnly);

		/* Fertigungsplan */
		cardPanel.add(new JScrollPane(new JPanel()),"1");

		/* Property */
		final JPanel propertyPanel;
		cardPanel.add(propertyPanel=new JPanel(new BorderLayout()),"2");
		final JPanel propertyPanelInner;
		propertyPanel.add(propertyPanelInner=new JPanel(),BorderLayout.NORTH);
		propertyPanelInner.setLayout(new BoxLayout(propertyPanelInner,BoxLayout.PAGE_AXIS));
		final Object[] obj=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.TransportSource.Dialog.Property")+":",data.getRoutingProperty());
		propertyPanelInner.add((JPanel)obj[0]);
		propertyName=(JTextField)obj[1];

		/* Standardziel */
		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		line.add(label=new JLabel(Language.tr("Surface.TransportSource.Dialog.DefaultStation")+":"));
		line.add(defaultStation=new JComboBox<>(stations));
		label.setLabelFor(defaultStation);
		defaultStation.setEnabled(!readOnly);
		int index=-1;
		for (int i=0;i<stations.length;i++) if (stations[i].equals(data.getDefaultStation())) {index=i; break;}
		if (index<0 && stations.length>0) index=0;
		if (index>=0) defaultStation.setSelectedIndex(index);

		/* Panel starten */
		mode.addActionListener(e->cardLayout.show(cardPanel,""+mode.getSelectedIndex()));

		switch (data.getMode()) {
		case ROUTING_MODE_EXPLICITE: mode.setSelectedIndex(0); break;
		case ROUTING_MODE_SEQUENCE: mode.setSelectedIndex(1); break;
		case ROUTING_MODE_TEXT_PROPERTY: mode.setSelectedIndex(2); break;
		default: mode.setSelectedIndex(0); break;
		}
	}

	/**
	 * Prüft, ob die Einstellungen in Ordnung sind und gibt im Fehlerfall eine Fehlermeldung aus.
	 * @return	Gibt <code>true</code> zurück, wenn die Einstellungen in Ordnung sind.
	 */
	public boolean checkData() {
		return true; /* Es gibt nichts zu prüfen. */
	}

	/**
	 * Schreibt die möglicherweise veränderten Daten in das
	 * im Konstruktor übergebene {@link TransportTargetSystem}-Objekt zurück.
	 */
	public void storeData() {
		/* Modus */
		switch (mode.getSelectedIndex()) {
		case 0: data.setMode(TransportTargetSystem.RoutingMode.ROUTING_MODE_EXPLICITE); break;
		case 1: data.setMode(TransportTargetSystem.RoutingMode.ROUTING_MODE_SEQUENCE); break;
		case 2: data.setMode(TransportTargetSystem.RoutingMode.ROUTING_MODE_TEXT_PROPERTY); break;
		}

		/* Tabelle */
		data.getRouting().clear();
		tableRouting.storeData(data.getRouting());

		/* Property */
		data.setRoutingProperty(propertyName.getText().trim());

		/* Standardziel */
		if (defaultStation.getSelectedIndex()>=0) data.setDefaultStation(stations[defaultStation.getSelectedIndex()]);
	}
}