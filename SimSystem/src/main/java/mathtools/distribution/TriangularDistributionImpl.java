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
 * Implementierung einer Dreiecksverteilung
 * @author Alexander Herzog
 * @see AbstractRealDistribution
 */
public final class TriangularDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable, DistributionWithRandom {
	private static final long serialVersionUID = -4353370852835823187L;

	/**
	 * Untere Grenze des Trägers
	 */
	public final double lowerBound;

	/**
	 * x-Wert des Hochpunkts des Dreiecks
	 */
	public final double mostLikelyX;

	/**
	 * y-Wert des Hochpunkts des Dreiecks
	 */
	public final double mostLikelyY;

	/**
	 * Obere Grenze des Trägers
	 */
	public final double upperBound;

	/**
	 * Vorberechneter Wert zur Beschleunigung der
	 * Berechnung der inversen Verteilungsfunktion.
	 * @see #inverseCumulativeProbability(double)
	 */
	private final double caDIVba;

	/**
	 * Vorberechneter Wert zur Beschleunigung der
	 * Berechnung der inversen Verteilungsfunktion.
	 * @see #inverseCumulativeProbability(double)
	 */
	private final double baMULca;

	/**
	 * Vorberechneter Wert zur Beschleunigung der
	 * Berechnung der inversen Verteilungsfunktion.
	 * @see #inverseCumulativeProbability(double)
	 */
	private final double baMULbc;

	/**
	 * Konstruktor der Klasse
	 * @param lowerBound	Untere Grenze des Trägers
	 * @param mostLikely	x-Wert des Hochpunkt des Dreiecks
	 * @param upperBound	Obere Grenze des Trägers
	 */
	public TriangularDistributionImpl(final double lowerBound, final double mostLikely, final double upperBound) {
		super(null);
		this.lowerBound=lowerBound;
		this.upperBound=Math.max(lowerBound+0.0001,upperBound);
		mostLikelyX=Math.max(this.lowerBound,Math.min(this.upperBound,mostLikely));
		mostLikelyY=2.0/(this.upperBound-this.lowerBound);

		final double a=lowerBound;
		final double b=upperBound;
		final double c=mostLikelyX;

		caDIVba=(c-a)/(b-a);
		baMULca=(b-a)*(c-a);
		baMULbc=(b-a)*(b-c);
	}

	@Override
	public double density(double x) {
		if (x<=lowerBound || x>=upperBound) return 0;

		final double a=lowerBound;
		final double b=upperBound;
		final double c=mostLikelyX;

		/* https://de.wikipedia.org/wiki/Dreiecksverteilung */
		if (x<c) {
			return 2*(x-a)/(b-a)/(c-a);
		} else if (x==c) {
			return 2/(b-a);
		} else {
			return 2*(b-x)/(b-a)/(b-c);
		}
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=lowerBound) return 0;
		if (x>=upperBound) return 1;

		final double a=lowerBound;
		final double b=upperBound;
		final double c=mostLikelyX;

		/* https://de.wikipedia.org/wiki/Dreiecksverteilung */
		if (x<c) {
			return (x-a)*(x-a)/(b-a)/(c-a);
		} else if (x==c) {
			return (c-a)/(b-a);
		} else {
			return 1-(b-x)*(b-x)/(b-a)/(b-c);
		}
	}

	@Override
	public double inverseCumulativeProbability(final double p) {
		if (p<0) return -Double.MAX_VALUE;
		if (p>1) return Double.MAX_VALUE;

		/* https://de.wikipedia.org/wiki/Dreiecksverteilung */
		final double a=lowerBound;
		final double b=upperBound;
		if (p<=caDIVba) { /* caDIVba = (c-a)/(b-a) */
			return a+Math.sqrt(p*baMULca); /* baMULca = (b-a)*(c-a) */
		} else {
			return b-Math.sqrt(baMULbc*(1-p)); /* baMULbc = (b-a)*(b-c) */
		}
	}

	@Override
	public TriangularDistributionImpl clone() {
		return new TriangularDistributionImpl(lowerBound,mostLikelyX,upperBound);
	}

	@Override
	public double getNumericalMean() {
		/* https://de.wikipedia.org/wiki/Dreiecksverteilung */
		final double A=lowerBound;
		final double B=upperBound;
		final double C=mostLikelyX;
		return (A+B+C)/3;

		/*
		final double A=lowerBound;
		final double B=upperBound;
		final double Px=mostLikelyX;
		final double Py=mostLikelyY;
		return Py/6*((2*Px*Px*Px-3*A*Px*Px+A*A*A)/(Px-A)+3*B-3*Px-(2*B*B*B-3*Px*B*B+Px*Px*Px)/(B-Px));
		 */
	}

	@Override
	public double getNumericalVariance() {
		/* https://de.wikipedia.org/wiki/Dreiecksverteilung */
		final double A=lowerBound;
		final double B=upperBound;
		final double C=mostLikelyX;
		return (A*A+B*B+C*C-A*B-A*C-B*C)/18;
	}

	@Override
	public double getSupportLowerBound() {
		return lowerBound;
	}

	@Override
	public double getSupportUpperBound() {
		return upperBound;
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
	public double random(final RandomGenerator generator) {
		return inverseCumulativeProbability(generator.nextDouble());
	}
}
