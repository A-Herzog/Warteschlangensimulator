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
package simulator.builder;

import java.util.ArrayList;
import java.util.List;

import language.Language;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementAction;
import simulator.elements.RunElementAnalogAssign;
import simulator.elements.RunElementAnalogValue;
import simulator.elements.RunElementAnimationConnect;
import simulator.elements.RunElementAssign;
import simulator.elements.RunElementAssignSequence;
import simulator.elements.RunElementAssignString;
import simulator.elements.RunElementBalking;
import simulator.elements.RunElementBarrier;
import simulator.elements.RunElementBarrierPull;
import simulator.elements.RunElementBatch;
import simulator.elements.RunElementBatchMulti;
import simulator.elements.RunElementClientIcon;
import simulator.elements.RunElementConveyor;
import simulator.elements.RunElementCosts;
import simulator.elements.RunElementCounter;
import simulator.elements.RunElementCounterMulti;
import simulator.elements.RunElementDecideByChance;
import simulator.elements.RunElementDecideByClientType;
import simulator.elements.RunElementDecideByCondition;
import simulator.elements.RunElementDecideByKeyValue;
import simulator.elements.RunElementDecideByScript;
import simulator.elements.RunElementDecideBySequence;
import simulator.elements.RunElementDecideByStation;
import simulator.elements.RunElementDelay;
import simulator.elements.RunElementDifferentialCounter;
import simulator.elements.RunElementDispose;
import simulator.elements.RunElementDuplicate;
import simulator.elements.RunElementHold;
import simulator.elements.RunElementHoldJS;
import simulator.elements.RunElementHoldMulti;
import simulator.elements.RunElementInput;
import simulator.elements.RunElementInputDB;
import simulator.elements.RunElementInputDDE;
import simulator.elements.RunElementInputJS;
import simulator.elements.RunElementInteractiveButton;
import simulator.elements.RunElementInteractiveCheckbox;
import simulator.elements.RunElementInteractiveRadiobutton;
import simulator.elements.RunElementInteractiveSlider;
import simulator.elements.RunElementLogicDo;
import simulator.elements.RunElementLogicElse;
import simulator.elements.RunElementLogicElseIf;
import simulator.elements.RunElementLogicEndIf;
import simulator.elements.RunElementLogicEndWhile;
import simulator.elements.RunElementLogicIf;
import simulator.elements.RunElementLogicUntil;
import simulator.elements.RunElementLogicWhile;
import simulator.elements.RunElementLongRunStatistics;
import simulator.elements.RunElementMatch;
import simulator.elements.RunElementOutput;
import simulator.elements.RunElementOutputDB;
import simulator.elements.RunElementOutputDDE;
import simulator.elements.RunElementOutputJS;
import simulator.elements.RunElementPickUp;
import simulator.elements.RunElementProcess;
import simulator.elements.RunElementRecord;
import simulator.elements.RunElementReference;
import simulator.elements.RunElementRelease;
import simulator.elements.RunElementSectionEnd;
import simulator.elements.RunElementSectionStart;
import simulator.elements.RunElementSeize;
import simulator.elements.RunElementSeparate;
import simulator.elements.RunElementSet;
import simulator.elements.RunElementSetJS;
import simulator.elements.RunElementSetStatisticsMode;
import simulator.elements.RunElementSignal;
import simulator.elements.RunElementSource;
import simulator.elements.RunElementSourceDB;
import simulator.elements.RunElementSourceDDE;
import simulator.elements.RunElementSourceMulti;
import simulator.elements.RunElementSourceTable;
import simulator.elements.RunElementSplit;
import simulator.elements.RunElementStateStatistics;
import simulator.elements.RunElementSub;
import simulator.elements.RunElementSubConnect;
import simulator.elements.RunElementTank;
import simulator.elements.RunElementTankFlowByClient;
import simulator.elements.RunElementTankFlowBySignal;
import simulator.elements.RunElementTankSensor;
import simulator.elements.RunElementTankValveSetup;
import simulator.elements.RunElementTeleportDestination;
import simulator.elements.RunElementTeleportSource;
import simulator.elements.RunElementThroughput;
import simulator.elements.RunElementTransportDestination;
import simulator.elements.RunElementTransportParking;
import simulator.elements.RunElementTransportSource;
import simulator.elements.RunElementTransportTransporterSource;
import simulator.elements.RunElementUserStatistic;
import simulator.runmodel.RunModel;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.InteractiveElement;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Erstellt aus einem veränderbaren Editormodell ein simulierbares Laufzeitmodell.
 * @author Alexander Herzog
 * @see EditModel
 * @see RunModel
 */
public final class RunModelCreator {
	/** Editormodell aus dem die Daten ausgelesen werden sollen */
	private final EditModel editModel;
	/** Laufzeitmodell in das die (Stations-)Daten eingetragen werden sollen */
	private final RunModel runModel;
	/** Wird hier <code>true</code> übergeben, so werden externe Datenquellen nicht wirklich geladen */
	private final boolean testOnly;
	/** Vorlagen für die Elemente */
	private static final List<RunElement> templates=new ArrayList<>();

