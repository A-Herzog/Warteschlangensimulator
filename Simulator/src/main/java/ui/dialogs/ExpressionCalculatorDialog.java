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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropperData;
import scripting.java.DynamicFactory;
import scripting.js.JSRunDataFilterTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
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
import ui.modeleditor.coreelements.ModelElementAnimationInfoDialog;
import ui.modeleditor.coreelements.ModelElementAnimationInfoDialog.ClientInfo;
import ui.modeleditor.coreelements.ModelElementAnimationInfoDialog.JClientInfoRender;
import ui.script.ScriptEditorAreaBuilder;
import ui.script.ScriptPopup;
import ui.script.ScriptTools;
import ui.statistics.StatisticTools;
import ui.tools.WindowSizeStorage;

/**
 * Dieser Dialog erlaubt die Berechnung von Ausdr�cken aus einer
 * laufenden Animation oder �hnlichem heraus.
 * @author Alexander Herzog
 */
public final class ExpressionCalculatorDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2213485790093666048L;

	/**
	 * Startwert f�r das Javascript-Eingabefeld, wenn kein nutzerdefinierter Startwert �bergeben wurde
	 */
	private static final String DEFAULT_JAVA_SCRIPT="Output.println(\"\");";

	/**
	 * Startwert f�r das Java-Eingabefeld, wenn kein nutzerdefinierter Startwert �bergeben wurde
	 */
	private static final String DEFAULT_JAVA="void function(SimulationInterface sim) {\n  sim.getOutput().println(\"\");\n}\n";

	/**
	 * Editor-Modell als Information f�r den Expression-Builder
	 */
	private final EditModel model;

	/**
	 * Simulationsdatenobjekt
	 */
	private final SimulationData simData;

	/**
	 * Funktion, die die Berechnungen erlaubt
	 */
	private final Function<String,Double> calc;

	/**
	 * Funktion, die die Javascript-Ausf�hrungen erlaubt
	 */
	private final Function<String,String> runJavaScript;

	/**
	 * Funktion, die die Java-Ausf�hrungen erlaubt
	 */
	private final Function<String,String> runJava;

	/**
	 * Timer f�r automatische Aktualisierungen
	 */
	private Timer timer;

	/**
	 * Schaltfl�che zum Umschalten zwischen automatischer und manueller Aktualisierung
	 */
	private JButton buttonAutoUpdate;

	/**
	 * Registerreiter: Ausdruck auswerten, Javascript ausf�hren, Java ausf�hren
	 */
	private final JTabbedPane tabs;

	/**
	 * Label zur Anzeige der allgemeinen Informationen
	 */
	private JLabel infoLabel;

	/**
	 * Eingabefeld f�r den zu berechnenden Ausdruck
	 */
	private JTextField expressionEdit;

	/**
	 * Ergebnis der Berechnung von {@link #expressionEdit}
	 */
	private JTextField resultsEdit;

	/**
	 * Tabellendaten f�r die Variablen-Tabelle
	 */
	private ExpressionCalculatorDialogVariablesTableModel variablesTableModel;

	/** Liste mit an der Station befindlichen Kunden abrufen (kann <code>null</code> sein) */
	private final Supplier<List<ClientInfo>> clientInfo;

	/** Erm�glicht das Abrufen eines tats�chlichen Kunden-Objekts (kann <code>null</code> sein, wenn kein Schreibzugriff auf die realen Kunden m�glich ist) */
	private final Function<Long,RunDataClient> getRealClient;

	/** Liste der Kunden im System */
	private JList<ClientInfo> clientsList;

	/** Info zur Liste der Kunden im System */
	private JLabel clientsListInfo;

	/** "Neu"-Schaltfl�che f�r den Javascript-Code */
	private JButton buttonJavaScriptNew;
	/** "Laden"-Schaltfl�che f�r den Javascript-Code */
	private JButton buttonJavaScriptLoad;
	/** "Speichern"-Schaltfl�che f�r den Javascript-Code */
	private JButton buttonJavaScriptSave;
	/** "Tools"-Schaltfl�che f�r den Javascript-Code */
	private JButton buttonJavaScriptTools;
	/** "Ausf�hren"-Schaltfl�che f�r den Javascript-Code */
	private JButton buttonJavaScriptRun;
	/** "Ergebnisse kopieren"-Schaltfl�che f�r den Javascript-Code */
	private JButton buttonJavaScriptCopyResults;
	/** "Hilfe"-Schaltfl�che f�r den Javascript-Code */
	private JButton buttonJavaScriptHelp;
	/** Eingabefeld f�r den Javascript-Code */
	private RSyntaxTextArea scriptJavaScriptEdit;
	/** Ausgabe der Ergebnisse der Ausf�hrung des Javascript-Codes aus {@link #scriptJavaScriptEdit} */
	private JTextArea scriptJavaScriptResults;
	/** Zuletzt geladener/gespeicherter Javascript-Code (zur Pr�fung, ob die aktuellen Eingaben ohne Warnung verworfen werden d�rfen) */
	private String lastJavaScript="";

	/** "Neu"-Schaltfl�che f�r den Java-Code */
	private JButton buttonJavaNew;
	/** "Laden"-Schaltfl�che f�r den Java-Code */
	private JButton buttonJavaLoad;
	/** "Speichern"-Schaltfl�che f�r den Java-Code */
	private JButton buttonJavaSave;
	/** "Tools"-Schaltfl�che f�r den Java-Code */
	private JButton buttonJavaTools;
	/** "Ausf�hren"-Schaltfl�che f�r den Java-Code */
	private JButton buttonJavaRun;
	/** "Ergebnisse kopieren"-Schaltfl�che f�r den Java-Code */
	private JButton buttonJavaCopyResults;
	/** "Hilfe"-Schaltfl�che f�r den Java-Code */
	private JButton buttonJavaHelp;
	/** Eingabefeld f�r den Java-Code */
	private RSyntaxTextArea scriptJavaEdit;
	/** Ausgabe der Ergebnisse der Ausf�hrung des Java-Codes aus {@link #scriptJavaEdit} */
	private JTextArea scriptJavaResults;
	/** Zuletzt geladener/gespeicherter Java-Code (zur Pr�fung, ob die aktuellen Eingaben ohne Warnung verworfen werden d�rfen) */
	private String lastJava="";

	/**
	 * Tabellendaten f�r die Zuordnungs-Tabelle
	 */
	private ExpressionCalculatorDialogTableModel mapTableModel;

	/** Benutzerdefinierte Animationsicons */
	private final ModelAnimationImages modelImages;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param model	Editor-Modell als Information f�r den Expression-Builder
	 * @param simData	Simulationsdatenobjekt
	 * @param variableNames	Namen der globalen Variablen (kann <code>null</code> sein)
	 * @param getVariable	Callback zum Abrufen eines Variablenwertes
	 * @param setVariable	Callback zum Einstellen eines Variablenwertes
	 * @param mapGlobal	Zuordnung der globalen Scripting-Werte (kann <code>null</code> sein)
	 * @param calc	Funktion, die die Berechnungen erlaubt
	 * @param clientInfo	Liste mit an der Station befindlichen Kunden abrufen (kann <code>null</code> sein)
	 * @param getRealClient	Erm�glicht das Abrufen eines tats�chlichen Kunden-Objekts (kann <code>null</code> sein, wenn kein Schreibzugriff auf die realen Kunden m�glich ist)
	 * @param runJavaScript	Funktion, die die Javascript-Ausf�hrungen erlaubt
	 * @param runJava	Funktion, die die Java-Ausf�hrungen erlaubt
	 * @param initialTab	Anf�nglich anzuzeigenden Tab
	 * @param initialExpression	Startwert f�r das Eingabefeld
	 * @param initialJavaScript	Startwert f�r das Javascript-Eingabefeld
	 * @param initialJava	Startwert f�r das Java-Eingabefeld
	 * @param readOnly	 Nur-Lese-Status
	 * @see ExpressionCalculatorDialog#getLastExpression()
	 */
	public ExpressionCalculatorDialog(final Component owner, final EditModel model, final SimulationData simData, final String[] variableNames, final Function<String,Double> getVariable, final BiConsumer<String,Double> setVariable, final Map<String,Object> mapGlobal, final Function<String,Double> calc, final Supplier<List<ClientInfo>> clientInfo, final Function<Long,RunDataClient> getRealClient, final UnaryOperator<String> runJavaScript, final UnaryOperator<String> runJava, final int initialTab, final String initialExpression, final String initialJavaScript, final String initialJava, final boolean readOnly) {
		super(owner,Language.tr("ExpressionCalculator.Title"));
		this.model=model;
		this.simData=simData;
		this.calc=calc;
		this.clientInfo=clientInfo;
		this.getRealClient=getRealClient;
		this.runJavaScript=runJavaScript;
		this.runJava=runJava;
		this.modelImages=model.animationImages;

		timer=null;

		showCloseButton=true;
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		if (readOnly) {
			/* Toolbar */
			final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
			toolbar.setFloatable(false);
			content.add(toolbar,BorderLayout.NORTH);
			addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Update"),Images.ANIMATION_DATA_UPDATE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateHint"),e->commandUpdate());
			buttonAutoUpdate=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdate"),Images.ANIMATION_DATA_UPDATE_AUTO.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdateHint"),e->commandAutoUpdate());
		}

		/* Tabs */
		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		JPanel tab;
		int index=0;

		/* Daten */
		tabs.add(Language.tr("ExpressionCalculator.Tab.GeneralData"),tab=new JPanel(new BorderLayout()));
		tabs.setIconAt(index++,Images.MODEL.getIcon());
		buildGeneralDataTab(tab);

		/* Ausdruck berechnen */
		if (calc!=null) {
			tabs.add(Language.tr("ExpressionCalculator.Tab.Expression"),tab=new JPanel(new BorderLayout()));
			tabs.setIconAt(index++,Images.SCRIPT_MODE_EXPRESSION.getIcon());
			buildExpressionCalculatorTab(tab,initialExpression);
		}

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
		if (runJavaScript!=null && !readOnly) {
			tabs.add(Language.tr("ExpressionCalculator.Tab.Javascript"),tab=new JPanel(new BorderLayout()));
			tabs.setIconAt(index++,Images.SCRIPT_MODE_JAVASCRIPT.getIcon());
			buildJavascriptTab(tab,initialJavaScript);
		}

		/* if (DynamicClass.isWindows()) { - brauchen wir nicht mehr bei fully intern */
		if (runJava!=null && !readOnly && (DynamicFactory.isWindows() || DynamicFactory.isInMemoryProcessing())) {
			/* Java */
			tabs.add(Language.tr("ExpressionCalculator.Tab.Java"),tab=new JPanel(new BorderLayout()));
			tabs.setIconAt(index++,Images.SCRIPT_MODE_JAVA.getIcon());
			buildJavaTab(tab,initialJava);
		}

		/* Zuordnung */
		if (mapGlobal!=null) {
			tabs.add(Language.tr("ExpressionCalculator.Tab.Map"),tab=new JPanel(new BorderLayout()));
			tabs.setIconAt(index++,Images.SCRIPT_MAP.getIcon());
			buildGlobalMapTab(tab,mapGlobal,readOnly);
		}

		/* Start */
		if (initialTab>=0 && initialTab<tabs.getTabCount()) tabs.setSelectedIndex(initialTab);
		recalc();
		setMinSizeRespectingScreensize(850+Math.max(tabs.getTabCount()-6,0)*100,400);
		pack();
		final Dimension size=getSize();
		setSize(size);
		setMinimumSize(size);
		setResizable(true);
		setLocationRelativeTo(this.owner);
		WindowSizeStorage.window(this,"AnimationExpressionCalculator");
	}

	/**
	 * Erstellt eine neue Schaltfl�che und f�gt sie zur Symbolleiste hinzu.
	 * @param toolbar	Symbolleiste auf der die neue Schaltfl�che eingef�gt werden soll
	 * @param name	Beschriftung der Schaltfl�che
	 * @param hint	Tooltip f�r die Schaltfl�che (darf <code>null</code> sein)
	 * @param icon	Optionales Icon f�r die Schaltfl�che (darf <code>null</code> sein)
	 * @param listener	Aktion die beim Anklicken der Schaltfl�che ausgef�hrt werden soll
	 * @return	Neue Schaltfl�che (ist bereits in die Symbolleiste eingef�gt)
	 */
	private JButton addButton(final JToolBar toolbar, final String name, final Icon icon, final String hint, final ActionListener listener) {
		final JButton button=new JButton(name);
		if (icon!=null) button.setIcon(icon);
		if (hint!=null && !hint.isBlank()) button.setToolTipText(hint);
		button.addActionListener(listener);
		toolbar.add(button);
		return button;
	}

	/**
	 * F�gt eine Schaltfl�che zu einer Symbolleiste hinzu
	 * @param toolbar	Symbolleiste zu der die Schaltfl�che hinzugef�gt werden soll
	 * @param title	Beschriftung der Schaltfl�che
	 * @param icon	Icon f�r die Schaltfl�che (kann <code>null</code> sein)
	 * @param hint	Tooltip f�r die Schaltfl�che (kann <code>null</code> sein)
	 * @return	Bereits hinzugef�gte Schaltfl�che
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
	 * Erzeugt eine Symbolleiste mit einer "Kopieren"- und einer "Speichern"-Schaltfl�che
	 * @param parent	�bergeordnetes Element, bei dem die Symbolleiste oben eingef�gt werden soll
	 * @param getData	Callback, welches den auszugebenden Text liefert
	 * @param isTable	Sollen die Daten als Text (<code>false</code>) oder als Tabelle (<code>true</code>) gespeichert werden?
	 */
	private void addCopySaveToolbar(final JPanel parent, final Supplier<String> getData, final boolean isTable) {
		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		parent.add(toolbar,BorderLayout.NORTH);

		JButton button;

		toolbar.add(button=new JButton(Language.tr("Dialog.Button.Copy")));
		button.setToolTipText(Language.tr("Dialog.Button.Copy.Info"));
		button.setIcon(Images.EDIT_COPY.getIcon());
		button.addActionListener(e->getToolkit().getSystemClipboard().setContents(new StringSelection(getData.get()),null));

		toolbar.add(button=new JButton(Language.tr("Dialog.Button.Save")));
		button.setIcon(Images.GENERAL_SAVE.getIcon());
		if (isTable) {
			button.setToolTipText(Language.tr("ExpressionCalculator.Tab.General.SaveTable"));
			button.addActionListener(e->{
				final File file=Table.showSaveDialog(this,Language.tr("ExpressionCalculator.Tab.General.SaveTable.Title"),null,null,null);
				if (file!=null) {
					final Table table=new Table();
					table.load(getData.get());
					table.save(file);
				}
			});
		} else {
			button.setToolTipText(Language.tr("ExpressionCalculator.Tab.General.SaveText"));
			button.addActionListener(e->{
				final String fileName=ScriptTools.selectTextSaveFile(this,Language.tr("ExpressionCalculator.Tab.General.SaveText.Info"),null);
				if (fileName!=null) Table.saveTextToFile(getData.get(),new File(fileName));
			});
		}
	}

	/**
	 * Erstellt den "Globale Daten"-Tab
	 * @param tab	Tab-Panel
	 */
	private void buildGeneralDataTab(final JPanel tab) {
		final JPanel sub=new JPanel();
		addCopySaveToolbar(tab,()->makeHTMLPlain(getGeneralData()),false);
		tab.add(sub,BorderLayout.CENTER);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		sub.setBorder(BorderFactory.createEmptyBorder(10,5,5,5));
		sub.add(infoLabel=new JLabel());
		commandUpdateGeneralData();
	}

	/**
	 * Liefert den anzuzeigenden Text f�r den "Globale Daten"-Tab.
	 * @return	HTML-formatierter Text f�r den "Globale Daten"-Tab
	 * @see #commandUpdateGeneralData()
	 */
	private String getGeneralData() {
		final StringBuilder generalData=new StringBuilder();

		generalData.append("<html><body>");

		final double time=simData.currentTime*simData.runModel.scaleToSeconds;
		generalData.append(Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentTime")+": <b>"+StatisticTools.formatNumberExt(time,false)+"</b> (<b>"+StatisticTools.formatExactTime(time)+"</b>)");
		generalData.append("<br>\n");

		final long clientsArrived=simData.runData.clientsArrived;
		generalData.append(Language.tr("ExpressionCalculator.Tab.General.ArrivalCount")+": <b>"+NumberTools.formatLong(clientsArrived)+"</b>\n");
		generalData.append("<br><br>\n");

		final int nq=(simData.runData.clientsInQueuesByType==null)?0:Arrays.stream(simData.runData.clientsInQueuesByType).sum();
		final double nq_avg=simData.statistics.clientsInSystemQueues.getTimeMean();
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.CurrentNumberInSystemWaiting")+": <b>"+NumberTools.formatLong(nq)+"</b> ");
		generalData.append("("+Language.tr("Statistics.Average")+": <b>"+StatisticTools.formatNumberExt(nq_avg,false)+"</b>)");
		generalData.append("<br>\n");

		final int ns=(simData.runData.clientsInProcessByType==null)?0:Arrays.stream(simData.runData.clientsInProcessByType).sum();
		final double ns_avg=simData.statistics.clientsInSystemProcess.getTimeMean();
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.CurrentNumberInSystemProcess")+": <b>"+NumberTools.formatLong(ns)+"</b> ");
		generalData.append("("+Language.tr("Statistics.Average")+": <b>"+StatisticTools.formatNumberExt(ns_avg,false)+"</b>)");
		generalData.append("<br>\n");

		final int wip=(simData.runData.clientsInSystemByType==null)?0:Arrays.stream(simData.runData.clientsInSystemByType).sum();
		final double wip_avg=simData.statistics.clientsInSystem.getTimeMean();
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.CurrentNumberInSystem")+": <b>"+NumberTools.formatLong(wip)+"</b> ");
		generalData.append("("+Language.tr("Statistics.Average")+": <b>"+StatisticTools.formatNumberExt(wip_avg,false)+"</b>)");
		generalData.append("<br>\n");

		generalData.append("<br>\n");

		final double W_avg=simData.statistics.clientsAllWaitingTimes.getMean();
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.AverageWaitingTime")+": <b>"+StatisticTools.formatNumberExt(W_avg,false)+"</b> (<b>"+StatisticTools.formatExactTime(W_avg)+"</b>)");
		generalData.append("<br>\n");

		final double T_avg=simData.statistics.clientsAllTransferTimes.getMean();
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.TransferWaitingTime")+": <b>"+StatisticTools.formatNumberExt(T_avg,false)+"</b> (<b>"+StatisticTools.formatExactTime(T_avg)+"</b>)");
		generalData.append("<br>\n");

		final double P_avg=simData.statistics.clientsAllProcessingTimes.getMean();
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.ProcessWaitingTime")+": <b>"+StatisticTools.formatNumberExt(P_avg,false)+"</b> (<b>"+StatisticTools.formatExactTime(P_avg)+"</b>)");
		generalData.append("<br>\n");

		final double V_avg=simData.statistics.clientsAllResidenceTimes.getMean();
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.ResidenceWaitingTime")+": <b>"+StatisticTools.formatNumberExt(V_avg,false)+"</b> (<b>"+StatisticTools.formatExactTime(V_avg)+"</b>)");
		generalData.append("<br>\n");

		generalData.append("<br>\n");

		final boolean isWarmUp=simData.runData.isWarmUp;
		generalData.append(Language.tr("ExpressionCalculator.Tab.Clients.SystemInWarmUpPhase")+": <b>"+(isWarmUp?Language.tr("Dialog.Button.Yes"):Language.tr("Dialog.Button.No"))+"</b>");

		generalData.append("</body></html>");

		return generalData.toString();
	}

	/**
	 * Befehl: Seite "Globale Daten" aktualisieren
	 */
	private void commandUpdateGeneralData() {
		infoLabel.setText(getGeneralData());
	}

	/**
	 * Entfernt &lt;html&gt;, &lt;body&gt;, &lt;b&gt; und &lt;br&gt; Tags.
	 * @param html	Zu bereinigender HTML-String
	 * @return	Unformatierter Text
	 */
	private String makeHTMLPlain(final String html) {
		String plain=html.replace("<b>","");
		plain=plain.replace("</b>","");
		plain=plain.replace("<br>","");
		plain=plain.replace("<html>","");
		plain=plain.replace("<body>","");
		plain=plain.replace("</html>","");
		plain=plain.replace("</body>","");
		return plain;
	}

	/**
	 * Erstellt den "Ausdruck berechnen"-Tab
	 * @param tab	Tab-Panel
	 * @param initialExpression	Startwert f�r das Eingabefeld
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
		addCopySaveToolbar(tab,()->variablesTableModel.getTableData().toString(),true);
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
		addCopySaveToolbar(tab,()->{
			final ListModel<ClientInfo> model=clientsList.getModel();
			final List<ClientInfo> list=new ArrayList<>();
			for (int i=0;i<model.getSize();i++) list.add(model.getElementAt(i));
			return ModelElementAnimationInfoDialog.buildTable(list).toString();
		},true);
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
		clientsList.setPrototypeCellValue(new ClientInfo(null,null,new RunDataClient(0,false,false,0)));
		final JPanel line;
		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		line.add(clientsListInfo=new JLabel());

		commandUpdateClientList();
	}

	/**
	 * Erstellt den "Javascript"-Tab
	 * @param tab	Tab-Panel
	 * @param initialJavaScript	Startwert f�r das Javascript-Eingabefeld
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
		buttonJavaScriptCopyResults=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.CopyScriptResult"),Images.EDIT_COPY.getIcon(),Language.tr("ExpressionCalculator.Toolbar.CopyScriptResult.Hint"));
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
	 * @param initialJava	Startwert f�r das Java-Eingabefeld
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
		buttonJavaCopyResults=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.CopyScriptResult"),Images.EDIT_COPY.getIcon(),Language.tr("ExpressionCalculator.Toolbar.CopyScriptResult.Hint"));
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
	 * @param readOnly	Nur-Lese-Status
	 */
	private void buildGlobalMapTab(final JPanel tab, final Map<String,Object> mapGlobal, final boolean readOnly) {
		addCopySaveToolbar(tab,()->mapTableModel.getTableData().toString(),true);
		final JTableExt mapTable=new JTableExt();
		mapTable.setModel(mapTableModel=new ExpressionCalculatorDialogTableModel(mapTable,mapGlobal,readOnly));
		mapTable.getColumnModel().getColumn(0).setMaxWidth(125);
		mapTable.getColumnModel().getColumn(0).setMinWidth(125);
		mapTable.getColumnModel().getColumn(2).setMinWidth(150);
		mapTable.getColumnModel().getColumn(2).setMaxWidth(150);
		if (!readOnly) {
			mapTable.getColumnModel().getColumn(3).setMinWidth(100);
			mapTable.getColumnModel().getColumn(3).setMaxWidth(100);
		}
		mapTable.setIsPanelCellTable(3);
		tab.add(new JScrollPane(mapTable),BorderLayout.CENTER);
	}

	/**
	 * Aktualisiert die Berechnung.
	 * @see #expressionEdit
	 */
	private void recalc() {
		if (expressionEdit==null) return;

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
	 * (z.B. zur Wiederverwendung beim n�chsten �ffnen des Dialogs).
	 * @return	0-basierende Nummer des zuletzt angezeigten Tabs
	 */
	public int getLastMode() {
		return tabs.getSelectedIndex();
	}

	/**
	 * Liefert den zuletzt in das Eingabefeld eingegebenen Ausdruck
	 * (z.B. zur Wiederverwendung beim n�chsten �ffnen des Dialogs).
	 * @return	Letzter eingegebener Ausdruck
	 */
	public String getLastExpression() {
		return expressionEdit.getText().trim();
	}

	/**
	 * Liefert das zuletzt in das JavaSkript-Eingabefeld eingegebene Skript
	 * (z.B. zur Wiederverwendung beim n�chsten �ffnen des Dialogs).
	 * @return	Letztes eingegebenes JavaSkript
	 */
	public String getLastJavaScript() {
		return scriptJavaScriptEdit.getText().trim();
	}

	/**
	 * Liefert das zuletzt in das Java-Eingabefeld eingegebene Skript
	 * (z.B. zur Wiederverwendung beim n�chsten �ffnen des Dialogs).
	 * @return	Letztes eingegebener Java-Code
	 */
	public String getLastJava() {
		return scriptJavaEdit.getText().trim();
	}

	/**
	 * Befehl: Anzeige aktualisieren
	 */
	private void commandUpdate() {
		commandUpdateGeneralData();
		commandUpdateClientList();
		mapTableModel.updateAll();
	}

	/**
	 * Timer-Task zur Aktualisierung der Daten
	 * @see ModelElementAnimationInfoDialog#commandAutoUpdate()
	 */
	private class UpdateTimerTask extends TimerTask {
		/**
		 * Konstruktor der Klasse
		 */
		public UpdateTimerTask() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void run() {
			if (buttonAutoUpdate.isSelected()) SwingUtilities.invokeLater(()->{
				commandUpdate();
				scheduleNextUpdate();
			});
		}
	}

	/**
	 * Befehl: Anzeige automatisch aktualisieren (an/aus)
	 */
	private void commandAutoUpdate() {
		buttonAutoUpdate.setSelected(!buttonAutoUpdate.isSelected());

		if (buttonAutoUpdate.isSelected()) {
			timer=new Timer("SimulationDataUpdate");
			scheduleNextUpdate();
		} else {
			if (timer!=null) {timer.cancel(); timer=null;}
		}
	}

	/**
	 * Plant den n�chsten Update-Schritt ein.
	 * @see #commandAutoUpdate()
	 * @see UpdateTimerTask
	 */
	private void scheduleNextUpdate() {
		if (timer!=null) timer.schedule(new UpdateTimerTask(),250);
	}

	@Override
	protected boolean closeButtonOK() {
		if (timer!=null) {timer.cancel(); timer=null;}
		return true;
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
	 * Befehl: Daten zu dem gew�hlten Kunden anzeigen
	 */
	private void commandShowClientData() {
		final int index=clientsList.getSelectedIndex();
		if (index<0) return;

		final ClientInfo clientInfo=clientsList.getModel().getElementAt(index);
		if (clientInfo==null) return;

		final ModelElementAnimationInfoClientDialog infoDialog=new ModelElementAnimationInfoClientDialog(this,simData.runModel,clientInfo,false,getRealClient!=null);
		if (!infoDialog.getShowEditorDialog()) return;

		commandEditClientData();
	}

	/**
	 * Befehl: Editor zu dem gew�hltenKunden anzeigen
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

		final ModelElementAnimationEditClientDialog editDialog=new ModelElementAnimationEditClientDialog(this,modelImages,simData.runModel,client);
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
	 * L�dt den Javascript-Code aus einer Datei
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
	 * L�dt den Java-Code aus einer Datei
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
	 * F�hrt den Javascript-Code aus.
	 */
	private void commandRunJavaScript() {
		final String s=runJavaScript.apply(scriptJavaScriptEdit.getText().trim());
		scriptJavaScriptResults.setText(s);

		recalc();
		if (variablesTableModel!=null) variablesTableModel.updateTable();
		if (mapTableModel!=null) mapTableModel.updateTable();
	}

	/**
	 * F�hrt den Java-Code aus.
	 */
	private void commandRunJava() {
		final String s=runJava.apply(scriptJavaEdit.getText().trim());
		scriptJavaResults.setText(s);

		recalc();
		if (variablesTableModel!=null) variablesTableModel.updateTable();
		if (mapTableModel!=null) mapTableModel.updateTable();
	}

	/**
	 * Reagiert auf Klicks auf die Symbolleisten-Schaltfl�chen
	 */
	private class ButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ButtonListener() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			final Object source=e.getSource();

			if (source instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)source;
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

			if (source==buttonJavaScriptNew) {
				if (allowDiscardJavaScript()) {
					scriptJavaScriptEdit.setText("");
					lastJavaScript=scriptJavaScriptEdit.getText();
				}
				return;
			}

			if (source==buttonJavaScriptLoad) {
				if (allowDiscardJavaScript()) commandLoadJavaScript(null);
				return;
			}

			if (source==buttonJavaScriptSave) {
				commandSaveJavaScript();
				return;
			}

			if (source==buttonJavaScriptRun) {
				commandRunJavaScript();
				return;
			}

			if (source==buttonJavaScriptCopyResults) {
				getToolkit().getSystemClipboard().setContents(new StringSelection(scriptJavaScriptResults.getText()),null);
				return;
			}

			if (source==buttonJavaScriptTools) {
				final ScriptPopup popup=new ScriptPopup(buttonJavaScriptTools,model,ScriptPopup.ScriptMode.Javascript,null);
				popup.addFeature(ScriptPopup.ScriptFeature.Simulation);
				popup.addFeature(ScriptPopup.ScriptFeature.Output);
				popup.build();
				popup.show(scriptJavaScriptEdit);
				return;
			}

			if (source==buttonJavaScriptHelp) {
				Help.topicModal(ExpressionCalculatorDialog.this,"JS");
				return;
			}

			/* Java */

			if (source==buttonJavaNew) {
				if (allowDiscardJava()) {
					scriptJavaEdit.setText("");
					lastJava=scriptJavaEdit.getText();
				}
				return;
			}

			if (source==buttonJavaLoad) {
				if (allowDiscardJava()) commandLoadJava(null);
				return;
			}

			if (source==buttonJavaSave) {
				commandSaveJava();
				return;
			}

			if (source==buttonJavaTools) {
				final ScriptPopup popup=new ScriptPopup(buttonJavaTools,model,ScriptPopup.ScriptMode.Java,null);
				popup.addFeature(ScriptPopup.ScriptFeature.Simulation);
				popup.addFeature(ScriptPopup.ScriptFeature.Output);
				popup.build();
				popup.show(scriptJavaEdit);
				return;
			}

			if (source==buttonJavaRun) {
				commandRunJava();
				return;
			}

			if (source==buttonJavaCopyResults) {
				getToolkit().getSystemClipboard().setContents(new StringSelection(scriptJavaResults.getText()),null);
				return;
			}

			if (source==buttonJavaHelp) {
				Help.topicModal(ExpressionCalculatorDialog.this,"Java");
				return;
			}
		}
	}
}