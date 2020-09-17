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
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Implementierung einer Potenzverteilung
 * @author Alexander Herzog
 * @see AbstractRealDistribution
 */
public final class PowerDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	private static final long serialVersionUID = 6763221514361547817L;

	/**
	 * Untere Grenze des Trägers
	 */
	public final double a;

	/**
	 * Obere Grenze des Trägers
	 */
	public final double b;

	/**
	 * Potenzierungswert
	 */
	public final double c;

	/**
	 * Kehrwert des Parameters {@link #c}.<br>
	 * Wird für die inverse Verteilungsfunktion vorberechnet.
	 * @see #inverseCumulativeProbability(double)
	 */
	private final double inverseC;

	/**
	 * Vorberechneter Wert zur Beschleunigung der Berechnung
	 * von Dichte, Verteilungsfunktion und inverser Verteilungsfunktion.
	 * @see #density(double)
	 * @see #cumulativeProbability(double)
	 * @see #inverseCumulativeProbability(double)
	 */
	private final double denominator;

	/**
	 * Konstruktor der Klasse
	 * @param a	Untere Grenze des Trägers
	 * @param b	Obere Grenze des Trägers
	 * @param c	Potenzierungswert
	 */
	public PowerDistributionImpl(final double a, final double b, final double c) {
		super(null);
		this.a=a;
		if (b<=a) this.b=a+0.0001; else this.b=b;
		if (c<=0) this.c=0.0001; else this.c=c;

		denominator=Math.pow(b-a,c);
		inverseC=1/c;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public PowerDistributionImpl(final PowerDistributionImpl source) {
		this((source==null)?0:source.a,(source==null)?1:source.b,(source==null)?1:source.c);
	}

	@Override
	public double density(double x) {
		if (x<a || x>b) return 0;
		return c*Math.pow(x-a,c-1)/denominator;
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=a) return 0;
		if (x>=b) return 1;
		return Math.pow(x-a,c)/denominator;
	}

	@Override
	public double inverseCumulativeProbability(final double p) {
		if (p<0) return -Double.MAX_VALUE;
		if (p>1) return Double.MAX_VALUE;

		return Math.pow(p*denominator,inverseC)+a;
	}

	@Override
	public PowerDistributionImpl clone() {
		return new PowerDistributionImpl(a,b,c);
	}

	@Override
	public double getNumericalMean() {
		return a+(b-a)*c/(c+1);
	}

	@Override
	public double getNumericalVariance() {
		return (b-a)*(b-a)*c/((c+1)*(c+1)*(c+2));
	}

	@Override
	public double getSupportLowerBound() {
		return a;
	}

	@Override
	public double getSupportUpperBound() {
		return b;
	}

	@Override
	public boolean isSupportLowerBoundInclusive() {
		return true;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return true;
	}

	@Override
	public boolean isSupportConnected() {
		return true;
	}

	@Override
	public double random(final RandomGenerator generator) {
		return inverseCumulativeProbability(generator.nextDouble());
	}
}
