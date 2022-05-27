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
package mathtools.distribution;

import java.io.Serializable;

import parser.symbols.CalcSymbolPreOperatorBinomial;

/**
 * Kontinuierliche Darstellung der negativen Binomialverteilung, siehe
 * <a href="https://de.wikipedia.org/wiki/Negative_Binomialverteilung">https://de.wikipedia.org/wiki/Negative_Binomialverteilung</a>
 * @author Alexander Herzog
 * @see AbstractDiscreteRealDistribution
 */
public final class DiscreteNegativeBinomialDistributionImpl extends AbstractDiscreteRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5825764322259597097L;

	/**
	 * Einzel-Erfolgs-Wahrscheinlichkeit
	 */
	public final double p;

	/**
	 * Anzahl Erfolge bis zum Abbruch
	 */
	public final int r;

	/**
	 * Konstruktor der Klasse
	 * @param p	Einzel-Erfolgs-Wahrscheinlichkeit
	 * @param r	Anzahl Erfolge bis zum Abbruch
	 */
	public DiscreteNegativeBinomialDistributionImpl(final double p, final int r) {
		this.p=Math.max(Math.min(p,1),0);
		this.r=Math.max(1,r);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public DiscreteNegativeBinomialDistributionImpl(final DiscreteNegativeBinomialDistributionImpl source) {
		this((source==null)?0.5:source.p,(source==null)?1:source.r);
	}

	@Override
	protected double getCountDensity(int k) {
		try {
			return CalcSymbolPreOperatorBinomial.binomialCoefficient(k+r-1,k)*Math.pow(p,r)*Math.pow(1-p,k);
		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	public DiscreteNegativeBinomialDistributionImpl clone() {
		return new DiscreteNegativeBinomialDistributionImpl(this);
	}

	@Override
	public double getNumericalMean() {
		return r*(1-p)/p;
	}

	@Override
	public double getNumericalVariance() {
		return r*(1-p)/p/p;
	}

	/**
	 * Liefert die Schiefe der Verteilung.
	 * @return	Schiefe der Verteilung
	 */
	public double getSkewness() {
		return (2-p)/Math.sqrt(r*(1-p));
	}

	/**
	 * Liefert den Modus der Verteilung.
	 * @return	Modus der Verteilung
	 */
	public double getMode() {
		if (r<=1) return 0;
		return Math.floor(p*(r-1)/(1-p));
	}

	@Override
	public double getSupportUpperBound() {
		return Double.MAX_VALUE;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return false;
	}
}
