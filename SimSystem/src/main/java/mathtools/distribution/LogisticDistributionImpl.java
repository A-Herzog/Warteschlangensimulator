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
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

/**
 * Logistische Verteilung
 * @author Alexander Herzog
 * @version 1.0
 */
public final class LogisticDistributionImpl extends AbstractRealDistribution implements Serializable, Cloneable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4319813650956502662L;

	/**
	 * Parameter mu (entspricht Erwartungswert)<br>
	 * Schreibzugriff ist möglich
	 */
	public final double mu;

	/**
	 * Parameter s<br>
	 * Schreibzugriff ist möglich
	 */
	public final double s;

	/**
	 * Konstruktor der Logistischen Verteilung
	 * @param mu	Lageparameter (=Erwartungswert)
	 * @param s	Skalierungsparameter
	 */
	public LogisticDistributionImpl(final double mu, final double s) {
		super(null);
		this.mu=mu;
		this.s=(s<=0)?1:s;
	}

	@Override
	public double density(double x) {
		/* https://en.wikipedia.org/wiki/Logistic_distribution */
		/* if (s<=0) return 0;  - per Konstruktor ausgeschlossen */
		double part=FastMath.exp(-(x-mu)/s);
		return part/(s*(1+part)*(1+part));
	}

	@Override
	public double cumulativeProbability(double x) {
		/* https://en.wikipedia.org/wiki/Logistic_distribution */
		/* if (s<=0) return 0;  - per Konstruktor ausgeschlossen */
		return 1/(1+FastMath.exp(-(x-mu)/s));
	}

	@Override
	public double getNumericalMean() {
		return mu;
	}

	/**
	 * Vorab berechneter Wert zur Beschleunigung der Berechnung der Varianz.
	 * @see #getNumericalVariance()
	 */
	private static final double varConst=Math.PI*Math.PI/3;

	@Override
	public double getNumericalVariance() {
		return s*s*varConst;
	}

	@Override
	public double getSupportLowerBound() {
		return -Double.MAX_VALUE;
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

	@Override
	public LogisticDistributionImpl clone() {
		return new LogisticDistributionImpl(mu,s);
	}

	@Override
	public double random(final RandomGenerator generator) {
		/* https://de.wikipedia.org/wiki/Logistische_Verteilung */
		final double u=generator.nextDouble();
		return mu+s*Math.log(u/(1-u));
	}
}
