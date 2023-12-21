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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Laufzeitdaten eines {@link RunElementBarrier}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementBarrier
 * @see RunElementData
 */
public class RunElementBarrierData extends RunElementData implements RunElementDataWithWaitingClients {
	/**
	 * Anzahl an Kunden, die das Element noch direkt durchqueren dürfen (ein Wert jeweils pro Signal-Listener)
	 */
	public final int[] initialClients;

	/**
	 * Liste der wartenden Kunden
	 */
	public final List<RunDataClient> waitingClients;

	/**
	 * Wenn die Warteschlange gerade bearbeitet wird, dürfen per
	 * {@link RunElementBarrier#getClient(simulator.runmodel.SimulationData)}
	 * keine Kunden entnommen werden. Dieser Status kann hier eingestellt werden.
	 * @see RunElementBarrier#processArrival(simulator.runmodel.SimulationData, RunDataClient)
	 * @see RunElementBarrier#signalNotify(simulator.runmodel.SimulationData, String)
	 */
	public boolean queueLockedForPickUp;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param initialClients	Anzahl an Kunden, die das Element noch direkt durchqueren dürfen (ein Wert jeweils pro Signal-Listener)
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementBarrierData(final RunElementBarrier station, final int[] initialClients, final SimulationData simData) {
		super(station,simData);
		queueLockedForPickUp=false;
		this.initialClients=Arrays.copyOf(initialClients,initialClients.length);
		waitingClients=new ArrayList<>();
	}

	@Override
	public List<RunDataClient> getWaitingClients() {
		return waitingClients;
	}
}