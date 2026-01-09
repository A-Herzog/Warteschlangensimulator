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
import mathtools.Table;
import mathtools.TimeTools;
import parser.MathCalcError;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunData;
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
	/** Ausgabe aktiv? */
	private boolean outputActive;
	/** Gibt an, ob die Ausgabe zeilenweise (<code>false</code>) oder über ein {@link Table}-Objekt (<code>true</code>) erfolgen soll */
	private boolean tableMode;
	/** Zahlen im lokalen Format (<code>false</code>) oder im System-Format (<code>true</code>) ausgeben? */
	private boolean systemFormat;
	/** Dateiname der Datei für die Ausgaben */
	private File outputFile;
	/** Wenn die Ausgabedatei schon besteht, soll diese überschrieben werden (anstatt Daten anzuhängen)? */
	private boolean outputFileOverwrite;
	/** System zur gepufferten Dateiausgabe ({@link RunData#getOutputWriter(File, boolean)}) */
	private RunDataOutputWriter outputWriter;
	/** Wie sollen Überschriften ausgegeben werden? */
	private ModelElementOutput.HeadingMode headingMode;
	/** Liste der Ausgabeelemente für die Überschrift (nur im Modus nutzerdefinierter Überschriften) */
	private ModelElementOutput.OutputRecord[] outputHeadingRecord;
	/** Liste der Ausgabeelemente */
	private ModelElementOutput.OutputRecord[] outputRecord;

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

		Object list;

		/* Auslaufende Kante */
		final String edgeError=output.buildEdgeOut(outputElement);
		if (edgeError!=null) return edgeError;

		/* Ausgabe aktiv? */
		output.outputActive=outputElement.isOutputActive();

		/* Ausgabedatei */
		if (outputElement.getOutputFile().isBlank()) return String.format(Language.tr("Simulation.Creator.NoOutputFile"),element.getId());
		output.outputFile=outputElement.getOutputFileWithFullPath(runModel.modelPath);
		output.outputFileOverwrite=outputElement.isOutputFileOverwrite();

		/* Ausgaben */
		output.tableMode=isTable(output.outputFile);
		final String outputFileLower=outputElement.getOutputFile().toLowerCase();
		if (outputFileLower.endsWith(".txt") || outputFileLower.endsWith(".tsv")) {
			output.systemFormat=outputElement.isSystemFormat();
		} else {
			output.systemFormat=false;
		}

		/* Überschriften */
		output.headingMode=outputElement.getHeadingMode();
		if (output.headingMode==ModelElementOutput.HeadingMode.USER_DEFINED) {
			list=buildOutputRecords(outputElement.getOutputHeading(),output,runModel);
			if (list instanceof String) return list;
			output.outputHeadingRecord=(ModelElementOutput.OutputRecord[])list;
		}

		/* Ausgabeelemente */
		list=buildOutputRecords(outputElement.getOutput(),output,runModel);
		if (list instanceof String) return list;
		output.outputRecord=(ModelElementOutput.OutputRecord[])list;

		return output;
	}

	/**
	 * Erstellt eine Liste mit den Ausgaben
	 * @param outputList	Einzulesende und zu verarbeitende Liste mit Ausgaben
	 * @param output	Ausgabe-Laufzeit-Element
	 * @param runModel	Laufzeit-Modell
	 * @return	Array mit den Ausgaben oder im Fehlerfall eine Fehlermeldung
	 */
	private Object buildOutputRecords(List<ModelElementOutput.OutputRecord> outputList, final RunElementOutput output, final RunModel runModel) {
		final List<ModelElementOutput.OutputRecord> results=new ArrayList<>();

		for (int i=0;i<outputList.size();i++) {
			final ModelElementOutput.OutputMode mode=outputList.get(i).mode;

			if (output.tableMode) {
				if (mode==ModelElementOutput.OutputMode.MODE_TABULATOR || mode==ModelElementOutput.OutputMode.MODE_NEWLINE) continue;
			}

			final String s=outputList.get(i).data;

			if (mode==ModelElementOutput.OutputMode.MODE_TEXT || mode==ModelElementOutput.OutputMode.MODE_STRING) {
				results.add(new ModelElementOutput.OutputRecord(mode,s));
				continue;
			}

			if (mode==ModelElementOutput.OutputMode.MODE_EXPRESSION) {
				final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames,runModel.modelUserFunctions);
				final int error=calc.parse(s);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidOutputExpression"),i+1,output.id,error+1);
				results.add(new ModelElementOutput.OutputRecord(calc));
				continue;
			}

			results.add(new ModelElementOutput.OutputRecord(mode,""));
		}

		return results.toArray(ModelElementOutput.OutputRecord[]::new);
	}

	/**
	 * Prüft, ob die Ausgabe auf Basis des Dateinamens über ein
	 * {@link Table}-Objekt (d.h. am Ende am Stück) erfolgen soll.
	 * @param file	Ausgabedatei
	 * @return	Liefert <code>true</code>, wenn ein {@link Table}-Objekt für die Ausgabe verwendet werden soll
	 * @see #tableMode
	 */
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
		if (outputElement.getOutputFile().isBlank()) return RunModelCreatorStatus.noOutputFile(element);

		return RunModelCreatorStatus.ok;
	}

	/**
	 * Liefert eine Textzeile als Ausgabe.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @param outputRecord	Zu verwendende Liste mit den Ausgabeelementen
	 * @return	Tabellenzeile
	 * @see #processOutput(SimulationData, RunDataClient)
	 */
	private String getOutputString(final SimulationData simData, final RunDataClient client, final ModelElementOutput.OutputRecord[] outputRecord) {
		double number;
		final StringBuilder sb=new StringBuilder();
		for (int i=0;i<outputRecord.length;i++) switch (outputRecord[i].mode) {
		case MODE_TIMESTAMP:
			sb.append(systemFormat?simData.formatScaledSimTimeSystem(simData.currentTime):simData.formatScaledSimTime(simData.currentTime));
			break;
		case MODE_TEXT:
			sb.append(outputRecord[i].data);
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
				number=outputRecord[i].calc.calc(simData.runData.variableValues,simData,client);
				sb.append(systemFormat?NumberTools.formatSystemNumber(number):NumberTools.formatNumberMax(number));
			} catch (MathCalcError e) {
				simData.calculationErrorStation(outputRecord[i].calc,this);
				sb.append('0');
			}
			break;
		case MODE_CLIENT:
			sb.append(simData.runModel.clientTypes[client.type]);
			break;
		case MODE_WAITINGTIME_NUMBER:
			number=client.waitingTime*simData.runModel.scaleToSeconds;
			sb.append(systemFormat?NumberTools.formatSystemNumber(number):NumberTools.formatNumberMax(number));
			break;
		case MODE_WAITINGTIME_TIME:
			number=client.waitingTime*simData.runModel.scaleToSeconds;
			sb.append(systemFormat?TimeTools.formatExactSystemTime(number):TimeTools.formatExactTime(number));
			break;
		case MODE_TRANSFERTIME_NUMBER:
			number=client.transferTime*simData.runModel.scaleToSeconds;
			sb.append(systemFormat?NumberTools.formatSystemNumber(number):NumberTools.formatNumberMax(number));
			break;
		case MODE_TRANSFERTIME_TIME:
			number=client.transferTime*simData.runModel.scaleToSeconds;
			sb.append(systemFormat?TimeTools.formatExactSystemTime(number):TimeTools.formatExactTime(number));
			break;
		case MODE_PROCESSTIME_NUMBER:
			number=client.processTime*simData.runModel.scaleToSeconds;
			sb.append(systemFormat?NumberTools.formatSystemNumber(number):NumberTools.formatNumberMax(number));
			break;
		case MODE_PROCESSTIME_TIME:
			number=client.processTime*simData.runModel.scaleToSeconds;
			sb.append(systemFormat?TimeTools.formatExactSystemTime(number):TimeTools.formatExactTime(number));
			break;
		case MODE_RESIDENCETIME_NUMBER:
			number=client.residenceTime*simData.runModel.scaleToSeconds;
			sb.append(systemFormat?NumberTools.formatSystemNumber(number):NumberTools.formatNumberMax(number));
			break;
		case MODE_RESIDENCETIME_TIME:
			number=client.residenceTime*simData.runModel.scaleToSeconds;
			sb.append(systemFormat?TimeTools.formatExactSystemTime(number):TimeTools.formatExactTime(number));
			break;
		case MODE_STRING:
			sb.append(client.getUserDataString(outputRecord[i].data));
			break;
		}
		return sb.toString();
	}

	/**
	 * Liefert eine Tabellenzeile als Ausgabe.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @param outputRecord	Zu verwendende Liste mit den Ausgabeelementen
	 * @return	Tabellenzeile
	 * @see #processOutput(SimulationData, RunDataClient)
	 */
	private String[] getOutputTableLine(final SimulationData simData, final RunDataClient client, final ModelElementOutput.OutputRecord[] outputRecord) {
		final String[] line=new String[outputRecord.length];
		for (int i=0;i<outputRecord.length;i++) switch (outputRecord[i].mode) {
		case MODE_TIMESTAMP:
			line[i]=simData.formatScaledSimTime(simData.currentTime);
			break;
		case MODE_TEXT:
			line[i]=outputRecord[i].data;
			break;
		case MODE_TABULATOR:
			/* In der Tabelle nichts zu tun */
			break;
		case MODE_NEWLINE:
			/* In der Tabelle nichts zu tun */
			break;
		case MODE_EXPRESSION:
			simData.runData.setClientVariableValues(client);
			try {
				line[i]=NumberTools.formatNumberMax(outputRecord[i].calc.calc(simData.runData.variableValues,simData,client));
			} catch (MathCalcError e) {
				simData.calculationErrorStation(outputRecord[i].calc,this);
				line[i]=NumberTools.formatNumberMax(0);
			}
			break;
		case MODE_CLIENT:
			line[i]=simData.runModel.clientTypes[client.type];
			break;
		case MODE_WAITINGTIME_NUMBER:
			line[i]=NumberTools.formatNumberMax(client.waitingTime*simData.runModel.scaleToSeconds);
			break;
		case MODE_WAITINGTIME_TIME:
			line[i]=TimeTools.formatExactTime(client.waitingTime*simData.runModel.scaleToSeconds);
			break;
		case MODE_TRANSFERTIME_NUMBER:
			line[i]=NumberTools.formatNumberMax(client.transferTime*simData.runModel.scaleToSeconds);
			break;
		case MODE_TRANSFERTIME_TIME:
			line[i]=TimeTools.formatExactTime(client.transferTime*simData.runModel.scaleToSeconds);
			break;
		case MODE_PROCESSTIME_NUMBER:
			line[i]=NumberTools.formatNumberMax(client.processTime*simData.runModel.scaleToSeconds);
			break;
		case MODE_PROCESSTIME_TIME:
			line[i]=TimeTools.formatExactTime(client.processTime*simData.runModel.scaleToSeconds);
			break;
		case MODE_RESIDENCETIME_NUMBER:
			line[i]=NumberTools.formatNumberMax(client.residenceTime*simData.runModel.scaleToSeconds);
			break;
		case MODE_RESIDENCETIME_TIME:
			line[i]=TimeTools.formatExactTime(client.residenceTime*simData.runModel.scaleToSeconds);
			break;
		case MODE_STRING:
			line[i]=client.getUserDataString(outputRecord[i].data);
			break;
		default:
			break;
		}
		return line;
	}

	/**
	 * Liefert die Überschriften für die Tabellenausgabe.
	 * @return Überschriften für die Tabellenausgabe
	 */
	private String[] getOutputTableHeadings() {
		final String[] line=new String[outputRecord.length];
		for (int i=0;i<outputRecord.length;i++) switch (outputRecord[i].mode) {
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
			line[i]=outputRecord[i].data;
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
			line[i]=Language.tr("Simulation.Output.String")+" \""+outputRecord[i].data+"\"";
			break;
		default:
			break;
		}
		return line;
	}

	/**
	 * Führt die eigentliche Ausgabe-Verarbeitung durch.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 */
	private void processOutput(final SimulationData simData, final RunDataClient client) {
		if (outputFile==null) return;
		if (outputWriter==null) {
			outputWriter=simData.runData.getOutputWriter(outputFile,outputFileOverwrite);
			switch (headingMode) {
			case OFF:
				/* Nichts ausgeben */
				break;
			case AUTO:
				if (tableMode) outputWriter.output(getOutputTableHeadings());
				break;
			case USER_DEFINED:
				if (tableMode) {
					outputWriter.output(getOutputTableLine(simData,client,outputHeadingRecord));
				} else {
					outputWriter.output(getOutputString(simData,client,outputHeadingRecord));
				}
				break;
			}
		}
		if (tableMode) {
			outputWriter.output(getOutputTableLine(simData,client,outputRecord));
		} else {
			outputWriter.output(getOutputString(simData,client,outputRecord));
		}
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Output"),String.format(Language.tr("Simulation.Log.Output.Info"),client.logInfo(simData),name));

		if (!client.isWarmUp && client.inStatistics && outputActive) {
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