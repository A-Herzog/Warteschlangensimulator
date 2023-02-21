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

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import net.dde.DDEConnect;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementInput.AssignMode;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementInputDDE;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementInputDDE</code>
 * @author Alexander Herzog
 * @see ModelElementInputDDE
 */
public class RunElementInputDDE extends RunElementPassThrough {
	/**
	 * Verhalten beim Erreichen des Dateiendes
	 */
	private ModelElementInputDDE.EofModes mode;

	/**
	 * Vorgabewert (Zahl)
	 */
	private double defaultValue;

	/**
	 * Vorgabewert (Text)
	 */
	private String defaultText;

	/**
	 * Geladene Zahlen für die Modi
	 * {@link AssignMode#VARIABLE} und
	 * {@link AssignMode#CLIENT_NUMBER}
	 * @see AssignMode#VARIABLE
	 * @see AssignMode#CLIENT_NUMBER
	 */
	private double[] inputData;

	/**
	 * Geladene Zeichenkette für den Modus
	 * {@link AssignMode#CLIENT_TEXT}
	 * @see AssignMode#CLIENT_TEXT
	 */
	private String[] inputStrings;

	/**
	 * Was soll bei der Zuweisung passieren?
	 */
	private AssignMode assignMode;

	/**
	 * Bei einer Zuweisung an eine Variable
	 * (Modus {@link AssignMode#VARIABLE})
	 * wird hier der Index der Variable angegeben.
	 */
	private int variableIndex;

