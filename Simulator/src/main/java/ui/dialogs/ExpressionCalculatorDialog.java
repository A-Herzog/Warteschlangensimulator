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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropperData;
import scripting.java.DynamicFactory;
import scripting.js.JSRunDataFilterTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelAnimationImages;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElementAnimationEditClientDialog;
import ui.modeleditor.coreelements.ModelElementAnimationInfoClientDialog;
import ui.modeleditor.coreelements.ModelElementAnimationInfoDialog.ClientInfo;
import ui.modeleditor.coreelements.ModelElementAnimationInfoDialog.JClientInfoRender;
import ui.script.ScriptEditorAreaBuilder;
import ui.script.ScriptPopup;
import ui.statistics.StatisticTools;
import ui.tools.WindowSizeStorage;

/**
 * Dieser Dialog erlaubt die Berechnung von Ausdrücken aus einer
 * laufenden Animation oder ähnlichem heraus.
 * @author Alexander Herzog
 */
public final class ExpressionCalculatorDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2213485790093666048L;

	/**
	 * Startwert für das Javascript-Eingabefeld, wenn kein nutzerdefinierter Startwert übergeben wurde
	 */
	private static final String DEFAULT_JAVA_SCRIPT="Output.println(\"\");";

	/**
	 * Startwert für das Java-Eingabefeld, wenn kein nutzerdefinierter Startwert übergeben wurde
	 */
	private static final String DEFAULT_JAVA="void function(SimulationInterface sim) {\n  sim.getOutput().println(\"\");\n}\n";

	/**
	 * Editor-Modell als Information für den Expression-Builder
	 */
	private final EditModel model;

	/**
	 * Laufzeitmodell
	 */
	private final RunModel runModel;

	/**
	 * Funktion, die die Berechnungen erlaubt
	 */
	private final Function<String,Double> calc;

	/**
	 * Funktion, die die Javascript-Ausführungen erlaubt
	 */
	private final Function<String,String> runJavaScript;

	/**
	 * Funktion, die die Java-Ausführungen erlaubt
	 */
	private final Function<String,String> runJava;

	/**
	 * Registerreiter: Ausdruck auswerten, Javascript ausführen, Java ausführen
	 */
	private final JTabbedPane tabs;

	/**
	 * Eingabefeld für den zu berechnenden Ausdruck
	 */
	private JTextField expressionEdit;

	/**
	 * Ergebnis der Berechnung von {@link #expressionEdit}
	 */
	private JTextField resultsEdit;

	/**
	 * Tabellendaten für die Variablen-Tabelle
	 */
	private ExpressionCalculatorDialogVariablesTableModel variablesTableModel;

	/** Liste mit an der Station befindlichen Kunden abrufen (kann <code>null</code> sein) */
	private final Supplier<List<ClientInfo>> clientInfo;

	/** Ermöglicht das Abrufen eines tatsächlichen Kunden-Objekts (kann <code>null</code> sein, wenn kein Schreibzugriff auf die realen Kunden möglich ist) */
	private final Function<Long,RunDataClient> getRealClient;

	/** Liste der Kunden im System */
	private JList<ClientInfo> clientsList;

	/** Info zur Liste der Kunden im System */
	private JLabel clientsListInfo;

	/** "Neu"-Schaltfläche für den Javascript-Code */
	private JButton buttonJavaScriptNew;
	/** "Laden"-Schaltfläche für den Javascript-Code */
	private JButton buttonJavaScriptLoad;
	/** "Speichern"-Schaltfläche für den Javascript-Code */
	private JButton buttonJavaScriptSave;
	/** "Tools"-Schaltfläche für den Javascript-Code */
	private JButton buttonJavaScriptTools;
	/** "Ausführen"-Schaltfläche für den Javascript-Code */
	private JButton buttonJavaScriptRun;
	/** "Hilfe"-Schaltfläche für den Javascript-Code */
	private JButton buttonJavaScriptHelp;
	/** Eingabefeld für den Javascript-Code */
	private RSyntaxTextArea scriptJavaScriptEdit;
	/** Ausgabe der Ergebnisse der Ausführung des Javascript-Codes aus {@link #scriptJavaScriptEdit} */
	private JTextArea scriptJavaScriptResults;
	/** Zuletzt geladener/gespeicherter Javascript-Code (zur Prüfung, ob die aktuellen Eingaben ohne Warnung verworfen werden dürfen) */
	private String lastJavaScript="";

	/** "Neu"-Schaltfläche für den Java-Code */
	private JButton buttonJavaNew;
	/** "Laden"-Schaltfläche für den Java-Code */
	private JButton buttonJavaLoad;
	/** "Speichern"-Schaltfläche für den Java-Code */
	private JButton buttonJavaSave;
	/** "Tools"-Schaltfläche für den Java-Code */
	private JButton buttonJavaTools;
	/** "Ausführen"-Schaltfläche für den Java-Code */
	private JButton buttonJavaRun;
	/** "Hilfe"-Schaltfläche für den Java-Code */
	private JButton buttonJavaHelp;
	/** Eingabefeld für den Java-Code */
	private RSyntaxTextArea scriptJavaEdit;
	/** Ausgabe der Ergebnisse der Ausführung des Java-Codes aus {@link #scriptJavaEdit} */
	private JTextArea scriptJavaResults;
	/** Zuletzt geladener/gespeicherter Java-Code (zur Prüfung, ob die aktuellen Eingaben ohne Warnung verworfen werden dürfen) */
	private String lastJava="";

	/**
	 * Tabellendaten für die Zuordnungs-Tabelle
	 */
	private ExpressionCalculatorDialogTableModel mapTableModel;

	/** Benutzerdefinierte Animationsicons */
	private final ModelAnimationImages modelImages;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Editor-Modell als Information für den Expression-Builder
	 * @param runModel	Laufzeitmodell
	 * @param variableNames	Namen der globalen Variablen (kann <code>null</code> sein)
	 * @param getVariable	Callback zum Abrufen eines Variablenwertes
	 * @param setVariable	Callback zum Einstellen eines Variablenwertes
	 * @param mapGlobal	Zuordnung der globalen Scripting-Werte (kann <code>null</code> sein)
	 * @param calc	Funktion, die die Berechnungen erlaubt
	 * @param clientInfo	Liste mit an der Station befindlichen Kunden abrufen (kann <code>null</code> sein)
	 * @param getRealClient	Ermöglicht das Abrufen eines tatsächlichen Kunden-Objekts (kann <code>null</code> sein, wenn kein Schreibzugriff auf die realen Kunden möglich ist)
	 * @param runJavaScript	Funktion, die die Javascript-Ausführungen erlaubt
	 * @param runJava	Funktion, die die Java-Ausführungen erlaubt
	 * @param initialTab	Anfänglich anzuzeigenden Tab
	 * @param initialExpression	Startwert für das Eingabefeld
	 * @param initialJavaScript	Startwert für das Javascript-Eingabefeld
	 * @param initialJava	Startwert für das Java-Eingabefeld
	 * @see ExpressionCalculatorDialog#getLastExpression()
	 */
	public ExpressionCalculatorDialog(final Component owner, final EditModel model, final RunModel runModel, final String[] variableNames, final Function<String,Double> getVariable, final BiConsumer<String,Double> setVariable, final Map<String,Object> mapGlobal, final Function<String,Double> calc, final Supplier<List<ClientInfo>> clientInfo, final Function<Long,RunDataClient> getRealClient, final UnaryOperator<String> runJavaScript, final UnaryOperator<String> runJava, final int initialTab, final String initialExpression, final String initialJavaScript, final String initialJava) {
		super(owner,Language.tr("ExpressionCalculator.Title"));
		this.model=model;
		this.runModel=runModel;
		this.calc=calc;
		this.clientInfo=clientInfo;
		this.getRealClient=getRealClient;
		this.runJavaScript=runJavaScript;
		this.runJava=runJava;
		this.modelImages=model.animationImages;

		showCloseButton=true;
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		JPanel tab;
		int index=0;

		/* Daten */
		tabs.add(Language.tr("ExpressionCalculator.Tab.GeneralData"),tab=new JPanel(new BorderLayout()));
		tabs.setIconAt(index++,Images.MODEL.getIcon());
		buildGeneralDataTab(tab);

		/* Ausdruck berechnen */
		tabs.add(Language.tr("ExpressionCalculator.Tab.Expression"),tab=new JPanel(new BorderLayout()));
		tabs.setIconAt(index++,Images.SCRIPT_MODE_EXPRESSION.getIcon());
		buildExpressionCalculatorTab(tab,initialExpression);

		/* Variablen */
		if (variableNames!=null && variableNames.length>0) {
			tabs.add(Language.tr("ExpressionCalculator.Tab.Variables"),tab=new JPanel(new BorderLayout()));
			tabs.setIconAt(index++,Images.EXPRESSION_BUILDER_VARIABLE.getIcon());
			buildVariablesTab(tab,variableNames,getVariable,setVariable);
		}

		/* Kunden */
		tabs.add(Language.tr("ExpressionCalculator.Tab.Clients"),tab=new JPanel(new BorderLayout()));
		tabs.setIconAt(index++,Images.MODELPROPERTIES_CLIENTS.getIcon());
		buildClientsTab(tab);

		/* Javascript */
		tabs.add(Language.tr("ExpressionCalculator.Tab.Javascript"),tab=new JPanel(new BorderLayout()));
		tabs.setIconAt(index++,Images.SCRIPT_MODE_JAVASCRIPT.getIcon());
		buildJavascriptTab(tab,initialJavaScript);

		/* if (DynamicClass.isWindows()) { - brauchen wir nicht mehr bei fully intern */
		if (DynamicFactory.isWindows() || DynamicFactory.isInMemoryProcessing()) {
			/* Java */
			tabs.add(Language.tr("ExpressionCalculator.Tab.Java"),tab=new JPanel(new BorderLayout()));
			tabs.setIconAt(index++,Images.SCRIPT_MODE_JAVA.getIcon());
			buildJavaTab(tab,initialJava);
		}

		/* Zuordnung */
		if (mapGlobal!=null) {
			tabs.add(Language.tr("ExpressionCalculator.Tab.Map"),tab=new JPanel(new BorderLayout()));
			tabs.setIconAt(index++,Images.SCRIPT_MAP.getIcon());
			buildGlobalMapTab(tab,mapGlobal);
		}

		/* Start */
		if (initialTab>=0 && initialTab<tabs.getTabCount()) tabs.setSelectedIndex(initialTab);
		recalc();
		setMinSizeRespectingScreensize(850,400);
		pack();
		final Dimension size=getSize();
		setSize(size);
		setMinimumSize(size);
		setResizable(true);
		setLocationRelativeTo(this.owner);
		WindowSizeStorage.window(this,"AnimationExpressionCalculator");
	}

	/**
	 * Fügt eine Schaltfläche zu einer Symbolleiste hinzu
	 * @param toolbar	Symbolleiste zu der die Schaltfläche hinzugefügt werden soll
	 * @param title	Beschriftung der Schaltfläche
	 * @param icon	Icon für die Schaltfläche (kann <code>null</code> sein)
	 * @param hint	Tooltip für die Schaltfläche (kann <code>null</code> sein)
	 * @return	Bereits hinzugefügte Schaltfläche
	 */
	private JButton addToolbarButton(final JToolBar toolbar, final String title, final Icon icon, final String hint) {
		final JButton button=new JButton(title);
		toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		if (icon!=null) button.setIcon(icon);
		button.addActionListener(new ButtonListener());
		return button;
	}

	/**
	 * Erstellt den "Globale Daten"-Tab
	 * @param tab	Tab-Panel
	 */
	private void buildGeneralDataTab(final JPanel tab) {
		final JPanel sub=new JPanel();
		tab.add(sub,BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		sub.setBorder(BorderFactory.createEmptyBorder(10,5,5,5));

		double d;

		final StringBuilder generalData=new StringBuilder();
		generalData.append("<html><body>");

		d=calc.apply("TNow()");
		generalData.append(Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentTime")+": <b>"+StatisticTools.formatNumber(d)+"</b> (<b>"+StatisticTools.formatExactTime(d)+"</b>)\n");
		generalData.append("<br>\n");
		generalData.append("<br>\n");

		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.CurrentNumberInSystem")+": <b>"+StatisticTools.formatNumber(calc.apply("NQ()"))+"</b>\n");
		generalData.append("("+Language.tr("Statistics.Average")+": <b>"+StatisticTools.formatNumber(calc.apply("NQ_avg()"))+"</b>)");
		generalData.append("<br>\n");
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.CurrentNumberInSystemWaiting")+": <b>"+StatisticTools.formatNumber(calc.apply("WIP()"))+"</b>\n");
		generalData.append("("+Language.tr("Statistics.Average")+": <b>"+StatisticTools.formatNumber(calc.apply("WIP_avg()"))+"</b>)");
		generalData.append("<br>\n");
		generalData.append("<br>\n");

		d=calc.apply("Wartezeit_avg()");
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.AverageWaitingTime")+": <b>"+StatisticTools.formatNumber(d)+"</b> (<b>"+StatisticTools.formatExactTime(d)+"</b>)\n");
		generalData.append("<br>\n");
		d=calc.apply("Transferzeit_avg()");
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.TransferWaitingTime")+": <b>"+StatisticTools.formatNumber(d)+"</b> (<b>"+StatisticTools.formatExactTime(d)+"</b>)\n");
		generalData.append("<br>\n");
		d=calc.apply("Bedienzeit_avg()");
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.ProcessWaitingTime")+": <b>"+StatisticTools.formatNumber(d)+"</b> (<b>"+StatisticTools.formatExactTime(d)+"</b>)\n");
		generalData.append("<br>\n");
		d=calc.apply("Verweilzeit_avg()");
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.ResidenceWaitingTime")+": <b>"+StatisticTools.formatNumber(d)+"</b> (<b>"+StatisticTools.formatExactTime(d)+"</b>)\n");
		generalData.append("<br>\n");
		generalData.append("<br>\n");

		d=calc.apply("isWarmUp()");
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.SystemInWarmUpPhase")+": <b>"+((d>0)?Language.tr("Dialog.Button.Yes"):Language.tr("Dialog.Button.No"))+"</b>\n");
		generalData.append("<br>\n");

		generalData.append("</body></html>");
		sub.add(new JLabel(generalData.toString()));
	}

	/**
	 * Erstellt den "Ausdruck berechnen"-Tab
	 * @param tab	Tab-Panel
	 * @param initialExpression	Startwert für das Eingabefeld
	 */
	private void buildExpressionCalculatorTab(final JPanel tab, final String initialExpression) {
		final JPanel sub=new JPanel();
		tab.add(sub,BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		Object[] data;
		JPanel line;

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ExpressionCalculator.Expression")+":",(initialExpression==null)?"":initialExpression);
		sub.add(line=(JPanel)data[0]);
		expressionEdit=(JTextField)data[1];
		expressionEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {recalc();}
			@Override public void keyReleased(KeyEvent e) {recalc();}
			@Override public void keyPressed(KeyEvent e) {recalc();}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,expressionEdit,false,false,model,model.surface),BorderLayout.EAST);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ExpressionCalculator.Results")+":","");
		sub.add(line=(JPanel)data[0]);
		resultsEdit=(JTextField)data[1];
		resultsEdit.setEditable(false);

		final JButton button=new JButton("");
		button.setIcon(Images.EDIT_COPY.getIcon());
		button.setToolTipText(Language.tr("ExpressionCalculator.Results.Copy"));
		final Dimension size=button.getPreferredSize();
		button.setPreferredSize(new Dimension(size.height,size.height));
		button.addActionListener(e->copyResultToClipboard());
		line.add(button,BorderLayout.EAST);
	}

	/**
	 * Erstellt den "Variablen"-Tab
	 * @param tab	Tab-Panel
	 * @param variableNames	Namen der globalen Variablen (kann <code>null</code> sein)
	 * @param getVariable	Callback zum Abrufen eines Variablenwertes
	 * @param setVariable	Callback zum Einstellen eines Variablenwertes
	 */
	private void buildVariablesTab(final JPanel tab, final String[] variableNames, final Function<String,Double> getVariable, final BiConsumer<String,Double> setVariable) {
		final JTableExt variablesTable=new JTableExt();
		variablesTable.setModel(variablesTableModel=new ExpressionCalculatorDialogVariablesTableModel(variablesTable,variableNames,getVariable,setVariable));
		variablesTable.setIsPanelCellTable(1);
		tab.add(new JScrollPane(variablesTable),BorderLayout.CENTER);
	}

	/**
	 * Erstellt den "Kunden"-Tab
	 * @param tab	Tab-Panel
	 */
	private void buildClientsTab(final JPanel tab) {
		clientsList=new JList<>(new DefaultListModel<ClientInfo>());
		tab.add(new JScrollPane(clientsList),BorderLayout.CENTER);
		clientsList.setCellRenderer(new JClientInfoRender(new AnimationImageSource(),false));
		clientsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
					if (getRealClient!=null && e.getModifiersEx()==InputEvent.CTRL_DOWN_MASK) {
						commandEditClientData();
					} else {
						commandShowClientData();
					}
				}
			}
		});
		clientsList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER) {
					if (getRealClient!=null && e.getModifiersEx()==InputEvent.CTRL_DOWN_MASK) {
						commandEditClientData();
					} else {
						commandShowClientData();
					}
					e.consume();
					return;
				}
			}
		});
		clientsList.setPrototypeCellValue(new ClientInfo(null,null,new RunDataClient(0,false,0)));
		final JPanel line;
		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		line.add(clientsListInfo=new JLabel());

		commandUpdateClientList();
	}

	/**
	 * Erstellt den "Javascript"-Tab
	 * @param tab	Tab-Panel
	 * @param initialJavaScript	Startwert für das Javascript-Eingabefeld
	 */
	private void buildJavascriptTab(JPanel tab, final String initialJavaScript) {
		final JSplitPane split=new JSplitPane();
		tab.add(split);
		split.setOrientation(JSplitPane.VERTICAL_SPLIT);
		final JPanel sub=new JPanel(new BorderLayout());
		split.setTopComponent(sub);

		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		buttonJavaScriptNew=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.New"),Images.SCRIPT_NEW.getIcon(),Language.tr("ExpressionCalculator.Toolbar.New.Hint"));
		buttonJavaScriptLoad=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.Load"),Images.SCRIPT_LOAD.getIcon(),Language.tr("ExpressionCalculator.Toolbar.Load.Hint"));
		buttonJavaScriptSave=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.Save"),Images.SCRIPT_SAVE.getIcon(),Language.tr("ExpressionCalculator.Toolbar.Save.Hint"));
		toolbar.addSeparator();
		buttonJavaScriptTools=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.Tools"),Images.SCRIPT_TOOLS.getIcon(),Language.tr("ExpressionCalculator.Toolbar.Tools.Hint"));
		toolbar.addSeparator();
		buttonJavaScriptRun=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.Run"),Images.SCRIPT_RUN.getIcon(),Language.tr("ExpressionCalculator.Toolbar.Run.Hint"));
		toolbar.addSeparator();
		buttonJavaScriptHelp=addToolbarButton(toolbar,Language.tr("Main.Toolbar.Help"),Images.HELP.getIcon(),Language.tr("ExpressionCalculator.Toolbar.Help.Hint"));
		sub.add(toolbar,BorderLayout.NORTH);

		final ScriptEditorAreaBuilder builder=new ScriptEditorAreaBuilder(ScriptPopup.ScriptMode.Javascript);
		builder.addFileDropper(new ButtonListener());
		builder.addAutoCompleteFeature(ScriptPopup.ScriptFeature.Simulation);
		builder.addAutoCompleteFeature(ScriptPopup.ScriptFeature.Output);
		builder.setText((initialJavaScript==null || initialJavaScript.isEmpty())?DEFAULT_JAVA_SCRIPT:initialJavaScript);
		final RTextScrollPane scrollJavaScript;
		sub.add(scrollJavaScript=new RTextScrollPane(scriptJavaScriptEdit=builder.get()),BorderLayout.CENTER);
		scrollJavaScript.setLineNumbersEnabled(true);
		lastJavaScript=scriptJavaScriptEdit.getText();

		split.setBottomComponent(tab=new JPanel(new BorderLayout()));
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		tab.add(line,BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("ExpressionCalculator.Results")+":"));
		tab.add(new JScrollPane(scriptJavaScriptResults=new JTextArea("")),BorderLayout.CENTER);
		scriptJavaScriptResults.setEditable(false);

		split.setDividerLocation(0.66);
		split.setResizeWeight(0.75);
	}

	/**
	 * Erstellt den "Java"-Tab
	 * @param tab	Tab-Panel
	 * @param initialJava	Startwert für das Java-Eingabefeld
	 */
	private void buildJavaTab(JPanel tab, final String initialJava) {
		final JSplitPane split=new JSplitPane();
		tab.add(split);
		split.setOrientation(JSplitPane.VERTICAL_SPLIT);
		final JPanel sub=new JPanel(new BorderLayout());
		split.setTopComponent(sub);

		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		buttonJavaNew=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.NewJava"),Images.SCRIPT_NEW.getIcon(),Language.tr("ExpressionCalculator.Toolbar.NewJava.Hint"));
		buttonJavaLoad=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.LoadJava"),Images.SCRIPT_LOAD.getIcon(),Language.tr("ExpressionCalculator.Toolbar.LoadJava.Hint"));
		buttonJavaSave=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.SaveJava"),Images.SCRIPT_SAVE.getIcon(),Language.tr("ExpressionCalculator.Toolbar.SaveJava.Hint"));
		toolbar.addSeparator();
		buttonJavaTools=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.ToolsJava"),Images.SCRIPT_TOOLS.getIcon(),Language.tr("ExpressionCalculator.Toolbar.ToolsJava.Hint"));
		toolbar.addSeparator();
		buttonJavaRun=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.RunJava"),Images.SCRIPT_RUN.getIcon(),Language.tr("ExpressionCalculator.Toolbar.RunJava.Hint"));
		toolbar.addSeparator();
		buttonJavaHelp=addToolbarButton(toolbar,Language.tr("Main.Toolbar.Help"),Images.HELP.getIcon(),Language.tr("ExpressionCalculator.Toolbar.Help.Hint"));
		sub.add(toolbar,BorderLayout.NORTH);

		final ScriptEditorAreaBuilder builder=new ScriptEditorAreaBuilder(ScriptPopup.ScriptMode.Java);
		builder.addAutoCompleteFeature(ScriptPopup.ScriptFeature.Simulation);
		builder.addAutoCompleteFeature(ScriptPopup.ScriptFeature.Output);
		builder.addFileDropper(new ButtonListener());
		builder.setText((initialJava==null || initialJava.isEmpty())?DEFAULT_JAVA:initialJava);
		final RTextScrollPane scrollJava;
		sub.add(scrollJava=new RTextScrollPane(scriptJavaEdit=builder.get()),BorderLayout.CENTER);
		scrollJava.setLineNumbersEnabled(true);
		lastJava=scriptJavaEdit.getText();

		split.setBottomComponent(tab=new JPanel(new BorderLayout()));
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		tab.add(line,BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("ExpressionCalculator.Results")+":"));
		tab.add(new JScrollPane(scriptJavaResults=new JTextArea("")),BorderLayout.CENTER);
		scriptJavaResults.setEditable(false);

		split.setDividerLocation(0.66);
		split.setResizeWeight(0.75);
	}

	/**
	 * Erstellt den "Globale Zuordnung"-Tab
	 * @param tab	Tab-Panel
	 * @param mapGlobal	Zuordnung der globalen Scripting-Werte (kann <code>null</code> sein)
	 */
	private void buildGlobalMapTab(final JPanel tab, final Map<String,Object> mapGlobal) {
		final JTableExt mapTable=new JTableExt();
		mapTable.setModel(mapTableModel=new ExpressionCalculatorDialogTableModel(mapTable,mapGlobal));
		mapTable.getColumnModel().getColumn(0).setMaxWidth(125);
		mapTable.getColumnModel().getColumn(0).setMinWidth(125);
		mapTable.getColumnModel().getColumn(2).setMinWidth(150);
		mapTable.getColumnModel().getColumn(2).setMaxWidth(150);
		mapTable.getColumnModel().getColumn(3).setMinWidth(100);
		mapTable.getColumnModel().getColumn(3).setMaxWidth(100);
		mapTable.setIsPanelCellTable(3);
		tab.add(new JScrollPane(mapTable),BorderLayout.CENTER);
	}

	/**
	 * Aktualisiert die Berechnung.
	 * @see #expressionEdit
	 */
	private void recalc() {
		final String expression=expressionEdit.getText().trim();

		if (expression.isEmpty()) {
			resultsEdit.setText(Language.tr("ExpressionCalculator.Results.NoExpression"));
			return;
		}

		final Double D=calc.apply(expression);
		if (D==null) {
			resultsEdit.setText(Language.tr("ExpressionCalculator.Results.NoResult"));
			return;
		}

		resultsEdit.setText(NumberTools.formatNumberMax(D.doubleValue()));
	}

	/**
	 * Kopiert das aktuelle Ergebnis in die Zwischenablage.
	 */
	private void copyResultToClipboard() {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(resultsEdit.getText()),null);
	}

	/**
	 * Liefert die 0-basierende Nummer des zuletzt angezeigten Tabs
	 * (z.B. zur Wiederverwendung beim nächsten Öffnen des Dialogs).
	 * @return	0-basierende Nummer des zuletzt angezeigten Tabs
	 */
	public int getLastMode() {
		return tabs.getSelectedIndex();
	}

	/**
	 * Liefert den zuletzt in das Eingabefeld eingegebenen Ausdruck
	 * (z.B. zur Wiederverwendung beim nächsten Öffnen des Dialogs).
	 * @return	Letzter eingegebener Ausdruck
	 */
	public String getLastExpression() {
		return expressionEdit.getText().trim();
	}

	/**
	 * Liefert das zuletzt in das JavaSkript-Eingabefeld eingegebene Skript
	 * (z.B. zur Wiederverwendung beim nächsten Öffnen des Dialogs).
	 * @return	Letztes eingegebenes JavaSkript
	 */
	public String getLastJavaScript() {
		return scriptJavaScriptEdit.getText().trim();
	}

	/**
	 * Liefert das zuletzt in das Java-Eingabefeld eingegebene Skript
	 * (z.B. zur Wiederverwendung beim nächsten Öffnen des Dialogs).
	 * @return	Letztes eingegebener Java-Code
	 */
	public String getLastJava() {
		return scriptJavaEdit.getText().trim();
	}

	/**
	 * Befehl: Liste der Kunden aktualisieren
	 */
	private void commandUpdateClientList() {
		if (clientInfo==null) return;

		final int index=clientsList.getSelectedIndex();

		final DefaultListModel<ClientInfo> model=new DefaultListModel<>();
		new Thread(()->{
			final List<ClientInfo> list=clientInfo.get();
			if (list!=null) list.forEach(model::addElement);
			SwingUtilities.invokeLater(()->{
				clientsList.setModel(model);
				if (index>=0 && index<model.size()) clientsList.setSelectedIndex(index);

				final int size=model.size();
				clientsListInfo.setText(String.format((size==1)?Language.tr("ExpressionCalculator.Tab.Clients.Info.Singular"):Language.tr("ExpressionCalculator.Tab.Clients.Info.Plural"),size));

			});
		},"ProcessAllClientsList").start();
	}

	/**
	 * Befehl: Daten zu dem gewählten Kunden anzeigen
	 */
	private void commandShowClientData() {
		final int index=clientsList.getSelectedIndex();
		if (index<0) return;

		final ClientInfo clientInfo=clientsList.getModel().getElementAt(index);
		if (clientInfo==null) return;

		final ModelElementAnimationInfoClientDialog infoDialog=new ModelElementAnimationInfoClientDialog(this,runModel,clientInfo,false,getRealClient!=null);
		if (!infoDialog.getShowEditorDialog()) return;

		commandEditClientData();
	}

	/**
	 * Befehl: Editor zu dem gewähltenKunden anzeigen
	 */
	private void commandEditClientData() {
		final int index=clientsList.getSelectedIndex();
		if (index<0) return;

		final ClientInfo clientInfo=clientsList.getModel().getElementAt(index);
		if (clientInfo==null) return;

		final RunDataClient client=getRealClient.apply(clientInfo.number);
		if (client==null) {
			MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.EditClient"),Language.tr("Surface.PopupMenu.SimulationStatisticsData.EditClient.Error"));
			return;
		}

		final ModelElementAnimationEditClientDialog editDialog=new ModelElementAnimationEditClientDialog(this,modelImages,runModel,client);
		if (editDialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			commandUpdateClientList();
		}
	}

	/**
	 * Darf der Javascript-Code verworfen werden?
	 * @return	Liefert <code>true</code>, wenn der Javascript-Code verworfen werden kann
	 */
	private boolean allowDiscardJavaScript() {
		if (lastJavaScript.equals(scriptJavaScriptEdit.getText())) return true;
		switch (MsgBox.confirmSave(this,Language.tr("ExpressionCalculator.DiscardConfirmationJavascript.Title"),Language.tr("ExpressionCalculator.DiscardConfirmationJavascript.Info"))) {
		case JOptionPane.YES_OPTION: commandSaveJavaScript(); return allowDiscardJavaScript();
		case JOptionPane.NO_OPTION: return true;
		case JOptionPane.CANCEL_OPTION: return false;
		default: return false;
		}
	}

	/**
	 * Darf der Java-Code verworfen werden?
	 * @return	Liefert <code>true</code>, wenn der Java-Code verworfen werden kann
	 */
	private boolean allowDiscardJava() {
		if (lastJava.equals(scriptJavaEdit.getText())) return true;
		switch (MsgBox.confirmSave(this,Language.tr("ExpressionCalculator.DiscardConfirmationJava.Title"),Language.tr("ExpressionCalculator.DiscardConfirmationJava.Info"))) {
		case JOptionPane.YES_OPTION: commandSaveJava(); return allowDiscardJava();
		case JOptionPane.NO_OPTION: return true;
		case JOptionPane.CANCEL_OPTION: return false;
		default: return false;
		}
	}

	/**
	 * Lädt den Javascript-Code aus einer Datei
	 * @param file	Zu ladende Datei; wird <code>null</code> angegeben, so wird ein Dateiauswahl-Dialog angezeigt
	 */
	private void commandLoadJavaScript(File file) {
		if (file==null) {
			JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			FileFilter filter;
			fc.setDialogTitle(Language.tr("FileType.Load.JS"));
			filter=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
			fc.addChoosableFileFilter(filter);
			fc.setFileFilter(filter);
			if (fc.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			file=fc.getSelectedFile();
			if (file.getName().indexOf('.')<0 && fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+".js");
		}

		final String text=JSRunDataFilterTools.loadText(file);
		if (text==null) return;
		scriptJavaScriptEdit.setText(text);
		lastJavaScript=scriptJavaScriptEdit.getText();
	}

	/**
	 * Lädt den Java-Code aus einer Datei
	 * @param file	Zu ladende Datei; wird <code>null</code> angegeben, so wird ein Dateiauswahl-Dialog angezeigt
	 */
	private void commandLoadJava(File file) {
		if (file==null) {
			JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			FileFilter filter;
			fc.setDialogTitle(Language.tr("FileType.Load.Java"));
			filter=new FileNameExtensionFilter(Language.tr("FileType.Java")+" (*.java)","java");
			fc.addChoosableFileFilter(filter);
			fc.setFileFilter(filter);
			if (fc.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			file=fc.getSelectedFile();
			if (file.getName().indexOf('.')<0 && fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+".java");
		}

		final String text=JSRunDataFilterTools.loadText(file);
		if (text==null) return;
		scriptJavaEdit.setText(text);
		lastJava=scriptJavaEdit.getText();
	}

	/**
	 * Speichert den Javascript-Code in einer Datei.
	 */
	private void commandSaveJavaScript() {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		FileFilter filter;
		fc.setDialogTitle(Language.tr("FileType.Save.JS"));
		filter=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+".js");
		}
		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		if (!JSRunDataFilterTools.saveText(scriptJavaScriptEdit.getText(),file,false)) return;
		lastJavaScript=scriptJavaScriptEdit.getText();
	}

	/**
	 * Speichert den Java-Code in einer Datei.
	 */
	private void commandSaveJava() {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		FileFilter filter;
		fc.setDialogTitle(Language.tr("FileType.Save.Java"));
		filter=new FileNameExtensionFilter(Language.tr("FileType.Java")+" (*.java)","java");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+".java");
		}
		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		if (!JSRunDataFilterTools.saveText(scriptJavaEdit.getText(),file,false)) return;
		lastJava=scriptJavaEdit.getText();
	}

	/**
	 * Führt den Javascript-Code aus.
	 */
	private void commandRunJavaScript() {
		final String s=runJavaScript.apply(scriptJavaScriptEdit.getText().trim());
		scriptJavaScriptResults.setText(s);

		recalc();
		if (variablesTableModel!=null) variablesTableModel.updateTable();
		if (mapTableModel!=null) mapTableModel.updateTable();
	}

	/**
	 * Führt den Java-Code aus.
	 */
	private void commandRunJava() {
		final String s=runJava.apply(scriptJavaEdit.getText().trim());
		scriptJavaResults.setText(s);

		recalc();
		if (variablesTableModel!=null) variablesTableModel.updateTable();
		if (mapTableModel!=null) mapTableModel.updateTable();
	}

	/**
	 * Reagiert auf Klicks auf die Symbolleisten-Schaltflächen
	 */
	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			if (e.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				final File file=data.getFile();
				if (file.isFile()) {
					if (data.getDropComponent()==scriptJavaScriptEdit) {
						if (allowDiscardJavaScript()) commandLoadJavaScript(file);
					}
					if (data.getDropComponent()==scriptJavaEdit) {
						if (allowDiscardJava()) commandLoadJava(file);
					}
					data.dragDropConsumed();
				}
				return;
			}

			/* Javascript */

			if (e.getSource()==buttonJavaScriptNew) {
				if (allowDiscardJavaScript()) {
					scriptJavaScriptEdit.setText("");
					lastJavaScript=scriptJavaScriptEdit.getText();
				}
				return;
			}

			if (e.getSource()==buttonJavaScriptLoad) {
				if (allowDiscardJavaScript()) commandLoadJavaScript(null);
				return;
			}

			if (e.getSource()==buttonJavaScriptSave) {
				commandSaveJavaScript();
				return;
			}

			if (e.getSource()==buttonJavaScriptRun) {
				commandRunJavaScript();
				return;
			}

			if (e.getSource()==buttonJavaScriptTools) {
				final ScriptPopup popup=new ScriptPopup(buttonJavaScriptTools,model,ScriptPopup.ScriptMode.Javascript,null);
				popup.addFeature(ScriptPopup.ScriptFeature.Simulation);
				popup.addFeature(ScriptPopup.ScriptFeature.Output);
				popup.build();
				popup.show(scriptJavaScriptEdit);
				return;
			}

			if (e.getSource()==buttonJavaScriptHelp) {
				Help.topicModal(ExpressionCalculatorDialog.this,"JS");
				return;
			}

			/* Java */

			if (e.getSource()==buttonJavaNew) {
				if (allowDiscardJava()) {
					scriptJavaEdit.setText("");
					lastJava=scriptJavaEdit.getText();
				}
				return;
			}

			if (e.getSource()==buttonJavaLoad) {
				if (allowDiscardJava()) commandLoadJava(null);
				return;
			}

			if (e.getSource()==buttonJavaSave) {
				commandSaveJava();
				return;
			}

			if (e.getSource()==buttonJavaTools) {
				final ScriptPopup popup=new ScriptPopup(buttonJavaTools,model,ScriptPopup.ScriptMode.Java,null);
				popup.addFeature(ScriptPopup.ScriptFeature.Simulation);
				popup.addFeature(ScriptPopup.ScriptFeature.Output);
				popup.build();
				popup.show(scriptJavaEdit);
				return;
			}

			if (e.getSource()==buttonJavaRun) {
				commandRunJava();
				return;
			}

			if (e.getSource()==buttonJavaHelp) {
				Help.topicModal(ExpressionCalculatorDialog.this,"Java");
				return;
			}
		}
	}
}