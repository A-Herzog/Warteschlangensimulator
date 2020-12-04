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

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionRandomNumber;
import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementAnalogProcessing;
import simulator.coreelements.RunElementAnalogProcessingData;
import simulator.coreelements.RunElementData;
import simulator.elements.ClientMoveListener;
import simulator.elements.FreeResourcesListener;
import simulator.elements.RunSource;
import simulator.elements.SignalListener;
import simulator.elements.StateChangeListener;
import simulator.elements.TransporterMoveListener;
import simulator.elements.TransporterPosition;
import simulator.events.SystemChangeEvent;
import simulator.events.TimedCheckEvent;
import simulator.simparser.ExpressionCalc;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import statistics.StatisticsValuePerformanceIndicator;
import ui.modeleditor.ModelResources;

/**
 * Dynamische, thread-lokale Laufzeitdaten
 * @author Alexander Herzog
 */
public class RunData {
	/**
	 * Laufzeitmodell, welches über alle Threads geteilt wird und folglich keine lokalen Daten enthalten darf.<br>
	 * Die lokalen Daten zu den Stationen befinden sich im <code>elementData</code>-Objekt.
	 * @see #getStationData(RunElement)
	 * @see #setStationData(RunElement, RunElementData)
	 */
	private final RunModel runModel;

	/**
	 * Anzahl der bereits eingetroffenen Kunden<br>
	 * (Beim Übergang <code>isWarmUp=true</code> zu <code>isWarmUp=false</code> wird dieses Feld auf 0 zurückgesetzt.)
	 */
	public long clientsArrived;

	/**
	 * Gibt an, ob es eine Einschwingphase gibt.
	 */
	public final boolean hasWarmUp;

	/**
	 * Befindet sich die Simulation noch in der Einschwingphase?
	 */
	public boolean isWarmUp;

	/**
	 * Zeitpunkt, an dem die Warm-up-Phase endet
	 */
	public double warmUpEndTime=0;

	/**
	 * Abbruch und Ende der Simulation
	 */
	public boolean stopp=false;

	/*
	 * Zugriff auf die stationsabhängigen Daten beschleunigen, in dem diese direkt nach IDs sortiert in
	 * einem Array gespeichert werden und nicht jedes Mal die HashMap nach dem Namen durchsucht werden muss.
	 */

	/**
	 * Cache für die "Zwischenankunftszeiten der Kunden (bei den "Kundenquelle"-Elementen)"-Statistikobjekte
	 * @see Statistics#clientsInterarrivalTime
	 */
	private IndicatorAccessCacheStations cacheClientsInterarrivalTime;

	/**
	 * Cache für die "Zwischenankunftszeiten der Kunden bei den einzelnen Stationen"-Statistikobjekte
	 * @see Statistics#stationsInterarrivalTime
	 */
	private IndicatorAccessCacheStations cacheStationsInterarrivalTime;

	/**
	 * Cache für die "Zwischenankunftszeiten der Kunden bei den einzelnen Stationen (Batch)"-Statistikobjekte
	 * @see Statistics#stationsInterarrivalTimeBatch
	 */
	private IndicatorAccessCacheStations cacheStationsInterarrivalTimeBatch;

	/**
	 * Cache für die "Zwischenankunftszeiten der Kunden bei den einzelnen Stationen ausdifferenziert nach der Anzahl an Kunden an der Station"-Statistikobjekte
	 * @see Statistics#stationsInterarrivalTimeByState
	 */
	private IndicatorAccessCacheStationsNumber cacheStationsInterarrivalTimeByState;

	/**
	 * Cache für die "Zwischenankunftszeiten der Kunden bei den einzelnen Stationen (zusätzlich differenziert nach Kundentyp)"-Statistikobjekte
	 * @see Statistics#stationsInterarrivalTimeByClientType
	 */
	private IndicatorAccessCacheStationsClientTypes cacheStationsInterarrivalTimeByClientType;

	/**
	 * Cache für die "Zwischenabgangszeiten der Kunden bei den einzelnen Stationen"-Statistikobjekte
	 * @see Statistics#stationsInterleavingTime
	 */
	private IndicatorAccessCacheStations cacheStationsInterleavingTime;

	/**
	 * Cache für die "Zwischenabgangszeiten der Kunden bei den einzelnen Stationen (zusätzlich differenziert nach Kundentyp)"-Statistikobjekte
	 * @see Statistics#stationsInterleavingTimeByClientType
	 */
	private IndicatorAccessCacheStationsClientTypes cacheStationsInterleavingTimeByClientType;

	/**
	 * Cache für die "Wartezeiten an den Stationen"-Statistikobjekte
	 * @see Statistics#stationsWaitingTimes
	 */
	private IndicatorAccessCacheStations cacheStationsWaitingTimes;

	/**
	 * Cache für die "Transferzeiten an den Stationen"-Statistikobjekte
	 * @see Statistics#stationsTransferTimes
	 */
	private IndicatorAccessCacheStations cacheStationsTransferTimes;

	/**
	 * Cache für die "Bedienzeiten an den Stationen"-Statistikobjekte
	 * @see Statistics#stationsProcessingTimes
	 */
	private IndicatorAccessCacheStations cacheStationsProcessingTimes;

	/**
	 * Cache für die "Verweilzeiten an den Stationen"-Statistikobjekte
	 * @see Statistics#stationsResidenceTimes
	 */
	private IndicatorAccessCacheStations cacheStationsResidenceTimes;

	/**
	 * Cache für die "Wartezeiten an den Stationen (zusätzlich differenziert nach Kundentyp)"-Statistikobjekte
	 * @see Statistics#stationsWaitingTimesByClientType
	 */
	private IndicatorAccessCacheStationsClientTypes cacheStationsWaitingTimesByClientType;

	/**
	 * Cache für die "Transferzeiten an den Stationen (zusätzlich differenziert nach Kundentyp)"-Statistikobjekte
	 * @see Statistics#stationsTransferTimesByClientType
	 */
	private IndicatorAccessCacheStationsClientTypes cacheStationsTransferTimesByClientType;

	/**
	 * Cache für die "Bedienzeiten an den Stationen (zusätzlich differenziert nach Kundentyp)"-Statistikobjekte
	 * @see Statistics#stationsProcessingTimesByClientType
	 */
	private IndicatorAccessCacheStationsClientTypes cacheStationsProcessingTimesByClientType;

	/**
	 * Cache für die "Verweilzeiten an den Stationen (zusätzlich differenziert nach Kundentyp)"-Statistikobjekte
	 * @see Statistics#stationsResidenceTimesByClientType
	 */
	private IndicatorAccessCacheStationsClientTypes cacheStationsResidenceTimesByClientType;

	/**
	 * Cache für die "Verteilung der Anzahl an Kunden an den Stationen (erfasst nach Stationen)"-Statistikobjekte
	 * @see Statistics#clientsAtStationByStation
	 */
	private IndicatorAccessCacheStations cacheClientsAtStation;

	/**
	 * Cache für die "Verteilung der Anzahl an Kunden an den Stationen (erfasst nach Stationen und Kundentypen)"-Statistikobjekte
	 * @see Statistics#clientsAtStationByStationAndClient
	 */
	private IndicatorAccessCacheStationsClientTypes cacheClientsAtStationByClientType;

	/**
	 * Cache für die "Verteilung der Anzahl an Kunden an den Stationen (erfasst nach Kundentypen)"-Statistikobjekte
	 * @see Statistics#clientsInSystemByClient
	 */
	private IndicatorAccessCacheClientTypes cacheClientsInSystemByType;

	/**
	 * Cache für die "Verteilung der Anzahl an Kunden in den Warteschlangen an den Stationen (erfasst nach Stationen)"-Statistikobjekte
	 * @see Statistics#clientsAtStationQueueByStation
	 */
	private IndicatorAccessCacheStations cacheClientsAtStationQueueByStation;

	/**
	 * Cache für die "Verteilung der Anzahl an Kunden in den Warteschlangen an den Stationen (erfasst nach Stationen und Kundentypen)"-Statistikobjekte
	 * @see Statistics#clientsAtStationQueueByStationAndClient
	 */
	private IndicatorAccessCacheStationsClientTypes cacheClientsAtStationQueueByStationByClientType;

	/**
	 * Cache für die "Verteilung der Anzahl an Kunden in den Warteschlangen an den Stationen (erfasst nach Kundentypen)"-Statistikobjekte
	 * @see Statistics#clientsAtStationQueueByClient
	 */
	private IndicatorAccessCacheClientTypes cacheClientsAtStationQueueByClient;

	/**
	 * Cache für die "Erfassung der an den Stationen entstehenden Kosten"-Statistikobjekte
	 * @see Statistics#stationCosts
	 */
	private IndicatorAccessCacheStations cacheStationCosts;

	/**
	 * Speichert, wie viele Kunden pro Kundentyp sich momentan in der Warteschlange einer Station aufhalten.
	 */
	private int[] clientsAtStationQueueByType;

	/**
	 * Speichert, wie viele Kunden pro Kundentyp sich momentan im System befinden.
	 */
	private int[] clientsInSystemByType;

	/**
	 * Hält den Cache der momentan nicht verwendeten <code>RunDataClient</code>-Objekte vor und
	 * erfasst am Lebensdauerende eines <code>RunDataClient</code>-Objektes die Daten in der Statistik
	 */
	public final RunDataClients clients;

	/**
	 * Laufzeitdaten für die <code>RunElement</code>-Objekte<br>
	 * (Die <code>RunElement</code>-Objekte selbst gehören zu <code>RunModel</code> und werden über alle Threads geteilt, können
	 * also keine thread-spezifischen Daten - wie dies <code>RunData</code> kann - enthalten.)
	 */
	private RunElementData[] elementData;

	/**
	 * Enthält Informationen darüber, wie viele Bediener welchen Typs momentan verfügbar sind
	 */
	public final RunDataResources resources;

	/**
	 * Enthält Informationen darüber, welcher Transporter sich gerade wo befindet
	 */
	public final RunDataTransporters transporters;

	/**
	 * Werte der Variablen im System
	 * @see #setClientVariableValues(RunDataClient)
	 * @see #setClientVariableValues(RunDataClient, double)
	 * @see #setClientVariableValues(long, long, long)
	 */
	public final double[] variableValues;

	/**
	 * Liste der Namen der Differenzzähler
	 */
	public final List<String> differentialCounterName;

	/**
	 * Werte der Differenzzähler
	 */
	public int[] differentialCounterValue;

	/**
	 * Auswertbare Ausdrücke für die Zuweisungen pro Schritt
	 */
	public ExpressionCalc[][][] sequenceStepAssignmentExpression;

	/**
	 * Zuordnung der schon initialisierten Dateiausgabe-Objekte
	 * @see #getOutputWriter(File)
	 */
	private Map<File,RunDataOutputWriter> outputWriter;

	/**
	 * Anzahl der Kunden im System, die an irgendeiner Station warten
	 * @see #logClientEntersStationQueue(SimulationData, RunElement, RunElementData, RunDataClient)
	 * @see #logClientLeavesStationQueue(SimulationData, RunElement, RunElementData, RunDataClient)
	 */
	private int waitingClients;

	/**
	 * Thread-übergreifendes System um alle Threads möglichst gleichmäßig mit Kundenankünften zu versorgen
	 */
	private final DynamicLoadBalancer dynamicLoadBalancer;

	/**
	 * Konstruktor der Klasse <code>RunData</code>
	 * @param runModel	Globales Laufzeitmodell, auf dessen Basis hier die Laufzeitdaten vorbereitet werden können (z.B. Arrays in passender Größe angelegt werden usw.)
	 * @param dynamicLoadBalancer	Optional ein Load-Balancer für die Ankünfte über alle Threads (kann <code>null</code> sein)
	 */
	public RunData(final RunModel runModel, final DynamicLoadBalancer dynamicLoadBalancer) {
		this.runModel=runModel;
		this.dynamicLoadBalancer=dynamicLoadBalancer;
		clientsArrived=0;
		hasWarmUp=runModel.warmUpTime>0;
		isWarmUp=hasWarmUp;
		clients=new RunDataClients();
		this.resources=runModel.resourcesTemplate.clone();
		this.transporters=runModel.transportersTemplate.clone();
		variableValues=new double[runModel.variableNames.length];
		differentialCounterName=new ArrayList<>();
		outputWriter=new HashMap<>();
		waitingClients=0;

		sequenceStepAssignmentExpression=new ExpressionCalc[runModel.sequenceStepAssignmentExpression.length][][];
		for (int i=0;i<sequenceStepAssignmentExpression.length;i++) {
			sequenceStepAssignmentExpression[i]=new ExpressionCalc[runModel.sequenceStepAssignmentExpression[i].length][];
			for (int j=0;j<sequenceStepAssignmentExpression[i].length;j++) {
				sequenceStepAssignmentExpression[i][j]=new ExpressionCalc[runModel.sequenceStepAssignmentExpression[i][j].length];
				for (int k=0;k<sequenceStepAssignmentExpression[i][j].length;k++) {
					sequenceStepAssignmentExpression[i][j][k]=new ExpressionCalc(runModel.variableNames);
					sequenceStepAssignmentExpression[i][j][k].parse(runModel.sequenceStepAssignmentExpression[i][j][k]);
				}
			}
		}
	}

