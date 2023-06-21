/**
 * Copyright 2022 Alexander Herzog
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
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Im Falle von einem Parameter:<br>
 * (a) Liefert den Variationskoeffizienten der Anzahl an Kunden in Bedienung, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt.<br>
 * (b) Liefert den Variationskoeffizienten der Anzahl an Kunden in Bedienung an Station id (1. Parameter).<br>
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataProcessCount_cv extends CalcSymbolStationData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"NS_cv","Process_cv"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataProcessCount_cv() {
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
	protected boolean hasAllData() {
		return true;
	}

	@Override
	protected boolean hasSingleClientData() {
		return true;
	}

	@Override
	protected double calcAll() {
		return getSimData().statistics.clientsInSystemProcess.getTimeCV();
	}

	@Override
	protected double calcSingleClient(final String name) {
		final StatisticsTimePerformanceIndicator indicator=getClientTimeIndicator(name,getSimData().statistics.clientsAtStationProcessByClient);
		return (indicator==null)?0.0:indicator.getTimeCV();
	}

	@Override
	protected double calc(final RunElementData data) {
		if (data.statisticClientsAtStationProcess==null) return 0;
		return data.statisticClientsAtStationProcess.getTimeCV();
	}

	@Override
	protected boolean hasStationAndClientData() {
		return true;
	}

	@Override
	protected double calcStationClient(final RunElementData data, final int clientTypeIndex) {
		if (data.statisticClientsAtStationProcessByClientType==null) return 0;
		return data.statisticClientsAtStationProcessByClientType[clientTypeIndex].getTimeCV();
	}
}