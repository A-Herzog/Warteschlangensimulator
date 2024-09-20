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
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.special.Erf;

/**
 * Klasse zur Abbildung der halben Normalverteilung<br>
 * <a href="https://mathworld.wolfram.com/Half-NormalDistribution.html">https://mathworld.wolfram.com/Half-NormalDistribution.html</a>
 * @author Alexander Herzog
 */
public final class HalfNormalDistribution extends AbstractRealDistribution implements Cloneable {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=2253286473816115553L;

	/**
	 * Parameter &theta; der Verteilung
	 */
	public final double theta;

	/**
	 * Erwartungswert
	 */
	public final double mean;

	/**
	 * Standardabweichung
	 */
	public final double sd;

	/**
	 * Vorberechneter Faktor zur Berechnung von Werten der Dichte
	 * @see #density(double)
	 */
	private final double densityFactor1;

	/**
	 * Vorberechneter Faktor zur Berechnung von Werten der Dichte
	 * @see #density(double)
	 */
	private final double densityFactor2;

	/**
	 * Vorberechneter Faktor zur Berechnung von Werten der Verteilungsfunktion
	 * @see #cumulativeProbability(double)
	 */
	private final double pdfFactor;

	/**
	 * Konstruktor der Klasse
	 * @param mean	Erwartungswert
	 */
	public HalfNormalDistribution(double mean) {
		super(null);
		mean=Math.max(mean,0.00001);
		theta=1/mean;
		this.mean=mean;
		sd=Math.sqrt((Math.PI-2)/(2*theta*theta));
		densityFactor1=2*theta/Math.PI;
		densityFactor2=theta*theta/Math.PI;
		pdfFactor=theta/Math.sqrt(Math.PI);
	}

	@Override
	public double density(double x) {
		if (x<0) return 0;
		return densityFactor1*Math.exp(-x*x*densityFactor2);
	}

	@Override
	public double cumulativeProbability(final double x) {
		if (x<0) return 0;
		return Erf.erf(x*pdfFactor);
	}

	@Override
	public double inverseCumulativeProbability(final double p) throws OutOfRangeException {
		if (p<0) return -Double.MAX_VALUE;
		if (p>1) return Double.MAX_VALUE;
		return super.inverseCumulativeProbability(p);
	}

	@Override
	public HalfNormalDistribution clone() {
		return new HalfNormalDistribution(mean);
	}

	@Override
	public double getNumericalMean() {
		return mean;
	}

	@Override
	public double getNumericalVariance() {
		return sd*sd;
	}

	/**
	 * Schiefe (ist unabhängig von &theta;
	 */
	private static final double skewness=Math.sqrt(2)*(4-Math.PI)/Math.pow((Math.PI-2),3.0/2.0);

	/**
	 * Liefert die Schiefe der Verteilung.
	 * @return	Schiefe der Verteilung
	 */
	public double getSkewness() {
		return skewness;
	}

	/**
	 * Liefert den Modus der Verteilung.
	 * @return	Modus der Verteilung
	 */
	public double getMode() {
		return 0;
	}

	@Override
	public double getSupportLowerBound() {return 0;}

	@Override
	public double getSupportUpperBound() {return Double.MAX_VALUE;}

	@Override
	public boolean isSupportLowerBoundInclusive() {return true;}

	@Override
	public boolean isSupportUpperBoundInclusive() {return false;}

	@Override
	public boolean isSupportConnected() {return true;}
}
