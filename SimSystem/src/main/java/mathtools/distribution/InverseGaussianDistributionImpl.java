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
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

/**
 * Inverse Gauß-Verteilung
 * @author Alexander Herzog
 * @version 1.1
 */
public final class InverseGaussianDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2495954212542594178L;

	/**
	 * Parameter lambda (lambda&gt;0)<br>
	 * Schreibzugriff ist möglich
	 */
	public final double lambda;

	/**
	 * Parameter mu (mu&gt;0)<br>
	 * Schreibzugriff ist möglich
	 */
	public final double mu;

	/**
	 * Vorberechneter Ausdruck zur Beschleunigung der Pseudozufallszahlen-Erzeugung.
	 * @see #random(RandomGenerator)
	 */
	private final double inverse2Lambda;

	/**
	 * Standardnormalverteilung, die verwendet wird, um Verteilungswerte
	 * zu berechnen.
	 * @see #cumulativeProbability(double)
	 */
	private static final NormalDistribution stdNormal=new NormalDistribution(null,0.0,1.0,NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

	/**
	 * Konstruktor der Klasse
	 * @param lambda	Parameter lambda&gt;0
	 * @param mu	Parameter mu&gt;0
	 * @see #lambda
	 * @see #mu
	 */
	public InverseGaussianDistributionImpl(final double lambda, final double mu) {
		super(null);
		this.lambda=(lambda<=0)?1:lambda;
		this.mu=(mu<=0)?1:mu;
		inverse2Lambda=1.0/(2.0*lambda);
	}

	@Override
	public double density(double x) {
		/* if (lambda<=0 || mu<=0) return 0; - per Konstruktor ausgeschlossen */
		/* https://en.wikipedia.org/wiki/Inverse_Gaussian_distribution */
		if (x<=0) return 0;
		return Math.sqrt(lambda/2/Math.PI/x/x/x)*FastMath.exp(-lambda*(x-mu)*(x-mu)/2/mu/mu/x);
	}

	@Override
	public double cumulativeProbability(double x) {
		/* if (lambda<=0 || mu<=0) return 0; - per Konstruktor ausgeschlossen */
		/* https://en.wikipedia.org/wiki/Inverse_Gaussian_distribution */
		if (x<=0) return 0;
		return stdNormal.cumulativeProbability(Math.sqrt(lambda/x)*(x/mu-1))+FastMath.exp(2*lambda/mu)*stdNormal.cumulativeProbability(-Math.sqrt(lambda/x)*(x/mu+1));
	}

	@Override
	public double getNumericalMean() {
		return mu;
	}

	@Override
	public double getNumericalVariance() {
		/* if (lambda<=0) return 0; - per Konstruktor ausgeschlossen */
		return mu*mu*mu/lambda;
	}

	/**
	 * Liefert die Schiefe der Verteilung.
	 * @return	Schiefe der Verteilung
	 */
	public double getSkewness() {
		/* if (lambda<=0) return 0; - per Konstruktor ausgeschlossen */
		return 3*Math.sqrt(mu/lambda);
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
	public InverseGaussianDistributionImpl clone() {
		return new InverseGaussianDistributionImpl(lambda,mu);
	}

	@Override
	public double random(final RandomGenerator generator) {
		/* if (lambda<=0 || mu<=0) return 0; - per Konstruktor ausgeschlossen */
		/* https://en.wikipedia.org/wiki/Inverse_Gaussian_distribution */
		final double v=generator.nextGaussian();
		final double y=v*v;
		final double x=mu + (mu*mu*y)*inverse2Lambda - (mu*inverse2Lambda) * Math.sqrt(4*mu*lambda*y + mu*mu*y*y);
		final double test=generator.nextDouble();
		if (test<=mu/(mu+x)) return x; else return (mu*mu)/x;
	}
}