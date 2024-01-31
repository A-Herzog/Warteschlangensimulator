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
package scripting.java;

import java.util.List;

import mathtools.TimeTools;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Implementierungsklasse für das Interface {@link ClientsInterface}
 * @author Alexander Herzog
 * @see SimulationInterface
 * @see SimulationImpl
 */
public abstract class ClientsBaseImpl implements ClientsInterface {
	/** Simulationsdatenobjekt, dessen Daten bereitgestellt werden sollen */
	protected final SimulationData simData;
	/** Anzahl der wartenden Kunden ({@link #clients}) */
	protected int count;
	/** Liste der wartenden Kunden */
	protected List<RunDataClient> clients;

	/** Umrechnungsfaktor von Simulationszeit auf Sekunden (um während der Simulation Divisionen zu vermeiden) */
	private double toSec;

	/** Umrechnungsfaktor von Sekunden zur Simulationszeit */
	private long toSimTime;

	/**
	 * Konstruktor der Klasse
	 * @param simData	Simulationsdatenobjekt, dessen Daten bereitgestellt werden sollen
	 */
	public ClientsBaseImpl(final SimulationData simData) {
		this.simData=simData;
		this.toSec=simData.runModel.scaleToSeconds;
		this.toSimTime=simData.runModel.scaleToSimTime;
	}

	/**
	 * Stellt eine Liste der wartenden Kunden ein.
	 * @param clients	Liste der wartenden Kunden ein
	 */
	public void setClients(final List<RunDataClient> clients) {
		this.clients=clients;
		count=clients.size();
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public String clientTypeName(final int index) {
		if (index<0 || index>=count) return "";
		return simData.runModel.clientTypes[clients.get(index).type];
	}

	@Override
	public String[] clientBatchTypeNames(final int index) {
		if (index<0 || index>=count) return new String[0];
		final List<RunDataClient> batchedClients=clients.get(index).getBatchData();
		if (batchedClients==null) return new String[0];
		return batchedClients.stream().map(c->simData.runModel.clientTypes[c.type]).toArray(String[]::new);
	}

	@Override
	public int clientSourceStationID(final int index) {
		if (index<0 || index>=count) return 0;
		return clients.get(index).sourceStationID;
	}

	@Override
	public double clientData(final int index, final int data) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).getUserData(data);
	}

	@Override
	public void clientData(final int index, final int data, final double value) {
		if (index<0 || index>=count) return;
		clients.get(index).setUserData(data,value);
	}

	@Override
	public String clientTextData(final int index, final String key) {
		if (index<0 || index>=count) return "";
		return clients.get(index).getUserDataString(key);
	}

	@Override
	public void clientTextData(final int index, final String key, final String value) {
		if (index<0 || index>=count) return;
		clients.get(index).setUserDataString(key,value);
	}

	@Override
	public double clientWaitingSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).waitingTime*toSec;
	}

	@Override
	public String clientWaitingTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(clients.get(index).waitingTime*toSec);
	}

	@Override
	public void clientWaitingSecondsSet(final int index, final double time) {
		if (index<0 || index>=count) return;
		clients.get(index).waitingTime=Math.round(time*toSimTime);
	}

	@Override
	public double clientTransferSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).transferTime*toSec;
	}

	@Override
	public String clientTransferTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(clients.get(index).transferTime*toSec);
	}

	@Override
	public void clientTransferSecondsSet(final int index, final double time) {
		if (index<0 || index>=count) return;
		clients.get(index).transferTime=Math.round(time*toSimTime);
	}

	@Override
	public double clientProcessSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).processTime*toSec;
	}

	@Override
	public String clientProcessTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(clients.get(index).processTime*toSec);
	}

	@Override
	public void clientProcessSecondsSet(final int index, final double time) {
		if (index<0 || index>=count) return;
		clients.get(index).processTime=Math.round(time*toSimTime);
	}

	@Override
	public double clientResidenceSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).residenceTime*toSec;
	}

	@Override
	public String clientResidenceTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(clients.get(index).residenceTime*toSec);
	}

	@Override
	public void clientResidenceSecondsSet(final int index, final double time) {
		if (index<0 || index>=count) return;
		clients.get(index).residenceTime=Math.round(time*toSimTime);
	}
}
