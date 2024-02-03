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
 * (a) Liefert den Median der Anzahl an wartenden Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt.<br>
 * (b) Liefert den Median der Anzahl an Kunden in der Warteschlange an Station id (1. Parameter).<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert den Median der Anzahl an Kunden in allen Warteschlange zusammen.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataQueue_median extends CalcSymbolStationData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"NQ_median","Queue_median","Schlange_median","Warteschlange_median"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataQueue_median() {
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
		return getSimData().statistics.clientsInSystemQueues.getTimeMedian();
	}

	@Override
	protected double calcSingleClient(final String name) {
		final StatisticsTimePerformanceIndicator indicator=getClientTimeIndicator(name,getSimData().statistics.clientsAtStationQueueByClient);
		return (indicator==null)?0.0:indicator.getTimeMedian();
	}

	@Override
	protected double calc(final RunElementData data) {
		if (data.statisticClientsAtStationQueue==null) return 0.0;
		return data.statisticClientsAtStationQueue.getTimeMedian();
	}

	@Override
	protected boolean hasStationAndClientData() {
		return true;
	}

	@Override
	protected double calcStationClient(final RunElementData data, final int clientTypeIndex) {
		if (data.statisticClientsAtStationQueueByClientType==null || data.statisticClientsAtStationQueueByClientType[clientTypeIndex]==null) return 0.0;
		return data.statisticClientsAtStationQueueByClientType[clientTypeIndex].getTimeMedian();
	}
}