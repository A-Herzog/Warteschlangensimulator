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
 * Statistik-Klasse, die die Veränderung eines kontinuierlichen Wertes über die Zeit erfasst.
 * Es wird bei der Integration über die Zeit automatisch linear zwischen dem letzten und dem aktuellen Wert
 * interpoliert. (Es wird also ein linearer Verlauf des Wertes angenommen und folglich per Rechteckregel
 * integriert.)<br>
 * Die Zählung wird über die Funktion {@link StatisticsTimeAnalogPerformanceIndicator#set(double, double)} realisiert.
 * @author Alexander Herzog
 * @version 1.1
 */
public final class StatisticsTimeAnalogPerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/** XML-Attribut für "Summe" */
	public static String[] xmlNameSum=new String[]{"Summe"};
	/** Fehlermeldung, wenn das "Summe"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameSumError="Das Summe-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Zeit" */
	public static String[] xmlNameTime=new String[]{"Zeit"};
	/** Fehlermeldung, wenn das "Zeit"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameTimeError="Das Zeit-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Mittelwert" */
	public static String xmlNameMean="Mittelwert";
	/** XML-Attribut für "Minimum" */
	public static String[] xmlNameMin=new String[]{"Minimum"};
	/** Fehlermeldung, wenn das "Minimum"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameMinError="Das Minimum-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Maximum" */
	public static String[] xmlNameMax=new String[]{"Maximum"};
	/** Fehlermeldung, wenn das "Maximum"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameMaxError="Das Maximum-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";

	/**
	 * Letzter erfasster Zeitstempel
	 * (-1 bedeutet, dass das System noch nicht initialisiert wurde)
	 */
	private double lastTime;

	/**
	 * Letzter erfasster Wert
	 */
	private double lastValue;

	/**
	 * Integral über die Werte über die Zeit
	 */
	private double sum;

	/**
	 * Zeitpunkt der ersten Erfassung eines Wertes (z.B. Ende der Warmup-Zeit)
	 */
	private double startTime;

	/**
	 * Minimaler Messwert
	 */
	private double min;

	/**
	 * Maximaler Messwert
	 */
	private double max;

	/**
	 * Konstruktor der Klasse <code>StatisticsTimeAnalogPerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsTimeAnalogPerformanceIndicator(final String[] xmlNodeNames) {
		super(xmlNodeNames);
		reset();
	}

	/**
	 * Liefert den zuletzt per <code>set()</code> eingestellten Zustand
	 * @return	Aktuell eingestellter Zustand
	 * @see #set(double, double)
	 */
	public double getCurrentState() {
		if (lastTime<0) return 0;
		return lastValue;
	}

	/**
	 * Erfasst eine Zustandsänderung
	 * @param time	Aktuelle Zeit
	 * @param newState	Neuer Zustand
	 */
	public void set(final double time, final double newState) {
		if (lastTime>=0) {
			sum+=(time-lastTime)*(lastValue+newState)/2.0;
			if (newState<min) min=newState;
			if (newState>max) max=newState;
		} else {
			sum=0;
			min=newState;
			max=newState;
			startTime=time;
		}

		lastTime=time;
		lastValue=newState;
	}

	@Override
	public void add(final StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsTimeAnalogPerformanceIndicator)) return;
		final StatisticsTimeAnalogPerformanceIndicator data=(StatisticsTimeAnalogPerformanceIndicator)moreStatistics;

		if (lastTime>=0) {
			startTime+=data.startTime;
			lastTime+=data.lastTime;
			sum+=data.sum;

			min=Math.min(min,data.min);
			max=Math.max(max,data.max);
		} else {
			copyDataFrom(moreStatistics);
		}
	}

	@Override
	public void reset() {
		lastTime=-1;
		sum=0;
		min=0;
		max=0;
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsTimeAnalogPerformanceIndicator)) return;
		final StatisticsTimeAnalogPerformanceIndicator data=(StatisticsTimeAnalogPerformanceIndicator)indicator;

		lastTime=data.lastTime;
		lastValue=data.lastValue;
		sum=data.sum;
		startTime=data.startTime;
		min=data.min;
		max=data.max;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsTimeAnalogPerformanceIndicator clone() {
		final StatisticsTimeAnalogPerformanceIndicator indicator=new StatisticsTimeAnalogPerformanceIndicator(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsTimeAnalogPerformanceIndicator cloneEmpty() {
		return new StatisticsTimeAnalogPerformanceIndicator(xmlNodeNames);
	}

	/**
	 * Berechnet aus den Messreihen-Kenngrößen den Mittelwert
	 * @return	Mittelwert der Messreihe
	 */
	public double getMean() {
		if (lastTime<0) return 0;
		return sum/(lastTime-startTime);
	}

	/**
	 * Liefert den minimalen Wert, der aufgetreten ist
	 * @return	Minimaler Wert der Messreihe
	 */
	public double getMin() {
		if (lastTime<0) return 0;
		return min;
	}

	/**
	 * Liefert den maximalen Wert, der aufgetreten ist
	 * @return	Maximaler Wert der Messreihe
	 */
	public double getMax() {
		if (lastTime<0) return 0;
		return max;
	}

	@Override
	protected void addToXMLIntern(Element node) {

		node.setAttribute(xmlNameSum[0],NumberTools.formatSystemNumber(sum));
		node.setAttribute(xmlNameTime[0],NumberTools.formatSystemNumber(lastTime-startTime));
		node.setAttribute(xmlNameMean,NumberTools.formatSystemNumber(getMean()));
		node.setAttribute(xmlNameMin[0],NumberTools.formatSystemNumber(min));
		node.setAttribute(xmlNameMax[0],NumberTools.formatSystemNumber(max));
	}

	@Override
	public String loadFromXML(Element node) {
		String value;

		value=getAttributeValue(node,xmlNameSum);
		if (!value.isEmpty()) {
			Double sum=NumberTools.getDouble(value);
			if (sum==null) return String.format(xmlNameSumError,node.getNodeName(),value);
			this.sum=sum;
		}

		value=getAttributeValue(node,xmlNameTime);
		if (!value.isEmpty()) {
			Double time=NumberTools.getDouble(value);
			if (time==null) return String.format(xmlNameTimeError,node.getNodeName(),value);
			this.lastTime=time;
			this.startTime=0;
		}

		value=getAttributeValue(node,xmlNameMin);
		if (!value.isEmpty()) {
			Double min=NumberTools.getDouble(value);
			if (min==null) return String.format(xmlNameMinError,node.getNodeName(),value);
			this.min=min;
		}

		value=getAttributeValue(node,xmlNameMax);
		if (!value.isEmpty()) {
			Double max=NumberTools.getDouble(value);
			if (max==null) return String.format(xmlNameMaxError,node.getNodeName(),value);
			this.max=max;
		}

		return null;
	}
}