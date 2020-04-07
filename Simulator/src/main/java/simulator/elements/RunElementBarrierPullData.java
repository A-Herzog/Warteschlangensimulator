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
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Laufzeitdaten eines <code>RunElementBarrierPull</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementBarrierPull
 * @see RunElementData
 */
public class RunElementBarrierPullData extends RunElementData {
	/**
	 * Liste der wartenden Kunden
	 */
	public final List<RunDataClient> waitingClients;

	/**
	 * Warteschlange f�r Arbeiten gesperrt; kein PickUp m�glich;
	 */
	public boolean queueLockedForPickUp;

	/**
	 * Zeitpunkt der letzten Freigabe
	 */
	public long lastSendTime;

	private final ExpressionCalc maxClients;
	private final int nextControlledID;

	private RunElement nextDirect;
	private RunElement nextControlled;
	private RunElement[] nextControlledAddon;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param nextControlledID	ID des Elements bis zu dem die Anzahl an Kunden �berwacht werden soll
	 * @param maxClients	Maximalanzahl an Kunden in dem Abschnitt
	 * @param variableNames	Liste aller Variablennamen (f�r den <code>maxClients</code>-Rechenausdruck)
	 */

	public RunElementBarrierPullData(final RunElement station, final int nextControlledID, final String maxClients, final String[] variableNames) {
		super(station);
		queueLockedForPickUp=false;
		waitingClients=new ArrayList<>();
		lastSendTime=-1;
		this.nextControlledID=nextControlledID;
		this.maxClients=new ExpressionCalc(variableNames);
		this.maxClients.parse(maxClients);
	}

	private void getBarrierAddOns(final SimulationData simData, final RunElement process, final List<RunElement> list) {
		for (RunElement element: simData.runModel.elementsFast) if (element instanceof RunElementBarrierPull) {
			final RunElementBarrierPull barrier=(RunElementBarrierPull)element;
			if (barrier.getNext()==process && !list.contains(barrier)) list.add(barrier);
		}
	}

	private void getPathAddOns(final SimulationData simData, final RunElement processA, final RunElement processB, final List<RunElement> list) {
		final List<RunElement> partialList=new ArrayList<>();
		int steps=0;
		RunElement next=processA.getNext();
		while (next!=null) {
			steps++;
			if (steps>10) return; /* Weg zu Lang, Abbruch */
			if (!partialList.contains(next) && !list.contains(next)) partialList.add(next);
			next=next.getNext();
			if (next==processB) {list.addAll(partialList); return;} /* Hurra, Weg gefunden. */
		}
		/* Kein Weg gefunden */
	}

	private void initConnections(final SimulationData simData) {
		nextDirect=station.getNext();
		nextControlled=simData.runModel.elementsFast[nextControlledID];
		if (nextDirect==nextControlled) {
			nextControlledAddon=new RunElement[0];
		} else {
			final List<RunElement> list=new ArrayList<>();
			getBarrierAddOns(simData,nextControlled,list);
			getPathAddOns(simData,nextDirect,nextControlled,list);
			nextControlledAddon=list.toArray(new RunElement[0]);
		}
	}

	/**
	 * Gibt an, ob in dem �berwachten Abschnitt die zul�ssige Anzahl an Kunden noch nicht erreicht ist, so dass weitere Kunden freigegeben werden d�rfen
	 * @param simData	Simulationsdatenobjekt
	 * @return	Liefert <code>true</code>, wenn mindestens ein weiterer Kunde freigegeben werden darf
	 */
	public boolean releaseOK(final SimulationData simData) {
		/* Verkn�pfungen vorbereiten */
		if (nextDirect==null) initConnections(simData);

		/* Anzahl an Kunden im Segment */
		int sum=nextDirect.getData(simData).reportedClientsAtStation(simData);
		if (nextControlled!=nextDirect) sum+=nextControlled.getData(simData).reportedClientsAtStation(simData);
		for (RunElement addon: nextControlledAddon) sum+=addon.getData(simData).reportedClientsAtStation(simData);

		/* Erlaubte Maximalanzahl */
		simData.runData.setClientVariableValues(null);
		final int maxClients;
		if (simData.runModel.stoppOnCalcError) {
			final Double D=this.maxClients.calc(simData.runData.variableValues,simData,null);
			if (D==null) simData.calculationErrorStation(this.maxClients,this);
			maxClients=(int)FastMath.round((D==null)?1.0:D.doubleValue());
		} else {
			maxClients=(int)FastMath.round(this.maxClients.calcOrDefault(simData.runData.variableValues,simData,null,1));
		}

		/* D�rfen wir noch Kunden freigeben? */
		return (sum<maxClients);
	}
}