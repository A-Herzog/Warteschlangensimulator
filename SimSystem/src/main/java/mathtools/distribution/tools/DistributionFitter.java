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
package mathtools.distribution.tools;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.OnePointDistributionImpl;

/**
 * Versucht zu gegebenen Messwerten eine Verteilungsfunktion zu finden,
 * die diese möglichst gut beschreibt.
 * @author Alexander Herzog
 * @version 2.0
 */
public class DistributionFitter {
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
	 * Bezeichner für "Geprüfte Verteilungen"
	 */
	public static String ComparedDistributions="Geprüfte Verteilungen";

	/**
	 * Bezeichner für "Mittlere quadratische Abweichung"
	 */
	public static String MeanSquares="Mittlere quadratische Abweichung";

	/**
	 * Bezeichner für "KS-Anpassungstest p-Wert"
	 */
	public static String PValue="KS-Anpassungstest p-Wert";

	/**
	 * Bezeichner für "Chi²-Anpassungstest p-Wert"
	 */
	public static String PValueChiSqr="Chi²-Anpassungstest p-Wert";

	/**
	 * Bezeichner für "Anderson-Darling-Anpassungstest p-Wert"
	 */
	public static String PValueAndersonDarling="Anderson-Darling-Anpassungstest p-Wert";

	/**
	 * Bezeichner für "Beste Übereinstimmung für"
	 */
	public static String BestFitFor="Beste Übereinstimmung für";

	/**
	 * Bezeichner für "Abweichung"
	 */
	public static String FitError="Abweichung";

	private final StringBuilder outputPlain;
	private final StringBuilder outputHTML;
	private int count;
	private DataDistributionImpl rawSamples;
	private DataDistributionImpl samples;
	private AbstractRealDistribution fit;
	private double fitError;

	private final List<String> outputInfo;
	private final List<AbstractRealDistribution> outputDist;
	private final List<Double> outputError;

	/**
	 * Konstruktor der Klasse
	 */
	public DistributionFitter() {
		outputPlain=new StringBuilder();
		outputHTML=new StringBuilder();
		outputInfo=new ArrayList<>();
		outputDist=new ArrayList<>();
		outputError=new ArrayList<>();
		clear();
	}

	/**
	 * Setzt alle geladenen Daten und Verarbeitetungsergebnisse zurück.
	 */
	public void clear() {
		outputPlain.setLength(0);
		outputHTML.setLength(0);
		outputInfo.clear();
		outputDist.clear();
		outputError.clear();
		rawSamples=null;
		samples=null;
		count=0;
		fit=null;
		fitError=Integer.MAX_VALUE;
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
		return process(dist);
	}

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
		outputHTML.append(ValueCount+": "+count+"<br>\n");
		outputHTML.append(ValueRange+": "+m1+".."+m2+"<br>\n");
		outputHTML.append("<br>\n");
		outputHTML.append(Mean+": "+NumberTools.formatNumber(mean,3)+"<br>\n");
		outputHTML.append(StdDev+": "+NumberTools.formatNumber(sd,3)+"<br>\n");
		outputHTML.append("<br>\n");

