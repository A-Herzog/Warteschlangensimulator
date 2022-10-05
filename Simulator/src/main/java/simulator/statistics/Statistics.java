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
package simulator.statistics;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import statistics.StatisticsBase;
import statistics.StatisticsDataCollector;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsLongRunPerformanceIndicator;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsQuotientPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import statistics.StatisticsSimpleValueMaxPerformanceIndicator;
import statistics.StatisticsSimpleValuePerformanceIndicator;
import statistics.StatisticsSimulationBaseData;
import statistics.StatisticsStateTimePerformanceIndicator;
import statistics.StatisticsTimeAnalogPerformanceIndicator;
import statistics.StatisticsTimeContinuousPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import statistics.StatisticsValuePerformanceIndicator;

/**
 * Statistik über die komplette Simulation
 * Die <code>Statistics</code>-Klasse ist dabei so ausgelegt, dass sie sowohl thread-lokal Datensammeln kann als auch am Ende die globale Statistik ausweisen kann
 * @author Alexander Herzog
 */
public class Statistics extends StatisticsBase {
	/* ====================================================
	 * Basisdaten
	 * ====================================================
	 */

	/**
	 * Das Editor-Modell wird mit in der Statistik gespeichert. So ist immer nachvollziehbar, auf welches Modell sich die Statistik bezieht.
	 */
	public EditModel editModel;

	/**
	 * Technische Basisdaten zur Simulation
	 */
	public final StatisticsSimulationBaseData simulationData;

	/**
	 * Konfidenzintervalle auf Basis der Threads
	 */

	public final StatisticsMultiPerformanceIndicator threadBasedConfidence;

	/* ====================================================
	 * Zwischenankunftszeiten
	 * ====================================================
	 */

	/**
	 * Zwischenankunftszeiten der Kunden (bei den "Kundenquelle"-Elementen)
	 */
	public final StatisticsMultiPerformanceIndicator clientsInterarrivalTime;

	/**
	 * Zwischenankunftszeiten der Kunden bei den einzelnen Stationen
	 */
	public final StatisticsMultiPerformanceIndicator stationsInterarrivalTime;

	/**
	 * Zwischenankunftszeiten der Kunden bei den einzelnen Stationen (Batch)
	 */
	public final StatisticsMultiPerformanceIndicator stationsInterarrivalTimeBatch;

	/**
	 * Zwischenankunftszeiten der Kunden bei den einzelnen Stationen ausdifferenziert nach der Anzahl an Kunden an der Station
	 */
	public final StatisticsMultiPerformanceIndicator stationsInterarrivalTimeByState;

	/**
	 * Zwischenankunftszeiten der Kunden bei den einzelnen Stationen (zusätzlich differenziert nach Kundentyp)
	 */
	public final StatisticsMultiPerformanceIndicator stationsInterarrivalTimeByClientType;

	/**
	 * Maximaler Durchsatz an den Stationen
	 */
	public final StatisticsMultiPerformanceIndicator stationsMaxThroughput;

	/* ====================================================
	 * Zwischenabgangszeiten
	 * ====================================================
	 */

	/**
	 * Zwischenabgangszeiten der Kunden aus dem System
	 */
	public final StatisticsMultiPerformanceIndicator clientsInterleavingTime;

	/**
	 * Zwischenabgangszeiten der Kunden bei den einzelnen Stationen
	 */
	public final StatisticsMultiPerformanceIndicator stationsInterleavingTime;

	/**
	 * Zwischenabgangszeiten der Kunden bei den einzelnen Stationen (Batch)
	 */
	public final StatisticsMultiPerformanceIndicator stationsInterleavingTimeBatch;

	/**
	 * Zwischenabgangszeiten der Kunden bei den einzelnen Stationen (zusätzlich differenziert nach Kundentyp)
	 */
	public final StatisticsMultiPerformanceIndicator stationsInterleavingTimeByClientType;

	/* ====================================================
	 * Kundenbewegungen zwischen den Stationen
	 * ====================================================
	 */

	/**
	 * Übergänge zwischen den Stationen
	 */
	public final StatisticsMultiPerformanceIndicator stationTransition;

	/**
	 * Pfade der Kunden durch das System
	 */
	public final StatisticsMultiPerformanceIndicator clientPaths;

	/* ====================================================
	 * Zeiten pro Kundentyp
	 * ====================================================
	 */

	/**
	 * Wartezeiten der Kunden (pro Kundentyp)
	 */
	public final StatisticsMultiPerformanceIndicator clientsWaitingTimes;

