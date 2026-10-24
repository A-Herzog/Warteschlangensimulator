/**
 * Copyright 2026 Alexander Herzog
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

import parser.coresymbols.CalcSymbolConst;

/**
 * Konstante phi=(1+sqrt(5))/2=1,6180339887499
 * @author Alexander Herzog
 */
public final class CalcSymbolConstPhi extends CalcSymbolConst {
	/**
	 * Konstruktor der Klasse<br>
	 * Konstante mit dem Wert phi=(1+sqrt(5))/2=1,6180339887499
	 */
	public CalcSymbolConstPhi() {
		setValue((1+Math.sqrt(5))/2);
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"phi"};

	@Override
	public String[] getNames() {
		return names;
	}
}
