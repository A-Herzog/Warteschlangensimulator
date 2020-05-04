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
 * Statistik-Klasse, die einen Wert als Summe erfasst (und sonst nichts).<br>
 * Die Zählung wird über die Funktion {@link StatisticsValuePerformanceIndicator#add(double)} realisiert.
 * @author Alexander Herzog
 * @version 1.1
 */
public final class StatisticsValuePerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/** XML-Attribut für "Wert" */
	public static String[] xmlNameValue=new String[]{"Wert"};
	/** Fehlermeldung, wenn das "Wert"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameValueError="Das Wert-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";

	/**
	 * Erfasster Wert
	 */
	private double value;

	/**
	 * Konstruktor der Klasse <code>StatisticsValuePerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsValuePerformanceIndicator(final String[] xmlNodeNames) {
		super(xmlNodeNames);
		reset();
	}

	/**
	 * Fügt einen Teilwert zu dem Gesamtwert hinzu
	 * @param value	Zu addierender Teilwert
	 */
	public void add(final double value) {
		this.value+=value;
	}

	/**
	 * Fügt zu der Teil-Messreihe eine weitere Teilmessreihe hinzu
	 * @param moreStatistics	Statistikobjekt, dessen Daten zu diesem hinzugefügt werden sollen
	 */
	@Override
	public void add(final StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsValuePerformanceIndicator)) return;
		StatisticsValuePerformanceIndicator moreValueStatistics=(StatisticsValuePerformanceIndicator)moreStatistics;

		value+=moreValueStatistics.value;
	}

	/**
	 * Setzt den Gesamtwert auf 0 zurück.
	 */
	@Override
	public void reset() {
		value=0;
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsValuePerformanceIndicator)) return;
		value=((StatisticsValuePerformanceIndicator)indicator).value;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsValuePerformanceIndicator clone() {
		final StatisticsValuePerformanceIndicator indicator=new StatisticsValuePerformanceIndicator(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsValuePerformanceIndicator cloneEmpty() {
		return new StatisticsValuePerformanceIndicator(xmlNodeNames);
	}

	/**
	 * Liefert den erfassten Gesamtwert zurück.
	 * @return	Gesamtwert
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Speichert eine Kenngröße, die intern aus Gesamtanzahl und Anzahl der erfolgreichen Ereignisse besteht, in einem xml-Knoten.
	 * Es wird dabei zusätzlich der Anteil an erfolgreichen Ereignissen berechnet und gespeichert
	 * @param node	Neuer xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 */
	@Override
	protected void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder) {
		node.setAttribute(xmlNameValue[0],NumberTools.formatSystemNumber(value,recycleStringBuilder));
	}

	/**
	 * Versucht eine Kenngröße, die intern durch die Gesamtanzahl die und Anzahl der erfolgreichen Ereignisse repräsentiert wird, aus einem xml-Knoten zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	@Override
	public String loadFromXML(final Element node) {
		final String valueString=getAttributeValue(node,xmlNameValue);
		if (!valueString.isEmpty()) {
			final Double value=NumberTools.getDouble(valueString);
			if (value==null) return String.format(xmlNameValueError,node.getNodeName(),valueString);
			this.value=value;
		}

		return null;
	}
}
