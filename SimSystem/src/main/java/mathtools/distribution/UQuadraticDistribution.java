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

/**
 * Klasse zur Abbildung der U-quadratischen Verteilung
 * @author Alexander Herzog
 */
public class UQuadraticDistribution extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5840683091205876418L;

	/**
	 * Untere Grenze des Trägers
	 */
	public final double a;

	/**
	 * Obere Grenze des Trägers
	 */
	public final double b;

	/**
	 * Vorab berechneter Faktor 1 der Dichte
	 */
	private final double pdfFactor1;

	/**
	 * Vorab berechneter Faktor 2 der Dichte
	 */
	private final double pdfFactor2;

	/**
	 * Vorab berechneter Faktor 1 der Verteilungsfunktion
	 */
	private final double cdfFactor1;

	/**
	 * Vorab berechneter Faktor 2 der Verteilungsfunktion
	 */
	private final double cdfFactor2;

	/**
	 * Vorab berechneter Faktor 3 der Verteilungsfunktion
	 */
	private final double cdfFactor3;

	/**
	 * Vorab berechneter Faktor für die Pseudozufallszahlen
	 */
	private final double randomFactor;

	/**
	 * Konstruktor
	 * @param a	Untere Grenze des Trägers
	 * @param b	Obere Grenze des Trägers
	 */
	public UQuadraticDistribution(final double a, final double b) {
		super(null);
		this.a=a;
		this.b=Math.max(a+0.0001,b);
		pdfFactor1=12.0/(this.b-this.a)/(this.b-this.a)/(this.b-this.a);
		pdfFactor2=(this.a+this.b)/2.0;
		cdfFactor1=4.0/(this.b-this.a)/(this.b-this.a)/(this.b-this.a);
		cdfFactor2=(this.a+this.b)/2.0;
		cdfFactor3=((this.a+this.b)/2.0-this.a)*((this.a+this.b)/2.0-this.a)*((this.a+this.b)/2.0-this.a);
		randomFactor=(this.b-this.a)*(this.b-this.a)*(this.b-this.a)/4.0;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public UQuadraticDistribution(final UQuadraticDistribution source) {
		this((source==null)?0:source.a,(source==null)?10:source.b);
	}

	@Override
	public double density(double x) {
		if (x<a || x>b) return 0;
		return pdfFactor1*(x-pdfFactor2)*(x-pdfFactor2);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=a) return 0;
		if (x>=b) return 1;
		return cdfFactor1*((x-cdfFactor2)*(x-cdfFactor2)*(x-cdfFactor2)+cdfFactor3);
	}

	@Override
	public UQuadraticDistribution clone() {
		return new UQuadraticDistribution(a,b);
	}

	@Override
	public double getNumericalMean() {
		return (a+b)/2;
	}

	@Override
	public double getNumericalVariance() {
		return 3.0/20.0*(b-a)*(b-a);
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
	public double inverseCumulativeProbability(final double p) throws OutOfRangeException {
		if (p<0.0 || p>1.0) throw new OutOfRangeException(p,0,1);

		if (p==0.0) return getSupportLowerBound();
		if (p==1.0) return getSupportUpperBound();

		/*
		p=4/(b-a)^3*[(x-(a+b)/2)^3+((a+b)/2-a)^3]
		p*(b-a)^3/4-((a+b)/2-a)^3=(x-(a+b)/2)^3
		[p*(b-a)^3/4-((a+b)/2-a)^3]^(1/3)+(a+b)/2=x
		 */

		final double z=p*randomFactor-cdfFactor3;
		return Math.signum(z)*Math.pow(Math.abs(z),1.0/3.0)+cdfFactor2;
	}

	@Override
	public double random(final RandomGenerator generator) {
		return inverseCumulativeProbability(generator.nextDouble());
	}
}
