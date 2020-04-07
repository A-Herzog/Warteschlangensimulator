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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTree;

import org.w3c.dom.Element;

import language.Language;
import simulator.statistics.Statistics;
import statistics.StatisticsTimePerformanceIndicator;
import statistics.StatisticsValuePerformanceIndicator;
import systemtools.MsgBox;
import systemtools.statistics.StatisticNode;
import systemtools.statistics.StatisticViewer;
import systemtools.statistics.StatisticsBasePanel;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.statistics.analyticcompare.AnalyticInfo;
import xml.XMLTools;

/**
 * Diese Klasse erlaubt die Anzeige einer oder mehrerer Statistikdaten
 * @author Alexander Herzog
 */
public class StatisticsPanel extends StatisticsBasePanel {
	private static final long serialVersionUID = 6515474808784376450L;

	private Statistics[] statistics;
	private StatisticViewerFastAccess fastAccess;

	/**
	 * Konstruktor der Klasse <code>StatisticsPanel</code>
	 * @param numberOfViewers	Anzahl der nebeneinander anzuzeigenden Ergebnisse
	 * @param startSimulation	Callback, das ausgelöst wird, wenn der Nutzer auf der "Noch keine Daten"-Seite auf "Simulation jetzt starten" klickt. (Wird hier <code>null</code> übergeben, so wird diese Option nicht angezeigt.)
	 */
	public StatisticsPanel(final int numberOfViewers, final Runnable startSimulation) {
		super(numberOfViewers,Language.tr("Main.Menu.View.SimulationResults"),Images.STATISTICS_DARK.getURL(),Language.trPrimary("CommandLine.Report.Name"),true);
		setCallBacks(startSimulation,()->loadStatistics(null),()->Help.topicModal(StatisticsPanel.this,"MainStatistik"));
		this.statistics=new Statistics[]{null};
		updateViewer(true);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticsPanel</code>
	 * @param numberOfViewers	Anzahl der nebeneinander anzuzeigenden Ergebnisse
	 */
	public StatisticsPanel(final int numberOfViewers) {
		this(numberOfViewers,null);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticsPanel</code>
	 * @param startSimulation	Callback, das ausgelöst wird, wenn der Nutzer auf der "Noch keine Daten"-Seite auf "Simulation jetzt starten" klickt. (Wird hier <code>null</code> übergeben, so wird diese Option nicht angezeigt.)
	 */
	public StatisticsPanel(final Runnable startSimulation) {
		this(1,startSimulation);
	}

	/**
	 * Wählt das vorgabemäßige Element in der Baumstruktur aus (wenn diese neu geladen wurde und nicht das zuletzt verwendete Element erneut ausgewählt werden kann)
	 * @param tree	Baumstruktur
	 * @param root	Struktur aus <code>StatisticNode</code>-Elementen, die den Inhalt des Baum repräsentieren
	 */
	@Override
	protected void selectDefaultTreeNode(final JTree tree, final StatisticNode root) {
		if (root!=null && root.getChildCount()>0) {
			if (root.getChild(0)!=null && root.getChild(0).viewer!=null && root.getChild(0).viewer.length==1 && root.getChild(0).viewer[0]!=null && root.getChild(0).viewer[0] instanceof StatisticViewerFastAccess) {
				tree.setSelectionRow(1);
				return;
			}
		}

		tree.setSelectionRow(0);
	}

	/**
	 * Diese Funktion wird aufgerufen, wenn die Einstellungen, welche Report-Einträge ausgewählt sein sollen, abgefragt werden sollen.
	 * @return	Einstellungen, welche Report-Einträge selektiert sein sollen
	 */
	@Override
	protected String getReportSelectSettings() {
		return SetupData.getSetup().reportSettings;
	}

	/**
	 * Diese Funktion wird aufgerufen, wenn die Einstellungen, welche Report-Einträge ausgewählt sind, gespeichert werden sollen.
	 * @param settings	Neue Einstellungen, welche Report-Einträge selektiert sind
	 */
	@Override
	protected void setReportSelectSettings(String settings) {
		SetupData setup=SetupData.getSetup();
		setup.reportSettings=settings;
		setup.saveSetup();
	}

	/**
	 * Lädt die Einstellung, ob Bilder bei HTML-Reports inline ausgegeben werden sollen, aus dem Setup.
	 * @return	Gibt an, ob Bilder bei bei HTML-Reports inline ausgegeben werden sollen.
	 */
	@Override
	protected boolean getImagesInlineSetting() {
		return SetupData.getSetup().imagesInline;
	}

	/**
	 * Speichert die Einstellung, ob Bilder bei HTML-Reports inline ausgegeben werden sollen, im Setup.
	 * @param imagesInline	Gibt an, ob Bilder bei HTML-Reports inline ausgegeben werden sollen.
	 */
	@Override
	protected void setImagesInlineSetting(final boolean imagesInline) {
		SetupData setup=SetupData.getSetup();
		setup.imagesInline=imagesInline;
		setup.saveSetup();
	}

	/**
	 * Liefert das bisher eingestellte Statistik-Objekt (kann auch <code>null</code> sein)
	 * @return	Aktuelles Statistik-Objekt
	 */
	public Statistics getStatistics() {
		if (statistics==null || statistics.length==0) return null; else return statistics[0];
	}

	/**
	 * Setzt ein Statistik-Objekt für die Anzeige (kann auch <code>null</code> sein, wenn nichts ausgegeben werden soll)
	 * @param statistics	Neues, anzuzeigendes Statistik-Objekt
	 */
	public void setStatistics(final Statistics statistics) {
		setStatistics(statistics,true);
	}

	/**
	 * Setzt ein Statistik-Objekt für die Anzeige (kann auch <code>null</code> sein, wenn nichts ausgegeben werden soll)
	 * @param statistics	Neues, anzuzeigendes Statistik-Objekt
	 * @param updateTree	Soll die Baumstruktur aktualisiert werden (sollte <code>true</code> sein, wenn das Panel tatsächlich grafisch verwendet werden soll und nicht nur als Datenquelle dienen soll)
	 */
	public void setStatistics(final Statistics statistics, final boolean updateTree) {
		lastRoot=null;
		if (statistics==null && (this.statistics==null || (this.statistics.length==1 && this.statistics[0]==null))) return;
		this.statistics=new Statistics[]{statistics};
		updateViewer(updateTree);
	}

	/**
	 * Setzt mehrere Statistik-Objekte für die parallele Anzeige (kann auch <code>null</code> sein, wenn nichts ausgegeben werden soll)
	 * @param data	Neue anzuzeigende Statistik-Objekte
	 * @param title	Titel über den Anzeigen
	 */
	public void setStatistics(Statistics[] data, String[] title) {
		lastRoot=null;

		/* Leeres Array abfangen */
		if (data==null || data.length==0) {
			data=new Statistics[Math.max(numberOfViewers,1)];
			Arrays.fill(data,null);
		}

		/* Zu lange oder zu kurze Arrays anpassen */
		Statistics[] data2=new Statistics[numberOfViewers];
		for (int i=0;i<data2.length;i++) data2[i]=(i<data.length)?data[i]:null;
		data=data2;

		statistics=data;

		/* Titel */
		String[] titleArray=new String[data.length];
		for (int i=0;i<titleArray.length;i++) {
			if (title==null || title.length<=i || title[i]==null) titleArray[i]=null; else titleArray[i]=title[i];
		}
		additionalTitle=titleArray;

		updateViewer(true);
	}

	/**
	 * Liefert alle Elemente der Statistikgruppe zurück. Sind keine Elemente gesetzt, so wird ein Element mit <code>null</code> als einzigem Eintrag geliefert.
	 * @return	Array aller Elemente der Statistikgruppe
	 */
	public Statistics[] getStatisticsGroup() {
		if (statistics==null || statistics.length==0) return new Statistics[]{null}; else return statistics;
	}

	/**
	 * Setzt mehrere Statistikelemente zur parallelen Anzeige
	 * @param statistics	Array mit allen anzuzeigenden Statistik-Elementen
	 */
	public void setStatisticsGroup(final Statistics[] statistics) {
		lastRoot=null;
		if (statistics==null || statistics.length==0) this.statistics=new Statistics[]{null}; else this.statistics=statistics;
		updateViewer(true);
	}

	/**
	 * Lädt die Statistikdaten aus einer Datei
	 * @param file	Datei, aus der die Statistikdaten geladen werden sollen. Wird hier <code>null</code> übergeben, so wird ein Dateiauswahl-Dialog angezeigt.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public String loadStatistics(File file) {
		if (file==null) {
			file=XMLTools.showLoadDialog(getParent(),Language.tr("Main.Toolbar.LoadStatistics"));
			if (file==null) return null;
		}

		Statistics newStatistics=new Statistics();
		String error=newStatistics.loadFromFile(file);
		if (error!=null) return error;

		setStatistics(newStatistics);

		return null;
	}

	/**
	 * Lädt die Statistikdaten aus einem XML-Element
	 * @param root	XML-Wurzelelement, aus dem die Statistikdaten geladen werden sollen.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public String loadStatisticsFromXML(final Element root) {
		Statistics newStatistics=new Statistics();
		String error=newStatistics.loadFromXML(root);
		if (error!=null) return error;

		setStatistics(newStatistics);

		return null;
	}

	/**
	 * Speichert die Statistikdaten in einer Datei
	 * @param file	Datei, in die die Statistikdaten geschrieben werden sollen. Wird hier <code>null</code> übergeben, so wird ein Dateiauswahl-Dialog angezeigt.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public String saveStatistics(File file) {
		if (statistics==null || statistics.length==0 || statistics[0]==null) return Language.tr("Main.Statistic.NoStatisticsAvailable");

		if (file==null) {
			file=XMLTools.showSaveDialog(getParent(),Language.tr("Main.Toolbar.SaveStatistics"),SetupData.getSetup().defaultSaveFormatStatistics);
			if (file==null) return null;
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(getTopLevelAncestor(),file)) return null;
		}

		if (!statistics[0].saveToFile(file)) return Language.tr("Main.Statistic.ErrorSaving");

		return null;
	}

	private StatisticNode lastRoot;

	private void updateViewer(final boolean updateTree) {
		final StatisticNode root=new StatisticNode();
		String modelName=null;
		if (statistics!=null && statistics.length>0 && statistics[0]!=null) {
			addNodesToTree(root);
			modelName=statistics[0].editModel.name;
		}
		lastRoot=root;

		if (updateTree) setData(root,modelName);
	}

	private boolean testClientMovement(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.stationTransition.size()>0) return true;
		}
		return false;
	}

	private boolean testClientPathRecording(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.clientPaths.size()>0) return true;
		}
		return false;
	}

	private boolean testResourcesAvailable(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.resourceCount.size()>0) return true;
			if (statistic.resourceUtilization.size()>0) return true;
		}
		return false;
	}

	private boolean testResourceFailures(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			for (StatisticsTimePerformanceIndicator indicator: (StatisticsTimePerformanceIndicator[])statistic.resourceInDownTime.getAll(StatisticsTimePerformanceIndicator.class)) {
				if (indicator.getTimeMax()>0) return true;
			}
		}
		return false;
	}

	private boolean testTransportersAvailable(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.editModel.transporters.count()>0) return true;
		}
		return false;
	}

	private boolean testAdditionalStatisticsAvailable(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.longRunStatistics.size()>0) return true;
		}
		return false;
	}

	private boolean testAutoCorrelationAvailable(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.clientsAllWaitingTimes.isCorrelationAvailable()) return true;
		}
		return false;
	}

	private boolean testUserStatisticsAvailable(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.userStatistics.size()>0) return true;
		}
		return false;
	}

	private boolean testCounterAvailable(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.counter.size()>0 || statistic.differentialCounter.size()>0) return true;
		}
		return false;
	}

	private boolean testThroughputAvailable(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.throughputStatistics.size()>0) return true;
		}
		return false;
	}

	private boolean testStateStatisticsAvailable(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.stateStatistics.size()>0) return true;
		}
		return false;
	}

	private boolean testMultiClientTypesInterarrival(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.clientsInterarrivalTime.size()>1) return true;
		}
		return false;
	}

	private boolean testMultiClientTypes(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.clientsWaitingTimes.size()>1) return true;
			if (statistic.clientsTransferTimes.size()>1) return true;
			if (statistic.clientsProcessingTimes.size()>1) return true;
			if (statistic.clientsResidenceTimes.size()>1) return true;
		}
		return false;
	}

	private boolean testMultiStations(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.stationsWaitingTimes.size()>1) return true;
			if (statistic.stationsTransferTimes.size()>1) return true;
			if (statistic.stationsProcessingTimes.size()>1) return true;
			if (statistic.stationsResidenceTimes.size()>1) return true;
		}
		return false;
	}

	private boolean testMultiStationsClientTypes(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.stationsWaitingTimesByClientType.size()>1) return true;
			if (statistic.stationsTransferTimesByClientType.size()>1) return true;
			if (statistic.stationsProcessingTimesByClientType.size()>1) return true;
			if (statistic.stationsResidenceTimesByClientType.size()>1) return true;
		}
		return false;
	}

	private boolean testAnalogStatistics(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.analogStatistics.size()>0) return true;
		}
		return false;
	}

	private boolean testClientData(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.clientData.size()>0) return true;
		}
		return false;
	}

	private boolean testCosts(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.clientsCostsWaiting.size()>0) return true;
			if (statistic.clientsCostsTransfer.size()>0) return true;
			if (statistic.clientsCostsProcess.size()>0) return true;
			if (statistic.stationCosts.size()>0) return true;
			for (StatisticsValuePerformanceIndicator indicator: (StatisticsValuePerformanceIndicator[])statistic.resourceTimeCosts.getAll(StatisticsValuePerformanceIndicator.class)) {
				if (indicator.getValue()!=0.0) return true;
			}
			for (StatisticsValuePerformanceIndicator indicator: (StatisticsValuePerformanceIndicator[])statistic.resourceTimeCosts.getAll(StatisticsValuePerformanceIndicator.class)) {
				if (indicator.getValue()!=0.0) return true;
			}
			for (StatisticsValuePerformanceIndicator indicator: (StatisticsValuePerformanceIndicator[])statistic.resourceWorkCosts.getAll(StatisticsValuePerformanceIndicator.class)) {
				if (indicator.getValue()!=0.0) return true;
			}
			for (StatisticsValuePerformanceIndicator indicator: (StatisticsValuePerformanceIndicator[])statistic.resourceIdleCosts.getAll(StatisticsValuePerformanceIndicator.class)) {
				if (indicator.getValue()!=0.0) return true;
			}
		}
		return false;

	}

	private boolean testValueRecording(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.valueRecording.size()>0) return true;
		}
		return false;
	}

	private boolean testWaitingTimes(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.clientsAllWaitingTimes.getMax()>0) return true;
		}
		return false;
	}

	private boolean testTransferTimes(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.clientsAllTransferTimes.getMax()>0) return true;
		}
		return false;
	}

	private boolean testProcessTimes(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (statistic.clientsAllProcessingTimes.getMax()>0) return true;
		}
		return false;
	}

	private boolean testErlangCCompare(final Statistics[] statistics) {
		for (Statistics statistic: statistics) {
			if (AnalyticInfo.canCompare(statistic)) return true;
		}
		return false;
	}

	private void addNodesToTree(final StatisticNode root) {
		List<StatisticViewer> viewer;
		StatisticNode group, sub, sub2;

		final SetupData setup=SetupData.getSetup();

		/* Schnellzugriff */

		if (statistics.length==1) {
			viewer=new ArrayList<>();
			viewer.add(fastAccess=new StatisticViewerFastAccess(
					statistics[0],
					()->Help.topic(StatisticsPanel.this,"MainStatistik"),
					()->Help.topicModal(StatisticsPanel.this,"MainStatistik"),
					()->Help.topic(StatisticsPanel.this,"JS"),
					()->Help.topicModal(StatisticsPanel.this,"JS"),
					()->Help.topic(StatisticsPanel.this,"Java"),
					()->Help.topicModal(StatisticsPanel.this,"Java")
					));
			root.addChild(new StatisticNode(Language.tr("Statistics.FastAccess"),viewer));
		} else {
			fastAccess=null;
		}

		/* Ergebnisübersicht */

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_OVERVIEW,mode->modeClick(mode),(m,s)->fastAccess.addXML(m,s)));
		root.addChild(new StatisticNode(Language.tr("Statistics.ResultsOverview"),viewer));

