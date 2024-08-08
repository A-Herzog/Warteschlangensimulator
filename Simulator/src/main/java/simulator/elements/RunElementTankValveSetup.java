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
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTank;
import ui.modeleditor.elements.ModelElementTankValveSetup;

/**
 * Äquivalent zu <code>ModelElementTankValveSetup</code>
 * @author Alexander Herzog
 * @see ModelElementTankValveSetup
 */
public class RunElementTankValveSetup extends RunElementPassThrough {
	/** Liste der Ventil-Setups */
	private ModelElementTankValveSetup.ValveSetup[] valveSetups;
	/** Tanks auf die sich die jeweiligen Einstellungen in {@link #valveSetups} beziehen */
	private RunElementTank[] tanks;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTankValveSetup(final ModelElementTankValveSetup element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TankValveSetup.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementTankValveSetup)) return null;
		final ModelElementTankValveSetup setupElement=(ModelElementTankValveSetup)element;
		final RunElementTankValveSetup setup=new RunElementTankValveSetup(setupElement);

		/* Auslaufende Kante */
		final String edgeError=setup.buildEdgeOut(setupElement);
		if (edgeError!=null) return edgeError;

		/* Ventilkonfigurationen */
		final ModelSurface mainSurface=editModel.surface;
		for (ModelElementTankValveSetup.ValveSetup valveSetup: setupElement.getValveSetups()) {
			final ModelElement tank=mainSurface.getById(valveSetup.tankId);
			if (!(tank instanceof ModelElementTank)) return String.format(Language.tr("Simulation.Creator.TankValveSetup.IDIsNoTank"),element.getId(),valveSetup.tankId);
			if (((ModelElementTank)tank).getValves().size()<=valveSetup.valveNr) return String.format(Language.tr("Simulation.Creator.TankValveSetup.InvalidValve"),element.getId(),valveSetup.tankId,valveSetup.valveNr+1,((ModelElementTank)tank).getValves().size());

			final int error=ExpressionCalc.check(valveSetup.maxFlow,runModel.variableNames,runModel.modelUserFunctions);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.TankValveSetup.InvalidFlow"),element.getId(),valveSetup.maxFlow,error+1);
		}
		setup.valveSetups=setupElement.getValveSetups().toArray(new ModelElementTankValveSetup.ValveSetup[0]);
		setup.tanks=new RunElementTank[setup.valveSetups.length];

		return setup;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementTankValveSetup)) return null;
		final ModelElementTankValveSetup setupElement=(ModelElementTankValveSetup)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(setupElement);
		if (edgeError!=null) return edgeError;

		/* Ventilkonfigurationen */
		final ModelSurface mainSurface=element.getModel().surface;
		for (ModelElementTankValveSetup.ValveSetup valveSetup: setupElement.getValveSetups()) {
			final ModelElement tank=mainSurface.getById(valveSetup.tankId);
			if (!(tank instanceof ModelElementTank)) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.TankValveSetup.IDIsNoTank"),element.getId(),valveSetup.tankId));
			if (((ModelElementTank)tank).getValves().size()<=valveSetup.valveNr) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.TankValveSetup.InvalidValve"),element.getId(),valveSetup.tankId,valveSetup.valveNr+1,((ModelElementTank)tank).getValves().size()));
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		super.prepareRun(runModel); /* Standardverbindung herstellen */
		for (int i=0;i<valveSetups.length;i++) tanks[i]=(RunElementTank)runModel.elements.get(valveSetups[i].tankId);
	}

	@Override
	public RunElementTankValveSetupData getData(final SimulationData simData) {
		RunElementTankValveSetupData data;
		data=(RunElementTankValveSetupData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementTankValveSetupData(this,valveSetups,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		final RunElementTankValveSetupData data=getData(simData);

		for (int i=0;i<valveSetups.length;i++) {
			/* Ventilkonfiguration anpassen */
			final ModelElementTankValveSetup.ValveSetup valveSetup=valveSetups[i];
			final RunElementTank tank=tanks[i];
			simData.runData.setClientVariableValues(client);
			double maxFlow;
			try {
				maxFlow=data.maxFlow[i].calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(data.maxFlow[i],this);
				maxFlow=-1;
			}
			if (maxFlow<0) continue;
			tank.getData(simData).getValveValues()[valveSetup.valveNr]=maxFlow;

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TankValveSetup"),String.format(Language.tr("Simulation.Log.TankValveSetup.Info"),client.logInfo(simData),name,valveSetup.tankId,valveSetup.valveNr+1,NumberTools.formatNumberMax(maxFlow)));
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}