/**
 * Copyright 2021 Alexander Herzog
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

import org.apache.commons.math3.util.FastMath;

import parser.MathCalcError;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Fragt einen Wert der globalen per Skript einstellbaren Map ab.<br>
 * Der Parser erstellt bereits eine Übersetzungstabelle von (vom Nutzer angegebenen)
 * Zeichenketten zu Index-Werten. Dieser Index-Wert wird der Funktion als Parameter
 * übergeben.
 * @see ExpressionCalc#getTextContent(String, int)
 * @see SimulationData#runtimeData
 * @author Alexander Herzog
 */
public class CalcSymbolScriptMap extends CalcSymbolSimData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"§"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length!=1) throw error();

		final int index=(int)FastMath.round(parameters[0]);

		final String key=((ExpressionCalc)calcSystem).getTextContent("§",index);
		if (key==null) throw error();

		final SimulationData simData=((ExpressionCalc)calcSystem).getSimData();
		if (simData==null) throw error();

		final Object obj=simData.runtimeData.get().get(key);
		if (!(obj instanceof Number)) return 0; /* Kein passender Wert in Zuordnung soll als "0", nicht als Fehler interpretiert werden. */

		return ((Number)obj).doubleValue();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=1) return fallbackValue;

		final int index=(int)FastMath.round(parameters[0]);

		final String key=((ExpressionCalc)calcSystem).getTextContent("§",index);
		if (key==null) return fallbackValue;

		final SimulationData simData=((ExpressionCalc)calcSystem).getSimData();
		if (simData==null) return fallbackValue;

		final Object obj=simData.runtimeData.get().get(key);
		if (!(obj instanceof Number)) return fallbackValue;

		return ((Number)obj).doubleValue();
	}
}
