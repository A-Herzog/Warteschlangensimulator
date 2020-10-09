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

import org.apache.commons.math3.distribution.TDistribution;
import org.w3c.dom.Element;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;

/**
 * Statistik-Klasse, die einzelne Werte erfasst (als aggregierte Werte).
 * Dies ist die Standard-Klasse zur Erfassung von Wartezeiten usw.<br>
 * Die Z�hlung wird �ber die Funktion {@link StatisticsDataPerformanceIndicator#add(double)} realisiert.
 * @author Alexander Herzog
 * @version 2.3
 */
public final class StatisticsDataPerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/** XML-Attribut f�r "Anzahl" */
	public static String[] xmlNameCount=new String[]{"Anzahl"};
	/** Fehlermeldung, wenn das "Anzahl"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameCountError="Das Anzahl-Attribut im \"%s\"-Element muss eine nicht-negative Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut f�r "Summe" */
	public static String[] xmlNameSum=new String[]{"Summe"};
	/** Fehlermeldung, wenn das "Summe"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameSumError="Das Summe-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut f�r "Summe2" (=Summe der quadrierten Werte) */
	public static String[] xmlNameSumSquared=new String[]{"Summe2"};
	/** Fehlermeldung, wenn das "Summe2"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameSumSquaredError="Das Summe2-Attribut im \"%s\"-Element muss eine nicht-negative Zahl sein, ist aber \"%s\".";
	/** XML-Attribut f�r "Mittelwert" */
	public static String[] xmlNameMean=new String[]{"Mittelwert"};
	/** XML-Attribut f�r "Standardabweichung" */
	public static String[] xmlNameSD=new String[]{"Standardabweichung"};
	/** XML-Attribut f�r "Variationskoeffizient" */
	public static String[] xmlNameCV=new String[]{"Variationskoeffizient"};
	/** XML-Attribut f�r "Minimum" */
	public static String[] xmlNameMin=new String[]{"Minimum"};
	/** Fehlermeldung, wenn das "Minimum"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameMinError="Das Minimum-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut f�r "Maximum" */
	public static String[] xmlNameMax=new String[]{"Maximum"};
	/** Fehlermeldung, wenn das "Maximum"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameMaxError="Das Maximum-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut f�r "Verteilung" */
	public static String[] xmlNameDistribution=new String[]{"Verteilung"};
	/** Fehlermeldung, wenn das "Verteilung"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameDistributionError="Das Verteilung-Attribut im \"%s\"-Element muss eine H�ufigkeitsverteilung enthalten.";
	/** XML-Attribut f�r "Autokorrelation" */
	public static String[] xmlNameCorrelation=new String[]{"Autokorrelation"};
	/** Fehlermeldung, wenn das "Autokorrelation"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameCorrelationError="Das Autokorrelation-Attribut im \"%S\"-Element muss die Autokorrelationswerte beinhalten.";
	/** XML-Attribut f�r "BatchGroesse" */
	public static String[] xmlNameBatchSize=new String[]{"BatchGroesse"};
	/** Fehlermeldung, wenn das "Verteilung"-BatchGroesse nicht gelesen werden konnte. */
	public static String xmlNameBatchSizeError="Das BatchGroesse-Attribut im \"%s\"-Element muss eine positive Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut f�r "BatchAnzahl" */
	public static String[] xmlNameBatchCount=new String[]{"BatchAnzahl"};
	/** Fehlermeldung, wenn das "BatchAnzahl"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameBatchCountError="Das BatchAnzahl-Attribut im \"%s\"-Element muss eine positive Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut f�r "BatchVarianz" */
	public static String[] xmlNameBatchMeansVar=new String[]{"BatchVarianz"};
	/** Fehlermeldung, wenn das "BatchVarianz"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameBatchMeansVarError="Das BatchVarianz-Attribut im \"%s\"-Element eine nicht-negative Zahl sein, ist aber \"%s\".";
	/** XML-Attribut f�r "MittelwertKonfidenzRadius" */
	public static String[] xmlNameMeanBatchHalfWide=new String[]{"MittelwertKonfidenzRadius"};
	/** XML-Attribut f�r "Quantil" */
	public static String xmlNameQuantil="Quantil";

	/**
	 * Quantile, die aus der H�ufigkeitsverteilung berechnet und in der xml-Datei gespeichert werden
	 */
	public static final double[] storeQuantilValues=new double[] {0.10,0.25,0.5,0.75,0.9};

	/**
	 * Schrittweite f�r die Berechnung der Autokorrelation
	 */
	public static final int CORRELATION_RANGE_STEPPING=10;

	/**
	 * Anzahl der erfassten Messwerte
	 */
	private long count;

	/**
	 * Letzter hinzugef�gter Wert
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
	 * H�ufigkeitsverteilung der Messwerte (kann <code>null</code> sein, wenn keine H�ufigkeitsverteilung erhoben wird)
	 */
	private DataDistributionImpl dist;

	/**
	 * Referenz auf <code>dist.densityData</code> um Dereferenzierungen bei <code>add(value)</code> zu vermeiden.
	 */
	private double[] densityData;

	/**
	 * L�nge von <code>densityDat</code>, um diese nicht immer wieder neu auslesen zu m�ssen.
	 */
	private int densityDataLength;

	/**
	 * Partialsummen �ber x_i * x_(i-k) zur Bestimmung der Korrelation
	 * (nur w�hrend der Datenerfassung und beim Zusammenf�hren relevant)
	 */
	private double[] correlationSums;

	/**
	 * Tempor�re Erfassung von Werten, um die Autokorrelation bestimmen zu k�nnen
	 * (nur w�hrend der Datenerfassung relevant)
	 */
	private double[] correlationTempValues;

	/**
	 * Korrelationswerte zur Schrittweite
	 * @see StatisticsDataPerformanceIndicator#CORRELATION_RANGE_STEPPING
	 */
	private double[] correlation;

	/**
	 * Skalierung der Array-Eintr�ge von <code>dist</code>
	 * @see StatisticsDataPerformanceIndicator#dist
	 * @see StatisticsDataPerformanceIndicator#argumentScaleFactorIsOne
	 */
	private double argumentScaleFactor;

	/**
	 * Es findet keine Skalierung statt.
	 * @see StatisticsDataPerformanceIndicator#argumentScaleFactor
	 */
	private boolean argumentScaleFactorIsOne;

	/**
	 * Batchgr��e f�r Batch-Means-Methode
	 */
	private int batchSize;

	/**
	 * Anzahl der erfassten Batche
	 */
	private int batchMeansCount;

	/**
	 * Summe der erfassten Batch-Mittelwerte
	 */
	private double batchMeansSum;

	/**
	 * Summe der quadrierten Batch-Mittelwerte
	 */
	private double batchMeansSum2;

	/**
	 * Varianz zwischen den Batch-Mittelwerten
	 * Wird von StatisticsDataPerformanceIndicator#getBatchVar() berechnet und dann hier gespeichert.
	 * @see StatisticsDataPerformanceIndicator#getBatchVar()
	 */
	private double batchMeansVar;

	/**
	 * Summe der Daten, die f�r den Mittelwert des n�chsten Batches herangezogen werden sollen
	 */
	private double batchTempSum;

	/**
	 * Anzahl der bereits aufgezeichneten Daten, die f�r die Berechnung des Mittelwertes des n�chsten Batches herangezogen werden sollen
	 */
	private int batchTempCount;

	/**
	 * Obergrenze des Tr�gers der H�ufigkeitsverteilung
	 */
	private final double upperBound;

	/**
	 * Wie viele einzelne Werte sollen f�r die H�ufigkeitsverteilung vorgehalten werden?
	 */
	private final int steps;

	/**
	 * Sollen Verteilungswerte erfasst werden?
	 */
	private final boolean hasDistribution;

	/**
	 * Anzahl der Werte, die f�r die Verteilung als zum Zeitpunkt 0 erfasst werden sollen.<br>
	 * (Dieser Z�hler wird verwendet, so lange {@link #dist} noch nicht initialisiert wurde.)
	 */
	private long distributionZeroCount;

	/**
	 * Konstruktor der Klasse <code>StatisticsDataPerformanceIndicator</code>
	 * Bei der Datenaufzeichnung wird eine H�ufigkeitsverteilung der Werte angelegt
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param upperBound	Gibt die Obergrenze des Tr�gers der H�ufigkeitsverteilung an
	 * @param steps	Gibt an, wie viele einzelne Werte f�r die H�ufigkeitsverteilung vorgehalten werden sollen
	 */
	public StatisticsDataPerformanceIndicator(final String[] xmlNodeNames, final double upperBound, final int steps) {
		this(xmlNodeNames,upperBound,steps,-1,1,false);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticsDataPerformanceIndicator</code>
	 * Bei der Datenaufzeichnung wird eine H�ufigkeitsverteilung der Werte angelegt
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param upperBound	Gibt die Obergrenze des Tr�gers der H�ufigkeitsverteilung an
	 * @param steps	Gibt an, wie viele einzelne Werte f�r die H�ufigkeitsverteilung vorgehalten werden sollen
	 * @param correlationRange	Reichweite f�r die Erfassung der Autokorrelation (Werte &le;0 schalten die Erfassung aus)
	 * @param batchSize	Wird hier ein Wert &gt;1 �bergeben, so werden Batch-Means erfasst, auf deren Basis sp�ter Konfidenzintervalle bestimmt werden k�nnen
	 */
	public StatisticsDataPerformanceIndicator(final String[] xmlNodeNames, final double upperBound, final int steps, final int correlationRange, final int batchSize) {
		this(xmlNodeNames,upperBound,steps,correlationRange,batchSize,false);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticsDataPerformanceIndicator</code>
	 * Bei der Datenaufzeichnung wird eine H�ufigkeitsverteilung der Werte angelegt
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param upperBound	Gibt die Obergrenze des Tr�gers der H�ufigkeitsverteilung an
	 * @param steps	Gibt an, wie viele einzelne Werte f�r die H�ufigkeitsverteilung vorgehalten werden sollen
	 * @param correlationRange	Reichweite f�r die Erfassung der Autokorrelation (Werte &le;0 schalten die Erfassung aus)
	 * @param batchSize	Wird hier ein Wert &gt;1 �bergeben, so werden Batch-Means erfasst, auf deren Basis sp�ter Konfidenzintervalle bestimmt werden k�nnen
	 * @param isEmpty	Gibt an, ob es sich bei diesem Objekt um eine leere Kopiervorlage handelt
	 */
	public StatisticsDataPerformanceIndicator(final String[] xmlNodeNames, final double upperBound, final int steps, final int correlationRange, final int batchSize, final boolean isEmpty) {
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

		if (correlationRange>0) correlationTempValues=new double[correlationRange+CORRELATION_RANGE_STEPPING];

		this.batchSize=batchSize;

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
	 * F�gt einen Wert zu der Messreihe hinzu.
	 * @param value	Hinzuzuf�gender Wert
	 */
	public void add(final double value) {
		/* Anzahl */
		last=value;
		count++;

		if (value>0.0d) {
			/* Summe, quadrierte Summe */
			sum+=value;
			squaredSum+=(value*value);

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

			/* Verteilung der Werte */
			if (hasDistribution) {
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
			}
		} else {
			/* Summe (entf�llt), quadrierte Summe (entf�llt), Minimum, Maximum, Verteilung der Werte */
			min=0;
			if (count==1) max=0;
			if (dist==null) distributionZeroCount++; else densityData[0]++;
		}

		/* Autokorrelation */
		if (correlationTempValues!=null) {
			final int correlationRange=correlationTempValues.length;
			correlationTempValues[(int)((count-1)%correlationRange)]=value;
			int m=correlationRange;
			if (count<m) m=(int)count;
			m=m/CORRELATION_RANGE_STEPPING;
			if (value!=0.0) for (int k=1;k<m;k++) {
				final int index=(int)((count-1+correlationRange-k*CORRELATION_RANGE_STEPPING)%correlationRange);
				final double valueMinusK=correlationTempValues[index];
				if (valueMinusK!=0.0) correlationSums[k]+=value*valueMinusK; /* sum(i=k+1..n)x(i)*x(i-k) wird partiell aufgebaut */
			}
		}

		/* Batch-Means */
		if (batchSize>1) {
			batchTempSum+=value;
			batchTempCount++;
			if (batchTempCount==batchSize) {
				final double b=batchTempSum/batchSize;
				batchMeansCount++;
				batchMeansSum+=b;
				batchMeansSum2+=(b*b);

				batchTempSum=0;
				batchTempCount=0;
			}
		}
	}

	/**
	 * F�gt mehrere gleiche Werte zu der Messreihe hinzu.
	 * @param value	Hinzuzuf�gender Wert
	 * @param count	H�ufigkeit mit der der Wert hinzugef�gt werden soll
	 */
	public void add(final double value, final long count) {
		if (count<1) return;

		if (correlationTempValues!=null || batchSize>1) {
			/* Werte einzeln hinzuf�gen */
			for (long l=1;l<=count;l++) add(value);
			return;
		}

		/* Anzahl */
		last=value;
		this.count+=count;

		if (value<=0.0d) {
			/* Summe (entf�llt), quadrierte Summe (entf�llt), Minimum, Maximum, Verteilung der Werte */
			min=0;
			if (this.count==1) max=0;
			if (dist==null) distributionZeroCount++; else densityData[0]++;
		} else {
			/* Summe, quadrierte Summe */
			sum+=value*count;
			squaredSum+=(value*value)*count;

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

			/* Verteilung der Werte */
			if (hasDistribution) {
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
	 * F�gt zu der Teil-Messreihe eine weitere Teilmessreihe hinzu
	 * @param moreStatistics	Statistikobjekt, dessen Daten zu diesem hinzugef�gt werden sollen
	 */
	@Override
	public void add(final StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsDataPerformanceIndicator)) return;
		StatisticsDataPerformanceIndicator moreDataStatistics=(StatisticsDataPerformanceIndicator)moreStatistics;

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

		/* Autokorrelation */
		if (moreDataStatistics.correlationSums!=null) {
			if (correlationSums==null || count==moreDataStatistics.count) {
				correlationSums=Arrays.copyOf(moreDataStatistics.correlationSums,moreDataStatistics.correlationSums.length);
			} else {
				for (int i=0;i<Math.min(correlationSums.length,moreDataStatistics.correlationSums.length);i++) correlationSums[i]+=moreDataStatistics.correlationSums[i];
			}
		}
		if (moreDataStatistics.correlation!=null) {
			correlation=Arrays.copyOf(moreDataStatistics.correlation,moreDataStatistics.correlation.length);
		}

		/* Batch-Means */
		if (moreDataStatistics.batchSize>1) {
			batchMeansCount+=moreDataStatistics.batchMeansCount;
			batchMeansSum+=moreDataStatistics.batchMeansSum;
			batchMeansSum2+=moreDataStatistics.batchMeansSum2;
			batchSize=moreDataStatistics.batchSize;
		}
		if (moreDataStatistics.batchMeansVar>0) batchMeansVar=moreDataStatistics.batchMeansVar;
	}

	/**
	 * Setzt alle Teil-Kenngr��en auf 0 zur�ck.
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

		/* Autokorrelation */
		if (correlationTempValues!=null) {
			Arrays.fill(correlationTempValues,0);
			if (correlationSums==null) correlationSums=new double[correlationTempValues.length/CORRELATION_RANGE_STEPPING]; else Arrays.fill(correlationSums,0);
		}
		correlation=null;

		/* Batch-Means */
		if (batchSize>1) {
			batchTempCount=0;
			batchTempSum=0;
			batchMeansCount=0;
			batchMeansSum=0;
			batchMeansSum2=0;
		}
		batchMeansVar=0;
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsDataPerformanceIndicator)) return;
		final StatisticsDataPerformanceIndicator data=(StatisticsDataPerformanceIndicator)indicator;

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

		/* Autokorrelation */
		if (data.correlationTempValues!=null) correlationTempValues=Arrays.copyOf(data.correlationTempValues,data.correlationTempValues.length);
		if (data.correlationSums!=null) correlationSums=Arrays.copyOf(data.correlationSums,data.correlationSums.length);
		if (data.correlation!=null) correlation=Arrays.copyOf(data.correlation,data.correlation.length);

		/* Batch-Means */
		batchSize=data.batchSize;
		batchMeansCount=data.batchMeansCount;
		batchMeansSum=data.batchMeansSum;
		batchMeansSum2=data.batchMeansSum2;
		batchMeansVar=data.batchMeansVar;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsDataPerformanceIndicator clone() {
		final StatisticsDataPerformanceIndicator indicator=new StatisticsDataPerformanceIndicator(xmlNodeNames,upperBound,steps,(correlationTempValues==null)?-1:correlationTempValues.length,batchSize);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, �bernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsDataPerformanceIndicator cloneEmpty() {
		return new StatisticsDataPerformanceIndicator(xmlNodeNames,upperBound,steps,(correlationTempValues==null)?-1:correlationTempValues.length,batchSize);
	}

	/**
	 * Liefert die Anzahl der Messwerte, aus der die Messreihe besteht
	 * @return	Anzahl der erfassten Messwerte in der Messreihe
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Liefert den als letztes hinzugef�gten Wert
	 * @return	Als letztes hinzugef�gter Wert
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
	 * Berechnet aus den Messreihen-Kenngr��en den Mittelwert
	 * @return	Mittelwert der Messreihe
	 */
	public double getMean() {
		if (count==0) return 0;
		return sum/count;
	}

	/**
	 * Berechnet ein Quantil der Messreihe aus der H�ufigkeitsverteilung.
	 * @param sum	Summe �ber die Messreihe
	 * @param p	Wert f�r das Quantil
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
	 * Berechnet ein Quantil der Messreihe aus der H�ufigkeitsverteilung.<br>
	 * Die Quantile stehen nur zur Verf�gung, wenn eine H�ufigkeitsverteilung erfasst wurde,
	 * also {@link #getDistribution()} einen Wert ungleich <code>null</code> besitzt.
	 * @param p	Wert f�r das Quantil
	 * @return	Quantil der Messreihe
	 * @see #getDistribution()
	 */
	public double getQuantil(final double p) {
		if (dist==null) return 0.0;

		return getQuantil(dist.getSum(),p);
	}

	/**
	 * Berechnet mehrere Quantile der Messreihe aus der H�ufigkeitsverteilung.<br>
	 * Die Quantile stehen nur zur Verf�gung, wenn eine H�ufigkeitsverteilung erfasst wurde,
	 * also {@link #getDistribution()} einen Wert ungleich <code>null</code> besitzt.
	 * @param p	Werte f�r die Quantile
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
	 * Berechnet den Median der Messreihe aus der H�ufigkeitsverteilung.<br>
	 * Der Median steht nur zur Verf�gung, wenn eine H�ufigkeitsverteilung erfasst wurde,
	 * also {@link #getDistribution()} einen Wert ungleich <code>null</code> besitzt.
	 * @return	Median der Messreihe
	 * @see #getDistribution()
	 */
	public double getMedian() {
		return getQuantil(0.5);
	}

	/**
	 * Berechnet aus den Messreihen-Kenngr��en die Standardabweichung
	 * @return	Standardabweichung der Messreihe
	 */
	public double getSD() {
		if (count<2) return 0;
		final double v=squaredSum/(count-1)-(sum*sum)/count/(count-1);
		return StrictMath.sqrt(Math.max(0,v));
	}

	/**
	 * Berechnet aus den Messreihen-Kenngr��en die Varianz
	 * @return	Standardabweichung der Messreihe
	 */
	public double getVar() {
		if (count<2) return 0;
		final double v=squaredSum/(count-1)-(sum*sum)/count/(count-1);
		return Math.max(0,v);
	}

	/**
	 * Berechnet aus den Messreihen-Kenngr��en den Variationskoeffizient
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
	 * Liefert die H�ufigkeitsverteilung der Messreihe (oder <code>null</code>, wenn die Erfassung der Verteilung deaktiviert ist)
	 * @return	H�ufigkeitsverteilung der Messwerte in der Messreihe
	 */
	public DataDistributionImpl getDistribution() {
		if (hasDistribution && dist==null) initDistribution();
		return dist;
	}

	/**
	 * Liefert die normalisierte H�ufigkeitsverteilung der Messreihe (oder <code>null</code>, wenn die Erfassung der Verteilung deaktiviert ist)
	 * (nur Dichtewerte, keine Verteilung; diese kann aber per {@link DataDistributionImpl#updateCumulativeDensity()} berechnet werden)
	 * @return	Normalisierte H�ufigkeitsverteilung der Messwerte in der Messreihe
	 */
	public DataDistributionImpl getNormalizedDistribution() {
		if (hasDistribution && dist==null) initDistribution();
		if (dist==null) return null;
		DataDistributionImpl normalized=dist.clone();
		normalized.normalizeDensityOnly();
		return normalized;
	}

	/**
	 * Gibt an, ob Daten zur Autokorrelation vorhanden sind
	 * @return	Gibt <code>true</code> zur�ck, wenn Autocorrelationdaten vorhanden sind
	 * @see StatisticsDataPerformanceIndicator#getCorrelationLevelDistance(double)
	 * @see StatisticsDataPerformanceIndicator#getCorrelationData()
	 */
	public boolean isCorrelationAvailable() {
		return (correlationSums!=null || correlation!=null);
	}

	/**
	 * Berechnet intern die Autokorrelationsdaten.
	 * @see #getCorrelationLevelDistance(double)
	 * @see #getCorrelationData()
	 */
	private void calcCorrelation() {
		if (correlationSums==null) return;

		/*
		 * g(k) = 1/n * sum(i=k+1..n) (x(i)-xMean)*(x(i-k)*xMean)
		 * g(k) \approx 1/n * (sum(i=k+1..n)x(i)*x(i-k) - (n-k)*xMean^2)
		 * rho(k)=g(k)/xVar
		 */

		final double mean=getMean();
		final double var=getVar();

		correlation=new double[correlationSums.length];
		correlation[0]=1;

		for (int k=1;k<correlationSums.length;k++) {
			if (k*CORRELATION_RANGE_STEPPING>count) {
				correlation[k]=0;
			} else {
				final double corr=(correlationSums[k]-(count-k*CORRELATION_RANGE_STEPPING)*mean*mean);
				if (count>0 && var>0) correlation[k]=corr/count/var;
			}
		}

		/* Speicher aufr�umen */
		correlationSums=null;
		correlationTempValues=null;
	}

	/**
	 * Berechnet den Abstand an Messwerten, f�r die gilt, dass die Autokorrelation zwischen diesen kleiner als das angegebene Level ist
	 * @param level	Autokorrelationslevel, welches von denen Elementen eines gewissen Abstands unterschritten werden soll
	 * @return	Abstand an Werten, die das angegebene Autokorrelationslevel unterschreiten
	 * @see StatisticsDataPerformanceIndicator#isCorrelationAvailable()
	 */
	public int getCorrelationLevelDistance(final double level) {
		calcCorrelation();
		if (correlation==null) return 1;

		for (int k=1;k<correlation.length;k++) if (Math.abs(correlation[k])<level) return k*CORRELATION_RANGE_STEPPING;
		return correlation.length*CORRELATION_RANGE_STEPPING;
	}

	/**
	 * Liefert eine Liste mit allen Autokorrelationskoeffizienten.<br>
	 * Der erste Eintrag steht dabei f�r {@link StatisticsDataPerformanceIndicator#CORRELATION_RANGE_STEPPING}
	 * Schritte Entfernung, der zweite f�r das doppelte davon usw.
	 * @return	Autokorrelationskoeffizienten
	 * @see StatisticsDataPerformanceIndicator#isCorrelationAvailable()
	 */
	public double[] getCorrelationData() {
		calcCorrelation();
		if (correlation==null) return null;
		return Arrays.copyOf(correlation,correlation.length);
	}

	/**
	 * Halbe Breite des Konfidenzintervalls f�r den Mittelwert<br>
	 * Das Konfidenzintervall geht dann von <code>getMean()-getConfidenceHalfWide(alpha)</code> bis <code>getMean()+getConfidenceHalfWide(alpha)</code>
	 * @param alpha	Konfidenzniveau (z.B. alpha=0.05 oder alpha=0.01)
	 * @return	Halbe Breite des Konfidenzintervalls
	 */
	public double getConfidenceHalfWide(final double alpha) {
		if (count<2) return 0; /* count==1 f�hrt zu TDistribution(0), was nicht geht. */
		if (min==max) return 0;
		final TDistribution dist=new TDistribution(count-1);
		final double t=dist.inverseCumulativeProbability(1-alpha/2);
		final double sd=getSD();
		return t*sd/StrictMath.sqrt(count);
	}

	/**
	 * Halbe Breite des Konfidenzintervalls f�r den Mittelwert (mehrere Konfidenzniveaus gleichzeitig)<br>
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
	 * Liefert die Batchgr��e f�r die Erfassung von Batch-Means zur�ck.<br>
	 * Ein Wert &lt;2 bedeutet, dass keine Erfassung von Batches stattgefunden hat.
	 * @return	Batchgr��e f�r die Erfassung von Batch-Means
	 */
	public int getBatchSize() {
		return Math.max(1,batchSize);
	}

	/**
	 * Liefert die Anzahl der erfassten Batche<br>
	 * (Setzt voraus, dass das System Bache aufgezeichnet hat.)
	 * @return	Anzahl der erfassten Batche
	 */
	public int getBatchCount() {
		return batchMeansCount;
	}

	/**
	 * Liefert die Varianz zwischen den Batches<br>
	 * (Setzt voraus, dass das System Bache aufgezeichnet hat.)
	 * @return	Varianz zwischen den Batches
	 */
	public double getBatchVar() {
		if (batchMeansVar==0 && batchSize>1) {
			final double xMean=getMean();
			final int b=batchMeansCount;
			batchMeansVar=1.0/b/(b-1)*(batchMeansSum2-2*xMean*batchMeansSum+b*xMean*xMean);
		}

		return batchMeansVar;
	}

	/**
	 * Liefert die Standardabweichung zwischen den Batches<br>
	 * (Setzt voraus, dass das System Bache aufgezeichnet hat.)
	 * @return	Standardabweichung zwischen den Batches
	 */
	public double getBatchSD() {
		return StrictMath.sqrt(getBatchVar());
	}

	/**
	 * Liefert die Standardabweichung zwischen den Batches<br>
	 * Die internen Z�hler werden dabei nicht finalisiert, so dass weitere Messwerte hinzugef�gt werden k�nnen.<br>
	 * (Setzt voraus, dass das System Bache aufgezeichnet hat.)
	 * @return	Standardabweichung zwischen den Batches
	 */
	public double getBatchSDWithoutFinalize() {
		if (batchSize<2 || batchMeansVar>0) return getBatchSD();

		final double xMean=getMean();
		final int b=batchMeansCount;
		return StrictMath.sqrt(1.0/b/(b-1)*(batchMeansSum2-2*xMean*batchMeansSum+b*xMean*xMean));
	}

	/**
	 * Halbe Breite des Konfidenzintervalls f�r den Mittelwert (unter Ber�cksichtigung der Batche)<br>
	 * Das Konfidenzintervall geht dann von <code>getMean()-getBatchMeanConfidenceHalfWide(alpha)</code> bis <code>getMean()+getBatchMeanConfidenceHalfWide(alpha)</code>
	 * @param alpha	Konfidenzniveau (z.B. alpha=0.05 oder alpha=0.01)
	 * @return	Halbe Breite des Konfidenzintervalls
	 */
	public double getBatchMeanConfidenceHalfWide(final double alpha) {
		if (min==max) return 0;
		final int b=batchMeansCount;
		if (b==0) return 0; /* Kein Batching erfolgt */
		if (b==1) return 0; /* Sorry, aber TDistribution(0) geht auch nicht. */
		final TDistribution dist=new TDistribution(b-1);
		final double t=dist.inverseCumulativeProbability(1-alpha/2);
		final double sd=getBatchSD();
		return t*sd; /* Division durch sqrt(b) steckt schon in getBatchSD() */
	}

	/**
	 * Halbe Breite des Konfidenzintervalls f�r den Mittelwert (unter Ber�cksichtigung der Batche) (mehrere Konfidenzniveaus gleichzeitig)<br>
	 * Das Konfidenzintervall geht dann von <code>getMean()-getBatchMeanConfidenceHalfWide(alpha)</code> bis <code>getMean()+getBatchMeanConfidenceHalfWide(alpha)</code>
	 * @param alpha	Konfidenzniveaus (z.B. alpha=0.05 oder alpha=0.01)
	 * @return	Halbe Breite der Konfidenzintervalle
	 */
	public double[] getBatchMeanConfidenceHalfWide(final double[] alpha) {
		if (alpha==null || alpha.length==0) return new double[0];
		if (count==0) return new double[alpha.length];
		if (min==max) return new double[alpha.length];

		final int b=batchMeansCount;
		if (b==0) return new double[alpha.length]; /* Kein Batching erfolgt */
		if (b==1) return new double[alpha.length]; /* Sorry, aber TDistribution(0) geht auch nicht. */

		final TDistribution dist=new TDistribution(b-1);
		final double sd=getBatchSD();

		final double[] results=new double[alpha.length];
		for (int i=0;i<alpha.length;i++) {
			final double t=dist.inverseCumulativeProbability(1-alpha[i]/2);
			results[i]=t*sd; /* Division durch sqrt(b) steckt schon in getBatchSD() */
		}
		return results;
	}

	/**
	 * Wenn dieser Wert noch mit {@link #batchMeansCount} �bereinstimmt, kann {@link #lastBatchT} verwendet werden.
	 * @see #getBatchMeanConfidenceHalfWideWithoutFinalize(double)
	 */
	private int lastBatchCount;

	/**
	 * Wenn dieser Wert noch mit dem Parameter <code>alpha</code> �bereinstimmt, kann {@link #lastBatchT} verwendet werden.
	 * @see #getBatchMeanConfidenceHalfWideWithoutFinalize(double)
	 */
	private int lastBatchAlpha;
	/**
	 * Zwischengespeicherter Wert t f�r die Batch-Means-Konfidenzintervall-Berechnung
	 *  @see #getBatchMeanConfidenceHalfWideWithoutFinalize(double)
	 */
	private double lastBatchT;

	/**
	 * Halbe Breite des Konfidenzintervalls f�r den Mittelwert (unter Ber�cksichtigung der Batche)<br>
	 * Die internen Z�hler werden dabei nicht finalisiert, so dass weitere Messwerte hinzugef�gt werden k�nnen.<br>
	 * Das Konfidenzintervall geht dann von <code>getMean()-getBatchMeanConfidenceHalfWide(alpha)</code> bis <code>getMean()+getBatchMeanConfidenceHalfWide(alpha)</code>
	 * @param alpha	Konfidenzniveau (z.B. alpha=0.05 oder alpha=0.01)
	 * @return	Halbe Breite des Konfidenzintervalls
	 */
	public double getBatchMeanConfidenceHalfWideWithoutFinalize(final double alpha) {
		if (min==max) return 0;
		final int b=batchMeansCount;
		if (b==0) return 0; /* Kein Batching erfolgt */
		if (b==1) return 0; /* Sorry, aber TDistribution(0) geht auch nicht. */

		final double t;
		if (lastBatchCount==b && lastBatchAlpha==alpha && lastBatchT>0) {
			t=lastBatchT;
		} else {
			final TDistribution dist=new TDistribution(b-1);
			t=lastBatchT=dist.inverseCumulativeProbability(1-alpha/2);
		}

		final double sd=getBatchSDWithoutFinalize();
		return t*sd; /* Division durch sqrt(b) steckt schon in getBatchSD() */
	}

	/**
	 * Berechnet aus den Messreihen-Kenngr��en die Varianz
	 * unter Ber�cksichtigung der Batches.
	 * @return	Standardabweichung der Messreihe
	 */
	public double getVarByBatch() {
		if (batchMeansCount==0) return getVar();
		return getVar()*(count-1)/count+getBatchVar();
	}

	/**
	 * Berechnet aus den Messreihen-Kenngr��en die Standardabweichung
	 * unter Ber�cksichtigung der Batches.
	 * @return	Standardabweichung der Messreihe
	 */
	public double getSDByBatch() {
		if (batchMeansCount==0) return getSD();
		return Math.sqrt(getVar()*(count-1)/count+getBatchVar());
	}

	/**
	 * Autokorrelations-Levels zum Speichern in der xml-Datei
	 * @see #addToXMLIntern(Element, StringBuilder)
	 */
	private static final double[] AUTOCORRELATION_SAVE_LEVEL=new double[]{0.1,0.05,0.01,0.005,0.001};

	/**
	 * Konfidenzintervall-Levels zum Speichern in der xml-Datei
	 * @see #addToXMLIntern(Element, StringBuilder)
	 */
	private static final double[] CONFIDENCE_SAVE_LEVEL=new double[]{0.1,0.05,0.01};

	/**
	 * Speichert eine Kenngr��e, die intern aus Anzahl, Summe und Summe der quadrierten Werte besteht, in einem xml-Knoten.
	 * Es werden dabei zus�tzlich Mittelwert, Standardabweichung und Variationskoeffizient berechnet und gespeichert
	 * @param node	Neuer xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
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

		if (dist!=null) node.setAttribute(xmlNameDistribution[0],dist.storeToString(recycleStringBuilder));

		calcCorrelation();
		if (correlation!=null) {
			/* Aufbereitete Daten (werden nur geschrieben, nicht wieder gelesen) */
			final DataDistributionImpl temp=new DataDistributionImpl(correlation.length,correlation);
			node.setAttribute(xmlNameCorrelation[0],temp.storeToString());
			/* Levelwerte */
			for (double level: AUTOCORRELATION_SAVE_LEVEL) {
				String s=String.valueOf(Math.round(level*1000));
				while (s.length()<4) s="0"+s;
				node.setAttribute(xmlNameCorrelation[0]+s,""+getCorrelationLevelDistance(level));
			}
		}

		if (batchMeansCount>0) {
			node.setAttribute(xmlNameBatchSize[0],""+batchSize);
			node.setAttribute(xmlNameBatchCount[0],""+batchMeansCount);
			node.setAttribute(xmlNameBatchMeansVar[0],NumberTools.formatSystemNumber(getBatchVar()));
			for (double level: CONFIDENCE_SAVE_LEVEL) {
				String s=String.valueOf(Math.round((1-level)*100));
				double radius=NumberTools.reduceDigits(getBatchMeanConfidenceHalfWide(level),8);
				node.setAttribute(xmlNameMeanBatchHalfWide[0]+s,NumberTools.formatSystemNumber(radius));
			}
		}

		if (hasDistribution) {
			if (dist==null) initDistribution();
			final double[] quantils=getQuantil(storeQuantilValues);
			for (int i=0;i<storeQuantilValues.length;i++) {
				node.setAttribute(xmlNameQuantil+Math.round(storeQuantilValues[i]*100),NumberTools.formatSystemNumber(quantils[i]));
			}
		}
	}

	/**
	 * Versucht eine Kenngr��e, die intern durch die Anzahl an Messwerten, deren Summe und deren quadrierte Summe repr�sentiert wird, aus einem xml-Knoten zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> zur�ck, sonst eine Fehlermeldung
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

		value=getAttributeValue(node,xmlNameCorrelation);
		if (!value.isEmpty() && hasDistribution) {
			if (dist==null) initDistribution();
			final DataDistributionImpl temp=DataDistributionImpl.createFromString(value,dist.upperBound);
			if (temp==null) return String.format(xmlNameCorrelationError,node.getNodeName());
			correlation=temp.densityData;
		}

		value=getAttributeValue(node,xmlNameBatchSize);
		if (!value.isEmpty()) {
			Long L=NumberTools.getPositiveLong(value);
			if (L==null) return String.format(xmlNameBatchSizeError,node.getNodeName(),value);
			batchSize=L.intValue();
		}

		value=getAttributeValue(node,xmlNameBatchCount);
		if (!value.isEmpty()) {
			Long L=NumberTools.getPositiveLong(value);
			if (L==null) return String.format(xmlNameBatchCountError,node.getNodeName(),value);
			batchMeansCount=L.intValue();
		}

		value=getAttributeValue(node,xmlNameBatchMeansVar);
		if (!value.isEmpty()) {
			Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(value));
			if (D==null) return String.format(xmlNameBatchMeansVarError,node.getNodeName(),value);
			batchMeansVar=D.doubleValue();
		}

		return null;
	}

	/**
	 * Berechnet die Standardabweichung aus mehreren Teilindikatoren, die dabei jeweils als einzelne Batche aufgefasst werden.
	 * @param partial	Teil-Kenngr��en, die als Batche aufgefasst werden sollen
	 * @param all	Zusammenfassung der Teil-Kenngr��en
	 * @return	Batch-Standardabweichung
	 * @see #getConfidenceHalfWideByMultiStatistics(StatisticsDataPerformanceIndicator[], StatisticsDataPerformanceIndicator, double[])
	 */
	private static double getBatchByMultiStatisticsSD(final StatisticsDataPerformanceIndicator[] partial, final StatisticsDataPerformanceIndicator all) {
		final double xMean=all.getMean();
		double s=0;
		for (int i=0;i<partial.length;i++) {
			final double partialMean=partial[i].getMean();
			s+=(partialMean-xMean)*(partialMean-xMean);
		}
		final int b=partial.length;
		return s/b/(b-1);
	}

	/**
	 * Halbe Breite des Konfidenzintervalls f�r den Mittelwert (jeder Teilindikator ist ein Batch) (mehrere Konfidenzniveaus gleichzeitig)<br>
	 * @param partial	Teil-Kenngr��en, die als Batche aufgefasst werden sollen
	 * @param all	Zusammenfassung der Teil-Kenngr��en
	 * @param alpha	Konfidenzniveaus (z.B. alpha=0.05 oder alpha=0.01)
	 * @return	Halbe Breite der Konfidenzintervalle
	 */
	public static double[] getConfidenceHalfWideByMultiStatistics(final StatisticsDataPerformanceIndicator[] partial, final StatisticsDataPerformanceIndicator all, double[] alpha) {
		if (alpha==null || alpha.length==0) return new double[0];
		if (partial==null || partial.length<2 || all==null) return new double[alpha.length];

		final int b=partial.length;

		final TDistribution dist=new TDistribution(b-1);
		final double sd=Math.sqrt(getBatchByMultiStatisticsSD(partial,all));

		final double[] results=new double[alpha.length];
		for (int i=0;i<alpha.length;i++) {
			final double t=dist.inverseCumulativeProbability(1-alpha[i]/2);
			results[i]=t*sd; /* Division durch sqrt(b) steckt schon in getBatchByMultiStatisticsSD() */
		}
		return results;
	}
}