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
package ui.modeleditor;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import language.Language;

/**
 * Diese Klasse stellt einen Eintrag auf einem
 * möglichen temporären Pfad dar.
 * @author Alexander Herzog
 * @see PathPoint
 */
public class PathPointEntry {
	/**
	 * Ausgangsstation
	 */
	private final PathPoint point;

	/**
	 * Mögliche Zwischenstationen
	 */
	private final List<PathPoint> options;

	/**
	 * Panel in dem Auswahlbox und Checkbox angezeigt werden sollen
	 * @see #getPanel()
	 */
	private final JPanel panel;

	/** Auswahlfeld für die möglichen nächsten Stationen */
	private final JComboBox<String> combo;
	/** Option: Gegenrichtung auch verwenden */
	private final JCheckBox check;

	/**
	 * Liste der Listener, die über Änderungen benachrichtigt werden sollen
	 */
	private final List<Runnable> changeListeners;

	/**
	 * Handelt es sich um eine interne Änderung von Daten, die keine Aktionen auslösen soll?
	 */
	private boolean isInternalChange;

	/**
	 * Konstruktor der Klasse
	 * @param point	Ausgangsstation
	 * @param options	Mögliche Zwischenstationen
	 * @param select	Zielstation
	 */
	public PathPointEntry(final PathPoint point, final List<PathPoint> options, final PathPoint select) {
		this.point=point;
		this.options=options;
		isInternalChange=false;

		panel=new JPanel(new FlowLayout(FlowLayout.LEFT));

		combo=new JComboBox<>(getNextStationNames());
		if (select!=null && options.contains(select)) {
			combo.setSelectedIndex(1+options.indexOf(select));
		} else {
			combo.setSelectedIndex(0);
		}
		combo.addActionListener(e->selectionChanged());

		check=new JCheckBox(Language.tr("PathEditor.InverseDirectionToo"),false);
		check.setEnabled(combo.getSelectedIndex()>0);
		check.addActionListener(e->checkChanged());

		panel.add(combo);
		panel.add(check);

		changeListeners=new ArrayList<>();
	}

	/**
	 * Konstruktor der Klasse
	 * @param point	Ausgangsstation
	 * @param options	Mögliche Zwischenstationen
	 */
	public PathPointEntry(final PathPoint point, final List<PathPoint> options) {
		this(point,options,null);
	}

	/**
	 * Liefert die Namen der möglichen Folgestationen.
	 * @return	Liste der Namen der möglichen Folgestationen
	 * @see #combo
	 */
	private String[] getNextStationNames() {
		final List<String> list=options.stream().map(p->p.getLongName()).collect(Collectors.toList());
		list.add(0,Language.tr("PathEditor.NoSelection"));
		return list.toArray(new String[0]);
	}

	/**
	 * Wird aufgerufen, wenn sich die Auswahl geändert hat,
	 * um die weiteren GUI-Elemente zu aktualisieren.
	 * @see #combo
	 */
	private void selectionChanged() {
		for (PathPoint option: options) option.setNextNamesListCheckBoxes();
		check.setEnabled(combo.getSelectedIndex()>0);
		testReverse();
		fireChangeListeners();
	}

	/**
	 * Wird aufgerufen, wenn die Checkbox, ob die Gegenrichtung
	 * auch entsprechend einbezogen werden soll, geändert wurde.
	 * @see #check
	 */
	private void checkChanged() {
		final int index=combo.getSelectedIndex();
		if (index<1) return;
		final PathPoint connection=options.get(index-1);
		connection.setConnectedTo(point,check.isSelected());
	}

	/**
	 * Liefert die als nächsten Pfadschritt gewählte Station
	 * @return	Als nächsten Pfadschritt gewählte Station (kann <code>null</code> sein)
	 */
	public PathPoint getSelected() {
		final int index=combo.getSelectedIndex();
		if (index<1) return null;
		return options.get(index-1);
	}

	/**
	 * Stellt die Station für den nächsten Pfadschritt ein
	 * @param point	Station für den nächsten Pfadschritt ein
	 */
	public void setSelected(final PathPoint point) {
		int index=-1;
		if (point!=null) index=options.indexOf(point);
		if (index<0) index=0;

		boolean isInternal=isInternalChange;
		isInternalChange=true;
		try {
			combo.setSelectedIndex(index+1);
			check.setEnabled(combo.getSelectedIndex()>0);
			if (index>=0) check.setSelected(true);
		} finally {
			isInternalChange=isInternal;
		}
	}

	/**
	 * Liefert die Einstellungen als Panel.
	 * @return	Einstellungen als Panel
	 */
	public JPanel getPanel() {
		return panel;
	}

	/**
	 * Fügt einen Listener zu der Liste der bei Änderungen zu benachrichtigenden Listener hinzu.
	 * @param changeListener	Zusätzlicher zu benachrichtigender Listener
	 */
	public void addChangeListener(final Runnable changeListener) {
		if (!changeListeners.contains(changeListener)) changeListeners.add(changeListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der bei Änderungen zu benachrichtigenden Listener.
	 * @param changeListener	Nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener aus der Liste entfernt werden konnte
	 */
	public boolean removeChangeListener(final Runnable changeListener) {
		return changeListeners.remove(changeListener);
	}

	/**
	 * Benachrichtigt alle Listener, die über Änderungen benachrichtigt werden sollen.
	 * @see #changeListeners
	 */
	private void fireChangeListeners() {
		for (Runnable listener: changeListeners) listener.run();
	}

	/**
	 * Passt die Verbindung in die umgekehrte Richtung an.
	 */
	public void testReverse() {
		boolean isInternal=isInternalChange;
		isInternalChange=true;
		try {
			if (combo.getSelectedIndex()<1) {
				check.setEnabled(false);
				check.setSelected(false);
				return;
			}
			check.setEnabled(true);
			check.setSelected(options.get(combo.getSelectedIndex()-1).isConnectedTo(point));
		} finally {
			isInternalChange=isInternal;
		}
	}
}