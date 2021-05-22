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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
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
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorAreaBuilder;
import ui.script.ScriptPopup;
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
	private final JTextField expressionEdit;

	/**
	 * Ergebnis der Berechnung von {@link #expressionEdit}
	 */
	private final JTextField resultsEdit;

	/** "Neu"-Schaltfläche für den Javascript-Code */
	private final JButton buttonJavaScriptNew;
	/** "Laden"-Schaltfläche für den Javascript-Code */
	private final JButton buttonJavaScriptLoad;
	/** "Speichern"-Schaltfläche für den Javascript-Code */
	private final JButton buttonJavaScriptSave;
	/** "Tools"-Schaltfläche für den Javascript-Code */
	private final JButton buttonJavaScriptTools;
	/** "Ausführen"-Schaltfläche für den Javascript-Code */
	private final JButton buttonJavaScriptRun;
	/** "Hilfe"-Schaltfläche für den Javascript-Code */
	private final JButton buttonJavaScriptHelp;
	/** Eingabefeld für den Javascript-Code */
	private final RSyntaxTextArea scriptJavaScriptEdit;
	/** Ausgabe der Ergebnisse der Ausführung des Javascript-Codes aus {@link #scriptJavaScriptEdit} */
	private final JTextArea scriptJavaScriptResults;
	/** Zuletzt geladener/gespeicherter Javascript-Code (zur Prüfung, ob die aktuellen Eingaben ohne Warnung verworfen werden dürfen) */
	private String lastJavaScript="";

	/** "Neu"-Schaltfläche für den Java-Code */
	private final JButton buttonJavaNew;
	/** "Laden"-Schaltfläche für den Java-Code */
	private final JButton buttonJavaLoad;
	/** "Speichern"-Schaltfläche für den Java-Code */
	private final JButton buttonJavaSave;
	/** "Tools"-Schaltfläche für den Java-Code */
	private final JButton buttonJavaTools;
	/** "Ausführen"-Schaltfläche für den Java-Code */
	private final JButton buttonJavaRun;
	/** "Hilfe"-Schaltfläche für den Java-Code */
	private final JButton buttonJavaHelp;
	/** Eingabefeld für den Java-Code */
	private final RSyntaxTextArea scriptJavaEdit;
	/** Ausgabe der Ergebnisse der Ausführung des Java-Codes aus {@link #scriptJavaEdit} */
	private final JTextArea scriptJavaResults;
	/** Zuletzt geladener/gespeicherter Java-Code (zur Prüfung, ob die aktuellen Eingaben ohne Warnung verworfen werden dürfen) */
	private String lastJava="";

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Editor-Modell als Information für den Expression-Builder
	 * @param mapGlobal	Zuordnung der globalen Scripting-Werte (kann <code>null</code> sein)
	 * @param calc	Funktion, die die Berechnungen erlaubt
	 * @param runJavaScript	Funktion, die die Javascript-Ausführungen erlaubt
	 * @param runJava	Funktion, die die Java-Ausführungen erlaubt
	 * @param initialTab	Anfänglich anzuzeigenden Tab
	 * @param initialExpression	Startwert für das Eingabefeld
	 * @param initialJavaScript	Startwert für das Javascript-Eingabefeld
	 * @param initialJava	Startwert für das Java-Eingabefeld
	 * @see ExpressionCalculatorDialog#getLastExpression()
	 */
	public ExpressionCalculatorDialog(final Component owner, final EditModel model, final Map<String,Object> mapGlobal, final Function<String,Double> calc, final UnaryOperator<String> runJavaScript, final UnaryOperator<String> runJava, final int initialTab, final String initialExpression, final String initialJavaScript, final String initialJava) {
		super(owner,Language.tr("ExpressionCalculator.Title"));
		this.model=model;
		this.calc=calc;
		this.runJavaScript=runJavaScript;
		this.runJava=runJava;

		showCloseButton=true;
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		JPanel tab;

		JPanel sub, line;
		Object[] data;
		Dimension size;
		JToolBar toolbar;
		JSplitPane split;
		ScriptEditorAreaBuilder builder;

		/* Ausdruck */

		tabs.add(Language.tr("ExpressionCalculator.Tab.Expression"),tab=new JPanel(new BorderLayout()));
		tab.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

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
		size=button.getPreferredSize();
		button.setPreferredSize(new Dimension(size.height,size.height));
		button.addActionListener(e->copyResultToClipboard());
		line.add(button,BorderLayout.EAST);

		/* Javascript */

		tabs.add(Language.tr("ExpressionCalculator.Tab.Javascript"),sub=new JPanel(new BorderLayout()));
		sub.add(split=new JSplitPane());
		split.setOrientation(JSplitPane.VERTICAL_SPLIT);
		split.setTopComponent(tab=new JPanel(new BorderLayout()));

		toolbar=new JToolBar();
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
		tab.add(toolbar,BorderLayout.NORTH);

		builder=new ScriptEditorAreaBuilder(ScriptPopup.ScriptMode.Javascript);
		builder.addFileDropper(new ButtonListener());
		builder.addAutoCompleteFeature(ScriptPopup.ScriptFeature.Simulation);
		builder.addAutoCompleteFeature(ScriptPopup.ScriptFeature.Output);
		builder.setText((initialJavaScript==null || initialJavaScript.isEmpty())?DEFAULT_JAVA_SCRIPT:initialJavaScript);
		final RTextScrollPane scrollJavaScript;
		tab.add(scrollJavaScript=new RTextScrollPane(scriptJavaScriptEdit=builder.get()),BorderLayout.CENTER);
		scrollJavaScript.setLineNumbersEnabled(true);
		lastJavaScript=scriptJavaScriptEdit.getText();

		split.setBottomComponent(sub=new JPanel(new BorderLayout()));
		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("ExpressionCalculator.Results")+":"));
		sub.add(new JScrollPane(scriptJavaScriptResults=new JTextArea("")),BorderLayout.CENTER);
		scriptJavaScriptResults.setEditable(false);

		split.setDividerLocation(0.66);
		split.setResizeWeight(0.75);

		/* if (DynamicClass.isWindows()) { - brauchen wir nicht mehr bei fully intern */
		if (DynamicFactory.isWindows() || DynamicFactory.isInMemoryProcessing()) {
			/* Java */

			tabs.add(Language.tr("ExpressionCalculator.Tab.Java"),sub=new JPanel(new BorderLayout()));
			sub.add(split=new JSplitPane());
			split.setOrientation(JSplitPane.VERTICAL_SPLIT);
			split.setTopComponent(tab=new JPanel(new BorderLayout()));

			toolbar=new JToolBar();
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
			tab.add(toolbar,BorderLayout.NORTH);

			builder=new ScriptEditorAreaBuilder(ScriptPopup.ScriptMode.Java);
			builder.addAutoCompleteFeature(ScriptPopup.ScriptFeature.Simulation);
			builder.addAutoCompleteFeature(ScriptPopup.ScriptFeature.Output);
			builder.addFileDropper(new ButtonListener());
			builder.setText((initialJava==null || initialJava.isEmpty())?DEFAULT_JAVA:initialJava);
			final RTextScrollPane scrollJava;
			tab.add(scrollJava=new RTextScrollPane(scriptJavaEdit=builder.get()),BorderLayout.CENTER);
			scrollJava.setLineNumbersEnabled(true);
			lastJava=scriptJavaEdit.getText();

			split.setBottomComponent(sub=new JPanel(new BorderLayout()));
			sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
			line.add(new JLabel(Language.tr("ExpressionCalculator.Results")+":"));
			sub.add(new JScrollPane(scriptJavaResults=new JTextArea("")),BorderLayout.CENTER);
			scriptJavaResults.setEditable(false);

			split.setDividerLocation(0.66);
			split.setResizeWeight(0.75);
		} else {
			buttonJavaNew=null;
			buttonJavaLoad=null;
			buttonJavaSave=null;
			buttonJavaTools=null;
			buttonJavaRun=null;
			buttonJavaHelp=null;
			scriptJavaEdit=null;
			scriptJavaResults=null;
		}

		/* Zuordnung */

		if (mapGlobal!=null) {
			tabs.add(Language.tr("ExpressionCalculator.Tab.Map"),sub=new JPanel(new BorderLayout()));
			final JTableExt mapTable=new JTableExt();
			mapTable.setModel(new ExpressionCalculatorDialogTableModel(mapTable,mapGlobal));
			mapTable.getColumnModel().getColumn(0).setMaxWidth(125);
			mapTable.getColumnModel().getColumn(0).setMinWidth(125);
			mapTable.getColumnModel().getColumn(2).setMinWidth(150);
			mapTable.getColumnModel().getColumn(2).setMaxWidth(150);
			mapTable.getColumnModel().getColumn(3).setMinWidth(100);
			mapTable.getColumnModel().getColumn(3).setMaxWidth(100);
			mapTable.setIsPanelCellTable(3);
			sub.add(new JScrollPane(mapTable),BorderLayout.CENTER);
		}

		/* Icons für Tabs */

		int index=0;
		tabs.setIconAt(index++,Images.SCRIPT_MODE_EXPRESSION.getIcon());
		tabs.setIconAt(index++,Images.SCRIPT_MODE_JAVASCRIPT.getIcon());
		if (DynamicFactory.isWindows() || DynamicFactory.isInMemoryProcessing()) {
			tabs.setIconAt(index++,Images.SCRIPT_MODE_JAVA.getIcon());
		}
		if (mapGlobal!=null) {
			tabs.setIconAt(index++,Images.SCRIPT_MAP.getIcon());
		}

		/* Start */

		if (initialTab>=0 && initialTab<tabs.getTabCount()) tabs.setSelectedIndex(initialTab);

		recalc();

		setMinSizeRespectingScreensize(550,400);
		pack();
		size=getSize();
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
	}

	/**
	 * Führt den Java-Code aus.
	 */
	private void commandRunJava() {
		final String s=runJava.apply(scriptJavaEdit.getText().trim());
		scriptJavaResults.setText(s);
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