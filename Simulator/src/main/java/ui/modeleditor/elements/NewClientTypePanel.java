/**
 * Copyright 2025 Alexander Herzog
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Eingabe-Panel zur Definition der neuen Kundentypennamen
 * an einer {@link ModelElementAssignMulti}-Station
 * @see ModelElementAssignMulti
 */
public class NewClientTypePanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5890417115355901069L;

	/**
	 * Nur-Lese-Status
	 */
	private final boolean readOnly;

	/**
	 * Auswahlfeld für die Anzahl der Kundentypen
	 */
	private SpinnerModel countField;

	/**
	 * Liste der vor dem Aufruf des Dialogs bereits existierenden Kundentypennamen (als Vorschlagswerte für die Comboboxen)
	 */
	private final List<String> existingClientTypeNames;

	/**
	 * Dialogzeilen, in denen sich die Auswahlboxen für die Kundentypennamen befinden
	 * @see #buildEditors(List)
	 */
	private List<JPanel> clientNameLines;

	/**
	 * Eingabefelder für die Kundentypennamen
	 */
	private List<JComboBox<String>> clientNameCombos;

	/**
	 * Dialogbereich, der die Eingabefelder für die Kundentypennamen aufnimmt
	 */
	private JPanel mainPanel;

	/**
	 * Konstruktor
	 * @param model	Modell
	 * @param readOnly	Nur-Lese-Status
	 * @param initialClientTypes	Liste der anfänglich anzuzeigenden Kundentypennamen
	 * @param existingClientTypeNames	Liste der vor dem Aufruf des Dialogs bereits existierenden Kundentypennamen (als Vorschlagswerte für die Comboboxen)
	 */
	public NewClientTypePanel(final EditModel model, final boolean readOnly, final List<String> initialClientTypes, final List<String> existingClientTypeNames) {
		setLayout(new BorderLayout());
		this.readOnly=readOnly;
		this.existingClientTypeNames=(existingClientTypeNames==null)?List.of():existingClientTypeNames;
		clientNameLines=new ArrayList<>();
		clientNameCombos=new ArrayList<>();

		JLabel label;

		/* Anzahl an Kundentypen */
		final JPanel setup=new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(setup,BorderLayout.NORTH);
		setup.add(label=new JLabel(Language.tr("Surface.AssignMulti.Dialog.NumberOfClientTypes")+":"));
		final JSpinner countSpinner=new JSpinner(countField=new SpinnerNumberModel(1,1,99,1));
		final JSpinner.NumberEditor countEditor=new JSpinner.NumberEditor(countSpinner);
		countEditor.getFormat().setGroupingUsed(false);
		countEditor.getTextField().setColumns(2);
		countSpinner.setEditor(countEditor);
		setup.add(countSpinner);
		label.setLabelFor(countSpinner);
		countSpinner.setValue((initialClientTypes==null)?1:Math.max(1,initialClientTypes.size()));
		countEditor.setEnabled(!readOnly);
		countSpinner.setEnabled(!readOnly);

		/* Bereich für die Kundentypennamen */
		final JPanel mainPanelOuter=new JPanel(new BorderLayout());
		add(new JScrollPane(mainPanelOuter),BorderLayout.CENTER);
		mainPanelOuter.add(mainPanel=new JPanel(),BorderLayout.NORTH);
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));

		buildEditors(initialClientTypes);

		countSpinner.addChangeListener(e->buildEditors(null));
	}

	/**
	 * Wird gesetzt, während {@link #buildEditors(List)} läuft, um zu verhindern,
	 * dass die Eingabefelder programmgesteuerte Initialisierungen für
	 * Nutzereingaben halten.
	 * @see #buildEditors(List)
	 */
	private boolean updateDataRunning=false;

	/**
	 * Aktualisiert die Liste der Eingabefelder
	 * @param initialClientTypes	Zusätzlich einzutragende Daten
	 */
	private void buildEditors(final List<String> initialClientTypes) {
		updateDataRunning=true;
		try {
			final int count=(Integer)countField.getValue();
			final int additional=(initialClientTypes==null)?0:initialClientTypes.size();

			/* Prüfen: Hat sich überhaupt etwas geändert? Wenn nicht, Methode verlassen, ohne Update auszulösen */
			if (count+additional==clientNameCombos.size()) return;

			/* Bisherige Einstellungen aus GUI sichern */
			final List<String> oldClientTypesList=new ArrayList<>();
			for (int i=0;i<Math.min(clientNameCombos.size(),count);i++) {
				final JComboBox<String> comboBox=clientNameCombos.get(i);
				oldClientTypesList.add((String)comboBox.getEditor().getItem());
			}

			/* Überzählige Einträge entfernen */
			while (clientNameCombos.size()>Math.max(count,additional)) {
				final int lastIndex=clientNameLines.size()-1;
				mainPanel.remove(clientNameLines.get(lastIndex));
				clientNameLines.remove(lastIndex);
				clientNameCombos.remove(lastIndex);
			}

			/* Neue Einträge anlegen */
			while (clientNameCombos.size()<Math.max(count,additional)) {
				final Object[] data=ModelElementBaseDialog.getComboBoxPanel(String.format(Language.tr("Surface.AssignMulti.Dialog.ClientTypeNr"),clientNameCombos.size()+1)+":",existingClientTypeNames);
				final JPanel panel=(JPanel)data[0];
				@SuppressWarnings("unchecked")
				final JComboBox<String> comboBox=(JComboBox<String>)data[1];
				comboBox.setEditable(!readOnly);
				mainPanel.add(panel);
				final JButton button=new JButton();
				button.setIcon(Images.EDIT_DELETE.getIcon());
				button.setToolTipText(Language.tr("Surface.AssignMulti.Dialog.RemoveClientType"));
				button.addActionListener(e->removeClientType(comboBox));
				button.setEnabled(!readOnly);
				panel.add(button);
				clientNameLines.add(panel);
				comboBox.setMinimumSize(new Dimension(250,comboBox.getMinimumSize().height));
				comboBox.setPreferredSize(new Dimension(250,comboBox.getPreferredSize().height));
				clientNameCombos.add(comboBox);
				comboBox.setEnabled(!readOnly);
				comboBox.addActionListener(e->{if (!updateDataRunning) {checkData(false); fireDataChanged();}});
				final ComboBoxEditor editor=comboBox.getEditor();
				editor.addActionListener(e->{if (!updateDataRunning) {checkData(false); fireDataChanged();}});
				editor.getEditorComponent().addKeyListener(new KeyAdapter() {
					@Override public void keyReleased(KeyEvent e) {checkData(false); fireDataChanged();}
				});
				((JTextField)(editor.getEditorComponent())).setOpaque(true);
			}

			/* Einstellungen in GUI schreiben */
			for (int i=0;i<oldClientTypesList.size();i++) {
				final JComboBox<String> comboBox=clientNameCombos.get(i);
				comboBox.getEditor().setItem(oldClientTypesList.get(i));
			}
			if (initialClientTypes!=null) for (int i=0;i<additional;i++) {
				final JComboBox<String> comboBox=clientNameCombos.get(oldClientTypesList.size()+i);
				comboBox.getEditor().setItem(initialClientTypes.get(i));
			}

			mainPanel.setVisible(false);
			mainPanel.setVisible(true);
		} finally {
			updateDataRunning=false;
		}

		checkData(false);
		fireDataChanged();
	}

	/**
	 * Entfernt eine Ziel-Zeile.
	 * @param comboBox	Zugehörige Auswahlbox
	 */
	private void removeClientType(final JComboBox<String> comboBox) {
		final int nr=clientNameCombos.indexOf(comboBox);
		if (nr<0) return;

		if (clientNameLines.size()==1) {
			MsgBox.error(this,Language.tr("Surface.AssignMulti.Dialog.RemoveClientType"),Language.tr("Surface.AssignMulti.Dialog.RemoveClientType.Error"));
			return;
		}

		/* Zeile entfernen */
		mainPanel.remove(clientNameLines.get(nr));
		clientNameLines.remove(nr);
		clientNameCombos.remove(nr);

		/* Spinner-Wert reduzieren */
		updateDataRunning=true;
		try {
			countField.setValue(clientNameLines.size());
		} finally {
			updateDataRunning=false;
		}

		/* Beschriftungen anpassen */
		int count=0;
		for (JPanel line: clientNameLines) {
			for (Component comp: line.getComponents()) if (comp instanceof JLabel) {
				((JLabel)comp).setText(String.format(Language.tr("Surface.AssignMulti.Dialog.ClientTypeNr")+":",++count));
			}
		}
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	public boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;
		for (int i=0;i<clientNameCombos.size();i++) {
			final ComboBoxEditor editor=clientNameCombos.get(i).getEditor();
			if (((String)editor.getItem()).isBlank()) {
				if (ok && showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.AssignMulti.Dialog.ClientType.ErrorTitle"),Language.tr("Surface.AssignMulti.Dialog.ClientType.ErrorInfo"));
				}
				editor.getEditorComponent().setBackground(Color.RED);
				ok=false;
			} else {
				editor.getEditorComponent().setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}
		return ok;
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
	 * Liefert die Liste der aktuell eingestellten Kundentypennamen
	 * @return	Liste der Kundentypennamen
	 */
	public List<String> getNewClientTypes() {
		return new ArrayList<>(clientNameCombos.stream().map(combo->(String)combo.getEditor().getItem()).collect(Collectors.toList()));
	}
}