	/**
	 * Transferzeiten der Kunden (pro Kundentyp)
	 */
	public final StatisticsMultiPerformanceIndicator clientsTransferTimes;

	/**
	 * Bedienzeiten der Kunden (pro Kundentyp)
	 */
	public final StatisticsMultiPerformanceIndicator clientsProcessingTimes;

	/**
	 * Verweilzeiten der Kunden (pro Kundentyp)
	 */
	public final StatisticsMultiPerformanceIndicator clientsResidenceTimes;

	/**
	 * Wartezeiten der Kunden (über alle Kundentypen)
	 * @see Statistics#clientsAllWaitingTimesCollector
	 */
	public final StatisticsDataPerformanceIndicator clientsAllWaitingTimes;

	/**
	 * Transferzeiten der Kunden (über alle Kundentypen)
	 */
	public final StatisticsDataPerformanceIndicator clientsAllTransferTimes;

	/**
	 * Bedienzeiten der Kunden (über alle Kundentypen)
	 */
	public final StatisticsDataPerformanceIndicator clientsAllProcessingTimes;

	/**
	 * Verweilzeiten der Kunden (über alle Kundentypen)
	 */
	public final StatisticsDataPerformanceIndicator clientsAllResidenceTimes;

	/* ====================================================
	 * Kundendatenfelder
	 * ====================================================
	 */

	/**
	 * Kundenspezifische Werte
	 */
	public final StatisticsMultiPerformanceIndicator clientData;

	/* ====================================================
	 * Zeiten auf Seiten der Stationen
	 * ====================================================
	 */

	/**
	 * Wartezeiten an den Stationen
	 */
	public final StatisticsMultiPerformanceIndicator stationsWaitingTimes;

	/**
	 * Transferzeiten an den Stationen
	 */
	public final StatisticsMultiPerformanceIndicator stationsTransferTimes;

	/**
	 * Bedienzeiten an den Stationen
	 */
	public final StatisticsMultiPerformanceIndicator stationsProcessingTimes;

	/**
	 * Verweilzeiten an den Stationen
	 */
	public final StatisticsMultiPerformanceIndicator stationsResidenceTimes;

	/**
	 * Wartezeiten an den Stationen (passiert ein Kunde die Station mehrfach, so wird hier die Summe erfasst; Erfassung kann deaktiviert sein)
	 */
	public final StatisticsMultiPerformanceIndicator stationsTotalWaitingTimes;

	/**
	 * Transferzeiten an den Stationen (passiert ein Kunde die Station mehrfach, so wird hier die Summe erfasst; Erfassung kann deaktiviert sein)
	 */
	public final StatisticsMultiPerformanceIndicator stationsTotalTransferTimes;

	/**
	 * Bedienzeiten an den Stationen (passiert ein Kunde die Station mehrfach, so wird hier die Summe erfasst; Erfassung kann deaktiviert sein)
	 */
	public final StatisticsMultiPerformanceIndicator stationsTotalProcessingTimes;

	/**
	 * Verweilzeiten an den Stationen (passiert ein Kunde die Station mehrfach, so wird hier die Summe erfasst; Erfassung kann deaktiviert sein)
	 */
	public final StatisticsMultiPerformanceIndicator stationsTotalResidenceTimes;

	/**
	 * Wartezeiten an den Stationen (zusätzlich differenziert nach Kundentyp)
	 */
	public final StatisticsMultiPerformanceIndicator stationsWaitingTimesByClientType;

	/**
	 * Transferzeiten an den Stationen (zusätzlich differenziert nach Kundentyp)
	 */
	public final StatisticsMultiPerformanceIndicator stationsTransferTimesByClientType;

	/**
	 * Bedienzeiten an den Stationen (zusätzlich differenziert nach Kundentyp)
	 */
	public final StatisticsMultiPerformanceIndicator stationsProcessingTimesByClientType;

	/**
	 * Verweilzeiten an den Stationen (zusätzlich differenziert nach Kundentyp)
	 */
	public final StatisticsMultiPerformanceIndicator stationsResidenceTimesByClientType;

	/**
	 * Rüstzeiten an den Bedienstationen
	 */
	public final StatisticsMultiPerformanceIndicator stationsSetupTimes;

	/* ====================================================
	 * Anzahlen an Kunden
	 * ====================================================
	 */

	/**
	 * Verteilung der Anzahl an Kunden im System
	 */
	public final StatisticsTimePerformanceIndicator clientsInSystem;

	/**
	 * Verteilung der Anzahl an Kunden im System (wartend)
	 */
	public final StatisticsTimePerformanceIndicator clientsInSystemQueues;

