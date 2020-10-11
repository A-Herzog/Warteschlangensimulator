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
package scripting.js;

import java.util.function.Consumer;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;

/**
 * Javascript-Interpreter zur Durchführung von mehreren verknüpften Simulationsläufen
 * @author Alexander Herzog
 */
public class JSRunComplexScript {
	/** Callback für Ausgaben */
	private final Consumer<String> outputCallback;
	/** Ausgangsmodell */
	private final EditModel model;
	private EditModel modelChanged;
	private JSRunComplexScriptModel modelJS;
	private JSCommandXML statisticsJS;
	private JSCommandOutput fileJS;

	private boolean lastSuccess;
	private String lastResults;

	/**
	 * Konstruktor der Klasse
	 * @param model	Ausgangsmodell
	 * @param outputCallback	Callback für Ausgaben
	 */
	public JSRunComplexScript(final EditModel model, final Consumer<String> outputCallback) {
		this.model=model;
		this.outputCallback=outputCallback;
		this.modelChanged=model;
		lastSuccess=false;
		lastResults="";
	}

	/**
	 * Liefert das veränderte Modell.
	 * @return	Verändertes Modell
	 */
	public EditModel getChangedModel() {
		return modelChanged;
	}

	/**
	 * Setzt ein neues verändertes Modell.
	 * @param model	Verändertes Modell
	 */
	public void setChangedModel(final EditModel model) {
		if (model==null) modelChanged=this.model.clone(); else modelChanged=model.clone();
	}

	/**
	 * Stellt die Statistikdaten, die im JS-Skript zur Verfügung stehen sollen, ein
	 * @param statistics	Statistikdaten für die Nutzung im JS-Skript
	 */
	public void setStatistics(final Statistics statistics) {
		this.statisticsJS.setXML(statistics.saveToXMLDocument());
	}

	/**
	 * Führt ein Skript aus
	 * @param script	Auszuführendes Skript
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean run(final String script) {
		final JSBuilder builder=new JSBuilder(100*86_400,outputCallback);
		builder.addBinding("System",new JSCommandSystem());
		builder.addBinding("Output",new JSCommandOutput(builder.output,false));
		builder.addBinding("Model",modelJS=new JSRunComplexScriptModel(builder.output,this));
		builder.addBinding("Statistics",statisticsJS=new JSCommandXML(builder.output,null,true));
		builder.addBinding("FileOutput",fileJS=new JSCommandOutput(builder.output,true));

		final JSEngine runner=builder.build();
		if (runner==null) {
			lastResults=String.format(Language.tr("Statistics.Filter.EngineInitError"),builder.engineName.name);
			return false;
		}

		if (!runner.initScript(script)) {
			lastResults=Language.tr("Statistics.Filter.ScriptInitError");
			return false;
		}
		lastSuccess=runner.run();
		lastResults=runner.getResult();
		return lastSuccess;
	}

	/**
	 * Liefert die Ausgabe des Skriptes.
	 * @return	Ausgabe des Skriptes
	 */
	public String getResults() {
		return lastResults;
	}

	/**
	 * Gibt an, ob die letzte Skriptausführung erfolgreich war.
	 * @return	Erfolg der letzten	Skriptausführung
	 */
	public boolean getLastSuccess() {
		return lastSuccess;
	}

	/**
	 * Bricht die Skriptausführung ab.
	 */
	public synchronized void cancel() {
		modelJS.cancel();
		statisticsJS.cancel();
		fileJS.cancel();
	}
}
