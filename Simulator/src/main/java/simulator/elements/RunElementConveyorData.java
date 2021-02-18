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

import language.Language;
import mathtools.NumberTools;
import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.events.ConveyorSystemChangeEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Laufzeitdaten eines <code>RunElementConveyor</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementConveyor
 * @see RunElementData
 */
public class RunElementConveyorData extends RunElementData implements RunElementDataWithWaitingClients {
	/**
	 * Initiale Größe für {@link #waitingClients}
	 */
	private static final int DEFAULT_QUEUE_SIZE=256;

	/**
	 * Verfügbare Kapazität
	 */
	public double freeCapacity;

	/**
	 * Wartende Kunden
	 */
	public final List<RunDataClient> waitingClients;

	/**
	 * Verzögerungsausdrucke
	 */
	public ExpressionCalc[] capacityNeeded;

	/**
	 * Findet gerade eine Verarbeitung statt, so dass keine Kunden entnommen werden können?
	 */
	public boolean queueLockedForPickUp;

	/**
	 * Notwendige Zeit, um einen Kunden über das Fließband zu bewegen
	 */
	private long transportTimeMS;

	/**
	 * Existiert ein von {@link #triggerUpdateEvent(SimulationData)}
	 * generiertes, noch nicht abgearbeitetes Event?
	 * @see #triggerUpdateEvent(SimulationData)
	 * @see ConveyorSystemChangeEvent
	 */
	private boolean notifyTriggered;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param capacityNeededStrings	Kundentyp-abhängige pro Kunde benötigte Kapazitäten auf dem Fließband
	 * @param variableNames	Liste der globalen Variablen (zur Initialisierung der Rechenausdrücke)
	 * @param freeCapacity	Verfügbare Kapazität
	 * @param transportTimeMS	Notwendige Zeit, um einen Kunden über das Fließband zu bewegen
	 */
	public RunElementConveyorData(final RunElement station, final String[] capacityNeededStrings, final String[] variableNames, final double freeCapacity, final long transportTimeMS) {
		super(station);
		queueLockedForPickUp=false;
		waitingClients=new ArrayList<>(DEFAULT_QUEUE_SIZE);

		capacityNeeded=new ExpressionCalc[capacityNeededStrings.length];
		for (int i=0;i<capacityNeededStrings.length;i++) if (capacityNeededStrings[i]!=null) {
			capacityNeeded[i]=new ExpressionCalc(variableNames);
			capacityNeeded[i].parse(capacityNeededStrings[i]);
		}

		this.freeCapacity=freeCapacity;
		this.transportTimeMS=transportTimeMS;
		notifyTriggered=false;
	}

	/**
	 * Fügt einen Kunden zu der Liste der wartenden Kunden hinzu
	 * @param client	Hinzuzufügender Kunde
	 * @param time	Zeitpunkt an dem der Kunde an der <code>RunElementProcess</code>-Station eingetroffen ist (zur späteren Berechnung der Wartezeit der Kunden)
	 * @param simData	Simulationsdatenobjekt
	 */
	public void addClientToQueue(final RunDataClient client, final long time, final SimulationData simData) {
		/* Kunden an Warteschlange anstellen */
		client.stationInformationLong=-1;
		waitingClients.add(client);
		client.lastWaitingStart=time;
		client.stationInformationDouble=getNeededCapacity(simData,client);

		/* Logging */
		if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.ConveyorArrival"),String.format(Language.tr("Simulation.Log.ConveyorArrival.Info"),client.logInfo(simData),station.name,NumberTools.formatNumber(client.stationInformationDouble)));

		/* Statistik */
		simData.runData.logClientEntersStationQueue(simData,station,this,client);

		/* Bei Animationen Zwischenevent einfügen, um Darstellung flüssig zu gestalten */
		if (simData.runModel.isAnimation) triggerUpdateEvent(simData);
	}

	/**
	 * Entfernt einen Kunden aus der Warteschlange
	 * @param client	Zu entfernender Kunde
	 * @param indexOfClientInQueue	Index des Kunden in der <code>waitingClients</code>-Liste. Kann -1 sein, dann wird der Index gemäß dem <code>client</code>-Objekt selbst ermittelt
	 * @param time	Zeitpunkt, an dem der Kunde entfernt werden soll
	 * @param simData	Simulationsdaten
	 * @return	Gibt die Wartezeit des Kunden zurück
	 */
	public long removeClientFromQueue(final RunDataClient client, final int indexOfClientInQueue, final long time, final SimulationData simData) {
		final int index=(indexOfClientInQueue>=0)?indexOfClientInQueue:waitingClients.indexOf(client);
		if (index<0) return 0;
		final long timeInQueue=time-client.lastWaitingStart;

		/* Kunde aus Warteschlange austragen */
		waitingClients.remove(index);

		/* Statistik */
		simData.runData.logClientLeavesStationQueue(simData,station,this,client);

		/* Wartezeit zurückliefern */
		return timeInQueue;
	}

	/** Umrechnungsfaktor von Millisekunden auf Sekunden, um die Division während der Simulation zu vermeiden */
	private static final double toSec=1.0/1000.0;

	/**
	 * Liefert notwendige Kapazität für einen Kunden
	 * @param simData	Simulationsdaten
	 * @param client	Kunde, für den die Kapazität bestimmt werden soll
	 * @return	Bedienzeit in Sekunden
	 */
	public double getNeededCapacity(final SimulationData simData, final RunDataClient client) {
		final int type=client.type;

		final double additionalWaitingTime=(simData.currentTime-client.lastWaitingStart)*toSec;
		simData.runData.setClientVariableValues(client,additionalWaitingTime);
		try {
			return capacityNeeded[type].calc(simData.runData.variableValues,simData,client);
		} catch (MathCalcError e) {
			simData.calculationErrorStation(capacityNeeded[type],this);
			return 0;
		}
	}

	/**
	 * Wird durch {@link ConveyorSystemChangeEvent} ausgelöst und benachrichtigt
	 * das Fließband, dass sich der Systemzustand in Bezug auf die Bewegung
	 * eines Kunden auf dem Fließband verändert.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void updateEvent(final SimulationData simData) {
		notifyTriggered=false;
		if (clientsAtStation>0) triggerUpdateEvent(simData);
		simData.runData.fireStateChangeNotify(simData);
	}

	/**
	 * Generiert ein Event zur Aktualisierung der Fließbanddaten
	 * @param simData	Simulationsdatenobjekt
	 */
	private void triggerUpdateEvent(final SimulationData simData) {
		if (notifyTriggered && transportTimeMS>1000) return;
		if (simData.runData.stopp) return;
		final ConveyorSystemChangeEvent event=(ConveyorSystemChangeEvent)simData.getEvent(ConveyorSystemChangeEvent.class);
		event.init(simData.currentTime+transportTimeMS/10);
		event.conveyorData=this;
		simData.eventManager.addEvent(event);
		notifyTriggered=true;
	}

	@Override
	public List<RunDataClient> getWaitingClients() {
		return waitingClients;
	}
}
