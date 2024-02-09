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
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Implementierung einer Trapezverteilung
 * @author Alexander Herzog
 * @see AbstractRealDistribution
 */
public final class TrapezoidDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-7613243047391568008L;

	/**
	 * Erster Parameter (Startpunkt des Anstiegs)
	 */
	public final double a;

	/**
	 * Zweiter Parameter (Beginn des horizontalen Bereichs)
	 */
	public final double b;

	/**
	 * Dritter Parameter (Startpunkt des Abstiegs)
	 */
	public final double c;

	/**
	 * Vierter Parameter (Ende des Tr‰gerbereichs)
	 */
	public final double d;

	/**
	 * Hˆhe des horizontalen Bereichs zwischen c und d
	 */
	public final double h;

	/**
	 * Erwartungswert
	 */
	public final double mean;

	/**
	 * Varianz
	 */
	public final double variance;

	/**
	 * F(b)
	 * @see #inverseCumulativeProbability(double)
	 */
	private final double pdfB;

	/**
	 * F(c)
	 * @see #inverseCumulativeProbability(double)
	 */
	private final double pdfC;

	/**
	 * Konstruktor
	 * @param a	Erster Parameter (Startpunkt des Anstiegs)
	 * @param b	Zweiter Parameter (Beginn des horizontalen Bereichs)
	 * @param c	Dritter Parameter (Startpunkt des Abstiegs)
	 * @param d	Vierter Parameter (Ende des Tr‰gerbereichs)
	 */
	public TrapezoidDistributionImpl(final double a, final double b, final double c, final double d) {
		super(null);

		this.a=a;
		this.b=Math.max(this.a,b);
		this.c=Math.max(this.b,c);
		this.d=Math.max(this.c,d);

		if (this.d>this.a) {
			h=2.0/(this.c+this.d-this.a-this.b);
			mean=calcMean(this.a,this.b,this.c,this.d);
			variance=calcVariance(this.a,this.b,this.c,this.d,mean);
			pdfB=(this.b-this.a)*h/2.0;
			pdfC=1-(this.d-this.c)*h/2.0;
		} else {
			h=10E10;
			mean=a;
			variance=0;
			pdfB=1;
			pdfC=1;
		}
	}

	/**
	 * Berechnet den Erwartungswert<br>
	 * a&le;b&le;c&le;d und a&ne;d muss bereits vorab sichergestellt sein.
	 * @param a	Erster Parameter (Startpunkt des Anstiegs)
	 * @param b	Zweiter Parameter (Beginn des horizontalen Bereichs)
	 * @param c	Dritter Parameter (Startpunkt des Abstiegs)
	 * @param d	Vierter Parameter (Ende des Tr‰gerbereichs)
	 * @return	Erwartungswert
	 */
	public static double calcMean(final double a, final double b, final double c, final double d) {
		final double part1, part2;
		if (c==d) part1=3*c*c; else part1=(d*d*d-c*c*c)/(d-c);
		if (a==b) part2=3*a*a; else part2=(b*b*b-a*a*a)/(b-a);
		return 1.0/3.0/(c+d-a-b)*(part1-part2); /* a=b=c=d ist bereits ausgeschlossen - sonst wird diese Methode nicht aufgerufen. Daher Division gefahrlos. */
	}

	/**
	 * Berechnet die Varianz<br>
	 * a&le;b&le;c&le;d und a&ne;d muss bereits vorab sichergestellt sein.
	 * @param a	Erster Parameter (Startpunkt des Anstiegs)
	 * @param b	Zweiter Parameter (Beginn des horizontalen Bereichs)
	 * @param c	Dritter Parameter (Startpunkt des Abstiegs)
	 * @param d	Vierter Parameter (Ende des Tr‰gerbereichs)
	 * @param mean	Erwartungswert
	 * @return	Varianz
	 */
	public static double calcVariance(final double a, final double b, final double c, final double d, final double mean) {
		final double part1, part2;
		if (c==d) part1=4*c*c*c; else part1=(d*d*d*d-c*c*c*c)/(d-c);
		if (a==b) part2=4*a*a*a; else part2=(b*b*b*b-a*a*a*a)/(b-a);
		return 1.0/6.0/(c+d-a-b)*(part1-part2)-mean*mean; /* a=b=c=d ist bereits ausgeschlossen - sonst wird diese Methode nicht aufgerufen. Daher Division gefahrlos. */
	}

	/**
	 * Berechnet die Schiefe<br>
	 * a&le;b&le;c&le;d, a&ne;d und sd&gt;0 muss bereits vorab sichergestellt sein.
	 * @param a	Erster Parameter (Startpunkt des Anstiegs)
	 * @param b	Zweiter Parameter (Beginn des horizontalen Bereichs)
	 * @param c	Dritter Parameter (Startpunkt des Abstiegs)
	 * @param d	Vierter Parameter (Ende des Tr‰gerbereichs)
	 * @param mean	Erwartungswert
	 * @param sd	Standardabweichung
	 * @return	Schiefe
	 */
	public static double calcSk(final double a, final double b, final double c, final double d, final double mean, final double sd) {
		final double part1, part2;
		if (c==d) part1=5*c*c*c*c; else part1=(d*d*d*d*d-c*c*c*c*c)/(d-c);
		if (a==b) part2=5*a*a*a*a; else part2=(b*b*b*b*b-a*a*a*a*a)/(b-a);
		final double EX3=1.0/10.0/(c+d-a-b)*(part1-part2);

		return (EX3-3*mean*sd*sd-mean*mean*mean)/(sd*sd*sd);
	}

	@Override
	public double density(double x) {
		/* Die Kleiner- oder Kleinergleich-Zeichen sind jeweils so gew‰hlt, dass auch bei a=b=c=d nichts schlimmes passiert. */

		/* Auﬂen */
		if (x<=a || x>=d) return 0.0;

		/* Anstieg */
		if (x>a && x<b) return h*(x-a)/(b-a);

		/* Abstieg */
		if (x>c && x<d) return h*(d-x)/(d-c);

		/* Mitte */
		return h;
	}

	@Override
	public double cumulativeProbability(double x) {
		/* Die Kleiner- oder Kleinergleich-Zeichen sind jeweils so gew‰hlt, dass auch bei a=b=c=d nichts schlimmes passiert. */

		/* Auﬂen */
		if (x>=d) return 1.0;
		if (x<=a) return 0.0;

		/* Anstieg */
		if (x>a && x<b) return h*(x-a)*(x-a)/(b-a)/2;

		/* Abstieg */
		if (x>c && x<d) return 1-h*(d-x)*(d-x)/(d-c)/2;

		/* Mitte */
		return h*(2*x-a-b)/2.0;
	}

	@Override
	public double inverseCumulativeProbability(final double p) {
		if (p<=0) return -Double.MAX_VALUE;
		if (p>=1) return Double.MAX_VALUE;

		if (p<pdfB) {
			/* Anstieg */
			if (a==b) return a;
			/* p=h*(x-a)^2/(b-a)/2  =>  p*2*(b-a)/h=(x-a)^2  =>  sqrt(p*2*(b-a)/h)+a=x */
			return Math.sqrt(p*2*(b-a)/h)+a;
		}

		if (p>pdfC) {
			/* Abstieg */
			if (c==d) return c;
			/* p=1-h*(d-x)^2/(d-c)/2  => 1-p=h*(d-x)^2/(d-c)/2  =>  (1-p)*2*(d-c)/h=(d-x)^2  =>  x=d-sqrt((1-p)*2*(d-c)/h) */
			return d-Math.sqrt((1-p)*2*(d-c)/h);
		}

		/* Mitte */
		if (b==c) return b;
		final double relativePosition=(p-pdfB)/(pdfC-pdfB); /* Position zwischen b und c */
		return b+(c-b)*relativePosition;
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
		return a;
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
	public TrapezoidDistributionImpl clone() {
		return new TrapezoidDistributionImpl(a,b,c,d);
	}

	@Override
	public double random(RandomGenerator generator) {
		return inverseCumulativeProbability(generator.nextDouble());
	}
}
