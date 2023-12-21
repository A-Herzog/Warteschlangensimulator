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
package simulator.coreelements;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.coreelements.ModelElementLogic;
import ui.modeleditor.coreelements.ModelElementLogicWithCondition;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementLogicElse;
import ui.modeleditor.elements.ModelElementLogicElseIf;
import ui.modeleditor.elements.ModelElementLogicEndIf;
import ui.modeleditor.elements.ModelElementLogicEndWhile;
import ui.modeleditor.elements.ModelElementLogicIf;
import ui.modeleditor.elements.ModelElementLogicWhile;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Basisklasse für die Laufzeit-Flusssteuerungslogik-Elemente
 * @author Alexander Herzog
 */
public abstract class RunElementLogic extends RunElement {
	/**
	 * ID der nächsten Station an der auslaufenden Kante
	 * @see #build(EditModel, RunModel, ModelElement, ModelElementSub, boolean)
	 */
	protected int connectionId;

	/**
	 * Laufzeit-Element der nächsten Station an der auslaufenden Kante
	 * @see #connectionId
	 * @see #prepareRun(RunModel)
	 */
	protected RunElement connection;

	/**
	 * Bezeichnet der zu prüfenden Bedingung
	 * (nur wenn es sich um ein {@link ModelElementLogicWithCondition}-Element handelt)
	 * @see #build(EditModel, RunModel, ModelElement, ModelElementSub, boolean)
	 */
	protected String condition;

	/**
	 * Konstruktor der Klasse <code>RunElementLogic</code>
	 * @param element	Modell-Element aus dem ID und Farbe ausgelesen werden
	 * @param logicElementName	Name des Stationstyps (aus dem der Name per {@link RunElement#buildName(ui.modeleditor.coreelements.ModelElementPosition, String)} gebildet wird)
	 */
	public RunElementLogic(final ModelElementLogic element, final String logicElementName) {
		super(element,buildName(element,logicElementName));
	}

