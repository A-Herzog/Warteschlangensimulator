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
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;

/**
 * Laufzeitdaten eines <code>RunElementHold</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementHold
 * @see RunElementData
 */
public class RunElementHoldData extends RunElementData implements RunElementDataWithWaitingClients {
	/**
	 * Zu pr�fende Bedingung
	 */
	public ExpressionMultiEval condition;

	/**
	 * Rechenausdr�cke f�r die Kundenpriorit�ten an der Bedienstation
	 * (einzelne Eintr�ge k�nnen <code>null</code> sein; f�r diese soll dann "w" gelten)
	 */
	public final ExpressionCalc[] priority;

	/**
	 * Gilt f�r alle Kundentypen FIFO?
	 * @see #priority
	 */
	public final boolean allPriorityFIFO;

	/**
	 * Liste der wartenden Kunden
	 */
	public List<RunDataClient> waitingClients;

	/**
	 * Speichert, wann zu letzten Mal ein Kunde die Station verlassen hat (um nicht zum selben Zeitschritt mehrfach die Bedingung zu pr�fen)
	 */
	public long lastRelease;

	/**
	 * Warteschlange f�r Zugriff durch PickUp (bzw. {@link RunElementHold#getClient(SimulationData)}) sperren.
	 */
	public boolean queueLockedForPickUp;

	/**
	 * Rechenausdruck zur Bestimmung der maximalen Wartezeit bzw. der Zeit nach der eine automatische Freigabe erfolgt
	 */
	public final ExpressionCalc maxWaitingTime;

	/**
	 * Konstruktor der Klasse <code>RunElementHoldData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param condition	Bei der Verz�gerung von Kunden zu pr�fende Bedingung (zur Umsetzung in ein <code>ExpressionMultiEval</code>-Objekt)
	 * @param priority	Priorit�ts-Rechenausdr�cke
	 * @param maxWaitingTime	Rechenausdruck zur Bestimmung der maximalen Wartezeit bzw. der Zeit nach der eine automatische Freigabe erfolgt
	 * @param variableNames	Liste der global verf�gbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementHoldData(final RunElement station, final String condition, final String[] priority, final String maxWaitingTime, final String[] variableNames, final SimulationData simData) {
		super(station,simData);
		queueLockedForPickUp=false;
		waitingClients=new ArrayList<>();
		lastRelease=-Long.MAX_VALUE;

		if (condition==null || condition.isBlank()) {
			this.condition=null;
		} else {
			this.condition=new ExpressionMultiEval(variableNames,simData.runModel.modelUserFunctions);
			this.condition.parse(condition);
		}

		boolean allPriorityFIFO=true;
		this.priority=new ExpressionCalc[priority.length];
		for (int i=0;i<priority.length;i++) {
			if (priority[i]!=null) { /* Wenn null, war Default Priorit�t gesetzt (="w"). Dann priority[i] auf Vorgabe null lassen. Dies wird von ModelElementProcess.startProcessing() entsprechend erkannt. */
				this.priority[i]=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
				this.priority[i].parse(priority[i]);
				allPriorityFIFO=false;
			}
		}
		this.allPriorityFIFO=allPriorityFIFO;

		if (maxWaitingTime==null || maxWaitingTime.isBlank()) {
			this.maxWaitingTime=null;
		} else {
			this.maxWaitingTime=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			this.maxWaitingTime.parse(maxWaitingTime);
		}
	}

	@Override
	public List<RunDataClient> getWaitingClients() {
		return waitingClients;
	}
}
