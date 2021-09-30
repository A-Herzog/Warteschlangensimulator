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
package ui.infopanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import tools.SetupData;
import ui.images.Images;
import ui.modeleditor.ModelElementCatalog;
import ui.tools.FlatLaFHelper;

/**
 * Diese Klasse ermöglicht das Anzeigen von Informationstexten in Dialogen.
 * Es kann dabei für jeden Dialog einzelnen eingestellt werden, ob der
 * Hinweis angezeigt werden soll oder nicht.
 * @author Alexander Herzog
 */
public class InfoPanel {
	/** Teil-ID für Infos zu Fenstern */
	private static final String groupWindow="Window|";
	/** Teil-ID für Infos zu Modelleigenschaften-Dialogseiten */
	private static final String groupModel="EditorPanelDialog|";
	/** Teil-ID für Parameterreihen-Panel */
	private static final String groupParameterSeries="ParameterSeriesPanel|";
	/** Teil-ID für Infos zu Optimierer-Dialogseiten */
	private static final String groupOptimizer="OptimizerPanel|";
	/** Teil-ID für Infos zu Modellelementen */
	private static final String groupElement="Element|";

	/** Info-ID für die Willkommensseite */
	public static final String globalWelcome=groupWindow+"Welcome";

	/** Info-ID für den "Kante hinzufügen"-Hinweis */
	public static final String globalAddEdge=groupWindow+"AddEdge";

	/* "Modelleigenschaften"-Dialog" */

	/** Info-ID für die Dialogseite "Modellbeschreibung" im Modelleigenschaften-Dialog */
	public static final String modelDescription=groupModel+"ModelDescription";

	/** Info-ID für die Dialogseite "Simulation" im Modelleigenschaften-Dialog */
	public static final String modelSimulation=groupModel+"Simulation";

	/** Info-ID für die Dialogseite "Kunden" im Modelleigenschaften-Dialog */
	public static final String modelClients=groupModel+"Clients";

	/** Info-ID für die Dialogseite "Bediener" im Modelleigenschaften-Dialog */
	public static final String modelOperators=groupModel+"Operators";

	/** Info-ID für die Dialogseite "Transporter" im Modelleigenschaften-Dialog */
	public static final String modelTransporters=groupModel+"Transporters";

	/** Info-ID für die Dialogseite "Zeitpläne" im Modelleigenschaften-Dialog */
	public static final String modelSchedule=groupModel+"Schedule";

	/** Info-ID für die Dialogseite "Fertigungspläne" im Modelleigenschaften-Dialog */
	public static final String modelSequences=groupModel+"Sequences";

	/** Info-ID für die Dialogseite "Initiale Variablenwerte" im Modelleigenschaften-Dialog */
	public static final String modelInitialValues=groupModel+"InitialVariableValues";

	/** Info-ID für den Dialog "Startwerte für globale Zuordnung" auf der Dialogseite "Initiale Variablenwerte" im Modelleigenschaften-Dialog */
	public static final String modelInitialValuesMap=groupModel+"InitialVariableValuesMap";

	/** Info-ID für die Dialogseite "Laufzeitstatistik" im Modelleigenschaften-Dialog */
	public static final String modelRunTimeStatistics=groupModel+"RunTimeStatistics";

	/** Info-ID für die Dialogseite "Ausgabeanalyse" im Modelleigenschaften-Dialog */
	public static final String modelOutputAnalysis=groupModel+"OutputAnalysis";

	/** Info-ID für die Dialogseite "Pfadaufzeichnung" im Modelleigenschaften-Dialog */
	public static final String modelPathRecording=groupModel+"PathRecording";

	/** Info-ID für die Dialogseite "Simulationssystem" im Modelleigenschaften-Dialog */
	public static final String modelSimulationSystem=groupModel+"SimulationSystem";

	/* Parameterreihe */

	/** Info-ID für den Basismodell-Ersetzen-Dialog im Parameterreihen-Panel */
	public static final String parameterSeriesReplaceModel=groupParameterSeries+"ReplaceModel";

	/* Optimierer-Panel */

	/** Info-ID für die Dialogseite "Kontrollvariable" im Optimierer-Panel */
	public static final String optimizerControlVariables=groupOptimizer+"ControlVariables";

	/** Info-ID für die Dialogseite "Ziel" im Optimierer-Panel */
	public static final String optimizerTarget=groupOptimizer+"Target";

	/** Info-ID für die Dialogseite "Optimierung" im Optimierer-Panel */
	public static final String optimizerOptimization=groupOptimizer+"Optimization";

	/* Weitere Dialoge */

	/** Info-ID für den "Einfaches Modell erstellen"-Dialog */
	public static final String globalGenerator="ModelGeneratorDialog";

	/** Info-ID für den "Beispiel auswählen"-Dialog */
	public static final String globalSelectExample="ModelSelectExampleDialog";

	/** Info-ID für den "Transporterstrecken bearbeiten"-Dialog */
	public static final String globalPathEditor="PathEditorDialog";

	/** Info-ID für das Parameterreihen-Panel */
	public static final String globalParameterCompare="ParameterComparePanel";

	/** Info-ID für das "Skript ausführen"-Panel */
	public static final String globalScriptRunner="ScriptRunner";

	/** Info-ID für den "Simulationsergebnisse vergleichen"-Dialog */
	public static final String globlCompare="Compare";

	/** Info-ID für den "Externe Modelldaten bearbeiten"-Dialog */
	public static final String globalModelLoadData="ModelLoadDataDialog";

	/** Info-ID für den "Element suchen"-Dialog */
	public static final String globalFindElement="FindElement";

	/** Info-ID für den "Verteilung anpassen"-Dialog */
	public static final String globalFit="Fit";

	/** Info-ID für den "Varianzanalyse"-Dialog */
	public static final String globalVarianceAnalysis="VarianceAnalysis";

	/** Info-ID für den "Stationsstatistik"-Dialog */
	public static final String globalStationStatistics="StationStatistics";

	/** Info-ID für den "Notizen"-Dialog */
	public static final String globalNotes="Notes";

	/** Info-ID für den "Modellbeschreibung"-Dialog */
	public static final String globalModelDescription="ModelDescription";

	/** Info-ID für den "Vergleich mit analytischem Modell" */
	public static final String globalAnalyticModelCompare="AnalyticModelCompare";

	/** Info-ID für den "Ebenen"-Dialog */
	public static final String globalLayers="Layers";

	/** Info-ID für den "Git-Konfigurationen"-Dialog */
	public static final String globalGit="Git";

	/** Info-ID für den "Tabelle für Tabellenquelle aufbereiten"-Dialog */
	public static final String globalProcessClientTable="ProcessClientTable";

	/** Info-ID für den "Imports für nutzerdefinierten Java-Code"-Dialog */
	public static final String globalJavaImports="JavaImports";

	/* Stationen - Eingang/Ausgang */

	/** Info-ID für den "Quelle"-Bearbeiten-Dialog (Gruppe "Eingang/Ausgang") */
	public static final String stationSource=groupElement+"Source";

	/** Info-ID für den "Mehrfachquelle"-Bearbeiten-Dialog (Gruppe "Eingang/Ausgang") */
	public static final String stationSourceMulti=groupElement+"SourceMulti";

	/** Info-ID für den "Tabellenquelle"-Bearbeiten-Dialog (Gruppe "Eingang/Ausgang") */
	public static final String stationSourceTable=groupElement+"SourceTable";

	/** Info-ID für den "Datenbankquelle"-Bearbeiten-Dialog (Gruppe "Eingang/Ausgang") */
	public static final String stationSourceDB=groupElement+"SourceDB";

	/** Info-ID für den "DDE-Quelle"-Bearbeiten-Dialog (Gruppe "Eingang/Ausgang") */
	public static final String stationSourceDDE=groupElement+"SourceDDE";

	/** Info-ID für den "Ausgang"-Bearbeiten-Dialog (Gruppe "Eingang/Ausgang") */
	public static final String stationDispose=groupElement+"Dispose";

	/** Info-ID für den "Ausgang mit Speicherung"-Bearbeiten-Dialog (Gruppe "Eingang/Ausgang") */
	public static final String stationDisposeWithTable=groupElement+"DisposeWithTable";

	/* Stationen - Verarbeitung */

	/** Info-ID für den "Bedienstation"-Bearbeiten-Dialog (Gruppe "Verarbeitung") */
	public static final String stationProcess=groupElement+"Process";

