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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.swing.CommonVariables;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import systemtools.statistics.StatisticsBasePanel;
import ui.help.Help;
import ui.images.Images;

/**
 * Erstellt auf Basis einer Kundenbewegungstabelle ein Sankey-Diagramm.
 * @author Alexander Herzog
 * @see StatisticViewerMovementTable#getClientMovementTable(simulator.statistics.Statistics)
 */
public final class CreateSankey extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3545073435893826580L;

	/**
	 * Auf welcher Basis soll das Sankey-Diagramm erzeugt werden?
	 * @author Alexander Herzog
	 * @see CreateSankey#CreateSankey(Component, Table, Mode)
	 */
	public enum Mode {
		/** Übergänge zwischen Stationen */
		STATION_TRANSITION,
		/** Pfade der Kunden */
		CLIENT_PATHS,
	}

	/**
	 * Auf welcher Basis soll das Sankey-Diagramm erzeugt werden?
	 * @see Mode
	 */
	private final Mode mode;

	/**
	 * Tabelle mit den Verknüpfungsdaten
	 */
	private final Table table;

	/**
	 * Vorgabename für die virtuelle Startstation
	 * @see #nameStart
	 */
	private final String stationNameStart;

	/**
	 * Vorgabename für die virtuelle Endstation
	 * @see #nameEnd
	 */
	private final String stationNameEnd;

	/** Ausgabeoption: Daten in temporäre Datei schreiben und diese direkt öffnen */
	private final JRadioButton outputDirect;
	/** Ausgabeoption: Daten in Datei speichern */
	private final JRadioButton outputFile;
	/** Eingabefeld für die Ausgabedatei */
	private final JTextField editFile;
	/** Liste der Stationen */
	private final List<String> stations;
	/** Zusätzliche virtuelle Startstation verwenden? */
	private final JCheckBox useStart;
	/** Name der virtuellen Startstation */
	private final JTextField nameStart;
	/** Zusätzliche virtuelle Endstation verwenden? */
	private final JCheckBox useEnd;
	/** Name der virtuellen Endstation */
	private final JTextField nameEnd;
	/** Weiterleitungen zur selben Station abbilden? */
	private final JCheckBox includeJSLibrary;
	/** Datenmodell für die Liste der Stationen zur Auswahl der auszugebenden Stationen */
	private final DefaultListModel<JCheckBox> model;

	/**
	 * Konstruktor der Klasse<br>
	 * Der Dialog wird bereits durch den Konstruktor sichtbar gemacht.
	 * @param owner	Übergeordnetes Element
	 * @param table	Tabelle mit den Verknüpfungsdaten
	 * @param mode	Auf welcher Basis soll das Sankey-Diagramm erzeugt werden?
	 * @see Mode
	 */
	public CreateSankey(final Component owner, final Table table, final Mode mode) {
		super(owner,Language.tr("Statistics.ClientMovement.Title"));

		this.table=table;
		this.mode=mode;
		stationNameStart=Language.tr("Simulation.ClientMovement.Start").toUpperCase();
		stationNameEnd=Language.tr("Simulation.ClientMovement.End").toUpperCase();
		this.stations=new ArrayList<>(getStationsList(table,mode,stationNameStart,stationNameEnd));

		addUserButton(Language.tr("Simulation.ClientMovement.SelectAll"),Images.EDIT_ADD.getIcon());
		addUserButton(Language.tr("Simulation.ClientMovement.SelectNone"),Images.EDIT_DELETE.getIcon());
		final JPanel content=createGUI(()->Help.topicModal(this,"Sankey"));
		content.setLayout(new BorderLayout());

		JPanel setup, line;

		content.add(setup=new JPanel(),BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		/* Art der Ausgabe */

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(outputDirect=new JRadioButton(Language.tr("Simulation.ClientMovement.OutputFile.OutputDirect"),true));

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(outputFile=new JRadioButton(Language.tr("Simulation.ClientMovement.OutputFile.OutputFile")));

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(outputDirect);
		buttonGroup.add(outputFile);

		/* Ausgabedatei */

		final JPanel p=new JPanel(new BorderLayout());
		setup.add(p);
		JPanel p2=new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(p2,BorderLayout.WEST);
		final JLabel label=new JLabel(Language.tr("Simulation.ClientMovement.OutputFile")+":");
		p2.add(label);
		p.add(editFile=new JTextField(),BorderLayout.CENTER);
		label.setLabelFor(editFile);
		editFile.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {outputFile.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {outputFile.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {outputFile.setSelected(true);}
		});
		final JButton button=new JButton();
		button.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		button.setToolTipText(Language.tr("Simulation.ClientMovement.OutputFile.Hint"));
		button.addActionListener(e->selectFile());
		p.add(button,BorderLayout.EAST);

		/* Liste */

		final JList<JCheckBox> list=new JList<>();
		content.add(new JScrollPane(list),BorderLayout.CENTER);
		list.setCellRenderer(new JCheckBoxCellRenderer());
		list.setModel(model=new DefaultListModel<>());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index=list.locationToIndex(e.getPoint());
				if (index<0) return;
				final JCheckBox checkbox=list.getModel().getElementAt(index);
				checkbox.setSelected(!checkbox.isSelected());
				repaint();
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (String station: stations) model.addElement(new JCheckBox(station,true));

		/* Start/Ende verwenden */

		content.add(setup=new JPanel(),BorderLayout.SOUTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useStart=new JCheckBox(Language.tr("Simulation.ClientMovement.Start.Use")+":",true));
		line.add(nameStart=new JTextField(Language.tr("Simulation.ClientMovement.Start"),10));
		nameStart.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {useStart.setSelected(true);}
		});
		line.add(useEnd=new JCheckBox(Language.tr("Simulation.ClientMovement.End.Use")+":",true));
		line.add(nameEnd=new JTextField(Language.tr("Simulation.ClientMovement.End"),10));
		nameEnd.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {useEnd.setSelected(true);}
		});

		/* Javascript-Bibliothek direkt in html-Code einbetten */

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(includeJSLibrary=new JCheckBox(Language.tr("Simulation.ClientMovement.EmbedJS"),true));

		/* Dialog starten */

		setMinimumSize(new Dimension(800,600));
		setSize(new Dimension(800,600));
		setResizable(true);
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Erstellt die Liste der Stationen.
	 * @param table	Tabelle mit den Verknüpfungsdaten
	 * @param mode	Auf welcher Basis soll das Sankey-Diagramm erzeugt werden?
	 * @param stationNameStart	Name für die virtuelle Startstation
	 * @param stationNameEnd		Name für die virtuelle Endstation
	 * @return	Liste der Stationen
	 * @see #stations
	 */
	private static Set<String> getStationsList(final Table table, final Mode mode, final String stationNameStart, final String stationNameEnd) {
		final Set<String> stations=new HashSet<>();
		final int rows=table.getSize(0);

		switch (mode) {
		case STATION_TRANSITION:
			for (int i=0;i<rows;i++) {
				final List<String> row=table.getLine(i);
				if (row==null || row.size()<3) continue;
				stations.add(row.get(0));
				stations.add(row.get(1));
			}
			stations.remove(stationNameStart);
			stations.remove(stationNameEnd);
			break;
		case CLIENT_PATHS:
			for (int i=0;i<rows;i++) {
				final List<String> row=table.getLine(i);
				if (row==null || row.size()!=2) continue;
				final String[] cells=row.get(0).split(" -> ");
				if (cells!=null) for (String cell: cells) stations.add(cell);
			}
			break;
		}

		return stations;
	}

	/**
	 * Zeigt in Abhängigkeit vom gewählten Betriebsmodus des Dialogs
	 * einen passenden Auswahldialog für die Ausgabedatei an.
	 * Im Erfolgsfall werden die Daten in {@link #editFile} eingetragen.
	 * @see #editFile
	 */
	private void selectFile() {
		final String oldFile=editFile.getText().trim();
		final File initialFolder=(oldFile.isEmpty())?null:new File(oldFile).getParentFile();

		final JFileChooser fc=new JFileChooser();
		if (initialFolder==null) {
			CommonVariables.initialDirectoryToJFileChooser(fc);
		} else {
			fc.setCurrentDirectory(initialFolder);
		}
		fc.setDialogTitle(Language.tr("Simulation.ClientMovement.OutputFile.Hint"));

		final FileFilter html=new FileNameExtensionFilter(Language.tr("Simulation.ClientMovement.OutputFile.HTML")+" (*.html)","html");
		final FileFilter r=new FileNameExtensionFilter(Language.tr("Simulation.ClientMovement.OutputFile.R")+" (*.r)","r");
		fc.addChoosableFileFilter(html);
		fc.addChoosableFileFilter(r);
		fc.setFileFilter(html);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==html) file=new File(file.getAbsoluteFile()+".html");
			if (fc.getFileFilter()==r) file=new File(file.getAbsoluteFile()+".r");
		}

		editFile.setText(file.toString());
		outputFile.setSelected(true);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		switch (nr) {
		case 0:
			for (int i=0;i<model.getSize();i++) model.getElementAt(i).setSelected(true);
			repaint();
			break;
		case 1:
			for (int i=0;i<model.getSize();i++) model.getElementAt(i).setSelected(false);
			repaint();
			break;
		}
	}

	@Override
	protected boolean checkData() {
		if (outputFile.isSelected() && editFile.getText().trim().isEmpty()) {
			MsgBox.error(this,Language.tr("Simulation.ClientMovement.OutputFile.NoFileErrorTitle"),Language.tr("Simulation.ClientMovement.OutputFile.NoFileErrorInfo"));
			return false;
		}

		boolean selctOk=false;
		for (int i=0;i<model.getSize();i++) if (model.getElementAt(i).isSelected()) {selctOk=true; break;}
		if (!selctOk) {
			MsgBox.error(this,Language.tr("Simulation.ClientMovement.NoStationSelectedErrorTitle"),Language.tr("Simulation.ClientMovement.NoStationSelectedErrorInfo"));
			return false;
		}

		final File file=new File(editFile.getText());
		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return false;
		}

		return true;
	}

	/**
	 * Liefert die R-Sankey-Daten.
	 * @param names	Stationsnamen
	 * @param data	Stationsverknüpfungen
	 * @return	R-Sankey-Daten
	 */
	private String storeSankeyR(final List<String> names, final List<long[]> data) {
		final StringBuilder sb=new StringBuilder();

		sb.append("library(networkD3)\n");
		sb.append("library(tidyverse)\n");
		sb.append("\n");

		for (int i=0;i<names.size();i++) if (names.get(i)!=null) sb.append("s"+i+"=\""+names.get(i)+"\"\n");
		sb.append("\n");

		sb.append("links=data.frame(\n");
		sb.append("  source=c("+data.stream().map(value->"s"+value[0]).collect(Collectors.joining(", "))+"),\n");
		sb.append("  target=c("+data.stream().map(value->"s"+value[1]).collect(Collectors.joining(", "))+"),\n");
		sb.append("  value=c("+data.stream().map(value->""+value[2]).collect(Collectors.joining(", "))+")\n");
		sb.append(")\n");
		sb.append("\n");

		sb.append("nodes=data.frame(name=c(as.character(links$source), as.character(links$target)) %>% unique())\n");
		sb.append("links$IDsource=match(links$source, nodes$name)-1\n");
		sb.append("links$IDtarget=match(links$target, nodes$name)-1\n");
		sb.append("\n");

		sb.append("sankeyNetwork(\n");
		sb.append("  Links = links, Nodes = nodes, Source = \"IDsource\", Target = \"IDtarget\", Value = \"value\", NodeID = \"name\",\n");
		sb.append("  sinksRight=TRUE, fontSize=40, width=1980, height=800, nodePadding=650\n");
		sb.append(")\n");

		return sb.toString();
	}

	/**
	 * Liefert die HTML-Sankey-Daten.
	 * @param names	Stationsnamen
	 * @param data	Stationsverknüpfungen
	 * @param includeLibrary	Sollen die notwendigen JS-Bibliotheken direkt in den HTML-Code eingebettet werden?
	 * @return	HTML-Sankey-Daten
	 */
	private String storeSankeyHTML(final List<String> names, final List<long[]> data, final boolean includeLibrary) {
		final String namesString="\""+String.join("\",\n  \"",names)+"\"";
		final String links=data.stream().map(v->"{\"source\": "+v[0]+", \"target\": "+v[1]+", \"value\": "+v[2]+"}").collect(Collectors.joining(",\n  "));

		final StringBuilder html=new StringBuilder();

		html.append("<!DOCTYPE html>\n");
		html.append("<html>\n");
		html.append("<head>\n");
		html.append("  <meta charset=\"utf-8\"/>\n");
		html.append("  <title>"+Table.ExportTitle+"</title>\n");
		html.append("  <meta name=\"author\" content=\"Alexander Herzog\">\n");
		if (includeLibrary) {
			html.append("  <script type=\"text/javascript\">\n");
			html.append("  <!--\n");
			final String[] resourceNames=new String[] {
					"sankey/d3.min.js",
					"sankey/sankey.js",
					"sankey/sankey-run.js"
			};
			getLibrary(resourceNames,html);
			html.append("  //-->\n");
			html.append("  </script>\n");
		} else {
			html.append("  <script type=\"text/javascript\" src=\"sankey.js\"></script>\n");
		}
		html.append("  <style>html, body {height: 100%; font-family: sans-serif; margin: 0; padding: 0;}</style>\n");
		html.append("</head>\n");
		html.append("\n");

		html.append("<body>\n");
		html.append("\n");
		html.append("<div id=\"chart\"></div>\n");
		html.append("\n");

		html.append("<script type=\"text/javascript\">\n");
		html.append("\n");
		html.append("var nodes=[\n");
		html.append("  "+namesString+"\n");
		html.append("];\n");
		html.append("\n");
		html.append("var links=[\n");
		html.append("  "+links+"\n");
		html.append("];\n");
		html.append("\n");
		if (includeJSLibrary.isSelected()) {
			html.append("buildSankey(\"chart\",nodes,links);\n");
		} else {
			html.append("if (typeof(buildSankey)=='undefined') document.getElementById('chart').innerHTML='"+Language.tr("Simulation.ClientMovement.JSLibraryMissing")+"'; else buildSankey(\"chart\",nodes,links);\n");
		}
		html.append("\n");
		html.append("</script>\n");
		html.append("\n");

		html.append("</body>\n");
		html.append("</html>\n");

		return html.toString();
	}

	/**
	 * Liefert den JS-Code mehrerer JS-Bibliotheken
	 * @param resourceNames	Liste der JS-Bibliotheken
	 * @param result	Ausgabe-{@link StringBuilder}
	 * @return	Liefert im Erfolgsfall (wenn alle Ressourcen gefunden wurden) <code>true</code>
	 * @see #storeSankeyHTML(List, List, boolean)
	 * @see #storeLibrary(File)
	 */
	@SuppressWarnings("resource")
	private boolean getLibrary(final String[] resourceNames, final StringBuilder result) {
		for (String resourceName: resourceNames) {
			final URL url=getClass().getResource(resourceName);
			try {
				final URI uri=url.toURI();
				final String[] array=uri.toString().split("!");
				final Path path;
				FileSystem fileSystem;
				if (array.length==1) {
					fileSystem=null;
					path=Paths.get(uri);
				} else {
					final URI uri2=URI.create(array[0]);
					try {
						fileSystem=FileSystems.getFileSystem(uri2);
					} catch (FileSystemNotFoundException e) {
						fileSystem=FileSystems.newFileSystem(uri2,new HashMap<>());
					}
					if (fileSystem==null) fileSystem=FileSystems.newFileSystem(uri2,new HashMap<>());
					path=fileSystem.getPath(array[1]);
				}
				result.append(new String(Files.readAllBytes(path)));
				if (fileSystem!=null) fileSystem.close();
			} catch (IOException | URISyntaxException e1) {
				return false;
			}
			result.append("\n\n");
		}
		return true;
	}

	/**
	 * Speichert die JS-Sankey-Bibliotheken als externe Dateien
	 * @param folder	Ausgabeordner
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 * @see #storeData()
	 */
	private boolean storeLibrary(final File folder) {
		final StringBuilder result=new StringBuilder();

		/* Dateien zusammenfügen */
		final String[] resourceNames=new String[] {
				"sankey/d3.min.js",
				"sankey/sankey.js",
				"sankey/sankey-run.js"
		};
		getLibrary(resourceNames,result);

		/* Daten speichern */
		final File file=new File(folder,"sankey.js");
		if (!Table.saveTextToFile(result.toString(),file)) {
			MsgBox.error(this,Language.tr("Simulation.ClientMovement.OutputFile.NoSaveErrorTitle"),String.format(Language.tr("Simulation.ClientMovement.OutputFile.NoSaveErrorInfo"),file.toString()));
			return false;
		}

		return true;
	}

	/*
	private int findConnectionEnd(final int fromIndex, final List<String> stations, final List<String> usedStations, final Map<String,Map<String,Integer>> connections) {
		for (int i=fromIndex+1;i<stations.size();i++) if (usedStations.contains(stations.get(i))) {
			addConnection(fromIndex,i,stations,connections);
			return i;
		}
		addConnection(fromIndex,-1,stations,connections);
		return -1;
	}

	private void addConnection(final int fromIndex, final int toIndex, final List<String> stations, final Map<String,Map<String,Integer>> connections) {
		final String fromStation=(fromIndex==-1)?stationNameStart:stations.get(fromIndex);
		final String toStation=(toIndex==-1)?stationNameEnd:stations.get(toIndex);

		Map<String,Integer> stationConnection=connections.get(fromStation);
		if (stationConnection==null) connections.put(fromStation,stationConnection=new HashMap<>());
		stationConnection.put(toStation,stationConnection.getOrDefault(toStation,0)+1);
	}
	 */

	/**
	 * Wandelt einen Stationsnamen passend für die Ausgabe um.
	 * @param name	Stationsname
	 * @return	Stationsname in einer für die Ausgabe passenden Form.
	 * @see #storeData()
	 */
	private String processName(String name) {
		if (name.equals(stationNameStart)) name=nameStart.getText().trim();
		if (name.equals(stationNameEnd)) name=nameEnd.getText().trim();
		return name.replace("\"","\\\"");
	}

	/**
	 * Liefert eine Zuordnung, die angibt, wie viele Kunden von einer Station
	 * zu einer anderen gewechselt sind, auf Basis der Übergangstabelle.
	 * @param table	Tabelle mit Verknüpfungsdaten
	 * @return	Zuordnung von Start- und Zielstation zu der Anzahl an jeweiligen Übergängen
	 */
	private static Map<String,Map<String,Long>> buildMapFromTransitions(final Table table) {
		final Map<String,Map<String,Long>> allConnections=new HashMap<>();

		final int rows=table.getSize(0);
		for (int i=0;i<rows;i++) {
			final List<String> row=table.getLine(i);
			if (row==null || row.size()<3) continue;
			final String from=row.get(0);
			final String to=row.get(1);
			final Long L=NumberTools.getLong(row.get(2));
			if (L==null) continue;

			Map<String,Long> sub=allConnections.get(from);
			if (sub==null) allConnections.put(from,sub=new HashMap<>());
			final Long value=sub.get(to);
			if (value==null) sub.put(to,L); else sub.put(to,Long.valueOf(value.longValue()+L.longValue()));
		}

		return allConnections;
	}

	/**
	 * Liefert die Anzahlen an Übergängen zwischen den Stationen
	 * @param connections	Allgemeine Stations-Übergangs-Zuordnung
	 * @param usedStations	Betrachtete Stationen
	 * @return	Übergänge zwischen den betrachteten Stationen (jeder Listeneintrag ist ein 3-elementiges Array aus Quell-ID, Ziel-ID und Anzagl)
	 * @see #storeData()
	 */
	private List<long[]> getSankeyConnectionData(final Map<String,Map<String,Long>> connections, final List<String> usedStations) {
		final List<long[]> data=new ArrayList<>();
		for (Map.Entry<String,Map<String,Long>> entry1: connections.entrySet()) {
			final String from=entry1.getKey();
			for (Map.Entry<String,Long> entry2: entry1.getValue().entrySet()) {
				final String to=entry2.getKey();
				final int index1=usedStations.indexOf(from);
				final int index2=usedStations.indexOf(to);
				final long value=entry2.getValue();
				if (value>0) data.add(new long[] {index1,index2,value});
			}
		}
		return data;
	}

	/**
	 * Fügt eine Verbindung zu der allgemeinen Stations-Übergangs-Zuordnung hinzu.
	 * @param connections	Allgemeine Stations-Übergangs-Zuordnung
	 * @param from	Quell-Station
	 * @param to	Ziel-Station
	 * @param count	Anzahl an Kunden
	 * @see #buildSelectedStationsMap(Map, Set)
	 */
	private static void addConnection(final Map<String,Map<String,Long>> connections, final String from, final String to, final long count) {
		Map<String,Long> sub=connections.get(from);
		if (sub==null) connections.put(from,sub=new HashMap<>());
		final Long L=sub.get(to);
		if (L==null) sub.put(to,count); else sub.put(to,Long.valueOf(L.longValue()+count));
	}

	/**
	 * Fügt eine Verbindung zu der allgemeinen Stations-Übergangs-Zuordnung hinzu.<br>
	 * (Diese Methode ist für den Fall gedacht, dass eine Zwischenstation zwischen zwei Stationen für die Ausgabe nicht vorgesehen ist.)
	 * @param allConnections	Zuordnung aller Übergänge
	 * @param usedStations	Tatsächlich betrachtete Stationen
	 * @param connections	Allgemeine Stations-Übergangs-Zuordnung
	 * @param from	Quell-Station
	 * @param to	Ziel-Station
	 * @param count	Anzahl an Kunden
	 * @see #buildSelectedStationsMap(Map, Set)
	 */
	private static void addConnectionIndirect(final Map<String,Map<String,Long>> allConnections, final Set<String> usedStations, final Map<String,Map<String,Long>> connections, final String from, final String to, final long count) {
		/* from->to existiert nicht, daher Kunden aufteilen auf die to->... Ausgänge */

		final Map<String,Long> sub=allConnections.get(to);
		if (sub==null) return;

		/* Summe auslaufend aus to */
		double sum=0;
		for (Map.Entry<String,Long> entry: sub.entrySet()) sum+=entry.getValue();

		for (Map.Entry<String,Long> entry: sub.entrySet()) {
			final String nextTo=entry.getKey();
			final long subCount=Math.round(count*entry.getValue().doubleValue()/((sum>0)?sum:1)) ;
			if (usedStations.contains(nextTo)) {
				/* from->to Wert anteilig auf from->toNext aufteilen (toNext ist in Liste) */
				addConnection(connections,from,nextTo,subCount);
			} else {
				/* Wenn toNext nicht in Liste noch eine Stufe weiter iterieren */
				addConnectionIndirect(allConnections,usedStations,connections,from,nextTo,subCount);
			}
		}
	}

	/**
	 * Berechnet die Anzahl an Kundenübergängen zwischen bestimmten Stationen
	 * @param allConnections	Zuordnung aller Übergänge
	 * @param usedStations	Tatsächlich betrachtete Stationen
	 * @return	Zuordnung der Kundenübergänge zwischen den betrachteten Stationen
	 * @see #storeData()
	 */
	private static Map<String,Map<String,Long>> buildSelectedStationsMap(Map<String,Map<String,Long>> allConnections, final Set<String> usedStations) {
		final Map<String,Map<String,Long>> realConnections=new HashMap<>();

		for (Map.Entry<String,Map<String,Long>> entry1: allConnections.entrySet()) {
			final String from=entry1.getKey();
			if (!usedStations.contains(from)) continue;
			for (Map.Entry<String,Long> entry2: entry1.getValue().entrySet()) {
				final String to=entry2.getKey();
				final long count=entry2.getValue();
				if (usedStations.contains(to)) {
					/* Vorhandene Verbindungen A->B direkt übernehmen */
					addConnection(realConnections,from,to,count);
				} else {
					/* Wenn A->X nicht existiert, Ausgänge von X durchlaufen und Übergang A->X anteilig auf X->B, X->C usw. als A->B, A->C aufteilen */
					addConnectionIndirect(allConnections,usedStations,realConnections,from,to,count);
				}
			}
		}

		return realConnections;
	}

	/**
	 * Berechnet die Anzahl an Kundenübergängen zwischen bestimmten Stationen
	 * @param table	Tabelle der die Übergangsdaten entnommen werden sollen
	 * @param usedStations	Tatsächlich betrachtete Stationen
	 * @param stationNameStart	Name für die virtuelle Startstation
	 * @param stationNameEnd		Name für die virtuelle Endstation
	 * @return	Zuordnung der Kundenübergänge zwischen den betrachteten Stationen
	 * @see #storeData()
	 */
	private static Map<String,Map<String,Long>> buildMapFromPaths(final Table table, final Set<String> usedStations, final String stationNameStart, final String stationNameEnd) {
		final Map<String,Map<String,Long>> connections=new HashMap<>();

		final int rows=table.getSize(0);
		for (int i=0;i<rows;i++) {
			final List<String> row=table.getLine(i);
			if (row==null || row.size()!=2) continue;
			final String[] path=(stationNameStart+" -> "+row.get(0)+" -> "+stationNameEnd).split(" -> ");
			final Long L=NumberTools.getLong(row.get(1));
			if (path==null || path.length==0 || L==null) continue;
			final long count=L.longValue();

			final String[] usedPath=Arrays.asList(path).stream().filter(station->usedStations.contains(station)).toArray(String[]::new);
			if (usedPath.length>1) for (int j=1;j<usedPath.length;j++) addConnection(connections,usedPath[j-1],usedPath[j],count);
		}

		return connections;
	}

	/**
	 * Stellt eine Zuordnung mit allen Verbindungen zusammen.
	 * @param table	Tabelle mit den Verknüpfungsdaten
	 * @param mode	Auf welcher Basis soll das Sankey-Diagramm erzeugt werden?
	 * @return	Zuordnung mit allen Verbindungen
	 */
	public static Map<String,Map<String,Long>> getConnections(final Table table, final Mode mode) {
		final String stationNameStart=Language.tr("Simulation.ClientMovement.Start").toUpperCase();
		final String stationNameEnd=Language.tr("Simulation.ClientMovement.End").toUpperCase();
		final Set<String> usedStations=getStationsList(table,mode,stationNameStart,stationNameEnd);
		if (mode==Mode.CLIENT_PATHS) {
			usedStations.add(stationNameStart);
			usedStations.add(stationNameEnd);
		}
		return getConnections(table,mode,usedStations,stationNameStart,stationNameEnd);
	}

	/**
	 * Stellt eine Zuordnung mit allen Verbindungen zusammen.
	 * @param table	Tabelle mit den Verknüpfungsdaten
	 * @param mode	Auf welcher Basis soll das Sankey-Diagramm erzeugt werden?
	 * @param usedStations	Tatsächlich betrachtete Stationen
	 * @param stationNameStart	Name für die virtuelle Startstation
	 * @param stationNameEnd		Name für die virtuelle Endstation
	 * @return	Zuordnung mit allen Verbindungen
	 */
	public static Map<String,Map<String,Long>> getConnections(final Table table, final Mode mode, final Set<String> usedStations, final String stationNameStart, final String stationNameEnd) {
		final Map<String,Map<String,Long>> realConnections;

		switch (mode) {
		case STATION_TRANSITION:
			final Map<String,Map<String,Long>> allConnections=buildMapFromTransitions(table);
			realConnections=buildSelectedStationsMap(allConnections,usedStations);
			break;
		case CLIENT_PATHS:
			realConnections=buildMapFromPaths(table,usedStations,stationNameStart,stationNameEnd);
			break;
		default:
			realConnections=null;
			break;
		}

		return realConnections;
	}

	@Override
	public void storeData() {
		/* Verwendete Stationen */

		final List<String> usedStations=new ArrayList<>();
		for (int i=0;i<stations.size();i++) if (model.get(i).isSelected()) usedStations.add(stations.get(i));
		if (useStart.isSelected()) usedStations.add(stationNameStart);
		if (useEnd.isSelected()) usedStations.add(stationNameEnd);
		final List<String> names=usedStations.stream().map(name->processName(name)).collect(Collectors.toList());

		/* Daten vorbereiten */

		final Map<String,Map<String,Long>> realConnections=getConnections(table,mode,new HashSet<>(usedStations),stationNameStart,stationNameEnd);
		if (realConnections==null) return;
		final List<long[]> data=getSankeyConnectionData(realConnections,usedStations);

		/* Datei wählen */

		final File file;
		if (outputDirect.isSelected()) {
			try {
				file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".html");
			} catch (IOException e) {
				MsgBox.error(this,StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
				return;
			}
		} else {
			file=new File(editFile.getText());
		}
		final String ext=file.toString().toUpperCase();

		/* Ausgabe erzeugen */

		String result=null;

		if (ext.endsWith(".R")) result=storeSankeyR(names,data);
		if (ext.endsWith(".HTML")) {
			final boolean includeLibrary=includeJSLibrary.isSelected() || outputDirect.isSelected();
			if (!includeLibrary) {
				if (!storeLibrary(file.getParentFile())) return;
			}
			result=storeSankeyHTML(names,data,includeLibrary);
		}
		if (result==null) result=storeSankeyR(names,data);

		/* Daten speichern */

		if (result!=null) {
			if (!Table.saveTextToFile(result,file)) {
				MsgBox.error(this,Language.tr("Simulation.ClientMovement.OutputFile.NoSaveErrorTitle"),String.format(Language.tr("Simulation.ClientMovement.OutputFile.NoSaveErrorInfo"),file.toString()));
				return;
			}
			if (outputDirect.isSelected()) {
				file.deleteOnExit();
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException e) {
					MsgBox.error(this,StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
				}
			}
		}
	}

	/**
	 * Renderer für die Auswahlliste der Stationen
	 */
	private static class JCheckBoxCellRenderer implements ListCellRenderer<JCheckBox> {
		/**
		 * Konstruktor der Klasse
		 */
		public JCheckBoxCellRenderer() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value, int index, boolean isSelected, boolean cellHasFocus) {
			value.setForeground(list.getForeground());
			value.setBackground(list.getBackground());
			return value;
		}
	}
}