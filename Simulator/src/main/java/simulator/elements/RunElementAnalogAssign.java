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
import mathtools.NumberTools;
import parser.MathCalcError;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementAnalogProcessing;
import simulator.coreelements.RunElementAnalogProcessingData;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementAnalogAssign;
import ui.modeleditor.elements.ModelElementAnalogValue;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTank;

/**
 * Äquivalent zu <code>ModelElementAnalogAssign</code>
 * @author Alexander Herzog
 * @see ModelElementAnalogAssign
 */
public class RunElementAnalogAssign extends RunElementPassThrough {
	/** IDs der Stationen an denen Änderungen vorgenommen werden sollen */
	private int[] changeID;
	/** Stationsobjekte an denen Änderungen vorgenommen werden sollen (wird aus {@link #changeID} abgeleitet) */
	private RunElementAnalogProcessing[] changeElement;
	/** In {@link #changeElement} Werte (<code>false</code>) oder Raten (<code>true</code>) ändern */
	private boolean[] changeRate;
	/** Auszuwertende und zuzuweisende Ausdrücke */
	private String[] changeExpression;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementAnalogAssign(final ModelElementAnalogAssign element) {
		super(element,buildName(element,Language.tr("Simulation.Element.AnalogAssign.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementAnalogAssign)) return null;
		final ModelElementAnalogAssign assignElement=(ModelElementAnalogAssign)element;
		final RunElementAnalogAssign assign=new RunElementAnalogAssign(assignElement);

		/* Auslaufende Kanten */
		final String edgeError=assign.buildEdgeOut(assignElement);
		if (edgeError!=null) return edgeError;

		/* Zuweisungen */
		final List<Integer> listID=new ArrayList<>();
		final List<Boolean> listRate=new ArrayList<>();
		final List<String> listExpression=new ArrayList<>();
		final int min=Math.min(assignElement.getChangeID().size(),Math.min(assignElement.getChangeMode().size(),assignElement.getChangeExpression().size()));
		for (int i=0;i<min;i++) {

			final int id=assignElement.getChangeID().get(i);
			final ModelElementAnalogAssign.ChangeMode mode=assignElement.getChangeMode().get(i);
			final String expression=assignElement.getChangeExpression().get(i);
			final ModelElement changeElement=editModel.surface.getByIdIncludingSubModels(id);
			if (changeElement==null) return String.format(Language.tr("Simulation.Creator.AnalogAssignTargetIDNotExistent"),element.getId(),id);

			if (mode!=ModelElementAnalogAssign.ChangeMode.CHANGE_MODE_RATE && mode!=ModelElementAnalogAssign.ChangeMode.CHANGE_MODE_VALUE) return String.format(Language.tr("Simulation.Creator.AnalogAssignInvalidMode"),element.getId());

			if (!(changeElement instanceof ModelElementAnalogValue) && !(changeElement instanceof ModelElementTank)) return String.format(Language.tr("Simulation.Creator.AnalogAssignTargetIDNoAnalogValue"),element.getId(),id);
			if (!(changeElement instanceof ModelElementAnalogValue) && (mode==ModelElementAnalogAssign.ChangeMode.CHANGE_MODE_RATE)) return String.format(Language.tr("Simulation.Creator.AnalogAssignCannotChangeRateOnTank"),element.getId(),id);

			final int error=ExpressionCalc.check(expression,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.AnalogAssignExpression"),expression,element.getId(),error+1);

			listID.add(id);
			listRate.add(mode==ModelElementAnalogAssign.ChangeMode.CHANGE_MODE_RATE);
			listExpression.add(expression);
		}
		assign.changeID=listID.stream().mapToInt(Integer::intValue).toArray();
		assign.changeRate=new boolean[listRate.size()];
		for (int i=0;i<assign.changeRate.length;i++) assign.changeRate[i]=listRate.get(i);
		assign.changeExpression=listExpression.toArray(new String[0]);

		return assign;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementAnalogAssign)) return null;
		final ModelElementAnalogAssign assignElement=(ModelElementAnalogAssign)element;

		/* Auslaufende Kanten */
		final RunModelCreatorStatus edgeError=testEdgeOut(assignElement);
		if (edgeError!=null) return edgeError;

		/* Zuweisungen */
		final int min=Math.min(assignElement.getChangeID().size(),Math.min(assignElement.getChangeMode().size(),assignElement.getChangeExpression().size()));
		for (int i=0;i<min;i++) {
			final int id=assignElement.getChangeID().get(i);
			final ModelElementAnalogAssign.ChangeMode mode=assignElement.getChangeMode().get(i);
			final ModelElement changeElement=element.getModel().surface.getByIdIncludingSubModels(id);
			if (changeElement==null) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogAssignTargetIDNotExistent"),element.getId(),id));

			if (mode!=ModelElementAnalogAssign.ChangeMode.CHANGE_MODE_RATE && mode!=ModelElementAnalogAssign.ChangeMode.CHANGE_MODE_VALUE) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogAssignInvalidMode"),element.getId()));

			if (!(changeElement instanceof ModelElementAnalogValue) && !(changeElement instanceof ModelElementTank)) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogAssignTargetIDNoAnalogValue"),element.getId(),id));
			if (!(changeElement instanceof ModelElementAnalogValue) && (mode==ModelElementAnalogAssign.ChangeMode.CHANGE_MODE_RATE)) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogAssignCannotChangeRateOnTank"),element.getId(),id));
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		super.prepareRun(runModel);

		changeElement=new RunElementAnalogProcessing[changeID.length];
		for (int i=0;i<changeID.length;i++) changeElement[i]=(RunElementAnalogProcessing)runModel.elements.get(changeID[i]);
	}

	@Override
	public RunElementAnalogAssignData getData(final SimulationData simData) {
		RunElementAnalogAssignData data;
		data=(RunElementAnalogAssignData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementAnalogAssignData(this,changeExpression,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		final RunElementAnalogAssignData data=getData(simData);

		simData.runData.setClientVariableValues(client);

		/* Zuweisungen vornehmen */
		for (int i=0;i<changeElement.length;i++) {
			final RunElementAnalogProcessingData changeData=(RunElementAnalogProcessingData)changeElement[i].getData(simData);

			/* Wert berechnen */
			double value=0;
			try {
				value=data.expressions[i].calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(data.expressions[i],this);
				continue;
			}

			if (changeRate[i]) {
				((RunElementAnalogValueData)changeData).setRate(simData,value);

				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.AnalogAssign"),String.format(Language.tr("Simulation.Log.AnalogAssign.InfoRate"),changeID[i],NumberTools.formatNumber(value)));
			} else {
				changeData.setValue(simData,value);

				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.AnalogAssign"),String.format(Language.tr("Simulation.Log.AnalogAssign.InfoValue"),changeID[i],NumberTools.formatNumber(value)));
			}
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

}