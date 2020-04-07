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
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementClientIcon;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementClientIcon</code>
 * @author Alexander Herzog
 * @see ModelElementClientIcon
 */
public class RunElementClientIcon extends RunElementPassThrough {
	private String icon;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementClientIcon(final ModelElementClientIcon element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Icon.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementClientIcon)) return null;
		final ModelElementClientIcon assignIconElement=(ModelElementClientIcon)element;
		final RunElementClientIcon assignIcon=new RunElementClientIcon(assignIconElement);

		/* Auslaufende Kante */
		final String edgeError=assignIcon.buildEdgeOut(assignIconElement);
		if (edgeError!=null) return edgeError;

		/* Icon */
		final String icon=assignIconElement.getIcon();
		if (icon==null || icon.isEmpty()) return String.format(Language.tr("Simulation.Creator.NoIconName"),element.getId());
		assignIcon.icon=icon;

		return assignIcon;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementClientIcon)) return null;
		final ModelElementClientIcon assignIconElement=(ModelElementClientIcon)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(assignIconElement);
		if (edgeError!=null) return edgeError;

		/* Icon */
		final String icon=assignIconElement.getIcon();
		if (icon==null || icon.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoIconName"),element.getId()),RunModelCreatorStatus.Status.NO_ICON);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Kundentyp ändern */
		client.iconLast=client.icon;
		client.icon=icon;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Icon"),String.format(Language.tr("Simulation.Log.Icon.Info"),client.logInfo(simData),((client.icon==null)?Language.tr("Simulation.Log.Icon.Default"):client.icon),icon,name));

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
