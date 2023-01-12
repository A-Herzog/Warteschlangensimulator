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
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementCosts;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementCosts</code>
 * @author Alexander Herzog
 * @see ModelElementCosts
 */
public class RunElementCosts extends RunElementPassThrough {
	/**
	 * Kosten an der Station (kann <code>null</code> sein)
	 * @see RunElementCostsData#stationCosts
	 */
	private String stationCosts;

	/**
	 * Zusätzliche Kunden-Wartezeit-Kosten (kann <code>null</code> sein)
	 * @see RunElementCostsData#clientWaitingCosts
	 */
	private String clientWaitingCosts;

	/**
	 * Zusätzliche Kunden-Transferzeit-Kosten (kann <code>null</code> sein)
	 * @see RunElementCostsData#clientTransferCosts
	 */
	private String clientTransferCosts;

	/**
	 * Zusätzliche Kunden-Bedienzeit-Kosten (kann <code>null</code> sein)
	 * @see RunElementCostsData#clientProcessCosts
	 */
	private String clientProcessCosts;

	/**
	 * Optionale zusätzliche Bedingung, die für eine Zuweisung erfüllt sein muss (kann <code>null</code> sein)
	 */
	private String condition;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementCosts(final ModelElementCosts element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Costs.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementCosts)) return null;
		final ModelElementCosts costsElement=(ModelElementCosts)element;
		final RunElementCosts costs=new RunElementCosts(costsElement);

		/* Auslaufende Kante */
		final String edgeError=costs.buildEdgeOut(costsElement);
		if (edgeError!=null) return edgeError;

		/* Kosten */
		String text;

		text=costsElement.getStationCosts();
		if (text==null || text.trim().isEmpty()) {
			costs.stationCosts=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorStation"),text,element.getId(),error+1);
			costs.stationCosts=text;
		}

		text=costsElement.getClientWaitingCosts();
		if (text==null || text.trim().isEmpty()) {
			costs.clientWaitingCosts=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorWaiting"),text,element.getId(),error+1);
			costs.clientWaitingCosts=text;
		}

		text=costsElement.getClientTransferCosts();
		if (text==null || text.trim().isEmpty()) {
			costs.clientTransferCosts=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorTransfer"),text,element.getId(),error+1);
			costs.clientTransferCosts=text;
		}

		text=costsElement.getClientProcessCosts();
		if (text==null || text.trim().isEmpty()) {
			costs.clientProcessCosts=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorProcess"),text,element.getId(),error+1);
			costs.clientProcessCosts=text;
		}

		/* Optionale Bedingung */
		final String condition=costsElement.getCondition();
		if (condition==null || condition.trim().isEmpty()) {
			costs.condition=null;
		} else {
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsCondition"),condition,element.getId(),error+1);
			costs.condition=condition;
		}

		return costs;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementCosts)) return null;
		final ModelElementCosts costsElement=(ModelElementCosts)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(costsElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementCostsData getData(final SimulationData simData) {
		RunElementCostsData data;
		data=(RunElementCostsData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementCostsData(this,stationCosts,clientWaitingCosts,clientTransferCosts,clientProcessCosts,condition,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Führt die eigentliche Kostenzuweisung durch.
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param client	Kunde
	 */
	private void applyCosts(final SimulationData simData, final RunElementCostsData data, final RunDataClient client) {
		simData.runData.setClientVariableValues(client);

		double stationCosts;
		if (data.stationCosts==null) {
			stationCosts=0.0;
		} else {
			try {
				stationCosts=data.stationCosts.calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(data.stationCosts,this);
				stationCosts=0;
			}
		}
		double clientWaitingCosts;
		if (data.clientWaitingCosts==null) {
			clientWaitingCosts=0.0;
		} else {
			try {
				clientWaitingCosts=data.clientWaitingCosts.calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(data.clientWaitingCosts,this);
				clientWaitingCosts=0;
			}
		}
		double clientTransferCosts;
		if (data.clientTransferCosts==null) {
			clientTransferCosts=0.0;
		} else {
			try {
				clientTransferCosts=data.clientTransferCosts.calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(data.clientTransferCosts,this);
				clientTransferCosts=0;
			}
		}
		double clientProcessCosts;
		if (data.clientProcessCosts==null) {
			clientProcessCosts=0.0;
		} else {
			try {
				clientProcessCosts=data.clientProcessCosts.calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(data.clientProcessCosts,this);
				clientProcessCosts=0;
			}
		}

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Costs"),String.format(Language.tr("Simulation.Log.Costs.Info"),client.logInfo(simData),name,NumberTools.formatNumber(stationCosts),NumberTools.formatNumber(clientWaitingCosts),NumberTools.formatNumber(clientTransferCosts),NumberTools.formatNumber(clientProcessCosts)));

		if (!simData.runData.isWarmUp) {
			/* Stationskosten verarbeiten */
			simData.runData.logStationCosts(simData,this,stationCosts);

			/* Kundenkosten verarbeiten */
			client.waitingAdditionalCosts+=clientWaitingCosts;
			client.transferAdditionalCosts+=clientTransferCosts;
			client.processAdditionalCosts+=clientProcessCosts;
		}
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementCostsData data=getData(simData);

		/* Zuweisung durchführen */
		if (condition!=null) {
			simData.runData.setClientVariableValues(client);
			if (data.condition.eval(simData.runData.variableValues,simData,client)) applyCosts(simData,data,client);
		} else {
			applyCosts(simData,data,client);
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
