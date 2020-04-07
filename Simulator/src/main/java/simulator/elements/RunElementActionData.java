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

/**
 * Laufzeitdaten eines <code>RunElementAction</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementAction
 * @see RunElementData
 */
public class RunElementActionData extends RunElementData {
	/**
	 * Liste mit den thread-spezifischen Laufzeit-Zuweisungsdatensätzen
	 */
	public final RunElementActionRecord[] records;

	/**
	 * Konstruktor der Klasse <code>RunElementActionData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param records	Liste mit den thread-spezifischen Laufzeit-Zuweisungsdatensätzen
	 */
	public RunElementActionData(final RunElement station, final RunElementActionRecord[] records) {
		super(station);
		this.records=records;
	}
}
