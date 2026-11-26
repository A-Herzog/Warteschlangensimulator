/**
 * Copyright 2026 Alexander Herzog
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

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Liefert einen Wert der harmonischen Reihe.
 * @author Alexander Herzog
 */
public final class CalcSymbolPreOperatorHarmonic extends CalcSymbolPreOperator {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorHarmonic() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"harmonic"};

	@Override
	public String[] getNames() {
		return names;
	}

	/**
	 * Berechnet den Wert der harmonischen Reihe bis n.
	 * @param n	Parameter n
	 * @return	H(n)
	 */
	public static double calcHarmonic(final int n) {
		if (n<=1_000_000) {
			double sum=0;
			for (int i=1;i<=n;i++) sum+=1.0/i;
			return sum;
		} else {
			/* Approximation für große n */
			final double gamma=0.577215664901532;
			return Math.log(n) + gamma + 1/(2*n) - 1/(12*Math.pow(n,2)) + 1/(120*Math.pow(n,4)) + 1/(252*Math.pow(n,6)) - 1/(240*Math.pow(n,8)) + 1/(132*Math.pow(n,10));
		}
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=1) throw error();
		final int n=(int)Math.round(parameters[0]);
		if (n<1) throw error();
		return calcHarmonic(n);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=1) return fallbackValue;
		final int n=(int)Math.round(parameters[0]);
		if (n<1) return fallbackValue;
		return calcHarmonic(n);
	}
}
