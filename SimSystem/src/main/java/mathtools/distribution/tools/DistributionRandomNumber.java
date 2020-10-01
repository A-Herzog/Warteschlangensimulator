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
package mathtools.distribution.tools;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import mathtools.distribution.DistributionWithRandom;

/**
 * Da die Klasse <code>AbstractContinuousDistribution</code> keine Funktionen zur
 * Erzeugung von Zufallszahlen gemäß der jeweiligen Verteilung enthält, wird
 * dies durch diese Klasse nachgerüstet.<br><br>
 * Die Klasse besitzt nur eine statische Methode <code>Random</code> die eine Zufallszahl erzeugt.
 * @author Alexander Herzog
 * @version 2.3
 */
public final class DistributionRandomNumber {

	/**
	 * Diese Klasse stellt nur statische Methoden zur Verfügung und kann nicht instanziert werden.
	 */
	private DistributionRandomNumber() {
	}

	/**
	 * Zu verwendender Zufallszahlengenerator. Der Generator muss Thread-lokal oder zumindest
	 * Thread-save arbeiten.<br>
	 * Standardmäßig wird <code>ThreadLocalRandomGenerator</code>, der auf <code>ThreadLocalRandom</code>
	 * aufbaut und damit sehr schnell ist, verwendet. Soll ein Seed gesetzt werden, muss hier eine
	 * Instanz von <code>SeedableThreadLocalRandomGenerator</code> eingetragen werden. Diese ist zwar
	 * auch Thread-lokal, aber nicht ganz so schnell.
	 */
	public static RandomGenerator generator;

	static {
		generator=new ThreadLocalRandomGenerator();
	}

	/**
	 * Liefert eine standardnormalverteilte Zufallszahl zurück.
	 * @return Standardnormalverteilte Zufallszahl
	 */
	public static double getStdNormalRandom() {
		return generator.nextGaussian();
	}

	/**
	 * Liefert über den aktuellen Zufallszahlengenerator eine Double-Zufallszahl.
	 * @return	Double-Zufallszahl
	 */
	public static double nextDouble() {
		return generator.nextDouble();
	}

	/**
	 * Liefert 1/e.<br>
	 * Wird in {@link #randomGammaDirect(double, double)} verwendet.<br>
	 * Die Vorausberechnung macht die Pseudozufallszahlenerzeugung schneller.
	 * @see #randomGammaDirect(double, double)
	 */
	private static final double inverseE=1/FastMath.E;

	/**
	 * Liefert eine Pseudozufallszahl gemäß der Gamma-Verteilung
	 * @param shape	Form-Parameter
	 * @param scale	Skalierungsparameter
	 * @return	Pseudozufallszahl
	 * @see #random(AbstractRealDistribution)
	 */
	private static double randomGammaDirect(final double shape, final double scale)  {
		/*
		 * see org.apache.commons.math3.distribution.GammaDistribution.sample()
		 * By using this method wie avoid creating a GammaDistribution object each time a random number is needed.
		 */
		if (shape < 1) {
			/* [1]: p. 228, Algorithm GS */

			while (true) {
				/* Step 1: */
				final double u = generator.nextDouble();
				final double bGS = 1 + shape*inverseE;
				final double p = bGS * u;

				if (p <= 1) {
					/* Step 2: */

					final double x = FastMath.pow(p, 1 / shape);
					final double u2 = generator.nextDouble();

					if (u2 > FastMath.exp(-x)) {
						/* Reject */
						continue;
					} else {
						return scale * x;
					}
				} else {
					/* Step 3: */

					final double x = -1 * Math.log((bGS - p) / shape);
					final double u2 = generator.nextDouble();

					if (u2 > FastMath.pow(x, shape - 1)) {
						/* Reject */
						continue;
					} else {
						return scale * x;
					}
				}
			}
		}

		/* Now shape >= 1 */

		final double d = shape - 0.333333333333333333;
		final double c = 1 / (3 * FastMath.sqrt(d));

		while (true) {
			final double x = generator.nextGaussian();
			final double v = (1 + c * x) * (1 + c * x) * (1 + c * x);

			if (v <= 0) {
				continue;
			}

			final double x2 = x * x;
			final double u = generator.nextDouble();

			/* Squeeze */
			if (u < 1 - 0.0331 * x2 * x2) {
				return scale * d * v;
			}

			if (Math.log(u) < 0.5 * x2 + d * (1 - v + Math.log(v))) {
				return scale * d * v;
			}
		}
	}

