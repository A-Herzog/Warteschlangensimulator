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
 * Cosinus-Verteilung<br>
 * <pre>
 * f(x)=1/(b-a)*[1+cos(2*pi*(x-a)/(b-a)-pi)]
 * F(x)=1/(2*pi*(b-a))*(2*pi*(x-a)-(b-a)*sin(2*pi*(x-a)/(b-a)))
 * E[X]=(a+b)/2
 * E[X^2]=(a^2+a*b+b^2)/3-(b-a)^2/(2*pi^2)
 * Median=(a+b)/2
 * Mode=(a+b)/2
 * Var[X]=(pi^2-6)*(a-b)^2/(12*pi^2)
 * Integrate[1/(b-a)*(1+Cos[2*Pi*(x-a)/(b-a)-Pi]),x]
 * Integrate[x*(1/(b-a)*(1+Cos[2*Pi*(x-a)/(b-a)-Pi])),{x,a,b}]
 * Integrate[x^2*(1/(b-a)*(1+Cos[2*Pi*(x-a)/(b-a)-Pi])),{x,a,b}]
 * </pre>
 * @see <a href="https://en.wikipedia.org/wiki/Raised_cosine_distribution">https://en.wikipedia.org/wiki/Raised_cosine_distribution</a>
 * @author Alexander Herzog
 * @version 1.0
 */
public class CosineDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8707702684789955027L;

	/**
	 * Untere Bereichtsgrenze
	 */
	public final double a;

	/**
	 * Obere Bereichtsgrenze
	 */
	public final double b;

	/**
	 * Konstruktor der Klasse
	 * @param a	Untere Bereichtsgrenze
	 * @param b	Obere Bereichtsgrenze
	 */
	public CosineDistributionImpl(final double a, final double b) {
		super(null);
		this.a=a;
		this.b=Math.max(a,b);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public CosineDistributionImpl(final CosineDistributionImpl source) {
		super(null);
		if (source==null) {
			a=0;
			b=1;
		} else {
			a=source.a;
			b=source.b;
		}
	}

	@Override
	public double density(double x) {
		if (a==b) return (x==a)?Double.NEGATIVE_INFINITY:0;
		if (x<a || x>b) return 0;
		return 1/(b-a)*(1+Math.cos(2*Math.PI*(x-a)/(b-a)-Math.PI));
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<a) return 0;
		if (x>=b) return 1;
		return 1/(2*Math.PI*(b-a))*(2*Math.PI*(x-a)-(b-a)*Math.sin(2*Math.PI*(x-a)/(b-a)));
	}

	@Override
	public CosineDistributionImpl clone() {
		return new CosineDistributionImpl(a,b);
	}

	@Override
	public double getNumericalMean() {
		return (a+b)/2;
	}

	@Override
	public double getNumericalVariance() {
		return (Math.PI*Math.PI-6)*(a-b)*(a-b)/(12*Math.PI*Math.PI);
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
}
