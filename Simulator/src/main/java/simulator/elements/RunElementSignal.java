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
import mathtools.TimeTools;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.FireSignalDelayed;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSignal;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSignal</code>
 * @author Alexander Herzog
 * @see ModelElementSignal
 */
public class RunElementSignal extends RunElementPassThrough {
	/** Name des auszulösenden Signals */
	private String signalName;

	/** Optionale verzögerte Auslösung des Signals (in MS) */
	private long signalDelayMS;

	/**
	 * Optionale zusätzliche Bedingung, die für eine Signalauslösung erfüllt sein muss (kann <code>null</code> sein)
	 */
	private String condition;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSignal(final ModelElementSignal element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Signal.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSignal)) return null;
		final ModelElementSignal signalElement=(ModelElementSignal)element;
		final RunElementSignal signal=new RunElementSignal(signalElement);

		/* Auslaufende Kante */
		final String edgeError=signal.buildEdgeOut(signalElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (element.getName().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoName"),element.getId());
		signal.signalName=element.getName();

		/* Optionale verzögerte Auslösung des Signals */
		signal.signalDelayMS=Math.round(signalElement.getSignalDelay()*runModel.scaleToSimTime);

		/* Optionale Bedingung */
		final String condition=signalElement.getCondition();
		if (condition==null || condition.isBlank()) {
			signal.condition=null;
		} else {
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames,runModel.modelUserFunctions);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.SignalCondition"),condition,element.getId(),error+1);
			signal.condition=condition;
		}

		return signal;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementSignal)) return null;
		final ModelElementSignal signalElement=(ModelElementSignal)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(signalElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (element.getName().isEmpty()) return RunModelCreatorStatus.noName(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementSignalData getData(final SimulationData simData) {
		RunElementSignalData data;
		data=(RunElementSignalData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementSignalData(this,condition,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Signal"),String.format(Language.tr("Simulation.Log.Signal.Info"),client.logInfo(simData),name,signalName));

		final RunElementSignalData data=getData(simData);

		boolean fireSignal=true;
		if (condition!=null) {
			simData.runData.setClientVariableValues(client);
			if (!data.condition.eval(simData.runData.variableValues,simData,client)) fireSignal=false;
		}

		if (fireSignal) {
			if (signalDelayMS>0) {
				/* Logging */
				log(simData,Language.tr("Simulation.Log.Signal"),String.format(Language.tr("Simulation.Log.Signal.InfoDelay1"),TimeTools.formatLongTime(signalDelayMS*simData.runModel.scaleToSeconds)));

				/* Ereignis zur verzögerten Signalauslösung anlegen */
				final FireSignalDelayed event=(FireSignalDelayed)simData.getEvent(FireSignalDelayed.class);
				event.init(simData.currentTime+signalDelayMS);
				event.signalStation=this;
				event.signalName=signalName;
				if (!simData.runData.stopp) simData.eventManager.addEvent(event);
			} else {
				/* Signal direkt auslösen */
				simData.runData.fireSignal(simData,signalName);
			}
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}