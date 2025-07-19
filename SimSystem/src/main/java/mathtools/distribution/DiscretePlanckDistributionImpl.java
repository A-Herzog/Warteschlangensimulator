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
import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

/**
 * Kontinuierliche Darstellung der Plank-Verteilung, siehe
 * <a href="https://docs.scipy.org/doc/scipy/reference/generated/scipy.stats.planck.html#scipy.stats.planck">https://docs.scipy.org/doc/scipy/reference/generated/scipy.stats.planck.html#scipy.stats.planck</a>
 * @author Alexander Herzog
 * @see AbstractDiscreteRealDistribution
 */
public final class DiscretePlanckDistributionImpl extends AbstractDiscreteRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=2069626348326299153L;

	/**
	 * Verteilungsparameter &lambda;
	 */
	public final double lambda;

	/**
	 * Vorabbrechneter Faktor zur schnelleren Berechnung von Werten der Dichte
	 * @see #density(double)
	 */
	private final double densityFactor;

	/**
	 * Cache für vorab berechnete Dichte-Werte
	 * @see #getCountDensity(int)
	 */
	private final double[] densityCache;

	/**
	 * Konstruktor der Klasse
	 * @param lambda	Verteilungsparameter &lambda;
	 */
	public DiscretePlanckDistributionImpl(final double lambda) {
		if (lambda<=0) this.lambda=0.001; else this.lambda=lambda;

		densityFactor=1-FastMath.exp(-this.lambda);

		final double mean=1.0/(FastMath.exp(this.lambda)-1.0);
		densityCache=new double[Math.min(2000,(int)Math.ceil(mean*5))];
		Arrays.fill(densityCache,-1);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public DiscretePlanckDistributionImpl(final DiscretePlanckDistributionImpl source) {
		this((source==null)?1:source.lambda);
	}

	@Override
	protected double getCountDensity(int k) {
		if (k<0) return 0;
		if (k<densityCache.length) {
			if (densityCache[k]<0) densityCache[k]=densityFactor*FastMath.exp(-lambda*k);
			return densityCache[k];
		}

		return densityFactor*FastMath.exp(-lambda*k);
	}

	@Override
	public DiscretePlanckDistributionImpl clone() {
		return new DiscretePlanckDistributionImpl(this);
	}


	@Override
	public double getNumericalMean() {
		return 1.0/(FastMath.exp(lambda)-1.0);
	}

	@Override
	public double getNumericalVariance() {
		return FastMath.exp(-lambda)/Math.pow(FastMath.exp(-lambda)-1,2);
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