	/**
	 * Verteilung der Anzahl an Kunden im System (wartend)
	 */
	public final StatisticsTimePerformanceIndicator clientsInSystemProcess;

	/**
	 * Verteilung der Anzahl an Kunden an den Stationen (erfasst nach Stationen)
	 */
	public final StatisticsMultiPerformanceIndicator clientsAtStationByStation;

	/**
	 * Verteilung der Anzahl an Kunden an den Stationen (erfasst nach Stationen und Kundentypen)
	 */
	public final StatisticsMultiPerformanceIndicator clientsAtStationByStationAndClient;

	/**
	 * Verteilung der Anzahl an Kunden an den Stationen (erfasst nach Kundentypen)
	 */
	public final StatisticsMultiPerformanceIndicator clientsInSystemByClient;

	/**
	 * Verteilung der Anzahl an Kunden in den Warteschlangen an den Stationen (erfasst nach Stationen)
	 */
	public final StatisticsMultiPerformanceIndicator clientsAtStationQueueByStation;

	/**
	 * Verteilung der Anzahl an Kunden in den Warteschlangen an den Stationen (erfasst nach Stationen und Kundentypen)
	 */
	public final StatisticsMultiPerformanceIndicator clientsAtStationQueueByStationAndClient;

	/**
	 * Verteilung der Anzahl an Kunden in den Warteschlangen an den Stationen (erfasst nach Kundentypen)
	 */
	public final StatisticsMultiPerformanceIndicator clientsAtStationQueueByClient;

	/**
	 * Verteilung der Anzahl an Kunden in Bedienung an den Stationen (erfasst nach Stationen)
	 */
	public final StatisticsMultiPerformanceIndicator clientsAtStationProcessByStation;

	/**
	 * Verteilung der Anzahl an Kunden in Bedienung an den Stationen (erfasst nach Stationen und Kundentypen)
	 */
	public final StatisticsMultiPerformanceIndicator clientsAtStationProcessByStationAndClient;

	/**
	 * Verteilung der Anzahl an Kunden in Bedienung an den Stationen (erfasst nach Kundentypen)
	 */
	public final StatisticsMultiPerformanceIndicator clientsAtStationProcessByClient;


	/* ====================================================
	 * Ressourcen
	 * ====================================================
	 */

	/**
	 * Anzahl an Bedienern in den jeweiligen Ressourcen
	 */
	public final StatisticsMultiPerformanceIndicator resourceCount;

	/**
	 * Auslastung der einzelnen Ressourcen
	 */
	public final StatisticsMultiPerformanceIndicator resourceUtilization;

	/**
	 * Auslastung der einzelnen Ressourcen
	 */
	public final StatisticsTimePerformanceIndicator resourceUtilizationAll;

	/**
	 * Ausfälle der Ressourcen
	 */
	public final StatisticsMultiPerformanceIndicator resourceInDownTime;

	/**
	 * Auslastung der Ressourcen (wird am Ende aus {@link #resourceCount} und {@link #resourceUtilization} berechnet.
	 * @see #calc()
	 */
	public final StatisticsMultiPerformanceIndicator resourceRho;

	/**
	 * Auslastung der Ressourcen (wird am Ende aus {@link #resourceCount} und {@link #resourceUtilizationAll} berechnet.
	 * @see #calc()
	 */
	public final StatisticsSimpleValuePerformanceIndicator resourceRhoAll;

	/* ====================================================
	 * Transporter
	 * ====================================================
	 */

	/**
	 * Auslastung der einzelnen Transportertypen
	 */
	public final StatisticsMultiPerformanceIndicator transporterUtilization;

	/**
	 * Ausfälle der Transporter
	 */
	public final StatisticsMultiPerformanceIndicator transporterInDownTime;

	/* ====================================================
	 * Zähler / Differenzzähler / Batch-Zähler
	 * ====================================================
	 */

	/**
	 * Erfassung von Zählern
	 */
	public final StatisticsMultiPerformanceIndicator counter;

	/**
	 * Erfassung von Differenzzählern
	 */
	public final StatisticsMultiPerformanceIndicator differentialCounter;

	/**
	 * Erfassung von Batch-Zählerdaten
	 */
	public final StatisticsMultiPerformanceIndicator counterBatch;


	/* ====================================================
	 * Kosten
	 * ====================================================
	 */

	/**
	 * Erfassung der Wartezeit-bedingten Kosten auf Kundenseite
	 */
	public final StatisticsMultiPerformanceIndicator clientsCostsWaiting;

	/**
	 * Erfassung der Transferzeit-bedingten Kosten auf Kundenseite
	 */
	public final StatisticsMultiPerformanceIndicator clientsCostsTransfer;

