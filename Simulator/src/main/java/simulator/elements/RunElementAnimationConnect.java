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
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataTransporter;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementAnimationConnect;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementAnimationConnect</code>
 * @author Alexander Herzog
 * @see ModelElementAnimationConnect
 * @see RunModelAnimationViewer
 */
public class RunElementAnimationConnect extends RunElement implements StateChangeListener, ClientMoveListener, TransporterMoveListener {
	/**
	 * Über dieses Objekt benachrichtigt diese Pseudo-Stations die
	 * Animationsanzeige über Veränderungen des Systems.
	 * @see #systemStateChangeNotify(SimulationData)
	 * @see #clientMoveNotify(SimulationData, RunDataClient, boolean)
	 * @see #transporterMoveNotify(SimulationData, RunDataTransporter)
	 */
	private RunModelAnimationViewer animationViewer;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementAnimationConnect(final ModelElementAnimationConnect element) {
		super(element,buildName(element,Language.tr("Simulation.Element.AnimationConnect.Name")));
	}

	@Override
	public Object build(EditModel editModel, RunModel runModel, ModelElement element, ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementAnimationConnect) || parent!=null) return null;

		final RunElementAnimationConnect animationConnect=new RunElementAnimationConnect((ModelElementAnimationConnect)element);
		animationConnect.animationViewer=((ModelElementAnimationConnect)element).animationViewer;
		return animationConnect;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementAnimationConnect)) return null;
		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* wird nicht passieren */
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* wird nicht passieren */
	}

	@Override
	public boolean systemStateChangeNotify(SimulationData simData) {
		if (animationViewer!=null) {
			if (!animationViewer.updateViewer(simData)) animationViewer=null;
		}
		return false;
	}

	@Override
	public void clientMoveNotify(SimulationData simData, RunDataClient client, final boolean moveByTransport) {
		if (animationViewer!=null) {
			if (!animationViewer.updateViewer(simData,client,moveByTransport)) animationViewer=null;
		}
	}

	@Override
	public void transporterMoveNotify(SimulationData simData, RunDataTransporter transporter) {
		if (animationViewer!=null) {
			if (!animationViewer.updateViewer(simData,transporter)) animationViewer=null;
		}
	}
}
