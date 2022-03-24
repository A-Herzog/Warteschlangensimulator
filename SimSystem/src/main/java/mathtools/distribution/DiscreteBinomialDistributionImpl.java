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
 * Kontinuierliche Darstellung der Binomialverteilung, siehe
 * <a href="https://de.wikipedia.org/wiki/Binomialverteilung">https://de.wikipedia.org/wiki/Binomialverteilung</a>
 * @author Alexander Herzog
 * @see AbstractDiscreteRealDistribution
 */
public final class DiscreteBinomialDistributionImpl extends AbstractDiscreteRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1681747655102977334L;

	/**
	 * Wahrscheinlichkeit für eine rote Kugel
	 */
	public final double p;

	/**
	 * Anzahl der Kugeln, die der Urne entnommen werden
	 */
	public final int n;

	/**
	 * Konstruktor der Klasse
	 * @param p	Wahrscheinlichkeit für eine rote Kugel
	 * @param n	Anzahl der Kugeln, die der Urne entnommen werden
	 */
	public DiscreteBinomialDistributionImpl(final double p, final int n) {
		this.p=Math.max(Math.min(p,1),0);
		this.n=Math.max(1,n);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public DiscreteBinomialDistributionImpl(final DiscreteBinomialDistributionImpl source) {
		this((source==null)?0.5:source.p,(source==null)?1:source.n);
	}

	@Override
	protected double getCountDensity(int k) {
		try {
			return CalcSymbolPreOperatorBinomial.binomialCoefficient(n,k)*Math.pow(p,k)*Math.pow(1-p,n-k);
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public DiscreteBinomialDistributionImpl clone() {
		return new DiscreteBinomialDistributionImpl(this);
	}

	@Override
	public double getNumericalMean() {
		return n*p;
	}

	@Override
	public double getNumericalVariance() {
		return n*p*(1-p);
	}

	/**
	 * Liefert die Schiefe der Verteilung.
	 * @return	Schiefe der Verteilung
	 */
	public double getSkewness() {
		return (1-2*p)/Math.sqrt(n*p*(1-p));
	}

	@Override
	public double getSupportUpperBound() {
		return n;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return true;
	}
}
