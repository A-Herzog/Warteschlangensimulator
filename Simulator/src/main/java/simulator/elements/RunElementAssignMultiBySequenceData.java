/**
 * Copyright 2025 Alexander Herzog
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

/**
 * Laufzeitdaten eines {@link RunElementAssignMultiBySequence}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementAssignMultiBySequence
 * @see RunElementData
 */
public class RunElementAssignMultiBySequenceData extends RunElementAssignMultiBaseData {
	/** 0-basierte Nummer des als nächstes zu wählenden Ausgangspfades */
	public int nextNr;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param condition	Optionale zusätzliche Bedingung, die für eine Zuweisung erfüllt sein muss (kann <code>null</code> sein)	 *
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementAssignMultiBySequenceData(final RunElement station, final String condition, final String[] variableNames, final SimulationData simData) {
		super(station,condition,variableNames,simData);
		nextNr=0;
	}
}
