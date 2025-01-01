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
 * Klasse zur Abbildung der Log-Cauchy-Verteilung
 * @author Alexander Herzog
 */
public class LogCauchyDistributionImpl extends AbstractRealDistribution implements Cloneable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=536877804339830030L;

	/**
	 * Verteilungsparameter &mu;
	 */
	public final double mu;

	/**
	 * Verteilungsparameter &sigma;&gt;0
	 */
	public final double sigma;

	/**
	 * Vorabbrechneter Wert zur Beschleunigung der Berechnung
	 * der Dichte und der Verteilungsfunktion
	 * @see #density(double)
	 * @see #cumulativeProbability(double)
	 */
	private static final double invPi=1/Math.PI;

	/**
	 * Konstruktor
	 * @param mu	Verteilungsparameter &mu;
	 * @param sigma	Verteilungsparameter &sigma;&gt;0
	 */
	public LogCauchyDistributionImpl(final double mu, final double sigma) {
		super(null);
		this.mu=mu;
		this.sigma=Math.max(sigma,0.0001);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public LogCauchyDistributionImpl(final LogCauchyDistributionImpl source) {
		this((source==null)?4:source.mu,(source==null)?0.5:source.sigma);
	}

	@Override
	public double density(double x) {
		if (x<=0) return 0;
		return invPi/x*sigma/(Math.pow(Math.log(x)-mu,2)+sigma*sigma);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=0) return 0;
		return invPi*Math.atan((Math.log(x)-mu)/sigma)+0.5;
	}

	@Override
	public double getNumericalMean() {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public double getNumericalVariance() {
		return Double.POSITIVE_INFINITY;
	}

	/**
	 * Liefert den Median der Verteilung.
	 * @return Median der Verteilung
	 */
	public double getMedian() {
		return Math.exp(mu);
	}

	@Override
	public double getSupportLowerBound() {
		return 0;
	}

	@Override
	public boolean isSupportLowerBoundInclusive() {
		return false;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.POSITIVE_INFINITY;
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
	public LogCauchyDistributionImpl clone() {
		return new LogCauchyDistributionImpl(mu,sigma);
	}

	@Override
	public double random(RandomGenerator generator) {
		final double u=generator.nextDouble();
		/*
		p=1/pi*arctan((log(x)-mu)/sigma)+0.5
		<=> tan((p-0.5)*pi)=(log(x)-mu)/sigma
		<=> exp(tan((p-0.5)*pi)*sigma+mu)=x
		 */
		return Math.exp(Math.tan((u-0.5)*Math.PI)*sigma+mu);
	}
}
