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

import mathtools.distribution.DataDistributionImpl;
import simulator.coreelements.RunElementData;
import simulator.simparser.coresymbols.CalcSymbolStationDataHistogram;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Im Falle von drei Parametern:<br>
 * (a) Liefert den Anteil der Zeit, in der sich das System in Bezug auf die Anzahl an Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt, in einem Zustand gr��er als stateA (2. Parameter) und kleiner oder gleich stateB (3. Parameter) befunden hat.<br>
 * (b) Liefert den Anteil der Zeit, in der sich das System in Bezug auf die Anzahl an Kunden an Station id (1. Parameter) in einem Zustand gr��er als stateA (2. Parameter) und kleiner oder gleich stateB (3. Parameter) befunden hat.<br>
 * Im Falle von zwei Parametern:<br>
 * (a) Liefert den Anteil der Zeit, in der sich das System in Bezug auf die Anzahl an Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt, im Zustand state (2. Parameter) befunden hat.<br>
 * (b) Liefert den Anteil der Zeit, in der sich das System in Bezug auf die Anzahl an Kunden an Station id (1. Parameter) im Zustand state (2. Parameter) befunden hat.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataWIP_hist extends CalcSymbolStationDataHistogram {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"WIP_hist","Station_hist","N_hist"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataWIP_hist() {
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
	 * Zur�ckgeliefertes Statistikobjekt beim letzten Aufruf von {@link #getDistributionForClientType(String)}
	 * oder {@link #getDistributionSumForClientType(String)}
	 * @see #getDistributionForClientType(String)
	 * @see #getDistributionForClientType(String)
	 * @see #lastClientType
	 */
	private StatisticsTimePerformanceIndicator lastIndicator;

	@Override
	protected DataDistributionImpl getDistributionForClientType(final String name) {
		if (lastClientType==null || !name.equals(lastClientType)) {
			lastIndicator=((StatisticsTimePerformanceIndicator)getSimData().statistics.clientsInSystemByClient.get(name));
			if (lastIndicator==null) return null;
			lastClientType=name;
		}
		return lastIndicator.getReadOnlyDistribution();
	}

	@Override
	protected double getDistributionSumForClientType(final String name) {
		if (lastClientType==null || !name.equals(lastClientType)) {
			lastIndicator=((StatisticsTimePerformanceIndicator)getSimData().statistics.clientsInSystemByClient.get(name));
			if (lastIndicator==null) return 0.0;
			lastClientType=name;
		}
		return lastIndicator.getSum();
	}

	@Override
	protected DataDistributionImpl getDistribution(RunElementData data) {
		if (data.statisticClientsAtStation==null) return null;
		return data.statisticClientsAtStation.getReadOnlyDistribution();
	}

	@Override
	protected double getDistributionSum(RunElementData data) {
		if (data.statisticClientsAtStation==null) return 0;
		return data.statisticClientsAtStation.getSum();
	}
}
