/**
 * Copyright 2022 Alexander Herzog
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
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementAnimationPause;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementAnimationPause}
 * @author Alexander Herzog
 * @see ModelElementAnimationPause
 */
public class RunElementAnimationPause extends RunElementPassThrough {
	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementAnimationPause(final ModelElementAnimationPause element) {
		super(element,buildName(element,Language.tr("Simulation.Element.AnimationPause.Name")));
	}

	@Override
	public Object build(EditModel editModel, RunModel runModel, ModelElement element, ModelElementSub parent, boolean testOnly) {
		if (!(element instanceof ModelElementAnimationPause)) return null;
		final ModelElementAnimationPause pauseElement=(ModelElementAnimationPause)element;
		final RunElementAnimationPause pause=new RunElementAnimationPause(pauseElement);

		/* Auslaufende Kanten */
		final String edgeError=pause.buildEdgeOut(pauseElement);
		if (edgeError!=null) return edgeError;

		return pause;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementAnimationPause)) return null;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Animation bei Kundenankunft pausieren */
		if (simData.runModel.isAnimation) {
			simData.runModel.animationConnect.animationViewer.pauseAnimation();
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
