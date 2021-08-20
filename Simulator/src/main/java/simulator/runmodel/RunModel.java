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
package simulator.runmodel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import scripting.java.ImportSettingsBuilder;
import simulator.StartAnySimulator;
import simulator.StartAnySimulator.AdditionalPrepareErrorInfo;
import simulator.builder.RunModelCreator;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementSourceTable;
import simulator.elements.RunSource;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionEval;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import simulator.statistics.Statistics;
import simulator.statistics.Statistics.CorrelationMode;
import tools.SetupData;
import ui.modeleditor.ModelSequence;
import ui.modeleditor.ModelSequenceStep;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ElementWithNewVariableNames;
import ui.modeleditor.elements.ElementWithScript;
import ui.modeleditor.elements.InteractiveElement;
import ui.modeleditor.elements.ModelElementAnimationConnect;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementDisposeWithTable;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceDB;
import ui.modeleditor.elements.ModelElementSourceDDE;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.modeleditor.elements.ModelElementSourceTable;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Laufzeit-Modell
 * Dieses Modell wird read-only in der Simulation verwendet. Im Gegensatz zu dem Editor-Modell ist es auf Konsistenz geprüft,
 * Bediensationen sind per Referenzen verknüpft, nicht mehr nur durch Freitextfelder.
 * @author Alexander Herzog
 * @see EditModel
 * @see RunModel#getRunModel(EditModel, boolean, boolean)
 */
public class RunModel {
	/**
	 * Anzahl der zu simulierenden Kundenankünfte (kann <code>-1</code> sein, wenn das Simulationsende nicht über die Zählung der Kundenankünfte erfolgen soll)
	 */
	public long clientCount;

	/**
	 * Anzahl an zu simulierenden Kundenankünfte (nur für die Bestimmung der Warm-Up-Zeit)
	 */
	public long clientCountModel;

	/**
	 * Gibt an, wie viele Kunden insgesamt eintreffen werden.<br>
	 * Bei Modellen, die nur Tabellenquellen verwenden, ist dies das Minimum aus der Summe aller Tabellenzeilen und {@link RunModel#clientCount}. Bei allen anderen Modellen ist dies stets {@link RunModel#clientCount}.
	 */
	public long realArrivingClientCount;

	/**
	 * Länge der Einschwingphase (als Anteil der Kundenankünfte), bevor die Statistikzählung beginnt.<br>
	 * Die Einschwingphase wird nicht von der Kundenanzahl abgezogen, sondern besteht aus zusätzlichen Ankünften.
	 */
	public double warmUpTime;

	/**
	 * Gibt an, wie oft der Simulationslauf als Ganzes wiederholt werden soll.
	 */
	public int repeatCount;

	/**
	 * Sollen wiederholte Simulationsläufe ggf. aufgeteilt werden, um alle CPU-Kerne auszulasten?
	 */
	public boolean repeatAllowSplit;

	/**
	 * Gibt an, durch wie viel die Anzahl an Kunden für einen Thread geteilt werden soll.<br>
	 * (Dieser Wert steht auch in den SimulationData zur Verfügung.)
	 */
	public int clientCountDiv;

	/**
	 * Abbruchbedingung (kann <code>null</code> sein, wenn keine Abbruchbedingung definiert ist)
	 */
	public ExpressionEval terminationCondition;

	/**
	 * Abbruchzeitpunkt in Sekunden (kann -1 sein, wenn keine Abbruchzeit definiert ist)
	 */
	public long terminationTime;

	/**
	 * Abbruch bei Erreichen des Batch-Means-Konfidenzradius für die Wartezeiten (-1, wenn nicht zu verwenden)
	 */
	public double terminationWaitingTimeConfidenceHalfWidth;

	/**
	 * Abbruch bei Erreichen des Batch-Means-Konfidenzradius für die Wartezeiten für das angegebene Niveau (-1, wenn nicht zu verwenden)
	 */
	public double terminationWaitingTimeConfidenceLevel;

	/**
	 * Liste mit Namen aller vorhandenen Kundentypen
	 */
	public String[] clientTypes;

	/**
	 * Zuordnung von Kundentypnamen zu Indices in {@link #clientTypes}
	 * @see #clientTypes
	 */
	public Map<String,Integer> clientTypesMap;

	/**
	 * Kosten für die Zeitanteile pro Sekunde Kundentyp
	 */
	public double[][] clientCosts;

	/**
	 * Liste mit den Icons aller vorhandenen Kundentypen
	 */
	public String[] clientTypeIcons;

	/**
	 * Liste mit allen Variablennamen (in der Reihenfolge wie sie als Parameter dem Calc-System übergeben werden)
	 */
	public String[] variableNames;

	/**
	 * Initiale Werte für die Variablen (können teilweise <code>null</code> sein, wenn nicht explizit gesetzt werden soll)
	 */
	public ExpressionCalc[] variableInitialValues;

