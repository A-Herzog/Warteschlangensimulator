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
package ui.statistics;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsQuotientPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import statistics.StatisticsSimpleValuePerformanceIndicator;
import statistics.StatisticsStateTimePerformanceIndicator;
import statistics.StatisticsTimeAnalogPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import statistics.StatisticsValuePerformanceIndicator;
import systemtools.statistics.StatisticViewerText;
import tools.SetupData;
import ui.help.Help;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.elements.ModelElementUserStatistic;
import ui.statistics.FastAccessSelectorBuilder.IndicatorMode;

/**
 * Dieser Viewer gibt allgemeine Informationen in Textform zu den Simulationsergebnissen aus.
 * @see StatisticViewerText
 * @author Alexander Herzog
 */
public class StatisticViewerOverviewText extends StatisticViewerText {
	private final Statistics statistics;
	private final Mode mode;
	private final Consumer<Mode> modeClick;
	private final FastAccessSelectorBuilder fastAccessBuilder;

	/**
	 * Wählt die von {@link StatisticViewerOverviewText} auszugebende Information aus.
	 * @author Alexander Herzog
	 * @see StatisticViewerOverviewText#StatisticViewerOverviewText(Statistics, Mode)
	 */
	public enum Mode {
		/** Übersichtsdaten über die wichtigsten Kenngrößen */
		MODE_OVERVIEW,
		/** "Modellüberblick" (Basisinformationen zum Modell als solches; hinterlegte Modellbeschreibung und weitere Informationen) */
		MODE_MODEL,
		/** "Stationsbeschreibung" (automatisch generierte Beschreibung aller Stationen im Modell) */
		MODE_MODEL_DESCRIPTION,
		/** Informationen zum Simulationssystem */
		MODE_SYSTEM_INFO,
		/** Zwischenankunftszeiten der Kunden am System */
		MODE_INTERARRIVAL_CLIENTS,
		/** Zwischenankunftszeiten der Kunden an den Stationen */
		MODE_INTERARRIVAL_STATIONS,
		/** Zwischenabgangszeiten der Kunden aus dem System */
		MODE_INTERLEAVE_CLIENTS,
		/** Zwischenabgangszeiten der Kunden an den Stationen */
		MODE_INTERLEAVE_STATIONS,
		/** Wartezeit auf Kundentyp-Basis */
		MODE_WAITINGPROCESSING_CLIENTS,
		/** Wartezeiten an den Stationen */
		MODE_WAITINGPROCESSING_STATIONS,
		/** Wartezeiten an den Stationen (zusätzlich nach Kundentypen ausdifferenziert)  */
		MODE_WAITINGPROCESSING_STATIONS_CLIENTS,
		/** Anzahl an Kunden im System und an den Stationen */
		MODE_CLIENTS_COUNT,
		/** Auslastung der Bedienergruppen */
		MODE_UTILIZATION,
		/** Zählerwerte */
		MODE_COUNTER,
		/** Durchsatz */
		MODE_THROUGHPUT,
		/** "Zustandsstatistik" (erfasst an speziellen Zustandsstatistik-Stationen) */
		MODE_STATE_STATISTICS,
		/** Analogwert-Statistik */
		MODE_ANALOG_STATISTICS,
		/** Kosten */
		MODE_COSTS,
		/** Autokorrelationswerte */
		MODE_AUTOCORRELATION,
		/** Benutzerdefinierte Statistik */
		MODE_USER_STATISTICS,
		/** Auslastung der Transportergruppen */
		MODE_TRANSPORTER_UTILIZATION,
		/** Statistik über die Kundendatenfelder */
		MODE_CLIENT_DATA
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @see Mode
	 */
	public StatisticViewerOverviewText(final Statistics statistics, final Mode mode) {
		this(statistics,mode,null,null);
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @param fastAccessAdd	Callback, das aufgerufen wird, wenn in dem XML-Selektor-Kontextmenü ein Eintrag angeklickt wurde
	 * @see Mode
	 */
	public StatisticViewerOverviewText(final Statistics statistics, final Mode mode, final BiConsumer<StatisticViewerFastAccess.AddXMLMode,String> fastAccessAdd) {
		this(statistics,mode,null,fastAccessAdd);
	}

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param mode	Gibt an, welche Daten genau ausgegeben werden sollen
	 * @param modeClick	Callback, das aufgerufen wird, wenn ein "Details"-Link angeklickt wurde
	 * @param fastAccessAdd	Callback, das aufgerufen wird, wenn in dem XML-Selektor-Kontextmenü ein Eintrag angeklickt wurde
	 * @see Mode
	 */
	public StatisticViewerOverviewText(final Statistics statistics, final Mode mode, final Consumer<Mode> modeClick, final BiConsumer<StatisticViewerFastAccess.AddXMLMode,String> fastAccessAdd) {
		super();
		this.statistics=statistics;
		this.mode=mode;
		this.modeClick=modeClick;
		fastAccessBuilder=new FastAccessSelectorBuilder(statistics,fastAccessAdd);
	}

	/**
	 * Liefert den im Konstruktor angegebenen Modus, welche Daten ausgegeben werden sollen.
	 * @return	Anzeige-Modus
	 * @see Mode
	 */
	public Mode getMode() {
		return mode;
	}

	private String fullStationName(final String statisticName) {
		if (statisticName==null || statisticName.trim().isEmpty()) return "";
		return statisticName;
	}

	private final static double[] DEFAULT_CONFIDENCE_LEVELS=new double[]{0.1,0.05,0.01};

	/**
	 * Liefert die Konfidenzniveaus für die auszugebenden Konfidenzintervalle.
	 * @return Konfidenzniveaus für die auszugebenden Konfidenzintervalle
	 */
	public static double[] getConfidenceLevels() {
		final String level=SetupData.getSetup().batchMeansConfidenceLevels;
		if (level==null || level.trim().isEmpty()) return DEFAULT_CONFIDENCE_LEVELS;
		final List<Double> levels=new ArrayList<>();
		for (String value: level.split(";")) {
			final Double D=NumberTools.getProbability(value);
			if (D!=null) levels.add(1-D);
		}
		if (levels.size()==0) return DEFAULT_CONFIDENCE_LEVELS;
		return levels.stream().mapToDouble(D->D.doubleValue()).toArray();
	}

	/**
	 * Liefert die Levels zu denen Quantilwerte ausgegeben werden sollen.
	 * @return	Quantilwerte ausgegeben werden sollen
	 */
	public static double[] getQuantilLevels() {
		final String level=SetupData.getSetup().quantilLevels;
		if (level==null || level.trim().isEmpty()) return StatisticsDataPerformanceIndicator.storeQuantilValues;
		final List<Double> levels=new ArrayList<>();
		for (String value: level.split(";")) {
			final Double D=NumberTools.getProbability(value);
			if (D!=null) levels.add(D);
		}
		if (levels.size()==0) return StatisticsDataPerformanceIndicator.storeQuantilValues;
		return levels.stream().mapToDouble(D->D.doubleValue()).toArray();
	}

	private void outputConfidenceData(final StatisticsDataPerformanceIndicator indicator) {
		if (indicator.getBatchCount()<1) return;

		beginParagraph();
		final double m=indicator.getMean();
		for (double level: getConfidenceLevels()) {
			final double w=indicator.getBatchMeanConfidenceHalfWide(level);
			addLine(String.format(
					Language.tr("Statistics.Confidence.Level"),
					StatisticTools.formatPercent(1-level),
					StatisticTools.formatNumber(m-w),
					StatisticTools.formatNumber(m+w),
					StatisticTools.formatNumber(w)
					));

		}
		addLine(String.format(Language.tr("Statistics.Confidence.Info"),NumberTools.formatLong(indicator.getBatchCount()),NumberTools.formatLong(indicator.getBatchSize())));
		endParagraph();
	}

	private void outputThreadConfidenceData() {
		final double mean=statistics.clientsAllWaitingTimes.getMean();
		final List<String> names=new ArrayList<>(Arrays.asList(statistics.threadBasedConfidence.getNames()));
		names.sort((s1,s2)->{
			final Double d1=NumberTools.getDouble(s1);
			final Double d2=NumberTools.getDouble(s2);
			if (d1==null || d2==null) return 0;
			if (d1>d2) return 1;
			if (d2>d1) return -1;
			return 0;
		});
		for (int i=0;i<names.size();i++) {
			if (i==0) beginParagraph();
			final String name=names.get(i);
			final double value=((StatisticsSimpleValuePerformanceIndicator)statistics.threadBasedConfidence.get(name)).get();

			addLine(String.format(Language.tr("Statistics.Confidence.Level"),name,StatisticTools.formatNumber(mean-value),StatisticTools.formatNumber(mean+value),StatisticTools.formatNumber(value)));
		}
		if (names.size()>0) {
			addLine(String.format(Language.tr("Statistics.Confidence.InfoThread"),NumberTools.formatLong(statistics.simulationData.runThreads)));
			endParagraph();
		}
	}

	private void addModeLink(final Mode mode) {
		addLink(mode.toString(),Language.tr("Statistics.Details"));
	}

	private void addDescription(final String topic) {
		final URL url=StatisticViewerOverviewText.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	@Override
	protected JPopupMenu processContextClick(final String hint) {
		return fastAccessBuilder.getPopup(hint);
	}

	private void outputEmergencyShutDownInfo(final boolean fullInfo) {
		if (!statistics.simulationData.emergencyShutDown && (statistics.simulationData.warnings==null || statistics.simulationData.warnings.length==0)) return;

		if (statistics.simulationData.emergencyShutDown) {
			addHeading(2,Language.tr("Statistics.EmergencyShutDown.Title"));
		} else {
			addHeading(2,Language.tr("Statistics.Warnings.Title"));
		}

		beginParagraph();
		if (statistics.simulationData.emergencyShutDown) {
			addLine(Language.tr("Statistics.EmergencyShutDown"));
		} else {
			if (!fullInfo && statistics.simulationData.warnings!=null) addLine(Language.tr("Statistics.Warnings.Info"));
		}
		if (fullInfo) {
			if (statistics.simulationData.warnings!=null) Arrays.asList(statistics.simulationData.warnings).stream().forEach(s->{if (s!=null) addLine(s);});
		} else {
			if (statistics.simulationData.warnings!=null && statistics.simulationData.warnings.length==1) addLine(statistics.simulationData.warnings[0]);
			addModeLink(Mode.MODE_SYSTEM_INFO);
		}
		endParagraph();
	}

	private String timeAndNumber(final double value) {
		return StatisticTools.formatExactTime(value)+" ("+StatisticTools.formatNumber(value)+")";
	}

	private String xmlMean(final StatisticsPerformanceIndicator indicator) {
		return fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MEAN);
	}

	private String xmlCount(final StatisticsPerformanceIndicator indicator) {
		return fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.COUNT);
	}


	private void buildOverview() {
		addHeading(1,Language.tr("Statistics.ResultsOverview"));

		/* Simulationsmodell */

		addHeading(2,Language.tr("Statistics.SimulationModel"));
		beginParagraph();
		if (!statistics.editModel.name.trim().isEmpty()) addLine(Language.tr("Statistics.SimulationModel.Name")+": "+statistics.editModel.name);

		long sum=0;
		for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class)) sum+=indicator.getCount();
		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";
		addLine(Language.tr("Statistics.SimulatedClients")+": "+NumberTools.formatLong(sum)+repeatInfo);
		if (statistics.simulationData.runRepeatCount>1) addLine(Language.tr("Statistics.SystemData.RepeatCount")+": "+NumberTools.formatLong(statistics.simulationData.runRepeatCount));
		if (sum==0 && statistics.editModel.warmUpTime>0) addLine(Language.tr("Statistics.SimulatedClients.Zero"));
		addModeLink(Mode.MODE_MODEL);
		endParagraph();

		outputEmergencyShutDownInfo(false);

		/*
		 * Gruppe: Mittlere Anzahl an Kunden
		 */

		addHeading(2,Language.tr("Statistics.AverageNumberOfClients"));

		/* Mittlere Anzahl an Kunden (im System / an den Stationen) */

