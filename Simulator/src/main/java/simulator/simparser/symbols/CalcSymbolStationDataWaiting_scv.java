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
 * (a) Liefert den quadrierten Variationskoeffizienten der Wartezeiten der Kunden, deren Name an Quelle bzw. Namenszuweisung id auftritt.<br>
 * (b) Liefert den quadrierten Variationskoeffizienten der Wartezeiten der Kunden an Station id.<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert den quadrierten Variationskoeffizienten der Wartezeiten über alle Kunden.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataWaiting_scv extends CalcSymbolStationData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Wartezeit_scv","WaitingTime_scv"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataWaiting_scv() {
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
	protected double calc(final RunElementData data) {
		if (data.statisticWaiting==null) return 0;
		final double cv=data.statisticWaiting.getCV();
		return cv*cv;
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
		final double cv=getSimData().statistics.clientsAllWaitingTimes.getCV();
		return cv*cv;
	}

	@Override
	protected double calcSingleClient(final String name) {
		final StatisticsDataPerformanceIndicator indicator=getClientDataIndicator(name,getSimData().statistics.clientsWaitingTimes);
		final double d=(indicator==null)?0.0:indicator.getCV();
		return d*d;
	}

	@Override
	protected boolean hasStationAndClientData() {
		return true;
	}

	@Override
	protected double calcStationClient(final RunElementData data, final int clientTypeIndex) {
		if (data.statisticWaitingByClientType==null || data.statisticWaitingByClientType[clientTypeIndex]==null) return 0.0;
		final double cv=data.statisticWaitingByClientType[clientTypeIndex].getMean();
		return cv*cv;
	}
}
