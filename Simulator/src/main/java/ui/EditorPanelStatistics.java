/**
 * Copyright 2021 Alexander Herzog
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
package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.builder.RunModelCreator;
import simulator.elements.RunElementDecideByChance;
import simulator.elements.RunElementDecideByClientType;
import simulator.elements.RunElementDecideByCondition;
import simulator.elements.RunElementDecideByKeyValue;
import simulator.elements.RunElementDecideBySequence;
import simulator.elements.RunElementDecideByStation;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsQuotientPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import statistics.StatisticsSimpleValueMaxPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementAssign;
import ui.modeleditor.elements.ModelElementAssignMulti;
import ui.modeleditor.elements.ModelElementBatch;
import ui.modeleditor.elements.ModelElementCounter;
import ui.modeleditor.elements.ModelElementCounterBatch;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementDifferentialCounter;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementMatch;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementThroughput;
import ui.modeleditor.elements.ModelElementTransportSource;
import ui.modeleditor.elements.ModelElementTransportTransporterSource;
import ui.modeleditor.elements.ModelElementVertex;
import ui.statistics.StatisticTools;
import ui.statistics.StatisticViewerOverviewText;

/**
 * Diese Helferklasse ermöglicht es, Statistikdaten für einzlene
 * Editor-Modell-Stationen zu bestimmen und daraus Daten für die
 * Editor-Modellansicht (Tooltips, Heatmap-Intensitäten) zu generieren.
 * @author Alexander Herzog
 * @see EditorPanel
 */
public class EditorPanelStatistics {
	/**
	 * Sollen Farben bei der Statistikausgabe verwendet werden?
	 */
	private final boolean useColors;

	/**
	 * Zuordnung der bereits generierten Tooltips,
	 * um diese nicht ständig erneut anlegen zu müssen.
	 * @see #getTooltip(Statistics, ModelElementBox)
	 */
	private final Map<Integer,String> tooltipsCache;

	/**
	 * Zuordnung der bereits generierten Stations-Statistik-Namen,
	 * um diese nicht ständig erneut anlegen zu müssen.
	 * @see #elementStatisticsName(ModelElementBox)
	 */
	private final Map<Integer,String> nameCache;

	/**
	 * Maximalwert über die mittleren Anzahlen an Kunden an allen Stationen
	 * @see #maxMeanClientsAtStations(Statistics)
	 */
	private double maxMeanClientsAtStationsCache;

	/**
	 * Maximalwert über die mittleren Anzahlen an wartenden Kunden an allen Stationen
	 * @see #maxMeanClientsAtStationQueues(Statistics)
	 */
	private double maxMeanClientsAtStationQueuesCache;

	/**
	 * Maximalwert über die mittleren Anzahlen an Kunden in Bedienung an allen Stationen
	 * @see #maxMeanClientsAtStationProcess(Statistics)
	 */
	private double maxMeanClientsAtStationProcessCache;

	/**
	 * Maximalwert über die maximalen Anzahlen an Kunden an allen Stationen
	 * @see #maxMeanClientsAtStations(Statistics)
	 */
	private double maxClientsAtStationsCache;

	/**
	 * Maximalwert über die maximalen Anzahlen an wartenden Kunden an allen Stationen
	 * @see #maxMeanClientsAtStationQueues(Statistics)
	 */
	private double maxClientsAtStationQueuesCache;

	/**
	 * Maximalwert über die maximalen Anzahlen an Kunden im Bedienprozess an allen Stationen
	 * @see #maxMeanClientsAtStationProcess(Statistics)
	 */
	private double maxClientsAtStationProcessCache;

	/**
	 * Maximaler Flussgrad über alle Stationen
	 * @see #maxFlowFactor(Statistics)
	 */
	private double maxFlowFactorCache;

	/**
	 * Maximale Anzahl an Kundenankünften über alle Stationen
	 * @see #maxClientsCountCache
	 */
	private long maxClientsCountCache;

	/**
	 * Name des Modells
	 * (um zu prüfen, ob die gecachten Statistikdaten weiter verwendet
	 * werden können, ohne dafür das Statistikobjekt selbst referenziert
	 * halten zu müssen)
	 * @see #testStatistics(Statistics)
	 */
	private String lastModelName;

	/**
	 * Zeitpunkt der Ausführung der Simulation
	 * (um zu prüfen, ob die gecachten Statistikdaten weiter verwendet
	 * werden können, ohne dafür das Statistikobjekt selbst referenziert
	 * halten zu müssen)
	 * @see #testStatistics(Statistics)
	 */
	private String lastRunDate;

	/**
	 * Laufzeit der Simulation
	 * (um zu prüfen, ob die gecachten Statistikdaten weiter verwendet
	 * werden können, ohne dafür das Statistikobjekt selbst referenziert
	 * halten zu müssen)
	 * @see #testStatistics(Statistics)
	 */
	private long lastRunTime;

	/**
	 * Anzahl der Ereignisse während der Simulation
	 * (um zu prüfen, ob die gecachten Statistikdaten weiter verwendet
	 * werden können, ohne dafür das Statistikobjekt selbst referenziert
	 * halten zu müssen)
	 * @see #testStatistics(Statistics)
	 */
	private long lastRunEvents;

	/**
	 * Anzahl der Wiederholungen der Simulation
	 * (um zu prüfen, ob die gecachten Statistikdaten weiter verwendet
	 * werden können, ohne dafür das Statistikobjekt selbst referenziert
	 * halten zu müssen)
	 * @see #testStatistics(Statistics)
	 */
	private long lastRepeatCount;

	/**
	 * Konstruktor der Klasse
	 * @param useColors	Sollen Farben bei der Statistikausgabe verwendet werden?
	 */
	public EditorPanelStatistics(final boolean useColors) {
		this.useColors=useColors;
		tooltipsCache=new HashMap<>();
		nameCache=new HashMap<>();
		clearCache();
	}

	/**
	 * Konstruktor der Klasse
	 */
	public EditorPanelStatistics() {
		this(true);
	}

	/**
	 * Löscht den Cache der bereits generierten Daten
	 * (wenn ein neues Statistikobjekt verwendet werden soll).
	 * @see #testStatistics(Statistics)
	 */
	private void clearCache() {
		tooltipsCache.clear();
		nameCache.clear();
		maxMeanClientsAtStationsCache=-1;
		maxMeanClientsAtStationQueuesCache=-1;
		maxMeanClientsAtStationProcessCache=-1;
		maxClientsAtStationsCache=-1;
		maxClientsAtStationQueuesCache=-1;
		maxClientsAtStationProcessCache=-1;
		maxFlowFactorCache=-1;
		maxClientsCountCache=-1;
	}

