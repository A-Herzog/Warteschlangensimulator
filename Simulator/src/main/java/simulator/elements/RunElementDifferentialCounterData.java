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

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.coreelements.RunElementDataWithValue;
import simulator.runmodel.RunData;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Laufzeitdaten eines <code>RunElementDifferentialCounter</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementDifferentialCounter
 * @see RunElementData
 */
public class RunElementDifferentialCounterData extends RunElementData implements RunElementDataWithValue {
	/** Wert der Änderung des Zählers, wenn ein Kunde die Station passiert */
	private final int change;
	private int valueIndex;
	private final StatisticsTimePerformanceIndicator statistic;

	/**
	 * Konstruktor der Klasse <code>RunElementCounterData</code>
	 * @param station	Station für den hier die Daten erfasst werden sollen
	 * @param counterName	Name des Differenzzählers (um das zugehörige globale Zählerobjekt zu adressieren)
	 * @param change	Wert der Änderung des Zählers, wenn ein Kunde die Station passiert
	 * @param differentialCounterStatistic	Zugehöriger Zähler in der Statistik
	 * @param data	Laufzeitdatenobjekt (welches das globale Zählerobjekt enthält)
	 */
	public RunElementDifferentialCounterData(final RunElement station, final String counterName, final int change, final StatisticsMultiPerformanceIndicator differentialCounterStatistic, final RunData data) {
		super(station);
		statistic=(StatisticsTimePerformanceIndicator)differentialCounterStatistic.get(counterName);
		this.change=change;
		int nr=data.differentialCounterName.indexOf(counterName);
		if (nr<0) {
			data.differentialCounterName.add(counterName);
			nr=data.differentialCounterName.size()-1;
		}
		valueIndex=nr;
	}

	/** Umrechnungsfaktor von Millisekunden auf Sekunden, um die Division während der Simulation zu vermeiden */
	private static final double toSec=1.0/1000.0;

	/**
	 * Ändert den Zählerwert und erfasst dies in der Statistik
	 * @param time	Zeitpunkt, zu dem die Änderung erfolgt ist
	 * @return	Neuer Wert des Zählers
	 * @param data	Laufzeitdatenobjekt (welches das globale Zählerobjekt enthält)
	 */
	public int count(final long time, final RunData data) {
		if (data.differentialCounterValue==null) data.differentialCounterValue=new int[data.differentialCounterName.size()];
		if (data.differentialCounterValue.length<=valueIndex) data.differentialCounterValue=Arrays.copyOf(data.differentialCounterValue,data.differentialCounterName.size());
		int value=data.differentialCounterValue[valueIndex];

		value=FastMath.max(0,value+change);
		statistic.set(time*toSec,value);

		data.differentialCounterValue[valueIndex]=value;

		return value;
	}

	@Override
	public double getValue(final boolean fullValue) {
		return statistic.getCurrentState();
	}
}