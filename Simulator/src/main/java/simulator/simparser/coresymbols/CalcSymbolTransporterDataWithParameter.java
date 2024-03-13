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
package simulator.simparser.coresymbols;

import org.apache.commons.math3.util.FastMath;

import parser.MathCalcError;
import simulator.runmodel.SimulationData;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Basisklasse für Funktionen, die Transporterdaten mit einem zusätzlichen
 * Parameter aus den Simulationsdaten auslesen.
 * @author Alexander Herzog
 * @see SimulationData
 * @see CalcSymbolSimData
 */
public abstract class CalcSymbolTransporterDataWithParameter extends CalcSymbolSimData {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolTransporterDataWithParameter() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Gibt an, ob der Rechenbefehl Daten über alle Transportergruppen hinweg enthält
	 * @return	Wird hier <code>true</code> geliefert, so muss {@link #calcAllTransporters(StatisticsTimePerformanceIndicator[], double)} definiert sein
	 */
	protected boolean hasAllTransporterData() {
		return false;
	}

	/**
	 * Liefert Daten über alle Transportergruppen hinweg.
	 * @param statistics	Einzel-Statistik-Objekte die zusammengefasst werden sollen
	 * @param p	Zusätzlicher als Parameter angegebener Wert
	 * @return	Daten über alle Transportergruppen hinweg
	 * @see #hasAllTransporterData()
	 */
	protected double calcAllTransporters(final StatisticsTimePerformanceIndicator[] statistics, final double p) {
		return 0.0;
	}


	/**
	 * Liefert Daten für eine Transportergruppe.
	 * @param statistics	Statistik-Objekt für die Transportergruppe
	 * @param p	Zusätzlicher als Parameter angegebener Wert
	 * @return	Daten für eine Transportergruppe
	 */
	protected double calcSingleTransporterGroup(final StatisticsTimePerformanceIndicator statistics, final double p) {
		return 0.0;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		final StatisticsTimePerformanceIndicator[] statistics=getTransporterUsageStatistics();
		if (statistics==null) throw error();

		if (parameters.length==1 && hasAllTransporterData()) return calcAllTransporters(statistics,parameters[0]);

		if (parameters.length==2) {
			final int id=(int)FastMath.round(parameters[0])-1;
			if (id<0 || id>=statistics.length) throw error();
			return calcSingleTransporterGroup(statistics[id],parameters[1]);
		}

		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		final StatisticsTimePerformanceIndicator[] statistics=getTransporterUsageStatistics();
		if (statistics==null) return fallbackValue;

		if (parameters.length==1 && hasAllTransporterData()) return calcAllTransporters(statistics,parameters[0]);

		if (parameters.length==2) {
			final int id=(int)FastMath.round(parameters[0])-1;
			if (id<0 || id>=statistics.length) return fallbackValue;
			return calcSingleTransporterGroup(statistics[id],parameters[1]);
		}

		return fallbackValue;
	}
}
