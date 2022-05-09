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
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Im Falle von einem Parameter:<br>
 * (a) Liefert den quadrierten Variationskoeffizienten der Anzahl an wartenden Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt.<br>
 * (b) Liefert den quadrierten Variationskoeffizienten der Anzahl an Kunden in der Warteschlange an Station id (1. Parameter).<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert den quadrierten Variationskoeffizienten der Anzahl an Kunden in allen Warteschlange zusammen.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataQueue_scv extends CalcSymbolStationData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"NQ_scv","Queue_scv","Schlange_scv","Warteschlange_scv"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataQueue_scv() {
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
		final double cv=getSimData().statistics.clientsInSystemQueues.getTimeCV();
		return cv*cv;
	}

	@Override
	protected double calcSingleClient(final String name) {
		StatisticsPerformanceIndicator indicator=getSimData().statistics.clientsAtStationQueueByClient.get(name);
		if (indicator==null) return 0.0;
		final double cv=((StatisticsTimePerformanceIndicator)indicator).getTimeCV();
		return cv*cv;
	}

	@Override
	protected double calc(final RunElementData data) {
		if (data.statisticClientsAtStationQueue==null) return 0;
		final double cv=data.statisticClientsAtStationQueue.getTimeCV();
		return cv*cv;
	}
}