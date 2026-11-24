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
 * Digamma-Funktion
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorDigamma extends CalcSymbolPreOperator { // TODO Einbauen
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorDigamma() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Digamma"};

	@Override
	public String[] getNames() {
		return names;
	}

	/**
	 * Bernoulli-Zahlen
	 * B2, B4, B6, ..., B14 (B1 = 0, B3 = B5 = ... = 0)
	 */
	private static final double[] B = {
			0, 1.0/6, -1.0/30, 1.0/42, -1.0/30, 5.0/66, -691.0/2730, 7.0/6
	};

	/**
	 * Berechnet eine Näherung der Digamma-Funktion
	 * @param z	Parameter z
	 * @return	Digamma-Funktion an der Stelle z
	 */
	public static double digamma(double z) {
		if (z<=0) throw new IllegalArgumentException("z must be > 0");

		/* Euler-Mascheroni-Konstante */
		if (z==1.0) return 0.577215664901532d;

		/* Rekursion, bis z > 5 */
		int n = 0;
		while (z<5) {
			z+=1;
			n++;
		}

		/* Asymptotische Reihe: psi(z) \approx ln(z) - 1/(2z) - sum(B_{2k}/(2k z^{2k})) */
		double result=Math.log(z)-1.0/(2*z);
		final double z2=z*z;
		double z_power=z2;

		for (int k=1;k<=7;k++) { /* bis z^14 */
			final double b=B[k];
			z_power*=z2;
			result-=b/(2*k*z_power);
		}

		/* Rückrechnen */
		while (n-->0) {
			z-=1;
			result-=1.0/z;
		}

		return result;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=1) throw error();
		final double z=parameters[0];
		if (z<=0) throw error();
		return digamma(z);

	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=1) return fallbackValue;
		final double z=parameters[0];
		if (z<=0) return fallbackValue;
		return digamma(z);
	}
}
