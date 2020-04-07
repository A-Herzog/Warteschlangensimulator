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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simcore.SimData;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataOutputWriter;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementOutput;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementOutput</code>
 * @author Alexander Herzog
 * @see ModelElementOutput
 */
public class RunElementOutput extends RunElementPassThrough {
	private boolean tableMode;
	private File outputFile;
	private RunDataOutputWriter outputWriter;

	private ModelElementOutput.OutputMode[] mode;
	private Object[] data;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementOutput(final ModelElementOutput element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Output.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementOutput)) return null;
		final ModelElementOutput outputElement=(ModelElementOutput)element;
		final RunElementOutput output=new RunElementOutput(outputElement);

		/* Auslaufende Kante */
		final String edgeError=output.buildEdgeOut(outputElement);
		if (edgeError!=null) return edgeError;

		/* Ausgabedatei */
		if (outputElement.getOutputFile().trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoOutputFile"),element.getId());
		output.outputFile=new File(outputElement.getOutputFile());

		/* Ausgaben */
		output.tableMode=isTable(output.outputFile);

		final List<ModelElementOutput.OutputMode> modeList=outputElement.getModes();
		final List<String> dataList=outputElement.getData();
		final List<ModelElementOutput.OutputMode> modeOutputList=new ArrayList<>();
		final List<Object> dataOutputList=new ArrayList<>();

		for (int i=0;i<modeList.size();i++) {
			final ModelElementOutput.OutputMode mode=modeList.get(i);
			Object data=null;

			if (output.tableMode) {
				if (mode==ModelElementOutput.OutputMode.MODE_TABULATOR || mode==ModelElementOutput.OutputMode.MODE_NEWLINE) continue;
			}

			final String s=(dataList.size()>i)?dataList.get(i):"";

			if (mode==ModelElementOutput.OutputMode.MODE_TEXT || mode==ModelElementOutput.OutputMode.MODE_STRING) {
				data=s;
			}

			if (mode==ModelElementOutput.OutputMode.MODE_EXPRESSION) {
				final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
				final int error=calc.parse(s);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidOutputExpression"),i+1,output.id,error+1);
				data=calc;
			}

			modeOutputList.add(mode);
			dataOutputList.add(data);
		}
		output.mode=modeOutputList.toArray(new ModelElementOutput.OutputMode[0]);
		output.data=dataOutputList.toArray(new Object[0]);

		return output;
	}

	private boolean isTable(final File file) {
		if (file==null) return false;
		final String nameLower=file.toString().toLowerCase();
		return nameLower.endsWith(".xls") || nameLower.endsWith(".xlsx") || nameLower.endsWith(".csv");
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementOutput)) return null;
		final ModelElementOutput outputElement=(ModelElementOutput)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(outputElement);
		if (edgeError!=null) return edgeError;

		/* Ausgabedatei */
		if (outputElement.getOutputFile().trim().isEmpty()) return RunModelCreatorStatus.noOutputFile(element);

		return RunModelCreatorStatus.ok;
	}

	private String getOutputString(final SimulationData simData, final RunDataClient client) {
		final StringBuilder sb=new StringBuilder();
		for (int i=0;i<mode.length;i++) switch (mode[i]) {
		case MODE_TIMESTAMP:
			sb.append(SimData.formatSimTime(simData.currentTime));
			break;
		case MODE_TEXT:
			sb.append((String)data[i]);
			break;
		case MODE_TABULATOR:
			sb.append('\t');
			break;
		case MODE_NEWLINE:
			sb.append(System.lineSeparator());
			break;
		case MODE_EXPRESSION:
			simData.runData.setClientVariableValues(client);
			if (simData.runModel.stoppOnCalcError) {
				final Double D=((ExpressionCalc)data[i]).calc(simData.runData.variableValues,simData,client);
				if (D==null) simData.calculationErrorStation((ExpressionCalc)data[i],this);
				sb.append(NumberTools.formatNumberMax((D==null)?0.0:D.doubleValue()));
			} else {
				sb.append(NumberTools.formatNumberMax(((ExpressionCalc)data[i]).calcOrDefault(simData.runData.variableValues,simData,client,0)));
			}
			break;
		case MODE_CLIENT:
			sb.append(simData.runModel.clientTypes[client.type]);
			break;
		case MODE_WAITINGTIME_NUMBER:
			sb.append(NumberTools.formatNumberMax(((double)client.waitingTime)/1000));
			break;
		case MODE_WAITINGTIME_TIME:
			sb.append(TimeTools.formatExactTime(((double)client.waitingTime)/1000));
			break;
		case MODE_TRANSFERTIME_NUMBER:
			sb.append(NumberTools.formatNumberMax(((double)client.transferTime)/1000));
			break;
		case MODE_TRANSFERTIME_TIME:
			sb.append(TimeTools.formatExactTime(((double)client.transferTime)/1000));
			break;
		case MODE_PROCESSTIME_NUMBER:
			sb.append(NumberTools.formatNumberMax(((double)client.processTime)/1000));
			break;
		case MODE_PROCESSTIME_TIME:
			sb.append(TimeTools.formatExactTime(((double)client.processTime)/1000));
			break;
		case MODE_RESIDENCETIME_NUMBER:
			sb.append(NumberTools.formatNumberMax(((double)client.residenceTime)/1000));
			break;
		case MODE_RESIDENCETIME_TIME:
			sb.append(TimeTools.formatExactTime(((double)client.residenceTime)/1000));
			break;
		case MODE_STRING:
			sb.append(client.getUserDataString((String)data[i]));
			break;
		}
		return sb.toString();
	}

	private String[] getOutputTableLine(final SimulationData simData, final RunDataClient client) {
		final String[] line=new String[mode.length];
		for (int i=0;i<mode.length;i++) switch (mode[i]) {
		case MODE_TIMESTAMP:
			line[i]=SimData.formatSimTime(simData.currentTime);
			break;
		case MODE_TEXT:
			line[i]=(String)data[i];
			break;
		case MODE_TABULATOR:
			/* In der Tabelle nichts zu tun */
			break;
		case MODE_NEWLINE:
			/* In der Tabelle nichts zu tun */
			break;
		case MODE_EXPRESSION:
			simData.runData.setClientVariableValues(client);
			if (simData.runModel.stoppOnCalcError) {
				final Double D=((ExpressionCalc)data[i]).calc(simData.runData.variableValues,simData,client);
				if (D==null) simData.calculationErrorStation((ExpressionCalc)data[i],this);
				line[i]=NumberTools.formatNumberMax((D==null)?0.0:D.doubleValue());
			} else {
				line[i]=NumberTools.formatNumberMax(((ExpressionCalc)data[i]).calcOrDefault(simData.runData.variableValues,simData,client,0));
			}
			break;
		case MODE_CLIENT:
			line[i]=simData.runModel.clientTypes[client.type];
			break;
		case MODE_WAITINGTIME_NUMBER:
			line[i]=NumberTools.formatNumberMax(((double)client.waitingTime)/1000);
			break;
		case MODE_WAITINGTIME_TIME:
			line[i]=TimeTools.formatExactTime(((double)client.waitingTime)/1000);
			break;
		case MODE_TRANSFERTIME_NUMBER:
			line[i]=NumberTools.formatNumberMax(((double)client.transferTime)/1000);
			break;
		case MODE_TRANSFERTIME_TIME:
			line[i]=TimeTools.formatExactTime(((double)client.transferTime)/1000);
			break;
		case MODE_PROCESSTIME_NUMBER:
			line[i]=NumberTools.formatNumberMax(((double)client.processTime)/1000);
			break;
		case MODE_PROCESSTIME_TIME:
			line[i]=TimeTools.formatExactTime(((double)client.processTime)/1000);
			break;
		case MODE_RESIDENCETIME_NUMBER:
			line[i]=NumberTools.formatNumberMax(((double)client.residenceTime)/1000);
			break;
		case MODE_RESIDENCETIME_TIME:
			line[i]=TimeTools.formatExactTime(((double)client.residenceTime)/1000);
			break;
		case MODE_STRING:
			line[i]=client.getUserDataString((String)data[i]);
			break;
		default:
			break;
		}
		return line;
	}

	private String[] getOutputTableHeadings() {
		final String[] line=new String[mode.length];
		for (int i=0;i<mode.length;i++) switch (mode[i]) {
		case MODE_TIMESTAMP:
			line[i]=Language.tr("Simulation.Output.TimeStamp");
			break;
		case MODE_TEXT:
			line[i]=Language.tr("Simulation.Output.Text");
			break;
		case MODE_TABULATOR:
			/* In der Tabelle nichts zu tun */
			break;
		case MODE_NEWLINE:
			/* In der Tabelle nichts zu tun */
			break;
		case MODE_EXPRESSION:
			line[i]=((ExpressionCalc)data[i]).getText();
			break;
		case MODE_CLIENT:
			line[i]=Language.tr("Simulation.Output.ClientTypeName");
			break;
		case MODE_WAITINGTIME_NUMBER:
		case MODE_WAITINGTIME_TIME:
			line[i]=Language.tr("Simulation.Output.WaitingTime");
			break;
		case MODE_TRANSFERTIME_NUMBER:
		case MODE_TRANSFERTIME_TIME:
			line[i]=Language.tr("Simulation.Output.TransferTime");
			break;
		case MODE_PROCESSTIME_NUMBER:
		case MODE_PROCESSTIME_TIME:
			line[i]=Language.tr("Simulation.Output.ProcessTime");
			break;
		case MODE_RESIDENCETIME_NUMBER:
		case MODE_RESIDENCETIME_TIME:
			line[i]=Language.tr("Simulation.Output.ResidenceTime");
			break;
		case MODE_STRING:
			line[i]=Language.tr("Simulation.Output.String")+" \""+(String)data[i]+"\"";
		default:
			break;
		}
		return line;
	}

	private void processOutput(final SimulationData simData, final RunDataClient client) {
		if (outputFile==null) return;
		if (outputWriter==null) {
			outputWriter=simData.runData.getOutputWriter(outputFile);
			if (tableMode) outputWriter.output(getOutputTableHeadings());
		}
		if (tableMode) {
			outputWriter.output(getOutputTableLine(simData,client));
		} else {
			outputWriter.output(getOutputString(simData,client));
		}
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Output"),String.format(Language.tr("Simulation.Log.Output.Info"),client.logInfo(simData),name));

		if (!client.isWarmUp && client.inStatistics) {
			/* Ausgabe durchführen */
			processOutput(simData,client);
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void finalCleanUp(final SimulationData simData) {
		if (outputWriter!=null) outputWriter.close();
	}
}