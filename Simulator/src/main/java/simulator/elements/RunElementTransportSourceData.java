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

import java.util.ArrayDeque;
import java.util.Deque;

import language.Language;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;

/**
 * Laufzeitdaten eines <code>RunElementTransportSource</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementTransportSource
 * @see RunElementData
 */
public class RunElementTransportSourceData extends RunElementData {
	/**
	 * Kunden die auf Transport warten
	 */
	public final Deque<RunDataClient> waitingClients;

	/**
	 * Verzögerungsausdrucke
	 */
	public final ExpressionCalc[] delayExpression;

	/**
	 * Routingziel-Ausdrücke
	 */
	public final ExpressionMultiEval[] routingExpresions;

	/**
	 * Priorität bei der Ressourcenzuweisung
	 */
	public final ExpressionCalc resourcePriority;

	/**
	 * Ausdruck zur Bestimmung der verzögerten Ressourcenfreigabe
	 */
	public final ExpressionCalc[] releaseDelayExpressions;

	/**
	 * Konstruktor der Klasse <code>RunElementTransportSourceData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param delayExpressionString	Ausdrücke zur Verzögerung der Kunden (können <code>null</code> sein, wenn eine Verteilung verwendet werden soll)
	 * @param routingExpresionStrings	Bedingungen zum Routing der Kunden (einzelne Einträge können <code>null</code> sein, wenn eine Verzweigung nach Kundentyp verwendet werden soll)
	 * @param resourcePriorityString	Priorität bei der Ressourcenzuordnung (darf nicht <code>null</code> sein)
	 * @param releaseDelayExpressionStrings	Ausdrücke zur Bestimmung der verzögerten Ressourcenfreigabe (können teilweise oder auch ganz <code>null</code> sein)
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 */
	public RunElementTransportSourceData(final RunElement station, final String[] delayExpressionString, final String[] routingExpresionStrings, final String resourcePriorityString, final String[] releaseDelayExpressionStrings, final String[] variableNames) {
		super(station);

		waitingClients=new ArrayDeque<>();

		delayExpression=new ExpressionCalc[delayExpressionString.length];
		for (int i=0;i<delayExpressionString.length;i++) {
			if (delayExpressionString[i]==null) continue;
			delayExpression[i]=new ExpressionCalc(variableNames);
			delayExpression[i].parse(delayExpressionString[i]);
		}

		if (routingExpresionStrings!=null) {
			routingExpresions=new ExpressionMultiEval[routingExpresionStrings.length];
			for (int i=0;i<routingExpresionStrings.length;i++) if (routingExpresionStrings[i]!=null) {
				routingExpresions[i]=new ExpressionMultiEval(variableNames);
				routingExpresions[i].parse(routingExpresionStrings[i]);
			}
		} else {
			routingExpresions=null;
		}

		resourcePriority=new ExpressionCalc(variableNames);
		resourcePriority.parse(resourcePriorityString);
		if (releaseDelayExpressionStrings!=null) {
			releaseDelayExpressions=new ExpressionCalc[releaseDelayExpressionStrings.length];
			for (int i=0;i<releaseDelayExpressionStrings.length;i++) if (releaseDelayExpressionStrings[i]!=null) {
				releaseDelayExpressions[i]=new ExpressionCalc(variableNames);
				releaseDelayExpressions[i].parse(releaseDelayExpressionStrings[i]);
			}
		} else {
			releaseDelayExpressions=null;
		}
	}

	/**
	 * Fügt einen Kunden zu der Liste der wartenden Kunden hinzu
	 * @param client	Hinzuzufügender Kunde
	 * @param time	Zeitpunkt an dem der Kunde an der Station eingetroffen ist (zur späteren Berechnung der Wartezeit der Kunden)
	 * @param simData	Simulationsdatenobjekt
	 */
	public void addClientToQueue(final RunDataClient client, final long time, final SimulationData simData) {
		/* Kunden an Warteschlange anstellen */
		waitingClients.offer(client);
		client.lastWaitingStart=time;

		/* Logging */
		if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Creator.SeizeArrival"),String.format(Language.tr("Simulation.Creator.SeizeArrival.Info"),client.logInfo(simData),station.name));

		/* Statistik */
		simData.runData.logClientEntersStationQueue(simData,station,this,client);
	}

	/**
	 * Entfernt einen Kunden aus der Warteschlange
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt das Kundenobjekt zurück
	 */
	public RunDataClient removeClientFromQueue(final SimulationData simData) {
		RunDataClient client=waitingClients.poll();
		if (client==null) return null;

		/* Statistik */
		simData.runData.logClientLeavesStationQueue(simData,station,this,client);

		return client;
	}
}