		boolean headingWritten=false;
		for (String station: statistics.clientsAtStationByStation.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.get(station));
			final double mean=indicator.getTimeMean();
			if (mean>0) {
				if (!headingWritten) {
					addHeading(3,Language.tr("Statistics.AverageNumberOfClientsByStations")+" E[N]");
					beginParagraph();
					headingWritten=true;
					addLine(Language.tr("Statistics.ClientsInSystem")+": "+StatisticTools.formatNumber(statistics.clientsInSystem.getTimeMean()),xmlMean(statistics.clientsInSystem));
				}
				addLine(Language.tr("Statistics.ClientsAt")+" "+fullStationName(station)+": E[N]="+StatisticTools.formatNumber(mean),xmlMean(indicator));
			}
		}
		if (headingWritten) {
			addModeLink(Mode.MODE_CLIENTS_COUNT);
			endParagraph();
		} else {
			if (statistics.clientsInSystem.getTimeMean()>0) {
				addHeading(3,Language.tr("Statistics.AverageNumberOfClientsByStations")+" E[N]");
				beginParagraph();
				addLine(Language.tr("Statistics.ClientsInSystem")+": "+StatisticTools.formatNumber(statistics.clientsInSystem.getTimeMean()),xmlMean(statistics.clientsInSystem));
				addModeLink(Mode.MODE_CLIENTS_COUNT);
				endParagraph();
			}
		}

		/* Mittlere Anzahl an Kunden im System (nach Kundentypen) */

		if (statistics.clientsInSystemByClient.size()>1) {
			headingWritten=false;
			for (String clientType: statistics.clientsInSystemByClient.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=((StatisticsTimePerformanceIndicator)statistics.clientsInSystemByClient.get(clientType));
				final double mean=indicator.getTimeMean();
				if (mean>0) {
					if (!headingWritten) {
						addHeading(3,Language.tr("Statistics.AverageNumberOfClientsByClientTypes")+" E[N]");
						beginParagraph();
						headingWritten=true;
					}
					addLine(Language.tr("Statistics.ClientType")+" "+clientType+": E[N]="+StatisticTools.formatNumber(mean),xmlMean(indicator));
				}
			}
			if (headingWritten) {
				addModeLink(Mode.MODE_CLIENTS_COUNT);
				endParagraph();
			}
		}

		/* Mittlere Anzahl an Kunden in den Warteschlangen (im System / an den Stationen) */

		headingWritten=false;
		for (String station: statistics.clientsAtStationQueueByStation.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByStation.get(station));
			final double mean=indicator.getTimeMean();
			if (mean>0) {
				if (!headingWritten) {
					addHeading(3,Language.tr("Statistics.AverageNumberOfClientsInQueuesByStations")+" E[NQ]");
					beginParagraph();
					headingWritten=true;
					addLine(Language.tr("Statistics.ClientsInSystemWaiting")+": "+StatisticTools.formatNumber(statistics.clientsInSystemQueues.getTimeMean()),xmlMean(statistics.clientsInSystemQueues));
				}
				addLine(Language.tr("Statistics.ClientsInQueueAt")+" "+fullStationName(station)+": E[NQ]="+StatisticTools.formatNumber(mean),xmlMean(indicator));
			}
		}
		if (headingWritten) {
			addModeLink(Mode.MODE_CLIENTS_COUNT);
			endParagraph();
		} else {
			if (statistics.clientsInSystemQueues.getTimeMean()>0) {
				addHeading(3,Language.tr("Statistics.AverageNumberOfClientsInQueuesByStations")+" E[NQ]");
				beginParagraph();
				addLine(Language.tr("Statistics.ClientsInSystemWaiting")+": "+StatisticTools.formatNumber(statistics.clientsInSystemQueues.getTimeMean()),xmlMean(statistics.clientsInSystemQueues));
				addModeLink(Mode.MODE_CLIENTS_COUNT);
				endParagraph();
			}
		}

		/* Mittlere Anzahl an wartenden Kunden (nach Kundentypen) */

		if (statistics.clientsAtStationQueueByClient.size()>1) {
			headingWritten=false;
			for (String clientType: statistics.clientsAtStationQueueByClient.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByClient.get(clientType));
				final double mean=indicator.getTimeMean();
				if (mean>0) {
					if (!headingWritten) {
						addHeading(3,Language.tr("Statistics.AverageNumberOfWaitingClientsByClientTypes")+" E[NQ]");
						beginParagraph();
						headingWritten=true;
					}
					addLine(Language.tr("Statistics.ClientType")+" "+clientType+": E[NQ]="+StatisticTools.formatNumber(mean),xmlMean(indicator));
				}
			}
			if (headingWritten) {
				addModeLink(Mode.MODE_CLIENTS_COUNT);
				endParagraph();
			}
		}

		long count;

		/*
		 * Gruppe: Zeiten nach Kunden
		 */

		headingWritten=false;

		/* Wartezeiten nach Kundentypen */

		boolean writeBlock=false;
		for (String type: statistics.clientsWaitingTimes.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			sum=0;
			count=0;
			if (!headingWritten) {
				addHeading(2,Language.tr("Statistics.TimesByClientTypes"));
				headingWritten=true;
			}
			addHeading(3,Language.tr("Statistics.WaitingTimesByClientTypes")+" E[W]");
			beginParagraph();
			for (String type: statistics.clientsWaitingTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.get(type));
				final double time=indicator.getMean();
				sum+=time*indicator.getCount();
				count+=indicator.getCount();
				if (statistics.clientsWaitingTimes.size()>1) addLine(Language.tr("Statistics.ClientType")+" "+type+": E[W]="+timeAndNumber(time),xmlMean(indicator));
			}
			if (count==0) count=1;
			final double time=((double)sum)/count;
			addLine(Language.tr("Statistics.Average")+": E[W]="+timeAndNumber(time),xmlMean(statistics.clientsAllWaitingTimes));
			addModeLink(Mode.MODE_WAITINGPROCESSING_CLIENTS);
			endParagraph();
		}

		/* Transferzeiten nach Kundentypen */

		writeBlock=false;
		for (String type: statistics.clientsTransferTimes.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.clientsTransferTimes.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			sum=0;
			count=0;
			if (!headingWritten) {
				addHeading(2,Language.tr("Statistics.TimesByClientTypes"));
				headingWritten=true;
			}
			addHeading(3,Language.tr("Statistics.TransferTimesByClientTypes")+" E[T]");
			beginParagraph();
			for (String type: statistics.clientsTransferTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.clientsTransferTimes.get(type));
				final double time=indicator.getMean();
				sum+=time*indicator.getCount();
				count+=indicator.getCount();
				if (statistics.clientsTransferTimes.size()>1) addLine(Language.tr("Statistics.ClientType")+" "+type+": E[T]="+timeAndNumber(time),xmlMean(indicator));
			}
			if (count==0) count=1;
			final double time=((double)sum)/count;
			addLine(Language.tr("Statistics.Average")+": E[T]="+timeAndNumber(time),xmlMean(statistics.clientsAllTransferTimes));
			addModeLink(Mode.MODE_WAITINGPROCESSING_CLIENTS);
			endParagraph();
		}

		/* Bedienzeiten nach Kundentypen */

		writeBlock=false;
		for (String type: statistics.clientsProcessingTimes.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			sum=0;
			count=0;
			if (!headingWritten) {
				addHeading(2,Language.tr("Statistics.TimesByClientTypes"));
				headingWritten=true;
			}
			addHeading(3,Language.tr("Statistics.ProcessTimesByClientTypes")+" E[S]");
			beginParagraph();
			for (String type: statistics.clientsProcessingTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.get(type));
				final double time=indicator.getMean();
				sum+=time*indicator.getCount();
				count+=indicator.getCount();
				if (statistics.clientsProcessingTimes.size()>1) addLine(Language.tr("Statistics.ClientType")+" "+type+": E[S]="+timeAndNumber(time),xmlMean(indicator));
			}
			if (count==0) count=1;
			final double time=((double)sum)/count;
			addLine(Language.tr("Statistics.Average")+": E[S]="+timeAndNumber(time),xmlMean(statistics.clientsAllProcessingTimes));
			addModeLink(Mode.MODE_WAITINGPROCESSING_CLIENTS);
			endParagraph();
		}

		/* Verweilzeiten nach Kundentypen */

		writeBlock=false;
		for (String type: statistics.clientsResidenceTimes.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			sum=0;
			count=0;
			if (!headingWritten) {
				addHeading(2,Language.tr("Statistics.TimesByClientTypes"));
				headingWritten=true;
			}
			addHeading(3,Language.tr("Statistics.ResidenceTimesByClientTypes")+" E[V]");
			beginParagraph();
			for (String type: statistics.clientsResidenceTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.get(type));
				final double time=indicator.getMean();
				sum+=time*indicator.getCount();
				count+=indicator.getCount();
				if (statistics.clientsResidenceTimes.size()>1) addLine(Language.tr("Statistics.ClientType")+" "+type+": E[V]="+timeAndNumber(time),xmlMean(indicator));
			}
			if (count==0) count=1;
			final double time=((double)sum)/count;
			addLine(Language.tr("Statistics.Average")+": E[V]="+timeAndNumber(time),xmlMean(statistics.clientsAllResidenceTimes));
			addModeLink(Mode.MODE_WAITINGPROCESSING_CLIENTS);
			endParagraph();
		}

		/*
		 * Gruppe: Zeiten nach Stationen
		 */

		addHeading(2,Language.tr("Statistics.TimesByStationen"));

		/* Wartezeiten nach Stationen */

		writeBlock=false;
		for (String type: statistics.stationsWaitingTimes.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			addHeading(3,Language.tr("Statistics.WaitingTimesByStations")+" E[W]");
			beginParagraph();
			for (String station: statistics.stationsWaitingTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.get(station));
				final double time=indicator.getMean();
				addLine(fullStationName(station)+": E[W]="+timeAndNumber(time),xmlMean(indicator));
			}
			addModeLink(Mode.MODE_WAITINGPROCESSING_STATIONS);
			endParagraph();
		}

		/* Transferzeiten nach Stationen */

		writeBlock=false;
		for (String type: statistics.stationsTransferTimes.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.stationsTransferTimes.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			addHeading(3,Language.tr("Statistics.TransferTimesByStations")+ "E[T]");
			beginParagraph();
			for (String station: statistics.stationsTransferTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.stationsTransferTimes.get(station));
				final double time=indicator.getMean();
				addLine(fullStationName(station)+": E[T]="+timeAndNumber(time),xmlMean(indicator));
			}
			addModeLink(Mode.MODE_WAITINGPROCESSING_STATIONS);
			endParagraph();
		}

		/* Bedienzeiten nach Stationen */

		writeBlock=false;
		for (String type: statistics.stationsProcessingTimes.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			addHeading(3,Language.tr("Statistics.ProcessTimesByStations")+" E[S]");
			beginParagraph();
			for (String station: statistics.stationsProcessingTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.get(station));
				final double time=indicator.getMean();
				addLine(fullStationName(station)+": E[S]="+timeAndNumber(time),xmlMean(indicator));
			}
			addModeLink(Mode.MODE_WAITINGPROCESSING_STATIONS);
			endParagraph();
		}

		/* Verweilzeiten nach Stationen */

		writeBlock=false;
		for (String type: statistics.stationsResidenceTimes.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			addHeading(3,Language.tr("Statistics.ResidenceTimesByStations")+" E[V]");
			beginParagraph();
			for (String station: statistics.stationsResidenceTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.get(station));
				final double time=indicator.getMean();
				addLine(fullStationName(station)+": E[V]="+timeAndNumber(time),xmlMean(indicator));
			}
			addModeLink(Mode.MODE_WAITINGPROCESSING_STATIONS);
			endParagraph();
		}

		/*
		 * Gruppe: Zeiten nach Stationen und Kundentypen
		 */

		addHeading(2,Language.tr("Statistics.TimesByStationsAndClientTypes"));

		/* Wartezeiten nach Stationen und Kundentypen */

		writeBlock=false;
		for (String type: statistics.stationsWaitingTimesByClientType.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimesByClientType.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			addHeading(3,Language.tr("Statistics.WaitingTimesByStationsAndClients")+" E[W]");
			beginParagraph();
			for (String station: statistics.stationsWaitingTimesByClientType.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimesByClientType.get(station));
				final double time=indicator.getMean();
				addLine(fullStationName(station)+": E[W]="+timeAndNumber(time),xmlMean(indicator));
			}
			addModeLink(Mode.MODE_WAITINGPROCESSING_STATIONS);
			endParagraph();
		}

		/* Transferzeiten nach Stationen und Kundentypen */

		writeBlock=false;
		for (String type: statistics.stationsTransferTimesByClientType.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.stationsTransferTimesByClientType.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			addHeading(3,Language.tr("Statistics.TransferTimesByStationsAndClients")+ "E[T]");
			beginParagraph();
			for (String station: statistics.stationsTransferTimesByClientType.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.stationsTransferTimesByClientType.get(station));
				final double time=indicator.getMean();
				addLine(fullStationName(station)+": E[T]="+timeAndNumber(time),xmlMean(indicator));
			}
			addModeLink(Mode.MODE_WAITINGPROCESSING_STATIONS);
			endParagraph();
		}

		/* Bedienzeiten nach Stationen und Kundentypen */

		writeBlock=false;
		for (String type: statistics.stationsProcessingTimesByClientType.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimesByClientType.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			addHeading(3,Language.tr("Statistics.ProcessTimesByStationsAndClients")+" E[S]");
			beginParagraph();
			for (String station: statistics.stationsProcessingTimesByClientType.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimesByClientType.get(station));
				final double time=indicator.getMean();
				addLine(fullStationName(station)+": E[S]="+timeAndNumber(time),xmlMean(indicator));
			}
			addModeLink(Mode.MODE_WAITINGPROCESSING_STATIONS);
			endParagraph();
		}

		/* Verweilzeiten nach Stationen und Kundentypen */

		writeBlock=false;
		for (String type: statistics.stationsResidenceTimesByClientType.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimesByClientType.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			addHeading(3,Language.tr("Statistics.ResidenceTimesByStationsAndClients")+" E[V]");
			beginParagraph();
			for (String station: statistics.stationsResidenceTimesByClientType.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimesByClientType.get(station));
				final double time=indicator.getMean();
				addLine(fullStationName(station)+": E[V]="+timeAndNumber(time),xmlMean(indicator));
			}
			addModeLink(Mode.MODE_WAITINGPROCESSING_STATIONS);
			endParagraph();
		}

		/*
		 * Gruppe: Ressourcenauslastung
		 */

		if (statistics.resourceUtilization.getNames().length>0) {
			addHeading(2,Language.tr("Statistics.Utilization"));
			beginParagraph();
			for (String resource: statistics.resourceUtilization.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=((StatisticsTimePerformanceIndicator)statistics.resourceUtilization.get(resource));
				final double meanState=indicator.getTimeMean();
				final ModelResource resourceObj=statistics.editModel.resources.get(resource);
				if (resourceObj!=null && resourceObj.getMode()==ModelResource.Mode.MODE_NUMBER) {
					count=resourceObj.getCount();
					if (count>0) {
						final StatisticsTimePerformanceIndicator countIndicator=(StatisticsTimePerformanceIndicator)(statistics.resourceCount.getOrNull(resource));
						if (countIndicator==null || countIndicator.getTimeMean()<0.0001) {
							addLine(Language.tr("Statistics.Resource")+" "+resource+": "+Language.tr("Statistics.AverageNumberOfBusyOperators")+"="+StatisticTools.formatNumber(meanState,2)+" (rho="+StatisticTools.formatPercent(meanState/count)+")");
						} else {
							addLine(Language.tr("Statistics.Resource")+" "+resource+": "+Language.tr("Statistics.AverageNumberOfBusyOperators")+"="+StatisticTools.formatNumber(meanState,2)+" (rho="+StatisticTools.formatPercent(meanState/countIndicator.getTimeMean())+")");
						}
						if (resourceObj.getFailures().size()>0) {
							final StatisticsTimePerformanceIndicator indicator2=(StatisticsTimePerformanceIndicator)(statistics.resourceInDownTime.get(resource));
							addLine(Language.tr("Statistics.Resource")+" "+resource+": "+Language.tr("Statistics.FailureTime.AveragePartOfDownTimeOperators")+": "+StatisticTools.formatPercent(indicator2.getTimeMean()/count));
						}
					} else {
						addLine(Language.tr("Statistics.Resource")+" "+resource+": "+Language.tr("Statistics.AverageNumberOfBusyOperators")+"="+StatisticTools.formatNumber(meanState,2));
					}
				}
				if (resourceObj!=null && resourceObj.getMode()==ModelResource.Mode.MODE_SCHEDULE) {
					addLine(Language.tr("Statistics.Resource")+" "+resource+" ("+Language.tr("Statistics.bySchedule")+" "+resourceObj.getSchedule()+"): "+Language.tr("Statistics.AverageNumberOfBusyOperators")+"="+StatisticTools.formatNumber(meanState,2),xmlMean(indicator));
				}
			}

			addModeLink(Mode.MODE_UTILIZATION);

			endParagraph();
		}

		/*
		 * Gruppe: Transporterauslastung
		 */

		if (statistics.transporterUtilization.getNames().length>0) {
			addHeading(2,Language.tr("Statistics.TransporterUtilization"));
			beginParagraph();
			for (String transporter: statistics.transporterUtilization.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=((StatisticsTimePerformanceIndicator)statistics.transporterUtilization.get(transporter));
				final double meanState=indicator.getTimeMean();
				final ModelTransporter transporterObj=statistics.editModel.transporters.get(transporter);
				if (transporterObj!=null) {
					count=transporterObj.getCountAll();
					addLine(Language.tr("Statistics.TransporterType")+" "+transporter+": "+Language.tr("Statistics.AverageNumberOfBusyTransporters")+"="+StatisticTools.formatNumber(meanState,2)+" (rho="+StatisticTools.formatPercent(meanState/count)+")");
					if (transporterObj.getFailures().size()>0) {
						final StatisticsTimePerformanceIndicator indicator2=(StatisticsTimePerformanceIndicator)(statistics.transporterInDownTime.get(transporter));
						addLine(Language.tr("Statistics.TransporterType")+" "+transporter+": "+Language.tr("Statistics.FailureTime.AveragePartOfDownTimeTransporters")+": "+StatisticTools.formatPercent(indicator2.getTimeMean()/count));
					}
				} else {
					addLine(Language.tr("Statistics.TransporterType")+" "+transporter+": "+Language.tr("Statistics.AverageNumberOfBusyTransporters")+"="+StatisticTools.formatNumber(meanState,2),xmlMean(indicator));
				}
			}
			addModeLink(Mode.MODE_TRANSPORTER_UTILIZATION);
			endParagraph();
		}

		/*
		 * Gruppe: Zähler
		 */

		if (statistics.counter.size()>0 || statistics.differentialCounter.size()>0) {
			addHeading(2,Language.tr("Statistics.Counter"));
			buildCounterInt(3,false);
			beginParagraph();
			addModeLink(Mode.MODE_COUNTER);
			endParagraph();
		}

		/*
		 * Gruppe: Durchsatz
		 */

		if (statistics.throughputStatistics.size()>0) {
			addHeading(2,Language.tr("Statistics.Throughput"));
			buildThroughputInt();
			beginParagraph();
			addModeLink(Mode.MODE_THROUGHPUT);
			endParagraph();
		}

		/* Infotext  */
		addDescription("Overview");
	}

	@Override
	protected void processLinkClick(final String link) {
		for (Mode mode: Mode.values()) if (mode.toString().equals(link)) {
			if (modeClick!=null) modeClick.accept(mode);
			break;
		}
	}

	private void buildModelInfo() {
		addHeading(1,Language.tr("Statistics.ModelOverview"));

		addHeading(2,Language.tr("Editor.GeneralData.Name"));
		beginParagraph();
		if (statistics.editModel.name.trim().isEmpty()) addLine(Language.tr("Editor.GeneralData.Name.NoName")); else addLine(statistics.editModel.name);
		endParagraph();

		addHeading(2,Language.tr("Editor.GeneralData.Description"));
		beginParagraph();
		String s=statistics.editModel.description;
		if (s.trim().isEmpty()) addLine(Language.tr("Editor.GeneralData.Description.NoDescription")); else for (String line: s.split("\\n"))  addLine(line);
		endParagraph();

		if (statistics.editModel.useClientCount) {
			addHeading(2,Language.tr("Statistics.SimulatedClients"));
			beginParagraph();

			long sum=0;
			for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class)) sum+=indicator.getCount();
			String repeatInfo="";
			if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";
			addLine(Language.tr("Statistics.SimulatedClients")+": "+NumberTools.formatLong(sum)+repeatInfo);
			if (statistics.simulationData.runRepeatCount>1) addLine(Language.tr("Statistics.SystemData.RepeatCount")+": "+NumberTools.formatLong(statistics.simulationData.runRepeatCount));

			addLine(Language.tr("Statistics.SimulatedClients.WarmUp")+": "+NumberTools.formatLong(FastMath.round(statistics.editModel.clientCount*statistics.editModel.warmUpTime))+" ("+StatisticTools.formatPercent(statistics.editModel.warmUpTime)+")");
			endParagraph();
		}

		outputEmergencyShutDownInfo(false);
	}

	private void buildModelDescription() {
		addHeading(1,Language.tr("Statistics.ModelDescription"));

		final ModelDescriptionBuilder descriptionBuilder=new ModelDescriptionBuilder(statistics.editModel) {
			@Override
			protected void processStation(ModelElementBox station, Map<Integer, List<String[]>> properties) {
				addHeading(2,getStationName(station));
				for (int key: properties.keySet().stream().mapToInt(I->I.intValue()).sorted().toArray()) {
					for (String[] property: properties.get(key)) {
						addHeading(3,property[0]);
						beginParagraph();
						for (String line: property[1].split("\\n")) addLine(line);
						endParagraph();
					}
				}
			}
		};

		descriptionBuilder.run();
	}

	private void outputQuantilInfoTime(final String identifier, final StatisticsDataPerformanceIndicator indicator) {
		if (!SetupData.getSetup().showQuantils) return;
		if (indicator.getDistribution()==null) return;

		double upperBound=Double.MAX_VALUE;
		if (indicator.getDistribution()!=null) upperBound=indicator.getDistribution().upperBound-1;

		beginParagraph();
		boolean hitMax=false;
		final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
		for (double p: levels) {
			final String name=Language.tr("Statistics.Quantil")+"["+identifier+","+StatisticTools.formatPercent(p)+"]=";
			final double value=indicator.getQuantil(p);
			if (value>=upperBound) hitMax=true;
			addLine(name+timeAndNumber(value),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.quantil(p)));
		}
		endParagraph();

		if (hitMax && indicator.getMin()!=indicator.getMax()) {
			beginParagraph();
			addLine(String.format(Language.tr("Statistics.Quantil.InfoMax"),StatisticTools.formatNumber(upperBound)));
			endParagraph();
		}
	}

	private void outputQuantilInfoNumber(final String identifier, final StatisticsDataPerformanceIndicator indicator) {
		if (!SetupData.getSetup().showQuantils) return;
		if (indicator.getDistribution()==null) return;

		double upperBound=Double.MAX_VALUE;
		if (indicator.getDistribution()!=null) upperBound=indicator.getDistribution().upperBound-1;

		beginParagraph();
		boolean hitMax=false;
		final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
		for (double p: levels) {
			final String name=Language.tr("Statistics.Quantil")+"["+identifier+","+StatisticTools.formatPercent(p)+"]=";
			final double value=indicator.getQuantil(p);
			if (value>=upperBound) hitMax=true;
			addLine(name+StatisticTools.formatNumber(value),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.quantil(p)));
		}
		endParagraph();

		if (hitMax && indicator.getMin()!=indicator.getMax()) {
			beginParagraph();
			addLine(String.format(Language.tr("Statistics.Quantil.InfoMax"),StatisticTools.formatNumber(upperBound)));
			endParagraph();
		}
	}

	private void outputQuantilInfoNumber(final String identifier, final StatisticsDataPerformanceIndicatorWithNegativeValues indicator) {
		if (!SetupData.getSetup().showQuantils) return;

		double upperBound=Double.MAX_VALUE;
		if (indicator.getDistribution()!=null) upperBound=indicator.getDistribution().upperBound-1;

		beginParagraph();
		boolean hitMax=false;
		for (double p: StatisticsDataPerformanceIndicatorWithNegativeValues.storeQuantilValues) {
			final String name=Language.tr("Statistics.Quantil")+"["+identifier+","+StatisticTools.formatPercent(p)+"]=";
			final double value=indicator.getQuantil(p);
			if (value>=upperBound) hitMax=true;
			addLine(name+StatisticTools.formatNumber(value),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.quantil(p)));
		}
		endParagraph();

		if (hitMax && indicator.getMin()!=indicator.getMax()) {
			beginParagraph();
			addLine(String.format(Language.tr("Statistics.Quantil.InfoMax"),StatisticTools.formatNumber(upperBound)));
			endParagraph();
		}
	}

	private void outputQuantilInfoNumber(final String identifier, final StatisticsTimePerformanceIndicator indicator) {
		if (indicator==null) return;

		if (!SetupData.getSetup().showQuantils) return;

		double upperBound=Double.MAX_VALUE;
		if (indicator.getReadOnlyDistribution()!=null) upperBound=indicator.getReadOnlyDistribution().upperBound-1;

		beginParagraph();
		boolean hitMax=false;
		final double[] levels=getQuantilLevels();
		for (double p: levels) {
			final String name=Language.tr("Statistics.Quantil")+"["+identifier+","+StatisticTools.formatPercent(p)+"]=";
			final double value=indicator.getQuantil(p);
			if (value>=upperBound) hitMax=true;
			addLine(name+StatisticTools.formatNumber(value),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.quantil(p)));
		}
		endParagraph();

		if (hitMax && indicator.getTimeMin()!=indicator.getTimeMax()) {
			beginParagraph();
			addLine(String.format(Language.tr("Statistics.Quantil.InfoMax"),StatisticTools.formatNumber(upperBound)));
			endParagraph();
		}
	}

	private final static int MAX_SHORT_STATE_DISTRIBUTION=5;

	private void outputShortStateDistribution(final String identifier, final StatisticsTimePerformanceIndicator indicator) {
		if (indicator==null) return;
		if (indicator.getReadOnlyDistribution()==null) return;

		final double[] density=indicator.getReadOnlyDistribution().densityData;
		if (density.length==0) return;

		if (density.length>MAX_SHORT_STATE_DISTRIBUTION+1) {
			for (int i=MAX_SHORT_STATE_DISTRIBUTION+1;i<density.length;i++) if (density[i]>0) return;
		}

		double sum=0;
		int maxNonZero=0;
		for (int i=0;i<Math.min(MAX_SHORT_STATE_DISTRIBUTION+1,density.length);i++) {
			sum+=density[i];
			if (density[i]>0) maxNonZero=i;
		}
		if (maxNonZero==0) return;

		final StringBuilder info=new StringBuilder();
		info.append(Language.tr("Statistics.StateDistribution"));
		info.append(": ");
		for (int i=0;i<=maxNonZero;i++) {
			if (i>0) info.append(", ");
			info.append(String.format("P(%s=%d)=%s",identifier,i,StatisticTools.formatPercent(density[i]/sum)));
		}

		beginParagraph();
		addLine(info.toString());
		endParagraph();
	}

	private void buildInterarrivalSystem() {
		addHeading(1,Language.tr("Statistics.InterArrivalTimes"));

		if (statistics.editModel.useClientCount) {
			addHeading(2,Language.tr("Statistics.ArrivalsByModel"));
			beginParagraph();
			addLine(Language.tr("Statistics.NumberOfPlannedArrivals")+": "+NumberTools.formatLong(statistics.editModel.clientCount));
			endParagraph();
		}

		final String[] types=statistics.clientsInterarrivalTime.getNames();

		addHeading(2,Language.tr("Statistics.ArrivalsBySimulation"));
		beginParagraph();
		long sum=0;
		for (String type : types) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.clientsInterarrivalTime.get(type));
			final long count=indicator.getCount();
			addLine(type+": "+NumberTools.formatLong(count),xmlCount(indicator));
			sum+=count;
		}
		addLine(Language.tr("Statistics.TotalBig")+": "+NumberTools.formatLong(sum));
		endParagraph();

		if (statistics.simulationData.runRepeatCount>1) {
			addHeading(2,Language.tr("Statistics.SystemData.RepeatCount.Heading"));
			beginParagraph();
			addLine(Language.tr("Statistics.SystemData.RepeatCount")+": "+NumberTools.formatLong(statistics.simulationData.runRepeatCount));
			endParagraph();
		}

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		addHeading(2,Language.tr("Statistics.InterArrivalTimesInTheSimulation"));
		for (String type : types) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.clientsInterarrivalTime.get(type));
			addHeading(3,fullStationName(type));
			beginParagraph();
			addLine(Language.tr("Statistics.AverageInterArrivalCount")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
			addLine(Language.tr("Statistics.AverageInterArrivalTime")+": E[I]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDevInterArrivalTime")+": Std[I]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceInterArrivalTime")+": Var[I]="+timeAndNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.CVInterArrivalTime")+": CV[I]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.MinimalInterArrivalTime")+": Min[I]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterArrivalTime")+": Max[I]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("I",indicator);

			outputConfidenceData(indicator);
		}

		/* Infotext  */
		addDescription("InterarrivalSystem");
	}

	private long getArrivalSum() {
		long arrivalSum=0;
		for (String type : statistics.clientsInterarrivalTime.getNames()) {
			arrivalSum+=((StatisticsDataPerformanceIndicator)(statistics.clientsInterarrivalTime.get(type))).getCount();
		}
		return arrivalSum;
	}

	private boolean hasMultipleRecordsOfType(final StatisticsMultiPerformanceIndicator indicators, String name, final char marker) {
		while (!name.isEmpty() && name.charAt(name.length()-1)!=marker) name=name.substring(0,name.length()-1);
		if (name.isEmpty()) return true;
		name=name.substring(0,name.length()-1);
		if (name.isEmpty()) return true;

		int count=0;
		for (String record: indicators.getNames()) {
			if (record.startsWith(name)) count++;
			if (count>=2) return true;
		}

		return false;
	}

	private void buildInterarrivalStations() {
		addHeading(1,Language.tr("Statistics.ArrivalsAtStations"));

		/* Allgemeine Informationen */

		final long arrivalSum=getArrivalSum();

		final String[] stations=statistics.stationsInterarrivalTime.getNames();

		addHeading(2,Language.tr("Statistics.NumberOfArrivals"));
		beginParagraph();
		for (String station : stations) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.stationsInterarrivalTime.get(station));
			long count=indicator.getCount();
			if (count>0) {
				String part="";
				if (arrivalSum>0) part=" ("+StatisticTools.formatPercent(((double)count)/arrivalSum)+")";
				addLine(fullStationName(station)+": "+NumberTools.formatLong(count)+part,xmlCount(indicator));
			}
		}
		if (arrivalSum>0) {
			addLine("(100%="+NumberTools.formatLong(arrivalSum)+")");
		}
		endParagraph();

		if (statistics.simulationData.runRepeatCount>1) {
			addHeading(2,Language.tr("Statistics.SystemData.RepeatCount.Heading"));
			beginParagraph();
			addLine(Language.tr("Statistics.SystemData.RepeatCount")+": "+NumberTools.formatLong(statistics.simulationData.runRepeatCount));
			endParagraph();
		}

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		/* Ankünfte an den Stationen */

		addHeading(2,Language.tr("Statistics.InterArrivalTimesAtTheStations"));
		for (String station : stations) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.stationsInterarrivalTime.get(station));
			addHeading(3,fullStationName(station));
			beginParagraph();
			addLine(Language.tr("Statistics.AverageInterArrivalCount")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
			addLine(Language.tr("Statistics.AverageInterArrivalTime")+": E[I]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDevInterArrivalTime")+": Std[I]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceInterArrivalTime")+": Var[I]="+timeAndNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.CVInterArrivalTime")+": CV[I]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.MinimalInterArrivalTime")+": Min[I]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterArrivalTime")+": Max[I]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("I",indicator);

			outputConfidenceData(indicator);
		}

		/* Ankünfte an den Stationen nach Kundentypen */

		boolean headindPrinted=false;
		String[] records=statistics.stationsInterarrivalTimeByClientType.getNames();
		for (String record : records) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.stationsInterarrivalTimeByClientType.get(record));
			if (!hasMultipleRecordsOfType(statistics.stationsInterarrivalTimeByClientType,record,' ')) continue;
			if (!headindPrinted) {
				addHeading(2,Language.tr("Statistics.InterArrivalTimesAtTheStationsByClientTypes"));
				headindPrinted=true;
			}
			addHeading(3,fullStationName(record));
			beginParagraph();
			addLine(Language.tr("Statistics.AverageInterArrivalCount")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
			addLine(Language.tr("Statistics.AverageInterArrivalTime")+": E[I]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDevInterArrivalTime")+": Std[I]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceInterArrivalTime")+": Var[I]="+timeAndNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.CVInterArrivalTime")+": CV[I]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.MinimalInterArrivalTime")+": Min[I]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterArrivalTime")+": Max[I]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("I",indicator);

			outputConfidenceData(indicator);
		}

		/* Ankünfte an den Stationen nach Zuständen */

		headindPrinted=false;
		records=statistics.stationsInterarrivalTimeByState.getNames();
		for (String record : records) {
			if (!hasMultipleRecordsOfType(statistics.stationsInterarrivalTimeByState,record,'=')) continue;
			if (!headindPrinted) {
				addHeading(2,Language.tr("Statistics.InterArrivalTimesAtTheStationsByState"));
				headindPrinted=true;
			}
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.stationsInterarrivalTimeByState.get(record));
			addHeading(3,fullStationName(record));
			beginParagraph();
			addLine(Language.tr("Statistics.AverageInterArrivalCount")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
			addLine(Language.tr("Statistics.AverageInterArrivalTime")+": E[I]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDevInterArrivalTime")+": Std[I]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceInterArrivalTime")+": Var[I]="+timeAndNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.CVInterArrivalTime")+": CV[I]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.MinimalInterArrivalTime")+": Min[I]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterArrivalTime")+": Max[I]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("I",indicator);

			outputConfidenceData(indicator);
		}

		/* Infotext  */
		addDescription("InterarrivalStations");
	}

	private void buildInterleaveSystem() {
		addHeading(1,Language.tr("Statistics.InterLeaveTimes"));

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		for (String type : statistics.clientsInterleavingTime.getNames()) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.clientsInterleavingTime.get(type));
			addHeading(3,fullStationName(type));
			beginParagraph();
			addLine(Language.tr("Statistics.AverageInterLeaveCount")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
			addLine(Language.tr("Statistics.AverageInterLeaveTime")+": E[IL]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDevInterLeaveTime")+": Std[IL]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceInterLeaveTime")+": Var[IL]="+timeAndNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.CVInterLeaveTime")+": CV[IL]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.MinimalInterLeaveTime")+": Min[IL]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterLeaveTime")+": Max[IL]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("IL",indicator);

			outputConfidenceData(indicator);
		}

		/* Infotext  */
		addDescription("InterleaveSystem");
	}

	private void buildInterleaveStations() {
		addHeading(1,Language.tr("Statistics.LeavingsAtStations"));

		/* Allgemeine Informationen */

		final String[] stations=statistics.stationsInterleavingTime.getNames();

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		/* Abgänge an den Stationen */

		addHeading(2,Language.tr("Statistics.InterLeaveTimesAtTheStations"));
		for (String station : stations) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.stationsInterleavingTime.get(station));
			addHeading(3,fullStationName(station));
			beginParagraph();
			addLine(Language.tr("Statistics.AverageInterLeaveCount")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
			addLine(Language.tr("Statistics.AverageInterLeaveTime")+": E[IL]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDevInterLeaveTime")+": Std[IL]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceInterLeaveTime")+": Var[IL]="+timeAndNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.CVInterLeaveTime")+": CV[IL]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.MinimalInterLeaveTime")+": Min[IL]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterLeaveTime")+": Max[IL]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("IL",indicator);

			outputConfidenceData(indicator);
		}

		/* Abgänge an den Stationen nach Kundentypen */

		boolean headindPrinted=false;
		final String[] records=statistics.stationsInterleavingTimeByClientType.getNames();
		for (String record : records) {
			if (!hasMultipleRecordsOfType(statistics.stationsInterleavingTimeByClientType,record,' ')) continue;
			if (!headindPrinted) {
				addHeading(2,Language.tr("Statistics.InterLeaveTimesAtTheStationsByClientTypes"));
				headindPrinted=true;
			}
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.stationsInterleavingTimeByClientType.get(record));
			addHeading(3,fullStationName(record));
			beginParagraph();
			addLine(Language.tr("Statistics.AverageInterLeaveCount")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
			addLine(Language.tr("Statistics.AverageInterLeaveTime")+": E[IL]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDevInterLeaveTime")+": Std[IL]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceInterLeaveTime")+": Var[IL]="+timeAndNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.CVInterLeaveTime")+": CV[IL]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.MinimalInterLeaveTime")+": Min[IL]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterLeaveTime")+": Max[IL]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("IL",indicator);

			outputConfidenceData(indicator);
		}

		/* Infotext  */
		addDescription("InterleaveStations");
	}

	private void buildSystemInfo() {
		addHeading(1,Language.tr("Statistics.SystemData"));
		beginParagraph();
		addLine(Language.tr("Statistics.SystemData.Version")+": "+statistics.editModel.version);
		addLine(Language.tr("Statistics.SystemData.RunDate")+": "+statistics.simulationData.runDate);
		addLine(Language.tr("Statistics.SystemData.RunThreads")+": "+statistics.simulationData.runThreads);
		if (statistics.simulationData.runRepeatCount>1) addLine(Language.tr("Statistics.SystemData.RepeatCount")+": "+NumberTools.formatLong(statistics.simulationData.runRepeatCount));
		addLine(Language.tr("Statistics.SystemData.RunOS")+": "+statistics.simulationData.runOS);
		if (statistics.editModel.author!=null && !statistics.editModel.author.trim().isEmpty()) addLine(Language.tr("Statistics.SystemData.EditUser")+": "+statistics.editModel.author);
		addLine(Language.tr("Statistics.SystemData.RunUser")+": "+statistics.simulationData.runUser);
		endParagraph();

		beginParagraph();
		addLine(Language.tr("Statistics.SystemData.RunTime")+": "+NumberTools.formatLong(statistics.simulationData.runTime)+" ms");
		final DataDistributionImpl threads=new DataDistributionImpl(1000,statistics.simulationData.threadRunTimes);
		final double threadMax=threads.getMax();
		final double threadMin=threads.getMin();
		if (statistics.simulationData.runThreads>1 && threadMax>0 && threadMin>0) {
			addLine(Language.tr("Statistics.SystemData.ThreadRunTimeFactor")+": "+StatisticTools.formatPercent(threadMax/threadMin-1));
		}
		if (statistics.simulationData.numaAwareMode) addLine(Language.tr("Statistics.SystemData.NUMAMode"));
		endParagraph();

		beginParagraph();
		long sum=0;
		for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class)) sum+=indicator.getCount();
		addLine(Language.tr("Statistics.SystemData.SimulatedArrivals")+": "+NumberTools.formatLong(sum));
		String s;
		if (sum>0) {
			s=StatisticTools.formatNumber(((double)statistics.simulationData.runTime)*statistics.simulationData.runThreads/sum,2);
			if (s.equals("0")) {
				s=StatisticTools.formatNumber(((double)statistics.simulationData.runTime*1000)*statistics.simulationData.runThreads/sum,2);
				addLine(Language.tr("Statistics.SystemData.TimePerClient")+" (*) : "+s+" µs");
			} else {
				addLine(Language.tr("Statistics.SystemData.TimePerClient")+" (*): "+s+" ms");
			}
		}
		endParagraph();

		beginParagraph();
		addLine(Language.tr("Statistics.SystemData.SimulatedEvents")+": "+NumberTools.formatLong(statistics.simulationData.runEvents));
		if (statistics.simulationData.runTime>0) {
			addLine(Language.tr("Statistics.SystemData.EventsPerSecond")+": "+NumberTools.formatLong(statistics.simulationData.runEvents*1000/statistics.simulationData.runTime));
		}
		double time=((double)statistics.simulationData.runTime)*statistics.simulationData.runThreads/statistics.simulationData.runEvents;
		if (time>=1) {
			addLine(Language.tr("Statistics.SystemData.TimePerEvent")+" (*): "+StatisticTools.formatNumber(time,2)+" ms");
		} else {
			time*=1000;
			if (time>=1) {
				addLine(Language.tr("Statistics.SystemData.TimePerEvent")+" (*): "+StatisticTools.formatNumber(time,2)+" µs");
			} else {
				time*=1000;
				addLine(Language.tr("Statistics.SystemData.TimePerEvent")+" (*): "+Math.round(time)+" ns");
			}
		}
		endParagraph();

		beginParagraph();
		addLine(Language.tr("Statistics.SystemData.Info1"));
		addLine(Language.tr("Statistics.SystemData.Info2"));
		endParagraph();

		outputEmergencyShutDownInfo(true);
	}

	private void buildClientData(final StatisticsDataPerformanceIndicator waitingTime, final StatisticsDataPerformanceIndicator transferTime, final StatisticsDataPerformanceIndicator processingTime, final StatisticsDataPerformanceIndicator residenceTime, final boolean hasWaitingTimes, final boolean hasTransferTimes, final boolean hasProcessingTimes, final boolean hasResidenceTimes) {
		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		if (hasWaitingTimes) {
			addHeading(3,Language.tr("Statistics.WaitingTimes"));
			beginParagraph();
			addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(waitingTime.getCount())+repeatInfo,xmlCount(waitingTime));
			addLine(Language.tr("Statistics.AverageWaitingTime")+": E[W]="+timeAndNumber(waitingTime.getMean()),xmlMean(waitingTime));
			addLine(Language.tr("Statistics.StdDevWaitingTime")+": Std[W]="+timeAndNumber(waitingTime.getSD()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceWaitingTime")+": Var[W]="+timeAndNumber(waitingTime.getVar()));
			addLine(Language.tr("Statistics.CVWaitingTime")+": CV[W]="+StatisticTools.formatNumber(waitingTime.getCV()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.CV));
			addLine(Language.tr("Statistics.MinimumWaitingTime")+": Min[W]="+timeAndNumber(waitingTime.getMin()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximumWaitingTime")+": Max[W]="+timeAndNumber(waitingTime.getMax()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("W",waitingTime);

			outputConfidenceData(waitingTime);
		}

		if (hasTransferTimes) {
			addHeading(3,Language.tr("Statistics.TransferTimes"));
			beginParagraph();
			addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(transferTime.getCount())+repeatInfo,xmlCount(transferTime));
			addLine(Language.tr("Statistics.AverageTransferTime")+": E[T]="+timeAndNumber(transferTime.getMean()),xmlMean(transferTime));
			addLine(Language.tr("Statistics.StdDevTransferTime")+": Std[T]="+timeAndNumber(transferTime.getSD()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceTransferTime")+": Var[T]="+timeAndNumber(transferTime.getVar()));
			addLine(Language.tr("Statistics.CVTransferTime")+": CV[T]="+StatisticTools.formatNumber(transferTime.getCV()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.CV));
			addLine(Language.tr("Statistics.MinimumTransferTime")+": Min[T]="+timeAndNumber(transferTime.getMin()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximumTransferTime")+": Max[T]="+timeAndNumber(transferTime.getMax()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("T",transferTime);

			outputConfidenceData(transferTime);
		}

		if (hasProcessingTimes) {
			addHeading(3,Language.tr("Statistics.ProcessTimes"));
			beginParagraph();
			addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(processingTime.getCount())+repeatInfo,xmlCount(processingTime));
			addLine(Language.tr("Statistics.AverageProcessTime")+": E[S]="+timeAndNumber(processingTime.getMean()),xmlMean(processingTime));
			addLine(Language.tr("Statistics.StdDevProcessTime")+": Std[S]="+timeAndNumber(processingTime.getSD()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceProcessTime")+": Var[S]="+timeAndNumber(processingTime.getVar()));
			addLine(Language.tr("Statistics.CVProcessTime")+": CV[S]="+StatisticTools.formatNumber(processingTime.getCV()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.CV));
			addLine(Language.tr("Statistics.MinimumProcessTime")+": Min[S]="+timeAndNumber(processingTime.getMin()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximumProcessTime")+": Max[S]="+timeAndNumber(processingTime.getMax()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("S",processingTime);

			outputConfidenceData(processingTime);
		}

		if (hasResidenceTimes) {
			addHeading(3,Language.tr("Statistics.ResidenceTimes"));
			beginParagraph();
			addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(residenceTime.getCount())+repeatInfo,xmlCount(residenceTime));
			addLine(Language.tr("Statistics.AverageResidenceTime")+": E[V]="+timeAndNumber(residenceTime.getMean()),xmlMean(residenceTime));
			addLine(Language.tr("Statistics.StdDevResidenceTime")+": Std[V]="+timeAndNumber(residenceTime.getSD()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceResidenceTime")+": Var[V]="+timeAndNumber(residenceTime.getVar()));
			addLine(Language.tr("Statistics.CVResidenceTime")+": CV[V]="+StatisticTools.formatNumber(residenceTime.getCV()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.CV));
			addLine(Language.tr("Statistics.MinimumResidenceTime")+": Min[V]="+timeAndNumber(residenceTime.getMin()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximumResidenceTime")+": Max[V]="+timeAndNumber(residenceTime.getMax()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("V",residenceTime);

			outputConfidenceData(residenceTime);
		}

		if (!hasWaitingTimes && !hasTransferTimes && !hasProcessingTimes && !hasResidenceTimes) {
			beginParagraph();
			addLine(Language.tr("Statistics.NoWaitingTransferProcessingTimes"));
			endParagraph();
		}
	}

	private void buildClients() {
		addHeading(1,Language.tr("Statistics.WaitingAndProcessTimesByClients"));

		final String[] clients=statistics.clientsWaitingTimes.getNames();

		final boolean hasWaitingTimes=(statistics.clientsAllWaitingTimes.getMax()>0);
		final boolean hasTransferTimes=(statistics.clientsAllTransferTimes.getMax()>0);
		final boolean hasProcessingTimes=(statistics.clientsAllProcessingTimes.getMax()>0);
		final boolean hasResidenceTimes=(statistics.clientsAllResidenceTimes.getMax()>0);

		for (String client : clients) {
			addHeading(2,Language.tr("Statistics.ClientType")+" \""+client+"\"");
			final StatisticsDataPerformanceIndicator waitingTime=(StatisticsDataPerformanceIndicator)(statistics.clientsWaitingTimes.get(client));
			final StatisticsDataPerformanceIndicator transferTime=(StatisticsDataPerformanceIndicator)(statistics.clientsTransferTimes.get(client));
			final StatisticsDataPerformanceIndicator processingTime=(StatisticsDataPerformanceIndicator)(statistics.clientsProcessingTimes.get(client));
			final StatisticsDataPerformanceIndicator residenceTime=(StatisticsDataPerformanceIndicator)(statistics.clientsResidenceTimes.get(client));
			buildClientData(waitingTime,transferTime,processingTime,residenceTime,hasWaitingTimes,hasTransferTimes,hasProcessingTimes,hasResidenceTimes);
		}

		if (clients.length>1) {
			addHeading(2,Language.tr("Statistics.TotalAverage"));
			final StatisticsDataPerformanceIndicator waitingTime=statistics.clientsAllWaitingTimes;
			final StatisticsDataPerformanceIndicator transferTime=statistics.clientsAllTransferTimes;
			final StatisticsDataPerformanceIndicator processingTime=statistics.clientsAllProcessingTimes;
			final StatisticsDataPerformanceIndicator residenceTime=statistics.clientsAllResidenceTimes;
			buildClientData(waitingTime,transferTime,processingTime,residenceTime,hasWaitingTimes,hasTransferTimes,hasProcessingTimes,hasResidenceTimes);
		}

		if (statistics.clientsAllWaitingTimes.getBatchCount()<1 && statistics.threadBasedConfidence.size()>0) {
			addHeading(3,Language.tr("Statistics.Confidence.HeadingThread"));
			outputThreadConfidenceData();
		}

		/* Infotext  */
		addDescription("TimesClients");
	}

	private void buildStations() {
		addHeading(1,Language.tr("Statistics.WaitingAndProcessTimesByStations"));

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		final String[] stations=statistics.stationsWaitingTimes.getNames();

		for (String station : stations) {
			final StatisticsDataPerformanceIndicator waitingTime=(StatisticsDataPerformanceIndicator)(statistics.stationsWaitingTimes.get(station));
			final StatisticsDataPerformanceIndicator transferTime=(StatisticsDataPerformanceIndicator)(statistics.stationsTransferTimes.get(station));
			final StatisticsDataPerformanceIndicator processingTime=(StatisticsDataPerformanceIndicator)(statistics.stationsProcessingTimes.get(station));
			final StatisticsDataPerformanceIndicator residenceTime=(StatisticsDataPerformanceIndicator)(statistics.stationsProcessingTimes.get(station));
			if (waitingTime.getMean()>0 || transferTime.getMean()>0 || processingTime.getMean()>0 || residenceTime.getMean()>0) {
				addHeading(2,fullStationName(station));
				if (waitingTime.getMean()>0) {
					addHeading(3,Language.tr("Statistics.WaitingTimes"));
					beginParagraph();
					addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(waitingTime.getCount())+repeatInfo,xmlCount(waitingTime));
					addLine(Language.tr("Statistics.AverageWaitingTime")+": E[W]="+timeAndNumber(waitingTime.getMean()),xmlMean(waitingTime));
					addLine(Language.tr("Statistics.StdDevWaitingTime")+": Std[W]="+timeAndNumber(waitingTime.getSD()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceWaitingTime")+": Var[W]="+timeAndNumber(waitingTime.getVar()));
					addLine(Language.tr("Statistics.CVWaitingTime")+": CV[W]="+StatisticTools.formatNumber(waitingTime.getCV()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.CV));
					addLine(Language.tr("Statistics.MinimumWaitingTime")+": Min[W]="+timeAndNumber(waitingTime.getMin()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumWaitingTime")+": Max[W]="+timeAndNumber(waitingTime.getMax()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.MAXIMUM));
					endParagraph();

					outputQuantilInfoTime("W",waitingTime);

					outputConfidenceData(waitingTime);
				}

				if (transferTime.getMean()>0) {
					addHeading(3,Language.tr("Statistics.TransferTimes"));
					beginParagraph();
					addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(transferTime.getCount())+repeatInfo,xmlCount(transferTime));
					addLine(Language.tr("Statistics.AverageTransferTime")+": E[T]="+timeAndNumber(transferTime.getMean()),xmlMean(transferTime));
					addLine(Language.tr("Statistics.StdDevTransferTime")+": Std[T]="+timeAndNumber(transferTime.getSD()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceTransferTime")+": Var[T]="+timeAndNumber(transferTime.getVar()));
					addLine(Language.tr("Statistics.CVTransferTime")+": CV[T]="+StatisticTools.formatNumber(transferTime.getCV()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.CV));
					addLine(Language.tr("Statistics.MinimumTransferTime")+": Min[T]="+timeAndNumber(transferTime.getMin()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumTransferTime")+": Max[T]="+timeAndNumber(transferTime.getMax()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.MAXIMUM));
					endParagraph();

					outputQuantilInfoTime("T",transferTime);

					outputConfidenceData(transferTime);
				}

				if (processingTime.getMean()>0) {
					addHeading(3,Language.tr("Statistics.ProcessTimes"));
					beginParagraph();
					addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(processingTime.getCount())+repeatInfo,xmlCount(processingTime));
					addLine(Language.tr("Statistics.AverageProcessTime")+": E[S]="+timeAndNumber(processingTime.getMean()),xmlMean(processingTime));
					addLine(Language.tr("Statistics.StdDevProcessTime")+": Std[S]="+timeAndNumber(processingTime.getSD()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceProcessTime")+": Var[S]="+timeAndNumber(processingTime.getVar()));
					addLine(Language.tr("Statistics.CVProcessTime")+": CV[S]="+StatisticTools.formatNumber(processingTime.getCV()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.CV));
					addLine(Language.tr("Statistics.MinimumProcessTime")+": Min[S]="+timeAndNumber(processingTime.getMin()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumProcessTime")+": Max[S]="+timeAndNumber(processingTime.getMax()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.MAXIMUM));
					endParagraph();

					outputQuantilInfoTime("S",processingTime);

					outputConfidenceData(processingTime);
				}

				if (residenceTime.getMean()>0) {
					addHeading(3,Language.tr("Statistics.ResidenceTimes"));
					beginParagraph();
					addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(residenceTime.getCount())+repeatInfo,xmlCount(residenceTime));
					addLine(Language.tr("Statistics.AverageResidenceTime")+": E[V]="+timeAndNumber(residenceTime.getMean()),xmlMean(residenceTime));
					addLine(Language.tr("Statistics.StdDevResidenceTime")+": Std[V]="+timeAndNumber(residenceTime.getSD()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceResidenceTime")+": Var[V]="+timeAndNumber(residenceTime.getVar()));
					addLine(Language.tr("Statistics.CVResidenceTime")+": CV[V]="+StatisticTools.formatNumber(residenceTime.getCV()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.CV));
					addLine(Language.tr("Statistics.MinimumResidenceTime")+": Min[V]="+timeAndNumber(residenceTime.getMin()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumResidenceTime")+": Max[V]="+timeAndNumber(residenceTime.getMax()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MAXIMUM));
					endParagraph();

					outputQuantilInfoTime("V",residenceTime);

					outputConfidenceData(residenceTime);
				}
			}
		}

		/* Infotext  */
		addDescription("TimeStations");
	}

	private void buildStationsClients() {
		addHeading(1,Language.tr("Statistics.WaitingAndProcessTimesByStationsAndClients"));

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		for (String name : statistics.stationsWaitingTimesByClientType.getNames()) {
			final StatisticsDataPerformanceIndicator waitingTime=(StatisticsDataPerformanceIndicator)(statistics.stationsWaitingTimesByClientType.get(name));
			if (waitingTime.getMean()>0) {
				addHeading(2,Language.tr("Statistics.WaitingTimes")+" - "+name);
				beginParagraph();
				addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(waitingTime.getCount())+repeatInfo,xmlCount(waitingTime));
				addLine(Language.tr("Statistics.AverageWaitingTime")+": E[W]="+timeAndNumber(waitingTime.getMean()),xmlMean(waitingTime));
				addLine(Language.tr("Statistics.StdDevWaitingTime")+": Std[W]="+timeAndNumber(waitingTime.getSD()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.SD));
				addLine(Language.tr("Statistics.VarianceWaitingTime")+": Var[W]="+timeAndNumber(waitingTime.getVar()));
				addLine(Language.tr("Statistics.CVWaitingTime")+": CV[W]="+StatisticTools.formatNumber(waitingTime.getCV()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.CV));
				addLine(Language.tr("Statistics.MinimumWaitingTime")+": Min[W]="+timeAndNumber(waitingTime.getMin()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumWaitingTime")+": Max[W]="+timeAndNumber(waitingTime.getMax()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.MAXIMUM));
				endParagraph();

				outputQuantilInfoTime("W",waitingTime);

				outputConfidenceData(waitingTime);
			}
		}

		for (String name : statistics.stationsTransferTimesByClientType.getNames()) {
			final StatisticsDataPerformanceIndicator transferTime=(StatisticsDataPerformanceIndicator)(statistics.stationsTransferTimesByClientType.get(name));
			if (transferTime.getMean()>0) {
				addHeading(2,Language.tr("Statistics.TransferTimes")+" - "+name);
				beginParagraph();
				addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(transferTime.getCount())+repeatInfo,xmlCount(transferTime));
				addLine(Language.tr("Statistics.AverageTransferTime")+": E[T]="+timeAndNumber(transferTime.getMean()),xmlMean(transferTime));
				addLine(Language.tr("Statistics.StdDevTransferTime")+": Std[T]="+timeAndNumber(transferTime.getSD()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.SD));
				addLine(Language.tr("Statistics.VarianceTransferTime")+": Var[T]="+timeAndNumber(transferTime.getVar()));
				addLine(Language.tr("Statistics.CVTransferTime")+": CV[T]="+StatisticTools.formatNumber(transferTime.getCV()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.CV));
				addLine(Language.tr("Statistics.MinimumTransferTime")+": Min[T]="+timeAndNumber(transferTime.getMin()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumTransferTime")+": Max[T]="+timeAndNumber(transferTime.getMax()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.MAXIMUM));
				endParagraph();

				outputQuantilInfoTime("T",transferTime);

				outputConfidenceData(transferTime);
			}
		}

		for (String name : statistics.stationsProcessingTimesByClientType.getNames()) {
			final StatisticsDataPerformanceIndicator processingTime=(StatisticsDataPerformanceIndicator)(statistics.stationsProcessingTimesByClientType.get(name));
			if (processingTime.getMean()>0) {
				addHeading(2,Language.tr("Statistics.ProcessTimes")+" - "+name);
				beginParagraph();
				addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(processingTime.getCount())+repeatInfo,xmlCount(processingTime));
				addLine(Language.tr("Statistics.AverageProcessTime")+": E[S]="+timeAndNumber(processingTime.getMean()),xmlMean(processingTime));
				addLine(Language.tr("Statistics.StdDevProcessTime")+": Std[S]="+timeAndNumber(processingTime.getSD()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.SD));
				addLine(Language.tr("Statistics.VarianceProcessTime")+": Var[S]="+timeAndNumber(processingTime.getVar()));
				addLine(Language.tr("Statistics.CVProcessTime")+": CV[S]="+StatisticTools.formatNumber(processingTime.getCV()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.CV));
				addLine(Language.tr("Statistics.MinimumProcessTime")+": Min[S]="+timeAndNumber(processingTime.getMin()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumProcessTime")+": Max[S]="+timeAndNumber(processingTime.getMax()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.MAXIMUM));
				endParagraph();

				outputQuantilInfoTime("S",processingTime);

				outputConfidenceData(processingTime);
			}
		}

		for (String name : statistics.stationsResidenceTimesByClientType.getNames()) {
			final StatisticsDataPerformanceIndicator residenceTime=(StatisticsDataPerformanceIndicator)(statistics.stationsResidenceTimesByClientType.get(name));
			if (residenceTime.getMean()>0) {
				addHeading(2,Language.tr("Statistics.ResidenceTimes")+" - "+name);
				beginParagraph();
				addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(residenceTime.getCount())+repeatInfo,xmlCount(residenceTime));
				addLine(Language.tr("Statistics.AverageResidenceTime")+": E[V]="+timeAndNumber(residenceTime.getMean()),xmlMean(residenceTime));
				addLine(Language.tr("Statistics.StdDevResidenceTime")+": Std[V]="+timeAndNumber(residenceTime.getSD()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.SD));
				addLine(Language.tr("Statistics.VarianceResidenceTime")+": Var[V]="+timeAndNumber(residenceTime.getVar()));
				addLine(Language.tr("Statistics.CVResidenceTime")+": CV[V]="+StatisticTools.formatNumber(residenceTime.getCV()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.CV));
				addLine(Language.tr("Statistics.MinimumResidenceTime")+": Min[V]="+timeAndNumber(residenceTime.getMin()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumResidenceTime")+": Max[V]="+timeAndNumber(residenceTime.getMax()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MAXIMUM));
				endParagraph();

				outputQuantilInfoTime("V",residenceTime);

				outputConfidenceData(residenceTime);
			}
		}

		/* Infotext  */
		addDescription("TimeStationsClients");
	}

	private void buildClientsCount() {
		addHeading(1,Language.tr("Statistics.NumberOfClientsInTheSystemAndAtTheStations"));

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		StatisticsMultiPerformanceIndicator indicators, indicators2;

		/* Kunden im System */

		addHeading(2,Language.tr("Statistics.NumberOfClientsInTheSystem"));
		beginParagraph();
		addLine(Language.tr("Statistics.TotalNumberOfClients")+": "+NumberTools.formatLong(getArrivalSum())+repeatInfo);
		addLine(Language.tr("Statistics.AverageClientsInSystem")+": E[N]="+StatisticTools.formatNumber(statistics.clientsInSystem.getTimeMean()),xmlMean(statistics.clientsInSystem));
		addLine(Language.tr("Statistics.StdDevClientsInSystem")+": Std[N]="+StatisticTools.formatNumber(statistics.clientsInSystem.getTimeSD()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystem,IndicatorMode.SD));
		addLine(Language.tr("Statistics.VarianceClientsInSystem")+": Var[N]="+StatisticTools.formatNumber(statistics.clientsInSystem.getTimeVar()));
		addLine(Language.tr("Statistics.CVClientsInSystem")+": CV[N]="+StatisticTools.formatNumber(statistics.clientsInSystem.getTimeCV()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystem,IndicatorMode.CV));
		addLine(Language.tr("Statistics.MinimumClientsInSystem")+": Min[N]="+StatisticTools.formatNumber(statistics.clientsInSystem.getTimeMin()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystem,IndicatorMode.MINIMUM));
		addLine(Language.tr("Statistics.MaximumClientsInSystem")+": Max[N]="+StatisticTools.formatNumber(statistics.clientsInSystem.getTimeMax()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystem,IndicatorMode.MAXIMUM));
		endParagraph();

		outputShortStateDistribution("N",statistics.clientsInSystem);

		outputQuantilInfoNumber("N",statistics.clientsInSystem);

		/* Kunden an den Stationen */

		addHeading(2,Language.tr("Statistics.NumberOfClientsAtStations"));
		indicators=statistics.clientsAtStationByStation;
		indicators2=statistics.stationsInterarrivalTime;
		for (String station : indicators.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(station));
			final StatisticsDataPerformanceIndicator indicator2=(StatisticsDataPerformanceIndicator)(indicators2.get(station));
			if (indicator.getTimeMean()>0) {
				addHeading(3,fullStationName(station));
				beginParagraph();
				if (indicator2!=null) addLine(Language.tr("Statistics.TotalNumberOfClients")+": "+NumberTools.formatLong(indicator2.getCount())+repeatInfo,xmlCount(indicator));
				addLine(Language.tr("Statistics.AverageNumberOfClients")+": E[N]="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
				addLine(Language.tr("Statistics.StdDevNumberOfClients")+": Std[N]="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
				addLine(Language.tr("Statistics.VarianceNumberOfClients")+": Var[N]="+StatisticTools.formatNumber(indicator.getTimeVar()));
				addLine(Language.tr("Statistics.CVNumberOfClients")+": CV[N]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
				addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[N]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[N]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
				endParagraph();

				outputShortStateDistribution("N",indicator);

				outputQuantilInfoNumber("N",indicator);
			}
		}

		/* Kundentypen */

		indicators=statistics.clientsInSystemByClient;
		if (indicators.size()>1) {
			addHeading(2,Language.tr("Statistics.AverageNumberOfClientsByClientTypes"));
			for (String station : indicators.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(station));
				if (indicator.getTimeMean()>0) {
					addHeading(3,fullStationName(station));
					beginParagraph();
					addLine(Language.tr("Statistics.AverageNumberOfClients")+": E[N]="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlCount(indicator));
					addLine(Language.tr("Statistics.StdDevNumberOfClients")+": Std[N]="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceNumberOfClients")+": Var[N]="+StatisticTools.formatNumber(indicator.getTimeVar()));
					addLine(Language.tr("Statistics.CVNumberOfClients")+": CV[N]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
					addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[N]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[N]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();

					outputShortStateDistribution("N",indicator);

					outputQuantilInfoNumber("N",indicator);
				}
			}
		}

		/* Kundentypen an den Stationen */

		indicators=statistics.clientsAtStationByStationAndClient;
		if (indicators.size()>1) {
			addHeading(2,Language.tr("Statistics.AverageNumberOfClientsAtStationsByClientTypes"));
			for (String station : indicators.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(station));
				if (indicator.getTimeMean()>0) {
					addHeading(3,fullStationName(station));
					beginParagraph();
					addLine(Language.tr("Statistics.AverageNumberOfClients")+": E[N]="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlCount(indicator));
					addLine(Language.tr("Statistics.StdDevNumberOfClients")+": Std[N]="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceNumberOfClients")+": Var[N]="+StatisticTools.formatNumber(indicator.getTimeVar()));
					addLine(Language.tr("Statistics.CVNumberOfClients")+": CV[N]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
					addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[N]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[N]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();

					outputShortStateDistribution("N",indicator);

					outputQuantilInfoNumber("N",indicator);
				}
			}
		}

		/* Kunden im System (wartend) */

		addHeading(2,Language.tr("Statistics.NumberOfClientsInTheSystemWaiting"));
		beginParagraph();
		addLine(Language.tr("Statistics.AverageClientsInSystemWaiting")+": E[NQ]="+StatisticTools.formatNumber(statistics.clientsInSystemQueues.getTimeMean()),xmlMean(statistics.clientsInSystemQueues));
		addLine(Language.tr("Statistics.StdDevClientsInSystemWaiting")+": Std[NQ]="+StatisticTools.formatNumber(statistics.clientsInSystemQueues.getTimeSD()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemQueues,IndicatorMode.SD));
		addLine(Language.tr("Statistics.VarianceClientsInSystemWaiting")+": Var[NQ]="+StatisticTools.formatNumber(statistics.clientsInSystemQueues.getTimeVar()));
		addLine(Language.tr("Statistics.CVClientsInSystemWaiting")+": CV[NQ]="+StatisticTools.formatNumber(statistics.clientsInSystemQueues.getTimeCV()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemQueues,IndicatorMode.CV));
		addLine(Language.tr("Statistics.MinimumClientsInSystemWaiting")+": Min[NQ]="+StatisticTools.formatNumber(statistics.clientsInSystemQueues.getTimeMin()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemQueues,IndicatorMode.MINIMUM));
		addLine(Language.tr("Statistics.MaximumClientsInSystemWaiting")+": Max[NQ]="+StatisticTools.formatNumber(statistics.clientsInSystemQueues.getTimeMax()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemQueues,IndicatorMode.MAXIMUM));
		endParagraph();

		outputShortStateDistribution("NQ",statistics.clientsInSystemQueues);

		outputQuantilInfoNumber("NQ",statistics.clientsInSystemQueues);

		/* Kunden an den Stationen (wartend) */

		addHeading(2,Language.tr("Statistics.NumberOfClientsAtStationQueues"));
		indicators=statistics.clientsAtStationQueueByStation;
		for (String station : indicators.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(station));
			if (indicator.getTimeMean()>0) {
				addHeading(3,fullStationName(station));
				beginParagraph();
				addLine(Language.tr("Statistics.AverageNumberOfClients")+": E[NQ]="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
				addLine(Language.tr("Statistics.StdDevNumberOfClients")+": Std[NQ]="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
				addLine(Language.tr("Statistics.VarianceNumberOfClients")+": Var[NQ]="+StatisticTools.formatNumber(indicator.getTimeVar()));
				addLine(Language.tr("Statistics.CVNumberOfClients")+": CV[NQ]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
				addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[NQ]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[NQ]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
				endParagraph();

				outputShortStateDistribution("NQ",indicator);

				outputQuantilInfoNumber("NQ",indicator);
			}
		}

		/* Kundentypen (wartend) */

		indicators=statistics.clientsAtStationQueueByClient;
		if (indicators.size()>1) {
			addHeading(2,Language.tr("Statistics.NumberOfWaitingClients"));
			for (String clientTypes : indicators.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(clientTypes));
				if (indicator.getTimeMean()>0) {
					addHeading(3,clientTypes);
					beginParagraph();
					addLine(Language.tr("Statistics.AverageNumberOfClients")+": E[NQ]="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
					addLine(Language.tr("Statistics.StdDevNumberOfClients")+": Std[NQ]="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceNumberOfClients")+": Var[NQ]="+StatisticTools.formatNumber(indicator.getTimeVar()));
					addLine(Language.tr("Statistics.CVNumberOfClients")+": CV[NQ]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
					addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[NQ]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[NQ]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();

					outputShortStateDistribution("NQ",indicator);

					outputQuantilInfoNumber("NQ",indicator);
				}
			}
		}

		/* Kundentypen an den Stationen (wartend) */

		indicators=statistics.clientsAtStationQueueByStationAndClient;
		if (indicators.size()>1) {
			addHeading(2,Language.tr("Statistics.NumberOfClientsAtStationQueuesByClientTypes"));
			for (String station : indicators.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(station));
				if (indicator.getTimeMean()>0) {
					addHeading(3,fullStationName(station));
					beginParagraph();
					addLine(Language.tr("Statistics.AverageNumberOfClients")+": E[NQ]="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
					addLine(Language.tr("Statistics.StdDevNumberOfClients")+": Std[NQ]="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceNumberOfClients")+": Var[NQ]="+StatisticTools.formatNumber(indicator.getTimeVar()));
					addLine(Language.tr("Statistics.CVNumberOfClients")+": CV[NQ]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
					addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[NQ]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[NQ]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();

					outputShortStateDistribution("NQ",indicator);

					outputQuantilInfoNumber("NQ",indicator);
				}
			}
		}

		/* Infotext  */
		addDescription("ClientCount");
	}

	private void buildUtilization() {
		addHeading(1,Language.tr("Statistics.ResourceUtilization"));

		final StatisticsMultiPerformanceIndicator countIndicators=statistics.resourceCount;
		final StatisticsMultiPerformanceIndicator indicators=statistics.resourceUtilization;
		for (String resource : indicators.getNames()) {
			addHeading(2,resource);
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(resource));
			final StatisticsTimePerformanceIndicator countIndicator=(StatisticsTimePerformanceIndicator)(countIndicators.getOrNull(resource));
			final ModelResource resourceObj=statistics.editModel.resources.get(resource);

			double rho=-1;
			boolean variable=false;
			if (resourceObj!=null) {
				if (resourceObj.getMode()==ModelResource.Mode.MODE_NUMBER) {
					final int count=resourceObj.getCount();
					if (countIndicator==null || count<=0 || countIndicator.getTimeMean()<0.0001) {
						/* Einfache Fallback-Variante */
						beginParagraph();
						addLine(Language.tr("Statistics.Utilization.NumberOfOperators")+": "+((count>0)?(""+count):Language.tr("Statistics.Utilization.NumberOfOperators.infinite")));
						endParagraph();
						if (count>0) rho=indicator.getTimeMean()/count;
					} else {
						/* Direkt erfasste (variable) Anzahl an vorhanden Bedienern */
						beginParagraph();
						addLine(Language.tr("Statistics.Utilization.NumberOfOperators")+": "+StatisticTools.formatNumber(countIndicator.getTimeMean()),xmlMean(countIndicators));
						rho=indicator.getTimeMean()/countIndicator.getTimeMean();
						if (countIndicator.getTimeMin()!=countIndicator.getTimeMax()) {
							variable=true;
							addLine(Language.tr("Statistics.Utilization.NumberOfOperators.StdDev")+"="+StatisticTools.formatNumber(countIndicator.getTimeSD()),fastAccessBuilder.getXMLSelector(countIndicator,IndicatorMode.SD));
							addLine(Language.tr("Statistics.Utilization.NumberOfOperators.Variance")+"="+StatisticTools.formatNumber(countIndicator.getTimeVar()));
							addLine(Language.tr("Statistics.Utilization.NumberOfOperators.CV")+"="+StatisticTools.formatNumber(countIndicator.getTimeCV()),fastAccessBuilder.getXMLSelector(countIndicator,IndicatorMode.CV));
							addLine(Language.tr("Statistics.Utilization.NumberOfOperators.Minimum")+": "+StatisticTools.formatNumber(countIndicator.getTimeMin()),fastAccessBuilder.getXMLSelector(countIndicator,IndicatorMode.MINIMUM));
							addLine(Language.tr("Statistics.Utilization.NumberOfOperators.Maximum")+": "+StatisticTools.formatNumber(countIndicator.getTimeMax()),fastAccessBuilder.getXMLSelector(countIndicator,IndicatorMode.MAXIMUM));
						}
						endParagraph();
					}
				}
				if (resourceObj.getMode()==ModelResource.Mode.MODE_SCHEDULE) {
					beginParagraph();
					addLine(Language.tr("Statistics.Utilization.Schedule")+": "+resourceObj.getSchedule());
					endParagraph();
				}
			}

			beginParagraph();
			if (rho>=0) addLine(Language.tr("Statistics.Utilization")+" rho="+StatisticTools.formatPercent(rho));
			addLine(Language.tr("Statistics.Utilization.Average")+"="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.Utilization.StdDev")+"="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.Utilization.Variance")+"="+StatisticTools.formatNumber(indicator.getTimeVar()));
			addLine(Language.tr("Statistics.Utilization.CV")+"="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.Utilization.Minimum")+"="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.Utilization.Maximum")+"="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			beginParagraph();
			addLine(Language.tr("Statistics.IdleShare")+"="+StatisticTools.formatPercent(indicator.getTimePartForState(0)));
			if (variable) {
				addLine(Language.tr("Statistics.FullyBusyShare.ByMaxNumber")+"="+StatisticTools.formatPercent(indicator.getTimePartForState(indicator.getTimeMax())));
			} else {
				addLine(Language.tr("Statistics.FullyBusyShare")+"="+StatisticTools.formatPercent(indicator.getTimePartForState(indicator.getTimeMax())));
			}
			endParagraph();

			if (resourceObj!=null && resourceObj.getFailures().size()>0) {
				beginParagraph();
				addLine(Language.tr("Statistics.Utilization.FailureInfo"));
				endParagraph();

				final StatisticsTimePerformanceIndicator indicator2=(StatisticsTimePerformanceIndicator)(statistics.resourceInDownTime.get(resource));
				if (indicator2!=null) {
					beginParagraph();
					addLine(Language.tr("Statistics.FailureTime.Average")+"="+StatisticTools.formatNumber(indicator2.getTimeMean()),xmlMean(indicator2));
					addLine(Language.tr("Statistics.FailureTime.StdDev")+"="+StatisticTools.formatNumber(indicator2.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.SD));
					addLine(Language.tr("Statistics.FailureTime.Variance")+"="+StatisticTools.formatNumber(indicator2.getTimeVar()));
					addLine(Language.tr("Statistics.FailureTime.CV")+"="+StatisticTools.formatNumber(indicator2.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.CV));
					addLine(Language.tr("Statistics.FailureTime.Minimum")+"="+StatisticTools.formatNumber(indicator2.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.FailureTime.Maximum")+"="+StatisticTools.formatNumber(indicator2.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.MAXIMUM));
					if (resourceObj.getMode()==ModelResource.Mode.MODE_NUMBER) {
						final int count=resourceObj.getCount();
						if (count>0) addLine(Language.tr("Statistics.FailureTime.AveragePartOfDownTimeOperators")+": "+StatisticTools.formatPercent(indicator2.getTimeMean()/count));
					}
					endParagraph();
				}
			}
		}

		/* Infotext  */
		addDescription("UtilizationResource");
	}

	private void buildTransporterUtilization() {

		addHeading(1,Language.tr("Statistics.TransporterUtilization"));

		final StatisticsMultiPerformanceIndicator indicators=statistics.transporterUtilization;
		for (String resource : indicators.getNames()) {
			addHeading(2,resource);
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(resource));
			final ModelTransporter transporter=statistics.editModel.transporters.get(resource);
			beginParagraph();
			if (transporter!=null) {
				final int count=transporter.getCountAll();
				addLine(Language.tr("Statistics.TransporterUtilization.NumberOfTransporters")+": "+count);
				addLine(Language.tr("Statistics.TransporterUtilization")+" rho="+StatisticTools.formatPercent(indicator.getTimeMean()/count));
			}
			addLine(Language.tr("Statistics.TransporterUtilization.Average")+"="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.TransporterUtilization.StdDev")+"="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.TransporterUtilization.Variance")+"="+StatisticTools.formatNumber(indicator.getTimeVar()));
			addLine(Language.tr("Statistics.TransporterUtilization.CV")+"="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.TransporterUtilization.Minimum")+"="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.TransporterUtilization.Maximum")+"="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			beginParagraph();
			addLine(Language.tr("Statistics.TransporterUtilization.IdleShare")+"="+StatisticTools.formatPercent(indicator.getTimePartForState(0)));
			addLine(Language.tr("Statistics.TransporterUtilization.FullyBusyShare")+"="+StatisticTools.formatPercent(indicator.getTimePartForState(indicator.getTimeMax())));
			endParagraph();

			if (transporter!=null && transporter.getFailures().size()>0) {
				beginParagraph();
				addLine(Language.tr("Statistics.Utilization.FailureInfo.Transporters"));
				endParagraph();

				final StatisticsTimePerformanceIndicator indicator2=(StatisticsTimePerformanceIndicator)(statistics.transporterInDownTime.get(resource));
				if (indicator2!=null) {
					beginParagraph();
					addLine(Language.tr("Statistics.FailureTime.Average.Transporters")+"="+StatisticTools.formatNumber(indicator2.getTimeMean()),xmlMean(indicator2));
					addLine(Language.tr("Statistics.FailureTime.StdDev.Transporters")+"="+StatisticTools.formatNumber(indicator2.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.SD));
					addLine(Language.tr("Statistics.FailureTime.Variance.Transporters")+"="+StatisticTools.formatNumber(indicator2.getTimeVar()));
					addLine(Language.tr("Statistics.FailureTime.CV.Transporters")+"="+StatisticTools.formatNumber(indicator2.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.CV));
					addLine(Language.tr("Statistics.FailureTime.Minimum.Transporters")+"="+StatisticTools.formatNumber(indicator2.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.FailureTime.Maximum.Transporters")+"="+StatisticTools.formatNumber(indicator2.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.MAXIMUM));
					final int count=transporter.getCountAll();
					if (count>0) addLine(Language.tr("Statistics.FailureTime.AveragePartOfDownTimeTransporters")+": "+StatisticTools.formatPercent(indicator2.getTimeMean()/count));
				}
				endParagraph();
			}
		}

		/* Infotext  */
		addDescription("UtilizationTransporter");
	}

	private void buildCounterInt(final int level, final boolean details) {
		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		/* Normale Zähler */
		List<String> groups=new ArrayList<>();
		for (String fullName: statistics.counter.getNames()) {
			final String[] parts=fullName.split("-",2);
			if (parts.length==2) {
				boolean inList=false;
				for (String s: groups) if (s.equalsIgnoreCase(parts[0])) {inList=true; break;}
				if (!inList) groups.add(parts[0]);
			}
		}

		for (String group: groups) {
			final List<String> counterName=new ArrayList<>();
			final List<StatisticsSimpleCountPerformanceIndicator> counter=new ArrayList<>();
			final List<Long> counterValue=new ArrayList<>();
			long sum=0;

			for (String fullName: statistics.counter.getNames()) {
				final String[] parts=fullName.split("-",2);
				if (parts.length==2 && parts[0].equalsIgnoreCase(group)) {
					counterName.add(parts[1]);
					final StatisticsSimpleCountPerformanceIndicator c=(StatisticsSimpleCountPerformanceIndicator)statistics.counter.get(fullName);
					counter.add(c);
					final long value=c.get();
					counterValue.add(value);
					sum+=value;
				}
			}

			addHeading(level,Language.tr("Statistics.CounterGroup")+": "+group);
			beginParagraph();
			if (counterName.size()==1) {
				addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(counterValue.get(0))+repeatInfo,xmlCount(counter.get(0)));
			} else {
				for (int i=0;i<counterName.size();i++) {
					final long value=counterValue.get(i);
					addLine(Language.tr("Statistics.Counter")+" "+counterName.get(i)+": "+NumberTools.formatLong(value)+ " ("+Language.tr("Statistics.ShareInThisGroup")+": "+StatisticTools.formatPercent(((double)value)/sum,details?2:1)+")",xmlCount(counter.get(i)));
				}
				if (statistics.simulationData.runRepeatCount>1) {
					addLine(Language.tr("Statistics.Counter.RepeatInfo"));
				}
			}
			endParagraph();
		}

		/* Differenzzähler */

		if (statistics.differentialCounter.size()>0) {
			addHeading(level,Language.tr("Statistics.DifferenceCounter"));
			if (!details) beginParagraph();
			for (String name: statistics.differentialCounter.getNames()) {
				StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)statistics.differentialCounter.get(name);
				if (details) {
					addHeading(level+1,name);
					beginParagraph();
					addLine(Language.tr("Statistics.Average")+"="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
					addLine(Language.tr("Statistics.StdDev")+"="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
					addLine(Language.tr("Statistics.Variance")+"="+StatisticTools.formatNumber(indicator.getTimeVar()));
					addLine(Language.tr("Statistics.CV")+"="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
					addLine(Language.tr("Statistics.MinimumNumber")+"="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumNumber")+"="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();
				} else {
					addLine(name+" "+Language.tr("Statistics.Average")+": "+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
				}
			}
			if (!details) endParagraph();
		}
	}

	private void buildCounter() {
		addHeading(1,Language.tr("Statistics.Counter"));

		if (statistics.counter.size()==0 && statistics.differentialCounter.size()==0) {
			beginParagraph();
			addLine(Language.tr("Statistics.Counter.NoCounter"));
			endParagraph();
		} else {
			buildCounterInt(2,true);
		}

		/* Infotext  */
		addDescription("Counter");
	}

	private void buildThroughputInt() {
		if (statistics.throughputStatistics.size()>0) {
			beginParagraph();
			for (String name: statistics.throughputStatistics.getNames()) {
				StatisticsQuotientPerformanceIndicator indicator=(StatisticsQuotientPerformanceIndicator)statistics.throughputStatistics.get(name);
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
				addLine(Language.tr("Statistics.Throughput")+" "+name+": "+StatisticTools.formatNumber(value,2)+" (1/"+unit+")",fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.QUOTIENT));
			}
			endParagraph();
		}
	}

	private void buildThroughput() {
		addHeading(1,Language.tr("Statistics.Throughput"));

		if (statistics.throughputStatistics.size()==0) {
			beginParagraph();
			addLine(Language.tr("Statistics.Throughput.NoThroughput"));
			endParagraph();
		} else {
			buildThroughputInt();
		}

		/* Infotext  */
		addDescription("Throughput");
	}

	private void buildStateStatistics() {
		addHeading(1,Language.tr("Statistics.StateStatistics"));

		if (statistics.stateStatistics.size()==0) {
			beginParagraph();
			addLine(Language.tr("Statistics.StateStatistics.NoStateStatistics"));
			endParagraph();
		} else {
			for (String group: statistics.stateStatistics.getNames()) {
				addHeading(2,group);
				final StatisticsStateTimePerformanceIndicator stateStatistics=(StatisticsStateTimePerformanceIndicator)statistics.stateStatistics.get(group);
				final double sum=stateStatistics.getSum();
				beginParagraph();
				for (Map.Entry<String,Double> entry: stateStatistics.get().entrySet()) {
					double value=entry.getValue();
					double part=(sum==0)?1:value/sum;
					addLine(String.format(Language.tr("Statistics.StateStatistics.Info"),entry.getKey(),TimeTools.formatLongTime(value),StatisticTools.formatPercent(part)));
				}
				endParagraph();
			}
		}

		/* Infotext  */
		addDescription("StateStatistics");
	}

	private void buildAnalogStatistics() {
		addHeading(1,Language.tr("Statistics.AnalogStatistics"));

		if (statistics.analogStatistics.size()==0) {
			beginParagraph();
			addLine(Language.tr("Statistics.AnalogStatistics.NoAnalogStatistics"));
			endParagraph();
		} else {
			for (String name: statistics.analogStatistics.getNames()) {
				addHeading(2,name);
				final StatisticsTimeAnalogPerformanceIndicator analogStatistics=(StatisticsTimeAnalogPerformanceIndicator)statistics.analogStatistics.get(name);
				beginParagraph();
				addLine(Language.tr("Statistics.Average")+"="+StatisticTools.formatNumber(analogStatistics.getMean()),xmlMean(analogStatistics));
				addLine(Language.tr("Statistics.Minimum")+"="+StatisticTools.formatNumber(analogStatistics.getMin()),fastAccessBuilder.getXMLSelector(analogStatistics,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.Maximum")+"="+StatisticTools.formatNumber(analogStatistics.getMax()),fastAccessBuilder.getXMLSelector(analogStatistics,IndicatorMode.MAXIMUM));
				endParagraph();
			}
		}

		/* Infotext  */
		addDescription("AnalogStatistics");
	}

	private void buildCosts() {
		addHeading(1,Language.tr("Statistics.Costs"));

		/* Kunden */

		addHeading(2,Language.tr("Statistics.CostsByWaitingTransferProcessTimesByClients"));

		final List<String> names=new ArrayList<>();

		for (String name: statistics.clientsCostsWaiting.getNames()) if (names.indexOf(name)<0) names.add(name);
		for (String name: statistics.clientsCostsTransfer.getNames()) if (names.indexOf(name)<0) names.add(name);
		for (String name: statistics.clientsCostsProcess.getNames()) if (names.indexOf(name)<0) names.add(name);

		double waiting=0;
		double transfer=0;
		double process=0;
		long count=0;

		for (String name: names) {
			addHeading(3,name);
			final StatisticsValuePerformanceIndicator wIndicator=(StatisticsValuePerformanceIndicator)statistics.clientsCostsWaiting.get(name);
			final StatisticsValuePerformanceIndicator tIndicator=(StatisticsValuePerformanceIndicator)statistics.clientsCostsTransfer.get(name);
			final StatisticsValuePerformanceIndicator pIndicator=(StatisticsValuePerformanceIndicator)statistics.clientsCostsProcess.get(name);
			final double w=wIndicator.getValue();
			final double t=tIndicator.getValue();
			final double p=pIndicator.getValue();
			final long c=((StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.get(name)).getCount();
			waiting+=w;
			transfer+=t;
			process+=p;
			count+=c;

			beginParagraph();
			addLine(Language.tr("Statistics.Costs.ByWaitingTimes")+" ("+Language.tr("Statistics.total")+"): "+StatisticTools.formatNumberLong(w),fastAccessBuilder.getXMLSelector(wIndicator,IndicatorMode.VALUE));
			addLine(Language.tr("Statistics.Costs.ByTransferTimes")+" ("+Language.tr("Statistics.total")+"): "+StatisticTools.formatNumberLong(t),fastAccessBuilder.getXMLSelector(tIndicator,IndicatorMode.VALUE));
			addLine(Language.tr("Statistics.Costs.ByProcessTimes")+" ("+Language.tr("Statistics.total")+"): "+StatisticTools.formatNumberLong(p),fastAccessBuilder.getXMLSelector(pIndicator,IndicatorMode.VALUE));
			addLine(Language.tr("Statistics.Costs.Total")+" ("+Language.tr("Statistics.total")+"): "+StatisticTools.formatNumberLong(w+t+p));
			endParagraph();
			if (count>0) {
				beginParagraph();
				addLine(Language.tr("Statistics.Costs.ByWaitingTimes")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+StatisticTools.formatNumberLong(w/c));
				addLine(Language.tr("Statistics.Costs.ByTransferTimes")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+StatisticTools.formatNumberLong(t/c));
				addLine(Language.tr("Statistics.Costs.ByProcessTimes")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+StatisticTools.formatNumberLong(p/c));
				addLine(Language.tr("Statistics.Costs.Total")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+StatisticTools.formatNumberLong((w+t+p)/c));
				endParagraph();
			}
		}

		addHeading(3,Language.tr("Statistics.SumOverAllClientTypes"));
		beginParagraph();
		addLine(Language.tr("Statistics.Costs.ByWaitingTimes")+" ("+Language.tr("Statistics.total")+"): "+StatisticTools.formatNumberLong(waiting));
		addLine(Language.tr("Statistics.Costs.ByTransferTimes")+" ("+Language.tr("Statistics.total")+"): "+StatisticTools.formatNumberLong(transfer));
		addLine(Language.tr("Statistics.Costs.ByProcessTimes")+" ("+Language.tr("Statistics.total")+"): "+StatisticTools.formatNumberLong(process));
		addLine(Language.tr("Statistics.Costs.Total")+" ("+Language.tr("Statistics.total")+"): "+StatisticTools.formatNumberLong(waiting+transfer+process));
		endParagraph();
		if (count>0) {
			beginParagraph();
			addLine(Language.tr("Statistics.Costs.ByWaitingTimes")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+StatisticTools.formatNumberLong(waiting/count));
			addLine(Language.tr("Statistics.Costs.ByTransferTimes")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+StatisticTools.formatNumberLong(transfer/count));
			addLine(Language.tr("Statistics.Costs.ByProcessTimes")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+StatisticTools.formatNumberLong(process/count));
			addLine(Language.tr("Statistics.Costs.Total")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+StatisticTools.formatNumberLong((waiting+transfer+process)/count));
			endParagraph();
		}

		/* Stationen */

		addHeading(2,Language.tr("Statistics.CostsAtStations"));

		double station=0;
		for (String name: statistics.stationCosts.getNames()) station+=((StatisticsValuePerformanceIndicator)statistics.stationCosts.get(name)).getValue();
		if (station==0) station=1;

		double sum=0;
		for (String name: statistics.stationCosts.getNames()) {
			addHeading(3,name);
			final StatisticsValuePerformanceIndicator indicator=(StatisticsValuePerformanceIndicator)statistics.stationCosts.get(name);
			final double s=indicator.getValue();
			sum+=s;
			beginParagraph();
			addLine(Language.tr("Statistics.CostsAtStation")+" \""+name+"\": "+StatisticTools.formatNumberLong(s)+" ("+Language.tr("Statistics.Part")+": "+StatisticTools.formatPercent(s/station)+")",fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.VALUE));
			endParagraph();
		}

		addHeading(3,Language.tr("Statistics.SumOverAllStations"));
		beginParagraph();
		addLine(Language.tr("Statistics.Costs")+": "+StatisticTools.formatNumberLong(sum));
		endParagraph();

		/* Ressourcen */

		addHeading(2,Language.tr("Statistics.CostsForResources"));

		double time=0;
		double work=0;
		double idle=0;

		for (String name: statistics.resourceTimeCosts.getNames()) {
			addHeading(3,name);
			final StatisticsValuePerformanceIndicator tIndicator=(StatisticsValuePerformanceIndicator)statistics.resourceTimeCosts.get(name);
			final StatisticsValuePerformanceIndicator wIndicator=(StatisticsValuePerformanceIndicator)statistics.resourceWorkCosts.get(name);
			final StatisticsValuePerformanceIndicator iIndicator=(StatisticsValuePerformanceIndicator)statistics.resourceIdleCosts.get(name);
			final double t=tIndicator.getValue();
			final double w=wIndicator.getValue();
			final double i=iIndicator.getValue();
			time+=t;
			work+=w;
			idle+=i;

			beginParagraph();
			addLine(Language.tr("Statistics.CostsForResources.Available.Single")+": "+StatisticTools.formatNumberLong(t),fastAccessBuilder.getXMLSelector(tIndicator,IndicatorMode.VALUE));
			addLine(Language.tr("Statistics.CostsForResources.Working.Single")+": "+StatisticTools.formatNumberLong(w),fastAccessBuilder.getXMLSelector(wIndicator,IndicatorMode.VALUE));
			addLine(Language.tr("Statistics.CostsForResources.Idle.Single")+": "+StatisticTools.formatNumberLong(i),fastAccessBuilder.getXMLSelector(iIndicator,IndicatorMode.VALUE));
			addLine(Language.tr("Statistics.Costs.Total")+": "+StatisticTools.formatNumberLong(t+w+i));
			endParagraph();

		}

		addHeading(3,Language.tr("Statistics.SumOverAllResources"));
		beginParagraph();
		addLine(Language.tr("Statistics.CostsForResources.Available.All")+": "+StatisticTools.formatNumberLong(time));
		addLine(Language.tr("Statistics.CostsForResources.Working.All")+": "+StatisticTools.formatNumberLong(work));
		addLine(Language.tr("Statistics.CostsForResources.Idle.All")+": "+StatisticTools.formatNumberLong(idle));
		addLine(Language.tr("Statistics.Costs.Total")+": "+StatisticTools.formatNumberLong(time+work+idle));
		endParagraph();

		/* Info */

		if (statistics.simulationData.runRepeatCount>1) {
			beginParagraph();
			addLine(Language.tr("Statistics.Costs.RepeatInfo"));
			endParagraph();
		}

		/* Infotext  */
		addDescription("Costs");
	}

	/**
	 * Korrelationslevels zu denen angegeben werden soll, ab welchem
	 * Abstand dieser Wert erreicht bzw. unterschritten wird.
	 */
	public final static double[] AUTOCORRELATION_LEVELS=new double[]{0.1,0.05,0.01,0.005,0.001};

	private void outputAutocorrelationData(final StatisticsDataPerformanceIndicator indicator, final int[] maxDistance) {
		beginParagraph();
		final int maxSize=(indicator.getCorrelationData().length-1)*StatisticsDataPerformanceIndicator.CORRELATION_RANGE_STEPPING;
		for (int i=0;i<AUTOCORRELATION_LEVELS.length;i++) {
			final double level=AUTOCORRELATION_LEVELS[i];
			final int distance=indicator.getCorrelationLevelDistance(level);
			maxDistance[i]=Math.max(maxDistance[i],distance);
			if (distance>maxSize) {
				addLine(String.format(Language.tr("Statistics.ResultsAutocorrelation.LineMoreThan"),StatisticTools.formatPercent(level),NumberTools.formatLong(maxSize)));
			} else {
				addLine(String.format(Language.tr("Statistics.ResultsAutocorrelation.Line"),StatisticTools.formatPercent(level),NumberTools.formatLong(distance)));
			}
		}
		endParagraph();
	}

	private void buildAutocorrelation() {
		addHeading(1,Language.tr("Statistics.ResultsAutocorrelation"));

		int[] maxDistance=new int[AUTOCORRELATION_LEVELS.length];
		boolean heading;
		boolean individualData=false;
		StatisticsDataPerformanceIndicator indicator;

		/* Keine Daten vorhanden? */

		if (!statistics.clientsAllWaitingTimes.isCorrelationAvailable()) {
			beginParagraph();
			addLine(Language.tr("Statistics.ResultsAutocorrelation.NoData"));
			endParagraph();
			return;
		}

		/* Autokorrelation über die Wartezeit über alle Kunden */

		addHeading(2,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.AllClients"));
		outputAutocorrelationData(statistics.clientsAllWaitingTimes,maxDistance);
		final int maxSize=(statistics.clientsAllWaitingTimes.getCorrelationData().length-1)*StatisticsDataPerformanceIndicator.CORRELATION_RANGE_STEPPING;

		/* Autokorrelation über die Wartezeiten der einzelnen Kundentypen */

		heading=false;
		for (String name: statistics.clientsWaitingTimes.getNames()) {
			indicator=(StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.get(name);
			if (indicator.isCorrelationAvailable()) {
				if (!heading) {addHeading(2,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.ByClientTypes")); heading=true;}
				individualData=true;
				addHeading(3,name);
				outputAutocorrelationData(indicator,maxDistance);
			}
		}

		/* Autokorrelation über die Wartezeiten an den Stationen */

		heading=false;
		for (String name: statistics.stationsWaitingTimes.getNames()) {
			indicator=(StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.get(name);
			if (indicator.isCorrelationAvailable()) {
				if (!heading) {addHeading(2,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.ByStations")); heading=true;}
				individualData=true;
				addHeading(3,name);
				outputAutocorrelationData(indicator,maxDistance);
			}
		}

		/* Autokorrelation über die Wartezeiten an den Stationen ausdifferenziert nach Kundentypen */

		heading=false;
		for (String name: statistics.stationsWaitingTimesByClientType.getNames()) {
			indicator=(StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimesByClientType.get(name);
			if (indicator.isCorrelationAvailable()) {
				if (!heading) {addHeading(2,Language.tr("Statistics.ResultsAutocorrelation.WaitingTime.ByStationsAndClientTypes")); heading=true;}
				individualData=true;
				addHeading(3,name);
				outputAutocorrelationData(indicator,maxDistance);
			}
		}

		/* Maximum über alle Werte (nur wenn individuelle Were vorliegen) */

		if (individualData) {
			addHeading(2,Language.tr("Statistics.ResultsAutocorrelation.Maximum"));
			beginParagraph();
			for (int i=0;i<AUTOCORRELATION_LEVELS.length;i++) {
				final double level=AUTOCORRELATION_LEVELS[i];
				if (maxDistance[i]>maxSize) {
					addLine(String.format(Language.tr("Statistics.ResultsAutocorrelation.LineMoreThan"),StatisticTools.formatPercent(level),NumberTools.formatLong(maxSize)));
				} else {
					addLine(String.format(Language.tr("Statistics.ResultsAutocorrelation.Line"),StatisticTools.formatPercent(level),NumberTools.formatLong(maxDistance[i])));
				}
			}
			endParagraph();
		}

		/* Allgemeine Informationen zu den Autokorrelationsdaten */

		beginParagraph();
		addLines(Language.tr("Statistics.ResultsAutocorrelation.Step"));
		endParagraph();

		/* Infotext  */
		addDescription("Autocorrleation");
	}

	private boolean isUserStatisticsTime(final String key) {
		for (ModelElement element: statistics.editModel.surface.getElements()) {
			if (element instanceof ModelElementUserStatistic) {
				final Boolean B=((ModelElementUserStatistic)element).getIsTimeForKey(key);
				if (B!=null) return B.booleanValue();
			}
			if (element instanceof ModelElementUserStatistic) {
				for (ModelElement sub: ((ModelElementUserStatistic)element).getSurface().getElements()) {
					if (sub instanceof ModelElementUserStatistic) {
						final Boolean B=((ModelElementUserStatistic)sub).getIsTimeForKey(key);
						if (B!=null) return B.booleanValue();
					}
				}
			}
		}

		return true;
	}

	private void buildUserStatistics() {
		addHeading(1,Language.tr("Statistics.UserStatistics"));

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		if (statistics.userStatistics.size()==0) {
			beginParagraph();
			addLine(Language.tr("Statistics.UserStatistics.NothingRecorded"));
			endParagraph();
		}

		for (String name: statistics.userStatistics.getNames()) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.userStatistics.get(name);
			addHeading(2,name);
			beginParagraph();
			addLine(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
			if (isUserStatisticsTime(name)) {
				addLine(Language.tr("Statistics.AverageUserTime")+": E[X]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
				addLine(Language.tr("Statistics.StdDevUserTime")+": Std[X]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
				addLine(Language.tr("Statistics.VarianceUserTime")+": Var[X]="+timeAndNumber(indicator.getVar()));
				addLine(Language.tr("Statistics.CVUserTime")+": CV[X]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
				addLine(Language.tr("Statistics.MinimumUserTime")+": Min[X]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumUserTime")+": Max[X]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
				endParagraph();

				outputQuantilInfoTime("X",indicator);

				outputConfidenceData(indicator);
			} else {
				addLine(Language.tr("Statistics.AverageUser")+": E[X]="+StatisticTools.formatNumber(indicator.getMean()),xmlMean(indicator));
				addLine(Language.tr("Statistics.StdDevUser")+": Std[X]="+StatisticTools.formatNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
				addLine(Language.tr("Statistics.VarianceUser")+": Var[X]="+StatisticTools.formatNumber(indicator.getVar()));
				addLine(Language.tr("Statistics.CVUser")+": CV[X]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
				addLine(Language.tr("Statistics.MinimumUser")+": Min[X]="+StatisticTools.formatNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumUser")+": Max[X]="+StatisticTools.formatNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
				endParagraph();

				outputQuantilInfoNumber("X",indicator);
			}
		}

		/* Infotext  */
		addDescription("UserStatistics");
	}

	private void buildClientData() {
		addHeading(1,Language.tr("Statistics.ClientData"));

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		for (String name: statistics.clientData.getNames()) {
			final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)statistics.clientData.get(name);
			final String field=String.format(Language.tr("Statistics.ClientData.Field"),name);
			addHeading(2,field);
			beginParagraph();
			addLine(Language.tr("Statistics.Number")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo);
			addLine(Language.tr("Statistics.ClientData.Field.Average")+": E["+field+"]="+StatisticTools.formatNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.ClientData.Field.StdDev")+": Std["+field+"]="+StatisticTools.formatNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.ClientData.Field.Variance")+": Var["+field+"]="+StatisticTools.formatNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.ClientData.Field.CV")+": CV["+field+"]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.ClientData.Field.Minimum")+": Min["+field+"]="+StatisticTools.formatNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.ClientData.Field.Maximum")+": Max["+field+"]="+StatisticTools.formatNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoNumber(field,indicator);
		}

		/* Infotext  */
		addDescription("ClientData");
	}

	@Override
	protected void buildText() {
		switch (mode) {
		case MODE_OVERVIEW: buildOverview(); break;
		case MODE_MODEL: buildModelInfo(); break;
		case MODE_MODEL_DESCRIPTION: buildModelDescription(); break;
		case MODE_SYSTEM_INFO: buildSystemInfo(); break;
		case MODE_INTERARRIVAL_CLIENTS: buildInterarrivalSystem(); break;
		case MODE_INTERARRIVAL_STATIONS: buildInterarrivalStations(); break;
		case MODE_INTERLEAVE_CLIENTS: buildInterleaveSystem(); break;
		case MODE_INTERLEAVE_STATIONS: buildInterleaveStations(); break;
		case MODE_WAITINGPROCESSING_CLIENTS: buildClients(); break;
		case MODE_WAITINGPROCESSING_STATIONS: buildStations(); break;
		case MODE_WAITINGPROCESSING_STATIONS_CLIENTS: buildStationsClients(); break;
		case MODE_CLIENTS_COUNT: buildClientsCount(); break;
		case MODE_UTILIZATION: buildUtilization(); break;
		case MODE_COUNTER: buildCounter(); break;
		case MODE_THROUGHPUT: buildThroughput(); break;
		case MODE_STATE_STATISTICS: buildStateStatistics(); break;
		case MODE_ANALOG_STATISTICS: buildAnalogStatistics(); break;
		case MODE_COSTS: buildCosts(); break;
		case MODE_AUTOCORRELATION: buildAutocorrelation(); break;
		case MODE_USER_STATISTICS: buildUserStatistics(); break;
		case MODE_TRANSPORTER_UTILIZATION: buildTransporterUtilization(); break;
		case MODE_CLIENT_DATA: buildClientData(); break;
		}
	}
}