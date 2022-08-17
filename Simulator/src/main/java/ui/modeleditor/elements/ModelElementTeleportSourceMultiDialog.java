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
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;

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
	 * Namen der möglichen Teleportziele
	 * @see #getTeleportDestinations()
	 */
	private List<String> destinationNames;

	/**
	 * Auswahlfeld für die Anzahl der Teleportziele
	 */
	private SpinnerModel countField;

	/**
	 * Dialogbereich, der die Auswahlboxen für die Ziele aufnimmt
	 */
	private JPanel mainPanel;

	/**
	 * Liste der Ziele
	 * @see #updateMainPanel()
	 */
	private List<String> destinations;

	/**
	 * Dialogzeilen, in denen sich die Auswahlboxen für die Ziele befinden
	 * @see #updateMainPanel()
	 */
	private List<JPanel> destinationsLines;

	/**
	 * Auswahlboxen für die Ziele
	 * @see #updateMainPanel()
	 */
	private List<JComboBox<String>> destinationsComboBoxes;

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

	@Override
	protected JComponent getContentPanel() {
		destinationNames=getTeleportDestinations();

		final JPanel content=new JPanel(new BorderLayout());

		JLabel label;

		destinations=new ArrayList<>(((ModelElementTeleportSourceMulti)element).getDestinations());
		destinationsLines=new ArrayList<>();
		destinationsComboBoxes=new ArrayList<>();

		/* Anzahl an Ausgängen */
		final JPanel setup=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(setup,BorderLayout.NORTH);
		setup.add(label=new JLabel(Language.tr("Surface.TeleportSourceMulti.Dialog.DestinationCount")+":"));
		final JSpinner countSpinner=new JSpinner(countField=new SpinnerNumberModel(1,1,99,1));
		final JSpinner.NumberEditor countEditor=new JSpinner.NumberEditor(countSpinner);
		countEditor.getFormat().setGroupingUsed(false);
		countEditor.getTextField().setColumns(2);
		countSpinner.setEditor(countEditor);
		setup.add(countSpinner);
		label.setLabelFor(countSpinner);
		countSpinner.setValue(Math.max(1,destinations.size()));
		countEditor.setEnabled(!readOnly);
		countSpinner.setEnabled(!readOnly);

		/* Bereich für die Zielauswahl */
		content.add(new JScrollPane(mainPanel=new JPanel()),BorderLayout.CENTER);
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
		updateMainPanel();

		countSpinner.addChangeListener(e->updateMainPanel());

		return content;
	}

	/**
	 * Synchronisiert {@link #destinations} und die Dialogelemente
	 */
	private void updateMainPanel() {
		final int count=(Integer)countField.getValue();

		/* Bisherige Einstellungen aus GUI sichern */
		for (int i=0;i<Math.min(destinations.size(),destinationsComboBoxes.size());i++) {
			final JComboBox<String> comboBox=destinationsComboBoxes.get(i);
			if (comboBox.getItemCount()==0) destinations.set(i,""); else destinations.set(i,destinationNames.get(comboBox.getSelectedIndex()));
		}

		/* Überzählige Einträge entfernen */
		while (destinationsLines.size()>count) {
			final int lastIndex=destinationsLines.size()-1;
			mainPanel.remove(destinationsLines.get(lastIndex));
			destinationsLines.remove(lastIndex);
			destinationsComboBoxes.remove(lastIndex);
		}
		while (destinations.size()>count) destinations.remove(destinations.size()-1);

		/* Neue Einträge anlegen */
		while (destinations.size()<count) destinations.add((destinationNames.size()>0)?destinationNames.get(0):"");
		while (destinationsLines.size()<count) {
			final Object[] data=getComboBoxPanel(String.format(Language.tr("Surface.TeleportSourceMulti.Dialog.DestinationNr")+":",destinationsLines.size()+1),destinationNames);
			final JPanel panel=(JPanel)data[0];
			@SuppressWarnings("unchecked")
			final JComboBox<String> comboBox=(JComboBox<String>)data[1];
			mainPanel.add(panel);
			destinationsLines.add(panel);
			destinationsComboBoxes.add(comboBox);
			comboBox.setEnabled(!readOnly && destinationNames.size()>0);
		}

		/* Einstellungen in GUI schreiben */
		for (int i=0;i<destinations.size();i++) {
			final int nr=destinationNames.indexOf(destinations.get(i));
			final JComboBox<String> comboBox=destinationsComboBoxes.get(i);
			if (comboBox.getItemCount()>0) comboBox.setSelectedIndex(nr);
		}

		mainPanel.setVisible(false);
		mainPanel.setVisible(true);
	}

	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementTeleportSourceMulti) {
			updateMainPanel();
			((ModelElementTeleportSourceMulti)element).getDestinations().clear();
			((ModelElementTeleportSourceMulti)element).getDestinations().addAll(destinations);
		}
	}
}
