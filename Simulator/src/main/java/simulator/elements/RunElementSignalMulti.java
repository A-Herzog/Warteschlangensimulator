/**
 * Copyright 2026 Alexander Herzog
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

import java.util.List;

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
import ui.modeleditor.elements.ModelElementSignalMulti;
import ui.modeleditor.elements.ModelElementSignalMultiRecord;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementSignalMulti}
 * @author Alexander Herzog
 * @see ModelElementSignalMulti
 */
public class RunElementSignalMulti extends RunElementPassThrough {
	/** Namen der auszulösenden Signale */
	private String[] signalNames;

	/** Optionale verzögerte Auslösung der Signale (in MS) */
	private long[] signalDelayMS;

	/**
	 * Optionale zusätzliche Bedingungen, die für eine Signalauslösung erfüllt müssen muss (Einträge können <code>null</code> sein)
	 */
	private String[] conditions;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSignalMulti(final ModelElementSignalMulti element) {
		super(element,buildName(element,Language.tr("Simulation.Element.SignalMulti.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSignalMulti)) return null;
		final ModelElementSignalMulti signalElement=(ModelElementSignalMulti)element;
		final RunElementSignalMulti signal=new RunElementSignalMulti(signalElement);

		/* Auslaufende Kante */
		final String edgeError=signal.buildEdgeOut(signalElement);
		if (edgeError!=null) return edgeError;

		final List<ModelElementSignalMultiRecord> records=signalElement.getRecords();
		signal.signalNames=new String[records.size()];
		signal.signalDelayMS=new long[records.size()];
		signal.conditions=new String[records.size()];
		for (int i=0;i<records.size();i++) {
			final var record=records.get(i);

			/* Name */
			if (record.getName().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoSignalName"),element.getId());
			signal.signalNames[i]=record.getName();

			/* Optionale verzögerte Auslösung des Signals */
			signal.signalDelayMS[i]=Math.round(record.getSignalDelay()*runModel.scaleToSimTime);

			/* Optionale Bedingung */
			final String condition=record.getCondition();
			if (condition==null || condition.isBlank()) {
				signal.conditions[i]=null;
			} else {
				final int error=ExpressionMultiEval.check(condition,runModel.variableNames,runModel.modelUserFunctions);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.SignalCondition"),condition,element.getId(),error+1);
				signal.conditions[i]=condition;
			}
		}

		return signal;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementSignalMulti)) return null;
		final ModelElementSignalMulti signalElement=(ModelElementSignalMulti)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(signalElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		if (signalElement.getRecords().stream().map(ModelElementSignalMultiRecord::getName).filter(String::isBlank).findFirst().isPresent()) return RunModelCreatorStatus.noSignalName(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementSignalMultiData getData(final SimulationData simData) {
		RunElementSignalMultiData data;
		data=(RunElementSignalMultiData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementSignalMultiData(this,conditions,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementSignalMultiData data=getData(simData);

		for (int i=0;i<signalNames.length;i++) {
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Signal"),String.format(Language.tr("Simulation.Log.Signal.Info"),client.logInfo(simData),name,signalNames[i]));

			boolean fireSignal=true;
			if (conditions[i]!=null) {
				simData.runData.setClientVariableValues(client);
				if (!data.conditions[i].eval(simData.runData.variableValues,simData,client)) fireSignal=false;
			}

			if (fireSignal) {
				if (signalDelayMS[i]>0) {
					/* Logging */
					log(simData,Language.tr("Simulation.Log.Signal"),String.format(Language.tr("Simulation.Log.Signal.InfoDelay1"),TimeTools.formatLongTime(signalDelayMS[i]*simData.runModel.scaleToSeconds)));

					/* Ereignis zur verzögerten Signalauslösung anlegen */
					final FireSignalDelayed event=(FireSignalDelayed)simData.getEvent(FireSignalDelayed.class);
					event.init(simData.currentTime+signalDelayMS[i]);
					event.signalStation=this;
					event.signalName=signalNames[i];
					if (!simData.runData.stopp) simData.eventManager.addEvent(event);
				} else {
					/* Signal direkt auslösen */
					simData.runData.fireSignal(simData,signalNames[i]);
				}
			}
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}