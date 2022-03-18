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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Klasse zur Abbildung der Levy-Verteilung
 * @author Alexander Herzog
 */
public class LevyDistribution extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-7353088128842568386L;

	/**
	 * Lageparameter
	 */
	public final double mu;

	/**
	 * Formparameter
	 */
	public final double c;

	/**
	 * Vorberechneter Vorfaktor für die Berechnung der Dichte
	 * @see #density(double)
	 */
	private final double densityFactor;

	/**
	 * Standardnormalverteilung, die verwendet wird, um Verteilungswerte
	 * zu berechnen.
	 * @see #cumulativeProbability(double)
	 * @see #inverseCumulativeProbability(double)
	 */
	private static final NormalDistribution stdNormal=new NormalDistribution(null,0.0,1.0,NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

	/**
	 * Konstruktor der Klasse
	 * @param mu	Lageparameter
	 * @param c	Formparameter
	 */
	public LevyDistribution(final double mu, final double c) {
		super(null);
		this.mu=mu;
		this.c=Math.max(0.0001,c);
		densityFactor=Math.sqrt(this.c/2/Math.PI);
	}

	@Override
	public double density(double x) {
		if (x<mu) return 0;
		return densityFactor*Math.exp(-c/2/(x-mu))/Math.pow(x-mu,1.5);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<mu) return 0;
		return 2-2*stdNormal.cumulativeProbability(Math.sqrt(c/(x-mu)));
	}

	@Override
	public LevyDistribution clone() {
		return new LevyDistribution(mu,c);
	}

	@Override
	public double getNumericalMean() {
		return Double.MAX_VALUE;
	}

	@Override
	public double getNumericalVariance() {
		return Double.MAX_VALUE;
	}

	@Override
	public double getSupportLowerBound() {
		return mu;
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
	public double inverseCumulativeProbability(final double p) throws OutOfRangeException {
		if (p<0.0 || p>1.0) throw new OutOfRangeException(p,0,1);

		if (p==0.0) return getSupportLowerBound();
		if (p==1.0) return getSupportUpperBound();

		return mu+c/Math.pow(stdNormal.inverseCumulativeProbability((2-p)/2),2);
	}

	@Override
	public double random(final RandomGenerator generator) {
		return inverseCumulativeProbability(generator.nextDouble());
	}
}
