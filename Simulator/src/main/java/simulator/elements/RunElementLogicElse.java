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
import ui.modeleditor.elements.ModelElementLogicEndIf;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementLogicElse</code>
 * @author Alexander Herzog
 * @see ModelElementLogicElse
 */
public class RunElementLogicElse extends RunElementLogic {
	/** ID des zugehörigen "EndIf"-Elements */
	private int nextId;
	/** Zugehöriges "EndIf"-Element (Übersetzung aus {@link #nextId}) */
	private RunElement next;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementLogicElse(final ModelElementLogic element) {
		super(element,Language.tr("Simulation.Element.LogicElse.Name"));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementLogicElse)) return null;
		return super.build(editModel,runModel,element,parent,testOnly);
	}

	@Override
	protected String buildConnection(final EditModel editModel, final RunModel runModel, final ModelElementLogic element, final ModelElementSub parent) {
		nextId=findNextId(element,new Class<?>[]{ModelElementLogicEndIf.class});
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
			/* Else-Zweig verarbeiten */

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicElse"),String.format(Language.tr("Simulation.Log.LogicElse.ProcessElse"),client.logInfo(simData)));

			client.updateLogic();
			processLeaveIntern(simData,client,null);
		} else {
			/* If-Zweig wurde schon ausgeführt, Else-Zweig überspringen */

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicElse"),String.format(Language.tr("Simulation.Log.LogicElse.SkipElse"),client.logInfo(simData),next.id));

			processLeaveIntern(simData,client,next);
		}
	}
}