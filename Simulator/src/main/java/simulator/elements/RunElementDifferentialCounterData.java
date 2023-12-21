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
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Laufzeitdaten eines {@link RunElementDifferentialCounter}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementDifferentialCounter
 * @see RunElementData
 */
public class RunElementDifferentialCounterData extends RunElementData implements RunElementDataWithValue {
	/** Wert der Änderung des Zählers, wenn ein Kunde die Station passiert */
	private final int change;
	/** Zusätzliche Bedingung, die für die Zählung eines Kunden erfüllt sein muss */
	public final RunCounterConditionData condition;
	/** Index des aktuellen Zählers in {@link RunData#differentialCounterValue} */
	private int valueIndex;
	/** Zugehöriges Statistikobjekt */
	private final StatisticsTimePerformanceIndicator statistic;

	/**
	 * Konstruktor der Klasse {@link RunElementCounterData}
	 * @param station	Station für den hier die Daten erfasst werden sollen
	 * @param counterName	Name des Differenzzählers (um das zugehörige globale Zählerobjekt zu adressieren)
	 * @param change	Wert der Änderung des Zählers, wenn ein Kunde die Station passiert
	 * @param condition	Zusätzliche Bedingung, die für die Zählung eines Kunden erfüllt sein muss
	 * @param differentialCounterStatistic	Zugehöriger Zähler in der Statistik
	 * @param data	Laufzeitdatenobjekt (welches das globale Zählerobjekt enthält)
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementDifferentialCounterData(final RunElement station, final String counterName, final int change, final RunCounterConditionData condition, final StatisticsMultiPerformanceIndicator differentialCounterStatistic, final RunData data, final SimulationData simData) {
		super(station,simData);
		statistic=(StatisticsTimePerformanceIndicator)differentialCounterStatistic.get(counterName);
		this.change=change;
		this.condition=condition;
		int nr=data.differentialCounterName.indexOf(counterName);
		if (nr<0) {
			data.differentialCounterName.add(counterName);
			nr=data.differentialCounterName.size()-1;
		}
		valueIndex=nr;
	}

	/**
	 * Ändert den Zählerwert und erfasst dies in der Statistik
	 * @param time	Zeitpunkt, zu dem die Änderung erfolgt ist	 *
	 * @param runModel	Laufzeitmodell
	 * @param data	Laufzeitdatenobjekt (welches das globale Zählerobjekt enthält)
	 * @return	Neuer Wert des Zählers
	 */
	public int count(final long time, final RunModel runModel, final RunData data) {
		if (data.differentialCounterValue==null) data.differentialCounterValue=new int[data.differentialCounterName.size()];
		if (data.differentialCounterValue.length<=valueIndex) data.differentialCounterValue=Arrays.copyOf(data.differentialCounterValue,data.differentialCounterName.size());
		int value=data.differentialCounterValue[valueIndex];

		value=FastMath.max(0,value+change);
		statistic.set(time*runModel.scaleToSeconds,value);

		data.differentialCounterValue[valueIndex]=value;

		return value;
	}

	@Override
	public double getValue(final boolean fullValue) {
		return statistic.getCurrentState();
	}

	/**
	 * Liefert den Mittelwert des Differenzzählerwertes.
	 * @return	Mittelwert des Differenzzählerwertes
	 */
	public double getMean() {
		return statistic.getTimeMean();
	}

	/**
	 * Liefert den Minimalwert des Differenzzählerwertes.
	 * @return	Minimalwert des Differenzzählerwertes
	 */
	public double getMin() {
		return statistic.getTimeMin();
	}

	/**
	 * Liefert den Maximalwert des Differenzzählerwertes.
	 * @return	Maximalwert des Differenzzählerwertes
	 */
	public double getMax() {
		return statistic.getTimeMax();
	}

	/**
	 * Liefert die Standardabweichung des Differenzzählerwertes.
	 * @return	Standardabweichung des Differenzzählerwertes
	 */
	public double getSD() {
		return statistic.getTimeSD();
	}
}