	/**
	 * Zusätzliche Variablennamen, die immer belegt sein sollen.
	 */
	public static final String[] additionalVariables=new String[]{"w","t","p"};

	/**
	 * Variablenwerte in der Statistik erfassen?
	 * @see RunData#updateVariableValueForStatistics(SimulationData, int)
	 */
	public boolean recordVariableValuesToStatistic;

	/**
	 * Werte der globalen Zuordnung in der Statistik erfassen?
	 * @see RunData#updateMapValuesForStatistics(SimulationData)
	 */
	public boolean recordMapValueToStatistic;

	/**
	 * Liste der Modell-Elemente des Laufzeitmodells
	 * @see RunModel#elementsFast
	 */
	public Map<Integer,RunElement> elements;

	/**
	 * Direktzugriff-Liste der Modell-Elemente des Laufzeitmodells
	 * @see RunModel#elements
	 */
	public RunElement[] elementsFast;

	/**
	 * Zuordnung von Modell-Element-Namen zu IDs
	 */
	public TreeMap<String,Integer> namesToIDs;

	/**
	 * Globales Ressourcen-Objekt, welches nur bei der Initialisierung der thread-lokalen Laufzeitdaten als
	 * Kopier-Basis verwendet wird.
	 */
	public RunDataResources resourcesTemplate;

	/**
	 * Globales Transporter-Objekt, welches nur bei der Initialisierung der thread-lokalen Laufzeitdaten als
	 * Kopier-Basis verwendet wird.
	 */
	public RunDataTransporters transportersTemplate;

	/**
	 * Festen Seed für den Zufallszahlengenerator verwenden?
	 * @see #fixedSeed
	 */
	public boolean useFixedSeed;

	/**
	 * Seed für den Zufallszahlengenerator.<br>
	 * Ist nur aktiv, wenn <code>useFixedSeed=true</code> ist.
	 * @see #useFixedSeed
	 */
	public long fixedSeed;

	/**
	 * Maximaler Autokorrelationswert der bei der Erfassung der Daten vorgesehen werden soll.
	 * @see RunModel#correlationMode
	 */
	public int correlationRange;

	/**
	 * Art der Erfassung der Autokorrelation
	 * @see CorrelationMode#CORRELATION_MODE_OFF
	 * @see CorrelationMode#CORRELATION_MODE_FAST
	 * @see CorrelationMode#CORRELATION_MODE_FULL
	 */
	public Statistics.CorrelationMode correlationMode;

	/**
	 * Gibt an ob (bei &gt;1) und wenn ja von welcher Größe die Batches
	 * sein sollen, auf deren Basis Batch-Means berechnet werden sollen.
	 */
	public int batchMeansSize;

	/**
	 * Sollen die individuellen Wartezeiten gespeichert werden?
	 * @see Statistics#clientsAllWaitingTimesCollector
	 */
	public boolean collectWaitingTimes;

	/**
	 * Namen der Fertigungspläne
	 */
	public String[] sequenceNames;

	/**
	 * Liste der Namen der Zielstationen in den einzelnen Schritten der einzelnen Fertigungspläne
	 */
	public String[][] sequenceStepStationNames;

	/**
	 * Liste der IDs der Zielstationen in den einzelnen Schritten der einzelnen Fertigungspläne
	 */
	public int[][] sequenceStepStationIDs;

	/**
	 * 0-basierende Nummern der nächsten Schritte in den einzelnen Schritten der einzelnen Fertigungspläne.<br>
	 * -1 bedeutet dabei, dass kein weiterer Schritt folgt.
	 */
	public int[][] sequenceStepNext;

	/**
	 * ClientData-Nummern für die Zuweisungen pro Schritt
	 */
	public int[][][] sequenceStepAssignmentNr;

	/**
	 * Ausdrücke für die Zuweisungen pro Schritt
	 */
	public String[][][] sequenceStepAssignmentExpression;

	/**
	 * Welcher Sekundenwert soll in der Verteilungsstatistik maximal erfasst werden (Angabe in Stunden)?
	 */
	public int distributionRecordHours;

	/**
	 * Simulation abbrechen, wenn ein Rechenausdruck nicht ausgerechnet werden kann.
	 */
	public boolean stoppOnCalcError;

	/**
	 * Gibt an, ob es sich um eine Animation (<code>true</code>) oder um eine reine Simulation ohne Grafikausgabe (<code>false</code>) handelt
	 */
	public boolean isAnimation;

	/**
	 * Zeitabstand in dem für Bedingung- und ähnliche Stationen zusätzliche zeitabhängige Checks durchgeführt werden sollen.
	 * Werte &le;0 bedeuten, dass keine Checks stattfinden. Sonst ist der Wert die Millisekundenanzahl zwischen zwei Checks.
	 */
	public int timedChecksDelta=-1;

