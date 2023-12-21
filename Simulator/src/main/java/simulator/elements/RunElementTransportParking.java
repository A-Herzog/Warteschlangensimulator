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
import mathtools.NumberTools;
import parser.MathCalcError;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataTransporter;
import simulator.runmodel.RunDataTransporters;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportParking;

/**
 * Äquivalent zu <code>ModelElementTransportParking</code>
 * @author Alexander Herzog
 * @see ModelElementTransportParking
 */
public class RunElementTransportParking extends RunElement implements TransporterPosition {
	/** Index des Transportertyps der diese Station ansteuert */
	private int transporterIndex;
	/** Anzahl an Transportern, die hier parken können */
	private int capacity;
	/** Priorität mit der verfügbare Transporter angezogen werden */
	private String priorityString;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTransportParking(final ModelElementTransportParking element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TransportParking.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementTransportParking)) return null;

		RunElementTransportParking parking=new RunElementTransportParking((ModelElementTransportParking)element);

		/* Transportertyp */
		parking.transporterIndex=runModel.transportersTemplate.getTransporterIndex(((ModelElementTransportParking)element).getTransporterType());
		if (parking.transporterIndex<0) return String.format(Language.tr("Simulation.Creator.UnknownTransporterName"),element.getId(),((ModelElementTransportParking)element).getTransporterType());

		/* Kapazität */
		parking.capacity=((ModelElementTransportParking)element).getWaitingCapacity();
		if (parking.capacity<0) return String.format(Language.tr("Simulation.Creator.InvalidParkingCapacity"),element.getId(),parking.capacity);

		/* Priorität */
		parking.priorityString=((ModelElementTransportParking)element).getWaitingPriority();
		final int error=ExpressionCalc.check(parking.priorityString,runModel.variableNames);
		if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidParkingPriority"),element.getId(),parking.priorityString,error+1);

		return parking;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementTransportParking)) return null;

		/* Kapazität */
		final int capacity=((ModelElementTransportParking)element).getWaitingCapacity();
		if (capacity<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InvalidParkingCapacity"),element.getId(),capacity),RunModelCreatorStatus.Status.PARKING_NEGATIVE_CAPACITY);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementTransportParkingData getData(final SimulationData simData) {
		RunElementTransportParkingData data;
		data=(RunElementTransportParkingData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementTransportParkingData(this,priorityString,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Liefert den Index der Transportergruppe, die diesen Parkplatz ansteuert.
	 * @return	Index der Transportergruppe, die diesen Parkplatz ansteuert
	 * @see RunDataTransporters#getTransporterIndex(String)
	 */
	public int getTransporterIndex() {
		return transporterIndex;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Wird nie aufgerufen: Parkplatz-Elemente haben keine einlaufenden Kanten bzw. führen keine Verarbeitung von Kunden durch. */
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Wird nie aufgerufen: Parkplatz-Elemente haben keine einlaufenden Kanten bzw. führen keine Verarbeitung von Kunden durch. */
	}

	@Override
	public void transporterArrival(RunDataTransporter transporter, SimulationData simData) {
		final RunElementTransportParkingData data=getData(simData);
		data.moving--;
		data.count++;

		/* Logging */
		if (simData.loggingActive) {
			if (data.count==1) {
				log(simData,Language.tr("Simulation.Log.Parking"),String.format(Language.tr("Simulation.Log.Parking.Info.Singular"),name));
			} else {
				log(simData,Language.tr("Simulation.Log.Parking"),String.format(Language.tr("Simulation.Log.Parking.Info"),name,data.count));
			}
		}
	}

	@Override
	public void transporterLeave(RunDataTransporter transporter, SimulationData simData) {
		final RunElementTransportParkingData data=getData(simData);
		data.count--;

		/* Logging */
		if (simData.loggingActive) {
			if (data.count==1) {
				log(simData,Language.tr("Simulation.Log.Parking"),String.format(Language.tr("Simulation.Log.Parking.Info.Singular"),name));
			} else {
				log(simData,Language.tr("Simulation.Log.Parking"),String.format(Language.tr("Simulation.Log.Parking.Info"),name,data.count));
			}
		}

		/* Freie Transporter ggf. auf Warteplätze holen */
		if (data.count+data.moving<capacity) {
			final RunDataTransporter newTransporter=simData.runData.transporters.getWaitingTransporterAtOtherStation(transporterIndex,id,getRequestPriorityInt(simData),simData);
			if (newTransporter!=null) {
				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Parking"),String.format(Language.tr("Simulation.Log.Parking.Requesting"),name,newTransporter.position));

				/* Transporter anfordern */
				newTransporter.moveTo(id,0,simData);
			}
		}
	}

	@Override
	public void transporterFree(RunDataTransporter transporter, SimulationData simData) {
		if (transporter.type!=transporterIndex) return; /* Wollen wir nicht. */
		if (transporter.position==id) return; /* Haben wir schon. */

		final RunElementTransportParkingData data=getData(simData);
		if (data.count+data.moving>=capacity) return; /* Kein Platz mehr. */

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Parking"),String.format(Language.tr("Simulation.Log.Parking.Requesting"),name,transporter.position));

		/* Transporter anfordern */
		transporter.moveTo(id,0,simData);
	}

	/**
	 * Berechnet die Priorität zum Anfordern von Transportern.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Priorität zum Anfordern von Transportern
	 */
	private double getRequestPriorityInt(final SimulationData simData) {
		final RunElementTransportParkingData data=getData(simData);

		try {
			return data.priority.calc(simData.runData.variableValues,simData,null);
		} catch (MathCalcError e) {
			simData.calculationErrorStation(data.priority,this);
			return 0;
		}
	}

	@Override
	public Double requestPriority(RunDataTransporter transporter, SimulationData simData) {
		if (transporter.type!=transporterIndex) return null; /* Wollen wir nicht. */
		if (transporter.position==id) return null; /* Haben wir schon. */

		final RunElementTransportParkingData data=getData(simData);
		if (data.count+data.moving>=capacity) return null; /* Kein Platz mehr. */

		try {
			return NumberTools.fastBoxedValue(data.priority.calc(simData.runData.variableValues,simData,null));
		} catch (MathCalcError e) {
			simData.calculationErrorStation(data.priority,this);
			return NumberTools.fastBoxedValue(0);
		}
	}

	@Override
	public Double stayHerePriority(RunDataTransporter transporter, SimulationData simData) {
		if (transporter.type!=transporterIndex) return null; /* Wollen wir nicht. */

		final RunElementTransportParkingData data=getData(simData);
		if (data.count+data.moving>capacity) return null; /* Kein Platz mehr, wir wollen den überzähligen Transporter gerne loswerden. */

		try {
			return NumberTools.fastBoxedValue(data.priority.calc(simData.runData.variableValues,simData,null));
		} catch (MathCalcError e) {
			simData.calculationErrorStation(data.priority,this);
			return NumberTools.fastBoxedValue(0);
		}
	}

	@Override
	public void transporterStartsMoving(RunDataTransporter transporter, SimulationData simData) {
		final RunElementTransportParkingData data=getData(simData);
		data.moving++;
	}
}