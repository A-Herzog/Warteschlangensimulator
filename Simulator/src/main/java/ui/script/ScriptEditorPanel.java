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
package ui.script;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropperData;
import scripting.java.DynamicErrorInfo;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.ImportSettingsBuilder;
import scripting.js.JSRunDataFilterTools;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;

/**
 * Ermöglicht das Bearbeiten eines Javascript- oder Java-Codeabschnitts
 * in einem Dialog.
 * @author Alexander Herzog
 */
public class ScriptEditorPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7826682241784296994L;

	/**
	 * Standard-Rahmen für einen Java-Codeschnippsel<br>
	 * (Während bei Javascript direkt losgelegt werden kann, muss der Java-Code in eine Methode verpackt werden (die dann von der Ausführungsschicht noch in eine Klasse eingebaut wird).
	 */
	public static final String DEFAULT_JAVA="void function(SimulationInterface sim) {\n\n}\n";

	/**
	 * Listener, die benachrichtigt werden sollen, wenn der Nutzer Eingaben in das Skriptfeld vornimmt, aus.
	 * @see #fireKeyAction()
	 */
	private final List<Runnable> keyActionListeners;

	/**
	 * Im Vorlagen-Popupmenü sollen die Befehle für eine einfache Station (ohne Kundenkontakt) angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresPlainStation=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.Simulation
	};

	/**
	 * Im Vorlagen-Popupmenü sollen die Befehle für eine normale, durch einen Kunden angetriggerte Station angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresClientStation=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.Simulation,
			ScriptPopup.ScriptFeature.Client
	};

	/**
	 * Im Vorlagen-Popupmenü sollen die Befehle für eine durch einen Kunden angetriggerte Eingabe-Station angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresClientStationInput=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.Simulation,
			ScriptPopup.ScriptFeature.Client,
			ScriptPopup.ScriptFeature.InputValue
	};

	/**
	 * Im Vorlagen-Popupmenü sollen die Befehle für eine durch einen Kunden angetriggerte Ausgabe-Station angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresClientStationOutput=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.Simulation,
			ScriptPopup.ScriptFeature.Client,
			ScriptPopup.ScriptFeature.Output
	};

	/**
	 * Im Vorlagen-Popupmenü sollen die Befehle für eine Station (ohne Kundenkontakt) angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresPlainStationOutput=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.Simulation,
			ScriptPopup.ScriptFeature.Output
	};

	/**
	 * Im Vorlagen-Popupmenü sollen die Befehle für eine Station, die Kunden aufhält, angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresClientStationHold=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.Simulation,
			ScriptPopup.ScriptFeature.Client,
			ScriptPopup.ScriptFeature.ClientsList
	};

	/**
	 * Im Vorlagen-Popupmenü sollen die Befehle zur Verarbeitung der Statistikergebnisse angeboten werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresFilter=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.JSSystem,
			ScriptPopup.ScriptFeature.Statistics
	};

	/**
	 * Im Vorlagen-Popupmenü sollen die im Script-Runner verfügbaren Befehle dargestellt werden.
	 * @see ScriptPopup.ScriptFeature
	 */
	public static ScriptPopup.ScriptFeature[] featuresScriptRunner=new ScriptPopup.ScriptFeature[] {
			ScriptPopup.ScriptFeature.JSSystem,
			ScriptPopup.ScriptFeature.Output,
			ScriptPopup.ScriptFeature.FileOutput,
			ScriptPopup.ScriptFeature.Model,
			ScriptPopup.ScriptFeature.Statistics,
			ScriptPopup.ScriptFeature.Save
	};

	/**
	 * Skriptmodus
	 * @author Alexander Herzog
	 */
	public enum ScriptMode {
		/** Skriptmodus "Javascript" */
		Javascript,
		/** Skriptmodus "Java" */
		Java
	}

	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Hilfe-Runnable das in Dialogen verwendet wird */
	private final Runnable helpRunnalbe;
	/** Optionales Modell-Objekt, welches für den Aufbau eines Vorlagen-Popup-Menüs verwendet wird */
	private final EditModel model;
	/** Einstellungen zu Import und Classpath für Skripte */
	private final ImportSettingsBuilder scriptSettings;

	/** Optionales Statistik-Objekt, welches für den Aufbau eines Vorlagen-Popup-Menüs verwendet wird */
	private final Statistics statistics;
	/** Skriptfunktionen, die im Vorlagen-Popupmenü angeboten werden sollen */
	private final ScriptPopup.ScriptFeature[] scriptFeatures;

	/** Toolbar in dem Panel */
	private final JToolBar toolbar;
	/** "Neu"-Schaltfläche */
	private final JButton buttonNew;
	/** "Laden"-Schaltfläche */
	private final JButton buttonLoad;
	/** "Speichern"-Schaltfläche */
	private final JButton buttonSave;
	/** "Suchen"-Schaltfläche */
	private final JButton buttonSearch;
	/** "Prüfen"-Schaltfläche */
	private final JButton buttonCheck;
	/** "Tools"-Schaltfläche */
	private final JButton buttonTools;
	/** "Hilfe"-Schaltfläche */
	private final JButton buttonHelp;
	/** Eingabefeld für Javascript-Code */
	private final RSyntaxTextArea scriptEditJavascript;
	/** Eingabefeld für Java-Code */
	private final RSyntaxTextArea scriptEditJava;
	/** Panel welches die Editoren für die verschiedenen Skriptsprachen aufnimmt */
	private final JPanel scriptEditMulti;
	/** Layout zur Einblendung der verschiedenen Editoren in {@link #scriptEditMulti} */
	private final CardLayout scriptEditMultiLayout;
	/** Auswahlfeld für die Skriptsprache */
	private final JComboBox<String> languageCombo;

	/**
	 * Zuletzt eingestelltes oder gespeichertes Skript
	 * (zur Prüfung, ob das aktuelle Skript im Editor
	 * ohne Sicherheitsabfrage verworfen werden darf)
	 * @see #allowDiscard()
	 */
	private String lastScript="";

	/**
	 * Bisheriges Setup für die Suche
	 */
	private ScriptEditorAreaBuilder.SearchSetup lastSearchSetup=null;

	/**
	 * Konstruktor der Klasse
	 * @param script	Bisheriges Skript
	 * @param mode	Skriptmodus
	 * @param readOnly	Nur-Lese-Status
	 * @param scriptName	Überschrift über dem Skript (kann <code>null</code> sein, wenn keine Überschrift angezeigt werden soll)
	 * @param model	Optionales Modell-Objekt, welches für den Aufbau eines Vorlagen-Popup-Menüs verwendet wird
	 * @param statistics	Optionales Statistik-Objekt, welches für den Aufbau eines Vorlagen-Popup-Menüs verwendet wird
	 * @param helpRunnalbe	Hilfe-Runnable das in Dialogen verwendet wird
	 * @param scriptFeatures	Skriptfunktionen, die im Vorlagen-Popupmenü angeboten werden sollen
	 */
	public ScriptEditorPanel(final String script, final ScriptMode mode, final boolean readOnly, final String scriptName, final EditModel model, final Statistics statistics, final Runnable helpRunnalbe, final ScriptPopup.ScriptFeature[] scriptFeatures) {
		super();
		keyActionListeners=new ArrayList<>();
		setLayout(new BorderLayout());
		this.readOnly=readOnly;
		this.helpRunnalbe=helpRunnalbe;
		this.model=model;
		scriptSettings=new ImportSettingsBuilder(model);
		this.statistics=statistics;
		this.scriptFeatures=scriptFeatures;

		JPanel line;
		JLabel label;

		if (scriptName!=null && !scriptName.trim().isEmpty()) {
			add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
			line.add(new JLabel(scriptName+":"));
		}

		final JPanel sub=new JPanel(new BorderLayout());
		add(sub,BorderLayout.CENTER);

		/* Toolbar */
		toolbar=new JToolBar();
		toolbar.setFloatable(false);
		sub.add(toolbar,BorderLayout.NORTH);

		toolbar.add(label=new JLabel(Language.tr("Surface.ScriptEditor.Language")+":"));
		toolbar.add(Box.createHorizontalStrut(5));
		toolbar.add(languageCombo=new JComboBox<>(new String[]{
				Language.tr("Surface.ScriptEditor.Language.Javascript"),
				Language.tr("Surface.ScriptEditor.Language.Java")
		}));
		languageCombo.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SCRIPT_MODE_JAVASCRIPT,
				Images.SCRIPT_MODE_JAVA
		}));
		label.setLabelFor(languageCombo);
		switch (mode) {
		case Javascript: languageCombo.setSelectedIndex(0); break;
		case Java: languageCombo.setSelectedIndex(1); break;
		default: languageCombo.setSelectedIndex(0); break;
		}
		languageCombo.setMaximumSize(new Dimension(languageCombo.getPreferredSize().width,languageCombo.getMaximumSize().height));
		toolbar.add(Box.createHorizontalStrut(5));

		buttonNew=addToolbarButton(toolbar,Language.tr("Surface.ScriptEditor.New"),Images.SCRIPT_NEW.getIcon(),Language.tr("Surface.ScriptEditor.New.Hint"),readOnly);
		buttonLoad=addToolbarButton(toolbar,Language.tr("Surface.ScriptEditor.Load"),Images.SCRIPT_LOAD.getIcon(),Language.tr("Surface.ScriptEditor.Load.Hint"),readOnly);
		buttonSave=addToolbarButton(toolbar,Language.tr("Surface.ScriptEditor.Save"),Images.SCRIPT_SAVE.getIcon(),Language.tr("Surface.ScriptEditor.Save.Hint"),readOnly);
		addCustomToolbarButtons(toolbar);
		toolbar.addSeparator();
		buttonSearch=addToolbarButton(toolbar,Language.tr("Surface.ScriptEditor.Search"),Images.GENERAL_FIND.getIcon(),Language.tr("Surface.ScriptEditor.Search.Hint"),false);
		buttonTools=addToolbarButton(toolbar,Language.tr("Surface.ScriptEditor.Tools"),Images.SCRIPT_TOOLS.getIcon(),Language.tr("Surface.ScriptEditor.Tools.Hint"),readOnly);
		buttonCheck=addToolbarButton(toolbar,Language.tr("Surface.ScriptEditor.Check"),Images.SIMULATION_CHECK.getIcon(),Language.tr("Surface.ScriptEditor.Check.Hint"),readOnly);
		toolbar.addSeparator();
		buttonHelp=addToolbarButton(toolbar,Language.tr("Main.Toolbar.Help"),Images.HELP.getIcon(),Language.tr("Surface.ScriptEditor.Help.Hint"),readOnly);

		/* Eingabebereich */
		ScriptEditorAreaBuilder builder;

		builder=new ScriptEditorAreaBuilder(ScriptPopup.ScriptMode.Javascript,readOnly,e->fireKeyAction());
		builder.addAutoCompleteFeatures(scriptFeatures);
		if (script!=null && mode==ScriptMode.Javascript) builder.setText(script);
		builder.addFileDropper(new ButtonListener());
		scriptEditJavascript=builder.get();

		builder=new ScriptEditorAreaBuilder(ScriptPopup.ScriptMode.Java,readOnly,e->fireKeyAction());
		builder.addAutoCompleteFeatures(scriptFeatures);
		builder.setText(DEFAULT_JAVA);
		if (script!=null && mode==ScriptMode.Java) builder.setText(script);
		builder.addFileDropper(new ButtonListener());
		scriptEditJava=builder.get();

		lastScript=(script==null)?"":script;

		sub.add(scriptEditMulti=new JPanel(),BorderLayout.CENTER);
		scriptEditMulti.setLayout(scriptEditMultiLayout=new CardLayout());
		final RTextScrollPane scriptEditJavascriptScroll;
		scriptEditMulti.add(scriptEditJavascriptScroll=new RTextScrollPane(scriptEditJavascript),"0");
		scriptEditJavascriptScroll.setLineNumbersEnabled(true);
		final RTextScrollPane scriptEditJavaScroll;
		scriptEditMulti.add(scriptEditJavaScroll=new RTextScrollPane(scriptEditJava),"1");
		scriptEditJavaScroll.setLineNumbersEnabled(true);
		scriptEditMultiLayout.show(scriptEditMulti,""+languageCombo.getSelectedIndex());

		languageCombo.addActionListener(e->languageChanged());
		languageCombo.setEnabled(!readOnly);
		languageChanged();

		/* Hotkey registrieren */
		final InputMap inputMap=getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_DOWN_MASK),"CtrlF");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0),"F3");
		getActionMap().put("CtrlF",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=-3416116159118494680L;

			@Override
			public void actionPerformed(ActionEvent e) {commandSearch();}
		});
		getActionMap().put("F3",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=-4245259259848720255L;

			@Override
			public void actionPerformed(ActionEvent e) {commandSearchAgain();}
		});


		/* Ggf. Java-Code direkt beim Öffnen des Editor prüfen */
		if (mode==ScriptMode.Java) {
			SwingUtilities.invokeLater(()->testJavaCodeAtStartUp(script));
		}
	}

	/**
	 * Versucht den angegebenen Java-Code zu übersetzen (und im Erfolgsfall in den Cache aufzunehmen)
	 * @param script	Zu übersetzender Java-Code
	 */
	private void testJavaCodeAtStartUp(final String script) {
		final Thread testThread=new Thread(()->{
			if (!DynamicFactory.isWindows() && !DynamicFactory.isInMemoryProcessing()) return;
			DynamicFactory.getFactory().test(script,scriptSettings);
		},"Java code background tester");
		testThread.start();
	}

	/**
	 * Konstruktor der Klasse
	 * @param script	Bisheriges Skript
	 * @param mode	Skriptmodus
	 * @param readOnly	Nur-Lese-Status
	 * @param scriptName	Überschrift über dem Skript (kann <code>null</code> sein, wenn keine Überschrift angezeigt werden soll)
	 * @param model	Optionales Modell-Objekt, welches für den Aufbau eines Vorlagen-Popup-Menüs verwendet wird
	 * @param helpRunnable	Hilfe-Runnable das in Dialogen verwendet wird
	 * @param scriptFeatures	Skriptfunktionen, die im Vorlagen-Popupmenü angeboten werden sollen
	 */
	public ScriptEditorPanel(final String script, final ScriptMode mode, final boolean readOnly, final String scriptName, final EditModel model, final Runnable helpRunnable, final ScriptPopup.ScriptFeature[] scriptFeatures) {
		this(script,mode,readOnly,scriptName,model,null,helpRunnable,scriptFeatures);
	}

	/**
	 * Ermöglicht das Hinzufügen von Schaltflächen zur Symbolleiste durch abgeleitete Klassen
	 * @param toolbar	Symbolleiste zu der durch diese Methode weitere Schaltflächen hinzugefügt werden sollen
	 */
	protected void addCustomToolbarButtons(final JToolBar toolbar) {
	}

	/**
	 * Erstellt eine neue Schaltfläche und fügt sie zur Symbolleiste hinzu.
	 * @param toolbar	Symbolleiste auf der die neue Schaltfläche eingefügt werden soll
	 * @param title	Beschriftung der Schaltfläche
	 * @param icon	Optionales Icon für die Schaltfläche (darf <code>null</code> sein)
	 * @param hint	Tooltip für die Schaltfläche (darf <code>null</code> sein)
	 * @param readOnly	Soll die Schaltfläche aktiviert sein (<code>false</code>) oder deaktiviert werden (<code>true</code>)?
	 * @return	Neue Schaltfläche (ist bereits in die Symbolleiste eingefügt)
	 */
	private JButton addToolbarButton(final JToolBar toolbar, final String title, final Icon icon, final String hint, final boolean readOnly) {
		final JButton button=new JButton(title);
		toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		if (icon!=null) button.setIcon(icon);
		button.addActionListener(new ButtonListener());
		button.setEnabled(!readOnly);
		return button;
	}

	/**
	 * Wird aufgerufen, wenn in der Auswahlbox die Skriptsprache
	 * verändert wurde nun nun der Editor angepasst werden soll.
	 * @see #languageCombo
	 */
	private void languageChanged() {
		scriptEditMultiLayout.show(scriptEditMulti,""+languageCombo.getSelectedIndex());
		buttonCheck.setVisible(languageCombo.getSelectedIndex()==1 && (DynamicFactory.isWindows() || DynamicFactory.isInMemoryProcessing()));
	}

	/**
	 * Stellt das aktuelle Skript im Editor ein
	 * @param script	Neues Skript
	 */
	private void setCurrentScript(final String script) {
		switch (languageCombo.getSelectedIndex()) {
		case 0: scriptEditJavascript.setText(script); break;
		case 1: scriptEditJava.setText(script); break;
		}
		lastScript=script;
	}

	/**
	 * Prüft, ob das Skript im Editor verworfen werden darf.
	 * @return	Gibt <code>true</code> zurück, wenn das Skript verworfen werden darf.
	 */
	public boolean allowDiscard() {
		if (lastScript.equals(getScript())) return true;
		if (getMode()==ScriptMode.Java && getScript().equals(DEFAULT_JAVA)) return true;

		final String title;
		final String info;
		switch (languageCombo.getSelectedIndex()) {
		case 0:
			title=Language.tr("Surface.ScriptEditor.Language.JavaScript.DiscardConfirmation.Title");
			info=Language.tr("Surface.ScriptEditor.Language.JavaScript.DiscardConfirmation.Info");
			break;
		case 1:
			title=Language.tr("Surface.ScriptEditor.Language.Java.DiscardConfirmation.Title");
			info=Language.tr("Surface.ScriptEditor.Language.Java.DiscardConfirmation.Info");
			break;
		default:
			title=Language.tr("Surface.ScriptEditor.Language.JavaScript.DiscardConfirmation.Title");
			info=Language.tr("Surface.ScriptEditor.Language.JavaScript.DiscardConfirmation.Info");
			break;
		}

		switch (MsgBox.confirmSave(this,title,info)) {
		case JOptionPane.YES_OPTION: commandSave(); return allowDiscard();
		case JOptionPane.NO_OPTION: return true;
		case JOptionPane.CANCEL_OPTION: return false;
		default: return false;
		}
	}

	/**
	 * Befehl: Skript laden
	 * @param file	Zu ladende Datei (wird <code>null</code> übergeben, so wird ein Dateiauswahldialog angezeigt)
	 * @return	Liefert <code>true</code>, wenn eine Datei geladen wurde
	 */
	private boolean commandLoad(File file) {
		if (file==null) {
			final JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			final FileFilter filter;
			final String defaultExt;
			switch (languageCombo.getSelectedIndex()) {
			case 0:
				fc.setDialogTitle(Language.tr("FileType.Load.JS"));
				filter=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
				defaultExt="js";
				break;
			case 1:
				fc.setDialogTitle(Language.tr("FileType.Load.Java"));
				filter=new FileNameExtensionFilter(Language.tr("FileType.Java")+" (*.java)","java");
				defaultExt="java";
				break;
			default:
				fc.setDialogTitle(Language.tr("FileType.Load.JS"));
				filter=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
				defaultExt="js";
				break;
			}
			fc.addChoosableFileFilter(filter);
			fc.setFileFilter(filter);
			if (fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return false;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			file=fc.getSelectedFile();
			if (file.getName().indexOf('.')<0 && fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+"."+defaultExt);
		}

		final String text=JSRunDataFilterTools.loadText(file);
		if (text==null) return false;
		setCurrentScript(text);
		return true;
	}

	/**
	 * Befehl: Skript speichern
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean commandSave() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		final FileFilter filter;
		final String defaultExt;
		switch (languageCombo.getSelectedIndex()) {
		case 0:
			fc.setDialogTitle(Language.tr("FileType.Save.JS"));
			filter=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
			defaultExt="js";
			break;
		case 1:
			fc.setDialogTitle(Language.tr("FileType.Save.Java"));
			filter=new FileNameExtensionFilter(Language.tr("FileType.Java")+" (*.java)","java");
			defaultExt="java";
			break;
		default:
			fc.setDialogTitle(Language.tr("FileType.Save.JS"));
			filter=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
			defaultExt="js";
			break;
		}
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return false;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+"."+defaultExt);
		}
		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return false;
		}

		final String script=getScript();
		if (!JSRunDataFilterTools.saveText(script,file,false)) return false;
		lastScript=script;
		return true;
	}

	/**
	 * Zeigt ein Popup mit Suchfunktionen an.
	 */
	private void commandSearchPopup() {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("Surface.ScriptEditor.Search.Search"),Images.GENERAL_FIND.getIcon()));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_DOWN_MASK));
		item.addActionListener(e->commandSearch());

		if (lastSearchSetup!=null) {
			popup.add(item=new JMenuItem(Language.tr("Surface.ScriptEditor.Search.SearchAgain")));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));
			item.addActionListener(e->commandSearchAgain());
		}

		popup.addSeparator();

		popup.add(item=new JMenuItem(Language.tr("Surface.ScriptEditor.Search.RemoveMarkOccurrence"),Images.EDIT_DELETE.getIcon()));
		item.addActionListener(e->commandSearchRemoveHighlight());

		popup.show(buttonSearch,0,buttonSearch.getHeight());
	}

	/**
	 * Befehl: Suchen
	 */
	private void commandSearch() {
		RSyntaxTextArea textArea=null;
		switch (languageCombo.getSelectedIndex()) {
		case 0: textArea=scriptEditJavascript; break;
		case 1: textArea=scriptEditJava; break;
		}
		if (textArea==null) return;

		final SetupData setup=SetupData.getSetup();
		if (lastSearchSetup==null) {
			lastSearchSetup=new ScriptEditorAreaBuilder.SearchSetup(setup.scriptSearchMatchCase,setup.scriptSearchRegex,setup.scriptSearchForward,setup.scriptSearchWholeWord);
		}

		final ScriptEditorPanelSearchDialog dialog=new ScriptEditorPanelSearchDialog(this,lastSearchSetup);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
		lastSearchSetup=dialog.getNewSearchSetup();
		setup.scriptSearchMatchCase=lastSearchSetup.matchCase;
		setup.scriptSearchRegex=lastSearchSetup.regex;
		setup.scriptSearchForward=lastSearchSetup.forward;
		setup.scriptSearchWholeWord=lastSearchSetup.wholeWord;
		setup.saveSetup();

		ScriptEditorAreaBuilder.search(textArea,lastSearchSetup);
	}

	/**
	 * Befehl: Weitersuchen
	 */
	private void commandSearchAgain() {
		RSyntaxTextArea textArea=null;
		switch (languageCombo.getSelectedIndex()) {
		case 0: textArea=scriptEditJavascript; break;
		case 1: textArea=scriptEditJava; break;
		}
		if (textArea==null) return;

		ScriptEditorAreaBuilder.search(textArea,lastSearchSetup);
	}

	/**
	 * Befehl: Markierungen der Suchtreffer entfernen
	 */
	private void commandSearchRemoveHighlight() {
		RSyntaxTextArea textArea=null;
		switch (languageCombo.getSelectedIndex()) {
		case 0: textArea=scriptEditJavascript; break;
		case 1: textArea=scriptEditJava; break;
		}
		if (textArea==null) return;

		ScriptEditorAreaBuilder.search(textArea,null);
	}

	/**
	 * Zeigt ein Popupmenü mit Befehlsvorschlägen an.
	 * @see #buttonTools
	 */
	private void commandToolsPopup() {
		ScriptPopup.ScriptMode mode=ScriptPopup.ScriptMode.Javascript;
		switch (languageCombo.getSelectedIndex()) {
		case 0: mode=ScriptPopup.ScriptMode.Javascript; break;
		case 1: mode=ScriptPopup.ScriptMode.Java; break;
		}

		final ScriptPopup popup=new ScriptPopup(buttonTools,model,statistics,mode,helpRunnalbe);
		popup.addInfoText(Language.tr("Surface.ScriptEditor.PopupInfo"));
		popup.addFeatures(scriptFeatures);
		popup.build();
		switch (languageCombo.getSelectedIndex()) {
		case 0: popup.show(scriptEditJavascript); break;
		case 1: popup.show(scriptEditJava); break;
		}
	}

	/**
	 * Reagiert auf Klicks auf die Schaltflächen
	 */
	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;
			final Object source=e.getSource();

			if (source instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				final File file=data.getFile();
				if (file.isFile()) {
					if (allowDiscard()) commandLoad(file);
					data.dragDropConsumed();
				}
				return;
			}

			if (source==buttonNew) {
				if (allowDiscard()) {
					setCurrentScript(null);
					scriptEditJavascript.setText("");
					scriptEditJava.setText("");
					lastScript="";
					fireKeyAction();
				}
				return;
			}

			if (source==buttonLoad) {
				if (allowDiscard()) {
					if (commandLoad(null)) fireKeyAction();
				}
				return;
			}

			if (source==buttonSave) {
				if (commandSave()) fireKeyAction();
				return;
			}

			if (source==buttonSearch) {
				commandSearchPopup();
			}

			if (source==buttonCheck) {
				final DynamicRunner runner=DynamicFactory.getFactory().test(scriptEditJava.getText(),scriptSettings);
				if (runner.isOk()) {
					MsgBox.info(ScriptEditorPanel.this,Language.tr("Surface.ScriptEditor.Check.Success.Title"),Language.tr("Surface.ScriptEditor.Check.Success.Info"));
				} else {
					new DynamicErrorInfo(ScriptEditorPanel.this,runner);
				}
				return;
			}

			if (source==buttonTools) {
				commandToolsPopup();
				return;
			}

			if (source==buttonHelp) {
				switch (languageCombo.getSelectedIndex()) {
				case 0:
					Help.topicModal(ScriptEditorPanel.this,"JS");
					break;
				case 1:
					Help.topicModal(ScriptEditorPanel.this,"Java");
					break;
				}
				return;
			}
		}
	}

	/**
	 * Prüft ob die eingegebenen Daten in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	public boolean checkData() {
		switch (languageCombo.getSelectedIndex()) {
		case 0: /* Javascript */
			return true;
		case 1: /* Java */
			if (!DynamicFactory.isWindows() && !DynamicFactory.isInMemoryProcessing()) return true;
			final DynamicRunner runner=DynamicFactory.getFactory().test(scriptEditJava.getText(),scriptSettings);
			if (!runner.isOk()) new DynamicErrorInfo(this,runner);
			return runner.isOk();
		default:
			return true;
		}
	}

	/**
	 * Liefert das eingegebene Skript zurück.
	 * @return	Neues Skript
	 */
	public String getScript() {
		switch (languageCombo.getSelectedIndex()) {
		case 0: return scriptEditJavascript.getText();
		case 1: return scriptEditJava.getText();
		}
		return "";
	}

	/**
	 * Gibt den gewählten Skriptmodus an.
	 * @return	Aktueller Skriptmodus
	 */
	public ScriptMode getMode() {
		switch (languageCombo.getSelectedIndex()) {
		case 0: return ScriptMode.Javascript;
		case 1: return ScriptMode.Java;
		default: return ScriptMode.Javascript;
		}
	}

	/**
	 * Stellt den Skriptmodus und das aktuelle Skript für den Editor ein
	 * @param mode	Neuer Skriptmodus
	 * @param script	Neues Skript
	 */
	public void setScript(final ScriptMode mode, final String script) {
		switch (mode) {
		case Javascript: languageCombo.setSelectedIndex(0); break;
		case Java: languageCombo.setSelectedIndex(1); break;
		}
		setCurrentScript(script);
	}

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn der Nutzer Eingaben in das Skriptfeld vornimmt.
	 * @param listener	Neuer zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener in die Liste der zu benachrichtigenden Listener aufgenommen werden konnte.
	 */
	public boolean addKeyActionListener(final Runnable listener) {
		if (keyActionListeners.contains(listener)) return false;
		keyActionListeners.add(listener);
		return true;
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die benachrichtigt werden sollen, wenn der Nutzer Eingaben in das Skriptfeld vornimmt.
	 * @param listener	Nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener aus der Liste der zu benachrichtigenden Listener entfernt werden konnte.
	 */
	public boolean removeKeyActionListener(final Runnable listener) {
		return keyActionListeners.remove(listener);
	}

	/**
	 * Löst die Listener, die benachrichtigt werden sollen, wenn der Nutzer Eingaben in das Skriptfeld vornimmt, aus.
	 * @see #keyActionListeners
	 */
	private void fireKeyAction() {
		for (Runnable runnable: keyActionListeners) runnable.run();
	}

	/**
	 * Stellt ein, ob Editor und Toolbar aktiv sein sollen
	 * (nur verfügbar, wenn im Konstruktor nicht ReadOnly gewählt wurde).
	 * @param editable	Editor und Toolbar aktivieren
	 */
	public void setEditable(final boolean editable) {
		if (readOnly) return;
		for (int i=0;i<toolbar.getComponentCount();i++) if (toolbar.getComponent(i) instanceof JButton) ((JButton)toolbar.getComponent(i)).setEnabled(editable);
		scriptEditJavascript.setEditable(editable);
		scriptEditJava.setEditable(editable);
		languageCombo.setEnabled(editable);
	}
}
