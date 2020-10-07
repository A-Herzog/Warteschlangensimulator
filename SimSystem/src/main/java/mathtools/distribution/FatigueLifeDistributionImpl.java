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
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Implementierung einer Fatigue-Life-Verteilung
 * @author Alexander Herzog
 * @see AbstractRealDistribution
 */
public final class FatigueLifeDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6381222936424053668L;

	/**
	 * Lageparameter
	 */
	public final double mu;

	/**
	 * Skalierungsparameter
	 */
	public final double beta;

	/**
	 * Formparameter
	 */
	public final double gamma;

	/**
	 * Kehrwert des Skalierungsparameter {@link #beta}.<br>
	 * Wird für Dichte und Verteilungsfunktion vorberechnet.
	 * @see #density(double)
	 * @see #cumulativeProbability(double)
	 * @see #beta
	 */
	private final double inverseBeta;

	/**
	 * Kehrwert des Formparameter {@link #gamma}.
	 * Wird für Dichte und Verteilungsfunktion vorberechnet.
	 * @see #density(double)
	 * @see #cumulativeProbability(double)
	 * @see #gamma
	 */
	private final double inverseGamma;

	/**
	 * Standardnormalverteilung, die verwendet wird, um Verteilungswerte
	 * zu berechnen.
	 * @see #density(double)
	 * @see #cumulativeProbability(double)
	 */
	private static final NormalDistribution stdNormal=new NormalDistribution(null,0.0,1.0,NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

	/**
	 * Konstruktor der Klasse
	 * @param mu	Lageparameter
	 * @param beta	Skalierungsparameter
	 * @param gamma	Formparameter
	 */
	public FatigueLifeDistributionImpl(final double mu, final double beta, final double gamma) {
		super(null);
		this.mu=mu;
		this.beta=(beta<=0)?0.0001:beta;
		this.gamma=(gamma<=0)?0.0001:gamma;
		inverseBeta=1/beta;
		inverseGamma=1/gamma;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public FatigueLifeDistributionImpl(final FatigueLifeDistributionImpl source) {
		super(null);
		if (source!=null) {
			this.mu=source.mu;
			this.beta=source.beta;
			this.gamma=source.gamma;
			this.inverseBeta=source.inverseBeta;
			this.inverseGamma=source.inverseGamma;
		} else {
			this.mu=0;
			this.beta=1;
			this.gamma=1;
			this.inverseBeta=1;
			this.inverseGamma=1;
		}
	}

	@Override
	public double density(double x) {
		if (x<=mu) return 0;
		final double param=(x-mu)*inverseBeta;
		final double part1=Math.sqrt(param);
		final double part2=Math.sqrt(1.0/param);
		final double numerator1=part1+part2;
		final double numerator2=part1-part2;
		return numerator1/(2*gamma*(x-mu))*stdNormal.density(numerator2*inverseGamma);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=mu) return 0;
		final double param=(x-mu)*inverseBeta;
		final double numerator=Math.sqrt(param)-Math.sqrt(1.0/param);
		return stdNormal.cumulativeProbability(numerator*inverseGamma);
	}

	@Override
	public FatigueLifeDistributionImpl clone() {
		return new FatigueLifeDistributionImpl(this);
	}

	@Override
	public double getNumericalMean() {
		return mu+beta*(1+gamma*gamma/2);
	}

	@Override
	public double getNumericalVariance() {
		return beta*beta*gamma*gamma*(1+5*gamma*gamma/4);
	}

	@Override
	public double getSupportLowerBound() {
		return mu;
	}

	@Override
	public double getSupportUpperBound() {
		return Integer.MAX_VALUE;
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
