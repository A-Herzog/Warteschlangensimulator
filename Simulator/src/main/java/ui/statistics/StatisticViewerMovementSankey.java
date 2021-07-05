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
package ui.statistics;

import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.flow.FlowPlot;
import org.jfree.data.flow.DefaultFlowDataset;
import org.jfree.data.flow.NodeKey;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.statistics.Statistics;
import systemtools.statistics.StatisticViewerJFreeChart;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Dieser Viewer stellt die Übergänge der Kunden zwischen den Stationen
 * als Sankey-Diagramm an.
 * @author Alexander Herzog
 * @see StatisticViewerJFreeChart
 * @see CreateSankey
 */
public class StatisticViewerMovementSankey extends StatisticViewerJFreeChart {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Welche Informationen sollen angezeigt werden? */
	private final CreateSankey.Mode mode;
	/** Tabelle mit den Kundenbewegungen gemäß {@link #mode} */
	private final Table table;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	public StatisticViewerMovementSankey(final Statistics statistics) {
		this.statistics=statistics;
		mode=(statistics.clientPaths.size()>0)?CreateSankey.Mode.CLIENT_PATHS:CreateSankey.Mode.STATION_TRANSITION;

		switch (mode) {
		case STATION_TRANSITION:
			table=StatisticViewerMovementTable.getClientMovementTable(statistics);
			break;
		case CLIENT_PATHS:
			table=StatisticViewerMovementTable.getClientPathsTable(statistics,false);
			break;
		default:
			table=null;
			break;
		}
	}

	@Override
	public ViewerImageType getImageType() {
		return ViewerImageType.IMAGE_TYPE_SANKEY;
	}

	@Override
	public boolean getCanDo(CanDoAction canDoType) {
		if (canDoType==CanDoAction.CAN_DO_UNZOOM) return false;
		return super.getCanDo(canDoType);
	}

	@Override
	public void unZoom() {
	}

	/**
	 * Liefert ein Stationselement basiernd auf dem Namen.
	 * @param stationName	Name der Station (inkl. id)
	 * @return	Stationselement oder <code>null</code>, wenn keine passende Station gefunden wurde
	 */
	private ModelElementBox getStation(final String stationName) {
		if (stationName==null || stationName.length()<3) return null;
		final int index=stationName.lastIndexOf("(id=");
		if (index<0) return null;
		final String idString=stationName.substring(index+4,stationName.length()-1);
		final Integer id=NumberTools.getNotNegativeInteger(idString);
		if (id==null) return null;
		final ModelElement element=statistics.editModel.surface.getByIdIncludingSubModels(id);
		if (!(element instanceof ModelElementBox)) return null;
		return (ModelElementBox)element;
	}

	/**
	 * Liefert die Farbe für eine Station.
	 * @param node	Station für die die Farbe bestimmt werden soll
	 * @return	Farbe für die Station oder Fallback-Wert
	 */
	private Color getColor(final NodeKey<String> node) {
		if (node==null) return Color.GRAY;
		final ModelElementBox station=getStation(node.getNode());
		if (station==null) return Color.GRAY;
		return station.getDrawBackgroundColor();
	}

	/**
	 * Prüft, ob eine Station über einlaufende Kanten verfügt (wobei die Kante vom Pseudo-Startelement aus nicht zählt).
	 * @param connections	Zuordnung aller Verbindungen
	 * @param stationNameStart	Name für die virtuelle Startstation
	 * @param stationNameEnd		Name für die virtuelle Endstation
	 * @param stationName	Name der zu prüfenden Station
	 * @return	Einlaufende Kanten vorhanden?
	 */
	private boolean hasConnectionIn(final Map<String,Map<String,Long>> connections, final String stationNameStart, final String stationNameEnd, final String stationName) {
		final Map<String,Long> map=connections.get(stationNameStart);
		if (map==null) return true;
		return map.get(stationName)==null;
	}

	/**
	 * Prüft, ob eine Station über auslaufende Kanten verfügt (wobei die Kante zum Pseudo-Endelement nicht zählt).
	 * @param connections	Zuordnung aller Verbindungen
	 * @param stationNameStart	Name für die virtuelle Startstation
	 * @param stationNameEnd		Name für die virtuelle Endstation
	 * @param stationName	Name der zu prüfenden Station
	 * @return	Auslaufende Kanten vorhanden?
	 */
	private boolean hasConnectionOut(final Map<String,Map<String,Long>> connections, final String stationNameStart, final String stationNameEnd, final String stationName) {
		final Map<String,Long> map=connections.get(stationName);
		if (map==null) return false;
		for (Map.Entry<String,Long> entry: map.entrySet()) {
			if (entry.getKey().equals(stationNameEnd)) continue;
			return true;
		}
		return false;
	}

