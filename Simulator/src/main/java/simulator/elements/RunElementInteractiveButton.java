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
import simulator.events.InteractiveButtonClickedEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementActionRecord;
import ui.modeleditor.elements.ModelElementInteractiveButton;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementInteractiveButton</code>
 * @author Alexander Herzog
 * @see ModelElementInteractiveButton
 */
public class RunElementInteractiveButton extends RunElement {
	/**
	 * Liste der Aktions-Datensätze.<br>
	 * Diese Datensätze werden in {@link #getData(SimulationData)}
	 * an {@link RunElementInteractiveButtonData} in Form eines schnelleren
	 * Arrays übergeben und stehen dann als
	 * {@link RunElementInteractiveButtonData#records} zur Verfügung.
	 */
	private List<RunElementActionRecord> records;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementInteractiveButton(final ModelElementInteractiveButton element) {
		super(element,buildName(element,Language.tr("Simulation.Element.InteractiveButton.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementInteractiveButton)) return null;
		final ModelElementInteractiveButton buttonElement=(ModelElementInteractiveButton)element;
		final RunElementInteractiveButton button=new RunElementInteractiveButton(buttonElement);

		button.records=new ArrayList<>();
		for (int i=0;i<buttonElement.getRecordsList().size();i++) {
			final ModelElementActionRecord editRecord=buttonElement.getRecordsList().get(i);
			if (!editRecord.isActive()) continue;
			final RunElementActionRecord record=new RunElementActionRecord(editRecord,id);
			final String error=record.build(editModel,runModel,testOnly);
			if (error!=null) return error+" ("+String.format(Language.tr("Simulation.Creator.Action.ErrorInfo"),buttonElement.getId(),i+1)+")";
			button.records.add(record);
		}

		return button;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementInteractiveButton)) return null;
		final ModelElementInteractiveButton buttonElement=(ModelElementInteractiveButton)element;

		for (int i=0;i<buttonElement.getRecordsList().size();i++) {
			final RunElementActionRecord record=new RunElementActionRecord(buttonElement.getRecordsList().get(i),id);
			final String error=record.test();
			if (error!=null) return new RunModelCreatorStatus(error+" ("+String.format(Language.tr("Simulation.Creator.Action.ErrorInfo"),buttonElement.getId(),i+1)+")");
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
	public RunElementInteractiveButtonData getData(final SimulationData simData) {
		RunElementInteractiveButtonData data;
		data=(RunElementInteractiveButtonData)(simData.runData.getStationData(this));
		if (data==null) {
			final RunElementActionRecord[] dataRecords=records.stream().map(record->new RunElementActionRecord(record)).toArray(RunElementActionRecord[]::new);
			for (int i=0;i<dataRecords.length;i++) dataRecords[i].initRunData(simData,i);
			data=new RunElementInteractiveButtonData(this,dataRecords);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Wird indirekt von {@link #clicked(SimulationData)} über das
	 * {@link InteractiveButtonClickedEvent}-Ereignis ausgelöst.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void triggered(final SimulationData simData) {
		final RunElementInteractiveButtonData data=getData(simData);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.InteractiveButton"),String.format(Language.tr("Simulation.Log.InteractiveButton.Info"),name));

		boolean actionsTriggered=false;
		for (int i=0;i<data.records.length;i++) {
			final RunElementActionRecord record=data.records[i];
			/* Aktion auslösen */
			record.runAction(simData,name,logTextColor);
			actionsTriggered=true;
		}
		if (actionsTriggered) simData.runData.fireStateChangeNotify(simData);
	}

	/**
	 * Wird von {@link ModelElementInteractiveButton} aufgerufen, wenn das
	 * Button angeklickt wird. Es wird dann ein Event generiert, welches
	 * wiederum {@link #triggered(SimulationData)} aufruft.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void clicked(final SimulationData simData) {
		final InteractiveButtonClickedEvent event=(InteractiveButtonClickedEvent)simData.getEvent(InteractiveButtonClickedEvent.class);
		event.init(simData.currentTime);
		event.interactiveButton=this;
		simData.eventManager.addEvent(event);
	}
}