	/**
	 * Vorbereitung einer einzelnen Wiederholung der Simulation
	 * @param nr	Nummer der Wiederholung (0-basierend und thread-lokal)
	 * @param simData	Objekt vom Typ <code>SimulationData</code>, welches das Laufzeitmodell (vom Typ <code>RunModel</code> im Feld <code>runModel</code>) und die Statistik (vom Typ <code>Statistics</code> im Feld <code>statistics</code>) enthält und den Zugriff auf die von <code>SimData</code> geerbten Basis-Funktionen ermöglicht
	 * @param recordIncompleteClients	Sollen auch Kunden, die das System am Ende noch nicht verlassen haben, in der Statistik erfasst werden können (<code>true</code>). Dies verlangsamt die Simulation.
	 */
	public void initRun(final long nr, final SimulationData simData, final boolean recordIncompleteClients) {
		for (int i=0;i<variableValues.length;i++) {
			variableValues[i]=0;
			if (runModel.variableInitialValues[i]!=null) {
				try {
					variableValues[i]=runModel.variableInitialValues[i].calc();
				} catch (MathCalcError e) {}
			}
		}

		if (nr==0 && simData.runModel.useFixedSeed) {
			DistributionRandomNumber.generator.setSeed(simData.runModel.fixedSeed+simData.threadNr);
		}

		for (RunElement element: runModel.elementsFast) {
			if (element instanceof RunSource) ((RunSource)element).scheduleInitialArrivals(simData);
		}

		cacheClientsInterarrivalTime=new IndicatorAccessCacheStations(simData.statistics.clientsInterarrivalTime);
		cacheStationsInterarrivalTime=new IndicatorAccessCacheStations(simData.statistics.stationsInterarrivalTime);
		cacheStationsInterarrivalTimeBatch=new IndicatorAccessCacheStations(simData.statistics.stationsInterarrivalTimeBatch);
		cacheStationsInterarrivalTimeByState=new IndicatorAccessCacheStationsNumber(simData.statistics.stationsInterarrivalTimeByState,"NQ");
		cacheStationsInterarrivalTimeByClientType=new IndicatorAccessCacheStationsClientTypes(simData.statistics.stationsInterarrivalTimeByClientType);
		cacheStationsInterleavingTime=new IndicatorAccessCacheStations(simData.statistics.stationsInterleavingTime);
		cacheStationsInterleavingTimeByClientType=new IndicatorAccessCacheStationsClientTypes(simData.statistics.stationsInterleavingTimeByClientType);
		cacheStationsWaitingTimes=new IndicatorAccessCacheStations(simData.statistics.stationsWaitingTimes);
		cacheStationsTransferTimes=new IndicatorAccessCacheStations(simData.statistics.stationsTransferTimes);
		cacheStationsProcessingTimes=new IndicatorAccessCacheStations(simData.statistics.stationsProcessingTimes);
		cacheStationsResidenceTimes=new IndicatorAccessCacheStations(simData.statistics.stationsResidenceTimes);
		cacheStationsWaitingTimesByClientType=new IndicatorAccessCacheStationsClientTypes(simData.statistics.stationsWaitingTimesByClientType);
		cacheStationsTransferTimesByClientType=new IndicatorAccessCacheStationsClientTypes(simData.statistics.stationsTransferTimesByClientType);
		cacheStationsProcessingTimesByClientType=new IndicatorAccessCacheStationsClientTypes(simData.statistics.stationsProcessingTimesByClientType);
		cacheStationsResidenceTimesByClientType=new IndicatorAccessCacheStationsClientTypes(simData.statistics.stationsResidenceTimesByClientType);
		cacheClientsAtStation=new IndicatorAccessCacheStations(simData.statistics.clientsAtStationByStation);
		cacheClientsAtStationByClientType=new IndicatorAccessCacheStationsClientTypes(simData.statistics.clientsAtStationByStationAndClient);
		cacheClientsInSystemByType=new IndicatorAccessCacheClientTypes(simData.statistics.clientsInSystemByClient,simData.runModel.clientTypes);
		cacheClientsAtStationQueueByStation=new IndicatorAccessCacheStations(simData.statistics.clientsAtStationQueueByStation);
		cacheClientsAtStationQueueByStationByClientType=new IndicatorAccessCacheStationsClientTypes(simData.statistics.clientsAtStationQueueByStationAndClient);
		cacheClientsAtStationQueueByClient=new IndicatorAccessCacheClientTypes(simData.statistics.clientsAtStationQueueByClient,simData.runModel.clientTypes);
		cacheStationCosts=new IndicatorAccessCacheStations(simData.statistics.stationCosts);

		resources.prepareOperatorObjects(simData);
		transporters.prepare(simData);

		timedCheckStations.clear();

		/* State Change direkt zu Beginn auslösen, damit sich Elemente, die dies zum Verzögerten Init benötigen direkt am Anfang initalisieren können. */
		fireStateChangeNotify(simData);

		if (recordIncompleteClients) clients.requestFastClientsInUseList();
	}

	/**
	 * Abschluss einer einzelnen Wiederholung der Simulation
	 * @param now	Ausführungszeitpunkt des letzten Events
	 * @param simData	Objekt vom Typ <code>SimulationData</code>, welches das Laufzeitmodell (vom Typ <code>RunModel</code> im Feld <code>runModel</code>) und die Statistik (vom Typ <code>Statistics</code> im Feld <code>statistics</code>) enthält und den Zugriff auf die von <code>SimData</code> geerbten Basis-Funktionen ermöglicht
	 * @param disposeClients	Kunden zwangsweise aus dem System austragen (funktioniert nur, wenn auch die Erfassung der aktuellen Kunden aktiv ist)
	 */
	public void doneRun(final long now, final SimulationData simData, final boolean disposeClients) {
		/* Benachrichtigt alle Analog-Wert-Elemente am Ende der Simulation, um die Statistik noch einmal zu aktualisieren. */
		for (Map.Entry<Integer,RunElement> element: simData.runModel.elements.entrySet()) {
			if (element.getValue() instanceof RunElementAnalogProcessing) {
				final RunElementAnalogProcessingData data=(RunElementAnalogProcessingData)element.getValue().getData(simData);
				data.updateStatistics(simData.currentTime);
			}
		}

		/* Anzahl an Kunden im System letztmalig erfassen */
		clients.finalizeNumberOfClientsInSystem(simData);

		/* Kunden zwangsweise aus dem System austragen (funktioniert nur, wenn auch die Erfassung der aktuellen Kunden aktiv ist) */
		if (disposeClients) clients.disposeAll(simData);
	}

	/**
	 * Erfasst die Ankunft eines Kunden an einer Bedienstation für die Statistik (Zwischenankunftszeiten an den Stationen)
	 * @param now	Ankunftszeitpunkt
	 * @param simData	Objekt vom Typ <code>SimulationData</code>, welches das Laufzeitmodell (vom Typ <code>RunModel</code> im Feld <code>runModel</code>) und die Statistik (vom Typ <code>Statistics</code> im Feld <code>statistics</code>) enthält und den Zugriff auf die von <code>SimData</code> geerbten Basis-Funktionen ermöglicht
	 * @param station	Station, an der der Kunde eingetroffen ist
	 * @param stationData	Optionales Objekt mit den thread-lokalen Daten zu der Station (kann <code>null</code> sein, dann ermittelt es diese Funktion selbst)
	 * @param client	Kunde, der an der Station eingetroffen ist
	 */
	public void logStationArrival(final long now, final SimulationData simData, final RunElement station, final RunElementData stationData, final RunDataClient client) {
		final RunElementData data=(stationData==null)?station.getData(simData):stationData;

		data.clients++;

		if (isWarmUp) {
			data.lastArrival=now;

			final int clientType=client.type;
			if (data.lastArrivalByClientType==null) {
				data.lastArrivalByClientType=new long[simData.runModel.clientTypes.length];
				Arrays.fill(data.lastArrivalByClientType,-1);
			}
			data.lastArrivalByClientType[clientType]=now;

			return;
		}

		if (data.lastArrival<0 || data.lastArrival>now) data.lastArrival=0; /* Wenn kein Warmup, dann wird Zeitpunkt 0 als erste "Ankunft" für die Zählung der Zwischenankunftszeiten verwendet. */

		if (data.lastArrival>=0 && data.lastArrival<=now) {
			final double delta=scale*(now-data.lastArrival);
			StatisticsDataPerformanceIndicator indicator;

			if (station.stationStatisticsActive) {
				/* Allgemein */
				indicator=data.statisticStationsInterarrivalTime;
				if (indicator==null) indicator=data.statisticStationsInterarrivalTime=(StatisticsDataPerformanceIndicator)(cacheStationsInterarrivalTime.get(station));
				indicator.add(delta);

				/* Pro Zustand */
				if (station.isInterarrivalByQueueStation()) {
					final int count=data.clientsAtStationQueue;
					indicator=(StatisticsDataPerformanceIndicator)cacheStationsInterarrivalTimeByState.get(station,count);
					indicator.add(delta);
				}
			}

			/* Systemankunft */
			if (station instanceof RunSource) {
				indicator=data.statisticSourceStationsInterarrivalTime;
				if (indicator==null) indicator=data.statisticSourceStationsInterarrivalTime=(StatisticsDataPerformanceIndicator)(cacheClientsInterarrivalTime.get(station));
				indicator.add(delta);
			}
		}
		data.lastArrival=now;

		/* Pro Kundentyp */
		final int clientType=client.type;
		if (station.stationStatisticsActive) {
			if (data.lastArrivalByClientType!=null && data.lastArrivalByClientType[clientType]>=0 && data.lastArrivalByClientType[clientType]<=now) {
				final double delta=scale*(now-data.lastArrivalByClientType[clientType]);
				StatisticsDataPerformanceIndicator indicator=(data.statisticStationsInterarrivalTimeByClientType==null)?null:data.statisticStationsInterarrivalTimeByClientType[clientType];
				if (indicator==null) {
					if (data.statisticStationsInterarrivalTimeByClientType==null) data.statisticStationsInterarrivalTimeByClientType=new StatisticsDataPerformanceIndicator[simData.runModel.clientTypes.length];
					indicator=data.statisticStationsInterarrivalTimeByClientType[clientType]=(StatisticsDataPerformanceIndicator)(cacheStationsInterarrivalTimeByClientType.get(simData,station,client));
				}
				indicator.add(delta);

			}
		}
		if (data.lastArrivalByClientType==null) {
			data.lastArrivalByClientType=new long[simData.runModel.clientTypes.length];
			Arrays.fill(data.lastArrivalByClientType,-1);
		}
		data.lastArrivalByClientType[clientType]=now;
	}

	/**
	 * Erfasst die Ankunft eines Kunden-Batches an einer Bedienstation für die Statistik (Zwischenankunftszeiten an den Stationen).<br>
	 * Diese Funktion wirkt nur direkt auf die Statistik und verändert keine Modelldaten. Sie ist vollständig optional und
	 * wird nur von den Stationen, an denen Batches eintreffen, verwendet.
	 * @param now	Ankunftszeitpunkt
	 * @param simData	Objekt vom Typ <code>SimulationData</code>, welches das Laufzeitmodell (vom Typ <code>RunModel</code> im Feld <code>runModel</code>) und die Statistik (vom Typ <code>Statistics</code> im Feld <code>statistics</code>) enthält und den Zugriff auf die von <code>SimData</code> geerbten Basis-Funktionen ermöglicht
	 * @param station	Station, an der der Kunde eingetroffen ist
	 * @param stationData	Optionales Objekt mit den thread-lokalen Daten zu der Station (kann <code>null</code> sein, dann ermittelt es diese Funktion selbst)
	 */
	public void logStationBatchArrival(final long now, final SimulationData simData, final RunElement station, final RunElementData stationData) {
		final RunElementData data=(stationData==null)?station.getData(simData):stationData;

		if (isWarmUp) {
			data.lastBatchArrival=now;
			return;
		}

		if (data.lastBatchArrival<0 || data.lastBatchArrival>now) data.lastBatchArrival=0; /* Wenn kein Warmup, dann wird Zeitpunkt 0 als erste "Ankunft" für die Zählung der Zwischenankunftszeiten verwendet. */

		if (data.lastBatchArrival>=0 && data.lastBatchArrival<=now) {
			final double delta=scale*(now-data.lastBatchArrival);
			StatisticsDataPerformanceIndicator indicator;

			if (station.stationStatisticsActive) {
				/* Allgemein */
				indicator=data.statisticStationsInterarrivalTimeBatch;
				if (indicator==null) indicator=data.statisticStationsInterarrivalTimeBatch=(StatisticsDataPerformanceIndicator)(cacheStationsInterarrivalTimeBatch.get(station));
				indicator.add(delta);
			}
		}
		data.lastBatchArrival=now;
	}

