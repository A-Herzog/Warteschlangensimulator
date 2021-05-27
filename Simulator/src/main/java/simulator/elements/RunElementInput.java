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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementInput;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementInput</code>
 * @author Alexander Herzog
 * @see ModelElementInput
 */
public class RunElementInput extends RunElementPassThrough {
	/**
	 * Was soll bei der Zuweisung passieren?
	 * @author Alexander Herzog
	 */
	public enum AssignMode {
		/** Eingelesenen Wert an normale Variable zuweisen */
		VARIABLE,
		/** Eingelesenen Wert an numerisches Kundendatenfeld zuweisen */
		CLIENT_NUMBER,
		/** Eingelesenen Zeichenkette an textbasierendes Kundendatenfeld zuweisen */
		CLIENT_TEXT
	}

	/**
	 * Verhalten beim Erreichen des Dateiendes
	 */
	private ModelElementInput.EofModes mode;

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
	public RunElementInput(final ModelElementInput element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Input.Name")));
	}

	/**
	 * Versucht eine Reihe von <code>double</code>-Werten aus einer einfachen Textdatei zu laden.
	 * @param file	Zu ladende Datei
	 * @return	Array mit <code>double</code>-Werten oder im Fehlerfall <code>null</code>
	 * @see #loadDoubleData(File)
	 */
	private static double[] loadDoubleSimpleText(final File file) {
		double[] buffer=new double[10];
		int size=0;

		final List<String> lines=Table.loadTextLinesFromFile(file);
		if (lines==null) return null;

		for (String line: lines) {
			line=line.trim();
			if (line.isEmpty()) continue;
			final Double D=NumberTools.getDouble(line);
			if (D==null) continue;

			if (size==buffer.length) buffer=Arrays.copyOf(buffer,buffer.length*3/2);
			buffer[size]=D.doubleValue();
			size++;
		}

		return Arrays.copyOf(buffer,size);
	}

	/**
	 * Versucht eine Reihe von <code>double</code>-Werten aus einer komplexen Tabellendatei zu laden.
	 * @param file	Zu ladende Datei
	 * @return	Array mit <code>double</code>-Werten oder im Fehlerfall <code>null</code>
	 * @see #loadDoubleData(File)
	 */
	private static double[] loadDoubleTable(final File file) {
		final Table table=new Table();
		if (!table.load(file)) return null;

		double[] buffer=new double[10];
		int size=0;

		for (int i=0;i<table.getSize(0);i++) {
			final List<String> line=table.getLine(i);
			if (line.size()!=1) continue;
			final Double D=NumberTools.getDouble(line.get(0));
			if (D==null) continue;

			if (size==buffer.length) buffer=Arrays.copyOf(buffer,buffer.length*3/2);
			buffer[size]=D.doubleValue();
			size++;
		}

		return Arrays.copyOf(buffer,size);
	}

	/**
	 * Versucht eine Reihe von <code>double</code>-Werten aus einer Datei zu laden.
	 * @param file	Zu ladende Datei
	 * @return	Array mit <code>double</code>-Werten oder im Fehlerfall <code>null</code>
	 */
	public static double[] loadDoubleData(final File file) {
		final Table.SaveMode saveMode=Table.getSaveModeFromFileName(file,true,false);
		if (saveMode!=Table.SaveMode.SAVEMODE_TABS) {
			final double[] result=loadDoubleTable(file);
			if (result!=null) return result;
		}
		return loadDoubleSimpleText(file);
	}

	/**
	 * Versucht eine Reihe von Zeichenketten aus einer einfachen Textdatei zu laden.
	 * @param file	Zu ladende Datei
	 * @return	Array mit {@link String}-Werten oder im Fehlerfall <code>null</code>
	 * @see #loadStringData(File)
	 */
	private static String[] loadStringSimpleText(final File file) {
		final List<String> buffer=new ArrayList<>();

		final List<String> lines=Table.loadTextLinesFromFile(file);
		if (lines==null) return null;

		for (String line: lines) {
			line=line.trim();
			if (line.isEmpty()) continue;
			buffer.add(line);
		}

		return buffer.toArray(new String[0]);
	}

	/**
	 * Versucht eine Reihe von Zeichenketten aus einer komplexen Tabellendatei zu laden.
	 * @param file	Zu ladende Datei
	 * @return	Array mit {@link String}-Werten oder im Fehlerfall <code>null</code>
	 * @see #loadStringData(File)
	 */
	private static String[] loadStringTable(final File file) {
		final Table table=new Table();
		if (!table.load(file)) return null;

		final int size=table.getSize(0);
		final List<String> buffer=new ArrayList<>(size);

		for (int i=0;i<size;i++) {
			final List<String> line=table.getLine(i);
			if (line.size()!=1) continue;
			buffer.add(line.get(0));
		}

		return buffer.toArray(new String[0]);
	}

