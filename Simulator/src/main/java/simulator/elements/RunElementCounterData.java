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
package simulator.elements;

import java.util.ArrayList;
import java.util.List;

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.coreelements.RunElementDataWithValue;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;

/**
 * Laufzeitdaten eines <code>RunElementCounter</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementCounter
 * @see RunElementData
 */
public class RunElementCounterData extends RunElementData implements RunElementDataWithValue {
	/**
	 * Statistikobjekt welches den Wert des Zählers speichert
	 */
	public final StatisticsSimpleCountPerformanceIndicator statistic;

	private final StatisticsMultiPerformanceIndicator counterStatistic;
	private final String groupName;
	private final String groupNameExt;

	private StatisticsSimpleCountPerformanceIndicator[] indicators;
	private int indicatorsSize;

	/**
	 * Konstruktor der Klasse <code>RunElementCounterData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param counterName	Name des Zählers
	 * @param groupName	Name der Zählergruppe
	 * @param counterStatistic	Zugehöriges Statistikobjekt
	 */
	public RunElementCounterData(final RunElement station, final String counterName, final String groupName, final StatisticsMultiPerformanceIndicator counterStatistic) {
		super(station);
		this.counterStatistic=counterStatistic;
		this.groupName=groupName.replace('-','_');
		groupNameExt=this.groupName+"-";
		statistic=(StatisticsSimpleCountPerformanceIndicator)counterStatistic.get(this.groupNameExt+counterName);
	}

	@Override
	public double getValue(final boolean fullValue) {
		/* Aktueller Wert */
		if (fullValue) return statistic.get();

		/* Anteil */
		if (groupName.isEmpty()) return 1.0; /* Kein Gruppenname, keine Summe */

		/* Liste der Statistikdaten für diese Gruppe */
		if (indicators==null || indicatorsSize!=counterStatistic.size()) {
			final List<StatisticsSimpleCountPerformanceIndicator> list=new ArrayList<>();
			for (String name: counterStatistic.getNames()) {
				if (name.startsWith(groupNameExt)) list.add((StatisticsSimpleCountPerformanceIndicator)counterStatistic.get(name));
			}
			indicators=list.toArray(new StatisticsSimpleCountPerformanceIndicator[0]);
			indicatorsSize=counterStatistic.size();
		}

		/* Summe berechnen */
		long sum=0;
		for (StatisticsSimpleCountPerformanceIndicator indicator: indicators) sum+=indicator.get();

		if (sum>0) {
			return ((double)statistic.get())/sum;
		} else {
			return 0.0;
		}
	}
}