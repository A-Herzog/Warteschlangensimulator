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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Halbe Caucy-Verteilung
 * @author Alexander Herzog
 */
public class HalfCauchyDistribution extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=4248067752819138596L;

	/**
	 * Lageparameter &mu;
	 */
	public final double mu;

	/**
	 * Skalierungsparameter &sigma;
	 */
	public final double sigma;

	/**
	 * Vorab berechneter Wert zur schnelleren Berechnung von Dichte-Werten
	 * @see #density(double)
	 */
	private final double densityFactor;

	/**
	 * Vorab berechneter Wert zur schnelleren Berechnung von Werten der Verteilungsfunktion
	 * @see #cumulativeProbability(double)
	 */
	private static final double cumulativeFactor=2/Math.PI;

	/**
	 * Weiterer vorab berechneter Wert zur schnelleren Berechnung von Werten der Verteilungsfunktion
	 * @see #cumulativeProbability(double)
	 */
	private final double inverseSigmaSqr;

	/**
	 * Vorab berechneter Wert zur schnelleren Berechnung von Pseudozufallszahlen
	 * @see #random(RandomGenerator)
	 */
	private static final double randomFactor=Math.PI/2;

	/**
	 * Konstruktor
	 * @param mu	Lageparameter &mu;
	 * @param sigma	Skalierungsparameter &sigma;
	 */
	public HalfCauchyDistribution(final double mu, final double sigma) {
		super(null);
		this.mu=mu;
		this.sigma=Math.max(sigma,0.0001);
		densityFactor=2/Math.PI/this.sigma;
		inverseSigmaSqr=1/this.sigma/this.sigma;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public HalfCauchyDistribution(final HalfCauchyDistribution source) {
		this((source==null)?0:source.mu,(source==null)?1:source.sigma);
	}

	@Override
	public double density(double x) {
		if (x<mu) return 0;
		return densityFactor/(1+(x-mu)*(x-mu)*inverseSigmaSqr);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<mu) return 0;
		return cumulativeFactor*Math.atan((x-mu)/sigma);
	}

	@Override
	public double getNumericalMean() {
		return Double.NaN;
	}

	@Override
	public double getNumericalVariance() {
		return Double.NaN;
	}

	@Override
	public double getSupportLowerBound() {
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.POSITIVE_INFINITY;
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
	public HalfCauchyDistribution clone() {
		return new HalfCauchyDistribution(this);
	}

	@Override
	public double random(RandomGenerator generator) {
		/*
		 y=2/pi*arctan((x-mu)/sigma)
		 tan(pi/2*y)=(x-mu)/sigma
		 sigma*tan(pi/2*y)+mu=x
		 */
		return sigma*Math.tan(randomFactor*generator.nextDouble())+mu;
	}
}
