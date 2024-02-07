/**
 * Copyright 2022 Alexander Herzog
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
package mathtools.distribution.tools;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;

/**
 * Diese Basisklasse stellt allgemeine Funktionen für alle
 * Verteilungsfitter bereit.
 * @author Alexander Herzog
 */
public abstract class DistributionFitterBase {
	/**
	 * Fehlermeldung "Die Eingangswerte konnten nicht interpretiert werden."
	 * @see #processSamples(int[])
	 * @see #processSamples(String[])
	 * @see #processDensity(int[][])
	 * @see #processDensity(String[][])
	 */
	public static String ErrorInvalidFormat="Die Eingangswerte konnten nicht interpretiert werden.";

	/**
	 * Bezeichner für "Anzahl an Messwerten"
	 * @see #processSamples(int[])
	 * @see #processSamples(String[])
	 * @see #processDensity(int[][])
	 * @see #processDensity(String[][])
	 */
	public static String ValueCount="Anzahl an Messwerten";

	/**
	 * Bezeichner für "Wertebereich"
	 */
	public static String ValueRange="Wertebereich";

	/**
	 * Bezeichner für "Mittelwert der Messwerte"
	 */
	public static String Mean="Mittelwert der Messwerte";

	/**
	 * Bezeichner für "Standardabweichung der Messwerte"
	 */
	public static String StdDev="Standardabweichung der Messwerte";

	/**
	 * Nimmt die Statusausgaben in unformatierter Form auf.
	 * @see #getResult(boolean)
	 */
	protected final StringBuilder outputPlain;

	/**
	 * Nimmt die Statusausgaben in html-Form auf.
	 * @see #getResult(boolean)
	 */
	protected final StringBuilder outputHTML;

	/**
	 * Anzahl der erfassten Messwerte
	 * @see #prepareProcessing(DataDistributionImpl)
	 */
	protected int count;

	/**
	 * Messwerte so wie sie {@link #process(DataDistributionImpl)} übergeben wurden
	 */
	protected DataDistributionImpl rawSamples;

	/**
	 * Messwerte in normalisierter Form (gegenüber {@link #rawSamples})
	 */
	protected DataDistributionImpl samples;

	/**
	 * Konstruktor der Klasse
	 */
	public DistributionFitterBase() {
		outputPlain=new StringBuilder();
		outputHTML=new StringBuilder();
	}

	/**
	 * Setzt alle geladenen Daten und Verarbeitetungsergebnisse zurück.
	 */
	public void clear() {
		outputPlain.setLength(0);
		outputHTML.setLength(0);
		rawSamples=null;
		samples=null;
		count=0;
	}

	/**
	 * Verarbeitet eine Reihe von Messwerten, die als Zeichenketten vorliegen
	 * @param data	Zu verarbeitende Messwerte
	 * @return	Gibt <code>true</code> zurück, wenn die Daten verarveitet werden konnten.
	 */
	public boolean processSamples(String[] data) {
		clear();
		DataDistributionImpl dist=DataDistributionImpl.createFromSamplesArray(data,false);
		if (dist==null) {
			outputPlain.append(ErrorInvalidFormat+"\n");
			outputHTML.append(ErrorInvalidFormat+"<br>\n");
			return false;
		}

		if (!prepareProcessing(dist)) return false;
		return process(dist);
	}

	/**
	 * Verarbeitet eine Reihe von Messwerten, die als Zeichenketten vorliegen
	 * @param data	Zu verarbeitende Messwerte
	 * @return	Gibt <code>true</code> zurück, wenn die Daten verarbeitet werden konnten.
	 */
	public boolean processSamples(int[] data) {
		clear();
		DataDistributionImpl dist=DataDistributionImpl.createFromSamplesArray(data,false);
		if (dist==null) {
			outputPlain.append(ErrorInvalidFormat+"\n");
			outputHTML.append(ErrorInvalidFormat+"<br>\n");
			return false;
		}

		if (!prepareProcessing(dist)) return false;
		return process(dist);
	}

