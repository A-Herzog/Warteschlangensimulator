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

import java.util.HashMap;
import java.util.Map;

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Laufzeitdaten eines <code>RunElementDelay</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementDelay
 * @see RunElementData
 */
public class RunElementDelayData extends RunElementData {
	/**
	 * Verzögerungsausdrucke
	 */
	public ExpressionCalc[] expression;

	/**
	 * Kosten pro Bedienung
	 */
	public final ExpressionCalc costs;

	/**
	 * Liste der Kunden an der Station (kann <code>null</code> sein)
	 */
	public final Map<RunDataClient,StationLeaveEvent> clientsList;

	/**
	 * Konstruktor der Klasse <code>RunElementDelayData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param expresionStrings	Ausdruck zur Verzögerung der Kunden (einzelne Einträge können <code>null</code> sein, wenn eine Verteilung verwendet werden soll)
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param costs	Kosten pro Bedienvorgang (kann <code>null</code> sein)
	 * @param hasClientsList	Soll eine Liste der Kunden an der Station geführt werden?
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementDelayData(final RunElement station, final String[] expresionStrings, final String[] variableNames, final String costs, final boolean hasClientsList, final SimulationData simData) {
		super(station,simData);

		/* Verzögerungsausdrucke */
		expression=new ExpressionCalc[expresionStrings.length];
		for (int i=0;i<expresionStrings.length;i++) if (expresionStrings[i]!=null) {
			expression[i]=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			expression[i].parse(expresionStrings[i]);
		}

		/* Kosten pro Bedienung */
		if (costs==null || costs.trim().isEmpty()) {
			this.costs=null;
		} else {
			this.costs=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			this.costs.parse(costs);
		}

		/* Liste der Kunden an der Station (kann <code>null</code> sein) */
		clientsList=hasClientsList?new HashMap<>():null;
	}
}