	/**
	 * Erfasst den Abgang eines Kunden von einer Bedienstation für die Statistik (Zwischenabgangszeiten an den Stationen)
	 * @param now	Abgangszeitpunkt
	 * @param simData	Objekt vom Typ <code>SimulationData</code>, welches das Laufzeitmodell (vom Typ <code>RunModel</code> im Feld <code>runModel</code>) und die Statistik (vom Typ <code>Statistics</code> im Feld <code>statistics</code>) enthält und den Zugriff auf die von <code>SimData</code> geerbten Basis-Funktionen ermöglicht
	 * @param station	Station, die der Kunde verlassen hat
	 * @param client	Kunde, der die Station verlassen hat
	 */
	public void logStationLeave(final long now, final SimulationData simData, final RunElement station, final RunDataClient client) {
		final RunElementData data=station.getData(simData);

		if (isWarmUp) {
			data.lastLeave=now;

			final int clientType=client.type;
			if (data.lastLeaveByClientType==null) {
				data.lastLeaveByClientType=new long[simData.runModel.clientTypes.length];
				Arrays.fill(data.lastLeaveByClientType,-1);
			}
			data.lastLeaveByClientType[clientType]=now;

			return;
		}

		if (station.stationStatisticsActive) {
			if (data.lastLeave>=0 && data.lastLeave<=now) {
				final double delta=scale*(now-data.lastLeave);
				StatisticsDataPerformanceIndicator indicator;

				indicator=data.statisticStationsInterleaveTime;
				if (indicator==null) indicator=data.statisticStationsInterleaveTime=(StatisticsDataPerformanceIndicator)(cacheStationsInterleavingTime.get(station));
				indicator.add(delta);
			}
		}
		data.lastLeave=now;

		/* Pro Kundentyp */
		final int clientType=client.type;
		if (station.stationStatisticsActive) {
			if (data.lastLeaveByClientType!=null && data.lastLeaveByClientType[clientType]>=0 && data.lastLeaveByClientType[clientType]<=now) {
				final double delta=scale*(now-data.lastLeaveByClientType[clientType]);
				StatisticsDataPerformanceIndicator indicator=(data.statisticStationsInterleaveTimeByClientType==null)?null:data.statisticStationsInterleaveTimeByClientType[clientType];
				if (indicator==null) {
					if (data.statisticStationsInterleaveTimeByClientType==null) data.statisticStationsInterleaveTimeByClientType=new StatisticsDataPerformanceIndicator[simData.runModel.clientTypes.length];
					indicator=data.statisticStationsInterleaveTimeByClientType[clientType]=(StatisticsDataPerformanceIndicator)(cacheStationsInterleavingTimeByClientType.get(simData,station,client));
				}
				indicator.add(delta);
			}
		}
		if (data.lastLeaveByClientType==null) {
			data.lastLeaveByClientType=new long[simData.runModel.clientTypes.length];
			Arrays.fill(data.lastLeaveByClientType,-1);
		}
		data.lastLeaveByClientType[clientType]=now;
	}

	/** Umrechnungsfaktor von Millisekunden auf Sekunden, um die Division während der Simulation zu vermeiden */
	private static double scale=1.0d/1000.0d;

	/**
	 * Erfasst die Zeitdauer der Verarbeitung eines Kunden an einer Bedienstation für die Statistik (Wartezeiten und Bedienzeiten an den Stationen)
	 * @param simData	Objekt vom Typ <code>SimulationData</code>, welches das Laufzeitmodell (vom Typ <code>RunModel</code> im Feld <code>runModel</code>) und die Statistik (vom Typ <code>Statistics</code> im Feld <code>statistics</code>) enthält und den Zugriff auf die von <code>SimData</code> geerbten Basis-Funktionen ermöglicht
	 * @param station	Station
	 * @param client	Kunde (wird nicht verändert, wird nur verwendet um den Typ auszulesen für die Station-Kundentyp-Statistik)
	 * @param waitingTime	Wartezeit des Kunden an der Stations (0, wenn evtl. systembedingt nicht gewartet wurde)
	 * @param transferTime	Transferzeit des Kunden an der Stations (0, wenn evtl. systembedingt keine Transferzeit angefallen ist)
	 * @param processingTime	Bedienzeit des Kunden an der Stations (0, wenn evtl. systembedingt keine Bedienung erfolgt ist)
	 * @param residenceTime	Verweilzeit des Kunden an der Stations
	 */
	public void logStationProcess(final SimulationData simData, final RunElement station, final RunDataClient client, final long waitingTime, final long transferTime, final long processingTime, final long residenceTime) {
		if (isWarmUp) return;

		final double waitingTimeScaled=(waitingTime==0)?0:scale*waitingTime;
		final double transferTimeScaled=(transferTime==0)?0:scale*transferTime;
		final double processingTimeScaled=(processingTime==0)?0:scale*processingTime;
		final double residenceTimeScaled=(residenceTime==0)?0:scale*residenceTime;

		StatisticsDataPerformanceIndicator indicator;
		final RunElementData stationData=station.getData(simData);

		indicator=stationData.statisticWaiting;
		if (indicator==null) indicator=stationData.statisticWaiting=(StatisticsDataPerformanceIndicator)(cacheStationsWaitingTimes.get(station));
		indicator.add(waitingTimeScaled);

		indicator=stationData.statisticTransfer;
		if (indicator==null) indicator=stationData.statisticTransfer=(StatisticsDataPerformanceIndicator)(cacheStationsTransferTimes.get(station));
		indicator.add(transferTimeScaled);

		indicator=stationData.statisticProcess;
		if (indicator==null) indicator=stationData.statisticProcess=(StatisticsDataPerformanceIndicator)(cacheStationsProcessingTimes.get(station));
		indicator.add(processingTimeScaled);

		indicator=stationData.statisticResidence;
		if (indicator==null) indicator=stationData.statisticResidence=(StatisticsDataPerformanceIndicator)(cacheStationsResidenceTimes.get(station));
		indicator.add(residenceTimeScaled);

		final int clientType=client.type;

		indicator=(stationData.statisticWaitingByClientType==null)?null:stationData.statisticWaitingByClientType[clientType];
		if (indicator==null) {
			if (stationData.statisticWaitingByClientType==null) stationData.statisticWaitingByClientType=new StatisticsDataPerformanceIndicator[simData.runModel.clientTypes.length];
			indicator=stationData.statisticWaitingByClientType[clientType]=(StatisticsDataPerformanceIndicator)(cacheStationsWaitingTimesByClientType.get(simData,station,client));
		}
		indicator.add(waitingTimeScaled);

		indicator=(stationData.statisticTransferByClientType==null)?null:stationData.statisticTransferByClientType[clientType];
		if (indicator==null) {
			if (stationData.statisticTransferByClientType==null) stationData.statisticTransferByClientType=new StatisticsDataPerformanceIndicator[simData.runModel.clientTypes.length];
			indicator=stationData.statisticTransferByClientType[clientType]=(StatisticsDataPerformanceIndicator)(cacheStationsTransferTimesByClientType.get(simData,station,client));
		}
		indicator.add(transferTimeScaled);

		indicator=(stationData.statisticProcessByClientType==null)?null:stationData.statisticProcessByClientType[clientType];
		if (indicator==null) {
			if (stationData.statisticProcessByClientType==null) stationData.statisticProcessByClientType=new StatisticsDataPerformanceIndicator[simData.runModel.clientTypes.length];
			indicator=stationData.statisticProcessByClientType[clientType]=(StatisticsDataPerformanceIndicator)(cacheStationsProcessingTimesByClientType.get(simData,station,client));
		}
		indicator.add(processingTimeScaled);

		indicator=(stationData.statisticResidenceByClientType==null)?null:stationData.statisticResidenceByClientType[clientType];
		if (indicator==null) {
			if (stationData.statisticResidenceByClientType==null) stationData.statisticResidenceByClientType=new StatisticsDataPerformanceIndicator[simData.runModel.clientTypes.length];
			indicator=stationData.statisticResidenceByClientType[clientType]=(StatisticsDataPerformanceIndicator)(cacheStationsResidenceTimesByClientType.get(simData,station,client));
		}
		indicator.add(residenceTimeScaled);
	}

	/**
	 * Erhöht den Zähler für die Kunden an der Bedienstation um eins<br>
	 * (um in der Statistik zu erfassen, wie lange sich wie viele Kunde an der Station befunden haben)
	 * @param simData	Objekt vom Typ <code>SimulationData</code>, welches das Laufzeitmodell (vom Typ <code>RunModel</code> im Feld <code>runModel</code>) und die Statistik (vom Typ <code>Statistics</code> im Feld <code>statistics</code>) enthält und den Zugriff auf die von <code>SimData</code> geerbten Basis-Funktionen ermöglicht
	 * @param station	Station
	 * @param client	Kunde (für die Erfassung nach Kundentyp)
	 * @param stationData	Optionales Objekt mit den thread-lokalen Daten zu der Station (kann <code>null</code> sein, dann ermittelt es diese Funktion selbst)
	 */
	public void logClientEntersStation(final SimulationData simData, final RunElement station, RunElementData stationData, final RunDataClient client) {
		if (stationData==null) stationData=station.getData(simData);
		final int count1=clientsAtStation(simData,station,stationData,1);  /* Anzahl an Kunden an Station auch in WarmUp-Phase aktualisieren */
		final int count2=clientsAtStationByType(simData,station,stationData,client,1);
		if (isWarmUp) return;

		if (station.stationStatisticsActive) {
			StatisticsTimePerformanceIndicator indicator;
			final double time=simData.currentTime*scale;

			indicator=stationData.statisticClientsAtStation;
			if (indicator==null) indicator=stationData.statisticClientsAtStation=(StatisticsTimePerformanceIndicator)(cacheClientsAtStation.get(station));
			indicator.set(time,count1);

			if (stationData.statisticClientsAtStationByClientType==null) stationData.statisticClientsAtStationByClientType=new StatisticsTimePerformanceIndicator[simData.runModel.clientTypes.length];
			indicator=stationData.statisticClientsAtStationByClientType[client.type];
			if (indicator==null) indicator=stationData.statisticClientsAtStationByClientType[client.type]=(StatisticsTimePerformanceIndicator)(cacheClientsAtStationByClientType.get(simData,station,client));
			indicator.set(time,count2);
		}
	}

	/**
	 * Verringert den Zähler für die Kunden an der Bedienstation um eins<br>
	 * (um in der Statistik zu erfassen, wie lange sich wie viele Kunde an der Station befunden haben)
	 * @param simData	Objekt vom Typ <code>SimulationData</code>, welches das Laufzeitmodell (vom Typ <code>RunModel</code> im Feld <code>runModel</code>) und die Statistik (vom Typ <code>Statistics</code> im Feld <code>statistics</code>) enthält und den Zugriff auf die von <code>SimData</code> geerbten Basis-Funktionen ermöglicht
	 * @param station	Station
	 * @param stationData	Optionales Objekt mit den thread-lokalen Daten zu der Station (kann <code>null</code> sein, dann ermittelt es diese Funktion selbst)
	 * @param client	Kunde (für die Erfassung nach Kundentyp)
	 */
	public void logClientLeavesStation(final SimulationData simData, final RunElement station, RunElementData stationData, final RunDataClient client) {
		if (stationData==null) stationData=station.getData(simData);
		final int count1=clientsAtStation(simData,station,stationData,-1); /* Anzahl an Kunden an Station auch in WarmUp-Phase aktualisieren */
		final int count2=clientsAtStationByType(simData,station,stationData,client,-1);
		if (isWarmUp) return;

		if (station.stationStatisticsActive) {
			StatisticsTimePerformanceIndicator indicator;
			final double time=simData.currentTime*scale;

			indicator=stationData.statisticClientsAtStation;
			if (indicator==null) indicator=stationData.statisticClientsAtStation=(StatisticsTimePerformanceIndicator)(cacheClientsAtStation.get(station));
			indicator.set(time,count1);

			if (stationData.statisticClientsAtStationByClientType==null) stationData.statisticClientsAtStationByClientType=new StatisticsTimePerformanceIndicator[simData.runModel.clientTypes.length];
			indicator=stationData.statisticClientsAtStationByClientType[client.type];
			if (indicator==null) indicator=stationData.statisticClientsAtStationByClientType[client.type]=(StatisticsTimePerformanceIndicator)(cacheClientsAtStationByClientType.get(simData,station,client));
			indicator.set(time,count2);
		}
	}

