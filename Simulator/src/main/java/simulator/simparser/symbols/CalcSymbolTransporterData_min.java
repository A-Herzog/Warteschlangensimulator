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
 * Im Falle von einem Parameter:<br>
 * Liefert die minimale Anzahl an belegten Transportern in der angegebenen Transportergruppe.<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die minimale Anzahl an belegten Transportern in allen Transportergruppen zusammen.
 * @author Alexander Herzog
 */
public class CalcSymbolTransporterData_min extends CalcSymbolTransporterData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{
			"transporter_min","transporter_utilization_min",
			"transporter_Minimum","transporter_utilization_Minimum"
	};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolTransporterData_min() {
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
	protected boolean hasAllTransporterData() {return true;}

	@Override
	protected double calcAllTransporters(StatisticsTimePerformanceIndicator[] statistics) {
		double sum=0;
		for (StatisticsTimePerformanceIndicator indicator: statistics) sum+=indicator.getTimeMin();
		return sum;
	}

	@Override
	protected double calcSingleTransporterGroup(final StatisticsTimePerformanceIndicator statistics) {
		return statistics.getTimeMin();
	}
}
