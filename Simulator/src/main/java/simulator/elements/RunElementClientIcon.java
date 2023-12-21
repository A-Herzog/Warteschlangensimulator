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
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementClientIcon;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementClientIcon</code>
 * @author Alexander Herzog
 * @see ModelElementClientIcon
 */
public class RunElementClientIcon extends RunElementPassThrough {
	/**
	 * Zuzuweisendes Icon für die Kunden
	 */
	private String icon;

	/**
	 * Optionale zusätzliche Bedingung, die für eine Zuweisung erfüllt sein muss (kann <code>null</code> sein)
	 */
	private String condition;

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

		/* Optionale Bedingung */
		final String condition=assignIconElement.getCondition();
		if (condition==null || condition.trim().isEmpty()) {
			assignIcon.condition=null;
		} else {
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.Icon.Condition"),condition,element.getId(),error+1);
			assignIcon.condition=condition;
		}

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
	public RunElementClientIconData getData(final SimulationData simData) {
		RunElementClientIconData data;
		data=(RunElementClientIconData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementClientIconData(this,condition,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Führt die eigentliche Icon-Zuweisung durch.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde
	 */
	private void applyClientIcon(final SimulationData simData, final RunDataClient client) {
		/* Icon zuweisen */
		client.iconLast=client.icon;
		client.icon=icon;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Icon"),String.format(Language.tr("Simulation.Log.Icon.Info"),client.logInfo(simData),((client.icon==null)?Language.tr("Simulation.Log.Icon.Default"):client.icon),icon,name));
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		final RunElementClientIconData data=getData(simData);

		if (condition!=null) {
			simData.runData.setClientVariableValues(client);
			if (data.condition.eval(simData.runData.variableValues,simData,client)) applyClientIcon(simData,client);
		} else {
			applyClientIcon(simData,client);
		}

		super.processLeave(simData,client);
	}
}
