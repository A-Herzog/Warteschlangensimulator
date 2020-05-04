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
package statistics;

import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Basisklasse für einen Kenngröße in der Statistik
 * @author Alexander Herzog
 * @version 1.2
 */
public abstract class StatisticsPerformanceIndicator {
	/**
	 * Gruppe von Statistik-Elementen, zu denen dieses gehört
	 * @see StatisticsPerformanceIndicator#setGroup(StatisticsMultiPerformanceIndicator)
	 */
	private StatisticsMultiPerformanceIndicator indicatorsGroup;

	/**
	 * Namen des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * (Erster Name wird beim Speichern verwendet, die weiteren werden nur beim Lesen zusätzlich erkannt)
	 */
	public final String[] xmlNodeNames;

	/**
	 * Konstruktor der Klasse <code>StatisticsPerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsPerformanceIndicator(final String[] xmlNodeNames) {
		this.xmlNodeNames=xmlNodeNames;
	}

	/**
	 * Teil dem Objekt mit, dass es zu einer Gruppe von Statistikobjekten gehört.<br>
	 * Wird beim Einfügen eines Objektes in einen {@link StatisticsMultiPerformanceIndicator} automatisch aufgerufen.
	 * @param indicatorsGroup	Gruppe, zu der dieser Indikator gehört
	 * @see StatisticsPerformanceIndicator#getGroup()
	 */
	public void setGroup(final StatisticsMultiPerformanceIndicator indicatorsGroup) {
		this.indicatorsGroup=indicatorsGroup;
	}

	/**
	 * Liefert den Namen des aktuellen Statistikobjektes innerhalb des {@link StatisticsMultiPerformanceIndicator}
	 * @return	Name des Statistikobjektes oder <code>null</code>, wenn es sich in keiner Gruppe befindet.
	 */
	protected final String getOwnNameInGroup() {
		if (indicatorsGroup==null) return null;
		return indicatorsGroup.getName(this);
	}

	/**
	 * Liefert alle Statistik-Objekte, die sich in der aktuellen Gruppe befinden
	 * @return	Alle Statistik-Objekte, die sich in der aktuellen Gruppe befinden. Liefert nie <code>null</code>, selbst wenn keine Gruppe eingestellt ist.
	 * @see StatisticsPerformanceIndicator#setGroup(StatisticsMultiPerformanceIndicator)
	 */
	protected final StatisticsPerformanceIndicator[] getGroup() {
		if (indicatorsGroup==null) return new StatisticsPerformanceIndicator[] {this};
		return indicatorsGroup.getAll();
	}

	/**
	 * Liefert alle Statistik-Objekte, die sich in der aktuellen Gruppe befinden und von derselben Klasse sind wie das aktuelle Objekt
	 * @return	Alle Statistik-Objekte, die sich in der aktuellen Gruppe befinden und von derselben Klasse sind wie das aktuelle Objekt. Liefert nie einen leeren Stream, selbst wenn keine Gruppe eingestellt ist.
	 * @see StatisticsPerformanceIndicator#setGroup(StatisticsMultiPerformanceIndicator)
	 */

	protected final Stream<StatisticsPerformanceIndicator> getGroupStream() {
		return Stream.of(getGroup()).filter(indicator->getClass().isInstance(indicator));
	}

	/**
	 * Fügt zu der Teil-Messreihe eine gleichartige Teil-Messreihe hinzu
	 * @param moreStatistics	Hinzuzufügende Teil-Messreihe
	 */
	public abstract void add(final StatisticsPerformanceIndicator moreStatistics);

	/**
	 * Speichert eine Kenngröße in einem xml-Knoten.
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Übergeordneter xml-Knoten
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return	Liefert das hinzugefügte xml-Element zurück (kann <code>null</code> sein, wenn Speichern nicht vorgesehen ist)
	 */
	public final Element addToXML(final Document doc, final Element parent, final StringBuilder recycleStringBuilder) {
		if (xmlNodeNames==null || xmlNodeNames.length==0) return null;
		final Element node=doc.createElement(xmlNodeNames[0]);
		parent.appendChild(node);
		addToXMLIntern(node,recycleStringBuilder);
		return node;
	}

	/**
	 * Speichert in abgeleiteten Klassen die eigentlichen Statistikdaten
	 * in dem übergebenen xml-Knoten.
	 * @param node	Knoten in dem die Daten gespeichert werden sollen.
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 */
	protected abstract void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder);

	/**
	 * Versucht eine Kenngröße aus einem xml-Knoten zu laden.
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	public abstract String loadFromXML(final Element node);

	/**
	 * Führt ggf. nach dem Laden oder Zusammenführen notwendige Berechnungen durch
	 */
	public void calc() {}

	/**
	 * Setzt alle Teil-Kenngrößen auf 0 zurück.
	 */
	public void reset() {}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public abstract StatisticsPerformanceIndicator clone();

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	public abstract StatisticsPerformanceIndicator cloneEmpty();

	/**
	 * Prüft, ob das übergebene XML-Element ein Attribut gemäß der übergebenen
	 * Namensliste besitzt und liefert im Erfolgsfall den Inhalt zurück.
	 * @param node	XML-Element, bei dem das Attribut abgefragt werden soll
	 * @param names	Liste mit zu prüfenden Namen
	 * @return	Im Erfolgsfall Wert des Attributs, sonst ein leerer String
	 */
	protected final String getAttributeValue(final Element node, final String[] names) {
		if (names==null) return "";
		for (String name: names) {
			final String value=node.getAttribute(name);
			if (value!=null && !value.isEmpty()) return value;
		}
		return "";
	}

	/**
	 * Vergleicht einen String (ohne Beachtung der Groß- und Kleinschreibung mit einer
	 * ganzen Reihe anderer String und liefert zurück, ob er mit einem identisch war.
	 * @param test	Zu prüfender String
	 * @param names	Liste mit Strings, mit denen <code>test</code> verglichen werden soll
	 * @return	Liefert <code>true</code> zurück, wenn <code>test</code> mit mindestens einem der übergebenen Verglichs-Strings identisch ist.
	 */
	protected final boolean multiCompare(final String test, final String[] names) {
		if (names==null) return false;
		for (String name: names) if (name.equalsIgnoreCase(test)) return true;
		return false;
	}
}
