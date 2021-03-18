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
package statistics;

/**
 * Interface, welches in einigen Funktionen von {@link StatisticsDataCollector}
 * genutzt werden kann, um Messwerte vor der Summation zu gewichten.
 * @author Alexander Herzog
 * @see StatisticsDataCollector#getProcessedSum(ValueProcessorFunction)
 * @see StatisticsDataCollector#getProcessedSum(int, int, ValueProcessorFunction)
 */
public interface ValueProcessorFunction {
	/**
	 * Funktion, die bei der Berechnung der Summe mit jedem Messwert,
	 * der in die Summe aufgenommen werden soll, aufgerufen wird.
	 * @param index	Index des Messwertes
	 * @param value	Wert am Index
	 * @return	Verarbeiteter Messwert
	 */
	double process(int index, double value);

}
