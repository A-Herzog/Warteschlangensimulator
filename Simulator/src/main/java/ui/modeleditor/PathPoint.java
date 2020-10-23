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

import java.awt.Container;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementWayPoint;

/**
 * Enthält einen Pfadpunkt (zur Festlegung von Wegen für Transporter)
 * der in {@link PathEditorDialog} intern verwendet wird.
 * @author Alexander Herzog
 * @see PathEditorDialog
 */
public class PathPoint {
	private final ModelElement element;

	private final String name;
	private final String nameLong;

	private final List<PathPoint> options;

	private final JPanel panel;
	private final JPanel editArea;
	private final List<PathPointEntry> list;
	private final List<Runnable> changeListeners;

	/**
	 * Konstruktor der Klasse
	 * @param element	Station die durch dieses Objekt repräsentiert werden soll
	 * @param mainSurface	Haupt-Zeichenfläche (zum Auslesen des vollständigen Namens der Station)
	 */
	public PathPoint(final ModelElement element, final ModelSurface mainSurface) {
		this.element=element;
		name=element.getName();
		nameLong=buildName(mainSurface);
		options=new ArrayList<>();
		list=new ArrayList<>();
		changeListeners=new ArrayList<>();

		panel=new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		line.add(new JLabel(String.format(Language.tr("PathEditor.NextStationsFor"),encodeHTML(nameLong))));
		editArea=new JPanel();
		editArea.setLayout(new BoxLayout(editArea,BoxLayout.PAGE_AXIS));
		panel.add(editArea);
		panel.add(Box.createVerticalStrut(15));
	}

