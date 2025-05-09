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

import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.ImportSettingsBuilder;
import scripting.java.ModelImpl;
import scripting.java.OutputImpl;
import scripting.java.StatisticsImpl;
import scripting.js.JSRunComplexScript;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import tools.SetupData;
import ui.script.ScriptEditorPanel;

/**
 * F�hrt ein Datenreihen-Javascript aus
 * @author Alexander Herzog
 * @see JSModelRunnerPanel
 */
public class JSModelRunner {
	/** Wurde die Verarbeitung abgebrochen? */
	private boolean canceled;
	/** Skriptmodus */
	private final ScriptEditorPanel.ScriptMode mode;
	/** Auszuf�hrendes Skript */
	private final String script;
	/** Editor-Modell auf dessen Basis die JS-Datenreihe erstellt werden soll */
	private final EditModel model;
	/** Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen) */
	private final String editModelPath;
	/** Einstellungen zu Import und Classpath f�r Skripte */
	private final ImportSettingsBuilder scriptSettings;
	/** Wird aufgerufen, wenn Log-Ausgaben erfolgen sollen. */
	private final Consumer<String> outputNotify;
	/** Wird aufgerufen, wenn die Skriptausf�hrung abgeschlossen wurde. */
	private final Runnable doneNotify;

	/**
	 * F�hrt die eigentliche Javascript-Verarbeitung durch.
	 */
	private JSRunComplexScript scriptRunner;

	/**
	 * F�hrt die eigentliche Java-Verarbeitung durch.
	 */
	private DynamicRunner dynamicRunner;

	/**
	 * Konstruktor der Klasse <code>JSModelRunner</code>
	 * @param model	Editor-Modell auf dessen Basis die JS-Datenreihe erstellt werden soll
	 * @param editModelPath	Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen)
	 * @param mode	Skriptmodus
	 * @param script	Auszuf�hrendes Skript
	 * @param outputNotify	Wird aufgerufen, wenn Log-Ausgaben erfolgen sollen.
	 * @param doneNotify	Wird aufgerufen, wenn die Skriptausf�hrung abgeschlossen wurde.
	 */
	public JSModelRunner(final EditModel model, final String editModelPath, final ScriptEditorPanel.ScriptMode mode, final String script, final Consumer<String> outputNotify, final Runnable doneNotify) {
		canceled=false;
		this.model=model;
		this.editModelPath=editModelPath;
		scriptSettings=new ImportSettingsBuilder(model);
		this.mode=mode;
		this.script=script;
		this.outputNotify=outputNotify;
		this.doneNotify=doneNotify;
	}

	/**
	 * Pr�ft, ob das Modell simuliert werden kann.
	 * @return	Gibt <code>null</code> zur�ck, wenn das Modell in Ordnung ist, sonst eine Fehlermeldung.
	 */
	public String check() {
		if (model==null) return null;

		Object obj=RunModel.getRunModel(model,editModelPath,true,SetupData.getSetup().useMultiCoreSimulation);
		if (obj instanceof StartAnySimulator.PrepareError) return ((StartAnySimulator.PrepareError)obj).error;

		return null;
	}

	/**
	 * Startet die Ausf�hrung des Skripts.
	 */
	public void start() {
		final Thread thread=new Thread(()->{
			pendingOutput.setLength(0);
			switch (mode) {
			case Javascript:
				scriptRunner=new JSRunComplexScript(model,editModelPath,line->output(line));
				scriptRunner.run(script);
				break;
			case Java:
				final DynamicRunner runner=DynamicFactory.getFactory().load(script,scriptSettings);
				if (runner.getStatus()!=DynamicStatus.OK) {
					output(DynamicFactory.getLongStatusText(runner));
				} else {
					runner.parameter.output=new OutputImpl(line->output(line),false);
					runner.parameter.fileoutput=new OutputImpl(line->output(line),true);
					runner.parameter.statistics=new StatisticsImpl(line->output(line),null,null,true);
					if (model!=null) {
						runner.parameter.model=new ModelImpl(line->output(line),model,editModelPath,runner.parameter.statistics);
					}
					runner.run();
					if (runner.getStatus()!=DynamicStatus.OK) output(DynamicFactory.getLongStatusText(runner));
					dynamicRunner=runner;
				}
				break;
			}
			done();
		});
		thread.start();
	}

	/**
	 * Bricht die laufende Skriptausf�hrung ab.
	 */
	public void cancel() {
		canceled=true;
		if (scriptRunner!=null) scriptRunner.cancel();
		if (dynamicRunner!=null) {
			if (dynamicRunner.parameter.output!=null) dynamicRunner.parameter.output.cancel();
			if (dynamicRunner.parameter.fileoutput!=null) dynamicRunner.parameter.fileoutput.cancel();
			if (dynamicRunner.parameter.model!=null) dynamicRunner.parameter.model.cancel();
			if (dynamicRunner.parameter.statistics!=null) dynamicRunner.parameter.statistics.cancel();
		}
		pendingOutput.setLength(0);
	}

	/**
	 * Zeitpunkt der letzten Ausgabe �ber {@link #outputNotify}
	 * @see #output(String)
	 */
	private long lastOutput;

	/**
	 * Zwischengespeicherter Text f�r {@link #outputNotify}
	 * @see #output(String)
	 */
	private StringBuilder pendingOutput=new StringBuilder();

	/**
	 * Minimaler Zeitabstand (in ms) zwischen zwei Ausgaben an {@link #outputNotify}
	 * @see #output(String)
	 */
	private static final long OUTPUT_MIN_DELTA=1_000;

	/**
	 * Gibt eine Nachricht �ber {@link #outputNotify} aus.
	 * @param text	Auszugebende Meldung
	 * @see #outputNotify
	 */
	private void output(final String text) {
		if (canceled || outputNotify==null) return;

		final long currentTime=System.currentTimeMillis();
		if (currentTime-lastOutput<OUTPUT_MIN_DELTA) {
			pendingOutput.append(text);
		} else {
			if (pendingOutput.length()>0) {
				final String fullText=pendingOutput.toString()+text;
				SwingUtilities.invokeLater(()->outputNotify.accept(fullText));
				pendingOutput.setLength(0);
			} else {
				SwingUtilities.invokeLater(()->outputNotify.accept(text));
			}
			lastOutput=currentTime;
		}
	}

	/**
	 * Ruft {@link #doneNotify} im Kontext des Swing-Threads auf.
	 * @see #doneNotify
	 */
	private void done() {
		if (canceled || doneNotify==null) return;

		if (pendingOutput.length()>0) {
			final String fullText=pendingOutput.toString();
			SwingUtilities.invokeLater(()->outputNotify.accept(fullText));
			pendingOutput.setLength(0);
		}

		SwingUtilities.invokeLater(doneNotify);
	}
}
