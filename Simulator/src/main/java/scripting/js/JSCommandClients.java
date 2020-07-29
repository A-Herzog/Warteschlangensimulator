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
package scripting.js;

import java.util.Arrays;
import java.util.List;

import mathtools.TimeTools;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Stellt das "Clients"-Objekt in Javascript-Umgebungen zur Verfügung
 * @author Alexander Herzog
 */
public final class JSCommandClients extends JSBaseCommand {
	private SimulationData simData;
	private int count;
	private List<RunDataClient> clients;
	private boolean[] releaseClients;

	/**
	 * Konstruktor der Klasse <code>JSCommandClients</code>
	 * @param output	Wird aufgerufen, wenn Meldungen usw. ausgegeben werden sollen
	 */
	public JSCommandClients(final JSOutputWriter output) {
		super(output);
	}

	/**
	 * Stellt die Simulationsdaten und die Liste der wartenden Kunden für die Abfrage durch das Javascript-Verknüpfungs-Objekt ein
	 * @param simData	Simulationsdaten-Objekt (kann auch <code>null</code> sein)
	 * @param clients	Liste der wartenden Kunden
	 */
	public void setSimulationData(final SimulationData simData, final List<RunDataClient> clients) {
		this.simData=simData;
		this.clients=clients;
		count=clients.size();
		if (releaseClients!=null && releaseClients.length>=count) {
			Arrays.fill(releaseClients,false);
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

	/**
	 * Gibt an, wie viele Kunden momentan warten.
	 * @return	Anzahl der wartenden Kunden.
	 */
	public int count() {
		return count;
	}

	/**
	 * Gibt einen wartenden Kunden frei.
	 * @param index	Index des Kunden muss größer oder gleich 0 sein und kleiner als {@link #count()}
	 */
	public void release(final int index) {
		if (index<0 || index>=count) return;
		releaseClients[index]=true;
	}

	/**
	 * Liefert den Namen eines Kunden
	 * @param index	0-basierender Index des Kunden
	 * @return	Name des Kunden
	 * @see #setSimulationData(SimulationData, List)
	 */
	public String clientTypeName(final int index) {
		if (index<0 || index>=count) return "";
		return simData.runModel.clientTypes[clients.get(index).type];
	}

	/**
	 * Liefert ein Client-Daten-Element eines Kunden
	 * @param index	0-basierender Index des Kunden
	 * @param data	Index des Datenelements
	 * @return	Daten-Element des Kunden
	 */
	public double clientData(final int index, final int data) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).getUserData(data);
	}

	/**
	 * Stellt ein Client-Daten-Element eines Kunden ein
	 * @param index	0-basierender Index des Kunden
	 * @param data	Index des Datenelements
	 * @param value	Neuer Wert
	 */
	public void clientData(final int index, final int data, final double value) {
		if (index<0 || index>=count) return;
		clients.get(index).setUserData(data,value);
	}

	/**
	 * Liefert ein Client-Textdaten-Element eines Kunden
	 * @param index	0-basierender Index des Kunden
	 * @param key	Schlüssel des Datenelements
	 * @return	Daten-Element des Kunden
	 */
	public String clientTextData(final int index, final String key) {
		if (index<0 || index>=count) return "";
		return clients.get(index).getUserDataString(key);
	}

	/**
	 * Stellt ein Client-Textdaten-Element eines Kunden ein
	 * @param index	0-basierender Index des Kunden
	 * @param key	Schlüssel des Datenelements
	 * @param value	Neuer Wert
	 */
	public void clientTextData(final int index, final String key, final String value) {
		if (index<0 || index>=count) return;
		clients.get(index).setUserDataString(key,value);
	}

	/**
	 * Liefert die bisherige Wartezeit eines Kunden in Sekunden als Zahlenwert
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Wartezeit des Kunden
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientWaitingTime(int)
	 */
	public double clientWaitingSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).waitingTime/1000.0;
	}

	/**
	 * Liefert die bisherige Wartezeit eines Kunden in formatierter Form als Zeichenkette
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Wartezeit des Kunden
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientWaitingSeconds(int)
	 */
	public String clientWaitingTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(((double)clients.get(index).waitingTime)/1000);
	}

	/**
	 * Liefert die bisherige Transferzeit eines Kunden in Sekunden als Zahlenwert
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Transferzeit des Kunden
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientTransferTime(int)
	 */
	public double clientTransferSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).transferTime/1000.0;
	}

	/**
	 * Liefert die bisherige Transferzeit eines Kunden in formatierter Form als Zeichenkette
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Transferzeit des Kunden
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientTransferSeconds(int)
	 */
	public String clientTransferTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(((double)clients.get(index).transferTime)/1000);
	}

	/**
	 * Liefert die bisherige Bedienzeit eines Kunden in Sekunden als Zahlenwert
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Bedienzeit des Kunden
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientProcessTime(int)
	 */
	public double clientProcessSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).processTime/1000.0;
	}

	/**
	 * Liefert die bisherige Bedienzeit eines Kunden in formatierter Form als Zeichenkette
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Bedienzeit des Kunden
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientProcessSeconds(int)
	 */
	public String clientProcessTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(((double)clients.get(index).processTime)/1000);
	}

	/**
	 * Liefert die bisherige Bedienzeit eines Kunden in Sekunden als Zahlenwert
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Bedienzeit des Kunden
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientResidenceTime(int)
	 */
	public double clientResidenceSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).residenceTime/1000.0;
	}

	/**
	 * Liefert die bisherige Bedienzeit eines Kunden in formatierter Form als Zeichenkette
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Bedienzeit des Kunden
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientResidenceSeconds(int)
	 */
	public String clientResidenceTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(((double)clients.get(index).residenceTime)/1000);
	}
}