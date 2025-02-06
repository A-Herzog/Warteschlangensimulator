/**
 * Copyright 2023 Alexander Herzog
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
 * Differenz aus größtem und kleinstem Wert der Datenreihe, die aus den übergebenen Parametern gebildet wird
 * @author Alexander Herzog
 * @see CalcSymbolPreOperatorMin
 * @see CalcSymbolPreOperatorMax
 */
public final class CalcSymbolPreOperatorRange extends CalcSymbolPreOperator {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorRange() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Range","Spannweite"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length==0) throw error();
		double min=parameters[0];
		double max=parameters[0];
		for (double d: parameters) {
			if (d<min) min=d;
			if (d>max) max=d;
		}
		return max-min;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length==0) return fallbackValue;
		double min=parameters[0];
		double max=parameters[0];
		for (double d: parameters) {
			if (d<min) min=d;
			if (d>max) max=d;
		}
		return max-min;
	}
}
