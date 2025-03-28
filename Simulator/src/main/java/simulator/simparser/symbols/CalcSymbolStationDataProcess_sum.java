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
 * (a) Liefert die Summe der Bedienzeiten der Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt (in Sekunden).
 * (b) Liefert die Summe der an Station id (1. Parameter) bisher angefallenen Bedienzeiten (in Sekunden).
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataProcess_sum extends CalcSymbolStationData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{
			"Bedienzeit_sum","Bedienzeit_gesamt","Bedienzeit_summe",
			"ProcessTime_sum","ProcessingTime_sum","ProcessTime_gesamt","ProcessingTime_gesamt","ProcessingTime_summe","ProcessTime_summe",
			"ServiceTime_sum","ServiceTime_gesamt","ServiceTime_summe"
	};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataProcess_sum() {
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
		return data.statisticProcess.getSum();
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
		return getSimData().statistics.clientsAllProcessingTimes.getSum();
	}

	@Override
	protected double calcSingleClient(final String name) {
		final StatisticsDataPerformanceIndicator indicator=getClientDataIndicator(name,getSimData().statistics.clientsProcessingTimes);
		return (indicator==null)?0.0:indicator.getSum();
	}

	@Override
	protected boolean hasStationAndClientData() {
		return true;
	}

	@Override
	protected double calcStationClient(final RunElementData data, final int clientTypeIndex) {
		if (data.statisticProcessByClientType==null || data.statisticProcessByClientType[clientTypeIndex]==null) return 0.0;
		return data.statisticProcessByClientType[clientTypeIndex].getSum();
	}
}
