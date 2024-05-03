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

import parser.symbols.CalcSymbolPreOperatorBinomial;

/**
 * Kontinuierliche Darstellung der negativen hypergeometrischen Verteilung, siehe
 * <a href="https://de.wikipedia.org/wiki/Negative_hypergeometrische_Verteilung">https://de.wikipedia.org/wiki/Negative_hypergeometrische_Verteilung</a>
 * @author Alexander Herzog
 * @see AbstractDiscreteRealDistribution
 */
public class DiscreteNegativeHyperGeomDistributionImpl extends AbstractDiscreteRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-6390431157370879106L;


	/**
	 * Gesamtanzahl an Kugeln in der Urne
	 */
	public final int N;

	/**
	 * Anzahl an roten Kugeln in der Urne
	 */
	public final int K;

	/**
	 * Anzahl der roten Kugeln, die ohne Zurücklegen gezogen werden sollen
	 */
	public final int n;

	/**
	 * Bereits im Konstruktor berechneter Nenner der Zähldichte
	 */
	private final double densityDenominator;

	/**
	 * Modus (wird initial auf -1 gesetzt und beim ersten Aufruf von {@link #getMode()} berechnet und dann hier gespeichert)
	 * @see #getMode()
	 */
	private int mode=-1;

	/**
	 * Konstruktor der Klasse
	 * @param N	Gesamtanzahl an Kugeln in der Urne
	 * @param K	Anzahl an roten Kugeln in der Urne
	 * @param n	Anzahl der Kugeln, die der Urne entnommen werden
	 */
	public DiscreteNegativeHyperGeomDistributionImpl(final int N, final int K, final int n) {
		this.N=Math.max(N,1);
		this.K=Math.max(0,Math.min(N,K));
		this.n=Math.max(1,Math.min(K,n));
		densityDenominator=CalcSymbolPreOperatorBinomial.binomialCoefficient(N,K);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public DiscreteNegativeHyperGeomDistributionImpl(final DiscreteNegativeHyperGeomDistributionImpl source) {
		this((source==null)?1:source.N,(source==null)?1:source.K,(source==null)?1:source.n);
	}

	@Override
	protected double getCountDensity(int k) {
		try {
			return CalcSymbolPreOperatorBinomial.binomialCoefficient(k-1,n-1)*CalcSymbolPreOperatorBinomial.binomialCoefficient(N-k,K-n)/densityDenominator;
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public DiscreteNegativeHyperGeomDistributionImpl clone() {
		return new DiscreteNegativeHyperGeomDistributionImpl(this);
	}

	@Override
	public double getNumericalMean() {
		return n*((double)N+1)/((double)K+1);
	}

	@Override
	public double getNumericalVariance() {
		return n*((double)K+1-n)*((double)N-K)*((double)N+1)/(((double)K+1)*((double)K+1)*((double)K+2));
	}

	/**
	 * Liefert den Modus der Verteilung.
	 * @return	Modus der Verteilung
	 */
	public double getMode() {
		if (mode>=0) return mode;

		int maxIndex=0;
		double maxValue=getCountDensity(0);

		int index=1;
		while (true) {
			final double value=getCountDensity(index);
			if (value>maxValue) {
				maxValue=value;
				maxIndex=index;
			} else if (value<maxValue) {
				return mode=maxIndex;
			}
			index++;
		}
	}

	@Override
	public double getSupportUpperBound() {
		return N;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return true;
	}


}
