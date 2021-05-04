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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.EditorPanel;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementVertex;
import ui.tools.FlatLaFHelper;
import ui.tools.WindowSizeStorage;

/**
 * Zeigt einen Dialog an, in dem die möglichen Pfade der Kunden durch das System visualisiert werden.
 * @author Alexander Herzog
 * @see StatisticViewerMovementText
 * @see StatisticViewerMovementTable
 */
public class StatisticViewerMovementVisualizationDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2910523901591660604L;

	/**
	 * Statiatik-Objekt dem die Pfade und das Modell entnommen werden sollen
	 */
	private final Statistics statistics;

	/**
	 * Liste der möglichen Pfade durch das Modell
	 * @see StatisticViewerMovementVisualizationDialog.Path
	 */
	private final List<Path> paths;

	/**
	 * Auswahlfeld zur Auswahl des anzuzeigenden Pfades
	 * @see #pathSelectionChanged()
	 */
	private final JComboBox<String> comboBox;

	/**
	 * Schaltfläche für "nach oben" (in der Liste der Pfade)
	 */
	private final JButton buttonUp;

	/**
	 * Schaltfläche für "nach unten" (in der Liste der Pfade)
	 */
	private final JButton buttonDown;

	/**
	 * Zeichenfläche für das Modell
	 */
	private final EditorPanel editor;

	/**
	 * Listenmodell zur Anzeige der Liste der Stationen auf einem Pfad
	 */
	private final DefaultListModel<String> pathListModel;

	/**
	 * Tortendiagramm zur Anzeige des Anteils des aktuellen Pfades
	 */
	private final PiePlot<?> piePlot;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param statistics	Statiatik-Objekt dem die Pfade und das Modell entnommen werden sollen
	 */
	public StatisticViewerMovementVisualizationDialog(final Component owner, final Statistics statistics) {
		super(owner,Language.tr("Statistics.ClientMovement.Visualization.Title"));
		this.statistics=statistics;

		JPanel sub;

		/* Auflistung der Pfade erstellen */
		paths=new ArrayList<>();
		final double sum=Arrays.stream(statistics.clientPaths.getAll()).map(o->(StatisticsSimpleCountPerformanceIndicator)o).mapToLong(StatisticsSimpleCountPerformanceIndicator::get).sum();
		for (String name: statistics.clientPaths.getNames()) paths.add(new Path(name,((StatisticsSimpleCountPerformanceIndicator)statistics.clientPaths.get(name)).get()/sum));
		paths.sort((p1,p2)->-Double.compare(p1.getPart(),p2.getPart()));

		/* GUI */
		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(this,"ClientMovementVisualization"));
		content.setLayout(new BorderLayout());

		/* Bereich oben */
		final JPanel setup=new JPanel(new BorderLayout());
		content.add(setup,BorderLayout.NORTH);

		/* Auswahl des Pfades */
		setup.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.WEST);
		final JLabel label=new JLabel(Language.tr("Statistics.ClientMovement.Visualization.Path")+": ");
		sub.add(label);

		final List<String> pathNames=new ArrayList<>();
		for (int i=0;i<paths.size();i++) pathNames.add(Language.tr("Statistics.ClientMovement.Visualization.Path")+" "+(i+1)+": "+paths.get(i).toString());
		setup.add(comboBox=new JComboBox<>(pathNames.toArray(new String[0])),BorderLayout.CENTER);
		label.setLabelFor(comboBox);
		if (comboBox.getItemCount()>0) comboBox.setSelectedIndex(0);
		comboBox.addActionListener(e->pathSelectionChanged());

		/* Schaltflächen */
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		setup.add(toolbar,BorderLayout.EAST);
		toolbar.add(buttonUp=new JButton(Images.ARROW_UP.getIcon()));
		buttonUp.setToolTipText(Language.tr("Statistics.ClientMovement.Visualization.ListUp")+" ("+getHotkey(KeyStroke.getKeyStroke(KeyEvent.VK_UP,InputEvent.CTRL_DOWN_MASK))+")");
		buttonUp.addActionListener(e->stepComboBox(-1));
		toolbar.add(buttonDown=new JButton(Images.ARROW_DOWN.getIcon()));
		buttonDown.setToolTipText(Language.tr("Statistics.ClientMovement.Visualization.ListDown")+" ("+getHotkey(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.CTRL_DOWN_MASK))+")");
		buttonDown.addActionListener(e->stepComboBox(1));
		final JButton button=new JButton(Images.GENERAL_TOOLS.getIcon());
		toolbar.add(button);
		button.setToolTipText(Language.tr("Statistics.ClientMovement.Visualization.Tools"));
		button.addActionListener(e->showToolsPopup(button));

		/* Bereich in der Mitte */
		final JPanel main=new JPanel(new BorderLayout());
		content.add(main,BorderLayout.CENTER);

		/* Anzeige des Pfades im Modell im Hauptbereich */
		main.add(editor=new EditorPanel(this,new EditModel(),true,true,false,false),BorderLayout.CENTER);

		/* Bereich unten */
		final JPanel info=new JPanel(new BorderLayout());
		main.add(info,BorderLayout.EAST);

		/* Anzeige des Pfads als Liste */
		info.add(new JScrollPane(new JList<>(pathListModel=new DefaultListModel<>())),BorderLayout.CENTER);

		/* Daten für das Diagramm */
		DefaultPieDataset<String> dataset=new DefaultPieDataset<>();
		for (Path path: paths) dataset.setValue(path.name,path.getPart());
		final JFreeChart chart=ChartFactory.createPieChart(null,dataset);
		piePlot=(PiePlot<?>)chart.getPlot();
		for (Path path: paths) piePlot.setSectionPaint(path.name,Color.LIGHT_GRAY);

		/* ChartPanel */
		final ChartPanel chartPanel=new ChartPanel(
				chart,
				250,
				175,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_BUFFER_USED,
				true,
				false,
				true,
				true,
				true
				);
		chartPanel.setPopupMenu(null);
		if (FlatLaFHelper.isDark()) {
			piePlot.setBackgroundPaint(Color.DARK_GRAY);
			chart.setBackgroundPaint(Color.DARK_GRAY);
		} else {
			piePlot.setBackgroundPaint(new Color(245,245,245));
			chart.setBackgroundPaint(new Color(245,245,245));
		}
		piePlot.setLabelGenerator(null);
		chart.getLegend().setVisible(false);
		info.add(chartPanel,BorderLayout.SOUTH);

		/* Hotkeys */
		content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,InputEvent.CTRL_DOWN_MASK),"ListUp");
		content.getActionMap().put("ListUp",new AbstractAction("ListUp") {
			private static final long serialVersionUID=699698084812988456L;
			@Override public void actionPerformed(ActionEvent event) {stepComboBox(-1);}
		});
		content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.CTRL_DOWN_MASK),"ListDown");
		content.getActionMap().put("ListDown",new AbstractAction("ListDown") {
			private static final long serialVersionUID=9056074088944833973L;
			@Override public void actionPerformed(ActionEvent event) {stepComboBox(1);}
		});

		/* Dialog starten */
		pathSelectionChanged();
		setResizable(true);
		setMinSizeRespectingScreensize(1200,768);
		setSizeRespectingScreensize(1200,768);
		setLocationRelativeTo(getOwner());
		WindowSizeStorage.window(this,"ClientMovementVisualization");
		setVisible(true);
	}

	/**
	 * Wandelt ein Hotkey-Objekt in einen entsprechenden Text um.
	 * @param keyStroke	Hotkey-Objekt
	 * @return	Textbeschreibung zu dem Hotkey
	 */
	private static String getHotkey(final KeyStroke keyStroke) {
		final int modifiers=keyStroke.getModifiers();
		String acceleratorText=(modifiers==0)?"":InputEvent.getModifiersExText(modifiers)+"+";
		acceleratorText+=KeyEvent.getKeyText(keyStroke.getKeyCode());
		return acceleratorText;
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private String encodeHTMLentities(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	/**
	 * Wählt einen anderen Pfad in {@link #comboBox} aus.
	 * @param delta	Relative verschiebung der Auswahl (z.B. 1 oder -1)
	 * @see #comboBox
	 * @see #pathSelectionChanged()
	 */
	private void stepComboBox(final int delta) {
		comboBox.setSelectedIndex(Math.min(paths.size()-1,Math.max(0,comboBox.getSelectedIndex()+delta)));
		pathSelectionChanged();
	}

	/**
	 * Reagiert darauf, wenn in {@link #comboBox} ein neuer Pfad
	 * ausgewählt wird.
	 * @see #comboBox
	 */
	private void pathSelectionChanged() {
		final int index=comboBox.getSelectedIndex();
		final Path path=paths.get(index);
		final EditModel model=statistics.editModel.clone();

		/* Schaltflächen aktivieren oder deaktivieren */
		buttonUp.setEnabled(index>0);
		buttonDown.setEnabled(index<paths.size()-1);

		/* Neues Modell im Editor einstellen */
		final Set<Integer> highlightIDs=path.getAllPathIDs(model);
		for (ModelElement element: model.surface.getElements()) {
			if (highlightIDs.contains(element.getId())) {
				element.setDrawMode(ModelElement.DrawMode.HIGHLIGHTED);
			} else {
				element.setDrawMode(ModelElement.DrawMode.GRAYED_OUT);
			}
			if (element instanceof ModelElementSub) {
				for (ModelElement subElement: ((ModelElementSub)element).getSubSurface().getElements()) {
					if (highlightIDs.contains(subElement.getId())) {
						subElement.setDrawMode(ModelElement.DrawMode.HIGHLIGHTED);
					} else {
						subElement.setDrawMode(ModelElement.DrawMode.GRAYED_OUT);
					}
				}
			}
		}
		editor.setModel(model);

		/* Liste aktualisieren */
		pathListModel.clear();
		final StringBuilder heading=new StringBuilder();
		heading.append("<html><body>");
		heading.append("<b>");
		heading.append(Language.tr("Statistics.ClientMovement.Visualization.Path")+" "+(index+1)+" ("+StatisticTools.formatPercent(path.getPart())+")");
		heading.append("</b>");
		heading.append("</body></html>");
		pathListModel.addElement(heading.toString());

		for (Integer id: path.getStations()) {
			final ModelElement element=model.surface.getByIdIncludingSubModels(id);
			final StringBuilder record=new StringBuilder();
			record.append("<html><body>");
			if (element==null) {
				record.append("id="+id+" - "+encodeHTMLentities(Language.tr("Statistics.ClientMovement.Visualization.UnknownStation")));
			} else {
				final String typeName=(element instanceof ModelElementBox)?((ModelElementBox)element).getTypeName():"";
				final String elementName=element.getName();
				record.append(encodeHTMLentities(typeName));
				if (!typeName.isEmpty() && !elementName.isEmpty()) record.append(" - ");
				record.append("<b>"+encodeHTMLentities(elementName)+"</b>");
				record.append(" (id="+id+")");
			}
			record.append("</body></html>");
			pathListModel.addElement(record.toString());
		}

		/* Diagramm aktualisieren */
		for (Path p: paths) piePlot.setSectionPaint(p.name,(p==path)?Color.RED:Color.LIGHT_GRAY);
	}

	/**
	 * Zeigt das Exportieren-Popupmenü an
	 * @param parent	Übergeordnetes Element zum Ausrichten des Menüs
	 */
	private void showToolsPopup(final JButton parent) {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("Statistics.ClientMovement.Visualization.Copy"),Images.EDIT_COPY_AS_IMAGE.getIcon()));
		item.addActionListener(e->editor.exportModelToClipboard());

		popup.add(item=new JMenuItem(Language.tr("Statistics.ClientMovement.Visualization.Save"),Images.IMAGE_SAVE.getIcon()));
		item.addActionListener(e->{
			String error=editor.exportModelToFile(null,false);
			if (error!=null) MsgBox.error(getOwner(),Language.tr("XML.ExportErrorTitle"),error);
		});

		popup.show(parent,0,parent.getHeight());
	}

	/**
	 * Diese Klasse repräsentiert einen einzelnen Pfad
	 * @see Statistics#clientPaths
	 * @see StatisticViewerMovementVisualizationDialog#paths
	 */
	private static class Path {
		/**
		 * Maximaler anzuzeigender Länge des Namens eines Pfads
		 */
		private static final int MAX_PATH_NAME_LENGTH=250;

		/**
		 * Name des Pfads
		 */
		private final String name;

		/**
		 * Anteil der Kunden, die diesen Pfad gewählt haben
		 */
		private final double part;

		/**
		 * IDs der Stationen auf diesem Pfad
		 * @see #getStations()
		 */
		private final List<Integer> stations;

		/**
		 * IDs aller Elemente (inkl. Kanten) auf dem Pfad
		 * @see #getAllPathIDs(EditModel)
		 * @see #buildAllPathIDs(List, EditModel)
		 */
		private Set<Integer> allPathStations;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name des Pfads
		 * @param part	Anteil der Kunden, die diesen Pfad gewählt haben
		 */
		public Path(final String name, final double part) {
			this.name=name;
			this.part=part;

			stations=new ArrayList<>();
			for (String station: name.split(" -> ")) {
				if (!station.endsWith(")")) continue;
				String s=station.substring(0,station.length()-1);
				final int index=s.indexOf("(id=");
				if (index<0) continue;
				s=s.substring(index+4);
				final Integer I=NumberTools.getNotNegativeInteger(s);
				if (I==null) continue;
				stations.add(I);
			}
		}

		@Override
		public String toString() {
			String nameShort;
			if (name.length()>MAX_PATH_NAME_LENGTH) nameShort=name.substring(0,MAX_PATH_NAME_LENGTH)+"..."; else nameShort=name;

			return nameShort+" ("+StatisticTools.formatPercent(part)+")";
		}

		/**
		 * Liefert den Anteil der Kunden, die diesen Pfad gewählt haben.
		 * @return	Anteil der Kunden, die diesen Pfad gewählt haben
		 */
		public double getPart() {
			return part;
		}

		/**
		 * Sucht einen Pfad von einer Startkante zu einer Zielstation
		 * @param startEdge	Startkante
		 * @param destinationID	Zielstation
		 * @param model	Editor-Modell dem die zusätzlichen Informationen entnommen werden
		 * @return	Vollständiger Pfad inkl. Kanten und Ecken aber ohne die Zielstation selbst oder <code>null</code>, wenn kein Pfad gefunden werden konnte
		 */

		private static List<Integer> findPath(final ModelElementEdge startEdge, final int destinationID, final EditModel model) {
			if (startEdge==null) return null;
			final List<Integer> path=new ArrayList<>();
			path.add(startEdge.getId());

			ModelElement next=startEdge.getConnectionEnd();
			while (next!=null) {
				if (next instanceof ModelElementBox) {
					if (next.getId()==destinationID) return path;
					return null;
				}
				if (next instanceof ModelElementVertex) {
					final ModelElementVertex vertex=(ModelElementVertex)next;
					path.add(vertex.getId());
					final ModelElementEdge edge=vertex.getEdgeOut();
					if (edge==null) return null;
					path.add(edge.getId());
					next=edge.getConnectionEnd();
					continue;
				}
				return null;
			}

			return null;
		}

		/**
		 * Sucht einen Pfad von einer Start- zu einer Zielstation
		 * @param startID	Startstation
		 * @param destinationID	Zielstation
		 * @param model	Editor-Modell dem die zusätzlichen Informationen entnommen werden
		 * @return	Vollständiger Pfad inkl. Kanten und Ecken aber ohne Start- und Zielstation selbst oder <code>null</code>, wenn kein Pfad gefunden werden konnte
		 */
		private static List<Integer> findPath(final int startID, final int destinationID, final EditModel model) {
			final ModelElement element=model.surface.getByIdIncludingSubModels(startID);
			if (!(element instanceof ModelElementBox)) return null;

			if (element instanceof ModelElementEdgeOut) {
				return findPath(((ModelElementEdgeOut)element).getEdgeOut(),destinationID,model);
			}

			if (element instanceof ModelElementEdgeMultiOut) {
				for (ModelElementEdge edge: ((ModelElementEdgeMultiOut)element).getEdgesOut()) {
					final List<Integer> path=findPath(edge,destinationID,model);
					if (path!=null) return path;
				}
				return null;
			}

			return null;
		}

		/**
		 * Erstellt eine vollständige Liste aller Elemente auf dem Pfad (inkl. Kanten und Ecken)
		 * @param stations	Liste der Stationen auf dem Pfad
		 * @param model	Editor-Modell dem die zusätzlichen Informationen entnommen werden
		 * @return	Liste aller Elemente auf dem Pfad (inkl. Kanten und Ecken)
		 */
		private static List<Integer> buildAllPathIDs(final List<Integer> stations, final EditModel model) {
			final List<Integer> allPathStations=new ArrayList<>();

			int lastID=-1;
			for (Integer id: stations) {
				if (lastID>=0) {
					final List<Integer> subPath=findPath(lastID,id,model);
					if (subPath!=null && subPath.size()>0) allPathStations.addAll(subPath);
				}
				allPathStations.add(id);
				lastID=id;
			}

			return allPathStations;
		}

		/**
		 * Liefert die IDs der Stationen auf diesem Pfad in Besuchsreihenfolge.
		 * @return	IDs der Stationen auf diesem Pfad in Besuchsreihenfolge
		 */
		public List<Integer> getStations() {
			return stations;
		}

		/**
		 * Liefert die IDs aller Elemente (inkl. Kanten) auf dem Pfad.
		 * @param model	Editor-Modell dem die Zusatzinformationen zur Ergänzung des Pfads entnommen werden sollen
		 * @return	IDs aller Elemente (inkl. Kanten) auf dem Pfad
		 */
		public Set<Integer> getAllPathIDs(final EditModel model) {
			if (allPathStations==null) allPathStations=new HashSet<>(buildAllPathIDs(stations,model));
			return allPathStations;
		}
	}
}
