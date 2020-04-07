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
package simulator.coreelements;

/**
 * Wenn eine <code>RunElementData</code> dieses Interface implementiert,
 * kann über die <code>NQ</code>-Rechenfunktion mit Hilfe von zwei Parametern
 * die Teilwarteschlangenlänge (<code>NQ(id,nummer)</code>) abgefragt werden.
 * @author Alexander Herzog
 */
public interface RunElementMultiQueueData {
	/**
	 * Anzahl der verfügbaren Warteschlangen
	 * @return	Anzahl der verfügbaren Warteschlangen
	 */
	int getQueueCount();

	/**
	 * Warteschlangenlänge einer bestimmten Teilwarteschlange
	 * @param queueNumber	Nummer der Teilwarteschlange (0..<code>getQueueCount()</code>-1)
	 * @return	Länge der gewählten Teilwarteschlange
	 */
	int getQueueSize(final int queueNumber);
}
