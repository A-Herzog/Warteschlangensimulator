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

import org.apache.commons.math3.util.FastMath;

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolMiddleOperator;

/**
 * Potenzoperator
 * @author Alexander Herzog
 */
public final class CalcSymbolMiddleOperatorPower extends CalcSymbolMiddleOperator {

	@Override
	protected double calc(double left, double right) throws MathCalcError {
		if (right<0) throw error();
		return FastMath.pow(left,right);
	}

	@Override
	protected double calcOrDefault(double left, double right, double fallbackValue) {
		if (right<0) return fallbackValue;
		return FastMath.pow(left,right);
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"^"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	public int getPriority() {
		return (left==null || right==null)?3:0;
	}
}
