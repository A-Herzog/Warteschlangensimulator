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

import org.apache.commons.math3.distribution.TDistribution;
import org.w3c.dom.Element;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;

/**
 * Statistik-Klasse, die einzelne Werte erfasst (als aggregierte Werte).
 * Im Gegensatz zu {@link StatisticsDataPerformanceIndicator} erfasst diese Klasse
 * auch negative Werte. Verteilungen enthalten allerdings trotzdem nur positive Werte.<br>
 * Die Zählung wird über die Funktion {@link StatisticsDataPerformanceIndicatorWithNegativeValues#add(double)} realisiert.
 * @author Alexander Herzog
 * @version 1.4
 */
public final class StatisticsDataPerformanceIndicatorWithNegativeValues extends StatisticsPerformanceIndicator implements Cloneable {
	/** XML-Attribut für "Anzahl" */
	public static String[] xmlNameCount=new String[]{"Anzahl"};
	/** Fehlermeldung, wenn das "Anzahl"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameCountError="Das Anzahl-Attribut im \"%s\"-Element muss eine nicht-negative Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Summe" */
	public static String[] xmlNameSum=new String[]{"Summe"};
	/** Fehlermeldung, wenn das "Summe2"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameSumError="Das Summe-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Summe2" (=Summe der quadrierten Werte) */
	public static String[] xmlNameSumSquared=new String[]{"Summe2"};
	/** Fehlermeldung, wenn das "Summe2"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameSumSquaredError="Das Summe2-Attribut im \"%s\"-Element muss eine nicht-negative Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Mittelwert" */
	public static String[] xmlNameMean=new String[]{"Mittelwert"};
	/** XML-Attribut für "Standardabweichung" */
	public static String[] xmlNameSD=new String[]{"Standardabweichung"};
	/** XML-Attribut für "Variationskoeffizient" */
	public static String[] xmlNameCV=new String[]{"Variationskoeffizient"};
	/** XML-Attribut für "Minimum" */
	public static String[] xmlNameMin=new String[]{"Minimum"};
	/** Fehlermeldung, wenn das "Minimum"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameMinError="Das Minimum-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Maximum" */
	public static String[] xmlNameMax=new String[]{"Maximum"};
	/** Fehlermeldung, wenn das "Maximum"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameMaxError="Das Maximum-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Verteilung" */
	public static String[] xmlNameDistribution=new String[]{"Verteilung"};
	/** Fehlermeldung, wenn das "Verteilung"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameDistributionError="Das Verteilung-Attribut im \"%s\"-Element muss eine Häufigkeitsverteilung enthalten.";
	/** XML-Attribut für "Quantil" */
	public static String xmlNameQuantil="Quantil";

	/**
	 * Quantile, die aus der Häufigkeitsverteilung berechnet und in der xml-Datei gespeichert werden
	 */
	public static final double[] storeQuantilValues=new double[] {0.10,0.25,0.5,0.75,0.9};

	/**
	 * Anzahl der erfassten Messwerte
	 */
	private long count;

	/**
	 * Letzter hinzugefügter Wert
	 */
	private double last;

	/**
	 * Summe der Messwerte
	 */
	private double sum;

	/**
	 * Summe der quadrierten Messwerte
	 */
	private double squaredSum;

	/**
	 * Minimaler Messwert
	 */
	private double min;

	/**
	 * Maximaler Messwert
	 */
	private double max;

	/**
	 * Häufigkeitsverteilung der Messwerte (kann <code>null</code> sein, wenn keine Häufigkeitsverteilung erhoben wird)
	 */
	private DataDistributionImpl dist;

	/**
	 * Referenz auf <code>dist.densityData</code> um Dereferenzierungen bei <code>add(value)</code> zu vermeiden.
	 */
	private double[] densityData;

	/**
	 * Länge von <code>densityDat</code>, um diese nicht immer wieder neu auslesen zu müssen.
	 */
	private int densityDataLength;

	/**
	 * Skalierung der Array-Einträge von <code>dist</code>
	 * @see StatisticsDataPerformanceIndicatorWithNegativeValues#dist
	 * @see StatisticsDataPerformanceIndicatorWithNegativeValues#argumentScaleFactorIsOne
	 */
	private double argumentScaleFactor;

