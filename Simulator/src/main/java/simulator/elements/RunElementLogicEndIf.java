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
import ui.modeleditor.elements.ModelElementLogicEndIf;
import ui.modeleditor.elements.ModelElementSub;

/**
 * �quivalent zu <code>ModelElementLogicEndIf</code>
 * @author Alexander Herzog
 * @see ModelElementLogicEndIf
 */
public class RunElementLogicEndIf extends RunElementLogic {
	/**
	 * Konstruktor der Klasse
	 * @param element	Zugeh�riges Editor-Element
	 */
	public RunElementLogicEndIf(final ModelElementLogic element) {
		super(element,Language.tr("Simulation.Element.LogicEndIf.Name"));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementLogicEndIf)) return null;
		return super.build(editModel,runModel,element,parent,testOnly);
	}

	@Override
	protected String buildConnection(final EditModel editModel, final RunModel runModel, final ModelElementLogic element, final ModelElementSub parent) {
		/* EndIf - keine Verkn�pfungen n�tig, einfach weiterleiten. */
		return null;
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.LogicEndIf"),String.format(Language.tr("Simulation.Log.LogicEndIf.Info"),client.logInfo(simData)));

		client.leaveLogic();
		processLeaveIntern(simData,client,null);
	}
}