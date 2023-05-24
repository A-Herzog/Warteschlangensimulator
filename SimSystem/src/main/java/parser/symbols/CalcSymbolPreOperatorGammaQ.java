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

import org.apache.commons.math3.special.Gamma;

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Unvollständige, regularisierte untere Gamma-Funktion
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorGammaQ extends CalcSymbolPreOperator {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperatorGammaQ() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"RegularizedGammaQ"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=2) throw error();
		final double a=parameters[0];
		if (a==0) throw error();
		if (a<0 && (-a)%1==0.0) throw error();
		final double x=parameters[1];
		return Gamma.regularizedGammaQ(a,x);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=2) return fallbackValue;
		final double a=parameters[0];
		if (a==0) return fallbackValue;
		if (a<0 && (-a)%1==0.0) return fallbackValue;
		final double x=parameters[1];
		return Gamma.regularizedGammaQ(a,x);
	}
}
