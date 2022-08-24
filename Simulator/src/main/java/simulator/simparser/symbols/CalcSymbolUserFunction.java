/**
 * Copyright 2022 Alexander Herzog
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

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Dieses Rechensymbol enthält intern ein {@link ExpressionCalc}-System
 * über das eine innere Funktion berechnet werden kann.
 * @author Alexander Herzog
 * @see ExpressionCalc#userFunctions
 * @see ExpressionCalc#getUserFunctionCompiler(int)
 */
public class CalcSymbolUserFunction extends CalcSymbolPreOperator {
	/**
	 * Namen der Funktion
	 */
	private final String[] names;

	/**
	 * Interns Rechensystem
	 */
	private final ExpressionCalc calc;

	/**
	 * Anzahl der Parameter für die innere Funktion
	 */
	private final int parameterCount;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name der Funktion
	 * @param calc	Interns Rechensystem
	 * @param parameterCount	Anzahl der Parameter für die innere Funktion
	 * @see #compile(String, int, String)
	 */
	public CalcSymbolUserFunction(final String name, final ExpressionCalc calc, final int parameterCount) {
		names=new String[] {name};
		this.calc=calc;
		this.parameterCount=parameterCount;
	}

	/**
	 * Wandelt eine Zeichenkette in eine Funktion um
	 * @param name	Name der Funktion
	 * @param parameterCount	Anzahl der Parameter für die innere Funktion
	 * @param content	Zu interpretierende Zeichenkette
	 * @return	Liefert im Erfolgsfall ein {@link CalcSymbolUserFunction}-Objekt, sonst eine nullbasierende Zahl ({@link Integer}), die die Position des Fehlers beim Parsen angibt
	 */
	public static Object compile(final String name, final int parameterCount, final String content) {
		final ExpressionCalc compiler=ExpressionCalc.getUserFunctionCompiler(parameterCount);
		final int error=compiler.parse(content);
		if (error>0) return Integer.valueOf(error);
		return new CalcSymbolUserFunction(name,compiler,parameterCount);
	}

	/**
	 * Prüft ob eine Zeichenkette als Funktion interpretiert werden kann
	 * @param parameterCount	Anzahl der Parameter für die innere Funktion
	 * @param content	Zu interpretierende Zeichenkette
	 * @return	Liefert im Erfolgsfall -1, sonst die nullbasierende Position des Parser-Fehlers
	 */
	public static int test(final int parameterCount, final String content) {
		final Object result=compile("",parameterCount,content);
		if (result instanceof Integer) return (Integer)result;
		return -1;
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=parameterCount) throw error();

		SimulationData simData=null;
		RunDataClient client=null;
		if (calcSystem instanceof ExpressionCalc) {
			simData=((ExpressionCalc)calcSystem).getSimData();
			client=((ExpressionCalc)calcSystem).getCurrentClient();
		}

		return calc.calc(parameters,simData,client);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=parameterCount) return fallbackValue;

		SimulationData simData=null;
		RunDataClient client=null;
		if (calcSystem instanceof ExpressionCalc) {
			simData=((ExpressionCalc)calcSystem).getSimData();
			client=((ExpressionCalc)calcSystem).getCurrentClient();
		}

		return calc.calcOrDefault(parameters,simData,client,fallbackValue);
	}

	@Override
	protected boolean isDeterministic() {
		return false;
	}
}
