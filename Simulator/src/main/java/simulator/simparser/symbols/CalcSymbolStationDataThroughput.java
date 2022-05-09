/**
 * Copyright 2021 Alexander Herzog
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
import simulator.elements.RunElementThroughputData;
import simulator.runmodel.SimulationData;
import simulator.simparser.coresymbols.CalcSymbolStationData;
import statistics.StatisticsDataPerformanceIndicator;

/**
 * Liefert den Durchsatz an Station id (1. Parameter).
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataThroughput extends CalcSymbolStationData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Durchsatz","Throughput"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataThroughput() {
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
	protected double calc(final RunElementData data) {
		if (data instanceof RunElementThroughputData) return ((RunElementThroughputData)data).getValue(true);

		final SimulationData simData=getSimData();
		if (simData==null) return 0.0;
		if (simData.runData.isWarmUp) return 0.0;
		final double time=simData.statistics.clientsInSystem.getSum();
		if (time<=0.0) return 0.0;

		return data.clientsNonWarmUp/time; /* Zeit in clientsInSystem zählt erst ab Ende WarmUp, daher müssen wir die Kundenzählung auch ab diesem Zeitpunkt starten */
	}

	@Override
	protected double calcAll() {
		final SimulationData simData=getSimData();
		if (simData==null) return 0.0;
		if (simData.runData.isWarmUp) return 0.0;
		final double time=simData.statistics.clientsInSystem.getSum();
		if (time<=0.0) return 0.0;

		long sum=0;
		for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])simData.statistics.clientsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class)) sum+=indicator.getCount();

		return sum/time;
	}
}