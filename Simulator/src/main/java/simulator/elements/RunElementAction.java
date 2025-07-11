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
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.TimedActionEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementAction;
import ui.modeleditor.elements.ModelElementActionRecord;
import ui.modeleditor.elements.ModelElementActionRecord.ConditionType;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementAction</code>
 * @author Alexander Herzog
 * @see ModelElementAction
 */
public class RunElementAction extends RunElement implements StateChangeListener, SignalListener {
	/**
	 * Liste der Aktions-Datensätze.<br>
	 * Diese Datensätze werden in {@link #getData(SimulationData)}
	 * an {@link RunElementActionData} in Form eines schnelleren
	 * Arrays übergeben und stehen dann als
	 * {@link RunElementActionData#records} zur Verfügung.
	 */
	private List<RunElementActionRecord> records;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementAction(final ModelElementAction element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Action.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementAction)) return null;
		final ModelElementAction actionElement=(ModelElementAction)element;
		final RunElementAction action=new RunElementAction(actionElement);

		action.records=new ArrayList<>();
		for (int i=0;i<actionElement.getRecordsList().size();i++) {
			final ModelElementActionRecord editRecord=actionElement.getRecordsList().get(i);
			if (!editRecord.isActive()) continue;
			final RunElementActionRecord record=new RunElementActionRecord(editRecord,action.id);
			final String error=record.build(editModel,runModel,testOnly,i==0);
			if (error!=null) return error+" ("+String.format(Language.tr("Simulation.Creator.Action.ErrorInfo"),actionElement.getId(),i+1)+")";
			action.records.add(record);
		}

		return action;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementAction)) return null;
		final ModelElementAction actionElement=(ModelElementAction)element;

		for (int i=0;i<actionElement.getRecordsList().size();i++) {
			final RunElementActionRecord record=new RunElementActionRecord(actionElement.getRecordsList().get(i),id);
			final String error=record.test(i==0);
			if (error!=null) return new RunModelCreatorStatus(error+" ("+String.format(Language.tr("Simulation.Creator.Action.ErrorInfo"),actionElement.getId(),i+1)+")");
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Wird nie aufgerufen: Analog-Wert-Elemente haben keine einlaufenden Kanten bzw. führen keine Verarbeitung von Kunden durch. */
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		/* Wird nie aufgerufen: Analog-Wert-Elemente haben keine einlaufenden Kanten bzw. führen keine Verarbeitung von Kunden durch. */
	}

	@Override
	public RunElementActionData getData(final SimulationData simData) {
		RunElementActionData data;
		data=(RunElementActionData)(simData.runData.getStationData(this));
		if (data==null) {
			final RunElementActionRecord[] dataRecords=records.stream().map(record->new RunElementActionRecord(record)).toArray(RunElementActionRecord[]::new);
			for (int i=0;i<dataRecords.length;i++) dataRecords[i].initRunData(simData,i);
			for (int i=1;i<dataRecords.length;i++) if (dataRecords[i].conditionType==ConditionType.CONDITION_WITH_PREVIOUS) dataRecords[i-1].nextAction=dataRecords[i];
			data=new RunElementActionData(this,dataRecords,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public boolean systemStateChangeNotify(final SimulationData simData) {
		boolean actionTriggered=false;

		final RunElementActionData data=getData(simData);

		for (int i=0;i<data.records.length;i++) {
			final RunElementActionRecord record=data.records[i];
			if (record.checkTrigger(simData,name)) {
				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Action"),String.format(Language.tr("Simulation.Log.Action.Info"),name,i+1));

				/* Aktion auslösen */
				record.runAction(simData,name,logTextColor);

				actionTriggered=true;
			}
		}

		return actionTriggered;
	}

	@Override
	public void signalNotify(SimulationData simData, String signalName) {
		final RunElementActionData data=getData(simData);

		boolean actionsTriggered=false;
		for (int i=0;i<data.records.length;i++) {
			final RunElementActionRecord record=data.records[i];
			if (record.checkTriggerSignal(signalName)) {
				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Action"),String.format(Language.tr("Simulation.Log.Action.Info"),name,i+1));

				/* Aktion auslösen */
				record.runAction(simData,name,logTextColor);
				actionsTriggered=true;
			}
		}
		if (actionsTriggered) simData.runData.fireStateChangeNotify(simData);
	}

	/**
	 * Führt eine Aktion innerhalb des Elements zeitgesteuert aus.
	 * @param simData	Simulationsdatenobjekt
	 * @param index	Index des auszuführenden Datensatzes innerhalb dieses Action-Elements
	 * @see TimedActionEvent
	 */
	public void timeTrigger(final SimulationData simData, final int index) {
		final RunElementActionData data=getData(simData);
		data.records[index].runTimedAction(simData,name,logTextColor,index);
	}
}