	/**
	 * Verarbeitet eine Reihe von Dichte-Angaben (zwei Zeilen)
	 * @param data	ZU verarbeitende Dichte-Angaben
	 * @return	Gibt <code>true</code> zurück, wenn die Daten verarbeitet werden konnten.
	 */
	public boolean processDensity(String[][] data) {
		clear();
		DataDistributionImpl dist=DataDistributionImpl.createFromSamplesArray(data,false);
		if (dist==null) {
			outputPlain.append(ErrorInvalidFormat+"\n");
			outputHTML.append(ErrorInvalidFormat+"<br>\n");
			return false;
		}

		if (!prepareProcessing(dist)) return false;
		return process(dist);
	}

	/**
	 * Verarbeitet eine Reihe von Dichte-Angaben (zwei Zeilen)
	 * @param data	ZU verarbeitende Dichte-Angaben
	 * @return	Gibt <code>true</code> zurück, wenn die Daten verarbeitet werden konnten.
	 */
	public boolean processDensity(int[][] data) {
		clear();
		DataDistributionImpl dist=DataDistributionImpl.createFromSamplesArray(data,false);
		if (dist==null) {
			outputPlain.append(ErrorInvalidFormat+"\n");
			outputHTML.append(ErrorInvalidFormat+"<br>\n");
			return false;
		}

		if (!prepareProcessing(dist)) return false;
		return process(dist);
	}

	/**
	 * Verarbeitet eine Dichte.
	 * @param data	Zu verarbeitende Dichte
	 * @return	Gibt <code>true</code> zurück, wenn die Daten verarbeitet werden konnten.
	 */
	public boolean processDensity(final DataDistributionImpl data) {
		clear();

		final DataDistributionImpl dist=data.clone();

		if (!prepareProcessing(dist)) return false;
		return process(dist);
	}

	/**
	 * Vorverarbeitung der Messwerte
	 * (Normalisierung, Ausgabe der generellen Informationen in {@link #outputPlain} and {@link #outputHTML})
	 * @param dist	Zu verarbeitende Verteilung
	 * @return	Gibt an, ob die Vorverarbeitung erfolgreich war, die Datenreihe also verwendet werden kann
	 * @see #process(DataDistributionImpl)
	 */
	private boolean prepareProcessing(final DataDistributionImpl dist) {
		clear();
		if (dist==null || dist.densityData.length<2) return false;
		samples=dist.clone();
		rawSamples=dist.clone();
		count=(int)Math.round(samples.sum());
		samples.normalizeDensity();

		final double mean=samples.getMean();
		final double sd=samples.getStandardDeviation();

		int m1=-1; int m2=0;
		for (int i=0;i<samples.densityData.length;i++) if (samples.densityData[i]>0) {
			if (m1<0) m1=i;
			m2=i;
		}
		outputPlain.append(ValueCount+": "+count+"\n");
		outputPlain.append(ValueRange+": "+m1+".."+m2+"\n");
		outputPlain.append("\n");
		outputPlain.append(Mean+": "+NumberTools.formatNumber(mean,3)+"\n");
		outputPlain.append(StdDev+": "+NumberTools.formatNumber(sd,3)+"\n");
		outputPlain.append("\n");
		outputHTML.append("<p>");
		outputHTML.append(ValueCount+": "+count+"<br>\n");
		outputHTML.append(ValueRange+": "+m1+".."+m2+"<br>\n");
		outputHTML.append("</p>");
		outputHTML.append("<p>");
		outputHTML.append(Mean+": "+NumberTools.formatNumber(mean,3)+"<br>\n");
		outputHTML.append(StdDev+": "+NumberTools.formatNumber(sd,3)+"<br>\n");
		outputHTML.append("</p>");

		return true;
	}

