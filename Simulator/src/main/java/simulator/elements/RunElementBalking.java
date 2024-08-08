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

import java.util.List;

import language.Language;
import mathtools.distribution.tools.DistributionRandomNumber;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.elements.ModelElementBalking;
import ui.modeleditor.elements.ModelElementBalkingData;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementBalking</code>
 * @author Alexander Herzog
 * @see ModelElementBalking
 */
public class RunElementBalking extends RunElementPassThrough {
	/** ID der Station in Richtung "Abbruch" */
	private int connectionBalkingId;
	/** Station in Richtung "Abbruch" (wird aus {@link #connectionBalkingId} abgeleitet) */
	private RunElement connectionBalking;
	/** ID des zu überwachendes / nächstes Element */
	private int testStationId;
	/** Zu überwachendes / nächstes Element (wird aus {@link #testStationId} abgeleitet) */
	private RunElementProcess testStation;
	/** Zurückschreckwahrscheinlichkeit je Kundentyp (nur gültig, wenn kein Rechenausdruck in {@link #expression} für den Kundentyp gesetzt ist) */
	private double[] probability;
	/** Rechenausdruck für die Zurückschreckwahrscheinlichkeit je Kundentyp (kann für einzelne Kundentypen <code>null</code> sein, dann gilt für diese {@link #probability}) */
	private String[] expression;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementBalking(final ModelElementBalking element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Balking.Name")));
	}

	/**
	 * Findet entlang eines Pfades die nächste Bedienstation.
	 * @param startEdge	Kante der gefolgt werden soll
	 * @return	ID der nächsten Bedienstation oder -1, wenn keine Bedienstation gefunden wurde
	 */
	private int findNextProcessStation(final ModelElementEdge startEdge) {
		if (startEdge==null) return -1;
		ModelElement element=startEdge.getConnectionEnd();

		while (element!=null) {
			if (element instanceof ModelElementProcess) return element.getId();
			ModelElementEdge edge=null;
			if (element instanceof ModelElementEdgeOut) {
				edge=((ModelElementEdgeOut)element).getEdgeOut();
			}

			if (element instanceof ModelElementEdgeMultiOut) {
				final ModelElementEdge[] edges=((ModelElementEdgeMultiOut)element).getEdgesOut();
				if (edges!=null && edges.length>0) edge=edges[0];
			}

			if (edge==null) return -1;
			element=edge.getConnectionEnd();
		}

		return -1;
	}

	@Override
	public Object build(EditModel editModel, RunModel runModel, ModelElement element, ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementBalking)) return null;
		final ModelElementBalking balkingElement=(ModelElementBalking)element;
		final RunElementBalking balking=new RunElementBalking(balkingElement);

		/* Auslaufende Kanten */
		final ModelElementEdge[] edges=balkingElement.getEdgesOut();
		if (edges==null || edges.length!=2) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());

		/* Standardweg */
		final String edgeDefaultError=balking.buildEdgeOut(edges[0]);
		if (edgeDefaultError!=null) return edgeDefaultError;

		/* Abbruchweg */
		balking.connectionBalkingId=findNextId(edges[1]);
		if (balking.connectionBalkingId<0) return String.format(Language.tr("Simulation.Creator.NoEdgeOutBalking"),element.getId());

		/* Nächste Bedienstation */
		balking.testStationId=findNextProcessStation(edges[0]);
		if (balking.testStationId<0) return String.format(Language.tr("Simulation.Creator.NoFollowingProcessStation"),element.getId());

		/* Ausdrücke und Wahrscheinlichkeiten */
		balking.probability=new double[runModel.clientTypes.length];
		balking.expression=new String[runModel.clientTypes.length];
		final List<ModelElementBalkingData> list=balkingElement.getClientTypeData();
		for (int i=0;i<runModel.clientTypes.length;i++) {
			ModelElementBalkingData data=null;
			for (ModelElementBalkingData rec: list) if (rec.getClientType().equals(runModel.clientTypes[i])) {data=rec; break;}
			if (data==null) data=balkingElement.getGlobalData();
			if (data.getExpression()==null) {
				balking.probability[i]=Math.max(0,Math.min(1,data.getProbability()));
			} else {
				balking.expression[i]=data.getExpression();
				final int error=ExpressionMultiEval.check(balking.expression[i],runModel.variableNames,runModel.modelUserFunctions);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.BalkingCondition"),balking.expression[i],element.getId(),error+1);
			}
		}

		return balking;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementBalking)) return null;
		final ModelElementBalking balkingElement=(ModelElementBalking)element;

		/* Auslaufende Kanten */
		final ModelElementEdge[] edges=balkingElement.getEdgesOut();
		if (edges==null || edges.length!=2) return RunModelCreatorStatus.noEdgeOut(element);

		/* Standardweg */
		if (findNextId(edges[0])<0) return RunModelCreatorStatus.noEdgeOut(element);

		/* Abbruchweg */
		if (findNextId(edges[1])<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoEdgeOutBalking"),element.getId()),RunModelCreatorStatus.Status.NO_EDGE_OUT);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		super.prepareRun(runModel); /* Standardverbindung herstellen */
		connectionBalking=runModel.elements.get(connectionBalkingId);
		testStation=(RunElementProcess)runModel.elements.get(testStationId);
	}

	@Override
	public RunElementBalkingData getData(final SimulationData simData) {
		RunElementBalkingData data;
		data=(RunElementBalkingData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementBalkingData(this,expression,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		/* Die eigentliche Zielstation wird in processLeave bestimmt. */
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		final RunElementBalkingData data=getData(simData);
		if (data.testStationData==null) data.testStationData=testStation.getData(simData);

		boolean balking=false;
		if (data.testStationData.clientsAtStationQueue>0) {
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Balking"),String.format(Language.tr("Simulation.Log.Balking.QueueAtTarget"),client.logInfo(simData),testStation.id,data.testStationData.clientsAtStationQueue));

			/* Ausdruck auswerten / Über Wahrscheinlichkeit bestimmen */
			final ExpressionMultiEval condition=data.conditions[client.type];
			if (condition==null) {
				final double p=probability[client.type];
				final double rnd=DistributionRandomNumber.nextDouble();
				balking=(rnd<p);
			} else {
				simData.runData.setClientVariableValues(client);
				balking=condition.eval(simData.runData.variableValues,simData,client);
			}
		} else {
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Balking"),String.format(Language.tr("Simulation.Log.Balking.NoQueueAtTarget"),client.logInfo(simData),testStation.id));
		}

		if (balking) {
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Balking"),String.format(Language.tr("Simulation.Log.Balking.Balking"),client.logInfo(simData)));

			/* Zur Zurückschreck-Folgestation leiten */
			StationLeaveEvent.sendToStation(simData,client,this,connectionBalking);
		} else {
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Balking"),String.format(Language.tr("Simulation.Log.Balking.NormalProcessing"),client.logInfo(simData)));

			/* Zur regulär nächsten Station leiten */
			super.processLeave(simData,client);
		}
	}
}