	static {
		addTemplates();
	}

	/**
	 * Konstruktor der Klasse
	 * @param editModel	Editormodell aus dem die Daten ausgelesen werden sollen
	 * @param runModel	Laufzeitmodell in das die (Stations-)Daten eingetragen werden sollen
	 * @param testOnly	Wird hier <code>true</code> übergeben, so werden externe Datenquellen nicht wirklich geladen
	 */
	public RunModelCreator(final EditModel editModel, final RunModel runModel, final boolean testOnly) {
		this.editModel=editModel;
		this.runModel=runModel;
		this.testOnly=testOnly;
	}

	/**
	 * Stellt die Liste der Vorlagen für die Elemente zusammen.
	 * @see #templates
	 */
	private static void addTemplates() {
		templates.add(new RunElementSource(null));
		templates.add(new RunElementSourceMulti(null));
		templates.add(new RunElementSourceTable(null));
		templates.add(new RunElementSourceDB(null));
		templates.add(new RunElementSourceDDE(null));
		templates.add(new RunElementDispose(null));
		templates.add(new RunElementAssign(null));
		templates.add(new RunElementAssignString(null));
		templates.add(new RunElementDelay(null));
		templates.add(new RunElementBatch(null));
		templates.add(new RunElementBatchMulti(null));
		templates.add(new RunElementSeparate(null));
		templates.add(new RunElementDuplicate(null));
		templates.add(new RunElementDecideByChance(null));
		templates.add(new RunElementDecideByCondition(null));
		templates.add(new RunElementDecideByClientType(null));
		templates.add(new RunElementDecideBySequence(null));
		templates.add(new RunElementDecideByStation(null));
		templates.add(new RunElementDecideByScript(null));
		templates.add(new RunElementDecideByKeyValue(null));
		templates.add(new RunElementBalking(null));
		templates.add(new RunElementProcess(null));
		templates.add(new RunElementHold(null));
		templates.add(new RunElementHoldMulti(null));
		templates.add(new RunElementHoldJS(null));
		templates.add(new RunElementSignal(null));
		templates.add(new RunElementBarrier(null));
		templates.add(new RunElementBarrierPull(null));
		templates.add(new RunElementSeize(null));
		templates.add(new RunElementRelease(null));
		templates.add(new RunElementSetStatisticsMode(null));
		templates.add(new RunElementSet(null));
		templates.add(new RunElementSetJS(null));
		templates.add(new RunElementCounter(null));
		templates.add(new RunElementCounterMulti(null));
		templates.add(new RunElementThroughput(null));
		templates.add(new RunElementStateStatistics(null));
		templates.add(new RunElementMatch(null));
		templates.add(new RunElementSplit(null));
		templates.add(new RunElementInput(null));
		templates.add(new RunElementInputJS(null));
		templates.add(new RunElementInputDB(null));
		templates.add(new RunElementInputDDE(null));
		templates.add(new RunElementOutput(null));
		templates.add(new RunElementOutputJS(null));
		templates.add(new RunElementOutputDB(null));
		templates.add(new RunElementOutputDDE(null));
		templates.add(new RunElementRecord(null));
		templates.add(new RunElementUserStatistic(null));
		templates.add(new RunElementDifferentialCounter(null));
		templates.add(new RunElementSectionStart(null));
		templates.add(new RunElementSectionEnd(null));
		templates.add(new RunElementPickUp(null));
		templates.add(new RunElementAction(null));
		templates.add(new RunElementSub(null));
		templates.add(new RunElementSubConnect(null));
		templates.add(new RunElementAnimationConnect(null));
		templates.add(new RunElementClientIcon(null));
		templates.add(new RunElementCosts(null));
		templates.add(new RunElementTransportSource(null));
		templates.add(new RunElementAssignSequence(null));
		templates.add(new RunElementTransportDestination(null));
		templates.add(new RunElementTransportParking(null));
		templates.add(new RunElementTransportTransporterSource(null));
		templates.add(new RunElementTeleportSource(null));
		templates.add(new RunElementTeleportDestination(null));
		templates.add(new RunElementConveyor(null));
		templates.add(new RunElementLogicIf(null));
		templates.add(new RunElementLogicElse(null));
		templates.add(new RunElementLogicElseIf(null));
		templates.add(new RunElementLogicEndIf(null));
		templates.add(new RunElementLogicWhile(null));
		templates.add(new RunElementLogicEndWhile(null));
		templates.add(new RunElementLogicDo(null));
		templates.add(new RunElementLogicUntil(null));
		templates.add(new RunElementAnalogValue(null));
		templates.add(new RunElementAnalogAssign(null));
		templates.add(new RunElementTank(null));
		templates.add(new RunElementTankFlowByClient(null));
		templates.add(new RunElementTankFlowBySignal(null));
		templates.add(new RunElementTankSensor(null));
		templates.add(new RunElementTankValveSetup(null));
		templates.add(new RunElementReference(null));
		templates.add(new RunElementInteractiveButton(null));
		templates.add(new RunElementInteractiveSlider(null));
		templates.add(new RunElementInteractiveCheckbox(null));
		templates.add(new RunElementInteractiveRadiobutton(null));
	}

