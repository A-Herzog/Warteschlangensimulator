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
 * Laufzeitdaten eines <code>RunElementTankFlowBySignal</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementTankFlowBySignal
 * @see RunElementData
 */
public class RunElementTankFlowBySignalData extends RunElementData {
	/** Laufzeitdaten zu dem auszulösenden Fluss */
	private final RunElementTankFlow flowData;
	/** Quelltank */
	private final RunElementTank source;
	/** Zieltank */
	private final RunElementTank destination;

	/**
	 * Konstruktor der Klasse <code>RunElementTankFlowBySignalData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param flowData	Auszulösender Fluss
	 * @param source	Quell-Tank für den Fluss
	 * @param destination	Ziel-Tank für den Fluss
	 */
	public RunElementTankFlowBySignalData(final RunElement station, final RunElementTankFlow flowData, final RunElementTank source, final RunElementTank destination) {
		super(station);
		this.flowData=flowData;
		this.source=source;
		this.destination=destination;
	}

	/**
	 * Liefert ein konkretes, eigenständiges Fluss-Datenelement
	 * @param time	Aktuelle Zeit (in MS)
	 * @return	Fluss-Datenelement
	 */
	public RunElementTankFlow getFlow(final long time) {
		return new RunElementTankFlow(flowData,source,destination,time);
	}
}
