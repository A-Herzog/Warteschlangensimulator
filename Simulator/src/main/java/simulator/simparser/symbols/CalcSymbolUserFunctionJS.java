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
package simulator.simparser.symbols;

import mathtools.NumberTools;
import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;
import scripting.js.JSRunComplexScript;
import simulator.simparser.ExpressionCalc;

/**
 * Dieses Rechensymbol enthält intern ein JS-Skript.
 * @author Alexander Herzog
 * @see ExpressionCalc#userFunctions
 */
public class CalcSymbolUserFunctionJS extends CalcSymbolPreOperator {
	/**
	 * Namen der Funktion
	 */
	private final String[] names;

	/**
	 * Auszuführendes Skript
	 */
	private final String script;

	/**
	 * Skript-Umgebung
	 */
	private final JSRunComplexScript scriptRunner;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name der Funktion
	 * @param script	Auszuführendes Skript
	 */
	public CalcSymbolUserFunctionJS(final String name, final String script) {
		names=new String[]{name};
		this.script=script;
		scriptRunner=new JSRunComplexScript(null,null,null);
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (!scriptRunner.run(script,parameters)) throw error();
		if (scriptRunner.isOutputDouble()) return scriptRunner.getLastDouble();
		final Double D=NumberTools.getDouble(scriptRunner.getResults());
		if (D==null) throw error();
		return D;
	}
}