	private String buildName(final ModelSurface mainSurface) {
		ModelElementSub parent=null;
		if (element.getSurface().getParentSurface()!=null) {
			for (ModelElement test1: mainSurface.getElements()) if (test1 instanceof ModelElementSub) {
				for (ModelElement test2: ((ModelElementSub)test1).getSubSurface().getElements()) {
					if (test2==element) {parent=(ModelElementSub)test1; break;}
				}
				if (parent!=null) break;
			}
		}

		final String name=element.getName();
		if (parent!=null) {
			String parentName=parent.getName().trim();
			if (parentName.isEmpty() && parent instanceof ModelElementBox) parentName=parent.getTypeName();
			return name+" (id="+element.getId()+") in "+parentName+" (id="+parent.getId()+")";
		} else {
			return name+" (id="+element.getId()+")";
		}
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param text	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private String encodeHTML(final String text) {
		return text.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;");
	}

	/**
	 * Mögliche Pfade initial einrichten
	 * @param data	Liste mit allen für Pfade verfügbaren Stationen
	 */
	public void initOptions(final List<PathPoint> data) {
		final ModelSurface ownElementSurface=element.getSurface();
		options.clear();


		/* Elemente aus anderen Ebenen und this rausfiltern */
		Stream<PathPoint> stream=data.stream().filter(p->p.getElement().getSurface()==ownElementSurface).filter(p->p!=this);

		if (element instanceof ModelElementWayPoint) {
			/* Auf Wegpunkte können Wegpunkte oder Stationen folgen */
		} else {
			/* Auf Stationen können nur Wegpunkte folgen (für Station->Station brauchen wir keinen Pfad definieren) */
			stream=stream.filter(p->p.element instanceof ModelElementWayPoint);
		}

		/* Sortieren */
		stream=stream.sorted((p1,p2)->p1.getName().compareTo(p2.getName()));

		options.addAll(stream.collect(Collectors.toList()));

		updateEditArea(false);
	}

	/**
	 * Liefert den Namen der zugehörigen Station
	 * @return	Name der zugehörigen Station
	 */
	public String getName() {
		return name;
	}

	/**
	 * Liefert den vollständigen Namen der zugehörigen Station
	 * @return	Vollständiger Name der zugehörigen Station
	 */
	public String getLongName() {
		return nameLong;
	}

	/**
	 * Liefert die im Konstruktor angegebene zugehörige Station
	 * @return	Station auf die sich dieses Objekt bezieht
	 */
	public ModelElement getElement() {
		return element;
	}

	/**
	 * Liefert die Einstellungen als Panel
	 * @return	Einstellungen als Panel
	 */
	public JPanel getPanel() {
		return panel;
	}

	private void updateEditArea(final boolean isReset) {
		/* Liste komplett leer oder letzter Wert nicht auf Vorgabe */
		if (list.size()==0 || list.get(list.size()-1).getSelected()!=null) {
			final PathPointEntry entry=new PathPointEntry(this,options);
			entry.addChangeListener(()->updateEditArea(false));
			list.add(entry);
			editArea.add(entry.getPanel());
			if (!isReset) fireChangeListeners();
		}

		/* Leere Einträge entfernen */
		int index=0;
		while (index<list.size()-1) {
			if (list.get(index).getSelected()==null) {
				final PathPointEntry entry=list.remove(index);
				editArea.remove(entry.getPanel());
				fireChangeListeners();
			} else {
				index++;
			}
		}
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

	private void fireChangeListeners() {
		Container c=editArea;
		while (c!=null) {
			c.validate();
			c.repaint();
			c=c.getParent();
		}

		for (Runnable listener: changeListeners) listener.run();
	}

	/**
	 * Setzt alle Einstellungen zurück.
	 */
	public void reset() {
		list.clear();
		editArea.removeAll();
		updateEditArea(true);
	}

	/**
	 * Stellt eine Liste mit den nächsten möglichen Stationsnamen ein.
	 * @param nextNames	Liste mit den nächsten möglichen Stationsnamen
	 * @see #getNextNamesList()
	 */
	public void setNextNamesList(final List<String> nextNames) {
		list.clear();
		editArea.removeAll();
		for (String name: nextNames) {
			PathPoint next=null;
			for (PathPoint test: options) if (test.getName().equals(name)) {next=test; break;}
			if (next!=null) {
				final PathPointEntry entry=new PathPointEntry(this,options,next);
				entry.addChangeListener(()->updateEditArea(false));
				list.add(entry);
				editArea.add(entry.getPanel());
			}
		}
		updateEditArea(false);
	}

	/**
	 * Aktualisiert den Status der nächste Stationen Checkboxen
	 */
	public void setNextNamesListCheckBoxes() {
		for (PathPointEntry entry: list) entry.testReverse();
	}

	/**
	 * Liefert die Liste mit den nächsten möglichen Stationsnamen.
	 * @return	Liste mit den nächsten möglichen Stationsnamen
	 */
	public List<String> getNextNamesList() {
		final List<String> list=new ArrayList<>();
		for (PathPointEntry entry: this.list) if (entry.getSelected()!=null) list.add(entry.getSelected().getName());
		return list;
	}

	/**
	 * Fügt die eigenen Daten zum Pfad-Builder hinzu
	 * @param builder	Pfad-Builder, der ergänzt werden soll
	 * @see PathBuilder
	 */
	public void addToBuilder(final PathBuilder builder) {
		final List<ModelElement> list=new ArrayList<>();

		for (PathPointEntry entry: this.list) {
			final PathPoint next=entry.getSelected();
			if (next!=null) {
				final ModelElement nextElement=next.getElement();
				if (!list.contains(nextElement)) list.add(nextElement);
			}
		}

		if (list.size()>0) builder.add(element,list);
	}

	/**
	 * Gibt an, ob das eigene Objekt über einen Pfad zu einer anderen Station verbunden ist
	 * @param point	Station deren Erreichbarkeit geprüft werden soll
	 * @return	Gibt <code>true</code> zurück, wenn das Ziel über einen hier definierten Pfad erreichbar ist
	 */
	public boolean isConnectedTo(final PathPoint point) {
		for (PathPointEntry entry: list) if (entry.getSelected()==point) return true;
		return false;
	}

	private void setConnectedOn(final PathPoint point) {
		for (PathPointEntry entry: list) if (entry.getSelected()==point) return;

		list.get(list.size()-1).setSelected(point);
		updateEditArea(false);
	}

	private void setConnectedOff(final PathPoint point) {
		int index=0;
		while (index<list.size()) if (list.get(index).getSelected()==point) {
			final PathPointEntry entry=list.remove(index);
			editArea.remove(entry.getPanel());
			fireChangeListeners();
		} else {
			index++;
		}
		updateEditArea(false);
	}

	/**
	 * Stellt ein, dass ein Ziel über diese Station erreicht werden soll.
	 * @param point	Ziel das über diese Station erreicht werden soll
	 * @param active	Verbindung aktivieren (<code>true</code>) oder deaktivieren (<code>false</code>)
	 */
	public void setConnectedTo(final PathPoint point, final boolean active) {
		if (active) setConnectedOn(point); else setConnectedOff(point);
	}
}