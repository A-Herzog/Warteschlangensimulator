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
import ui.modeleditor.elements.ModelElementLogicDo;
import ui.modeleditor.elements.ModelElementLogicUntil;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementLogicDo</code>
 * @author Alexander Herzog
 * @see ModelElementLogicDo
 */
public class RunElementLogicDo extends RunElementLogic {
	/** ID des zugehörigen "Until"-Elements */
	private int nextId;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementLogicDo(final ModelElementLogic element) {
		super(element,Language.tr("Simulation.Element.LogicDo.Name"));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementLogicDo)) return null;
		return super.build(editModel,runModel,element,parent,testOnly);
	}

	@Override
	protected String buildConnection(final EditModel editModel, final RunModel runModel, final ModelElementLogic element, final ModelElementSub parent) {
		nextId=findNextId(element,new Class<?>[]{ModelElementLogicUntil.class});
		if (nextId<0) return String.format(Language.tr("Simulation.Creator.LogicNoEnd"),id);

		return null;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		super.prepareRun(runModel); /* Normale Verbindung in Elternklassen-Funktion setzen. */
		final RunElementLogicUntil next=(RunElementLogicUntil)runModel.elements.get(nextId);
		next.doElement=this;
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		processLeaveIntern(simData,client,null);
	}
}
