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
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.numbers.gamma.IncompleteGamma;

/**
 * Klasse zur Abbildung der Inversen Gamma-Verteilung<br>
 * Siehe auch: <a href="https://en.wikipedia.org/wiki/Inverse-gamma_distribution">https://en.wikipedia.org/wiki/Inverse-gamma_distribution</a>
 * @author Alexander Herzog
 */
public class InverseGammaDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-3400286109343026392L;

	/**
	 * Formparameter
	 */
	public final double alpha;

	/**
	 * Skalierung
	 */
	public final double beta;

	/**
	 * Vorab berechneter Erwartungswert
	 * @see #getNumericalMean()
	 */
	private final double mean;

	/**
	 * Vorab berechnete Varianz
	 * @see #getNumericalVariance()
	 */
	private final double variance;

	/**
	 * Faktor zur schnelleren Berechnung der Dichte
	 * @see #density(double)
	 */
	private final double pdfFactor;

	/**
	 * Faktor zur schnelleren Berechnung der Verteilungsfunktion
	 * @see #cumulativeProbability(double)
	 */
	private final double cdfFactor;

	/**
	 * Konstruktor
	 * @param alpha	Formparameter
	 * @param beta	Skalierung
	 */
	public InverseGammaDistributionImpl(final double alpha, final double beta) {
		super(null);
		this.alpha=Math.max(alpha,0.0001);
		this.beta=Math.max(beta,0.0001);
		mean=(this.alpha<=1)?Double.POSITIVE_INFINITY:(this.beta/(this.alpha-1));
		variance=(this.alpha<=2)?Double.POSITIVE_INFINITY:(this.beta*this.beta/(this.alpha-1)/(this.alpha-1)/(this.alpha-2));
		pdfFactor=Math.pow(this.beta,this.alpha)/Gamma.gamma(this.alpha);
		cdfFactor=1.0/Gamma.gamma(this.alpha);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public InverseGammaDistributionImpl(final InverseGammaDistributionImpl source) {
		this((source==null)?1:source.alpha,(source==null)?1:source.beta);
	}

	/**
	 * Liefert den Formparameter.
	 * @return	Formparameter
	 * @see #alpha
	 */
	public double getShape() {
		return alpha;
	}

	/**
	 * Liefert die Skalierung.
	 * @return	Skalierung
	 * @see #beta
	 */
	public double getScale() {
		return beta;
	}

	@Override
	public double density(double x) {
		if (x<=0) return 0;
		return pdfFactor*Math.pow(x,-alpha-1)*Math.exp(-beta/x);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=0) return 0;
		return cdfFactor*IncompleteGamma.Upper.value(alpha,beta/x);
	}

	@Override
	public InverseGammaDistributionImpl clone() {
		return new InverseGammaDistributionImpl(alpha,beta);
	}

	@Override
	public double getNumericalMean() {
		return mean;
	}

	@Override
	public double getNumericalVariance() {
		return variance;
	}

	@Override
	public double getSupportLowerBound() {
		return 0;
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
}
