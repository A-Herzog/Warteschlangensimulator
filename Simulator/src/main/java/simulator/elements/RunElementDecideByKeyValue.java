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
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementDecide</code>
 * @author Alexander Herzog
 * @see ModelElementDecide
 */
public class RunElementDecideByKeyValue extends RunElement {
	/** IDs der über die auslaufenden Kanten erreichbaren Folgestationen */
	private List<Integer> connectionIds;
	/** Über die auslaufenden Kanten erreichbaren Folgestationen (aus {@link #connectionIds} abgeleitet) */
	private RunElement[] connections;
	/** Für die Wahl der auslaufenden Kante auszuwertender Kundentextdaten-Schlüssel */
	private String key;
	/** Werte für {@link #key} die zu der Wahl einer jeweiligen auslaufenden Kante führen */
	private String[][] values;

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
	public RunElementDecideByKeyValue(final ModelElementDecide element) {
		super(element,buildName(element,Language.tr("Simulation.Element.DecideByKeyValue.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementDecide)) return null;
		if (((ModelElementDecide)element).getMode()!=ModelElementDecide.DecideMode.MODE_KEY_VALUE) return null;

		final RunElementDecideByKeyValue decide=new RunElementDecideByKeyValue((ModelElementDecide)element);

		/* Schlüssel */
		if (((ModelElementDecide)element).getKey().trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoKey"),element.getId());
		decide.key=((ModelElementDecide)element).getKey();

		decide.connectionIds=new ArrayList<>();
		final ModelElementEdge[] edges=((ModelElementDecide)element).getEdgesOut();
		final List<String> values=((ModelElementDecide)element).getValues();
		if (edges.length==0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());
		decide.values=new String[Math.max(0,edges.length-1)][];

		/* Mehrere Werte pro Wert-Eintrag? */
		final boolean multiTextValues=((ModelElementDecide)element).isMultiTextValues();

		/* Werte */
		for (int i=0;i<edges.length-1;i++) {
			final String value=(i>=values.size())?"":values.get(i);
			if (multiTextValues) {
				final String[] v=value.split(";");
				if (v.length==0) return String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),i+1);
				for (int j=0;j<v.length;j++) v[j]=v[j].trim();
				for (String s: v) if (s.isEmpty()) return String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),i+1);
				decide.values[i]=v;
			} else {
				if (value.trim().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),i+1);
				decide.values[i]=new String[]{value};
			}
		}

		/* Ausgangskanten erfassen */
		for (int i=0;i<edges.length;i++) {
			final ModelElementEdge edge=edges[i];
			final int id=findNextId(edge);
			if (id<0) return String.format(Language.tr("Simulation.Creator.EdgeToNowhere"),element.getId(),edge.getId());
			decide.connectionIds.add(id);
		}

		/* Kundentypzuweisungen */
		decide.clientTypeNames=((ModelElementDecide)element).getChangedClientTypes().toArray(String[]::new);
		decide.clientTypeIcons=new String[decide.clientTypeNames.length];
		for (int i=0;i<decide.clientTypeIcons.length;i++) decide.clientTypeIcons[i]=editModel.clientData.getIcon(decide.clientTypeNames[i]);

		return decide;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementDecide)) return null;
		if (((ModelElementDecide)element).getMode()!=ModelElementDecide.DecideMode.MODE_KEY_VALUE) return null;

		/* Schlüssel */
		if (((ModelElementDecide)element).getKey().trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoKey"),element.getId()));

		final ModelElementEdge[] edges=((ModelElementDecide)element).getEdgesOut();
		final List<String> values=((ModelElementDecide)element).getValues();

		/* Mehrere Werte pro Wert-Eintrag? */
		final boolean multiTextValues=((ModelElementDecide)element).isMultiTextValues();

		/* Werte */
		for (int i=0;i<edges.length-1;i++) {
			final String value=(i>=values.size())?"":values.get(i);
			if (multiTextValues) {
				final String[] v=value.split(";");
				if (v.length==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),i+1));
				for (String s: v) if (s.trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),i+1));
			} else {
				if (value.trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),i+1));
			}
		}

		/* Ausgangskanten erfassen */
		if (edges.length==0) return RunModelCreatorStatus.noEdgeOut(element);
		for (int i=0;i<edges.length;i++) {
			final ModelElementEdge edge=edges[i];
			final int id=findNextId(edge);
			if (id<0) return RunModelCreatorStatus.edgeToNowhere(element,edge);
		}

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
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		final String value=client.getUserDataString(key);
		int nr=-1;
		for (int i=0;i<values.length;i++) {
			final String[] v=values[i];
			for (int j=0;j<v.length;j++) if (value.equals(v[j])) {nr=i; break;}
			if (nr>=0) break;
		}
		if (nr<0) nr=connections.length-1; /* Else */

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideByKeyValue"),String.format(Language.tr("Simulation.Log.DecideByKeyValue.Info"),client.logInfo(simData),name,key,value,nr+1,connections.length));

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
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.DecideByKeyValue"),String.format(Language.tr("Simulation.Log.Decide.AssignInfo"),client.hashCode(),simData.runModel.clientTypes[lastType],simData.runModel.clientTypes[type],name));
			}
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.sendToStation(simData,client,this,connections[nr]);
	}
}
