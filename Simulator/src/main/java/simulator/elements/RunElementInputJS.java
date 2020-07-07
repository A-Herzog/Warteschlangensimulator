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
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.js.JSRunSimulationData;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementInput;
import ui.modeleditor.elements.ModelElementInputJS;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementInputJS</code>
 * @author Alexander Herzog
 * @see ModelElementInputJS
 */
public class RunElementInputJS extends RunElementPassThrough {
	private ModelElementInput.EofModes mode;
	private double defaultValue;
	private double[] inputData;
	private String script;
	private ModelElementInputJS.ScriptMode modeScript;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementInputJS(final ModelElementInputJS element) {
		super(element,buildName(element,Language.tr("Simulation.Element.InputJS.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementInputJS)) return null;
		final ModelElementInputJS inputElement=(ModelElementInputJS)element;
		final RunElementInputJS input=new RunElementInputJS(inputElement);

		/* Auslaufende Kante */
		final String edgeError=input.buildEdgeOut(inputElement);
		if (edgeError!=null) return edgeError;

		/* Eingabedatei */
		if (inputElement.getInputFile().trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoInputFile"),element.getId());
		final File inputFile=new File(inputElement.getInputFile());
		if (!testOnly) {
			input.inputData=RunElementInput.loadDoubleData(inputFile);
			if (input.inputData==null || input.inputData.length==0) return String.format(Language.tr("Simulation.Creator.NoInputData"),element.getId(),inputFile.toString());
		}

		/* Modus */
		input.mode=inputElement.getEofMode();
		input.defaultValue=inputElement.getDefaultValue();

		/* Skript */
		input.script=inputElement.getScript();

		input.modeScript=inputElement.getMode();

		if (input.modeScript==ModelElementInputJS.ScriptMode.Java && !testOnly) {
			final String scriptError=DynamicFactory.getFactory().test(input.script,true);
			if (scriptError!=null) return scriptError;
		}

		return input;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementInputJS)) return null;
		final ModelElementInputJS inputElement=(ModelElementInputJS)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(inputElement);
		if (edgeError!=null) return edgeError;

		/* Eingabedatei */
		if (inputElement.getInputFile().trim().isEmpty()) return RunModelCreatorStatus.noInputFile(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementInputJSData getData(final SimulationData simData) {
		RunElementInputJSData data;
		data=(RunElementInputJSData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementInputJSData(this,script,modeScript,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	private boolean processInput(final SimulationData simData, final RunDataClient client) {
		final RunElementInputJSData data=getData(simData);

		/* Nächsten Wert ermitteln */
		double value=defaultValue;
		if (data.position<inputData.length) {
			/* Weiterer Wert vorhanden */
			value=inputData[data.position];
			data.position++;
		} else {
			/* Dateiende */
			switch (mode) {
			case EOF_MODE_SKIP:
				return true; /* nix tun */
			case EOF_MODE_DEFAULT_VALUE:
				/* ist oben bereits gesetzt: value=defaultValue; */
				break;
			case EOF_MODE_LOOP:
				value=inputData[0];
				data.position=1;
				break;
			case EOF_MODE_TERMINATE:
				return false; /* Simulation abbrechen */
			}
		}

		/* Berechnungen durchführen */
		if (data.jsRunner!=null) {
			final JSRunSimulationData jsRunner=data.jsRunner;
			jsRunner.setSimulationInputValue(value);
			jsRunner.setSimulationData(simData,client);
			final String result=jsRunner.runCompiled();
			if (!jsRunner.getLastSuccess() && simData.runModel.cancelSimulationOnScriptError) {
				simData.doEmergencyShutDown(result);
				return false;
			}
			logJS(simData,data.script,result); /* Immer ausführen; Entscheidung Erfassen ja/nein erfolgt in logJS */
		}
		if (data.javaRunner!=null) {
			final DynamicRunner javaRunner=data.javaRunner;
			javaRunner.parameter.client.setClient(client);
			javaRunner.parameter.inputValue.set(value);
			final Object result=javaRunner.run();
			if (javaRunner.getStatus()!=DynamicStatus.OK && simData.runModel.cancelSimulationOnScriptError) {
				simData.doEmergencyShutDown(DynamicFactory.getLongStatusText(javaRunner));
				return false;
			}
			logJS(simData,data.script,result); /* Immer ausführen; Entscheidung Erfassen ja/nein erfolgt in logJS */
		}

		return true;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Eingabe durchführen */
		if (!processInput(simData,client)) {
			/* Simulationsende am Dateiende */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.InputJS"),String.format(Language.tr("Simulation.Log.InputJS.TerminateSimulation"),name));
			simData.eventManager.deleteAllEvents();
			simData.runData.stopp=true;
			return;
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
