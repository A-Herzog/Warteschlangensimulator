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
package simulator.simparser.coresymbols;

import org.apache.commons.math3.util.FastMath;

import parser.MathCalcError;
import simulator.runmodel.SimulationData;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Basisklasse für Funktionen, die Ressourcendaten aus den Simulationsdaten auslesen.
 * @author Alexander Herzog
 * @see SimulationData
 * @see CalcSymbolSimData
 */
public abstract class CalcSymbolResourceData extends CalcSymbolSimData {
	/**
	 * Gibt an, ob der Rechenbefehl Daten über alle Ressourcengruppen hinweg enthält
	 * @return	Wird hier <code>true</code> geliefert, so muss {@link #calcAllResources(StatisticsTimePerformanceIndicator[])} definiert sein
	 */
	protected boolean hasAllResourceData() {
		return false;
	}

	/**
	 * Liefert Daten über alle Ressourcen hinweg.
	 * @param statistics	Einzel-Statistik-Objekte die zusammengefasst werden sollen
	 * @return	Daten über alle Ressourcen hinweg
	 * @see #hasAllResourceData()
	 */
	protected double calcAllResources(final StatisticsTimePerformanceIndicator[] statistics) {
		return 0.0;
	}

	/**
	 * Liefert Daten für einen Ressourcentyp.
	 * @param statistics	Statistik-Objekt für den Ressourcentyp
	 * @return	Daten für einen Ressourcentyp
	 */
	protected double calcSingleResource(final StatisticsTimePerformanceIndicator statistics) {
		return 0.0;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		final StatisticsTimePerformanceIndicator[] statistics=getUsageStatistics();
		if (statistics==null) throw error();

		if (parameters.length==0 && hasAllResourceData()) return calcAllResources(statistics);

		if (parameters.length==1) {
			final int id=(int)FastMath.round(parameters[0])-1;
			if (id<0 || id>=statistics.length) throw error();
			return calcSingleResource(statistics[id]);
		}

		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		final StatisticsTimePerformanceIndicator[] statistics=getUsageStatistics();
		if (statistics==null) return fallbackValue;

		if (parameters.length==0 && hasAllResourceData()) return calcAllResources(statistics);

		if (parameters.length==1) {
			final int id=(int)FastMath.round(parameters[0])-1;
			if (id<0 || id>=statistics.length) return fallbackValue;
			return calcSingleResource(statistics[id]);
		}

		return fallbackValue;
	}
}
