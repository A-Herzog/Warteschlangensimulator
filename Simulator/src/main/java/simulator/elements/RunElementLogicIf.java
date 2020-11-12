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
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementLogic;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementLogic;
import ui.modeleditor.elements.ModelElementLogicElse;
import ui.modeleditor.elements.ModelElementLogicElseIf;
import ui.modeleditor.elements.ModelElementLogicEndIf;
import ui.modeleditor.elements.ModelElementLogicIf;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementLogicIf</code>
 * @author Alexander Herzog
 * @see ModelElementLogicIf
 */
public class RunElementLogicIf extends RunElementLogic {
	/** ID des zugehörigen "Else"-, "ElseIf" oder "EndIf"-Elements */
	private int nextId;
	/** Zugehöriges "Else"-, "ElseIf" oder "EndIf"-Element (Übersetzung aus {@link #nextId}) */
	private RunElement next;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementLogicIf(final ModelElementLogic element) {
		super(element,Language.tr("Simulation.Element.LogicIf.Name"));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementLogicIf)) return null;
		return super.build(editModel,runModel,element,parent,testOnly);
	}

	@Override
	protected String buildConnection(final EditModel editModel, final RunModel runModel, final ModelElementLogic element, final ModelElementSub parent) {
		nextId=findNextId(element,new Class<?>[]{ModelElementLogicElse.class,ModelElementLogicElseIf.class,ModelElementLogicEndIf.class});
		if (nextId<0) return String.format(Language.tr("Simulation.Creator.LogicNoEnd"),id);

		return null;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		super.prepareRun(runModel); /* Normale Verbindung in Elternklassen-Funktion setzen. */
		next=runModel.elements.get(nextId);
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		if (checkCondition(simData,client)) {
			/* Bedingung erfüllt, If-Zweig ausführen */

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicIf"),String.format(Language.tr("Simulation.Log.LogicIf.ConditionTrue"),client.logInfo(simData)));

			client.enterLogic(true);
			processLeaveIntern(simData,client,null);
		} else {
			/* Bedingung nicht erfüllt, If-Zweig überspringen */

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicIf"),String.format(Language.tr("Simulation.Log.LogicIf.ConditionFalse"),client.logInfo(simData),next.id));

			client.enterLogic(false);
			processLeaveIntern(simData,client,next);
		}
	}
}