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
 * Pareto-Verteilung
 * @author Alexander Herzog
 * @version 1.0
 */
public final class ParetoDistributionImpl extends AbstractRealDistribution implements Serializable, Cloneable, DistributionWithRandom {
	private static final long serialVersionUID = 1916306018142412092L;

	/**
	 * Skalierungsparameter Xmin (Xmin&gt;0)<br>
	 * Schreibzugriff ist möglich
	 */
	public final double xmin;

	/**
	 * Formparameter alpha (alpha&gt;0)<br>
	 * Schreibzugriff ist möglich
	 */
	public final double alpha;

	/**
	 * Konstruktor der Laplace-Verteilung
	 * @param xmin	Lageparameter
	 * @param alpha	Skalierungsparameter
	 */
	public ParetoDistributionImpl(final double xmin, final double alpha) {
		super(null);
		this.xmin=(xmin<=0)?1:xmin;
		this.alpha=(alpha<=0)?1:alpha;
	}

	@Override
	public double density(double x) {
		/* if (xmin<=0 || alpha<=0) return 0; - per Konstruktor ausgeschlossen */
		/* https://en.wikipedia.org/wiki/Pareto_distribution */
		if (x<xmin) return 0;
		return alpha*Math.pow(xmin,alpha)/Math.pow(x,alpha+1);
	}

	@Override
	public double cumulativeProbability(double x) {
		/* if (xmin<=0 || alpha<=0) return 0; - per Konstruktor ausgeschlossen */
		/* https://en.wikipedia.org/wiki/Pareto_distribution */
		if (x<xmin) return 0;
		return 1-Math.pow(xmin/x,alpha);
	}

	@Override
	public double getNumericalMean() {
		if (alpha<=1) return Double.MAX_VALUE;
		return alpha*xmin/(alpha-1);
	}

	@Override
	public double getNumericalVariance() {
		if (alpha<=2) return Double.MAX_VALUE;
		return xmin*xmin*alpha/(alpha-1)/(alpha-1)/(alpha-2);
	}

	@Override
	public double getSupportLowerBound() {
		return xmin;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.MAX_VALUE;
	}

	@Override
	public boolean isSupportLowerBoundInclusive() {
		return true;
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
	public ParetoDistributionImpl clone() {
		return new ParetoDistributionImpl(xmin,alpha);
	}

	@Override
	public final double random(final RandomGenerator generator) {
		/* https://en.wikipedia.org/wiki/Pareto_distribution */
		/* if (alpha<=0) return 0; - per Konstruktor ausgeschlossen */
		final double u=1-generator.nextDouble(); /* ==> (0,1] */
		return xmin/Math.pow(u,1/alpha);
	}
}