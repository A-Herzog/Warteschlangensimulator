/**
 * Copyright 2021 Alexander Herzog
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
package statistics;

/**
 * Verhält sich wie {@link StatisticsSimpleValuePerformanceIndicator},
 * aber addiert die Werte beim Zusammenführen von mehreren Teilobjekten
 * nicht, sondern bildet das Maximum.
 * @author Alexander Herzog
 */
public class StatisticsSimpleValueMaxPerformanceIndicator extends StatisticsSimpleValuePerformanceIndicator {
	/**
	 * Konstruktor der Klasse
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsSimpleValueMaxPerformanceIndicator(String[] xmlNodeNames) {
		super(xmlNodeNames);
	}

	@Override
	public void add(final StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsSimpleValueMaxPerformanceIndicator)) return;
		final StatisticsSimpleValueMaxPerformanceIndicator moreCountStatistics=(StatisticsSimpleValueMaxPerformanceIndicator)moreStatistics;

		value=Math.max(value,moreCountStatistics.value);
	}

	@Override
	public StatisticsSimpleValueMaxPerformanceIndicator clone() {
		final StatisticsSimpleValueMaxPerformanceIndicator indicator=new StatisticsSimpleValueMaxPerformanceIndicator(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	@Override
	public StatisticsSimpleValueMaxPerformanceIndicator cloneEmpty() {
		return new StatisticsSimpleValueMaxPerformanceIndicator(xmlNodeNames);
	}
}
