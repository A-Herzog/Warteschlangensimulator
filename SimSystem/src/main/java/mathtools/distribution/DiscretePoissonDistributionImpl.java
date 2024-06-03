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

import org.apache.commons.math3.util.FastMath;

/**
 * Kontinuierliche Darstellung der Poissonverteilung, siehe
 * <a href="https://de.wikipedia.org/wiki/Poisson-Verteilung">https://de.wikipedia.org/wiki/Poisson-Verteilung</a>
 * @author Alexander Herzog
 * @see AbstractDiscreteRealDistribution
 */
public final class DiscretePoissonDistributionImpl extends AbstractDiscreteRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=996268245370913674L;

	/**
	 * Verteilungsparameter &lambda;
	 */
	public final double lambda;

	/**
	 * Cache für vorab berechnete Dichte-Werte
	 * @see #getCountDensity(int)
	 */
	private final double[] densityCache;

	/**
	 * Berechnet lambda^k/k! (und vermeidet dabei Auslöschungen bei großen Werten k)
	 * @param k	Wert k
	 * @return	lambda^k/k!
	 */
	private double powerFactorial(final int k) {
		/* FastMath.pow(lambda,k)/Functions.getFactorial(k) */
		double d=1;
		for (int i=1;i<=k;i++) d*=lambda/i;
		return d;
	}

	/**
	 * Konstruktor der Klasse
	 * @param lambda	Verteilungsparameter &lambda;
	 */
	public DiscretePoissonDistributionImpl(final double lambda) {
		if (lambda<=0) this.lambda=0.001; else this.lambda=lambda;

		densityCache=new double[Math.min(2000,2*(int)Math.ceil(this.lambda))];
		for (int k=0;k<densityCache.length;k++) densityCache[k]=powerFactorial(k)*FastMath.exp(-this.lambda);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public DiscretePoissonDistributionImpl(final DiscretePoissonDistributionImpl source) {
		this((source==null)?1:source.lambda);
	}

	@Override
	protected double getCountDensity(int k) {
		if (k<0) return 0;
		if (k<densityCache.length) return densityCache[k];

		return powerFactorial(k)*FastMath.exp(-this.lambda)*FastMath.exp(-lambda);
	}

	@Override
	public DiscretePoissonDistributionImpl clone() {
		return new DiscretePoissonDistributionImpl(this);
	}

	@Override
	public double getNumericalMean() {
		return lambda;
	}

	@Override
	public double getNumericalVariance() {
		return lambda;
	}

	/**
	 * Liefert die Schiefe der Verteilung.
	 * @return	Schiefe der Verteilung
	 */
	public double getSkewness() {
		return 1/Math.sqrt(lambda);
	}

	/**
	 * Liefert den Modus der Verteilung.
	 * @return	Modus der Verteilung
	 */
	public double getMode() {
		final int x1=(int)Math.round(Math.ceil(lambda)-1);
		final int x2=(int)Math.round(Math.floor(lambda));
		final double d1=getCountDensity(x1);
		final double d2=getCountDensity(x2);
		return (d1>d2)?x1:x2;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.MAX_VALUE;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return false;
	}
}