	/**
	 * Fügt ein fertig erstelltes Laufzeit-Element zu dem Laufzeitmodell hinzu.
	 * @param element	Laufzeit-Element
	 * @param editName	Name des Elements
	 * @see #addElement(ModelElementPosition)
	 */
	private void addRunElementToRunModel(final RunElement element, final String editName) {
		runModel.elements.put(element.id,element);
		runModel.namesToIDs.put(editName,element.id);
	}

	/**
	 * Versucht aus einem Editor-Element ein Laufzeit-Element zu erstellen und fügt
	 * es im Erfolgsfall <b>nicht</b> zu dem Laufzeitmodell hinzu.
	 * @param element	Zu prüfendes und zu übertragendes Editor-Element
	 * @param parent	Übergeordnetes Editor-Element
	 * @return	Gibt im Erfolgsfall ein {@link RunElement}-Objekt; im Fehlerfall eine Fehlermeldung als String
	 */
	public Object buildRunElement(final ModelElementPosition element, final ModelElementSub parent) {
		for (RunElement runElement : templates) {
			final Object obj=runElement.build(editModel,runModel,element,parent,testOnly);
			if (obj==null) continue;
			return obj;
		}
		String typeName="";
		if (element instanceof ModelElementBox) typeName=((ModelElementBox)element).getTypeName(); else typeName=element.getContextMenuElementName();
		return String.format(Language.tr("Simulation.Creator.UnknownElement"),element.getId(),typeName);
	}

	/**
	 * Versucht aus einem Editor-Element ein Laufzeit-Element zu erstellen und fügt
	 * es im Erfolgsfall zu dem Laufzeitmodell hinzu.
	 * @param element	Zu prüfendes und zu übertragendes Editor-Element
	 * @param parent	Übergeordnetes Editor-Element
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String addElement(final ModelElementPosition element, final ModelElementSub parent) {
		final Object obj=buildRunElement(element,parent);

		if (obj instanceof RunElement) {
			addRunElementToRunModel((RunElement)obj,element.getName());
			return null;
		}

		return (String)obj;
	}

	/**
	 * Versucht aus einem Editor-Element ein Laufzeit-Element zu erstellen und fügt
	 * es im Erfolgsfall zu dem Laufzeitmodell hinzu.
	 * @param element	Zu prüfendes und zu übertragendes Editor-Element
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String addElement(final ModelElementPosition element) {
		return addElement(element,null);
	}

	/**
	 * Fügt ein Pseudo-Element zur Erfassung von Laufzeitstatistikdaten zu dem Laufzeitmodell hinzu
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String addLongRunStatistic() {
		RunElementLongRunStatistics template=new RunElementLongRunStatistics(editModel.surface.getMaxId()+1);
		final Object obj=template.build(editModel,runModel,null,null,false);
		if (obj==null) return Language.tr("Simulation.Creator.InternalErrorAddingAdditionalStatistic");
		if (obj instanceof String) return (String)obj;
		if (obj instanceof RunElement) {addRunElementToRunModel((RunElement)obj,"SpecialStatistic"); return null;}
		return Language.tr("Simulation.Creator.InternalErrorAddingAdditionalStatistic");
	}

	/**
	 * Führt eine Schnellprüfung für ein Editor-Element durch
	 * @param element	Zu prüfendes Editor-Element
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public static String testElement(final ModelElementPosition element) {
		final RunModelCreatorStatus result=testElementStatus(element);
		if (result.isOk()) return null;
		return result.message;
	}

	/**
	 * Führt eine Schnellprüfung für ein Editor-Element durch
	 * @param element	Zu prüfendes Editor-Element
	 * @return	Liefert immer ein Statusobjekt, welches jedoch auch den Status "Ok" darstellen kann
	 */
	public static RunModelCreatorStatus testElementStatus(final ModelElementPosition element) {
		if (!(element instanceof InteractiveElement)) {
			if (!(element instanceof ModelElementBox)) return RunModelCreatorStatus.ok;
			if (!((ModelElementBox)element).inputConnected()) return RunModelCreatorStatus.ok; /* Keine einlaufende Kante -> Kommt in Simulation überhaupt nicht vor, ignorieren. */
		}

		for (RunElement runElement : templates) {
			final RunModelCreatorStatus result=runElement.test(element);
			if (result==null) continue; /* Nicht unser Element */
			return result;
		}
		return RunModelCreatorStatus.ok; /* Nichts zum Prüfen gefunden */
	}
}