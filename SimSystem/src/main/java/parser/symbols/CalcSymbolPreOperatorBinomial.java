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
package parser.symbols;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Binomialkoeffizient
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorBinomial extends CalcSymbolPreOperator {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"binom","binomial","binomialkoeffizient","binomialcoefficient"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorBinomial() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=2) throw error();

		try {
			final int n=(int)FastMath.round(parameters[0]);
			final int k=(int)FastMath.round(parameters[1]);
			return binomialCoefficient(n,k);
		} catch (Exception e) {
			throw error();
		}
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=2) return fallbackValue;

		try {
			final int n=(int)FastMath.round(parameters[0]);
			final int k=(int)FastMath.round(parameters[1]);
			return binomialCoefficient(n,k);
		} catch (Exception e) {
			return fallbackValue;
		}
	}

	/**
	 * Liefert den Binomialkoeffizienten "n über k"
	 * @param n	n
	 * @param k	k
	 * @return	Binomialkoeffizient oder -1, wenn der Wert nicht berechnet werden konnte
	 * @throws NotPositiveException if {@code n < 0}.
	 * @throws NumberIsTooLargeException if {@code k > n}.
	 * @throws MathArithmeticException if the result is too large to be
	 * represented by a long integer.
	 */
	public static double binomialCoefficient(final int n, int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
		if (n==0) return (k==0)?1.0:0.0;
		if (k<0 || k>n) return 0.0;

		if (n>0) {
			if (k==0 || k==n) return 1.0;
			double prod=1;
			if (k>n/2) k=n-k;
			for (int i=1;i<=k;i++) {
				prod*=(n-i+1)/((double)i);
			}
			return prod;
		}

		return CombinatoricsUtils.binomialCoefficient(n,k);
	}
}