	/**
	 * Versucht eine Reihe von Zeichenketten aus einer Datei zu laden.
	 * @param file	Zu ladende Datei
	 * @return	Array mit {@link String}-Werten oder im Fehlerfall <code>null</code>
	 */
	public static String[] loadStringData(final File file) {
		final Table.SaveMode saveMode=Table.getSaveModeFromFileName(file,true,false);
		if (saveMode!=Table.SaveMode.SAVEMODE_TABS) {
			final String[] result=loadStringTable(file);
			if (result!=null) return result;
		}
		return loadStringSimpleText(file);
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementInput)) return null;
		final ModelElementInput inputElement=(ModelElementInput)element;
		final RunElementInput input=new RunElementInput(inputElement);

		/* Auslaufende Kante */
		final String edgeError=input.buildEdgeOut(inputElement);
		if (edgeError!=null) return edgeError;

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
		if (input.mode==ModelElementInput.EofModes.EOF_MODE_DEFAULT_VALUE) {
			if (input.assignMode==AssignMode.CLIENT_TEXT) {
				input.defaultText=inputElement.getDefaultValue();
			} else {
				final Double D=NumberTools.getDouble(inputElement.getDefaultValue());
				if (D==null) return String.format(Language.tr("Simulation.Creator.InvalidNumberDefaultValue"),element.getId(),inputElement.getDefaultValue());
				input.defaultValue=D.doubleValue();
			}
		}

		/* Eingabedatei */
		if (inputElement.getInputFile().trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoInputFile"),element.getId());
		final File inputFile=new File(inputElement.getInputFile());
		if (!testOnly) {
			if (input.assignMode==AssignMode.CLIENT_TEXT) {
				input.inputStrings=loadStringData(inputFile);
				if (input.inputStrings==null || input.inputStrings.length==0) return String.format(Language.tr("Simulation.Creator.NoInputData"),element.getId(),inputFile.toString());
			} else {
				input.inputData=loadDoubleData(inputFile);
				if (input.inputData==null || input.inputData.length==0) return String.format(Language.tr("Simulation.Creator.NoInputData"),element.getId(),inputFile.toString());
			}
		}

		return input;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementInput)) return null;
		final ModelElementInput inputElement=(ModelElementInput)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(inputElement);
		if (edgeError!=null) return edgeError;

		/* Eingabedatei */
		if (inputElement.getInputFile().trim().isEmpty()) return RunModelCreatorStatus.noInputFile(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementInputData getData(final SimulationData simData) {
		RunElementInputData data;
		data=(RunElementInputData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementInputData(this);
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
		final RunElementInputData data=getData(simData);

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
			/* Speichern als Kundendaten-Feld */
			client.setUserData(variableIndex,valueDouble);
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Input"),String.format(Language.tr("Simulation.Log.Input.InfoClientData"),client.logInfo(simData),name,variableIndex,NumberTools.formatNumber(valueDouble)));
			break;
		case CLIENT_TEXT:
			/* Zuweisung an Schlüssel */
			client.setUserDataString(key,valueString);
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Input"),String.format(Language.tr("Simulation.Log.Input.InfoClientDataString"),client.logInfo(simData),name,key,valueString));
			break;
		case VARIABLE:
			/* Speichern in Variable */
			boolean done=false;
			final int len=simData.runData.variableValues.length;
			if (variableIndex==len-3) {
				/* Pseudovariable: Wartezeit */
				client.waitingTime=FastMath.max(0,FastMath.round(valueDouble*1000));
				done=true;
			}
			if (variableIndex==len-2) {
				/* Pseudovariable: Transferzeit */
				client.transferTime=FastMath.max(0,FastMath.round(valueDouble*1000));
				done=true;
			}
			if (variableIndex==len-1) {
				/* Pseudovariable: Bedienzeit */
				client.processTime=FastMath.max(0,FastMath.round(valueDouble*1000));
				done=true;
			}
			if (!done) {
				/* Reguläre Variable speichern */
				simData.runData.variableValues[variableIndex]=valueDouble;
				simData.runData.updateVariableValueForStatistics(simData,variableIndex);
			}
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Input"),String.format(Language.tr("Simulation.Log.Input.Info"),client.logInfo(simData),name,simData.runModel.variableNames[variableIndex],NumberTools.formatNumber(valueDouble)));
			break;
		}

		return true;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Eingabe durchführen */
		if (!processInput(simData,client)) {
			/* Simulationsende am Dateiende */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Input"),String.format(Language.tr("Simulation.Log.Input.TerminateSimulation"),name));
			simData.eventManager.deleteAllEvents();
			simData.runData.stopp=true;
			return;
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}