	/** Info-ID für den "Verzögerung"-Bearbeiten-Dialog (Gruppe "Verarbeitung") */
	public static final String stationDelay=groupElement+"Delay";

	/* Stationen - Zuweisungen */

	/** Info-ID für den "Typzuweisung"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationAssign=groupElement+"Assign";

	/** Info-ID für den "Textzuweisung"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationAssignString=groupElement+"AssignString";

	/** Info-ID für den "Kosten"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationCosts=groupElement+"Costs";

	/** Info-ID für den "Variable"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationSet=groupElement+"Set";

	/** Info-ID für den "Script"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationSetJS=groupElement+"SetJS";

	/** Info-ID für den "Zähler"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationCounter=groupElement+"Counter";

	/** Info-ID für den "Multizähler"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationCounterMulti=groupElement+"CounterMulti";

	/** Info-ID für den "Durchsatz"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationThroughput=groupElement+"Throughput";

	/** Info-ID für den "Batch-Zähler"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationCounterBatch=groupElement+"CounterBatch";

	/** Info-ID für den "Zustand"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationStateStatistics=groupElement+"StateStatistics";

	/** Info-ID für den "Differenzzähler"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationDifferentialCounter=groupElement+"DifferentialCounter";

	/** Info-ID für den "Bereich betreten"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationSectionStart=groupElement+"SectionStart";

	/** Info-ID für den "Bereich verlassen"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationSectionEnd=groupElement+"SectionEnd";

	/** Info-ID für den "Kundenstatistik"-Bearbeiten-Dialog (Gruppe "Zuweisungen") */
	public static final String stationSetStatisticsMode=groupElement+"SetStatisticsMode";

	/* Stationen - Verzweigungen */

	/** Info-ID für den "Duplizieren"-Bearbeiten-Dialog (Gruppe "Verzweigungen") */
	public static final String stationDuplicate=groupElement+"Duplicate";

	/** Info-ID für den "Verzweigen"-Bearbeiten-Dialog (Gruppe "Verzweigungen") */
	public static final String stationDecide=groupElement+"Decide";

	/** Info-ID für den "Verzweigen (Skript)"-Bearbeiten-Dialog (Gruppe "Verzweigungen") */
	public static final String stationDecideJS=groupElement+"DecideJS";

	/** Info-ID für den "Zurückschrecken"-Bearbeiten-Dialog (Gruppe "Verzweigungen") */
	public static final String stationBalking=groupElement+"Balking";

	/* Stationen - Schranken */

	/** Info-ID für den "Bedingung"-Bearbeiten-Dialog (Gruppe "Schranken") */
	public static final String stationHold=groupElement+"Hold";

	/** Info-ID für den "Multibedingung"-Bearbeiten-Dialog (Gruppe "Schranken") */
	public static final String stationHoldMulti=groupElement+"HoldMulti";

	/** Info-ID für den "Bedingung (Skript)"-Bearbeiten-Dialog (Gruppe "Schranken") */
	public static final String stationHoldJS=groupElement+"HoldJS";

	/** Info-ID für den "Signal"-Bearbeiten-Dialog (Gruppe "Schranken") */
	public static final String stationSignal=groupElement+"Signal";

	/** Info-ID für den "Schranke"-Bearbeiten-Dialog (Gruppe "Schranken") */
	public static final String stationBarrier=groupElement+"Barrier";

	/** Info-ID für den "Pull-Schranke"-Bearbeiten-Dialog (Gruppe "Schranken") */
	public static final String stationBarrierPull=groupElement+"BarrierPull";

	/** Info-ID für den "Ressource belegen"-Bearbeiten-Dialog (Gruppe "Schranken") */
	public static final String stationSeize=groupElement+"Seize";

	/** Info-ID für den "Ressource freigeben"-Bearbeiten-Dialog (Gruppe "Schranken") */
	public static final String stationRelease=groupElement+"Release";

	/* Stationen - Kunden verbinden */

	/** Info-ID für den "Zusammenfassen"-Bearbeiten-Dialog (Gruppe "Kunden verbinden") */
	public static final String stationBatch=groupElement+"Batch";

	/** Info-ID für den "Mehrfach-Zusammenfassen"-Bearbeiten-Dialog (Gruppe "Kunden verbinden") */
	public static final String stationBatchMulti=groupElement+"BatchMulti";

	/** Info-ID für den "Trennen"-Bearbeiten-Dialog (Gruppe "Kunden verbinden") */
	public static final String stationSeparate=groupElement+"Separate";

	/** Info-ID für den "Zusammenführen"-Bearbeiten-Dialog (Gruppe "Kunden verbinden") */
	public static final String stationMatch=groupElement+"Match";

	/** Info-ID für den "Ausleiten"-Bearbeiten-Dialog (Gruppe "Kunden verbinden") */
	public static final String stationPickUp=groupElement+"PickUp";

	/** Info-ID für den "Zerteilen"-Bearbeiten-Dialog (Gruppe "Kunden verbinden") */
	public static final String stationSplit=groupElement+"Split";

	/* Stationen - Transport */

	/** Info-ID für den "Transportstart"-Bearbeiten-Dialog (Gruppe "Transport") */
	public static final String stationTransportSource=groupElement+"TransportSource";

	/** Info-ID für den "Haltestelle"-Bearbeiten-Dialog (Gruppe "Transport") */
	public static final String stationTransportTransporterSource=groupElement+"TransportTransporterSource";

	/** Info-ID für den "Transportziel"-Bearbeiten-Dialog (Gruppe "Transport") */
	public static final String stationTransportDestination=groupElement+"TransportDestination";

	/** Info-ID für den "Parkplatz"-Bearbeiten-Dialog (Gruppe "Transport") */
	public static final String stationTransportParking=groupElement+"TransportParking";

	/** Info-ID für den "Plan zuweisen"-Bearbeiten-Dialog (Gruppe "Transport") */
	public static final String stationAssignSequence=groupElement+"AssignSequence";

	/** Info-ID für den "Transporter Wegpunkt"-Bearbeiten-Dialog (Gruppe "Transport") */
	public static final String stationWayPoint=groupElement+"WayPoint";

	/** Info-ID für den "Teleport-Transport Startpunkt"-Bearbeiten-Dialog (Gruppe "Transport") */
	public static final String stationTeleportSource=groupElement+"TeleportSource";

	/** Info-ID für den "Teleport-Transport Zielpunkt"-Bearbeiten-Dialog (Gruppe "Transport") */
	public static final String stationTeleportDestination=groupElement+"TeleportDestination";

	/** Info-ID für den "Fließband"-Bearbeiten-Dialog (Gruppe "Transport") */
	public static final String stationConveyor=groupElement+"Conveyor";

	/* Stationen - Daten Ein-/Ausgabe */

	/** Info-ID für den "Eingabe"-Bearbeiten-Dialog (Gruppe "Daten Ein-/Ausgabe") */
	public static final String stationInput=groupElement+"Input";

	/** Info-ID für den "Eingabe (Skript)"-Bearbeiten-Dialog (Gruppe "Daten Ein-/Ausgabe") */
	public static final String stationInputJS=groupElement+"InputJS";

	/** Info-ID für den "Eingabe (SB)"-Bearbeiten-Dialog (Gruppe "Daten Ein-/Ausgabe") */
	public static final String stationInputDB=groupElement+"InputDB";

	/** Info-ID für den "Eingabe (DDE)"-Bearbeiten-Dialog (Gruppe "Daten Ein-/Ausgabe") */
	public static final String stationInputDDE=groupElement+"InputDDE";

	/** Info-ID für den "Ausgabe"-Bearbeiten-Dialog (Gruppe "Daten Ein-/Ausgabe") */
	public static final String stationOutput=groupElement+"Output";

	/** Info-ID für den "Ausgabe (Skript)"-Bearbeiten-Dialog (Gruppe "Daten Ein-/Ausgabe") */
	public static final String stationOutputJS=groupElement+"OutputJS";

	/** Info-ID für den "Ausgabe (DB)"-Bearbeiten-Dialog (Gruppe "Daten Ein-/Ausgabe") */
	public static final String stationOutputDB=groupElement+"OutputDB";

	/** Info-ID für den "Ausgabe (DDE)"-Bearbeiten-Dialog (Gruppe "Daten Ein-/Ausgabe") */
	public static final String stationOutputDDE=groupElement+"OutputDDE";

