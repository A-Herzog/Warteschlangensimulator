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

import simulator.coreelements.RunElementData;
import simulator.simparser.coresymbols.CalcSymbolStationData;
import statistics.StatisticsDataPerformanceIndicator;

/**
 * Im Falle von einem Parameter:<br>
 * (a) Liefert die mittlere Bedienzeit der Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt (in Sekunden).<br>
 * (b) Liefert die mittlere Bedienzeit der Kunden an Station id (1. Parameter) (in Sekunden).<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die mittlere Bedienzeit �ber alle Kunden (in Sekunden).
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataProcess_avg extends CalcSymbolStationData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{
			"Bedienzeit_avg","Bedienzeit_average","Bedienzeit_Mittelwert",
			"ProcessTime_avg","ProcessingTime_avg","ProcessingTime_average","ProcessingTime_Mittelwert",
			"ServiceTime_avg","ServiceTime_average","ServiceTime_Mittelwert"
	};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataProcess_avg() {
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
	protected double calc(final RunElementData data) {
		if (data.statisticProcess==null) return 0;
		return data.statisticProcess.getMean();
	}

	@Override
	protected boolean hasAllData() {
		return true;
	}

	@Override
	protected boolean hasSingleClientData() {
		return true;
	}

	@Override
	protected double calcAll() {
		return getSimData().statistics.clientsAllProcessingTimes.getMean();
	}

	@Override
	protected double calcSingleClient(final String name) {
		final StatisticsDataPerformanceIndicator indicator=getClientDataIndicator(name,getSimData().statistics.clientsProcessingTimes);
		return (indicator==null)?0.0:indicator.getMean();
	}

	@Override
	protected boolean hasStationAndClientData() {
		return true;
	}

	@Override
	protected double calcStationClient(final RunElementData data, final int clientTypeIndex) {
		if (data.statisticProcessByClientType==null) return 0;
		return data.statisticProcessByClientType[clientTypeIndex].getMean();
	}
}
