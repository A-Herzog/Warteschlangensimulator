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
 * Statistik-Klasse, die z�hlt wie h�ufig Experiment positiv oder negativ ausgefallen ist.<br>
 * Die Z�hlung wird �ber die Funktion {@link StatisticsCountPerformanceIndicator#add(boolean)} realisiert.
 * @author Alexander Herzog
 * @version 1.1
 */
public final class StatisticsCountPerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/** XML-Attribut f�r "Anzahl" */
	public static String[] xmlNameCount=new String[]{"Anzahl"};
	/** Fehlermeldung, wenn das "Anzahl"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameCountError="Das Anzahl-Attribut im \"%s\"-Element muss eine nicht-negative Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut f�r "AnzahlErfolg" */
	public static String[] xmlNameSuccessCount=new String[]{"AnzahlErfolg"};
	/** Fehlermeldung, wenn das "AnzahlErfolg"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameSuccessCountError="Das AnzahlErfolg-Attribut im \"%s\"-Element muss eine nicht-negative Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut f�r "AnteilErfolg" */
	public static String xmlNameSuccessPart="AnteilErfolg";

	/**
	 * Anzahl der erfassten Ereignisse
	 */
	private long countAll;

	/**
	 * Anzahl der erfassten erfolgreichen Ereignisse
	 */
	private long countHit;

	/**
	 * Konstruktor der Klasse <code>StatisticsCountPerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsCountPerformanceIndicator(final String[] xmlNodeNames) {
		super(xmlNodeNames);
		reset();
	}

	/**
	 * F�gt einen Wert zu der Messreihe hinzu
	 * @param success	Gibt an, ob es sich um einen Erfolg oder einen Misserfolg handelt
	 */
	public void add(final boolean success) {
		countAll++;
		if (success) countHit++;
	}

	/**
	 * F�gt zu der Teil-Messreihe eine weitere Teilmessreihe hinzu
	 * @param moreStatistics	Statistikobjekt, dessen Daten zu diesem hinzugef�gt werden sollen
	 */
	@Override
	public void add(final StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsCountPerformanceIndicator)) return;
		StatisticsCountPerformanceIndicator moreCountStatistics=(StatisticsCountPerformanceIndicator)moreStatistics;

		countAll+=moreCountStatistics.countAll;
		countHit+=moreCountStatistics.countHit;
	}

	/**
	 * Setzt alle Teil-Kenngr��en auf 0 zur�ck.
	 */
	@Override
	public void reset() {
		countAll=0;
		countHit=0;
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsCountPerformanceIndicator)) return;
		countAll=((StatisticsCountPerformanceIndicator)indicator).countAll;
		countHit=((StatisticsCountPerformanceIndicator)indicator).countHit;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsCountPerformanceIndicator clone() {
		final StatisticsCountPerformanceIndicator indicator=new StatisticsCountPerformanceIndicator(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, �bernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsCountPerformanceIndicator cloneEmpty() {
		return new StatisticsCountPerformanceIndicator(xmlNodeNames);
	}

	/**
	 * Liefert die Anzahl an erfassten Ereignissen zur�ck.
	 * @return	Anzahl an erfassten Ereignissen
	 */
	public long getAll() {
		return countAll;
	}

	/**
	 * Liefert die Anzahl an erfassten erfolgreichen Ereignissen zur�ck.
	 * @return	Anzahl an erfassten erfolgreichen Ereignissen
	 */
	public long getSuccess() {
		return countHit;
	}

	/**
	 * Liefert die Anzahl an erfassten nicht erfolgreichen Ereignissen zur�ck.
	 * @return	Anzahl an erfassten nicht erfolgreichen Ereignissen
	 */
	public long getNoSuccess() {
		return countAll-countHit;
	}

	/**
	 * Liefert den Anteil an erfolgreichen Ereignissen zur�ck.
	 * @return	Anteil an erfassten erfolgreichen Ereignissen
	 */
	public double getSuccessPart() {
		if (countAll==0) return 0.0;
		return ((double)countHit)/countAll;
	}

	/**
	 * Speichert eine Kenngr��e, die intern aus Gesamtanzahl und Anzahl der erfolgreichen Ereignisse besteht, in einem xml-Knoten.
	 * Es wird dabei zus�tzlich der Anteil an erfolgreichen Ereignissen berechnet und gespeichert
	 * @param node	Neuer xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	@Override
	protected void addToXMLIntern(final Element node) {
		node.setAttribute(xmlNameCount[0],""+countAll);
		node.setAttribute(xmlNameSuccessCount[0],""+countHit);
		node.setAttribute(xmlNameSuccessPart,NumberTools.formatSystemNumber(getSuccessPart()));
	}

	/**
	 * Versucht eine Kenngr��e, die intern durch die Gesamtanzahl die und Anzahl der erfolgreichen Ereignisse repr�sentiert wird, aus einem xml-Knoten zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> zur�ck, sonst eine Fehlermeldung
	 */
	@Override
	public String loadFromXML(final Element node) {
		Long count;
		String value;

		value=getAttributeValue(node,xmlNameCount);
		if (!value.isEmpty()) {
			count=NumberTools.getLong(value);
			if (count==null || count<0) return String.format(xmlNameCountError,node.getNodeName(),value);
			countAll=count;
		}

		value=getAttributeValue(node,xmlNameSuccessCount);
		if (!value.isEmpty()) {
			count=NumberTools.getLong(value);
			if (count==null || count<0) return String.format(xmlNameSuccessCountError,node.getNodeName(),value);
			countHit=count;
		}

		return null;
	}
}