	/** Info-ID für den "Ausgabe (Log)"-Bearbeiten-Dialog (Gruppe "Daten Ein-/Ausgabe") */
	public static final String stationOutputLog=groupElement+"OutputLog";

	/** Info-ID für den "Aufzeichnung"-Bearbeiten-Dialog (Gruppe "Daten Ein-/Ausgabe") */
	public static final String stationRecord=groupElement+"Record";

	/* Stationen - Flusssteuerungslogik */

	/** Info-ID für den "If"-Bearbeiten-Dialog (Gruppe "Flusssteuerungslogik") */
	public static final String stationLogicIf=groupElement+"LogicIf";

	/** Info-ID für den "ElseIf"-Bearbeiten-Dialog (Gruppe "Flusssteuerungslogik") */
	public static final String stationLogicElseIf=groupElement+"LogicElseIf";

	/** Info-ID für den "Else"-Bearbeiten-Dialog (Gruppe "Flusssteuerungslogik") */
	public static final String stationLogicElse=groupElement+"LogicElse";

	/** Info-ID für den "EndIf"-Bearbeiten-Dialog (Gruppe "Flusssteuerungslogik") */
	public static final String stationLogicEndIf=groupElement+"LogicEndIf";

	/** Info-ID für den "While"-Bearbeiten-Dialog (Gruppe "Flusssteuerungslogik") */
	public static final String stationLogicWhile=groupElement+"LogicWhile";

	/** Info-ID für den "EndWhile"-Bearbeiten-Dialog (Gruppe "Flusssteuerungslogik") */
	public static final String stationLogicEndWhile=groupElement+"LogicEndWhile";

	/** Info-ID für den "Do"-Bearbeiten-Dialog (Gruppe "Flusssteuerungslogik") */
	public static final String stationLogicDo=groupElement+"LogicDo";

	/** Info-ID für den "Until"-Bearbeiten-Dialog (Gruppe "Flusssteuerungslogik") */
	public static final String stationLogicUntil=groupElement+"LogicUntil";

	/* Stationen - Analoge Werte */

	/** Info-ID für den "Analoger Wert"-Bearbeiten-Dialog (Gruppe "Analoge Werte") */
	public static final String stationAnalogValue=groupElement+"AnalogValue";

	/** Info-ID für den "Analogen W. ändern"-Bearbeiten-Dialog (Gruppe "Analoge Werte") */
	public static final String stationAnalogAssign=groupElement+"AnalogAssign";

	/** Info-ID für den "Tank"-Bearbeiten-Dialog (Gruppe "Analoge Werte") */
	public static final String stationTank=groupElement+"Tank";

	/** Info-ID für den "Fluss"-Bearbeiten-Dialog (Gruppe "Analoge Werte") */
	public static final String stationTankFlowByClient=groupElement+"TankFlowByClient";

	/** Info-ID für den "Fluss (Signal)"-Bearbeiten-Dialog (Gruppe "Analoge Werte") */
	public static final String stationTankFlowBySignal=groupElement+"TankFlowBySignal";

	/** Info-ID für den "Sensor"-Bearbeiten-Dialog (Gruppe "Analoge Werte") */
	public static final String stationTankSensor=groupElement+"TankSensor";

	/** Info-ID für den "Ventil-Setup"-Bearbeiten-Dialog (Gruppe "Analoge Werte") */
	public static final String stationTankValveSetup=groupElement+"TankValveSetup";

	/* Stationen - Animation */

	/** Info-ID für den "Icon"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationClientIcon=groupElement+"ClientIcon";

	/** Info-ID für den "Simulationsdaten als Text"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationTextValue=groupElement+"AnimationTextValue";

	/** Info-ID für den "Skriptergebnis als Text"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationTextValueJS=groupElement+"AnimationTextValueJS";

	/** Info-ID für den "Text gemäß Simulationsdaten"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationTextSelect=groupElement+"AnimationTextSelect";

	/** Info-ID für den "Simulationsdaten als Balken"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationBar=groupElement+"AnimationBar";

	/** Info-ID für den "Simulationsdaten als gestapelter Balken"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationBarStack=groupElement+"AnimationBarStack";

	/** Info-ID für den "Simulationsdatenampel"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationTrafficLights=groupElement+"AnimationTrafficLights";

	/** Info-ID für den "LCD-Anzeige"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationLCD=groupElement+"AnimationLCD";

	/** Info-ID für den "Analogskalaanzeige"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationPointerMeasuring=groupElement+"AnimationPointerMeasuring";

	/** Info-ID für den "Simulationsdatenliniendiagramm"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationDiagram=groupElement+"AnimationDiagram";

	/** Info-ID für den "Simulationsdatenbalkendiagramm"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationBarChart=groupElement+"AnimationBarChart";

	/** Info-ID für den "Simulationsdatentortendiagramm"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationPieChart=groupElement+"AnimationPieChart";

	/** Info-ID für den "Simulationszeit"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationClock=groupElement+"AnimationClock";

	/** Info-ID für den "Animationsbild"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationImage=groupElement+"AnimationImage";

	/** Info-ID für den "Datenaufzeichnung anzeigen"-Bearbeiten-Dialog (Gruppe "Animation") */
	public static final String stationAnimationRecord=groupElement+"AnimationRecord";

	/* Stationen - Animation - Interaktiv */

	/** Info-ID für den "Schaltfläche"-Bearbeiten-Dialog (Gruppe "Animation - Interaktiv") */
	public static final String stationInteractiveButton=groupElement+"InteractiveButton";

	/** Info-ID für den "Schieberegler"-Bearbeiten-Dialog (Gruppe "Animation - Interaktiv") */
	public static final String stationInteractiveSlider=groupElement+"InteractiveSlider";

	/** Info-ID für den "Checkbox"-Bearbeiten-Dialog (Gruppe "Animation - Interaktiv") */
	public static final String stationInteractiveCheckbox=groupElement+"InteractiveCheckbox";

	/** Info-ID für den "Radiobutton"-Bearbeiten-Dialog (Gruppe "Animation - Interaktiv") */
	public static final String stationInteractiveRadiobutton=groupElement+"InteractiveRadiobutton";

	/* Stationen - Sonstiges */

	/** Info-ID für den "Statistik"-Bearbeiten-Dialog (Gruppe "Sonstiges") */
	public static final String stationUserStatistic=groupElement+"UserStatistic";

	/** Info-ID für den "Aktion"-Bearbeiten-Dialog (Gruppe "Sonstiges") */
	public static final String stationAction=groupElement+"Action";

	/** Info-ID für den "Untermodell"-Bearbeiten-Dialog (Gruppe "Sonstiges") */
	public static final String stationSub=groupElement+"Sub";

	/** Info-ID für den "Dashboard"-Bearbeiten-Dialog (Gruppe "Sonstiges") */
	public static final String stationDashboard=groupElement+"Dashboard";

	/** Info-ID für den "Referenz"-Bearbeiten-Dialog (Gruppe "Sonstiges") */
	public static final String stationReference=groupElement+"Reference";

	/* Stationen - Optische Gestaltung */

	/** Info-ID für den "Beschreibungstext"-Bearbeiten-Dialog (Gruppe "Optische Gestaltung") */
	public static final String stationText=groupElement+"Text";

	/** Info-ID für den "Linie"-Bearbeiten-Dialog (Gruppe "Optische Gestaltung") */
	public static final String stationLine=groupElement+"Line";

	/** Info-ID für den "Rechteck"-Bearbeiten-Dialog (Gruppe "Optische Gestaltung") */
	public static final String stationRectangle=groupElement+"Rectangle";

	/** Info-ID für den "Ellipse"-Bearbeiten-Dialog (Gruppe "Optische Gestaltung") */
	public static final String stationEllipse=groupElement+"Ellipse";

	/** Info-ID für den "Bild"-Bearbeiten-Dialog (Gruppe "Optische Gestaltung") */
	public static final String stationImage=groupElement+"Image";

	/** Info-ID für den "Notiz"-Bearbeiten-Dialog (Gruppe "Optische Gestaltung") */
	public static final String stationNote=groupElement+"Note";

	/** Liste der Hinweisdatensätze */
	private final List<Item> items;

	/** In verschiedenen Fenstern aktive Hinweis-Panels */
	private final Map<Window,List<JPanel>> activeHintsList;

	/** Semaphore um abzusichern, dass nicht zwei konkurrierende Aufrufe auf {@link #getInstance()} schreibend auf {@link #instance} zugreifen */
	private static final Semaphore mutex=new Semaphore(1);

