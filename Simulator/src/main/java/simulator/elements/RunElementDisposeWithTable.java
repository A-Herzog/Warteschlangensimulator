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

import language.Language;
import mathtools.NumberTools;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataOutputWriter;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDisposeWithTable;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementDisposeWithTable}
 * @author Alexander Herzog
 * @see ModelElementDisposeWithTable
 */
public class RunElementDisposeWithTable extends RunElement {
	/**
	 * Soll die Simulation abgebrochen werden, wenn an dieser Station ein Kunde eintrifft?
	 * @see #processArrival(SimulationData, RunDataClient)
	 */
	private boolean stoppSimulationOnClientArrival;

	/**
	 * Tabellendatei zum Speichern der Kunden
	 */
	private File clientsOutputTable;

	/**
	 * Sollen die Daten Tabulator-getrennt (<code>false</code>) oder im csv-Format (<code>true</code>) gespeichert werden?
	 */
	private boolean csvMode;

	/**
	 * System zur gepufferten Dateiausgabe ({@link RunData#getOutputWriter(File, boolean)})
	 */
	private RunDataOutputWriter outputWriter;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementDisposeWithTable(final ModelElementDisposeWithTable element) {
		super(element,buildName(element,Language.tr("Simulation.Element.DisposeWithTable.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDisposeWithTable)) return null;
		final ModelElementDisposeWithTable disposeElement=(ModelElementDisposeWithTable)element;
		final RunElementDisposeWithTable dispose=new RunElementDisposeWithTable(disposeElement);

		/* Soll die Simulation abgebrochen werden, wenn an dieser Station ein Kunde eintrifft? */
		dispose.stoppSimulationOnClientArrival=disposeElement.isStoppSimulationOnClientArrival();

		/* Tabellendatei zum Speichern der Kunden */
		final String tableFileName=disposeElement.getOutputFile();

		if (tableFileName==null || tableFileName.trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoOutputFile"),element.getId());
		dispose.clientsOutputTable=new File(tableFileName.trim());

		dispose.csvMode=tableFileName.trim().toLowerCase().endsWith(".csv");

		return dispose;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementDisposeWithTable)) return null;

		final ModelElementDisposeWithTable dispose=(ModelElementDisposeWithTable)element;

		if (dispose.getOutputFile().trim().isEmpty()) return RunModelCreatorStatus.noOutputFile(element);

		return RunModelCreatorStatus.ok;
	}

	/**
	 * Wandelt eine Zelle in einen gültigen csv-Wert um.
	 * @param cell	Inhalt der Zelle
	 * @return	Inhalt der Zelle in einen csv-Zell-Rahmen verpackt
	 */
	private String csvEscape(String cell) {
		cell=cell.replace("\"","\"\"");
		if (cell.indexOf('"')!=-1 || cell.indexOf(";")!=-1) cell='"'+cell+'"';
		if (cell.equalsIgnoreCase("id")) cell='"'+cell+'"';
		return cell;
	}

	/**
	 * Gibt eine Kunden-Zeile im csv-Modus aus.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @return	Auszugebende Zeile
	 * @see #processOutput(SimulationData, RunDataClient)
	 */
	private String getOutputTableLine(final SimulationData simData, final RunDataClient client) {
		final StringBuilder result=new StringBuilder();
		/* Zeitpunkt */
		result.append(NumberTools.formatNumberMax(simData.currentTime*simData.runModel.scaleToSeconds));

		/* Kundentyp */
		result.append(";");
		result.append(csvEscape(simData.runModel.clientTypes[client.type]));

		/* Numerische Kundendaten */
		final int maxIndex=client.getMaxUserDataIndex();
		for (int i=0;i<=maxIndex;i++) {
			final double value=client.getUserData(i);
			if (value!=0.0) {
				result.append(";");
				result.append("ClientData(");
				result.append(i);
				result.append(")=");
				result.append(NumberTools.formatNumberMax(value));
			}
		}

		/* Text Kundendaten */
		for (String key: client.getUserDataStringKeys()) {
			final String value=client.getUserDataString(key);
			if (value==null || value.isEmpty()) continue;
			result.append(";");
			result.append(csvEscape("ClientData('"+key+"')="+value));
		}

		/* Zeiten */
		if (client.waitingTime>0) {
			result.append(";");
			result.append("w=");
			result.append(NumberTools.formatNumberMax(client.waitingTime*simData.runModel.scaleToSeconds));
		}
		if (client.transferTime>0) {
			result.append(";");
			result.append("t=");
			result.append(NumberTools.formatNumberMax(client.transferTime*simData.runModel.scaleToSeconds));
		}
		if (client.processTime>0) {
			result.append(";");
			result.append("p=");
			result.append(NumberTools.formatNumberMax(client.processTime*simData.runModel.scaleToSeconds));
		}

		/* Kosten */
		if (client.waitingAdditionalCosts>0) {
			result.append(";");
			result.append("wCosts=");
			result.append(NumberTools.formatNumberMax(client.waitingAdditionalCosts));
		}
		if (client.transferAdditionalCosts>0) {
			result.append(";");
			result.append("tCosts=");
			result.append(NumberTools.formatNumberMax(client.transferAdditionalCosts));
		}
		if (client.processAdditionalCosts>0) {
			result.append(";");
			result.append("pCosts=");
			result.append(NumberTools.formatNumberMax(client.processAdditionalCosts));
		}

		result.append("\n");

		return result.toString();
	}

	/**
	 * Gibt eine Kunden-Zeile im Tabulator-Modus aus.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @return	Auszugebende Zeile
	 * @see #processOutput(SimulationData, RunDataClient)
	 */
	private String getOutputString(final SimulationData simData, final RunDataClient client) {
		final StringBuilder result=new StringBuilder();
		/* Zeitpunkt */
		result.append(NumberTools.formatNumberMax(simData.currentTime*simData.runModel.scaleToSeconds));

		/* Kundentyp */
		result.append("\t");
		result.append(simData.runModel.clientTypes[client.type]);

		/* Numerische Kundendaten */
		final int maxIndex=client.getMaxUserDataIndex();
		for (int i=0;i<=maxIndex;i++) {
			final double value=client.getUserData(i);
			if (value!=0.0) {
				result.append("\t");
				result.append("ClientData(");
				result.append(i);
				result.append(")=");
				result.append(NumberTools.formatNumberMax(value));
			}
		}

		/* Text Kundendaten */
		for (String key: client.getUserDataStringKeys()) {
			final String value=client.getUserDataString(key);
			if (value==null || value.isEmpty()) continue;
			result.append("\t");
			result.append("ClientData('");
			result.append(key);
			result.append("')=");
			result.append(value);
		}

		/* Zeiten */
		if (client.waitingTime>0) {
			result.append("\t");
			result.append("w=");
			result.append(NumberTools.formatNumberMax(client.waitingTime*simData.runModel.scaleToSeconds));
		}
		if (client.transferTime>0) {
			result.append("\t");
			result.append("t=");
			result.append(NumberTools.formatNumberMax(client.transferTime*simData.runModel.scaleToSeconds));
		}
		if (client.processTime>0) {
			result.append("\t");
			result.append("p=");
			result.append(NumberTools.formatNumberMax(client.processTime*simData.runModel.scaleToSeconds));
		}

		/* Kosten */
		if (client.waitingAdditionalCosts>0) {
			result.append("\t");
			result.append("wCosts=");
			result.append(NumberTools.formatNumberMax(client.waitingAdditionalCosts));
		}
		if (client.transferAdditionalCosts>0) {
			result.append("\t");
			result.append("tCosts=");
			result.append(NumberTools.formatNumberMax(client.transferAdditionalCosts));
		}
		if (client.processAdditionalCosts>0) {
			result.append("\t");
			result.append("pCosts=");
			result.append(NumberTools.formatNumberMax(client.processAdditionalCosts));
		}

		result.append("\n");

		return result.toString();
	}

	/**
	 * Führt die eigentliche Ausgabe-Verarbeitung durch.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 */
	private void processOutput(final SimulationData simData, final RunDataClient client) {
		if (clientsOutputTable==null) return;
		if (outputWriter==null) {
			outputWriter=simData.runData.getOutputWriter(clientsOutputTable,false);
		}

		if (csvMode) {
			outputWriter.output(getOutputTableLine(simData,client));
		} else {
			outputWriter.output(getOutputString(simData,client));
		}
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Dispose"),String.format(Language.tr("Simulation.Log.Dispose.Info"),client.logInfo(simData),name));

		/* Kunde in Tabelle speichern? */
		if (!client.isWarmUp && client.inStatistics) {
			processOutput(simData,client);
		}

		/* Ggf. Kunde aus Untermodell austragen */
		if (parentId>=0) simData.runData.logClientLeavesStation(simData,simData.runModel.elementsFast[parentId],null,client);

		/* Simulation regulär beenden */
		boolean lastClient=false;
		if (client.isLastClient) {
			lastClient=true;
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.EndOfSimulation"),Language.tr("Simulation.Log.EndOfSimulation.FinalClientLeftSystem"));
			simData.doShutDown();
		}

		/* Notausgangsstation - Simulation abbrechen */
		if (stoppSimulationOnClientArrival) {
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.EndOfSimulation"),Language.tr("Simulation.Log.EndOfSimulation.ClientAtStoppStation"));
			simData.doShutDown();
		}

		/* Notify-System über Kundenausgang informieren */
		client.lastStationID=id;
		client.nextStationID=-1;
		simData.runData.fireClientMoveNotify(simData,client,false);

		/* Kunde in Statistik erfassen und Objekt recyceln */
		simData.runData.clients.disposeClient(client,simData);

		/* Falls zwischenzeitlich doch noch weitere Ereignisse generiert wurden. */
		if (lastClient) {
			simData.doShutDown();
		}
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		/* Wird nie aufgerufen: Dispose-Elemente haben keine auslaufenden Kanten. */
	}

	@Override
	public boolean isClientCountStation() {
		return false;
	}

	@Override
	public void finalCleanUp(final SimulationData simData) {
		if (outputWriter!=null) outputWriter.close();
	}
}
