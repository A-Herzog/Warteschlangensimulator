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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
	 * Bezeichner für "Geprüfte Verteilungen"
	 */
	public static String ComparedDistributions="Geprüfte Verteilungen (%d Stück)";

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
	 * Infotext zu nicht geprüften Verteilungen
	 */
	public static String NotFit="Keine Anpassung an die aktuellen Messwerte möglich.";

	/**
	 * Angabe der quadratischen Abweichungen der Verteilungen
	 * @see #getFitDistribution()
	 */
	private List<Fit> fits;

	/**
	 * Liste mit Informationen (in Textform) zu den getesteten Verteilungen
	 * @see #getResultList()
	 */
	protected final List<String> outputInfo;

	/**
	 * Konstruktor der Klasse
	 */
	public DistributionFitter() {
		fits=new ArrayList<>();
		outputInfo=new ArrayList<>();
		clear();
	}

	/**
	 * Setzt alle geladenen Daten und Verarbeitetungsergebnisse zurück.
	 */
	@Override
	public void clear() {
		super.clear();
		outputInfo.clear();
		fits.clear();
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
		/* Sowohl samples.getMax() als auch dist.getMax() liefern nicht das, was wir brauchen */
		double min=dist.densityData.length-1;
		double max=0;
		for (int i=0;i<dist.densityData.length;i++) if (dist.densityData[i]>0) {
			if (i<min) min=i;
			if (i>max) max=i;
		}
		outputPlain.append(String.format(ComparedDistributions,getFitDistributionCount())+"\n");
		outputHTML.append("<h3>"+String.format(ComparedDistributions,getFitDistributionCount())+"</h3>\n");

		/* Fits berechnen */
		final Set<Class<? extends AbstractDistributionWrapper>> candidates=new HashSet<>(getFitDistributions());
		for (String name: DistributionTools.getDistributionNames()) {
			final AbstractDistributionWrapper wrapper=DistributionTools.getWrapper(name);
			if (calcMatch(wrapper,mean,sd,min,max)) {
				candidates.remove(wrapper.getClass());
			}
		}

		/* Ergebnisse für Fits ausgeben */
		outputPlain.append(BestFitFor+"\n");
		outputHTML.append("<h3>"+BestFitFor+"</h3>");
		outputHTML.append("<ol>");

		int rank=1;
		for (var fit: fits.stream().sorted((f1,f2)->(int)Math.signum(f1.fit-f2.fit)).toArray(Fit[]::new)) {

			outputPlain.append(rank+". ");
			outputPlain.append(fit.infoPlain);
			rank++;

			outputHTML.append("<li>");
			outputHTML.append(fit.infoHTML);
			outputHTML.append("</li>");
		}

		/* Info ausgeben für Verteilungen, für die kein Fit berechnet werden konnte */
		for (Class<? extends AbstractDistributionWrapper> wrapperCls: candidates) {
			try {
				final AbstractDistributionWrapper wrapper=wrapperCls.getDeclaredConstructor().newInstance();
				final String name=wrapper.getName();
				outputPlain.append(name);
				outputPlain.append("\n");
				outputPlain.append(NotFit);
				outputPlain.append("\n");
				outputHTML.append("<li>");
				outputHTML.append("<u>"+name+"</u><br>\n");
				outputHTML.append(NotFit+"<br>");
				outputHTML.append("</li>");
			} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e) {}
		}

		outputHTML.append("</ol>");

		return true;
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
		final double scaleFactor=samples.getArgumentScaleFactor();
		final int len=cdf.length;
		for (int i=0;i<len;i++) {
			final double d=dist.cumulativeProbability(i/scaleFactor);
			if (d>0.9999) break;
			if (i==0 && (Double.isInfinite(d) || Double.isNaN(d))) continue;
			maxDiff=FastMath.max(maxDiff,Math.abs(cdf[i]-d));
		}

		return Math.min(1,2*FastMath.exp(-2*maxDiff*maxDiff));
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
		final int len=cdf.length;
		final double scaleFactor=samples.getArgumentScaleFactor();
		double d1=dist.cumulativeProbability(0);
		for (int i=0;i<len;i++) {
			final double d2=dist.cumulativeProbability((i+1)/scaleFactor);
			if (Double.isInfinite(d1) || Double.isNaN(d1)) continue;
			if (Double.isInfinite(d2) || Double.isNaN(d2)) continue;
			final double delta=d2-d1;
			d1=d2;
			if (d2>0.9999) break;
			if (delta<=0) continue;
			steps++;
			final double samplesDelta=(i==0)?cdf[0]:(cdf[i]-cdf[i-1]);
			sumRelDif+=count*(samplesDelta-delta)*(samplesDelta-delta)/delta;
		}

		final ChiSquaredDistribution chiSqr=new ChiSquaredDistribution(Math.max(steps-1,1));
		return 1-chiSqr.cumulativeProbability(sumRelDif);
	}

	/**
	 * Standardnormalverteilung für {@link #calcPValueAndersonDarling(AbstractRealDistribution)}
	 * @see #calcPValueAndersonDarling(AbstractRealDistribution)
	 */
	private static final AbstractRealDistribution stdNormal=new NormalDistribution();

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
		final var densityData=rawSamples.densityData;
		final int len=densityData.length;
		for (int index1=0;index1<len;index1++) {
			final int count=(int)Math.round(densityData[index1]);
			for (int index2=0;index2<count;index2++) {
				y[offset]=(index1-mean)/stdDev;
				offset++;
			}
		}

		/* Summe S berechnen */
		double s=0;
		final double[] cdf=new double[n];
		Arrays.fill(cdf,-1);
		for (int i=1;i<=n;i++) {
			if (cdf[i-1]==-1) cdf[i-1]=stdNormal.cumulativeProbability(y[i-1]);
			if (cdf[n-i]==-1) cdf[n-i]=stdNormal.cumulativeProbability(y[n-i]);
			final double value=Math.log(cdf[i-1])+Math.log(1-cdf[n-i]);
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
	 * @return	Gibt an, ob für die gegebene Verteilung eine Anpassung vorgenommen werden konnte.
	 * @see #fits
	 */
	private boolean calcMatch(final AbstractRealDistribution dist) {
		/* Quadrierte mittlere Abweichung ausrechnen */
		final double diff=calcSquaredDiff(dist);
		if (Double.isNaN(diff)) return false;

		/* p-Value gemäß Kolmogorov-Smirnov-Anpassungstest ausrechnen */
		final double pKS=calcPValueKS(dist);

		/* p-Value gemäß chi²-Anpassungstest ausrechnen */
		final double pChiSqr=calcPValueChiSqr(dist);

		/* p-Value gemäß Anderson-Darling-Anpassungstest ausrechnen */
		final double pAndersonDarling=(dist instanceof NormalDistribution)?calcPValueAndersonDarling(dist):0;

		/* Ausgabe */

		final var outputPlain=new StringBuilder();
		final var outputHTML=new StringBuilder();

		String diffStr=NumberTools.formatNumber(diff,3);
		if (diffStr.equals("0")) diffStr=NumberTools.formatNumber(diff,9);

		outputPlain.append(DistributionTools.getDistributionName(dist)+" ");
		outputPlain.append("("+DistributionTools.getDistributionInfo(dist)+")\n");
		outputPlain.append(MeanSquares+": "+diffStr+"\n");
		outputPlain.append(PValue+": "+NumberTools.formatPercent(pKS)+"\n");
		outputPlain.append(PValueChiSqr+": "+NumberTools.formatPercent(pChiSqr)+"\n");
		if (dist instanceof NormalDistribution) {
			outputPlain.append(PValueAndersonDarling+": "+NumberTools.formatPercent(pAndersonDarling)+"\n");
		}

		outputHTML.append("<u>"+DistributionTools.getDistributionName(dist)+"</u><br>\n");
		outputHTML.append("("+DistributionTools.getDistributionInfo(dist)+")<br>");
		outputHTML.append(MeanSquares+": <b>"+diffStr+"</b><br>");
		outputHTML.append(PValue+": "+NumberTools.formatPercent(pKS)+"<br>");
		outputHTML.append(PValueChiSqr+": "+NumberTools.formatPercent(pChiSqr)+"<br>");
		if (dist instanceof NormalDistribution) {
			outputHTML.append(PValueAndersonDarling+": "+NumberTools.formatPercent(pAndersonDarling)+"<br>");
		}

		/* Qualität des Fits speichern */
		fits.add(new Fit(dist,diff,outputPlain.toString(),outputHTML.toString()));

		return true;
	}

	/**
	 * Versucht eine Verteilung an die Messwerte anzupassen und berechnet dann,
	 * wenn die Anpassung möglich ist, die Abweichung
	 * @param wrapper	Typ der Verteilung
	 * @param min	Minimal aufgetretener Messwert
	 * @param max	Maximal aufgetretener Messwert
	 * @param mean	Einzustellender Erwartungswert
	 * @param sd	Einzustellende Standardabweichung
	 * @return	Gibt an, ob für die gegebene Verteilung eine Anpassung vorgenommen werden konnte.
	 */
	private boolean calcMatch(final AbstractDistributionWrapper wrapper, final double mean, final double sd, final double min, final double max) {
		if (wrapper==null) return false;
		final AbstractRealDistribution fit=wrapper.getDistributionForFit(mean,sd,min,max);
		if (fit==null) return false;
		return calcMatch(fit);
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
		return getSortedFits().stream().map(fit->fit.distribution).collect(Collectors.toList());
	}

	/**
	 * Liefert eine Liste mit Informationen (in Textform) zu den getesteten Verteilungen
	 * @return	Liste mit Texten, die jeweils Verteilungsname und quadrierten Fehler pro Verteilung enthalten
	 */
	public List<String> getResultList() {
		return outputInfo;
	}


	/**
	 * Liefert die Liste aller angepassten Verteilungen.
	 * @return	Liste aller angepassten Verteilungen
	 */
	public List<Fit> getFits() {
		return fits;
	}

	/**
	 * Liefert die Liste aller angepassten Verteilungen in sortierter Reihenfolge.
	 * @return	Liste aller angepassten Verteilungen in sortierter Reihenfolge
	 */
	public List<Fit> getSortedFits() {
		return fits.stream().sorted((f1,f2)->(int)Math.signum(f1.fit-f2.fit)).collect(Collectors.toList());
	}

	/**
	 * Daten zu einer einzelnen angepassten Verteilung
	 */
	public static class Fit {
		/**
		 * Angepasste Verteilung
		 */
		public final AbstractRealDistribution distribution;

		/**
		 * Quadrierte mittlere Abweichung
		 */
		public final double fit;

		/**
		 * Informationen in Reintextform
		 */
		public final String infoPlain;

		/**
		 * Information als HTML-formatierter Text
		 */
		public final String infoHTML;

		/**
		 * Konstruktor der Klasse
		 * @param distribution	Angepasste Verteilung
		 * @param fit	Quadrierte mittlere Abweichung
		 * @param infoPlain	Informationen in Reintextform
		 * @param infoHTML	Information als HTML-formatierter Text
		 */
		private Fit(final AbstractRealDistribution distribution, final double fit, final String infoPlain, final String infoHTML) {
			this.distribution=distribution;
			this.fit=fit;
			this.infoPlain=infoPlain;
			this.infoHTML=infoHTML;
		}
	}
}