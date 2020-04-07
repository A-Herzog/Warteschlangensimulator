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
import ui.modeleditor.elements.ModelElementSetStatisticsMode;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSetStatisticsMode</code>
 * @author Alexander Herzog
 * @see ModelElementSetStatisticsMode
 */
public class RunElementSetStatisticsMode extends RunElementPassThrough {
	private ModelElementSetStatisticsMode.Mode mode;
	private String condition;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSetStatisticsMode(final ModelElementSetStatisticsMode element) {
		super(element,buildName(element,Language.tr("Simulation.Element.SetStatisticsMode.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSetStatisticsMode)) return null;
		final ModelElementSetStatisticsMode setElement=(ModelElementSetStatisticsMode)element;
		final RunElementSetStatisticsMode set=new RunElementSetStatisticsMode((ModelElementSetStatisticsMode)element);

		/* Auslaufende Kante */
		final String edgeError=set.buildEdgeOut(setElement);
		if (edgeError!=null) return edgeError;

		set.mode=setElement.getMode();
		if (set.mode==ModelElementSetStatisticsMode.Mode.CONDITION) {
			final String condition=setElement.getCondition();
			if (condition==null || condition.trim().isEmpty()) {
				return String.format(Language.tr("Simulation.Creator.SetStatisticsModeCondition.Missing"),element.getId());
			} else {
				final int error=ExpressionMultiEval.check(condition,runModel.variableNames);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.SetStatisticsModeCondition.Invalid"),condition,element.getId(),error+1);
				set.condition=condition;
			}
		} else {
			set.condition=null;
		}

		return set;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementSetStatisticsMode)) return null;
		final ModelElementSetStatisticsMode setElement=(ModelElementSetStatisticsMode)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(setElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementSetStatisticsModeData getData(final SimulationData simData) {
		RunElementSetStatisticsModeData data;
		data=(RunElementSetStatisticsModeData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementSetStatisticsModeData(this,condition,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		boolean newStatisticMode=true;

		switch (mode) {
		case ON:
			newStatisticMode=true;
			break;
		case OFF:
			newStatisticMode=false;
			break;
		case CONDITION:
			final RunElementSetStatisticsModeData data=getData(simData);
			simData.runData.setClientVariableValues(client);
			newStatisticMode=data.condition.eval(simData.runData.variableValues,simData,client);
			break;
		}

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.SetStatisticsMode"),String.format((newStatisticMode?Language.tr("Simulation.Log.SetStatisticsMode.On"):Language.tr("Simulation.Log.SetStatisticsMode.Off")),client.logInfo(simData),name));

		/* Statistik für Kunden ein- oder ausschalten */
		client.inStatistics=newStatisticMode;

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