	/**
	 * Bei einer Zuweisung an ein Kundendatentextfeld
	 * (Modus {@link AssignMode#CLIENT_TEXT})
	 * wird hier der Schlüssel für die Zuweisung angegeben.
	 */
	private String key;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementInputDDE(final ModelElementInputDDE element) {
		super(element,buildName(element,Language.tr("Simulation.Element.InputDDE.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementInputDDE)) return null;
		final ModelElementInputDDE inputElement=(ModelElementInputDDE)element;
		final RunElementInputDDE input=new RunElementInputDDE(inputElement);

		/* Auslaufende Kante */
		final String edgeError=input.buildEdgeOut(inputElement);
		if (edgeError!=null) return edgeError;

		/* DDE im Allgemeinen */
		if (!new DDEConnect().available()) return String.format(Language.tr("Simulation.Creator.DDENotAvailable"),element.getId());

		/* Variable */
		final int clientDataIndex=CalcSymbolClientUserData.testClientData(inputElement.getVariable());
		if (clientDataIndex>=0) {
			/* Kundendatenfeld */
			input.assignMode=AssignMode.CLIENT_NUMBER;
			input.variableIndex=clientDataIndex;
		} else {
			final String clientString=CalcSymbolClientUserData.testClientDataString(inputElement.getVariable());
			if (clientString!=null) {
				input.assignMode=AssignMode.CLIENT_TEXT;
				input.key=clientString;
			} else {
				/* Variablen */
				int index=-1;
				for (int j=0;j<runModel.variableNames.length;j++) if (runModel.variableNames[j].equalsIgnoreCase(inputElement.getVariable())) {index=j; break;}
				if (index<0) return String.format(Language.tr("Simulation.Creator.SetInternalError"),element.getId());
				input.assignMode=AssignMode.VARIABLE;
				input.variableIndex=index;
			}
		}

		/* Modus */
		input.mode=inputElement.getEofMode();
		input.defaultText="";
		input.defaultValue=0;
		if (input.mode==ModelElementInputDDE.EofModes.EOF_MODE_DEFAULT_VALUE) {
			if (input.assignMode==AssignMode.CLIENT_TEXT) {
				input.defaultText=inputElement.getDefaultValue();
			} else {
				final Double D=NumberTools.getDouble(inputElement.getDefaultValue());
				if (D==null) return String.format(Language.tr("Simulation.Creator.InvalidNumberDefaultValue"),element.getId(),inputElement.getDefaultValue());
				input.defaultValue=D.doubleValue();
			}
		}

		/* Daten laden */
		if (!testOnly) {
			final String ddeError=input.loadData(inputElement);
			if (ddeError!=null) return ddeError;
		}

		return input;
	}

	/**
	 * Lädt die Daten über die DDE-Verbindung
	 * @param inputElement	Modell-Element dem die Verbindungsdaten entnommen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	private String loadData(final ModelElementInputDDE inputElement) {
		final DDEConnect connect=new DDEConnect();

		if (assignMode==AssignMode.CLIENT_TEXT) {
			inputStrings=connect.getStringsColumn(inputElement.getWorkbook().trim(),inputElement.getTable().trim(),inputElement.getStartRow()-1,Table.numberFromColumnName(inputElement.getColumn()));
			if (inputStrings==null || inputStrings.length==0) return String.format(Language.tr("Simulation.Creator.DDEError.NoRows"),inputElement.getId(),inputElement.getTable());

		} else {
			inputData=connect.getNumbersColumn(inputElement.getWorkbook().trim(),inputElement.getTable().trim(),inputElement.getStartRow()-1,Table.numberFromColumnName(inputElement.getColumn()));
			if (inputData==null || inputData.length==0) return String.format(Language.tr("Simulation.Creator.DDEError.NoRows"),inputElement.getId(),inputElement.getTable());
		}
		return null;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementInputDDE)) return null;
		final ModelElementInputDDE inputElement=(ModelElementInputDDE)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(inputElement);
		if (edgeError!=null) return edgeError;

		/* DDE im Allgemeinen */
		if (!new DDEConnect().available()) return RunModelCreatorStatus.noDDE(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementInputDDEData getData(final SimulationData simData) {
		RunElementInputDDEData data;
		data=(RunElementInputDDEData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementInputDDEData(this);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Führt die eigentliche Eingabe-Verarbeitung durch.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @return	Liefert <code>true</code>, wenn die Simulation fortgestzt werden soll, oder <code>false</code> für Simulationsende (wegen Datenende)
	 */
	private boolean processInput(final SimulationData simData, final RunDataClient client) {
		final RunElementInputDDEData data=getData(simData);

		/* Nächsten Wert ermitteln */
		double valueDouble=defaultValue;
		String valueString=defaultText;
		if (assignMode==AssignMode.CLIENT_TEXT) {
			if (data.position<inputStrings.length) {
				/* Weiterer Wert vorhanden */
				valueString=inputStrings[data.position];
				data.position++;
			} else {
				/* Dateiende */
				switch (mode) {
				case EOF_MODE_SKIP: return true; /* nix tun */
				case EOF_MODE_DEFAULT_VALUE: break; /* ist oben bereits gesetzt: valueString=defaultText; */
				case EOF_MODE_LOOP: valueString=inputStrings[0]; data.position=1; break; /* wieder von vorne */
				case EOF_MODE_TERMINATE: return false; /* Simulation abbrechen */
				}
			}
		} else {
			if (data.position<inputData.length) {
				/* Weiterer Wert vorhanden */
				valueDouble=inputData[data.position];
				data.position++;
			} else {
				/* Dateiende */
				switch (mode) {
				case EOF_MODE_SKIP: return true; /* nix tun */
				case EOF_MODE_DEFAULT_VALUE: break; /* ist oben bereits gesetzt: valueDouble=defaultValue; */
				case EOF_MODE_LOOP: valueDouble=inputData[0]; data.position=1; break; /* wieder von vorne */
				case EOF_MODE_TERMINATE: return false; /* Simulation abbrechen */
				}
			}
		}

		/* Wert zuweisen */
		switch (assignMode) {
		case CLIENT_NUMBER:
			/* Speichern als Kundendanten-Feld */
			client.setUserData(variableIndex,valueDouble);
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.InputDDE"),String.format(Language.tr("Simulation.Log.InputDDE.InfoClientData"),client.logInfo(simData),name,variableIndex,NumberTools.formatNumber(valueDouble)));
			break;
		case CLIENT_TEXT:
			/* Zuweisung an Schlüssel */
			client.setUserDataString(key,valueString);
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.InputDDE"),String.format(Language.tr("Simulation.Log.InputDDE.InfoClientDataString"),client.logInfo(simData),name,key,valueString));
			break;
		case VARIABLE:
			/* Speichern in Variable */
			boolean done=false;
			final int len=simData.runData.variableValues.length;
			if (variableIndex==len-3) {
				/* Pseudovariable: Wartezeit */
				client.waitingTime=FastMath.max(0,FastMath.round(valueDouble*1000));
				client.residenceTime=client.waitingTime+client.transferTime+client.processTime;
				done=true;
			}
			if (variableIndex==len-2) {
				/* Pseudovariable: Transferzeit */
				client.transferTime=FastMath.max(0,FastMath.round(valueDouble*1000));
				client.residenceTime=client.waitingTime+client.transferTime+client.processTime;
				done=true;
			}
			if (variableIndex==len-1) {
				/* Pseudovariable: Bedienzeit */
				client.processTime=FastMath.max(0,FastMath.round(valueDouble*1000));
				client.residenceTime=client.waitingTime+client.transferTime+client.processTime;
				done=true;
			}
			if (!done) {
				/* Reguläre Variable speichern */
				simData.runData.variableValues[variableIndex]=valueDouble;
				simData.runData.updateVariableValueForStatistics(simData,variableIndex);
			}
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.InputDDE"),String.format(Language.tr("Simulation.Log.InputDDE.Info"),client.logInfo(simData),name,simData.runModel.variableNames[variableIndex],NumberTools.formatNumber(valueDouble)));
			break;
		}

		return true;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Eingabe durchführen */
		if (!processInput(simData,client)) {
			/* Simulationsende am Dateiende */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.InputDDE"),String.format(Language.tr("Simulation.Log.InputDDE.TerminateSimulation"),name));
			simData.eventManager.deleteAllEvents();
			simData.runData.stopp=true;
			return;
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
