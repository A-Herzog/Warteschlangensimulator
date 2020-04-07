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
package scripting.java;

import java.util.Arrays;
import java.util.List;

import mathtools.TimeTools;
import scripting.js.JSCommandClients;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Implementierungsklasse für das Interface {@link ClientsInterface}
 * @author Alexander Herzog
 * @see SimulationInterface
 * @see SimulationImpl
 */
public class ClientsImpl implements ClientsInterface {
	private final SimulationData simData;
	private int count;
	private List<RunDataClient> clients;
	private boolean[] releaseClients;

	/**
	 * Konstruktor der Klasse
	 * @param simData	Simulationsdatenobjekt, dessen Daten bereitgestellt werden sollen
	 */
	public ClientsImpl(final SimulationData simData) {
		this.simData=simData;
	}

	/**
	 * Stellt eine Liste der wartenden Kunden ein.
	 * @param clients	Liste der wartenden Kunden ein
	 */
	public void setClients(final List<RunDataClient> clients) {
		this.clients=clients;
		count=clients.size();
		if (releaseClients!=null && releaseClients.length>=count) {
			Arrays.fill(releaseClients,0,count,false);
		} else {
			releaseClients=new boolean[count*2];
		}
	}

	/**
	 * Gibt an, welche der Kunden freigegeben werden sollen.<br>
	 * Achtung: Das Array kann <b>länger</b> als das Kundenarray sein!
	 * @return	Array mit Angaben darüber, welche Kunden freigegeben werden sollen
	 * @see JSCommandClients#setSimulationData(SimulationData, List)
	 */
	public boolean[] getSimulationData() {
		return releaseClients;
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public void release(final int index) {
		if (index<0 || index>=count) return;
		releaseClients[index]=true;
	}

	@Override
	public String clientTypeName(final int index) {
		if (index<0 || index>=count) return "";
		return simData.runModel.clientTypes[clients.get(index).type];
	}

	@Override
	public double clientData(final int index, final int data) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).getUserData(data);
	}

	@Override
	public String clientTextData(final int index, final String key) {
		if (index<0 || index>=count) return "";
		return clients.get(index).getUserDataString(key);
	}

	@Override
	public double clientWaitingSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).waitingTime/1000.0;
	}

	@Override
	public String clientWaitingTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(((double)clients.get(index).waitingTime)/1000);
	}

	@Override
	public double clientTransferSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).transferTime/1000.0;
	}

	@Override
	public String clientTransferTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(((double)clients.get(index).transferTime)/1000);
	}

	@Override
	public double clientProcessSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).processTime/1000.0;
	}

	@Override
	public String clientProcessTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(((double)clients.get(index).processTime)/1000);
	}

	@Override
	public double clientResidenceSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).residenceTime/1000.0;
	}

	@Override
	public String clientResidenceTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(((double)clients.get(index).residenceTime)/1000);
	}
}
