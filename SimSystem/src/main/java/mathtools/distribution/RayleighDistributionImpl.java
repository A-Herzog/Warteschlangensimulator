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
package mathtools.distribution;

import java.io.Serializable;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

/**
 * Klasse zur Abbildung der Rayleigh-Verteilung
 * @author Alexander Herzog
 * @version 1.0
 * @see <a href="https://en.wikipedia.org/wiki/Rayleigh_distribution">https://en.wikipedia.org/wiki/Rayleigh_distribution</a>
 */
public final class RayleighDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7748498224666110097L;

	/**
	 * Parameter sigma der Rayleigh-Verteilung
	 */
	public final double sigma;

	/**
	 * Quadrierter Wert des Parameters sigma der Rayleigh-Verteilung
	 */
	private final double sigma2;

	/**
	 * Erwartungswert der Rayleigh-Verteilung
	 */
	public final double mean;

	/**
	 * Standardabweichung der Rayleigh-Verteilung
	 */
	public final double sd;

	/**
	 * Konstruktor der Klasse
	 * @param mean	Erwartungswert
	 */
	public RayleighDistributionImpl(final double mean) {
		super(null);
		sigma=(mean<=0)?0.0001:Math.sqrt(2/Math.PI)*mean;
		sigma2=sigma*sigma;
		this.mean=mean;
		sd=Math.sqrt((4-Math.PI)/2)*sigma;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public RayleighDistributionImpl(final RayleighDistributionImpl source) {
		this((source==null)?1:source.mean);
	}

	@Override
	public double density(double x) {
		if (x<0) return 0;
		return x/sigma2*FastMath.exp(-x*x/2/sigma2);
	}

	@Override
	public double cumulativeProbability(double x) {
		return 1-FastMath.exp(-x*x/2/sigma2);
	}

	@Override
	public double getNumericalMean() {
		return mean;
	}

	@Override
	public double getNumericalVariance() {
		return sd*sd;
	}

	@Override
	public double getSupportLowerBound() {
		return 0;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.MAX_VALUE;
	}

	@Override
	public boolean isSupportLowerBoundInclusive() {
		return true;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return false;
	}

	@Override
	public boolean isSupportConnected() {
		return true;
	}

	@Override
	public double inverseCumulativeProbability(final double p) {
		if (p<0) return -Double.MAX_VALUE;
		if (p>1) return Double.MAX_VALUE;
		return sigma*FastMath.sqrt(-2*Math.log(1-p));
	}

	@Override
	public double random(final RandomGenerator generator) {
		/* https://en.wikipedia.org/wiki/Rayleigh_distribution */
		return sigma*FastMath.sqrt(-2*Math.log(generator.nextDouble()));
	}

	@Override
	public RayleighDistributionImpl clone() {
		return new RayleighDistributionImpl(mean);
	}
}
