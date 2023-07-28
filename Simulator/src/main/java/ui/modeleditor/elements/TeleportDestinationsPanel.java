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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dieses Panel bietet die Auswahl von mehreren Teleportzielen an.
 * @author Alexander Herzog
 * @see ModelElementTeleportSourceMultiDialog
 * @see ModelElementDecideAndTeleportDialog
 */
public class TeleportDestinationsPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=2596698785704874675L;

	/**
	 * Nur-Lese-Status
	 */
	private final boolean readOnly;

	/**
	 * Namen der möglichen Teleportziele
	 * @see #getTeleportDestinations(EditModel)
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
	 * @see #updateData()
	 */
	private List<String> destinations;

	/**
	 * Liste der Vielfachheiten für die Ziele (kann <code>null</code> sein)
	 * @see #updateData()
	 */
	private List<Integer> destinationsMultiplicity;

	/**
	 * Dialogzeilen, in denen sich die Auswahlboxen für die Ziele befinden
	 * @see #updateData()
	 */
	private List<JPanel> destinationsLines;

	/**
	 * Auswahlboxen für die Ziele
	 * @see #updateData()
	 */
	private List<JComboBox<String>> destinationsComboBoxes;

	/**
	 * Eingabefelder für die Vielfacherheiten der Ziele (kann <code>null</code> sein, dann gibt es keine Vielfachheiten)
	 * @see #updateData()
	 */
	private List<SpinnerModel> destinationsMultiplicitySpinners;

	/**
	 * Konstruktor der Klasse
	 * @param destinations	Bisher gewählte Ziele (darf leer oder <code>null</code> sein)
	 * @param model	Editor-Modell aus dem die Namen der möglichen Ziele ausgelesen werden
	 * @param readOnly	Nur-Lese-Status
	 */
	public TeleportDestinationsPanel(final List<String> destinations, final EditModel model, final boolean readOnly) {
		this(destinations,null,model,readOnly);
	}

	/**
	 * Konstruktor der Klasse
	 * @param destinations	Bisher gewählte Ziele (darf leer oder <code>null</code> sein)
	 * @param destinationsMultiplicity	Vielfachheiten mit denen die bisherigen Ziele angesteuert werden (darf <code>null</code> sein, dann gibt es keine Vielfachheiten; muss ansonsten von der Länge der Zieleliste entsprechen)
	 * @param model	Editor-Modell aus dem die Namen der möglichen Ziele ausgelesen werden
	 * @param readOnly	Nur-Lese-Status
	 */
	public TeleportDestinationsPanel(final List<String> destinations, final List<Integer> destinationsMultiplicity, final EditModel model, final boolean readOnly) {
		setLayout(new BorderLayout());
		this.readOnly=readOnly;
		destinationNames=getTeleportDestinations(model);

		JLabel label;

		this.destinations=new ArrayList<>();
		if (destinations!=null) this.destinations.addAll(destinations);

		if (destinationsMultiplicity==null) {
			this.destinationsMultiplicity=null;
		} else {
			this.destinationsMultiplicity=new ArrayList<>();
			this.destinationsMultiplicity.addAll(destinationsMultiplicity);
			destinationsMultiplicitySpinners=new ArrayList<>();
		}

		destinationsLines=new ArrayList<>();
		destinationsComboBoxes=new ArrayList<>();

		/* Anzahl an Ausgängen */
		final JPanel setup=new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(setup,BorderLayout.NORTH);
		setup.add(label=new JLabel(Language.tr("Surface.TeleportSourceMulti.Dialog.DestinationCount")+":"));
		final JSpinner countSpinner=new JSpinner(countField=new SpinnerNumberModel(1,1,99,1));
		final JSpinner.NumberEditor countEditor=new JSpinner.NumberEditor(countSpinner);
		countEditor.getFormat().setGroupingUsed(false);
		countEditor.getTextField().setColumns(2);
		countSpinner.setEditor(countEditor);
		setup.add(countSpinner);
		label.setLabelFor(countSpinner);
		countSpinner.setValue(Math.max(1,this.destinations.size()));
		countEditor.setEnabled(!readOnly);
		countSpinner.setEnabled(!readOnly);

		/* Bereich für die Zielauswahl */
		final JPanel mainPanelOuter=new JPanel(new BorderLayout());
		add(new JScrollPane(mainPanelOuter),BorderLayout.CENTER);
		mainPanelOuter.add(mainPanel=new JPanel(),BorderLayout.NORTH);
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
		updateData();

		countSpinner.addChangeListener(e->updateData());
	}

	/**
	 * Liefert eine Liste mit allen verfügbaren Teleport-Zielen.
	 * @param model	Editor-Modell aus dem die Namen der möglichen Ziele ausgelesen werden
	 * @return	Liste mit allen verfügbaren Teleport-Zielen
	 */
	private List<String> getTeleportDestinations(final EditModel model) {
		final Set<String> destinations=new HashSet<>();

		for (ModelElement e1: model.surface.getElements()) {
			if (e1 instanceof ModelElementTeleportDestination && !e1.getName().trim().isEmpty()) destinations.add(e1.getName());
			if (e1 instanceof ModelElementSub) for (ModelElement e2: ((ModelElementSub)e1).getSubSurface().getElements()) {
				if (e2 instanceof ModelElementTeleportDestination && !e2.getName().trim().isEmpty()) destinations.add(e2.getName());
			}
		}

		return destinations.stream().sorted().collect(Collectors.toList());
	}

	/**
	 * Liefert eine Zuordnung von Teleport-Ziel-Namen zu den zugehörigen Elementen.
	 * @param model	Modell, dem die Daten entnommen werden sollen
	 * @return	Zuordnung von Teleport-Ziel-Namen zu den zugehörigen Elementen
	 */
	public static Map<String,ModelElementTeleportDestination> getAllDestinations(final EditModel model) {
		final Map<String, ModelElementTeleportDestination> map=new HashMap<>();

		for (ModelElement e1: model.surface.getElements()) {
			if (e1 instanceof ModelElementTeleportDestination && !e1.getName().trim().isEmpty()) map.put(e1.getName(),(ModelElementTeleportDestination)e1);
			if (e1 instanceof ModelElementSub) for (ModelElement e2: ((ModelElementSub)e1).getSubSurface().getElements()) {
				if (e2 instanceof ModelElementTeleportDestination && !e2.getName().trim().isEmpty()) map.put(e2.getName(),(ModelElementTeleportDestination)e2);
			}
		}

		return map;
	}

	/**
	 * Wird gesetzt, während {@link #updateData()} läuft, um zu verhindern,
	 * dass die ComboBoxen programmgesteuerte Initialisierungen für
	 * Nutzereingaben halten.
	 * @see #updateData()
	 */
	private boolean updateDataRunning=false;

	/**
	 * Synchronisiert {@link #destinations} und die Dialogelemente
	 */
	private void updateData() {
		updateDataRunning=true;
		try {
			final int count=(Integer)countField.getValue();

			/* Prüfen: Hat sich überhaupt etwas geändert? Wenn nicht, Methode verlassen, ohne Update auszulösen */
			if (count==destinations.size() && destinationsComboBoxes.size()==count) {
				boolean changed=false;
				for (int i=0;i<count;i++) {
					final JComboBox<String> comboBox=destinationsComboBoxes.get(i);
					String dest="";
					if (comboBox.getItemCount()>0 && comboBox.getSelectedIndex()>=0 && comboBox.getSelectedIndex()<destinationNames.size()) dest=destinationNames.get(comboBox.getSelectedIndex());
					if (!dest.equals(destinations.get(i))) {changed=true; break;}
					if (destinationsMultiplicitySpinners!=null) {
						final int muliplicity=(Integer)destinationsMultiplicitySpinners.get(i).getValue();
						if (muliplicity!=destinationsMultiplicity.get(i)) {changed=true; break;}
					}
				}
				if (!changed) return;
			}

			/* Bisherige Einstellungen aus GUI sichern */
			for (int i=0;i<Math.min(destinations.size(),destinationsComboBoxes.size());i++) {
				final JComboBox<String> comboBox=destinationsComboBoxes.get(i);
				final SpinnerModel spinner=(destinationsMultiplicitySpinners!=null)?destinationsMultiplicitySpinners.get(i):null;
				if (comboBox.getItemCount()==0) {
					destinations.set(i,"");
					if (spinner!=null) destinationsMultiplicity.set(i,0);
				} else {
					if (comboBox.getSelectedIndex()>=0) destinations.set(i,destinationNames.get(comboBox.getSelectedIndex())); else destinations.set(i,destinationNames.get(0));
					if (spinner!=null) destinationsMultiplicity.set(i,(Integer)spinner.getValue());
				}
			}

			/* Überzählige Einträge entfernen */
			while (destinationsLines.size()>count) {
				final int lastIndex=destinationsLines.size()-1;
				mainPanel.remove(destinationsLines.get(lastIndex));
				destinationsLines.remove(lastIndex);
				destinationsComboBoxes.remove(lastIndex);
				if (destinationsMultiplicitySpinners!=null) destinationsMultiplicitySpinners.remove(lastIndex);
			}
			while (destinations.size()>count) destinations.remove(destinations.size()-1);
			if (destinationsMultiplicity!=null) while (destinationsMultiplicity.size()>count) destinationsMultiplicity.remove(destinationsMultiplicity.size()-1);

			/* Neue Einträge anlegen */
			while (destinations.size()<count) destinations.add((destinationNames.size()>0)?destinationNames.get(0):"");
			if (destinationsMultiplicity!=null) while (destinationsMultiplicity.size()<count) destinationsMultiplicity.add(1);
			while (destinationsLines.size()<count) {
				final Object[] data=ModelElementBaseDialog.getComboBoxPanel(String.format(Language.tr("Surface.TeleportSourceMulti.Dialog.DestinationNr")+":",destinationsLines.size()+1),destinationNames);
				final JPanel panel=(JPanel)data[0];
				@SuppressWarnings("unchecked")
				final JComboBox<String> comboBox=(JComboBox<String>)data[1];
				mainPanel.add(panel);
				if (destinationsMultiplicitySpinners!=null) {
					final JLabel label;
					panel.add(label=new JLabel(Language.tr("Surface.TeleportSourceMulti.Dialog.DestinationQuantity")+":"));
					final SpinnerModel countField;
					final JSpinner countSpinner=new JSpinner(countField=new SpinnerNumberModel(1,0,999,1));
					final JSpinner.NumberEditor countEditor=new JSpinner.NumberEditor(countSpinner);
					countEditor.getFormat().setGroupingUsed(false);
					countEditor.getTextField().setColumns(2);
					countSpinner.setEditor(countEditor);
					panel.add(countSpinner);
					label.setLabelFor(countSpinner);
					countSpinner.setValue(Math.max(1,this.destinations.size()));
					countEditor.setEnabled(!readOnly);
					countSpinner.setEnabled(!readOnly);
					destinationsMultiplicitySpinners.add(countField);
				}
				final JButton button=new JButton();
				button.setIcon(Images.EDIT_DELETE.getIcon());
				button.setToolTipText(Language.tr("Surface.TeleportSourceMulti.Dialog.RemoveDestination"));
				button.addActionListener(e->removeTarget(comboBox));
				button.setEnabled(!readOnly);
				panel.add(button);
				destinationsLines.add(panel);
				destinationsComboBoxes.add(comboBox);
				comboBox.setEnabled(!readOnly && destinationNames.size()>0);
				comboBox.addActionListener(e->{
					if (updateDataRunning) return;
					for (int i=0;i<Math.min(destinations.size(),destinationsComboBoxes.size());i++) {
						final JComboBox<String> c=destinationsComboBoxes.get(i);
						if (c.getItemCount()==0) destinations.set(i,""); else destinations.set(i,destinationNames.get(c.getSelectedIndex()));
					}
					fireDataChanged();
				});
			}

			/* Einstellungen in GUI schreiben */
			for (int i=0;i<destinations.size();i++) {
				final int nr=destinationNames.indexOf(destinations.get(i));
				final JComboBox<String> comboBox=destinationsComboBoxes.get(i);
				if (comboBox.getItemCount()>0) comboBox.setSelectedIndex(nr);
				if (destinationsMultiplicity!=null) {
					destinationsMultiplicitySpinners.get(i).setValue(destinationsMultiplicity.get(i));
				}
			}

			mainPanel.setVisible(false);
			mainPanel.setVisible(true);
		} finally {
			updateDataRunning=false;
		}

		fireDataChanged();
	}

	/**
	 * Entfernt eine Ziel-Zeile.
	 * @param comboBox	Zugehörge Auswahlbox
	 */
	private void removeTarget(final JComboBox<String> comboBox) {
		final int nr=destinationsComboBoxes.indexOf(comboBox);
		if (nr<0) return;

		if (destinationsLines.size()==1) {
			MsgBox.error(this,Language.tr("Surface.TeleportSourceMulti.Dialog.RemoveDestination"),Language.tr("Surface.TeleportSourceMulti.Dialog.RemoveDestination.Error"));
			return;
		}

		/* Zeile entfernen */
		mainPanel.remove(destinationsLines.get(nr));
		destinationsLines.remove(nr);
		destinationsComboBoxes.remove(nr);

		/* Spinner-Wert reduzieren */
		updateDataRunning=true;
		try {
			countField.setValue(destinationsLines.size());
		} finally {
			updateDataRunning=false;
		}

		/* Beschriftungen anpassen */
		int count=0;
		for (JPanel line: destinationsLines) {
			for (Component comp: line.getComponents()) if (comp instanceof JLabel) {
				((JLabel)comp).setText(String.format(Language.tr("Surface.TeleportSourceMulti.Dialog.DestinationNr")+":",++count));
			}
		}
	}

	/**
	 * Listener, die bei Änderungen der Einstellungen benachrichtigt werden sollen
	 * @see #fireDataChanged()
	 * @see #addDataChangedListener(Runnable)
	 * @see #removeDataChangedListener(Runnable)
	 */
	private Set<Runnable> dataChangedListeners=new HashSet<>();

	/**
	 * Benachrichtigt alle registrierten Listener, dass sich die Daten im Panel verändert haben
	 * @see #dataChangedListeners
	 */
	private void fireDataChanged() {
		dataChangedListeners.forEach(listener->listener.run());
	}

	/**
	 * Fügt einen neuen Listener zu der Liste der bei Datenänderungen zu benachrichtigenden Listener hinzu.
	 * @param dataChangedListener	Neuer bei Datenänderungen zu benachrichtigender Listener
	 * @return	Liefert <code>true</code>, wenn der Listener zu der Liste hinzugefügt werden konnte
	 * @see #removeDataChangedListener(Runnable)
	 */
	public boolean addDataChangedListener(final Runnable dataChangedListener) {
		return dataChangedListeners.add(dataChangedListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der bei Datenänderungen zu benachrichtigenden Listener.
	 * @param dataChangedListener	Nicht mehr bei Datenänderungen zu benachrichtigender Listener
	 * @return	Liefert <code>true</code>, wenn der Listener aus der Liste entfernt werden konnte
	 * @see #addDataChangedListener(Runnable)
	 */
	public boolean removeDataChangedListener(final Runnable dataChangedListener) {
		return dataChangedListeners.remove(dataChangedListener);
	}

	/**
	 * Liefert die gewählten Ziele
	 * @return	Liste der gewählten Ziele
	 */
	public List<String> getDestinations() {
		updateData();
		return new ArrayList<>(destinations);
	}

	/**
	 * Liefert die gewählten Ziele
	 * @return	Liste der gewählten Ziele
	 */
	public List<Integer> getDestinationsMultiplicity() {
		updateData();
		if (destinationsMultiplicity==null) return destinations.stream().map(s->Integer.valueOf(1)).collect(Collectors.toList());
		return new ArrayList<>(destinationsMultiplicity);
	}
}