	/**
	 * Erfassung der Bedienzeit-bedingten Kosten auf Kundenseite
	 */
	public final StatisticsMultiPerformanceIndicator clientsCostsProcess;

	/**
	 * Erfassung der an den Stationen entstehenden Kosten
	 */
	public final StatisticsMultiPerformanceIndicator stationCosts;

	/**
	 * Erfassung der Kosten bedingt durch die generelle Verfügbarkeit der Ressourcen
	 */
	public final StatisticsMultiPerformanceIndicator resourceTimeCosts;

	/**
	 * Erfassung der Kosten bedingt durch die Auslastungszeit der Ressourcen
	 */
	public final StatisticsMultiPerformanceIndicator resourceWorkCosts;

	/**
	 * Erfassung der Kosten bedingt durch die Leerlaufzeit der Ressourcen
	 */
	public final StatisticsMultiPerformanceIndicator resourceIdleCosts;

	/* ====================================================
	 * Zeitliche Verläufe
	 * ====================================================
	 */

	/**
	 * Erfassung von Statistikdaten über die SimulationsLaufzeit hinweg
	 */
	public final StatisticsMultiPerformanceIndicator longRunStatistics;

	/* ====================================================
	 * Nutzerdefinierte Statistik
	 * ====================================================
	 */

	/**
	 * Erfassung von nutzerdefinierten Statistikdaten (diskrete Werte)
	 */
	public final StatisticsMultiPerformanceIndicator userStatistics;

	/**
	 * Erfassung von nutzerdefinierten Statistikdaten (zeitkontinuierliche Werte)
	 */
	public final StatisticsMultiPerformanceIndicator userStatisticsContinuous;

	/* ====================================================
	 * Variablenwerte
	 * ====================================================
	 */

	/**
	 * Werte der nutzerdefinierten Variablen
	 */
	public final StatisticsMultiPerformanceIndicator userVariables;

	/* ====================================================
	 * Zustände
	 * ====================================================
	 */

	/**
	 * Erfassung, wie lange sich das System in einem bestimmten Zustand befunden hat.
	 */
	public final StatisticsMultiPerformanceIndicator stateStatistics;

	/* ====================================================
	 * Analogwerte
	 * ====================================================
	 */

	/**
	 * Erfassung von Analogwerten
	 */
	public final StatisticsMultiPerformanceIndicator analogStatistics;

	/* ====================================================
	 * Durchsatz
	 * ====================================================
	 */

	/**
	 * Erfassung von Durchsatzwerten
	 */
	public final StatisticsMultiPerformanceIndicator throughputStatistics;

	/* ====================================================
	 * X-Y-Datenaufzeichnung
	 * ====================================================
	 */

	/**
	 * Aufzeichnung von Daten über Aufzeichnungselemente
	 */
	public StatisticsMultiPerformanceIndicator valueRecording;

	/* ====================================================
	 * Erfassung der Autokorrelation
	 * ====================================================
	 */

	/**
	 * Individuelle Werte der Wartezeiten der Kunden (über alle Kundentypen)<br>
	 * Dieses Feld kann im Gegensatz zu allen anderen <code>null</code> sein.
	 * Die Aufzeichnung von individuellen Werten ist nicht notwendig immer aktiv.
	 * @see Statistics#clientsAllWaitingTimes
	 */
	public StatisticsDataCollector clientsAllWaitingTimesCollector;

	/**
	 * Art der Erfassung der Autokorrelation
	 * @see Statistics#Statistics(int, CorrelationMode, int, boolean, int, int, boolean)
	 */
	public enum CorrelationMode {
		/** Keine Erfassung von Autokorrelationdaten */
		CORRELATION_MODE_OFF,

		/** Erfassung einer Autokorrelation über die Wartezeit aller Kunden */
		CORRELATION_MODE_FAST,

		/** Erfassung der Autokorrelation über alle Wartezeiten */
		CORRELATION_MODE_FULL
	}

	/**
	 * Maximale Anzahl an Einträge für zeitbasierende Histogramme
	 */
	private static int MAX_DISTRIBUTION_RECORD_TIME_STEPS=10_000;

	/**
	 * Begrenzung der vom Nutzer gewählten maximalen Anzahl an Kundendaten-Häufigkeitsverteilungs-Werten.
	 */
	private static int MAX_DISTRIBUTION_RECORD_CLIENT_VALUES=10_000_000;

	/**
	 * Optional: Name der Datei aus der die XML-Statistik-Daten stammen
	 */
	public File loadedStatistics;

