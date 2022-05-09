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

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Logarithmus dualis (2er Logarithmus)
 * @author Alexander Herzog
 */
public final class CalcSymbolPreOperatorLd extends CalcSymbolPreOperator {
	/**
	 * Vorab berechneter Wert log(2) zur schnellen
	 * Umwandlung des natürlichen zum 2er-Logarithmus.
	 */
	private static final double log2=Math.log(2);

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorLd() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=1) throw error();
		if (parameters[0]<=0) throw error();
		return Math.log(parameters[0])/log2;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=1) return fallbackValue;
		if (parameters[0]<=0) return fallbackValue;
		return Math.log(parameters[0])/log2;
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"ld"};

	@Override
	public String[] getNames() {
		return names;
	}
}