		return true;
	}

	/**
	 * Verarbeitet eine Messwerte-Dichte-Verteilung
	 * @param dist	Messwerte-Dichte-Verteilung
	 * @return	Gibt <code>true</code> zurück, wenn die Daten verarbeitet werden konnten.
	 * @see DistributionFitter#dataDistributionFromValues(double[][])
	 */
	public boolean process(final DataDistributionImpl dist) {
		if (!prepareProcessing(dist)) return false;

		final double mean=samples.getMean();
		final double sd=samples.getStandardDeviation();

		outputPlain.append(ComparedDistributions+"\n");
		outputHTML.append("<h3>"+ComparedDistributions+"</h3>\n");

		for (String name: DistributionTools.getDistributionNames()) {
			calcMatch(DistributionTools.getWrapper(name),mean,sd);
		}

		outputPlain.append("\n");
		outputPlain.append(BestFitFor+"\n");
		outputPlain.append(DistributionTools.getDistributionName(fit)+" ("+DistributionTools.getDistributionInfo(fit)+")\n");
		outputHTML.append("<h3>"+BestFitFor+"</h3>");
		outputHTML.append(DistributionTools.getDistributionName(fit)+" ("+DistributionTools.getDistributionInfo(fit)+")<br>");

		return true;
	}

	private void addResultToOutputList(AbstractRealDistribution dist, double error) {
		int pos=outputError.size();
		for (int i=0;i<outputError.size();i++) if (outputError.get(i)>error) {pos=i; break;}

		outputInfo.add(pos,DistributionTools.getDistributionName(dist)+" ("+DistributionTools.getDistributionInfo(dist)+") "+FitError+": "+NumberTools.formatNumber(error,3));
		outputDist.add(pos,DistributionTools.cloneDistribution(dist));
		outputError.add(pos,error);
	}

	private double calcSquaredDiff(AbstractRealDistribution dist) {
		double diff=0;

		/*System.out.println(DistributionTools.getDistributionName(dist));*/

		if (samples.getStandardDeviation()<0.00001 && dist instanceof OnePointDistributionImpl) {
			if (Math.abs(((OnePointDistributionImpl)dist).point-samples.getMean())<0.01) diff=0; else diff=Double.POSITIVE_INFINITY;
		} else {
			final int max=Math.min(1_000_000,samples.densityData.length*2);
			double[] valuesSamples=new double[max];
			double[] valuesDist=new double[max];
			double sumSamples=0, sumDist=0;
			for (int i=0;i<max;i++) {
				sumSamples+=valuesSamples[i]=samples.density(i);
				double d=dist.density(i);
				if (i==0 && (Double.isInfinite(d) || Double.isNaN(d))) d=0;
				sumDist+=valuesDist[i]=d;
			}
			if (sumSamples==0) sumSamples=1;
			if (sumDist==0) sumDist=1;
			for (int i=0;i<max;i++) {
				double a=valuesSamples[i]/sumSamples;
				double b=valuesDist[i]/sumDist;
				if (a>10E100 || b>10E100) continue;
				/*System.out.println("x="+i+", f(x)="+NumberTools.formatNumber(b,2)+", values(x)="+NumberTools.formatNumber(a,2));*/
				/*diff+=Math.sqrt(Math.abs(a-b)/Math.max(0.0001,Math.abs(b)));*/
				diff+=(a-b)*(a-b);
			}
		}
		/*System.out.println("diff="+NumberTools.formatNumber(diff,8));*/

		return diff;
	}

	private double calcPValueKS(AbstractRealDistribution dist) {
		double maxDiff=0;

		final double[] cdf=samples.cumulativeDensity;
		for (int i=0;i<cdf.length;i++) {
			final double d=dist.cumulativeProbability(i);
			if (i==0 && (Double.isInfinite(d) || Double.isNaN(d))) continue;
			maxDiff=FastMath.max(maxDiff,Math.abs(cdf[i]-d));
		}

		return Math.min(1,2*FastMath.exp(-2*count*maxDiff*maxDiff));
	}

	private double calcPValueChiSqr(AbstractRealDistribution dist) {
		double sumRelDif=0;
		int steps=0;

		final double[] cdf=samples.cumulativeDensity;
		for (int i=1;i<cdf.length;i++) {
			final double d1=dist.cumulativeProbability(i-1);
			final double d2=dist.cumulativeProbability(i);
			if (Double.isInfinite(d1) || Double.isNaN(d1)) continue;
			if (Double.isInfinite(d2) || Double.isNaN(d2)) continue;
			final double delta=d2-d1;
			if (delta<=0) continue;
			steps++;
			final double samplesDelta=cdf[i]-cdf[i-1];
			sumRelDif+=count*(samplesDelta-delta)*(samplesDelta-delta)/delta;
		}

		ChiSquaredDistribution chiSqr=new ChiSquaredDistribution(steps-1);
		return 1-chiSqr.cumulativeProbability(sumRelDif);
	}

	private double calcPValueAndersonDarling(AbstractRealDistribution dist) {
		/* Siehe https://en.wikipedia.org/wiki/Anderson–Darling_test */

		final double mean=dist.getNumericalMean();
		final double stdDev=Math.sqrt(dist.getNumericalVariance());
		if (stdDev==0) return 0;

		/* Vektor Y aufbauen */
		final int n=count;
		final double[] y=new double[count];
		int offset=0;
		for (int index1=0;index1<rawSamples.densityData.length;index1++) {
			int count=(int)Math.round(rawSamples.densityData[index1]);
			for (int index2=0;index2<count;index2++) {
				y[offset]=(index1-mean)/stdDev;
				offset++;
			}
		}

		/* Summe S berechnen */
		final AbstractRealDistribution stdNormal=new NormalDistribution();
		double s=0;
		for (int i=1;i<=n;i++) {
			final double value=Math.log(stdNormal.cumulativeProbability(y[i-1]))+Math.log(1-stdNormal.cumulativeProbability(y[n-i]));
			if (!Double.isNaN(value) && !Double.isInfinite(value)) {
				s+=(2*i-1)*value;
			} else {
				/* System.err.println("Fehler: "+y[i-1]+"   "+y[(n+1-i)-1]+"   "+stdNormal.cumulativeProbability(y[i-1])+"   "+(1-stdNormal.cumulativeProbability(y[(n+1-i)-1]))); */
			}
		}
		final double ASqr=-n-s/n;

		/* Hilfsgröße z */
		final double z=ASqr*(1+0.75/n+2.25/n/n);

		/* p-Value */
		/* Siehe https://www.sixsigmablackbelt.de/test-auf-normalverteilung-excel/ */
		double p;
		if (z>0.6) p=FastMath.exp(1.2937-5.709*z+0.0186*z*z); else {
			if (z>0.34) p=FastMath.exp(0.9177-4.279*z-1.38*z*z); else {
				if (z>0.2) p=1-FastMath.exp(-8.318+42.796*z-59.938*z*z); else {
					p=1-FastMath.exp(-13.436+101.14*z-223.73*z*z);
				}
			}
		}
		return Math.min(1,Math.max(0,1-p));
	}

	private void calcMatch(final AbstractRealDistribution dist) {
		/* Quadrierte mittlere Abweichung ausrechnen */
		final double diff=calcSquaredDiff(dist);

		/* p-Value gemäß Kolmogorov-Smirnov-Anpassungstest ausrechnen */
		final double pKS=calcPValueKS(dist);

		/* p-Value gemäß chi²-Anpassungstest ausrechnen */
		final double pChiSqr=calcPValueChiSqr(dist);

		/* p-Value gemäß Anderson-Darling-Anpassungstest ausrechnen */
		final double pAndersonDarling=(dist instanceof NormalDistribution)?calcPValueAndersonDarling(dist):0;

		/* Ausgabe */

		outputPlain.append(DistributionTools.getDistributionName(dist)+" ");
		outputPlain.append("("+DistributionTools.getDistributionInfo(dist)+")\n");
		String s=NumberTools.formatNumber(diff,3);
		if (s.equals("0")) s=NumberTools.formatNumber(diff,9);
		outputPlain.append(MeanSquares+": "+s+"\n");
		outputPlain.append(PValue+": "+NumberTools.formatPercent(pKS)+"\n");
		outputPlain.append(PValueChiSqr+": "+NumberTools.formatPercent(pChiSqr)+"\n");
		if (dist instanceof NormalDistribution) {
			outputPlain.append(PValueAndersonDarling+": "+NumberTools.formatPercent(pAndersonDarling)+"\n");
		}

		outputHTML.append("<u>"+DistributionTools.getDistributionName(dist)+"</u><br>\n");
		outputHTML.append("("+DistributionTools.getDistributionInfo(dist)+")<br>");
		outputHTML.append(MeanSquares+": <b>"+NumberTools.formatNumber(diff,3)+"</b><br>");
		outputHTML.append(PValue+": "+NumberTools.formatPercent(pKS)+"<br>");
		outputHTML.append(PValueChiSqr+": "+NumberTools.formatPercent(pChiSqr)+"<br>");
		if (dist instanceof NormalDistribution) {
			outputHTML.append(PValueAndersonDarling+": "+NumberTools.formatPercent(pAndersonDarling)+"<br>");
		}

		/* Ist der Fit besser? */
		if (diff<fitError) {
			fit=DistributionTools.cloneDistribution(dist);
			fitError=diff;
		}

		/* Ergebnis in Liste aufnehmen */
		addResultToOutputList(dist,diff);
	}

	private void calcMatch(final AbstractDistributionWrapper wrapper, final double mean, final double sd) {
		if (wrapper==null) return;
		final AbstractRealDistribution fit=wrapper.getDistributionForFit(mean,sd);
		if (fit!=null) calcMatch(fit);
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
	 * Liefert die Verteilung der Messwerte (Eingangsdaten)
	 * @return Verteilung der Messwerte
	 */
	public DataDistributionImpl getSamplesDistribution() {
		return samples;
	}

	/**
	 * Liefert die am besten zu den Messwerten passende Verteilung
	 * @return Am besten zu den Messwerten passende Verteilung
	 */
	public AbstractRealDistribution getFitDistribution() {
		return fit;
	}

	/**
	 * Liefert eine Liste mit Informationen (in Textform) zu den getesteten Verteilungen
	 * @return	Liste mit Texten, die jeweils Verteilungsname und quadrierten Fehler pro Verteilung enthalten
	 * @see DistributionFitter#getResultListDist
	 * @see DistributionFitter#getResultListError
	 */
	public List<String> getResultList() {
		return outputInfo;
	}

	/**
	 * Liste mit den Verteilungen, die getestet wurden
	 * @return Liste mit allen getesteten (und angepassten) Verteilungen
	 * @see DistributionFitter#getResultList
	 * @see DistributionFitter#getResultListError
	 */
	public List<AbstractRealDistribution> getResultListDist() {
		return outputDist;
	}

	/**
	 * Liste mit den Abweichungen pro getesteter Verteilung
	 * @return Liste mit den Abweichungen pro getesteter Verteilung
	 * @see DistributionFitter#getResultListDist
	 * @see DistributionFitter#getResultList
	 */
	public List<Double> getResultListError() {
		return outputError;
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