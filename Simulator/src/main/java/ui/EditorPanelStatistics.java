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
import statistics.StatisticsQuotientPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementAssign;
import ui.modeleditor.elements.ModelElementBatch;
import ui.modeleditor.elements.ModelElementCounter;
import ui.modeleditor.elements.ModelElementCounterBatch;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementDifferentialCounter;
import ui.modeleditor.elements.ModelElementMatch;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementThroughput;
import ui.modeleditor.elements.ModelElementTransportSource;
import ui.modeleditor.elements.ModelElementTransportTransporterSource;
import ui.statistics.StatisticTools;

/**
 * Diese Helferklasse erm�glicht es, Statistikdaten f�r einzlene
 * Editor-Modell-Stationen zu bestimmen und daraus Daten f�r die
 * Editor-Modellansicht (Tooltips, Heatmap-Intensit�ten) zu generieren.
 * @author Alexander Herzog
 * @see EditorPanel
 */
public class EditorPanelStatistics {
	/**
	 * Zuordnung der bereits generierten Tooltips,
	 * um diese nicht st�ndig erneut anlegen zu m�ssen.
	 * @see #getTooltip(Statistics, ModelElementBox)
	 */
	private final Map<Integer,String> tooltipsCache;

	/**
	 * Zuordnung der bereits generierten Stations-Statistik-Namen,
	 * um diese nicht st�ndig erneut anlegen zu m�ssen.
	 * @see #elementStatisticsName(ModelElementBox)
	 */
	private final Map<Integer,String> nameCache;

	/**
	 * Maximalwert �ber die mittleren Anzahlen an Kunden an allen Stationen
	 * @see #maxMeanClientsAtStations(Statistics)
	 */
	private double maxMeanClientsAtStationsCache;

	/**
	 * Maximalwert �ber die mittleren Anzahlen an wartenden Kunden an allen Stationen
	 * @see #maxMeanClientsAtStationQueues(Statistics)
	 */
	private double maxMeanClientsAtStationQueuesCache;

	/**
	 * Maximalwert �ber die maximalen Anzahlen an Kunden an allen Stationen
	 * @see #maxMeanClientsAtStations(Statistics)
	 */
	private double maxClientsAtStationsCache;

	/**
	 * Maximalwert �ber die maximalen Anzahlen an wartenden Kunden an allen Stationen
	 * @see #maxMeanClientsAtStationQueues(Statistics)
	 */
	private double maxClientsAtStationQueuesCache;

	/**
	 * Maximaler Flussgrad �ber alle Stationen
	 * @see #maxFlowFactor(Statistics)
	 */
	private double maxFlowFactorCache;

	/**
	 * Maximale Anzahl an Kundenank�nften �ber alle Stationen
	 * @see #maxClientsCountCache
	 */
	private long maxClientsCountCache;

	/**
	 * Name des Modells
	 * (um zu pr�fen, ob die gecachten Statistikdaten weiter verwendet
	 * werden k�nnen, ohne daf�r das Statistikobjekt selbst referenziert
	 * halten zu m�ssen)
	 * @see #testStatistics(Statistics)
	 */
	private String lastModelName;

	/**
	 * Zeitpunkt der Ausf�hrung der Simulation
	 * (um zu pr�fen, ob die gecachten Statistikdaten weiter verwendet
	 * werden k�nnen, ohne daf�r das Statistikobjekt selbst referenziert
	 * halten zu m�ssen)
	 * @see #testStatistics(Statistics)
	 */
	private String lastRunDate;

	/**
	 * Laufzeit der Simulation
	 * (um zu pr�fen, ob die gecachten Statistikdaten weiter verwendet
	 * werden k�nnen, ohne daf�r das Statistikobjekt selbst referenziert
	 * halten zu m�ssen)
	 * @see #testStatistics(Statistics)
	 */
	private long lastRunTime;

	/**
	 * Anzahl der Ereignisse w�hrend der Simulation
	 * (um zu pr�fen, ob die gecachten Statistikdaten weiter verwendet
	 * werden k�nnen, ohne daf�r das Statistikobjekt selbst referenziert
	 * halten zu m�ssen)
	 * @see #testStatistics(Statistics)
	 */
	private long lastRunEvents;