		if (testErlangCCompare(statistics) && setup.showErlangC) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerErlangCCompare(statistic));
			root.addChild(new StatisticNode(Language.tr("Statistics.ErlangCCompare"),viewer));
		}

		root.addChild(group=new StatisticNode(Language.tr("Statistics.ModelOverview"),!setup.expandAllStatistics));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_MODEL,(m,s)->fastAccess.addXML(m,s)));
		group.addChild(new StatisticNode(Language.tr("Statistics.ModelOverview"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerModelImage(statistic));
		group.addChild(new StatisticNode(Language.tr("Statistics.ModelOverview"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_MODEL_DESCRIPTION,(m,s)->fastAccess.addXML(m,s)));
		group.addChild(new StatisticNode(Language.tr("Statistics.ModelDescription"),viewer));

		/* Ankünfte und Abgänge an den Stationen */

		root.addChild(group=new StatisticNode(Language.tr("Statistics.ArrivalsLeavings"),!setup.expandAllStatistics));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_INTERARRIVAL_STATIONS,(m,s)->fastAccess.addXML(m,s)));
		group.addChild(new StatisticNode(Language.tr("Statistics.ArrivalsAtStations"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_INTERLEAVE_STATIONS,(m,s)->fastAccess.addXML(m,s)));
		group.addChild(new StatisticNode(Language.tr("Statistics.LeavingsAtStations"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_OVERVIEW_STATIONS_ARRIVAL_COUNT));
		group.addChild(new StatisticNode(Language.tr("Statistics.NumberOfArrivals"),viewer));

		/* (Untergruppe) Zwischenankunftszeiten am System */

		group.addChild(sub=new StatisticNode(Language.tr("Statistics.InterArrivalTimesAtTheSystem"),!setup.expandAllStatistics));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_INTERARRIVAL_CLIENTS,(m,s)->fastAccess.addXML(m,s)));
		sub.addChild(new StatisticNode(Language.tr("Statistics.InterArrivalTimes"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_OVERVIEW_CLIENTS_INTERARRIVAL));
		sub.addChild(new StatisticNode(Language.tr("Statistics.InterArrivalTimes"),viewer));

		if (testMultiClientTypesInterarrival(statistics)) {

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_INTERARRIVAL_CLIENTS));
			sub.addChild(new StatisticNode(Language.tr("Statistics.InterArrivalTimes"),viewer));

		}

		sub.addChild(sub2=new StatisticNode(Language.tr("Statistics.Distributions"),!setup.expandAllStatistics));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_CLIENTS_INTERARRIVAL));
		sub2.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfTheInterArrivalTimes"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_INTERARRIVAL_CLIENTS));
		sub2.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfTheInterArrivalTimes"),viewer));

		/* (Untergruppe) Zwischenabgangszeiten am System */

		group.addChild(sub=new StatisticNode(Language.tr("Statistics.InterLeaveTimesAtTheSystem"),!setup.expandAllStatistics));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_INTERLEAVE_CLIENTS,(m,s)->fastAccess.addXML(m,s)));
		sub.addChild(new StatisticNode(Language.tr("Statistics.InterLeaveTimes"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_OVERVIEW_CLIENTS_INTERLEAVE));
		sub.addChild(new StatisticNode(Language.tr("Statistics.InterLeaveTimes"),viewer));

		if (testMultiClientTypesInterarrival(statistics)) {

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_INTERLEAVE_CLIENTS));
			sub.addChild(new StatisticNode(Language.tr("Statistics.InterLeaveTimes"),viewer));

		}

		sub.addChild(sub2=new StatisticNode(Language.tr("Statistics.Distributions"),!setup.expandAllStatistics));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_CLIENTS_INTERLEAVE));
		sub2.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfTheInterLeaveTimes"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_INTERLEAVE_CLIENTS));
		sub2.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfTheInterLeaveTimes"),viewer));

		/* (Untergruppe) Zwischenankunftszeiten an den Stationen */

		group.addChild(sub=new StatisticNode(Language.tr("Statistics.InterArrivalTimesAtTheStations"),!setup.expandAllStatistics));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_OVERVIEW_STATIONS_INTERARRIVAL));
		sub.addChild(new StatisticNode(Language.tr("Statistics.InterArrivalTimes"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_INTERARRIVAL_STATION));
		sub.addChild(new StatisticNode(Language.tr("Statistics.InterArrivalTimes"),viewer));

		if (testMultiStationsClientTypes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_OVERVIEW_STATIONS_INTERARRIVAL_CLIENTS));
			sub.addChild(new StatisticNode(Language.tr("Statistics.InterArrivalTimesAtTheStationsByClientTypes.Short"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_INTERARRIVAL_STATION_CLIENTS));
			sub.addChild(new StatisticNode(Language.tr("Statistics.InterArrivalTimesAtTheStationsByClientTypes.Short"),viewer));
		}

		sub.addChild(sub2=new StatisticNode(Language.tr("Statistics.Distributions"),!setup.expandAllStatistics));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_STATIONS_INTERARRIVAL));
		sub2.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfTheInterArrivalTimes"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_INTERARRIVAL_STATION));
		sub2.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfTheInterArrivalTimes"),viewer));

		if (testMultiStationsClientTypes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_STATIONS_INTERARRIVAL_CLIENTS));
			sub2.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfTheInterArrivalTimesByClientType"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_INTERARRIVAL_STATION_CLIENTS));
			sub2.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfTheInterArrivalTimesByClientType"),viewer));
		}

		/* (Untergruppe) Zwischenabgangszeiten an den Stationen */

		group.addChild(sub=new StatisticNode(Language.tr("Statistics.InterLeaveTimesAtTheStations"),!setup.expandAllStatistics));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_OVERVIEW_STATIONS_INTERLEAVE));
		sub.addChild(new StatisticNode(Language.tr("Statistics.InterLeaveTimes"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_INTERLEAVE_STATION));
		sub.addChild(new StatisticNode(Language.tr("Statistics.InterLeaveTimes"),viewer));

		if (testMultiStationsClientTypes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_OVERVIEW_STATIONS_INTERLEAVE_CLIENTS));
			sub.addChild(new StatisticNode(Language.tr("Statistics.InterLeaveTimesAtTheStationsByClientTypes.Short"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_INTERLEAVE_STATION_CLIENTS));
			sub.addChild(new StatisticNode(Language.tr("Statistics.InterLeaveTimesAtTheStationsByClientTypes.Short"),viewer));
		}

		sub.addChild(sub2=new StatisticNode(Language.tr("Statistics.Distributions"),!setup.expandAllStatistics));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_STATIONS_INTERLEAVE));
		sub2.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfTheInterLeaveTimes"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_INTERLEAVE_STATION));
		sub2.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfTheInterLeaveTimes"),viewer));

		if (testMultiStationsClientTypes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_STATIONS_INTERLEAVE_CLIENTS));
			sub2.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfTheInterLeaveTimesByClientType"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_INTERLEAVE_STATION_CLIENTS));
			sub2.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfTheInterLeaveTimesByClientType"),viewer));
		}

		/* Kundenbewegungen */

		if (testClientMovement(statistics) || testClientPathRecording(statistics)) {

			root.addChild(group=new StatisticNode(Language.tr("Statistics.ClientMovement"),!setup.expandAllStatistics));

			if (testClientMovement(statistics)) {
				viewer=new ArrayList<>();
				for(Statistics statistic : statistics) viewer.add(new StatisticViewerMovementText(statistic,StatisticViewerMovementText.Mode.STATION_TRANSITION));
				group.addChild(new StatisticNode(Language.tr("Statistics.ClientMovement"),viewer));

				viewer=new ArrayList<>();
				for(Statistics statistic : statistics) viewer.add(new StatisticViewerMovementTable(statistic,StatisticViewerMovementTable.Mode.STATION_TRANSITION));
				group.addChild(new StatisticNode(Language.tr("Statistics.ClientMovement"),viewer));
			}

			if (testClientPathRecording(statistics)) {
				viewer=new ArrayList<>();
				for(Statistics statistic : statistics) viewer.add(new StatisticViewerMovementText(statistic,StatisticViewerMovementText.Mode.CLIENT_PATHS));
				group.addChild(new StatisticNode(Language.tr("Statistics.ClientPathRecording"),viewer));

				viewer=new ArrayList<>();
				for(Statistics statistic : statistics) viewer.add(new StatisticViewerMovementTable(statistic,StatisticViewerMovementTable.Mode.CLIENT_PATHS));
				group.addChild(new StatisticNode(Language.tr("Statistics.ClientPathRecording"),viewer));

				viewer=new ArrayList<>();
				for(Statistics statistic : statistics) viewer.add(new StatisticViewerMovementText(statistic,StatisticViewerMovementText.Mode.CLIENT_PATH_LENGTHS));
				group.addChild(new StatisticNode(Language.tr("Statistics.ClientPathLengths"),viewer));

				viewer=new ArrayList<>();
				for(Statistics statistic : statistics) viewer.add(new StatisticViewerMovementTable(statistic,StatisticViewerMovementTable.Mode.CLIENT_PATH_LENGTHS));
				group.addChild(new StatisticNode(Language.tr("Statistics.ClientPathLengths"),viewer));

				viewer=new ArrayList<>();
				for(Statistics statistic : statistics) viewer.add(new StatisticViewerMovementTable(statistic,StatisticViewerMovementTable.Mode.CLIENT_PATH_LENGTHS_DISTRIBUTION));
				group.addChild(new StatisticNode(Language.tr("Statistics.ClientPathLengthsDistribution"),viewer));
			}

		}

		/* Kunden an den Station */

		root.addChild(group=new StatisticNode(Language.tr("Statistics.ClientsAtStations")));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_CLIENTS_COUNT,(m,s)->fastAccess.addXML(m,s)));
		group.addChild(new StatisticNode(Language.tr("Statistics.NumberOfClientsAtStations"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_OVERVIEW_NUMBER));
		group.addChild(new StatisticNode(Language.tr("Statistics.NumberOfClientsAtStations")+" ("+Language.tr("Statistics.total")+")",viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_OVERVIEW_QUEUE));
		group.addChild(new StatisticNode(Language.tr("Statistics.NumberOfClientsAtStationQueues.Short"),viewer));

		/* (Untergruppe) Verteilungen */

		group.addChild(sub=new StatisticNode(Language.tr("Statistics.Distributions"),!setup.expandAllStatistics));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_NUMBER_STATION));
		sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfNumberOfClientsAtStations")+" ("+Language.tr("Statistics.total")+")",viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_NUMBER_CLIENT));
		sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfNumberOfClientsByType"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_QUEUE));
		sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfNumberOfClientsAtStationQueues"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_NUMBER_STATION));
		sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfNumberOfClientsAtStations")+" ("+Language.tr("Statistics.total")+")",viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_NUMBER_CLIENT));
		sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfNumberOfClientsByType")+" ("+Language.tr("Statistics.total")+")",viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_QUEUE));
		sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfNumberOfClientsAtStationQueues"),viewer));

		/* Warte- und Bedienzeiten der Kunden */

		root.addChild(group=new StatisticNode(Language.tr("Statistics.WaitingTransferProcessTimesOfClients.Short")));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_WAITINGPROCESSING_CLIENTS,(m,s)->fastAccess.addXML(m,s)));
		group.addChild(new StatisticNode(Language.tr("Statistics.WaitingTransferProcessTimesOfClients"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_OVERVIEW_CLIENTS_WAITINGPROCESSING));
		group.addChild(new StatisticNode(Language.tr("Statistics.WaitingTransferProcessTimesOfClients"),viewer));

		if (testMultiClientTypes(statistics)) {

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_WAITING_CLIENTS));
			group.addChild(new StatisticNode(Language.tr("Statistics.ClientsWaitingTimes"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_TRANSFER_CLIENTS));
			group.addChild(new StatisticNode(Language.tr("Statistics.ClientsTransferTimes"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_PROCESSING_CLIENTS));
			group.addChild(new StatisticNode(Language.tr("Statistics.ClientsProcessTimes"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_RESIDENCE_CLIENTS));
			group.addChild(new StatisticNode(Language.tr("Statistics.ClientsResidenceTimes"),viewer));
		}

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerPartsPieChart(statistic,StatisticViewerPartsPieChart.Mode.MODE_WAITINGPROCESSING));
		group.addChild(new StatisticNode(Language.tr("Statistics.RatioOfWaitingToProcessTime"),viewer));

		/* (Untergruppe) Verteilungen */

		group.addChild(sub=new StatisticNode(Language.tr("Statistics.Distributions"),!setup.expandAllStatistics));

		if (testWaitingTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_CLIENTS_WAITING));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfClientsWaitingTimes"),viewer));
		}

		if (testTransferTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_CLIENTS_TRANSFER));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfClientsTransferTimes"),viewer));
		}

		if (testProcessTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_CLIENTS_PROCESSING));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfClientsProcessTimes"),viewer));
		}

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_CLIENTS_RESIDENCE));
		sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfClientsResidenceTimes"),viewer));

		if (testWaitingTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_WAITING_CLIENTS));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfClientsWaitingTimes"),viewer));
		}

		if (testTransferTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_TRANSFER_CLIENTS));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfClientsTransferTimes"),viewer));
		}

		if (testProcessTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_PROCESSING_CLIENTS));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfClientsProcessTimes"),viewer));
		}

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_RESIDENCE_CLIENTS));
		sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfClientsResidenceTimes"),viewer));

		/* Kundendatenfelder */

		if (testClientData(statistics)) {

			root.addChild(group=new StatisticNode(Language.tr("Statistics.ClientData")));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_CLIENT_DATA,(m,s)->fastAccess.addXML(m,s)));
			group.addChild(new StatisticNode(Language.tr("Statistics.ClientData"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_CLIENT_DATA));
			group.addChild(new StatisticNode(Language.tr("Statistics.ClientData"),viewer));

			/* (Untergruppe) Verteilungen */

			group.addChild(sub=new StatisticNode(Language.tr("Statistics.Distributions"),!setup.expandAllStatistics));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_CLIENT_DATA_DISTRIBUTION));
			sub.addChild(new StatisticNode(Language.tr("Statistics.ClientData.Distribution"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_CLIENT_DATA_DISTRIBUTION));
			sub.addChild(new StatisticNode(Language.tr("Statistics.ClientData.Distribution"),viewer));
		}

		/* Warte- und Bedienzeiten an den Stationen */

		root.addChild(group=new StatisticNode(Language.tr("Statistics.WaitingTransferProcessTimesAtStations.Short")));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_WAITINGPROCESSING_STATIONS,(m,s)->fastAccess.addXML(m,s)));
		group.addChild(new StatisticNode(Language.tr("Statistics.WaitingTransferProcessTimesAtStations"),viewer));

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_OVERVIEW_STATIONSMODE_OVERVIEW_CLIENTS));
		group.addChild(new StatisticNode(Language.tr("Statistics.WaitingTransferProcessTimesAtStations"),viewer));

		if (testMultiStations(statistics)) {

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_WAITING_STATION));
			group.addChild(new StatisticNode(Language.tr("Statistics.StationsWaitingTimes"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_TRANSFER_STATION));
			group.addChild(new StatisticNode(Language.tr("Statistics.StationsTransferTimes"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_PROCESSING_STATION));
			group.addChild(new StatisticNode(Language.tr("Statistics.StationsProcessTimes"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_RESIDENCE_STATION));
			group.addChild(new StatisticNode(Language.tr("Statistics.StationsResidenceTimes"),viewer));

		}

		if (testMultiStationsClientTypes(statistics)) {

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_WAITINGPROCESSING_STATIONS_CLIENTS,(m,s)->fastAccess.addXML(m,s)));
			group.addChild(new StatisticNode(Language.tr("Statistics.WaitingTransferProcessTimesAtStationsClients.Short"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_OVERVIEW_STATIONSCLIENTMODE_OVERVIEW_CLIENTS));
			group.addChild(new StatisticNode(Language.tr("Statistics.WaitingTransferProcessTimesAtStationsClients.Short"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_WAITING_STATION_CLIENT));
			group.addChild(new StatisticNode(Language.tr("Statistics.StationsClientsWaitingTimes.Short"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_TRANSFER_STATION_CLIENT));
			group.addChild(new StatisticNode(Language.tr("Statistics.StationsClientsTransferTimes.Short"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_PROCESSING_STATION_CLIENT));
			group.addChild(new StatisticNode(Language.tr("Statistics.StationsClientsProcessTimes.Short"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_RESIDENCE_STATION_CLIENT));
			group.addChild(new StatisticNode(Language.tr("Statistics.StationsClientsResidenceTimes.Short"),viewer));
		}

		/* (Untergruppe) Verteilungen */

		group.addChild(sub=new StatisticNode(Language.tr("Statistics.Distributions"),!setup.expandAllStatistics));

		if (testWaitingTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_STATIONS_WAITING));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsWaitingTimes"),viewer));
		}

		if (testTransferTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_STATIONS_TRANSFER));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsTransferTimes"),viewer));
		}

		if (testProcessTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_STATIONS_PROCESSING));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsProcessTimes"),viewer));
		}

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_STATIONS_RESIDENCE));
		sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsResidenceTimes"),viewer));

		if (testWaitingTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_WAITING_STATION));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsWaitingTimes"),viewer));
		}

		if (testTransferTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_TRANSFER_STATION));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsTransferTimes"),viewer));
		}

		if (testProcessTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_PROCESSING_STATION));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsProcessTimes"),viewer));
		}

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_RESIDENCE_STATION));
		sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsResidenceTimes"),viewer));

		if (testWaitingTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_STATIONSCLIENTS_WAITING));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsClientsWaitingTimes"),viewer));
		}

		if (testTransferTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_STATIONSCLIENTS_TRANSFER));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsClientsTransferTimes"),viewer));
		}

		if (testProcessTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_STATIONSCLIENTS_PROCESSING));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsClientsProcessTimes"),viewer));
		}

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DISTRIBUTION_STATIONSCLIENTS_RESIDENCE));
		sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsClientsResidenceTimes"),viewer));

		if (testWaitingTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_WAITING_STATION_CLIENT));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsClientsWaitingTimes"),viewer));
		}

		if (testTransferTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_TRANSFER_STATION_CLIENT));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsClientsTransferTimes"),viewer));
		}

		if (testProcessTimes(statistics)) {
			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_PROCESSING_STATION_CLIENT));
			sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsClientsProcessTimes"),viewer));
		}

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_RESIDENCE_STATION_CLIENT));
		sub.addChild(new StatisticNode(Language.tr("Statistics.DistributionOfStationsClientsResidenceTimes"),viewer));

		/* Ressourcenauslastung */

		if (testResourcesAvailable(statistics)) {

			root.addChild(group=new StatisticNode(Language.tr("Statistics.Utilization")));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_UTILIZATION,(m,s)->fastAccess.addXML(m,s)));
			group.addChild(new StatisticNode(Language.tr("Statistics.Utilization"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_UTILIZATION));
			group.addChild(new StatisticNode(Language.tr("Statistics.Utilization"),viewer));

			if (testResourceFailures(statistics)) {

				viewer=new ArrayList<>();
				for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_DOWNTIMES));
				group.addChild(new StatisticNode(Language.tr("Statistics.FailureTime"),viewer));

			}

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_RESOURCE_UTILIZATION));
			group.addChild(new StatisticNode(Language.tr("Statistics.UtilizationAndFailures"),viewer));

		}

		/* Transporterauslastung */

		if (testTransportersAvailable(statistics)) {

			root.addChild(group=new StatisticNode(Language.tr("Statistics.TransporterUtilization")));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_TRANSPORTER_UTILIZATION,(m,s)->fastAccess.addXML(m,s)));
			group.addChild(new StatisticNode(Language.tr("Statistics.TransporterUtilization"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_TRANSPORTER_UTILIZATION));
			group.addChild(new StatisticNode(Language.tr("Statistics.TransporterUtilization"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_TRANSPORTER_DOWNTIMES));
			group.addChild(new StatisticNode(Language.tr("Statistics.FailureTime"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeBarChart(statistic,StatisticViewerTimeBarChart.Mode.MODE_TRANSPORTER_UTILIZATION));
			group.addChild(new StatisticNode(Language.tr("Statistics.TransporterUtilizationAndFailures"),viewer));

		}

		/* Laufzeit-Statistik */

		if (testAdditionalStatisticsAvailable(statistics)) {

			root.addChild(group=new StatisticNode(Language.tr("Statistics.AdditionalStatistics")));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerAdditionalTable(statistic));
			group.addChild(new StatisticNode(Language.tr("Statistics.AdditionalStatistics"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_ADDITIONAL_STATISTICS));
			group.addChild(new StatisticNode(Language.tr("Statistics.AdditionalStatistics"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerInteractiveBarChart(statistic));
			group.addChild(new StatisticNode(Language.tr("Statistics.AdditionalStatistics.Interactive"),viewer));

		}

		/* Autokorrelation */

		if (testAutoCorrelationAvailable(statistics)) {

			root.addChild(group=new StatisticNode(Language.tr("Statistics.Autokorrelation")));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_AUTOCORRELATION,(m,s)->fastAccess.addXML(m,s)));
			group.addChild(new StatisticNode(Language.tr("Statistics.Autokorrelation"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerAutocorrelationTable(statistic,StatisticViewerAutocorrelationTable.Mode.MODE_DEFAULT));
			group.addChild(new StatisticNode(Language.tr("Statistics.Autokorrelation"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerAutocorrelationTable(statistic,StatisticViewerAutocorrelationTable.Mode.MODE_DETAILS));
			group.addChild(new StatisticNode(Language.tr("Statistics.Autokorrelation.Details"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerAutocorrelationLineChart(statistic,StatisticViewerAutocorrelationLineChart.Mode.MODE_ALL_WAITING));
			group.addChild(new StatisticNode(Language.tr("Statistics.Autokorrelation.ModeAll"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerAutocorrelationLineChart(statistic,StatisticViewerAutocorrelationLineChart.Mode.MODE_BY_CLIENTTYPE_WAITING));
			group.addChild(new StatisticNode(Language.tr("Statistics.Autokorrelation.ModeByClients"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerAutocorrelationLineChart(statistic,StatisticViewerAutocorrelationLineChart.Mode.MODE_BY_STATION_WAITING));
			group.addChild(new StatisticNode(Language.tr("Statistics.Autokorrelation.ModeByStations"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerAutocorrelationLineChart(statistic,StatisticViewerAutocorrelationLineChart.Mode.MODE_BY_STATION_CLIENT_WAITING));
			group.addChild(new StatisticNode(Language.tr("Statistics.Autokorrelation.ModeByStationsAndClientTypes"),viewer));
		}

		/* Nutzerstatistik */

		if (testUserStatisticsAvailable(statistics)) {

			root.addChild(group=new StatisticNode(Language.tr("Statistics.UserStatistics")));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_USER_STATISTICS,(m,s)->fastAccess.addXML(m,s)));
			group.addChild(new StatisticNode(Language.tr("Statistics.UserStatistics"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerUserStatisticTable(statistic,StatisticViewerUserStatisticTable.Mode.MODE_DEFAULT));
			group.addChild(new StatisticNode(Language.tr("Statistics.UserStatistics"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerUserStatisticTable(statistic,StatisticViewerUserStatisticTable.Mode.MODE_DETAILS));
			group.addChild(new StatisticNode(Language.tr("Statistics.UserStatistics.Details"),viewer));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerUserStatisticLineChart(statistic));
			group.addChild(new StatisticNode(Language.tr("Statistics.UserStatistics"),viewer));

		}

		/* Werteaufzeichnung */

		if (testValueRecording(statistics)) {

			root.addChild(group=new StatisticNode(Language.tr("Statistics.ValueRecording")));

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerTimeTable(statistic,StatisticViewerTimeTable.Mode.MODE_VALUE_RECORDING));
			group.addChild(new StatisticNode(Language.tr("Statistics.ValueRecording"),viewer));

			final List<String> xplot=new ArrayList<>();
			final List<String> xyplot=new ArrayList<>();

			for (Statistics statistic : statistics) {
				final String[] names=statistic.valueRecording.getNames();
				for (int i=0;i<names.length;i++) {
					if (names[i].endsWith("-1") && (i==names.length-1 || !names[i+1].endsWith("-2"))) xplot.add(names[i].substring(0,names[i].length()-2));
					if (names[i].endsWith("-2")) xyplot.add(names[i].substring(0,names[i].length()-2));
				}
			}

			if (xplot.size()>0) {
				group.addChild(sub=new StatisticNode(Language.tr("Statistics.ValueRecording.Course")));
				for (String name: xplot) {
					viewer=new ArrayList<>();
					for(Statistics statistic : statistics) viewer.add(new StatisticViewerDistributionTimeLineChart(statistic,StatisticViewerDistributionTimeLineChart.Mode.MODE_VALUE_RECORDING,name));
					sub.addChild(new StatisticNode(name,viewer));
				}
			}

			if (xyplot.size()>0) {
				group.addChild(sub=new StatisticNode(Language.tr("Statistics.ValueRecording.XYPlot")));
				for (String name: xyplot) {
					viewer=new ArrayList<>();
					for(Statistics statistic : statistics) viewer.add(new StatisticViewerXYPlot(statistic,name));
					sub.addChild(new StatisticNode(name,viewer));
				}
			}
		}

		/* Zähler */

		if (testCounterAvailable(statistics)) {

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_COUNTER,(m,s)->fastAccess.addXML(m,s)));
			root.addChild(new StatisticNode(Language.tr("Statistics.Counter"),viewer));

		}

		/* Durchsatz */

		if (testThroughputAvailable(statistics)) {

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_THROUGHPUT,(m,s)->fastAccess.addXML(m,s)));
			root.addChild(new StatisticNode(Language.tr("Statistics.Throughput"),viewer));

		}

		/* Zustandsstatistik */

		if (testStateStatisticsAvailable(statistics)) {

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_STATE_STATISTICS,(m,s)->fastAccess.addXML(m,s)));
			root.addChild(new StatisticNode(Language.tr("Statistics.StateStatistics"),viewer));

		}

		/* Analoge Werte */

		if (testAnalogStatistics(statistics)) {

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_ANALOG_STATISTICS,(m,s)->fastAccess.addXML(m,s)));
			root.addChild(new StatisticNode(Language.tr("Statistics.AnalogStatistics"),viewer));
		}

		/* Kosten */

		if (testCosts(statistics)) {

			viewer=new ArrayList<>();
			for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_COSTS,(m,s)->fastAccess.addXML(m,s)));
			root.addChild(new StatisticNode(Language.tr("Statistics.Costs"),viewer));

		}

		/* Systemdaten */

		viewer=new ArrayList<>();
		for(Statistics statistic : statistics) viewer.add(new StatisticViewerOverviewText(statistic,StatisticViewerOverviewText.Mode.MODE_SYSTEM_INFO,(m,s)->fastAccess.addXML(m,s)));
		root.addChild(new StatisticNode(Language.tr("Statistics.SystemData"),viewer));
	}

	private void modeClick(final StatisticViewerOverviewText.Mode mode) {
		selectNode(node->{
			if (node.viewer.length<1) return false;
			if (!(node.viewer[0] instanceof StatisticViewerOverviewText)) return false;
			return ((StatisticViewerOverviewText)node.viewer[0]).getMode()==mode;
		});
	}

	/**
	 * Führt die Schnellzugriff-Skripte neu aus (nach einem Wechsel der JS-Engine aufzurufen).
	 */
	public void updateFastAccess() {
		if (fastAccess==null) return;
		fastAccess.updateResults();
	}

	@Override
	protected int getImageSize() {
		return SetupData.getSetup().imageSize;
	}

	@Override
	protected void setImageSize(int newSize) {
		final SetupData setup=SetupData.getSetup();
		setup.imageSize=newSize;
		setup.saveSetupWithWarning(this);
	}

	@Override
	public StatisticNode getStatisticNodeRoot() {
		if (lastRoot!=null) return lastRoot;
		return super.getStatisticNodeRoot();
	}
}