	/** Singleton-Instanz der Klasse */
	private static volatile InfoPanel instance;

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse ist ein Singleton und kann nicht direkt instanziert werden.
	 * Um die Instanz zu erhalten, muss {@link #getInstance()} aufgerufen werden.
	 */
	private InfoPanel() {
		items=new ArrayList<>();
		activeHintsList=new HashMap<>();
		registerInfos();
		loadSetup(SetupData.getSetup().hintDialogs);
	}

	/**
	 * Liefert die Instanz des Singletons
	 * @return	Instanz des Singletons
	 */
	public static InfoPanel getInstance() {
		mutex.acquireUninterruptibly();
		try {
			if (instance==null) instance=new InfoPanel();
			return instance;
		} finally {
			mutex.release();
		}
	}

	/**
	 * Registriert die verschiedenen verfügbaren Hinweisdatensätze.<br>
	 * (Wird direkt vom Konstruktor aufgerufen.)
	 */
	private void registerInfos() {
		/* Willkommensseite */

		register(globalWelcome,
				()->Language.tr("HintsDialog.TreeNodeMainWindow")+"|"+Language.tr("SettingsDialog.Tabs.ProgramStart.WelcomePage"),
				()->"",false);

		/* "Kante hinzufügen"-Hinweis */

		register(globalAddEdge,
				()->Language.tr("HintsDialog.TreeNodeMainWindow")+"|"+Language.tr("SettingsDialog.Tabs.ProgramStart.EdgeAddAdvice"),
				()->"");

		/* "Modelleigenschaften"-Dialog" */

		register(modelDescription,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.ModelDescription"),
				()->Language.tr("Editor.Dialog.Tab.ModelDescription.InfoText"));

		register(modelSimulation,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.Simulation"),
				()->Language.tr("Editor.Dialog.Tab.Simulation.InfoText"));

		register(modelClients,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.Clients"),
				()->Language.tr("Editor.Dialog.Tab.Clients.InfoText"));

		register(modelOperators,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.Operators"),
				()->Language.tr("Editor.Dialog.Tab.Operator.InfoText"));

		register(modelTransporters,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.Transporters"),
				()->Language.tr("Editor.Dialog.Tab.Transporters.InfoText"));

		register(modelSchedule,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.Schedule"),
				()->Language.tr("Editor.Dialog.Tab.Schedule.InfoText"));

		register(modelSequences,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.Sequences"),
				()->Language.tr("Editor.Dialog.Tab.Sequences.InfoText"));

		register(modelInitialValues,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.InitialVariableValues"),
				()->Language.tr("Editor.Dialog.Tab.InitialVariableValues.InfoText"));

		register(modelInitialValuesMap,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.InitialVariableValuesMap"),
				()->Language.tr("Editor.Dialog.Tab.InitialVariableValuesMap.InfoText"));

		register(modelRunTimeStatistics,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.RunTimeStatistics"),
				()->Language.tr("Editor.Dialog.Tab.RunTimeStatistics.InfoText"));

		register(modelOutputAnalysis,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.OutputAnalysis"),
				()->Language.tr("Editor.Dialog.Tab.OutputAnalysis.InfoText"));

		register(modelPathRecording,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.PathRecording"),
				()->Language.tr("Editor.Dialog.Tab.PathRecording.InfoText"));

		register(modelSimulationSystem,
				()->Language.tr("Editor.Dialog.Title")+"|"+Language.tr("Editor.Dialog.Tab.SimulationSystem"),
				()->Language.tr("Editor.Dialog.Tab.SimulationSystem.InfoText"));

		/* Parameterreihe */

		register(parameterSeriesReplaceModel,
				()->Language.tr("ParameterCompare.ParameterCompare")+"|"+Language.tr("ParameterCompare.ReplaceModel"),
				()->"");

		/* Optimierer-Panel */

		register(optimizerControlVariables,
				()->Language.tr("Optimizer.Optimizer")+"|"+Language.tr("Optimizer.Tab.ControlVariables"),
				()->Language.tr("Optimizer.Tab.ControlVariables.DialogHint"));

		register(optimizerTarget,
				()->Language.tr("Optimizer.Optimizer")+"|"+Language.tr("Optimizer.Tab.Target"),
				()->Language.tr("Optimizer.Tab.Target.DialogHint"));

		register(optimizerOptimization,
				()->Language.tr("Optimizer.Optimizer")+"|"+Language.tr("Optimizer.Tab.Optimization"),
				()->Language.tr("Optimizer.Tab.Optimization.DialogHint"));

		/* "Einfaches Modell erstellen"-Dialog */

		register(globalGenerator,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("ModelGenerator.Title"),
				()->Language.tr("ModelGenerator.DialogInfo"));

		/* "Beispiel auswählen"-Dialog */

		register(globalSelectExample,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("SelectExampleWithPreview.Title"),
				()->Language.tr("SelectExampleWithPreview.DialogInfo"));

		/* "Transporterstrecken bearbeiten"-Dialog */

		register(globalPathEditor,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("PathEditor.Title"),
				()->Language.tr("PathEditor.InfoText"));

		/* Parameterreihen-Panel */

		register(globalParameterCompare,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("ParameterCompare.ParameterCompare"),
				()->Language.tr("ParameterCompare.DialogHint"));

		/* Parameterreihen-Panel */

		register(globalScriptRunner,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("JSRunner.JSRunner"),
				()->Language.tr("JSRunner.DialogHint"));

		/* Simulationsergebnisse vergleichen */

		register(globlCompare,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("Compare.Title"),
				()->Language.tr("Compare.DialogHint"));

		/* Externe Modelldaten bearbeiten */

		register(globalModelLoadData,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("ModelLoadData.EditDialog.Title"),
				()->Language.tr("ModelLoadData.EditDialog.DialogHint"));

		/* Element suchen */

		register(globalFindElement,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("FindElementDirect.Title"),
				()->Language.tr("FindElementDirect.DialogHint"));

		/* Verteilung anpassen */

		register(globalFit,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("FitDialog.Title"),
				()->Language.tr("FitDialog.DialogHint"));

		/* Varianzanalyse */

		register(globalVarianceAnalysis,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("ParameterCompare.Settings.VarianceAnalysis.Title"),
				()->Language.tr("ParameterCompare.Settings.VarianceAnalysis.DialogHint"));

		/* Stationsstatistik */

		register(globalStationStatistics,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("StationStatistics.Title"),
				()->Language.tr("StationStatistics.DialogHint"));

		/* Notizen */

		register(globalNotes,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("NotesDialog.Title"),
				()->Language.tr("NotesDialog.DialogHint"));

		/* Modellbeschreibung */

		register(globalModelDescription,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("ModelDescription.Dialog.Title"),
				()->Language.tr("ModelDescription.Dialog.DialogHint"));

		/* Vergleich mit analytischem Modell */

		register(globalAnalyticModelCompare,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("AnalyticModelCompare.Dialog.Title"),
				()->Language.tr("AnalyticModelCompare.Dialog.DialogHint"));

		/* Ebenen */

		register(globalLayers,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("Window.Layers.Title"),
				()->Language.tr("Window.Layers.DialogHint"));

		/* Git-Konfigurationen"-Dialog */

		register(globalGit,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("Git.List.Title"),
				()->Language.tr("Git.List.DialogHint"));

		register(globalProcessClientTable,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("BuildClientSourceTable.Title"),
				()->Language.tr("BuildClientSourceTable.DialogHint"));

		register(globalJavaImports,
				()->Language.tr("HintsDialog.TreeNodeMoreDialogs")+"|"+Language.tr("JavaImports.Title"),
				()->Language.tr("JavaImports.DialogHint"));

		/* Stationen - Eingang/Ausgang */

		register(stationSource,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_INPUTOUTPUT+"|"+Language.tr("Surface.Source.Name"),
				()->Language.tr("Surface.Source.Infotext"));

		register(stationSourceMulti,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_INPUTOUTPUT+"|"+Language.tr("Surface.SourceMulti.Name"),
				()->Language.tr("Surface.SourceMulti.Infotext"));

		register(stationSourceTable,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_INPUTOUTPUT+"|"+Language.tr("Surface.SourceTable.Name"),
				()->Language.tr("Surface.SourceTable.Infotext"));

		register(stationSourceDB,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_INPUTOUTPUT+"|"+Language.tr("Surface.SourceDB.Name"),
				()->Language.tr("Surface.SourceDB.Infotext"));

		register(stationSourceDDE,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_INPUTOUTPUT+"|"+Language.tr("Surface.SourceDDE.Name"),
				()->Language.tr("Surface.SourceDDE.Infotext"));

		register(stationDispose,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_INPUTOUTPUT+"|"+Language.tr("Surface.Dispose.Name"),
				()->Language.tr("Surface.Dispose.Infotext"));

		register(stationDisposeWithTable,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_INPUTOUTPUT+"|"+Language.tr("Surface.DisposeWithTable.Name"),
				()->Language.tr("Surface.DisposeWithTable.Infotext"));

		/* Stationen - Verarbeitung */

		register(stationProcess,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_PROCESSING+"|"+Language.tr("Surface.Process.Name"),
				()->Language.tr("Surface.Process.Infotext"));

		register(stationDelay,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_PROCESSING+"|"+Language.tr("Surface.Delay.Name"),
				()->Language.tr("Surface.Delay.Infotext"));

		/* Stationen - Zuweisungen */

		register(stationAssign,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.Assign.Name"),
				()->Language.tr("Surface.Assign.Infotext"));

		register(stationAssignString,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.AssignString.Name"),
				()->Language.tr("Surface.AssignString.Infotext"));

		register(stationCosts,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.Costs.Name"),
				()->Language.tr("Surface.Costs.Infotext"));

		register(stationSet,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.Set.Name"),
				()->Language.tr("Surface.Set.Infotext"));

		register(stationSetJS,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.SetJS.Name"),
				()->Language.tr("Surface.SetJS.Infotext"));

		register(stationCounter,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.Counter.Name"),
				()->Language.tr("Surface.Counter.Infotext"));

		register(stationCounterMulti,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.CounterMulti.Name"),
				()->Language.tr("Surface.CounterMulti.Infotext"));

		register(stationThroughput,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.Throughput.Name"),
				()->Language.tr("Surface.Throughput.Infotext"));

		register(stationCounterBatch,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.CounterBatch.Name"),
				()->Language.tr("Surface.CounterBatch.Infotext"));

		register(stationStateStatistics,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.StateStatistics.Name"),
				()->Language.tr("Surface.StateStatistics.Infotext"));

		register(stationDifferentialCounter,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.DifferentialCounter.Name"),
				()->Language.tr("Surface.DifferentialCounter.Infotext"));
		register(stationSectionStart,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.SectionStart.Name"),
				()->Language.tr("Surface.SectionStart.Infotext"));

		register(stationSectionEnd,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.SectionEnd.Name"),
				()->Language.tr("Surface.SectionEnd.Infotext"));

		register(stationSetStatisticsMode,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ASSIGN+"|"+Language.tr("Surface.SetStatisticsMode.Name"),
				()->Language.tr("Surface.SetStatisticsMode.Infotext"));

		/* Stationen - Verzweigungen */

		register(stationDuplicate,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BRANCH+"|"+Language.tr("Surface.Duplicate.Name"),
				()->Language.tr("Surface.Duplicate.Infotext"));

		register(stationDecide,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BRANCH+"|"+Language.tr("Surface.Decide.Name"),
				()->Language.tr("Surface.Decide.Infotext"));

		register(stationDecideJS,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BRANCH+"|"+Language.tr("Surface.DecideJS.Name"),
				()->Language.tr("Surface.DecideJS.Infotext"));

		register(stationBalking,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BRANCH+"|"+Language.tr("Surface.Balking.Name"),
				()->Language.tr("Surface.Balking.Infotext"));

		/* Stationen - Schranken */

		register(stationHold,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BARRIER+"|"+Language.tr("Surface.Hold.Name"),
				()->Language.tr("Surface.Hold.Infotext"));

		register(stationHoldMulti,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BARRIER+"|"+Language.tr("Surface.HoldMulti.Name"),
				()->Language.tr("Surface.HoldMulti.Infotext"));

		register(stationHoldJS,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BARRIER+"|"+Language.tr("Surface.HoldJS.Name"),
				()->Language.tr("Surface.HoldJS.Infotext"));

		register(stationSignal,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BARRIER+"|"+Language.tr("Surface.Signal.Name"),
				()->Language.tr("Surface.Signal.Infotext"));

		register(stationBarrier,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BARRIER+"|"+Language.tr("Surface.Barrier.Name"),
				()->Language.tr("Surface.Barrier.Infotext"));

		register(stationBarrierPull,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BARRIER+"|"+Language.tr("Surface.BarrierPull.Name"),
				()->Language.tr("Surface.BarrierPull.Infotext"));

		register(stationSeize,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BARRIER+"|"+Language.tr("Surface.Seize.Name"),
				()->Language.tr("Surface.Seize.Infotext"));

		register(stationRelease,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BARRIER+"|"+Language.tr("Surface.Release.Name"),
				()->Language.tr("Surface.Release.Infotext"));

		/* Stationen - Kunden verbinden */

		register(stationBatch,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BATCH+"|"+Language.tr("Surface.Batch.Name"),
				()->Language.tr("Surface.Batch.InfoText"));

		register(stationBatchMulti,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BATCH+"|"+Language.tr("Surface.BatchMulti.Name"),
				()->Language.tr("Surface.BatchMulti.InfoText"));

		register(stationSeparate,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BATCH+"|"+Language.tr("Surface.Separate.Name"),
				()->Language.tr("Surface.Separate.Infotext"));

		register(stationMatch,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BATCH+"|"+Language.tr("Surface.Match.Name"),
				()->Language.tr("Surface.Match.Infotext"));

		register(stationPickUp,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BATCH+"|"+Language.tr("Surface.PickUp.Name"),
				()->Language.tr("Surface.PickUp.Infotext"));

		register(stationSplit,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_BATCH+"|"+Language.tr("Surface.Split.Name"),
				()->Language.tr("Surface.Split.Infotext"));

		/* Stationen - Transport */

		register(stationTransportSource,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_TRANSPORT+"|"+Language.tr("Surface.TransportSource.Name"),
				()->Language.tr("Surface.TransportSource.Infotext"));

		register(stationTransportTransporterSource,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_TRANSPORT+"|"+Language.tr("Surface.TransportTransporterSource.Name"),
				()->Language.tr("Surface.TransportTransporterSource.Infotext"));

		register(stationTransportDestination,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_TRANSPORT+"|"+Language.tr("Surface.TransportDestination.Name"),
				()->Language.tr("Surface.TransportDestination.Infotext"));

		register(stationTransportParking,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_TRANSPORT+"|"+Language.tr("Surface.TransportParking.Name"),
				()->Language.tr("Surface.TransportParking.Infotext"));

		register(stationAssignSequence,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_TRANSPORT+"|"+Language.tr("Surface.AssignSequence.Name"),
				()->Language.tr("Surface.AssignSequence.Infotext"));

		register(stationWayPoint,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_TRANSPORT+"|"+Language.tr("Surface.WayPoint.Name"),
				()->Language.tr("Surface.WayPoint.Infotext"));

		register(stationTeleportSource,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_TRANSPORT+"|"+Language.tr("Surface.TeleportSource.Name"),
				()->Language.tr("Surface.TeleportSource.Infotext"));

		register(stationTeleportDestination,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_TRANSPORT+"|"+Language.tr("Surface.TeleportDestination.Name"),
				()->Language.tr("Surface.TeleportDestination.Infotext"));

		register(stationConveyor,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_TRANSPORT+"|"+Language.tr("Surface.Conveyor.Name"),
				()->Language.tr("Surface.Conveyor.Infotext"));

		/* Stationen - Daten Ein- und Ausgabe */

		register(stationInput,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DATAINPUTOUTPUT+"|"+Language.tr("Surface.Input.Name"),
				()->Language.tr("Surface.Input.Infotext"));

		register(stationInputJS,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DATAINPUTOUTPUT+"|"+Language.tr("Surface.InputJS.Name"),
				()->Language.tr("Surface.InputJS.Infotext"));

		register(stationInputDB,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DATAINPUTOUTPUT+"|"+Language.tr("Surface.InputDB.Name"),
				()->Language.tr("Surface.InputDB.Infotext"));

		register(stationInputDDE,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DATAINPUTOUTPUT+"|"+Language.tr("Surface.InputDDE.Name"),
				()->Language.tr("Surface.InputDDE.Infotext"));

		register(stationOutput,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DATAINPUTOUTPUT+"|"+Language.tr("Surface.Output.Name"),
				()->Language.tr("Surface.Output.Infotext"));

		register(stationOutputJS,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DATAINPUTOUTPUT+"|"+Language.tr("Surface.OutputJS.Name"),
				()->Language.tr("Surface.OutputJS.Infotext"));

		register(stationOutputDB,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DATAINPUTOUTPUT+"|"+Language.tr("Surface.OutputDB.Name"),
				()->Language.tr("Surface.OutputDB.Infotext"));

		register(stationOutputDDE,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DATAINPUTOUTPUT+"|"+Language.tr("Surface.OutputDDE.Name"),
				()->Language.tr("Surface.OutputDDE.Infotext"));

		register(stationOutputLog,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DATAINPUTOUTPUT+"|"+Language.tr("Surface.OutputLog.Name"),
				()->Language.tr("Surface.OutputLog.Infotext"));

		register(stationRecord,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DATAINPUTOUTPUT+"|"+Language.tr("Surface.Record.Name"),
				()->Language.tr("Surface.Record.Infotext"));

		/* Stationen - Flusslogik */

		register(stationLogicIf,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_LOGIC+"|"+Language.tr("Surface.LogicIf.Name"),
				()->Language.tr("Surface.LogicIf.Infotext"));

		register(stationLogicElseIf,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_LOGIC+"|"+Language.tr("Surface.LogicElseIf.Name"),
				()->Language.tr("Surface.LogicElseIf.Infotext"));

		register(stationLogicElse,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_LOGIC+"|"+Language.tr("Surface.LogicElse.Name"),
				()->Language.tr("Surface.LogicElse.Infotext"));

		register(stationLogicEndIf,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_LOGIC+"|"+Language.tr("Surface.LogicEndIf.Name"),
				()->Language.tr("Surface.LogicEndIf.Infotext"));

		register(stationLogicWhile,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_LOGIC+"|"+Language.tr("Surface.LogicWhile.Name"),
				()->Language.tr("Surface.LogicWhile.Infotext"));

		register(stationLogicEndWhile,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_LOGIC+"|"+Language.tr("Surface.LogicEndWhile.Name"),
				()->Language.tr("Surface.LogicEndWhile.Infotext"));

		register(stationLogicDo,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_LOGIC+"|"+Language.tr("Surface.LogicDo.Name"),
				()->Language.tr("Surface.LogicDo.Infotext"));

		register(stationLogicUntil,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_LOGIC+"|"+Language.tr("Surface.LogicUntil.Name"),
				()->Language.tr("Surface.LogicUntil.Infotext"));

		/* Stationen - Analoge Werte */

		register(stationAnalogValue,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANALOG+"|"+Language.tr("Surface.AnalogValue.Name"),
				()->Language.tr("Surface.AnalogValue.Infotext"));

		register(stationAnalogAssign,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANALOG+"|"+Language.tr("Surface.AnalogAssign.Name"),
				()->Language.tr("Surface.AnalogAssign.Infotext"));

		register(stationTank,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANALOG+"|"+Language.tr("Surface.Tank.Name"),
				()->Language.tr("Surface.Tank.Infotext"));

		register(stationTankFlowByClient,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANALOG+"|"+Language.tr("Surface.TankFlowByClient.Name"),
				()->Language.tr("Surface.TankFlowByClient.Infotext"));

		register(stationTankFlowBySignal,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANALOG+"|"+Language.tr("Surface.TankFlowBySignal.Name"),
				()->Language.tr("Surface.TankFlowBySignal.Infotext"));

		register(stationTankSensor,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANALOG+"|"+Language.tr("Surface.TankSensor.Name"),
				()->Language.tr("Surface.TankSensor.Infotext"));

		register(stationTankValveSetup,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANALOG+"|"+Language.tr("Surface.TankValveSetup.Name"),
				()->Language.tr("Surface.TankValveSetup.Infotext"));

		/* Stationen - Animation */

		register(stationClientIcon,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.ClientIcon.Name"),
				()->Language.tr("Surface.ClientIcon.Infotext"));

		register(stationAnimationTextValue,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationText.Name"),
				()->Language.tr("Surface.AnimationText.Infotext"));

		register(stationAnimationTextValueJS,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationTextJS.Name"),
				()->Language.tr("Surface.AnimationTextJS.Infotext"));

		register(stationAnimationTextSelect,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationTextSelect.Name"),
				()->Language.tr("Surface.AnimationTextSelect.Infotext"));

		register(stationAnimationBar,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationBar.Name"),
				()->Language.tr("Surface.AnimationBar.Infotext"));

		register(stationAnimationBarStack,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationBarStack.Name"),
				()->Language.tr("Surface.AnimationBarStack.Infotext"));

		register(stationAnimationTrafficLights,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationTrafficLights.Name"),
				()->Language.tr("Surface.AnimationTrafficLights.Infotext"));

		register(stationAnimationLCD,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationLCD.Name"),
				()->Language.tr("Surface.AnimationLCD.Infotext"));

		register(stationAnimationPointerMeasuring,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationPointerMeasuring.Name"),
				()->Language.tr("Surface.AnimationPointerMeasuring.Infotext"));

		register(stationAnimationDiagram,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationDiagram.Name"),
				()->Language.tr("Surface.AnimationDiagram.Infotext"));

		register(stationAnimationBarChart,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationBarChart.Name"),
				()->Language.tr("Surface.AnimationBarChart.Infotext"));

		register(stationAnimationPieChart,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationPieChart.Name"),
				()->Language.tr("Surface.AnimationPieChart.Infotext"));

		register(stationAnimationClock,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationClock.Name"),
				()->Language.tr("Surface.AnimationClock.Infotext"));

		register(stationAnimationImage,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationImage.Name"),
				()->Language.tr("Surface.AnimationImage.Infotext"));

		register(stationAnimationRecord,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_ANIMATION+"|"+Language.tr("Surface.AnimationRecord.Name"),
				()->Language.tr("Surface.AnimationRecord.Infotext"));

		/* Stationen - Animation - Interaktiv */

		register(stationInteractiveButton,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_INTERACTIVE+"|"+Language.tr("Surface.InteractiveButton.Name"),
				()->Language.tr("Surface.InteractiveButton.Infotext"));

		register(stationInteractiveSlider,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_INTERACTIVE+"|"+Language.tr("Surface.InteractiveSlider.Name"),
				()->Language.tr("Surface.InteractiveSlider.Infotext"));

		register(stationInteractiveCheckbox,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_INTERACTIVE+"|"+Language.tr("Surface.InteractiveCheckbox.Name"),
				()->Language.tr("Surface.InteractiveCheckbox.Infotext"));

		register(stationInteractiveRadiobutton,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_INTERACTIVE+"|"+Language.tr("Surface.InteractiveRadiobutton.Name"),
				()->Language.tr("Surface.InteractiveRadiobutton.Infotext"));

		/* Stationen - Sonstiges */

		register(stationUserStatistic,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_OTHERS+"|"+Language.tr("Surface.UserStatistic.Name"),
				()->Language.tr("Surface.UserStatistic.Infotext"));

		register(stationAction,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_OTHERS+"|"+Language.tr("Surface.Action.Name"),
				()->Language.tr("Surface.Action.Infotext"));

		register(stationSub,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_OTHERS+"|"+Language.tr("Surface.Sub.Name"),
				()->Language.tr("Surface.Sub.Infotext"));

		register(stationDashboard,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_OTHERS+"|"+Language.tr("Surface.Dashboard.Name"),
				()->Language.tr("Surface.Dashboard.Infotext"));

		register(stationReference,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_OTHERS+"|"+Language.tr("Surface.Reference.Name"),
				()->Language.tr("Surface.Reference.Infotext"));

		/* Stationen - Optische Gestaltung */

		register(stationText,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DECORATION+"|"+Language.tr("Surface.Text.Name"),
				()->Language.tr("Surface.Text.Infotext"));

		register(stationLine,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DECORATION+"|"+Language.tr("Surface.Line.Name"),
				()->Language.tr("Surface.Line.Infotext"));

		register(stationRectangle,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DECORATION+"|"+Language.tr("Surface.Rectangle.Name"),
				()->Language.tr("Surface.Rectangle.Infotext"));

		register(stationEllipse,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DECORATION+"|"+Language.tr("Surface.Ellipse.Name"),
				()->Language.tr("Surface.Ellipse.Infotext"));

		register(stationImage,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DECORATION+"|"+Language.tr("Surface.Image.Name"),
				()->Language.tr("Surface.Image.Infotext"));

		register(stationNote,
				()->Language.tr("HintsDialog.TreeNodeStations")+"|"+ModelElementCatalog.GROUP_DECORATION+"|"+Language.tr("Surface.Note.Name"),
				()->Language.tr("Surface.Note.Infotext"));
	}

	/**
	 * Lädt die Einstellungen, welche Hinweise angezeigt werden sollen, aus einer Zeichenkette.
	 * @param text	Zeichenkette aus der die Einstellungen geladen werden sollen
	 * @see SetupData#hintDialogs
	 * @see InfoPanel#getSetup()
	 */
	public void loadSetup(final String text) {
		for (Item item: items) item.resetState();

		if (text==null) return;
		final String[] lines=text.split("\n");
		for (String line: lines) {
			if (line==null) continue;
			final String[] parts=line.split("=");
			if (parts.length!=2) continue;
			if (parts[0]==null || parts[1]==null) continue;
			final String id=parts[0].trim();
			final boolean visible=!parts[1].trim().equals("0");
			items.stream().filter(item->item.id.equalsIgnoreCase(id)).forEach(item->item.visible=visible);
		}
	}

	/**
	 * Liefert die Einstellungen, welche Hinweise angezeigt werden sollen, als Zeichenkette.
	 * @return	Zeichenkette, die die Einstellungen repräsentiert.
	 * @see SetupData#hintDialogs
	 * @see InfoPanel#loadSetup(String)
	 */
	public String getSetup() {
		final StringBuilder setup=new StringBuilder();
		for (Item item: items) {
			setup.append(item.id);
			setup.append("=");
			setup.append(item.visible?"1":"0");
			setup.append("\n");
		}
		return setup.toString();
	}

	/**
	 * Registriert einen Hinweis-Datensatz
	 * @param id	ID des Datensatzes
	 * @param name	Name für den Datensatz (Getter, um die jeweils aktuelle Sprache berücksichtigen zu können)
	 * @param info	Anzuzeigender Hinweistext (Getter, um die jeweils aktuelle Sprache berücksichtigen zu können)
	 * @see #registerInfos()
	 */
	private void register(final String id, final Supplier<String> name, final Supplier<String> info) {
		register(id,name,info,true);
	}

	/**
	 * Registriert einen Hinweis-Datensatz
	 * @param id	ID des Datensatzes
	 * @param name	Name für den Datensatz (Getter, um die jeweils aktuelle Sprache berücksichtigen zu können)
	 * @param info	Anzuzeigender Hinweistext (Getter, um die jeweils aktuelle Sprache berücksichtigen zu können)
	 * @param defaultState	Standardeinstellung in Bezug auf aktiv/deaktiviert
	 * @see #registerInfos()
	 */
	private void register(final String id, final Supplier<String> name, final Supplier<String> info, final boolean defaultState) {
		items.add(new Item(id,name,info,defaultState));
	}

	/**
	 * Erstellt (wenn im Setup aktiviert) ein Infopanel mit dem angegebenen Text und fügt es ein.
	 * @param parent	Übergeordnetes Element (wird von dieser Methode auf {@link BorderLayout} gestellt)
	 * @param id	ID des anzuzeigenden Textes
	 * @return	Liefert das eingefügte Infopanel (oder <code>null</code>, wenn kein Panel eingefügt wurde)
	 */
	public static JPanel addTopPanel(final Container parent, final String id) {
		return getInstance().addTopPanelIntern(parent,id);
	}

	/**
	 * Erstellt (wenn im Setup aktiviert) ein Infopanel mit dem angegebenen Text und fügt es ein.
	 * @param parent	Übergeordnetes Element (wird von dieser Methode auf {@link BorderLayout} gestellt)
	 * @param id	ID des anzuzeigenden Textes
	 * @return	Liefert das eingefügte Infopanel (oder <code>null</code>, wenn kein Panel eingefügt wurde)
	 * @see #addTopPanel(Container, String)
	 */
	private JPanel addTopPanelIntern(final Container parent, final String id) {
		if (!isVisible(id)) return null;

		final Item item=getItem(id);
		final JPanel hint=buildPanel(id,(item==null)?"":item.getInfo());
		parent.setLayout(new BorderLayout());
		parent.add(hint,BorderLayout.NORTH);
		registerPanel(getWindow(hint),hint);
		return hint;
	}

	/**
	 * Erstellt (wenn im Setup aktiviert) ein Infopanel mit dem angegebenen Text und fügt es ein.
	 * @param parent	Übergeordnetes Element (wird von dieser Methode auf {@link BorderLayout} gestellt)
	 * @param id	ID des anzuzeigenden Textes
	 * @return	Erstellt ein neues Panel und fügt es als Content in das angegeben übergeordnete Element ein
	 */
	public static JPanel addTopPanelAndGetNewContent(final Container parent, final String id) {
		return getInstance().addTopPanelAndGetNewContentIntern(parent,id);
	}

	/**
	 * Erstellt (wenn im Setup aktiviert) ein Infopanel mit dem angegebenen Text und fügt es ein.
	 * @param parent	Übergeordnetes Element (wird von dieser Methode auf {@link BorderLayout} gestellt)
	 * @param id	ID des anzuzeigenden Textes
	 * @return	Erstellt ein neues Panel und fügt es als Content in das angegeben übergeordnete Element ein
	 * @see #addTopPanelAndGetNewContent(Container, String)
	 */
	private JPanel addTopPanelAndGetNewContentIntern(final Container parent, final String id) {
		addTopPanelIntern(parent,id);
		JPanel content=new JPanel(new BorderLayout());
		parent.add(content,BorderLayout.CENTER);
		return content;
	}

	/**
	 * Erzeugt ein neues Panel, in das das bisherige eingebettet wird
	 * @param window	Übergeordnetes Fenster
	 * @param panel	Bisheriges Panel, welches (wenn ein Infotext ausgegeben werden soll) in ein neues Panel eingebettet wird
	 * @param id	ID des anzuzeigenden Textes
	 * @return	Neues Panel (wenn Infotext ausgegeben wird), in das das bestehende Panel eingebettet ist, oder das übergebene Panel selbst (wenn kein Infotext ausgegeben wird)
	 */
	public static JPanel createNewPanel(final Window window, final JPanel panel, final String id) {
		return getInstance().createNewPanelIntern(window,panel,id);
	}

	/**
	 * Erzeugt ein neues Panel, in das das bisherige eingebettet wird
	 * @param window	Übergeordnetes Fenster
	 * @param panel	Bisheriges Panel, welches (wenn ein Infotext ausgegeben werden soll) in ein neues Panel eingebettet wird
	 * @param id	ID des anzuzeigenden Textes
	 * @return	Neues Panel (wenn Infotext ausgegeben wird), in das das bestehende Panel eingebettet ist, oder das übergebene Panel selbst (wenn kein Infotext ausgegeben wird)
	 * @see #createNewPanel(Window, JPanel, String)
	 */
	private JPanel createNewPanelIntern(final Window window, final JPanel panel, final String id) {
		if (!isVisible(id)) return panel;

		final Item item=getItem(id);
		final JPanel hint=buildPanel(id,(item==null)?"":item.getInfo());
		final JPanel newPanel=new JPanel(new BorderLayout());
		newPanel.add(hint,BorderLayout.NORTH);
		newPanel.add(panel,BorderLayout.CENTER);
		registerPanel(window,hint);
		return newPanel;
	}

	/**
	 * Erstellt das eigentliche Info-Panel
	 * @param id	ID des anzuzeigenden Textes (für die Schließen-Schaltfläche)
	 * @param text	Anzuzeigender Text
	 * @return	Neues Info-Panel
	 */
	private JPanel buildPanel(final String id, final String text) {
		final JPanel topOuter=new JPanel(new BorderLayout());

		JMenuItem item;

		final JPopupMenu removeAllMenu=new JPopupMenu();
		removeAllMenu.add(item=new JMenuItem(Language.tr("Editor.AddEdge.Hint.RemoveAllButton")));
		item.addActionListener(e->turnOffAllHints());
		item.setIcon(Images.INFO_PANEL_CLOSE_ALL.getIcon());

		final JPopupMenu removePanelMenu=new JPopupMenu();
		removePanelMenu.add(item=new JMenuItem(Language.tr("Editor.AddEdge.Hint.RemoveThisButton")));
		item.addActionListener(e->SwingUtilities.invokeLater(()->turnOffHint(topOuter,id)));
		item.setIcon(Images.INFO_PANEL_CLOSE_THIS.getIcon());
		removePanelMenu.add(item=new JMenuItem(Language.tr("Editor.AddEdge.Hint.RemoveAllButton")));
		item.addActionListener(e->SwingUtilities.invokeLater(()->turnOffAllHints()));
		item.setIcon(Images.INFO_PANEL_CLOSE_ALL.getIcon());

		final JPanel topInner=new JPanel(new BorderLayout());
		if (FlatLaFHelper.isDark()) {
			topInner.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		} else {
			topInner.setBorder(BorderFactory.createLineBorder(new Color(212,212,212)));
			topInner.setBackground(new Color(250,250,245));
		}

		final JLabel label=new JLabel();
		label.setIcon(Images.GENERAL_INFO.getIcon());
		label.setVerticalAlignment(SwingConstants.TOP);
		label.setBorder(BorderFactory.createEmptyBorder(5,5,5,0));
		topInner.add(label,BorderLayout.WEST);

		final JTextPane pane=new JTextPane();
		pane.setBackground(new Color(0,0,0,0));
		pane.setOpaque(false);
		pane.setEditable(false);
		pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		pane.setComponentPopupMenu(removePanelMenu);
		topInner.add(pane,BorderLayout.CENTER);

		final JToolBar toolBar=new JToolBar(SwingConstants.HORIZONTAL);
		toolBar.setFloatable(false);
		final JButton button=new JButton(Language.tr("Editor.AddEdge.Hint.RemoveButton"));
		if (!FlatLaFHelper.isDark()) {
			button.setBackground(new Color(255,255,240));
		}
		button.setToolTipText(Language.tr("Editor.AddEdge.Hint.RemoveButton.Hint"));
		button.setIcon(Images.INFO_PANEL_CLOSE_THIS.getIcon());
		button.addActionListener(e->SwingUtilities.invokeLater(()->turnOffHint(topOuter,id)));
		button.setComponentPopupMenu(removeAllMenu);
		toolBar.add(button);
		if (!FlatLaFHelper.isDark()) {
			toolBar.setBackground(new Color(250,250,245));
		}
		topInner.add(toolBar,BorderLayout.EAST);

		SwingUtilities.invokeLater(()->{
			pane.setText(text);
			final int h1=topOuter.getHeight();
			final int h2=topOuter.getPreferredSize().height;
			if (h2>h1) topOuter.setSize(topOuter.getWidth(),h2);
		});

		topOuter.add(topInner,BorderLayout.CENTER);
		topOuter.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));

