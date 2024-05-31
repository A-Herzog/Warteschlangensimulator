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
 * Klasse zur Abbildung der Arcus Sinus-Verteilung<br>
 * Siehe auch: <a href="http://www.randomservices.org/random/special/Arcsine.html">http://www.randomservices.org/random/special/Arcsine.html</a>
 * @author Alexander Herzog
 */
public class ArcsineDistribution extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=6569135516191696579L;

	/**
	 * Untere Grenze des Trägers
	 */
	public final double a;

	/**
	 * Obere Grenze des Trägers
	 */
	public final double b;

	/**
	 * Konstruktor
	 * @param a	Untere Grenze des Trägers
	 * @param b	Obere Grenze des Trägers
	 */
	public ArcsineDistribution(final double a, final double b) {
		super(null);
		this.a=a;
		this.b=Math.max(a+0.0001,b);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public ArcsineDistribution(final ArcsineDistribution source) {
		this((source==null)?0:source.a,(source==null)?10:source.b);
	}

	@Override
	public double density(double x) {
		if (x<=a || x>=b) return 0;
		x=(x-a)/(b-a);
		return 1.0/Math.PI/Math.sqrt(x*(1-x));
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=a) return 0;
		if (x>=b) return 1;
		x=(x-a)/(b-a);

		return 2.0/Math.PI*Math.asin(Math.sqrt(x));
	}

	@Override
	public ArcsineDistribution clone() {
		return new ArcsineDistribution(a,b);
	}

	@Override
	public double getNumericalMean() {
		return (a+b)/2.0;
	}

	@Override
	public double getNumericalVariance() {
		return 1.0/8.0*(b-a)*(b-a);
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

		/* p=2/pi*arcsin(sqrt(x)) <=> x=(sin(pi/2*p))^2 */
		final double z=Math.pow(Math.sin(Math.PI/2.0*p),2);
		return z*(b-a)+a;
	}

	@Override
	public double random(final RandomGenerator generator) {
		return inverseCumulativeProbability(generator.nextDouble());
	}
}
