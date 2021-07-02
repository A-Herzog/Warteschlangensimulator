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

import org.apache.commons.math3.util.FastMath;

import language.Language;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDecideJS;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>RunElementDecideJS</code>
 * @author Alexander Herzog
 * @see ModelElementDecideJS
 */
public class RunElementDecideByScript extends RunElement {
	/** IDs der über die auslaufenden Kanten erreichbaren Folgestationen */
	private List<Integer> connectionIds;
	/** Über die auslaufenden Kanten erreichbaren Folgestationen (aus {@link #connectionIds} abgeleitet) */
	private RunElement[] connections;
	/** Auszuführendes Skript */
	private String script;
	/** Skriptspache für {@link #script} */
	private ModelElementDecideJS.ScriptMode mode;
	/** Bereits in {@link #build(EditModel, RunModel, ModelElement, ModelElementSub, boolean)} vorbereiteter (optionale) Java-Runner */
	private DynamicRunner jRunner;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementDecideByScript(final ModelElementDecideJS element) {
		super(element,buildName(element,Language.tr("Simulation.Element.DecideByScript.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDecideJS)) return null;
		final ModelElementDecideJS decideElement=(ModelElementDecideJS)element;
		final RunElementDecideByScript decide=new RunElementDecideByScript(decideElement);

		decide.connectionIds=new ArrayList<>();
		ModelElementEdge[] edges=(decideElement).getEdgesOut();
		if (edges.length==0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());

		for (ModelElementEdge edge : edges) {
			final int id=findNextId(edge);
			if (id<0) return String.format(Language.tr("Simulation.Creator.EdgeToNowhere"),element.getId(),edge.getId());
			decide.connectionIds.add(id);
		}

		decide.script=decideElement.getScript();

		decide.mode=decideElement.getMode();

		if (decide.mode==ModelElementDecideJS.ScriptMode.Java && !testOnly) {
			final Object runner=DynamicFactory.getFactory().test(decide.script,runModel.javaImports,true);
			if (runner instanceof String) return String.format(Language.tr("Simulation.Creator.ScriptError"),element.getId())+"\n"+runner;
			decide.jRunner=(DynamicRunner)runner;
		}

		return decide;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementDecideJS)) return null;
		final ModelElementDecideJS decideElement=(ModelElementDecideJS)element;

		final ModelElementEdge[] edges=decideElement.getEdgesOut();
		if (edges.length==0) return RunModelCreatorStatus.noEdgeOut(decideElement);
		for (ModelElementEdge edge : edges) {
			final int id=findNextId(edge);
			if (id<0) return RunModelCreatorStatus.edgeToNowhere(element,edge);
		}

		if (decideElement.getScript().trim().isEmpty()) {
			return RunModelCreatorStatus.noScript(element);
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connections=new RunElement[connectionIds.size()];
		for (int i=0;i<connectionIds.size();i++) connections[i]=runModel.elements.get(connectionIds.get(i));
	}

	@Override
	public RunElementDecideByScriptData getData(final SimulationData simData) {
		RunElementDecideByScriptData data;
		data=(RunElementDecideByScriptData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementDecideByScriptData(this,script,mode,jRunner,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		final RunElementDecideByScriptData data=getData(simData);

		/* Zielstation bestimmen */
		int nr=data.getNextStation(simData,client);
		nr=FastMath.max(1,nr);
		nr=FastMath.min(connections.length,nr);
		nr--; /* 1-basiert -> 0-basiert */

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideByScript"),String.format(Language.tr("Simulation.Log.DecideByScript.Info"),client.logInfo(simData),name,nr+1,connections.length));

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.sendToStation(simData,client,this,connections[nr]);
	}
}
