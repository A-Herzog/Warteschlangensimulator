/**
 * Copyright 2024 Alexander Herzog
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
 * Kontinuierliche Darstellung der geometrischen Verteilung, siehe
 * <a href="https://de.wikipedia.org/wiki/Geometrische_Verteilung">https://de.wikipedia.org/wiki/Geometrische_Verteilung</a>
 * @author Alexander Herzog
 * @see AbstractDiscreteRealDistribution
 */
public class DiscreteGeometricDistributionImpl extends AbstractDiscreteRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=7321408413305781819L;

	/**
	 * Einrittswahrscheinlichkeit
	 */
	public final double p;

	/**
	 * Minimal zulässiger Wert für die Einrittswahrscheinlichkeit p
	 */
	private static double MINIMUM_P=0.0000001;

	/**
	 * Konstruktor
	 * @param p Einrittswahrscheinlichkeit
	 */
	public DiscreteGeometricDistributionImpl(final double p) {
		if (p<=0) {
			this.p=MINIMUM_P;
		} else {
			if (p>=1) this.p=1-MINIMUM_P; else this.p=p;
		}
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public DiscreteGeometricDistributionImpl(final DiscreteGeometricDistributionImpl source) {
		this((source==null)?1:source.p);
	}

	@Override
	protected double getCountDensity(int k) {
		return p*Math.pow(1-p,k);
	}

	@Override
	public DiscreteGeometricDistributionImpl clone() {
		return new DiscreteGeometricDistributionImpl(this);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<0) return 0;
		final int k=(int)Math.floor(x);
		return 1-Math.pow(1-p,k+1);
	}


	@Override
	public double getNumericalMean() {
		return (1-p)/p;
	}

	@Override
	public double getNumericalVariance() {
		return (1-p)/p/p;
	}

	/**
	 * Liefert die Schiefe der Verteilung.
	 * @return	Schiefe der Verteilung
	 */
	public double getSkewness() {
		return (2-p)/Math.sqrt(1-p);
	}

	/**
	 * Liefert den Modus der Verteilung.
	 * @return	Modus der Verteilung
	 */
	public double getMode() {
		return 0;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.MAX_VALUE;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return false;
	}

	@Override
	public double inverseCumulativeProbability(final double p) {
		if (p<0.0) return -Double.MAX_VALUE;
		if (p>1.0) return Double.MAX_VALUE;

		/* p=1-Math.pow(1-this.p,k+1) <=> k+1=log(1-p)/log(1-this.p) */
		return Math.ceil(Math.log(1-p)/Math.log(1-this.p)-1);
	}
}

