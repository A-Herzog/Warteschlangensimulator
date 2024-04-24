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
 * Die Zählung wird über die Funktion {@link StatisticsDataPerformanceIndicator#add(double)} realisiert.
 * @author Alexander Herzog
 * @version 2.5
 */
public final class StatisticsDataPerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/** XML-Attribut für "Anzahl" */
	public static String[] xmlNameCount=new String[]{"Anzahl"};
	/** Fehlermeldung, wenn das "Anzahl"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameCountError="Das Anzahl-Attribut im \"%s\"-Element muss eine nicht-negative Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Summe" */
	public static String[] xmlNameSum=new String[]{"Summe"};
	/** Fehlermeldung, wenn das "Summe"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameSumError="Das Summe-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Summe2" (=Summe der quadrierten Werte) */
	public static String[] xmlNameSumSquared=new String[]{"Summe2"};
	/** Fehlermeldung, wenn das "Summe2"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameSumSquaredError="Das Summe2-Attribut im \"%s\"-Element muss eine nicht-negative Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Summe3" (=Summe der kubischen Werte) */
	public static String[] xmlNameSumCubic=new String[]{"Summe3"};
	/** Fehlermeldung, wenn das "Summe3"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameSumCubicError="Das Summe3-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Summe4" (=Summe der mit 4 potenzierten Werte) */
	public static String[] xmlNameSumQuartic=new String[]{"Summe4"};
	/** Fehlermeldung, wenn das "Summe4"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameSumQuarticError="Das Summe4-Attribut im \"%s\"-Element muss eine Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "Mittelwert" */
	public static String[] xmlNameMean=new String[]{"Mittelwert"};
	/** XML-Attribut für "Standardabweichung" */
	public static String[] xmlNameSD=new String[]{"Standardabweichung"};
	/** XML-Attribut für "Variationskoeffizient" */
	public static String[] xmlNameCV=new String[]{"Variationskoeffizient"};
	/** XML-Attribut für "Schiefe" */
	public static String[] xmlNameSk=new String[]{"Schiefe"};
	/** XML-Attribut für "Kurt" */
	public static String[] xmlNameKurt=new String[]{"Kurt"};
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
	/** XML-Attribut für "Autokorrelation" */
	public static String[] xmlNameCorrelation=new String[]{"Autokorrelation"};
	/** Fehlermeldung, wenn das "Autokorrelation"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameCorrelationError="Das Autokorrelation-Attribut im \"%S\"-Element muss die Autokorrelationswerte beinhalten.";
	/** XML-Attribut für "BatchGroesse" */
	public static String[] xmlNameBatchSize=new String[]{"BatchGroesse"};
	/** Fehlermeldung, wenn das "Verteilung"-BatchGroesse nicht gelesen werden konnte. */
	public static String xmlNameBatchSizeError="Das BatchGroesse-Attribut im \"%s\"-Element muss eine positive Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut für "BatchAnzahl" */
	public static String[] xmlNameBatchCount=new String[]{"BatchAnzahl"};
	/** Fehlermeldung, wenn das "BatchAnzahl"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameBatchCountError="Das BatchAnzahl-Attribut im \"%s\"-Element muss eine positive Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut für "BatchVarianz" */
	public static String[] xmlNameBatchMeansVar=new String[]{"BatchVarianz"};
	/** Fehlermeldung, wenn das "BatchVarianz"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameBatchMeansVarError="Das BatchVarianz-Attribut im \"%s\"-Element muss eine nicht-negative Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "MittelwertKonfidenzRadius" */
	public static String[] xmlNameMeanBatchHalfWide=new String[]{"MittelwertKonfidenzRadius"};
	/** XML-Attribut für "LaufAnzahl" */
	public static String[] xmlNameRunCount=new String[]{"LaufAnzahl"};
	/** Fehlermeldung, wenn das "LaufAnzahl"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameRunCountError="Das LaufAnzahl-Attribut im \"%s\"-Element muss eine positive Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut für "LaufVarianz" */
	public static String[] xmlNameRunVar=new String[]{"LaufVarianz"};
	/** Fehlermeldung, wenn das "LaufVarianz"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameRunVarError="Das LaufVarianz-Attribut im \"%s\"-Element muss eine nicht-negative Zahl sein, ist aber \"%s\".";
	/** XML-Attribut für "LaufMittelwertKonfidenzRadius" */
	public static String[] xmlNameRunHalfWide=new String[]{"LaufMittelwertKonfidenzRadius"};
	/** XML-Attribut für "Quantil" */
	public static String xmlNameQuantil="Quantil";
	/** XML-Attribut für "QuantilGrenze" */
	public static String[] xmlNameQuantilLimit=new String[]{"QuantilGrenze"};
	/** XML-Attribut für "WelfordM2" */
	public static String[] xmlNameWelfordM2=new String[]{"WelfordM2"};
	/** Fehlermeldung, wenn das "WelfordM2"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameWelfordM2Error="Das WelfordM2-Attribut im \"%s\"-Element muss eine nicht-negative Zahl sein, ist aber \"%s\".";

	/**
	 * Quantile, die aus der Häufigkeitsverteilung berechnet und in der xml-Datei gespeichert werden
	 */
	public static final double[] storeQuantilValues=new double[] {0.10,0.25,0.5,0.75,0.9};

	/**
	 * Schrittweite für die Berechnung der Autokorrelation
	 */
	public static final int CORRELATION_RANGE_STEPPING=10;

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
	 * Summe der mit 3 potenzierten Messwerte
	 */
	private double cubicSum;

	/**
	 * Summe der mit 4 potenzierten Messwerte
	 */
	private double quarticSum;

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
	 * Partialsummen über x_i * x_(i-k) zur Bestimmung der Korrelation
	 * (nur während der Datenerfassung und beim Zusammenführen relevant)
	 */
	private double[] correlationSums;

	/**
	 * Temporäre Erfassung von Werten, um die Autokorrelation bestimmen zu können
	 * (nur während der Datenerfassung relevant)
	 */
	private double[] correlationTempValues;

	/**
	 * Korrelationswerte zur Schrittweite
	 * @see StatisticsDataPerformanceIndicator#CORRELATION_RANGE_STEPPING
	 */
	private double[] correlation;

	/**
	 * Skalierung der Array-Einträge von <code>dist</code>
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
	 * Batchgröße für Batch-Means-Methode
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
	 * Varianz zwischen den Batch-Mittelwerten<br>
	 * Wird von {@link #getBatchVar()} berechnet und dann hier gespeichert.
	 * @see #getBatchVar()
	 */
	private double batchMeansVar;

	/**
	 * Summe der Daten, die für den Mittelwert des nächsten Batches herangezogen werden sollen
	 */
	private double batchTempSum;

	/**
	 * Anzahl der bereits aufgezeichneten Daten, die für die Berechnung des Mittelwertes des nächsten Batches herangezogen werden sollen
	 */
	private int batchTempCount;

	/**
	 * Anzahl an erfassten Teil-Simulationsläufen
	 */
	private int runCount;

	/**
	 * Summe der Werte pro erfasstem Teil-Simulationslauf
	 */
	private double runSum;

	/**
	 * Summe der quadrierten Werte pro erfasstem Teil-Simulationslauf
	 */
	private double runSum2;

	/**
	 * Varianz zwischen den Teil-Simulationslauf Mittelwerten<br>
	 * Wird von {@link #getRunVar()} berechnet.
	 * @see #getRunVar()
	 */
	private double runVar;

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
	 * Soll der Welford-Algorithmus zur Erfassung der Varianz verwendet werden? (langsamer, aber bei ganz kleinen Variationskoeffizienten exakter)
	 */
	private final boolean useWelford;

	/**
	 * Fortlaufend erfasster Wert M2 im Welford-Algorithmus
	 * @see #useWelford
	 */
	private double welfordM2;

	/**
	 * Konstruktor der Klasse <code>StatisticsDataPerformanceIndicator</code>
	 * Bei der Datenaufzeichnung wird eine Häufigkeitsverteilung der Werte angelegt
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param upperBound	Gibt die Obergrenze des Trägers der Häufigkeitsverteilung an
	 * @param steps	Gibt an, wie viele einzelne Werte für die Häufigkeitsverteilung vorgehalten werden sollen
	 */
	public StatisticsDataPerformanceIndicator(final String[] xmlNodeNames, final double upperBound, final int steps) {
		this(xmlNodeNames,upperBound,steps,-1,1,false);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticsDataPerformanceIndicator</code>
	 * Bei der Datenaufzeichnung wird eine Häufigkeitsverteilung der Werte angelegt
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param upperBound	Gibt die Obergrenze des Trägers der Häufigkeitsverteilung an
	 * @param steps	Gibt an, wie viele einzelne Werte für die Häufigkeitsverteilung vorgehalten werden sollen
	 * @param correlationRange	Reichweite für die Erfassung der Autokorrelation (Werte &le;0 schalten die Erfassung aus)
	 * @param batchSize	Wird hier ein Wert &gt;1 übergeben, so werden Batch-Means erfasst, auf deren Basis später Konfidenzintervalle bestimmt werden können
	 * @param useWelford	Soll der Welford-Algorithmus zur Erfassung der Varianz verwendet werden? (langsamer, aber bei ganz kleinen Variationskoeffizienten exakter)
	 */
	public StatisticsDataPerformanceIndicator(final String[] xmlNodeNames, final double upperBound, final int steps, final int correlationRange, final int batchSize, final boolean useWelford) {
		this(xmlNodeNames,upperBound,steps,correlationRange,batchSize,useWelford,false);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticsDataPerformanceIndicator</code>
	 * Bei der Datenaufzeichnung wird eine Häufigkeitsverteilung der Werte angelegt
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param upperBound	Gibt die Obergrenze des Trägers der Häufigkeitsverteilung an
	 * @param steps	Gibt an, wie viele einzelne Werte für die Häufigkeitsverteilung vorgehalten werden sollen
	 * @param correlationRange	Reichweite für die Erfassung der Autokorrelation (Werte &le;0 schalten die Erfassung aus)
	 * @param batchSize	Wird hier ein Wert &gt;1 übergeben, so werden Batch-Means erfasst, auf deren Basis später Konfidenzintervalle bestimmt werden können
	 * @param useWelford	Soll der Welford-Algorithmus zur Erfassung der Varianz verwendet werden? (langsamer, aber bei ganz kleinen Variationskoeffizienten exakter)
	 * @param isEmpty	Gibt an, ob es sich bei diesem Objekt um eine leere Kopiervorlage handelt
	 */
	public StatisticsDataPerformanceIndicator(final String[] xmlNodeNames, final double upperBound, final int steps, final int correlationRange, final int batchSize, final boolean useWelford, final boolean isEmpty) {
		super(xmlNodeNames);
		this.upperBound=upperBound;
		this.steps=steps;

		if (steps>0 && !isEmpty) {
			hasDistribution=true;
			setupArgumentScaleFactor(steps,upperBound);
		} else {
			hasDistribution=false;
		}

		if (correlationRange>0) correlationTempValues=new double[correlationRange+CORRELATION_RANGE_STEPPING];

		this.batchSize=batchSize;

		this.useWelford=useWelford;

		reset();
	}

	/**
	 * Berechnet die Skalierung zwischen Werten und Histogrammindicies
	 * nach dem Erstellen des Objektes oder dem Laden von Daten.
	 * @param steps	Gibt an, wie viele einzelne Werte für die Häufigkeitsverteilung vorgehalten werden sollen
	 * @param upperBound	Gibt die Obergrenze des Trägers der Häufigkeitsverteilung an
	 */
	private void setupArgumentScaleFactor(final int steps, final double upperBound) {
		if (steps>0 && upperBound>0) {
			argumentScaleFactor=steps/((upperBound==86399)?86400:upperBound);
			argumentScaleFactorIsOne=(Math.abs(argumentScaleFactor-1)<1E-10);
		}
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
	 * Fügt einen Wert zu der Messreihe hinzu.
	 * @param value	Hinzuzufügender Wert
	 */
	public void add(final double value) {
		/* Anzahl */
		last=value;
		count++;

		if (value>0.0d) {
			/* Summe, quadrierte Summe */
			sum+=value;
			final double squared=value*value;
			squaredSum+=squared;
			cubicSum+=(squared*value);
			quarticSum+=(squared*squared);

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
					l=(long)(value+0.5d);
				} else {
					/* langsamer: l=FastMath.round(argumentScaleFactor*value); */
					l=(long)((argumentScaleFactor*value)+0.5d);
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
			/* Summe (entfällt), quadrierte Summe (entfällt), Minimum, Maximum, Verteilung der Werte */
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

		if (useWelford) {
			if (count>1) {
				/* https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance */
				if (!hasLastWelfordMean) lastWelfordMean=(sum-value)/(count-1);
				final double delta=value-lastWelfordMean;
				final double newMean=sum/count;
				final double delta2=value-newMean;
				welfordM2+=delta*delta2;
				hasLastWelfordMean=true;
				lastWelfordMean=newMean;
			} else {
				welfordM2=0;
			}
		}
	}

	/**
	 * Ist der Wert in {@link #lastWelfordMean} gültig.
	 */
	private boolean hasLastWelfordMean;

	/**
	 * Bisheriger Mittelwert beim letzten Aufruf von {@link #add(double)}
	 */
	private double lastWelfordMean;

	/**
	 * Fügt mehrere gleiche Werte zu der Messreihe hinzu.
	 * @param value	Hinzuzufügender Wert
	 * @param count	Häufigkeit mit der der Wert hinzugefügt werden soll
	 */
	public void add(final double value, final long count) {
		if (count<1) return;

		if (correlationTempValues!=null || batchSize>1 || useWelford) {
			/* Werte einzeln hinzufügen */
			for (long l=1;l<=count;l++) add(value);
			return;
		}

		/* Anzahl */
		last=value;
		this.count+=count;

		if (value>0.0d) {
			/* Summe, quadrierte Summe */
			sum+=value*count;
			final double squared=value*value;
			squaredSum+=squared*count;
			cubicSum+=(squared*value)*count;
			quarticSum+=(squared*squared)*count;

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
					l=(long)(value+0.5d);
				} else {
					/* langsamer: l=FastMath.round(argumentScaleFactor*value); */
					l=(long)((argumentScaleFactor*value)+0.5d);
				}
				if (l<=0) {
					if (dist==null) distributionZeroCount+=count; else densityData[0]+=count;
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
		} else {
			/* Summe (entfällt), quadrierte Summe (entfällt), Minimum, Maximum, Verteilung der Werte */
			min=0;
			if (this.count==1) max=0;
			if (dist==null) distributionZeroCount++; else densityData[0]++;
		}
	}

	/**
	 * Fügt zu der Teil-Messreihe eine weitere Teilmessreihe hinzu
	 * @param moreStatistics	Statistikobjekt, dessen Daten zu diesem hinzugefügt werden sollen
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
				max=Math.max(max,moreDataStatistics.max);
			}
		}

		final long countOld=count;
		final double oldMean=getMean();
		count+=moreDataStatistics.count;
		sum+=moreDataStatistics.sum;
		squaredSum+=moreDataStatistics.squaredSum;
		cubicSum+=moreDataStatistics.cubicSum;
		quarticSum+=moreDataStatistics.quarticSum;

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

		/* Daten zu einzelnen Teil-Simulationsläufen */
		runCount+=moreDataStatistics.runCount;
		runSum+=moreDataStatistics.runSum;
		runSum2+=moreDataStatistics.runSum2;
		if (moreDataStatistics.runVar>0) runVar=moreDataStatistics.runVar;

		/* Welford */
		if (useWelford && moreDataStatistics.useWelford) {
			/* https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance */
			final double delta=oldMean-moreDataStatistics.getMean();
			welfordM2=welfordM2+moreDataStatistics.welfordM2+delta*delta*countOld*moreDataStatistics.count/(countOld+moreDataStatistics.count);
		} else {
			welfordM2=-1;
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
		cubicSum=0;
		quarticSum=0;

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

		/* Daten zu einzelnen Teil-Simulationsläufen */
		runCount=0;
		runSum=0;
		runSum2=0;
		runVar=0;

		/* Welford */
		welfordM2=-1;
		hasLastWelfordMean=false;
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
		cubicSum=data.cubicSum;
		quarticSum=data.quarticSum;
		min=data.min;
		max=data.max;

		/* Verteilung der Werte */
		if (data.dist!=null) {
			dist=data.dist.clone();
			densityData=dist.densityData;
			densityDataLength=densityData.length;
		}
		argumentScaleFactor=data.argumentScaleFactor;
		argumentScaleFactorIsOne=data.argumentScaleFactorIsOne;
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

		/* Daten zu einzelnen Teil-Simulationsläufen */
		runCount=data.runCount;
		runSum=data.runSum;
		runSum2=data.runSum2;
		runVar=data.runVar;

		/* Welford */
		welfordM2=data.welfordM2;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsDataPerformanceIndicator clone() {
		final StatisticsDataPerformanceIndicator indicator=new StatisticsDataPerformanceIndicator(xmlNodeNames,upperBound,steps,(correlationTempValues==null)?-1:correlationTempValues.length,batchSize,useWelford);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsDataPerformanceIndicator cloneEmpty() {
		return new StatisticsDataPerformanceIndicator(xmlNodeNames,upperBound,steps,(correlationTempValues==null)?-1:correlationTempValues.length,batchSize,useWelford);
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
		/* In dist.density wird gezählt, welcher Wert wie häufig auftritt, daher ist dist.getSum()==count */
		/* return getQuantil(dist.getSum(),p); */
		return getQuantil(count,p);
	}

	/**
	 * Berechnet mehrere Quantile der Messreihe aus der Häufigkeitsverteilung.<br>
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
			/* In dist.density wird gezählt, welcher Wert wie häufig auftritt, daher ist dist.getSum()==count */
			/* final double sum=dist.getSum(); */
			final double sum=count;
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
		final double v;
		if (useWelford && welfordM2>=0) {
			/* https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance */
			v=welfordM2/(count-1);
		} else {
			v=squaredSum/(count-1)-(sum*sum)/count/(count-1);
		}
		return Math.sqrt(Math.max(0,v));
	}

	/**
	 * Berechnet aus den Messreihen-Kenngrößen die Varianz
	 * @return	Standardabweichung der Messreihe
	 */
	public double getVar() {
		if (count<2) return 0;
		final double v;
		if (useWelford && welfordM2>=0) {
			/* https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance */
			v=welfordM2/(count-1);
		} else {
			v=squaredSum/(count-1)-(sum*sum)/count/(count-1);
		}
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
	 * Berechnet aus den Messreihen-Kenngrößen die Schiefe.
	 * @return	Schiefe der Messreihe
	 */
	public double getSk() {
		final double n=count;
		final double mean=getMean();
		final double sd=getSD();
		if (count<3 || sd==0.0) return 0;

		/* siehe: https://de.wikipedia.org/wiki/Schiefe_(Statistik) */
		return n/(n-1)/(n-2)/Math.pow(sd,3)*(cubicSum-3*mean*squaredSum+2*n*Math.pow(mean,3));
	}

	/**
	 * Berechnet aus den Messreihen-Kenngrößen den Exzess (von der Wölbung abgeleitetes Maß).
	 * @return	Exzess der Messreihe
	 */
	public double getKurt() {
		final double n=count;
		final double mean=getMean();
		final double sd=getSD();
		if (count<4 || sd==0.0) return 0;

		/* siehe: https://en.wikipedia.org/wiki/Kurtosis */
		final double normDistComparision=3*Math.pow(n-1,2)/(n-2)/(n-3);
		return n*(n+1)/(n-1)/(n-2)/(n-3)/Math.pow(sd,4)*(quarticSum-4*mean*cubicSum+6*Math.pow(mean,2)*squaredSum-3*n*Math.pow(mean,4))-normDistComparision;
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
	 * Gibt an, ob Daten zur Autokorrelation vorhanden sind
	 * @return	Gibt <code>true</code> zurück, wenn Autocorrelationdaten vorhanden sind
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
		 * g(k) = 1/n * sum(i=k+1..n) (x(i)-xMean)*(x(i-k)-xMean)
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

		/* Speicher aufräumen */
		correlationSums=null;
		correlationTempValues=null;
	}

	/**
	 * Berechnet den Abstand an Messwerten, für die gilt, dass die Autokorrelation zwischen diesen kleiner als das angegebene Level ist
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
	 * Der erste Eintrag steht dabei für {@link StatisticsDataPerformanceIndicator#CORRELATION_RANGE_STEPPING}
	 * Schritte Entfernung, der zweite für das doppelte davon usw.
	 * @return	Autokorrelationskoeffizienten
	 * @see StatisticsDataPerformanceIndicator#isCorrelationAvailable()
	 */
	public double[] getCorrelationData() {
		calcCorrelation();
		if (correlation==null) return null;
		return Arrays.copyOf(correlation,correlation.length);
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
	 * Liefert die Batchgröße für die Erfassung von Batch-Means zurück.<br>
	 * Ein Wert &lt;2 bedeutet, dass keine Erfassung von Batches stattgefunden hat.
	 * @return	Batchgröße für die Erfassung von Batch-Means
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

		return Math.max(0,batchMeansVar); /* Um Rundungsprobleme zu vermeiden. */
	}

	/**
	 * Liefert die Standardabweichung zwischen den Batches.<br>
	 * (Setzt voraus, dass das System Bache aufgezeichnet hat.)
	 * @return	Standardabweichung zwischen den Batches
	 */
	public double getBatchSD() {
		return StrictMath.sqrt(getBatchVar());
	}

	/**
	 * Liefert die Standardabweichung zwischen den Batches.<br>
	 * Die internen Zähler werden dabei nicht finalisiert, so dass weitere Messwerte hinzugefügt werden können.<br>
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
	 * Beendet einen Simulationslauf für die Erfassung der
	 * Konfidenzdaten über mehrere Teil-Simulationsläufe hinweg.
	 */
	public void finishRun() {
		final double value=(count>0)?(sum/count):0;
		runCount++;
		runSum+=value;
		runSum2+=(value*value);
	}

	/**
	 * Liefert die Anzahl an erfassten Teil-Simulationsläufen.
	 * @return	Anzahl an erfassten Teil-Simulationsläufen
	 */
	public int getRunCount() {
		return runCount;
	}

	/**
	 * Liefert die Varianz zwischen den Teil-Simulationsläufen.<br>
	 * (Setzt voraus, dass das System Daten zu Teil-Simulationsläufen aufgezeichnet hat.)
	 * @return	Varianz zwischen den Teil-Simulationsläufen
	 */
	public double getRunVar() {
		if (runVar==0.0) {
			if (runCount<2) return 0;
			final double xMean=getMean();
			final int b=runCount;
			runVar=1.0/b/(b-1)*(runSum2-2*xMean*runSum+b*xMean*xMean);
		}
		return Math.max(0,runVar); /* Um Rundungsprobleme zu vermeiden. */
	}

	/**
	 * Liefert die Standardabweichung zwischen den Teil-Simulationsläufen.<br>
	 * (Setzt voraus, dass das System Daten zu Teil-Simulationsläufen aufgezeichnet hat.)
	 * @return	Standardabweichung zwischen den Teil-Simulationsläufen
	 */
	public double getRunSD() {
		return StrictMath.sqrt(getRunVar());
	}

	/**
	 * Halbe Breite des Konfidenzintervalls für den Mittelwert (unter Berücksichtigung der Teil-Simulationsläufe)<br>
	 * Das Konfidenzintervall geht dann von <code>getMean()-getRunConfidenceHalfWide(alpha)</code> bis <code>getMean()+getRunConfidenceHalfWide(alpha)</code>
	 * @param alpha	Konfidenzniveau (z.B. alpha=0.05 oder alpha=0.01)
	 * @return	Halbe Breite des Konfidenzintervalls
	 */
	public double getRunConfidenceHalfWide(final double alpha) {
		if (min==max) return 0;
		final int b=runCount;
		if (b==0) return 0; /* Keine Läufe erfasst */
		if (b==1) return 0; /* Sorry, aber TDistribution(0) geht auch nicht. */
		final TDistribution dist=new TDistribution(b-1);
		final double t=dist.inverseCumulativeProbability(1-alpha/2);
		final double sd=getRunSD();
		return t*sd; /* Division durch sqrt(b) steckt schon in getRunSD() */
	}

	/**
	 * Halbe Breite des Konfidenzintervalls für den Mittelwert (unter Berücksichtigung der Teil-Simulationsläufe) (mehrere Konfidenzniveaus gleichzeitig)<br>
	 * Das Konfidenzintervall geht dann von <code>getMean()-getRunConfidenceHalfWide(alpha)</code> bis <code>getMean()+getRunConfidenceHalfWide(alpha)</code>
	 * @param alpha	Konfidenzniveaus (z.B. alpha=0.05 oder alpha=0.01)
	 * @return	Halbe Breite der Konfidenzintervalle
	 */
	public double[] getRunConfidenceHalfWide(final double[] alpha) {
		if (alpha==null || alpha.length==0) return new double[0];
		if (count==0) return new double[alpha.length];
		if (min==max) return new double[alpha.length];

		final int b=runCount;
		if (b==0) return new double[alpha.length]; /* Keine Läufe erfasst */
		if (b==1) return new double[alpha.length]; /* Sorry, aber TDistribution(0) geht auch nicht. */

		final TDistribution dist=new TDistribution(b-1);
		final double sd=getRunSD();

		final double[] results=new double[alpha.length];
		for (int i=0;i<alpha.length;i++) {
			final double t=dist.inverseCumulativeProbability(1-alpha[i]/2);
			results[i]=t*sd; /* Division durch sqrt(b) steckt schon in getBatchSD() */
		}
		return results;
	}

	/**
	 * Halbe Breite des Konfidenzintervalls für den Mittelwert (unter Berücksichtigung der Batche)<br>
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
	 * Halbe Breite des Konfidenzintervalls für den Mittelwert (unter Berücksichtigung der Batche) (mehrere Konfidenzniveaus gleichzeitig)<br>
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
	 * Wenn dieser Wert noch mit {@link #batchMeansCount} übereinstimmt, kann {@link #lastBatchT} verwendet werden.
	 * @see #getBatchMeanConfidenceHalfWideWithoutFinalize(double)
	 */
	private int lastBatchCount;

	/**
	 * Wenn dieser Wert noch mit dem Parameter <code>alpha</code> übereinstimmt, kann {@link #lastBatchT} verwendet werden.
	 * @see #getBatchMeanConfidenceHalfWideWithoutFinalize(double)
	 */
	private int lastBatchAlpha;
	/**
	 * Zwischengespeicherter Wert t für die Batch-Means-Konfidenzintervall-Berechnung
	 *  @see #getBatchMeanConfidenceHalfWideWithoutFinalize(double)
	 */
	private double lastBatchT;

	/**
	 * Halbe Breite des Konfidenzintervalls für den Mittelwert (unter Berücksichtigung der Batche)<br>
	 * Die internen Zähler werden dabei nicht finalisiert, so dass weitere Messwerte hinzugefügt werden können.<br>
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
	 * Berechnet aus den Messreihen-Kenngrößen die Varianz
	 * unter Berücksichtigung der Batches.
	 * @return	Standardabweichung der Messreihe
	 */
	public double getVarByBatch() {
		if (batchMeansCount==0) return getVar();
		return getVar()*(count-1)/count+getBatchVar();
	}

	/**
	 * Berechnet aus den Messreihen-Kenngrößen die Standardabweichung
	 * unter Berücksichtigung der Batches.
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
	 * Speichert eine Kenngröße, die intern aus Anzahl, Summe und Summe der quadrierten Werte besteht, in einem xml-Knoten.
	 * Es werden dabei zusätzlich Mittelwert, Standardabweichung und Variationskoeffizient berechnet und gespeichert
	 * @param node	Neuer xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 */
	@Override
	public void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder) {
		node.setAttribute(xmlNameCount[0],""+count);
		node.setAttribute(xmlNameSum[0],NumberTools.formatSystemNumber(sum,recycleStringBuilder));
		node.setAttribute(xmlNameSumSquared[0],NumberTools.formatSystemNumber(squaredSum,recycleStringBuilder));
		node.setAttribute(xmlNameSumCubic[0],NumberTools.formatSystemNumber(cubicSum,recycleStringBuilder));
		node.setAttribute(xmlNameSumQuartic[0],NumberTools.formatSystemNumber(quarticSum,recycleStringBuilder));
		node.setAttribute(xmlNameMean[0],NumberTools.formatSystemNumber(getMean(),recycleStringBuilder));
		node.setAttribute(xmlNameSD[0],NumberTools.formatSystemNumber(getSD(),recycleStringBuilder));
		node.setAttribute(xmlNameCV[0],NumberTools.formatSystemNumber(getCV(),recycleStringBuilder));
		node.setAttribute(xmlNameSk[0],NumberTools.formatSystemNumber(getSk(),recycleStringBuilder));
		node.setAttribute(xmlNameKurt[0],NumberTools.formatSystemNumber(NumberTools.reduceDigits(getKurt(),8),recycleStringBuilder));
		node.setAttribute(xmlNameMin[0],NumberTools.formatSystemNumber(getMin(),recycleStringBuilder));
		node.setAttribute(xmlNameMax[0],NumberTools.formatSystemNumber(getMax(),recycleStringBuilder));

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
			node.setAttribute(xmlNameBatchMeansVar[0],NumberTools.formatSystemNumber(getBatchVar(),recycleStringBuilder));
			for (double level: CONFIDENCE_SAVE_LEVEL) {
				String s=String.valueOf(Math.round((1-level)*100));
				double radius=NumberTools.reduceDigits(getBatchMeanConfidenceHalfWide(level),8);
				node.setAttribute(xmlNameMeanBatchHalfWide[0]+s,NumberTools.formatSystemNumber(radius,recycleStringBuilder));
			}
		}

		if (runCount>1) {
			node.setAttribute(xmlNameRunCount[0],""+runCount);
			node.setAttribute(xmlNameRunVar[0],NumberTools.formatSystemNumber(getRunVar(),recycleStringBuilder));
			for (double level: CONFIDENCE_SAVE_LEVEL) {
				String s=String.valueOf(Math.round((1-level)*100));
				double radius=NumberTools.reduceDigits(getRunConfidenceHalfWide(level),8);
				node.setAttribute(xmlNameRunHalfWide[0]+s,NumberTools.formatSystemNumber(radius,recycleStringBuilder));
			}
		}

		if (hasDistribution) {
			if (dist==null) initDistribution();
			node.setAttribute(xmlNameDistribution[0],dist.storeToString(recycleStringBuilder));
			final double[] quantils=getQuantil(storeQuantilValues);
			for (int i=0;i<storeQuantilValues.length;i++) {
				node.setAttribute(xmlNameQuantil+Math.round(storeQuantilValues[i]*100),NumberTools.formatSystemNumber(quantils[i],recycleStringBuilder));
			}
			node.setAttribute(xmlNameQuantilLimit[0],NumberTools.formatSystemNumber(dist.upperBound));
		}

		if (useWelford && welfordM2>=0) {
			node.setAttribute(xmlNameWelfordM2[0],NumberTools.formatSystemNumber(welfordM2,recycleStringBuilder));
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
			final Long count=NumberTools.getLong(value);
			if (count==null || count<0) return String.format(xmlNameCountError,node.getNodeName(),value);
			this.count=count;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameSum));
		if (!value.isEmpty()) {
			final Double sum=NumberTools.getDouble(value);
			if (sum==null) return String.format(xmlNameSumError,node.getNodeName(),value);
			this.sum=sum;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameSumSquared));
		if (!value.isEmpty()) {
			final Double sum2=NumberTools.getDouble(value);
			if (sum2==null || sum2<0) return String.format(xmlNameSumSquaredError,node.getNodeName(),value);
			squaredSum=sum2;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameSumCubic));
		if (!value.isEmpty()) {
			final Double sum3=NumberTools.getDouble(value);
			if (sum3==null) return String.format(xmlNameSumCubicError,node.getNodeName(),value);
			cubicSum=sum3;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameSumQuartic));
		if (!value.isEmpty()) {
			final Double sum4=NumberTools.getDouble(value);
			if (sum4==null) return String.format(xmlNameSumQuarticError,node.getNodeName(),value);
			quarticSum=sum4;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameMin));
		if (!value.isEmpty()) {
			final Double min=NumberTools.getDouble(value);
			if (min==null) return String.format(xmlNameMinError,node.getNodeName(),value);
			this.min=min;
		}

		value=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameMax));
		if (!value.isEmpty()) {
			final Double max=NumberTools.getDouble(value);
			if (max==null) return String.format(xmlNameMaxError,node.getNodeName(),value);
			this.max=max;
		}

		if (hasDistribution) {
			if (dist==null) initDistribution();
			value=getAttributeValue(node,xmlNameDistribution);
			if (!value.isEmpty()) {
				double upperBound=dist.upperBound;
				final String limitString=NumberTools.systemNumberToLocalNumber(getAttributeValue(node,xmlNameQuantilLimit));
				if (!limitString.isEmpty()) {
					final Double limit=NumberTools.getDouble(limitString);
					if (limit!=null && limit>0) upperBound=limit;
				}
				final DataDistributionImpl distLoaded=DataDistributionImpl.createFromString(value,upperBound);
				if (distLoaded==null) return String.format(xmlNameDistributionError,node.getNodeName());
				dist=distLoaded;
				densityData=dist.densityData;
				densityDataLength=densityData.length;
				setupArgumentScaleFactor(densityDataLength,upperBound);
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
			final Long L=NumberTools.getPositiveLong(value);
			if (L==null) return String.format(xmlNameBatchSizeError,node.getNodeName(),value);
			batchSize=L.intValue();
		}

		value=getAttributeValue(node,xmlNameBatchCount);
		if (!value.isEmpty()) {
			final Long L=NumberTools.getPositiveLong(value);
			if (L==null) return String.format(xmlNameBatchCountError,node.getNodeName(),value);
			batchMeansCount=L.intValue();
		}

		value=getAttributeValue(node,xmlNameBatchMeansVar);
		if (!value.isEmpty()) {
			final Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(value));
			if (D==null) return String.format(xmlNameBatchMeansVarError,node.getNodeName(),value);
			batchMeansVar=D.doubleValue();
		}

		value=getAttributeValue(node,xmlNameRunCount);
		if (!value.isEmpty()) {
			final Long L=NumberTools.getPositiveLong(value);
			if (L==null) return String.format(xmlNameRunCountError,node.getNodeName(),value);
			runCount=L.intValue();
		}

		value=getAttributeValue(node,xmlNameRunVar);
		if (!value.isEmpty()) {
			final Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(value));
			if (D==null) {
				final Double D2=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(value));
				if (D2==null || D2.doubleValue()<-0.1) return String.format(xmlNameRunVarError,node.getNodeName(),value);
				runVar=0; /* Frühere Versionen konnten beim Speichern aufgrund von Rundungsungenauigkeiten noch ganz leicht negative Werte speichern. */
			} else {
				runVar=D.doubleValue();
			}
		}

		value=getAttributeValue(node,xmlNameWelfordM2);
		if (!value.isEmpty()) {
			final Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(value));
			if (D==null) return String.format(xmlNameWelfordM2Error,node.getNodeName(),value);
			welfordM2=D.doubleValue();
		}

		return null;
	}

	/**
	 * Berechnet die Standardabweichung aus mehreren Teilindikatoren, die dabei jeweils als einzelne Batche aufgefasst werden.
	 * @param partial	Teil-Kenngrößen, die als Batche aufgefasst werden sollen
	 * @param all	Zusammenfassung der Teil-Kenngrößen
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
	 * Halbe Breite des Konfidenzintervalls für den Mittelwert (jeder Teilindikator ist ein Batch) (mehrere Konfidenzniveaus gleichzeitig)<br>
	 * @param partial	Teil-Kenngrößen, die als Batche aufgefasst werden sollen
	 * @param all	Zusammenfassung der Teil-Kenngrößen
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