	/**
	 * Es findet keine Skalierung statt.
	 * @see StatisticsDataPerformanceIndicatorWithNegativeValues#argumentScaleFactor
	 */
	private boolean argumentScaleFactorIsOne;

	/**
	 * Obergrenze des Trägers der Häufigkeitsverteilung
	 */
	private final double upperBound;

	/**
	 * Wie viele einzelne Werte sollen für die Häufigkeitsverteilung vorgehalten werden?
	 */
	private final int steps;

	/**
	 * Sollen Verteilungswerte erfasst werden?
	 */
	private final boolean hasDistribution;

	/**
	 * Anzahl der Werte, die für die Verteilung als zum Zeitpunkt 0 erfasst werden sollen.<br>
	 * (Dieser Zähler wird verwendet, so lange {@link #dist} noch nicht initialisiert wurde.)
	 */
	private long distributionZeroCount;

	/**
	 * Konstruktor der Klasse <code>StatisticsDataPerformanceIndicatorWthNegativeValues</code>
	 * Bei der Datenaufzeichnung wird eine Häufigkeitsverteilung der Werte angelegt
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param upperBound	Gibt die Obergrenze des Trägers der Häufigkeitsverteilung an
	 * @param steps	Gibt an, wie viele einzelne Werte für die Häufigkeitsverteilung vorgehalten werden sollen
	 */
	public StatisticsDataPerformanceIndicatorWithNegativeValues(final String[] xmlNodeNames, final double upperBound, final int steps) {
		this(xmlNodeNames,upperBound,steps,false);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticsDataPerformanceIndicatorWthNegativeValues</code>
	 * Bei der Datenaufzeichnung wird eine Häufigkeitsverteilung der Werte angelegt
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param upperBound	Gibt die Obergrenze des Trägers der Häufigkeitsverteilung an
	 * @param steps	Gibt an, wie viele einzelne Werte für die Häufigkeitsverteilung vorgehalten werden sollen
	 * @param isEmpty	Gibt an, ob es sich bei diesem Objekt um eine leere Kopiervorlage handelt
	 */
	public StatisticsDataPerformanceIndicatorWithNegativeValues(final String[] xmlNodeNames, final double upperBound, final int steps, final boolean isEmpty) {
		super(xmlNodeNames);
		this.upperBound=upperBound;
		this.steps=steps;

		if (steps>0 && !isEmpty) {
			hasDistribution=true;
			argumentScaleFactor=steps/((upperBound==86399)?86400:upperBound);
			argumentScaleFactorIsOne=(Math.abs(argumentScaleFactor-1)<1E-10);
		} else {
			hasDistribution=false;
		}

		reset();
	}

	/**
	 * Initialisiert die Verteilungsdaten
	 * @see #add(double)
	 * @see #add(double, long)
	 * @see #add(StatisticsPerformanceIndicator)
	 */
	private void initDistribution() {
		dist=new DataDistributionImpl(upperBound,steps);
		densityData=dist.densityData;
		densityDataLength=densityData.length;
		densityData[0]=distributionZeroCount;
	}

	/**
	 * Fügt einen Wert zu der Messreihe hinzu
	 * @param value	Hinzuzufügender Wert
	 */
	public void add(final double value) {
		/* Anzahl */
		last=value;
		count++;

		/* Minimum, Maximum */
		if (count>1) {
			if (value>max) max=value; else {
				if (value<min) min=value;
			}
			/* langsamer: min=FastMath.min(min,value); max=FastMath.max(max,value);  */
		} else {
			min=value;
			max=value;
		}

		/* Summe, quadrierte Summe */
		sum+=value;
		squaredSum+=(value*value);

		/* Verteilung der Werte */
		if (hasDistribution) {
			if (value>0.0d) {
				final long l;
				if (argumentScaleFactorIsOne) {
					/* langsamer:  l=FastMath.round(value); */
					l=(long)(value+0.5);
				} else {
					/* langsamer: l=FastMath.round(argumentScaleFactor*value); */
					l=(long)((argumentScaleFactor*value)+0.5);
				}
				if (l>0) {
					if (dist==null) initDistribution();
					if (l<densityDataLength) {
						final int index=(int)l;
						densityData[index]++;
					} else {
						densityData[densityDataLength-1]++;
					}
				} else {
					if (dist==null) distributionZeroCount++; else densityData[0]++;
				}
			} else {
				if (dist==null) distributionZeroCount++; else densityData[0]++;
			}
		}
	}

	/**
	 * Fügt mehrere gleiche Werte zu der Messreihe hinzu.
	 * @param value	Hinzuzufügender Wert
	 * @param count	Häufigkeit mit der der Wert hinzugefügt werden soll
	 */
	public void add(final double value, final long count) {
		if (count<1) return;

		/* Anzahl */
		last=value;
		this.count+=count;

		/* Minimum, Maximum */
		if (this.count==count) {
			min=value;
			max=value;
		} else {
			if (value>max) max=value; else {
				if (value<min) min=value;
			}
			/* langsamer: min=FastMath.min(min,value); max=FastMath.max(max,value);  */
		}

		/* Summe, quadrierte Summe */
		sum+=value*count;
		squaredSum+=(value*value)*count;

		/* Verteilung der Werte */
		if (hasDistribution) {
			if (value<=0.0d) {
				if (dist==null) distributionZeroCount++; else densityData[0]++;
			} else {
				final long l;
				if (argumentScaleFactorIsOne) {
					/* langsamer:  l=FastMath.round(value); */
					l=(long)(value+0.5);
				} else {
					/* langsamer: l=FastMath.round(argumentScaleFactor*value); */
					l=(long)((argumentScaleFactor*value)+0.5);
				}
				if (l<=0) {
					if (dist==null) distributionZeroCount++; else densityData[0]++;
				} else {
					if (dist==null) initDistribution();
					if (l>=densityDataLength) {
						densityData[densityDataLength-1]+=count;
					} else {
						final int index=(int)l;
						densityData[index]+=count;
					}
				}
			}
		}
	}

	/**
	 * Fügt zu der Teil-Messreihe eine weitere Teilmessreihe hinzu
	 * @param moreStatistics	Statistikobjekt, dessen Daten zu diesem hinzugefügt werden sollen
	 */
	@Override
	public void add(final StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsDataPerformanceIndicatorWithNegativeValues)) return;
		StatisticsDataPerformanceIndicatorWithNegativeValues moreDataStatistics=(StatisticsDataPerformanceIndicatorWithNegativeValues)moreStatistics;

