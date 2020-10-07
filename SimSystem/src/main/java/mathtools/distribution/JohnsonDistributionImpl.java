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
 * Implementierung der Johnson-SU-Verteilung,<br>siehe
 * <a href="https://en.wikipedia.org/wiki/Johnson%27s_SU-distribution">https://en.wikipedia.org/wiki/Johnson%27s_SU-distribution</a>
 * @author Alexander Herzog
 * @see AbstractRealDistribution
 */
public final class JohnsonDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7404228683794460123L;

	/**
	 * Parameter <b>gamma</b> der Verteilung (gamma&gt;0)
	 */
	public double gamma;

	/**
	 * Parameter <b>xi</b> der Verteilung (xi&gt;0)
	 */
	public double xi;

	/**
	 * Parameter <b>delta</b> der Verteilung (delta&gt;0)
	 */
	public double delta;

	/**
	 * Parameter <b>lambda</b> der Verteilung (lambda&gt;0)
	 */
	public double lambda;

	/**
	 * Kehrwert des Parameters {@link #delta}.<br>
	 * Wird für Verteilungsfunktion und Kenngrößen vorberechnet.
	 * @see #cumulativeProbability(double)
	 * @see #getNumericalMean()
	 * @see #getNumericalVariance()
	 */
	private final double inverseDelta;

	/**
	 * Standardnormalverteilung, die verwendet wird, um Verteilungswerte
	 * zu berechnen.
	 * @see #cumulativeProbability(double)
	 * @see #inverseCumulativeProbability(double)
	 */
	private static final NormalDistribution stdNormal=new NormalDistribution(null,0.0,1.0,NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

	/**
	 * Konstruktor der Johnson-SU-Verteilung
	 * @param gamma	Verteilungsparameter gamma (es muss gamma&gt;0 sein)
	 * @param xi	Verteilungsparameter xi (es muss xi&gt;0 sein)
	 * @param delta	Verteilungsparameter delta (es muss delta&gt;0 sein)
	 * @param lambda	Verteilungsparameter lambda (es muss lambda&gt;0 sein)
	 */
	public JohnsonDistributionImpl(final double gamma, final double xi, final double delta, final double lambda) {
		super(null);
		this.gamma=Math.max(gamma,0.0001);
		this.xi=Math.max(xi,0.0001);
		this.delta=Math.max(delta,0.0001);
		this.lambda=Math.max(lambda,0.0001);
		inverseDelta=1/delta;
	}

	@Override
	public double density(double x) {
		final double frac=(x-xi)/lambda;
		final double exponentPart=gamma+delta*FastMath.asinh(frac);

		return delta/(lambda*Math.sqrt(2*FastMath.PI))*1/Math.sqrt(1+frac*frac)*FastMath.exp(-0.5*exponentPart*exponentPart);
	}

	@Override
	public double cumulativeProbability(double x) {
		return stdNormal.cumulativeProbability(gamma+delta*FastMath.asinh((x-xi)/lambda));
	}

	@Override
	public double inverseCumulativeProbability(final double p) {
		final double val=stdNormal.inverseCumulativeProbability(p);
		return FastMath.sinh((val-gamma)*inverseDelta)*lambda+xi;
	}

	@Override
	public JohnsonDistributionImpl clone() {
		return new JohnsonDistributionImpl(gamma,xi,delta,lambda);
	}

	@Override
	public double getNumericalMean() {
		return xi-lambda*FastMath.exp(1/(2*delta*delta))*FastMath.sinh(gamma*inverseDelta);
	}

	@Override
	public double getNumericalVariance() {
		final double ex=FastMath.exp(1/delta/delta);
		return lambda*lambda/2*(ex-1)*(ex*FastMath.cosh(2*gamma*inverseDelta)+1);
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
	public double random(final RandomGenerator generator) {
		final double u=generator.nextDouble();
		return lambda*FastMath.sinh((stdNormal.inverseCumulativeProbability(u)-gamma)*inverseDelta)+xi;
	}
}
