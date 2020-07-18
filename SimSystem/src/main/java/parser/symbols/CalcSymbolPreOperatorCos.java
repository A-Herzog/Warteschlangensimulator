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
 * Cosinus
 * @author Alexander Herzog
 */
public final class CalcSymbolPreOperatorCos extends CalcSymbolPreOperator {
	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=1) throw error();
		final double d=Math.cos(parameters[0]);
		return (Math.abs(d)<2E-16)?0.0:d;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=1) return fallbackValue;
		final double d=Math.cos(parameters[0]);
		return (Math.abs(d)<2E-16)?0.0:d;
	}

	@Override
	public String[] getNames() {
		return new String[]{"cosinus","cos"};
	}

}
