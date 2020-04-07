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
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionEval;

/**
 * Laufzeitdaten eines <code>RunElementTransportTransporterSource</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementTransportTransporterSource
 * @see RunElementData
 */
public class RunElementTransportTransporterSourceData extends RunElementData {
	/**
	 * Anzahl an momentan hier parkenden Transportern.
	 */
	public int count;

	/**
	 * Anzahl an Transportern, die sich momentan zu der Station bewegen.
	 */
	public int moving;

	/**
	 * Wartende Kunden
	 */
	public final List<RunDataClient> queue;

	/**
	 * Priorität zum Anfordern von Transportern (um sie hier abzustellen)
	 */
	public final ExpressionCalc priorityParking;

	/**
	 * Priorität zum Anfordern von Transportern (weil es wartende Kunden gibt)
	 */
	public final ExpressionCalc priorityRequest;

	/**
	 * Priorität der Kunden, in einem Transport mitgenommen zu werden
	 */
	public final ExpressionCalc[] priorityClient;

	/**
	 * Alle Kundentypen mit Standardpriorität?
	 */
	public final boolean priorityClientAllFirstComeFirstServe;

	/**
	 * Routingziel-Ausdrücke
	 */
	public final ExpressionEval[] routingExpresions;

	/**
	 * Wird von <code>RunElementTransportTransporterSource#getClientsToMove(SimulationData,List&lt;RunDataClient&gt;)</code> verwendet, um nicht jedes Mal neue Objekte anlegen zu müssen.
	 */
	public RunDataClient[] cacheListClient;

	/**
	 * Wird von <code>RunElementTransportTransporterSource#getClientsToMove(SimulationData,List&lt;RunDataClient&gt;)</code> verwendet, um nicht jedes Mal neue Objekte anlegen zu müssen.
	 */
	public double[] cacheListScore;

	/**
	 * Wird von <code>RunElementTransportTransporterSource#getClientsToMove(SimulationData,List&lt;RunDataClient&gt;)</code> verwendet, um nicht jedes Mal neue Objekte anlegen zu müssen.
	 */
	public int[] cacheListTarget;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param priorityWaitingString	Formel zur Berechnung der Priorität zur Anforderung von Transportern (um sie hier abzustellen)
	 * @param priorityRequestingString	Formel zur Berechnung der Priorität zur Anforderung von Transportern (weil es wartende Kunden gibt)
	 * @param priorityClientString	Priorität der Kunden, in einem Transport mitgenommen zu werden
	 * @param routingExpresionStrings	Bedingungen zum Routing der Kunden (einzelne Einträge können <code>null</code> sein, wenn eine Verzweigung nach Kundentyp verwendet werden soll)
	 * @param variableNames	Systemweite Variablennamen (zum Parsen der Prioritätsformel)
	 */
	public RunElementTransportTransporterSourceData(final RunElement station, final String priorityWaitingString, final String priorityRequestingString, final String[] priorityClientString, final String[] routingExpresionStrings, final String[] variableNames) {
		super(station);

		count=0;
		moving=0;
		queue=new ArrayList<>();

		priorityParking=new ExpressionCalc(variableNames);
		priorityParking.parse(priorityWaitingString);

		priorityRequest=new ExpressionCalc(variableNames);
		priorityRequest.parse(priorityRequestingString);

		boolean allFCFS=true;
		priorityClient=new ExpressionCalc[priorityClientString.length];
		for (int i=0;i<priorityClient.length;i++) {
			if (priorityClientString[i]==null) {
				priorityClient[i]=null; /* Default Priorität */
			} else {
				priorityClient[i]=new ExpressionCalc(variableNames);
				priorityClient[i].parse(priorityClientString[i]);
				allFCFS=false;
			}
		}
		priorityClientAllFirstComeFirstServe=allFCFS;

		if (routingExpresionStrings!=null) {
			routingExpresions=new ExpressionEval[routingExpresionStrings.length];
			for (int i=0;i<routingExpresionStrings.length;i++) if (routingExpresionStrings[i]!=null) {
				routingExpresions[i]=new ExpressionEval(variableNames);
				routingExpresions[i].parse(routingExpresionStrings[i]);
			}
		} else {
			routingExpresions=null;
		}
	}

	/**
	 * Fügt einen Kunden zu der Liste der wartenden Kunden hinzu
	 * @param client	Hinzuzufügender Kunde
	 * @param simData	Simulationsdatenobjekt
	 */
	public void addClientToQueue(final RunDataClient client, final SimulationData simData) {
		/* Kunden an Warteschlange anstellen */
		queue.add(client);
		client.lastWaitingStart=simData.currentTime;

		/* Logging */
		if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Creator.SeizeArrival"),String.format(Language.tr("Simulation.Creator.SeizeArrival.Info"),client.logInfo(simData),station.name));

		/* Statistik */
		simData.runData.logClientEntersStationQueue(simData,station,this,client);
	}

	/**
	 * Entfernt einen Kunden aus der Warteschlange
	 * @param client	Zu entfernender Kunde
	 * @param simData	Simulationsdatenobjekt
	 */
	public void removeClientFromQueue(final RunDataClient client, final SimulationData simData) {
		/* Kunden aus Warteschlange entfernen */
		queue.remove(client);

		/* Statistik */
		simData.runData.logClientLeavesStationQueue(simData,station,this,client);
	}

	/**
	 * Entfernt einen Kunden aus der Warteschlange
	 * @param clientIndex	Index des zu entfernenden Kunden in der Warteschlange
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt das Kundenobjekt zurück
	 */
	public RunDataClient removeClientFromQueue(final int clientIndex, final SimulationData simData) {
		/* Kunden aus Warteschlange entfernen */
		final RunDataClient client=queue.remove(clientIndex);

		/* Statistik */
		simData.runData.logClientLeavesStationQueue(simData,station,this,client);

		return client;
	}
}