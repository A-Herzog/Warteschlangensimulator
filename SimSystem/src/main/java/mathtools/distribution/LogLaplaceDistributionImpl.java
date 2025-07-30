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

/**
 * Log-Laplace-Verteilung
 * @see <a href="https://en.wikipedia.org/wiki/Log-Laplace_distribution">https://en.wikipedia.org/wiki/Log-Laplace_distribution</a>
 * @author Alexander Herzog
 * @version 1.0
 */
public class LogLaplaceDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2870258712036472160L;

	/**
	 * Parameter c (Form)
	 */
	public final double c;

	/**
	 * Parameter s (Verschiebung)
	 */
	public final double s;

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
	 * Konstruktor der Klasse
	 * @param c	Parameter c (Form)
	 * @param s	Parameter s (Verschiebung)
	 */
	public LogLaplaceDistributionImpl(final double c, final double s) {
		super(null);
		this.c=Math.max(c,0.0001);
		this.s=s;

		final double c2=this.c*this.c;
		final double c4=c2*c2;
		mean=(this.c>1)?(s+c2/(this.c-1)/(this.c+1)):Double.POSITIVE_INFINITY;
		variance=(this.c>2)?(c2/(this.c-2)/(this.c+2)-c4/(this.c-1)/(this.c-1)/(this.c+1)/(this.c+1)):Double.POSITIVE_INFINITY;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public LogLaplaceDistributionImpl(final LogLaplaceDistributionImpl source) {
		this((source==null)?2:source.c,(source==null)?0:source.s);
	}

	@Override
	public double density(double x) {
		x=x-s;
		if (x<=0) return 0;
		if (x<1) return c/2*Math.pow(x,c-1);
		return c/2*Math.pow(x,-c-1);
	}

	@Override
	public double cumulativeProbability(double x) {
		x=x-s;
		if (x<=0) return 0;
		if (x<1) return 0.5*Math.pow(x,c);
		return 1-0.5*Math.pow(x,-c);
	}

	@Override
	public LogLaplaceDistributionImpl clone() {
		return new LogLaplaceDistributionImpl(c,s);
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
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.POSITIVE_INFINITY;
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
