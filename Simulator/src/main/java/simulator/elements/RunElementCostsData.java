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
import simulator.simparser.ExpressionCalc;

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
	 * Konstruktor der Klasse <code>RunElementHoldData</code>
	 * @param station Station zu diesem Datenelement
	 * @param stationCosts	Allgemeine Kosten an der Station (erfasst bei der Station)
	 * @param clientWaitingCosts	Wartezeit-Kosten (bei den Kunden)
	 * @param clientTransferCosts	Transferzeit-Kosten (bei den Kunden)
	 * @param clientProcessCosts	Bedienzeit-Kosten (bei den Kunden)
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 */
	public RunElementCostsData(final RunElement station, final String stationCosts, final String clientWaitingCosts, final String clientTransferCosts, final String clientProcessCosts, final String[] variableNames) {
		super(station);

		if (stationCosts==null || stationCosts.trim().isEmpty()) {
			this.stationCosts=null;
		} else {
			this.stationCosts=new ExpressionCalc(variableNames);
			this.stationCosts.parse(stationCosts);
		}

		if (clientWaitingCosts==null || clientWaitingCosts.trim().isEmpty()) {
			this.clientWaitingCosts=null;
		} else {
			this.clientWaitingCosts=new ExpressionCalc(variableNames);
			this.clientWaitingCosts.parse(clientWaitingCosts);
		}

		if (clientTransferCosts==null || clientTransferCosts.trim().isEmpty()) {
			this.clientTransferCosts=null;
		} else {
			this.clientTransferCosts=new ExpressionCalc(variableNames);
			this.clientTransferCosts.parse(clientTransferCosts);
		}

		if (clientProcessCosts==null || clientProcessCosts.trim().isEmpty()) {
			this.clientProcessCosts=null;
		} else {
			this.clientProcessCosts=new ExpressionCalc(variableNames);
			this.clientProcessCosts.parse(clientProcessCosts);
		}
	}
}
