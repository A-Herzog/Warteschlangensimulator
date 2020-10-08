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

import mathtools.Functions;
import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPostOperator;

/**
 * Fakultät als nachgestelltes Symbol ("!")
 * @author Alexander Herzog
 */
public final class CalcSymbolPostOperatorFactorial extends CalcSymbolPostOperator {

	@Override
	protected double calc(double parameter) throws MathCalcError {
		double signum=Math.signum(parameter);
		if (signum==0.0) signum=1;
		return signum*Math.round(Functions.getFactorial((int)Math.round(Math.abs(parameter))));
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"!"};

	@Override
	public String[] getNames() {
		return names;
	}

}
