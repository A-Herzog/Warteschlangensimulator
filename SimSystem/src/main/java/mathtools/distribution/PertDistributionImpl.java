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
import org.apache.commons.math3.special.Beta;
import org.apache.commons.math3.util.FastMath;

/**
 * Implementierung einer PERT-Verteilung
 * @author Alexander Herzog
 * @see <a href="https://en.wikipedia.org/wiki/PERT_distribution">https://en.wikipedia.org/wiki/PERT_distribution</a>
 */
public final class PertDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5002792590117661632L;

	/**
	 * Untere Grenze des Trägers
	 */
	public final double lowerBound;

	/**
	 * x-Wert des Hochpunkts
	 */
	public final double mostLikely;

	/**
	 * Obere Grenze des Trägers
	 */
	public final double upperBound;

	/**
	 * Vorberechneter Wert zur Beschleunigung der Berechnung
	 * von Dichte und Verteilungsfunktion.
	 * @see #density(double)
	 * @see #cumulativeProbability(double)
	 */
	private final double alpha;

	/**
	 * Vorberechneter Wert zur Beschleunigung der Berechnung
	 * von Dichte und Verteilungsfunktion.
	 * @see #density(double)
	 * @see #cumulativeProbability(double)
	 */
	private final double beta;

	/**
	 * Vorberechneter Wert zur Beschleunigung der Berechnung
	 * von Dichte und Verteilungsfunktion.
	 * @see #density(double)
	 * @see #cumulativeProbability(double)
	 */
	private final double factorPDF;

	/**
	 * Vorberechneter Wert zur Beschleunigung der Berechnung
	 * von Dichte und Verteilungsfunktion.
	 * @see #density(double)
	 * @see #cumulativeProbability(double)
	 */
	private final double factorCDF;

	/**
	 * Konstruktor der Klasse
	 * @param lowerBound	Untere Grenze des Trägers
	 * @param mostLikely	x-Wert des Hochpunkt
	 * @param upperBound	Obere Grenze des Trägers
	 */
	public PertDistributionImpl(final double lowerBound, final double mostLikely, final double upperBound) {
		super(null);
		this.lowerBound=lowerBound;
		this.mostLikely=Math.max(this.lowerBound+0.0001,mostLikely);
		this.upperBound=Math.max(this.mostLikely+0.0001,upperBound);

		alpha=1+4*(this.mostLikely-this.lowerBound)/(this.upperBound-this.lowerBound);
		beta=1+4*(this.upperBound-this.mostLikely)/(this.upperBound-this.lowerBound);
		factorPDF=1/FastMath.exp(Beta.logBeta(alpha,beta))/FastMath.pow(this.upperBound-this.lowerBound,alpha+beta-1);
		factorCDF=1/(this.upperBound-this.lowerBound);
	}

	@Override
	public double density(double x) {
		if (x<=lowerBound || x>=upperBound) return 0;
		return factorPDF*Math.pow(x-lowerBound,alpha-1)*Math.pow(upperBound-x,beta-1); /* FastMath.pow(...) würde hier new double[2] durchführen! */
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=lowerBound) return 0;
		if (x>=upperBound) return 1;

		final double z=(x-lowerBound)*factorCDF;
		return Beta.regularizedBeta(z,alpha,beta);
	}

	@Override
	public PertDistributionImpl clone() {
		return new PertDistributionImpl(lowerBound,mostLikely,upperBound);
	}

	@Override
	public double getNumericalMean() {
		return (lowerBound+4*mostLikely+upperBound)/6;
	}

	@Override
	public double getNumericalVariance() {
		final double mu=(lowerBound+4*mostLikely+upperBound)/6;
		return (mu-lowerBound)*(upperBound-mu)/7;
	}

	@Override
	public double getSupportLowerBound() {
		return lowerBound;
	}

	@Override
	public double getSupportUpperBound() {
		return upperBound;
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
}
