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

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;

/**
 * Laufzeitdaten eines <code>RunElementCosts</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementCosts
 * @see RunElementData
 */
public class RunElementCostsData extends RunElementData {
	/**
	 * Kosten an der Station
	 */
	public ExpressionCalc stationCosts;

	/**
	 * Wartezeit-Kosten (bei den Kunden)
	 */
	public ExpressionCalc clientWaitingCosts;

	/**
	 * Transferzeit-Kosten (bei den Kunden)
	 */
	public ExpressionCalc clientTransferCosts;

	/**
	 * Bedienzeit-Kosten (bei den Kunden)
	 */
	public ExpressionCalc clientProcessCosts;

	/**
	 * Zu pr�fende Bedingung (kann <code>null</code> sein)
	 */
	public ExpressionMultiEval condition;

	/**
	 * Konstruktor der Klasse
	 * @param station Station zu diesem Datenelement
	 * @param stationCosts	Allgemeine Kosten an der Station (erfasst bei der Station)
	 * @param clientWaitingCosts	Wartezeit-Kosten (bei den Kunden)
	 * @param clientTransferCosts	Transferzeit-Kosten (bei den Kunden)
	 * @param clientProcessCosts	Bedienzeit-Kosten (bei den Kunden)
	 * @param condition	Optionale zus�tzliche Bedingung, die f�r eine Zuweisung erf�llt sein muss (kann <code>null</code> sein)
	 * @param variableNames	Liste der global verf�gbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementCostsData(final RunElement station, final String stationCosts, final String clientWaitingCosts, final String clientTransferCosts, final String clientProcessCosts, final String condition, final String[] variableNames, final SimulationData simData) {
		super(station,simData);

		if (stationCosts==null || stationCosts.isBlank()) {
			this.stationCosts=null;
		} else {
			this.stationCosts=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			this.stationCosts.parse(stationCosts);
		}

		if (clientWaitingCosts==null || clientWaitingCosts.isBlank()) {
			this.clientWaitingCosts=null;
		} else {
			this.clientWaitingCosts=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			this.clientWaitingCosts.parse(clientWaitingCosts);
		}

		if (clientTransferCosts==null || clientTransferCosts.isBlank()) {
			this.clientTransferCosts=null;
		} else {
			this.clientTransferCosts=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			this.clientTransferCosts.parse(clientTransferCosts);
		}

		if (clientProcessCosts==null || clientProcessCosts.isBlank()) {
			this.clientProcessCosts=null;
		} else {
			this.clientProcessCosts=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			this.clientProcessCosts.parse(clientProcessCosts);
		}

		if (condition==null || condition.isBlank()) {
			this.condition=null;
		} else {
			this.condition=new ExpressionMultiEval(variableNames,simData.runModel.modelUserFunctions);
			this.condition.parse(condition);
		}
	}
}
