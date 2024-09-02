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

import mathtools.Functions;
import parser.symbols.CalcSymbolPreOperatorBinomial;

/**
 * Klasse zur Abbildung der Irwin-Hall-Verteilung
 * @author Alexander Herzog
 */
public class IrwinHallDistribution extends AbstractRealDistribution implements Cloneable, Serializable {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=416452137962590289L;

	/**
	 * Parameter der Verteilung
	 */
	public final int n;

	/**
	 * Vorfaktor zur Berechnung der Dichte
	 * @see #density(double)
	 */
	private final double pdfFactor;

	/**
	 * Vorfaktor zur Berechnung der Verteilungsfunktion
	 * @see #cumulativeProbability(double)
	 */
	private final double cdfFactor;

	/**
	 * Konstruktor
	 * @param n	Parameter
	 */
	public IrwinHallDistribution(final double n) {
		super(null);
		this.n=(int)Math.min(30,Math.max(1,Math.round(n)));
		pdfFactor=1.0/Functions.getFactorial(this.n-1);
		cdfFactor=1.0/Functions.getFactorial(this.n);
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierende Ausgangsverteilung
	 */
	public IrwinHallDistribution(final IrwinHallDistribution source) {
		this((source==null)?1:source.n);
	}

	@Override
	public double density(double x) {
		if (x<0 || x>n) return 0;
		if (n>25) {
			if (x>n*0.8) return 0;
		}
		double sum=0;
		final int upperBound=(int)Math.floor(x);
		for (int k=0;k<=upperBound;k++) {
			double d=CalcSymbolPreOperatorBinomial.binomialCoefficient(n,k)*Math.pow(x-k,n-1);
			if (k%2!=0) /* (k%2==1) wird von SpotBugs als Fehler gesehen, da dies für negatives k nicht funktioniert. - Auch wenn k hier überhaupt nicht negativ sein kann. */
				d=-d;
			sum+=d;
		}
		return pdfFactor*sum;
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=0) return 0;
		if (x>=n) return 1;
		if (n>25) {
			if (x>n*0.8) return 1;
		}
		double sum=0;
		final int upperBound=(int)Math.floor(x);
		for (int k=0;k<=upperBound;k++) {
			double d=CalcSymbolPreOperatorBinomial.binomialCoefficient(n,k)*Math.pow(x-k,n);
			if (k%2!=0) /* (k%2==1) wird von SpotBugs als Fehler gesehen, da dies für negatives k nicht funktioniert. - Auch wenn k hier überhaupt nicht negativ sein kann. */
				d=-d;
			sum+=d;
		}
		return cdfFactor*sum;
	}

	@Override
	public IrwinHallDistribution clone() {
		return new IrwinHallDistribution(n);
	}

	@Override
	public double getNumericalMean() {
		return n/2.0;
	}

	@Override
	public double getNumericalVariance() {
		return n/12.0;
	}

	@Override
	public double getSupportLowerBound() {
		return 0;
	}

	@Override
	public double getSupportUpperBound() {
		return n;
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
