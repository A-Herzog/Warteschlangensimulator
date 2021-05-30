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

import java.util.Arrays;

import org.w3c.dom.Element;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;

/**
 * Statistik-Klasse, die erfasst, wie lange sich das System in einem bestimmten, durch einen
 * Integer-Zahlenwert benannten Zustand befunden hat.<br>
 * Die Zählung wird über die Funktion {@link StatisticsTimePerformanceIndicator#set(double, int)} realisiert.<br>
 * Sollen hingegen durch einen (String-)Namen definierte Zustände erfasst werden,
 * so kann dafür die Klasse {@link StatisticsStateTimePerformanceIndicator} verwendet werden.
 * @author Alexander Herzog
 * @version 3.2
 */
public final class StatisticsTimePerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/**
	 * Höchster erfasster Zustandswert (Array läuft von 0 bis <code>MAX_STATE</code>, also <code>MAX_STATE+1</code> Werte)
	 */
	public static final int MAX_STATE=2048*1024;

	/** Fehlermeldung, wenn der Inhalt des XML-Elements nicht gelesen werden konnte. */
	public static String xmlLoadError="Die in dem Element \"%s\" angegebene Verteilung ist ungültig.";
	/** XML-Attribut für "Summe" */
	public static String xmlNameSum="Summe";
	/** XML-Attribut für "WerteSumme" */
	public static String[] xmlNameValues=new String[]{"WerteSumme"};
	/** Fehlermeldung, wenn das "WerteSumme"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameValuesError="Das WerteSumme-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "WerteSumme2" */
	public static String[] xmlNameValuesSquared=new String[]{"WerteSumme2"};
	/** Fehlermeldung, wenn das "WerteSumme2"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameValuesSquaredError="Das WerteSumme2-Attribut im \"%s\"-Element muss eine nicht-negative Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Mittelwert" */
	public static String xmlNameMean="Mittelwert";
	/** XML-Attribut für "Standardabweichung" */
	public static String xmlNameSD="Standardabweichung";
	/** XML-Attribut für "Variationskoeffizient" */
	public static String xmlNameCV="Variationskoeffizient";
	/** XML-Attribut für "Minimum" */
	public static String[] xmlNameMin=new String[]{"Minimum"};
	/** Fehlermeldung, wenn das "Minimum"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameMinError="Das Minimum-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Maximum" */
	public static String[] xmlNameMax=new String[]{"Maximum"};
	/** Fehlermeldung, wenn das "Maximum"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameMaxError="Das Maximum-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Quantil" */
	public static String xmlNameQuantil="Quantil";

	/**
	 * Quantile, die aus der Häufigkeitsverteilung berechnet und in der xml-Datei gespeichert werden
	 */
	public static final double[] storeQuantilValues=new double[] {0.10,0.25,0.5,0.75,0.9};

	/**
	 * Zeitpunkt der letzten Änderung des Zustands des Systems.
	 */
	private double lastTime=0.0;

	/**
	 * Aktueller Zustand des Systems.
	 */
	private int lastState=0;

	/**
	 * Zählt, wie lange sich das System in welchem Zustand befinden hat.
	 */
	private double[] stateTime=null;

	/**
	 * Zählt die Zeit, die sich das System im Zustand 0 befunden hat (solange stateTime==null ist gültig)
	 */
	private double time0=-1;

	/**
	 * Zählt die Zeit, in der sich das System im Zustand timeMaxState befunden hat (solange stateTime==null ist gültig)
	 */
	private double timeMax=-1;

	/**
	 * Zustand dessen Zeit in timeMax erfasst wird
	 */
	private int timeMaxState=-1;

	/**
	 * Summe der erfassten Zeiten (d.h. Summe über {@link StatisticsTimePerformanceIndicator#stateTime})
	 */
	private double sum=0;

	/**
	 * Minimaler Zustand in dem sich das System eine Zeitdauer >0 befunden hat
	 */
	private int min=-1;

	/**
	 * Maximaler Zustand in dem sich das System eine Zeitdauer >0 befunden hat
	 */
	private int max=-1;

	/**
	 * Summe der Zustände gewichtet mit der jeweiligen Zeitdauer
	 */
	private double valueSum=0;

	/**
	 * Summe der quadrierten Zustände gewichtet mit der jeweiligen Zeitdauer
	 */
	private double valueSumSquared=0;

	/**
	 * Ergebnis der letzten Berechnung des Durchschnitts (-1, wenn sich die Werte seit der letzten Berechnung verändert haben und eine neue Rechnung notwendig ist)
	 */
	private double lastTimeMean=-1;

	/**
	 * Gibt an, ob der Zähler gerade auf eine explizite Startzeit gesetzt wurde.
	 * @see StatisticsTimePerformanceIndicator#setTime
	 */
	private boolean explicitTimeInit=false;

	/**
	 * Konstruktor der Klasse <code>StatisticsTimePerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsTimePerformanceIndicator(final String[] xmlNodeNames) {
		super(xmlNodeNames);
		reset();
	}

	/**
	 * Wurden bislang nur {@link #time0} und {@link #timeMax} erfasst, so kann über
	 * diese Funktion das vollständige {@link #stateTime}-Array aufgespannt werden.
	 * @see #set(double, int)
	 */
	private void forceExpandStateTime() {
		if (stateTime==null) {
			final int size=Math.min(MAX_STATE,Math.max(timeMaxState,10))+1;
			stateTime=new double[size];
			if (time0>0) stateTime[0]=time0;
			if (timeMaxState>0 && timeMax>0) stateTime[Math.min(stateTime.length-1,timeMaxState)]=timeMax;
			time0=-1;
			timeMax=-1;
			timeMaxState=-1;
		}
	}

	/**
	 * Erfasst eine Zustandsänderung
	 * @param time	Aktuelle Zeit
	 * @param newState	Neuer Zustand (muss eine nicht-negative Zahl sein)
	 */
	public void set(final double time, final int newState) {
		if (time!=lastTime) {
			int max=(lastState>newState)?lastState:newState;
			if (max<1) max=1;
			if (max>MAX_STATE) max=MAX_STATE;
			final boolean init=(lastTime<=0 && stateTime==null) || explicitTimeInit;

			explicitTimeInit=false;
			if (stateTime==null && time0<0 && timeMax<0) {
				this.min=-1;
				this.max=-1;
				sum=0;
				valueSum=0;
				valueSumSquared=0;
			}

			if (!init) {
				final double add=time-lastTime;
				if (stateTime==null) {
					if (lastState==0) {
						time0=(time0>=0)?(time0+add):add;
					} else {
						if (timeMaxState==-1 || timeMaxState==lastState) {
							timeMaxState=lastState;
							timeMax=(timeMax>=0)?(timeMax+add):add;
						} else {
							forceExpandStateTime();
						}
					}
				}

				if (stateTime!=null) {
					if (stateTime.length<=max) stateTime=Arrays.copyOf(stateTime,Math.min(MAX_STATE+1,(max+1)*2));
					stateTime[(stateTime.length-1<lastState)?stateTime.length-1:lastState]+=add;
				}

				if (lastState<this.min || this.min==-1) this.min=lastState;
				if (lastState>this.max || this.max==-1) this.max=lastState;
				sum+=add;
				valueSum+=add*lastState;
				valueSumSquared+=add*lastState*lastState;
			}

			lastTime=time;
		}

		lastState=(newState>=0)?newState:0;

		lastTimeMean=-1;
	}

	/**
	 * Liefert den zuletzt per <code>set()</code> eingestellten Zustand
	 * @return	Aktuell eingestellter Zustand
	 * @see #set(double, int)
	 */
	public int getCurrentState() {
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
		time0=-1;
		timeMax=-1;
		timeMaxState=-1;
	}

	@Override
	public void add(StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsTimePerformanceIndicator)) return;
		StatisticsTimePerformanceIndicator moreCountStatistics=(StatisticsTimePerformanceIndicator)moreStatistics;

		moreCountStatistics.forceExpandStateTime();
		if (moreCountStatistics.stateTime!=null) {
			if (stateTime==null) stateTime=new double[moreCountStatistics.stateTime.length];
			if (stateTime.length<moreCountStatistics.stateTime.length) stateTime=Arrays.copyOf(stateTime,moreCountStatistics.stateTime.length);
			for (int i=0;i<moreCountStatistics.stateTime.length;i++) stateTime[i]=stateTime[i]+moreCountStatistics.stateTime[i];
		}

		if (moreCountStatistics.min>=0) {
			if (min>=0) min=Math.min(min,moreCountStatistics.min); else min=moreCountStatistics.min; /* Das brauchen wir nur bei min. Bei max wird der Fall max==-1 automatisch durch Math.max(...) erledigt. */
		}
		if (moreCountStatistics.max>=0) {
			max=Math.max(max,moreCountStatistics.max);
		}
		sum+=moreCountStatistics.sum;
		valueSum+=moreCountStatistics.valueSum;
		valueSumSquared+=moreCountStatistics.valueSumSquared;

		lastTimeMean=-1;
	}

	/**
	 * Setzt alle Teil-Kenngrößen auf 0 zurück.
	 */
	@Override
	public void reset() {
		lastState=0;
		lastTime=0;
		stateTime=null;
		time0=-1;
		timeMax=-1;
		timeMaxState=-1;

		min=-1;
		max=-1;
		sum=0;
		valueSum=0;
		valueSumSquared=0;

		lastTimeMean=-1;
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsTimePerformanceIndicator)) return;
		lastState=((StatisticsTimePerformanceIndicator)indicator).lastState;
		lastTime=((StatisticsTimePerformanceIndicator)indicator).lastTime;
		if (((StatisticsTimePerformanceIndicator)indicator).stateTime!=null) {
			stateTime=Arrays.copyOf(((StatisticsTimePerformanceIndicator)indicator).stateTime,((StatisticsTimePerformanceIndicator)indicator).stateTime.length);
		}
		time0=((StatisticsTimePerformanceIndicator)indicator).time0;
		timeMax=((StatisticsTimePerformanceIndicator)indicator).timeMax;
		timeMaxState=((StatisticsTimePerformanceIndicator)indicator).timeMaxState;

		min=((StatisticsTimePerformanceIndicator)indicator).min;
		max=((StatisticsTimePerformanceIndicator)indicator).max;
		sum=((StatisticsTimePerformanceIndicator)indicator).sum;
		valueSum=((StatisticsTimePerformanceIndicator)indicator).valueSum;
		valueSumSquared=((StatisticsTimePerformanceIndicator)indicator).valueSumSquared;

		lastTimeMean=-1;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsTimePerformanceIndicator clone() {
		final StatisticsTimePerformanceIndicator indicator=new StatisticsTimePerformanceIndicator(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsTimePerformanceIndicator cloneEmpty() {
		return new StatisticsTimePerformanceIndicator(xmlNodeNames);
	}

	/**
	 * Liefert die Verteilung zurück, wie lange sich das System in welchem Zustand befinden hat
	 * @return	Zeit-Verteilung über die Systemzustände
	 */
	public DataDistributionImpl getDistribution() {
		if (stateTime==null && time0<0 && timeMax<0) return new DataDistributionImpl(1,new double[]{0.0});
		forceExpandStateTime();
		return new DataDistributionImpl(stateTime.length,stateTime);
		/* stateTime.length ist die richtige Größe, sonst skaliert DataDistributionImpl und getMean() und Co. liefern verzerrte Werte. */
	}

	/**
	 * Leeres Verteilungs-Objekt welches von {@link #getReadOnlyDistribution()}
	 * zurückgeliefert wird, wenn noch keine Daten erfasst wurden.
	 * @see #getReadOnlyDistribution()
	 */
	private static DataDistributionImpl emptyDistribution=new DataDistributionImpl(1,new double[]{0.0});

	/**
	 * Zuletzt per {@link #getReadOnlyDistribution()} ausgelieferte Verteilung<br>
	 * (Wird wiederverwendet, wenn sich die Daten seit dem letzten Aufruf nicht verändert haben.)
	 * @see #getReadOnlyDistribution()
	 */
	private DataDistributionImpl readOnlyDistribution=null;

	/**
	 * Liefert die Verteilung zurück, wie lange sich das System in welchem Zustand befinden hat.
	 * Verwendet dabei die Originaldaten, d.h. das <code>DataDistributionImpl</code>-Objekt darf danach nicht verändert werden (z.B. darf es nicht mit anderen Objekten zusammengeführt werden)
	 * @return	Zeit-Verteilung über die Systemzustände
	 * @see StatisticsTimePerformanceIndicator#getDistribution
	 */
	public DataDistributionImpl getReadOnlyDistribution() {
		if (stateTime==null && time0<0 && timeMax<0) return emptyDistribution;
		forceExpandStateTime();
		if (readOnlyDistribution==null || readOnlyDistribution.densityData!=stateTime) readOnlyDistribution=new DataDistributionImpl(stateTime.length,stateTime,true);
		/* stateTime.length ist die richtige Größe, sonst skaliert DataDistributionImpl und getMean() und Co. liefern verzerrte Werte. */
		return readOnlyDistribution;
	}

	/**
	 * Liefert die Summe der Zeiten der Systemzustände zurück.
	 * @return	Summe der Zeiten der Systemzustände
	 */
	public double getSum() {
		return sum;
	}

	/**
	 * Liefert die normalisierte Häufigkeitsverteilung der Messreihe
	 * (nur Dichtewerte, keine Verteilung; diese kann aber per {@link DataDistributionImpl#updateCumulativeDensity()} berechnet werden)
	 * @return	Normalisierte Häufigkeitsverteilung der Messwerte in der Messreihe
	 */
	public DataDistributionImpl getNormalizedDistribution() {
		final DataDistributionImpl normalized=getDistribution();
		normalized.normalizeDensityOnly();
		return normalized;
	}

	/**
	 * Liefert die Laufzeit des Systems (Summe über die Aufenthaltszeiten in allen Zuständen)
	 * @return	Laufzeit des Systems
	 */
	public double getTimeSum() {
		return getReadOnlyDistribution().sumAsStoredAsString();
	}

	/**
	 * Liefert den Zustand, in dem sich das System im Mittel befunden hat (z.B. mittlere Anzahl an Kunden im System, wenn die Anzahl an Kunden im System erfasst wird)
	 * @return	Mittlerer Zustand
	 */
	public double getTimeMean() {
		if (lastTimeMean<0) {
			if (sum>0) lastTimeMean=valueSum/sum; else lastTimeMean=0;
		}
		/* So würde ggf. abgeschnitten werden: getReadOnlyDistribution().getMean(); */
		return lastTimeMean;
	}

	/**
	 * Berechnet ein Quantil der Messreihe aus der Häufigkeitsverteilung.
	 * @param sum	Summe über die Messreihe
	 * @param p	Wert für das Quantil
	 * @return	Quantil der Messreihe
	 * @see #getQuantil(double)
	 */
	private int getQuantil(final double sum, final double p) {
		final double quantilSum=sum*Math.min(1.0,Math.max(0.0,p));
		double partialSum=0;
		for (int i=0;i<stateTime.length;i++) {
			partialSum+=stateTime[i];
			if (partialSum>=quantilSum) return i;
		}
		return 0;
	}

	/**
	 * Berechnet ein Quantil der Messreihe aus der Häufigkeitsverteilung.
	 * @param p	Wert für das Quantil
	 * @return	Quantil der Messreihe
	 */
	public int getQuantil(final double p) {
		if (stateTime==null) {
			return (timeMax>0)?timeMaxState:0;
		}

		double sum=0.0;
		for (double value: stateTime) sum+=value;

		return getQuantil(sum,p);
	}

	/**
	 * Berechnet mehrere Quantile der Messreihe aus der Häufigkeitsverteilung.
	 * @param p	Werte für die Quantile
	 * @return	Quantile der Messreihe
	 */
	public int[] getQuantil(final double[] p) {
		if (p==null) return null;
		if (p.length==0) return new int[0];

		final int[] result=new int[p.length];

		if (stateTime==null) {
			Arrays.fill(result,(timeMax>0)?timeMaxState:0);
		} else {
			double sum=0.0;
			for (double value: stateTime) sum+=value;
			for (int i=0;i<p.length;i++) result[i]=getQuantil(sum,p[i]);
		}

		return result;
	}

	/**
	 * Berechnet den Median der Messreihe aus der Häufigkeitsverteilung.
	 * @return	Median der Messreihe
	 */
	public int getTimeMedian() {
		return getQuantil(0.5);
	}

	/**
	 * Liefert die Standardabweichung über die Verteilung der Zeiten in den verschiedenen Zuständen
	 * @return	Standardabweichung über die Verteilung der Zeiten in den verschiedenen Zuständen
	 */
	public double getTimeSD() {
		/* So würde ggf. abgeschnitten werden: return getReadOnlyDistribution().getStandardDeviation(); */
		if (sum<2) return 0;
		final double v=valueSumSquared/(sum-1)-(valueSum*valueSum)/sum/(sum-1);
		return StrictMath.sqrt(Math.max(0,v));
	}

	/**
	 * Liefert die Varianz über die Verteilung der Zeiten in den verschiedenen Zuständen
	 * @return	Varianz über die Verteilung der Zeiten in den verschiedenen Zuständen
	 */
	public double getTimeVar() {
		/* So würde ggf. abgeschnitten werden: double sd=getReadOnlyDistribution().getStandardDeviation(); return sd*sd; */
		if (sum<2) return 0;
		return Math.max(0,valueSumSquared/(sum-1)-(valueSum*valueSum)/sum/(sum-1));
	}

	/**
	 * Liefert den Variationskoeffizienten über die Verteilung der Zeiten in den verschiedenen Zuständen
	 * @return	Variationskoeffizienten über die Verteilung der Zeiten in den verschiedenen Zuständen
	 */
	public double getTimeCV() {
		/* So würde ggf. abgeschnitten werden: DataDistributionImpl dist=getReadOnlyDistribution(); return dist.getStandardDeviation()/FastMath.max(0.0001,FastMath.abs(dist.getMean())); */
		double mean=getTimeMean();
		return (mean>0)?(getTimeSD()/mean):0;
	}

	/**
	 * Liefert den höchsten Zustand, in dem sich das System eine Zeit &gt;0 befunden hat
	 * @return	Maximale Zustand, in dem sich das System eine positive Zeit lang befunden hat
	 */
	public int getTimeMax() {
		if ((stateTime==null || sum==0) && time0<0 && timeMax<0) return 0;
		/*
		So würde ggf. abgeschnitten werden:
		for (int i=stateTime.length-1;i>=0;i--) if (stateTime[i]>0) return i;
		return 0;
		 */
		return max;
	}

	/**
	 * Liefert den niedrigsten Zustand, in dem sich das System eine Zeit &gt;0 befunden hat
	 * @return	Minimaler Zustand, in dem sich das System eine positive Zeit lang befunden hat
	 */
	public int getTimeMin() {
		if ((stateTime==null || sum==0) && time0<0 && timeMax<0) return 0;
		/*
		So würde ggf. abgeschnitten werden:
		for (int i=0;i<stateTime.length;i++) if (stateTime[i]>0) return i;
		return stateTime.length-1;
		 */
		return min;
	}

	/**
	 * Liefert den Zeitanteil, in dem sich das System in einem bestimmten Zustand befunden hat
	 * @param state	Zustand, von dem der Zeitanteil abgefragt werden soll
	 * @return	Zeitanteil, in dem sich das System in dem Zustand befunden hat
	 */
	public double getTimePartForState(int state) {
		if (stateTime==null && time0<0 && timeMax<0) return 0;
		if (stateTime==null) {
			if (state<0) return 0;
			if (state==0) {
				if (timeMaxState<0) return (time0>0)?1:0;
				if (time0+timeMax>0) return	time0/(time0+timeMax); else return 0;
			}
			if (state==timeMaxState) {
				if (timeMax>0) return timeMax/(time0+timeMax); else return 0;
			}
			return 0;
		} else {
			if (state<0 || state>=stateTime.length) return 0.0;
			return stateTime[state]/sum;
		}
	}

	@Override
	protected void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder) {
		if (stateTime==null) {
			final StringBuilder sb;
			if (recycleStringBuilder==null) {
				sb=new StringBuilder();
			} else {
				sb=recycleStringBuilder;
				sb.setLength(0);
			}
			sb.append(NumberTools.formatSystemNumber(time0>0?time0:0));
			if (timeMaxState>0) {
				sb.append(";");
				for (int i=1;i<Math.min(timeMaxState,MAX_STATE);i++) sb.append("0;");
				sb.append(NumberTools.formatSystemNumber(timeMax));
			}
			node.setTextContent(sb.toString());
		} else {
			if (getTimeMax()==0 || getTimeMax()==-1) {
				node.setTextContent(NumberTools.formatSystemNumber(getReadOnlyDistribution().densityData[0]));
			} else {
				node.setTextContent(getReadOnlyDistribution().storeToStringShort());
			}
		}

		node.setAttribute(xmlNameSum,NumberTools.formatSystemNumber(getSum(),recycleStringBuilder));
		node.setAttribute(xmlNameValues[0],NumberTools.formatSystemNumber(valueSum,recycleStringBuilder));
		node.setAttribute(xmlNameValuesSquared[0],NumberTools.formatSystemNumber(valueSumSquared,recycleStringBuilder));
		node.setAttribute(xmlNameMean,NumberTools.formatSystemNumber(getTimeMean(),recycleStringBuilder));
		node.setAttribute(xmlNameSD,NumberTools.formatSystemNumber(getTimeSD(),recycleStringBuilder));
		node.setAttribute(xmlNameCV,NumberTools.formatSystemNumber(getTimeCV(),recycleStringBuilder));
		node.setAttribute(xmlNameMin[0],""+Math.max(0,getTimeMin()));
		node.setAttribute(xmlNameMax[0],""+Math.max(0,getTimeMax()));

		if (stateTime!=null) {
			final int[] quantils=getQuantil(storeQuantilValues);
			for (int i=0;i<storeQuantilValues.length;i++) {
				node.setAttribute(xmlNameQuantil+Math.round(storeQuantilValues[i]*100),""+quantils[i]);
			}
		}
	}

	@Override
	public String loadFromXML(Element node) {
		final DataDistributionImpl dist=DataDistributionImpl.createFromString(node.getTextContent(),1000);
		if (dist==null) return String.format(xmlLoadError,node.getNodeName());
		stateTime=dist.densityData;
		sum=dist.sum();
		lastTimeMean=-1;

		String value;

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameValues));
		if (!value.isEmpty()) {
			final Double valueSum=NumberTools.getDouble(value);
			if (valueSum==null) return String.format(xmlNameValuesError,node.getNodeName(),value);
			this.valueSum=valueSum;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameValuesSquared));
		if (!value.isEmpty()) {
			final Double valueSum2=NumberTools.getDouble(value);
			if (valueSum2==null || valueSum2<0) return String.format(xmlNameValuesSquaredError,node.getNodeName(),value);
			valueSumSquared=valueSum2;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameMin));
		if (!value.isEmpty()) {
			final Integer min=NumberTools.getInteger(value);
			if (min==null) return String.format(xmlNameMinError,node.getNodeName(),value);
			this.min=min;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameMax));
		if (!value.isEmpty()) {
			final Integer max=NumberTools.getInteger(value);
			if (max==null) return String.format(xmlNameMaxError,node.getNodeName(),value);
			this.max=max;
		}

		return null;
	}
}