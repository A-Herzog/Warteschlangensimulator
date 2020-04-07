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

import language.Language;
import simulator.coreelements.RunElementLogic;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementLogic;
import ui.modeleditor.elements.ModelElementLogicEndWhile;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementLogicEndWhile</code>
 * @author Alexander Herzog
 * @see ModelElementLogicEndWhile
 */
public class RunElementLogicEndWhile extends RunElementLogic {
	/**
	 * Zugehöriges "While"-Element
	 */
	public RunElementLogicWhile whileElement;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementLogicEndWhile(final ModelElementLogic element) {
		super(element,Language.tr("Simulation.Element.LogicEndWhile.Name"));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementLogicEndWhile)) return null;
		return super.build(editModel,runModel,element,parent,testOnly);
	}

	@Override
	protected String buildConnection(final EditModel editModel, final RunModel runModel, final ModelElementLogic element, final ModelElementSub parent) {
		/* EndWhile - keine Verknüpfungen nötig, einfach weiterleiten. */
		return null;
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		if (whileElement==null) {
			/* EndWhile ohne zugehöriges While-Element; Kunden einfach weiterleiten. */
			processLeaveIntern(simData,client,null);
			return;
		}

		if (!client.testLogic()) {
			/* Schleifenbedingung war erfüllt, zurück nach vorne */

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicEndWhile"),String.format(Language.tr("Simulation.Log.LogicEndWhile.Loop"),client.logInfo(simData),whileElement.id));

			client.leaveLogic(); /* While erstellt einen neuen Eintrag, daher den aktuellen entfernen */
			processLeaveIntern(simData,client,whileElement);
		} else {
			/* Schleifenbedingung nicht erfüllt, einfach weiterleiten */

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicEndWhile"),String.format(Language.tr("Simulation.Log.LogicEndWhile.EndLoop"),client.logInfo(simData)));

			client.leaveLogic();
			processLeaveIntern(simData,client,null);
		}
	}
}
