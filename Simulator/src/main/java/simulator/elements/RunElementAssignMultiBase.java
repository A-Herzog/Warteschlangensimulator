/**
 * Copyright 2025 Alexander Herzog
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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.DecideRecord;
import ui.modeleditor.elements.ModelElementAssignMulti;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementAssignMulti}
 * @author Alexander Herzog
 * @see ModelElementAssignMulti
 */
public abstract class RunElementAssignMultiBase extends RunElementPassThrough {
	/**
	 * Indices der zuzuweisenden Kundentypen
	 */
	private int[] clientType;

	/**
	 * Icons der zuzuweisenden Kundentypen
	 */
	private String[] clientIcon;

	/**
	 * Namen der zuzuweisenden Kundentypen
	 */
	private String[] clientTypeName;

	/**
	 * Gewählter Modus zur Bestimmung des neuen Kundentyps
	 */
	public final DecideRecord.DecideMode mode;

	/**
	 * Optionale zusätzliche Bedingung, die für eine Zuweisung erfüllt sein muss (kann <code>null</code> sein)
	 */
	protected String condition;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 * @param mode	Gewählter Modus zur Bestimmung des neuen Kundentyps
	 */
	public RunElementAssignMultiBase(final ModelElementAssignMulti element, final DecideRecord.DecideMode mode) {
		super(element,buildName(element,Language.tr("Simulation.Element.AssignMulti.Name")));
		this.mode=mode;
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementAssignMulti)) return null;
		final ModelElementAssignMulti assignElement=(ModelElementAssignMulti)element;
		if (assignElement.getDecideRecord().getMode()!=mode) return null;
		RunElementAssignMultiBase assign;
		try {
			assign=getClass().getConstructor(ModelElementAssignMulti.class).newInstance(assignElement);
		} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e) {
			return e.toString();
		}

		/* Auslaufende Kanten */
		final String edgeError=assign.buildEdgeOut(assignElement);
		if (edgeError!=null) return edgeError;

		/* Namen und Icons */
		final List<String> names=assignElement.getNewClientTypesList();
		assign.clientType=new int[names.size()];
		assign.clientIcon=new String[names.size()];
		assign.clientTypeName=new String[names.size()];
		for (int i=0;i<names.size();i++) {
			final String name=names.get(i);
			if (name==null || name.isEmpty()) return String.format(Language.tr("Simulation.Creator.NoAssignName"),element.getId());
			assign.clientType[i]=runModel.getClientTypeNr(name);
			assign.clientTypeName[i]=name;
			assign.clientIcon[i]=editModel.clientData.getIcon(assign.clientTypeName[i]);
		}

		/* Bedingungen für die einzelnen Typen */
		final DecideRecord record=assignElement.getDecideRecord();
		final String errorMsg=assign.buildDecideData(editModel,runModel,assignElement,record,assignElement.getNewClientTypesList().size());
		if (errorMsg!=null) return errorMsg;

		/* Optionale Bedingung */
		final String condition=assignElement.getCondition();
		if (condition==null || condition.isBlank()) {
			assign.condition=null;
		} else {
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames,runModel.modelUserFunctions);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.AssignCondition"),condition,element.getId(),error+1);
			assign.condition=condition;
		}

		return assign;
	}

	/**
	 * Generiert die Einstellungen für den Modus zur Auswahl des Kundentyps.
	 * @param editModel	Editor-Modell
	 * @param runModel	Laufzeit-Modell
	 * @param element	Editor-Modell-Element zu dem ein Laufzeit-Modell-Element erstellt werden soll
	 * @param record	Datensatz mit den Informationen zur Verzweigung
	 * @param decideCount	Anzahl an möglichen neuen Kundentypen
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung.
	 */
	protected abstract String buildDecideData(final EditModel editModel, final RunModel runModel, final ModelElementAssignMulti element, final DecideRecord record, final int decideCount);

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementAssignMulti)) return null;
		final ModelElementAssignMulti assignElement=(ModelElementAssignMulti)element;
		if (assignElement.getDecideRecord().getMode()!=mode) return null;

		/* Auslaufende Kanten */
		final RunModelCreatorStatus edgeError=testEdgeOut(assignElement);
		if (edgeError!=null) return edgeError;

		/* Namen */
		final List<String> names=assignElement.getNewClientTypesList();
		for (int i=0;i<names.size();i++) {
			final String name=names.get(i);
			if (name==null || name.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoAssignName"),element.getId()),RunModelCreatorStatus.Status.NO_NAME);
		}

		/* Bedingungen für die einzelnen Typen */
		final DecideRecord record=assignElement.getDecideRecord();
		return testDecideData(assignElement,record,assignElement.getNewClientTypesList().size());
	}

	/**
	 * Prüft die Verzweigungseinstellungen
	 * @param element	Editor-Modell-Element zu dem ein Laufzeit-Modell-Element erstellt werden soll
	 * @param record	Datensatz mit den Informationen zur Verzweigung
	 * @param decideCount	Anzahl an möglichen neuen Kundentypen
	 * @return	Liefert ein Objekt mit weiteren Informationen (dieses Objekt kann auch "Erfolg" darstellen) zurück
	 */
	protected abstract RunModelCreatorStatus testDecideData(final ModelElementAssignMulti element, final DecideRecord record, final int decideCount);

	@Override
	public RunElementAssignMultiBaseData getData(final SimulationData simData) {
		RunElementAssignMultiBaseData data;
		data=(RunElementAssignMultiBaseData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementAssignMultiBaseData(this,condition,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Ermittelt, welcher Kundentyp-Index (in Bezug auf {@link #clientType}) zugewiesen werden soll.
	 * (Wird während der Verarbeitung des Leave-Events ausgeführt, d.h. zu einem Zeitpunkt, zu dem der Kunde bereits aus der Station ausgetragen ist.)
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde
	 * @param data	Stationsdatenobjekt
	 * @param decideCount	Anzahl der möglichen Optionen
	 * @return	Index des neuen Kundentyps in {@link #clientType}
	 */
	protected abstract int getOptionIndex(final SimulationData simData, final RunDataClient client, final RunElementAssignMultiBaseData data, int decideCount);

	/**
	 * Führt die eigentliche Zuweisung durch.
	 * (Wird während der Verarbeitung des Leave-Events ausgeführt, d.h. zu einem Zeitpunkt, zu dem der Kunde bereits aus der Station ausgetragen ist.)
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde
	 * @param data	Stationsdatenobjekt
	 */
	private void applyAssignment(final SimulationData simData, final RunDataClient client, final RunElementAssignMultiBaseData data) {
		/* Neuen Kundentyp ermitteln */
		final int nr=getOptionIndex(simData,client,data,clientType.length);
		int newClientType=clientType[nr];
		String newClientIcon=clientIcon[nr];

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.AssignMulti"),String.format(Language.tr("Simulation.Log.AssignMulti.Info"),client.hashCode(),simData.runModel.clientTypes[client.type],simData.runModel.clientTypes[newClientType],name));

		/* Kundentyp ändern */
		if (newClientType!=client.type) {
			/* Wurde bereits in StationLeaveEvent.run ausgeführt: simData.runData.logClientLeavesStation(simData,this,null,client); */
			if (parentId>=0) simData.runData.logClientLeavesStation(simData,simData.runModel.elementsFast[parentId],null,client);
			client.changeType(newClientType,simData,id);
			/* Da kein weiteres leave folgt, muss der neue Kunde die Station auch nicht betreten: simData.runData.logClientEntersStation(simData,this,null,client); */
			if (parentId>=0) simData.runData.logClientEntersStation(simData,simData.runModel.elementsFast[parentId],null,client);
		}
		client.iconLast=client.icon;
		client.icon=newClientIcon;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		final RunElementAssignMultiBaseData data=getData(simData);

		if (condition!=null) {
			simData.runData.setClientVariableValues(client);
			if (data.condition.eval(simData.runData.variableValues,simData,client)) applyAssignment(simData,client,data);
		} else {
			applyAssignment(simData,client,data);
		}

		super.processLeave(simData,client);
	}
}
