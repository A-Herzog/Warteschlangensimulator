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
 * Statistik-Klasse, die einen Durchsatz (z.B. Kunden/Stunde) in Form eines Quotienten erfasst.<br>
 * Die Erfassung wird über die Funktion {@link StatisticsQuotientPerformanceIndicator#set(double, double)} realisiert.
 * @author Alexander Herzog
 * @version 1.1
 */
public final class StatisticsQuotientPerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/** XML-Attribut für "Zaehler" */
	public static String[] xmlNameNumerator=new String[]{"Zaehler"};
	/** XML-Attribut für "Nenner" */
	public static String[] xmlNameDenominator=new String[]{"Nenner"};
	/** XML-Attribut für "Quotient" */
	public static String[] xmlNameQuotient=new String[]{"Quotient"};
	/** Fehlermeldung, wenn das "Zaehler"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameNumeratorError="Das Zaehler-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** Fehlermeldung, wenn das "Nenner"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameDenominatorError="Das Nenner-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";

	/**
	 * Zähler
	 */
	private double numerator;

	/**
	 * Nenner
	 */
	private double denominator;

	/**
	 * Konstruktor der Klasse <code>StatisticsQuotientPerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsQuotientPerformanceIndicator(final String[] xmlNodeNames) {
		super(xmlNodeNames);
		reset();
	}

	/**
	 * Fügt zu der Teil-Messreihe eine weitere Teilmessreihe hinzu
	 * @param moreStatistics	Statistikobjekt, dessen Daten zu diesem hinzugefügt werden sollen
	 */
	@Override
	public void add(final StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsQuotientPerformanceIndicator)) return;
		StatisticsQuotientPerformanceIndicator moreQuotientStatistics=(StatisticsQuotientPerformanceIndicator)moreStatistics;

		numerator+=moreQuotientStatistics.numerator;
		denominator+=moreQuotientStatistics.denominator;
	}

	/**
	 * Setzt alle Teil-Kenngrößen auf 0 zurück.
	 */
	@Override
	public void reset() {
		numerator=0;
		denominator=0;
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsQuotientPerformanceIndicator)) return;
		numerator=((StatisticsQuotientPerformanceIndicator)indicator).numerator;
		denominator=((StatisticsQuotientPerformanceIndicator)indicator).denominator;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsQuotientPerformanceIndicator clone() {
		final StatisticsQuotientPerformanceIndicator indicator=new StatisticsQuotientPerformanceIndicator(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsQuotientPerformanceIndicator cloneEmpty() {
		return new StatisticsQuotientPerformanceIndicator(xmlNodeNames);
	}

	/**
	 * Stellt den Zähler und den Nenner des Quotienten ein
	 * @param numerator	Neuer Zähler
	 * @param denominator	Neuer Nenner
	 * @see StatisticsQuotientPerformanceIndicator#getQuotient()
	 * @see StatisticsQuotientPerformanceIndicator#getNumerator()
	 * @see StatisticsQuotientPerformanceIndicator#getDenominator()
	 */
	public void set(final double numerator, final double denominator) {
		this.numerator=numerator;
		this.denominator=denominator;
	}

	/**
	 * Liefert den Quotienten (d.h. den Durchsatz)
	 * @return	Quotient (oder 0, wenn der Nenner 0 ist)
	 * @see StatisticsQuotientPerformanceIndicator#set(double, double)
	 * @see StatisticsQuotientPerformanceIndicator#getNumerator()
	 * @see StatisticsQuotientPerformanceIndicator#getDenominator()
	 */
	public double getQuotient() {
		if (Math.abs(denominator)>1E-16) return numerator/denominator; else return 0;
	}

	/**
	 * Liefert den Zähler der Quotienten
	 * @return	Zähler der Quotienten
	 * @see StatisticsQuotientPerformanceIndicator#getDenominator()
	 * @see StatisticsQuotientPerformanceIndicator#getQuotient()
	 */
	public double getNumerator() {
		return numerator;
	}

	/**
	 * Liefert den Nenner der Quotienten
	 * @return	Zähler der Nenner
	 * @see StatisticsQuotientPerformanceIndicator#getNumerator()
	 * @see StatisticsQuotientPerformanceIndicator#getQuotient()
	 */
	public double getDenominator() {
		return denominator;
	}

	/**
	 * Speichert eine Kenngröße in einem xml-Knoten.
	 * Es wird dabei zusätzlich der Anteil an erfolgreichen Ereignissen berechnet und gespeichert
	 * @param node	Neuer xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 */
	@Override
	protected void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder) {
		node.setAttribute(xmlNameNumerator[0],NumberTools.formatSystemNumber(numerator,recycleStringBuilder));
		node.setAttribute(xmlNameDenominator[0],NumberTools.formatSystemNumber(denominator,recycleStringBuilder));
		node.setAttribute(xmlNameQuotient[0],NumberTools.formatSystemNumber(getQuotient(),recycleStringBuilder));
	}

	/**
	 * Versucht eine Kenngröße aus einem xml-Knoten zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	@Override
	public String loadFromXML(Element node) {
		String value;

		value=getAttributeValue(node,xmlNameNumerator);
		if (!value.isEmpty()) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(value));
			if (D==null) return String.format(xmlNameNumeratorError,node.getNodeName(),value);
			numerator=D.doubleValue();
		}

		value=getAttributeValue(node,xmlNameDenominator);
		if (!value.isEmpty()) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(value));
			if (D==null) return String.format(xmlNameDenominatorError,node.getNodeName(),value);
			denominator=D.doubleValue();
		}

		return null;
	}
}