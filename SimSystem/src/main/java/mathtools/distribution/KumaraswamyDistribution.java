/**
 * Copyright 2024 Alexander Herzog
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
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.special.Gamma;

/**
 * Klasse zur Abbildung der Kumaraswamy-Verteilung
 * @author Alexander Herzog
 */
public class KumaraswamyDistribution extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5073494121705653463L;

	/**
	 * Parameter 1
	 */
	public final double a;

	/**
	 * Parameter 2
	 */
	public final double b;

	/**
	 * Untere Grenze des Trägers
	 */
	public final double c;

	/**
	 * Obere Grenze des Trägers
	 */
	public final double d;

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
	 * Konstruktor
	 * @param a Erster Parameter
	 * @param b Zweiter Parameter
	 * @param c	Untere Grenze des Trägers
	 * @param d	Obere Grenze des Trägers
	 */
	public KumaraswamyDistribution(final double a, final double b, final double c, final double d) {
		super(null);
		this.a=Math.max(0.0001,a);
		this.b=Math.max(0.0001,b);
		this.c=c;
		this.d=Math.max(this.c+0.0001,d);

		final double m1=this.b*Gamma.gamma(1+1/this.a)*Gamma.gamma(this.b)/Gamma.gamma(1+1/this.a+this.b);
		mean=m1*(this.d-this.c)+this.c;
		final double m2=this.b*Gamma.gamma(1+2/this.a)*Gamma.gamma(this.b)/Gamma.gamma(1+2/this.a+this.b);
		variance=(m2-m1*m1)*(this.d-this.c)*(this.d-this.c);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public KumaraswamyDistribution(final KumaraswamyDistribution source) {
		this((source==null)?0:source.a,(source==null)?10:source.b,(source==null)?0:source.c,(source==null)?10:source.d);
	}

	@Override
	public double density(double x) {
		if (x<c || x>d) return 0;
		x=(x-c)/(d-c);
		return a*b*Math.pow(x,a-1)*Math.pow(1-Math.pow(x,a),b-1);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=c) return 0;
		if (x>=d) return 1;
		x=(x-c)/(d-c);
		return 1-Math.pow(1-Math.pow(x,a),b);
	}

	@Override
	public KumaraswamyDistribution clone() {
		return new KumaraswamyDistribution(a,b,c,d);
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
		return c;
	}

	@Override
	public double getSupportUpperBound() {
		return d;
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
	public double inverseCumulativeProbability(final double p) throws OutOfRangeException {
		if (p<0.0 || p>1.0) throw new OutOfRangeException(p,0,1);

		if (p==0.0) return getSupportLowerBound();
		if (p==1.0) return getSupportUpperBound();

		/*
		p=1-(1-x^a)^b
		1-x^a=
		(1-(1-p)^(1/b))^(1/a)=x
		 */
		return Math.pow(1-Math.pow(1-p,1/b),1/a)*(d-c)+c;
	}

	@Override
	public double random(final RandomGenerator generator) {
		return inverseCumulativeProbability(generator.nextDouble());
	}
}
