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
import parser.MathCalcError;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementOutputLog;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementOutputLog</code>
 * @author Alexander Herzog
 * @see ModelElementOutputLog
 */
public class RunElementOutputLog extends RunElementPassThrough {
	/** Ausgabe aktiv? */
	private boolean outputActive;
	/** Liste mit den Modi der Ausgabeelemente */
	private ModelElementOutputLog.OutputMode[] mode;
	/** Zusätzliche Daten zu den jeweiligen Ausgabe-Datensätzen in {@link #mode} */
	private Object[] data;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementOutputLog(final ModelElementOutputLog element) {
		super(element,buildName(element,Language.tr("Simulation.Element.OutputLog.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementOutputLog)) return null;
		final ModelElementOutputLog outputElement=(ModelElementOutputLog)element;
		final RunElementOutputLog output=new RunElementOutputLog(outputElement);

		/* Auslaufende Kante */
		final String edgeError=output.buildEdgeOut(outputElement);
		if (edgeError!=null) return edgeError;

		/* Ausgabe aktiv? */
		output.outputActive=outputElement.isOutputActive();

		/* Ausgaben */
		final List<ModelElementOutputLog.OutputMode> modeList=outputElement.getModes();
		final List<String> dataList=outputElement.getData();
		final List<ModelElementOutputLog.OutputMode> modeOutputList=new ArrayList<>();
		final List<Object> dataOutputList=new ArrayList<>();

		for (int i=0;i<modeList.size();i++) {
			final ModelElementOutputLog.OutputMode mode=modeList.get(i);
			Object data=null;

			final String s=(dataList.size()>i)?dataList.get(i):"";

			if (mode==ModelElementOutputLog.OutputMode.MODE_TEXT || mode==ModelElementOutputLog.OutputMode.MODE_STRING) {
				data=s;
			}

			if (mode==ModelElementOutputLog.OutputMode.MODE_EXPRESSION) {
				final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames,runModel.modelUserFunctions);
				final int error=calc.parse(s);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidOutputExpression"),i+1,output.id,error+1);
				data=calc;
			}

			modeOutputList.add(mode);
			dataOutputList.add(data);
		}
		output.mode=modeOutputList.toArray(ModelElementOutputLog.OutputMode[]::new);
		output.data=dataOutputList.toArray(Object[]::new);

		return output;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementOutputLog)) return null;
		final ModelElementOutputLog outputElement=(ModelElementOutputLog)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(outputElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	/**
	 * Liefert eine Textzeile als Ausgabe.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @return	Tabellenzeile
	 * @see #processOutput(SimulationData, RunDataClient)
	 */
	private String getOutputString(final SimulationData simData, final RunDataClient client) {
		final StringBuilder sb=new StringBuilder();
		for (int i=0;i<mode.length;i++) switch (mode[i]) {
		case MODE_TIMESTAMP:
			sb.append(simData.formatScaledSimTime(simData.currentTime));
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
			try {
				sb.append(NumberTools.formatNumberMax(((ExpressionCalc)data[i]).calc(simData.runData.variableValues,simData,client)));
			} catch (MathCalcError e) {
				simData.calculationErrorStation((ExpressionCalc)data[i],this);
				sb.append(NumberTools.formatNumberMax(0));
			}
			break;
		case MODE_CLIENT:
			sb.append(simData.runModel.clientTypes[client.type]);
			break;
		case MODE_WAITINGTIME_NUMBER:
			sb.append(NumberTools.formatNumberMax(client.waitingTime*simData.runModel.scaleToSeconds));
			break;
		case MODE_WAITINGTIME_TIME:
			sb.append(TimeTools.formatExactTime(client.waitingTime*simData.runModel.scaleToSeconds));
			break;
		case MODE_TRANSFERTIME_NUMBER:
			sb.append(NumberTools.formatNumberMax(client.transferTime*simData.runModel.scaleToSeconds));
			break;
		case MODE_TRANSFERTIME_TIME:
			sb.append(TimeTools.formatExactTime(client.transferTime*simData.runModel.scaleToSeconds));
			break;
		case MODE_PROCESSTIME_NUMBER:
			sb.append(NumberTools.formatNumberMax(client.processTime*simData.runModel.scaleToSeconds));
			break;
		case MODE_PROCESSTIME_TIME:
			sb.append(TimeTools.formatExactTime(client.processTime*simData.runModel.scaleToSeconds));
			break;
		case MODE_RESIDENCETIME_NUMBER:
			sb.append(NumberTools.formatNumberMax(client.residenceTime*simData.runModel.scaleToSeconds));
			break;
		case MODE_RESIDENCETIME_TIME:
			sb.append(TimeTools.formatExactTime(client.residenceTime*simData.runModel.scaleToSeconds));
			break;
		case MODE_STRING:
			sb.append(client.getUserDataString((String)data[i]));
			break;
		}
		return sb.toString();
	}

	/**
	 * Führt die eigentliche Ausgabe-Verarbeitung durch.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 */
	private void processOutput(final SimulationData simData, final RunDataClient client) {
		for(String line: getOutputString(simData,client).split("\n")) {
			log(simData,Language.tr("Simulation.Log.OutputLog"),line);
		}
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (mode.length==0) {
			/* Nur als Fallback, wenn keine Logging-Ausgaben definiert sind. */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.OutputLog"),String.format(Language.tr("Simulation.Log.OutputLog.Info"),client.logInfo(simData),name));
		}

		if (!client.isWarmUp && client.inStatistics && outputActive) {
			/* Ausgabe durchführen */
			if (simData.loggingActive) processOutput(simData,client);
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}