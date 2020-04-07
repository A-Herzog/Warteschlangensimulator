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
 * Laufzeitdaten eines <code>RunElementHold</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementHold
 * @see RunElementData
 */
public class RunElementHoldData extends RunElementData {
	/**
	 * Zu prüfende Bedingung
	 */
	public ExpressionMultiEval condition;

	/**
	 * Liste der wartenden Kunden
	 */
	public List<RunDataClient> waitingClients;

	/**
	 * Speichert, wann zu letzten Mal ein Kunde die Station verlassen hat (um nicht zum selben Zeitschritt mehrfach die Bedingung zu prüfen)
	 */
	public long lastRelease;

	/**
	 * Warteschlange für Zugriff durch PickUp (bzw. {@link RunElementHold#getClient(SimulationData)}) sperren.
	 */
	public boolean queueLockedForPickUp;

	/**
	 * Konstruktor der Klasse <code>RunElementHoldData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param condition	Bei der Verzögerung von Kunden zu prüfende Bedingung (zur Umsetzung in ein <code>ExpressionMultiEval</code>-Objekt)
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 */
	public RunElementHoldData(final RunElement station, final String condition, final String[] variableNames) {
		super(station);
		queueLockedForPickUp=false;
		waitingClients=new ArrayList<>();
		lastRelease=-Long.MAX_VALUE;
		if (condition==null || condition.trim().isEmpty()) {
			this.condition=null;
		} else {
			this.condition=new ExpressionMultiEval(variableNames);
			this.condition.parse(condition);
		}
	}
}
