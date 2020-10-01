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
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;

import mathtools.Functions;

/**
 * Chi-Verteilung (<b>nicht</b> Chi^2-Verteilung)
 * @author Alexander Herzog
 * @version 1.0
 */
public final class ChiDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable {
	private static final long serialVersionUID = 2215589145625848353L;

	/**
	 * Anzahl an Freiheitsgraden
	 */
	public final int degreesOfFreedom;

	/**
	 * Vorberechneter Faktor in der Dichte
	 * @see #density(double)
	 */
	private final double inverseDensityDenominator;

	/**
	 * Konstruktor der Klasse
	 * @param degreesOfFreedom	Anzahl an Freiheitsgraden
	 */
	public ChiDistributionImpl(final int degreesOfFreedom) {
		super(null);
		this.degreesOfFreedom=(degreesOfFreedom>0)?degreesOfFreedom:1;
		final double densityDenominator=FastMath.pow(2,degreesOfFreedom*0.5-1)*Functions.getGamma(degreesOfFreedom*0.5);
		inverseDensityDenominator=1.0/densityDenominator;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public ChiDistributionImpl(final ChiDistributionImpl source) {
		this((source==null)?1:source.degreesOfFreedom);
	}

	@Override
	public double density(double x) {
		if (x<=0) return 0;
		return FastMath.exp(-x*x*0.5)*FastMath.pow(x,degreesOfFreedom-1)*inverseDensityDenominator;
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=0) return 0;
		return Gamma.regularizedGammaP(degreesOfFreedom*0.5,x*x*0.5);
	}

	@Override
	public ChiDistributionImpl clone() {
		return new ChiDistributionImpl(this);
	}

	@Override
	public double getNumericalMean() {
		return Math.sqrt(2)*Functions.getGamma((degreesOfFreedom+1)*0.5)/Functions.getGamma(degreesOfFreedom*0.5);
	}

	@Override
	public double getNumericalVariance() {
		final double value=Functions.getGamma((degreesOfFreedom+1)*0.5);
		final double numerator=2*Functions.getGamma(degreesOfFreedom*0.5)*Functions.getGamma(1+degreesOfFreedom*0.5)-value*value;
		return numerator/Functions.getGamma(degreesOfFreedom*0.5);
	}

	@Override
	public double getSupportLowerBound() {
		return 0;
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
