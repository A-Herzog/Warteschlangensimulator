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

import parser.coresymbols.CalcSymbolMiddleOperator;

/**
 * Subtrastionsoperator
 * @author Alexander Herzog
 */
public final class CalcSymbolMiddleOperatorMinus extends CalcSymbolMiddleOperator {

	@Override
	protected Double calc(double left, double right) {
		return fastBoxedValue(left-right);
	}

	@Override
	protected double calcOrDefault(double left, double right, double fallbackValue) {
		return left-right;
	}

	@Override
	public String[] getNames() {
		return new String[]{"-"};
	}

	@Override
	public int getPriority() {
		return (left==null || right==null)?1:0;
	}
}
