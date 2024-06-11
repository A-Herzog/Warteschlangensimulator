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
	/**
	 * Callback für Ausgaben
	 */
	private final Consumer<String> outputCallback;

	/**
	 * Ausgangsmodell
	 */
	private final EditModel model;

	/**
	 * Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 */
	private final String editModelPath;

	/**
	 * Verändertes Modell
	 * @see #getChangedModel()
	 */
	private EditModel modelChanged;

	/**
	 * Stellt das Javascript-"Model"-Objekt zur Verfügung.
	 * @see #run(String)
	 */
	private JSRunComplexScriptModel modelJS;

	/**
	 * Stellt das Javascript-"Statistics"-Objekt zur Verfügung.
	 * @see #run(String)
	 */
	private JSCommandXML statisticsJS;


	/**
	 * Stellt das Javascript-"FileOutput"-Objekt zur Verfügung.
	 * @see #run(String)
	 */
	private JSCommandOutput fileJS;

	/**
	 * Erfolg der letzten Skriptausführung
	 * @see #getLastSuccess()
	 */
	private boolean lastSuccess;

	/**
	 * Ausgabe des Skriptes
	 * @see #getResults()
	 */
	private String lastResults;

	/**
	 * Wurde über das Ausgabesysteme ein Zahlenwert zurückgeliefert?
	 * @see #lastDouble
	 */
	private boolean isLastDouble;

	/**
	 * Über das Ausgabesystem zurückgelieferter Zahlenwert
	 */
	private double lastDouble;

	/**
	 * Konstruktor der Klasse
	 * @param model	Ausgangsmodell
	 * @param editModelPath	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 * @param outputCallback	Callback für Ausgaben
	 */
	public JSRunComplexScript(final EditModel model, final String editModelPath, final Consumer<String> outputCallback) {
		this.model=model;
		this.editModelPath=editModelPath;
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
	 * Liefert den Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen).
	 * @return	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 */
	public String getEditModelPath() {
		return editModelPath;
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
		this.statisticsJS.setXML(statistics.saveToXMLDocument(),statistics.loadedStatistics);
	}

	/**
	 * Führt ein Skript aus
	 * @param script	Auszuführendes Skript
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean run(final String script) {
		return run(script,null);
	}

	/**
	 * Führt ein Skript aus
	 * @param script	Auszuführendes Skript
	 * @param parameters	Zusätzliche Werte, die mit den Namen "Parameter1", "Parameter2" usw. im Skript zur Verfügung gestellt werden sollen
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean run(final String script, final double[] parameters) {
		final JSBuilder builder=new JSBuilder(1000*86_400,outputCallback);
		final JSCommandOutput output;
		builder.addBinding("System",new JSCommandSystem());
		builder.addBinding("Output",output=new JSCommandOutput(builder.output,false));
		builder.addBinding("Model",modelJS=new JSRunComplexScriptModel(builder.output,this));
		builder.addBinding("Statistics",statisticsJS=new JSCommandXML(builder.output,null,null,true));
		builder.addBinding("FileOutput",fileJS=new JSCommandOutput(builder.output,true));
		if (parameters!=null) for (int i=0;i<parameters.length;i++) builder.addBinding("Parameter"+(i+1),parameters[i]);

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
		isLastDouble=output.isOutputDouble();
		if (isLastDouble) lastDouble=output.getOutputDouble();
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
	 * @return	Erfolg der letzten Skriptausführung
	 */
	public boolean getLastSuccess() {
		return lastSuccess;
	}

	/**
	 * Wurde bei der letzten Skriptausführung über Output.print eine
	 * Zahl, die nun über {@link #getLastDouble()} abrufbar ist, bereitgestellt?
	 * @return	Ist in {@link #getLastDouble()} eine Zahl verfügbar?
	 */
	public boolean isOutputDouble() {
		return isLastDouble;

	}

	/**
	 * Liefert die bei der Ausführung über Output.print zurückgegebene Zahl.
	 * @return	Rückgabewert (über Output.print) des Skripts
	 */
	public double getLastDouble() {
		return lastDouble;
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
