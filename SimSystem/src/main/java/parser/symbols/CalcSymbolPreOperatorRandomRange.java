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
 * Zufallszahl im Bereich [a,b)<br>
 * @author Alexander Herzog
 */
public final class CalcSymbolPreOperatorRandomRange extends CalcSymbolPreOperator {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"RandomRange"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorRandomRange() {
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
	protected boolean isDeterministic() {
		return false;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=2) throw error();

		final double a=Math.min(parameters[0],parameters[1]);
		final double b=Math.max(parameters[0],parameters[1]);

		return a+calcSystem.getRandomDouble()*(b-a);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=2) return fallbackValue;

		final double a=Math.min(parameters[0],parameters[1]);
		final double b=Math.max(parameters[0],parameters[1]);

		return a+calcSystem.getRandomDouble()*(b-a);
	}
}
