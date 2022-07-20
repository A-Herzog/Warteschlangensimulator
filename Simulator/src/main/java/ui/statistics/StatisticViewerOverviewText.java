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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.Icon;
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
import statistics.StatisticsSimpleValueMaxPerformanceIndicator;
import statistics.StatisticsSimpleValuePerformanceIndicator;
import statistics.StatisticsStateTimePerformanceIndicator;
import statistics.StatisticsTimeAnalogPerformanceIndicator;
import statistics.StatisticsTimeContinuousPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import statistics.StatisticsValuePerformanceIndicator;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import systemtools.images.SimToolsImages;
import systemtools.statistics.StatisticViewerText;
import systemtools.statistics.StatisticsBasePanel;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.elements.ElementWithOutputFile;
import ui.modeleditor.elements.ModelElementUserStatistic;
import ui.statistics.FastAccessSelectorBuilder.IndicatorMode;

/**
 * Dieser Viewer gibt allgemeine Informationen in Textform zu den Simulationsergebnissen aus.
 * @see StatisticViewerText
 * @author Alexander Herzog
 */
public class StatisticViewerOverviewText extends StatisticViewerText {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Gibt an, welche Daten genau ausgegeben werden sollen */
	private final Mode mode;
	/** Callback, das aufgerufen wird, wenn ein "Details"-Link angeklickt wurde */
	private final Consumer<Mode> modeClick;
	/** System zu Erstellung von Ausdrücken zum Abruf von XML-Daten */
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
		/** Dateiausgaben */
		FILE_OUTPUT,
		/** Simulierte Zeit */
		MODE_SIM_TIME,
		/** Informationen zum Simulationssystem */
		MODE_SYSTEM_INFO,
		/** Ankünfte pro Thread */
		MODE_SYSTEM_INFO_THREAD_BALANCE,
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
		/** Wartezeiten an den Stationen (Einzelzeiten der Kunden summiert) */
		MODE_WAITINGPROCESSING_STATIONS_TOTAL,
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
		MODE_CLIENT_DATA,
		/** Statistik über die globalen Variablen */
		MODE_USER_VARIABLES
	}

	/**
	 * Zielangabe für Details-Links, wenn diese eine Information zur Bestimmung
	 * von Konfidenzintervallen aufrufen sollen.
	 */
	private static final String MORE_CONFIDENCE_DATA_LINK="MoreConfidence";

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

	/**
	 * Liefert einen Stationsnamen
	 * @param statisticName	Eingabe-Stationsname (kann <code>null</code> sein)
	 * @return	Ausgabe-Stationsname (ist nie <code>null</code>)
	 */
	private String fullStationName(final String statisticName) {
		if (statisticName==null || statisticName.trim().isEmpty()) return "";
		return statisticName;
	}

	/**
	 * Standardmäßig zu verwendende Konfidenzniveaus, wenn keine spezifischen
	 * Niveaus in den Einstellungen definiert sind.
	 * @see #getConfidenceLevels()
	 */
	private static final double[] DEFAULT_CONFIDENCE_LEVELS=new double[]{0.1,0.05,0.01};

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

	/**
	 * Gibt Konfidenzdaten (sofern diese vorliegen) aus.
	 * @param indicator	Staitstikobjekt für das die Konfidenzdaten ausgegeben werden sollen
	 */
	private void outputConfidenceData(final StatisticsDataPerformanceIndicator indicator) {
		/* Batch-Means-Konfidenzintervalle */
		if (indicator.getBatchCount()>1) {
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
			addLine(String.format(Language.tr("Statistics.Confidence.Info"),NumberTools.formatLong(indicator.getBatchCount()),NumberTools.formatLong(indicator.getBatchSize()),StatisticTools.formatNumber(indicator.getBatchSD())));
			endParagraph();
			return;
		}

		/* Konfidenzintervall auf Basis der Wiederholungen der Simulation */
		if (statistics.simulationData.runRepeatCount>1 && indicator.getRunCount()>1) {
			beginParagraph();
			final double m=indicator.getMean();
			for (double level: getConfidenceLevels()) {
				final double w=indicator.getRunConfidenceHalfWide(level);
				addLine(String.format(
						Language.tr("Statistics.Confidence.Level"),
						StatisticTools.formatPercent(1-level),
						StatisticTools.formatNumber(m-w),
						StatisticTools.formatNumber(m+w),
						StatisticTools.formatNumber(w)
						));

			}
			addLine(String.format(Language.tr("Statistics.Confidence.InfoRun"),NumberTools.formatLong(indicator.getRunCount()),StatisticTools.formatNumber(indicator.getRunSD())));
			endParagraph();
			return;
		}
	}

	/**
	 * Gibt Konfidenzdaten (sofern diese vorliegen) aus.
	 * @param indicator	Staitstikobjekt für das die Konfidenzdaten ausgegeben werden sollen
	 */
	private void outputConfidenceData(final StatisticsDataPerformanceIndicatorWithNegativeValues indicator) {
		/* Batch-Means-Konfidenzintervalle */
		if (indicator.getBatchCount()>1) {
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
			addLine(String.format(Language.tr("Statistics.Confidence.Info"),NumberTools.formatLong(indicator.getBatchCount()),NumberTools.formatLong(indicator.getBatchSize()),StatisticTools.formatNumber(indicator.getBatchSD())));
			endParagraph();
			return;
		}

		/* Konfidenzintervall auf Basis der Wiederholungen der Simulation */
		if (statistics.simulationData.runRepeatCount>1 && indicator.getRunCount()>1) {
			beginParagraph();
			final double m=indicator.getMean();
			for (double level: getConfidenceLevels()) {
				final double w=indicator.getRunConfidenceHalfWide(level);
				addLine(String.format(
						Language.tr("Statistics.Confidence.Level"),
						StatisticTools.formatPercent(1-level),
						StatisticTools.formatNumber(m-w),
						StatisticTools.formatNumber(m+w),
						StatisticTools.formatNumber(w)
						));

			}
			addLine(String.format(Language.tr("Statistics.Confidence.InfoRun"),NumberTools.formatLong(indicator.getRunCount()),StatisticTools.formatNumber(indicator.getRunSD())));
			endParagraph();
			return;
		}
	}

	/**
	 * Gibt Konfidenzdaten (sofern diese vorliegen) aus.
	 * @param indicator	Staitstikobjekt für das die Konfidenzdaten ausgegeben werden sollen
	 */
	private void outputConfidenceData(final StatisticsTimePerformanceIndicator indicator) {
		/* Konfidenzintervall auf Basis der Wiederholungen der Simulation */
		if (statistics.simulationData.runRepeatCount>1 && indicator.getRunCount()>1) {
			beginParagraph();
			final double m=indicator.getTimeMean();
			for (double level: getConfidenceLevels()) {
				final double w=indicator.getRunConfidenceHalfWide(level);
				addLine(String.format(
						Language.tr("Statistics.Confidence.Level"),
						StatisticTools.formatPercent(1-level),
						StatisticTools.formatNumber(m-w),
						StatisticTools.formatNumber(m+w),
						StatisticTools.formatNumber(w)
						));

			}
			addLine(String.format(Language.tr("Statistics.Confidence.InfoRun"),NumberTools.formatLong(indicator.getRunCount()),StatisticTools.formatNumber(indicator.getRunSD())));
			endParagraph();
			return;
		}
	}

	/**
	 * Gibt die über die Thread-Aufteilung ermittelten Konfidenzdaten aus.
	 */
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
		}

		addLink(MORE_CONFIDENCE_DATA_LINK,Language.tr("Statistics.Confidence.More"));
		endParagraph();
	}

	/**
	 * Fügt einen Link auf eine andere Statistikseite als "Details"-Link in die Ausgabe ein.
	 * @param mode	Zielseite für den "Details"-Link
	 */
	private void addModeLink(final Mode mode) {
		addLink(mode.toString(),Language.tr("Statistics.Details"));
	}

	/**
	 * Zeigt im Fußbereich der Hilfeseite eine "Erklärung einblenden"-Schaltfläche, die,
	 * wenn sie angeklickt wird, eine html-Hilfeseite anzeigt.
	 * @param topic	Hilfe-Thema (wird als Datei in den "description_*"-Ordern gesucht)
	 */
	private void addDescription(final String topic) {
		final URL url=StatisticViewerOverviewText.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	@Override
	protected JPopupMenu processContextClick(final StatisticsBasePanel owner, final String hint) {
		final JPopupMenu popup=new JPopupMenu();
		addOwnSettingsToPopup(owner,popup);
		fastAccessBuilder.addToPopup(popup,hint);
		return popup;
	}

	/**
	 * Zeigt (wenn vorhanden) Warnung und Informationen zu Simulation-Notabbruch-Aktionen an.
	 * @param fullInfo	Warnungen anzeigen (<code>true</code>) oder nur auf Abbrüche und Warnungen hinweisen (<code>false</code>)
	 */
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

	/**
	 * Liefert eine Zeitangabe als Zeit und als Dezimalzahl als Zeichenkette.
	 * @param value	Auszugebende Zeitangabe
	 * @return	Zeitangabe als Zeit und als Dezimalzahl
	 */
	private String timeAndNumber(final double value) {
		return StatisticTools.formatExactTime(value)+" ("+StatisticTools.formatNumber(value)+")";
	}

	/**
	 * Liefert den XML-Selektor um auf den Mittelwert ein bestimmtes Statistikobjekt per XML zuzugreifen.
	 * @param indicator	Statistikobjekt
	 * @return	XML-Selektor für den Mittelwert eines Statistikobjekts
	 */
	private String xmlMean(final StatisticsPerformanceIndicator indicator) {
		return fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MEAN);
	}

	/**
	 * Liefert den XML-Selektor um auf die Anzahl an Werten in einem bestimmten Statistikobjekt per XML zuzugreifen.
	 * @param indicator	Statistikobjekt
	 * @return	XML-Selektor für die Anzahl an Werten in einem Statistikobjekt
	 */
	private String xmlCount(final StatisticsPerformanceIndicator indicator) {
		return fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.COUNT);
	}

	/**
	 * Mögliche Teil-Ausgaben
	 * @see StatisticViewerOverviewText#buildOverview()
	 */
	public enum Filter {
		/** Simulationsmodell */
		MODEL(()->Language.tr("Statistics.SimulationModel"),viewer->viewer.buildOverviewModel()),
		/** Dateiausgaben */
		FILE_OUTPUT(()->Language.tr("Statistics.FileOutput"),viewer->viewer.buildFileOutput(false),viewer->hasFileOutput(viewer.statistics)),
		/** Mittlere Anzahl an Kunden */
		NUMBER_CLIENTS(()->Language.tr("Statistics.AverageNumberOfClients"),viewer->viewer.buildOverviewCountClients()),
		/** Zeiten nach Kunden */
		TIMES_CLIENTS(()->Language.tr("Statistics.TimesByClientTypes"),viewer->viewer.buildOverviewTimesClients()),
		/** Zeiten nach Stationen */
		TIMES_STATIONS(()->Language.tr("Statistics.TimesByStationen"),viewer->viewer.buildOverviewTimesStations()),
		/** Zeiten nach Stationen und Kundentypen */
		TIMES_STATIONS_CLIENTS(()->Language.tr("Statistics.TimesByStationsAndClientTypes"),viewer->viewer.buildOverviewTimesStationsClients()),
		/** Ressourcenauslastung */
		UTILIZATION_RESOURCES(()->Language.tr("Statistics.Utilization"),viewer->viewer.buildOverviewUtilizationResources()),
		/** Transporterauslastung */
		UTILIZATION_TRANSPORTERS(()->Language.tr("Statistics.TransporterUtilization"),viewer->viewer.buildOverviewUtilizationTransporters()),
		/** Zähler */
		COUNTERS(()->Language.tr("Statistics.Counter"),viewer->viewer.buildOverviewCounters()),
		/** Durchsatz */
		THROUGHPUT(()->Language.tr("Statistics.Throughput"),viewer->viewer.buildOverviewThroughput());

		/**
		 * Callback zur Ermittelung des Namens der Teil-Ausgabe
		 * @see #getName()
		 */
		private final Supplier<String> nameGetter;

		/**
		 * Ausgabemethode zur Erzeugung der Teil-Ausgabe
		 * @see #process(StatisticViewerOverviewText)
		 */
		private final Consumer<StatisticViewerOverviewText> output;

		/**
		 * Stehen entsprechende Statiatikdaten in dem Statistikobjekt zur Verfügung?<br>
		 * (Kann <code>null</code> sein, dann heißt das "Daten sind immer verfügbar".)
		 */
		private final Predicate<StatisticViewerOverviewText> hasTopic;

		/**
		 * Konstruktor des Enum
		 * @param nameGetter	Callback zur Ermittelung des Namens der Teil-Ausgabe
		 * @param output	Ausgabemethode zur Erzeugung der Teil-Ausgabe
		 * @param hasTopic	Stehen entsprechende Statiatikdaten in dem Statistikobjekt zur Verfügung? (Kann <code>null</code> sein, dann heißt das "Daten sind immer verfügbar".)
		 */
		Filter(final Supplier<String> nameGetter, final Consumer<StatisticViewerOverviewText> output, final Predicate<StatisticViewerOverviewText> hasTopic) {
			this.nameGetter=nameGetter;
			this.output=output;
			this.hasTopic=hasTopic;
		}

		/**
		 * Konstruktor des Enum
		 * @param nameGetter	Callback zur Ermittelung des Namens der Teil-Ausgabe
		 * @param output	Ausgabemethode zur Erzeugung der Teil-Ausgabe
		 */
		Filter(final Supplier<String> nameGetter, final Consumer<StatisticViewerOverviewText> output) {
			this.nameGetter=nameGetter;
			this.output=output;
			this.hasTopic=null;
		}

		/**
		 * Stehen Stehen entsprechende Statiatikdaten in dem Statistikobjekt zur Verfügung?
		 * @param viewer	Zu prüfendes Statistikobjekt
		 * @return	Liefert <code>true</code>, wenn das Statistikobjekt passende Daten enthält
		 */
		private boolean hasTopic(final StatisticViewerOverviewText viewer) {
			if (hasTopic==null) return true;
			return hasTopic.test(viewer);
		}

		/**
		 * Liefert den Namen der Teil-Ausgabe
		 * @return	Name der Teil-Ausgabe
		 */
		public String getName() {
			return nameGetter.get();
		}

		/**
		 * Führt die Ausgabe des Teil-Ausgabe-Elements aus.
		 * @param viewer	Statistik-Viewer über den die Ausgabe erfolgen soll
		 */
		public void process(final StatisticViewerOverviewText viewer) {
			output.accept(viewer);
		}

		/**
		 * Wandelt eine Konfigurations-Zeichenkette in eine Menge von Filter-Objekten um.
		 * @param text	Konfigurations-Zeichenkette mit Filter-Einstellungen
		 * @return	Menge mit aktiven Filter-Objekten
		 */
		public static Set<Filter> stringToSet(String text) {
			final Set<Filter> result=new HashSet<>();
			if (text==null) text="";
			final int count=values().length;
			while (text.length()<count) text+="X";
			for (int i=0;i<count;i++) if (text.charAt(i)!='-') result.add(values()[i]);
			return result;
		}

		/**
		 * Wandelt eine Menge mit aktiven Filter-Objekten in eine Konfigurations-Zeichenkette um.
		 * @param set	Menge mit aktiven Filter-Objekten
		 * @return	Konfigurations-Zeichenkette mit Filter-Einstellungen
		 */
		public static String setToString(final Set<Filter> set) {
			final StringBuilder result=new StringBuilder();
			for (Filter filter: values()) if (set!=null && set.contains(filter)) result.append('X'); else result.append('-');
			return result.toString();
		}

		/**
		 * Führt die Ausgaben für mehrere Filter aus.
		 * @param viewer	Statistik-Viewer über den die Ausgabe erfolgen soll
		 * @param filters	Menge der Filter, die angegeben werden sollen
		 */
		public static void process(final StatisticViewerOverviewText viewer, final Set<Filter> filters) {
			for (Filter filter: values()) if (filters!=null && filters.contains(filter) && filter.hasTopic(viewer)) filter.process(viewer);
		}

		/**
		 * Liefert den Standard-String für die Filtereinstellungen.
		 * @return	Standard-String für die Filtereinstellungen
		 */
		public static String getDefault() {
			final StringBuilder result=new StringBuilder();
			final int size=values().length;
			for (int i=0;i<size;i++) result.append('X');
			return result.toString();
		}
	}

	/**
	 * Teil-Ausgabe für "Modellüberblick" (Basisinformationen zum Modell als solches; hinterlegte Modellbeschreibung und weitere Informationen)<br>
	 * Ausgabe von "Simulationsmodell"
	 * @see #buildOverview()
	 * @see Mode#MODE_OVERVIEW
	 * @see Filter#MODEL
	 */
	private void buildOverviewModel() {
		addHeading(2,Language.tr("Statistics.SimulationModel"));
		beginParagraph();
		if (!statistics.editModel.name.trim().isEmpty()) addLine(Language.tr("Statistics.SimulationModel.Name")+": "+statistics.editModel.name);

		long sum=0;
		for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class)) sum+=indicator.getCount();
		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";
		addLine(Language.tr("Statistics.SimulatedClients")+": "+NumberTools.formatLong(sum)+repeatInfo);
		if (statistics.simulationData.runRepeatCount>1) addLine(Language.tr("Statistics.SystemData.RepeatCount")+": "+NumberTools.formatLong(statistics.simulationData.runRepeatCount));
		if (sum==0 && statistics.editModel.warmUpTime>0) {
			addLine(Language.tr("Statistics.SimulatedClients.Zero"));
			addLine(String.format(Language.tr("Statistics.SimulatedClients.Zero.Info"),NumberTools.formatLong(Math.round(statistics.editModel.clientCount*statistics.editModel.warmUpTime))));
		}
		addModeLink(Mode.MODE_MODEL);
		endParagraph();

		outputEmergencyShutDownInfo(false);
	}

	/**
	 * Teil-Ausgabe für "Modellüberblick" (Basisinformationen zum Modell als solches; hinterlegte Modellbeschreibung und weitere Informationen)<br>
	 * Ausgabe von "Dateiausgabe"
	 * @param full	Vollständige Seite (<code>true</code>) oder nur Teil einer Seite (<code>false</code>)
	 * @see #buildOverview()
	 * @see Mode#MODE_OVERVIEW
	 * @see Filter#FILE_OUTPUT
	 */
	private void buildFileOutput(final boolean full) {
		addHeading(full?1:2,Language.tr("Statistics.FileOutput"));

		final List<FileOutputInfo> list=getOutputFiles(statistics);
		if (list.size()==0) {
			beginParagraph();
			addLine(Language.tr("Statistics.FileOutput.InfoNoOutput"));
			endParagraph();
			return;
		}

		beginParagraph();
		addLine(Language.tr("Statistics.FileOutput.Info"));
		endParagraph();

		for (FileOutputInfo fileOutputInfo : list) {
			beginParagraph();
			addLine(fileOutputInfo.getFullName()+":");
			addLinkLine("file:"+fileOutputInfo.getFile(),fileOutputInfo.getFile());
			endParagraph();
		}
	}

	/**
	 * Prüft, ob in mehreren Statistikobjekten mindestens eins mit Dateiausgaben dabei ist.
	 * @param statistics	Zu prüfende Statistikobjekte
	 * @return	Liefert <code>true</code>, wenn mindestens eines mit Dateiausgaben dabei ist
	 */
	public static boolean hasFileOutput(final Statistics[] statistics) {
		for (Statistics statistic: statistics) if (hasFileOutput(statistic)) return true;
		return false;
	}

	/**
	 * Prüft, ob das Statistikobjekt Dateiausgaben beinhaltet.
	 * @param statistics	Zu prüfendes Statistikobjekt
	 * @return	Liefert <code>true</code>, wenn Dateiausgaben dabei sind
	 */
	public static boolean hasFileOutput(final Statistics statistics) {
		return getOutputFiles(statistics).size()>0;
	}

	/**
	 * Liefert eine Liste mit allen Dateiausgaben eines Statistikobjektes (bzw. des enthaltenen Modells)
	 * @param statistics	Zu prüfendes Statistikobjekt
	 * @return	Liste mit allen Dateiausgaben des Statistikobjektes (bzw. des enthaltenen Modells), kann leer sein, ist aber nie <code>null</code>
	 * @see FileOutputInfo
	 */
	private static List<FileOutputInfo> getOutputFiles(final Statistics statistics) {
		final List<FileOutputInfo> files=new ArrayList<>();

		for (ModelElement element: statistics.editModel.surface.getElementsIncludingSubModels()) if ((element instanceof ElementWithOutputFile) && (element instanceof ModelElementBox)) {
			final File file=new File(((ElementWithOutputFile)element).getOutputFile());
			if (file.isFile()) files.add(new FileOutputInfo((ModelElementBox)element,file));
		}

		return files;
	}

	/**
	 * Diese Klasse hält Informationen zu einer Dateiausgabe vor.
	 * @see StatisticViewerOverviewText#getOutputFiles(Statistics)
	 */
	private static class FileOutputInfo {
		/**
		 * Station an der die Dateiausgabe erfolgt ist
		 */
		private final ModelElementBox element;

		/**
		 * Ausgabedatei
		 */
		private final File file;

		/**
		 * Konstruktor der Klasse
		 * @param element	Station an der die Dateiausgabe erfolgt ist
		 * @param file	Ausgabedatei
		 */
		private FileOutputInfo(final ModelElementBox element, final File file) {
			this.element=element;
			this.file=file;
		}

		/**
		 * Liefert den vollständigen Namen der Station an der die Dateiausgabe erfolgt ist.
		 * @return	Vollständiger Namen der Station an der die Dateiausgabe erfolgt ist
		 */
		private String getFullName() {
			final StringBuilder info=new StringBuilder();
			info.append(element.getTypeName());
			if (!element.getName().isEmpty()) {
				info.append(" \"");
				info.append(element.getName());
				info.append("\"");
			}
			info.append(" (id=");
			info.append(element.getId());
			info.append(")");
			return info.toString();
		}

		/**
		 * Liefert den Namen der Ausgabedatei.
		 * @return	Namen der Ausgabedatei
		 */
		private String getFile() {
			return file.toString();
		}
	}

	/**
	 * Ausgabe "Simulierte Zeit"
	 * @see Mode#MODE_SIM_TIME
	 */
	private void buildSimTime() {
		addHeading(1,Language.tr("Statistics.SimulatedTime"));

		final double timeSpan=statistics.clientsInSystem.getSum();
		beginParagraph();
		if (statistics.simulationData.runThreads==1) {
			final double timeStart=statistics.clientsInSystem.getStartTime();
			final double timeEnd=timeStart+timeSpan;
			addLine(Language.tr("Statistics.SimulatedTime.StartTime")+": "+timeAndNumber(timeStart));
			addLine(Language.tr("Statistics.SimulatedTime.EndTime")+": "+timeAndNumber(timeEnd));
		} else {
			addLine(Language.tr("Statistics.SimulatedTime.MultiCoreInfo"));
			addLine(Language.tr("Statistics.SimulatedTime.TimeSpan")+": "+timeAndNumber(timeSpan));
		}

		endParagraph();
	}

	/**
	 * Prüft, ob Informationen zur Simulationslaufzeit verfügbar sind.
	 * @param statistics			Zu prüfende Statistikobjekte
	 * @return	Liefert <code>true</code>, wenn mindestens eines der Statistikobjekte Informationen zur Simulationslaufzeit besitzt.
	 */
	public static boolean hasSimTime(final Statistics[] statistics) {
		for (Statistics statistic: statistics) if (hasSimTime(statistic)) return true;
		return false;
	}

	/**
	 * Prüft, ob Informationen zur Simulationslaufzeit verfügbar sind.
	 * @param statistics		Zu prüfendes Statistikobjekt
	 * @return	Liefert <code>true</code>, wenn das Statistikobjekt Informationen zur Simulationslaufzeit besitzt.
	 */
	public static boolean hasSimTime(final Statistics statistics) {
		return statistics.simulationData.runThreads==1;
	}

	/**
	 * Teil-Ausgabe für "Modellüberblick" (Basisinformationen zum Modell als solches; hinterlegte Modellbeschreibung und weitere Informationen)<br>
	 * Ausgabe von "Mittlere Anzahl an Kunden"
	 * @see #buildOverview()
	 * @see Mode#MODE_OVERVIEW
	 * @see Filter#NUMBER_CLIENTS
	 */
	private void buildOverviewCountClients() {
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



		/* Mittlere Anzahl an Kunden in Bedienung (im System / an den Stationen) */

		headingWritten=false;
		for (String station: statistics.clientsAtStationProcessByStation.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationProcessByStation.get(station));
			final double mean=indicator.getTimeMean();
			if (mean>0) {
				if (!headingWritten) {
					addHeading(3,Language.tr("Statistics.AverageNumberOfClientsInProcessByStations")+" E[NS]");
					beginParagraph();
					headingWritten=true;
					addLine(Language.tr("Statistics.ClientsInSystemProcess")+": "+StatisticTools.formatNumber(statistics.clientsInSystemProcess.getTimeMean()),xmlMean(statistics.clientsInSystemProcess));
				}
				addLine(Language.tr("Statistics.ClientsInProcessAt")+" "+fullStationName(station)+": E[NS]="+StatisticTools.formatNumber(mean),xmlMean(indicator));
			}
		}
		if (headingWritten) {
			addModeLink(Mode.MODE_CLIENTS_COUNT);
			endParagraph();
		} else {
			if (statistics.clientsInSystemProcess.getTimeMean()>0) {
				addHeading(3,Language.tr("Statistics.AverageNumberOfClientsInProcessByStations")+" E[NS]");
				beginParagraph();
				addLine(Language.tr("Statistics.ClientsInSystemProcess")+": "+StatisticTools.formatNumber(statistics.clientsInSystemProcess.getTimeMean()),xmlMean(statistics.clientsInSystemProcess));
				addModeLink(Mode.MODE_CLIENTS_COUNT);
				endParagraph();
			}
		}

		/* Mittlere Anzahl Kunden in Bedienung (nach Kundentypen) */

		if (statistics.clientsAtStationProcessByClient.size()>1) {
			headingWritten=false;
			for (String clientType: statistics.clientsAtStationProcessByClient.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationProcessByClient.get(clientType));
				final double mean=indicator.getTimeMean();
				if (mean>0) {
					if (!headingWritten) {
						addHeading(3,Language.tr("Statistics.AverageNumberOfClientsInProcessByClientTypes")+" E[NS]");
						beginParagraph();
						headingWritten=true;
					}
					addLine(Language.tr("Statistics.ClientType")+" "+clientType+": E[NS]="+StatisticTools.formatNumber(mean),xmlMean(indicator));
				}
			}
			if (headingWritten) {
				addModeLink(Mode.MODE_CLIENTS_COUNT);
				endParagraph();
			}
		}
	}

	/**
	 * Teil-Ausgabe für "Modellüberblick" (Basisinformationen zum Modell als solches; hinterlegte Modellbeschreibung und weitere Informationen)<br>
	 * Ausgabe von "Zeiten nach Kunden"
	 * @see #buildOverview()
	 * @see Mode#MODE_OVERVIEW
	 * @see Filter#TIMES_CLIENTS
	 */
	private void buildOverviewTimesClients() {
		boolean headingWritten=false;

		/* Wartezeiten nach Kundentypen */

		boolean writeBlock=false;
		for (String type: statistics.clientsWaitingTimes.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			long sum=0;
			long count=0;
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
			long sum=0;
			long count=0;
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
			long sum=0;
			long count=0;
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
			long sum=0;
			long count=0;
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

		/* Flussgrade nach Kundentypen */

		writeBlock=false;
		for (String type: statistics.clientsProcessingTimes.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			double sum1=0;
			long count1=0;
			double sum2=0;
			long count2=0;
			if (!headingWritten) {
				addHeading(2,Language.tr("Statistics.TimesByClientTypes"));
				headingWritten=true;
			}
			addHeading(3,Language.tr("Statistics.FlowFactorByClientTypes"));
			beginParagraph();
			for (String type: statistics.clientsResidenceTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator1=((StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.get(type));
				final StatisticsDataPerformanceIndicator indicator2=((StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.get(type));
				final double time1=indicator1.getMean();
				final double time2=indicator2.getMean();
				sum1+=time1*indicator1.getCount();
				count1+=indicator1.getCount();
				sum2+=time2*indicator2.getCount();
				count2+=indicator2.getCount();
				if (statistics.clientsProcessingTimes.size()>1 && time2>0) addLine(Language.tr("Statistics.ClientType")+" "+type+": "+StatisticTools.formatNumber(time1/time2));
			}
			if (count1==0) count1=1;
			if (count2==0) count2=1;
			final double time1=sum1/count1;
			final double time2=sum2/count2;
			if (time2>0) {
				addLine(Language.tr("Statistics.Average")+": "+StatisticTools.formatNumber(time1/time2));
			}
			addModeLink(Mode.MODE_WAITINGPROCESSING_CLIENTS);
			endParagraph();
		}
	}

	/**
	 * Teil-Ausgabe für "Modellüberblick" (Basisinformationen zum Modell als solches; hinterlegte Modellbeschreibung und weitere Informationen)<br>
	 * Ausgabe von "Zeiten nach Stationen"
	 * @see #buildOverview()
	 * @see Mode#MODE_OVERVIEW
	 * @see Filter#TIMES_STATIONS
	 */
	private void buildOverviewTimesStations() {
		addHeading(2,Language.tr("Statistics.TimesByStationen"));

		/* Wartezeiten nach Stationen */

		boolean writeBlock=false;
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
			addHeading(3,Language.tr("Statistics.TransferTimesByStations")+" E[T]");
			beginParagraph();
			for (String station: statistics.stationsTransferTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=((StatisticsDataPerformanceIndicator)statistics.stationsTransferTimes.get(station));
				final double time=indicator.getMean();
				if (time>0) addLine(fullStationName(station)+": E[T]="+timeAndNumber(time),xmlMean(indicator));
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
				if (time>0) addLine(fullStationName(station)+": E[S]="+timeAndNumber(time),xmlMean(indicator));
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

		/* Flussgrade nach Stationen */

		writeBlock=false;
		for (String type: statistics.stationsProcessingTimes.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			addHeading(3,Language.tr("Statistics.FlowFactorByStations"));
			beginParagraph();
			for (String station: statistics.stationsProcessingTimes.getNames()) {
				final StatisticsDataPerformanceIndicator indicator1=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.get(station));
				final StatisticsDataPerformanceIndicator indicator2=((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.get(station));
				final double time1=indicator1.getMean();
				final double time2=indicator2.getMean();
				if (time2>0) {
					addLine(fullStationName(station)+": "+StatisticTools.formatNumber(time1/time2));
				}
			}
			addModeLink(Mode.MODE_WAITINGPROCESSING_STATIONS);
			endParagraph();
		}
	}

	/**
	 * Teil-Ausgabe für "Modellüberblick" (Basisinformationen zum Modell als solches; hinterlegte Modellbeschreibung und weitere Informationen)<br>
	 * Ausgabe von "Zeiten nach Stationen und Kundentypen"
	 * @see #buildOverview()
	 * @see Mode#MODE_OVERVIEW
	 * @see Filter#TIMES_STATIONS_CLIENTS
	 */
	private void buildOverviewTimesStationsClients() {
		addHeading(2,Language.tr("Statistics.TimesByStationsAndClientTypes"));

		/* Wartezeiten nach Stationen und Kundentypen */

		boolean writeBlock=false;
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
			addHeading(3,Language.tr("Statistics.TransferTimesByStationsAndClients")+" E[T]");
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

		/* Flussgrade nach Stationen und Kundentypen */

		writeBlock=false;
		for (String type: statistics.stationsProcessingTimesByClientType.getNames()) if (((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimesByClientType.get(type)).getMean()>0) {writeBlock=true; break;}

		if (writeBlock) {
			addHeading(3,Language.tr("Statistics.FlowFactorByStationsAndClients"));
			beginParagraph();
			for (String station: statistics.stationsProcessingTimesByClientType.getNames()) {
				final StatisticsDataPerformanceIndicator indicator1=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimesByClientType.get(station));
				final StatisticsDataPerformanceIndicator indicator2=((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimesByClientType.get(station));
				final double time1=indicator1.getMean();
				final double time2=indicator2.getMean();
				if (time2>0) {
					addLine(fullStationName(station)+": "+StatisticTools.formatNumber(time1/time2));
				}
			}
			addModeLink(Mode.MODE_WAITINGPROCESSING_STATIONS);
			endParagraph();
		}
	}

	/**
	 * Teil-Ausgabe für "Modellüberblick" (Basisinformationen zum Modell als solches; hinterlegte Modellbeschreibung und weitere Informationen)<br>
	 * Ausgabe von "Ressourcenauslastung"
	 * @see #buildOverview()
	 * @see Mode#MODE_OVERVIEW
	 * @see Filter#UTILIZATION_RESOURCES
	 */
	private void buildOverviewUtilizationResources() {
		if (statistics.resourceUtilization.getNames().length>0) {
			addHeading(2,Language.tr("Statistics.Utilization"));

			beginParagraph();

			final double rhoAll=statistics.resourceRhoAll.get();
			String rhoInfo="";
			if (rhoAll>0) rhoInfo=" (rho="+StatisticTools.formatPercent(rhoAll)+")";
			addLine(Language.tr("Statistics.AverageNumberOfBusyOperators")+"="+StatisticTools.formatNumber(statistics.resourceUtilizationAll.getTimeMean(),2)+rhoInfo);

			for (String resource: statistics.resourceUtilization.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=((StatisticsTimePerformanceIndicator)statistics.resourceUtilization.get(resource));
				final double meanState=indicator.getTimeMean();
				final ModelResource resourceObj=statistics.editModel.resources.get(resource);
				if (resourceObj!=null && resourceObj.getMode()==ModelResource.Mode.MODE_NUMBER) {
					long count=resourceObj.getCount();
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
	}

	/**
	 * Teil-Ausgabe für "Modellüberblick" (Basisinformationen zum Modell als solches; hinterlegte Modellbeschreibung und weitere Informationen)<br>
	 * Ausgabe von "Transporterauslastung"
	 * @see #buildOverview()
	 * @see Mode#MODE_OVERVIEW
	 * @see Filter#UTILIZATION_TRANSPORTERS
	 */
	private void buildOverviewUtilizationTransporters() {
		if (statistics.transporterUtilization.getNames().length>0) {
			addHeading(2,Language.tr("Statistics.TransporterUtilization"));
			beginParagraph();
			for (String transporter: statistics.transporterUtilization.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=((StatisticsTimePerformanceIndicator)statistics.transporterUtilization.get(transporter));
				final double meanState=indicator.getTimeMean();
				final ModelTransporter transporterObj=statistics.editModel.transporters.get(transporter);
				if (transporterObj!=null) {
					long count=transporterObj.getCountAll();
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
	}

	/**
	 * Teil-Ausgabe für "Modellüberblick" (Basisinformationen zum Modell als solches; hinterlegte Modellbeschreibung und weitere Informationen)<br>
	 * Ausgabe von "Zähler"
	 * @see #buildOverview()
	 * @see Mode#MODE_OVERVIEW
	 * @see Filter#COUNTERS
	 */
	private void buildOverviewCounters() {
		if (statistics.counter.size()>0 || statistics.differentialCounter.size()>0 || statistics.counterBatch.size()>0 ) {
			addHeading(2,Language.tr("Statistics.Counter"));
			buildCounterInt(3,false);
			beginParagraph();
			addModeLink(Mode.MODE_COUNTER);
			endParagraph();
		}
	}

	/**
	 * Teil-Ausgabe für "Modellüberblick" (Basisinformationen zum Modell als solches; hinterlegte Modellbeschreibung und weitere Informationen)<br>
	 * Ausgabe von "Durchsatz"
	 * @see #buildOverview()
	 * @see Mode#MODE_OVERVIEW
	 * @see Filter#THROUGHPUT
	 */
	private void buildOverviewThroughput() {
		if (statistics.throughputStatistics.size()>0) {
			addHeading(2,Language.tr("Statistics.Throughput"));
			buildThroughputInt();
			beginParagraph();
			addModeLink(Mode.MODE_THROUGHPUT);
			endParagraph();
		}
	}

	/**
	 * Ausgabe von
	 * "Modellüberblick" (Basisinformationen zum Modell als solches; hinterlegte Modellbeschreibung und weitere Informationen)
	 * @see Mode#MODE_OVERVIEW
	 */
	private void buildOverview() {
		addHeading(1,Language.tr("Statistics.ResultsOverview"));

		/* Ausgabe der Teil-Statistiken */
		Filter.process(this,Filter.stringToSet(SetupData.getSetup().statisticOverviewFilter));

		/* Infotext  */
		addDescription("Overview");
	}

	@Override
	protected void processLinkClick(final String link) {
		/* Details-Links */
		for (Mode mode: Mode.values()) if (mode.toString().equals(link)) {
			if (modeClick!=null) modeClick.accept(mode);
			return;
		}

		/* Konfidenzintervall-Hilfe-Link */
		if (link.equals(MORE_CONFIDENCE_DATA_LINK)) {
			MsgBox.info(getViewer(false),Language.tr("Statistics.Confidence.More"),"<html><body>"+Language.tr("Statistics.Confidence.More.Info").replace("\n","")+"</body></html>");
			return;
		}

		/* Aufrufe externer Dateien (Anzeige von generierten Ausgabedateien) */
		if (link.startsWith("file:")) {
			try {
				Desktop.getDesktop().open(new File(link.substring(5)));
			} catch (IOException e) {}
			return;
		}
	}

	/**
	 * Ausgabe von
	 * "Stationsbeschreibung" (automatisch generierte Beschreibung aller Stationen im Modell)
	 * @see Mode#MODE_MODEL
	 */
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
			if (sum==0 && statistics.editModel.warmUpTime>0) {
				beginParagraph();
				addLine(Language.tr("Statistics.SimulatedClients.Zero"));
				addLine(String.format(Language.tr("Statistics.SimulatedClients.Zero.Info"),NumberTools.formatLong(Math.round(statistics.editModel.clientCount*statistics.editModel.warmUpTime))));
				addLine(Language.tr("Statistics.SimulatedClients.Zero.Info2"));
				endParagraph();
			}
		}

		outputEmergencyShutDownInfo(false);
	}

	/**
	 * Ausgabe von
	 * "Stationsbeschreibung" (automatisch generierte Beschreibung aller Stationen im Modell)
	 * @see Mode#MODE_MODEL_DESCRIPTION
	 */
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
						addLines(property[1]);
						endParagraph();
					}
				}
			}
			@Override
			protected void processVariables(List<String> variables) {
				addHeading(2,Language.tr("ModelDescription.InitialValuesForVariables"));
				beginParagraph();
				for (String variable: variables) addLine(variable);
				endParagraph();
			}
			@Override
			protected void processResources(final List<String> resources) {
				addHeading(2,Language.tr("ModelDescription.Resources"));
				beginParagraph();
				for (String resource: resources) addLine(resource);
				endParagraph();
			}
		};

		descriptionBuilder.run();
		descriptionBuilder.done();
	}

	/**
	 * Gibt Quantil-Informationen zu einem Zeit-Statistikobjekt aus.
	 * @param identifier	Bezeichner für das Statistikobjekt (z.B. "W")
	 * @param indicator	Statistikobjekt
	 */
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

	/**
	 * Gibt Quantil-Informationen zu einem Zeit-Statistikobjekt aus.
	 * @param identifier	Bezeichner für das Statistikobjekt (z.B. "W")
	 * @param indicator	Statistikobjekt
	 */
	private void outputQuantilInfoTime(final String identifier, final StatisticsDataPerformanceIndicatorWithNegativeValues indicator) {
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

	/**
	 * Gibt Quantil-Informationen zu einem Zahlen-Statistikobjekt aus.
	 * @param identifier	Bezeichner für das Statistikobjekt (z.B. "W")
	 * @param indicator	Statistikobjekt
	 */
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

	/**
	 * Gibt Quantil-Informationen zu einem Zahlen-Statistikobjekt aus.
	 * @param identifier	Bezeichner für das Statistikobjekt (z.B. "W")
	 * @param indicator	Statistikobjekt
	 */
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

	/**
	 * Maximalanzahl an auszugebenden Verteilungsdaten in
	 * {@link #outputShortStateDistribution(String, StatisticsTimePerformanceIndicator)}
	 * @see #outputShortStateDistribution(String, StatisticsTimePerformanceIndicator)
	 */
	private static final int MAX_SHORT_STATE_DISTRIBUTION=5;

	/**
	 * Gibt Verteilungsdaten aus, sofern bei der Zähldichte nur für wenige Einträge Werte ungleich 0 vorliegen.
	 * @param identifier	Bezeichner für das Statistikobjekt (z.B. "W")
	 * @param indicator	Statistikobjekt
	 * @see #MAX_SHORT_STATE_DISTRIBUTION
	 */
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

	/**
	 * Ausgabe von
	 * Zwischenankunftszeiten der Kunden am System
	 * @see Mode#MODE_INTERARRIVAL_CLIENTS
	 */
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
			addLine(Language.tr("Statistics.Skewness")+": Sk[I]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[I]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
			addLine(Language.tr("Statistics.MinimalInterArrivalTime")+": Min[I]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterArrivalTime")+": Max[I]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("I",indicator);

			outputConfidenceData(indicator);
		}

		/* Infotext  */
		addDescription("InterarrivalSystem");
	}

	/**
	 * Liefert die Summe der Ankünfte über alle Kundentypen.
	 * @return	Summe der Ankünfte über alle Kundentypen
	 */
	private long getArrivalSum() {
		long arrivalSum=0;
		for (String type : statistics.clientsInterarrivalTime.getNames()) {
			arrivalSum+=((StatisticsDataPerformanceIndicator)(statistics.clientsInterarrivalTime.get(type))).getCount();
		}
		return arrivalSum;
	}

	/**
	 * Gibt es in einem Multi-Statistik-Objekt mehrere Einträge, die mit demselben Bezeichner beginnen?
	 * @param indicators	Multi-Statistik-Objekt
	 * @param name	Bezeichner
	 * @param marker	Markierung bei zu der der Bezeichner ausgewertet werden soll
	 * @return	Liefert <code>true</code>, wenn es mehrere Einträge in dem Multi-Statistik-Objekt gibt, bei denen der Bezeichner mit dem angegebenen Bezeichner bis zu der Markierung beginnt
	 */
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

	/**
	 * Berechnet den Durchsatz aus Ankunftsanzahl und Simulationslaufzeit.
	 * @param clients	Anzahl an Ankünften
	 * @param statistics	Statistikobjekt aus dem die Simulationslaufzeit ausgelesen wird
	 * @return	Durchsatz als Text
	 */
	public static String getThroughputText(final long clients, final Statistics statistics) {
		final String[] cols=getThroughputColumns(clients,statistics);
		return cols[0]+" "+cols[1];
	}

	/**
	 * Formatiert den maximalen Durchsatz
	 * @param maxThroughput	Maximaler Durchsatz
	 * @return	Maximaler Durchsatz als Text
	 */
	public static String getMaxThroughputText(final double maxThroughput) {
		final String[] cols=getMaxThroughputColumns(maxThroughput);
		return cols[0]+" "+cols[1];
	}

	/**
	 * Berechnet den Durchsatz aus Ankunftsanzahl und Simulationslaufzeit.
	 * @param clients	Anzahl an Ankünften
	 * @param statistics	Statistikobjekt aus dem die Simulationslaufzeit ausgelesen wird
	 * @return	Durchsatz in Form von zwei Spalten: Zahlenwert und Einheit
	 */
	public static String[] getThroughputColumns(final long clients, final Statistics statistics) {
		final double timeSpan=statistics.clientsInSystem.getSum();

		double throughput=clients/timeSpan;

		if (throughput>=1.0) return new String[] {StatisticTools.formatNumber(throughput),Language.tr("Statistics.Throughput.ArrivalsPerSecond")};

		throughput*=60;
		if (throughput>=1.0) return new String[] {StatisticTools.formatNumber(throughput),Language.tr("Statistics.Throughput.ArrivalsPerMinute")};

		throughput*=60;
		if (throughput>=1.0) return new String[] {StatisticTools.formatNumber(throughput),Language.tr("Statistics.Throughput.ArrivalsPerHour")};

		throughput*=24;
		return new String[] {StatisticTools.formatNumber(throughput),Language.tr("Statistics.Throughput.ArrivalsPerDay")};
	}

	/**
	 * Formatiert den maximalen Durchsatz
	 * @param maxThroughput	Maximaler Durchsatz
	 * @return	Maximaler Durchsatz in Form von zwei Spalten: Zahlenwert und Einheit
	 */
	public static String[] getMaxThroughputColumns(double maxThroughput) {
		if (maxThroughput>=1.0) return new String[] {StatisticTools.formatNumber(maxThroughput),Language.tr("Statistics.Throughput.ArrivalsPerSecond")};

		maxThroughput*=60;
		if (maxThroughput>=1.0) return new String[] {StatisticTools.formatNumber(maxThroughput),Language.tr("Statistics.Throughput.ArrivalsPerMinute")};

		maxThroughput*=60;
		if (maxThroughput>=1.0) return new String[] {StatisticTools.formatNumber(maxThroughput),Language.tr("Statistics.Throughput.ArrivalsPerHour")};

		maxThroughput*=24;
		return new String[] {StatisticTools.formatNumber(maxThroughput),Language.tr("Statistics.Throughput.ArrivalsPerDay")};
	}

	/**
	 * Liefert ein Station-Objekt zu einem Statistik-Stationsnamen
	 * @param fullStationName	Statistik-Stationsnamen
	 * @param statistics	Statistikobjekt, aus dem die Modelldaten entnommen werden sollen
	 * @return	Liefert im Erfolgsfall das Statistik-Objekt, sonst <code>null</code>
	 */
	public static ModelElementBox getStationFromStatisticsName(final String fullStationName, final Statistics statistics) {
		final int index1=fullStationName.indexOf(" (id=");
		if (index1<0) return null;
		final int index2=fullStationName.indexOf(")",index1+5);
		if (index2<0) return null;
		final String idString=fullStationName.substring(index1+5,index2);
		final Integer id=NumberTools.getNotNegativeInteger(idString);
		if (id==null) return null;
		final ModelElement element=statistics.editModel.surface.getByIdIncludingSubModels(id);
		if (element instanceof ModelElementBox) return (ModelElementBox)element;
		return null;
	}

	/**
	 * Ausgabe von
	 * Zwischenankunftszeiten der Kunden an den Stationen
	 * @see Mode#MODE_INTERARRIVAL_STATIONS
	 */
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
			addLine(Language.tr("Statistics.Skewness")+": Sk[I]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[I]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
			addLine(Language.tr("Statistics.MinimalInterArrivalTime")+": Min[I]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterArrivalTime")+": Max[I]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			addLine(Language.tr("Statistics.Throughput")+": "+getThroughputText(indicator.getCount(),statistics));
			final StatisticsSimpleValueMaxPerformanceIndicator maxThroughput=(StatisticsSimpleValueMaxPerformanceIndicator)(statistics.stationsMaxThroughput.getOrNull(station));
			if (maxThroughput!=null && maxThroughput.get()>0) {
				String maxThroughputInfo="";
				final ModelElementBox element=getStationFromStatisticsName(station,statistics);
				if (element!=null && element.getMaxThroughputIntervalSeconds()>0) maxThroughputInfo=" ("+String.format(Language.tr("Statistics.Throughput.Maximum.IntervalLength"),NumberTools.formatLong(element.getMaxThroughputIntervalSeconds()))+")";
				addLine(Language.tr("Statistics.Throughput.Maximum")+": "+getMaxThroughputText(maxThroughput.get())+maxThroughputInfo);
			}
			endParagraph();

			outputQuantilInfoTime("I",indicator);

			outputConfidenceData(indicator);
		}

		/* Ankünfte an den Stationen in Batchen gerechnet */

		boolean first=true;
		for (String station : stations) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.stationsInterarrivalTimeBatch.get(station));
			if (indicator.getCount()==0) continue;
			if (first) {
				addHeading(2,Language.tr("Statistics.InterArrivalTimesAtTheStationsBatch"));
				first=false;
			}
			addHeading(3,fullStationName(station));
			beginParagraph();
			addLine(Language.tr("Statistics.AverageInterArrivalCount")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
			addLine(Language.tr("Statistics.AverageInterArrivalTime")+": E[IB]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDevInterArrivalTime")+": Std[IB]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceInterArrivalTime")+": Var[IB]="+timeAndNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.CVInterArrivalTime")+": CV[IB]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.Skewness")+": Sk[IB]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[IB]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
			addLine(Language.tr("Statistics.MinimalInterArrivalTime")+": Min[IB]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterArrivalTime")+": Max[IB]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			addLine(Language.tr("Statistics.Throughput")+": "+getThroughputText(indicator.getCount(),statistics));
			endParagraph();

			outputQuantilInfoTime("IB",indicator);

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
			addLine(Language.tr("Statistics.Skewness")+": Sk[I]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[I]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
			addLine(Language.tr("Statistics.MinimalInterArrivalTime")+": Min[I]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterArrivalTime")+": Max[I]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			addLine(Language.tr("Statistics.Throughput")+": "+getThroughputText(indicator.getCount(),statistics));
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
			addLine(Language.tr("Statistics.Skewness")+": Sk[I]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[I]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
			addLine(Language.tr("Statistics.MinimalInterArrivalTime")+": Min[I]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterArrivalTime")+": Max[I]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("I",indicator);

			outputConfidenceData(indicator);
		}

		/* Infotext  */
		addDescription("InterarrivalStations");
	}

	/**
	 * Ausgabe von
	 * Zwischenabgangszeiten der Kunden aus dem System
	 * @see Mode#MODE_INTERLEAVE_CLIENTS
	 */
	private void buildInterleaveSystem() {
		addHeading(1,Language.tr("Statistics.InterLeaveTimes"));

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		for (String type : statistics.clientsInterleavingTime.getNames()) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.clientsInterleavingTime.get(type));
			addHeading(3,fullStationName(type));
			beginParagraph();
			addLine(Language.tr("Statistics.AverageInterLeaveCount")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
			addLine(Language.tr("Statistics.AverageInterLeaveTime")+": E[ID]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDevInterLeaveTime")+": Std[ID]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceInterLeaveTime")+": Var[ID]="+timeAndNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.CVInterLeaveTime")+": CV[ID]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.Skewness")+": Sk[ID]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[ID]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
			addLine(Language.tr("Statistics.MinimalInterLeaveTime")+": Min[ID]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterLeaveTime")+": Max[ID]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("IL",indicator);

			outputConfidenceData(indicator);
		}

		/* Infotext  */
		addDescription("InterleaveSystem");
	}

	/**
	 * Ausgabe von
	 * Zwischenabgangszeiten der Kunden an den Stationen
	 * @see Mode#MODE_INTERLEAVE_STATIONS
	 */
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
			addLine(Language.tr("Statistics.AverageInterLeaveTime")+": E[ID]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDevInterLeaveTime")+": Std[ID]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceInterLeaveTime")+": Var[ID]="+timeAndNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.CVInterLeaveTime")+": CV[ID]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.Skewness")+": Sk[ID]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[ID]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
			addLine(Language.tr("Statistics.MinimalInterLeaveTime")+": Min[ID]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterLeaveTime")+": Max[ID]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("IL",indicator);

			outputConfidenceData(indicator);
		}

		/* Abgänge an den Stationen in Batchen gerechnet */

		boolean first=true;
		for (String station : stations) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)(statistics.stationsInterleavingTimeBatch.get(station));
			if (indicator.getCount()==0) continue;
			if (first) {
				addHeading(2,Language.tr("Statistics.InterLeaveTimesAtTheStationsBatch"));
				first=false;
			}
			addHeading(3,fullStationName(station));
			beginParagraph();
			addLine(Language.tr("Statistics.AverageInterLeaveCount")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
			addLine(Language.tr("Statistics.AverageInterLeaveTime")+": E[ID]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDevInterLeaveTime")+": Std[ID]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceInterLeaveTime")+": Var[ID]="+timeAndNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.CVInterLeaveTime")+": CV[ID]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.Skewness")+": Sk[ID]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[ID]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
			addLine(Language.tr("Statistics.MinimalInterLeaveTime")+": Min[ID]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterLeaveTime")+": Max[ID]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
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
			addLine(Language.tr("Statistics.AverageInterLeaveTime")+": E[ID]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDevInterLeaveTime")+": Std[ID]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.VarianceInterLeaveTime")+": Var[ID]="+timeAndNumber(indicator.getVar()));
			addLine(Language.tr("Statistics.CVInterLeaveTime")+": CV[ID]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.Skewness")+": Sk[ID]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[ID]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
			addLine(Language.tr("Statistics.MinimalInterLeaveTime")+": Min[ID]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximalInterLeaveTime")+": Max[ID]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("IL",indicator);

			outputConfidenceData(indicator);
		}

		/* Infotext  */
		addDescription("InterleaveStations");
	}

	/**
	 * Ausgabe von
	 * Informationen zum Simulationssystem
	 * @see Mode#MODE_SYSTEM_INFO
	 */
	private void buildSystemInfo() {
		/* Basisdaten */
		addHeading(1,Language.tr("Statistics.SystemData"));
		beginParagraph();
		addLine(Language.tr("Statistics.SystemData.Version")+": "+statistics.editModel.version);
		addLine(Language.tr("Statistics.SystemData.RunDate")+": "+statistics.simulationData.runDate);
		addLine(Language.tr("Statistics.SystemData.RunThreads")+": "+statistics.simulationData.runThreads);
		if (statistics.simulationData.runRepeatCount>1) addLine(Language.tr("Statistics.SystemData.RepeatCount")+": "+NumberTools.formatLong(statistics.simulationData.runRepeatCount));
		addLine(Language.tr("Statistics.SystemData.RunOS")+": "+statistics.simulationData.runOS);
		if (statistics.editModel.author!=null && !statistics.editModel.author.trim().isEmpty()) addLine(Language.tr("Statistics.SystemData.EditUser")+": "+statistics.editModel.author);
		if (statistics.editModel.authorEMail!=null && !statistics.editModel.authorEMail.trim().isEmpty()) addLine(Language.tr("Statistics.SystemData.EditUserEMail")+": "+statistics.editModel.authorEMail);
		addLine(Language.tr("Statistics.SystemData.RunUser")+": "+statistics.simulationData.runUser);
		endParagraph();

		/* Laufzeit */
		beginParagraph();
		addLine(Language.tr("Statistics.SystemData.RunTime")+": "+NumberTools.formatLong(statistics.simulationData.runTime)+" ms");
		final DataDistributionImpl threads=new DataDistributionImpl(1000,statistics.simulationData.threadRunTimes);
		final double threadMax=threads.getMax();
		final double threadMin=threads.getMin();
		if (statistics.simulationData.runThreads>1) {
			if (threadMax>0 && threadMin>0) {
				addLine(Language.tr("Statistics.SystemData.ThreadRunTimeFactor")+": "+StatisticTools.formatPercent(threadMax/threadMin-1));
			}
			if (statistics.simulationData.threadDynamicBalance>0) {
				addLine(Language.tr("Statistics.SystemData.ThreadDynamicBalance")+": "+StatisticTools.formatPercent(statistics.simulationData.threadDynamicBalance));
			}
		}
		if (statistics.simulationData.numaAwareMode) addLine(Language.tr("Statistics.SystemData.NUMAMode"));
		endParagraph();

		/* Kunden */
		beginParagraph();
		long sum=0;
		for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class)) sum+=indicator.getCount();
		addLine(Language.tr("Statistics.SystemData.SimulatedArrivals")+": "+NumberTools.formatLong(sum));
		String s;
		if (sum>0 && statistics.simulationData.runTime>0) {
			addLine(Language.tr("Statistics.SystemData.ClientsPerSecond")+": "+NumberTools.formatLong(sum*1000/statistics.simulationData.runTime));
			double d=((double)statistics.simulationData.runTime)*statistics.simulationData.runThreads/sum;
			s=NumberTools.formatNumber(d,2);
			if (s.equals("0")) {
				s=StatisticTools.formatNumber(((double)statistics.simulationData.runTime*1000)*statistics.simulationData.runThreads/sum,2);
				addLine(Language.tr("Statistics.SystemData.TimePerClient")+" (*): "+s+" µs");
			} else {
				s=StatisticTools.formatNumber(d,2);
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

	/**
	 * Ausgabe von
	 * Ankünfte pro Threads
	 * @see Mode#MODE_SYSTEM_INFO_THREAD_BALANCE
	 */
	private void buildThreadBalanceInfo() {
		addHeading(1,Language.tr("Statistics.SystemData.ThreadBalance"));
		beginParagraph();
		final long[] data=statistics.simulationData.threadDynamicBalanceData;
		final int[] dataTimes=statistics.simulationData.threadRunTimes;
		long sum=0;
		for (long value: data) sum+=value;
		final long mean=sum/data.length;
		for (int i=0;i<data.length;i++) {
			final StringBuilder line=new StringBuilder();
			line.append(Language.tr("Statistics.SystemData.ThreadBalance.Thread"));
			line.append(" ");
			line.append(i+1);
			line.append(": ");
			line.append(NumberTools.formatLong(data[i]));
			line.append(" (");
			line.append(StatisticTools.formatPercent(((double)data[i])/sum));
			line.append(", ");
			line.append(Language.tr("Statistics.SystemData.ThreadBalance.DeviationFromAverage"));
			line.append("=");
			line.append(NumberTools.formatLong(data[i]-mean));
			if (dataTimes!=null && dataTimes.length>i) {
				line.append(", ");
				line.append(Language.tr("Statistics.SystemData.ThreadBalance.Runtime"));
				line.append("=");
				line.append(StatisticTools.formatNumber(dataTimes[i]/1000.0,3));
				line.append(" ");
				line.append(Language.tr("Statistic.Seconds"));
			}
			line.append(")");
			addLine(line.toString());
		}
		endParagraph();

		/* Infotext  */
		addDescription("ThreadBalance");
	}

	/**
	 * Erstellt die Ausgabe für einen Kundentyp.
	 * @param waitingTime	Wartezeiten für den Kundentyp
	 * @param transferTime	Transferzeiten für den Kundentyp
	 * @param processingTime	Bedienzeiten für den Kundentyp
	 * @param residenceTime	Verweilzeiten für den Kundentyp
	 * @param hasWaitingTimes	Sollen Daten zu den Wartezeiten ausgegeben werden?
	 * @param hasTransferTimes	Sollen Daten zu den Transferzeiten ausgegeben werden?
	 * @param hasProcessingTimes	Sollen Daten zu den Bedienzeiten ausgegeben werden?
	 * @param hasResidenceTimes	Sollen Daten zu den Verweilzeiten ausgegeben werden?
	 */
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
			addLine(Language.tr("Statistics.Skewness")+": Sk[W]="+StatisticTools.formatNumber(waitingTime.getSk()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[W]="+StatisticTools.formatNumber(waitingTime.getKurt()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.Kurt));
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
			addLine(Language.tr("Statistics.Skewness")+": Sk[T]="+StatisticTools.formatNumber(transferTime.getSk()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[T]="+StatisticTools.formatNumber(transferTime.getKurt()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.Kurt));
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
			addLine(Language.tr("Statistics.Skewness")+": Sk[S]="+StatisticTools.formatNumber(processingTime.getSk()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[S]="+StatisticTools.formatNumber(processingTime.getKurt()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.Kurt));
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
			addLine(Language.tr("Statistics.Skewness")+": Sk[V]="+StatisticTools.formatNumber(residenceTime.getSk()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+": Kurt[V]="+StatisticTools.formatNumber(residenceTime.getKurt()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.Kurt));
			addLine(Language.tr("Statistics.MinimumResidenceTime")+": Min[V]="+timeAndNumber(residenceTime.getMin()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.MaximumResidenceTime")+": Max[V]="+timeAndNumber(residenceTime.getMax()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoTime("V",residenceTime);

			outputConfidenceData(residenceTime);
		}

		if (hasProcessingTimes && hasResidenceTimes && processingTime.getMean()>0) {
			addHeading(3,Language.tr("Statistics.FlowFactor"));
			beginParagraph();
			addLine(Language.tr("Statistics.FlowFactor")+": "+StatisticTools.formatNumber(residenceTime.getMean()/processingTime.getMean()));
			endParagraph();
		}

		if (!hasWaitingTimes && !hasTransferTimes && !hasProcessingTimes && !hasResidenceTimes) {
			beginParagraph();
			addLine(Language.tr("Statistics.NoWaitingTransferProcessingTimes"));
			endParagraph();
		}
	}

	/**
	 * Ausgabe von
	 * Wartezeit auf Kundentyp-Basis
	 * @see Mode#MODE_WAITINGPROCESSING_CLIENTS
	 */
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

	/**
	 * Ausgabe von
	 * Wartezeiten an den Stationen
	 * @see Mode#MODE_WAITINGPROCESSING_STATIONS
	 */
	private void buildStations() {
		addHeading(1,Language.tr("Statistics.WaitingAndProcessTimesByStations"));

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		final String[] stations=statistics.stationsWaitingTimes.getNames();

		for (String station : stations) {
			final StatisticsDataPerformanceIndicator waitingTime=(StatisticsDataPerformanceIndicator)(statistics.stationsWaitingTimes.get(station));
			final StatisticsDataPerformanceIndicator transferTime=(StatisticsDataPerformanceIndicator)(statistics.stationsTransferTimes.get(station));
			final StatisticsDataPerformanceIndicator processingTime=(StatisticsDataPerformanceIndicator)(statistics.stationsProcessingTimes.get(station));
			final StatisticsDataPerformanceIndicator residenceTime=(StatisticsDataPerformanceIndicator)(statistics.stationsResidenceTimes.get(station));
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
					addLine(Language.tr("Statistics.Skewness")+": Sk[W]="+StatisticTools.formatNumber(waitingTime.getSk()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[W]="+StatisticTools.formatNumber(waitingTime.getKurt()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.Kurt));
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
					addLine(Language.tr("Statistics.Skewness")+": Sk[T]="+StatisticTools.formatNumber(transferTime.getSk()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[T]="+StatisticTools.formatNumber(transferTime.getKurt()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.Kurt));
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
					addLine(Language.tr("Statistics.Skewness")+": Sk[S]="+StatisticTools.formatNumber(processingTime.getSk()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[S]="+StatisticTools.formatNumber(processingTime.getKurt()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.Kurt));
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
					addLine(Language.tr("Statistics.Skewness")+": Sk[V]="+StatisticTools.formatNumber(residenceTime.getSk()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[V]="+StatisticTools.formatNumber(residenceTime.getKurt()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.Kurt));
					addLine(Language.tr("Statistics.MinimumResidenceTime")+": Min[V]="+timeAndNumber(residenceTime.getMin()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumResidenceTime")+": Max[V]="+timeAndNumber(residenceTime.getMax()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MAXIMUM));
					endParagraph();

					outputQuantilInfoTime("V",residenceTime);

					outputConfidenceData(residenceTime);
				}

				if (processingTime.getMean()>0 && residenceTime.getMean()>0) {
					addHeading(3,Language.tr("Statistics.FlowFactor"));
					beginParagraph();
					addLine(Language.tr("Statistics.FlowFactor")+": "+StatisticTools.formatNumber(residenceTime.getMean()/processingTime.getMean()));
					endParagraph();
				}

				boolean inThroughputParagraph=false;

				final StatisticsDataPerformanceIndicator indicatorThroughput=(StatisticsDataPerformanceIndicator)(statistics.stationsInterarrivalTime.get(station));
				final long arrivalCount=indicatorThroughput.getCount();
				if (arrivalCount>0) {
					inThroughputParagraph=true;
					addHeading(3,Language.tr("Statistics.Throughput"));
					beginParagraph();
					addLine(Language.tr("Statistics.Throughput")+": "+getThroughputText(arrivalCount,statistics));
				}

				final StatisticsSimpleValueMaxPerformanceIndicator indicatorThroughputMax=(StatisticsSimpleValueMaxPerformanceIndicator)(statistics.stationsMaxThroughput.get(station));
				if (indicatorThroughputMax!=null && indicatorThroughputMax.get()>0) {
					if (!inThroughputParagraph) {
						addHeading(3,Language.tr("Statistics.Throughput"));
						beginParagraph();
					}
					String maxThroughputInfo="";
					final ModelElementBox element=getStationFromStatisticsName(station,statistics);
					if (element!=null && element.getMaxThroughputIntervalSeconds()>0) maxThroughputInfo=" ("+String.format(Language.tr("Statistics.Throughput.Maximum.IntervalLength"),NumberTools.formatLong(element.getMaxThroughputIntervalSeconds()))+")";
					addLine(Language.tr("Statistics.Throughput.Maximum")+": "+getMaxThroughputText(indicatorThroughputMax.get())+maxThroughputInfo);
					endParagraph();
				}

				if (inThroughputParagraph) {
					endParagraph();
				}
			}
		}

		/* Infotext  */
		addDescription("TimeStations");
	}

	/**
	 * Ausgabe von
	 * Wartezeiten an den Stationen
	 * @see Mode#MODE_WAITINGPROCESSING_STATIONS_TOTAL
	 */
	private void buildStationsTotal() {
		addHeading(1,Language.tr("Statistics.WaitingAndProcessTimesByStationsTotal"));

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		final String[] stations=statistics.stationsTotalWaitingTimes.getNames();

		for (String station : stations) {
			final StatisticsDataPerformanceIndicator waitingTime=(StatisticsDataPerformanceIndicator)(statistics.stationsTotalWaitingTimes.get(station));
			final StatisticsDataPerformanceIndicator transferTime=(StatisticsDataPerformanceIndicator)(statistics.stationsTotalTransferTimes.get(station));
			final StatisticsDataPerformanceIndicator processingTime=(StatisticsDataPerformanceIndicator)(statistics.stationsTotalProcessingTimes.get(station));
			final StatisticsDataPerformanceIndicator residenceTime=(StatisticsDataPerformanceIndicator)(statistics.stationsTotalResidenceTimes.get(station));
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
					addLine(Language.tr("Statistics.Skewness")+": Sk[W]="+StatisticTools.formatNumber(waitingTime.getSk()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[W]="+StatisticTools.formatNumber(waitingTime.getKurt()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.Kurt));
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
					addLine(Language.tr("Statistics.Skewness")+": Sk[T]="+StatisticTools.formatNumber(transferTime.getSk()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[T]="+StatisticTools.formatNumber(transferTime.getKurt()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.Kurt));
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
					addLine(Language.tr("Statistics.Skewness")+": Sk[S]="+StatisticTools.formatNumber(processingTime.getSk()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[S]="+StatisticTools.formatNumber(processingTime.getKurt()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.Kurt));
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
					addLine(Language.tr("Statistics.Skewness")+": Sk[V]="+StatisticTools.formatNumber(residenceTime.getSk()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[V]="+StatisticTools.formatNumber(residenceTime.getKurt()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.Kurt));
					addLine(Language.tr("Statistics.MinimumResidenceTime")+": Min[V]="+timeAndNumber(residenceTime.getMin()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumResidenceTime")+": Max[V]="+timeAndNumber(residenceTime.getMax()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MAXIMUM));
					endParagraph();

					outputQuantilInfoTime("V",residenceTime);

					outputConfidenceData(residenceTime);
				}

				if (processingTime.getMean()>0 && residenceTime.getMean()>0) {
					addHeading(3,Language.tr("Statistics.FlowFactor"));
					beginParagraph();
					addLine(Language.tr("Statistics.FlowFactor")+": "+StatisticTools.formatNumber(residenceTime.getMean()/processingTime.getMean()));
					endParagraph();
				}
			}
		}

		/* Infotext  */
		addDescription("TimeStationsTotal");
	}

	/**
	 * Ausgabe von
	 * Wartezeiten an den Stationen (zusätzlich nach Kundentypen ausdifferenziert)
	 * @see Mode#MODE_WAITINGPROCESSING_STATIONS_CLIENTS
	 */
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
				addLine(Language.tr("Statistics.Skewness")+": Sk[W]="+StatisticTools.formatNumber(waitingTime.getSk()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.Sk));
				addLine(Language.tr("Statistics.Kurt")+": Kurt[W]="+StatisticTools.formatNumber(waitingTime.getKurt()),fastAccessBuilder.getXMLSelector(waitingTime,IndicatorMode.Kurt));
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
				addLine(Language.tr("Statistics.Skewness")+": Sk[T]="+StatisticTools.formatNumber(transferTime.getSk()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.Sk));
				addLine(Language.tr("Statistics.Kurt")+": Kurt[T]="+StatisticTools.formatNumber(transferTime.getKurt()),fastAccessBuilder.getXMLSelector(transferTime,IndicatorMode.Kurt));
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
				addLine(Language.tr("Statistics.Skewness")+": Sk[S]="+StatisticTools.formatNumber(processingTime.getSk()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.Sk));
				addLine(Language.tr("Statistics.Kurt")+": Kurt[S]="+StatisticTools.formatNumber(processingTime.getKurt()),fastAccessBuilder.getXMLSelector(processingTime,IndicatorMode.Kurt));
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
				addLine(Language.tr("Statistics.Skewness")+": Sk[V]="+StatisticTools.formatNumber(residenceTime.getSk()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.Sk));
				addLine(Language.tr("Statistics.Kurt")+": Kurt[V]="+StatisticTools.formatNumber(residenceTime.getKurt()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.Kurt));
				addLine(Language.tr("Statistics.MinimumResidenceTime")+": Min[V]="+timeAndNumber(residenceTime.getMin()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumResidenceTime")+": Max[V]="+timeAndNumber(residenceTime.getMax()),fastAccessBuilder.getXMLSelector(residenceTime,IndicatorMode.MAXIMUM));
				endParagraph();

				outputQuantilInfoTime("V",residenceTime);

				outputConfidenceData(residenceTime);
			}
		}

		for (String name : statistics.stationsProcessingTimesByClientType.getNames()) {
			final StatisticsDataPerformanceIndicator processingTime=(StatisticsDataPerformanceIndicator)(statistics.stationsProcessingTimesByClientType.get(name));
			final StatisticsDataPerformanceIndicator residenceTime=(StatisticsDataPerformanceIndicator)(statistics.stationsResidenceTimesByClientType.get(name));
			if (processingTime.getMean()>0 && residenceTime.getMean()>0) {
				addHeading(2,Language.tr("Statistics.FlowFactor")+" - "+name);
				beginParagraph();
				addLine(Language.tr("Statistics.FlowFactor")+": "+StatisticTools.formatNumber(residenceTime.getMean()/processingTime.getMean()));
				endParagraph();
			}
		}



		/* Infotext  */
		addDescription("TimeStationsClients");
	}

	/**
	 * Ausgabe von
	 * Anzahl an Kunden im System und an den Stationen
	 * @see Mode#MODE_CLIENTS_COUNT
	 */
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
		addLine(Language.tr("Statistics.Skewness")+": Sk[N]="+StatisticTools.formatNumber(statistics.clientsInSystem.getTimeSk()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystem,IndicatorMode.Sk));
		addLine(Language.tr("Statistics.Kurt")+": Kurt[N]="+StatisticTools.formatNumber(statistics.clientsInSystem.getTimeKurt()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystem,IndicatorMode.Kurt));
		addLine(Language.tr("Statistics.MinimumClientsInSystem")+": Min[N]="+StatisticTools.formatNumber(statistics.clientsInSystem.getTimeMin()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystem,IndicatorMode.MINIMUM));
		addLine(Language.tr("Statistics.MaximumClientsInSystem")+": Max[N]="+StatisticTools.formatNumber(statistics.clientsInSystem.getTimeMax()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystem,IndicatorMode.MAXIMUM));
		endParagraph();

		outputShortStateDistribution("N",statistics.clientsInSystem);

		outputQuantilInfoNumber("N",statistics.clientsInSystem);

		outputConfidenceData(statistics.clientsInSystem);

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
				addLine(Language.tr("Statistics.Skewness")+": Sk[N]="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
				addLine(Language.tr("Statistics.Kurt")+": Kurt[N]="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
				addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[N]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[N]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
				endParagraph();

				outputShortStateDistribution("N",indicator);

				outputQuantilInfoNumber("N",indicator);

				outputConfidenceData(indicator);
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
					addLine(Language.tr("Statistics.AverageNumberOfClients")+": E[N]="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
					addLine(Language.tr("Statistics.StdDevNumberOfClients")+": Std[N]="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceNumberOfClients")+": Var[N]="+StatisticTools.formatNumber(indicator.getTimeVar()));
					addLine(Language.tr("Statistics.CVNumberOfClients")+": CV[N]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
					addLine(Language.tr("Statistics.Skewness")+": Sk[N]="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[N]="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
					addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[N]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[N]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();

					outputShortStateDistribution("N",indicator);

					outputQuantilInfoNumber("N",indicator);

					outputConfidenceData(indicator);
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
					addLine(Language.tr("Statistics.AverageNumberOfClients")+": E[N]="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
					addLine(Language.tr("Statistics.StdDevNumberOfClients")+": Std[N]="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceNumberOfClients")+": Var[N]="+StatisticTools.formatNumber(indicator.getTimeVar()));
					addLine(Language.tr("Statistics.CVNumberOfClients")+": CV[N]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
					addLine(Language.tr("Statistics.Skewness")+": Sk[N]="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[N]="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
					addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[N]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[N]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();

					outputShortStateDistribution("N",indicator);

					outputQuantilInfoNumber("N",indicator);

					outputConfidenceData(indicator);
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
		addLine(Language.tr("Statistics.Skewness")+": Sk[NQ]="+StatisticTools.formatNumber(statistics.clientsInSystemQueues.getTimeSk()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemQueues,IndicatorMode.Sk));
		addLine(Language.tr("Statistics.Kurt")+": Kurt[NQ]="+StatisticTools.formatNumber(statistics.clientsInSystemQueues.getTimeKurt()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemQueues,IndicatorMode.Kurt));
		addLine(Language.tr("Statistics.MinimumClientsInSystemWaiting")+": Min[NQ]="+StatisticTools.formatNumber(statistics.clientsInSystemQueues.getTimeMin()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemQueues,IndicatorMode.MINIMUM));
		addLine(Language.tr("Statistics.MaximumClientsInSystemWaiting")+": Max[NQ]="+StatisticTools.formatNumber(statistics.clientsInSystemQueues.getTimeMax()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemQueues,IndicatorMode.MAXIMUM));
		endParagraph();

		outputShortStateDistribution("NQ",statistics.clientsInSystemQueues);

		outputQuantilInfoNumber("NQ",statistics.clientsInSystemQueues);

		outputConfidenceData(statistics.clientsInSystemQueues);

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
				addLine(Language.tr("Statistics.Skewness")+": Sk[NQ]="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
				addLine(Language.tr("Statistics.Kurt")+": Kurt[NQ]="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
				addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[NQ]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[NQ]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
				endParagraph();

				outputShortStateDistribution("NQ",indicator);

				outputQuantilInfoNumber("NQ",indicator);

				outputConfidenceData(indicator);
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
					addLine(Language.tr("Statistics.Skewness")+": Sk[NQ]="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[NQ]="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
					addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[NQ]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[NQ]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();

					outputShortStateDistribution("NQ",indicator);

					outputQuantilInfoNumber("NQ",indicator);

					outputConfidenceData(indicator);
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
					addLine(Language.tr("Statistics.Skewness")+": Sk[NQ]="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[NQ]="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
					addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[NQ]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[NQ]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();

					outputShortStateDistribution("NQ",indicator);

					outputQuantilInfoNumber("NQ",indicator);

					outputConfidenceData(indicator);
				}
			}
		}

		/* Kunden im System (in Bedienung) */

		addHeading(2,Language.tr("Statistics.NumberOfClientsInTheSystemInProcess"));
		beginParagraph();
		addLine(Language.tr("Statistics.AverageClientsInSystemInProcess")+": E[NS]="+StatisticTools.formatNumber(statistics.clientsInSystemProcess.getTimeMean()),xmlMean(statistics.clientsInSystemProcess));
		addLine(Language.tr("Statistics.StdDevClientsInSystemInProcess")+": Std[NS]="+StatisticTools.formatNumber(statistics.clientsInSystemProcess.getTimeSD()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemProcess,IndicatorMode.SD));
		addLine(Language.tr("Statistics.VarianceClientsInSystemInProcess")+": Var[NS]="+StatisticTools.formatNumber(statistics.clientsInSystemProcess.getTimeVar()));
		addLine(Language.tr("Statistics.CVClientsInSystemInProcess")+": CV[NS]="+StatisticTools.formatNumber(statistics.clientsInSystemProcess.getTimeCV()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemProcess,IndicatorMode.CV));
		addLine(Language.tr("Statistics.Skewness")+": Sk[NS]="+StatisticTools.formatNumber(statistics.clientsInSystemProcess.getTimeSk()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemProcess,IndicatorMode.Sk));
		addLine(Language.tr("Statistics.Kurt")+": Kurt[NS]="+StatisticTools.formatNumber(statistics.clientsInSystemProcess.getTimeKurt()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemProcess,IndicatorMode.Kurt));
		addLine(Language.tr("Statistics.MinimumClientsInSystemInProcess")+": Min[NS]="+StatisticTools.formatNumber(statistics.clientsInSystemProcess.getTimeMin()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemProcess,IndicatorMode.MINIMUM));
		addLine(Language.tr("Statistics.MaximumClientsInSystemInProcess")+": Max[NS]="+StatisticTools.formatNumber(statistics.clientsInSystemProcess.getTimeMax()),fastAccessBuilder.getXMLSelector(statistics.clientsInSystemProcess,IndicatorMode.MAXIMUM));
		endParagraph();

		outputShortStateDistribution("NS",statistics.clientsInSystemProcess);

		outputQuantilInfoNumber("NS",statistics.clientsInSystemProcess);

		outputConfidenceData(statistics.clientsInSystemProcess);

		/* Kunden an den Stationen (in Bedienung) */

		addHeading(2,Language.tr("Statistics.NumberOfClientsAtStationProcess"));
		indicators=statistics.clientsAtStationProcessByStation;
		for (String station : indicators.getNames()) {
			final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(station));
			if (indicator.getTimeMean()>0) {
				addHeading(3,fullStationName(station));
				beginParagraph();
				addLine(Language.tr("Statistics.AverageNumberOfClients")+": E[NS]="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
				addLine(Language.tr("Statistics.StdDevNumberOfClients")+": Std[NS]="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
				addLine(Language.tr("Statistics.VarianceNumberOfClients")+": Var[NS]="+StatisticTools.formatNumber(indicator.getTimeVar()));
				addLine(Language.tr("Statistics.CVNumberOfClients")+": CV[NS]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
				addLine(Language.tr("Statistics.Skewness")+": Sk[NS]="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
				addLine(Language.tr("Statistics.Kurt")+": Kurt[NS]="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
				addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[NS]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[NS]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
				endParagraph();

				outputShortStateDistribution("NS",indicator);

				outputQuantilInfoNumber("NS",indicator);

				outputConfidenceData(indicator);
			}
		}

		/* Kundentypen (in Bedienung) */

		indicators=statistics.clientsAtStationProcessByClient;
		if (indicators.size()>1) {
			addHeading(2,Language.tr("Statistics.NumberOfClientsInProcess"));
			for (String clientTypes : indicators.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(clientTypes));
				if (indicator.getTimeMean()>0) {
					addHeading(3,clientTypes);
					beginParagraph();
					addLine(Language.tr("Statistics.AverageNumberOfClients")+": E[NS]="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
					addLine(Language.tr("Statistics.StdDevNumberOfClients")+": Std[NS]="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceNumberOfClients")+": Var[NS]="+StatisticTools.formatNumber(indicator.getTimeVar()));
					addLine(Language.tr("Statistics.CVNumberOfClients")+": CV[NS]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
					addLine(Language.tr("Statistics.Skewness")+": Sk[NS]="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[NS]="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
					addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[NS]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[NS]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();

					outputShortStateDistribution("NS",indicator);

					outputQuantilInfoNumber("NS",indicator);

					outputConfidenceData(indicator);
				}
			}
		}

		/* Kundentypen an den Stationen (in Bedienung) */

		indicators=statistics.clientsAtStationProcessByStationAndClient;
		if (indicators.size()>1) {
			addHeading(2,Language.tr("Statistics.NumberOfClientsAtStationProcessByClientTypes"));
			for (String station : indicators.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)(indicators.get(station));
				if (indicator.getTimeMean()>0) {
					addHeading(3,fullStationName(station));
					beginParagraph();
					addLine(Language.tr("Statistics.AverageNumberOfClients")+": E[NS]="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
					addLine(Language.tr("Statistics.StdDevNumberOfClients")+": Std[NS]="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceNumberOfClients")+": Var[NS]="+StatisticTools.formatNumber(indicator.getTimeVar()));
					addLine(Language.tr("Statistics.CVNumberOfClients")+": CV[NS]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
					addLine(Language.tr("Statistics.Skewness")+": Sk[NS]="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[NS]="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
					addLine(Language.tr("Statistics.MinimumNumberOfClients")+": Min[NS]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumNumberOfClients")+": Max[NS]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();

					outputShortStateDistribution("NS",indicator);

					outputQuantilInfoNumber("NS",indicator);

					outputConfidenceData(indicator);
				}
			}
		}

		/* Infotext  */
		addDescription("ClientCount");
	}

	/**
	 * Ausgabe von
	 * Auslastung der Bedienergruppen
	 * @see Mode#MODE_UTILIZATION
	 */
	private void buildUtilization() {
		addHeading(1,Language.tr("Statistics.ResourceUtilization"));

		addHeading(2,Language.tr("Statistics.ResourceUtilization.All"));
		beginParagraph();
		final double rhoAll=statistics.resourceRhoAll.get();
		String rhoInfo="";
		if (rhoAll>0) rhoInfo=" (rho="+StatisticTools.formatPercent(rhoAll)+")";
		addLine(Language.tr("Statistics.AverageNumberOfBusyOperators")+"="+StatisticTools.formatNumber(statistics.resourceUtilizationAll.getTimeMean(),2)+rhoInfo);
		endParagraph();

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
							addLine(Language.tr("Statistics.Utilization.NumberOfOperators.Sk")+"="+StatisticTools.formatNumber(countIndicator.getTimeSk()),fastAccessBuilder.getXMLSelector(countIndicator,IndicatorMode.Sk));
							addLine(Language.tr("Statistics.Utilization.NumberOfOperators.Kurt")+"="+StatisticTools.formatNumber(countIndicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(countIndicator,IndicatorMode.Kurt));
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
			addLine(Language.tr("Statistics.Utilization.Sk")+"="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Utilization.Kurt")+"="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
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
					addLine(Language.tr("Statistics.FailureTime.Sk")+"="+StatisticTools.formatNumber(indicator2.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.FailureTime.Kurt")+"="+StatisticTools.formatNumber(indicator2.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.Kurt));
					addLine(Language.tr("Statistics.FailureTime.Minimum")+"="+StatisticTools.formatNumber(indicator2.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.FailureTime.Maximum")+"="+StatisticTools.formatNumber(indicator2.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.MAXIMUM));
					if (resourceObj.getMode()==ModelResource.Mode.MODE_NUMBER) {
						final int count=resourceObj.getCount();
						if (count>0) addLine(Language.tr("Statistics.FailureTime.AveragePartOfDownTimeOperators")+": "+StatisticTools.formatPercent(indicator2.getTimeMean()/count));
					}
					endParagraph();
				}
			}

			outputConfidenceData(indicator);
		}

		/* Infotext  */
		addDescription("UtilizationResource");
	}

	/**
	 * Ausgabe von
	 * Auslastung der Transportergruppen
	 * @see Mode#MODE_TRANSPORTER_UTILIZATION
	 */
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
			addLine(Language.tr("Statistics.TransporterUtilization.Sk")+"="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.TransporterUtilization.Kurt")+"="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
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
					addLine(Language.tr("Statistics.FailureTime.Sk.Transporters")+"="+StatisticTools.formatNumber(indicator2.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.FailureTime.Kurt.Transporters")+"="+StatisticTools.formatNumber(indicator2.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.Kurt));
					addLine(Language.tr("Statistics.FailureTime.Minimum.Transporters")+"="+StatisticTools.formatNumber(indicator2.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.FailureTime.Maximum.Transporters")+"="+StatisticTools.formatNumber(indicator2.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator2,IndicatorMode.MAXIMUM));
					final int count=transporter.getCountAll();
					if (count>0) addLine(Language.tr("Statistics.FailureTime.AveragePartOfDownTimeTransporters")+": "+StatisticTools.formatPercent(indicator2.getTimeMean()/count));
				}
				endParagraph();
			}

			outputConfidenceData(indicator);
		}

		/* Infotext  */
		addDescription("UtilizationTransporter");
	}

	/**
	 * Gibt Zählerwerte aus.
	 * @param level	Level für Überschriften
	 * @param details	Detailausgabe?
	 * @see #buildCounter()
	 */
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
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)statistics.differentialCounter.get(name);
				if (details) {
					addHeading(level+1,name);
					beginParagraph();
					addLine(Language.tr("Statistics.Average")+"="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
					addLine(Language.tr("Statistics.StdDev")+"="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
					addLine(Language.tr("Statistics.Variance")+"="+StatisticTools.formatNumber(indicator.getTimeVar()));
					addLine(Language.tr("Statistics.CV")+"="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
					addLine(Language.tr("Statistics.Skewness")+"="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+"="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
					addLine(Language.tr("Statistics.MinimumNumber")+"="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximumNumber")+"="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();
					outputConfidenceData(indicator);
				} else {
					addLine(name+" "+Language.tr("Statistics.Average")+": "+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
				}
			}
			if (!details) endParagraph();
		}

		/* Batch-Zähler */

		if (statistics.counterBatch.size()>0) {
			addHeading(level,Language.tr("Statistics.CounterBatch"));
			if (!details) beginParagraph();
			for (String name: statistics.counterBatch.getNames()) {
				final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.counterBatch.get(name);
				if (indicator.getCount()==0) continue;
				if (details) {
					addHeading(level+1,name);
					beginParagraph();
					addLine(Language.tr("Statistics.AverageInterArrivalCount")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
					addLine(Language.tr("Statistics.AverageInterArrivalTime")+": E[IB]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
					addLine(Language.tr("Statistics.StdDevInterArrivalTime")+": Std[IB]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
					addLine(Language.tr("Statistics.VarianceInterArrivalTime")+": Var[IB]="+timeAndNumber(indicator.getVar()));
					addLine(Language.tr("Statistics.CVInterArrivalTime")+": CV[IB]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
					addLine(Language.tr("Statistics.Skewness")+": Sk[IB]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
					addLine(Language.tr("Statistics.Kurt")+": Kurt[IB]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
					addLine(Language.tr("Statistics.MinimalInterArrivalTime")+": Min[IB]="+timeAndNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
					addLine(Language.tr("Statistics.MaximalInterArrivalTime")+": Max[IB]="+timeAndNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
					endParagraph();
				} else {
					addLine(name+" "+Language.tr("Statistics.AverageInterArrivalCount")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));
					addLine(name+" "+Language.tr("Statistics.AverageInterArrivalTime")+": E[IB]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
				}
			}
			if (!details) endParagraph();
		}
	}

	/**
	 * Ausgabe von
	 * Zählerwerten
	 * @see Mode#MODE_COUNTER
	 */
	private void buildCounter() {
		addHeading(1,Language.tr("Statistics.Counter"));

		if (statistics.counter.size()==0 && statistics.differentialCounter.size()==0 && statistics.counterBatch.size()==0 ) {
			beginParagraph();
			addLine(Language.tr("Statistics.Counter.NoCounter"));
			endParagraph();
		} else {
			buildCounterInt(2,true);
		}

		/* Infotext  */
		addDescription("Counter");
	}

	/**
	 * Gibt Durchsatzwerte aus.
	 * @see #buildThroughput()
	 */
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

	/**
	 * Ausgabe von
	 * Durchsatzwerten
	 * @see Mode#MODE_THROUGHPUT
	 */
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

	/**
	 * Ausgabe von
	 * "Zustandsstatistik" (erfasst an speziellen Zustandsstatistik-Stationen)
	 * @see Mode#MODE_STATE_STATISTICS
	 */
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

	/**
	 * Ausgabe von
	 * Analogwert-Statistik
	 * @see Mode#MODE_ANALOG_STATISTICS
	 */
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

	/**
	 * Ausgabe von
	 * Kosten
	 * @see Mode#MODE_COSTS
	 */
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
			addLine(Language.tr("Statistics.Costs.ByWaitingTimes")+" ("+Language.tr("Statistics.total")+"): "+NumberTools.formatNumberLong(w),fastAccessBuilder.getXMLSelector(wIndicator,IndicatorMode.VALUE));
			addLine(Language.tr("Statistics.Costs.ByTransferTimes")+" ("+Language.tr("Statistics.total")+"): "+NumberTools.formatNumberLong(t),fastAccessBuilder.getXMLSelector(tIndicator,IndicatorMode.VALUE));
			addLine(Language.tr("Statistics.Costs.ByProcessTimes")+" ("+Language.tr("Statistics.total")+"): "+NumberTools.formatNumberLong(p),fastAccessBuilder.getXMLSelector(pIndicator,IndicatorMode.VALUE));
			addLine(Language.tr("Statistics.Costs.Total")+" ("+Language.tr("Statistics.total")+"): "+NumberTools.formatNumberLong(w+t+p));
			endParagraph();
			if (count>0) {
				beginParagraph();
				addLine(Language.tr("Statistics.Costs.ByWaitingTimes")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+NumberTools.formatNumberLong(w/c));
				addLine(Language.tr("Statistics.Costs.ByTransferTimes")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+NumberTools.formatNumberLong(t/c));
				addLine(Language.tr("Statistics.Costs.ByProcessTimes")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+NumberTools.formatNumberLong(p/c));
				addLine(Language.tr("Statistics.Costs.Total")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+NumberTools.formatNumberLong((w+t+p)/c));
				endParagraph();
			}
		}

		addHeading(3,Language.tr("Statistics.SumOverAllClientTypes"));
		beginParagraph();
		addLine(Language.tr("Statistics.Costs.ByWaitingTimes")+" ("+Language.tr("Statistics.total")+"): "+NumberTools.formatNumberLong(waiting));
		addLine(Language.tr("Statistics.Costs.ByTransferTimes")+" ("+Language.tr("Statistics.total")+"): "+NumberTools.formatNumberLong(transfer));
		addLine(Language.tr("Statistics.Costs.ByProcessTimes")+" ("+Language.tr("Statistics.total")+"): "+NumberTools.formatNumberLong(process));
		addLine(Language.tr("Statistics.Costs.Total")+" ("+Language.tr("Statistics.total")+"): "+NumberTools.formatNumberLong(waiting+transfer+process));
		endParagraph();
		if (count>0) {
			beginParagraph();
			addLine(Language.tr("Statistics.Costs.ByWaitingTimes")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+NumberTools.formatNumberLong(waiting/count));
			addLine(Language.tr("Statistics.Costs.ByTransferTimes")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+NumberTools.formatNumberLong(transfer/count));
			addLine(Language.tr("Statistics.Costs.ByProcessTimes")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+NumberTools.formatNumberLong(process/count));
			addLine(Language.tr("Statistics.Costs.Total")+" ("+Language.tr("Statistics.inAveragePerClient")+"): "+NumberTools.formatNumberLong((waiting+transfer+process)/count));
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
			addLine(Language.tr("Statistics.CostsAtStation")+" \""+name+"\": "+NumberTools.formatNumberLong(s)+" ("+Language.tr("Statistics.Part")+": "+StatisticTools.formatPercent(s/station)+")",fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.VALUE));
			endParagraph();
		}

		addHeading(3,Language.tr("Statistics.SumOverAllStations"));
		beginParagraph();
		addLine(Language.tr("Statistics.Costs")+": "+NumberTools.formatNumberLong(sum));
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
			addLine(Language.tr("Statistics.CostsForResources.Available.Single")+": "+NumberTools.formatNumberLong(t),fastAccessBuilder.getXMLSelector(tIndicator,IndicatorMode.VALUE));
			addLine(Language.tr("Statistics.CostsForResources.Working.Single")+": "+NumberTools.formatNumberLong(w),fastAccessBuilder.getXMLSelector(wIndicator,IndicatorMode.VALUE));
			addLine(Language.tr("Statistics.CostsForResources.Idle.Single")+": "+NumberTools.formatNumberLong(i),fastAccessBuilder.getXMLSelector(iIndicator,IndicatorMode.VALUE));
			addLine(Language.tr("Statistics.Costs.Total")+": "+NumberTools.formatNumberLong(t+w+i));
			endParagraph();

		}

		addHeading(3,Language.tr("Statistics.SumOverAllResources"));
		beginParagraph();
		addLine(Language.tr("Statistics.CostsForResources.Available.All")+": "+NumberTools.formatNumberLong(time));
		addLine(Language.tr("Statistics.CostsForResources.Working.All")+": "+NumberTools.formatNumberLong(work));
		addLine(Language.tr("Statistics.CostsForResources.Idle.All")+": "+NumberTools.formatNumberLong(idle));
		addLine(Language.tr("Statistics.Costs.Total")+": "+NumberTools.formatNumberLong(time+work+idle));
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
	 * @see #outputAutocorrelationData(StatisticsDataPerformanceIndicator, int[])
	 */
	public static final double[] AUTOCORRELATION_LEVELS=new double[]{0.1,0.05,0.01,0.005,0.001};

	/**
	 * Gibt die Autokorrelationsdaten zu einem Statistikobjekt aus
	 * @param indicator	Statistikobjekt
	 * @param maxDistance	Abstände für die die Autokorrelationswerte ausgegeben werden sollen
	 */
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

	/**
	 * Ausgabe von
	 * Autokorrelationswerten
	 * @see Mode#MODE_AUTOCORRELATION
	 */
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

	/**
	 * In welchem Format soll ein Nutzer-Statistik-Bezeichner ausgegeben werden?
	 * @see StatisticViewerOverviewText#isUserStatisticsTime(Statistics, String)
	 */
	public enum UserStatisticsFormat {
		/** Als Zahl formatieren */
		NUMBER,
		/** Als Zeitangabe formatieren */
		TIME,
		/** Es wurde sowohl eine Formatierung als Zahl als auch als Zeit angegeben */
		MIXED,
	}

	/**
	 * Prüft, ob es sich bei einem Nutzer-Statistik-Bezeichner um eine Zeitangabe handelt.
	 * @param statistics	Statistik-Objekt aus dem die Daten ausgelesen werden sollen
	 * @param key	Nutzer-Statistik-Bezeichner
	 * @return Liefert <code>true</code>, wenn es sich um eine Zeitangabe handelt
	 * @see ModelElementUserStatistic
	 * @see UserStatisticsFormat
	 */
	public static UserStatisticsFormat isUserStatisticsTime(final Statistics statistics, final String key) {
		UserStatisticsFormat result=null;

		for (ModelElement element: statistics.editModel.surface.getElements()) {
			if (element instanceof ModelElementUserStatistic) {
				final ModelElementUserStatistic.IsTime B=((ModelElementUserStatistic)element).getIsTimeForKey(key);
				if (B!=ModelElementUserStatistic.IsTime.NOT_FOUND) {
					if (result==null) {
						if (B.bool) result=UserStatisticsFormat.TIME; else result=UserStatisticsFormat.NUMBER;
					} else {
						if ((B.bool && result==UserStatisticsFormat.NUMBER) || (!B.bool && result==UserStatisticsFormat.TIME)) result=UserStatisticsFormat.MIXED;
					}
				}
			}
			if (element instanceof ModelElementUserStatistic) {
				for (ModelElement sub: ((ModelElementUserStatistic)element).getSurface().getElements()) {
					if (sub instanceof ModelElementUserStatistic) {
						final ModelElementUserStatistic.IsTime B=((ModelElementUserStatistic)sub).getIsTimeForKey(key);
						if (B!=ModelElementUserStatistic.IsTime.NOT_FOUND) {
							if (result==null) {
								if (B.bool) result=UserStatisticsFormat.TIME; else result=UserStatisticsFormat.NUMBER;
							} else {
								if ((B.bool && result==UserStatisticsFormat.NUMBER) || (!B.bool && result==UserStatisticsFormat.TIME)) result=UserStatisticsFormat.MIXED;
							}
						}
					}
				}
			}
		}

		if (result==null) result=UserStatisticsFormat.NUMBER;
		return result;
	}

	/**
	 * Ausgabe von
	 * benutzerdefinierter Statistik
	 * @see Mode#MODE_USER_STATISTICS
	 */
	private void buildUserStatistics() {
		addHeading(1,Language.tr("Statistics.UserStatistics"));

		String repeatInfo="";
		if (statistics.simulationData.runRepeatCount>1) repeatInfo=" ("+Language.tr("Statistics.SimulatedClients.RepeatInfo")+")";

		if (statistics.userStatistics.size()==0 && statistics.userStatisticsContinuous.size()==0) {
			beginParagraph();
			addLine(Language.tr("Statistics.UserStatistics.NothingRecorded"));
			endParagraph();
		}

		/* Diskrete Werte */

		for (String name: statistics.userStatistics.getNames()) {
			final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)statistics.userStatistics.get(name);
			addHeading(2,name);
			final UserStatisticsFormat format=isUserStatisticsTime(statistics,name);

			if (format==UserStatisticsFormat.MIXED) {
				beginParagraph();
				addLine(Language.tr("Statistics.UserStatistics.TimeAndNumberWarning"));
				endParagraph();
			}

			beginParagraph();
			addLine(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(indicator.getCount())+repeatInfo,xmlCount(indicator));

			if (format==UserStatisticsFormat.TIME || format==UserStatisticsFormat.MIXED) {
				addLine(Language.tr("Statistics.AverageUserTime")+": E[X]="+timeAndNumber(indicator.getMean()),xmlMean(indicator));
				addLine(Language.tr("Statistics.StdDevUserTime")+": Std[X]="+timeAndNumber(indicator.getSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
				addLine(Language.tr("Statistics.VarianceUserTime")+": Var[X]="+timeAndNumber(indicator.getVar()));
				addLine(Language.tr("Statistics.CVUserTime")+": CV[X]="+StatisticTools.formatNumber(indicator.getCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
				addLine(Language.tr("Statistics.Skewness")+": Sk[X]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
				addLine(Language.tr("Statistics.Kurt")+": Kurt[X]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
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
				addLine(Language.tr("Statistics.Skewness")+": Sk[X]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
				addLine(Language.tr("Statistics.Kurt")+": Kurt[X]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
				addLine(Language.tr("Statistics.MinimumUser")+": Min[X]="+StatisticTools.formatNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumUser")+": Max[X]="+StatisticTools.formatNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
				endParagraph();

				outputQuantilInfoNumber("X",indicator);

				outputConfidenceData(indicator);
			}
		}

		/* Kontinuierliche Werte */

		for (String name: statistics.userStatisticsContinuous.getNames()) {
			final StatisticsTimeContinuousPerformanceIndicator indicator=(StatisticsTimeContinuousPerformanceIndicator)statistics.userStatisticsContinuous.get(name);
			addHeading(2,name);
			final UserStatisticsFormat format=isUserStatisticsTime(statistics,name);

			if (format==UserStatisticsFormat.MIXED) {
				beginParagraph();
				addLine(Language.tr("Statistics.UserStatistics.TimeAndNumberWarning"));
				endParagraph();
			}

			beginParagraph();

			if (format==UserStatisticsFormat.TIME || format==UserStatisticsFormat.MIXED) {
				addLine(Language.tr("Statistics.AverageUserTime")+": E[X]="+timeAndNumber(indicator.getTimeMean()),xmlMean(indicator));
				addLine(Language.tr("Statistics.StdDevUserTime")+": Std[X]="+timeAndNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
				addLine(Language.tr("Statistics.VarianceUserTime")+": Var[X]="+timeAndNumber(indicator.getTimeVar()));
				addLine(Language.tr("Statistics.CVUserTime")+": CV[X]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
				addLine(Language.tr("Statistics.Skewness")+": Sk[X]="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
				addLine(Language.tr("Statistics.Kurt")+": Kurt[X]="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
				addLine(Language.tr("Statistics.MinimumUserTime")+": Min[X]="+timeAndNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumUserTime")+": Max[X]="+timeAndNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
				endParagraph();
			} else {
				addLine(Language.tr("Statistics.AverageUser")+": E[X]="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
				addLine(Language.tr("Statistics.StdDevUser")+": Std[X]="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
				addLine(Language.tr("Statistics.VarianceUser")+": Var[X]="+StatisticTools.formatNumber(indicator.getTimeVar()));
				addLine(Language.tr("Statistics.CVUser")+": CV[X]="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
				addLine(Language.tr("Statistics.Skewness")+": Sk[X]="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
				addLine(Language.tr("Statistics.Kurt")+": Kurt[X]="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
				addLine(Language.tr("Statistics.MinimumUser")+": Min[X]="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
				addLine(Language.tr("Statistics.MaximumUser")+": Max[X]="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
				endParagraph();
			}
		}

		/* Infotext  */
		addDescription("UserStatistics");
	}

	/**
	 * Ausgabe von
	 * Statistik über die Kundendatenfelder
	 * @see Mode#MODE_CLIENT_DATA
	 */
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
			addLine(Language.tr("Statistics.ClientData.Field.Sk")+": Sk["+field+"]="+StatisticTools.formatNumber(indicator.getSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.ClientData.Field.Kurt")+": Kurt["+field+"]="+StatisticTools.formatNumber(indicator.getKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
			addLine(Language.tr("Statistics.ClientData.Field.Minimum")+": Min["+field+"]="+StatisticTools.formatNumber(indicator.getMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.ClientData.Field.Maximum")+": Max["+field+"]="+StatisticTools.formatNumber(indicator.getMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();

			outputQuantilInfoNumber(field,indicator);
		}

		/* Infotext  */
		addDescription("ClientData");
	}

	/**
	 * Ausgabe von
	 * Statistik über die globalen Variablen
	 * @see Mode#MODE_USER_VARIABLES
	 */
	private void buildUserVariables() {
		addHeading(1,Language.tr("Statistics.Variables"));

		for (String name: statistics.userVariables.getNames()) {
			final StatisticsTimeContinuousPerformanceIndicator indicator=(StatisticsTimeContinuousPerformanceIndicator)statistics.userVariables.get(name);
			addHeading(2,name);
			beginParagraph();
			addLine(Language.tr("Statistics.Average")+"="+StatisticTools.formatNumber(indicator.getTimeMean()),xmlMean(indicator));
			addLine(Language.tr("Statistics.StdDev")+"="+StatisticTools.formatNumber(indicator.getTimeSD()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.SD));
			addLine(Language.tr("Statistics.Variance")+"="+StatisticTools.formatNumber(indicator.getTimeVar()));
			addLine(Language.tr("Statistics.CV")+"="+StatisticTools.formatNumber(indicator.getTimeCV()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.CV));
			addLine(Language.tr("Statistics.Skewness")+"="+StatisticTools.formatNumber(indicator.getTimeSk()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Sk));
			addLine(Language.tr("Statistics.Kurt")+"="+StatisticTools.formatNumber(indicator.getTimeKurt()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.Kurt));
			addLine(Language.tr("Statistics.Minimum")+"="+StatisticTools.formatNumber(indicator.getTimeMin()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MINIMUM));
			addLine(Language.tr("Statistics.Maximum")+"="+StatisticTools.formatNumber(indicator.getTimeMax()),fastAccessBuilder.getXMLSelector(indicator,IndicatorMode.MAXIMUM));
			endParagraph();
		}

		/* Infotext  */
		addDescription("Variables");
	}

	@Override
	protected void buildText() {
		switch (mode) {
		case MODE_OVERVIEW: buildOverview(); break;
		case MODE_MODEL: buildModelInfo(); break;
		case MODE_MODEL_DESCRIPTION: buildModelDescription(); break;
		case FILE_OUTPUT: buildFileOutput(true); break;
		case MODE_SIM_TIME: buildSimTime(); break;
		case MODE_SYSTEM_INFO: buildSystemInfo(); break;
		case MODE_SYSTEM_INFO_THREAD_BALANCE: buildThreadBalanceInfo(); break;
		case MODE_INTERARRIVAL_CLIENTS: buildInterarrivalSystem(); break;
		case MODE_INTERARRIVAL_STATIONS: buildInterarrivalStations(); break;
		case MODE_INTERLEAVE_CLIENTS: buildInterleaveSystem(); break;
		case MODE_INTERLEAVE_STATIONS: buildInterleaveStations(); break;
		case MODE_WAITINGPROCESSING_CLIENTS: buildClients(); break;
		case MODE_WAITINGPROCESSING_STATIONS: buildStations(); break;
		case MODE_WAITINGPROCESSING_STATIONS_TOTAL: buildStationsTotal(); break;
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
		case MODE_USER_VARIABLES: buildUserVariables(); break;
		}
	}

	@Override
	public String[] ownSettingsName() {
		final List<String> names=new ArrayList<>();
		names.add(Language.tr("Statistics.TextSettings.DropdownName"));
		if (mode==Mode.MODE_OVERVIEW) names.add(Language.tr("Statistics.ResultsOverview.Filter"));
		return names.toArray(new String[0]);
	}

	@Override
	public Icon[] ownSettingsIcon() {
		final List<Icon> icons=new ArrayList<>();
		icons.add(Images.GENERAL_NUMBERS.getIcon());
		if (mode==Mode.MODE_OVERVIEW) icons.add(SimToolsImages.STATISTICS_TEXT.getIcon());
		return icons.toArray(new Icon[0]);
	}

	@Override
	public boolean ownSettings(final StatisticsBasePanel owner, final int nr) {
		boolean changed=false;
		BaseDialog dialog=null;
		switch (nr) {
		case 0:
			dialog=new StatisticViewerOverviewTextDialog(owner);
			break;
		case 1:
			dialog=new StatisticViewerOverviewTextFilterDialog(owner);
			break;
		}
		changed=(dialog!=null && dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK);
		if (changed) owner.recreateViewers();
		return changed;
	}
}