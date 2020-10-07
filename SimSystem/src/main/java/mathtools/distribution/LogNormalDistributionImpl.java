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
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

/**
 * Klasse zur Abbildung der Lognormalverteilung
 * @author Alexander Herzog
 * @version 1.2
 */
public final class LogNormalDistributionImpl extends AbstractRealDistribution implements Cloneable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1138792895684404135L;

	/**
	 * Parameter <code>mu</code> der Verteilung
	 */
	public final double mu;

	/**
	 * Quadrierte Fassung des Parameters <code>sigma</code> der Verteilung
	 */
	public final double sigma2;

	/**
	 * Parameter <code>sigma</code> der Verteilung
	 */
	public final double sigma;

	/**
	 * Erwartungswert der Lognormalverteilung
	 */
	public final double mean;

	/**
	 * Standardabweichung der Lognormalverteilung
	 */
	public final double sd;

	/**
	 * Eingebettete Normalverteilung.
	 * @see #density(double)
	 */
	private final NormalDistribution normal;

	/**
	 * Standardnormalverteilung, die verwendet wird, um Verteilungswerte
	 * zu berechnen.
	 * @see #cumulativeProbability(double)
	 * @see #inverseCumulativeProbability(double)
	 */
	private static final NormalDistribution stdNormal=new NormalDistribution(null,0.0,1.0,NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

	/**
	 * Konstruktor der Klasse <code>LogNormalDistributionImpl</code>
	 * @param mean	Mittelwert der Verteilung
	 * @param sd	Standardabweichung der Verteilung
	 */
	public LogNormalDistributionImpl(final double mean, final double sd) {
		super(null);
		this.mean=mean;
		this.sd=sd;

		sigma2=Math.log(FastMath.pow(sd/mean,2)+1);
		mu=Math.log(mean)-sigma2/2; /* StrictMath.log ist schneller als FastMath. Math.log laut Code StrictMath.log auf, aber in Wirklichkeit scheint hier der Compiler Magic zu machen, so dass Math.log schneller ist. */
		sigma=StrictMath.sqrt(sigma2);

		normal=new NormalDistribution(null,mu,sigma,NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}

	/**
	 * Konstruktor der Klasse <code>LogNormalDistributionImpl</code><br>
	 * (Als Parameter werden mean=sd=1 angenommen.)
	 */
	public LogNormalDistributionImpl() {this(1.0,1.0);}


	@Override
	public double density(double x) {
		if (x<=0) return 0;
		return x*normal.density(Math.log(x)); /* StrictMath.log ist schneller als FastMath. Math.log laut Code StrictMath.log auf, aber in Wirklichkeit scheint hier der Compiler Magic zu machen, so dass Math.log schneller ist. */
	}

	@Override
	public double cumulativeProbability(final double x) {
		return stdNormal.cumulativeProbability((Math.log(x)-mu)/sigma); /* StrictMath.log ist schneller als FastMath. Math.log laut Code StrictMath.log auf, aber in Wirklichkeit scheint hier der Compiler Magic zu machen, so dass Math.log schneller ist. */
	}

	@Override
	public double inverseCumulativeProbability(final double p) {
		return FastMath.exp(mu+sigma*stdNormal.inverseCumulativeProbability(p));
	}

	@Override
	public LogNormalDistributionImpl clone() {return new LogNormalDistributionImpl(mean,sd);}

	@Override
	public double getNumericalMean() {return mean;}

	@Override
	public double getNumericalVariance() {return sd*sd;}

	@Override
	public double getSupportLowerBound() {return 0;}

	@Override
	public double getSupportUpperBound() {return Double.MAX_VALUE;}

	@Override
	public boolean isSupportLowerBoundInclusive() {return false;}

	@Override
	public boolean isSupportUpperBoundInclusive() {return false;}

	@Override
	public boolean isSupportConnected() {return true;}

	/*private static final double TwoTimesPI=2*Math.PI;*/

	/**
	 * Es werden immer zwei Pseudozufallszahlen gleichzeitig generiert.
	 * Steht eine zweite Zahl direkt zur Verfügung?
	 * @see #nextRandom
	 * @see #random(RandomGenerator)
	 */
	private boolean randomAvailable=false;

	/**
	 * Es werden immer zwei Pseudozufallszahlen gleichzeitig generiert.
	 * Wenn eine zweite zur Verfügung steht, so wird sie hier angeboten.
	 * @see #randomAvailable
	 * @see #random(RandomGenerator)
	 */
	private double nextRandom=0;

	@Override
	public double random(final RandomGenerator generator) {
		if (!randomAvailable) {
			/* https://de.wikipedia.org/wiki/Box-Muller-Methode - Verträgt sich nicht gut mit dem linearen Kongruenz Math.random() */
			/*
			final double r1=TwoTimesPI*rnd.nextDouble();
			final double r2=StrictMath.sqrt(-2*Math.log(rnd.nextDouble()));
			randomAvailable=true; ohne Nutzung der anderen Zahl langsamer, aber bessere Werte (in Bezug auf die Kenngrößen)
			nextRandom=FastMath.exp(-StrictMath.sin(r1)*r2*sigma+mu);
			return FastMath.exp(StrictMath.cos(r1)*r2*sigma+mu);
			 */

			/* https://de.wikipedia.org/wiki/Polar-Methode */
			double q=10, u=0, v=0;
			while (q==0 || q>=1) {
				u=2*generator.nextDouble()-1;
				v=2*generator.nextDouble()-1;
				q=u*u+v*v;
			}
			final double p=StrictMath.sqrt(-2*Math.log(q)/q); /* StrictMath.log ist schneller als FastMath. Math.log laut Code StrictMath.log auf, aber in Wirklichkeit scheint hier der Compiler Magic zu machen, so dass Math.log schneller ist. */
			randomAvailable=true;
			final double product=p*sigma;
			nextRandom=FastMath.exp(v*product+mu);
			return FastMath.exp(u*product+mu);
		} else {
			randomAvailable=false;
			return nextRandom;
		}
	}
}
