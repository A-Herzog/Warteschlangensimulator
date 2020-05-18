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
import statistics.StatisticsDataCollector;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementRecord;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementRecord</code>
 * @author Alexander Herzog
 * @see ModelElementRecord
 */
public class RunElementRecord extends RunElementPassThrough {
	private final static int MAX_VALUES=2_000_000;

	private String recordName;
	private String expression1;
	private String expression2;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementRecord(final ModelElementRecord element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Record.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementRecord)) return null;
		final ModelElementRecord recordElement=(ModelElementRecord)element;
		final RunElementRecord record=new RunElementRecord(recordElement);

		/* Auslaufende Kante */
		final String edgeError=record.buildEdgeOut(recordElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		final String name=recordElement.getName();
		if (name==null || name.isEmpty()) return String.format(Language.tr("Simulation.Creator.NoRecordName"),element.getId());
		record.recordName=name;

		/* Ausdruck 1 */
		final String expression1=recordElement.getExpression1();
		if (expression1==null || expression1.trim().isEmpty()) {
			return String.format(Language.tr("Simulation.Creator.RecordErrorExpression1Empty"),element.getId());
		} else {
			final int error=ExpressionCalc.check(expression1,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.RecordErrorExpression1"),expression1,element.getId(),error+1);
			record.expression1=expression1;
		}

		/* Ausdruck 2 */
		final String expression2=recordElement.getExpression2();
		if (expression2==null || expression2.trim().isEmpty()) {
			/* Ausdruck 2 ist optional */
		} else {
			final int error=ExpressionCalc.check(expression2,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.RecordErrorExpression2"),expression2,element.getId(),error+1);
			record.expression2=expression2;
		}

		return record;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementRecord)) return null;
		final ModelElementRecord recordElement=(ModelElementRecord)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(recordElement);
		if (edgeError!=null) return edgeError;

		/* Name */
		final String name=recordElement.getName();
		if (name==null || name.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoRecordName"),element.getId()),RunModelCreatorStatus.Status.NO_NAME);

		/* Ausdruck 1 */
		final String expression1=recordElement.getExpression1();
		if (expression1==null || expression1.trim().isEmpty()) {
			return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.RecordErrorExpression1Empty"),element.getId()));
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementRecordData getData(final SimulationData simData) {
		RunElementRecordData data;
		data=(RunElementRecordData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementRecordData(this,expression1,expression2,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Liefert die Statistikobjekte, in die die aufgezeichneten Daten eingetragen werden
	 * @param simData	Simulationsdatenobjekt
	 * @return	2-elementiges Array bei dem der zweite Eintrag <code>null</code> sein kann
	 */
	public StatisticsDataCollector[] getStatistics(final SimulationData simData) {
		final RunElementRecordData data=getData(simData);
		if (data.statistics1==null) {
			if (data.statistics1==null) data.statistics1=(StatisticsDataCollector)simData.statistics.valueRecording.get(recordName+"-1");
			if (data.expression2!=null) {
				if (data.statistics2==null) data.statistics2=(StatisticsDataCollector)simData.statistics.valueRecording.get(recordName+"-2");
			}
		}
		return new StatisticsDataCollector[]{data.statistics1,data.statistics2};
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementRecordData data=getData(simData);

		simData.runData.setClientVariableValues(client);

		double value1;
		try {
			value1=data.expression1.calc(simData.runData.variableValues,simData,client);
		} catch (MathCalcError e) {
			simData.calculationErrorStation(data.expression1,this);
			value1=0;
		}

		double value2;
		final boolean hasValue2;
		if (data.expression2==null) {
			hasValue2=false;
			value2=0;
		} else {
			hasValue2=true;
			try {
				value2=data.expression2.calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(data.expression1,this);
				value2=0;
			}
		}

		/* Logging */
		if (simData.loggingActive) {
			if (hasValue2) {
				log(simData,Language.tr("Simulation.Log.Record"),String.format(Language.tr("Simulation.Log.Record.Info1"),client.logInfo(simData),name,NumberTools.formatNumber(value1),NumberTools.formatNumber(value2)));
			} else {
				log(simData,Language.tr("Simulation.Log.Record"),String.format(Language.tr("Simulation.Log.Record.Info2"),client.logInfo(simData),name,NumberTools.formatNumber(value1)));
			}
		}

		/* Daten speichern */
		if (!simData.runData.isWarmUp) {
			if (data.statistics1==null) data.statistics1=(StatisticsDataCollector)simData.statistics.valueRecording.get(recordName+"-1");
			if (data.statistics1.getCount()<MAX_VALUES) {
				data.statistics1.add(value1);
				if (hasValue2) {
					if (data.statistics2==null) data.statistics2=(StatisticsDataCollector)simData.statistics.valueRecording.get(recordName+"-2");
					data.statistics2.add(value2);
				}
			} else {
				if (!data.warningDisplayed) {
					data.warningDisplayed=true;
					simData.addWarning(String.format(Language.tr("Simulation.Log.Record.StoppWarning"),name,MAX_VALUES));
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Record"),String.format(Language.tr("Simulation.Log.Record.StoppWarning"),name,MAX_VALUES));
				}
			}
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	/**
	 * Liefert den ersten zu erfassenden Ausdruck.
	 * @return	Erster zu erfassenden Ausdruck (nie <code>null</code>)
	 */
	public String getExpression1() {
		return expression1;
	}

	/**
	 * Liefert den zweiten zu erfassenden Ausdruck.
	 * @return	Zweiter zu erfassenden Ausdruck (kann <code>null</code> sein)
	 */
	public String getExpression2() {
		return expression2;
	}
}
