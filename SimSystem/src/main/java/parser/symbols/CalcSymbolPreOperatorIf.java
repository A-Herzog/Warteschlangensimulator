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
 * Liefert verschiedene Werte in Abhängigkeit davon, ob bestimmte
 * andere Werte echt größer 0 sind.<br><br>
 * Aufruf: Wenn(bedingung1,ergebnis1,bedingung2,ergebnis2,...,ergebnisSonst)<br>
 * Ist bedingung1&gt;0 so wird ergebnis1 geliefert.<br>
 * Ist bedingung2&gt;0 so wird ergebnis2 geliefert.<br>
 * Sonst wird ergebnisSonst geliefert.
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorIf extends CalcSymbolPreOperator {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorIf() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Wenn","If"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length%2!=1) throw error();

		int index=0;
		while (index<parameters.length-1) {
			if (parameters[index]>0) return parameters[index+1];
			index+=2;
		}

		return parameters[parameters.length-1];
	}
}
