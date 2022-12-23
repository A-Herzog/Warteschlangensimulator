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
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionRandomNumber;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementDecideAndTeleport;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementDecideAndTeleport} (für den Modus "Zufall")
 * @author Alexander Herzog
 * @see ModelElementDecideAndTeleport
 */
public class RunElementTeleportDecideByChance extends RunElement {
	/** Namen der Teleport-Zielstationen */
	private String[] destinationStrings;
	/** IDs der Teleport-Zielstationen */
	private int[] destinationIDs;
	/** Teleport-Zielstationen (übersetzt aus {@link #destinationIDs}) */
	private RunElement[] destinations;
	/** Wahrscheinlichkeiten für die verschiedenen auslaufenden Kanten */
	private double[] probabilites;
	/** Rechenausdrücke für die Raten für die verschiedenen auslaufenden Kanten */
	private String[] probabilitesStrings;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTeleportDecideByChance(final ModelElementDecideAndTeleport element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TeleportDecideByChance.Name")));
	}

	@Override
	public Object build(EditModel editModel, RunModel runModel, ModelElement element, ModelElementSub parent, boolean testOnly) {
		if (!(element instanceof ModelElementDecideAndTeleport)) return null;
		final ModelElementDecideAndTeleport decideElement=(ModelElementDecideAndTeleport)element;
		if (decideElement.getMode()!=ModelElementDecide.DecideMode.MODE_CHANCE) return null;
		final RunElementTeleportDecideByChance decide=new RunElementTeleportDecideByChance((ModelElementDecideAndTeleport)element);

		decide.destinationStrings=decideElement.getDestinations().toArray(new String[0]);
		final List<String> rates=decideElement.getRates();
		decide.probabilites=new double[decide.destinationStrings.length];
		decide.probabilitesStrings=new String[decide.destinationStrings.length];
		decide.destinationIDs=new int[decide.destinationStrings.length];
		double sum=0;
		int count=0;
		if (decide.destinationStrings.length==0) return String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId());
		for (String destination: decide.destinationStrings) {
			final int destinationID=RunElementTeleportSource.getDestinationID(element.getModel(),destination);
			if (destinationID<0) return String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),decide.destinationStrings[count]);
			decide.destinationIDs[count]=destinationID;
			final String rate=(count>=rates.size())?"1":rates.get(count);
			decide.probabilitesStrings[count]=rate;
			final Double D=NumberTools.getPlainDouble(rate);
			if (D==null || sum==-1) {
				sum=-1;
				decide.probabilites=null;
			} else {
				decide.probabilites[count]=Math.max(0,D);
				sum+=Math.max(0,D);
			}
			count++;
		}

		if (decide.probabilites==null) {
			for (int i=0;i<decide.probabilitesStrings.length;i++) {
				final int error=ExpressionCalc.check(decide.probabilitesStrings[i],runModel.variableNames);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.DecideRate"),i+1,decide.probabilitesStrings[i],element.getId(),error+1);
			}
		} else {
			decide.probabilitesStrings=null;
			if (sum==0) return String.format(Language.tr("Simulation.Creator.NoDecideByChanceRates"),element.getId());
			for (int i=0;i<decide.probabilites.length;i++) decide.probabilites[i]=decide.probabilites[i]/sum;
		}

		return decide;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementDecideAndTeleport)) return null;
		final ModelElementDecideAndTeleport decideElement=(ModelElementDecideAndTeleport)element;
		if (decideElement.getMode()!=ModelElementDecide.DecideMode.MODE_CHANCE) return null;

		final List<String> destinationStrings=decideElement.getDestinations();
		final List<String> rates=decideElement.getRates();
		double sum=0;
		int count=0;
		if (destinationStrings.size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoTeleportDestination"),element.getId()),RunModelCreatorStatus.Status.TELEPORT_INVALID_DESTINATION);
		for (String destination: destinationStrings) {
			final int destinationID=RunElementTeleportSource.getDestinationID(element.getModel(),destination);
			if (destinationID<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidTeleportDestination"),element.getId(),destination),RunModelCreatorStatus.Status.TELEPORT_INVALID_DESTINATION);

			final String rateString=(count>=rates.size())?"1":rates.get(count);
			if (sum>=0) {
				Double D=NumberTools.getDouble(rateString);
				if (D==null) sum=-1; else sum+=Math.max(0,D);
			}
			count++;
		}
		if (sum==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoDecideByChanceRates"),element.getId()));

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		destinations=IntStream.of(destinationIDs).mapToObj(id->runModel.elements.get(id)).toArray(RunElement[]::new);
	}

	@Override
	public RunElementTeleportDecideByChanceData getData(final SimulationData simData) {
		RunElementTeleportDecideByChanceData data;
		data=(RunElementTeleportDecideByChanceData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementTeleportDecideByChanceData(this,probabilitesStrings,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Zielstation bestimmen */
		int nr=-1;
		if (probabilites==null) {
			/* Rechenausdrücke auswerten */
			simData.runData.setClientVariableValues(client);
			nr=getData(simData).getDestinationIndex(simData);
			if (nr<0) nr=probabilitesStrings.length-1;
		} else {
			/* Einfache Wahrscheinlichkeiten */
			final double rnd=DistributionRandomNumber.nextDouble();
			double sum=0;
			for (int i=0;i<probabilites.length;i++) {
				sum+=probabilites[i];
				if (sum>=rnd) {nr=i; break;}
			}
			if (nr<0) nr=probabilites.length-1;
		}

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TeleportDecideByChance"),String.format(Language.tr("Simulation.Log.TeleportDecideByChance.Info"),client.logInfo(simData),name,nr+1,destinations.length));

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.sendToStation(simData,client,this,destinations[nr]);
	}
}
