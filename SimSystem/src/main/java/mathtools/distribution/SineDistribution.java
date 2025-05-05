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
 * Klasse zur Abbildung der Sinus-Verteilung<br>
 * Siehe auch: <a href="http://www.randomservices.org/random/special/Sine.html">http://www.randomservices.org/random/special/Sine.html</a>
 * @author Alexander Herzog
 */
public class SineDistribution extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=5380306629461260392L;

	/**
	 * Untere Grenze des Trägers
	 */
	public final double a;

	/**
	 * Obere Grenze des Trägers
	 */
	public final double b;

	/**
	 * Vorab berechnete Varianz
	 * @see #getNumericalVariance()
	 */
	private final double variance;

	/**
	 * Faktor zur schnelleren Berechnung der Dichte
	 * @see #density(double)
	 */
	private static final double pdfFactor=Math.PI/2.0;

	/**
	 * Konstruktor
	 * @param a	Untere Grenze des Trägers
	 * @param b	Obere Grenze des Trägers
	 */
	public SineDistribution(final double a, final double b) {
		super(null);
		this.a=a;
		this.b=Math.max(a+0.0001,b);
		variance=(1.0/4.0-2.0/Math.pow(Math.PI,2))*Math.pow(this.b-this.a,2);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public SineDistribution(final SineDistribution source) {
		this((source==null)?0:source.a,(source==null)?10:source.b);
	}

	@Override
	public double density(double x) {
		if (x<=a || x>=b) return 0;
		x=(x-a)/(b-a);
		return pdfFactor*Math.sin(Math.PI*x);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=a) return 0;
		if (x>=b) return 1;
		x=(x-a)/(b-a);

		return 0.5*(1.0-Math.cos(Math.PI*x));
	}

	@Override
	public SineDistribution clone() {
		return new SineDistribution(a,b);
	}

	@Override
	public double getNumericalMean() {
		return (a+b)/2;
	}

	@Override
	public double getNumericalVariance() {
		return variance;
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

		/* p=1/2*(1-cos(pi*x)) <=> x=arccos(1-2p)/pi */
		final double z=Math.acos(1.0-2.0*p)/Math.PI;
		return z*(b-a)+a;
	}

	@Override
	public double random(final RandomGenerator generator) {
		return inverseCumulativeProbability(generator.nextDouble());
	}
}
