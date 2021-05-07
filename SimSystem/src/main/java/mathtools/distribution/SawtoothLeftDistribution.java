/**
 * Copyright 2021 Alexander Herzog
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

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Sägezahn-Verteilung (links)
 * @author Alexander Herzog
 */
public class SawtoothLeftDistribution extends AbstractSawtoothDistribution {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-3146918536990281629L;

	/**
	 * Konstruktor der Klasse
	 * @param a	Untere Grenze
	 * @param b	Obere Grenze
	 */
	public SawtoothLeftDistribution(final double a, final double b) {
		super(a,b);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public SawtoothLeftDistribution(AbstractSawtoothDistribution source) {
		super(source);
	}

	@Override
	public SawtoothLeftDistribution clone() {
		return new SawtoothLeftDistribution(a,b);
	}

	@Override
	protected double densityChecked(final double x) {
		return twoDivBMinusASquare*(b-x);
	}

	@Override
	protected double cumulativeProbabilityChecked(final double x) {
		return 1-(b-x)*(b-x)*oneDivBMinusASquare;
	}

	@Override
	public double getNumericalMean() {
		return (2*a+b)/3;
	}

	@Override
	public double inverseCumulativeProbability(final double p) {
		if (p<0) return -Double.MAX_VALUE;
		if (p>1) return Double.MAX_VALUE;
		return b-(b-a)*Math.sqrt(1-p);
	}

	@Override
	public double random(RandomGenerator generator) {
		final double p=generator.nextDouble();
		return b-(b-a)*Math.sqrt(1-p);
	}
}
