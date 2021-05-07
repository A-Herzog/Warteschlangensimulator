/**
 * Copyright 2021 Alexander Herzog
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
 * Basisklasse für Sägezahn-Verteilungen
 * @author Alexander Herzog
 */
public abstract class AbstractSawtoothDistribution extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-971944858433036182L;

	/**
	 * Untere Grenze
	 */
	public final double a;

	/**
	 * Obere Grenze
	 */
	public final double b;

	/**
	 * Ausdruck zur Beschleunigung von Berechnungen<br>
	 * 1/(b-a)^2
	 */
	protected final double oneDivBMinusASquare;

	/**
	 * Ausdruck zur Beschleunigung von Berechnungen<br>
	 * 2/(b-a)^2
	 */
	protected final double twoDivBMinusASquare;

	/**
	 * Konstruktor der Klasse
	 * @param a	Untere Grenze
	 * @param b	Obere Grenze
	 */
	public AbstractSawtoothDistribution(final double a, final double b) {
		super(null);
		this.a=a;
		this.b=Math.max(a+0.0001,b);
		oneDivBMinusASquare=1/((this.b-this.a)*(this.b-this.a));
		twoDivBMinusASquare=2/((this.b-this.a)*(this.b-this.a));
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public AbstractSawtoothDistribution(final AbstractSawtoothDistribution source) {
		super(null);
		if (source==null) {
			a=1;
			b=2;
		} else {
			a=source.a;
			b=source.b;
		}
		oneDivBMinusASquare=1/((this.b-this.a)*(this.b-this.a));
		twoDivBMinusASquare=2/((this.b-this.a)*(this.b-this.a));
	}

	@Override
	public double getSupportLowerBound() {
		return a;
	}

	@Override
	public double getSupportUpperBound() {
		return b;
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

	@Override
	public double density(final double x) {
		if (x<a || x>b) return 0;
		return densityChecked(x);
	}

	/**
	 * Berechnet einen Dichtewert im Bereich [a,b]
	 * @param x	Stelle an der f(x) berechnet werden soll; es ist a<=x<=b
	 * @return	Dichte an der Stelle x
	 */
	protected abstract double densityChecked(final double x);

	@Override
	public double cumulativeProbability(final double x) {
		if (x<a) return 0;
		if (x>b) return 1;
		return cumulativeProbabilityChecked(x);
	}

	/**
	 * Berechnet einen Verteilungsfunktion im Bereich [a,b]
	 * @param x	Stelle an der F(x) berechnet werden soll; es ist a<=x<=b
	 * @return	Wert der Verteilungsfunktion an der Stelle x
	 */
	protected abstract double cumulativeProbabilityChecked(final double x);

	@Override
	public double getNumericalVariance() {
		return (b-a)*(b-a)/18;
	}
}