	/**
	 * Zählung wie häufig welche Stationsübergänge stattgefunden haben
	 */
	public boolean recordStationTransitions;

	/**
	 * Erfassung aller Kundenpfade
	 */
	public boolean recordClientPaths;

	/**
	 * Simulation bei einem Scripting-Fehler abbrechen
	 */
	public boolean cancelSimulationOnScriptError;

	/**
	 * Sollen auch Kunden, die das System am Ende noch nicht verlassen haben, in der Statistik erfasst werden können (<code>true</code>). Dies verlangsamt die Simulation.
	 */
	public boolean recordIncompleteClients;

	/**
	 * Verzeichnis für optionale externe Java-Klassendateien
	 * (wird direkt aus {@link EditModel} übernommen und
	 * später von {@link SimulationData} ausgewertet.
	 */
	public String pluginsFolder;

	/**
	 * Einstellungen zu Import und Classpath für Skripte
	 */
	public ImportSettingsBuilder javaImports;

	/**
	 * Ein <code>RunModel</code> kann nicht direkt erzeugt werden, sondern es kann nur ein <code>EditModel</code>
	 * mittels der Funktion <code>getRunModel</code> in ein <code>RunModel</code> umgeformt werden. Dabei wird das
	 * Modell auf Konsistenz geprüft und alle notwendigen Verknüpfungen werden hergestellt.
	 * @see EditModel
	 * @see RunModel#getRunModel(EditModel, boolean, boolean)
	 */
	private RunModel() {
		elements=new HashMap<>();
		namesToIDs=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}

	/**
	 * Liefert den Index den angegebenen Clienttypennamens in dem <code>clientTypes</code>-Array (ohne Berücksichtigung von Groß- und Kleinschreibung)
	 * @param clientTypeName	Name, der dem Array gefunden werden soll
	 * @return	Index des Namens in dem <code>clientTypes</code>-Array oder -1, wenn der Name nicht in dem Array vorkommt
	 * @see #clientTypes
	 */
	public int getClientTypeNr(final String clientTypeName) {
		if (clientTypeName==null) return -1;
		for (int i=0;i<clientTypes.length;i++) if (clientTypeName.equalsIgnoreCase(clientTypes[i])) return i;
		return -1;
	}

	/**
	 * Stellt die Liste der globalen Variablen zusammen und belegt diese mit ihren Startwerten.
	 * @param editModel	Editor-Modell dem die Daten entnommen werden soll
	 * @param runModel	Laufzeit-Modell in das die entsprechenden Daten eingetragen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #getRunModel(EditModel, boolean, boolean)
	 */
	private static String initVariables(final EditModel editModel, final RunModel runModel) {
		/* Variablenliste aufstellen */
		final List<String> variables=new ArrayList<>();
		for (String variable: editModel.globalVariablesNames) {
			boolean inList=false;
			for (String s: variables) if (s.equalsIgnoreCase(variable)) {inList=true; break;}
			for (String s: RunModel.additionalVariables) if (s.equalsIgnoreCase(variable)) {inList=true; break;}
			if (!inList) variables.add(variable);
		}
		for (ModelElement element : editModel.surface.getElements()) if (element instanceof ElementWithNewVariableNames) {
			for (String variable: ((ElementWithNewVariableNames)element).getVariables()) {
				if (CalcSymbolClientUserData.testClientData(variable)>=0) continue;
				if (CalcSymbolClientUserData.testClientDataString(variable)!=null) continue;
				boolean varNameOk=ExpressionCalc.checkVariableName(variable);
				if (!varNameOk) return String.format(Language.tr("Simulation.Creator.InvalidVariableName"),element.getId(),variable);
				boolean inList=false;
				for (String s: variables) if (s.equalsIgnoreCase(variable)) {inList=true; break;}
				for (String s: RunModel.additionalVariables) if (s.equalsIgnoreCase(variable)) {inList=true; break;}
				if (!inList) variables.add(variable);
			}
		}
		variables.addAll(Arrays.asList(RunModel.additionalVariables));
		runModel.variableNames=variables.toArray(new String[0]);

		/* Initiale Werte für Variablen bestimmen */
		runModel.variableInitialValues=new ExpressionCalc[runModel.variableNames.length];
		for (int i=0;i<FastMath.min(editModel.globalVariablesNames.size(),editModel.globalVariablesExpressions.size());i++) {
			final String varName=editModel.globalVariablesNames.get(i);
			final String varExpression=editModel.globalVariablesExpressions.get(i);
			final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
			final int error=calc.parse(varExpression);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidInitialVariableExpression"),varName,varExpression,error+1);
			int index=-1;
			for (int j=0;j<runModel.variableNames.length;j++) if (runModel.variableNames[j].equals(varName)) {index=j; break;}
			if (index>=0) runModel.variableInitialValues[index]=calc;
		}

		/* Konfiguration zur Erfassung der Variablenwerte in der Statistik */
		runModel.recordVariableValuesToStatistic=(editModel.variableRecord==EditModel.VariableRecord.VARIABLES) || (editModel.variableRecord==EditModel.VariableRecord.MAPS_VARIABLES);
		runModel.recordMapValueToStatistic=(editModel.variableRecord==EditModel.VariableRecord.MAPS_VARIABLES);

		return null;
	}

