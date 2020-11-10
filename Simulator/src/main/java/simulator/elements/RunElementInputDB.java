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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.db.DBConnect;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementInputDB;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementInputDB</code>
 * @author Alexander Herzog
 * @see ModelElementInputDB
 */
public class RunElementInputDB extends RunElementPassThrough {
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
	private ModelElementInputDB.EofModes mode;

	/**
	 * Vorgabewert (Zahl)
	 */
	private double defaultValue;

	/**
	 * Vorgabewert (Text)
	 */
	private String defaultText;

	private double[] inputData;
	private String[] inputStrings;
	private AssignMode assignMode;
	private int variableIndex;
	private String key;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementInputDB(final ModelElementInputDB element) {
		super(element,buildName(element,Language.tr("Simulation.Element.InputDB.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementInputDB)) return null;
		final ModelElementInputDB inputElement=(ModelElementInputDB)element;
		final RunElementInputDB input=new RunElementInputDB(inputElement);

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
		if (input.mode==ModelElementInputDB.EofModes.EOF_MODE_DEFAULT_VALUE) {
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
			final String dbError=input.loadDatabase(inputElement);
			if (dbError!=null) return dbError;
		}

		return input;
	}

	private String loadDatabase(ModelElementInputDB inputElement) {
		try (DBConnect connect=new DBConnect(inputElement.getDb(),false)) {
			if (connect.getInitError()!=null) return Language.tr("Simulation.Creator.DatabaseError")+": "+connect.getInitError();

			final String sortColumn=(inputElement.getSortColumn().trim().isEmpty())?null:inputElement.getSortColumn();

			if (assignMode==AssignMode.CLIENT_TEXT) {
				final List<String> list=new ArrayList<>();
				final Iterator<String> iterator=connect.readStringTableColumn(inputElement.getTable(),inputElement.getLoadColumn(),sortColumn,inputElement.getSortMode(),null);
				while (iterator.hasNext()) {
					list.add(iterator.next());
				}
				if (list.size()==0) return String.format(Language.tr("Simulation.Creator.DatabaseError.NoRows"),inputElement.getId(),inputElement.getTable());
				inputStrings=list.toArray(new String[0]);

			} else {
				final List<Double> list=new ArrayList<>();
				final Iterator<Double> iterator=connect.readTableColumn(inputElement.getTable(),inputElement.getLoadColumn(),sortColumn,inputElement.getSortMode(),null);
				while (iterator.hasNext()) {
					final Double D=iterator.next();
					if (D!=null) list.add(D);
				}
				if (list.size()==0) return String.format(Language.tr("Simulation.Creator.DatabaseError.NoRows"),inputElement.getId(),inputElement.getTable());
				inputData=list.stream().mapToDouble(d->d.doubleValue()).toArray();
			}

			return null;
		}
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementInputDB)) return null;
		final ModelElementInputDB inputElement=(ModelElementInputDB)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(inputElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementInputDBData getData(final SimulationData simData) {
		RunElementInputDBData data;
		data=(RunElementInputDBData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementInputDBData(this);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	private boolean processInput(final SimulationData simData, final RunDataClient client) {
		final RunElementInputDBData data=getData(simData);

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
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.InputDB"),String.format(Language.tr("Simulation.Log.InputDB.InfoClientData"),client.logInfo(simData),name,variableIndex,NumberTools.formatNumber(valueDouble)));
			break;
		case CLIENT_TEXT:
			/* Zuweisung an Schlüssel */
			client.setUserDataString(key,valueString);
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.InputDB"),String.format(Language.tr("Simulation.Log.InputDB.InfoClientDataString"),client.logInfo(simData),name,key,valueString));
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
			}
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.InputDB"),String.format(Language.tr("Simulation.Log.InputDB.Info"),client.logInfo(simData),name,simData.runModel.variableNames[variableIndex],NumberTools.formatNumber(valueDouble)));
			break;
		}

		return true;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Eingabe durchführen */
		if (!processInput(simData,client)) {
			/* Simulationsende am Dateiende */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.InputDB"),String.format(Language.tr("Simulation.Log.InputDB.TerminateSimulation"),name));
			simData.eventManager.deleteAllEvents();
			simData.runData.stopp=true;
			return;
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