	/**
	 * Anzahl der Wiederholungen der Simulation
	 * (um zu pr�fen, ob die gecachten Statistikdaten weiter verwendet
	 * werden k�nnen, ohne daf�r das Statistikobjekt selbst referenziert
	 * halten zu m�ssen)
	 * @see #testStatistics(Statistics)
	 */
	private long lastRepeatCount;

	/**
	 * Konstruktor der Klasse
	 */
	public EditorPanelStatistics() {
		tooltipsCache=new HashMap<>();
		nameCache=new HashMap<>();
		clearCache();
	}

	/**
	 * L�scht den Cache der bereits generierten Daten
	 * (wenn ein neues Statistikobjekt verwendet werden soll).
	 * @see #testStatistics(Statistics)
	 */
	private void clearCache() {
		tooltipsCache.clear();
		nameCache.clear();
		maxMeanClientsAtStationsCache=-1;
		maxMeanClientsAtStationQueuesCache=-1;
		maxClientsAtStationsCache=-1;
		maxClientsAtStationQueuesCache=-1;
		maxFlowFactorCache=-1;
		maxClientsCountCache=-1;
	}

	/**
	 * Pr�ft, ob sich die Daten im Cache noch auf das �bergegebene
	 * Statistikobjekt beziehen und setzt wenn nicht den Cache zur�ck.
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
	 * Ermittelt den f�r Stationen, bei denen dies nicht �ber den Laufzeitstationen-Katalog m�glich ist,
	 * den Namen einer Station in der Formatierung, wie er f�r die Statistikaufzeichnung verwendet wird.
	 * @param element	Editor-Modell-Station f�r die der Statistik-Name ermittelt werden soll
	 * @return	Statistik-Name der Station
	 * @see #elementStatisticsName(ModelElementBox)
	 */
	private String elementStatisticsNameSpecial(final ModelElementBox element) {
		if (element instanceof ModelElementDecide) {
			final ModelElementDecide decide=(ModelElementDecide)element;
			switch (decide.getMode()) {
			case MODE_CHANCE: return new RunElementDecideByChance(decide).name;
			case MODE_CLIENTTYPE: return new RunElementDecideByClientType(decide).name;
			case MODE_CONDITION: return new RunElementDecideByCondition(decide).name;
			case MODE_KEY_VALUE: return new RunElementDecideByKeyValue(decide).name;
			case MODE_MIN_CLIENTS_NEXT_STATION: return new RunElementDecideByStation(decide).name;
			case MODE_MIN_CLIENTS_PROCESS_STATION: return new RunElementDecideByStation(decide).name;
			case MODE_SEQUENCE: return new RunElementDecideBySequence(decide).name;
			case MODE_SHORTEST_QUEUE_NEXT_STATION: return new RunElementDecideByStation(decide).name;
			case MODE_SHORTEST_QUEUE_PROCESS_STATION: return new RunElementDecideByStation(decide).name;
			}
		}

		return null;
	}