	/**
	 * Verarbeitet eine Messwerte-Dichte-Verteilung
	 * @param dist	Messwerte-Dichte-Verteilung
	 * @return	Gibt <code>true</code> zurück, wenn die Daten verarbeitet werden konnten.
	 */
	protected abstract boolean process(final DataDistributionImpl dist);

	/**
	 * Verteilungen, die beim Fitting geprüft werden
	 * @see #getFitDistributions()
	 * @see #getFitDistributionCount()
	 */
	private final static Set<Class<? extends AbstractDistributionWrapper>> fitDistributions=new HashSet<>();

	/**
	 * Liefert die Menge der Verteilungen, die beim Fitting geprüft werden.
	 * @return	Menge der Verteilungen, die beim Fitting geprüft werden
	 */
	public static Set<Class<? extends AbstractDistributionWrapper>> getFitDistributions() {
		if (fitDistributions.size()==0) {
			for (String name: DistributionTools.getDistributionNames()) {
				final AbstractDistributionWrapper wrapper=DistributionTools.getWrapper(name);
				final AbstractRealDistribution fit1=wrapper.getDistributionForFit(100,50,10,200);
				final AbstractRealDistribution fit2=wrapper.getDistributionForFit(100,0,10,200);
				final AbstractRealDistribution fit3=wrapper.getDistributionForFit(1,0.5,10,200);
				if (fit1!=null || fit2!=null || fit3!=null) {
					fitDistributions.add(wrapper.getClass());
				}
			}
		}
		return fitDistributions;
	}

	/**
	 * Liefert die Anzahl an Verteilungen, die beim Fitting geprüft werden.
	 * @return	Anzahl an Verteilungen, die beim Fitting geprüft werden
	 */
	public static int getFitDistributionCount() {
		return getFitDistributions().size();
	}

	/**
	 * Liefert alle Statusausgaben
	 * @param html	Gibt an, ob die Ergebnisse als einfacher Text (<code>false</code>) oder mit HTML-Auszeichnungen (<code>true</code>) ausgegeben werden sollen.
	 * @return	Statusausgaben zur Verteilungsanpassung
	 */
	public String getResult(final boolean html) {
		if (html) return outputHTML.toString(); else return outputPlain.toString();
	}

	/**
	 * Erstellt eine Verteilung auf Basis der übergebenen Messwerten (eine Zeile) oder Dichtewerten (zwei Zeilen).
	 * @param values	Array aus einem oder zwei gleichlangen Unterarrays
	 * @return	Im Fehlerfalle <code>null</code> sonst zwei Elemente: die Verteilung und ein boolscher Wert, der angibt, ob Werte gerundet werden mussten
	 */
	public static Object[] dataDistributionFromValues(double[][] values) {
		if (values==null || values.length==0 || values[0]==null || values[0].length==0) return null;

		double maxValue=1;
		for (double d: values[0]) maxValue=Math.max(d,maxValue);
		maxValue=Math.ceil(maxValue);
		int maxIndex=(int)Math.round(maxValue);
		DataDistributionImpl distribution=new DataDistributionImpl(maxValue,maxIndex+1);
		boolean hasFloat=false;
		if (values.length==1) {
			for (double d: values[0]) {
				distribution.densityData[(int)Math.max(0,Math.min(Math.round(d),maxIndex))]++;
				if (Math.abs(Math.round(d)-d)>0.0001) hasFloat=true;
			}
		} else {
			for (int i=0;i<Math.min(values[0].length,values[1].length);i++) {
				distribution.densityData[(int)Math.max(0,Math.min(Math.round(values[0][i]),maxIndex))]=values[1][i];
				if (Math.abs(Math.round(values[0][i])-values[0][i])>0.0001) hasFloat=true;
				if (Math.abs(Math.round(values[1][i])-values[1][i])>0.0001) hasFloat=true;
			}
		}
		return new Object[]{distribution,hasFloat};
	}
}
