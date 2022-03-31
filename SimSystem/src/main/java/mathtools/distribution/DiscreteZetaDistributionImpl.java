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

import parser.symbols.CalcSymbolPreOperatorZeta;

/**
 * Kontinuierliche Darstellung der Zeta-Verteilung, siehe
 * <a href="https://en.wikipedia.org/wiki/Zeta_distribution">https://en.wikipedia.org/wiki/Zeta_distribution</a>
 * @author Alexander Herzog
 * @see AbstractDiscreteRealDistribution
 */
public class DiscreteZetaDistributionImpl extends AbstractDiscreteRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-506560293471684514L;

	/**
	 * Verteilungsparameter s
	 */
	public final double s;

	/**
	 * Vorab berechneter Wert zeta(s)
	 * @see #getCountDensity(int)
	 */
	private final double zetaS;

	/**
	 * Erwartungswert
	 */
	public final double mean;

	/**
	 * Varianz
	 */
	public final double variance;

	/**
	 * Konstruktor der Klasse
	 * @param s	Verteilungsparameter s
	 */
	public DiscreteZetaDistributionImpl(final double s) {
		this.s=Math.max(s,1.0001);

		zetaS=CalcSymbolPreOperatorZeta.zeta(this.s);

		if (this.s<=2) {
			mean=Double.MAX_VALUE;
			variance=Double.MAX_VALUE;
		} else {
			final double zetaS1=CalcSymbolPreOperatorZeta.zeta(this.s-1);
			mean=zetaS1/zetaS;
			if (this.s<=3) {
				variance=Double.MAX_VALUE;
			} else {
				final double zetaS2=CalcSymbolPreOperatorZeta.zeta(this.s-2);
				variance=(zetaS*zetaS2-zetaS1*zetaS1)/zetaS/zetaS;
			}
		}
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public DiscreteZetaDistributionImpl(final DiscreteZetaDistributionImpl source) {
		this((source==null)?3:source.s);
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
	public double getSupportUpperBound() {
		return Double.MAX_VALUE;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return false;
	}

	@Override
	protected double getCountDensity(int k) {
		if (k<=0) return 0;
		return Math.pow(k,-s)/zetaS;
	}
}
