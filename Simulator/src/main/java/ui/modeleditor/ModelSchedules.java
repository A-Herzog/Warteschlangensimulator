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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;

/**
 * Diese Klasse kapselt die Zeitpläne innerhalb eines Modells.
 * @author Alexander Herzog
 * @see EditModel#schedules
 */
public final class ModelSchedules implements Cloneable {
	/**
	 * Name des XML-Elements, das die Zeitpläne enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"ModellZeitplaene"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * Liste der einzelnen Zeitpläne
	 */
	private List<ModelSchedule> schedules;

	/**
	 * Konstruktor der Klasse <code>ModelSchedules</code>
	 */
	public ModelSchedules() {
		clear();
	}

	/**
	 * Konstruktor der Klasse <code>ModelSchedules</code>
	 * @param schedules	Stellt diese Zeitpläne in der Zeitplanliste ein
	 */
	public ModelSchedules(final List<ModelSchedule> schedules) {
		clear();
		setSchedules(schedules);
	}

	/**
	 * Löscht alle Zeitpläne aus der Zeitplanliste.
	 */
	public void clear() {
		schedules=new ArrayList<>();
	}

	/**
	 * Liefert, basierend auf einer externen Liste an Zeitplänen, eine Liste der Namen aller benannten Zeitpläne
	 * @param schedules	Liste der Zeitpläne, deren Namen zusammengestellt werden sollen
	 * @return	Liste der Namen aller benannten Zeitpläne
	 */
	public static String[] getScheduleNames(final List<ModelSchedule> schedules) {
		final List<String> names=new ArrayList<>();
		for (ModelSchedule schedule: schedules) if (!schedule.getName().isEmpty()) names.add(schedule.getName());
		return names.toArray(String[]::new);
	}

	/**
	 * Liefert eine Liste der Namen aller benannten Zeitpläne
	 * @return	Liste der Namen aller benannten Zeitpläne
	 */
	public String[] getScheduleNames() {
		return getScheduleNames(schedules);
	}

	/**
	 * Liefert zu einem Namen den entsprechenden Zeitplan
	 * @param name	Name, zu dem der Zeitplan aus der Zeitplanliste gesucht werden soll
	 * @return	Liefert den Zeitplan mit dem entsprechenden Namen oder <code>null</code>, wenn es keinen Zeitplan mit diesem Namen gibt
	 */
	public ModelSchedule getSchedule(final String name) {
		for (ModelSchedule schedule: schedules) if (schedule.getName().equals(name)) return schedule;
		return null;
	}

	/**
	 * Liefert eine Liste mit allen Zeitplänen in dem Zeitplanlisten-Objekt
	 * @return	Liste mit allen Zeitplänen
	 */
	public List<ModelSchedule> getSchedules() {
		final List<ModelSchedule> result=new ArrayList<>();
		for (ModelSchedule schedule: schedules) result.add(schedule.clone());
		return result;
	}

	/**
	 * Stellt die Liste der in diesem Zeitplanlisten-Objekt erfassten Zeitpläne ein
	 * @param schedules	Neue Liste mit Zeitplänen
	 */
	public void setSchedules(final List<ModelSchedule> schedules) {
		this.schedules.clear();
		if (schedules!=null) for (ModelSchedule schedule: schedules) if (schedule!=null) this.schedules.add(schedule.clone());
	}

	/**
	 * Überprüft, ob die Zeitplanliste inhaltlich mit einer anderen Zeitplanliste übereinstimmt
	 * @param otherSchedules	Zeitplanliste, die mit der aktuellen Zeitplanliste verglichen werden soll
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Zeitplanlisten übereinstimmen
	 */
	public boolean equalsSchedules(final ModelSchedules otherSchedules) {
		if (otherSchedules==null) return false;

		if (schedules.size()!=otherSchedules.schedules.size()) return false;
		for (int i=0;i<schedules.size();i++) {
			if (schedules.get(i)==null && otherSchedules.schedules.get(i)==null) continue;
			if (schedules.get(i)==null || otherSchedules.schedules.get(i)==null) return false;
			if (!schedules.get(i).equalsSchedule(otherSchedules.schedules.get(i))) return false;
		}

		return true;
	}

	/**
	 * Erstellt eine Kopie der Zeitplanliste
	 * @return	Kopie der Zeitplanliste
	 */
	@Override
	public ModelSchedules clone() {
		final ModelSchedules clone=new ModelSchedules();

		for (ModelSchedule schedule: schedules) clone.schedules.add(schedule.clone());

		return clone;
	}

	/**
	 * Ersetzt die bisherige Liste an Zeitplänen durch eine neue Liste
	 * @param otherSchedules	Neue Zeitpläne, die in dieses Zeitplanlisten-Objekt eingefügt werden sollen
	 */
	public void setDataFrom(final ModelSchedules otherSchedules) {
		clear();
		for (ModelSchedule schedule: otherSchedules.schedules) if (schedule!=null) schedules.add(schedule.clone());
	}

	/**
	 * Speichert die Zeitplanliste in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Übergeordneter Knoten des Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addToXML(final Document doc, final Element parent) {
		if (schedules.size()==0) return;

		Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		for (ModelSchedule schedule: schedules) schedule.addToXML(doc,node);
	}

	/**
	 * Versucht eine Zeitplanliste aus einem gegebenen xml-Element zu laden
	 * @param node	XML-Element, aus dem die Zeitplanliste geladen werden soll
	 * @return Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);
			for (String test: ModelSchedule.XML_NODE_NAME) if (e.getNodeName().equals(test)) {
				final ModelSchedule schedule=new ModelSchedule();
				final String error=schedule.loadFromXML(e);
				if (error!=null) return error+" ("+String.format(Language.tr("Surface.XML.ErrorSchedule"),schedules.size()+1,node.getNodeName())+")";
				schedules.add(schedule);
				break;
			}
		}

		return null;
	}

	/**
	 * Sucht einen Text in den Daten der Zeitpläne.
	 * @param searcher	Such-System
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher) {
		for (ModelSchedule schedule: schedules) schedule.search(searcher);
	}
}