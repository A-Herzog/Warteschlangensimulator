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

import org.w3c.dom.Element;

import mathtools.NumberTools;

/**
 * Statistik-Klasse, die erfasst, wie häufig ein bestimmtes Ereignis
 * eingetreten ist (und sonst nichts).<br>
 * Die Zählung wird über die Funktion {@link StatisticsSimpleCountPerformanceIndicator#add()} realisiert.
 * @author Alexander Herzog
 * @version 1.4
 */
public final class StatisticsSimpleCountPerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/** XML-Attribut für "Anzahl" */
	public static String[] xmlNameCount=new String[]{"Anzahl"};
	/** XML-Attribut für "Anteil" */
	public static String[] xmlNamePart=new String[]{"Anteil"};
	/** Fehlermeldung, wenn das "Anteil"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameCountError="Das Anzahl-Attribut im \"%s\"-Element muss eine nicht-negative Ganzzahl sein, ist aber \"%s\".";

	/**
	 * Anzahl der erfassten Ereignisse
	 */
	private long count;

	/**
	 * Zähler innerhalb des übergeordneten Elements gruppieren
	 * und so Anteile ausgeben?
	 */
	private boolean useGrouping;

	/**
	 * Ist {@link #useGrouping} aktiv: An welchem Bindestrich soll
	 * in Bezug auf den Namen nach Gruppenname und Zählername getrennt werden?
	 */
	private int splitAtDash;

	/**
	 * Konstruktor der Klasse <code>StatisticsSimpleCountPerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsSimpleCountPerformanceIndicator(final String[] xmlNodeNames) {
		super(xmlNodeNames);
		useGrouping=true;
		splitAtDash=1;
		reset();
	}

	/**
	 * Konstruktor der Klasse <code>StatisticsSimpleCountPerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param useGrouping	Soll versucht werden, die Zähler innerhalb des übergeordneten Elements in einer Gruppe zusammenzufassen
	 */
	public StatisticsSimpleCountPerformanceIndicator(final String[] xmlNodeNames, final boolean useGrouping) {
		super(xmlNodeNames);
		this.useGrouping=useGrouping;
		splitAtDash=1;
		reset();
	}

	/**
	 * Konstruktor der Klasse <code>StatisticsSimpleCountPerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param splitAtDash	An welchem Bindestrich soll in Bezug auf den Namen nach Gruppenname und Zählername getrennt werden?
	 */
	public StatisticsSimpleCountPerformanceIndicator(final String[] xmlNodeNames, final int splitAtDash) {
		super(xmlNodeNames);
		useGrouping=true;
		this.splitAtDash=splitAtDash;
		reset();
	}

	/**
	 * Fügt einen Wert zu der Messreihe hinzu
	 */
	public void add() {
		count++;
	}

	/**
	 * Fügt zu der Teil-Messreihe eine weitere Teilmessreihe hinzu
	 * @param moreStatistics	Statistikobjekt, dessen Daten zu diesem hinzugefügt werden sollen
	 */
	@Override
	public void add(final StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsSimpleCountPerformanceIndicator)) return;
		StatisticsSimpleCountPerformanceIndicator moreCountStatistics=(StatisticsSimpleCountPerformanceIndicator)moreStatistics;

		count+=moreCountStatistics.count;
	}

	/**
	 * Setzt alle Teil-Kenngrößen auf 0 zurück.
	 */
	@Override
	public void reset() {
		count=0;
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsSimpleCountPerformanceIndicator)) return;
		useGrouping=((StatisticsSimpleCountPerformanceIndicator)indicator).useGrouping;
		splitAtDash=((StatisticsSimpleCountPerformanceIndicator)indicator).splitAtDash;
		count=((StatisticsSimpleCountPerformanceIndicator)indicator).count;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsSimpleCountPerformanceIndicator clone() {
		final StatisticsSimpleCountPerformanceIndicator indicator=new StatisticsSimpleCountPerformanceIndicator(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsSimpleCountPerformanceIndicator cloneEmpty() {
		final StatisticsSimpleCountPerformanceIndicator indicator=new StatisticsSimpleCountPerformanceIndicator(xmlNodeNames);
		indicator.useGrouping=useGrouping;
		indicator.splitAtDash=splitAtDash;
		return indicator;
	}

	/**
	 * Liefert die Anzahl an erfassten Ereignissen zurück.
	 * @return	Anzahl an erfassten Ereignissen
	 */
	public long get() {
		return count;
	}

	/**
	 * Liefert den Gruppennamen für einen Zähler
	 * @param name	Name des Zählers
	 * @return	Gruppenname oder <code>null</code>, wenn keine Gruppen vorhanden sind.
	 */
	private String groupNameFromName(final String name) {
		if (!useGrouping) return null;
		if (name==null || name.isEmpty()) return null;
		final String[] parts=name.split("-");
		if (parts.length<splitAtDash+1) return null;
		if (splitAtDash==1) return parts[0];
		final StringBuilder result=new StringBuilder();
		result.append(parts[0]);
		for (int i=1;i<splitAtDash;i++) {
			result.append("-");
			result.append(parts[i]);
		}
		return result.toString();
	}

	/**
	 * Liefert den Anteil an durch diesen Zähler in der Zählergruppe erfassten Ereignissen zurück.
	 * @return Anteil dieses Zählers in der Zählergruppe
	 */
	public double getPart() {
		final String group=groupNameFromName(getOwnNameInGroup());
		if (group==null) return 1.0;
		final long sum=getGroupStream().map(indicator->(StatisticsSimpleCountPerformanceIndicator)indicator).filter(counter->group.equals(groupNameFromName(counter.getOwnNameInGroup()))).mapToLong(counter->counter.get()).sum();
		return ((double)count)/sum;
	}

	/**
	 * Speichert eine Kenngröße in einem xml-Knoten.
	 * Es wird dabei zusätzlich der Anteil an erfolgreichen Ereignissen berechnet und gespeichert
	 * @param node	Neuer xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 */
	@Override
	protected void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder) {
		node.setAttribute(xmlNameCount[0],""+get());
		if (useGrouping) node.setAttribute(xmlNamePart[0],""+NumberTools.formatSystemNumber(getPart(),recycleStringBuilder));
	}

	/**
	 * Versucht eine Kenngröße aus einem xml-Knoten zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	@Override
	public String loadFromXML(final Element node) {
		Long count;
		String value;

		value=getAttributeValue(node,xmlNameCount);
		if (!value.isEmpty()) {
			count=NumberTools.getLong(value);
			if (count==null || count<0) return String.format(xmlNameCountError,node.getNodeName(),value);
			this.count=count;
		}

		return null;
	}
}