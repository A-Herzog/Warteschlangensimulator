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

/**
 * Laplace-Verteilung
 * @author Alexander Herzog
 * @version 1.0
 */
public final class LaplaceDistributionImpl extends AbstractRealDistribution implements Serializable, Cloneable, DistributionWithRandom {
	private static final long serialVersionUID = -7872838730639994363L;

	/**
	 * Lageparameter mu<br>
	 * Schreibzugriff ist möglich
	 */
	public final double mu;

	/**
	 * Skalierungsparameter b (b&gt;0)<br>
	 * Schreibzugriff ist möglich
	 */
	public final double b;

	/**
	 * Konstruktor der Laplace-Verteilung
	 * @param mu	Lageparameter
	 * @param b	Skalierungsparameter
	 */
	public LaplaceDistributionImpl(final double mu, final double b) {
		super(null);
		this.mu=mu;
		this.b=(b<=0)?1:b;
	}

	@Override
	public double density(double x) {
		/* if (b<=0) return 0;  - per Konstruktor ausgeschlossen */
		/* https://en.wikipedia.org/wiki/Laplace_distribution */
		return 0.5/b*Math.exp(-Math.abs(x-mu)/b);
	}

	@Override
	public double cumulativeProbability(double x) {
		/* if (b<=0) return 0;  - per Konstruktor ausgeschlossen */
		/* https://en.wikipedia.org/wiki/Laplace_distribution */
		if (x<mu) {
			return 0.5*Math.exp((x-mu)/b);
		} else {
			return 1-0.5*Math.exp(-(x-mu)/b);
		}
	}

	@Override
	public double getNumericalMean() {
		return mu;
	}

	@Override
	public double getNumericalVariance() {
		return 2*b*b;
	}

	@Override
	public double getSupportLowerBound() {
		return -Double.MAX_VALUE;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.MAX_VALUE;
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
	public LaplaceDistributionImpl clone() {
		return new LaplaceDistributionImpl(mu,b);
	}

	@Override
	public final double random(final RandomGenerator generator) {
		/* https://en.wikipedia.org/wiki/Laplace_distribution */
		final double u=generator.nextDouble()-0.5;
		return mu-b*Math.signum(u)*Math.log(1-2*Math.abs(u));
	}
}