/**
 * Copyright 2024 Alexander Herzog
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
 * Konstante tau=2*pi=6,2831853071796
 * @author Alexander Herzog
 */
public final class CalcSymbolConstTau extends CalcSymbolConst {
	/**
	 * Konstruktor der Klasse<br>
	 * Konstante mit dem Wert tau=6,2831853071796
	 */
	public CalcSymbolConstTau() {
		setValue(2*Math.PI);
		/* Math.TAU ist erst ab Java 19 verfügbar */
	}

	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"tau"};

	@Override
	public String[] getNames() {
		return names;
	}
}
