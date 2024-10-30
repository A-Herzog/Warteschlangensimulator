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
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.ImportSettingsBuilder;
import scripting.java.OutputImpl;
import simulator.simparser.ExpressionCalc;

/**
 * Dieses Rechensymbol enthält intern Java-Code.
 * @author Alexander Herzog
 * @see ExpressionCalc#userFunctions
 */
public class CalcSymbolUserFunctionJava extends CalcSymbolPreOperator {
	/**
	 * Namen der Funktion
	 */
	private final String[] names;

	/**
	 * Java-Umgebung
	 */
	private final DynamicRunner javaRunner;

	/**
	 * Ausgabeobjekt das die Java-Ausgaben aufnimmt.
	 */
	private final StringBuilder output;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name der Funktion
	 * @param script	Auszuführendes Skript
	 */
	public CalcSymbolUserFunctionJava(final String name, final String script) {
		names=new String[]{name};
		javaRunner=DynamicFactory.getFactory().load(script,new ImportSettingsBuilder(null));
		output=new StringBuilder();
		javaRunner.parameter.output=new OutputImpl(s->output.append(s),false);
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		output.setLength(0);
		javaRunner.run();
		if (javaRunner.getStatus()!=DynamicStatus.OK) {
			throw new MathCalcError(DynamicFactory.getLongStatusText(javaRunner));
		} else {
			if (javaRunner.parameter.output.isOutputDouble()) return javaRunner.parameter.output.getOutputDouble();
			final String result=output.toString().trim();
			final Double D=NumberTools.getPlainDouble(result);
			if (D==null) throw new MathCalcError(this);
			return D;
		}
	}

	@Override
	protected boolean isDeterministic() {
		return false;
	}
}
