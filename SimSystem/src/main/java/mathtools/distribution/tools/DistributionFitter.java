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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * @version 2.1
 */
public class DistributionFitter extends DistributionFitterBase {
	/**
	 * Anzahl an anzuzeigenden am besten passenden Fits
	 */
	private static final int DISPLAY_NUMBER_OF_FITS=5;

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

	/**
	 * Angabe der quadratischen Abweichungen der Verteilungen
	 * @see #getFitDistribution()
	 */
	private Map<AbstractRealDistribution,Double> fit;

	/**
	 * Liste mit den Verteilungen, die getestet wurden
	 * @see #getResultListDist()
	 */
	private final List<AbstractRealDistribution> outputDist;

	/**
	 * Liste mit den Abweichungen pro getesteter Verteilung
	 * @see #getResultListError()
	 */
	private final List<Double> outputError;

	/**
	 * Liste mit Informationen (in Textform) zu den getesteten Verteilungen
	 * @see #getResultList()
	 */
	protected final List<String> outputInfo;

	/**
	 * Konstruktor der Klasse
	 */
	public DistributionFitter() {
		fit=new HashMap<>();
		outputDist=new ArrayList<>();
		outputError=new ArrayList<>();
		outputInfo=new ArrayList<>();
		clear();
	}

	/**
	 * Setzt alle geladenen Daten und Verarbeitetungsergebnisse zurück.
	 */
	@Override
	public void clear() {
		super.clear();
		outputDist.clear();
		outputError.clear();
		outputInfo.clear();
		fit.clear();
	}

	/**
	 * Verarbeitet eine Messwerte-Dichte-Verteilung
	 * @param dist	Messwerte-Dichte-Verteilung
	 * @return	Gibt <code>true</code> zurück, wenn die Daten verarbeitet werden konnten.
	 */
	@Override
	protected boolean process(final DataDistributionImpl dist) {
		final double mean=samples.getMean();
		final double sd=samples.getStandardDeviation();
		final double min=dist.getMin();
		final double max=dist.getMax(); /* dist enthält die Messwerte; samples.getMax() würde hingegen den maximalen Dichte-Werte für ein Intervall angeben, nicht das höchste Intervall mit einem Dichtewert größer als 0 */

		outputPlain.append(ComparedDistributions+"\n");
		outputHTML.append("<h3>"+ComparedDistributions+"</h3>\n");

		for (String name: DistributionTools.getDistributionNames()) {
			calcMatch(DistributionTools.getWrapper(name),mean,sd,min,max);
		}

		outputPlain.append("\n");
		outputPlain.append(BestFitFor+"\n");
		outputHTML.append("<h3>"+BestFitFor+"</h3>");
		final List<AbstractRealDistribution> dists=getFitDistribution();
		for (int i=0;i<Math.min(DISPLAY_NUMBER_OF_FITS,dists.size());i++) {
			final AbstractRealDistribution fitDist=dists.get(i);
			final String name=DistributionTools.getDistributionName(fitDist);
			final String info=DistributionTools.getDistributionInfo(fitDist);
			double diff=0;
			for (Map.Entry<AbstractRealDistribution,Double> entry: fit.entrySet()) if (entry.getKey().getClass()==fitDist.getClass()) {
				diff=entry.getValue();
				break;
			}
			String error=NumberTools.formatNumber(diff,3);
			if (error.equals("0")) error=NumberTools.formatNumber(diff,9);
			outputPlain.append(""+(i+1)+". "+name+" ("+info+"), "+MeanSquares+": "+error+"\n");
			outputHTML.append(""+(i+1)+". "+name+" ("+info+"), "+MeanSquares+": <b>"+error+"</b><br>");
		}

		return true;
	}

	/**
	 * Fügt Informationen zu einer Verteilung (bzw. der Güte des Fits gegen diese Verteilung) zu der Ausgabe hinzu
	 * @param dist	Verteilung deren Daten ausgegeben werden sollen
	 * @param error	Quadratische Abweichung zwischen Messwerten und dieser Verteilung
	 * @see #calcMatch(AbstractRealDistribution)
	 */
	private void addResultToOutputList(AbstractRealDistribution dist, double error) {
		int pos=outputError.size();
		for (int i=0;i<outputError.size();i++) if (outputError.get(i)>error) {pos=i; break;}

		outputInfo.add(pos,DistributionTools.getDistributionName(dist)+" ("+DistributionTools.getDistributionInfo(dist)+") "+FitError+": "+NumberTools.formatNumber(error,3));
		outputDist.add(pos,DistributionTools.cloneDistribution(dist));
		outputError.add(pos,error);
	}

