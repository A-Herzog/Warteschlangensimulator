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
import java.util.Arrays;
import java.util.List;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementSubIn;
import ui.modeleditor.elements.ModelElementSubOut;

/**
 * Äquivalent zu <code>ModelElementSub</code>
 * @author Alexander Herzog
 * @see ModelElementSub
 */
public class RunElementSub extends RunElement {
	private int[][] connectionInIds;
	private int[] connectionOutIds;
	private int[] internInIds;
	private int[] internOutIds;
	private RunElement[] connectionOut;
	private RunElement[] internIn;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSub(final ModelElementSub element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Sub.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSub)) return null;

		RunElementSub sub=new RunElementSub((ModelElementSub)element);

		final List<List<Integer>> list=new ArrayList<>();
		ModelElementEdge[] edges;

		/* Einlaufende Kanten */
		edges=((ModelElementSub)element).getEdgesIn();
		if (edges.length!=((ModelElementSub)element).getInputCount()) return String.format(Language.tr("Simulation.Creator.InputConnectionsNotMatching"),element.getId());
		if (edges.length==0) return String.format(Language.tr("Simulation.Creator.NoEdgeIn"),element.getId());
		for (int i=0;i<edges.length;i++) {
			final List<Integer> ids=findPreviousId(edges[i]);
			if (ids.size()==0) return String.format(Language.tr("Simulation.Creator.SubEdgeInNotConnected"),i+1,element.getId());
			for (Integer newConnect: ids) for (List<Integer> otherQueue: list) for (Integer otherConnect: otherQueue) if (otherConnect.equals(newConnect)) return String.format(Language.tr("Simulation.Creator.SubMultipleEdgesInFromSameElement"),newConnect.intValue(),element.getId());
			list.add(ids);
		}

		sub.connectionInIds=new int[list.size()][];
		for (int i=0;i<list.size();i++) {
			final List<Integer> subList=list.get(i);
			sub.connectionInIds[i]=new int[subList.size()];
			for (int j=0;j<subList.size();j++) sub.connectionInIds[i][j]=subList.get(j);
		}

		/* Auslaufende Kanten */
		edges=((ModelElementSub)element).getEdgesOut();
		if (edges.length!=((ModelElementSub)element).getOutputCount()) return String.format(Language.tr("Simulation.Creator.OutputConnectionsNotMatching"),element.getId());
		if (edges.length==0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());
		sub.connectionOutIds=new int[edges.length];
		for (int i=0;i<edges.length;i++) {
			int id=findNextId(edges[i]);
			if (id<0) return String.format(Language.tr("Simulation.Creator.SubEdgeOutNotConnected"),i+1,element.getId());
			sub.connectionOutIds[i]=id;
		}

		/* Interne Verknüpfungen */
		ModelSurface subSurface=((ModelElementSub)element).getSubSurface();

		/* Interne Verknüpfungen in das Submodell einlaufend */
		sub.internInIds=new int[sub.connectionInIds.length];
		Arrays.fill(sub.internInIds,-1);
		for (ModelElement e: subSurface.getElements()) if (e instanceof ModelElementSubIn) {
			int nr=((ModelElementSubIn)e).getConnectionNr();
			int id=e.getId();
			if (nr<0 || nr>=sub.internInIds.length) return String.format(Language.tr("Simulation.Creator.InternalInputConnectionsNotMatching"),element.getId());
			sub.internInIds[nr]=id;
		}
		for (int i=0;i<sub.internInIds.length;i++) if (sub.internInIds[i]<0) return String.format(Language.tr("Simulation.Creator.InternalInputNotConnected"),element.getId(),i+1);

		/* Interne Verknüpfungen aus dem Submodell auslaufend */
		sub.internOutIds=new int[sub.connectionOutIds.length];
		Arrays.fill(sub.internOutIds,-1);
		for (ModelElement e: subSurface.getElements()) if (e instanceof ModelElementSubOut) {
			int nr=((ModelElementSubOut)e).getConnectionNr();
			int id=e.getId();
			if (nr<0 || nr>=sub.internOutIds.length) return String.format(Language.tr("Simulation.Creator.InternalOutputConnectionsNotMatching"),element.getId());
			sub.internOutIds[nr]=id;
		}
		for (int i=0;i<sub.internOutIds.length;i++) if (sub.internOutIds[i]<0) return String.format(Language.tr("Simulation.Creator.InternalOutputNotConnected"),element.getId(),i+1);

		return sub;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementSub)) return null;

		final List<List<Integer>> list=new ArrayList<>();
		ModelElementEdge[] edges;

		/* Einlaufende Kanten */
		edges=((ModelElementSub)element).getEdgesIn();
		if (edges.length!=((ModelElementSub)element).getInputCount()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InputConnectionsNotMatching"),element.getId()));
		if (edges.length==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoEdgeIn"),element.getId()));
		for (int i=0;i<edges.length;i++) {
			final List<Integer> ids=findPreviousId(edges[i]);
			if (ids.size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SubEdgeInNotConnected"),i+1,element.getId()));
			for (Integer newConnect: ids) for (List<Integer> otherQueue: list) for (Integer otherConnect: otherQueue) if (otherConnect.equals(newConnect)) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SubMultipleEdgesInFromSameElement"),newConnect.intValue(),element.getId()));
			list.add(ids);
		}

		int[][] connectionInIds=new int[list.size()][];
		for (int i=0;i<list.size();i++) {
			final List<Integer> subList=list.get(i);
			connectionInIds[i]=new int[subList.size()];
			for (int j=0;j<subList.size();j++) connectionInIds[i][j]=subList.get(j);
		}

		/* Auslaufende Kanten */
		edges=((ModelElementSub)element).getEdgesOut();
		if (edges.length!=((ModelElementSub)element).getOutputCount()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.OutputConnectionsNotMatching"),element.getId()));
		if (edges.length==0) return RunModelCreatorStatus.noEdgeOut(element);
		int[] connectionOutIds=new int[edges.length];
		for (int i=0;i<edges.length;i++) {
			int id=findNextId(edges[i]);
			if (id<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SubEdgeOutNotConnected"),i+1,element.getId()));
			connectionOutIds[i]=id;
		}

		/* Interne Verknüpfungen */
		ModelSurface subSurface=((ModelElementSub)element).getSubSurface();

		/* Interne Verknüpfungen in das Submodell einlaufend */
		final int[] internInIds=new int[connectionInIds.length];
		Arrays.fill(internInIds,-1);
		for (ModelElement e: subSurface.getElements()) if (e instanceof ModelElementSubIn) {
			int nr=((ModelElementSubIn)e).getConnectionNr();
			int id=e.getId();
			if (nr<0 || nr>=internInIds.length) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InternalInputConnectionsNotMatching"),element.getId()));
			internInIds[nr]=id;
		}
		for (int i=0;i<internInIds.length;i++) if (internInIds[i]<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InternalInputNotConnected"),element.getId(),i+1));

		/* Interne Verknüpfungen aus dem Submodell auslaufend */
		int[] internOutIds=new int[connectionOutIds.length];
		Arrays.fill(internOutIds,-1);
		for (ModelElement e: subSurface.getElements()) if (e instanceof ModelElementSubOut) {
			int nr=((ModelElementSubOut)e).getConnectionNr();
			int id=e.getId();
			if (nr<0 || nr>=internOutIds.length) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InternalOutputConnectionsNotMatching"),element.getId()));
			internOutIds[nr]=id;
		}
		for (int i=0;i<internOutIds.length;i++) if (internOutIds[i]<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.InternalOutputNotConnected"),element.getId(),i+1));

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connectionOut=new RunElement[connectionOutIds.length];
		for (int i=0;i<connectionOut.length;i++) connectionOut[i]=runModel.elements.get(connectionOutIds[i]);

		internIn=new RunElement[internInIds.length];
		for (int i=0;i<internIn.length;i++) internIn[i]=runModel.elements.get(internInIds[i]);
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Von welcher einlaufenden Kante aus ist der Kunde eingetroffen? */
		int edgeNumberIn=-1;
		for (int i=0;i<connectionInIds.length;i++) {
			for (int id: connectionInIds[i]) if (id==client.lastStationID) {edgeNumberIn=i; break;}
			if (edgeNumberIn>=0) break;
		}

		int edgeNumberOut=-1;
		if (edgeNumberIn<0)	for (int i=0;i<internOutIds.length;i++) {
			if (internOutIds[i]==client.lastStationID) {edgeNumberOut=i; break;}
			if (edgeNumberOut>=0) break;
		}

		if (edgeNumberIn<0 && edgeNumberOut<0) edgeNumberOut=0;

		if (edgeNumberIn>=0) {
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Sub"),String.format(Language.tr("Simulation.Log.Sub.In"),edgeNumberIn+1,client.logInfo(simData),name));

			/* Weiterleiten */
			client.stationInformationInt=edgeNumberIn;
			StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		} else {
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Sub"),String.format(Language.tr("Simulation.Log.Sub.Out"),edgeNumberOut+1,client.logInfo(simData),name));

			/* Weiterleiten */
			client.stationInformationInt=1_000_000_000+edgeNumberOut;
			StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		}
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		if (client.stationInformationInt<1_000_000_000) {
			/* Kunde bewegt sich in Submodell hinein, weiter also in internIn[...] */
			StationLeaveEvent.sendToStation(simData,client,this,internIn[client.stationInformationInt]);
			simData.runData.clientsAtStation(simData,this,null,1);
			simData.runData.clientsAtStationByType(simData,this,null,client,1);
		} else {
			/* Kunde bewegt sich aus Submodell hinaus, weiter also in connectionOut[...] */
			StationLeaveEvent.sendToStation(simData,client,this,connectionOut[client.stationInformationInt-1_000_000_000]);
			simData.runData.clientsAtStation(simData,this,null,-1);
			simData.runData.clientsAtStationByType(simData,this,null,client,-1);
		}
	}
}