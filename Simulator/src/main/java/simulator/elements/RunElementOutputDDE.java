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
import mathtools.Table;
import mathtools.TimeTools;
import net.dde.DDEConnect;
import parser.MathCalcError;
import simcore.SimData;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementOutputDDE;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementOutputDDE</code>
 * @author Alexander Herzog
 * @see ModelElementOutputDDE
 */
public class RunElementOutputDDE extends RunElementPassThrough {
	private String workbook;
	private String table;
	private int startRow;
	private int startColumn;

	private ModelElementOutputDDE.OutputMode[] mode;
	private Object[] data;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementOutputDDE(final ModelElementOutputDDE element) {
		super(element,buildName(element,Language.tr("Simulation.Element.OutputDDE.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementOutputDDE)) return null;
		final ModelElementOutputDDE outputElement=(ModelElementOutputDDE)element;
		final RunElementOutputDDE output=new RunElementOutputDDE(outputElement);

		/* Auslaufende Kante */
		final String edgeError=output.buildEdgeOut(outputElement);
		if (edgeError!=null) return edgeError;

		/* DDE im Allgemeinen */
		if (!new DDEConnect().available()) return String.format(Language.tr("Simulation.Creator.DDENotAvailable"),element.getId());

		/* DDE Daten */
		output.workbook=outputElement.getWorkbook().trim();
		if (output.workbook.isEmpty()) return String.format(Language.tr("Simulation.Creator.OutputDDE.NoWorkbook"),output.id);
		output.table=outputElement.getTable().trim();
		if (output.table.isEmpty()) return String.format(Language.tr("Simulation.Creator.OutputDDE.NoTable"),output.id);
		output.startRow=outputElement.getStartRow()-1;
		if (output.startRow<0) return String.format(Language.tr("Simulation.Creator.OutputDDE.InvalidStartRow"),output.id,outputElement.getStartRow());
		output.startColumn=Table.numberFromColumnName(outputElement.getColumn());
		if (output.startColumn<0) return String.format(Language.tr("Simulation.Creator.OutputDDE.InvalidStartColumn"),output.id,outputElement.getColumn());

		/* Ausgaben */
		final List<ModelElementOutputDDE.OutputMode> modeList=outputElement.getModes();
		final List<String> dataList=outputElement.getData();
		final List<ModelElementOutputDDE.OutputMode> modeOutputList=new ArrayList<>();
		final List<Object> dataOutputList=new ArrayList<>();

		for (int i=0;i<modeList.size();i++) {
			final ModelElementOutputDDE.OutputMode mode=modeList.get(i);
			Object data=null;

			final String s=(dataList.size()>i)?dataList.get(i):"";

			if (mode==ModelElementOutputDDE.OutputMode.MODE_TEXT || mode==ModelElementOutputDDE.OutputMode.MODE_STRING) {
				data=s;
			}

			if (mode==ModelElementOutputDDE.OutputMode.MODE_EXPRESSION) {
				final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
				final int error=calc.parse(s);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidOutputExpression"),i+1,output.id,error+1);
				data=calc;
			}

			modeOutputList.add(mode);
			dataOutputList.add(data);
		}
		output.mode=modeOutputList.toArray(new ModelElementOutputDDE.OutputMode[0]);
		output.data=dataOutputList.toArray(new Object[0]);

		return output;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementOutputDDE)) return null;
		final ModelElementOutputDDE outputElement=(ModelElementOutputDDE)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(outputElement);
		if (edgeError!=null) return edgeError;

		/* DDE im Allgemeinen */
		if (!new DDEConnect().available()) return RunModelCreatorStatus.noDDE(element);

		/* DDE Daten */
		if (outputElement.getWorkbook().trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.OutputDDE.NoWorkbook"),outputElement.getId()));
		if (outputElement.getTable().trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.OutputDDE.NoWorkbook"),outputElement.getId()));
		if (outputElement.getStartRow()<1) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.OutputDDE.InvalidStartRow"),outputElement.getId(),outputElement.getStartRow()),RunModelCreatorStatus.Status.DDE_OUTPUT_INVALID_ROW);
		if (Table.numberFromColumnName(outputElement.getColumn())<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.OutputDDE.InvalidStartColumn"),outputElement.getId(),outputElement.getColumn()),RunModelCreatorStatus.Status.DDE_OUTPUT_INVALID_COL);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementOutputDDEData getData(final SimulationData simData) {
		RunElementOutputDDEData data;
		data=(RunElementOutputDDEData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementOutputDDEData(this,workbook,table,startRow,startColumn);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	private static final double toSec=1.0/1000.0;

	private String[] getOutputTableLine(final SimulationData simData, final RunDataClient client) {
		final String[] line=new String[mode.length];
		for (int i=0;i<mode.length;i++) switch (mode[i]) {
		case MODE_TIMESTAMP:
			line[i]=SimData.formatSimTime(simData.currentTime);
			break;
		case MODE_TEXT:
			line[i]=(String)data[i];
			break;
		case MODE_EXPRESSION:
			simData.runData.setClientVariableValues(client);
			try {
				line[i]=NumberTools.formatNumberMax(((ExpressionCalc)data[i]).calc(simData.runData.variableValues,simData,client));
			} catch (MathCalcError e) {
				simData.calculationErrorStation((ExpressionCalc)data[i],this);
				line[i]=NumberTools.formatNumberMax(0);
			}
			break;
		case MODE_CLIENT:
			line[i]=simData.runModel.clientTypes[client.type];
			break;
		case MODE_WAITINGTIME_NUMBER:
			line[i]=NumberTools.formatNumberMax(client.waitingTime*toSec);
			break;
		case MODE_WAITINGTIME_TIME:
			line[i]=TimeTools.formatExactTime(client.waitingTime*toSec);
			break;
		case MODE_TRANSFERTIME_NUMBER:
			line[i]=NumberTools.formatNumberMax(client.transferTime*toSec);
			break;
		case MODE_TRANSFERTIME_TIME:
			line[i]=TimeTools.formatExactTime(client.transferTime*toSec);
			break;
		case MODE_PROCESSTIME_NUMBER:
			line[i]=NumberTools.formatNumberMax(client.processTime*toSec);
			break;
		case MODE_PROCESSTIME_TIME:
			line[i]=TimeTools.formatExactTime(client.processTime*toSec);
			break;
		case MODE_RESIDENCETIME_NUMBER:
			line[i]=NumberTools.formatNumberMax(client.residenceTime*toSec);
			break;
		case MODE_RESIDENCETIME_TIME:
			line[i]=TimeTools.formatExactTime(client.residenceTime*toSec);
			break;
		case MODE_STRING:
			line[i]=client.getUserDataString((String)data[i]);
			break;
		default:
			break;
		}
		return line;
	}

	private void processOutput(final SimulationData simData, final RunDataClient client) {
		final String[] line=getOutputTableLine(simData,client);
		if (line==null || line.length==0) return;

		final RunElementOutputDDEData data=getData(simData);
		data.writeLine(line);
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.OutputDDE"),String.format(Language.tr("Simulation.Log.OutputDDE.Info"),client.logInfo(simData),name));

		if (!client.isWarmUp && client.inStatistics) {
			/* Ausgabe durchführen */
			processOutput(simData,client);
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void finalCleanUp(final SimulationData simData) {
		getData(simData).closeConnection();
	}
}
