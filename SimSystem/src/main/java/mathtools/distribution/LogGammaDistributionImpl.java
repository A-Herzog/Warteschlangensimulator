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

/**
 * Log-Gamma-Verteilung
 * @see <a href="https://de.wikipedia.org/wiki/Logarithmische_Gammaverteilung">https://de.wikipedia.org/wiki/Logarithmische_Gammaverteilung</a>
 * @author Alexander Herzog
 * @version 1.0
 */
public class LogGammaDistributionImpl extends AbstractRealDistribution {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-4390422871725477721L;

	/**
	 * Parameter a
	 */
	public final double a;

	/**
	 * Parameter a
	 */
	public final double b;

	/**
	 * Vorab berechneter Wert b^a/Gamma(a)
	 * @see #density(double)
	 */
	private final double bPowerADivGammaA;

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
	 * @param a	Parameter a
	 * @param b	Parameter b
	 */
	public LogGammaDistributionImpl(final double a, final double b) {
		super(null);
		this.a=Math.max(a,0.0001);
		this.b=Math.max(b,0.0001);
		bPowerADivGammaA=Math.pow(this.b,this.a)/Gamma.gamma(this.a);
		mean=Math.pow(1.0-1.0/this.b,-this.a);
		variance=(this.b<=2)?Double.POSITIVE_INFINITY:(Math.pow(1.0-2.0/this.b,-this.a)-Math.pow(1.0-1.0/this.b,-2.0*this.a));
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public LogGammaDistributionImpl(final LogGammaDistributionImpl source) {
		super(null);
		if (source==null) {
			a=1;
			b=1;
		} else {
			a=source.a;
			b=source.b;
		}
		bPowerADivGammaA=Math.pow(this.b,this.a)/Gamma.gamma(this.a);
		mean=Math.pow(1.0-1.0/this.b,-this.a);
		variance=(this.b<=2)?Double.POSITIVE_INFINITY:(Math.pow(1.0-2.0/this.b,-this.a)-Math.pow(1.0-1.0/this.b,-2.0*this.a));
	}

	@Override
	public double density(double x) {
		if (x<1) return 0;
		return bPowerADivGammaA*Math.pow(x,-(b+1))*Math.pow(Math.log(x),a-1);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<1) return 0;
		/* gamma(a,b*log(x))/Gamma(a) = P(a,b*log(x))*Gamma(a)/Gamma(a) = P(a,b*log(x)) */
		return Gamma.regularizedGammaP(a,b*Math.log(x));
	}

	@Override
	public LogGammaDistributionImpl clone() {
		return new LogGammaDistributionImpl(a,b);
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
		return 1;
	}

	@Override
	public double getSupportUpperBound() {
		return Double.POSITIVE_INFINITY;
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
}