	/**
	 * Überträgt die allgemeinen Daten (Anzahl an Wiederholungen, Einschwingphase usw.) vom Editor- in das Laufzeit-Modell.<br>
	 * Es werden zunächst nur die Daten übertragen, die keine Stationen im Laufzeit-Modell voraussetzen. Die weiteren globalen
	 * Daten werden nach der Erstellung der Stationen von {@link #initGeneralData2(EditModel, RunModel)} übertragen.
	 * @param editModel	Editor-Modell dem die Daten entnommen werden soll
	 * @param runModel	Laufzeit-Modell in das die entsprechenden Daten eingetragen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #getRunModel(EditModel, boolean, boolean)
	 */
	private static String initGeneralData(final EditModel editModel, final RunModel runModel) {
		if (!editModel.useClientCount && !editModel.useFinishTime && !(editModel.useTerminationCondition && !editModel.terminationCondition.trim().isEmpty()) && !editModel.useFinishConfidence) return Language.tr("Simulation.Creator.NoEndCriteria");

		/* Anzahl der zu simulierenden Kundenankünfte */
		if (editModel.useClientCount) {
			if (editModel.clientCount<=0) return String.format(Language.tr("Simulation.Creator.InvalidNumberOfClients"),editModel.clientCount);
			runModel.clientCount=editModel.clientCount;
		} else {
			runModel.clientCount=-1;
		}

		/* Einschwingphase */
		runModel.clientCountModel=Math.max(0,editModel.clientCount);
		if (editModel.warmUpTime<0) return String.format(Language.tr("Simulation.Creator.InvalidWarmUpPeriod"),NumberTools.formatNumber(editModel.warmUpTime));
		runModel.warmUpTime=editModel.warmUpTime;

		/* Wiederholungen der Simulation */
		if (editModel.repeatCount<1) return String.format(Language.tr("Simulation.Creator.InvalidRepeatCount"),editModel.repeatCount);
		if (editModel.repeatCount>1) {
			final String noRepeat=editModel.getNoRepeatReason();
			if (noRepeat!=null) return String.format(Language.tr("Simulation.Creator.NoRepeatCountAllowed"),editModel.repeatCount,noRepeat);
		}
		runModel.repeatCount=editModel.repeatCount;
		runModel.repeatAllowSplit=SetupData.getSetup().useMultiCoreSimulationOnRepeatedSimulations;

		/* Simulation bei Rechenfehlern abbrechen */
		runModel.stoppOnCalcError=editModel.stoppOnCalcError;

		/* Liste der Kundentypen */
		runModel.clientTypes=editModel.surface.getClientTypes().toArray(new String[0]);
		runModel.clientTypesMap=new HashMap<>();
		for (int i=0;i<runModel.clientTypes.length;i++) runModel.clientTypesMap.put(runModel.clientTypes[i],i);

		/* Liste mit den Kosten pro Kundentyp */
		runModel.clientCosts=new double[runModel.clientTypes.length][];
		for (int i=0;i<runModel.clientCosts.length;i++) runModel.clientCosts[i]=editModel.clientData.getCosts(runModel.clientTypes[i]);

		/* List der Kundentypicons */
		runModel.clientTypeIcons=new String[runModel.clientTypes.length];
		for (int i=0;i<runModel.clientTypeIcons.length;i++) runModel.clientTypeIcons[i]=editModel.clientData.getIcon(runModel.clientTypes[i]);

		/* Ressourcen */
		runModel.resourcesTemplate=new RunDataResources();
		String error=runModel.resourcesTemplate.loadFromEditResources(editModel.resources,editModel.schedules,runModel.variableNames);
		if (error!=null) return error;

		/* Transporter */
		runModel.transportersTemplate=new RunDataTransporters();
		error=runModel.transportersTemplate.loadFromEditTransporters(editModel.transporters,editModel.surface,runModel.variableNames);
		if (error!=null) return error;

		/* Sequenzen */
		final int seqCount=editModel.sequences.getSequences().size();
		runModel.sequenceNames=new String[seqCount];
		runModel.sequenceStepStationNames=new String[seqCount][];
		runModel.sequenceStepStationIDs=new int[seqCount][];
		runModel.sequenceStepNext=new int[seqCount][];
		runModel.sequenceStepAssignmentNr=new int[seqCount][][];
		runModel.sequenceStepAssignmentExpression=new String[seqCount][][];
		for (int i=0;i<seqCount;i++) {
			final ModelSequence sequence=editModel.sequences.getSequences().get(i);
			final int stepCount=sequence.getSteps().size();
			if (sequence.getName()==null || sequence.getName().isEmpty()) return String.format(Language.tr("Simulation.Creator.SequenceNoName"),i+1);
			if (stepCount==0) return String.format(Language.tr("Simulation.Creator.SequenceNoSteps"),sequence.getName());
			runModel.sequenceNames[i]=sequence.getName();
			runModel.sequenceStepStationNames[i]=new String[stepCount];
			runModel.sequenceStepStationIDs[i]=new int[stepCount];
			runModel.sequenceStepNext[i]=new int[stepCount];
			runModel.sequenceStepAssignmentNr[i]=new int[stepCount][];
			runModel.sequenceStepAssignmentExpression[i]=new String[stepCount][];
			for (int j=0;j<stepCount;j++) {
				final ModelSequenceStep step=sequence.getSteps().get(j);
				if (step.getTarget()==null || step.getTarget().isEmpty()) return String.format(Language.tr("Simulation.Creator.SequenceStepNoTarget"),sequence.getName(),j+1);
				runModel.sequenceStepStationNames[i][j]=step.getTarget(); /* Zuordnung der IDs erfolgt erst in Schritt 2 in initGeneralData2 */
				int next=step.getNext();
				if (next<0) {
					next=(j==stepCount-1)?-1:(j+1);
				} else {
					if (next>=stepCount) return String.format(Language.tr("Simulation.Creator.SequenceInvalidNextStep"),sequence.getName(),j+1,next+1);
				}
				runModel.sequenceStepNext[i][j]=next;
				final int assignCount=step.getAssignments().size();
				runModel.sequenceStepAssignmentNr[i][j]=new int[assignCount];
				runModel.sequenceStepAssignmentExpression[i][j]=new String[assignCount];
				int k=0;
				for (Map.Entry<Integer,String> entry: step.getAssignments().entrySet()) {
					runModel.sequenceStepAssignmentNr[i][j][k]=entry.getKey();
					runModel.sequenceStepAssignmentExpression[i][j][k]=entry.getValue();
					final int exprError=ExpressionCalc.check(entry.getValue(),editModel.surface.getMainSurfaceVariableNames(editModel.getModelVariableNames(),false));
					if (exprError>=0) return String.format(Language.tr("Simulation.Creator.SequenceInvalidExpression"),sequence.getName(),j+1,runModel.sequenceStepAssignmentNr[i][j][k],entry.getValue(),exprError+1);
					k++;
				}
			}
		}

		/* Zeitabstand in dem für Bedingung- und ähnliche Stationen zusätzliche zeitabhängige Checks durchgeführt werden sollen. */
		runModel.timedChecksDelta=editModel.timedChecksDelta;

		/* Aufzeichnung der Kundenbewegungen */
		runModel.recordStationTransitions=editModel.recordStationTransitions;
		runModel.recordClientPaths=editModel.recordClientPaths;

		/* Scripting */
		runModel.cancelSimulationOnScriptError=SetupData.getSetup().cancelSimulationOnScriptError;

		/* Sollen auch Kunden, die das System am Ende noch nicht verlassen haben, in der Statistik erfasst werden? */
		runModel.recordIncompleteClients=editModel.recordIncompleteClients;

		/* Plugins-Verzeichnis */
		runModel.pluginsFolder=editModel.pluginsFolder;

		/* Optionale nutzerdefinierte Imports */
		runModel.javaImports=new ImportSettingsBuilder(editModel);

		return null;
	}

