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
 * Kontinuierliche Darstellung der hypergeometrischen Verteilung, siehe
 * <a href="https://de.wikipedia.org/wiki/Hypergeometrische_Verteilung">https://de.wikipedia.org/wiki/Hypergeometrische_Verteilung</a>
 * @author Alexander Herzog
 * @see AbstractDiscreteRealDistribution
 */
public final class DiscreteHyperGeomDistributionImpl extends AbstractDiscreteRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-87706745468122704L;

	/**
	 * Gesamtanzahl an Kugeln in der Urne
	 */
	public final int N;

	/**
	 * Anzahl an roten Kugeln in der Urne
	 */
	public final int K;

	/**
	 * Anzahl der Kugeln, die der Urne entnommen werden
	 */
	public final int n;

	/**
	 * Konstruktor der Klasse
	 * @param N	Gesamtanzahl an Kugeln in der Urne
	 * @param K	Anzahl an roten Kugeln in der Urne
	 * @param n	Anzahl der Kugeln, die der Urne entnommen werden
	 */
	public DiscreteHyperGeomDistributionImpl(final int N, final int K, final int n) {
		this.N=Math.max(N,1);
		this.K=Math.max(0,Math.min(N,K));
		this.n=Math.max(1,Math.min(N,n));
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public DiscreteHyperGeomDistributionImpl(final DiscreteHyperGeomDistributionImpl source) {
		this((source==null)?1:source.N,(source==null)?1:source.K,(source==null)?1:source.n);
	}

	@Override
	protected double getCountDensity(int k) {
		try {
			return CalcSymbolPreOperatorBinomial.binomialCoefficient(K,k)*CalcSymbolPreOperatorBinomial.binomialCoefficient(N-K,n-k)/CalcSymbolPreOperatorBinomial.binomialCoefficient(N,n);
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public DiscreteHyperGeomDistributionImpl clone() {
		return new DiscreteHyperGeomDistributionImpl(this);
	}

	@Override
	public double getNumericalMean() {
		return n*((double)K)/N;
	}

	@Override
	public double getNumericalVariance() {
		return n*((double)K)/N*(1-((double)K)/N)*(N-n)/(N-1);
	}

	/**
	 * Liefert die Schiefe der Verteilung.
	 * @return	Schiefe der Verteilung
	 */
	public double getSkewness() {
		if (N<=2) return 0;
		return (N-2*K)*Math.sqrt(N-1)*(N-2*n)/Math.sqrt(n*K*(N-K)*(N-n))/(N-2);
	}

	@Override
	public double getSupportUpperBound() {
		return Math.min(K,n);
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return true;
	}
}
