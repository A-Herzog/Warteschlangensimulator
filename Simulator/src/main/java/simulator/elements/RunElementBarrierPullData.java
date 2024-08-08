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

import parser.MathCalcError;
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
public class RunElementBarrierPullData extends RunElementData implements RunElementDataWithWaitingClients {
	/**
	 * Liste der wartenden Kunden
	 */
	public final List<RunDataClient> waitingClients;

	/**
	 * Warteschlange für Arbeiten gesperrt; kein PickUp möglich;
	 */
	public boolean queueLockedForPickUp;

	/**
	 * Zeitpunkt der letzten Freigabe
	 */
	public long lastSendTime;

	/**
	 * Maximale Anzahl an Kunden im Segment
	 */
	private final ExpressionCalc maxClients;

	/**
	 * ID des Elements bis zu dem die Anzahl an Kunden überwacht werden soll
	 */
	private final int nextControlledID;

	/**
	 * Nächste Station
	 */
	private RunElement nextDirect;

	/**
	 * Element bis zu dem die Anzahl an Kunden überwacht werden soll (Übersetzung von {@link #nextControlledID})
	 */
	private RunElement nextControlled;

	/**
	 * Weitere Stationen auf der kontrollierten Strecke deren Kundenbestände folglich berücksichtigt werden müssen.
	 */
	private RunElement[] nextControlledAddon;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param nextControlledID	ID des Elements bis zu dem die Anzahl an Kunden überwacht werden soll
	 * @param maxClients	Maximalanzahl an Kunden in dem Abschnitt
	 * @param variableNames	Liste aller Variablennamen (für den <code>maxClients</code>-Rechenausdruck)
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementBarrierPullData(final RunElement station, final int nextControlledID, final String maxClients, final String[] variableNames, final SimulationData simData) {
		super(station,simData);
		queueLockedForPickUp=false;
		waitingClients=new ArrayList<>();
		lastSendTime=-1;
		this.nextControlledID=nextControlledID;
		this.maxClients=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
		this.maxClients.parse(maxClients);
	}


	/**
	 * Sucht weitere {@link RunElementBarrierPull}-Stationen, die sich auf eine Bedienstation beziehen.
	 * @param simData	Simulationsdatenobjekt
	 * @param process	Bedienstation
	 * @param list	Liste der Pull-Barrieren, die sich auf die Bedienstation beziehen
	 * @see #initConnections(SimulationData)
	 */
	private void getBarrierAddOns(final SimulationData simData, final RunElement process, final List<RunElement> list) {
		for (RunElement element: simData.runModel.elementsFast) if (element instanceof RunElementBarrierPull) {
			final RunElementBarrierPull barrier=(RunElementBarrierPull)element;
			if (barrier.getNext()==process && !list.contains(barrier)) list.add(barrier);
		}
	}

	/**
	 * Sucht Stationen, die sich auf dem Weg zwischen zwei Stationen befinden
	 * @param simData	Simulationsdatenobjekt
	 * @param processA	Startstation
	 * @param processB	Zielstation
	 * @param list	Liste der Zwischenstationen
	 * @see #initConnections(SimulationData)
	 */
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

	/**
	 * Konfiguriert die Stationen, die für die Schranke relevant sind.
	 * @param simData	Simulationsdatenobjekt
	 * @see #nextDirect
	 * @see #nextControlled
	 * @see #nextControlledAddon
	 */
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
	 * Gibt an, ob in dem überwachten Abschnitt die zulässige Anzahl an Kunden noch nicht erreicht ist, so dass weitere Kunden freigegeben werden dürfen
	 * @param simData	Simulationsdatenobjekt
	 * @return	Liefert <code>true</code>, wenn mindestens ein weiterer Kunde freigegeben werden darf
	 */
	public boolean releaseOK(final SimulationData simData) {
		/* Verknüpfungen vorbereiten */
		if (nextDirect==null) initConnections(simData);

		/* Anzahl an Kunden im Segment */
		int sum=nextDirect.getData(simData).reportedClientsAtStation(simData);
		if (nextControlled!=nextDirect) sum+=nextControlled.getData(simData).reportedClientsAtStation(simData);
		for (RunElement addon: nextControlledAddon) sum+=addon.getData(simData).reportedClientsAtStation(simData);

		/* Erlaubte Maximalanzahl */
		simData.runData.setClientVariableValues(null);
		int maxClients;
		try {
			final double d=this.maxClients.calc(simData.runData.variableValues,simData,null);
			maxClients=(int)FastMath.round(d);
		} catch (MathCalcError e) {
			simData.calculationErrorStation(this.maxClients,this);
			maxClients=1;
		}

		/* Dürfen wir noch Kunden freigeben? */
		return (sum<maxClients);
	}

	@Override
	public List<RunDataClient> getWaitingClients() {
		return waitingClients;
	}
}