	/**
	 * Konstruktor der Klasse <code>Statistics</code>
	 * @param correlationRange	Maximaler Autokorrelationswert der bei der Erfassung der Daten vorgesehen werden soll.
	 * @param correlationMode	Art der Erfassung der Autokorrelation
	 * @param batchSize	Wird hier ein Wert &gt;1 übergeben, so werden Batch-Means erfasst, auf deren Basis später Konfidenzintervalle bestimmt werden können
	 * @param collectWaitingTimes	Statistik für die Aufzeichnung der Einzel-Wartezeiten vorbereiten?
	 * @param distributionRecordHours	Wie lang sollen die Verteilungen der Werte ausfallen (in Stunden)? (Werte kleiner oder gleich 0 schalten die Erfassung ab.)
	 * @param dataToRecordInClientDataDistribution	Wie lang so die Verteilung der Kundendaten-Werte ausfallen? (Werte kleiner oder gleich 0 schalten die Erfassung ab.)
	 * @param useWelford	Soll der Welford-Algorithmus zur Erfassung der Varianz verwendet werden? (langsamer, aber bei ganz kleinen Variationskoeffizienten exakter)
	 * @see CorrelationMode#CORRELATION_MODE_OFF
	 * @see CorrelationMode#CORRELATION_MODE_FAST
	 * @see CorrelationMode#CORRELATION_MODE_FULL
	 */
	public Statistics(final int correlationRange, final CorrelationMode correlationMode, final int batchSize, final boolean collectWaitingTimes, final int distributionRecordHours, int dataToRecordInClientDataDistribution, final boolean useWelford) {
		final String[] nameStation=Language.trAll("Statistics.XML.Station");
		final String[] nameClientType=Language.trAll("Statistics.XML.ClientType");
		final String[] nameClientData=Language.trAll("Statistics.XML.ClientDataRecord");
		final String[] nameCosts=Language.trAll("Statistics.XML.Costs");
		final String[] nameExpression=Language.trAll("Statistics.XML.ExpressionData");

		final int rangeFast=(correlationMode==CorrelationMode.CORRELATION_MODE_OFF)?-1:correlationRange;
		final int rangeFull=(correlationMode!=CorrelationMode.CORRELATION_MODE_FULL)?-1:correlationRange;

		final int secondsToRecordInDistributions=(distributionRecordHours<=0)?-1:(3600*Math.max(1,distributionRecordHours));
		int timeSteps=secondsToRecordInDistributions;
		if (timeSteps>MAX_DISTRIBUTION_RECORD_TIME_STEPS) timeSteps/=3;
		if (timeSteps>MAX_DISTRIBUTION_RECORD_TIME_STEPS) timeSteps/=3;
		while (timeSteps>MAX_DISTRIBUTION_RECORD_TIME_STEPS) timeSteps/=2;
		dataToRecordInClientDataDistribution=(dataToRecordInClientDataDistribution<=0)?-1:Math.min(MAX_DISTRIBUTION_RECORD_CLIENT_VALUES,dataToRecordInClientDataDistribution);

		/* Basisdaten */
		editModel=new EditModel();
		addPerformanceIndicator(simulationData=new StatisticsSimulationBaseData(Language.trAll("Statistics.XML.Element.Simulation")));
		addPerformanceIndicator(threadBasedConfidence=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ThreadBasedConfidence"),new StatisticsSimpleValuePerformanceIndicator(Language.trAll("Statistics.XML.Element.ThreadBasedConfidence.Level"))));

		/* Zwischenankunftszeiten */
		addPerformanceIndicator(clientsInterarrivalTime=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.InterArrivalClients"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsInterarrivalTime=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.InterArrivalStations"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsInterarrivalTimeBatch=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.InterArrivalStationsBatch"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsInterarrivalTimeByState=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.InterArrivalStationsByState"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsInterarrivalTimeByClientType=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.InterArrivalStationsByClientType"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsMaxThroughput=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.MaxThroughput"),new StatisticsSimpleValueMaxPerformanceIndicator(nameStation)));

