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

import mathtools.distribution.DataDistributionImpl;
import simulator.coreelements.RunElementData;
import simulator.simparser.coresymbols.CalcSymbolStationDataHistogram;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Im Falle von zwei Parameter:<br>
 * (a) Liefert den Anteil der Zeit, in der sich state (2. Parameter) Kunden in Bedienung, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt, im System befanden.<br>
 * (b) Liefert den Anteil der Zeit, in der sich state (2. Parameter) Kunden in Bedienung an Station id (1. Parameter) befunden haben.<br>
 * Im Falle von drei Parameter:<br>
 * (a) Liefert den Anteil der Zeit, in der sich mehr als stateA (2. Parameter) und höchsten stateB (3. Parameter) Kunden in Bedienung, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt, im System befanden.<br>
 * (b) Liefert den Anteil der Zeit, in der sich mehr als stateA (2. Parameter) und höchsten stateB (3. Parameter) Kunden in Bedienung an Station id (1. Parameter) befunden haben.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataProcessCount_hist extends CalcSymbolStationDataHistogram {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"NS_hist","Process_hist"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataProcessCount_hist() {
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
	protected boolean hasSingleClientData() {
		return true;
	}

	/**
	 * Kundentyp beim letzten Aufruf von {@link #getDistributionForClientType(String)}
	 * oder {@link #getDistributionSumForClientType(String)}
	 * @see #getDistributionForClientType(String)
	 * @see #getDistributionForClientType(String)
	 * @see #lastIndicator
	 */
	private String lastClientType;

	/**
	 * Zurückgeliefertes Statistikobjekt beim letzten Aufruf von {@link #getDistributionForClientType(String)}
	 * oder {@link #getDistributionSumForClientType(String)}
	 * @see #getDistributionForClientType(String)
	 * @see #getDistributionForClientType(String)
	 * @see #lastClientType
	 */
	private StatisticsTimePerformanceIndicator lastIndicator;

	@Override
	protected DataDistributionImpl getDistributionForClientType(final String name) {
		if (lastClientType==null || !name.equals(lastClientType)) {
			lastIndicator=((StatisticsTimePerformanceIndicator)getSimData().statistics.clientsAtStationProcessByClient.get(name));
			if (lastIndicator==null) return null;
			lastClientType=name;
		}
		return lastIndicator.getReadOnlyDistribution();
	}

	@Override
	protected double getDistributionSumForClientType(final String name) {
		if (lastClientType==null || !name.equals(lastClientType)) {
			lastIndicator=((StatisticsTimePerformanceIndicator)getSimData().statistics.clientsAtStationProcessByClient.get(name));
			if (lastIndicator==null) return 0.0;
			lastClientType=name;
		}
		return lastIndicator.getSum();
	}

	@Override
	protected DataDistributionImpl getDistribution(RunElementData data) {
		if (data.statisticClientsAtStationProcess==null) return null;
		return data.statisticClientsAtStationProcess.getReadOnlyDistribution();
	}

	@Override
	protected double getDistributionSum(RunElementData data) {
		if (data.statisticClientsAtStationProcess==null) return 0;
		return data.statisticClientsAtStationProcess.getSum();
	}
}
