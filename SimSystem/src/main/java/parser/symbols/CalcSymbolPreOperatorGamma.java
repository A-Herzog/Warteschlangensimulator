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

import org.apache.commons.math3.special.Gamma;

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Gamma-Funktion
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorGamma extends CalcSymbolPreOperator {

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=1) throw error();
		double d=parameters[0];
		if (d==0) throw error();
		if (d<0 && (-d)%1==0.0) throw error();
		return Gamma.gamma(d);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=1) return fallbackValue;
		double d=parameters[0];
		if (d==0) return fallbackValue;
		if (d<0 && (-d)%1==0.0) return fallbackValue;
		return Gamma.gamma(d);
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Gamma"};

	@Override
	public String[] getNames() {
		return names;
	}

}
