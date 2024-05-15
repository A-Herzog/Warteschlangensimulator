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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Klasse zur Abbildung der reziproken Verteilung
 * @author Alexander Herzog
 */
public class ReciprocalDistribution extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-4157327895459187928L;

	/**
	 * Untere Grenze des Trägers
	 */
	public final double a;

	/**
	 * Obere Grenze des Trägers
	 */
	public final double b;

	/**
	 * Vorab berechneter Faktor zu Beschleunigung der Berechnung
	 * von Dichte und Verteilungsfunktion
	 * @see #density(double)
	 * @see #cumulativeProbability(double)
	 */
	private final double logba;

	/**
	 * Konstruktor
	 * @param a	Untere Grenze des Trägers
	 * @param b	Obere Grenze des Trägers
	 */
	public ReciprocalDistribution(final double a, final double b) {
		super(null);
		this.a=Math.max(0.0001,a);
		this.b=Math.max(this.a+0.0001,b);
		logba=Math.log(this.b/this.a);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public ReciprocalDistribution(final ReciprocalDistribution source) {
		this((source==null)?0:source.a,(source==null)?10:source.b);
	}

	@Override
	public double density(double x) {
		if (x<a || x>b) return 0;
		return 1/(x*logba);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=a) return 0;
		if (x>=b) return 1;
		return Math.log(x/a)/logba;
	}

	@Override
	public ReciprocalDistribution clone() {
		return new ReciprocalDistribution(a,b);
	}

	@Override
	public double getNumericalMean() {
		return (b-a)/logba;
	}

	@Override
	public double getNumericalVariance() {
		final double d=(b-a)/logba;
		return (b*b-a*a)/(2*logba)-d*d;
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
	public double inverseCumulativeProbability(final double p) throws OutOfRangeException {
		if (p<0.0 || p>1.0) throw new OutOfRangeException(p,0,1);

		if (p==0.0) return getSupportLowerBound();
		if (p==1.0) return getSupportUpperBound();

		/*
		p=log(x/a)/logba
		exp(p*logba)*a=x
		 */

		return Math.exp(p*logba)*a;
	}

	@Override
	public double random(final RandomGenerator generator) {
		return inverseCumulativeProbability(generator.nextDouble());
	}
}