	/**
	 * Prüft, ob sich die Daten im Cache noch auf das übergegebene
	 * Statistikobjekt beziehen und setzt wenn nicht den Cache zurück.
	 * @param statistics	Neues Statistikobjekt
	 */
	private void testStatistics(final Statistics statistics) {
		boolean clearCache=false;

		if (!statistics.editModel.name.equals(lastModelName)) clearCache=true;
		lastModelName=statistics.editModel.name;

		if (!statistics.simulationData.runDate.equals(lastRunDate)) clearCache=true;
		lastRunDate=statistics.simulationData.runDate;

		if (statistics.simulationData.runTime!=lastRunTime) clearCache=true;
		lastRunTime=statistics.simulationData.runTime;

		if (statistics.simulationData.runEvents!=lastRunEvents) clearCache=true;
		lastRunEvents=statistics.simulationData.runEvents;

		if (statistics.simulationData.runRepeatCount!=lastRepeatCount) clearCache=true;
		lastRepeatCount=statistics.simulationData.runRepeatCount;

		if (clearCache) clearCache();
	}

	/**
	 * Ermittelt den für Stationen, bei denen dies nicht über den Laufzeitstationen-Katalog möglich ist,
	 * den Namen einer Station in der Formatierung, wie er für die Statistikaufzeichnung verwendet wird.
	 * @param element	Editor-Modell-Station für die der Statistik-Name ermittelt werden soll
	 * @return	Statistik-Name der Station
	 * @see #elementStatisticsName(ModelElementBox)
	 */
	private String elementStatisticsNameSpecial(final ModelElementBox element) {
		if (element instanceof ModelElementDecide) {
			final ModelElementDecide decide=(ModelElementDecide)element;
			switch (decide.getDecideRecord().getMode()) {
			case MODE_CHANCE: return new RunElementDecideByChance(decide).name;
			case MODE_CLIENTTYPE: return new RunElementDecideByClientType(decide).name;
			case MODE_CONDITION: return new RunElementDecideByCondition(decide).name;
			case MODE_KEY_VALUE: return new RunElementDecideByKeyValue(decide).name;
			case MODE_MIN_CLIENTS_NEXT_STATION: return new RunElementDecideByStation(decide).name;
			case MODE_MIN_CLIENTS_PROCESS_STATION: return new RunElementDecideByStation(decide).name;
			case MODE_MAX_CLIENTS_NEXT_STATION: return new RunElementDecideByStation(decide).name;
			case MODE_MAX_CLIENTS_PROCESS_STATION: return new RunElementDecideByStation(decide).name;
			case MODE_SEQUENCE: return new RunElementDecideBySequence(decide).name;
			case MODE_SHORTEST_QUEUE_NEXT_STATION: return new RunElementDecideByStation(decide).name;
			case MODE_SHORTEST_QUEUE_PROCESS_STATION: return new RunElementDecideByStation(decide).name;
			case MODE_LONGEST_QUEUE_NEXT_STATION: return new RunElementDecideByStation(decide).name;
			case MODE_LONGEST_QUEUE_PROCESS_STATION: return new RunElementDecideByStation(decide).name;
			}
		}

		return null;
	}

	/**
	 * Ermittelt den Namen einer Station in der Formatierung, wie er für die Statistikaufzeichnung verwendet wird.
	 * @param element	Editor-Modell-Station für die der Statistik-Name ermittelt werden soll
	 * @return	Statistik-Name der Station
	 */
	private String elementStatisticsName(final ModelElementBox element) {
		final int id=element.getId();
		String name=nameCache.get(id);
		if (name!=null) {
			if (name.isEmpty()) return null; else return name;
		}

		name=elementStatisticsNameSpecial(element);
		if (name==null) name=RunModelCreator.getName(element);
		if (name==null) nameCache.put(id,""); else nameCache.put(id,name); /* "Kein Name vorhanden" als leeren String speichern, damit auch in diesem Fall die Abfrage nicht erneut erfolgt. */
		return name;
	}

