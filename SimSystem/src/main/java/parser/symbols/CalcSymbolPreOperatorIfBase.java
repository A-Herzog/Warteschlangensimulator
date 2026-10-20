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
 * Basisklasse für alle If-Befehle
 * @author Alexander Herzog
 */
public abstract class CalcSymbolPreOperatorIfBase extends CalcSymbolPreOperator {
	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length%2!=1) throw error();

		int index=0;
		while (index<parameters.length-1) {
			if (calcLogic(parameters[index])) return parameters[index+1];
			index+=2;
		}

		return parameters[parameters.length-1];
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length%2!=1) return fallbackValue;

		int index=0;
		while (index<parameters.length-1) {
			if (calcLogic(parameters[index])) return parameters[index+1];
			index+=2;
		}

		return parameters[parameters.length-1];
	}

	/**
	 * Liefert <code>true</code>, wenn die Bedingung aufgrund dieses Zahlenwertes als erfüllt angenommen werden soll.
	 * @param value	Eingabewert
	 * @return	Liefert <code>true</code>, wenn der Eingabewert die Bedingung des Befehls erfüllt
	 */
	protected abstract boolean calcLogic(final double value);
}
