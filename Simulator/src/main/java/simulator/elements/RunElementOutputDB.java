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
import mathtools.TimeTools;
import simcore.SimData;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.db.DBConnect;
import simulator.db.DBSettings;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementOutputDB;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementOutputDB</code>
 * @author Alexander Herzog
 * @see ModelElementOutputDB
 */
public class RunElementOutputDB extends RunElementPassThrough {
	private ModelElementOutputDB.OutputMode[] mode;
	private Object[] data;
	private String[] column;
	private DBSettings settings;
	private String tableName;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementOutputDB(final ModelElementOutputDB element) {
		super(element,buildName(element,Language.tr("Simulation.Element.OutputDB.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementOutputDB)) return null;
		final ModelElementOutputDB outputElement=(ModelElementOutputDB)element;
		final RunElementOutputDB output=new RunElementOutputDB(outputElement);

		/* Auslaufende Kante */
		final String edgeError=output.buildEdgeOut(outputElement);
		if (edgeError!=null) return edgeError;

		/* Datenbank & Tabelle */

		output.settings=outputElement.getDb();
		output.tableName=outputElement.getTable();
		try (DBConnect connect=new DBConnect(output.settings,false)) {
			if (connect.getInitError()!=null) return Language.tr("Simulation.Creator.DatabaseError")+": "+connect.getInitError();
			boolean tableExists=false;
			for (String test: connect.listTables()) if (test.equalsIgnoreCase(output.tableName)) {tableExists=true; break;}
			if (!tableExists) return String.format(Language.tr("Simulation.Creator.DatabaseTableDoesNotExist"),output.id,output.tableName);
		}

		/* Ausgaben */

		final List<ModelElementOutputDB.OutputMode> modeList=outputElement.getModes();
		final List<String> columnList=outputElement.getColumns();
		final List<String> dataList=outputElement.getData();
		final List<ModelElementOutputDB.OutputMode> modeOutputList=new ArrayList<>();
		final List<String> columnOutputList=new ArrayList<>();
		final List<Object> dataOutputList=new ArrayList<>();

		for (int i=0;i<modeList.size();i++) {
			final ModelElementOutputDB.OutputMode mode=modeList.get(i);
			Object data=null;

			final String s=(dataList.size()>i)?dataList.get(i):"";

			if (mode==ModelElementOutputDB.OutputMode.MODE_TEXT || mode==ModelElementOutputDB.OutputMode.MODE_STRING) {
				data=s;
			}

			if (mode==ModelElementOutputDB.OutputMode.MODE_EXPRESSION) {
				final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
				final int error=calc.parse(s);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidOutputExpression"),i+1,output.id,error+1);
				data=calc;
			}

			final String column=columnList.get(i);
			for (int j=0;j<columnOutputList.size();j++) if (columnOutputList.get(j).equalsIgnoreCase(column)) return String.format(Language.tr("Simulation.Creator.DoubleUseOfDatabaseColumn"),output.id,column);

			modeOutputList.add(mode);
			columnOutputList.add(column);
			dataOutputList.add(data);
		}
		output.mode=modeOutputList.toArray(new ModelElementOutputDB.OutputMode[0]);
		output.column=columnOutputList.toArray(new String[0]);
		output.data=dataOutputList.toArray(new Object[0]);

		return output;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementOutputDB)) return null;
		final ModelElementOutputDB outputElement=(ModelElementOutputDB)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(outputElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementOutputDBData getData(final SimulationData simData) {
		RunElementOutputDBData data;
		data=(RunElementOutputDBData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementOutputDBData(this,settings);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	private String getCellValue(final SimulationData simData, final RunDataClient client, final int index) {
		switch (mode[index]) {
		case MODE_TIMESTAMP:
			return SimData.formatSimTime(simData.currentTime);
		case MODE_TEXT:
			return (String)data[index];
		case MODE_EXPRESSION:
			simData.runData.setClientVariableValues(client);
			if (simData.runModel.stoppOnCalcError) {
				final Double D=((ExpressionCalc)data[index]).calc(simData.runData.variableValues,simData,client);
				if (D==null) simData.calculationErrorStation((ExpressionCalc)data[index],this);
				return NumberTools.formatSystemNumber((D==null)?0.0:D.doubleValue());
			} else {
				return NumberTools.formatSystemNumber(((ExpressionCalc)data[index]).calcOrDefault(simData.runData.variableValues,simData,client,0));
			}
		case MODE_CLIENT:
			return simData.runModel.clientTypes[client.type];
		case MODE_WAITINGTIME_NUMBER:
			return NumberTools.formatSystemNumber(((double)client.waitingTime)/1000);
		case MODE_WAITINGTIME_TIME:
			return TimeTools.formatExactTime(((double)client.waitingTime)/1000);
		case MODE_TRANSFERTIME_NUMBER:
			return NumberTools.formatSystemNumber(((double)client.transferTime)/1000);
		case MODE_TRANSFERTIME_TIME:
			return TimeTools.formatExactTime(((double)client.transferTime)/1000);
		case MODE_PROCESSTIME_NUMBER:
			return NumberTools.formatSystemNumber(((double)client.processTime)/1000);
		case MODE_PROCESSTIME_TIME:
			return TimeTools.formatExactTime(((double)client.processTime)/1000);
		case MODE_RESIDENCETIME_NUMBER:
			return NumberTools.formatNumberMax(((double)client.residenceTime)/1000);
		case MODE_RESIDENCETIME_TIME:
			return TimeTools.formatExactTime(((double)client.residenceTime)/1000);
		case MODE_STRING:
			return client.getUserDataString((String)data[index]);
		default:
			return "";
		}
	}

	private void processOutput(final SimulationData simData, final RunDataClient client) {
		final RunElementOutputDBData data=getData(simData);
		if (data.connect==null) return;

		/* Werte zusammenstellen */
		if (data.cellValueBuffer==null) data.cellValueBuffer=new String[mode.length];
		for (int i=0;i<mode.length;i++) data.cellValueBuffer[i]=getCellValue(simData,client,i);

		/* Werte in Tabelle schreiben */
		data.connect.writeRow(tableName,column,data.cellValueBuffer);
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.OutputDB"),String.format(Language.tr("Simulation.Log.OutputDB.Info"),client.logInfo(simData),name));

		if (!client.isWarmUp && client.inStatistics) {
			/* Ausgabe durchführen */
			processOutput(simData,client);
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void finalCleanUp(final SimulationData simData) {
		getData(simData).closeDB();
	}
}
