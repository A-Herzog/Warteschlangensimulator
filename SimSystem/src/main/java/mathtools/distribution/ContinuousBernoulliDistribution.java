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
import org.apache.commons.math3.util.FastMath;

/**
 * Klasse zur Abbildung der kontinuierlichen Bernoulli-Verteilung
 * @author Alexander Herzog
 */
public class ContinuousBernoulliDistribution extends AbstractRealDistribution implements Cloneable, Serializable {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1107931603723841379L;

	/**
	 * Untere Grenze des Trägers
	 */
	public final double a;

	/**
	 * Obere Grenze des Trägers
	 */
	public final double b;

	/**
	 * Verteilungsparameter &lambda;
	 */
	public final double lambda;

	/**
	 * Vorabberechneter Wert C(lambda)
	 */
	private final double Clambda;

	/**
	 * Faktor für die Berechnung der Verteilungsfunktion
	 */
	private final double cdfFactor;

	/**
	 * Konstruktor
	 * @param a	Untere Grenze des Trägers
	 * @param b	Obere Grenze des Trägers
	 * @param lambda	Verteiulungsparameter &lambda;
	 */
	public ContinuousBernoulliDistribution(final double a, final double b, final double lambda) {
		super(null);
		this.a=a;
		this.b=Math.max(this.a,b);
		this.lambda=Math.max(0.0001,Math.min(0.9999,lambda));
		this.Clambda=calcClambda(this.lambda);
		this.cdfFactor=(this.lambda==0.5)?1:(1/(2*this.lambda-1));
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public ContinuousBernoulliDistribution(final ContinuousBernoulliDistribution source) {
		this((source==null)?100:source.a,(source==null)?400:source.b,(source==null)?0.5:source.lambda);
	}

	/**
	 * Vorabbrechnung des Wertes C(lambda) für die Dichte
	 * @param lambda	Verteiulungsparameter &lambda;
	 * @return	Wert C(lambda)
	 */
	private static double calcClambda(final double lambda) {
		if (lambda==0.5) return 2;
		return 2*FastMath.atanh(1-2*lambda)/(1-2*lambda);
	}

	@Override
	public double density(double x) {
		if (x<a || x>b) return 0;
		if (a==b) return (x==a)?Double.POSITIVE_INFINITY:0;
		x=(x-a)/(b-a);
		return Clambda*Math.pow(lambda,x)*Math.pow(1-lambda,1-x)/(b-a);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<a) return 0;
		if (x>b) return 1;
		if (a==b) return 1;
		x=(x-a)/(b-a);
		if (lambda==0.5) return x;
		return cdfFactor*(Math.pow(lambda,x)*Math.pow(1-lambda,1-x)+lambda-1);
	}

	/**
	 * Berechnet den Erwartungswert der Verteilung auf [0,1]
	 * @param lambda	Verteiulungsparameter &lambda;
	 * @return	Erwartungswert der Verteilung auf [0,1]
	 */
	private static double getMean(final double lambda) {
		if (lambda==0.5) return 0.5;
		return lambda/(2*lambda-1)+1/(2*FastMath.atanh(1-2*lambda));
	}

	@Override
	public double getNumericalMean() {
		if (a==b) return a;
		return getMean(lambda)*(b-a)+a;
	}

	/**
	 * Berechnet die Varianz der Verteilung auf [0,1]
	 * @param lambda	Verteiulungsparameter &lambda;
	 * @return	Varianz  der Verteilung auf [0,1]
	 */
	private static double getVariance(final double lambda) {
		if (lambda==0.5) return 1.0/12.0;
		return -((1-lambda)*lambda)/(1-2*lambda)/(1-2*lambda)+1/(2*FastMath.atanh(1-2*lambda))/(2*FastMath.atanh(1-2*lambda));
	}

	@Override
	public double getNumericalVariance() {
		if (a==b) return 0;
		return getVariance(lambda)*(b-a)*(b-a);
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
	public ContinuousBernoulliDistribution clone() {
		return new ContinuousBernoulliDistribution(this);
	}
}
