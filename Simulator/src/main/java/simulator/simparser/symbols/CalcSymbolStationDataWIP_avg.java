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
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Im Falle von einem Parameter:<br>
 * (a) Liefert die mittlere Anzahl an Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt.<br>
 * (b) Liefert die mittlere Anzahl an Kunden an Station id (1. Parameter).<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die mittlere Anzahl an Kunden im System.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataWIP_avg extends CalcSymbolStationData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"WIP_avg","Station_avg","N_avg","WIP_average","Station_average","N_average","WIP_Mittelwert","Station_Mittelwert","N_Mittelwert"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataWIP_avg() {
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
	protected boolean hasAllData() {
		return true;
	}

	@Override
	protected boolean hasSingleClientData() {
		return true;
	}

	@Override
	protected double calcAll() {
		return getSimData().statistics.clientsInSystem.getTimeMean();
	}

	@Override
	protected double calcSingleClient(final String name) {
		final StatisticsTimePerformanceIndicator indicator=getClientTimeIndicator(name,getSimData().statistics.clientsInSystemByClient);
		return (indicator==null)?0.0:indicator.getTimeMean();
	}

	@Override
	protected double calc(final RunElementData data) {
		if (data.statisticClientsAtStation==null) return 0.0;
		return data.statisticClientsAtStation.getTimeMean();
	}

	@Override
	protected boolean hasStationAndClientData() {
		return true;
	}

	@Override
	protected double calcStationClient(final RunElementData data, final int clientTypeIndex) {
		if (data.statisticClientsAtStationByClientType==null || data.statisticClientsAtStationByClientType[clientTypeIndex]==null) return 0.0;
		return data.statisticClientsAtStationByClientType[clientTypeIndex].getTimeMean();
	}
}