	/**
	 * Ermittelt den Namen einer Station in der Formatierung, wie er f�r die Statistikaufzeichnung verwendet wird.
	 * @param element	Editor-Modell-Station f�r die der Statistik-Name ermittelt werden soll
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
		sb.append("<i><span style='color: green;'>");
		sb.append(Language.tr("Main.Toolbar.ShowStatistics"));
		sb.append(":<br>");
		sb.append(String.join("<br>",lines));
		sb.append("</span></i>");
		return sb.toString();
	}

	/**
	 * Setzt aus mehreren Zeilen eine html-formatierte Statistikausgabe zusammen
	 * @param lines	Eingabezeilen
	 * @return	html-formatierte Statistikausgabe
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String formatStatisticsData(final List<String> lines) {
		return formatStatisticsData(lines.toArray(new String[0]));
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
		return TimeTools.formatExactTime(time)+" ("+StatisticTools.formatNumber(time)+")";
	}

	/**
	 * Tr�gt, wenn vorhanden, Daten zu einer Station oder einem Kundentyp (Zeiten und Anzahlen) in eine Ausgabe ein.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param name	Name der Station oder des Kundentyps
	 * @param lines	Ausgabeobjekt
	 * @param nameIsClientType	Handelt es sich bei dem Namen um einen Kundentyp?
	 */
	private void addFullInformation(final Statistics statistics, final String name, final List<String> lines, final boolean nameIsClientType) {
		final StatisticsDataPerformanceIndicator waiting;
		final StatisticsDataPerformanceIndicator transfer;
		final StatisticsDataPerformanceIndicator process;
		final StatisticsDataPerformanceIndicator residence;
		final StatisticsTimePerformanceIndicator wip;
		final StatisticsTimePerformanceIndicator nq;
		if (nameIsClientType) {
			waiting=((StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.getOrNull(name));
			transfer=((StatisticsDataPerformanceIndicator)statistics.clientsTransferTimes.getOrNull(name));
			process=((StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.getOrNull(name));
			residence=((StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.getOrNull(name));
			wip=((StatisticsTimePerformanceIndicator)statistics.clientsInSystemByClient.getOrNull(name));
			nq=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByClient.getOrNull(name));
		} else {
			waiting=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.getOrNull(name));
			transfer=((StatisticsDataPerformanceIndicator)statistics.stationsTransferTimes.getOrNull(name));
			process=((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.getOrNull(name));
			residence=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.getOrNull(name));
			wip=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.getOrNull(name));
			nq=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByStation.getOrNull(name));
		}

