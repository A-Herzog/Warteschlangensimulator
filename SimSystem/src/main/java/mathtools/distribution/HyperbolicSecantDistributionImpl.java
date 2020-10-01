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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

/**
 * Hyperbolische Sekanten-Verteilung
 * @author Alexander Herzog
 * @version 1.1
 */
public final class HyperbolicSecantDistributionImpl extends AbstractRealDistribution implements Cloneable, DistributionWithRandom {
	private static final long serialVersionUID = -4529594266413420045L;

	/**
	 * Parameter <code>mu</code> der Verteilung
	 */
	public final double mu;

	/**
	 * Quadrierte Fassung des Parameters <code>sigma</code> der Verteilung
	 */
	public final double sigma2;

	/**
	 * Parameter <code>sigma</code> der Verteilung
	 */
	public final double sigma;

	/**
	 * Konstruktor der Klasse
	 * @param mu	Parameter <code>mu</code> der Verteilung
	 * @param sigma	Parameter <code>sigma</code> der Verteilung
	 */
	public HyperbolicSecantDistributionImpl(final double mu, final double sigma) {
		super(null);
		this.mu=mu;
		this.sigma=(sigma<=0)?0.0001:sigma;
		sigma2=this.sigma*this.sigma;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public HyperbolicSecantDistributionImpl(final HyperbolicSecantDistributionImpl source) {
		super(null);
		if (source==null) {
			mu=0;
			sigma=1;
			sigma2=1;
		} else {
			mu=source.mu;
			sigma=source.sigma;
			sigma2=sigma*sigma;
		}
	}

	@Override
	public double density(double x) {
		final double z=(x-mu)/sigma;
		return (1.0 / (Math.cosh (Math.PI * z / 2.0) * 2.0 * sigma));
	}

	@Override
	public double cumulativeProbability(double x) {
		final double z=(x-mu)/sigma;
		final double arg=Math.PI*z*0.5;
		if (arg>=750) return 1;
		return (2.0 * Math.atan(FastMath.exp(arg))) / Math.PI;
	}

	@Override
	public double getNumericalMean() {
		return mu;
	}

	@Override
	public double getNumericalVariance() {
		return sigma2;
	}

	@Override
	public double getSupportLowerBound() {
		return Integer.MIN_VALUE;
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
	public HyperbolicSecantDistributionImpl clone() {
		return new HyperbolicSecantDistributionImpl(this);
	}

	private static final double twoDivPi=2.0/Math.PI;

	@Override
	public double random(RandomGenerator generator) {
		final double p=generator.nextDouble();
		return Math.log(Math.tan(p*Math.PI*0.5))*twoDivPi*sigma+mu;
	}
}