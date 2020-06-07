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

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Binomialkoeffizient
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorBinomial extends CalcSymbolPreOperator {
	@Override
	public String[] getNames() {
		return new String[]{"binom","binomial","binomialkoeffizient","binomialcoefficient"};
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=2) throw error();

		try {
			final int n=(int)FastMath.round(parameters[0]);
			final int k=(int)FastMath.round(parameters[1]);
			return CombinatoricsUtils.binomialCoefficient(n,k);
		} catch (Exception e) {
			throw error();
		}
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=2) return fallbackValue;

		try {
			final int n=(int)FastMath.round(parameters[0]);
			final int k=(int)FastMath.round(parameters[1]);
			return CombinatoricsUtils.binomialCoefficient(n,k);
		} catch (Exception e) {
			return fallbackValue;
		}
	}
}
