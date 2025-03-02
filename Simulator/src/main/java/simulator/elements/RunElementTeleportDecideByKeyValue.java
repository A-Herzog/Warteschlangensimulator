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

import java.util.List;
import java.util.stream.IntStream;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementDecideAndTeleport;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementDecideAndTeleport} (für den Modus "Texteigenschaft")
 * @author Alexander Herzog
 * @see ModelElementDecideAndTeleport
 */
public class RunElementTeleportDecideByKeyValue extends RunElement {
	/** Namen der Teleport-Zielstationen */
	private String[] destinationStrings;
	/** IDs der Teleport-Zielstationen */
	private int[] destinationIDs;
	/** Teleport-Zielstationen (übersetzt aus {@link #destinationIDs}) */
	private RunElement[] destinations;

	/** Für die Wahl der auslaufenden Kante auszuwertender Kundentextdaten-Schlüssel */
	private String key;
	/** Werte für {@link #key} die zu der Wahl einer jeweiligen auslaufenden Kante führen */
	private String[][] values;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTeleportDecideByKeyValue(final ModelElementDecideAndTeleport element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TeleportDecideByKeyValue.Name")));
	}

	@Override
	public Object build(EditModel editModel, RunModel runModel, ModelElement element, ModelElementSub parent, boolean testOnly) {
		if (!(element instanceof ModelElementDecideAndTeleport)) return null;
		final ModelElementDecideAndTeleport decideElement=(ModelElementDecideAndTeleport)element;
		if (decideElement.getMode()!=ModelElementDecide.DecideMode.MODE_KEY_VALUE) return null;
		final RunElementTeleportDecideByKeyValue decide=new RunElementTeleportDecideByKeyValue((ModelElementDecideAndTeleport)element);

		/* Schlüssel */
		if (decideElement.getKey().trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoKey"),element.getId());
		decide.key=decideElement.getKey();

		/* Mehrere Werte pro Wert-Eintrag? */
		final boolean multiTextValues=decideElement.isMultiTextValues();

		decide.destinationStrings=decideElement.getDestinations().toArray(String[]::new);
		decide.destinationIDs=new int[decide.destinationStrings.length];
		decide.values=new String[decide.destinationStrings.length-1][];
		final List<String> values=decideElement.getValues();
		int count=0;
		if (decide.destinationStrings.length==0) return String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId());
		for (String destination: decide.destinationStrings) {
			final int destinationID=RunElementTeleportSource.getDestinationID(element.getModel(),destination);
			if (destinationID<0) return String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),decide.destinationStrings[count]);
			decide.destinationIDs[count]=destinationID;
			/* Werte */
			final String value=(count>=values.size())?"":values.get(count);
			if (count<decide.destinationStrings.length-1) {
				if (multiTextValues) {
					final String[] v=value.split(";");
					if (v.length==0) return String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),count+1);
					for (int j=0;j<v.length;j++) v[j]=v[j].trim();
					for (String s: v) if (s.isEmpty()) return String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),count+1);
					decide.values[count]=v;
				} else {
					if (value.trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),count+1);
					decide.values[count]=new String[]{value};
				}
			}
			count++;
		}

		return decide;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementDecideAndTeleport)) return null;
		final ModelElementDecideAndTeleport decideElement=(ModelElementDecideAndTeleport)element;
		if (decideElement.getMode()!=ModelElementDecide.DecideMode.MODE_KEY_VALUE) return null;

		/* Schlüssel */
		if (decideElement.getKey().trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoKey"),element.getId()));

		/* Mehrere Werte pro Wert-Eintrag? */
		final boolean multiTextValues=decideElement.isMultiTextValues();

		final List<String> destinationStrings=decideElement.getDestinations();
		final List<String> values=decideElement.getValues();
		int count=0;
		if (destinationStrings.size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId()),RunModelCreatorStatus.Status.TELEPORT_INVALID_DESTINATION);
		for (String destination: destinationStrings) {
			final int destinationID=RunElementTeleportSource.getDestinationID(element.getModel(),destination);
			if (destinationID<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),destination),RunModelCreatorStatus.Status.TELEPORT_INVALID_DESTINATION);
			final String value=(count>=values.size())?"":values.get(count);
			/* Werte */
			if (count<destinationStrings.size()-1) {
				if (multiTextValues) {
					final String[] v=value.split(";");
					if (v.length==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),count+1));
					for (String s: v) if (s.trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),count+1));
				} else {
					if (value.trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),count+1));
				}
			}
			count++;
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		destinations=IntStream.of(destinationIDs).mapToObj(id->runModel.elements.get(id)).toArray(RunElement[]::new);
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		final String value=client.getUserDataString(key);
		int nr=-1;
		for (int i=0;i<values.length;i++) {
			final String[] v=values[i];
			for (int j=0;j<v.length;j++) if (value.equals(v[j])) {nr=i; break;}
			if (nr>=0) break;
		}
		if (nr<0) nr=destinations.length-1; /* Else */

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TeleportDecideByKeyValue"),String.format(Language.tr("Simulation.Log.TeleportDecideByKeyValue.Info"),client.logInfo(simData),name,key,value,nr+1,destinations.length));

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.sendToStation(simData,client,this,destinations[nr]);
	}
}