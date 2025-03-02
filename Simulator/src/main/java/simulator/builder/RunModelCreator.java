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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.elements.*;
import simulator.runmodel.RunModel;
import ui.modeleditor.coreelements.ModelElement;
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
	private static final RunElement[] templates;

	static {
		final List<RunElement> templatesList=new ArrayList<>();

		templatesList.add(new RunElementSource(null));
		templatesList.add(new RunElementSourceMulti(null));
		templatesList.add(new RunElementSourceTable(null));
		templatesList.add(new RunElementSourceDB(null));
		templatesList.add(new RunElementSourceDDE(null));
		templatesList.add(new RunElementDispose(null));
		templatesList.add(new RunElementDisposeWithTable(null));
		templatesList.add(new RunElementAssign(null));
		templatesList.add(new RunElementAssignString(null));
		templatesList.add(new RunElementDelay(null));
		templatesList.add(new RunElementDelayJS(null));
		templatesList.add(new RunElementBatch(null));
		templatesList.add(new RunElementBatchMulti(null));
		templatesList.add(new RunElementSeparate(null));
		templatesList.add(new RunElementDuplicate(null));
		templatesList.add(new RunElementDecideByChance(null));
		templatesList.add(new RunElementDecideByCondition(null));
		templatesList.add(new RunElementDecideByClientType(null));
		templatesList.add(new RunElementDecideBySequence(null));
		templatesList.add(new RunElementDecideByStation(null));
		templatesList.add(new RunElementDecideByScript(null));
		templatesList.add(new RunElementDecideByKeyValue(null));
		templatesList.add(new RunElementBalking(null));
		templatesList.add(new RunElementProcess(null));
		templatesList.add(new RunElementHold(null));
		templatesList.add(new RunElementHoldMulti(null));
		templatesList.add(new RunElementHoldJS(null));
		templatesList.add(new RunElementSignal(null));
		templatesList.add(new RunElementBarrier(null));
		templatesList.add(new RunElementBarrierPull(null));
		templatesList.add(new RunElementSeize(null));
		templatesList.add(new RunElementRelease(null));
		templatesList.add(new RunElementSetStatisticsMode(null));
		templatesList.add(new RunElementSet(null));
		templatesList.add(new RunElementSetJS(null));
		templatesList.add(new RunElementCounter(null));
		templatesList.add(new RunElementCounterMulti(null));
		templatesList.add(new RunElementCounterBatch(null));
		templatesList.add(new RunElementThroughput(null));
		templatesList.add(new RunElementStateStatistics(null));
		templatesList.add(new RunElementMatch(null));
		templatesList.add(new RunElementSplit(null));
		templatesList.add(new RunElementInput(null));
		templatesList.add(new RunElementInputJS(null));
		templatesList.add(new RunElementInputDB(null));
		templatesList.add(new RunElementInputDDE(null));
		templatesList.add(new RunElementOutput(null));
		templatesList.add(new RunElementOutputJS(null));
		templatesList.add(new RunElementOutputDB(null));
		templatesList.add(new RunElementOutputDDE(null));
		templatesList.add(new RunElementOutputLog(null));
		templatesList.add(new RunElementRecord(null));
		templatesList.add(new RunElementUserStatistic(null));
		templatesList.add(new RunElementDifferentialCounter(null));
		templatesList.add(new RunElementSectionStart(null));
		templatesList.add(new RunElementSectionEnd(null));
		templatesList.add(new RunElementPickUp(null));
		templatesList.add(new RunElementAction(null));
		templatesList.add(new RunElementSub(null));
		templatesList.add(new RunElementSubConnect(null));
		templatesList.add(new RunElementAnimationConnect(null));
		templatesList.add(new RunElementClientIcon(null));
		templatesList.add(new RunElementCosts(null));
		templatesList.add(new RunElementTransportSource(null));
		templatesList.add(new RunElementAssignSequence(null));
		templatesList.add(new RunElementTransportDestination(null));
		templatesList.add(new RunElementTransportParking(null));
		templatesList.add(new RunElementTransportTransporterSource(null));
		templatesList.add(new RunElementTeleportSource(null));
		templatesList.add(new RunElementTeleportSourceMulti(null));
		templatesList.add(new RunElementTeleportDecideByChance(null));
		templatesList.add(new RunElementTeleportDecideByCondition(null));
		templatesList.add(new RunElementTeleportDecideByClientType(null));
		templatesList.add(new RunElementTeleportDecideBySequence(null));
		templatesList.add(new RunElementTeleportDecideByStation(null));
		templatesList.add(new RunElementTeleportDecideByKeyValue(null));
		templatesList.add(new RunElementTeleportDestination(null));
		templatesList.add(new RunElementConveyor(null));
		templatesList.add(new RunElementLogicIf(null));
		templatesList.add(new RunElementLogicElse(null));
		templatesList.add(new RunElementLogicElseIf(null));
		templatesList.add(new RunElementLogicEndIf(null));
		templatesList.add(new RunElementLogicWhile(null));
		templatesList.add(new RunElementLogicEndWhile(null));
		templatesList.add(new RunElementLogicDo(null));
		templatesList.add(new RunElementLogicUntil(null));
		templatesList.add(new RunElementAnalogValue(null));
		templatesList.add(new RunElementAnalogAssign(null));
		templatesList.add(new RunElementTank(null));
		templatesList.add(new RunElementTankFlowByClient(null));
		templatesList.add(new RunElementTankFlowBySignal(null));
		templatesList.add(new RunElementTankSensor(null));
		templatesList.add(new RunElementTankValveSetup(null));
		templatesList.add(new RunElementReference(null));
		templatesList.add(new RunElementInteractiveButton(null));
		templatesList.add(new RunElementInteractiveSlider(null));
		templatesList.add(new RunElementInteractiveCheckbox(null));
		templatesList.add(new RunElementInteractiveRadiobutton(null));
		templatesList.add(new RunElementAnimationAlarm(null));
		templatesList.add(new RunElementAnimationPause(null));

		templates=templatesList.toArray(RunElement[]::new);
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
	 * Ermittelt den Laufzeit-Element-Namen für ein Editor-Element
	 * @param element	Editor-Element
	 * @return	Name des Laufzeit-Elements (der auch für die Statistik verwendet wird), der zu dem Editor-Element-Namen gehört
	 */
	public static String getName(final ModelElement element) {
		final Class<?> elementClass=element.getClass();

		for (RunElement run: templates) {
			for (Constructor<?> constructor: run.getClass().getConstructors()) {
				final Class<?>[] parameters=constructor.getParameterTypes();
				if (parameters.length==1 && parameters[0].equals(elementClass)) {
					try {
						final RunElement instance=(RunElement)constructor.newInstance(element);
						return instance.name;
					} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
						return null;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Fügt ein fertig erstelltes Laufzeit-Element zu dem Laufzeitmodell hinzu.
	 * @param element	Laufzeit-Element
	 * @param editName	Name des Elements
	 * @see #addElement(ModelElementPosition)
	 */
	private synchronized void addRunElementToRunModel(final RunElement element, final String editName) {
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