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

/**
 * Kontinuierliche Darstellung der Borel-Verteilung, siehe
 * <a href="https://en.wikipedia.org/wiki/Borel_distribution">https://en.wikipedia.org/wiki/Borel_distribution</a>
 * @author Alexander Herzog
 * @see AbstractDiscreteRealDistribution
 */
public final class DiscreteBorelDistributionImpl extends AbstractDiscreteRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=4768211128889252690L;

	/**
	 * Verteilungsparameter &mu;
	 */
	public final double mu;

	/**
	 * Cache für vorab berechnete Dichte-Werte
	 * @see #getCountDensity(int)
	 */
	private final double[] densityCache;

	/**
	 * Konstruktor der Klasse
	 * @param mu	Verteilungsparameter &mu;
	 */
	public DiscreteBorelDistributionImpl(final double mu) {
		this.mu=Math.max(0,Math.min(1,mu));

		densityCache=new double[100];
		for (int i=0;i<densityCache.length;i++) densityCache[i]=calcCountDensity(this.mu,i);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public DiscreteBorelDistributionImpl(final DiscreteBorelDistributionImpl source) {
		this((source==null)?1:source.mu);
	}

	/**
	 * Berechnet den Wert der Zähldichte ohne Nutzung des Caches.
	 * @param mu	Verteilungsparameter &mu;
	 * @param n	Stelle, an der p(n) berechnet werden soll
	 * @return	p(n)
	 */
	public static double calcCountDensity(final double mu, final int n) {
		if (n<=0) return 0;
		double fraction=Math.exp(-mu*n)/n;
		for (int i=1;i<=n-1;i++) {
			fraction*=(mu*n)/i;
		}
		return fraction;
	}

	@Override
	protected double getCountDensity(int k) {
		if (k<0 || k>750) return 0;
		if (k<densityCache.length) return densityCache[k];

		return calcCountDensity(this.mu,k);
	}

	@Override
	public DiscreteBorelDistributionImpl clone() {
		return new DiscreteBorelDistributionImpl(this);
	}

	@Override
	public double getNumericalMean() {
		if (mu==1) return Double.POSITIVE_INFINITY;
		return 1/(1-mu);
	}

	@Override
	public double getNumericalVariance() {
		if (mu==1) return Double.POSITIVE_INFINITY;
		return mu/Math.pow(1-mu,3);
	}

	@Override
	public double getSupportLowerBound() {
		return 1;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.MAX_VALUE;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return false;
	}

	/**
	 * Liefert den zu einem Erwartungswert gehörenden Verteilungsparameter &mu;
	 * @param mean	Erwartungswert
	 * @return	Verteilungsparameter &mu; oder <code>null</code>, wenn für den Erwartungswert kein Verteilungsparameter existiert
	 */
	public static Double getMuFromMean(final double mean) {
		if (mean<=1) return null;
		/* mean=1/(1-mu) <=> 1-(1/mean)=mu */
		return 1-1/mean;
	}
}