		return topOuter;
	}

	/**
	 * Liefert das Fenster in dem sich eine Komponente befindet
	 * @param component	Komponente für die das übergeordnete Fenster bestimmt werden soll
	 * @return	Übergeordnetes Fenster oder <code>null</code>, wenn kein Fenster ermittelt werden konnte
	 * @see #addTopPanelIntern(Container, String)
	 */
	private Window getWindow(final Component component) {
		Component c=component;
		while (c!=null) {
			if (c instanceof Window) return (Window)c;
			c=c.getParent();
		}
		return null;
	}

	/**
	 * Registriert ein neues Info-Panel in der Liste
	 * @param window	Für dieses Fenster registrieren
	 * @param hintPanel	Info-Panel
	 * @see #turnOffAllHints()
	 */
	private void registerPanel(final Window window, final JPanel hintPanel) {
		if (window==null) return;
		if (activeHintsList.get(window)==null) {
			window.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					activeHintsList.remove(window);
				}
			});
		}

		List<JPanel> list=activeHintsList.get(window);
		if (list==null) {
			list=new ArrayList<>();
			activeHintsList.put(window,list);
		}

		list.add(hintPanel);
	}

	/**
	 * Fragt ab, ob ein bestimmter Hinweisdatensatz sichtbar sein soll
	 * @param id	ID des Hinweisdatensatzes
	 * @return	Gibt an, ob der Hinweisdatensatz sichtbar sein soll
	 */
	public boolean isVisible(final String id) {
		final Item item=getItem(id);
		return (item!=null && item.visible);
	}

	/**
	 * Stellt ein, ob ein bestimmter Hinweisdatensatz sichtbar sein soll
	 * @param id	ID des Hinweisdatensatz
	 * @param visible	Soll der Hinweisdatensatz sichtbar sein
	 */
	public void setVisible(final String id, final boolean visible) {
		final Item item=getItem(id);
		if (item!=null) {
			item.visible=false;
			final SetupData setup=SetupData.getSetup();
			setup.hintDialogs=getSetup();
			setup.saveSetup();
		}
	}

	/**
	 * Schaltet ein bestimmtes Info-Panel aus
	 * @param panel	Auszuschaltendes Panel
	 * @param id	ID des Textes, der nun nicht mehr angezeigt werden soll
	 */
	private void turnOffHint(final JPanel panel, final String id) {
		final Container parent=panel.getParent();
		parent.remove(panel);
		parent.revalidate();
		parent.repaint();

		setVisible(id,false);
	}

	/**
	 * Schaltet alle Info-Panel aus.
	 */
	private void turnOffAllHints() {
		for (Map.Entry<Window,List<JPanel>> entry: activeHintsList.entrySet()) {
			for (JPanel panel: entry.getValue()) {
				Container parent=panel.getParent();
				if (parent!=null) {
					parent.remove(panel);
					parent.revalidate();
					parent.repaint();
				}
			}
		}
		activeHintsList.clear();

		for (Item item: items) item.visible=false;

		final SetupData setup=SetupData.getSetup();
		setup.hintDialogs=getSetup();
		setup.saveSetup();
	}

	/**
	 * Liefert den Datensatz zu einer bestimmten ID
	 * @param id	ID für die der Datensatz bereitgestellt werden soll
	 * @return	Datensatz oder <code>null</code>, wenn die ID nicht vergeben ist
	 */
	private Item getItem(final String id) {
		final Optional<Item> item=items.stream().filter(i->i.id.equalsIgnoreCase(id)).findFirst();
		if (!item.isPresent()) return null;
		return item.get();
	}

	/**
	 * Liefert die Liste aller Hinweisdatensätze
	 * @return	Liste aller Hinweisdatensätze
	 */
	public List<Item> getItems() {
		return items;
	}

	/**
	 * Repräsentiert einen Hinweisdatensatz
	 * @author Alexander Herzog
	 */
	public static class Item {
		/**
		 * ID des Hinweisdatensatzes (für den Aufruf von {@link InfoPanel#addTopPanel(Container, String)} oder von {@link InfoPanel#addTopPanelAndGetNewContent(Container, String)} und zum Speichern der Einstellungen)
		 */
		public final String id;

		/**
		 * Gibt an, ob der Hinweisdatensatz angezeigt werden soll.
		 */
		public boolean visible;

		/**
		 * Standardeinstellung in Bezug auf aktiv/deaktiviert
		 */
		private final boolean defaultState;

		/**
		 * Name für den Datensatz (Getter, um die jeweils aktuelle Sprache berücksichtigen zu können)
		 */
		private final Supplier<String> name;

		/**
		 * Anzuzeigender Hinweistext (Getter, um die jeweils aktuelle Sprache berücksichtigen zu können)
		 */
		private final Supplier<String> info;

		/**
		 * Konstruktor der Klasse
		 * @param id	ID des Datensatzes
		 * @param name	Name für den Datensatz (Getter, um die jeweils aktuelle Sprache berücksichtigen zu können)
		 * @param info	Anzuzeigender Hinweistext (Getter, um die jeweils aktuelle Sprache berücksichtigen zu können)
		 * @param defaultState	Standardeinstellung in Bezug auf aktiv/deaktiviert
		 */
		private Item(final String id, final Supplier<String> name, final Supplier<String> info, final boolean defaultState) {
			this.id=id;
			visible=defaultState;
			this.defaultState=defaultState;
			this.name=name;
			this.info=info;
		}

		/**
		 * Liefert den Namen für den Hinweisdatensatz (für den Einstellungendialog)
		 * @return	Dialogname des Hinweisdatensatzes
		 */
		public String getName() {
			return name.get();
		}

		/**
		 * Liefert den Hinweistext eines Hinweisdatensatzes
		 * @return	Hinweistext eines Hinweisdatensatzes
		 */
		public String getInfo() {
			return info.get();
		}

		/**
		 * Stellt den Sichtbarkeitsstatus des Hinweisdatensatzes auf den Vorgabewert zurück.
		 */
		public void resetState() {
			visible=defaultState;
		}
	}
}