	/**
	 * Liefert die Anzahl, wie viele Kunden insgesamt eintreffen werden.<br>
	 * Bei Modellen, die nur Tabellenquellen verwenden, ist dies das Minimum aus der Summe aller Tabellenzeilen und {@link RunModel#clientCount}. Bei allen anderen Modellen ist dies stets {@link RunModel#clientCount}.
	 * @param runModel	Laufzeitmodell
	 * @return	Anzahl an insgesamt eintreffenden Kunden
	 * @see #realArrivingClientCount
	 */
	private static long getArrivingRealClientCount(final RunModel runModel) {
		long sum=0;
		for (RunElement element: runModel.elementsFast) {
			if (element instanceof RunSource) {
				if (!(element instanceof RunElementSourceTable)) return runModel.clientCount;
				final long value=((RunElementSourceTable)element).getArrivalCount();
				if (value<0) return runModel.clientCount; /* Quelle hat die eigenen Daten noch nicht geladen. */
				sum+=value;
			}
		}
		return Math.min(sum,runModel.clientCount);
	}

	/**
	 * Überträgt den Teil der allgemeinen Daten, der voraussetzt, dass sich bereits Stationen im Laufzeit-Modell befinden, vom Editor- in das Laufzeit-Modell.
	 * @param editModel	Editor-Modell dem die Daten entnommen werden soll
	 * @param runModel	Laufzeit-Modell in das die entsprechenden Daten eingetragen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #getRunModel(EditModel, boolean, boolean)
	 */
	private static String initGeneralData2(final EditModel editModel, final RunModel runModel) {
		/* Evtl. treffen weniger Kunden ein, als eingestellt ist (nämlich wenn nur Tabellenquellen verwendet werden). */
		runModel.realArrivingClientCount=getArrivingRealClientCount(runModel);

		/* Abbruchbedingung */
		if (editModel.useTerminationCondition && !editModel.terminationCondition.trim().isEmpty()) {
			runModel.terminationCondition=new ExpressionEval(runModel.variableNames);
			final int error=runModel.terminationCondition.parse(editModel.terminationCondition);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidTerminationCondition"),editModel.terminationCondition,error+1);
		} else {
			runModel.terminationCondition=null;
		}

		/* Abbruchzeit */
		if (editModel.useFinishTime) {
			runModel.terminationTime=FastMath.max(0,editModel.finishTime);
		} else {
			runModel.terminationTime=-1;
		}

		/* Abbruch über Konfidenzintervall */
		if (editModel.useFinishConfidence && editModel.finishConfidenceHalfWidth>0 && editModel.finishConfidenceLevel>0) {
			runModel.terminationWaitingTimeConfidenceHalfWidth=editModel.finishConfidenceHalfWidth;
			runModel.terminationWaitingTimeConfidenceLevel=editModel.finishConfidenceLevel;
		} else {
			runModel.terminationWaitingTimeConfidenceHalfWidth=-1;
			runModel.terminationWaitingTimeConfidenceLevel=-1;
		}

		/* Seed für Zufallszahlengenerator */
		runModel.useFixedSeed=editModel.useFixedSeed;
		runModel.fixedSeed=editModel.fixedSeed;

		/* Bestimmung der Autokorrelation */
		runModel.correlationRange=editModel.correlationRange;
		runModel.correlationMode=editModel.correlationMode;

		/* Batch-Means */
		runModel.batchMeansSize=editModel.batchMeansSize;

		/* Erfassung individueller Wartezeiten */
		runModel.collectWaitingTimes=editModel.collectWaitingTimes;

		/* Sequenzen - Stationen verdrahten */
		for (int i=0;i<runModel.sequenceStepStationNames.length;i++) {
			for (int j=0;j<runModel.sequenceStepStationNames[i].length;j++) {
				final Integer I=runModel.namesToIDs.get(runModel.sequenceStepStationNames[i][j]);
				if (I==null) return String.format(Language.tr("Simulation.Creator.SequenceUnknownStation"),runModel.sequenceNames[i],j+1,runModel.sequenceStepStationNames[i][j]);
				runModel.sequenceStepStationIDs[i][j]=I.intValue();
			}
		}

		/* Erfassungsbereich für Verteilungen */
		runModel.distributionRecordHours=editModel.distributionRecordHours;

		return null;
	}

