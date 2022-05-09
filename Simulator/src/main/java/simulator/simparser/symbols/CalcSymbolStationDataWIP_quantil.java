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
import simulator.simparser.coresymbols.CalcSymbolStationDataQuantil;
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Im Falle von zwei Parametern:<br>
 * (a) Liefert das Quantil zur Wahrscheinlichkeit p (1. Parameter) der Anzahl an Kunden, deren Name an Quelle bzw. Namenszuweisung id (2. Parameter) auftritt.<br>
 * (b) Liefert das Quantil zur Wahrscheinlichkeit p (1. Parameter) der Anzahl an Kunden an Station id (2. Parameter).<br>
 * Im Falle von einem Parameter:<br>
 * Liefert das Quantil zur Wahrscheinlichkeit p (1. Parameter) der Anzahl an Kunden im System.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataWIP_quantil extends CalcSymbolStationDataQuantil {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"WIP_quantil","Station_quantil","N_quantil"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataWIP_quantil() {
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
	protected double calcAll(final double p) {
		return getSimData().statistics.clientsInSystem.getQuantil(p);
	}

	@Override
	protected double calcSingleClient(final double p, final String name) {
		StatisticsPerformanceIndicator indicator=getSimData().statistics.clientsInSystemByClient.get(name);
		if (indicator==null) return 0.0;
		return ((StatisticsTimePerformanceIndicator)indicator).getQuantil(p);
	}

	@Override
	protected double calc(final double p, final RunElementData data) {
		if (data.statisticClientsAtStation==null) return 0;
		return data.statisticClientsAtStation.getQuantil(p);
	}
}
