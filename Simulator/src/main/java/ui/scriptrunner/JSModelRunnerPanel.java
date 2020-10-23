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
package ui.scriptrunner;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.MsgBox;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.script.ScriptEditorPanel;
import ui.script.ScriptPanel;
import ui.tools.SpecialPanel;

/**
 * Ermöglicht die skriptbasierende Stapelverarbeitung
 * @author Alexander Herzog
 */
public class JSModelRunnerPanel extends SpecialPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7960967403157448841L;

	/** Editor-Modell auf dessen Basis die Skript-Datenreihe erstellt werden soll */
	private final EditModel model;

	/** Schaltfläche "Start" */
	private final JButton startButton;
	/** Schaltfläche "Hilfe" */
	private final JButton helpButton;

	private ScriptPanel scriptPanel;
	private JSModelRunner runner;

	/** Anzeige als vollwertigem Skriptrunner, der das Fenster als alleiniges Element füllt, (<code>true</code>) oder als einfaches Panel z.B. als Tab innerhalb eines Dialogs (<code>false</code>). */
	private final boolean fullMode;

	/**
	 * Konstruktor der Klasse <code>JSModelRunnerPanel</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param model	Editor-Modell auf dessen Basis die Skript-Datenreihe erstellt werden soll
	 * @param miniStatistics	Minimales Statistik-Objekt, um XML-Elemente auswählen zu können
	 * @param doneNotify	Wird aufgerufen, wenn sich das Panel schließen möchte
	 * @param fullMode	Anzeige als vollwertigem Skriptrunner, der das Fenster als alleiniges Element füllt, (<code>true</code>) oder als einfaches Panel z.B. als Tab innerhalb eines Dialogs (<code>false</code>).
	 */
	public JSModelRunnerPanel(final Window owner, final EditModel model, final Statistics miniStatistics, final Runnable doneNotify, final boolean fullMode) {
		super(doneNotify);
		this.model=model;
		this.fullMode=fullMode;

		/* Haupttoolbar */
		startButton=addUserButton(Language.tr("JSRunner.Toolbar.Start"),Language.tr("JSRunner.Toolbar.Start.Hint"),Images.SCRIPT_RUN.getURL());
		if (fullMode) {
			addSeparator();
			addCloseButton();
			addSeparator();
			helpButton=addUserButton(Language.tr("Main.Toolbar.Help"),Language.tr("Main.Toolbar.Help.Hint"),Images.HELP.getURL());
		} else {
			helpButton=null;
		}

		final JPanel content=new JPanel(new BorderLayout());
		add(content,BorderLayout.CENTER);

		if (fullMode) {
			InfoPanel.addTopPanel(content,InfoPanel.globalScriptRunner);
		}

		content.add(scriptPanel=new ScriptPanel(model,false,ScriptEditorPanel.featuresScriptRunner,null) {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = -4514089397823686283L;
			@Override protected EditModel getModel() {return model;}
			@Override protected Statistics getMiniStatistics() {return miniStatistics;}
			@Override protected Runnable getHelpRunnable() {return ()->Help.topicModal(JSModelRunnerPanel.this,"JSRunner");}
		},BorderLayout.CENTER);

		/* F1-Hotkey */
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"),"actionHelp");
		getActionMap().put("actionHelp",new AbstractAction("actionHelp") {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = -7962315945186700028L;
			@Override public void actionPerformed(ActionEvent event) {Help.topicModal(JSModelRunnerPanel.this,"JSRunner");}
		});

		/* Skript laden */
		if (fullMode) {
			final SetupData setup=SetupData.getSetup();
			scriptPanel.setEditorScript(setup.javascript);
		}
	}

	@Override
	public void requestClose() {
		if (runner!=null) commandRun(false); else close();
	}

	@Override
	protected void close() {
		if (fullMode) {
			final SetupData setup=SetupData.getSetup();
			setup.javascript=scriptPanel.getEditorScript();
			setup.saveSetup();
		}

		super.close();
	}

	private void commandRun(final boolean start) {
		if ((runner!=null)==start) return;

		if (start) {
			/* Skript speichern */
			if (fullMode) {
				final SetupData setup=SetupData.getSetup();
				setup.javascript=scriptPanel.getEditorScript();
				setup.saveSetup();
			}

			final JSModelRunner newRunner=new JSModelRunner(model,scriptPanel.getEditorMode(),scriptPanel.getEditorScript(),text->scriptPanel.addOutput(text),()->commandRun(false));
			String error=newRunner.check();
			if (error!=null) {
				MsgBox.error(this,Language.tr("Dialog.Title.Error"),error);
				return;
			}
			runner=newRunner;

			scriptPanel.clearOutput();
			scriptPanel.showResultsTab();

			runner.start();
		} else {
			/* Verarbeitung abbrechen */
			if (runner!=null) {
				runner.cancel();
				runner=null;
			}
		}

		/* GUI passend einstellen */

		if (start) {
			startButton.setText(Language.tr("JSRunner.Toolbar.Cancel"));
			startButton.setToolTipText(Language.tr("JSRunner.Toolbar.Cancel.Hint"));
			startButton.setIcon(Images.SCRIPT_CANCEL.getIcon());
		} else {
			startButton.setText(Language.tr("JSRunner.Toolbar.Start"));
			startButton.setToolTipText(Language.tr("JSRunner.Toolbar.Start.Hint"));
			startButton.setIcon(Images.SCRIPT_RUN.getIcon());
		}

		scriptPanel.setEnabledGUI(!start);

		setWaitIndicatorVisible(start);
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		if (button==startButton) {
			commandRun(runner==null);
			return;
		}

		if (button==helpButton) {
			Help.topicModal(JSModelRunnerPanel.this,"JSRunner");
			return;
		}
	}
}