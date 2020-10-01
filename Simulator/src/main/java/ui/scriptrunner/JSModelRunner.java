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
import scripting.java.ModelImpl;
import scripting.java.OutputImpl;
import scripting.java.StatisticsImpl;
import scripting.js.JSRunComplexScript;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import ui.script.ScriptEditorPanel;

/**
 * Führt ein Datenreihen-Javascript aus
 * @author Alexander Herzog
 * @see JSModelRunnerPanel
 */
public class JSModelRunner {
	private boolean canceled;
	private final ScriptEditorPanel.ScriptMode mode;
	private final String script;
	private final EditModel model;
	private final Consumer<String> outputNotify;
	private final Runnable doneNotify;

	private JSRunComplexScript scriptRunner;
	private DynamicRunner dynamicRunner;

	/**
	 * Konstruktor der Klasse <code>JSModelRunner</code>
	 * @param model	Editor-Modell auf dessen Basis die JS-Datenreihe erstellt werden soll
	 * @param mode	Skriptmodus
	 * @param script	Auszuführendes Skript
	 * @param outputNotify	Wird aufgerufen, wenn Log-Ausgaben erfolgen sollen.
	 * @param doneNotify	Wird aufgerufen, wenn die Skriptausführung absgeschlossen wurde.
	 */
	public JSModelRunner(final EditModel model, final ScriptEditorPanel.ScriptMode mode, final String script, final Consumer<String> outputNotify, final Runnable doneNotify) {
		canceled=false;
		this.model=model;
		this.mode=mode;
		this.script=script;
		this.outputNotify=outputNotify;
		this.doneNotify=doneNotify;
	}

	/**
	 * Prüft, ob das Modell simuliert werden kann.
	 * @return	Gibt <code>null</code> zurück, wenn das Modell in Ordnung ist, sonst eine Fehlermeldung.
	 */
	public String check() {
		if (model==null) return null;

		Object obj=RunModel.getRunModel(model,true);
		if (obj instanceof String) return (String)obj;

		return null;
	}

	/**
	 * Startet die Ausführung des Skripts.
	 */
	public void start() {
		final Thread thread=new Thread(()->{
			switch (mode) {
			case Javascript:
				scriptRunner=new JSRunComplexScript(model,line->output(line));
				scriptRunner.run(script);
				break;
			case Java:
				final DynamicRunner runner=DynamicFactory.getFactory().load(script);
				if (runner.getStatus()!=DynamicStatus.OK) {
					output(DynamicFactory.getLongStatusText(runner));
				} else {
					runner.parameter.output=new OutputImpl(line->output(line),false);
					runner.parameter.fileoutput=new OutputImpl(line->output(line),true);
					runner.parameter.statistics=new StatisticsImpl(line->output(line),null,true);
					if (model!=null) {
						runner.parameter.model=new ModelImpl(line->output(line),model,runner.parameter.statistics);
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
	 * Bricht die laufende Skriptausführung ab.
	 */
	public void cancel() {
		canceled=true;
		if (scriptRunner!=null) scriptRunner.cancel();
		if (dynamicRunner!=null) {
			dynamicRunner.parameter.output.cancel();
			dynamicRunner.parameter.fileoutput.cancel();
			dynamicRunner.parameter.model.cancel();
			dynamicRunner.parameter.statistics.cancel();
		}
	}

	private void output(final String text) {
		if (canceled || outputNotify==null) return;
		SwingUtilities.invokeLater(()->outputNotify.accept(text));
	}

	private void done() {
		if (canceled || doneNotify==null) return;
		SwingUtilities.invokeLater(doneNotify);
	}
}
