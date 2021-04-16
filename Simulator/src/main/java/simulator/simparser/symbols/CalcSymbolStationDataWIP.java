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
import simulator.runmodel.SimulationData;
import simulator.simparser.coresymbols.CalcSymbolStationData;

/**
 * Im Falle von einem Parameter:<br>
 * (a) Liefert die aktuelle Gesamtanzahl an Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt.<br>
 * (b) Liefert die aktuelle Gesamtanzahl an Kunden an Station id (1. Parameter).<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die aktuelle Gesamtanzahl an Kunden im System.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataWIP extends CalcSymbolStationData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"WIP","Station","N"};

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected boolean hasAllData() {
		return true;
	}

	@Override
	protected double calcAll() {
		final SimulationData simData=getSimData();

		final int[] count=simData.runData.clientsInSystemByType;
		if (count==null) return 0.0;
		double sum=0.0;
		for (int c: count) sum+=c;
		return sum;

		/*
		Funktioniert nicht während Warmup:
		return simData.statistics.clientsInSystem.getCurrentState();
		 */
	}

	@Override
	protected boolean hasSingleClientData() {
		return true;
	}

	@Override
	protected double calcSingleClient(final String name) {
		final SimulationData simData=getSimData();

		final Integer I=simData.runModel.clientTypesMap.get(name);
		if (I==null) return 0.0;

		final int[] count=simData.runData.clientsInSystemByType;
		if (count==null) return 0.0;

		return count[I];

		/*
		Funktioniert nicht während Warmup:
		StatisticsPerformanceIndicator indicator=getSimData().statistics.clientsInSystemByClient.get(name);
		if (indicator==null) return 0.0;
		return ((StatisticsTimePerformanceIndicator)indicator).getCurrentState();
		 */
	}

	@Override
	protected boolean hasStationAndClientData() {
		return true;
	}

	@Override
	protected double calcStationClient(final RunElementData data, final int clientTypeIndex) {
		if (data==null) return 0.0;

		return data.reportedClientsAtStation(getSimData(),clientTypeIndex);
	}

	@Override
	protected double calc(final RunElementData data) {
		if (data==null) return 0.0;
		return data.reportedClientsAtStation(getSimData());
	}
}
