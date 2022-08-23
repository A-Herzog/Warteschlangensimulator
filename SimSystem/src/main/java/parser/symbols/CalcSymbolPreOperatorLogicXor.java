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
 * Liefert 1, wenn die Anzahl der übergebenen Parameter,
 * deren Werte ungleich 0 sind, ungerade ist, sonst 0.
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorLogicXor extends CalcSymbolPreOperator {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"xor"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorLogicXor() {
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
		if (parameters.length==0) throw error();
		int count=0;
		for (double d: parameters) if (Math.abs(d)>=10E-10) count++;
		return (count%2==1)?1.0:0.0;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length==0) return fallbackValue;
		int count=0;
		for (double d: parameters) if (Math.abs(d)>=10E-10) count++;
		return (count%2==1)?1.0:0.0;
	}
}
