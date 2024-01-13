/**
 * Copyright 2023 Alexander Herzog
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
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Im Falle von einem Parameter:<br>
 * Liefert die Anzahl der im Mittel vorhandenen Bediener in der angegebenen Ressource (1. Parameter).<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die Anzahl der im Mittel vorhandenen Bedienern in allen Ressourcen zusammen.
 * @author Alexander Herzog
 */
public class CalcSymbolResourceCount_avg extends CalcSymbolSimData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"resource_count_avg","resource_count_average","MR_avg","MR_average","resource_capacity_avg","resource_capacity_average"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolResourceCount_avg() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		final SimulationData simData=getSimData();
		if (simData==null) throw error();

		if (parameters.length==0) {
			return simData.runData.resources.getAllCountAverage(simData);
		}

		if (parameters.length==1) {
			final int id=(int)FastMath.round(parameters[0])-1;
			return simData.runData.resources.getCountAverage(id,simData);
		}

		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		final SimulationData simData=getSimData();
		if (simData==null) return fallbackValue;

		if (parameters.length==0) {
			return simData.runData.resources.getAllCountAverage(simData);
		}

		if (parameters.length==1) {
			final int id=(int)FastMath.round(parameters[0])-1;
			return simData.runData.resources.getCountAverage(id,simData);
		}

		return fallbackValue;
	}
}