	/**
	 * Setzt aus mehreren Zeilen eine html-formatierte Statistikausgabe zusammen
	 * @param lines	Eingabezeilen
	 * @return	html-formatierte Statistikausgabe
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String formatStatisticsData(final String[] lines) {
		if (lines==null || lines.length==0) return null;
		final StringBuilder sb=new StringBuilder();
		if (useColors) sb.append("<i><span style='color: green;'>");
		sb.append(Language.tr("Main.Toolbar.ShowStatistics"));
		sb.append(":<br/>");
		sb.append(String.join("<br/>",lines));
		if (useColors) sb.append("</span></i>");
		return sb.toString();
	}

	/**
	 * Setzt aus mehreren Zeilen eine html-formatierte Statistikausgabe zusammen
	 * @param lines	Eingabezeilen
	 * @return	html-formatierte Statistikausgabe
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String formatStatisticsData(final List<String> lines) {
		return formatStatisticsData(lines.toArray(String[]::new));
	}

	/**
	 * Wandelt eine Zeile eine html-formatierte Statistikausgabe um
	 * @param line	Eingabezeile
	 * @return	html-formatierte Statistikausgabe
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String formatStatisticsData(final String line) {
		return formatStatisticsData(new String[]{line});
	}

	/**
	 * Wandelt eine Zeitangabe in eine Zeichenkette um
	 * @param time	Zeitangabe
	 * @return	Textdarstellung der Zeitangabe
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String formatTime(final double time) {
		return TimeTools.formatExactTime(time)+" ("+StatisticTools.formatNumberExt(time,false)+")";
	}

	/**
	 * Trägt, wenn vorhanden, Daten zu einer Station oder einem Kundentyp (Zeiten und Anzahlen) in eine Ausgabe ein.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param name	Name der Station oder des Kundentyps
	 * @param lines	Ausgabeobjekt
	 * @param nameIsClientType	Handelt es sich bei dem Namen um einen Kundentyp?
	 * @param element	Station auf die sich die Daten beziehen (kann <code>null</code> sein)
	 */
	private void addFullInformation(final Statistics statistics, final String name, final List<String> lines, final boolean nameIsClientType, final ModelElement element) {
		final StatisticsDataPerformanceIndicator waiting;
		final StatisticsDataPerformanceIndicator transfer;
		final StatisticsDataPerformanceIndicator process;
		final StatisticsDataPerformanceIndicator residence;
		final StatisticsTimePerformanceIndicator wip;
		final StatisticsTimePerformanceIndicator nq;
		final StatisticsTimePerformanceIndicator ns;
		String throughput=null;
		String maxThroughput=null;
		String maxThroughputInfo=null;
		if (nameIsClientType) {
			waiting=((StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.getOrNull(name));
			transfer=((StatisticsDataPerformanceIndicator)statistics.clientsTransferTimes.getOrNull(name));
			process=((StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.getOrNull(name));
			residence=((StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.getOrNull(name));
			wip=((StatisticsTimePerformanceIndicator)statistics.clientsInSystemByClient.getOrNull(name));
			nq=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByClient.getOrNull(name));
			ns=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationProcessByClient.getOrNull(name));
		} else {
			waiting=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.getOrNull(name));
			transfer=((StatisticsDataPerformanceIndicator)statistics.stationsTransferTimes.getOrNull(name));
			process=((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.getOrNull(name));
			residence=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.getOrNull(name));
			wip=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.getOrNull(name));
			nq=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByStation.getOrNull(name));
			ns=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationProcessByStation.getOrNull(name));
			final StatisticsDataPerformanceIndicator arrival=(StatisticsDataPerformanceIndicator)(statistics.stationsInterarrivalTime.getOrNull(name));
			if (arrival!=null) throughput=StatisticViewerOverviewText.getThroughputText(arrival.getCount(),statistics);
			final StatisticsSimpleValueMaxPerformanceIndicator maxThroughputIndicator=(StatisticsSimpleValueMaxPerformanceIndicator)(statistics.stationsMaxThroughput.getOrNull(name));
			if (maxThroughputIndicator!=null && maxThroughputIndicator.get()>0) {
				maxThroughput=StatisticViewerOverviewText.getMaxThroughputText(maxThroughputIndicator.get());
				if (element instanceof ModelElementBox) {
					final int maxThroughputIntervalLength=((ModelElementBox)element).getMaxThroughputIntervalSeconds();
					if (maxThroughputIntervalLength>0) {
						maxThroughputInfo="("+String.format(Language.tr("Statistics.Throughput.Maximum.IntervalLength"),NumberTools.formatLong(maxThroughputIntervalLength))+")";
					}
				}
			}
		}

		if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
		if (transfer!=null && transfer.getMean()>0) lines.add("E[T]="+formatTime(transfer.getMean()));
		if (process!=null && process.getMean()>0) lines.add("E[S]="+formatTime(process.getMean()));
		if (residence!=null && residence.getMean()>0) lines.add("E[V]="+formatTime(residence.getMean()));
		if (nq!=null && nq.getTimeMean()>0) lines.add("E[NQ]"+StatisticTools.formatNumberExt(nq.getTimeMean(),true));
		if (ns!=null && ns.getTimeMean()>0) lines.add("E[NS]"+StatisticTools.formatNumberExt(ns.getTimeMean(),true));
		if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]"+StatisticTools.formatNumberExt(wip.getTimeMean(),true));
		if (throughput!=null) lines.add(Language.tr("Statistics.Throughput")+": "+throughput);
		if (maxThroughput!=null) lines.add(Language.tr("Statistics.Throughput.Maximum")+": "+maxThroughput);
		if (maxThroughputInfo!=null) lines.add(maxThroughputInfo);
	}

	/**
	 * Trägt, wenn vorhanden, Daten zu einer Station (Zeiten) in eine Ausgabe ein.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param name	Name der Station
	 * @param lines	Ausgabeobjekt
	 * @param element	Station auf die sich die Daten beziehen (kann <code>null</code> sein)
	 */
	private void addTimeInformation(final Statistics statistics, final String name, final List<String> lines, final ModelElement element) {
		final StatisticsDataPerformanceIndicator waiting=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.getOrNull(name));
		final StatisticsDataPerformanceIndicator transfer=((StatisticsDataPerformanceIndicator)statistics.stationsTransferTimes.getOrNull(name));
		final StatisticsDataPerformanceIndicator process=((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.getOrNull(name));
		final StatisticsDataPerformanceIndicator residence=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.getOrNull(name));
		final StatisticsTimePerformanceIndicator wip=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.getOrNull(name));
		final StatisticsDataPerformanceIndicator arrival=(StatisticsDataPerformanceIndicator)(statistics.stationsInterarrivalTime.getOrNull(name));
		String throughput=null;
		String maxThroughput=null;
		String maxThroughputInfo=null;
		if (arrival!=null) throughput=StatisticViewerOverviewText.getThroughputText(arrival.getCount(),statistics);
		final StatisticsSimpleValueMaxPerformanceIndicator maxThroughputIndicator=(StatisticsSimpleValueMaxPerformanceIndicator)(statistics.stationsMaxThroughput.getOrNull(name));
		if (maxThroughputIndicator!=null && maxThroughputIndicator.get()>0) {
			maxThroughput=StatisticViewerOverviewText.getMaxThroughputText(maxThroughputIndicator.get());
			if (element instanceof ModelElementBox) {
				final int maxThroughputIntervalLength=((ModelElementBox)element).getMaxThroughputIntervalSeconds();
				if (maxThroughputIntervalLength>0) {
					maxThroughputInfo="("+String.format(Language.tr("Statistics.Throughput.Maximum.IntervalLength"),NumberTools.formatLong(maxThroughputIntervalLength))+")";
				}
			}
		}

		if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
		if (transfer!=null && transfer.getMean()>0) lines.add("E[T]="+formatTime(transfer.getMean()));
		if (process!=null && process.getMean()>0) lines.add("E[S]="+formatTime(process.getMean()));
		if (residence!=null && residence.getMean()>0) lines.add("E[V]="+formatTime(residence.getMean()));
		if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]"+StatisticTools.formatNumberExt(wip.getTimeMean(),true));
		if (throughput!=null) lines.add(Language.tr("Statistics.Throughput")+": "+throughput);
		if (maxThroughput!=null) lines.add(Language.tr("Statistics.Throughput.Maximum")+": "+maxThroughput);
		if (maxThroughputInfo!=null) lines.add(maxThroughputInfo);
	}

	/**
	 * Generiert den Tooltip-Text für eine Kundenquelle-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipSource(final Statistics statistics, final ModelElementBox element) {
		final String nameStation=elementStatisticsName(element);
		final String nameClient=element.getName();

		final StatisticsDataPerformanceIndicator inter=((StatisticsDataPerformanceIndicator)statistics.clientsInterarrivalTime.getOrNull(nameStation));

		final List<String> lines=new ArrayList<>();
		if (inter!=null) lines.add("E[I]="+formatTime(inter.getMean()));
		lines.add(nameClient);
		addFullInformation(statistics,nameClient,lines,true,null);

		return formatStatisticsData(lines);
	}

	/**
	 * Generiert den Tooltip-Text für eine Zuweisung-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipAssign(final Statistics statistics, final ModelElementBox element) {
		final String nameClient=element.getName();

		final List<String> lines=new ArrayList<>();
		lines.add(nameClient);
		addFullInformation(statistics,nameClient,lines,true,null);

		return formatStatisticsData(lines);
	}

	/**
	 * Generiert den Tooltip-Text für eine Mehrfach-Zuweisung-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipAssignMulti(final Statistics statistics, final ModelElementBox element) {
		// TODI
		return "";
	}

	/**
	 * Generiert den Tooltip-Text für eine Station, bei der alle Arten von Zeiten auftreten können.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipAllTimes(final Statistics statistics, final ModelElementBox element) {
		final String nameStation=elementStatisticsName(element);

		final List<String> lines=new ArrayList<>();
		addFullInformation(statistics,nameStation,lines,false,element);

		return formatStatisticsData(lines);
	}

	/**
	 * Generiert den Tooltip-Text für eine Station, bei der nur Wartezeiten auftreten können.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipWaiting(final Statistics statistics, final ModelElementBox element) {
		final String nameStation=elementStatisticsName(element);

		final List<String> lines=new ArrayList<>();
		addTimeInformation(statistics,nameStation,lines,element);

		return formatStatisticsData(lines);
	}

	/**
	 * Generiert den Tooltip-Text für eine Zähler-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipCounter(final Statistics statistics, final ModelElementBox element) {
		final String groupName=((ModelElementCounter)element).getGroupName().replace('-','_')+"-";
		final StatisticsSimpleCountPerformanceIndicator indicator=((StatisticsSimpleCountPerformanceIndicator)statistics.counter.getOrNull(groupName+element.getName()));
		final long value=(indicator==null)?0:indicator.get();
		long sum=0;
		for (String name: statistics.counter.getNames()) {
			if (name.startsWith(groupName)) sum+=((StatisticsSimpleCountPerformanceIndicator)statistics.counter.get(name)).get();
		}
		return formatStatisticsData(NumberTools.formatLong(value)+" ("+StatisticTools.formatPercentExt(((double)value)/sum,false)+")");
	}

	/**
	 * Generiert den Tooltip-Text für eine Batch-Zähler-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipCounterBatch(final Statistics statistics, final ModelElementBox element) {
		final List<String> lines=new ArrayList<>();
		final String stationName=element.getName();
		for (String name: statistics.counterBatch.getNames()) {
			if (!name.startsWith(stationName)) continue;
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.counterBatch.get(name);
			if (indicator.getCount()==0) continue;
			if (stationName.equals(name)) {
				lines.add("E[IB]="+formatTime(indicator.getMean())+", #IB="+NumberTools.formatLong(indicator.getCount()));
			} else {
				lines.add(name.substring(stationName.length()+1)+": E[IB]="+formatTime(indicator.getMean())+", #IB="+NumberTools.formatLong(indicator.getCount()));
			}
		}
		return formatStatisticsData(lines);
	}

	/**
	 * Generiert den Tooltip-Text für eine Differenzzähler-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipDifferentialCounter(final Statistics statistics, final ModelElementBox element) {
		final StatisticsTimePerformanceIndicator counter=(StatisticsTimePerformanceIndicator)statistics.differentialCounter.getOrNull(element.getName());
		if (counter==null) return null;
		return formatStatisticsData(Language.tr("Statistics.Average")+StatisticTools.formatNumberExt(counter.getTimeMean(),true));
	}

	/**
	 * Generiert den Tooltip-Text für eine Durchsatzmess-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipThroughput(final Statistics statistics, final ModelElementBox element) {
		final StatisticsQuotientPerformanceIndicator indicator=(StatisticsQuotientPerformanceIndicator)statistics.throughputStatistics.get(element.getName());
		if (indicator==null) return null;
		double value=indicator.getQuotient();
		String unit=Language.tr("Statistics.TimeUnit.Second");
		if (value<1) {
			value*=60;
			unit=Language.tr("Statistics.TimeUnit.Minute");
			if (value<1) {
				value*=60;
				unit=Language.tr("Statistics.TimeUnit.Hour");
				if (value<1) {
					value*=24;
					unit=Language.tr("Statistics.TimeUnit.Day");
				}
			}
		}
		return formatStatisticsData(Language.tr("Statistics.Throughput")+" "+element.getName()+": "+StatisticTools.formatNumberExt(value,2,false)+" (1/"+unit+")");
	}

	/**
	 * Generiert den Tooltip-Text für eine Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 * @see #getTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltip(final Statistics statistics, final ModelElementBox element) {
		if (element instanceof ModelElementSource) return buildTooltipSource(statistics,element);
		if (element instanceof ModelElementAssign) return buildTooltipAssign(statistics,element);
		if (element instanceof ModelElementAssignMulti) return buildTooltipAssignMulti(statistics,element);
		if (element instanceof ModelElementProcess) return buildTooltipAllTimes(statistics,element);

		if (element instanceof ModelElementTransportSource) return buildTooltipAllTimes(statistics,element);
		if (element instanceof ModelElementTransportTransporterSource) return buildTooltipAllTimes(statistics,element);

		if (element instanceof ModelElementSub) return buildTooltipAllTimes(statistics,element);

		if (element instanceof ModelElementDelay) return buildTooltipWaiting(statistics,element);
		if (element instanceof ModelElementBatch) return buildTooltipWaiting(statistics,element);
		if (element instanceof ModelElementMatch) return buildTooltipWaiting(statistics,element);

		if (element instanceof ModelElementCounter) return buildTooltipCounter(statistics,element);
		if (element instanceof ModelElementCounterBatch) return buildTooltipCounterBatch(statistics,element);
		if (element instanceof ModelElementDifferentialCounter) return buildTooltipDifferentialCounter(statistics,element);
		if (element instanceof ModelElementThroughput) return buildTooltipThroughput(statistics,element);

		return null;
	}

	/**
	 * Liefert die Anzahl an Übergänge von einer zu einer anderen Station
	 * @param statistics	Statistikobjekt (darf <code>null</code> sein)
	 * @param id1	ID der Quellstation
	 * @param id2	ID der Zielstation
	 * @return	Liefert im Erfolgsfall den die Anzahl, sonst -1
	 */
	public static long getEdgeCount(final Statistics statistics, final int id1, final int id2) {
		if (statistics==null) return -1;

		final Pattern pattern=Pattern.compile("^.* \\(id="+id1+"\\) \\-> .* \\(id="+id2+"\\)$", Pattern.CASE_INSENSITIVE);

		for (String name: statistics.stationTransition.getNames()) {
			final Matcher matcher=pattern.matcher(name);
			if (matcher.find()) {
				return ((StatisticsSimpleCountPerformanceIndicator)statistics.stationTransition.get(name)).get();
			}
		}

		return -1;
	}

	/**
	 * Liefert den Durchsatz zwischen zwei Stationen (wenn die Stationsübergangsaufzeichnung aktiv ist).
	 * @param statistics	Statistikobjekt (darf <code>null</code> sein)
	 * @param id1	ID der Quellstation
	 * @param id2	ID der Zielstation
	 * @return	Liefert im Erfolgsfall den Durchsatz, sonst <code>null</code>
	 */
	public static String getEdgeThroughput(final Statistics statistics, final int id1, final int id2) {
		final long count=getEdgeCount(statistics,id1,id2);
		if (count<0) return null;
		return StatisticViewerOverviewText.getThroughputText(count,statistics);
	}

	/**
	 * Generiert den Tooltip-Text für eine Kante.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param edge	Kante für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 * @see #getTooltip(Statistics, ModelElementEdge)
	 */
	private String buildTooltip(final Statistics statistics, final ModelElementEdge edge) {
		if (edge==null || edge.getConnectionStart()==null) return null;

		final List<Integer> id1=new ArrayList<>();
		int id2=-1;

		/* Startstationen finden */
		final List<ModelElement> toProcess=new ArrayList<>();
		toProcess.add(edge.getConnectionStart());
		while (toProcess.size()>0) {
			ModelElement element=toProcess.remove(0);
			while (true) {
				if (element instanceof ModelElementVertex) {
					final ModelElementEdge[] edges=((ModelElementVertex)element).getEdgesIn();
					if (edges.length>=2) for (int i=1;i<edges.length;i++) toProcess.add(edges[i].getConnectionStart());
					if (edges.length>=1) {
						element=edges[0].getConnectionStart();
						if (element==null) break;
						continue;
					}
					break;
				}
				if (element instanceof ModelElementBox) {
					id1.add(element.getId());
					break;
				}
				break;
			}
		}

		/* Zielstation finden */
		ModelElementEdge e=edge;
		while (true) {
			if (e==null || e.getConnectionEnd()==null) return null;
			final ModelElement element=e.getConnectionEnd();
			if (element instanceof ModelElementVertex) {e=((ModelElementVertex)element).getEdgeOut(); continue;}
			if (element instanceof ModelElementBox) {id2=element.getId(); break;}
			return null;
		}

		/* Existieren Pfade? */
		if (id1.size()==0 || id2<0) return null;

		/* Pfade aufsummieren */
		long sum=0;
		for (Integer i: id1) sum+=Math.max(0,getEdgeCount(statistics,i,id2));
		if (sum==0) return null;
		return formatStatisticsData(new String[] {Language.tr("Statistics.Throughput")+": "+StatisticViewerOverviewText.getThroughputText(sum,statistics)});
	}

	/**
	 * Liefert, sofern verfügbar, Statistikdaten, die sich auf eine bestimmte
	 * Station beziehen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 */
	public String getTooltip(final Statistics statistics, final ModelElementBox element) {
		/* Statistikdaten verfügbar? */
		if (statistics==null) return null;

		/* Ist das noch dasselbe Statistikobjekt wie beim letzten Aufruf? Wenn nein, Caches löschen */
		testStatistics(statistics);

		/* Bereits generierten Tooltip abrufen */
		final int id=element.getId();
		String tooltip=tooltipsCache.get(id);
		if (tooltip!=null) {
			if (tooltip.isEmpty()) return null; else return tooltip;
		}

		/* Neuen Tooltip generieren */
		tooltip=buildTooltip(statistics,element);
		if (tooltip==null) tooltipsCache.put(id,""); else tooltipsCache.put(id,tooltip); /* "Kein Tooltip vorhanden" als leeren String speichern, damit auch in diesem Fall die Generierung nicht erneut erfolgt. */
		return tooltip;
	}

	/**
	 * Liefert, sofern verfügbar, Statistikdaten, die sich auf eine bestimmte
	 * Kante beziehen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param edge	Katne für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verfügung stehen
	 */
	public String getTooltip(final Statistics statistics, final ModelElementEdge edge) {
		/* Statistikdaten verfügbar? */
		if (statistics==null) return null;

		/* Ist das noch dasselbe Statistikobjekt wie beim letzten Aufruf? Wenn nein, Caches löschen */
		testStatistics(statistics);

		/* Bereits generierten Tooltip abrufen */
		final int id=edge.getId();
		String tooltip=tooltipsCache.get(id);
		if (tooltip!=null) {
			if (tooltip.isEmpty()) return null; else return tooltip;
		}

		/* Neuen Tooltip generieren */
		tooltip=buildTooltip(statistics,edge);
		if (tooltip==null) tooltipsCache.put(id,""); else tooltipsCache.put(id,tooltip); /* "Kein Tooltip vorhanden" als leeren String speichern, damit auch in diesem Fall die Generierung nicht erneut erfolgt. */
		return tooltip;
	}

	/**
	 * Liefert den Maximalwert über die mittleren Anzahlen an Kunden an allen Stationen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximalwert über die mittleren Anzahlen an Kunden an allen Stationen
	 */
	private double maxMeanClientsAtStations(final Statistics statistics) {
		if (maxMeanClientsAtStationsCache<0.0) {
			final StatisticsTimePerformanceIndicator[] stat=statistics.clientsAtStationByStation.getAll(StatisticsTimePerformanceIndicator.class);
			maxMeanClientsAtStationsCache=0.0;
			for (StatisticsTimePerformanceIndicator indicator: stat) {
				maxMeanClientsAtStationsCache=Math.max(maxMeanClientsAtStationsCache,indicator.getTimeMean());
			}
		}

		return maxMeanClientsAtStationsCache;
	}

	/**
	 * Liefert den Maximalwert über die mittleren Anzahlen an wartenden Kunden an allen Stationen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximalwert über die mittleren Anzahlen an wartenden Kunden an allen Stationen
	 */
	private double maxMeanClientsAtStationQueues(final Statistics statistics) {
		if (maxMeanClientsAtStationQueuesCache<0.0) {
			final StatisticsTimePerformanceIndicator[] stat=statistics.clientsAtStationQueueByStation.getAll(StatisticsTimePerformanceIndicator.class);
			maxMeanClientsAtStationQueuesCache=0.0;
			for (StatisticsTimePerformanceIndicator indicator: stat) {
				maxMeanClientsAtStationQueuesCache=Math.max(maxMeanClientsAtStationQueuesCache,indicator.getTimeMean());
			}
		}

		return maxMeanClientsAtStationQueuesCache;
	}

	/**
	 * Liefert den Maximalwert über die mittleren Anzahlen Kunden in Bedienung an allen Stationen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximalwert über die mittleren Anzahlen Kunden in Bedienung an allen Stationen
	 */
	private double maxMeanClientsAtStationProcess(final Statistics statistics) {
		if (maxMeanClientsAtStationProcessCache<0.0) {
			final StatisticsTimePerformanceIndicator[] stat=statistics.clientsAtStationProcessByStation.getAll(StatisticsTimePerformanceIndicator.class);
			maxMeanClientsAtStationProcessCache=0.0;
			for (StatisticsTimePerformanceIndicator indicator: stat) {
				maxMeanClientsAtStationProcessCache=Math.max(maxMeanClientsAtStationProcessCache,indicator.getTimeMean());
			}
		}

		return maxMeanClientsAtStationProcessCache;
	}

	/**
	 * Liefert den Maximalwert über die maximalen Anzahlen an Kunden an allen Stationen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximalwert über die mittleren Anzahlen an Kunden an allen Stationen
	 */
	private double maxClientsAtStations(final Statistics statistics) {
		if (maxClientsAtStationsCache<0.0) {
			final StatisticsTimePerformanceIndicator[] stat=statistics.clientsAtStationByStation.getAll(StatisticsTimePerformanceIndicator.class);
			maxClientsAtStationsCache=0.0;
			for (StatisticsTimePerformanceIndicator indicator: stat) {
				maxClientsAtStationsCache=Math.max(maxClientsAtStationsCache,indicator.getTimeMax());
			}
		}

		return maxClientsAtStationsCache;
	}

	/**
	 * Liefert den Maximalwert über die maximalen  Anzahlen an wartenden Kunden an allen Stationen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximalwert über die mittleren Anzahlen an wartenden Kunden an allen Stationen
	 */
	private double maxClientsAtStationQueues(final Statistics statistics) {
		if (maxClientsAtStationQueuesCache<0.0) {
			final StatisticsTimePerformanceIndicator[] stat=statistics.clientsAtStationQueueByStation.getAll(StatisticsTimePerformanceIndicator.class);
			maxClientsAtStationQueuesCache=0.0;
			for (StatisticsTimePerformanceIndicator indicator: stat) {
				maxClientsAtStationQueuesCache=Math.max(maxClientsAtStationQueuesCache,indicator.getTimeMax());
			}
		}

		return maxClientsAtStationQueuesCache;
	}

	/**
	 * Liefert den Maximalwert über die maximalen Anzahlen an Kunden in Bedienung an allen Stationen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximalwert über die maximalen Anzahlen an Kunden in Bedienung an allen Stationen
	 */
	private double maxClientsAtStationProcess(final Statistics statistics) {
		if (maxClientsAtStationProcessCache<0.0) {
			final StatisticsTimePerformanceIndicator[] stat=statistics.clientsAtStationProcessByStation.getAll(StatisticsTimePerformanceIndicator.class);
			maxClientsAtStationProcessCache=0.0;
			for (StatisticsTimePerformanceIndicator indicator: stat) {
				maxClientsAtStationProcessCache=Math.max(maxClientsAtStationProcessCache,indicator.getTimeMax());
			}
		}

		return maxClientsAtStationProcessCache;
	}

	/**
	 * Liefert den maximalen Flussgrad über alle Stationen.
	 * @param statistics		Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximaler Flussgrad über alle Stationen
	 */
	private double maxFlowFactor(final Statistics statistics) {
		if (maxFlowFactorCache<0.0) {
			maxFlowFactorCache=0.0;
			for (String station: statistics.stationsProcessingTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator1=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.get(station));
				final StatisticsDataPerformanceIndicator indicator2=((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.get(station));
				final double time1=indicator1.getMean();
				final double time2=indicator2.getMean();
				if (time2>0) maxFlowFactorCache=Math.max(maxFlowFactorCache,time1/time2);
			}
		}

		return maxFlowFactorCache;
	}

	/**
	 * Liefert die maximale Anzahl an Kundenankünften über alle Stationen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximale Anzahl an Kundenankünften über alle Stationen
	 */
	private double maxClientsCount(final Statistics statistics) {
		if (maxClientsCountCache<0) {
			final StatisticsDataPerformanceIndicator[] stat=statistics.stationsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class);
			maxClientsCountCache=0;
			for (StatisticsDataPerformanceIndicator indicator: stat) {
				maxClientsCountCache=Math.max(maxClientsCountCache,indicator.getCount());
			}
		}

		return maxClientsCountCache;
	}

	/**
	 * Welche Werte sollen zur Berechnung der Heatmap herangezogen werden?
	 * @see EditorPanelStatistics#getHeatMapIntensity(Statistics, ModelElementBox, HeatMapMode)
	 */
	public enum HeatMapMode {
		/** Keine Heatmap-Daten darstellen */
		OFF("Off",()->Language.tr("Main.Menu.View.Statistics.HeatMap.Off"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.Off.Mnemonic")),
		/** Mittlere Anzahl an Kunden an der Station */
		WIP_AVG("WIP",()->Language.tr("Main.Menu.View.Statistics.HeatMap.WipAvg"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.WipAvg.Mnemonic")),
		/** Mittlere Anzahl an wartenden Kunden an der Station */
		NQ_AVG("NQ",()->Language.tr("Main.Menu.View.Statistics.HeatMap.NqAvg"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.NqAvg.Mnemonic")),
		/** Mittlere Anzahl an Kunden in Bedienung an der Station */
		NS_AVG("NS",()->Language.tr("Main.Menu.View.Statistics.HeatMap.NsAvg"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.NsAvg.Mnemonic")),
		/** Maximale Anzahl an Kunden an der Station */
		WIP_MAX("WIPMax",()->Language.tr("Main.Menu.View.Statistics.HeatMap.WipMax"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.WipMax.Mnemonic")),
		/** Maximale Anzahl an wartenden Kunden an der Station */
		NQ_MAX("NQMax",()->Language.tr("Main.Menu.View.Statistics.HeatMap.NqMax"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.NqMax.Mnemonic")),
		/** Maximale Anzahl an Kunden in Bedienung an der Station */
		NS_MAX("NSMax",()->Language.tr("Main.Menu.View.Statistics.HeatMap.NsMax"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.NsMax.Mnemonic")),
		/** Anzahl an Ankünften an der Station */
		ARRIVALS("Arrivals",()->Language.tr("Main.Menu.View.Statistics.HeatMap.Arrivals"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.Arrivals.Mnemonic")),
		/** Mittlere Wartezeit an der Station */
		WAITING_TIME_AVG("WaitingTime",()->Language.tr("Main.Menu.View.Statistics.HeatMap.Waiting"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.Waiting.Mnemonic")),
		/** Mittlere Transportzeit an der Station */
		TRANSFER_TIME_AVG("TransferTime",()->Language.tr("Main.Menu.View.Statistics.HeatMap.Transfer"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.Transfer.Mnemonic")),
		/** Mittlere Bedienzeit an der Station */
		PROCESS_TIME_AVG("ProcessTime",()->Language.tr("Main.Menu.View.Statistics.HeatMap.Process"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.Process.Mnemonic")),
		/** Mittlere Verweilzeit an der Station */
		RESIDENCE_TIME_AVG("ResidenceTime",()->Language.tr("Main.Menu.View.Statistics.HeatMap.Residence"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.Residence.Mnemonic")),
		/** Flussgrade an den Stationen */
		FLOW_FACTOR("FlowFactor",()->Language.tr("Main.Menu.View.Statistics.HeatMap.FlowFactor"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.FlowFactor.Mnemonic")),
		/** Durchsatz an den Stationen (= "Anzahl an Ankünften an der Station") */
		THROUGHPUT("Throughput",()->Language.tr("Main.Menu.View.Statistics.HeatMap.Throughput"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.Throughput.Mnemonic"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.ThroughputTooltip")),
		/** Maximaler Durchsatz an den Stationen */
		MAX_THROUGHPUT("MaxThroughput",()->Language.tr("Main.Menu.View.Statistics.HeatMap.MaxThroughput"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.MaxThroughput.Mnemonic"));

		/**
		 * Callback welches den Namen des Heatmap-Modus liefert
		 */
		private final Supplier<String> name;

		/**
		 * Optionales Callback welches einen Tooltip für den Heatmap-Modus liefert
		 */
		private final Supplier<String> tooltip;

		/**
		 * Callback welches den Mnemonic-Wert für den Namen des Heatmap-Modus liefert
		 */
		private final Supplier<String> nameMnemonic;

		/**
		 * Liefert den Namen des Heatmap-Modus.
		 * @return	Name des Heatmap-Modus
		 */
		public String getName() {
			return name.get();
		}

		/**
		 * Liefert optional einen Tooltip für den Heatmap-Modus.
		 * @return	Tooltip des Heatmap-Modus (kann <code>null</code> sein)
		 */
		public String getTooltip() {
			if (tooltip==null) return null;
			return tooltip.get();
		}

		/**
		 * Liefert den Mnemonic-Wert für den Namen des Heatmap-Modus.
		 * @return	Mnemonic-Wert für den Namen des Heatmap-Modus
		 */
		public String getNameMnemonic() {
			return nameMnemonic.get();
		}

		/**
		 * XML-Bezeichner für den Modus
		 */
		public final String xmlName;

		/**
		 * Konstruktor des Enum
		 * @param xmlName	XML-Bezeichner für den Modus
		 * @param name	Callback welches den Namen des Heatmap-Modus liefert
		 * @param nameMnemonic	Callback welches den Mnemonic-Wert für den Namen des Heatmap-Modus liefert
		 * @param tooltip	Optionales Callback für einen Tooltip für den Heatmap-Modus
		 */
		HeatMapMode(final String xmlName, final Supplier<String> name, final Supplier<String> nameMnemonic, final Supplier<String> tooltip) {
			this.xmlName=xmlName;
			this.name=name;
			this.nameMnemonic=nameMnemonic;
			this.tooltip=tooltip;
		}

		/**
		 * Konstruktor des Enum
		 * @param xmlName	XML-Bezeichner für den Modus
		 * @param name	Callback welches den Namen des Heatmap-Modus liefert
		 * @param nameMnemonic	Callback welches den Mnemonic-Wert für den Namen des Heatmap-Modus liefert
		 */
		HeatMapMode(final String xmlName, final Supplier<String> name, final Supplier<String> nameMnemonic) {
			this(xmlName,name,nameMnemonic,null);
		}

		/**
		 * Ermittelt auf Basis eines XML-Bezeichners den Modus
		 * @param xmlName	XML-Bezeichner
		 * @return	Heatmap-Modus (kann <code>null</code> sein, das ist auch ein gültiger Modus)
		 */
		public static HeatMapMode fromName(final String xmlName) {
			for (HeatMapMode mode: values()) if (mode.xmlName.equalsIgnoreCase(xmlName)) return mode;
			return OFF;
		}
	}

	/**
	 * Liefert, sofern verfügbar, die HeatMap-Intensität für eine bestimmte Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station für die die Intensität zurückgegeben werden sollen
	 * @param mode	Welche Werte sollen zur Berechnung der Heatmap herangezogen werden?
	 * @return	Intensität (kann <code>null</code> sein, wenn keine Daten verfügbar sind)
	 */
	public Double getHeatMapIntensity(final Statistics statistics, final ModelElementBox element, final HeatMapMode mode) {
		if (mode==null || mode==HeatMapMode.OFF) return null;

		/* Ist das noch dasselbe Statistikobjekt wie beim letzten Aufruf? Wenn nein, Caches löschen */
		testStatistics(statistics);

		final String nameStation=elementStatisticsName(element);
		if (nameStation==null) return null;

		StatisticsTimePerformanceIndicator number;
		StatisticsDataPerformanceIndicator time;
		StatisticsDataPerformanceIndicator time1, time2;
		StatisticsSimpleValueMaxPerformanceIndicator maxThroughput;
		double all;

		switch (mode) {
		case OFF:
			return null;
		case WIP_AVG:
			number=(StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.getOrNull(nameStation);
			if (number==null) return 0.0;
			return number.getTimeMean()/maxMeanClientsAtStations(statistics);
		case NQ_AVG:
			number=(StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByStation.getOrNull(nameStation);
			if (number==null) return 0.0;
			return number.getTimeMean()/maxMeanClientsAtStationQueues(statistics);
		case NS_AVG:
			number=(StatisticsTimePerformanceIndicator)statistics.clientsAtStationProcessByStation.getOrNull(nameStation);
			if (number==null) return 0.0;
			return number.getTimeMean()/maxMeanClientsAtStationProcess(statistics);
		case WIP_MAX:
			number=(StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.getOrNull(nameStation);
			if (number==null) return 0.0;
			return number.getTimeMax()/maxClientsAtStations(statistics);
		case NQ_MAX:
			number=(StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByStation.getOrNull(nameStation);
			if (number==null) return 0.0;
			return number.getTimeMax()/maxClientsAtStationQueues(statistics);
		case NS_MAX:
			number=(StatisticsTimePerformanceIndicator)statistics.clientsAtStationProcessByStation.getOrNull(nameStation);
			if (number==null) return 0.0;
			return number.getTimeMax()/maxClientsAtStationProcess(statistics);
		case ARRIVALS:
			time=(StatisticsDataPerformanceIndicator)statistics.stationsInterarrivalTime.getOrNull(nameStation);
			if (time==null) return 0.0;
			all=maxClientsCount(statistics);
			return (all==0.0)?0.0:(time.getCount()/all);
		case WAITING_TIME_AVG:
			time=(StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.getOrNull(nameStation);
			if (time==null) return 0.0;
			all=statistics.clientsAllWaitingTimes.getMean();
			return (all==0.0)?0.0:(time.getMean()/all);
		case TRANSFER_TIME_AVG:
			time=(StatisticsDataPerformanceIndicator)statistics.stationsTransferTimes.getOrNull(nameStation);
			if (time==null) return 0.0;
			all=statistics.clientsAllTransferTimes.getMean();
			return (all==0.0)?0.0:(time.getMean()/all);
		case PROCESS_TIME_AVG:
			time=(StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.getOrNull(nameStation);
			if (time==null) return 0.0;
			all=statistics.clientsAllProcessingTimes.getMean();
			return (all==0.0)?0.0:(time.getMean()/all);
		case RESIDENCE_TIME_AVG:
			time=(StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.getOrNull(nameStation);
			if (time==null) return 0.0;
			all=statistics.clientsAllResidenceTimes.getMean();
			return (all==0.0)?0.0:(time.getMean()/all);
		case FLOW_FACTOR:
			time1=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.get(nameStation));
			time2=((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.get(nameStation));
			if (time1==null || time2==null) return 0.0;
			if (time2.getMean()==0.0) return 0.0;
			final double flowfactor=time1.getMean()/time2.getMean();
			return flowfactor/maxFlowFactor(statistics);
		case THROUGHPUT:
			/* Identisch zu ARRIVALS */
			time=(StatisticsDataPerformanceIndicator)statistics.stationsInterarrivalTime.getOrNull(nameStation);
			if (time==null) return 0.0;
			all=maxClientsCount(statistics);
			return (all==0.0)?0.0:(time.getCount()/all);
		case MAX_THROUGHPUT:
			maxThroughput=(StatisticsSimpleValueMaxPerformanceIndicator)statistics.stationsMaxThroughput.getOrNull(nameStation);
			double max=0.0;
			for (StatisticsPerformanceIndicator record: statistics.stationsMaxThroughput.getAll()) max=Math.max(max,((StatisticsSimpleValueMaxPerformanceIndicator)record).get());
			if (maxThroughput==null || maxThroughput.get()<=0.0) return 0.0;
			return maxThroughput.get()/max;
		}

		return null;
	}

	/**
	 * Liefert, sofern verfügbar, die HeatMap-Intensität für einen bestimmten Stationsübergang.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element1	Startstation
	 * @param element2	Zielstation
	 * @param mode	Welche Werte sollen zur Berechnung der Heatmap herangezogen werden?
	 * @return	Intensität (kann <code>null</code> sein, wenn keine Daten verfügbar sind)
	 */
	public Double getHeatMapIntensity(final Statistics statistics, final ModelElementBox element1, final ModelElementBox element2, final HeatMapMode mode) {
		if (mode==null || mode==HeatMapMode.OFF) return null;

		/* Ist das noch dasselbe Statistikobjekt wie beim letzten Aufruf? Wenn nein, Caches löschen */
		testStatistics(statistics);

		final String nameStation1=elementStatisticsName(element1);
		if (nameStation1==null) return null;
		final String nameStation2=elementStatisticsName(element2);
		if (nameStation2==null) return null;

		StatisticsSimpleCountPerformanceIndicator counter;
		double max;

		switch (mode) {
		case OFF:
			return null;
		case THROUGHPUT:
			counter=(StatisticsSimpleCountPerformanceIndicator)statistics.stationTransition.getOrNull(nameStation1+" -> "+nameStation2);
			if (counter==null) return null;
			max=0;
			for (StatisticsPerformanceIndicator indicator: statistics.stationTransition.getAll()) max=Math.max(max,((StatisticsSimpleCountPerformanceIndicator)indicator).get());
			return counter.get()/max;
		default:
			return null;
		}
	}

	/**
	 * Bestimmt die Ausgangs- und die Zielstation einer Kante.
	 * @param edge	Kante zu der Start- und Zielpunkt (ggf. über mehrere Ecken) gesucht werden sollen
	 * @return	Liefert im Erfolgsfall ein 2-elementiges Array, sonst <code>null</code>
	 */
	public static ModelElementBox[] boxesFromEdge(ModelElementEdge edge) {
		ModelElementEdge currentEdge;

		/* Quelle */
		ModelElementBox source;
		currentEdge=edge;
		while (true) {
			final ModelElement e=currentEdge.getConnectionStart();
			if (e instanceof ModelElementBox) {
				source=(ModelElementBox)e;
				break;
			}
			if (e instanceof ModelElementVertex) {
				final ModelElementEdge[] edges=((ModelElementVertex)e).getEdgesIn();
				if (edges.length!=1 || edges[0]==null) return null;
				currentEdge=edges[0];
				continue;
			}
			return null;
		}

		/* Ziel */
		ModelElementBox destination;
		currentEdge=edge;
		while (true) {
			final ModelElement e=currentEdge.getConnectionEnd();
			if (e instanceof ModelElementBox) {
				destination=(ModelElementBox)e;
				break;
			}
			if (e instanceof ModelElementVertex) {
				currentEdge=((ModelElementVertex)e).getEdgeOut();
				if (currentEdge==null) return null;
				continue;
			}
			return null;
		}

		return new ModelElementBox[] {source,destination};
	}
}
