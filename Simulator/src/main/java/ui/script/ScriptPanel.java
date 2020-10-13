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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.MsgBox;
import ui.images.Images;
import ui.script.ScriptEditorPanel.ScriptMode;
import ui.scriptrunner.JSModelTemplates;

/**
 * Dieses Panel bietet einen Skript-Editor und eine Ausgabeanzeige.
 * Das Panel kann daher in verschiedene Funktionen zur Skriptanzeige
 * eingebettet werden.
 * @author Alexander Herzog
 *
 */
public abstract class ScriptPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5776312809204193826L;

	/** Wird hier ein Modell übergeben (statt <code>null</code>, was auch zulässig ist), so wird eine Symbolleisten-Schaltfläche zur Auswahl von Beispiel-Skripten angezeigt */
	private final EditModel model;
	/** Optionale Beispielskripte für die jeweiligen Sprachen */
	private final Map<ScriptEditorPanel.ScriptMode,String> example;
	private final JTabbedPane tabs;
	private JButton templatesButton;
	private final ScriptEditorPanel editor;
	private final List<JButton> buttons;
	private final JLabel waitIndicator;

	private final JTextArea outputArea;
	private volatile Thread thread=null;

	/**
	 * Konstruktor der Klasse
	 * @param model	Wird hier ein Modell übergeben (statt <code>null</code>, was auch zulässig ist), so wird eine Symbolleisten-Schaltfläche zur Auswahl von Beispiel-Skripten angezeigt
	 * @param showRun	Gibt an, ob die "Skript ausführen"-Schaltfläche angezeigt werden soll
	 * @param scriptFeatures	Skriptfunktionen, die im Vorlagen-Popupmenü angeboten werden sollen
	 * @param example	Optionale Beispielskripte für die jeweiligen Sprachen
	 */
	public ScriptPanel(final EditModel model, final boolean showRun, final ScriptPopup.ScriptFeature[] scriptFeatures, final Map<ScriptEditorPanel.ScriptMode,String> example) {
		super();
		this.model=model;
		this.example=example;

		buttons=new ArrayList<>();

		setLayout(new BorderLayout());
		add(tabs=new JTabbedPane());

		JPanel tab, top;
		JToolBar toolbar;

		editor=new ScriptEditorPanel("",ScriptEditorPanel.ScriptMode.Javascript,false,null,getModel(),getMiniStatistics(),getHelpRunnable(),scriptFeatures){
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = -1345017777248153806L;
			@Override
			protected void addCustomToolbarButtons(final JToolBar toolbar) {
				if (model!=null) templatesButton=addButton(toolbar,Language.tr("ParameterCompare.ScriptRunner.Script.Template"),Language.tr("ParameterCompare.ScriptRunner.Script.Template.Hint"),Images.SCRIPT_TEMPLATE.getIcon(),e->commandTemplate(),false);
				if (ScriptPanel.this.example!=null) addButton(toolbar,Language.tr("ParameterCompare.ScriptRunner.Script.Example"),Language.tr("ParameterCompare.ScriptRunner.Script.Example.Hint"),Images.SCRIPT_EXAMPLE.getIcon(),e->commandLoadExample(),false);
				if (showRun) addButton(toolbar,Language.tr("ParameterCompare.ScriptRunner.Script.Run"),Language.tr("ParameterCompare.ScriptRunner.Script.Run.Hint"),Images.SCRIPT_RUN.getIcon(),e->commandRun(),false);
			}
		};
		tabs.add(Language.tr("ParameterCompare.ScriptRunner.Tab.Script"),editor);

		tabs.add(Language.tr("ParameterCompare.ScriptRunner.Tab.Output"),tab=new JPanel(new BorderLayout()));
		tab.add(top=new JPanel(new BorderLayout()),BorderLayout.NORTH);
		top.add(toolbar=new JToolBar(SwingConstants.HORIZONTAL),BorderLayout.CENTER);
		toolbar.setFloatable(false);
		addButton(toolbar,Language.tr("ParameterCompare.ScriptRunner.Results.Copy"),Language.tr("ParameterCompare.ScriptRunner.Results.Copy.Hint"),Images.EDIT_COPY.getIcon(),e->commandResultsCopy(),true);
		addButton(toolbar,Language.tr("ParameterCompare.ScriptRunner.Results.Save"),Language.tr("ParameterCompare.ScriptRunner.Results.Save.Hint"),Images.GENERAL_SAVE.getIcon(),e->commandResultsSave(),true);
		addButton(toolbar,Language.tr("JSRunner.Toolbar.Clear"),Language.tr("JSRunner.Toolbar.Clear.Hint"),Images.SCRIPT_CLEAR.getIcon(),e->clearOutput(),true);

		waitIndicator=new JLabel();
		waitIndicator.setIcon(Images.GENERAL_WAIT_INDICATOR.getIcon());
		waitIndicator.setVisible(false);
		top.add(waitIndicator,BorderLayout.EAST);
		waitIndicator.setBorder(BorderFactory.createEmptyBorder(5,5,0,15));

		tab.add(new JScrollPane(outputArea=new JTextArea()),BorderLayout.CENTER);
		outputArea.setEditable(false);

		tabs.setIconAt(0,Images.SCRIPT_PANEL_INPUT.getIcon());
		tabs.setIconAt(1,Images.SCRIPT_PANEL_OUTPUT.getIcon());
	}

	private JButton addButton(final JToolBar toolbar, final String title, final String hint, final Icon icon, final ActionListener listener, final boolean addToList) {
		final JButton button=new JButton(title);
		button.setToolTipText(hint);
		if (icon!=null) button.setIcon(icon);
		button.addActionListener(listener);
		toolbar.add(button);
		if (addToList) buttons.add(button);
		return button;
	}

	/**
	 * Stellt ein Skript im Editor ein
	 * @param mode	Neuer Skriptmodus
	 * @param script	Neues Skript
	 */
	public void setEditorScript(final ScriptMode mode, final String script) {
		editor.setScript(mode,script);
	}

	/**
	 * Stellt ein Skript im Editor ein und versucht dabei die Sprache selbst zu erkennen
	 * @param script	Neues Skript
	 */
	public void setEditorScript(final String script) {
		editor.setScript(getScriptType(script),script);
	}

	/**
	 * Liefert die aktuell für den Editor gewählte Skriptsprache
	 * @return	Aktuelle Skriptsprache
	 */
	public ScriptEditorPanel.ScriptMode getEditorMode() {
		return editor.getMode();
	}

	/**
	 * Liefert das aktuelle Skript des Editors
	 * @return	Aktuelles Skript
	 */
	public String getEditorScript() {
		return editor.getScript();
	}

	/**
	 * Liefert ein Statistik-Objekt, aus dem Daten für den XML-Tag-Auswahl-Dialog ausgelesen werden
	 * @return	Statistik-Objekt für XML-Tag-Auswahl-Dialog
	 */
	protected abstract Statistics getMiniStatistics();

	/**
	 * Liefert optional ein Editor-Modell-Objekt, aus dem Daten für den XML-Tag-Auswahl-Dialog ausgelesen werden.
	 * Wird hier <code>null</code> geliefert, so wird der entsprechende Menüpunkt nicht angezeigt
	 * @return	Editor-Modell-Objekt für XML-Tag-Auswahl-Dialog (oder <code>null</code>)
	 */
	protected abstract EditModel getModel();

	/**
	 * Liefert das Help-Runnable, das im XML-Tag-Auswahl-Dialog verwendet werden soll
	 * @return	Help-Runnable für XML-Tag-Auswahl-Dialog
	 */
	protected abstract Runnable getHelpRunnable();

	/**
	 * Wird aufgerufen, wenn die Toolbar-Schaltfläche zur Skriptausführung angeklickt wurde
	 * @param mode	Skriptsprache
	 * @param script	Auszuführendes Skript
	 * @return	Gibt <code>true</code> zurück, wenn das Skript erfolgreich ausgeführt werden konnte
	 */
	protected boolean run(final ScriptEditorPanel.ScriptMode mode, final String script) {
		return false;
	}

	/**
	 * Aktiviert oder deaktiviert die GUI
	 * @param enabled	Aktivierungsstatus für GUI
	 */
	public final synchronized void setEnabledGUI(final boolean enabled) {
		for (JButton button: buttons) button.setEnabled(enabled);
		editor.setEditable(enabled);
	}

	/**
	 * Fügt einen Text an die Ausgabe an
	 * @param text	Auszugebender Text
	 */
	public final synchronized void addOutput(final String text) {
		outputArea.setText(outputArea.getText()+text);
	}

	/**
	 * Wechselt auf die Ausgabe-Seite
	 */
	public void showResultsTab() {
		tabs.setSelectedIndex(1);
	}

	private void commandTemplate() {
		final JSModelTemplates templates=new JSModelTemplates(editor.getMode(),model);
		templates.showMenu(templatesButton,code->{
			if (!allowDiscard()) return;
			editor.setScript(editor.getMode(),code);
		});
	}

	private void commandLoadExample() {
		final ScriptEditorPanel.ScriptMode mode=editor.getMode();
		final String newScript=example.get(mode);
		if (newScript==null || newScript.trim().isEmpty()) return;
		if (!editor.allowDiscard()) return;
		editor.setScript(mode,newScript);
	}

	private void commandRun() {
		outputArea.setText("");
		showResultsTab();
		setEnabledGUI(false);
		waitIndicator.setVisible(true);
		thread=new Thread(()->{
			run(editor.getMode(),editor.getScript());
			setEnabledGUI(true);
			waitIndicator.setVisible(false);
			thread=null;
		});
		thread.start();
	}

	private void commandResultsCopy() {
		final StringSelection stringSelection=new StringSelection(outputArea.getText());
		final Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection,null);
	}

	private void commandResultsSave() {
		final String fileName=ScriptTools.selectTextSaveFile(this,Language.tr("ParameterCompare.ScriptRunner.Results.Save.Title"),null);
		if (fileName==null) return;
		final File file=new File(fileName);

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return;
		}

		try {
			Files.write(file.toPath(),outputArea.getText().getBytes());
		} catch (IOException e) {
			MsgBox.error(this,Language.tr("ParameterCompare.ScriptRunner.Results.Save.Error.Title"),String.format(Language.tr("ParameterCompare.ScriptRunner.Results.Save.Error.Info"),file.toString()));
			return;
		}
	}

	/**
	 * Löscht die Ausgabe
	 */
	public final void clearOutput() {
		outputArea.setText("");
	}

	/**
	 * Gibt an, ob das Panel geschlossen werden darf
	 * (also alles gespeichert ist oder der Nutzer dem Verwerfen zugestimmt hat).
	 * @return	Gibt <code>true</code> zurück, wenn das Panel geschlossen werden darf
	 */
	public boolean allowDiscard() {
		return editor.allowDiscard();
	}

	/**
	 * Versucht auf Basis des Skripttextes zu erkennen, um welche Sprache es sich handelt
	 * @param script	Zu prüfendes Skript
	 * @return	Sprache des Skriptes
	 */
	public static ScriptMode getScriptType(final String script) {
		ScriptMode mode=ScriptMode.Javascript;
		if (script!=null) {
			String s=script.trim().toLowerCase();
			if (s.startsWith("void")) mode=ScriptMode.Java;
			if (s.startsWith("public")) mode=ScriptMode.Java;
			if (s.startsWith("private")) mode=ScriptMode.Java;
			if (s.startsWith("protected")) mode=ScriptMode.Java;
		}
		return mode;
	}
}