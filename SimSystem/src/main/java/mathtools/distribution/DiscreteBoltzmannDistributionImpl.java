/**
 * Copyright 2025 Alexander Herzog
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
 * Kontinuierliche Darstellung der Boltzmann-Verteilung
 * @author Alexander Herzog
 * @see AbstractDiscreteRealDistribution
 */
public final class DiscreteBoltzmannDistributionImpl extends AbstractDiscreteRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-827465333857497087L;

	/**
	 * Parameter &lambda&gt;0
	 */
	public final double lambda;

	/**
	 * Parameter N&gt;0, natürliche Zahl
	 */
	public final int N;

	/**
	 * Vorabberechneter Faktor in der Formel für die Dichte
	 * @see #getCountDensity(int)
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
	 * Konstruktor der Klasse
	 * @param lambda	Parameter &lambda&gt;0
	 * @param N	Parameter N&gt;0, natürliche Zahl
	 */
	public DiscreteBoltzmannDistributionImpl(final double lambda, final int N) {
		this.lambda=Math.max(lambda,0.00001);
		this.N=Math.max(1,N);

		densityFactor=(1-Math.exp(-this.lambda))/(1-Math.exp(-this.lambda*this.N));

		final double z=Math.exp(-this.lambda);
		final double zN=Math.exp(-this.lambda*this.N);
		mean=z/(1.0-z)-N*zN/(1.0-zN);
		variance=z/(1.0-z)/(1.0-z)-N*N*zN/(1.0-zN)/(1.0-zN);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public DiscreteBoltzmannDistributionImpl(final DiscreteBoltzmannDistributionImpl source) {
		this((source==null)?0.5:source.lambda,(source==null)?20:source.N);
	}

	@Override
	protected double getCountDensity(int k) {
		if (k<0 || k>=N) return 0;
		return densityFactor*Math.exp(-lambda*k);
	}

	@Override
	public DiscreteBoltzmannDistributionImpl clone() {
		return new DiscreteBoltzmannDistributionImpl(this);
	}

	@Override
	public double getNumericalMean() {
		return mean;
	}

	@Override
	public double getNumericalVariance() {
		return variance;
	}

	@Override
	public double getSupportUpperBound() {
		return N-1;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return true;
	}
}
