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
package ui.expressionbuilder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import language.Language;
import mathtools.NumberTools;
import simulator.runmodel.RunModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementSub;
import ui.quickaccess.JPlaceholderTextField;

/**
 * Dieser Dialog ermöglicht das Zusammenstellen eines Ausdrucks (zur Berechnung oder
 * zur Vergleich von Ausdrücken) auf Basis der Liste der verfügbaren Funktionen.
 * @author Alexander Herzog
 */
public class ExpressionBuilder extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -8629304820144439899L;

	/** Gibt an, ob es sich bei dem Ausdruck um einen Vergleich (<code>true</code>) oder um einen zu einer Zahl auszurechnenden Ausdruck (<code>false</code>) handelt */
	private final boolean isCompare;
	/** Liste mit den im System vorhandenen Variablen */
	private final String[] variables;
	/** Liste der initialen Variablen mit Werten */
	private final Map<String,String> initialVariableValues;
	/** Zuordnung von Stations-IDs und Stationsnamen (kann über die statische Funktion <code>getStationIDs(surface)</code> erstellt werden) */
	private final Map<Integer,String> stations;
	/** Zuordnung von Stations-IDs zu vom Nutzer angegebenen Stationsnamen */
	private final Map<Integer,String> stationNames;
	/** Gibt an, ob Funktionen zum Zugriff auf Kundenobjekt-spezifische Datenfelder angeboten werden sollen */
	private final boolean hasClientData;
	/** Gibt an, dass nur Funktionen angeboten werden sollen, deren Ergebnisse aus Statistikdaten gewonnen werden können (keine reinen Runtime-Daten) */
	private final boolean statisticsOnly;
	/** Gibt an, dass überhaupt keine Funktionen, die sich auf Simulation oder Ergebnisse beziehen, angeboten werden sollen. */
	private final boolean noSimulator;

	/** Schnellfilter-Eingabefeld */
	private final JPlaceholderTextField quickFilter;

	/** Baumstruktur zur Auswahl eines Symbols */
	private final JTree tree;

	/** Panel zur Anzeige von Informationen zum aktuell gewählten Symbol */
	private final JTextPane info;

	/** Eingabefeld für Ausdrücke */
	private final JTextArea input;

	/**
	 * Initial in der Baumstruktur zu öffnende Pfade
	 * @see #buildTreeData(String)
	 */
	private final List<TreePath> pathsToOpen;

	/**
	 * Konstruktor der Klasse <code>ExpressionBuilder</code>
	 * @param owner	Übergeordnetes Element
	 * @param expression	Bisheriger Ausdruck, wird initial im Eingabefeld angezeigt
	 * @param isCompare	Gibt an, ob es sich bei dem Ausdruck um einen Vergleich (<code>true</code>) oder um einen zu einer Zahl auszurechnenden Ausdruck (<code>false</code>) handelt
	 * @param variables	Liste mit den im System vorhandenen Variablen
	 * @param initialVariables	Liste der initialen Variablen mit Werten
	 * @param stations	Zuordnung von Stations-IDs und Stationsnamen (kann über die statische Funktion <code>getStationIDs(surface)</code> erstellt werden)
	 * @param stationNames	Zuordnung von Stations-IDs zu vom Nutzer angegebenen Stationsnamen
	 * @param hasClientData	Gibt an, ob Funktionen zum Zugriff auf Kundenobjekt-spezifische Datenfelder angeboten werden sollen
	 * @param statisticsOnly	Gibt an, dass nur Funktionen angeboten werden sollen, deren Ergebnisse aus Statistikdaten gewonnen werden können (keine reinen Runtime-Daten)
	 * @param noSimulator	Gibt an, dass überhaupt keine Funktionen, die sich auf Simulation oder Ergebnisse beziehen, angeboten werden sollen.
	 */
	public ExpressionBuilder(final Component owner, final String expression, final boolean isCompare, final String[] variables, final Map<String,String> initialVariables, final Map<Integer,String> stations, final Map<Integer,String> stationNames, final boolean hasClientData, final boolean statisticsOnly, final boolean noSimulator) {
		super(owner,Language.tr("ExpressionBuilder.Title"));

		this.isCompare=isCompare;
		final Set<String> tempVariables=new HashSet<>();
		if (variables!=null) tempVariables.addAll(Arrays.asList(variables));
		if (hasClientData) for (String var: RunModel.additionalVariables) {
			boolean inList=false;
			for (String s: tempVariables) if (s.equalsIgnoreCase(var)) {inList=true; break;}
			if (!inList) tempVariables.add(var);
		}
		if (initialVariables!=null) tempVariables.addAll(initialVariables.keySet());
		this.variables=tempVariables.toArray(new String[0]);
		this.initialVariableValues=initialVariables;
		if (stations==null) this.stations=new HashMap<>(); else this.stations=stations;
		if (stationNames==null) this.stationNames=new HashMap<>(); else this.stationNames=stationNames;
		this.hasClientData=hasClientData;
		this.statisticsOnly=statisticsOnly;
		this.noSimulator=noSimulator;

		pathsToOpen=new ArrayList<>();

		final JPanel content=createGUI(()->Help.topicModal(ExpressionBuilder.this,"Expressions"));
		content.setLayout(new BorderLayout());

		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(panel,BorderLayout.NORTH);
		panel.add(new JLabel(Language.tr("ExpressionBuilder.Info")));

		tree=new JTree();
		tree.setRootVisible(false);
		((DefaultTreeModel)(tree.getModel())).setRoot(buildTreeData(null));
		tree.addTreeSelectionListener(e->{
			if (e.getPath()!=null && e.getPath().getLastPathComponent()!=null && e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode)e.getPath().getLastPathComponent()).getUserObject()!=null && ((DefaultMutableTreeNode)e.getPath().getLastPathComponent()).getUserObject() instanceof ExpressionSymbol) {
				selectTreeNode((ExpressionSymbol)((DefaultMutableTreeNode)e.getPath().getLastPathComponent()).getUserObject());
			} else {
				selectTreeNode(null);
			}
		});
		tree.setCellRenderer(new ExpressionBuilderTreeCellRenderer());
		tree.addMouseListener(new TreeMouseListener());
		for (TreePath path: pathsToOpen) tree.expandPath(path);

		info=new JTextPane();
		info.setEditable(false);

		final JScrollPane scroll1, scroll2;
		final JPanel left=new JPanel(new BorderLayout());
		left.add(quickFilter=new JPlaceholderTextField(),BorderLayout.NORTH);
		quickFilter.setPlaceholder(Language.tr("Editor.QuickFilter"));
		quickFilter.setToolTipText(Language.tr("Editor.QuickFilter.Tooltip"));
		quickFilter.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {updateTemplatesFilter(); quickFilter.requestFocus();}
			@Override public void keyReleased(KeyEvent e) {updateTemplatesFilter(); quickFilter.requestFocus();}
			@Override public void keyPressed(KeyEvent e) {updateTemplatesFilter(); quickFilter.requestFocus();}
		});
		left.add(scroll1=new JScrollPane(tree),BorderLayout.CENTER);
		content.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,left,scroll2=new JScrollPane(info)),BorderLayout.CENTER);
		scroll1.setMinimumSize(new Dimension(300,0));
		scroll2.setMinimumSize(new Dimension(300,0));

		content.add(new JScrollPane(input=new JTextArea(5,0)),BorderLayout.SOUTH);
		input.setWrapStyleWord(true);
		input.setLineWrap(true);
		input.setText(expression);
		input.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		input.setCaret(new DefaultCaret() {
			private static final long serialVersionUID = -8304299937440074456L;
			@Override public void setSelectionVisible(boolean visible) {super.setSelectionVisible(true);}
		});
		ModelElementBaseDialog.addUndoFeature(input);

		ExpressionBuilderAutoComplete.process(this,input);

		setSizeRespectingScreensize(800,600);
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);

		SwingUtilities.invokeLater(()->{checkData(false); input.requestFocus();});
	}

	/**
	 * Aktualisiert die Baumstruktur {@link #tree} nach einer Eingabe in {@link #quickFilter}.
	 * @see #tree
	 * @see #quickFilter
	 */
	private void updateTemplatesFilter() {
		((DefaultTreeModel)(tree.getModel())).setRoot(buildTreeData(quickFilter.getText().trim()));
	}

	/**
	 * Erstellt einen neuen Eintrag für die Baumstruktur (fügt diesen aber noch nicht ein)
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol für den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 * @param type	Typ des Symbols
	 * @return	Neuer Eintrag für die Baumstruktur
	 * @see #addTreeNode(DefaultMutableTreeNode, String, String, String, String, ExpressionSymbolType)
	 */
	private DefaultMutableTreeNode getTreeNode(final String name, final String symbol, final String description, final ExpressionSymbolType type) {
		return new DefaultMutableTreeNode(new ExpressionSymbol(name,symbol,description,type));
	}

	/**
	 * Fügt einen Eintrag zur Baumstruktur hinzu
	 * @param group	Gruppe zu der der Eintrag hinzugefügt werden soll
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird der Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol für den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 * @param type	Typ des Symbols
	 */
	private void addTreeNode(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String symbol, final String description, final ExpressionSymbolType type) {
		if (filterUpper==null || name.toUpperCase().contains(filterUpper) || symbol.toUpperCase().contains(filterUpper)) {
			group.add(getTreeNode(name,symbol,description,type));
		}
	}

	/**
	 * Fügt Variablen zur Baumstruktur hinzu
	 * @param root	Wurzelelement der Baumstruktur
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird ein Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 */
	private void buildTreeDataVariables(final DefaultMutableTreeNode root, final String filterUpper) {
		if (variables==null || variables.length==0) return;

		final DefaultMutableTreeNode group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.Variables.Plural"));
		for (String variable: variables) {
			String expression=null;
			if (initialVariableValues!=null) expression=initialVariableValues.get(variable);
			if (expression==null) {
				expression=String.format(Language.tr("ExpressionBuilder.Variables.InitialValueImplicite"),variable);
			} else {
				expression=String.format(Language.tr("ExpressionBuilder.Variables.InitialValue"),variable,expression);
			}
			addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.Variables.Singular")+" "+variable,variable,"<p>"+Language.tr("ExpressionBuilder.Variables.Info")+" <b>"+variable+"</b>.</p>"+expression,ExpressionSymbolType.TYPE_VARIABLE);
		}
		if (group.getChildCount()>0) root.add(group);
	}

	/**
	 * Fügt die Stations-IDs zur Baumstruktur hinzu
	 * @param root	Wurzelelement der Baumstruktur
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird ein Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 */
	private void buildTreeDataStationIDs(final DefaultMutableTreeNode root, final String filterUpper) {
		DefaultMutableTreeNode group;

		if (stations!=null && stations.size()>0) {
			group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.StationIDs"));
			for (Map.Entry<Integer,String> entry: stations.entrySet()) {
				String title=entry.getValue();
				title=title.replaceAll("<","&lt;");
				title=title.replaceAll(">","&gt;");
				addTreeNode(group,filterUpper,title+" (id="+entry.getKey()+")",""+entry.getKey(),"<p>"+Language.tr("ExpressionBuilder.SimulationCharacteristics.StationIDs.idOfStation")+" <b>"+title+"</b></p>",ExpressionSymbolType.TYPE_STATION_ID);
			}
			if (group.getChildCount()>0) root.add(group);
		}

		if (stations!=null && stationNames!=null && stationNames.size()>0) {
			group=null;
			for (Map.Entry<Integer,String> entry: stationNames.entrySet()) {
				String title=entry.getValue();
				if (title.trim().isEmpty()) continue;
				if (group==null) group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.StationIDsByNames"));
				String longTitle=stations.get(entry.getKey());
				if (longTitle==null) longTitle=title;
				addTreeNode(group,filterUpper,longTitle+" (id="+entry.getKey()+")","$(\""+title.replaceAll("\"","\\\\\"")+"\")","<p>"+Language.tr("ExpressionBuilder.SimulationCharacteristics.StationIDsByNames.idOfStation")+" <b>"+longTitle+"</b></p>",ExpressionSymbolType.TYPE_STATION_ID);
			}
			if (group!=null && group.getChildCount()>0) root.add(group);
		}
	}

	/**
	 * Fügt die Stations-IDs zur Baumstruktur hinzu
	 * @param root	Wurzelelement der Baumstruktur
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird ein Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 */
	private void buildTreeDataGlobalMapKeys(final DefaultMutableTreeNode root, final String filterUpper) {
		DefaultMutableTreeNode group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.GlobalMapKeys"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.SimulationCharacteristics.GlobalMapKeys"),"§(\"key\")","<p>"+Language.tr("ExpressionBuilder.SimulationCharacteristics.GlobalMap.Info")+"</p>",ExpressionSymbolType.TYPE_GLOBAL_MAP);
		if (group.getChildCount()>0) root.add(group);
	}

	/**
	 * Erstellt die Baumstruktur
	 * @param filter	Optionaler Filter (kann <code>null</code> sein)
	 * @return	Wurzelelement der neuen Baumstruktur
	 */
	public TreeNode buildTreeData(final String filter) {
		final String filterUpper=(filter!=null && !filter.trim().isEmpty())?filter.trim().toUpperCase():null;

		final DefaultMutableTreeNode root=new DefaultMutableTreeNode();

		buildTreeDataVariables(root,filterUpper);
		ExpressionBuilderBasics.build(root,pathsToOpen,filterUpper);
		ExpressionBuilderDistributions.build(root,pathsToOpen,filterUpper);
		ExpressionBuilderQueueingTheory.build(root,pathsToOpen,filterUpper);
		if (!noSimulator) {
			ExpressionBuilderSimulationData.build(root,pathsToOpen,statisticsOnly,hasClientData,filterUpper);
			buildTreeDataStationIDs(root,filterUpper);
			buildTreeDataGlobalMapKeys(root,filterUpper);
		}
		if (isCompare) ExpressionBuilderCompare.build(root,pathsToOpen,filterUpper);

		return root;
	}

	/**
	 * HTML-Kopfbereich für die Ausgabe von Beschreibungen für ein Symbol
	 * @see #htmlFooter
	 * @see #selectTreeNode(ExpressionSymbol)
	 */
	private static final String htmlHeader="<html><body style=\"font-family: Verdana, Lucida, sans-serif; background-color: #FFFFF3; padding: 5px;\">";

	/**
	 * HTML-Fußbereich für die Ausgabe von Beschreibungen für ein Symbol
	 * @see #htmlHeader
	 * @see #selectTreeNode(ExpressionSymbol)
	 */
	private static final String htmlFooter="</body></html>";

	/**
	 * Zeigt die Beschreibung zu einem Symbol an
	 * @param symbol	Symbol zu dem die Beschreibung angezeigt werden soll
	 */
	private void selectTreeNode(final ExpressionSymbol symbol) {
		info.setContentType("text/html");
		if (symbol==null) {
			info.setText(htmlHeader+htmlFooter);
		} else {
			String title=symbol.toString();
			title=title.replaceAll("<","&lt;");
			title=title.replaceAll(">","&gt;");
			info.setText(htmlHeader+"<h1 style=\"font-size: larger; margin: 0; padding: 2px;\">"+title+"</h1>"+symbol.description+htmlFooter);
		}
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		final String expression=getExpression();
		int error=-1;
		if (isCompare) {
			error=ExpressionMultiEval.check(expression,variables);
		} else {
			error=ExpressionCalc.check(expression,variables);
		}

		if (error<0) {
			input.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			input.setBackground(Color.RED);
			if (showErrorMessage) MsgBox.error(this,Language.tr("ExpressionBuilder.Error.Title"),String.format("<html><body>"+Language.tr("ExpressionBuilder.Error"),expression,error+1));
		}

		return error<0;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert die Eingabe nach dem schließen des Dialogs zurück.
	 * @return	Neuer Ausdruck
	 */
	public String getExpression() {
		return input.getText().replaceAll("\\n"," ").trim();
	}

	/**
	 * Typ des Symbols
	 * @author Alexander Herzog
	 * @see ExpressionSymbol#type
	 */
	public enum ExpressionSymbolType {
		/**
		 * Konstante (pi, e, ...)
		 */
		TYPE_CONST,

		/**
		 * Variable
		 */
		TYPE_VARIABLE,

		/**
		 * Allgemeine Funktion
		 */
		TYPE_FUNCTION,

		/**
		 * Verteilungsfunktion (Dichte, Verteilung, Erzeugung einer Zufallsvariable)
		 */
		TYPE_DISTRIBUTION,

		/**
		 * Funktion zur Abfrage von allgemeinen Simulationsdaten
		 */
		TYPE_SIMDATA,

		/**
		 * Abfrage der ID einer Station basierend auf ihrem Namen
		 */
		TYPE_STATION_ID,

		/**
		 * Abfrage von Daten aus der globalen Zuordnung im Scripting-System
		 */
		TYPE_GLOBAL_MAP,

		/**
		 * Funktion zur Abfrage von kundenspezifischen Simulationsdaten
		 */
		TYPE_CLIENTDATA
	}

	/**
	 * Diese Klasse hält die Daten für ein Berechnungssymbol für die Anzeige in der Baumstruktur
	 * des <code>ExpressionBuilder</code>-Dialogs vor. Außerhalb des Dialogs und der zugehörigen
	 * Klasse zur Anzeige der Baumstruktur (<code>ExpressionBuilderTreeCellRenderer</code>) muss
	 * nicht weiter auf diese Klasse zugegriffen werden.
	 * @author Alexander Herzog
	 */
	public static class ExpressionSymbol {
		/** Name des Symbols (zur Anzeige in der Baumstruktur) */
		private final String name;

		/**
		 * Darzustellender Beispieltext für das Symbol
		 */
		public final String symbol;

		/**
		 * Beschreibung zur Anzeige im Expression-Builder für dieses Symbol
		 */
		public final String description;

		/**
		 * Typ des Symbols
		 */
		public final ExpressionSymbolType type;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name des Symbols (zur Anzeige in der Baumstruktur)
		 * @param symbol	Darzustellender Beispieltext für das Symbol
		 * @param description	Beschreibung zur Anzeige im Expression-Builder für dieses Symbol
		 * @param type	Typ des Symbols
		 */
		public ExpressionSymbol(final String name, final String symbol, final String description, final ExpressionSymbolType type) {
			this.name=name;
			this.symbol=symbol;
			this.description=description;
			this.type=type;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Reagiert auf Mausklicks auf Einträge in {@link ExpressionBuilder#tree} und fügt
	 * diese bei einem Doppelklick in die Ausgabe {@link ExpressionBuilder#input} ein.
	 * @see ExpressionBuilder#tree
	 * @see ExpressionBuilder#input
	 */
	private class TreeMouseListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
				if (tree.getSelectionPath()!=null && tree.getSelectionPath().getLastPathComponent()!=null && tree.getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
					final DefaultMutableTreeNode node=(DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
					if (node.getUserObject() instanceof ExpressionSymbol) {
						final ExpressionSymbol symbol=(ExpressionSymbol)node.getUserObject();
						selectTreeNode(symbol);
						final String text=input.getText();
						String part1=text.substring(0,input.getSelectionStart());
						String part2=(input.getSelectionEnd()==text.length())?"":text.substring(input.getSelectionEnd());
						input.setText(part1+symbol.symbol+part2);
						input.requestFocusInWindow();
						input.setSelectionStart(part1.length());
						input.setSelectionEnd((part1+symbol.symbol).length());
						checkData(false);
					}
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}
	}

	/**
	 * Fügt die Stationen mit ihren IDs von der Zeichenfläche in eine Zuordnung ein.
	 * @param map	Zuordnung von Stations-IDs zu Stationsnamen
	 * @param surface	Zeichenflächen-Element, welches die Stationsdaten enthält
	 * @see #getStationIDs(ModelSurface)
	 */
	private static void addStationIDs(final Map<Integer,String> map, final ModelSurface surface) {
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementSub) {addStationIDs(map,((ModelElementSub)element).getSubSurface()); continue;}
			if (element instanceof ModelElementBox) {
				String name=element.getName();
				if (name.isEmpty()) name=Language.tr("ExpressionBuilder.ElementNoName");
				map.put(element.getId(),element.getContextMenuElementName()+" ("+name+")");
			}
		}
	}

	/**
	 * Erstellt ein Zuordnung von Stations-IDs und Stationsbeschreibungen für die
	 * Darstellung der Stations-IDs in der Liste der verfügbaren Symbole im
	 * <code>ExpressionBuilder</code>-Dialog.
	 * @param surface	Zeichenflächen-Element, welches die Stationsdaten enthält
	 * @return	Zuordnung von Stations-IDs zu Stationsbeschreibungen
	 */
	public static Map<Integer,String> getStationIDs(final ModelSurface surface) {
		final Map<Integer,String> map=new HashMap<>();
		if (surface!=null) {
			if (surface.getParentSurface()!=null) {
				addStationIDs(map,surface.getParentSurface());
			} else {
				addStationIDs(map,surface);
			}
		}
		return map;
	}

	/**
	 * Fügt die Stationenbeschreibungen mit ihren IDs von der Zeichenfläche in eine Zuordnung ein.
	 * @param map	Zuordnung von Stations-IDs zu Stationsbeschreibungen
	 * @param surface	Zeichenflächen-Element, welches die Stationsdaten enthält
	 * @see #getStationIDs(ModelSurface)
	 */
	private static void addStationNameIDs(final Map<Integer,String> map, final ModelSurface surface) {
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementSub) {addStationNameIDs(map,((ModelElementSub)element).getSubSurface()); continue;}
			if (element instanceof ModelElementBox) {
				String name=element.getName();
				if (name.trim().isEmpty()) continue;
				map.put(element.getId(),name);
			}
		}
	}

	/**
	 * Erstellt ein Zuordnung von Stations-IDs und Stationsname für die
	 * Darstellung der Stations-IDs in der Liste der verfügbaren Symbole im
	 * <code>ExpressionBuilder</code>-Dialog.
	 * @param surface	Zeichenflächen-Element, welches die Stationsdaten enthält
	 * @return	Zuordnung von Stations-IDs zu Stationsnamen
	 */
	public static Map<Integer,String> getStationNameIDs(final ModelSurface surface) {
		final Map<Integer,String> map=new HashMap<>();
		if (surface!=null) {
			if (surface.getParentSurface()!=null) addStationNameIDs(map,surface.getParentSurface()); else addStationNameIDs(map,surface);
		}
		return map;
	}
}