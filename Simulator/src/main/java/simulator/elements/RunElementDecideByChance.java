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

import language.Language;
import mathtools.NumberTools;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.DecideRecord;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementDecide</code>
 * @author Alexander Herzog
 * @see ModelElementDecide
 */
public class RunElementDecideByChance extends RunElement {
	/** IDs der über die auslaufenden Kanten erreichbaren Folgestationen */
	private List<Integer> connectionIds;
	/** Über die auslaufenden Kanten erreichbaren Folgestationen (aus {@link #connectionIds} abgeleitet) */
	private RunElement[] connections;
	/** Wahrscheinlichkeiten für die verschiedenen auslaufenden Kanten */
	private double[] probabilites;
	/** Rechenausdrücke für die Raten für die verschiedenen auslaufenden Kanten */
	private String[] probabilitesStrings;

	/** Kundentyp-Zuweisungen an den Ausgängen */
	private String[] clientTypeNames;
	/** IDs der Kundentyp-Zuweisungen an den Ausgängen (kann in Gänze <code>null</code> sein, oder einzelne Einträge können -1 sein) */
	private int[] clientTypeIds;
	/** Icons für die Kundentyp-Zuweisungen an den Ausgängen */
	private String[] clientTypeIcons;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementDecideByChance(final ModelElementDecide element) {
		super(element,buildName(element,Language.tr("Simulation.Element.DecideByChance.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDecide)) return null;
		if (((ModelElementDecide)element).getDecideRecord(). getMode()!=DecideRecord.DecideMode.MODE_CHANCE) return null;

		final RunElementDecideByChance decide=new RunElementDecideByChance((ModelElementDecide)element);

		double sum=0;
		int count=0;
		decide.connectionIds=new ArrayList<>();
		ModelElementEdge[] edges=((ModelElementDecide)element).getEdgesOut();
		if (edges.length==0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());
		decide.probabilites=new double[edges.length];
		decide.probabilitesStrings=new String[edges.length];
		final List<String> editRates=((ModelElementDecide)element).getDecideRecord().getRates();
		for (ModelElementEdge edge : edges) {
			final int id=findNextId(edge);
			if (id<0) return String.format(Language.tr("Simulation.Creator.EdgeToNowhere"),element.getId(),edge.getId());
			decide.connectionIds.add(id);
			final String rate=(count>=editRates.size())?"1":editRates.get(count);
			decide.probabilitesStrings[count]=rate;
			final Double D=NumberTools.getPlainDouble(rate);
			if (D==null || sum==-1) {
				sum=-1;
				decide.probabilites=null;
			} else {
				decide.probabilites[count]=Math.max(0,D);
				sum+=Math.max(0,D);
			}
			count++;
		}

		if (decide.probabilites==null) {
			for (int i=0;i<decide.probabilitesStrings.length;i++) {
				final int error=ExpressionCalc.check(decide.probabilitesStrings[i],runModel.variableNames,runModel.modelUserFunctions);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.DecideRate"),i+1,decide.probabilitesStrings[i],element.getId(),error+1);
			}
		} else {
			decide.probabilitesStrings=null;
			if (sum==0) return String.format(Language.tr("Simulation.Creator.NoDecideByChanceRates"),element.getId());
			for (int i=0;i<decide.probabilites.length;i++) decide.probabilites[i]=decide.probabilites[i]/sum;
		}

		/* Kundentypzuweisungen */
		decide.clientTypeNames=((ModelElementDecide)element).getChangedClientTypes().toArray(String[]::new);
		decide.clientTypeIcons=new String[decide.clientTypeNames.length];
		for (int i=0;i<decide.clientTypeIcons.length;i++) decide.clientTypeIcons[i]=editModel.clientData.getIcon(decide.clientTypeNames[i]);

		return decide;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementDecide)) return null;
		if (((ModelElementDecide)element).getDecideRecord(). getMode()!=DecideRecord.DecideMode.MODE_CHANCE) return null;

		double sum=0;
		ModelElementEdge[] edges=((ModelElementDecide)element).getEdgesOut();
		if (edges.length==0) return RunModelCreatorStatus.noEdgeOut(element);
		int count=0;
		final List<String> editRates=((ModelElementDecide)element).getDecideRecord().getRates();
		for (ModelElementEdge edge : edges) {
			final int id=findNextId(edge);
			if (id<0) return RunModelCreatorStatus.edgeToNowhere(element,edge);

			final String rateString=(count>=editRates.size())?"1":editRates.get(count);
			if (sum>=0) {
				Double D=NumberTools.getDouble(rateString);
				if (sum!=-1 && D==null) sum=-1; else sum+=Math.max(0,D);
			}
			count++;
		}
		if (sum==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoDecideByChanceRates"),element.getId()));

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connections=new RunElement[connectionIds.size()];
		for (int i=0;i<connectionIds.size();i++) connections[i]=runModel.elements.get(connectionIds.get(i));

		clientTypeIds=new int[clientTypeNames.length];
		boolean hasData=false;
		for (int i=0;i<clientTypeNames.length;i++) {
			final int nr=runModel.getClientTypeNr(clientTypeNames[i]);
			if (nr>=0) hasData=true;
			clientTypeIds[i]=nr;
		}
		if (!hasData) clientTypeIds=null;
	}

	@Override
	public RunElementDecideByChanceData getData(final SimulationData simData) {
		RunElementDecideByChanceData data;
		data=(RunElementDecideByChanceData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementDecideByChanceData(this,probabilitesStrings,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Zielstation bestimmen */
		int nr=-1;
		if (probabilites==null) {
			/* Rechenausdrücke auswerten */
			simData.runData.setClientVariableValues(client);
			nr=getData(simData).getDestinationIndex(simData);
			if (nr<0) nr=probabilitesStrings.length-1;
		} else {
			/* Einfache Wahrscheinlichkeiten */
			final double rnd=simData.runData.random.nextDouble();
			double sum=0;
			for (int i=0;i<probabilites.length;i++) {
				sum+=probabilites[i];
				if (sum>=rnd) {nr=i; break;}
			}
			if (nr<0) nr=probabilites.length-1;
		}

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideByChance"),String.format(Language.tr("Simulation.Log.DecideByChance.Info"),client.logInfo(simData),name,nr+1,connections.length));

		/* Kundentypen zuweisen */
		if (clientTypeIds!=null) {
			final int type=clientTypeIds[nr];
			if (type>=0) {
				/* Kundentyp zuweisen */
				final int lastType=client.type;
				client.typeLast=lastType;
				client.type=type;
				client.iconLast=client.icon;
				client.icon=clientTypeIcons[nr];

				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideByChance"),String.format(Language.tr("Simulation.Log.Decide.AssignInfo"),client.hashCode(),simData.runModel.clientTypes[lastType],simData.runModel.clientTypes[type],name));
			}
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.sendToStation(simData,client,this,connections[nr]);
	}
}
