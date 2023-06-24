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

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.coreelements.RunElementDataWithValue;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsQuotientPerformanceIndicator;

/**
 * Laufzeitdaten eines <code>RunElementThroughput</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementThroughput
 * @see RunElementData
 */
public class RunElementThroughputData extends RunElementData implements RunElementDataWithValue {
	/** Zusätzliche Bedingung, die für die Zählung eines Kunden erfüllt sein muss */
	public final RunCounterConditionData condition;

	/**
	 * Statistikobjekt welches den Durchsatzwert also Quotient Kunden/Zeit speichert
	 */
	public final StatisticsQuotientPerformanceIndicator statistic;

	/**
	 * Anfangswert der Erfassung des Durchsatzes
	 * @see #reset(long)
	 * @see #countClient(long)
	 */
	private double startTime;

	/**
	 * Konstruktor der Klasse <code>RunElementThroughputData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param throughputCounterName	Name des Durchsatz-Elements (für die Statistikerfassung)
	 * @param condition	Zusätzliche Bedingung, die für die Zählung eines Kunden erfüllt sein muss
	 * @param throughputStatistic	Statistik-Objekt, welches alle Durchsatz-Werte vorhält
	 */
	public RunElementThroughputData(final RunElement station, final String throughputCounterName, final RunCounterConditionData condition, final StatisticsMultiPerformanceIndicator throughputStatistic) {
		super(station);

		this.condition=condition;
		statistic=(StatisticsQuotientPerformanceIndicator)throughputStatistic.get(throughputCounterName);
		startTime=0;
	}

	@Override
	public double getValue(final boolean fullValue) {
		/* Es gibt nur einen "fullValue", keinen Anteil */
		return statistic.getQuotient();
	}

	/** Umrechnungsfaktor von Millisekunden auf Sekunden, um die Division während der Simulation zu vermeiden */
	private static final double toSec=1.0/1000.0;

	/**
	 * Setzt den Anfangswert der Erfassung des Durchsatzes
	 * @param timeMS	Startzeitpunkt in MS
	 */
	public void reset(final long timeMS) {
		startTime=timeMS*toSec;
		statistic.set(0,0);
	}

	/**
	 * Erfasst, dass ein Kunde die Station passiert hat
	 * @param timeMS	Zeitpunkt in MS
	 */
	public void countClient(final long timeMS) {
		statistic.set(statistic.getNumerator()+1,(timeMS*toSec-startTime));
	}
}