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
package simulator.simparser.symbols;

import simulator.simparser.coresymbols.CalcSymbolTransporterData;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Liefert den quadrierten Variationskoeffizienten der Anzahl an belegten Transportern in der angegebenen Transportergruppe.
 * @author Alexander Herzog
 */
public class CalcSymbolTransporterData_scv extends CalcSymbolTransporterData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"transporter_scv","transporter_utilization_scv"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolTransporterData_scv() {
		/*
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calcSingleTransporterGroup(final StatisticsTimePerformanceIndicator statistics) {
		final double cv=statistics.getTimeCV();
		return cv*cv;
	}
}
