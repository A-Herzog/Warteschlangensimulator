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
 * Liefert zuf�llig (gem�� vorgegebener Raten) einen von mehreren Werten
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorRandomValues extends CalcSymbolPreOperator {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"RandomValues"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorRandomValues() {
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
		if (parameters.length==0 || parameters.length%2==1) throw error();
		return calcOrDefault(parameters,0);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length==0 || parameters.length%2==1) return fallbackValue;

		double rateSum=0;
		for (int i=0;i<parameters.length/2;i++) rateSum+=parameters[2*i];

		final double random=Math.random()*rateSum;
		rateSum=0;
		for (int i=0;i<parameters.length/2;i++) {
			rateSum+=parameters[2*i];
			if (rateSum>=random) return parameters[2*i+1];
		}
		return parameters[parameters.length-1];
	}

	@Override
	protected boolean isDeterministic() {
		return false;
	}
}