	/**
	 * Ist bei einer Kundenquelle die Anzahl an Ankünften limitiert?
	 * @param record	Kundenquellen-Datensatz
	 * @return	Liefert <code>true</code>, wenn entweder nur eine fest endliche Anzahl an Kunden eintreffen soll oder eine feste endliche Anzahl an Batchen (die ihrerseitg begrenzt groß sind) eintreffen soll
	 */
	private static boolean isLimitedSource(final ModelElementSourceRecord record) {
		return (record.getMaxArrivalClientCount()>0 || record.getMaxArrivalCount()>0);
	}

	/**
	 * Gibt an, ob die Übertragung vom Editor- zum Laufzeit-Element
	 * für das angegebene Element im Hintergrund erfolgen kann.
	 * @param element	Zu prüfendes Element
	 * @return	Liefert <code>true</code>, wenn die Übertragung im Hintergrund erfolgen kann
	 */
	private static boolean runInBackgroundThread(final ModelElementBox element) {
		if (element instanceof ElementWithScript) return true;
		if (element instanceof ModelElementSourceTable) return true;
		return false;
	}

	/**
	 * Ermittelt aus der Fehlermeldung selbst weitere Fehler-Flags
	 * @param errorMessage	Fehlermeldung
	 * @return	Menge mit optionalen zusätzlichen Fehler-Flags
	 * @see AdditionalPrepareErrorInfo
	 */
	private static Set<StartAnySimulator.AdditionalPrepareErrorInfo> generateAdditionalScriptErrorInfo(final String errorMessage) {
		if (errorMessage.contains(Language.tr("Simulation.Java.Error.NoCompiler.Internal"))) {
			return new HashSet<>(Arrays.asList(StartAnySimulator.AdditionalPrepareErrorInfo.NO_COMPILER));
		}
		return Collections.emptySet();
	}

	/**
	 * Zähler für die Nummerierung der Hintergrund-Kompiler-Threads
	 */
	private static int prepareThreadNr=0;

