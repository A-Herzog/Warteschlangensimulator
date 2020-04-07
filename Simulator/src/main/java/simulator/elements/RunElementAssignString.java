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

import java.util.ArrayList;
import java.util.List;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementAssignString;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementAssignString</code>
 * @author Alexander Herzog
 * @see ModelElementAssignString
 */
public class RunElementAssignString extends RunElementPassThrough {
	private String[] stringKeys;
	private String[] stringValues;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementAssignString(final ModelElementAssignString element) {
		super(element,buildName(element,Language.tr("Simulation.Element.AssignString.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementAssignString)) return null;
		final ModelElementAssignString assignElement=(ModelElementAssignString)element;
		final RunElementAssignString assign=new RunElementAssignString(assignElement);

		/* Auslaufende Kanten */
		final String edgeError=assign.buildEdgeOut(assignElement);
		if (edgeError!=null) return edgeError;

		/* Schlüssel und Wert */
		final List<String> keys=new ArrayList<>();
		final List<String> values=new ArrayList<>();
		for (int i=0;i<Math.min(assignElement.getKeys().size(),assignElement.getValues().size());i++) {
			final String key=assignElement.getKeys().get(i);
			final String value=assignElement.getValues().get(i);
			if (key==null || key.isEmpty()) return String.format(Language.tr("Simulation.Creator.NoAssignKey"),element.getId());
			keys.add(key);
			values.add(value);
		}
		assign.stringKeys=keys.toArray(new String[0]);
		assign.stringValues=values.toArray(new String[0]);

		return assign;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementAssignString)) return null;
		final ModelElementAssignString assignElement=(ModelElementAssignString)element;

		/* Auslaufende Kanten */
		final RunModelCreatorStatus edgeError=testEdgeOut(assignElement);
		if (edgeError!=null) return edgeError;

		/* Schlüssel */
		for (int i=0;i<Math.min(assignElement.getKeys().size(),assignElement.getValues().size());i++) {
			final String key=assignElement.getKeys().get(i);
			if (key==null || key.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoAssignKey"),element.getId()));

		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) {
			for (int i=0;i<stringKeys.length;i++) {
				log(simData,Language.tr("Simulation.Log.AssignString"),String.format(Language.tr("Simulation.Log.AssignString.Info"),client.hashCode(),stringKeys[i],stringValues[i],name));
			}
		}

		/* Kundentexte ändern */
		for (int i=0;i<stringKeys.length;i++) client.setUserDataString(stringKeys[i],stringValues[i]);

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}