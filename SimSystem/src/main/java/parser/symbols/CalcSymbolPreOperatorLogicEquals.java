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
package parser.symbols;

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Liefert 1, wenn alle �bergebenen Parameter denselben Wert haben, sonst 0.
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorLogicEquals extends CalcSymbolPreOperator {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"equals","gleich"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorLogicEquals() {
		/*
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length==0) throw error();
		final double value=parameters[0];
		for (int i=1;i<parameters.length;i++) if (parameters[i]!=value) return 0.0;
		return 1.0;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length==0) return fallbackValue;
		final double value=parameters[0];
		for (int i=1;i<parameters.length;i++) if (parameters[i]!=value) return 0.0;
		return 1.0;
	}
}
