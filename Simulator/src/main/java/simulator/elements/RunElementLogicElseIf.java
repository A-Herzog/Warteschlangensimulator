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
import ui.modeleditor.elements.ModelElementSub;

/**
 * �quivalent zu <code>ModelElementLogicIfElse</code>
 * @author Alexander Herzog
 * @see ModelElementLogicElseIf
 */
public class RunElementLogicElseIf extends RunElementLogic {
	/** ID des zugeh�rigen "Else"-, "ElseIf" oder "EndIf"-Elements */
	private int nextId;
	/** Zugeh�riges "Else"-, "ElseIf" oder "EndIf"-Element (�bersetzung aus {@link #nextId}) */
	private RunElement next;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugeh�riges Editor-Element
	 */
	public RunElementLogicElseIf(final ModelElementLogic element) {
		super(element,Language.tr("Simulation.Element.LogicElseIf.Name"));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementLogicElseIf)) return null;
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
		if (!client.testLogic()) {
			/* ElseIf-Zweig verarbeiten */

			if (checkCondition(simData,client)) {
				/* Bedingung erf�llt, ElseIf-Zweig ausf�hren */

				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicElseIf"),String.format(Language.tr("Simulation.Log.LogicElseIf.ConditionTrue"),client.logInfo(simData)));

				client.enterLogic(true);
				processLeaveIntern(simData,client,null);
			} else {
				/* Bedingung nicht erf�llt, ElseIf-Zweig �berspringen */

				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicElseIf"),String.format(Language.tr("Simulation.Log.LogicElseIf.ConditionFalse"),client.logInfo(simData),next.id));

				client.enterLogic(false);
				processLeaveIntern(simData,client,next);
			}
		} else {
			/* If-Zweig wurde schon ausgef�hrt, ElseIf-Zweig �berspringen */

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicElseIf"),String.format(Language.tr("Simulation.Log.LogicElseIf.SkipElseIf"),client.logInfo(simData),next.id));

			processLeaveIntern(simData,client,next);
		}
	}
}