/**
 * Copyright 2023 Alexander Herzog
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
import org.apache.commons.math3.special.Beta;
import org.apache.commons.math3.special.Gamma;

/**
 * Studentsche t-Verteilung
 * @author Alexander Herzog
 */
public class StudentTDistributionImpl extends AbstractRealDistribution implements Serializable, Cloneable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-7402810796214509814L;

	/**
	 * Erwartungswert mu<br>
	 */
	public final double mu;

	/**
	 * Freiheitsgrade &nu; (&nu;&gt;0)
	 */
	public final double nu;

	/**
	 * Von x unabhängiger Vorfaktor für die Berechnung der Dichte
	 * @see #density(double)
	 */
	private final double densityFactor;

	/**
	 * Minimaler X-Wert für die Bestimmung der inversen Verteilungsfunktion
	 * @see #random(RandomGenerator)
	 */
	private final double randomSearchMinX;

	/**
	 * Maximaler X-Wert für die Bestimmung der inversen Verteilungsfunktion
	 * @see #random(RandomGenerator)
	 */
	private final double randomSearchMaxX;

	/**
	 * Y-Wert am minimalen X-Wert für die Bestimmung der inversen Verteilungsfunktion
	 * @see #random(RandomGenerator)
	 */
	private final double randomSearchMinY;

	/**
	 * Y-Wert am maximalen X-Wert für die Bestimmung der inversen Verteilungsfunktion
	 * @see #random(RandomGenerator)
	 */
	private final double randomSearchMaxY;

	/**
	 * Konstruktor der Klasse
	 * @param mu	Erwartungswert mu<br>
	 * @param nu	Freiheitsgrade &nu; (&nu;&gt;0)
	 */
	public StudentTDistributionImpl(final double mu, final double nu) {
		super(null);
		this.mu=mu;
		this.nu=Math.max(nu,0.0001);

		densityFactor=Gamma.gamma((nu+1)/2)/(Math.sqrt(nu*Math.PI)*Gamma.gamma(nu/2));

		final double sd;
		if (this.nu>2) {
			sd=Math.max(100,Math.sqrt(this.nu/(this.nu-2)));
		} else {
			sd=1_000;
		}
		randomSearchMinX=mu-2000*sd;
		randomSearchMaxX=mu+2000*sd;
		randomSearchMinY=cumulativeProbability(randomSearchMinX);
		randomSearchMaxY=cumulativeProbability(randomSearchMaxX);
	}

	/**
	 * Copy-Konstruktor
	 * @param copySource	Zu kopierendes Ausgangsobjekt
	 */
	public StudentTDistributionImpl(final StudentTDistributionImpl copySource) {
		this(copySource.mu,copySource.nu);
	}

	@Override
	public double density(double x) {
		x=x-mu;
		return densityFactor*Math.pow(1+x*x/nu,-(nu+1)/2);
	}

	@Override
	public double inverseCumulativeProbability(final double p) {
		double minX, maxX, minY, maxY;
		if (p>0.5) {
			minX=mu;
			minY=0.5;
			maxX=randomSearchMaxX;
			maxY=randomSearchMaxY;
		} else {
			minX=randomSearchMinX;
			minY=randomSearchMinY;
			maxX=mu;
			maxY=0.5;
		}

		if (p<minY) return minX;
		if (p>maxY) return maxX;

		while (maxX-minX>=0.002) {
			final double newX=(maxX+minX)/2;
			final double newY=cumulativeProbability(newX);
			if (p>newY) minX=newX; else maxX=newX;
		}
		return (maxX+minX)/2;
	}

	@Override
	public double cumulativeProbability(double x) {
		x=x-mu;
		if (x>=0) {
			x=nu/(x*x+nu);
			final double ibeta=Beta.regularizedBeta(x,nu/2.0,0.5);
			return 1-0.5*ibeta;
		} else {
			x=nu/(x*x+nu);
			final double ibeta=Beta.regularizedBeta(x,nu/2.0,0.5);
			return 0.5*ibeta;
		}
	}

	@Override
	public double getNumericalMean() {
		return mu;
	}

	@Override
	public double getNumericalVariance() {
		if (nu>2) return nu/(nu-2);
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public double getSupportLowerBound() {
		return -Double.MAX_VALUE;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.MAX_VALUE;
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
	public StudentTDistributionImpl clone() {
		return new StudentTDistributionImpl(mu,nu);
	}

	@Override
	public double random(RandomGenerator generator) {
		return inverseCumulativeProbability(generator.nextDouble());
	}
}
