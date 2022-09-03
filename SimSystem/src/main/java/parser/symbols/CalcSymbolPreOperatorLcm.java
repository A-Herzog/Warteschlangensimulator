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
 * Kleinstes gemeinsames Vielfaches
 * @author Alexander Herzog
 *
 */
public class CalcSymbolPreOperatorLcm extends CalcSymbolPreOperator {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"lcm","kgv"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorLcm() {
		/*
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	/**
	 * Berechnet das kgV der beiden als Parameter angegebenen Zahlen.<br>
	 * (Die Zahlen werden dabei wenn n�tig zu Ganzzahlen gerundet und das Vorzeichen wird verworfen.)
	 * @param a	Zahl 1 f�r das kgV
	 * @param b	Zahl 2 f�r das kgV
	 * @return	kgV der beiden Zeilen
	 */
	public static double lcm(final double a, final double b) {
		final double gcd=CalcSymbolPreOperatorGcd.gcd(a,b);
		if (gcd==0.0) return 0.0;
		return Math.abs(a*b)/gcd;
	}


	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length==0) throw error();
		double result=parameters[0];
		for (int i=1;i<parameters.length;i++) result=lcm(result,parameters[i]);
		return result;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length==0) return fallbackValue;
		double result=parameters[0];
		for (int i=1;i<parameters.length;i++) result=lcm(result,parameters[i]);
		return result;
	}
}