	/**
	 * Überträgt die Stationen aus dem Editor-Modell in das Laufzeit-Modell.
	 * @param editModel	Editor-Modell dem die Daten entnommen werden soll
	 * @param runModel	Laufzeit-Modell in das die entsprechenden Daten eingetragen werden sollen
	 * @param testOnly	Wird hier <code>true</code> übergeben, so werden externe Datenquellen nicht wirklich geladen
	 * @param allowBackgroundProcessing	Darf die Vorbereitung von benutzerdefiniertem Java-Code und von externen Tabellenquelle in eigene Threads ausgelagert werden?
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #getRunModel(EditModel, boolean, boolean)
	 */
	private static StartAnySimulator.PrepareError initElementsData(final EditModel editModel, final RunModel runModel, final boolean testOnly, final boolean allowBackgroundProcessing) {
		final RunModelCreator creator=new RunModelCreator(editModel,runModel,testOnly);
		final List<ModelElement> elements=new ArrayList<>(editModel.surface.getElements());

		/* Skript-Elemente auf der Hauptebene verarbeiten */
		prepareThreadNr=0;
		final int coreCount=Runtime.getRuntime().availableProcessors();
		final int maxThreadsByMemory=(int)Math.max(1,Runtime.getRuntime().maxMemory()/1024/1024/100); /* min. 100 MB pro Thread */
		final int threadCount=Math.min(coreCount,maxThreadsByMemory);
		final ThreadPoolExecutor executorPool=new ThreadPoolExecutor(threadCount,threadCount,2,TimeUnit.SECONDS,new LinkedBlockingQueue<>(),(ThreadFactory)r-> {
			prepareThreadNr++;
			return new Thread(r,"Prepare model for simulation "+prepareThreadNr);
		});
		final List<Future<StartAnySimulator.PrepareError>> scriptProcessor=new ArrayList<>();

		try {

			/* Normale Elemente verarbeiten */
			boolean hasSource=false;
			boolean hasDispose=false;
			boolean allSourcesLimited=true;
			for (ModelElement element : elements) {
				if (element instanceof ModelElementBox) {
					final ModelElementBox boxElement=(ModelElementBox)element;
					if (!boxElement.inputConnected()) continue; /* Keine einlaufende Ecke in Element -> kann ignoriert werden */

					if (element instanceof ModelElementSource) {
						hasSource=true;
						allSourcesLimited=allSourcesLimited && isLimitedSource(((ModelElementSource)element).getRecord());
					}
					if (element instanceof ModelElementAnimationConnect) runModel.isAnimation=true;
					if (element instanceof ModelElementSourceMulti && !((ModelElementSourceMulti)element).getRecords().isEmpty()) {
						hasSource=true;
						for (ModelElementSourceRecord record: ((ModelElementSourceMulti)element).getRecords()) {
							if (!record.isActive()) continue;
							allSourcesLimited=allSourcesLimited && isLimitedSource(record);
						}
					}
					if (element instanceof ModelElementSourceTable) hasSource=true;
					if (element instanceof ModelElementSourceDB) hasSource=true;
					if (element instanceof ModelElementSourceDDE) hasSource=true;
					if (element instanceof ModelElementDispose) hasDispose=true;
					if (element instanceof ModelElementDisposeWithTable) hasDispose=true;

					if (allowBackgroundProcessing && !testOnly && runInBackgroundThread(boxElement)) {
						scriptProcessor.add(executorPool.submit(()->{
							final String err=creator.addElement(boxElement);
							if (err==null) return null;
							return new StartAnySimulator.PrepareError(err,boxElement.getId(),generateAdditionalScriptErrorInfo(err));
						}));
					} else {
						final String error=creator.addElement(boxElement);
						if (error!=null) return new StartAnySimulator.PrepareError(error,boxElement.getId(),generateAdditionalScriptErrorInfo(error));
					}

					if (element instanceof ModelElementSub) {
						final ModelElementSub sub=(ModelElementSub)element;
						final List<ModelElement> subElements=sub.getSubSurfaceReadOnly().getElements();
						for (ModelElement subElement: subElements) if (subElement instanceof ModelElementBox) {
							final ModelElementBox subBox=(ModelElementBox)subElement;
							if (allowBackgroundProcessing && !testOnly && runInBackgroundThread(subBox)) {
								scriptProcessor.add(executorPool.submit(()->{
									final String err=creator.addElement(subBox,sub);
									if (err==null) return null;
									return new StartAnySimulator.PrepareError(err,subBox.getId(),generateAdditionalScriptErrorInfo(err));
								}));
							} else {
								final String error=creator.addElement(subBox,sub);
								if (error!=null) return new StartAnySimulator.PrepareError(error,subBox.getId(),generateAdditionalScriptErrorInfo(error));
							}
						}
					}
				} else {
					if ((element instanceof InteractiveElement) && (element instanceof ModelElementPosition)) {
						final String error=creator.addElement((ModelElementPosition)element);
						if (error!=null) return new StartAnySimulator.PrepareError(error,element.getId(),generateAdditionalScriptErrorInfo(error));
					}
				}
			}

			/* Sind Eingang und Ausgang vorhanden? */
			if (!hasSource) return new StartAnySimulator.PrepareError(Language.tr("Simulation.Creator.NoSource"),-1);
			if (!hasDispose) {
				if (!allSourcesLimited || (!editModel.useFinishTime && !editModel.useTerminationCondition)) return new StartAnySimulator.PrepareError(Language.tr("Simulation.Creator.NoDispose"),-1);
			}

			/* Hintergrundverarbeitungen abschließen */
			for (Future<StartAnySimulator.PrepareError> future: scriptProcessor) try {
				final StartAnySimulator.PrepareError error=future.get();
				if (error!=null) return error;
			} catch (InterruptedException|ExecutionException e) {
				return new StartAnySimulator.PrepareError(e.getMessage(),-1);
			}

		} finally {
			executorPool.shutdown();
		}

		/* Verknüpfungen umstellen von IDs auf Referenzen */
		int maxID=0;
		for (Map.Entry<Integer,RunElement> entry : runModel.elements.entrySet()) {
			final RunElement element=entry.getValue();
			if (element.id<0) return new StartAnySimulator.PrepareError(String.format(Language.tr("Simulation.Creator.NegativeID"),element.getClass().getName()),element.id);
			if (element.id>maxID) maxID=element.id;
			element.prepareRun(runModel);
		}

		/* Direkter Zugriff auf die Elemente über ihre IDs */
		runModel.elementsFast=new RunElement[maxID+1];
		for (int i=0;i<=maxID;i++) runModel.elementsFast[i]=runModel.elements.get(i);

		return null;
	}

