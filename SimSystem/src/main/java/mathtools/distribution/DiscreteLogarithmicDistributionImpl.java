/**
 * Copyright 2024 Alexander Herzog
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
package mathtools.distribution;

import java.io.Serializable;

/**
 * Kontinuierliche Darstellung der Logarithmischen Verteilung, siehe
 * <a href="https://de.wikipedia.org/wiki/Logarithmische_Verteilung">https://de.wikipedia.org/wiki/Logarithmische_Verteilung</a>
 * @author Alexander Herzog
 * @see AbstractDiscreteRealDistribution
 */
public class DiscreteLogarithmicDistributionImpl extends AbstractDiscreteRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-4181859133115570631L;

	/**
	 * Verteilungsparameter
	 */
	public final double p;

	/**
	 * Vorfaktor zur Berechnung von Werten der Dichte
	 */
	private final double densityFactor;

	/**
	 * Erwartungswert
	 * @see #getNumericalMean()
	 */
	private final double mean;

	/**
	 * Varianz
	 * @see #getNumericalVariance()
	 */
	private final double variance;

	/**
	 * Schiefe
	 * @see #getSkewness()
	 */
	private final double skewness;

	/**
	 * Konstruktor der Klasse
	 * @param p	Verteilungsparameter &lambda;
	 */
	public DiscreteLogarithmicDistributionImpl(final double p) {
		if (p<=0) {
			this.p=0.0001;
		} else {
			if (p>=1) this.p=0.9999; else this.p=p;
		}

		densityFactor=-1/Math.log(1-this.p);

		final double ln1p=Math.log(1-this.p);
		mean=-this.p/(1-this.p)/ln1p;
		variance=-this.p*(ln1p+this.p)/(1-this.p)/(1-this.p)/ln1p/ln1p;
		skewness=1/Math.sqrt(this.p*(-ln1p-this.p))*(ln1p*ln1p/(-ln1p-this.p)+this.p*(-ln1p-2*this.p));
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public DiscreteLogarithmicDistributionImpl(final DiscreteLogarithmicDistributionImpl source) {
		this((source==null)?0.5:source.p);
	}

	@Override
	protected double getCountDensity(int k) {
		if (k<=0) return 0;
		return densityFactor*Math.pow(p,k)/k;
	}

	@Override
	public double getNumericalMean() {
		return mean;
	}

	@Override
	public double getNumericalVariance() {
		return variance;
	}

	/**
	 * Liefert die Schiefe der Verteilung.
	 * @return	Schiefe der Verteilung
	 */
	public double getSkewness() {
		return skewness;
	}

	@Override
	public double getSupportLowerBound() {
		return 1;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.MAX_VALUE;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return false;
	}

	/**
	 * Ermittelt den Verteilungsparameter p basierend auf dem Erwartungswert
	 * @param mean	Erwartungswert
	 * @return	Verteilungsparameter p
	 */
	public static double getPFromMean(final double mean) {
		if (mean<=1.00006) return 0.0001;
		if (mean>1085) return 0.9999;

		double minP=0.001;
		double maxP=0.999;
		while (maxP-minP>0.0001) {
			final double p=(minP+maxP)/2;
			final double calcMean=-p/(1-p)/Math.log(1-p);
			if (calcMean>mean) {
				maxP=p;
			} else {
				minP=p;
			}
		}
		return (minP+maxP)/2;
	}
}
