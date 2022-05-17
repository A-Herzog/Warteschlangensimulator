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

/**
 * Kontinuierliche Darstellunge der diskreten Gleichverteilung, siehe
 * <a href="https://de.wikipedia.org/wiki/Diskrete_Gleichverteilung">https://de.wikipedia.org/wiki/Diskrete_Gleichverteilung</a>
 * @author Alexander Herzog
 * @see AbstractDiscreteRealDistribution
 */
public final class DiscreteUniformDistributionImpl extends AbstractDiscreteRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2820700465915524682L;

	/**
	 * Untere Grenze des Trägers
	 */
	public final int a;

	/**
	 * Obere Grenze des Trägers
	 */
	public final int b;

	/**
	 * Erwartungswert
	 */
	private final double mean;

	/**
	 * Varianz
	 */
	private final double var;

	/**
	 * Konstruktor der Klasse
	 * @param a	Untere Grenze des Trägers
	 * @param b	Obere Grenze des Trägers
	 */
	public DiscreteUniformDistributionImpl(final int a, final int b) {
		this.a=a;
		this.b=Math.max(a,b);
		mean=((double)(a+b)/2);
		var=(Math.pow(b-a+1,2)-1)/12;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public DiscreteUniformDistributionImpl(final DiscreteUniformDistributionImpl source) {
		this((source==null)?1:source.a,(source==null)?5:source.b);
	}

	@Override
	public DiscreteUniformDistributionImpl clone() {
		return new DiscreteUniformDistributionImpl(this);
	}

	@Override
	public double getNumericalMean() {
		return mean;
	}

	@Override
	public double getNumericalVariance() {
		return var;
	}

	/**
	 * Liefert die Schiefe der Verteilung.
	 * @return	Schiefe der Verteilung
	 */
	public double getSkewness() {
		return 0; /* Schiefe der diskreten Gleichverteilung ist immer 0. */
	}

	@Override
	public double getSupportLowerBound() {
		return a;
	}

	@Override
	public boolean isSupportLowerBoundInclusive() {
		return true;
	}

	@Override
	public double getSupportUpperBound() {
		return b;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return true;
	}

	@Override
	protected double getCountDensity(int k) {
		if (k<a || k>b) return 0;
		return 1.0/(b-a+1);
	}
}