		/* Zwischenabgangszeiten */
		addPerformanceIndicator(clientsInterleavingTime=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.InterLeavingClients"),new StatisticsDataPerformanceIndicator(nameClientType,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsInterleavingTime=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.InterLeavingStations"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsInterleavingTimeBatch=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.InterLeavingStationsBatch"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsInterleavingTimeByClientType=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.InterLeavingStationsByClientType"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));

		/* Kundenbewegungen zwischen den Stationen */
		addPerformanceIndicator(stationTransition=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientMovement"),new StatisticsSimpleCountPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientMovement.Stations"),false))); /* kein Grouping, da "->" sonst als Trenner zwischen Name und Gruppe herangezogen würde */
		addPerformanceIndicator(clientPaths=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientPaths"),new StatisticsSimpleCountPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientPaths.Path"),false))); /* kein Grouping, da "->" sonst als Trenner zwischen Name und Gruppe herangezogen würde */

		/* Zeiten pro Kundentyp */
		addPerformanceIndicator(clientsWaitingTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.WaitingClients"),new StatisticsDataPerformanceIndicator(nameClientType,secondsToRecordInDistributions,timeSteps,rangeFull,batchSize,useWelford,true)));
		addPerformanceIndicator(clientsTransferTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.TransferClients"),new StatisticsDataPerformanceIndicator(nameClientType,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(clientsProcessingTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ProcessClients"),new StatisticsDataPerformanceIndicator(nameClientType,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(clientsResidenceTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ResidenceClients"),new StatisticsDataPerformanceIndicator(nameClientType,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(clientsAllWaitingTimes=new StatisticsDataPerformanceIndicator(Language.trAll("Statistics.XML.Element.WaitingAllClients"),secondsToRecordInDistributions,timeSteps,rangeFast,batchSize,useWelford));
		addPerformanceIndicator(clientsAllTransferTimes=new StatisticsDataPerformanceIndicator(Language.trAll("Statistics.XML.Element.TransferAllClients"),secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford));
		addPerformanceIndicator(clientsAllProcessingTimes=new StatisticsDataPerformanceIndicator(Language.trAll("Statistics.XML.Element.ProcessAllClients"),secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford));
		addPerformanceIndicator(clientsAllResidenceTimes=new StatisticsDataPerformanceIndicator(Language.trAll("Statistics.XML.Element.ResidenceAllClients"),secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford));

		/* Kundendatenfelder */
		addPerformanceIndicator(clientData=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientData"),new StatisticsDataPerformanceIndicatorWithNegativeValues(nameClientData,dataToRecordInClientDataDistribution,dataToRecordInClientDataDistribution,1,useWelford,true)));

		/* Zeiten auf Seiten der Stationen */
		addPerformanceIndicator(stationsWaitingTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.WaitingStations"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,rangeFull,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsTransferTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.TransferStations"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsProcessingTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ProcessStations"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsResidenceTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ResidenceStations"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsTotalWaitingTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.WaitingStationsTotal"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,rangeFull,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsTotalTransferTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.TransferStationsTotal"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsTotalProcessingTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ProcessStationsTotal"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsTotalResidenceTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ResidenceStationsTotal"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsWaitingTimesByClientType=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.WaitingStationsByClientType"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,rangeFull,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsTransferTimesByClientType=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.TransferStationsByClientType"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsProcessingTimesByClientType=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ProcessStationsByClientType"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsResidenceTimesByClientType=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ResidenceStationsByClientType"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,-1,batchSize,useWelford,true)));
		addPerformanceIndicator(stationsSetupTimes=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.SetupStations"),new StatisticsDataPerformanceIndicator(nameStation,secondsToRecordInDistributions,timeSteps,rangeFull,batchSize,useWelford,true)));

		/* Anzahlen an Kunden */
		addPerformanceIndicator(clientsInSystem=new StatisticsTimePerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientsInSystem")));
		addPerformanceIndicator(clientsInSystemQueues=new StatisticsTimePerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientsInSystemWaiting")));
		addPerformanceIndicator(clientsInSystemProcess=new StatisticsTimePerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientsInSystemProcessAll")));
		addPerformanceIndicator(clientsAtStationByStation=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientsAtStation"),new StatisticsTimePerformanceIndicator(nameStation)));
		addPerformanceIndicator(clientsAtStationByStationAndClient=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientsAtStationByClientType"),new StatisticsTimePerformanceIndicator(nameStation)));
		addPerformanceIndicator(clientsInSystemByClient=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientsAtStationByType"),new StatisticsTimePerformanceIndicator(nameClientType)));
		addPerformanceIndicator(clientsAtStationQueueByStation=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientsAtStationQueue"),new StatisticsTimePerformanceIndicator(nameStation)));
		addPerformanceIndicator(clientsAtStationQueueByStationAndClient=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientsAtStationQueueByClientType"),new StatisticsTimePerformanceIndicator(nameStation)));
		addPerformanceIndicator(clientsAtStationQueueByClient=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientsInSystemQueue"),new StatisticsTimePerformanceIndicator(nameClientType)));
		addPerformanceIndicator(clientsAtStationProcessByStation=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientsAtStationProcess"),new StatisticsTimePerformanceIndicator(nameStation)));
		addPerformanceIndicator(clientsAtStationProcessByStationAndClient=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientsAtStationProcessByClientType"),new StatisticsTimePerformanceIndicator(nameStation)));
		addPerformanceIndicator(clientsAtStationProcessByClient=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ClientsInSystemProcess"),new StatisticsTimePerformanceIndicator(nameClientType)));

		/* Ressourcen */
		addPerformanceIndicator(resourceCount=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.UtilizationCountParent"),new StatisticsTimePerformanceIndicator(Language.trAll("Statistics.XML.Element.UtilizationCount"))));
		addPerformanceIndicator(resourceUtilization=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.Utilization"),new StatisticsTimePerformanceIndicator(Language.trAll("Statistics.XML.Element.UtilizationResource"))));
		addPerformanceIndicator(resourceUtilizationAll=new StatisticsTimePerformanceIndicator(Language.trAll("Statistics.XML.Element.UtilizationAll")));
		addPerformanceIndicator(resourceInDownTime=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.InDownTime"),new StatisticsTimePerformanceIndicator(Language.trAll("Statistics.XML.Element.UtilizationResource"))));
		addPerformanceIndicator(resourceRho=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.Rho"),new StatisticsSimpleValuePerformanceIndicator(Language.trAll("Statistics.XML.Element.UtilizationResourceRho"))));
		addPerformanceIndicator(resourceRhoAll=new StatisticsSimpleValuePerformanceIndicator(Language.trAll("Statistics.XML.Element.UtilizationResourceRhoAll")));

		/* Transporter */
		addPerformanceIndicator(transporterUtilization=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.UtilizationTransporter"),new StatisticsTimePerformanceIndicator(Language.trAll("Statistics.XML.Element.UtilizationTransporterType"))));
		addPerformanceIndicator(transporterInDownTime=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.InDownTimeTransporter"),new StatisticsTimePerformanceIndicator(Language.trAll("Statistics.XML.Element.UtilizationTransporterType"))));

		/* Zähler / Differenzzähler / Batch-Zähler */
		addPerformanceIndicator(counter=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.Counter"),new StatisticsSimpleCountPerformanceIndicator(Language.trAll("Statistics.XML.Element.CounterName"))));
		addPerformanceIndicator(differentialCounter=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.DifferenceCounter"),new StatisticsTimePerformanceIndicator(Language.trAll("Statistics.XML.Element.DifferenceCounterName"))));
		addPerformanceIndicator(counterBatch=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.CounterBatch"),new StatisticsDataPerformanceIndicator(Language.trAll("Statistics.XML.Element.CounterBatchName"),dataToRecordInClientDataDistribution,dataToRecordInClientDataDistribution,-1,batchSize,useWelford,true)));

		/* Kosten */
		addPerformanceIndicator(clientsCostsWaiting=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.CostsWaiting"),new StatisticsValuePerformanceIndicator(nameCosts)));
		addPerformanceIndicator(clientsCostsTransfer=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.CostsTransfer"),new StatisticsValuePerformanceIndicator(nameCosts)));
		addPerformanceIndicator(clientsCostsProcess=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.CostsProcess"),new StatisticsValuePerformanceIndicator(nameCosts)));
		addPerformanceIndicator(stationCosts=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.CostsStations"),new StatisticsValuePerformanceIndicator(nameCosts)));
		addPerformanceIndicator(resourceTimeCosts=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ResourceTimeCosts"),new StatisticsValuePerformanceIndicator(nameCosts)));
		addPerformanceIndicator(resourceWorkCosts=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ResourceWorkCosts"),new StatisticsValuePerformanceIndicator(nameCosts)));
		addPerformanceIndicator(resourceIdleCosts=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Element.ResourceIdleCosts"),new StatisticsValuePerformanceIndicator(nameCosts)));

		/* Zeitliche Verläufe */
		addPerformanceIndicator(longRunStatistics=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Expression"),new StatisticsLongRunPerformanceIndicator(nameExpression)));

		/* Nutzerdefinierte Statistik */
		addPerformanceIndicator(userStatistics=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.UserStatistics"),new StatisticsDataPerformanceIndicatorWithNegativeValues(Language.trAll("Statistics.XML.UserStatisticsKey"),dataToRecordInClientDataDistribution,dataToRecordInClientDataDistribution,batchSize,useWelford,true)));
		addPerformanceIndicator(userStatisticsContinuous=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.UserStatisticsContinuous"),new StatisticsTimeContinuousPerformanceIndicator(Language.trAll("Statistics.XML.UserStatisticsContinuousKey"))));

		/* Nutzerdefinierte Variablen */
		addPerformanceIndicator(userVariables=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.Variables"),new StatisticsTimeContinuousPerformanceIndicator(Language.trAll("Statistics.XML.VariablesKey"))));

		/* Zustände */
		addPerformanceIndicator(stateStatistics=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.StateStatistics"),new StatisticsStateTimePerformanceIndicator(Language.trAll("Statistics.XML.StateStatisticsKey"))));

		/* Analogwerte */
		addPerformanceIndicator(analogStatistics=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.AnalogStatistics"),new StatisticsTimeAnalogPerformanceIndicator(Language.trAll("Statistics.XML.AnalogStatisticsName"))));

		/* Durchsatz */
		addPerformanceIndicator(throughputStatistics=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.ThroughputStatistics"),new StatisticsQuotientPerformanceIndicator(Language.trAll("Statistics.XML.ThroughputStatisticsName"))));

		/* X-Y-Datenaufzeichnung */
		addPerformanceIndicator(valueRecording=new StatisticsMultiPerformanceIndicator(Language.trAll("Statistics.XML.ValueRecording"),new StatisticsDataCollector(Language.trAll("Statistics.XML.ValueRecordingName"))));

		/* Erfassung der Autokorrelation */
		if (collectWaitingTimes) addPerformanceIndicator(clientsAllWaitingTimesCollector=new StatisticsDataCollector(Language.trAll("Statistics.XML.CollectedWaitingTimes"))); /* Nicht in xsd/dtd, da normalerweise Statistiken mit diesen Daten nicht gespeichert werden können. */

		resetData();
	}

	/**
	 * Konstruktor der Klasse <code>Statistics</code>
	 * @param collectWaitingTimes	Statistik für die Aufzeichnung der Einzel-Wartezeiten vorbereiten?
	 * @param useWelford	Soll der Welford-Algorithmus zur Erfassung der Varianz verwendet werden? (langsamer, aber bei ganz kleinen Variationskoeffizienten exakter)
	 */
	public Statistics(final boolean collectWaitingTimes, final boolean useWelford) {
		this(-1,CorrelationMode.CORRELATION_MODE_OFF,1,collectWaitingTimes,1,10000,useWelford);
	}

	/**
	 * Konstruktor der Klasse <code>Statistics</code><br>
	 * Es erfolgt keine Erfassung von Autokorrelationsdaten, d.h.
	 * der Aufruf dieses Konstruktors ist äquivalent zum Aufruf von <code>Statistics(-1,CORRELATION_MODE_OFF,1,false,1,10000,false);</code>.
	 */
	public Statistics() {
		this(-1,CorrelationMode.CORRELATION_MODE_OFF,1,false,1,10000,false);
	}

	@Override
	public void calc() {
		double countSum=0;

		for (String name: resourceUtilization.getNames()) {
			final StatisticsTimePerformanceIndicator utilization=(StatisticsTimePerformanceIndicator)resourceUtilization.getOrNull(name);
			final StatisticsTimePerformanceIndicator count=(StatisticsTimePerformanceIndicator)resourceCount.getOrNull(name);
			final double utilizationMean=utilization.getTimeMean();
			final double countMean=count.getTimeMean();
			if (countMean>0.0) {
				if (countSum>=0) countSum+=countMean;
				if (utilizationMean>0.0) {
					final StatisticsSimpleValuePerformanceIndicator rho=(StatisticsSimpleValuePerformanceIndicator)resourceRho.get(name);
					rho.set(utilizationMean/countMean);
				}
			} else {
				countSum=-1;
			}
		}

		final double utilizationGlobal=resourceUtilizationAll.getTimeMean();
		if (countSum>0.0 && utilizationGlobal>0.0) {
			resourceRhoAll.set(utilizationGlobal/countSum);
		}
	}

	/**
	 * Wurzel-Element für Statistik-xml-Dateien
	 */
	@Override
	public String[] getRootNodeNames() {
		return Language.trAll("Statistics.XML.Root");
	}

	@Override
	protected String loadProperty(final String name, final String text, final Element node) {
		for (String test: editModel.getRootNodeNames()) if (name.equalsIgnoreCase(test)) return editModel.loadFromXML(node);
		String error=super.loadProperty(name,text,node); if (error!=null) return error;
		return null;
	}

	@Override
	protected void addDataToXML(final Document doc, final Element node, final boolean isPartOfOtherFile, final File file) {
		editModel.saveToXML(node,true);
		super.addDataToXML(doc,node,isPartOfOtherFile,file);
	}
}