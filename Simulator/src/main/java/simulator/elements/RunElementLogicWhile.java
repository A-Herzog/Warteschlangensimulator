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
import ui.modeleditor.elements.ModelElementLogicWhile;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementLogicWhile</code>
 * @author Alexander Herzog
 * @see ModelElementLogicWhile
 */
public class RunElementLogicWhile extends RunElementLogic {
	private int nextId;
	private RunElementLogicEndWhile next;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementLogicWhile(final ModelElementLogic element) {
		super(element,Language.tr("Simulation.Element.LogicWhile.Name"));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementLogicWhile)) return null;
		return super.build(editModel,runModel,element,parent,testOnly);
	}

	@Override
	protected String buildConnection(final EditModel editModel, final RunModel runModel, final ModelElementLogic element, final ModelElementSub parent) {
		nextId=findNextId(element,new Class<?>[]{ModelElementLogicEndWhile.class});
		if (nextId<0) return String.format(Language.tr("Simulation.Creator.LogicNoEnd"),id);

		return null;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		super.prepareRun(runModel); /* Normale Verbindung in Elternklassen-Funktion setzen. */
		next=(RunElementLogicEndWhile)runModel.elements.get(nextId);
		next.whileElement=this;
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		if (checkCondition(simData,client)) {
			/* Bedingung erfüllt, Schleifenrumpf ausführen */

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicWhile"),String.format(Language.tr("Simulation.Log.LogicWhile.ConditionTrue"),client.logInfo(simData)));

			client.enterLogic(false); /* false = Schleife noch nicht erledigt */
			processLeaveIntern(simData,client,null);
		} else {
			/* Bedingung nicht erfüllt, Schleifenrumpf überspringen */

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicWhile"),String.format(Language.tr("Simulation.Log.LogicWhile.ConditionFalse"),client.logInfo(simData),next.id));

			client.enterLogic(true); /* true = Schleife erledigt */
			processLeaveIntern(simData,client,next);
		}
	}
}
