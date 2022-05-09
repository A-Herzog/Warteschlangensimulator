/**
 * Copyright 2022 Alexander Herzog
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
 * Zeta-Funktion
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorZeta extends CalcSymbolPreOperator {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Zeta"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorZeta() {
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
		if (parameters.length!=1) throw error();
		final double s=parameters[0];
		if (s<=1.0) throw error();
		return zeta(s);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=1) return fallbackValue;
		final double s=parameters[0];
		if (s<=1.0) return fallbackValue;
		return zeta(s);
	}

	/**
	 * Berechnet näherungsweise den Wert der Zeta-Funktion für <b>reelle, positive Argumente</b>
	 * @param s	Argument der Zeta-Funktion (muss &gt;1 sein)
	 * @return	Näherungsweiser Wert der Zeta-Funktion
	 */
	public static double zeta(final double s) {
		double z=0;
		long n=1;
		final double minusS=-s;
		if (s<1.8) { /* Unterhalb von 1,8 greift das Abbruchkriterium vor 5*10^6 sowieso nicht, daher können wir es auch einsparen. */
			while (n<5_000_000l) {
				z+=Math.pow(n,minusS);
				n++;
			}
		} else {
			while (n<5_000_000l) {
				final double add=Math.pow(n,minusS);
				z+=add;
				if (add<1E-12) break;
				n++;
			}
		}
		return z;
	}
}
