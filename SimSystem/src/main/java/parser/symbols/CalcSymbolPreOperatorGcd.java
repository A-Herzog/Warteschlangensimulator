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
 * Gr��ter gemeinsamer Teiler
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorGcd extends CalcSymbolPreOperator {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"gcd","ggt"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorGcd() {
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
	 * Berechnet den ggT der beiden als Parameter angegebenen Zahlen.<br>
	 * (Die Zahlen werden dabei wenn n�tig zu Ganzzahlen gerundet und das Vorzeichen wird verworfen.)
	 * @param a	Zahl 1 f�r den ggT
	 * @param b	Zahl 2 f�r den ggT
	 * @return	ggT der beiden Zeilen
	 */
	public static double gcd(final double a, final double b) {
		long intA=Math.abs(Math.round(a));
		long intB=Math.abs(Math.round(b));
		long r;
		while (intA%intB>0) {
			r=intA%intB;
			intA=intB;
			intB=r;
		}
		return intB;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length==0) throw error();
		double result=parameters[0];
		for (int i=1;i<parameters.length;i++) result=gcd(result,parameters[i]);
		return result;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length==0) return fallbackValue;
		double result=parameters[0];
		for (int i=1;i<parameters.length;i++) result=gcd(result,parameters[i]);
		return result;
	}
}
