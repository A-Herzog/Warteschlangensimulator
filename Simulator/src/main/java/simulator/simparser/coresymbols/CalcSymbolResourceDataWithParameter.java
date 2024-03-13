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
 * Basisklasse für Funktionen, die Ressourcendaten mit einem zusätzlichen
 * Parameter aus den Simulationsdaten auslesen.
 * @author Alexander Herzog
 * @see SimulationData
 * @see CalcSymbolSimData
 */
public abstract class CalcSymbolResourceDataWithParameter extends CalcSymbolSimData {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolResourceDataWithParameter() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Gibt an, ob der Rechenbefehl Daten über alle Ressourcengruppen hinweg enthält	 *
	 * @return	Wird hier <code>true</code> geliefert, so muss {@link #calcAllResources(StatisticsTimePerformanceIndicator[], double)} definiert sein
	 */
	protected boolean hasAllResourceData() {
		return false;
	}

	/**
	 * Liefert Daten über alle Ressourcen hinweg.
	 * @param statistics	Einzel-Statistik-Objekte die zusammengefasst werden sollen
	 * @param p	Zusätzlicher als Parameter angegebener Wert
	 * @return	Daten über alle Ressourcen hinweg
	 * @see #hasAllResourceData()
	 */
	protected double calcAllResources(final StatisticsTimePerformanceIndicator[] statistics, final double p) {
		return 0.0;
	}

	/**
	 * Liefert Daten für einen Ressourcentyp.
	 * @param statistics	Statistik-Objekt für den Ressourcentyp
	 * @param p	Zusätzlicher als Parameter angegebener Wert
	 * @return	Daten für einen Ressourcentyp
	 */
	protected double calcSingleResource(final StatisticsTimePerformanceIndicator statistics, final double p) {
		return 0.0;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		final StatisticsTimePerformanceIndicator[] statistics=getUsageStatistics();
		if (statistics==null) throw error();

		final SimulationData simData=getSimData();
		simData.runData.resources.updateStatistics(simData);

		if (parameters.length==1 && hasAllResourceData()) return calcAllResources(statistics,parameters[0]);

		if (parameters.length==2) {
			final int id=(int)FastMath.round(parameters[0])-1;
			if (id<0 || id>=statistics.length) throw error();
			return calcSingleResource(statistics[id],parameters[1]);
		}

		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		final StatisticsTimePerformanceIndicator[] statistics=getUsageStatistics();
		if (statistics==null) return fallbackValue;

		final SimulationData simData=getSimData();
		simData.runData.resources.updateStatistics(simData);

		if (parameters.length==1 && hasAllResourceData()) return calcAllResources(statistics,parameters[0]);

		if (parameters.length==2) {
			final int id=(int)FastMath.round(parameters[0])-1;
			if (id<0 || id>=statistics.length) return fallbackValue;
			return calcSingleResource(statistics[id],parameters[1]);
		}

		return fallbackValue;
	}
}