	/**
	 * Erfasst eine Veränderung der Anzahl an Kunden im System bezogen auf einen bestimmten Kundentyp
	 * @param simData	Simulationsdatenobjekt
	 * @param clientType	Kundentyp dessen Anzahl im System sich verändert hat
	 * @param delta	Relative Veränderung (üblicherweise also 1 oder -1)
	 */
	public void logClientsInSystemChange(final SimulationData simData, final int clientType, final int delta) {
		if (clientsInSystemByType==null) clientsInSystemByType=new int[simData.runModel.clientTypes.length];

		clientsInSystemByType[clientType]=FastMath.max(0,clientsInSystemByType[clientType]+delta);

		if (!isWarmUp) {
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)cacheClientsInSystemByType.get(clientType);
			indicator.set(simData.currentTime*scale,clientsInSystemByType[clientType]);
		}
	}

	/**
	 * Initialisiert explizit die Caches für eine Station.<br>
	 * Bei der normalen Simulation muss diese Funktion nicht aufgerufen werden.
	 * Diese Funktion ist nur notwendig, wenn nach Simulationsende wieder ein RunData-Objekt erstellt
	 * wurde, diesem die bereits vorliegenden Statistikdaten übergeben wurden und diese nun (ohne dass je ein Ereignis ausgeführt wurde)
	 * über die Stationsobjekte abgefragt werden sollen.
	 * @param simData	Simulationsdatenobjekt
	 * @param station	Station für die die Statistik-Cache-Felder initialisiert werden sollen
	 */
	public void explicitInitStatistics(final SimulationData simData, final RunElement station) {
		final RunElementData stationData=station.getData(simData);
		if (stationData.statisticClientsAtStationQueue==null) {
			stationData.statisticClientsAtStationQueue=(StatisticsTimePerformanceIndicator)(cacheClientsAtStationQueueByStation.get(station));
		}

		if (stationData.statisticWaiting==null) stationData.statisticWaiting=(StatisticsDataPerformanceIndicator)(cacheStationsWaitingTimes.get(station));
		if (stationData.statisticTransfer==null) stationData.statisticTransfer=(StatisticsDataPerformanceIndicator)(cacheStationsTransferTimes.get(station));
		if (stationData.statisticProcess==null) stationData.statisticProcess=(StatisticsDataPerformanceIndicator)(cacheStationsProcessingTimes.get(station));
		if (stationData.statisticResidence==null) stationData.statisticResidence=(StatisticsDataPerformanceIndicator)(cacheStationsResidenceTimes.get(station));
		if (stationData.statisticClientsAtStation==null) stationData.statisticClientsAtStation=(StatisticsTimePerformanceIndicator)(cacheClientsAtStation.get(station));
		if (stationData.statisticClientsAtStationQueue==null) stationData.statisticClientsAtStationQueue=(StatisticsTimePerformanceIndicator)(cacheClientsAtStationQueueByStation.get(station));
		if (stationData.statisticStationsInterarrivalTime==null) stationData.statisticStationsInterarrivalTime=(StatisticsDataPerformanceIndicator)(cacheStationsInterarrivalTime.get(station));
		if (stationData.statisticStationsInterarrivalTimeBatch==null) stationData.statisticStationsInterarrivalTimeBatch=(StatisticsDataPerformanceIndicator)(cacheStationsInterarrivalTimeBatch.get(station));
		if (stationData.statisticStationsInterleaveTime==null) stationData.statisticStationsInterleaveTime=(StatisticsDataPerformanceIndicator)(cacheStationsInterleavingTime.get(station));
		if (stationData.statisticSourceStationsInterarrivalTime==null) stationData.statisticSourceStationsInterarrivalTime=(StatisticsDataPerformanceIndicator)(cacheClientsInterarrivalTime.get(station));

		if (stationData.statisticClientsAtStationByClientType==null) {
			final int clientTypeCount=simData.runModel.clientTypes.length;
			stationData.statisticClientsAtStationByClientType=new StatisticsTimePerformanceIndicator[clientTypeCount];
			stationData.statisticClientsAtStationByClientTypeValue=new int[clientTypeCount];
			stationData.statisticClientsAtStationQueueByClientType=new StatisticsTimePerformanceIndicator[clientTypeCount];
			stationData.statisticClientsAtStationQueueByClientTypeValue=new int[clientTypeCount];
			for (int i=0;i<clientTypeCount;i++) {
				stationData.statisticClientsAtStationByClientType[i]=(StatisticsTimePerformanceIndicator)(cacheClientsAtStationByClientType.get(simData,station,i));
				stationData.statisticClientsAtStationQueueByClientType[i]=(StatisticsTimePerformanceIndicator)(cacheClientsAtStationByClientType.get(simData,station,i));
			}
		}
	}

	/**
	 * Erhöht den Zähler für die Kunden in der Warteschlange der Bedienstation um eins<br>
	 * (um in der Statistik zu erfassen, wie lange sich wie viele Kunde in der Warteschlange der Station befunden haben)
	 * @param simData	Objekt vom Typ <code>SimulationData</code>, welches das Laufzeitmodell (vom Typ <code>RunModel</code> im Feld <code>runModel</code>) und die Statistik (vom Typ <code>Statistics</code> im Feld <code>statistics</code>) enthält und den Zugriff auf die von <code>SimData</code> geerbten Basis-Funktionen ermöglicht
	 * @param station	Station
	 * @param stationData	Optionales Objekt mit den thread-lokalen Daten zu der Station (kann <code>null</code> sein, dann ermittelt es diese Funktion selbst)
	 * @param client	Aktueller Kunde
	 */
	public void logClientEntersStationQueue(final SimulationData simData, final RunElement station, RunElementData stationData, final RunDataClient client) {
		if (stationData==null) stationData=station.getData(simData);
		final int count1=clientsAtStationQueue(simData,station,stationData,1); /* Anzahl an Kunden an Station auch in WarmUp-Phase aktualisieren */
		final int count2=clientsAtStationQueue(simData,client,1);
		final int count3=clientsAtStationQueueByType(simData,station,stationData,client,1);
		waitingClients++;

		if (isWarmUp) return;

		StatisticsTimePerformanceIndicator indicator;
		final double time=simData.currentTime*scale;

		if (station.stationStatisticsActive) {
			/* Zählung pro Station */
			indicator=stationData.statisticClientsAtStationQueue;
			if (indicator==null) indicator=stationData.statisticClientsAtStationQueue=(StatisticsTimePerformanceIndicator)(cacheClientsAtStationQueueByStation.get(station));
			indicator.set(time,FastMath.min(count1,100_000));

			/* Zählung pro Station und Kundentyp */
			if (stationData.statisticClientsAtStationQueueByClientType==null) stationData.statisticClientsAtStationQueueByClientType=new StatisticsTimePerformanceIndicator[simData.runModel.clientTypes.length];
			indicator=stationData.statisticClientsAtStationQueueByClientType[client.type];
			if (indicator==null) indicator=stationData.statisticClientsAtStationQueueByClientType[client.type]=(StatisticsTimePerformanceIndicator)(cacheClientsAtStationQueueByStationByClientType.get(simData,station,client));
			indicator.set(time,FastMath.min(count3,100_000));
		}

		/* Zählung pro Kundentyp */
		indicator=(StatisticsTimePerformanceIndicator)(cacheClientsAtStationQueueByClient.get(client));
		indicator.set(time,FastMath.min(count2,100_000));

		/* Zählung für System */
		simData.statistics.clientsInSystemQueues.set(time,waitingClients);
	}

	/**
	 * Verringert den Zähler für die Kunden in der Warteschlange der Bedienstation um eins<br>
	 * (um in der Statistik zu erfassen, wie lange sich wie viele Kunde in der Warteschlange der Station befunden haben)
	 * @param simData	Objekt vom Typ <code>SimulationData</code>, welches das Laufzeitmodell (vom Typ <code>RunModel</code> im Feld <code>runModel</code>) und die Statistik (vom Typ <code>Statistics</code> im Feld <code>statistics</code>) enthält und den Zugriff auf die von <code>SimData</code> geerbten Basis-Funktionen ermöglicht
	 * @param station	Station
	 * @param stationData	Optionales Objekt mit den thread-lokalen Daten zu der Station (kann <code>null</code> sein, dann ermittelt es diese Funktion selbst)
	 * @param client	Aktueller Kunde
	 */
	public void logClientLeavesStationQueue(final SimulationData simData, final RunElement station, RunElementData stationData, final RunDataClient client) {
		if (stationData==null) stationData=station.getData(simData);
		final int count1=clientsAtStationQueue(simData,station,stationData,-1); /* Anzahl an Kunden an Station auch in WarmUp-Phase aktualisieren */
		final int count2=clientsAtStationQueue(simData,client,-1);
		final int count3=clientsAtStationQueueByType(simData,station,stationData,client,-1);
		waitingClients--;

		if (isWarmUp) return;

		StatisticsTimePerformanceIndicator indicator;
		final double time=simData.currentTime*scale;

		if (station.stationStatisticsActive) {
			/* Zählung pro Station */
			indicator=stationData.statisticClientsAtStationQueue;
			if (indicator==null) indicator=stationData.statisticClientsAtStationQueue=(StatisticsTimePerformanceIndicator)(cacheClientsAtStationQueueByStation.get(station));
			indicator.set(time,FastMath.min(count1,100_000));

			/* Zählung pro Station und Kundentyp */
			if (stationData.statisticClientsAtStationQueueByClientType==null) stationData.statisticClientsAtStationQueueByClientType=new StatisticsTimePerformanceIndicator[simData.runModel.clientTypes.length];
			indicator=stationData.statisticClientsAtStationQueueByClientType[client.type];
			if (indicator==null) indicator=stationData.statisticClientsAtStationQueueByClientType[client.type]=(StatisticsTimePerformanceIndicator)(cacheClientsAtStationQueueByStationByClientType.get(simData,station,client));
			indicator.set(time,FastMath.min(count3,100_000));
		}

		/* Zählung pro Kundentyp */
		indicator=(StatisticsTimePerformanceIndicator)(cacheClientsAtStationQueueByClient.get(client));
		indicator.set(time,FastMath.min(count2,100_000));

		/* Zählung für System */
		simData.statistics.clientsInSystemQueues.set(time,waitingClients);
	}

	/**
	 * Erfasst die Kosten, die bedingt durch die Arbeiten an einer Station angefallen sind
	 * @param simData	Objekt vom Typ <code>SimulationData</code>, welches das Laufzeitmodell (vom Typ <code>RunModel</code> im Feld <code>runModel</code>) und die Statistik (vom Typ <code>Statistics</code> im Feld <code>statistics</code>) enthält und den Zugriff auf die von <code>SimData</code> geerbten Basis-Funktionen ermöglicht
	 * @param station	Station
	 * @param costs	Entstandene Kosten
	 */
	public void logStationCosts(final SimulationData simData, final RunElement station, double costs) {
		if (costs==0.0) return;
		if (isWarmUp) return;
		((StatisticsValuePerformanceIndicator)(cacheStationCosts.get(station))).add(costs);
	}

	/**
	 * Ermöglicht einen schnelleren Zugriff auf die einzelnen Teil-Indikatoren
	 * innerhalb eines {@link StatisticsMultiPerformanceIndicator}-Objektes,
	 * wenn sich diese auf Stationen (die schneller über ihre IDs identifiziert
	 * werden können) beziehen.
	 * @author Alexander Herzog
	 * @see StatisticsMultiPerformanceIndicator
	 * @see RunElement
	 */
	private class IndicatorAccessCacheStations {
		/** Statistik-Objekt, bei dem der Zugriff auf die Teil-Indikatoren beschleunigt werden soll */
		private final StatisticsMultiPerformanceIndicator multi;
		/** Statistik-Teil-Indikator für die Stationen */
		private StatisticsPerformanceIndicator[] indicators;

		/**
		 * Konstruktor der Klasse
		 * @param multi	Statistik-Objekt, bei dem der Zugriff auf die Teil-Indikatoren beschleunigt werden soll
		 */
		public IndicatorAccessCacheStations(final StatisticsMultiPerformanceIndicator multi) {
			this.multi=multi;
		}

		/**
		 * Liefert ein Statistikobjekt für eine konkrete Station
		 * @param station	Station
		 * @return	Statistikobjekt
		 */
		public StatisticsPerformanceIndicator get(final RunElement station) {
			final int id=station.id;
			if (indicators!=null && indicators.length>id && indicators[id]!=null) return indicators[id];

			final StatisticsPerformanceIndicator indicator=multi.get(station.name);
			if (indicator instanceof StatisticsTimePerformanceIndicator) {
				((StatisticsTimePerformanceIndicator)indicator).setTime(warmUpEndTime);
			}

			if (indicators==null) indicators=new StatisticsPerformanceIndicator[(id+1)*2];
			if (indicators.length<=id) indicators=Arrays.copyOf(indicators,(id+1)*2);
			indicators[id]=indicator;
			return indicator;
		}
	}

	/**
	 * Ermöglicht einen schnelleren Zugriff auf die einzelnen Teil-Indikatoren
	 * innerhalb eines {@link StatisticsMultiPerformanceIndicator}-Objektes,
	 * wenn sich diese auf Stationen (die schneller über ihre IDs identifiziert
	 * werden können) beziehen.
	 * @author Alexander Herzog
	 * @see StatisticsMultiPerformanceIndicator
	 * @see RunElement
	 */
	private class IndicatorAccessCacheStationsNumber {
		/** Statistik-Objekt, bei dem der Zugriff auf die Teil-Indikatoren beschleunigt werden soll */
		private final StatisticsMultiPerformanceIndicator multi;
		/** Teilbezeichner zwischen Stationsname und Kundentypnummer im Statistikobjektnamen (z.B. "NQ") */
		private final String name;
		/** Statistik-Teil-Indikator für die Stationen und einen Status darin */
		private ArrayList<StatisticsPerformanceIndicator>[] indicators;

		/**
		 * Konstruktor der Klasse
		 * @param multi	Statistik-Objekt, bei dem der Zugriff auf die Teil-Indikatoren beschleunigt werden soll
		 * @param name	Teilbezeichner zwischen Stationsname und Status im Statistikobjektnamen (z.B. "NQ")
		 */
		public IndicatorAccessCacheStationsNumber(final StatisticsMultiPerformanceIndicator multi, final String name) {
			this.multi=multi;
			this.name=name;
		}

		/**
		 * Liefert ein Statistikobjekt für eine konkrete Station und einen konkreten Status
		 * @param station	Station
		 * @param nr	Status (z.B. Anzahl an wartenden Kunden)
		 * @return	Statistikobjekt
		 */
		@SuppressWarnings("unchecked")
		public StatisticsPerformanceIndicator get(final RunElement station, final int nr) {
			final int id=station.id;
			if (indicators==null) indicators=new ArrayList[(id+1)*2];
			if (indicators.length<=id) indicators=Arrays.copyOf(indicators,(id+1)*2);
			if (indicators[id]==null) indicators[id]=new ArrayList<>();

			final ArrayList<StatisticsPerformanceIndicator> list=indicators[id];
			while (list.size()<=nr) list.add(null);

			StatisticsPerformanceIndicator indicator=list.get(nr);
			if (indicator==null) {
				indicator=multi.get(station.name+" - "+name+"="+nr);
				list.set(nr,indicator);
				if (indicator instanceof StatisticsTimePerformanceIndicator) {
					((StatisticsTimePerformanceIndicator)indicator).setTime(warmUpEndTime);
				}
			}
			return indicator;
		}
	}

	/**
	 * Ermöglicht einen schnelleren Zugriff auf die einzelnen Teil-Indikatoren
	 * innerhalb eines {@link StatisticsMultiPerformanceIndicator}-Objektes,
	 * wenn sich diese auf Stationen und darin auf Kundentypen (die jeweils
	 * schneller über ihre IDs identifiziert werden können) beziehen.
	 * @author Alexander Herzog
	 * @see StatisticsMultiPerformanceIndicator
	 * @see RunElement
	 * @see RunDataClient
	 */
	private class IndicatorAccessCacheStationsClientTypes {
		/** Statistik-Objekt, bei dem der Zugriff auf die Teil-Indikatoren beschleunigt werden soll */
		private final StatisticsMultiPerformanceIndicator multi;
		/** Statistik-Teil-Indikator für die Stationen und die Kundentypen darin */
		private StatisticsPerformanceIndicator[][] indicators;

		/**
		 * Konstruktor der Klasse
		 * @param multi	Statistik-Objekt, bei dem der Zugriff auf die Teil-Indikatoren beschleunigt werden soll
		 */
		public IndicatorAccessCacheStationsClientTypes(final StatisticsMultiPerformanceIndicator multi) {
			this.multi=multi;
		}

		/**
		 * Liefert ein Statistikobjekt für eine konkrete Station und einen konkreten Kundentyp darin
		 * @param simData	Simulationsdatenobjekt
		 * @param station	Station
		 * @param client	Kundentyp
		 * @return	Statistikobjekt
		 */
		public StatisticsPerformanceIndicator get(final SimulationData simData, final RunElement station, final RunDataClient client) {
			final int id1=station.id;
			final int id2=client.type;

			if (indicators!=null && indicators.length>id1 && indicators[id1]!=null && indicators[id1].length>id2 && indicators[id1][id2]!=null) {
				return indicators[id1][id2];
			}

			final StatisticsPerformanceIndicator indicator=multi.get(station.name+" - "+simData.runModel.clientTypes[id2]);
			if (indicator instanceof StatisticsTimePerformanceIndicator) {
				((StatisticsTimePerformanceIndicator)indicator).setTime(warmUpEndTime);
			}

			if (indicators==null) indicators=new StatisticsPerformanceIndicator[(id1+1)*2][];
			if (indicators.length<=id1) indicators=Arrays.copyOf(indicators,(id1+1)*2);
			if (indicators[id1]==null) indicators[id1]=new StatisticsPerformanceIndicator[(id2+1)*2];
			if (indicators[id1].length<=id2) indicators[id1]=Arrays.copyOf(indicators[id1],(id2+1)*2);
			indicators[id1][id2]=indicator;

			return indicator;
		}

		/**
		 * Liefert ein Statistikobjekt für eine konkrete Station und einen konkreten Kundentyp darin
		 * @param simData	Simulationsdatenobjekt
		 * @param station	Station
		 * @param clientID	ID des Kundentyps
		 * @return	Statistikobjekt
		 */
		public StatisticsPerformanceIndicator get(final SimulationData simData, final RunElement station, final int clientID) {
			final int id1=station.id;
			final int id2=clientID;

			if (indicators!=null && indicators.length>id1 && indicators[id1]!=null && indicators[id1].length>id2 && indicators[id1][id2]!=null) {
				return indicators[id1][id2];
			}

			final StatisticsPerformanceIndicator indicator=multi.get(station.name+" - "+simData.runModel.clientTypes[id2]);
			if (indicator instanceof StatisticsTimePerformanceIndicator) {
				((StatisticsTimePerformanceIndicator)indicator).setTime(warmUpEndTime);
			}

			if (indicators==null) indicators=new StatisticsPerformanceIndicator[(id1+1)*2][];
			if (indicators.length<=id1) indicators=Arrays.copyOf(indicators,(id1+1)*2);
			if (indicators[id1]==null) indicators[id1]=new StatisticsPerformanceIndicator[(id2+1)*2];
			if (indicators[id1].length<=id2) indicators[id1]=Arrays.copyOf(indicators[id1],(id2+1)*2);
			indicators[id1][id2]=indicator;

			return indicator;
		}
	}

	/**
	 * Ermöglicht einen schnelleren Zugriff auf die einzelnen Teil-Indikatoren
	 * innerhalb eines {@link StatisticsMultiPerformanceIndicator}-Objektes,
	 * wenn sich diese auf Kundentypen (die schneller über ihre IDs identifiziert
	 * werden können) beziehen.
	 * @author Alexander Herzog
	 * @see StatisticsMultiPerformanceIndicator
	 * @see RunDataClient
	 */
	public static class IndicatorAccessCacheClientTypes {
		/** Statistik-Objekt, bei dem der Zugriff auf die Teil-Indikatoren beschleunigt werden soll */
		private final StatisticsMultiPerformanceIndicator multi;
		/** Liste mit allen Kundentypnamen ({@link RunModel#clientTypes}) */
		private final String[] clientTypes;
		/** Statistik-Teil-Indikator für die Kundentypen */
		private final StatisticsPerformanceIndicator[] indicators;

		/**
		 * Konstruktor der Klasse
		 * @param multi	Statistik-Objekt, bei dem der Zugriff auf die Teil-Indikatoren beschleunigt werden soll
		 * @param clientTypes	Liste mit allen Kundentypnamen ({@link RunModel#clientTypes})
		 */
		public IndicatorAccessCacheClientTypes(final StatisticsMultiPerformanceIndicator multi, final String[] clientTypes) {
			this.multi=multi;
			this.clientTypes=clientTypes;
			indicators=new StatisticsPerformanceIndicator[clientTypes.length];
		}

		/**
		 * Liefert den Teil-Indikator für einen Kunden
		 * @param client	Kunde
		 * @return	Statistik-Teil-Indikator
		 */
		public StatisticsPerformanceIndicator get(final RunDataClient client) {
			final int clientType=client.type;
			StatisticsPerformanceIndicator indicator=indicators[clientType];
			if (indicator==null) indicator=indicators[clientType]=multi.get(clientTypes[clientType]);
			return indicator;
		}

		/**
		 * Liefert den Teil-Indikator für eine Kunden-ID
		 * @param clientType	Kunden-ID
		 * @return	Statistik-Teil-Indikator
		 * @see RunModel#clientTypes
		 * @see RunDataClient#type
		 */
		public StatisticsPerformanceIndicator get(final int clientType) {
			StatisticsPerformanceIndicator indicator=indicators[clientType];
			if (indicator==null) indicator=indicators[clientType]=multi.get(clientTypes[clientType]);
			return indicator;
		}
	}

	/**
	 * Ändert den Wert von {@link RunElementData#clientsAtStation} für eine Station.
	 * @param simData	Simulationsdatenobjekt
	 * @param station	Station an der der Wert verändert werden soll (muss nur dann einen Wert ungleich <code>null</code> besitzen, wenn <code>stationData</code> keinen Wert ungleich <code>null</code> besitzt)
	 * @param stationData	Station an der der Wert verändert werden soll (schneller als über <code>station</code>; ist dieser Parameter <code>null</code>, so muss ein <code>station</code> ein Wert übergeben werden)
	 * @param change	Veränderung der Anzahl
	 * @return	Neue Anzahl
	 */
	public int clientsAtStation(final SimulationData simData, final RunElement station, final RunElementData stationData, final int change) {
		final RunElementData data=(stationData==null)?station.getData(simData):stationData;
		return data.clientsAtStation=FastMath.max(0,data.clientsAtStation+change);
		/* So würden alle Ein- und Ausgänge eines Bereiches zusammengefasst werden: return data.reportedClientsAtStation(simData); */
	}

	/**
	 * Ändert den Wert von {@link RunElementData#statisticClientsAtStationByClientTypeValue} für eine Station.
	 * @param simData	Simulationsdatenobjekt
	 * @param station	Station an der der Wert verändert werden soll (muss nur dann einen Wert ungleich <code>null</code> besitzen, wenn <code>stationData</code> keinen Wert ungleich <code>null</code> besitzt)
	 * @param stationData	Station an der der Wert verändert werden soll (schneller als über <code>station</code>; ist dieser Parameter <code>null</code>, so muss ein <code>station</code> ein Wert übergeben werden)
	 * @param client	Kunde von dem der Typ ausgelesen wird
	 * @param change	Veränderung der Anzahl
	 * @return	Neue Anzahl
	 */
	public int clientsAtStationByType(final SimulationData simData, final RunElement station, final RunElementData stationData, final RunDataClient client, final int change) {
		final RunElementData data=(stationData==null)?station.getData(simData):stationData;
		if (data.statisticClientsAtStationByClientTypeValue==null) data.statisticClientsAtStationByClientTypeValue=new int[simData.runModel.clientTypes.length];
		return data.statisticClientsAtStationByClientTypeValue[client.type]=FastMath.max(0,data.statisticClientsAtStationByClientTypeValue[client.type]+change);
	}

	/**
	 * Erfasst eine Änderung der Anzahl an einer Stationswarteschlange
	 * @param simData	Simulationsdatenobjekt
	 * @param station	Station an der der Wert verändert werden soll (muss nur dann einen Wert ungleich <code>null</code> besitzen, wenn <code>stationData</code> keinen Wert ungleich <code>null</code> besitzt)
	 * @param stationData	Station an der der Wert verändert werden soll (schneller als über <code>station</code>; ist dieser Parameter <code>null</code>, so muss ein <code>station</code> ein Wert übergeben werden)
	 * @param change	Veränderung der Anzahl
	 * @return	Neue Anzahl
	 */
	private int clientsAtStationQueue(final SimulationData simData, final RunElement station, final RunElementData stationData, final int change) {
		final RunElementData data=(stationData==null)?station.getData(simData):stationData;
		return data.clientsAtStationQueue=FastMath.max(0,data.clientsAtStationQueue+change);
	}

	/**
	 * Ändert den Wert von {@link RunElementData#statisticClientsAtStationQueueByClientTypeValue} für eine Station.
	 * @param simData	Simulationsdatenobjekt
	 * @param station	Station an der der Wert verändert werden soll (muss nur dann einen Wert ungleich <code>null</code> besitzen, wenn <code>stationData</code> keinen Wert ungleich <code>null</code> besitzt)
	 * @param stationData	Station an der der Wert verändert werden soll (schneller als über <code>station</code>; ist dieser Parameter <code>null</code>, so muss ein <code>station</code> ein Wert übergeben werden)
	 * @param client	Kunde von dem der Typ ausgelesen wird
	 * @param change	Veränderung der Anzahl
	 * @return	Neue Anzahl
	 */
	private int clientsAtStationQueueByType(final SimulationData simData, final RunElement station, final RunElementData stationData, final RunDataClient client, final int change) {
		final RunElementData data=(stationData==null)?station.getData(simData):stationData;
		if (data.statisticClientsAtStationQueueByClientTypeValue==null) data.statisticClientsAtStationQueueByClientTypeValue=new int[simData.runModel.clientTypes.length];
		return data.statisticClientsAtStationQueueByClientTypeValue[client.type]=FastMath.max(0,data.statisticClientsAtStationQueueByClientTypeValue[client.type]+change);
	}

	/**
	 * Erfasst eine Änderung der Anzahl an wartenden Kunden pro Kundentyp
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde von dem der Typ ausgelesen wird
	 * @param change	Veränderung der Anzahl
	 * @return	Neue Anzahl
	 */
	private int clientsAtStationQueue(final SimulationData simData, final RunDataClient client, final int change) {
		final int clientType=client.type;

		if (clientsAtStationQueueByType==null) clientsAtStationQueueByType=new int[simData.runModel.clientTypes.length];

		clientsAtStationQueueByType[clientType]=FastMath.max(0,clientsAtStationQueueByType[clientType]+change);
		return clientsAtStationQueueByType[clientType];
	}

	/**
	 * Liefert das zu einer Station (<code>RunElement</code>-Objekt) gehörige Datenobjekt<br>
	 * Existiert zu einer Station noch kein Datenobjekt, so liefert diese Funktion <code>null</code>,
	 * da sie selbst keine Datenobjekte anlegen kann. Daher soll bevorzugt die Funktion <code>RunElement.getData()</code>
	 * verwendet werden, die in diesem Fall ein neues Datenobjekt erstellen kann und dann dieses zurück liefert.
	 * @param station	Station, zu der das Datenobjekt geliefert werden soll
	 * @return	Liefert das Datenobjekt oder <code>null</code>, wenn noch kein Datenobjekt hinterlegt wurde.
	 */
	public RunElementData getStationData(final RunElement station) {
		if (elementData==null) return null;
		final int id=station.id;
		if (id<0 || elementData.length<=id) return null;
		return elementData[id];
	}

	/**
	 * Speichert ein Datenobjekt zu einer Station (d.h. zu einem <code>RunElement</code>-Objekt)
	 * @param station	Station, zu der das Datenobjekt gespeichert werden soll
	 * @param data	Zu speicherndes Datenobjekt
	 */
	public void setStationData(final RunElement station, final RunElementData data) {
		final int id=station.id;
		if (elementData==null) elementData=new RunElementData[id+1];
		if (elementData.length<=id) elementData=Arrays.copyOf(elementData,id+1);
		elementData[id]=data;
	}

	/**
	 * Listener die über Änderungen in Bezug auf verfügbare Bediener benachrichtigt werden sollen
	 * @see #fireReleasedResourcesNotify(SimulationData)
	 */
	private FreeResourcesListener[] freeResourcesListener;

	/**
	 * Rechenausdrücke zur Bestimmung der Aufrufreihenfolgen von {@link #freeResourcesListenerPriorityConst}
	 * @see #freeResourcesListener
	 * @see #fireReleasedResourcesNotify(SimulationData)
	 */
	private ExpressionCalc[] freeResourcesListenerPriority;

	/**
	 * Liefert einer oder liefern mehrere der Rechenausdrücke in {@link #freeResourcesListenerPriority} konstante Werte?
	 * @see #freeResourcesListenerPriority
	 * @see #fireReleasedResourcesNotify(SimulationData)
	 */
	private Double[] freeResourcesListenerPriorityConst;

	/**
	 * Cache des Arrays zur Bestimmung der aktuellen Werte von {@link #freeResourcesListenerPriority}
	 * @see #freeResourcesListenerPriority
	 * @see #fireReleasedResourcesNotify(SimulationData)
	 */
	private double[] globalFreeResourcesListenerCurrentPriority;

	/**
	 * Globale Liste zur Erfassung der Indices der zu benachrichtigen Listener für freie Bediener
	 * @see #fireReleasedResourcesNotify(SimulationData)
	 * @see #fireReleaseTransporterNotify(RunDataTransporter, SimulationData)
	 */
	private List<Integer> globalFreeResourcesMaxIndex;

	/**
	 * Steht die Liste {@link #globalFreeResourcesMaxIndex} momentan
	 * zur Verfügung?
	 * @see #globalFreeResourcesMaxIndex
	 * @see #fireReleasedResourcesNotify(SimulationData)
	 * @see #fireReleaseTransporterNotify(RunDataTransporter, SimulationData)
	 */
	private boolean canUseGlobalFreeResourcesListenerCurrentPriority=true;

	/**
	 * Wird {@link #secondaryPriority} gerade von einem anderen
	 * Aufruf verwendet oder darf ein Aufruf von {@link #getSecondaryPriorityIndex(SimulationData, List)}
	 * die globale Liste verwenden?
	 * @see #secondaryPriority
	 * @see #getSecondaryPriorityIndex(SimulationData, List)
	 */
	private boolean canUseGlobalSecondaryPriorityList=true;

	/**
	 * Globales Listenobjekt zur Bestimmung der zweiten Prioritäten.
	 * @see #getSecondaryPriorityIndex(SimulationData, List)
	 */
	private List<Integer> secondaryPriority;

	/**
	 * Ermittlung des zu erst zu benachrichtigenden Listeners bei Prioritätsgleichstand in der ersten Ebene
	 * (es werden dann z.B. die Prioritäten der wartenden Kunden ausgewertet).
	 * @param simData	Simulationsdatenobjekt
	 * @param indexList	Indizes der zu betrachtenden Listener
	 * @return	Zu benachrichtigender Listener
	 */
	private int getSecondaryPriorityIndex(final SimulationData simData, final List<Integer> indexList) {
		if (resources.secondaryResourcePriority==ModelResources.SecondaryResourcePriority.CLIENT_PRIORITY) {
			/* Auswahl der Station gemäß der Kundenpriorität */
			boolean secondaryPriorityListInUse=false;
			final List<Integer> bestIndex;
			if (canUseGlobalSecondaryPriorityList) {
				canUseGlobalSecondaryPriorityList=false;
				secondaryPriorityListInUse=true;
				if (secondaryPriority==null) secondaryPriority=new ArrayList<>();
				bestIndex=secondaryPriority;
				bestIndex.clear();
			} else {
				bestIndex=new ArrayList<>();
			}

			try {
				double bestPriority=-Double.MAX_VALUE;
				for (int i=0;i<indexList.size();i++) {
					final Integer I=indexList.get(i);
					final double priority=freeResourcesListener[I.intValue()].getSecondaryResourcePriority(simData);
					if (priority>bestPriority) {
						bestIndex.clear();
						bestIndex.add(i);
						bestPriority=priority;
					} else {
						if (priority==bestPriority) bestIndex.add(i);
					}
				}
				if (bestIndex.size()==1) {
					return bestIndex.get(0).intValue();
				} else {
					return bestIndex.get((int)FastMath.round(FastMath.floor(bestIndex.size()*DistributionRandomNumber.nextDouble()))).intValue();
				}
			} finally {
				if (secondaryPriorityListInUse) canUseGlobalSecondaryPriorityList=true;
			}
		}

		/* Zufällige Auswahl der Station */
		return (int)FastMath.round(FastMath.floor(indexList.size()*DistributionRandomNumber.nextDouble()));
	}

	/**
	 * Diese Methode muss aufgerufen werden, wenn Ressourcen freigegeben wurden.
	 * Es werden dann alle Elemente, die sich für diese Tatsache interessieren
	 * (die das {@link FreeResourcesListener}-Interface implementieren),
	 * darüber benachrichtigt.
	 * @param simData	Simulationsdatenobjekt
	 * @see FreeResourcesListener
	 */
	public void fireReleasedResourcesNotify(final SimulationData simData) {
		/* Beim ersten Aufruf: Array mit relevanten Einträgen aufbauen */
		if (freeResourcesListener==null) {
			final List<FreeResourcesListener> elements=new ArrayList<>();
			for (Map.Entry<Integer,RunElement> entry: runModel.elements.entrySet()) if (entry.getValue() instanceof FreeResourcesListener) elements.add((FreeResourcesListener)(entry.getValue()));
			freeResourcesListener=elements.toArray(new FreeResourcesListener[0]);
			freeResourcesListenerPriority=new ExpressionCalc[freeResourcesListener.length];
			freeResourcesListenerPriorityConst=new Double[freeResourcesListener.length];
			for (int i=0;i<freeResourcesListener.length;i++) {
				freeResourcesListenerPriority[i]=freeResourcesListener[i].getResourcePriority(simData);
				if (freeResourcesListenerPriority[i].isConstValue()) try {
					freeResourcesListenerPriorityConst[i]=freeResourcesListenerPriority[i].calc(variableValues,simData,null);
				} catch (MathCalcError e) {}
			}
		}

		/* Wenn Ereignis getriggert durch Pausenzeitende, dann System Möglichkeit geben, nächsten Pausenbeginn gemäß Ausdruck zu berechnen */
		resources.updateStatus(simData);

		/* Formelauswertung */
		double[] freeResourcesListenerCurrentPriority;
		List<Integer> maxIndex;
		if (canUseGlobalFreeResourcesListenerCurrentPriority) {
			if (globalFreeResourcesListenerCurrentPriority==null) globalFreeResourcesListenerCurrentPriority=new double[freeResourcesListener.length];
			freeResourcesListenerCurrentPriority=globalFreeResourcesListenerCurrentPriority;
			if (globalFreeResourcesMaxIndex==null) globalFreeResourcesMaxIndex=new ArrayList<>(freeResourcesListenerPriority.length);
			maxIndex=globalFreeResourcesMaxIndex;
		} else {
			freeResourcesListenerCurrentPriority=new double[freeResourcesListener.length];
			maxIndex=new ArrayList<>(freeResourcesListenerPriority.length);
		}
		canUseGlobalFreeResourcesListenerCurrentPriority=false;
		try {
			simData.runData.setClientVariableValues(null);
			for (int i=0;i<freeResourcesListenerPriority.length;i++) {
				if (freeResourcesListenerPriorityConst[i]!=null) {
					freeResourcesListenerCurrentPriority[i]=freeResourcesListenerPriorityConst[i];
				} else {
					try {
						freeResourcesListenerCurrentPriority[i]=NumberTools.fastBoxedValue(freeResourcesListenerPriority[i].calc(variableValues,simData,null));
					} catch (MathCalcError e) {
						freeResourcesListenerCurrentPriority[i]=0;
					}
				}
			}

			while (true) {
				double maxValue=-Double.MAX_VALUE;
				maxIndex.clear();
				for (int i=0;i<freeResourcesListenerCurrentPriority.length;i++) {
					if (freeResourcesListenerCurrentPriority[i]>maxValue) {
						maxValue=freeResourcesListenerCurrentPriority[i];
						if (maxIndex.size()==1) {
							maxIndex.set(0,i);
						} else {
							maxIndex.clear();
							maxIndex.add(i);
						}
					} else {
						if (freeResourcesListenerCurrentPriority[i]==maxValue && maxValue>-Double.MAX_VALUE) maxIndex.add(i);
					}
				}
				if (maxIndex.isEmpty()) break;
				/* Bei gleicher Priorität werden die Elemente in zufälliger Reihenfolge benachrichtigt. */
				while (!maxIndex.isEmpty()) {
					int select;
					if (maxIndex.size()==1) {
						select=0;
					} else {
						select=getSecondaryPriorityIndex(simData,maxIndex);
					}
					final int index=maxIndex.remove(select);
					freeResourcesListener[index].releasedResourcesNotify(simData);
					freeResourcesListenerCurrentPriority[index]=-Double.MAX_VALUE;
				}
			}
		} finally {
			if (freeResourcesListenerCurrentPriority==globalFreeResourcesListenerCurrentPriority) canUseGlobalFreeResourcesListenerCurrentPriority=true;
		}
	}

	/**
	 * Listener die über verfügbare gewordene Transporter benachrichtigt werden sollen
	 * @see #fireReleaseTransporterNotify(RunDataTransporter, SimulationData)
	 */
	private TransporterPosition[] freeTransporterListener;

	/**
	 * Prioritäten der Listener in {@link #freeTransporterListener}
	 * @see #freeTransporterListener
	 * @see #fireReleaseTransporterNotify(RunDataTransporter, SimulationData)
	 */
	private Double[] freeTransporterListenerPriority;

	/**
	 * Diese Methode muss aufgerufen werden, wenn Transporter freigegeben wurden.
	 * Es werden dann alle Elemente, die sich für diese Tatsache interessieren
	 * (die das {@link TransporterPosition}-Interface implementieren),
	 * darüber benachrichtigt.
	 * @param transporter	Freigegebener Transporter
	 * @param simData	Simulationsdatenobjekt
	 * @see TransporterPosition
	 */
	public void fireReleaseTransporterNotify(final RunDataTransporter transporter, final SimulationData simData) {

		/* Beim ersten Aufruf: Array mit relevanten Einträgen aufbauen */
		if (freeTransporterListener==null) {
			final List<TransporterPosition> elements=new ArrayList<>();
			for (Map.Entry<Integer,RunElement> entry: runModel.elements.entrySet()) if (entry.getValue() instanceof TransporterPosition) elements.add((TransporterPosition)(entry.getValue()));
			freeTransporterListener=elements.toArray(new TransporterPosition[0]);
			freeTransporterListenerPriority=new Double[freeTransporterListener.length];
		}

		/* Mit welcher Priorität hält die aktuelle Station den Transporter fest? */
		double stayHerePriority=-Double.MAX_VALUE;
		TransporterPosition currentPosition=null;
		final RunElement element=(transporter.position>=0)?simData.runModel.elementsFast[transporter.position]:null;
		if (element instanceof TransporterPosition) {
			currentPosition=(TransporterPosition)element;
			final Double D=currentPosition.stayHerePriority(transporter,simData);
			if (D!=null) stayHerePriority=D.doubleValue();
		}

		/* Aktuelle Prioritäten bestimmen */
		boolean transporterNeeded=false;
		for (int i=0;i<freeTransporterListener.length;i++) {
			if (freeTransporterListener[i]==currentPosition) {
				freeTransporterListenerPriority[i]=null;
				continue;
			}
			freeTransporterListenerPriority[i]=freeTransporterListener[i].requestPriority(transporter,simData);
			if (freeTransporterListenerPriority[i]!=null) {
				if (freeTransporterListenerPriority[i]<=stayHerePriority) freeTransporterListenerPriority[i]=null; else transporterNeeded=true;
			}
		}
		if (!transporterNeeded) return;

		List<Integer> maxIndex;
		if (canUseGlobalFreeResourcesListenerCurrentPriority) {
			if (globalFreeResourcesMaxIndex==null) globalFreeResourcesMaxIndex=new ArrayList<>();
			maxIndex=globalFreeResourcesMaxIndex;
		} else {
			maxIndex=new ArrayList<>(freeResourcesListenerPriority.length);
		}
		canUseGlobalFreeResourcesListenerCurrentPriority=false;
		try {
			/* Der Reihe nach alle Listener anfragen */
			while (true) {

				double maxPriority=-Double.MAX_VALUE;
				maxIndex.clear();

				for (int i=0;i<freeTransporterListenerPriority.length;i++) {
					final Double D=freeTransporterListenerPriority[i];
					if (D!=null && D.doubleValue()>=maxPriority) {
						maxPriority=D.doubleValue();
						maxIndex.add(i);
					}
				}

				if (maxIndex.isEmpty()) break;
				/* Bei gleicher Priorität werden die Elemente in zufälliger Reihenfolge benachrichtigt. */
				while (!maxIndex.isEmpty()) {
					int select;
					if (maxIndex.size()==1) {
						select=0;
					} else {
						select=(int)FastMath.round(FastMath.floor(maxIndex.size()*DistributionRandomNumber.nextDouble()));
					}
					final int index=maxIndex.remove(select);

					freeTransporterListener[index].transporterFree(transporter,simData);
					if (transporter.inTransfer) return; /* Transporter wurde vermittelt, restliche Bearbeitung abbrechen. */
					freeTransporterListenerPriority[index]=null;
				}
			}

		} finally {
			if (maxIndex==globalFreeResourcesMaxIndex) canUseGlobalFreeResourcesListenerCurrentPriority=true;
		}
	}

	/**
	 * Listener die benachrichtigt werden sollen, wenn sich der Systemzustand ändert.
	 * @see #fireStateChangeNotify(SimulationData)
	 */
	private StateChangeListener[] stateChangeListener;

	/**
	 * Aus {@link #stateChangeListener} abzumeldender Listener.
	 * @see #fireStateChangeNotify(SimulationData)
	 * @see #removeStateChangeListener(StateChangeListener)
	 */
	private StateChangeListener stateChangeListenerRemove=null;

	/**
	 * Zeitpunkt der letzten Zustandsänderung
	 * @see #fireStateChangeNotify(SimulationData)
	 */
	private long lastStateChange=0;

	/**
	 * Zeitpunkt der letzten Auslösung der Listener
	 * @see #fireStateChangeNotify(SimulationData)
	 */
	private long lastTriggered=0;

	/**
	 * Diese Methode muss aufgerufen werden, wenn sich der Systemzustand (z.B. Anzahl an
	 * wartenden Kunden an einer Station) ändert.
	 * Es werden dann alle Elemente, die sich für diese Tatsache interessieren
	 * (die das <code>StateChangeListener</code>-Interface implementieren),
	 * darüber benachrichtigt.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void fireStateChangeNotify(final SimulationData simData) {
		/* Beim ersten Aufruf: Array mit relevanten Einträgen aufbauen */
		if (stateChangeListener==null) {
			final List<StateChangeListener> list=new ArrayList<>();
			for (Map.Entry<Integer,RunElement> element: simData.runModel.elements.entrySet()) {
				if (element.getValue() instanceof StateChangeListener) list.add((StateChangeListener)element.getValue());
			}
			stateChangeListener=list.toArray(new StateChangeListener[0]);
			lastStateChange=-1;
		}

		/* Es gibt nichts zu tun - und das wird sich auch nicht ändern. Daher gar kein retriggern nach einer ms und ähnliches planen. */
		if (stateChangeListener.length==0 && simData.runModel.terminationCondition==null && simData.runModel.terminationTime<0) return;

		/* Nur einmal pro ms ausführen */
		if (lastStateChange>0 && lastStateChange==simData.currentTime) {
			if (lastTriggered!=simData.currentTime+1) {
				lastTriggered=simData.currentTime+1;
				SystemChangeEvent.triggerEvent(simData,1);
			}
			return;
		}
		lastStateChange=simData.currentTime;

		/* Erfassen, ob weitere Aufrufe dieser Methode zu späterem Zeitpunkt notwendig sind. */
		boolean systemChangedByChangedListener=false;

		if (stateChangeListener.length>0) {

			for (StateChangeListener listener: stateChangeListener) {
				if (listener.systemStateChangeNotify(simData)) {
					/* Es wurden Veränderungen (in Form von angelegten neuen Ereignissen) vorgenommen, die nun erstmal regulär abgearbeitet werden müssen. Erst danach (nach einer ms) hier fortsetzen, sonst erfolgen z.B. Freigaben, auf Basis veralteter Systemzustände. */
					systemChangedByChangedListener=true;
					break;
				}
			}

			/* Hat ein Listener während der Benachrichtigungsrunde mitgeteilt, dass er sich ausklinken möchte? */
			if (stateChangeListenerRemove!=null) {
				final List<StateChangeListener> list=new ArrayList<>(Arrays.asList(stateChangeListener));
				list.remove(stateChangeListenerRemove);
				stateChangeListener=list.toArray(new StateChangeListener[0]);
				stateChangeListenerRemove=null;
			}

			/* Müssen wir später noch eine Runde starten? */
			if (systemChangedByChangedListener) {
				if (lastTriggered!=simData.currentTime+1) {
					lastTriggered=simData.currentTime+1;
					SystemChangeEvent.triggerEvent(simData,1);
				}
			}
		}

		/* Simulationsende über Bedingung */
		if (simData.runModel.terminationCondition!=null) {
			simData.runData.setClientVariableValues(null);
			if (simData.runModel.terminationCondition.eval(simData.runData.variableValues,simData,null)) {
				/* Logging */
				if (simData.loggingActive && simData.logInfoSystem) simData.logEventExecution(Color.BLACK,Language.tr("Simulation.Log.EndOfSimulation"),-1,String.format(Language.tr("Simulation.Log.EndOfSimulation.Condition"),simData.runModel.terminationCondition.getCondition()));

				/* Ende */
				simData.doShutDown();
			}
		}

		/* Simulationsende über Zeitpunkt */
		if (simData.runModel.terminationTime>=0) {
			if (simData.currentTime>simData.runModel.terminationTime*1000) {
				/* Logging */
				if (simData.loggingActive && simData.logInfoSystem) simData.logEventExecution(Color.BLACK,Language.tr("Simulation.Log.EndOfSimulation"),-1,Language.tr("Simulation.Log.EndOfSimulation.Time"));

				/* Ende */
				simData.doShutDown();
			}
		}
	}

	/**
	 * Listener die über Bewegungen der Kunden informiert werden sollen
	 * @see #fireClientMoveNotify(SimulationData, RunDataClient, boolean)
	 */
	private ClientMoveListener[] clientMoveListener;

	/**
	 * Listener der nicht mehr über Kundenbewegungen informiert werden möchte
	 * @see #fireClientMoveNotify(SimulationData, RunDataClient, boolean)
	 * @see #removeClientMoveListener(ClientMoveListener)
	 */
	private ClientMoveListener clientMoveListenerRemove;

	/**
	 * Diese Methode muss aufgerufen werden, wenn ein Kunde an eine andere Station verwiesen wird.
	 * Es werden dann alle Elemente, die sich für diese Tatsache interessieren
	 * (die das <code>ClientMoveListener</code>-Interface implementieren),
	 * darüber benachrichtigt.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kundenobjekt, welches an eine andere Station verwiesen wird
	 * @param moveByTransport	Gibt an, ob sich der Kunde entlang der Verbindungskanten (<code>false</code>) oder unsichtbar über einen Transporter (<code>true</code>) bewegt hat
	 */
	public void fireClientMoveNotify(final SimulationData simData, final RunDataClient client, final boolean moveByTransport) {
		recordClientMovement(simData,client);

		if (clientMoveListener==null) {
			final List<ClientMoveListener> list=new ArrayList<>();
			for (Map.Entry<Integer,RunElement> element: simData.runModel.elements.entrySet()) {
				if (element.getValue() instanceof ClientMoveListener) list.add((ClientMoveListener)element.getValue());
			}
			clientMoveListener=list.toArray(new ClientMoveListener[0]);
		}

		if (clientMoveListener.length==0) return; /* Nix zu tun. */

		for (ClientMoveListener listener: clientMoveListener) listener.clientMoveNotify(simData,client,moveByTransport);

		/* Hat ein Listener während der Benachrichtigungsrunde mitgeteilt, dass er sich ausklinken möchte? */
		if (clientMoveListenerRemove!=null) {
			List<ClientMoveListener> list=new ArrayList<>(Arrays.asList(clientMoveListener));
			list.remove(clientMoveListenerRemove);
			clientMoveListener=list.toArray(new ClientMoveListener[0]);
			clientMoveListenerRemove=null;
		}
	}

	/**
	 * Listener die über Bewegungen der Transporter informiert werden sollen
	 * @see #fireTransporterMoveNotify(SimulationData, RunDataTransporter)
	 */
	private TransporterMoveListener[] transporterMoveListener;

	/**
	 * Listener der nicht mehr über Transporterbewegungen informiert werden möchte
	 * @see #fireTransporterMoveNotify(SimulationData, RunDataTransporter)
	 * @see #removeClientMoveListener(TransporterMoveListener)
	 */
	private TransporterMoveListener transporterMoveListenerRemove;

	/**
	 * Diese Methode muss aufgerufen werden, wenn ein Transporter an eine andere Station geschickt wurde.
	 * Es werden dann alle Elemente, die sich für diese Tatsache interessieren
	 * (die das <code>TransporterMoveListener</code>-Interface implementieren),
	 * darüber benachrichtigt.
	 * @param simData	Simulationsdatenobjekt
	 * @param transporter	Transporterobjekt, welches sich bewegt hat
	 */
	public void fireTransporterMoveNotify(final SimulationData simData, final RunDataTransporter transporter) {
		/* Beim ersten Aufruf: Array mit relevanten Einträgen aufbauen */
		if (transporterMoveListener==null) {
			List<TransporterMoveListener> list=new ArrayList<>();
			for (Map.Entry<Integer,RunElement> element: simData.runModel.elements.entrySet()) {
				if (element.getValue() instanceof TransporterMoveListener) list.add((TransporterMoveListener)element.getValue());
			}
			transporterMoveListener=list.toArray(new TransporterMoveListener[0]);
		}

		for (TransporterMoveListener listener: transporterMoveListener) listener.transporterMoveNotify(simData,transporter);

		/* Hat ein Listener während der Benachrichtigungsrunde mitgeteilt, dass er sich ausklinken möchte? */
		if (transporterMoveListenerRemove!=null) {
			List<TransporterMoveListener> list=new ArrayList<>(Arrays.asList(transporterMoveListener));
			list.remove(transporterMoveListenerRemove);
			transporterMoveListener=list.toArray(new TransporterMoveListener[0]);
			transporterMoveListenerRemove=null;
		}
	}

	/**
	 * Listener die über Signalauslösungen benachrichtigt werden möchten
	 * @see #fireSignal(SimulationData, String)
	 */
	private SignalListener[] signalListener;

	/**
	 * Diese Methode muss aufgerufen werden, wenn ein Signal ausgelöst wurde.
	 * Es werden dann alle Elemente, die sich für diese Tatsache interessieren
	 * (die das <code>SignalListener</code>-Interface implementieren),
	 * darüber benachrichtigt.
	 * @param simData	Simulationsdatenobjekt
	 * @param signalName	Name des ausgelösten Signals
	 */
	public void fireSignal(final SimulationData simData, final String signalName) {
		/* Beim ersten Aufruf: Array mit relevanten Einträgen aufbauen */
		if (signalListener==null) {
			List<SignalListener> list=new ArrayList<>();
			for (Map.Entry<Integer,RunElement> element: simData.runModel.elements.entrySet()) {
				if (element.getValue() instanceof SignalListener) list.add((SignalListener)element.getValue());
			}
			signalListener=list.toArray(new SignalListener[0]);
		}

		for (SignalListener listener: signalListener) listener.signalNotify(simData,signalName);
	}

	/**
	 * Liefert ein <code>RunDataOutputWriter</code>-Objekt für die angegebene Datei.<br>
	 * Verwenden mehrere Ausgabe-Objekte dieselbe Datei, so können die Ausgaben trotz Pufferung
	 * so zeitlich korrekt synchronisiert werden.
	 * @param outputFile	Ausgabedatei
	 * @return	<code>RunDataOutputWriter</code>-Objekt
	 */
	public RunDataOutputWriter getOutputWriter(final File outputFile) {
		RunDataOutputWriter writer=outputWriter.get(outputFile);
		if (writer==null) outputWriter.put(outputFile,writer=new RunDataOutputWriter(outputFile));
		return writer;
	}

	/**
	 * Stellt die Variablen "w", "t" und "p" gemäß den Daten eines Kunden ein
	 * @param client	Kunden-Objekt von dem die Zeitdaten ausgelesen und in die Variablen geschrieben werden sollen
	 */
	public void setClientVariableValues(final RunDataClient client) {
		final int len=variableValues.length;
		if (client==null) {
			variableValues[len-3]=0;
			variableValues[len-2]=0;
			variableValues[len-1]=0;
		} else {
			variableValues[len-3]=client.waitingTime*scale;
			variableValues[len-2]=client.transferTime*scale;
			variableValues[len-1]=client.processTime*scale;
		}
	}

	/**
	 * Stellt die Variablen "w", "t" und "p" gemäß den Daten eines Kunden ein
	 * @param client	Kunden-Objekt von dem die Zeitdaten ausgelesen und in die Variablen geschrieben werden sollen
	 * @param additionalWaitingTime	Zusätzliche Wartezeit (in Sekunden), die in "w" eingetragen soll, aber noch nicht im Kundenobjekt erfasst ist
	 */
	public void setClientVariableValues(final RunDataClient client, final double additionalWaitingTime) {
		final int len=variableValues.length;
		if (client==null) {
			variableValues[len-3]=0;
			variableValues[len-2]=0;
			variableValues[len-1]=0;
		} else {
			variableValues[len-3]=client.waitingTime*scale+additionalWaitingTime;
			variableValues[len-2]=client.transferTime*scale;
			variableValues[len-1]=client.processTime*scale;
		}
	}

	/**
	 * Stellt die Variablen "w", "t" und "p" auf bestimmte Werte
	 * @param waitingTime	Wert für die Variable "w"
	 * @param transferTime	Wert für die Variable "t"
	 * @param processTime	Wert für die Variable "p"
	 */
	public void setClientVariableValues(final long waitingTime, final long transferTime, final long processTime) {
		final int len=variableValues.length;
		variableValues[len-3]=waitingTime*scale;
		variableValues[len-2]=transferTime*scale;
		variableValues[len-1]=processTime*scale;
	}

	/**
	 * Informiert das <code>RunData</code>-Objekt, dass ein <code>StateChangeListener</code> keine Nachrichten mehr erhalten möchte.<br>
	 * Dies kann während einer Benachrichtigungsrunde erfolgen. Der Listener wird erst nach Abschluss der Runde aus der Liste entfernt, so dass es keine konkurrierenden Zugriffe gibt.
	 * @param listener	Listener, der nicht mehr benachrichtigt werden möchte.
	 */
	public void removeStateChangeListener(final StateChangeListener listener) {
		stateChangeListenerRemove=listener;
	}

	/**
	 * Informiert das <code>RunData</code>-Objekt, dass ein <code>ClientMoveListener</code> keine Nachrichten mehr erhalten möchte.<br>
	 * Dies kann während einer Benachrichtigungsrunde erfolgen. Der Listener wird erst nach Abschluss der Runde aus der Liste entfernt, so dass es keine konkurrierenden Zugriffe gibt.
	 * @param listener	Listener, der nicht mehr benachrichtigt werden möchte.
	 */
	public void removeClientMoveListener(final ClientMoveListener listener) {
		clientMoveListenerRemove=listener;
	}

	/**
	 * Informiert das <code>RunData</code>-Objekt, dass ein <code>TransporterMoveListener</code> keine Nachrichten mehr erhalten möchte.<br>
	 * Dies kann während einer Benachrichtigungsrunde erfolgen. Der Listener wird erst nach Abschluss der Runde aus der Liste entfernt, so dass es keine konkurrierenden Zugriffe gibt.
	 * @param listener	Listener, der nicht mehr benachrichtigt werden möchte.
	 */
	public void removeClientMoveListener(final TransporterMoveListener listener) {
		transporterMoveListenerRemove=listener;
	}

	/**
	 * Liefert einen String (für Logging), der angibt, ob wir uns noch in der Warm-up-Phase befinden
	 * @return "ja" oder "nein", je nach dem ob die Warm-up-Phase noch läuft
	 */
	public String getWarmUpStatus() {
		return isWarmUp?Language.tr("Dialog.Button.Yes").toLowerCase():Language.tr("Dialog.Button.No").toLowerCase();
	}

	/**
	 * Liste der Stationen, die zeitabhängig benachrichtigt werden wollen
	 * @see #requestTimedChecks(SimulationData, StateChangeListener)
	 * @see #runTimedCheckNow(SimulationData)
	 */
	private Set<StateChangeListener> timedCheckStations=new HashSet<>();

	/**
	 * Meldet, dass eine Station momentan prinzipiell Interesse an zeitabhängigen Checks hätte.<br>
	 * Ob diese aktiv sind, muss die Funktion prüfen.
	 * @param simData	Simulationsdatenobjekt
	 * @param station	Station, die zeitabhängig benachrichtigt werden möchte
	 */
	public void requestTimedChecks(final SimulationData simData, final StateChangeListener station) {
		if (simData.runModel.timedChecksDelta<=0) return;

		if (timedCheckStations.contains(station)) return; /* Ist schon auf der Liste */

		final boolean checksNotRunning=timedCheckStations.isEmpty();
		timedCheckStations.add(station);

		if (checksNotRunning) {
			TimedCheckEvent.scheduleCheck(simData);
		}
	}

	/**
	 * Führt die zeitabhängigen Checks durch.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt an, ob auch in Zukunft Zeit-Checks stattfinden sollen (<code>true</code>)
	 * @see TimedCheckEvent
	 * @see #requestTimedChecks(SimulationData, StateChangeListener)
	 */
	public boolean runTimedCheckNow(final SimulationData simData) {
		StateChangeListener remove=null;
		for (StateChangeListener station: timedCheckStations) if (station.interestedInChangeNotifiesAtTheMoment(simData)) {
			if (station.systemStateChangeNotify(simData)) {
				/* Nur eine Änderung pro Zeitschritt */
				SystemChangeEvent.triggerEvent(simData,1);
				break;
			}
		} else {
			if (remove==null) remove=station;
		}
		if (remove!=null) timedCheckStations.remove(remove);

		/* Weitere Checks nötig? */
		return !timedCheckStations.isEmpty();
	}

	/**
	 * Erfassung der Übergänge zwischen den Stationen
	 * @see #recordClientMovement(SimulationData, RunDataClient)
	 */
	private StatisticsSimpleCountPerformanceIndicator[][] movementCounter;

	/**
	 * Erfassung der Übergänge zwischen den Stationen
	 * @see #recordClientMovement(SimulationData, RunDataClient)
	 */
	private StatisticsSimpleCountPerformanceIndicator[] movementCounterFromStart;

	/**
	 * Erfassung der Übergänge zwischen den Stationen
	 * @see #recordClientMovement(SimulationData, RunDataClient)
	 */
	private StatisticsSimpleCountPerformanceIndicator[] movementCounterToEnd;

	/**
	 * Erfassung der Übergänge zwischen den Stationen
	 * @see #recordClientMovement(SimulationData, RunDataClient)
	 */
	private StatisticsSimpleCountPerformanceIndicator movementCounterEmpty;

	/**
	 * Liefert den Namen für einen bestimmten Kunden-Stationsübergangs-Zähler
	 * @param simData	Simulationsdatenobjekt
	 * @param fromID	ID der Startstation (oder -1 für Systemeingang)
	 * @param toID	ID der Zielstation (oder -1 für Systemausgang)
	 * @return	Name des Kunden-Stationsübergangs-Zählers
	 */
	private String getClientMovementCounterName(final SimulationData simData, final int fromID, final int toID) {
		final String fromName;
		if (fromID>=0) {
			fromName=simData.runModel.elementsFast[fromID].name;
		} else {
			fromName=Language.tr("Simulation.ClientMovement.Start").toUpperCase();
		}

		final String toName;
		if (toID>=0) {
			toName=simData.runModel.elementsFast[toID].name;
		} else {
			toName=Language.tr("Simulation.ClientMovement.End").toUpperCase();
		}
		return fromName+" -> "+toName;
	}

	/**
	 * Liefert einen Kunden-Stationsübergangs-Zähler
	 * @param simData	Simulationsdatenobjekt
	 * @param fromID	ID der Startstation (oder -1 für Systemeingang)
	 * @param toID	ID der Zielstation (oder -1 für Systemausgang)
	 * @return	Kunden-Stationsübergangs-Zähler
	 */
	private StatisticsSimpleCountPerformanceIndicator getNewClientMovementCounter(final SimulationData simData, final int fromID, final int toID) {
		return (StatisticsSimpleCountPerformanceIndicator)simData.statistics.stationTransition.get(getClientMovementCounterName(simData,fromID,toID));
	}

	/**
	 * Erfasst den Stationsübergang eines Kunden
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde
	 * @see #fireClientMoveNotify(SimulationData, RunDataClient, boolean)
	 */
	private void recordClientMovement(final SimulationData simData, final RunDataClient client) {
		if (client.isWarmUp || !client.inStatistics) return;

		final int fromID=client.lastStationID;
		final int toID=client.nextStationID;

		if (simData.runModel.recordClientPaths) {
			if (toID>=0) client.recordPathStep(toID);
		}

		if (simData.runModel.recordStationTransitions) {
			if (fromID<0 && toID<0) {
				if (movementCounterEmpty==null) movementCounterEmpty=getNewClientMovementCounter(simData,fromID,toID);
				movementCounterEmpty.add();
				return;
			}

			if (fromID<0) {
				if (movementCounterFromStart==null) movementCounterFromStart=new StatisticsSimpleCountPerformanceIndicator[simData.runModel.elementsFast.length];
				if (movementCounterFromStart[toID]==null) movementCounterFromStart[toID]=getNewClientMovementCounter(simData,fromID,toID);
				movementCounterFromStart[toID].add();
				return;
			}

			if (toID<0) {
				if (movementCounterToEnd==null) movementCounterToEnd=new StatisticsSimpleCountPerformanceIndicator[simData.runModel.elementsFast.length];
				if (movementCounterToEnd[fromID]==null) movementCounterToEnd[fromID]=getNewClientMovementCounter(simData,fromID,toID);
				movementCounterToEnd[fromID].add();
				return;
			}

			if (movementCounter==null) movementCounter=new StatisticsSimpleCountPerformanceIndicator[simData.runModel.elementsFast.length][];
			if (movementCounter[fromID]==null) movementCounter[fromID]=new StatisticsSimpleCountPerformanceIndicator[simData.runModel.elementsFast.length];
			if (movementCounter[fromID][toID]==null) movementCounter[fromID][toID]=getNewClientMovementCounter(simData,fromID,toID);
			movementCounter[fromID][toID].add();
		}
	}

	/**
	 * Zählung der Ankünfte
	 * @see #nextClientIsLast(SimulationData)
	 */
	private long arrivalsToBeSimulated=0;

	/**
	 * Vom Load-Balancer erhaltene Ankünfte
	 * @see #nextClientIsLast(SimulationData)
	 */
	private long currentArrivalPackage=0;

	/**
	 * Liefert die Information, ob der aktuell zu generierende Kunde der letzte sein soll.<br>
	 * Diese Methode muss bei Ankünften zur Zählung aufgerufen werden.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Liefert <code>true</code>, wenn der aktuelle Kunde der letzte sein soll; sonst (also wenn die Simulation weiter laufen soll) <code>false</code>
	 */
	public boolean nextClientIsLast(final SimulationData simData) {
		if (isWarmUp) return false;
		if (simData.runModel.clientCount<=0) return false;

		if (dynamicLoadBalancer==null) {
			/* Direkte Berechnung der Anzahl an zu simulierenden Kunden */
			if (arrivalsToBeSimulated<=0) {
				arrivalsToBeSimulated=simData.runModel.clientCount/simData.clientCountDiv;
				if (simData.threadCount>1 && simData.threadNr==simData.threadCount-1) arrivalsToBeSimulated+=simData.runModel.clientCount%simData.clientCountDiv;
			}
			return clientsArrived>=arrivalsToBeSimulated;
		} else {
			/* Ankünfte beim Load-Balancer anfragen */
			if (currentArrivalPackage<=0) currentArrivalPackage=dynamicLoadBalancer.getArrivals();
			currentArrivalPackage--;
			return currentArrivalPackage<0;
		}
	}
}