	/**
	 * Erzeugt eine Zufallszahl gemäß der übergebenen Verteilung.
	 * @param distribution	Verteilungsfunktion gemäß der eine Zufallszahl erzeugt werden soll
	 * @return	Zufallszahl gemäß der gegebenen Verteilung
	 */
	public static double random(final AbstractRealDistribution distribution) {
		/* Die eigenen Verteilungen implementieren DistributionWithRandom und bringen daher eigene Methoden mit. */
		if (distribution instanceof DistributionWithRandom) {
			return ((DistributionWithRandom)distribution).random(generator);
		}

		/* Für die CommonMath-Verteilungen sind hier die Zufallszahlengeneratoren implementiert. */

		if (distribution instanceof UniformRealDistribution) {
			return ((UniformRealDistribution)distribution).getSupportLowerBound()+(((UniformRealDistribution)distribution).getSupportUpperBound()-((UniformRealDistribution)distribution).getSupportLowerBound())*generator.nextDouble();
		}

		if (distribution instanceof ExponentialDistribution) {
			return -Math.log(1-generator.nextDouble())*((ExponentialDistribution)distribution).getMean(); /* StrictMath.log ist schneller als FastMath. Math.log laut Code StrictMath.log auf, aber in Wirklichkeit scheint hier der Compiler Magic zu machen, so dass Math.log schneller ist. */
		}

		if (distribution instanceof NormalDistribution) {
			return getStdNormalRandom()*((NormalDistribution)distribution).getStandardDeviation()+((NormalDistribution)distribution).getMean();
		}

		if (distribution instanceof GammaDistribution) {
			final double alpha=((GammaDistribution)distribution).getShape();
			final double beta=((GammaDistribution)distribution).getScale();
			if (Math.abs(alpha-1)<0.00000001) {
				/* Ist Exp-Verteilung mit E=1/beta */
				return -Math.log(1-generator.nextDouble())*beta; /* StrictMath.log ist schneller als FastMath. Math.log laut Code StrictMath.log auf, aber in Wirklichkeit scheint hier der Compiler Magic zu machen, so dass Math.log schneller ist. */
			}
			return randomGammaDirect(alpha,beta);
			/*
			Um Faktor 3 langsamer:
			final GammaDistribution tempGamma=new GammaDistribution(generator,alpha,beta,GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
			return tempGamma.sample();
			 */
		}

		if (distribution instanceof CauchyDistribution) {
			/* https://de.wikipedia.org/wiki/Cauchy-Verteilung */
			final double x=1/FastMath.tan(Math.PI*generator.nextDouble());
			return ((CauchyDistribution)distribution).getScale()*x+((CauchyDistribution)distribution).getMedian();
		}

		/* Für die folgenden Verteilungen muss auf die Umkehrung per Bisektion zurückgegriffen werden: ChiSquaredDistribution, FDistribution, ChiDistribution, PertDistribution */
		/* Für folgende Verteilungen ist inverseCumulativeProbability eine direkte Funktion: Weibull */
		return distribution.inverseCumulativeProbability(generator.nextDouble());
	}

	/**
	 * Erzeugt eine nicht-negative Zufallszahl gemäß der übergebenen Verteilung.
	 * @param distribution	Verteilungsfunktion gemäß der eine Zufallszahl erzeugt werden soll
	 * @return	Zufallszahl gemäß der gegebenen Verteilung
	 */
	public static double randomNonNegative(final AbstractRealDistribution distribution) {
		double d=random(distribution);
		if (d>=0) return d;

		final int maxTrys=10;

		int count=0;
		while (d<0 && count<maxTrys) {
			count++;
			d=random(distribution);
		}
		return (d>=0)?d:0;
	}
}