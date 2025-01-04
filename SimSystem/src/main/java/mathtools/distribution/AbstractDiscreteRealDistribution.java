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
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Basisklasse für die kontinuierliche Erweiterung diskreter Wahrscheinlichkeitsverteilungen
 * @author Alexander Herzog
 */
public abstract class AbstractDiscreteRealDistribution extends AbstractRealDistribution implements DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=7099769518111679373L;

	/**
	 * Maximalwert für die diskreten möglichen Werte für die Erzeugung von Zufallszahlen
	 * @see #random(RandomGenerator)
	 */
	public static int MAX_K=1_000_000;

	/**
	 * Konstruktor der Klasse
	 */
	public AbstractDiscreteRealDistribution() {
		super(null);
	}

	@Override
	public double density(double x) {
		return 0;
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<0) return 0;
		int k=(int)Math.floor(x);
		double sum=0;
		for (int i=0;i<=k;i++) {
			sum+=getCountDensity(i);
			if (sum>=1) break;
		}
		return sum;
	}

	@Override
	public double getSupportLowerBound() {
		return 0;
	}

	@Override
	public boolean isSupportLowerBoundInclusive() {
		return true;
	}

	@Override
	public boolean isSupportConnected() {
		return true;
	}

	@Override
	public double inverseCumulativeProbability(final double p) {
		if (p<0.0) return -Double.MAX_VALUE;
		if (p>1.0) return Double.MAX_VALUE;

		int k=-1;
		double pSum=0;
		while (pSum<p && k<MAX_K) {
			k++;
			final double density=getCountDensity(k);
			if (k>100 && density==0) return k;
			pSum+=density;
		}

		return k;
	}

	/**
	 * Zähldichtewert für einen bestimmten Wert
	 * @param k	Wert, für den die Zähldichte ermittelt werden soll
	 * @return	Zähldichte
	 */
	protected abstract double getCountDensity(final int k);

	@Override
	public double random(final RandomGenerator generator) {
		return inverseCumulativeProbability(generator.nextDouble());
	}
}
