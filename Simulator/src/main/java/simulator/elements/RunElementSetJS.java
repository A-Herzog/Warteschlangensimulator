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
import ui.modeleditor.elements.ModelElementSetJS;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSetJS</code>
 * @author Alexander Herzog
 * @see ModelElementSetJS
 */
public class RunElementSetJS extends RunElementPassThrough {
	private String script;
	private ModelElementSetJS.ScriptMode mode;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSetJS(final ModelElementSetJS element) {
		super(element,buildName(element,Language.tr("Simulation.Element.SetJS.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSetJS)) return null;
		final ModelElementSetJS setElement=(ModelElementSetJS)element;
		final RunElementSetJS set=new RunElementSetJS(setElement);

		/* Auslaufende Kante */
		final String edgeError=set.buildEdgeOut(setElement);
		if (edgeError!=null) return edgeError;

		/* Skript */
		set.script=setElement.getScript();

		set.mode=setElement.getMode();

		if (set.mode==ModelElementSetJS.ScriptMode.Java && !testOnly) {
			final String scriptError=DynamicFactory.getFactory().test(set.script,true);
			if (scriptError!=null) return scriptError;
		}

		return set;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementSetJS)) return null;
		final ModelElementSetJS setElement=(ModelElementSetJS)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(setElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementSetJSData getData(final SimulationData simData) {
		RunElementSetJSData data;
		data=(RunElementSetJSData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementSetJSData(this,script,mode,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.SetJS"),String.format(Language.tr("Simulation.Log.SetJS.Info"),client.logInfo(simData),name));

		final RunElementSetJSData data=getData(simData);

		/* Berechnungen durchführen */
		if (data.jsRunner!=null) {
			/* JS-Mode */
			final JSRunSimulationData jsRunner=data.jsRunner;
			jsRunner.setSimulationData(simData,client);
			final String result=jsRunner.runCompiled();
			if (!jsRunner.getLastSuccess() && simData.runModel.canelSimulationOnScriptError) {
				simData.doEmergencyShutDown(result);
				return;
			}
			logJS(simData,data.script,result); /* Immer ausführen; Entscheidung Erfassen ja/nein erfolgt in logJS */
		}
		if (data.javaRunner!=null) {
			/* Java-Mode */
			final DynamicRunner javaRunner=data.javaRunner;
			javaRunner.parameter.client.setClient(client);
			final Object result=javaRunner.run();
			if (javaRunner.getStatus()!=DynamicStatus.OK && simData.runModel.canelSimulationOnScriptError) {
				simData.doEmergencyShutDown(DynamicFactory.getLongStatusText(javaRunner));
				return;
			}
			logJS(simData,data.script,result); /* Immer ausführen; Entscheidung Erfassen ja/nein erfolgt in logJS */
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}