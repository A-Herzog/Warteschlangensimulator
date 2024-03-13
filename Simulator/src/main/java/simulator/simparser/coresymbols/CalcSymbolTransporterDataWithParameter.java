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
 * Basisklasse f�r Funktionen, die Transporterdaten mit einem zus�tzlichen
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
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Gibt an, ob der Rechenbefehl Daten �ber alle Transportergruppen hinweg enth�lt
	 * @return	Wird hier <code>true</code> geliefert, so muss {@link #calcAllTransporters(StatisticsTimePerformanceIndicator[], double)} definiert sein
	 */
	protected boolean hasAllTransporterData() {
		return false;
	}

	/**
	 * Liefert Daten �ber alle Transportergruppen hinweg.
	 * @param statistics	Einzel-Statistik-Objekte die zusammengefasst werden sollen
	 * @param p	Zus�tzlicher als Parameter angegebener Wert
	 * @return	Daten �ber alle Transportergruppen hinweg
	 * @see #hasAllTransporterData()
	 */
	protected double calcAllTransporters(final StatisticsTimePerformanceIndicator[] statistics, final double p) {
		return 0.0;
	}


	/**
	 * Liefert Daten f�r eine Transportergruppe.
	 * @param statistics	Statistik-Objekt f�r die Transportergruppe
	 * @param p	Zus�tzlicher als Parameter angegebener Wert
	 * @return	Daten f�r eine Transportergruppe
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
