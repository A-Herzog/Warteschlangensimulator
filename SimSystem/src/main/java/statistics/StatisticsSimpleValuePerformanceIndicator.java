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
 * Statistik-Klasse, die einen Wert erfasst (und sonst nichts).<br>
 * @author Alexander Herzog
 * @version 1.0
 */
public final class StatisticsSimpleValuePerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/** XML-Attribut für "Wert" */
	public static String[] xmlNameValue=new String[]{"Wert"};
	/** Fehlermeldung, wenn das "Wert"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameValueError="Das Wert-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";

	/**
	 * Wert
	 */
	private double value;

	/**
	 * Konstruktor der Klasse <code>StatisticsSimpleCountPerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsSimpleValuePerformanceIndicator(final String[] xmlNodeNames) {
		super(xmlNodeNames);
		reset();
	}

	/**
	 * Stellt den Wert ein.
	 * @param value	Wert
	 */
	public void set(final double value) {
		this.value=value;
	}

	/**
	 * Fügt zu der Teil-Messreihe eine weitere Teilmessreihe hinzu
	 * @param moreStatistics	Statistikobjekt, dessen Daten zu diesem hinzugefügt werden sollen
	 */
	@Override
	public void add(final StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsSimpleValuePerformanceIndicator)) return;
		StatisticsSimpleValuePerformanceIndicator moreCountStatistics=(StatisticsSimpleValuePerformanceIndicator)moreStatistics;

		value+=moreCountStatistics.value;
	}

	/**
	 * Setzt alle Teil-Kenngrößen auf 0 zurück.
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
		if (!(indicator instanceof StatisticsSimpleValuePerformanceIndicator)) return;
		value=((StatisticsSimpleValuePerformanceIndicator)indicator).value;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsSimpleValuePerformanceIndicator clone() {
		final StatisticsSimpleValuePerformanceIndicator indicator=new StatisticsSimpleValuePerformanceIndicator(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsSimpleValuePerformanceIndicator cloneEmpty() {
		return new StatisticsSimpleValuePerformanceIndicator(xmlNodeNames);
	}

	/**
	 * Liefert den Wert zurück.
	 * @return	Wert
	 */
	public double get() {
		return value;
	}

	/**
	 * Speichert eine Kenngröße in einem xml-Knoten.
	 * Es wird dabei zusätzlich der Anteil an erfolgreichen Ereignissen berechnet und gespeichert
	 * @param node	Neuer xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 */
	@Override
	protected void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder) {
		node.setAttribute(xmlNameValue[0],NumberTools.formatSystemNumber(NumberTools.reduceDigits(value,10),recycleStringBuilder));
	}

	/**
	 * Versucht eine Kenngröße aus einem xml-Knoten zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	@Override
	public String loadFromXML(final Element node) {
		Double value;
		String attr;

		attr=getAttributeValue(node,xmlNameValue);
		if (!attr.isEmpty()) {
			value=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(attr));
			if (value==null) return String.format(xmlNameValueError,node.getNodeName(),attr);
			this.value=value;
		}

		return null;
	}
}