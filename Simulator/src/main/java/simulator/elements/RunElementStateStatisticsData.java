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
import statistics.StatisticsStateTimePerformanceIndicator;

/**
 * Laufzeitdaten eines <code>RunElementStateStatistics</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementStateStatistics
 * @see RunElementData
 */
public class RunElementStateStatisticsData extends RunElementData {
	/**
	 * Statistikobjekt welches den Wert speichert
	 */
	public final StatisticsStateTimePerformanceIndicator statistic;

	/**
	 * Konstruktor der Klasse <code>RunElementStateStatisticsData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param statistic	Zugehöriges Statistikobjekt
	 */
	public RunElementStateStatisticsData(final RunElement station, final StatisticsStateTimePerformanceIndicator statistic) {
		super(station);
		this.statistic=statistic;
	}
}