	/**
	 * Überträgt die Daten zur Laufzeitstatistik-Erfassung vom Editor- in das Laufzeit-Modell.
	 * @param editModel	Editor-Modell dem die Daten entnommen werden soll
	 * @param runModel	Laufzeit-Modell in das die entsprechenden Daten eingetragen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see #getRunModel(EditModel, boolean, boolean)
	 */
	private static String initAdditionalStatistics(final EditModel editModel, final RunModel runModel) {
		if (!editModel.longRunStatistics.isActive()) return null;

		final RunModelCreator creator=new RunModelCreator(editModel,runModel,false);
		return creator.addLongRunStatistic();
	}

	/**
	 * Wandelt ein <code>EditModel</code> in ein <code>RunModel</code> um. Dabei wird das Modell auf Konsistenz geprüft
	 * und alle notwendigen Verknüpfungen werden hergestellt.
	 * @param editModel	Editor-Modell, welches in ein Laufzeit-Modell umgewandelt werden soll
	 * @param testOnly	Wird hier <code>true</code> übergeben, so werden externe Datenquellen nicht wirklich geladen
	 * @param allowBackgroundProcessing	Darf die Vorbereitung von benutzerdefiniertem Java-Code und von externen Tabellenquelle in eigene Threads ausgelagert werden?
	 * @return	Gibt im Erfolgsfall ein Objekt vom Typ <code>RunModel</code> zurück, sonst <code>PrepareError</code>-Objekt mit einer Fehlermeldung.
	 * @see EditModel
	 */
	public static Object getRunModel(final EditModel editModel, final boolean testOnly, final boolean allowBackgroundProcessing) {
		RunModel runModel=new RunModel();

		String error;
		error=initVariables(editModel,runModel); if (error!=null) return new StartAnySimulator.PrepareError(error,-1);
		error=initGeneralData(editModel,runModel); if (error!=null) return new StartAnySimulator.PrepareError(error,-1);
		final StartAnySimulator.PrepareError prepareError=initElementsData(editModel,runModel,testOnly,allowBackgroundProcessing); if (prepareError!=null) return prepareError;
		error=initGeneralData2(editModel,runModel); if (error!=null) return new StartAnySimulator.PrepareError(error,-1); /* Hier brauchen wir die Variablennamen und die werden erst in initElementsData gesetzt. */
		error=initAdditionalStatistics(editModel,runModel); if (error!=null) return new StartAnySimulator.PrepareError(error,-1);

		return runModel;
	}

	/**
	 * Liefert die Nummer eines Fertigungsplans
	 * @param name	Name des Fertigungsplans
	 * @return	Nummer des Plans (oder -1, wenn kein Plan mit dem Namen gefunden wurde)
	 * @see RunModel#sequenceNames
	 */
	public int getSequenceNr(final String name) {
		if (name.isEmpty()) return -1;
		for (int i=0;i<sequenceNames.length;i++) if (sequenceNames[i].equalsIgnoreCase(name)) return i;
		return -1;
	}
}