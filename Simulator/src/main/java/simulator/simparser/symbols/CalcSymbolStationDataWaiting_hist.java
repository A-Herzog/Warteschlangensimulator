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
import statistics.StatisticsDataPerformanceIndicator;

/**
 * Im Falle von drei Parametern:<br>
 * Liefert den Anteil der Kunden, für den die Wartezeit an Station id (1. Parameter) mehr als timeA (2. Parameter) und höchstens timeB (3. Parameter) Sekunden gedauert hat.<br>
 * Im Falle von zwei Parametern:<br>
 * Liefert den Anteil der Kunden, für den die Wartezeit an Station id (1. Parameter) time (2. Parameter) Sekunden gedauert hat.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataWaiting_hist extends CalcSymbolStationDataHistogram {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Wartezeit_hist","WaitingTime_hist"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataWaiting_hist() {
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
	private StatisticsDataPerformanceIndicator lastIndicator;

	@Override
	protected DataDistributionImpl getDistributionForClientType(final String name) {
		if (lastClientType==null || !name.equals(lastClientType)) {
			lastIndicator=((StatisticsDataPerformanceIndicator)getSimData().statistics.clientsWaitingTimes.get(name));
			if (lastIndicator==null) return null;
			lastClientType=name;
		}
		return lastIndicator.getDistribution();
	}

	@Override
	protected double getDistributionSumForClientType(final String name) {
		if (lastClientType==null || !name.equals(lastClientType)) {
			lastIndicator=((StatisticsDataPerformanceIndicator)getSimData().statistics.clientsWaitingTimes.get(name));
			if (lastIndicator==null) return 0.0;
			lastClientType=name;
		}
		return lastIndicator.getCount();
	}

	@Override
	protected DataDistributionImpl getDistribution(RunElementData data) {
		if (data.statisticWaiting==null) return null;
		return data.statisticWaiting.getDistribution();
	}

	@Override
	protected double getDistributionSum(RunElementData data) {
		if (data.statisticWaiting==null) return 0;
		return data.statisticWaiting.getCount();
	}
}
