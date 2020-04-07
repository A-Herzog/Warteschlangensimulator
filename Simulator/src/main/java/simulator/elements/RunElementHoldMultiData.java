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

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;

/**
 * Laufzeitdaten eines <code>RunElementHoldMulti</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementHoldMulti
 * @see RunElementData
 */
public class RunElementHoldMultiData extends RunElementData {
	/**
	 * Zu prüfende Bedingungen
	 */
	public ExpressionMultiEval[] conditions;

	/**
	 * Liste der wartenden Kunden
	 */
	public List<RunDataClient> waitingClients;

	/**
	 * Speichert, wann zu letzten Mal ein Kunde die Station verlassen hat (um nicht zum selben Zeitschritt mehrfach die Bedingung zu prüfen)
	 */
	public long lastRelease;

	/**
	 * Wenn die Warteschlange gerade bearbeitet wird, dürfen per
	 * {@link RunElementHoldMulti#getClient(simulator.runmodel.SimulationData)}
	 * keine Kunden entnommen werden. Dieser Status kann hier eingestellt werden.
	 * @see RunElementHoldMulti#processArrival(simulator.runmodel.SimulationData, RunDataClient)
	 * @see RunElementHoldMulti#systemStateChangeNotify(SimulationData)
	 */
	public boolean queueLockedForPickUp;

	/**
	 * Konstruktor der Klasse <code>RunElementHoldMultiData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param conditions	Bei der Verzögerung von Kunden zu prüfende Bedingungen (zur Umsetzung in ein <code>ExpressionMultiEval</code>-Objekt)
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 */
	public RunElementHoldMultiData(final RunElement station, final String[] conditions, final String[] variableNames) {
		super(station);

		this.conditions=new ExpressionMultiEval[conditions.length];
		for (int i=0;i<conditions.length;i++) {
			ExpressionMultiEval eval=new ExpressionMultiEval(variableNames);
			eval.parse(conditions[i]);
			this.conditions[i]=eval;
		}

		waitingClients=new ArrayList<>();

		lastRelease=-Long.MAX_VALUE;

		queueLockedForPickUp=false;
	}
}
