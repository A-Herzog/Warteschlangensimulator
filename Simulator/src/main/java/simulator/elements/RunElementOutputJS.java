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
import simulator.runmodel.RunData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataOutputWriter;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementOutputJS;
import ui.modeleditor.elements.ModelElementSub;

/**
 * �quivalent zu <code>ModelElementOutputJS</code>
 * @author Alexander Herzog
 * @see ModelElementOutputJS
 */
public class RunElementOutputJS extends RunElementPassThrough {
	/** Ausgabe aktiv? */
	private boolean outputActive;
	/** Ausgabedatei */
	private File outputFile;
	/** Wenn die Ausgabedatei schon besteht, soll diese �berschrieben werden (anstatt Daten anzuh�ngen)? */
	private boolean outputFileOverwrite;
	/** Auszuf�hrendes Skript zur Generierung der Ausgabe */
	private String script;
	/** Skriptsprache des Skriptes in {@link #script} */
	private ModelElementOutputJS.ScriptMode mode;
	/** Auszuf�hrendes Skript zur Generierung der �berschriften (kann <code>null</code> sein, wenn keine �berschrift ausgegeben werden soll) */
	private String scriptHeading;
	/** Skriptsprache des Skriptes in {@link #scriptHeading} */
	private ModelElementOutputJS.ScriptMode modeHeading;
	/** System zur gepufferten Dateiausgabe ({@link RunData#getOutputWriter(File, boolean)}) */
	private RunDataOutputWriter outputWriter;
	/** Bereits in {@link #build(EditModel, RunModel, ModelElement, ModelElementSub, boolean)} vorbereiteter (optionale) Java-Runner */
	private DynamicRunner jRunner;
	/** Bereits in {@link #build(EditModel, RunModel, ModelElement, ModelElementSub, boolean)} vorbereiteter (optionale) Java-Runner f�r die �berschriften */
	private DynamicRunner jRunnerHeading;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugeh�riges Editor-Element
	 */
	public RunElementOutputJS(final ModelElementOutputJS element) {
		super(element,buildName(element,Language.tr("Simulation.Element.OutputJS.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementOutputJS)) return null;
		final ModelElementOutputJS outputElement=(ModelElementOutputJS)element;
		final RunElementOutputJS output=new RunElementOutputJS(outputElement);

		/* Auslaufende Kante */
		final String edgeError=output.buildEdgeOut(outputElement);
		if (edgeError!=null) return edgeError;

		/* Ausgabe aktiv? */
		output.outputActive=outputElement.isOutputActive();

		/* Ausgabedatei */
		if (outputElement.getOutputFile().trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoOutputFile"),element.getId());
		output.outputFile=new File(outputElement.getOutputFile());
		output.outputFileOverwrite=outputElement.isOutputFileOverwrite();

		/* Skript */
		output.script=outputElement.getScript();
		output.mode=outputElement.getMode();

		/* �berschriften-Skript */
		if (outputElement.isUseHeadingScript()) {
			output.scriptHeading=outputElement.getScriptHeading();
			output.modeHeading=outputElement.getModeHeading();
		} else {
			output.scriptHeading=null;
			output.modeHeading=ModelElementOutputJS.ScriptMode.Javascript;
		}

		if (output.mode==ModelElementOutputJS.ScriptMode.Java && !testOnly) {
			final Object runner=DynamicFactory.getFactory().test(output.script,runModel.javaImports,true);
			if (runner instanceof String) return String.format(Language.tr("Simulation.Creator.ScriptError"),element.getId())+"\n"+runner;
			output.jRunner=(DynamicRunner)runner;
		}

		if (output.scriptHeading!=null && output.modeHeading==ModelElementOutputJS.ScriptMode.Java && !testOnly) {
			final Object runner=DynamicFactory.getFactory().test(output.scriptHeading,runModel.javaImports,true);
			if (runner instanceof String) return String.format(Language.tr("Simulation.Creator.ScriptError"),element.getId())+"\n"+runner;
			output.jRunnerHeading=(DynamicRunner)runner;
		}

		return output;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementOutputJS)) return null;
		final ModelElementOutputJS outputElement=(ModelElementOutputJS)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(outputElement);
		if (edgeError!=null) return edgeError;

		/* Ausgabedatei */
		if (outputElement.getOutputFile().trim().isEmpty()) return RunModelCreatorStatus.noOutputFile(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementOutputJSData getData(final SimulationData simData) {
		RunElementOutputJSData data;
		data=(RunElementOutputJSData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementOutputJSData(this,script,mode,scriptHeading,modeHeading,jRunner,jRunnerHeading,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Liefert eine Textzeile als Ausgabe.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @return	Tabellenzeile
	 * @see #processOutput(SimulationData, RunDataClient)
	 */
	private String getOutputString(final SimulationData simData, final RunDataClient client) {
		final RunElementOutputJSData data=getData(simData);

		String result="";

		if (data.jsRunner!=null) {
			final JSRunSimulationData jsRunner=data.jsRunner;
			jsRunner.setSimulationData(simData,id,client);
			result=jsRunner.runCompiled();
			if (!jsRunner.getLastSuccess() && simData.runModel.cancelSimulationOnScriptError) {
				simData.doEmergencyShutDown(name+": "+result);
			}
			logJS(simData,data.script,result); /* Immer ausf�hren; Entscheidung Erfassen ja/nein erfolgt in logJS */
		}
		if (data.javaRunner!=null) {
			final DynamicRunner javaRunner=data.javaRunner;
			data.output.setLength(0);
			javaRunner.parameter.client.setClient(client);
			final Object javaResult=javaRunner.run();
			if (javaRunner.getStatus()!=DynamicStatus.OK && simData.runModel.cancelSimulationOnScriptError) {
				simData.doEmergencyShutDown(name+": "+DynamicFactory.getLongStatusText(javaRunner));
			}
			logJS(simData,data.script,javaResult); /* Immer ausf�hren; Entscheidung Erfassen ja/nein erfolgt in logJS */
			result=data.output.toString();
		}
		simData.runData.updateMapValuesForStatistics(simData);

		return result;
	}

	/**
	 * Liefert die �berschriften-Textzeile als Ausgabe.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @return	Tabellenzeile
	 * @see #processOutput(SimulationData, RunDataClient)
	 */
	private String getOutputStringHeading(final SimulationData simData, final RunDataClient client) {
		final RunElementOutputJSData data=getData(simData);

		String result="";

		if (data.jsRunnerHeading!=null) {
			final JSRunSimulationData jsRunner=data.jsRunnerHeading;
			jsRunner.setSimulationData(simData,id,client);
			result=jsRunner.runCompiled();
			if (!jsRunner.getLastSuccess() && simData.runModel.cancelSimulationOnScriptError) {
				simData.doEmergencyShutDown(name+": "+result);
			}
			logJS(simData,data.scriptHeading,result); /* Immer ausf�hren; Entscheidung Erfassen ja/nein erfolgt in logJS */
		}
		if (data.javaRunnerHeading!=null) {
			final DynamicRunner javaRunner=data.javaRunnerHeading;
			data.outputHeading.setLength(0);
			javaRunner.parameter.client.setClient(client);
			final Object javaResult=javaRunner.run();
			if (javaRunner.getStatus()!=DynamicStatus.OK && simData.runModel.cancelSimulationOnScriptError) {
				simData.doEmergencyShutDown(name+": "+DynamicFactory.getLongStatusText(javaRunner));
			}
			logJS(simData,data.scriptHeading,javaResult); /* Immer ausf�hren; Entscheidung Erfassen ja/nein erfolgt in logJS */
			result=data.outputHeading.toString();
		}
		simData.runData.updateMapValuesForStatistics(simData);

		return result;
	}

	/**
	 * F�hrt die eigentliche Ausgabe-Verarbeitung durch.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 */
	private void processOutput(final SimulationData simData, final RunDataClient client) {
		if (outputFile==null) return;
		if (outputWriter==null) {
			outputWriter=simData.runData.getOutputWriter(outputFile,outputFileOverwrite);
			if (scriptHeading!=null) outputWriter.output(getOutputStringHeading(simData,client));
		}
		outputWriter.output(getOutputString(simData,client));
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.OutputJS"),String.format(Language.tr("Simulation.Log.OutputJS.Info"),client.logInfo(simData),name));

		if (!client.isWarmUp && client.inStatistics && outputActive) {
			/* Ausgabe durchf�hren */
			processOutput(simData,client);
		}

		/* Kunde zur n�chsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void finalCleanUp(final SimulationData simData) {
		if (outputWriter!=null) outputWriter.close();
	}
}