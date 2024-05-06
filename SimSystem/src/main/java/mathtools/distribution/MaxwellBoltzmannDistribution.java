/**
 * Copyright 2022 Alexander Herzog
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
import org.apache.commons.math3.special.Erf;

/**
 * Klasse zur Abbildung der Maxwell-Boltzmann-Verteilung
 * @author Alexander Herzog
 */
public class MaxwellBoltzmannDistribution extends AbstractRealDistribution implements Cloneable, Serializable {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=2034075961519890250L;

	/**
	 * Parameter
	 */
	public final double a;

	/**
	 * Vorberechneter Faktor zur Berechnung der Dichte
	 * @see #density(double)
	 */
	private final double densityFactor;

	/**
	 * Vorberechneter Faktor zur Berechnung der Verteilungsfunktion
	 * @see #cumulativeProbability(double)
	 */
	private final double cumulativeProbabilityFactor;

	/**
	 * Vorberechneter Faktor zur Berechnung von Erf im Rahmen der Berechnung der Verteilungsfunktion
	 * @see #cumulativeProbability(double)
	 */
	private final double erfFactor;

	/**
	 * Konstruktor der Klasse
	 * @param a	Verteilungsparameter
	 */
	public MaxwellBoltzmannDistribution(final double a) {
		super(null);
		this.a=Math.max(a,0.0001);
		densityFactor=Math.sqrt(2/Math.PI)/a/a/a;
		cumulativeProbabilityFactor=Math.sqrt(2/Math.PI)/a;
		erfFactor=1/Math.sqrt(2)/a;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public MaxwellBoltzmannDistribution(final MaxwellBoltzmannDistribution source) {
		this((source==null)?1:source.a);
	}

	/**
	 * Konstanter, vorab berechneter Faktor für die Berechnung des Erwartungswertes
	 *  @see #getNumericalMean()
	 */
	private static final double MEAN_FACTOR=2*Math.sqrt(2/Math.PI);

	@Override
	public double getNumericalMean() {
		return a*MEAN_FACTOR;
	}

	/**
	 * Konstanter, vorab berechneter Faktor für die Berechnung der Varianz
	 *  @see #getNumericalVariance()
	 */
	private static final double VARIANCE_FACTOR=(3*Math.PI-8)/Math.PI;

	@Override
	public double getNumericalVariance() {
		return a*a*VARIANCE_FACTOR;
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
	public MaxwellBoltzmannDistribution clone() {
		return new MaxwellBoltzmannDistribution(a);
	}

	@Override
	public double density(double x) {
		if (x<=0) return 0;
		final double x2=x*x;
		return densityFactor*x2*Math.exp(-x2/2/a/a);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=0) return 0;
		return Erf.erf(x*erfFactor)-cumulativeProbabilityFactor*x*Math.exp(-x*x/2/a/a);
	}
}
