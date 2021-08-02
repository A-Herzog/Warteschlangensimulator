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
 * Sägezahn-Verteilung (rechts)
 * @author Alexander Herzog
 */
public class SawtoothRightDistribution extends AbstractSawtoothDistribution {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-8726326629109427594L;

	/**
	 * Konstruktor der Klasse
	 * @param a	Untere Grenze
	 * @param b	Obere Grenze
	 */
	public SawtoothRightDistribution(final double a, final double b) {
		super(a,b);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public SawtoothRightDistribution(AbstractSawtoothDistribution source) {
		super(source);
	}

	@Override
	public SawtoothRightDistribution clone() {
		return new SawtoothRightDistribution(a,b);
	}

	@Override
	protected double densityChecked(final double x) {
		return twoDivBMinusASquare*(x-a);
	}

	@Override
	protected double cumulativeProbabilityChecked(final double x) {
		return (x-a)*(x-a)*oneDivBMinusASquare;
	}

	@Override
	public double getNumericalMean() {
		return (a+2*b)/3;
	}

	/**
	 * Liefert die Schiefe der Verteilung.
	 * @return	Schiefe der Verteilung
	 */
	public double getSkewness() {
		final double Px=b;
		return Math.sqrt(2.0)*(a+b-2*Px)*(2*a-b-Px)*(a-2*b+Px)/5/Math.pow(a*a+b*b+Px*Px-a*b-a*Px-b*Px,3.0/2.0); /* Übernommen von Dreiecksverteilung */
	}

	@Override
	public double inverseCumulativeProbability(final double p) {
		if (p<0) return -Double.MAX_VALUE;
		if (p>1) return Double.MAX_VALUE;
		return a+(b-a)*Math.sqrt(p);
	}

	@Override
	public double random(RandomGenerator generator) {
		final double p=generator.nextDouble();
		return a+(b-a)*Math.sqrt(p);
	}
}