	/**
	 * Berechnet die quadrierte mittlere Abweichung zwischen einer Verteilung und den Messwerten
	 * @param dist	Verteilung zu der die Abweichung berechnet werden sollen
	 * @return	Quadrierte mittlere Abweichung
	 * @see #calcMatch(AbstractRealDistribution)
	 */
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

	/**
	 * p-Value gemäß Kolmogorov-Smirnov-Anpassungstest ausrechnen
	 * @param dist	Verteilung zwischen der und den Messwerten der p-Value bestimmt werden soll
	 * @return	p-Value gemäß Kolmogorov-Smirnov-Anpassungstest
	 * @see #calcMatch(AbstractRealDistribution)
	 */
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

	/**
	 * p-Value gemäß chi²-Anpassungstest ausrechnen
	 * @param dist	Verteilung zwischen der und den Messwerten der p-Value bestimmt werden soll
	 * @return	p-Value gemäß chi²-Anpassungstest
	 * @see #calcMatch(AbstractRealDistribution)
	 */
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

	/**
	 * p-Value gemäß Anderson-Darling-Anpassungstest ausrechnen
	 * @param dist	Verteilung zwischen der und den Messwerten der p-Value bestimmt werden soll
	 * @return	p-Value gemäß Anderson-Darling-Anpassungstest
	 * @see #calcMatch(AbstractRealDistribution)
	 */
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

	/**
	 * Berechnet, wie gut eine vorgegebene Verteilung zu den Messwerten passt
	 * (quadrierte mittlere Abweichung und auch verschiedene Anpassungstests)
	 * @param dist	Zu prüfende Verteilung
	 * @see #fit
	 */
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

		/* Qualität des Fits speichern */
		fit.put(DistributionTools.cloneDistribution(dist),diff);

		/* Ergebnis in Liste aufnehmen */
		addResultToOutputList(dist,diff);
	}

	/**
	 * Versucht eine Verteilung an die Messwerte anzupassen und berechnet dann,
	 * wenn die Anpassung möglich ist, die Abweichung
	 * @param wrapper	Typ der Verteilung
	 * @param min	Minimal aufgetretener Messwert
	 * @param max	Maximal aufgetretener Messwert
	 * @param mean	Einzustellender Erwartungswert
	 * @param sd	Einzustellende Standardabweichung
	 */
	private void calcMatch(final AbstractDistributionWrapper wrapper, final double mean, final double sd, final double min, final double max) {
		if (wrapper==null) return;
		final AbstractRealDistribution fit=wrapper.getDistributionForFit(mean,sd,min,max);
		if (fit!=null) calcMatch(fit);
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
	public List<AbstractRealDistribution> getFitDistribution() {
		final List<AbstractRealDistribution> results=new ArrayList<>();

		final Map<AbstractRealDistribution,Double> data=new HashMap<>(fit);
		while (data.size()>0) {
			double bestValue=Double.MAX_VALUE;
			AbstractRealDistribution bestDist=null;
			for (Map.Entry<AbstractRealDistribution,Double> entry: data.entrySet()) if (entry.getValue()<bestValue) {
				bestValue=entry.getValue();
				bestDist=entry.getKey();
			}
			data.remove(bestDist);
			results.add(bestDist);
		}

		return results;
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
	 * Liefert eine Liste mit den Verteilungen, die getestet wurden
	 * @return Liste mit allen getesteten (und angepassten) Verteilungen
	 * @see DistributionFitter#getResultList
	 * @see DistributionFitter#getResultListError
	 */
	public List<AbstractRealDistribution> getResultListDist() {
		return outputDist;
	}

	/**
	 * Liefert eine Liste mit den Abweichungen pro getesteter Verteilung
	 * @return Liste mit den Abweichungen pro getesteter Verteilung
	 * @see DistributionFitter#getResultListDist
	 * @see DistributionFitter#getResultList
	 */
	public List<Double> getResultListError() {
		return outputError;
	}
}