		if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
		if (transfer!=null && transfer.getMean()>0) lines.add("E[T]="+formatTime(transfer.getMean()));
		if (process!=null && process.getMean()>0) lines.add("E[S]="+formatTime(process.getMean()));
		if (residence!=null && residence.getMean()>0) lines.add("E[V]="+formatTime(residence.getMean()));
		if (nq!=null && nq.getTimeMean()>0) lines.add("E[NQ]="+StatisticTools.formatNumber(nq.getTimeMean()));
		if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]="+StatisticTools.formatNumber(wip.getTimeMean()));
	}

	/**
	 * Tr�gt, wenn vorhanden, Daten zu einer Station (Zeiten) in eine Ausgabe ein.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param name	Name der Station
	 * @param lines	Ausgabeobjekt
	 */
	private void addTimeInformation(final Statistics statistics, final String name, final List<String> lines) {
		final StatisticsDataPerformanceIndicator waiting=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.getOrNull(name));
		final StatisticsDataPerformanceIndicator transfer=((StatisticsDataPerformanceIndicator)statistics.stationsTransferTimes.getOrNull(name));
		final StatisticsDataPerformanceIndicator process=((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.getOrNull(name));
		final StatisticsDataPerformanceIndicator residence=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.getOrNull(name));
		final StatisticsTimePerformanceIndicator wip=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.getOrNull(name));

		if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
		if (transfer!=null && transfer.getMean()>0) lines.add("E[T]="+formatTime(transfer.getMean()));
		if (process!=null && process.getMean()>0) lines.add("E[S]="+formatTime(process.getMean()));
		if (residence!=null && residence.getMean()>0) lines.add("E[V]="+formatTime(residence.getMean()));
		if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]="+StatisticTools.formatNumber(wip.getTimeMean()));
	}

	/**
	 * Generiert den Tooltip-Text f�r eine Kundenquelle-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station f�r die die Statistikdaten zur�ckgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verf�gung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipSource(final Statistics statistics, final ModelElementBox element) {
		final String nameStation=elementStatisticsName(element);
		final String nameClient=element.getName();

		final StatisticsDataPerformanceIndicator inter=((StatisticsDataPerformanceIndicator)statistics.clientsInterarrivalTime.getOrNull(nameStation));

		final List<String> lines=new ArrayList<>();
		if (inter!=null) lines.add("E[I]="+formatTime(inter.getMean()));
		lines.add(nameClient);
		addFullInformation(statistics,nameClient,lines,true);

		return formatStatisticsData(lines);
	}

	/**
	 * Generiert den Tooltip-Text f�r eine Zuweisung-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station f�r die die Statistikdaten zur�ckgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verf�gung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipAssign(final Statistics statistics, final ModelElementBox element) {
		final String nameClient=element.getName();

		final List<String> lines=new ArrayList<>();
		lines.add(nameClient);
		addFullInformation(statistics,nameClient,lines,true);

		return formatStatisticsData(lines);
	}

	/**
	 * Generiert den Tooltip-Text f�r eine Station, bei der alle Arten von Zeiten auftreten k�nnen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station f�r die die Statistikdaten zur�ckgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verf�gung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipAllTimes(final Statistics statistics, final ModelElementBox element) {
		final String nameStation=elementStatisticsName(element);

		final List<String> lines=new ArrayList<>();
		addFullInformation(statistics,nameStation,lines,false);

		return formatStatisticsData(lines);
	}

	/**
	 * Generiert den Tooltip-Text f�r eine Station, bei der nur Wartezeiten auftreten k�nnen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station f�r die die Statistikdaten zur�ckgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verf�gung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipWaiting(final Statistics statistics, final ModelElementBox element) {
		final String nameStation=elementStatisticsName(element);

		final List<String> lines=new ArrayList<>();
		addTimeInformation(statistics,nameStation,lines);

		return formatStatisticsData(lines);
	}

	/**
	 * Generiert den Tooltip-Text f�r eine Z�hler-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station f�r die die Statistikdaten zur�ckgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verf�gung stehen
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
		return formatStatisticsData(NumberTools.formatLong(value)+" ("+StatisticTools.formatPercent(((double)value)/sum)+")");
	}

	/**
	 * Generiert den Tooltip-Text f�r eine Batch-Z�hler-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station f�r die die Statistikdaten zur�ckgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verf�gung stehen
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
	 * Generiert den Tooltip-Text f�r eine Differenzz�hler-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station f�r die die Statistikdaten zur�ckgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verf�gung stehen
	 * @see #buildTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltipDifferentialCounter(final Statistics statistics, final ModelElementBox element) {
		final StatisticsTimePerformanceIndicator counter=(StatisticsTimePerformanceIndicator)statistics.differentialCounter.getOrNull(element.getName());
		if (counter==null) return null;
		return formatStatisticsData(Language.tr("Statistics.Average")+"="+StatisticTools.formatNumber(counter.getTimeMean()));
	}

	/**
	 * Generiert den Tooltip-Text f�r eine Durchsatzmess-Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station f�r die die Statistikdaten zur�ckgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verf�gung stehen
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
		return formatStatisticsData(Language.tr("Statistics.Throughput")+" "+element.getName()+": "+StatisticTools.formatNumber(value,2)+" (1/"+unit+")");
	}

	/**
	 * Generiert den Tooltip-Text f�r eine Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station f�r die die Statistikdaten zur�ckgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verf�gung stehen
	 * @see #getTooltip(Statistics, ModelElementBox)
	 */
	private String buildTooltip(final Statistics statistics, final ModelElementBox element) {
		if (element instanceof ModelElementSource) return buildTooltipSource(statistics,element);
		if (element instanceof ModelElementAssign) return buildTooltipAssign(statistics,element);
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
	 * Liefert, sofern verf�gbar, Statistikdaten, die sich auf eine bestimmte
	 * Station beziehen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station f�r die die Statistikdaten zur�ckgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazu zur Verf�gung stehen
	 */
	public String getTooltip(final Statistics statistics, final ModelElementBox element) {
		/* Statistikdaten verf�gbar? */
		if (statistics==null) return null;

		/* Ist das noch dasselbe Statistikobjekt wie beim letzten Aufruf? Wenn nein, Caches l�schen */
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
	 * Liefert den Maximalwert �ber die mittleren Anzahlen an Kunden an allen Stationen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximalwert �ber die mittleren Anzahlen an Kunden an allen Stationen
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
	 * Liefert den Maximalwert �ber die mittleren Anzahlen an wartenden Kunden an allen Stationen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximalwert �ber die mittleren Anzahlen an wartenden Kunden an allen Stationen
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
	 * Liefert den Maximalwert �ber die maximalen Anzahlen an Kunden an allen Stationen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximalwert �ber die mittleren Anzahlen an Kunden an allen Stationen
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
	 * Liefert den Maximalwert �ber die maximalen  Anzahlen an wartenden Kunden an allen Stationen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximalwert �ber die mittleren Anzahlen an wartenden Kunden an allen Stationen
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
	 * Liefert den maximalen Flussgrad �ber alle Stationen.
	 * @param statistics		Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximaler Flussgrad �ber alle Stationen
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
	 * Liefert die maximale Anzahl an Kundenank�nften �ber alle Stationen.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @return	Maximale Anzahl an Kundenank�nften �ber alle Stationen
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
		/** Maximale Anzahl an Kunden an der Station */
		WIP_MAX("WIPMax",()->Language.tr("Main.Menu.View.Statistics.HeatMap.WipMax"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.WipMax.Mnemonic")),
		/** Maximale Anzahl an wartenden Kunden an der Station */
		NQ_MAX("NQMax",()->Language.tr("Main.Menu.View.Statistics.HeatMap.NqMax"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.NqMax.Mnemonic")),
		/** Anzahl an Ank�nften an der Station */
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
		FLOW_FACTOR("FlowFactor",()->Language.tr("Main.Menu.View.Statistics.HeatMap.FlowFactor"),()->Language.tr("Main.Menu.View.Statistics.HeatMap.FlowFactor.Mnemonic"));

		/**
		 * Callback welches den Namen des Heatmap-Modus liefert
		 */
		private final Supplier<String> name;

		/**
		 * Callback welches den Mnemonic-Wert f�r den Namen des Heatmap-Modus liefert
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
		 * Liefert den Mnemonic-Wert f�r den Namen des Heatmap-Modus.
		 * @return	Mnemonic-Wert f�r den Namen des Heatmap-Modus
		 */
		public String getNameMnemonic() {
			return nameMnemonic.get();
		}

		/**
		 * XML-Bezeichner f�r den Modus
		 */
		public final String xmlName;

		/**
		 * Konstruktor des Enum
		 * @param xmlName	XML-Bezeichner f�r den Modus
		 * @param name	Callback welches den Namen des Heatmap-Modus liefert
		 * @param nameMnemonic	Callback welches den Mnemonic-Wert f�r den Namen des Heatmap-Modus liefert
		 */
		HeatMapMode(final String xmlName, final Supplier<String> name, final Supplier<String> nameMnemonic) {
			this.xmlName=xmlName;
			this.name=name;
			this.nameMnemonic=nameMnemonic;
		}

		/**
		 * Ermittelt auf Basis eines XML-Bezeichners den Modus
		 * @param xmlName	XML-Bezeichner
		 * @return	Heatmap-Modus (kann <code>null</code> sein, das ist auch ein g�ltiger Modus)
		 */
		public static HeatMapMode fromName(final String xmlName) {
			for (HeatMapMode mode: values()) if (mode.xmlName.equalsIgnoreCase(xmlName)) return mode;
			return OFF;
		}
	}

	/**
	 * Liefert, sofern verf�gbar, die HeatMap-Intensit�t f�r eine bestimmte Station.
	 * @param statistics	Statistikobjekt dem die Daten entnommen werden sollen
	 * @param element	Station f�r die die Intensit�t zur�ckgegeben werden sollen
	 * @param mode	Welche Werte sollen zur Berechnung der Heatmap herangezogen werden?
	 * @return	Intensit�t (kann <code>null</code> sein, wenn keine Daten verf�gbar sind)
	 */
	public Double getHeatMapIntensity(final Statistics statistics, final ModelElementBox element, final HeatMapMode mode) {
		if (mode==null || mode==HeatMapMode.OFF) return null;

		final String nameStation=elementStatisticsName(element);
		if (nameStation==null) return null;

		StatisticsTimePerformanceIndicator number;
		StatisticsDataPerformanceIndicator time;
		StatisticsDataPerformanceIndicator time1, time2;
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
		case WIP_MAX:
			number=(StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.getOrNull(nameStation);
			if (number==null) return 0.0;
			return number.getTimeMax()/maxClientsAtStations(statistics);
		case NQ_MAX:
			number=(StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByStation.getOrNull(nameStation);
			if (number==null) return 0.0;
			return number.getTimeMax()/maxClientsAtStationQueues(statistics);
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
		}

		return null;
	}
}
