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
import ui.modeleditor.elements.ModelElementLogicUntil;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementLogicUntil</code>
 * @author Alexander Herzog
 * @see ModelElementLogicUntil
 */
public class RunElementLogicUntil extends RunElementLogic {
	/**
	 * Zugehöriges "Do"-Element
	 */
	public RunElementLogicDo doElement;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementLogicUntil(final ModelElementLogic element) {
		super(element,Language.tr("Simulation.Element.LogicUntil.Name"));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementLogicUntil)) return null;
		return super.build(editModel,runModel,element,parent,testOnly);
	}

	@Override
	protected String buildConnection(final EditModel editModel, final RunModel runModel, final ModelElementLogic element, final ModelElementSub parent) {
		/* Until - keine Verknüpfungen nötig, einfach weiterleiten. */
		return null;
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		if (!checkCondition(simData,client)) {
			/* "Until" noch nicht erreicht, Schleifenrumpf erneut ausführen */

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicUntil"),String.format(Language.tr("Simulation.Log.LogicUntil.ConditionTrue"),client.logInfo(simData),doElement.id));

			processLeaveIntern(simData,client,doElement);
		} else {
			/* "Until" erreicht, einfach weiter zum nächsten Element */

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicUntil"),String.format(Language.tr("Simulation.Log.LogicUntil.ConditionFalse"),client.logInfo(simData)));

			processLeaveIntern(simData,client,null);
		}
	}
}