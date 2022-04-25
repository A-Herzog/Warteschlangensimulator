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
package scripting.js;

import java.util.List;

import mathtools.TimeTools;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Basisklasse für "Clients"-Objekts in Javascript-Umgebungen
 * @author Alexander Herzog
 */
public class JSCommandClientsBase extends JSBaseCommand {
	/** Simulationsdaten-Objekt (kann auch <code>null</code> sein) */
	protected SimulationData simData;
	/** Anzahl der wartenden Kunden ({@link #clients}) */
	protected int count;
	/** Liste der wartenden Kunden */
	protected List<RunDataClient> clients;

	/** Umrechnungsfaktor von Millisekunden auf Sekunden (um während der Simulation Divisionen zu vermeiden) */
	private static final double toSec=1.0/1000.0;

	/**
	 * Konstruktor der Klasse <code>JSCommandClients</code>
	 */
	public JSCommandClientsBase() {
		super(null);
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
	}

	/**
	 * Gibt an, wie viele Kunden momentan warten.
	 * @return	Anzahl der wartenden Kunden.
	 */
	public int count() {
		return count;
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
		return clients.get(index).waitingTime*toSec;
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
		return TimeTools.formatExactTime(clients.get(index).waitingTime*toSec);
	}

	/**
	 * Stellt die Wartezeit des Kunden ein.
	 * @param index	0-basierender Index des Kunden
	 * @param time	Wartezeit des Kunden (in Sekunden)
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientWaitingSeconds(int)
	 */
	public void clientWaitingSecondsSet(final int index, final double time) {
		if (index<0 || index>=count) return;
		clients.get(index).waitingTime=Math.round(time*1000);
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
		return clients.get(index).transferTime*toSec;
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
		return TimeTools.formatExactTime(clients.get(index).transferTime*toSec);
	}

	/**
	 * Stellt die Transferzeit des Kunden ein.
	 * @param index	0-basierender Index des Kunden
	 * @param time	Transferzeit des Kunden (in Sekunden)
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientTransferSeconds(int)
	 */
	public void clientTransferSecondsSet(final int index, final double time) {
		if (index<0 || index>=count) return;
		clients.get(index).transferTime=Math.round(time*1000);
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
		return clients.get(index).processTime*toSec;
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
		return TimeTools.formatExactTime(clients.get(index).processTime*toSec);
	}

	/**
	 * Stellt die Bedienzeit des Kunden ein.
	 * @param index	0-basierender Index des Kunden
	 * @param time	Bedienzeit des Kunden (in Sekunden)
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientProcessSeconds(int)
	 */
	public void clientProcessSecondsSet(final int index, final double time) {
		if (index<0 || index>=count) return;
		clients.get(index).processTime=Math.round(time*1000);
	}

	/**
	 * Liefert die bisherige Verweilzeit eines Kunden in Sekunden als Zahlenwert
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Verweilzeit des Kunden
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientResidenceTime(int)
	 */
	public double clientResidenceSeconds(final int index) {
		if (index<0 || index>=count) return 0.0;
		return clients.get(index).residenceTime*toSec;
	}

	/**
	 * Liefert die bisherige Verweilzeit eines Kunden in formatierter Form als Zeichenkette
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Verweilzeit des Kunden
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientResidenceSeconds(int)
	 */
	public String clientResidenceTime(final int index) {
		if (index<0 || index>=count) return "";
		return TimeTools.formatExactTime(clients.get(index).residenceTime*toSec);
	}

	/**
	 * Stellt die Verweilzeit des Kunden ein.
	 * @param index	0-basierender Index des Kunden
	 * @param time	Verweilzeit des Kunden (in Sekunden)
	 * @see #setSimulationData(SimulationData, List)
	 * @see #clientResidenceSeconds(int)
	 */
	public void clientResidenceSecondsSet(final int index, final double time) {
		if (index<0 || index>=count) return;
		clients.get(index).residenceTime=Math.round(time*1000);
	}
}