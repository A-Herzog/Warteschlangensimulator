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

import mathtools.Functions;

/**
 * Frechet-Verteilung
 * @author Alexander Herzog
 * @version 1.0
 */
public final class FrechetDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	private static final long serialVersionUID = -86971112941518298L;

	/**
	 * Lageparameter
	 */
	public final double delta;

	/**
	 * Skalierungsparameter
	 */
	public final double beta;

	/**
	 * Formparameter
	 */
	public final double alpha;

	/**
	 * Konstruktor der Klasse
	 * @param delta	Lageparameter
	 * @param beta	Skalierungsparameter
	 * @param alpha	Formparameter
	 */
	public FrechetDistributionImpl(final double delta, final double beta, final double alpha) {
		super(null);
		this.delta=delta;
		this.beta=(beta<=0)?0.0001:beta;
		this.alpha=(alpha<=0)?0.0001:alpha;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public FrechetDistributionImpl(final FrechetDistributionImpl source) {
		super(null);
		if (source==null) {
			delta=0;
			beta=1;
			alpha=1;
		} else {
			delta=source.delta;
			beta=source.beta;
			alpha=source.alpha;
		}
	}

	@Override
	public double density(double x) {
		if (x<=delta) return 0;
		final double z=(x-delta)/beta;
		return alpha*FastMath.exp(-FastMath.pow(z,-alpha))/(beta*FastMath.pow(z,alpha+1));
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=delta) return 0;
		final double z=(x-delta)/beta;
		//return FastMath.exp(-FastMath.pow(z,-alpha));
		return FastMath.exp(-1/FastMath.pow(z,alpha));
	}

	@Override
	public double getNumericalMean() {
		return delta+beta*Functions.getGamma(1-1/alpha);
	}

	@Override
	public double getNumericalVariance() {
		final double g1=Functions.getGamma(1-2/alpha);
		final double g2=Functions.getGamma(1-1/alpha);
		return beta*beta*(g1-g2*g2);
	}

	@Override
	public double getSupportLowerBound() {
		return delta;
	}

	@Override
	public double getSupportUpperBound() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isSupportLowerBoundInclusive() {
		return false;
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
	public FrechetDistributionImpl clone() {
		return new FrechetDistributionImpl(this);
	}

	@Override
	public double random(RandomGenerator generator) {
		final double p=generator.nextDouble();
		return FastMath.pow(-FastMath.log(p),-1/alpha)*beta+delta;
	}
}