	/**
	 * Erstellt eine neue Instanz dieser Klasse (nicht {@link RunElementLogic},
	 * sondern der konkreten, abgeleiteten Klasse)
	 * @param element	Parameter für den Konstruktor
	 * @return	Neues Laufzeitelement
	 */
	private RunElementLogic getInstance(final ModelElementLogic element) {
		try {
			return getClass().getConstructor(ModelElementLogic.class).newInstance(element);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException	| NoSuchMethodException | SecurityException e) {
			return null;
		}
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementLogic)) return null;
		final ModelElementLogic logicElement=(ModelElementLogic)element;
		final RunElementLogic logic=getInstance(logicElement);
		if (logic==null) return String.format(Language.tr("Simulation.Creator.InternalErrorLogicElement"),element.getId());

		/* Auslaufende Kanten */
		logic.connectionId=findNextId(logicElement.getEdgeOut());
		if (logic.connectionId<0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());

		/* Ggf. Bedingung */
		if (logicElement instanceof ModelElementLogicWithCondition) {
			logic.condition=((ModelElementLogicWithCondition)logicElement).getCondition();
			final int error=ExpressionMultiEval.check(logic.condition,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.LogicCondition"),logic.condition,element.getId(),error+1);
		} else {
			logic.condition=null;
		}

		/* Verknüpfungen zu weiteren Logik-Elementen finden */
		final String connectError=logic.buildConnection(editModel,runModel,logicElement,parent);
		if (connectError!=null) return connectError;

		return logic;
	}

	/**
	 * Muss von den abgeleiteten Klassen überschrieben werden, um jeweils die Verbindung zum nächsten Element zu bestimmen.
	 * @param editModel	Editor-Modell
	 * @param runModel	Laufzeit-Modell
	 * @param element	Editor-Modell-Element zu dem ein Laufzeit-Modell-Element erstellt werden soll
	 * @param parent	Optional übergeordnetes Untermodell-Element (oder <code>null</code>, wenn sich das Element auf der Hauptebene befindet)
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	protected abstract String buildConnection(final EditModel editModel, final RunModel runModel, final ModelElementLogic element, final ModelElementSub parent);

	/**
	 * Liefert das nächste Element
	 * @param start	Ausganspunkt
	 * @return	Nächstes Element oder <code>null</code>, wenn kein unmittelbar nächstes Element gefunden wurde
	 * @see #findNextId(ModelElementLogic, Class[])
	 */
	private ModelElement nextElement(final ModelElement start) {
		ModelElementEdge edge=null;

		if (start instanceof ModelElementEdgeOut) {
			edge=((ModelElementEdgeOut)start).getEdgeOut();
		}

		if (start instanceof ModelElementEdgeMultiOut) {
			final ModelElementEdge[] edges=((ModelElementEdgeMultiOut)start).getEdgesOut();
			if (edges!=null && edges.length==1) edge=edges[0];
		}

		if (edge==null) return null;
		return edge.getConnectionEnd();
	}

	/**
	 * Sucht ausgehend von einem angegebenen Startelement ein Element eines bestimmten Typs
	 * @param start	Startelement
	 * @param classes	Liste der Typen nach denen gesucht wird
	 * @return	ID des gefundenen Zielelements oder -1, wenn nichts gefunden wurde.
	 */
	protected final int findNextId(final ModelElementLogic start, final Class<?>[] classes) {
		ModelElementBox element=start;
		final List<ModelElementLogic> sub=new ArrayList<>();

		while (element!=null) {
			/* Ziel erreicht? */
			if (element!=start && sub.isEmpty()) {
				for (Class<?> test: classes) if (test.equals(element.getClass())) return ((ModelElementLogic)element).getId();
			}

			/* Verarbeitung: Beginn/Ende von Sub-Bereichen */
			if (element!=start && (element instanceof ModelElementLogic)) {
				final ModelElementLogic logic=(ModelElementLogic)element;
				if (logic instanceof ModelElementLogicIf) sub.add(logic);
				if (logic instanceof ModelElementLogicWhile) sub.add(logic);
				if (logic instanceof ModelElementLogicEndIf) {
					if (sub.isEmpty() || ! (sub.get(sub.size()-1) instanceof ModelElementLogicIf)) return -1;
					sub.remove(sub.size()-1);
				}
				if (logic instanceof ModelElementLogicElse) {
					if (sub.isEmpty() || !(sub.get(sub.size()-1) instanceof ModelElementLogicIf)) return -1;
				}
				if (logic instanceof ModelElementLogicElseIf) {
					if (sub.isEmpty() || !(sub.get(sub.size()-1) instanceof ModelElementLogicIf)) return -1;
				}
				if (logic instanceof ModelElementLogicEndWhile) {
					if (sub.isEmpty() || !(sub.get(sub.size()-1) instanceof ModelElementLogicWhile)) return -1;
					sub.remove(sub.size()-1);
				}
			}

			/* Weiter zum nächsten */
			ModelElement next=element;
			while (next==element || !(next instanceof ModelElementBox)) {
				next=nextElement(next);
				if (next==null) return -1;
			}
			element=(ModelElementBox)next;
		}

		return -1;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementLogic)) return null;
		final ModelElementLogic logicElement=(ModelElementLogic)element;

		/* Auslaufende Kanten */
		if (findNextId(logicElement.getEdgeOut())<0) return RunModelCreatorStatus.noEdgeOut(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connection=runModel.elements.get(connectionId);
	}

	@Override
	public RunElementLogicData getData(final SimulationData simData) {
		RunElementLogicData data;
		data=(RunElementLogicData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementLogicData(this,condition,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		/* Die eigentliche Zielstation wird in processLeave in den von RunElementLogic Klassen bestimmt. */
	}

	/**
	 * Prüft (bei LogicWithCondition-Elementen), ob die Bedingung erfüllt ist
	 * @param simData	Simulationsdaten
	 * @param client	Kunde
	 * @return	Gibt an, ob die Bedingung erfüllt ist
	 */
	protected boolean checkCondition(final SimulationData simData, final RunDataClient client) {
		final RunElementLogicData data=getData(simData);
		simData.runData.setClientVariableValues(client);
		return data.condition.eval(simData.runData.variableValues,simData,client);
	}

	/**
	 * Wird in der processLeave-Methode der abgeleiteten Klassen verwendet, um den Kunden entweder zu
	 * direkten Folgestation oder zu einer anderen speziellen Station zu leiten
	 * @param simData	Simulationsdaten
	 * @param client	Kunde
	 * @param specialNext	Wird <code>null</code> übergeben, so wird der Kunde zur direkten Folgestation geleitet, sonst zu der hier angegebenen.
	 */
	protected void processLeaveIntern(final SimulationData simData, final RunDataClient client, final RunElement specialNext) {
		if (specialNext==null) {
			/* Einfach weiter zum nächsten Element */
			StationLeaveEvent.sendToStation(simData,client,this,connection);
		} else {
			/* Weiter zu nutzerdefiniertem Element */
			StationLeaveEvent.sendToStation(simData,client,this,specialNext);
		}
	}

	@Override
	public RunElement getNext() {
		return connection; /* Aus Sicht einer Pull-Produktions-Schranke ist das ein normaler Verarbeitungsstrang. */
	}
}