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

/**
 * Klasse zur Abbildung der Log-Logistische-Verteilung
 * @author Alexander Herzog
 * @version 1.0
 * @see <a href="https://en.wikipedia.org/wiki/Log-logistic_distribution">https://en.wikipedia.org/wiki/Log-logistic_distribution</a>
 */
public final class LogLogisticDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4941002449586863880L;

	/**
	 * Parameter alpha der Log-Logistischen-Verteilung
	 */
	public final double alpha;

	/**
	 * Parameter beta der Log-Logistischen-Verteilung
	 */
	public final double beta;

	/**
	 * Kehrwert des Parameters {@link #beta}.<br>
	 * Wird zur Beschleunigung der Pseudozufallszahlen-Erzeugung vorberechnet.
	 * @see #random(RandomGenerator)
	 */
	private final double inverseBeta;

	/**
	 * Konstruktor der Klasse
	 * @param alpha	Parameter alpha der Log-Logistischen-Verteilung
	 * @param beta	Parameter beta der Log-Logistischen-Verteilung
	 */
	public LogLogisticDistributionImpl(final double alpha, final double beta) {
		super(null);
		this.alpha=(alpha<=0)?0.0001:alpha;
		this.beta=(beta<=0)?0.0001:beta;
		inverseBeta=1/beta;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public LogLogisticDistributionImpl(final LogLogisticDistributionImpl source) {
		this((source==null)?1:source.alpha,(source==null)?1:source.beta);
	}

	@Override
	public double density(double x) {
		if (x<0) return 0.0;
		final double xalpha=x/alpha;
		final double denominator=1+Math.pow(xalpha,beta); /* FastMath.pow(...) würde hier new double[2] durchführen! */
		return (beta/alpha)*Math.pow(xalpha,beta-1)/denominator/denominator; /* FastMath.pow(...) würde hier new double[2] durchführen! */
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=0) return 0.0;
		return 1/(1+Math.pow(x/alpha,-beta)); /* FastMath.pow(...) würde hier new double[2] durchführen! */
	}

	@Override
	public double getNumericalMean() {
		if (beta<=1) return 0.0; /* eigentlich "undefiniert" */
		return alpha*Math.PI/beta/Math.sin(Math.PI/beta);
	}

	@Override
	public double getNumericalVariance() {
		if (beta<=2) return 0.0; /* eigentlich "undefiniert" */
		final double b=Math.PI/beta;
		final double sinb=Math.sin(b);
		return alpha*alpha*(2*b/Math.sin(2*b)-b*b/sinb/sinb);
	}

	/**
	 * Liefert den Modus der Verteilung.
	 * @return	Modus der Verteilung
	 */
	public double getMode() {
		if (beta<=1) return 0;
		return alpha*Math.pow((beta-1)/(beta+1),1/beta);
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

	@Override
	public double random(final RandomGenerator generator) {
		/* F^{-1}(p)=alpha*(p/(1-p))^(1/beta) */
		final double p=generator.nextDouble();
		return alpha*Math.pow(p/(1-p),inverseBeta); /* FastMath.pow(...) würde hier new double[2] durchführen! */
	}

	@Override
	public LogLogisticDistributionImpl clone() {
		return new LogLogisticDistributionImpl(alpha,beta);
	}
}