	@Override
	protected void firstChartRequest() {
		if (table==null) return;
		final Map<String,Map<String,Long>> connections=CreateSankey.getConnections(table,mode);

		final String stationNameStart=Language.tr("Simulation.ClientMovement.Start").toUpperCase();
		final String stationNameEnd=Language.tr("Simulation.ClientMovement.End").toUpperCase();

		final List<Flow> flows=new ArrayList<>();

		for (Map.Entry<String,Map<String,Long>> entry1: connections.entrySet()) for (Map.Entry<String,Long> entry2: entry1.getValue().entrySet()) {
			final String from=entry1.getKey();
			final String to=entry2.getKey();
			final long value=entry2.getValue();

			if (from.equals(stationNameStart) || to.equals(stationNameEnd)) continue;

			if (!hasConnectionIn(connections,stationNameStart,stationNameEnd,from)) {flows.add(new Flow(0,from,to,value)); continue;}
			if (!hasConnectionOut(connections,stationNameStart,stationNameEnd,to)) {flows.add(new Flow(2,from,to,value)); continue;}

			flows.add(new Flow(1,from,to,value));
		}

		final DefaultFlowDataset<String> dataset=new DefaultFlowDataset<>();

		int stage=0;
		for (int i=0;i<3;i++) {
			final int filterStage=i;
			final Flow[] flowsStage=flows.stream().filter(flow->flow.stage==filterStage).toArray(Flow[]::new);
			for (Flow flow: flowsStage) dataset.setFlow(stage,flow.from,flow.to,flow.value);
			if (flowsStage.length>0) stage++;
		}

		final FlowPlot plot=new FlowPlot(dataset);
		for(NodeKey<String> node: dataset.getAllNodes()) {
			plot.setNodeFillColor(node,getColor(node));
		}

		plot.setToolTipGenerator((data,key)->{
			@SuppressWarnings("unchecked")
			final Number value = data.getFlow(key.getStage(),key.getSource(),key.getDestination());
			return key.getSource().toString()+" -> "+key.getDestination().toString()+" = "+NumberTools.formatLong(value.longValue());
		});

		final JFreeChart chart=new JFreeChart(Language.tr("Statistics.ClientMovement"),plot);

		initChart(chart);

		/* Infotext  */
		addDescription("ClientMovement");
	}

	/**
	 * Speichert die Daten zu einem einzelnen Fluss
	 */
	private static class Flow {
		/** Stufe */
		public final int stage;
		/** Quelle */
		public final String from;
		/** Ziel */
		public final String to;
		/** Stärke des Flusses */
		public final long value;

		/**
		 * Konstruktor der Klasse
		 * @param stage	Stufe
		 * @param from	Quelle
		 * @param to	Ziel
		 * @param value	Stärke des Flusses
		 */
		public Flow(final int stage, final String from, final String to, final long value) {
			this.stage=stage;
			this.from=from;
			this.to=to;
			this.value=value;
		}	}

	/**
	 * Zeigt im Fußbereich der Hilfeseite eine "Erklärung einblenden"-Schaltfläche, die,
	 * wenn sie angeklickt wird, eine html-Hilfeseite anzeigt.
	 * @param topic	Hilfe-Thema (wird als Datei in den "description_*"-Ordern gesucht)
	 */
	private void addDescription(final String topic) {
		final URL url=StatisticViewerMovementSankey.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	@Override
	public JButton[] getAdditionalButton() {
		final List<JButton> buttons=new ArrayList<>();

		JButton button;

		buttons.add(button=new JButton(Language.tr("Statistics.ClientMovement.Sankey")));
		button.setToolTipText(Language.tr("Statistics.ClientMovement.Sankey.Tooltip"));
		button.setIcon(Images.STATISTICS_DIAGRAM_SANKEY.getIcon());
		button.addActionListener(e->{
			switch (mode) {
			case STATION_TRANSITION:
				new CreateSankey(getViewer(false),StatisticViewerMovementTable.getClientMovementTable(statistics),CreateSankey.Mode.STATION_TRANSITION);
				break;
			case CLIENT_PATHS:
				new CreateSankey(getViewer(false),StatisticViewerMovementTable.getClientPathsTable(statistics,false),CreateSankey.Mode.CLIENT_PATHS);
				break;
			default:
				break;
			}
		});

		return buttons.toArray(new JButton[0]);
	}
}
