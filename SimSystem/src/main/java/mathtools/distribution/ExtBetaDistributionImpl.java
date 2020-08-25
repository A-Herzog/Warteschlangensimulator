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

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;

/**
 * Die Klasse <code>ExtBetaDistributionImpl</code> stellt eine Erweiterung der Klasse
 * <code>BetaDistributionImpl</code> dar, bei der der Tr�ge der Dichte frei w�hlbar ist
 * und nicht auf das Intervall [0,1] festgelegt ist.
 * @author Alexander Herzog
 * @version 1.5
 * @see BetaDistribution
 */
public final class ExtBetaDistributionImpl extends BetaDistribution implements Cloneable, DistributionWithRandom {
	private static final long serialVersionUID = 787331141252463205L;

	/**
	 * Obere Grenze des Tr�gerbereichs der Wahrscheinlichkeitsmasse
	 */
	public final double domainLowerBound;

	/**
	 * Untere Grenze des Tr�gerbereichs der Wahrscheinlichkeitsmasse
	 */
	public final double domainUpperBound;

	private final double inverseRange;

	/**
	 * Konstruktor der Klasse <code>ExtBetaDistributionImpl</code>
	 * @param domainLowerBound	Untere Grenze des Tr�gers der Dichte
	 * @param domainUpperBound	Obere Grenze des Tr�gers der Dichte
	 * @param alpha	Verteilungsparameter alpha
	 * @param beta	Verteilungsparameter beta
	 */
	public ExtBetaDistributionImpl(final double domainLowerBound, final double domainUpperBound, final double alpha, final double beta) {
		super(null,alpha,beta,BetaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		this.domainLowerBound=domainLowerBound;
		this.domainUpperBound=domainUpperBound;
		inverseRange=1.0/(domainUpperBound-domainLowerBound);
	}

	/**
	 *
	 * Konstruktor der Klasse <code>ExtBetaDistributionImpl</code>
	 * @param domainLowerBound	Untere Grenze des Tr�gers der Dichte
	 * @param domainUpperBound	Obere Grenze des Tr�gers der Dichte
	 * @param alpha	Verteilungsparameter alpha
	 * @param beta	Verteilungsparameter beta
	 * @param inverseCumAccuracy	Genauigkeit f�r die Funktion <code>inverseCumulativeProbability</code>
	 */
	public ExtBetaDistributionImpl(final double domainLowerBound, final double domainUpperBound, final double alpha, final double beta, final double inverseCumAccuracy) {
		super(null,alpha,beta,inverseCumAccuracy);
		this.domainLowerBound=domainLowerBound;
		this.domainUpperBound=domainUpperBound;
		inverseRange=1.0/(domainUpperBound-domainLowerBound);
	}

	@Override
	public double density(final double x) {
		if ((x<domainLowerBound) || (x>domainUpperBound)) return 0;
		return super.density((x-domainLowerBound)*inverseRange);
	}

	@Override
	public double cumulativeProbability(final double x) {
		if (x<domainLowerBound) return 0;
		if (x>domainUpperBound) return 1;
		return super.cumulativeProbability((x-domainLowerBound)*inverseRange);
	}

	@Override
	public double getNumericalMean() {
		final double alpha=getAlpha();
		final double beta=getBeta();
		final double a=getSupportLowerBound();
		final double b=getSupportUpperBound();
		return alpha/(alpha+beta)*(b-a)+a;
	}

	@Override
	public double getNumericalVariance() {
		final double alpha=getAlpha();
		final double beta=getBeta();
		final double a=getSupportLowerBound();
		final double b=getSupportUpperBound();
		return Math.pow(a-b,2)*alpha*beta/Math.pow(alpha+beta,2)/(1+alpha+beta);
	}

	@Override
	public ExtBetaDistributionImpl clone() {
		return new ExtBetaDistributionImpl(domainLowerBound,domainUpperBound,getAlpha(),getBeta());
	}

	@Override
	public double getSupportLowerBound() {return domainLowerBound;}

	@Override
	public double getSupportUpperBound() {return domainUpperBound;}

	@Override
	public boolean isSupportLowerBoundInclusive() {return true;}

	@Override
	public boolean isSupportUpperBoundInclusive() {return true;}

	@Override
	public double random(RandomGenerator generator) {
		return sampleBeta(generator,getAlpha(),getBeta())*(domainUpperBound-domainLowerBound)+domainLowerBound;
	}

	/*
	 * �bernommen aus {@link BetaDistribution}.
	 * Dort leider private, so dass nicht direkt auf die Klasse
	 * zugegriffen werden kann.
	 * @see BetaDistribution#ChengBetaSampler
	 */

	/**
	 * Returns one sample using Cheng's sampling algorithm.
	 * @param random random generator to use
	 * @param alpha distribution first shape parameter
	 * @param beta distribution second shape parameter
	 * @return sampled value
	 */
	static double sampleBeta(RandomGenerator random, final double alpha, final double beta) {
		final double a = FastMath.min(alpha, beta);
		final double b = FastMath.max(alpha, beta);

		if (a > 1) {
			return algorithmBB(random, alpha, a, b);
		} else {
			return algorithmBC(random, alpha, b, a);
		}
	}

	/**
	 * Returns one sample using Cheng's BB algorithm, when both &alpha; and &beta; are greater than 1.
	 * @param random random generator to use
	 * @param a0 distribution first shape parameter (&alpha;)
	 * @param a min(&alpha;, &beta;) where &alpha;, &beta; are the two distribution shape parameters
	 * @param b max(&alpha;, &beta;) where &alpha;, &beta; are the two distribution shape parameters
	 * @return sampled value
	 */
	private static double algorithmBB(RandomGenerator random,
			final double a0,
			final double a,
			final double b) {
		final double alpha = a + b;
		final double beta = FastMath.sqrt((alpha - 2.) / (2. * a * b - alpha));
		final double gamma = a + 1. / beta;

		double r;
		double w;
		double t;
		do {
			final double u1 = random.nextDouble();
			final double u2 = random.nextDouble();
			final double v = beta * (FastMath.log(u1) - FastMath.log1p(-u1));
			w = a * FastMath.exp(v);
			final double z = u1 * u1 * u2;
			r = gamma * v - 1.3862944;
			final double s = a + r - w;
			if (s + 2.609438 >= 5 * z) {
				break;
			}

			t = FastMath.log(z);
			if (s >= t) {
				break;
			}
		} while (r + alpha * (FastMath.log(alpha) - FastMath.log(b + w)) < t);

		w = FastMath.min(w, Double.MAX_VALUE);
		return Precision.equals(a, a0) ? w / (b + w) : b / (b + w);
	}

	/**
	 * Returns one sample using Cheng's BC algorithm, when at least one of &alpha; and &beta; is smaller than 1.
	 * @param random random generator to use
	 * @param a0 distribution first shape parameter (&alpha;)
	 * @param a max(&alpha;, &beta;) where &alpha;, &beta; are the two distribution shape parameters
	 * @param b min(&alpha;, &beta;) where &alpha;, &beta; are the two distribution shape parameters
	 * @return sampled value
	 */
	private static double algorithmBC(RandomGenerator random,
			final double a0,
			final double a,
			final double b) {
		final double alpha = a + b;
		final double beta = 1. / b;
		final double delta = 1. + a - b;
		final double k1 = delta * (0.0138889 + 0.0416667 * b) / (a * beta - 0.777778);
		final double k2 = 0.25 + (0.5 + 0.25 / delta) * b;

		double w;
		for (;;) {
			final double u1 = random.nextDouble();
			final double u2 = random.nextDouble();
			final double y = u1 * u2;
			final double z = u1 * y;
			if (u1 < 0.5) {
				if (0.25 * u2 + z - y >= k1) {
					continue;
				}
			} else {
				if (z <= 0.25) {
					final double v = beta * (FastMath.log(u1) - FastMath.log1p(-u1));
					w = a * FastMath.exp(v);
					break;
				}

				if (z >= k2) {
					continue;
				}
			}

			final double v = beta * (FastMath.log(u1) - FastMath.log1p(-u1));
			w = a * FastMath.exp(v);
			if (alpha * (FastMath.log(alpha) - FastMath.log(b + w) + v) - 1.3862944 >= FastMath.log(z)) {
				break;
			}
		}

		w = FastMath.min(w, Double.MAX_VALUE);
		return Precision.equals(a, a0) ? w / (b + w) : b / (b + w);
	}
}
