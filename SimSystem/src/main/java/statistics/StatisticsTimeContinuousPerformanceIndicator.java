/**
 * Copyright 2021 Alexander Herzog
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
 * Statistik-Klasse, die erfasst, wie lange sich das System in einem bestimmten, durch einen
 * Double-Zahlenwert benannten Zustand befunden hat.<br>
 * Die Zählung wird über die Funktion {@link StatisticsTimeContinuousPerformanceIndicator#set(double, double)} realisiert.<br>
 * @author Alexander Herzog
 * @version 1.0
 */
public final class StatisticsTimeContinuousPerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/**
	 * Zeitpunkt der letzten Änderung des Zustands des Systems.
	 */
	private double lastTime=0.0;

	/**
	 * Aktueller Zustand des Systems.
	 */
	private double lastState=0;

	/**
	 * Summe der erfassten Zeiten
	 */
	private double sum=0;

	/**
	 * Minimaler Zustand in dem sich das System eine Zeitdauer >0 befunden hat
	 */
	private double min=0;

	/**
	 * Maximaler Zustand in dem sich das System eine Zeitdauer >0 befunden hat
	 */
	private double max=0;

	/**
	 * Summe der Zustände gewichtet mit der jeweiligen Zeitdauer
	 */
	private double valueSum=0;

	/**
	 * Summe der quadrierten Zustände gewichtet mit der jeweiligen Zeitdauer
	 */
	private double valueSumSquared=0;

	/**
	 * Summe der mit 3 potenzierten Zustände gewichtet mit der jeweiligen Zeitdauer
	 */
	private double valueSumCubic=0;

	/**
	 * Ergebnis der letzten Berechnung des Durchschnitts (-1, wenn sich die Werte seit der letzten Berechnung verändert haben und eine neue Rechnung notwendig ist)
	 */
	private double lastTimeMean=-1;

	/**
	 * Gibt an, ob der Zähler gerade auf eine explizite Startzeit gesetzt wurde.
	 * @see StatisticsTimeContinuousPerformanceIndicator#setTime
	 */
	private boolean explicitTimeInit=false;

	/**
	 * Konstruktor der Klasse <code>StatisticsTimePerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsTimeContinuousPerformanceIndicator(final String[] xmlNodeNames) {
		super(xmlNodeNames);
		reset();
	}

	/**
	 * Erfasst eine Zustandsänderung
	 * @param time	Aktuelle Zeit
	 * @param newState	Neuer Zustand (muss eine nicht-negative Zahl sein)
	 */
	public void set(final double time, final double newState) {
		if (time!=lastTime) {
			final boolean init=(lastTime<=0) || explicitTimeInit;
			explicitTimeInit=false;

			if (init) {
				this.min=0;
				this.max=0;
				sum=0;
				valueSum=0;
				valueSumSquared=0;
				valueSumCubic=0;
			} else {
				final double add=time-lastTime;

				if (lastState<this.min || this.sum==0) this.min=lastState;
				if (lastState>this.max || this.sum==0) this.max=lastState;
				sum+=add;
				valueSum+=add*lastState;
				valueSumSquared+=add*lastState*lastState;
				valueSumCubic+=add*lastState*lastState*lastState;
			}

			lastTime=time;
		}

		lastState=newState;
		lastTimeMean=-1;
	}

	/**
	 * Liefert den zuletzt per <code>set()</code> eingestellten Zustand
	 * @return	Aktuell eingestellter Zustand
	 * @see #set(double, double)
	 */
	public double getCurrentState() {
		return lastState;
	}

	/**
	 * Stellt die aktuelle Systemzeit ein (z.B. wenn der Beginn der Simulation nicht zum Zeitpunkt 0 erfolgt)
	 * @param time	Neue aktuelle Systemzeit
	 */
	public void setTime(final double time) {
		lastTime=time;
		lastTimeMean=-1;
		explicitTimeInit=true;
	}

	@Override
	public void add(StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsTimeContinuousPerformanceIndicator)) return;
		StatisticsTimeContinuousPerformanceIndicator moreCountStatistics=(StatisticsTimeContinuousPerformanceIndicator)moreStatistics;

		if (moreCountStatistics.sum>0) {
			if (sum>0) {
				min=Math.min(min,moreCountStatistics.min);
				max=Math.max(max,moreCountStatistics.max);
			} else {
				min=moreCountStatistics.min;
				max=moreCountStatistics.max;
			}
		}

		sum+=moreCountStatistics.sum;
		valueSum+=moreCountStatistics.valueSum;
		valueSumSquared+=moreCountStatistics.valueSumSquared;
		valueSumCubic+=moreCountStatistics.valueSumCubic;

		lastTimeMean=-1;
	}

	/**
	 * Setzt alle Teil-Kenngrößen auf 0 zurück.
	 */
	@Override
	public void reset() {
		lastState=0;
		lastTime=0;

		min=0;
		max=0;
		sum=0;
		valueSum=0;
		valueSumSquared=0;
		valueSumCubic=0;

		lastTimeMean=-1;
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsTimeContinuousPerformanceIndicator)) return;
		lastState=((StatisticsTimeContinuousPerformanceIndicator)indicator).lastState;
		lastTime=((StatisticsTimeContinuousPerformanceIndicator)indicator).lastTime;

		min=((StatisticsTimeContinuousPerformanceIndicator)indicator).min;
		max=((StatisticsTimeContinuousPerformanceIndicator)indicator).max;
		sum=((StatisticsTimeContinuousPerformanceIndicator)indicator).sum;
		valueSum=((StatisticsTimeContinuousPerformanceIndicator)indicator).valueSum;
		valueSumSquared=((StatisticsTimeContinuousPerformanceIndicator)indicator).valueSumSquared;
		valueSumCubic=((StatisticsTimeContinuousPerformanceIndicator)indicator).valueSumCubic;

		lastTimeMean=-1;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsTimeContinuousPerformanceIndicator clone() {
		final StatisticsTimeContinuousPerformanceIndicator indicator=new StatisticsTimeContinuousPerformanceIndicator(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsTimeContinuousPerformanceIndicator cloneEmpty() {
		return new StatisticsTimeContinuousPerformanceIndicator(xmlNodeNames);
	}

	/**
	 * Liefert die Summe der Zeiten der Systemzustände zurück.
	 * @return	Summe der Zeiten der Systemzustände
	 */
	public double getSum() {
		return sum;
	}

	/**
	 * Liefert den Zustand, in dem sich das System im Mittel befunden hat (z.B. mittlere Anzahl an Kunden im System, wenn die Anzahl an Kunden im System erfasst wird)
	 * @return	Mittlerer Zustand
	 */
	public double getTimeMean() {
		if (lastTimeMean<0) {
			if (sum>0) lastTimeMean=valueSum/sum; else lastTimeMean=0;
		}
		return lastTimeMean;
	}

	/**
	 * Liefert die Standardabweichung über die Verteilung der Zeiten in den verschiedenen Zuständen
	 * @return	Standardabweichung über die Verteilung der Zeiten in den verschiedenen Zuständen
	 */
	public double getTimeSD() {
		if (sum<2) return 0;
		final double v=valueSumSquared/(sum-1)-(valueSum*valueSum)/sum/(sum-1);
		return StrictMath.sqrt(Math.max(0,v));
	}

	/**
	 * Liefert die Varianz über die Verteilung der Zeiten in den verschiedenen Zuständen
	 * @return	Varianz über die Verteilung der Zeiten in den verschiedenen Zuständen
	 */
	public double getTimeVar() {
		if (sum<2) return 0;
		return Math.max(0,valueSumSquared/(sum-1)-(valueSum*valueSum)/sum/(sum-1));
	}

	/**
	 * Liefert den Variationskoeffizienten über die Verteilung der Zeiten in den verschiedenen Zuständen
	 * @return	Variationskoeffizienten über die Verteilung der Zeiten in den verschiedenen Zuständen
	 */
	public double getTimeCV() {
		double mean=getTimeMean();
		return (mean>0)?(getTimeSD()/mean):0;
	}

	/**
	 * Liefert die Schiefe über die Verteilung der Zeiten in den verschiedenen Zuständen
	 * @return	Schiefe  über die Verteilung der Zeiten in den verschiedenen Zuständen
	 */
	public double getTimeSk() {
		final double sd=getTimeSD();
		if (sum<3 || sd==0.0) return 0;

		/* siehe: https://de.wikipedia.org/wiki/Schiefe_(Statistik) */
		return sum/(sum-1)/(sum-2)/Math.pow(sd,3)*(valueSumCubic-3*getTimeMean()*valueSumSquared+2*sum*Math.pow(getTimeMean(),3));
	}

	/**
	 * Liefert den höchsten Zustand, in dem sich das System eine Zeit &gt;0 befunden hat
	 * @return	Maximale Zustand, in dem sich das System eine positive Zeit lang befunden hat
	 */
	public double getTimeMax() {
		return max;
	}

	/**
	 * Liefert den niedrigsten Zustand, in dem sich das System eine Zeit &gt;0 befunden hat
	 * @return	Minimaler Zustand, in dem sich das System eine positive Zeit lang befunden hat
	 */
	public double getTimeMin() {
		return min;
	}

	@Override
	protected void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder) {
		node.setAttribute(StatisticsTimePerformanceIndicator.xmlNameSum,NumberTools.formatSystemNumber(getSum(),recycleStringBuilder));
		node.setAttribute(StatisticsTimePerformanceIndicator.xmlNameValues[0],NumberTools.formatSystemNumber(valueSum,recycleStringBuilder));
		node.setAttribute(StatisticsTimePerformanceIndicator.xmlNameValuesSquared[0],NumberTools.formatSystemNumber(valueSumSquared,recycleStringBuilder));
		node.setAttribute(StatisticsTimePerformanceIndicator.xmlNameValuesCubic[0],NumberTools.formatSystemNumber(valueSumCubic,recycleStringBuilder));
		node.setAttribute(StatisticsTimePerformanceIndicator.xmlNameMean,NumberTools.formatSystemNumber(getTimeMean(),recycleStringBuilder));
		node.setAttribute(StatisticsTimePerformanceIndicator.xmlNameSD,NumberTools.formatSystemNumber(getTimeSD(),recycleStringBuilder));
		node.setAttribute(StatisticsTimePerformanceIndicator.xmlNameCV,NumberTools.formatSystemNumber(getTimeCV(),recycleStringBuilder));
		node.setAttribute(StatisticsTimePerformanceIndicator.xmlNameSk[0],NumberTools.formatSystemNumber(getTimeSk(),recycleStringBuilder));
		node.setAttribute(StatisticsTimePerformanceIndicator.xmlNameMin[0],""+Math.max(0,getTimeMin()));
		node.setAttribute(StatisticsTimePerformanceIndicator.xmlNameMax[0],""+Math.max(0,getTimeMax()));
	}

	@Override
	public String loadFromXML(Element node) {
		lastTimeMean=-1;

		String value;

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,new String[] {StatisticsTimePerformanceIndicator.xmlNameSum}));
		if (!value.isEmpty()) {
			final Double sum=NumberTools.getDouble(value);
			if (sum==null) return String.format(StatisticsTimePerformanceIndicator.xmlNameValuesError,node.getNodeName(),value);
			this.sum=sum;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,StatisticsTimePerformanceIndicator.xmlNameValues));
		if (!value.isEmpty()) {
			final Double valueSum=NumberTools.getDouble(value);
			if (valueSum==null) return String.format(StatisticsTimePerformanceIndicator.xmlNameValuesError,node.getNodeName(),value);
			this.valueSum=valueSum;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,StatisticsTimePerformanceIndicator.xmlNameValuesSquared));
		if (!value.isEmpty()) {
			final Double valueSum2=NumberTools.getDouble(value);
			if (valueSum2==null || valueSum2<0) return String.format(StatisticsTimePerformanceIndicator.xmlNameValuesSquaredError,node.getNodeName(),value);
			valueSumSquared=valueSum2;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,StatisticsTimePerformanceIndicator.xmlNameValuesCubic));
		if (!value.isEmpty()) {
			final Double valueSum3=NumberTools.getDouble(value);
			if (valueSum3==null) return String.format(StatisticsTimePerformanceIndicator.xmlNameValuesCubicError,node.getNodeName(),value);
			valueSumCubic=valueSum3;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,StatisticsTimePerformanceIndicator.xmlNameMin));
		if (!value.isEmpty()) {
			final Double min=NumberTools.getDouble(value);
			if (min==null) return String.format(StatisticsTimePerformanceIndicator.xmlNameMinError,node.getNodeName(),value);
			this.min=min;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,StatisticsTimePerformanceIndicator.xmlNameMax));
		if (!value.isEmpty()) {
			final Double max=NumberTools.getDouble(value);
			if (max==null) return String.format(StatisticsTimePerformanceIndicator.xmlNameMaxError,node.getNodeName(),value);
			this.max=max;
		}

		return null;
	}
}