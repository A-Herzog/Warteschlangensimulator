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
package simulator.events;

import simcore.Event;
import simcore.SimData;
import simulator.coreelements.RunElement;
import simulator.elements.RunElementBatch;
import simulator.elements.RunElementBatchMulti;
import simulator.elements.RunElementProcess;
import simulator.runmodel.SimulationData;

/**
 * Beendet das Warten auf weitere Kunden (was angedacht war, um näher an die maximale Batch-Größe zu kommen).
 * @author Alexander Herzog
 * @see RunElementProcess
 * @see RunElementBatch
 * @see RunElementBatchMulti
 */
public class ProcessWaitingClientsEvent extends Event {
	/**
	 * Station an der das Warten auf weitere Kunden beendet werden soll
	 */
	public RunElement station;

	@Override
	public void run(SimData data) {
		/* Wir haben gewartet, ob sich nach dem Eintreffen von batchMinSize Kunden noch weitere Kunden finden,
		 * um näher an batchMaxSize zu kommen. Aber das war wohl nicht der Fall, also Bedienung starten.
		 */
		station.processArrival((SimulationData)data,null);

		/* System über Status-Änderung benachrichtigen */
		((SimulationData)data).runData.fireStateChangeNotify((SimulationData)data);
	}
}