		/* Allgemeine Daten */
		if (moreDataStatistics.count>0) {
			if (count==0) {
				min=moreDataStatistics.min;
				max=moreDataStatistics.max;
			} else {
				min=Math.min(min,moreDataStatistics.min);
				max=Math.min(max,moreDataStatistics.max);
			}
		}

		count+=moreDataStatistics.count;
		sum+=moreDataStatistics.sum;
		squaredSum+=moreDataStatistics.squaredSum;

		/* Verteilung der Werte */
		if (hasDistribution && moreDataStatistics.hasDistribution) {
			if (dist==null) initDistribution();
			if (moreDataStatistics.dist==null) {
				densityData[0]+=moreDataStatistics.distributionZeroCount;
			} else {
				dist.addToThis(moreDataStatistics.dist);
				densityData=dist.densityData;
				densityDataLength=densityData.length;
			}
		}
	}

	/**
	 * Setzt alle Teil-Kenngrößen auf 0 zurück.
	 */
	@Override
	public void reset() {
		/* Allgemeine Daten */
		min=0;
		max=0;
		count=0;
		sum=0;
		squaredSum=0;

		/* Verteilung der Werte */
		if (dist!=null) dist.setToValue(0.0);
		distributionZeroCount=0;
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsDataPerformanceIndicatorWithNegativeValues)) return;
		final StatisticsDataPerformanceIndicatorWithNegativeValues data=(StatisticsDataPerformanceIndicatorWithNegativeValues)indicator;

		/* Allgemeine Daten */
		count=data.count;
		sum=data.sum;
		squaredSum=data.squaredSum;
		min=data.min;
		max=data.max;

		/* Verteilung der Werte */
		if (data.dist!=null) {
			dist=data.dist.clone();
			densityData=dist.densityData;
			densityDataLength=densityData.length;
			argumentScaleFactor=data.argumentScaleFactor;
			argumentScaleFactorIsOne=data.argumentScaleFactorIsOne;
		}
		distributionZeroCount=data.distributionZeroCount;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsDataPerformanceIndicatorWithNegativeValues clone() {
		final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=new StatisticsDataPerformanceIndicatorWithNegativeValues(xmlNodeNames,upperBound,steps);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsDataPerformanceIndicatorWithNegativeValues cloneEmpty() {
		return new StatisticsDataPerformanceIndicatorWithNegativeValues(xmlNodeNames,upperBound,steps);
	}

	/**
	 * Liefert die Anzahl der Messwerte, aus der die Messreihe besteht
	 * @return	Anzahl der erfassten Messwerte in der Messreihe
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Liefert den als letztes hinzugefügten Wert
	 * @return	Als letztes hinzugefügter Wert
	 */
	public double getLastAddedValue() {
		return last;
	}

	/**
	 * Liefert die Summe alle Messwerte, aus der die Messreihe besteht
	 * @return	Summe aller Messwerte
	 */
	public double getSum() {
		return sum;
	}

	/**
	 * Berechnet aus den Messreihen-Kenngrößen den Mittelwert
	 * @return	Mittelwert der Messreihe
	 */
	public double getMean() {
		if (count==0) return 0;
		return sum/count;
	}

	/**
	 * Berechnet ein Quantil der Messreihe aus der Häufigkeitsverteilung.
	 * @param sum	Summe über die Messreihe
	 * @param p	Wert für das Quantil
	 * @return	Quantil der Messreihe
	 * @see #getQuantil(double)
	 */
	private double getQuantil(final double sum, final double p) {
		final double quantilSum=sum*Math.min(1.0,Math.max(0.0,p));
		int index=-1;
		double partialSum=0;
		for (int i=0;i<densityDataLength;i++) {
			partialSum+=densityData[i];
			if (partialSum>=quantilSum) {index=i; break;}
		}
		if (index<0) return 0.0;

		if (argumentScaleFactorIsOne) return index; else {
			if (argumentScaleFactor==0.0) return 0.0;
			return index/argumentScaleFactor;
		}
	}

	/**
	 * Berechnet ein Quantil der Messreihe aus der Häufigkeitsverteilung.<br>
	 * Die Quantile stehen nur zur Verfügung, wenn eine Häufigkeitsverteilung erfasst wurde,
	 * also {@link #getDistribution()} einen Wert ungleich <code>null</code> besitzt.
	 * @param p	Wert für das Quantil
	 * @return	Quantil der Messreihe
	 * @see #getDistribution()
	 */
	public double getQuantil(final double p) {
		if (dist==null) return 0.0;

		return getQuantil(dist.getSum(),p);
	}

	/**
	 * Berechnet mehrere Quantile der Messreihe  aus der Häufigkeitsverteilung.<br>
	 * Die Quantile stehen nur zur Verfügung, wenn eine Häufigkeitsverteilung erfasst wurde,
	 * also {@link #getDistribution()} einen Wert ungleich <code>null</code> besitzt.
	 * @param p	Werte für die Quantile
	 * @return	Quantile der Messreihe
	 * @see #getDistribution()
	 */
	public double[] getQuantil(final double[] p) {
		if (p==null) return null;
		if (p.length==0) return new double[0];

		final double[] result=new double[p.length];

		if (dist!=null) {
			final double sum=dist.getSum();
			for (int i=0;i<p.length;i++) result[i]=getQuantil(sum,p[i]);
		}

		return result;
	}

	/**
	 * Berechnet den Median der Messreihe aus der Häufigkeitsverteilung.<br>
	 * Der Median steht nur zur Verfügung, wenn eine Häufigkeitsverteilung erfasst wurde,
	 * also {@link #getDistribution()} einen Wert ungleich <code>null</code> besitzt.
	 * @return	Median der Messreihe
	 * @see #getDistribution()
	 */
	public double getMedian() {
		return getQuantil(0.5);
	}

	/**
	 * Berechnet aus den Messreihen-Kenngrößen die Standardabweichung
	 * @return	Standardabweichung der Messreihe
	 */
	public double getSD() {
		if (count<2) return 0;
		if (max==min) return 0;
		final double v=squaredSum/(count-1)-(sum*sum)/count/(count-1);
		return StrictMath.sqrt(Math.max(0,v));
	}

	/**
	 * Berechnet aus den Messreihen-Kenngrößen die Varianz
	 * @return	Standardabweichung der Messreihe
	 */
	public double getVar() {
		if (count<2) return 0;
		if (max==min) return 0;
		final double v=squaredSum/(count-1)-(sum*sum)/count/(count-1);
		return Math.max(0,v);
	}

	/**
	 * Berechnet aus den Messreihen-Kenngrößen den Variationskoeffizient
	 * @return	Variationskoeffizient der Messreihe
	 */
	public double getCV() {
		double mean=getMean();
		return (mean>0)?(getSD()/mean):0;
	}

	/**
	 * Liefert den maximalen Wert, der aufgetreten ist
	 * @return	Maximaler Wert der Messreihe
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Liefert den minimalen Wert, der aufgetreten ist
	 * @return	Minimaler Wert der Messreihe
	 */
	public double getMin() {
		return min;
	}

	/**
	 * Liefert die Häufigkeitsverteilung der Messreihe (oder <code>null</code>, wenn die Erfassung der Verteilung deaktiviert ist)
	 * @return	Häufigkeitsverteilung der Messwerte in der Messreihe
	 */
	public DataDistributionImpl getDistribution() {
		if (hasDistribution && dist==null) initDistribution();
		return dist;
	}

	/**
	 * Liefert die normalisierte Häufigkeitsverteilung der Messreihe (oder <code>null</code>, wenn die Erfassung der Verteilung deaktiviert ist)
	 * (nur Dichtewerte, keine Verteilung; diese kann aber per {@link DataDistributionImpl#updateCumulativeDensity()} berechnet werden)
	 * @return	Normalisierte Häufigkeitsverteilung der Messwerte in der Messreihe
	 */
	public DataDistributionImpl getNormalizedDistribution() {
		if (hasDistribution && dist==null) initDistribution();
		if (dist==null) return null;
		DataDistributionImpl normalized=dist.clone();
		normalized.normalizeDensityOnly();
		return normalized;
	}

	/**
	 * Halbe Breite des Konfidenzintervalls für den Mittelwert<br>
	 * Das Konfidenzintervall geht dann von <code>getMean()-getConfidenceHalfWide(alpha)</code> bis <code>getMean()+getConfidenceHalfWide(alpha)</code>
	 * @param alpha	Konfidenzniveau (z.B. alpha=0.05 oder alpha=0.01)
	 * @return	Halbe Breite des Konfidenzintervalls
	 */
	public double getConfidenceHalfWide(final double alpha) {
		if (count<2) return 0; /* count==1 führt zu TDistribution(0), was nicht geht. */
		if (min==max) return 0;
		final TDistribution dist=new TDistribution(count-1);
		final double t=dist.inverseCumulativeProbability(1-alpha/2);
		final double sd=getSD();
		return t*sd/StrictMath.sqrt(count);
	}

	/**
	 * Halbe Breite des Konfidenzintervalls für den Mittelwert (mehrere Konfidenzniveaus gleichzeitig)<br>
	 * Das Konfidenzintervall geht dann von <code>getMean()-getConfidenceHalfWide(alpha)</code> bis <code>getMean()+getConfidenceHalfWide(alpha)</code>
	 * @param alpha	Konfidenzniveaus (z.B. alpha=0.05 oder alpha=0.01)
	 * @return	Halbe Breite der Konfidenzintervalle
	 */
	public double[] getConfidenceHalfWide(final double[] alpha) {
		if (alpha==null || alpha.length==0) return new double[0];
		if (count==0) return new double[alpha.length];
		if (min==max) return new double[alpha.length];

		final TDistribution dist=new TDistribution(count-1);
		final double sd=getSD();

		final double invSqrtCount=1/StrictMath.sqrt(count);
		final double[] results=new double[alpha.length];
		for (int i=0;i<alpha.length;i++) {
			final double t=dist.inverseCumulativeProbability(1-alpha[i]/2);
			results[i]=t*sd*invSqrtCount;
		}
		return results;
	}

	/**
	 * Speichert eine Kenngröße, die intern aus Anzahl, Summe und Summe der quadrierten Werte besteht, in einem xml-Knoten.
	 * Es werden dabei zusätzlich Mittelwert, Standardabweichung und Variationskoeffizient berechnet und gespeichert
	 * @param node	Neuer xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	@Override
	public void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder) {
		node.setAttribute(xmlNameCount[0],""+count);
		node.setAttribute(xmlNameSum[0],NumberTools.formatSystemNumber(sum,recycleStringBuilder));
		node.setAttribute(xmlNameSumSquared[0],NumberTools.formatSystemNumber(squaredSum,recycleStringBuilder));
		node.setAttribute(xmlNameMean[0],NumberTools.formatSystemNumber(getMean(),recycleStringBuilder));
		node.setAttribute(xmlNameSD[0],NumberTools.formatSystemNumber(getSD(),recycleStringBuilder));
		node.setAttribute(xmlNameCV[0],NumberTools.formatSystemNumber(getCV(),recycleStringBuilder));
		node.setAttribute(xmlNameMin[0],NumberTools.formatSystemNumber(getMin(),recycleStringBuilder));
		node.setAttribute(xmlNameMax[0],NumberTools.formatSystemNumber(getMax(),recycleStringBuilder));

		if (hasDistribution) {
			if (dist==null) initDistribution();

			node.setAttribute(xmlNameDistribution[0],dist.storeToString(recycleStringBuilder));

			final double[] quantils=getQuantil(storeQuantilValues);
			for (int i=0;i<storeQuantilValues.length;i++) {
				node.setAttribute(xmlNameQuantil+Math.round(storeQuantilValues[i]*100),NumberTools.formatSystemNumber(quantils[i],recycleStringBuilder));
			}
		}
	}

	/**
	 * Versucht eine Kenngröße, die intern durch die Anzahl an Messwerten, deren Summe und deren quadrierte Summe repräsentiert wird, aus einem xml-Knoten zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	@Override
	public String loadFromXML(final Element node) {
		String value;

		value=getAttributeValue(node,xmlNameCount);
		if (!value.isEmpty()) {
			Long count=NumberTools.getLong(value);
			if (count==null || count<0) return String.format(xmlNameCountError,node.getNodeName(),value);
			this.count=count;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameSum));
		if (!value.isEmpty()) {
			Double sum=NumberTools.getDouble(value);
			if (sum==null) return String.format(xmlNameSumError,node.getNodeName(),value);
			this.sum=sum;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameSumSquared));
		if (!value.isEmpty()) {
			Double sum2=NumberTools.getDouble(value);
			if (sum2==null || sum2<0) return String.format(xmlNameSumSquaredError,node.getNodeName(),value);
			squaredSum=sum2;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameMin));
		if (!value.isEmpty()) {
			Double min=NumberTools.getDouble(value);
			if (min==null) return String.format(xmlNameMinError,node.getNodeName(),value);
			this.min=min;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameMax));
		if (!value.isEmpty()) {
			Double max=NumberTools.getDouble(value);
			if (max==null) return String.format(xmlNameMaxError,node.getNodeName(),value);
			this.max=max;
		}

		if (hasDistribution) {
			if (dist==null) initDistribution();
			value=getAttributeValue(node,xmlNameDistribution);
			if (!value.isEmpty()) {
				final DataDistributionImpl distLoaded=DataDistributionImpl.createFromString(value,dist.upperBound);
				if (distLoaded==null) return String.format(xmlNameDistributionError,node.getNodeName());
				dist=distLoaded;
				densityData=dist.densityData;
				densityDataLength=densityData.length;
			}
		}

		return null;
	}
}