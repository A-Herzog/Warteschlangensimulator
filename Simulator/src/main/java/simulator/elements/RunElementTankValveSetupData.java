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

import simulator.coreelements.RunElementData;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.elements.ModelElementTankValveSetup;

/**
 * Laufzeitdaten eines <code>RunElementTankValveSetup</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementTankValveSetup
 * @see RunElementData
 */
public class RunElementTankValveSetupData extends RunElementData {
	/** Rechenausdr�cke zur Bestimmung der maximalen Durchfl�sse f�r die einzelnen Ventile */
	public final ExpressionCalc[] maxFlow;

	/**
	 * Konstruktor der Klasse
	 * @param station	Zugeh�riges RunElement
	 * @param valveSetups	Ventilkonfigurationen (aus denen die Formeln f�r die Berechnung des maximalen Durchflusses ausgelesen werden)
	 * @param variableNames	Liste der global verf�gbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementTankValveSetupData(final RunElementTankValveSetup station, final ModelElementTankValveSetup.ValveSetup[] valveSetups, final String[] variableNames, final SimulationData simData) {
		super(station,simData);

		maxFlow=new ExpressionCalc[valveSetups.length];
		for (int i=0;i<valveSetups.length;i++) {
			maxFlow[i]=new ExpressionCalc(variableNames,simData.runModel.modelUserFunctions);
			maxFlow[i].parse(valveSetups[i].maxFlow);
		}
	}
}