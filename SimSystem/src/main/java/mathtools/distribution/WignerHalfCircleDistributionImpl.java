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

/**
 * Klasse zur Abbildung der Wigner Halbkreis-Verteilung<br>
 * Siehe auch: <a href="https://en.wikipedia.org/wiki/Wigner_semicircle_distribution">https://en.wikipedia.org/wiki/Wigner_semicircle_distribution</a>
 * @author Alexander Herzog
 */
public class WignerHalfCircleDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2444362329994457203L;

	/**
	 * Mittelpunkt
	 */
	public final double m;

	/**
	 * Radius
	 */
	public final double R;

	/**
	 * Vorab berechneter Faktor zur Beschleunigung der Berechnung von Dichte-Werten
	 * @see #density(double)
	 */
	private final double pdfFactor;

	/**
	 * Vorab berechneter Faktor zur Beschleunigung der Berechnung von Verteilungs-Werten
	 * @see #cumulativeProbability(double)
	 */
	private final double cdfFactor;

	/**
	 * Konstruktor
	 * @param R	Radius
	 * @param m	Mittelpunkt
	 */
	public WignerHalfCircleDistributionImpl(final double m, final double R) {
		super(null);
		this.m=m;
		this.R=Math.max(0,R);

		pdfFactor=2/Math.PI/(this.R*this.R);
		cdfFactor=Math.PI*this.R*this.R;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public WignerHalfCircleDistributionImpl(final WignerHalfCircleDistributionImpl source) {
		this((source==null)?1:source.m,(source==null)?0:source.R);
	}

	@Override
	public double density(double x) {
		x=x-m;
		if (x<=-R || x>=R) return 0;

		return pdfFactor*Math.sqrt(R*R-x*x);
	}

	@Override
	public double cumulativeProbability(double x) {
		x=x-m;
		if (x<=-R) return 0;
		if (x>=R) return 1;

		return 0.5+x*Math.sqrt(R*R-x*x)/cdfFactor+Math.asin(x/R)/Math.PI;
	}

	@Override
	public WignerHalfCircleDistributionImpl clone() {
		return new WignerHalfCircleDistributionImpl(m,R);
	}

	@Override
	public double getNumericalMean() {
		return m;
	}

	@Override
	public double getNumericalVariance() {
		return R*R/4;
	}

	@Override
	public double getSupportLowerBound() {
		return m-R;
	}

	@Override
	public double getSupportUpperBound() {
		return m+R;
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
