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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Verallgemeinerte Rademacher-Verteilung<br>
 * (Es können zwei beliebige Punkte vorgegeben werden und dann für einen eine Wahrscheinlichkeit angegeben werden.)
 * @author Alexander Herzog
 */
public class GeneralizedRademacherDistribution extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-885424410211728395L;

	/**
	 * Kleinerer der beiden Punkte
	 */
	public final double a;

	/**
	 * Größerer der beiden Punkte
	 */
	public final double b;

	/**
	 * Wahrscheinlichkeit für a
	 */
	public final double pA;

	/**
	 * Wahrscheinlichkeit für b
	 */
	public final double pB;

	/**
	 * Konstruktor
	 * @param a	Kleinerer der beiden Punkte
	 * @param b	Größerer der beiden Punkte
	 * @param pA	Wahrscheinlichkeit für a
	 */
	public GeneralizedRademacherDistribution(final double a, final double b, final double pA) {
		super(null);
		if (a<=b) {
			this.a=a;
			this.b=b;
			this.pA=Math.max(0,Math.min(1,pA));
			this.pB=1-this.pA;
		} else {
			this.b=a;
			this.a=b;
			this.pB=Math.max(0,Math.min(1,pA));
			this.pA=1-this.pB;
		}
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public GeneralizedRademacherDistribution(final GeneralizedRademacherDistribution source) {
		this((source==null)?1:source.a,(source==null)?2:source.b,(source==null)?0.5:source.pA);
	}

	@Override
	public double density(double x) {
		return (x==a || x==b)?Double.POSITIVE_INFINITY:0;
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<a) return 0;
		if (x<b) return pA;
		return 1;
	}

	@Override
	public double getNumericalMean() {
		return a*pA+b*pB;
	}

	@Override
	public double getNumericalVariance() {
		final double EXsqr=a*a*pA+b*b*pB;
		final double EX=a*pA+b*pB;
		return EXsqr-EX*EX;
	}

	@Override
	public double getSupportLowerBound() {
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public boolean isSupportLowerBoundInclusive() {
		return false;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return false;
	}

	@Override
	public boolean isSupportConnected() {
		return true;
	}

	@Override
	public GeneralizedRademacherDistribution clone() {
		return new GeneralizedRademacherDistribution(this);
	}

	@Override
	public double random(RandomGenerator generator) {
		return (generator.nextDouble()<pA)?a:b;
	}
}
