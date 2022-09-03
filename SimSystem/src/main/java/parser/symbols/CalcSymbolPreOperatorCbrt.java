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
 * Kubikwurzel
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorCbrt extends CalcSymbolPreOperator {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"cbrt","Kubikwurzel"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorCbrt() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	/**
	 * Vorab berechneter Wert 1/3 für den Exponenten
	 * (um die Division einzusparen)
	 */
	private static final double oneThird=1.0/3.0;

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=1) throw error();
		final double x=parameters[0];
		if (x==0.0) return 0.0;
		if (x<0) return -Math.pow(-x,oneThird);
		return Math.pow(x,oneThird);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=1) return fallbackValue;
		final double x=parameters[0];
		if (x==0.0) return 0.0;
		if (x<0) return -Math.pow(-x,oneThird);
		return Math.pow(x,oneThird);
	}
}
