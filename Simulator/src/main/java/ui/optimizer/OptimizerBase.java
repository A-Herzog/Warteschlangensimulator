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
package ui.optimizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.w3c.dom.Document;

import language.Language;
import mathtools.NumberTools;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.OutputImpl;
import scripting.java.StatisticsImpl;
import scripting.js.JSCommandXML;
import scripting.js.JSOutputWriter;
import scripting.js.JSRunDataFilter;
import scripting.js.JSRunDataFilterTools;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import ui.optimizer.OptimizerSetup.ControlVariable;
import ui.script.ScriptPanel;
import xml.XMLTools;

/**
 * Basisklasse für alle Optimiererimplementierungen
 * @author Alexander Herzog
 */
public abstract class OptimizerBase {
	/** Callback, welches aufgerufen wird, wenn Statusinformationen ausgegeben werden sollen. */
	private Consumer<String> logOutput;
	/** Callback, welches aufgerufen wird, wenn die Optimierung beendet wurde (egal ob erfolgreich oder per Abbruch-Knopf) */
	private Consumer<Boolean> whenDone;
	/** Callback, welches aufgerufen wird, wenn ein Optimierungsschritt abgeschlossen wurde */
	private Runnable whenStepDone;
	/** Ausgabeverzeichnis für die Optimiererergebnisse */
	private File outputFolder;
	/** Ergebnisauswertung: Skript ausführen */
	private String targetScript;
	/** Ergebnisauswertung: XML-Eintrag auswerten */
	private String targetXML;
	/** Liste der Ergebnisse der einzelnen Optimierungs-Teilschritte */
	private List<OptimizationRunResults> optimizationRunResultsList;

	/**
	 * Ausgangsmodell
	 */
	protected EditModel model;

	/**
	 * Setup des Optimierers
	 */
	protected OptimizerSetup setup;

	/**
	 * Konstruktor der Klasse
	 */
	public OptimizerBase() {
		optimizationRunResultsList=new ArrayList<>();
	}

	/**
	 * Name des Optimierers in der aktuellen Sprache
	 * @return	Name des Optimierers in der aktuellen Sprache
	 */
	public abstract String getName();

	/**
	 * Alle Name des Optimierers (d.h. in allen Sprachen)
	 * @return	Alle Name des Optimierers
	 */
	protected abstract String[] getNames();

	/**
	 * Prüft, ob ein Optimierer durch einen bestimmten Namen (in einer beliebigen Sprache) angesprochen wird.
	 * @param name	Name, bei dem geprüft werden soll, ob er zu dem Optimierer passt
	 * @return	Gibt <code>true</code> zurück, wenn der Name zu dem aktuellen Optimierer passt
	 */
	public final boolean matchName(final String name) {
		for (String test: getNames()) if (test.equalsIgnoreCase(name)) return true;
		return false;
	}

	/**
	 * Erstellt eine Kopie des Optimierer-Objektes.<br>
	 * Diese Methode darf nur vor der Initialisierung per {@link #check(EditModel, OptimizerSetup, Consumer, Consumer, Runnable)} aufgerufen werden bzw. kopiert nur das Basis-Objekt.
	 */
	@Override
	public abstract OptimizerBase clone();

	/**
	 * Prüft die Konfiguration und initialisiert den Optimierer
	 * @param model	Basis-Editor-Modell für die Optimierung
	 * @param setup	Optimierer-Setup
	 * @param logOutput	Callback, welches aufgerufen wird, wenn Statusinformationen ausgegeben werden sollen.
	 * @param whenDone	Callback, welches aufgerufen wird, wenn die Optimierung beendet wurde (egal ob erfolgreich oder per Abbruch-Knopf)
	 * @param whenStepDone	Callback, welches aufgerufen wird, wenn ein Optimierungsschritt abgeschlossen wurde
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public String check(final EditModel model, final OptimizerSetup setup, final Consumer<String> logOutput, final Consumer<Boolean> whenDone, final Runnable whenStepDone) {
		this.model=model.clone();
		this.setup=setup.clone();
		this.logOutput=logOutput;
		this.whenDone=whenDone;
		this.whenStepDone=whenStepDone;

		if (setup.controlVariables.size()==0) return Language.tr("Optimizer.Error.NoControlVariables");
		for (int i=0;i<setup.controlVariables.size();i++) {
			final ControlVariable controlVariable=setup.controlVariables.get(i);
			switch (controlVariable.mode) {
			case MODE_RESOURCE:
				if (controlVariable.tag==null || controlVariable.tag.trim().isEmpty()) return String.format(Language.tr("Optimizer.Error.NoResourceNameForControlVariables"),i+1);
				if (!OptimizerSetup.isResourceNameOk(model,controlVariable.tag)) return String.format(Language.tr("Optimizer.Error.ResourceNameForControlVariablesInvalid"),i+1,controlVariable.tag);
				break;
			case MODE_VARIABLE:
				if (controlVariable.tag==null || controlVariable.tag.trim().isEmpty()) return String.format(Language.tr("Optimizer.Error.NoVariableNameForControlVariables"),i+1);
				if (!OptimizerSetup.isGlobalVariableOk(model,controlVariable.tag)) return String.format(Language.tr("Optimizer.Error.VariableNameForControlVariablesInvalid"),i+1,controlVariable.tag);
				break;
			case MODE_XML:
				if (controlVariable.tag==null || controlVariable.tag.trim().isEmpty()) return String.format(Language.tr("Optimizer.Error.NoXMLTagForControlVariables"),i+1);
				break;
			}
		}

		if (setup.outputFolder==null || setup.outputFolder.trim().isEmpty()) return Language.tr("Optimizer.Error.NoOutputFolder");
		outputFolder=new File(setup.outputFolder);
		if (!outputFolder.isDirectory()) return String.format(Language.tr("Optimizer.Error.OutputFolderDoesNotExists"),setup.outputFolder);


		if (setup.target==null || setup.target.trim().isEmpty()) return Language.tr("Optimizer.Error.NoTarget");
		if (setup.targetType==OptimizerSetup.TargetType.TARGET_TYPE_SCRIPT) {
			File file=new File(setup.target);
			if (!file.isFile()) return String.format(Language.tr("Optimizer.Error.TargetScriptDoesNotExists"),setup.target);
			targetScript=JSRunDataFilterTools.loadText(file);
			if (targetScript==null) return String.format(Language.tr("Optimizer.Error.TargetScriptCouldNotBeLoaded"),setup.target);
			targetXML=null;
		} else {
			targetScript=null;
			targetXML=setup.target;
		}

		return null;
	}

	/**
	 * Liefert den Dateinamen der nächsten Statistikdatei zum
	 * Speichern von Ergebnissen
	 * @return	Nächster verwendbarer Statistikdateiname
	 */
	protected final File getNextStatisticFile() {
		if (outputFolder==null) return null;

		int i=0;
		File file=null;
		while (file==null || file.exists()) {
			i++;
			if (i>9999) return null;
			file=new File(outputFolder,String.format(Language.tr("Optimizer.OutputFileFormat"),i));
		}
		return file;
	}

	/**
	 * Speichert die Statistikdaten als xml-Datei
	 * @param doc	Statistikdaten-xml-Dokument
	 * @return	Liefert im Erfolgsfall den Namen der Datei, in der die Daten gespeichert wurden
	 */
	protected final File saveStatistics(final Document doc) {
		final File file=getNextStatisticFile();
		if (file==null) {
			logOutput("  "+Language.tr("Optimizer.Error.NoOutputFileCouldBeDetermined"));
			return null;
		} else {
			final XMLTools xml=new XMLTools(file);
			if (xml.save(doc.getDocumentElement())) {
				logOutput(String.format("  "+Language.tr("Optimizer.ResultsSaved"),file.getName()));
				return file;
			} else {
				logOutput(String.format("  "+Language.tr("Optimizer.Error.CouldNotSaveResults"),file.getName()));
				return null;
			}
		}
	}

	/**
	 * Liefert basierend auf den Statistikdaten den Zielwert
	 * @param statistics	Statistik, aus der der Zielwert ausgelesen werden soll
	 * @return	Zielwert oder <code>null</code>, wenn ein Fehler aufgetreten ist
	 */
	protected final Double checkTarget(final Statistics statistics) {
		return checkTarget(statistics.saveToXMLDocument());
	}

	/**
	 * Liefert basierend auf den Statistikdaten den Zielwert
	 * @param doc	XML-Statistik-Dokument, aus der der Zielwert ausgelesen werden soll
	 * @return	Zielwert oder <code>null</code>, wenn ein Fehler aufgetreten ist
	 */
	protected final Double checkTarget(final Document doc) {
		if (targetXML!=null) return checkTargetXML(doc);
		if (targetScript!=null) return checkTargetScript(doc);
		return 0.0;
	}

	/**
	 * Wendet {@link #targetXML} auf die Statistikergebnisse an und liefert
	 * den so ermittelten Zielwert zurück.
	 * @param doc	Statistikergebnisse
	 * @return	Zielwert
	 * @see #targetXML
	 * @see #checkTarget(Document)
	 */
	private Double checkTargetXML(final Document doc) {
		try (final JSOutputWriter output=new JSOutputWriter(line->logOutput(line))) {
			final JSCommandXML command=new JSCommandXML(output,doc,null,false);
			final Object result=command.xmlNumber(targetXML);
			if (result instanceof Double) return (Double)result;
			if (result instanceof String) {
				logOutput((String)result);
				return null;
			}
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Wendet das Skript {@link #targetScript} auf die Statistikergebnisse an und liefert
	 * den so ermittelten Zielwert zurück.
	 * @param doc	Statistikergebnisse
	 * @return	Zielwert
	 * @see #targetScript
	 * @see #checkTarget(Document)
	 */
	private Double checkTargetScript(final Document doc) {
		Double D;

		switch (ScriptPanel.getScriptType(targetScript)) {
		case Javascript:
			final JSRunDataFilter filter=new JSRunDataFilter(doc,null);
			filter.run(targetScript);
			if (!filter.getLastSuccess()) {
				logOutput(String.format("  "+Language.tr("Optimizer.Error.ErrorExecutingScript")+":\n%s",filter.getResults()));
				return null;
			}
			final String filterResults=filter.getResults().trim();
			D=NumberTools.getDouble(filterResults);
			if (D==null) logOutput(String.format("  "+Language.tr("Optimizer.Error.DataCouldNotBeInterpretedAsNumber"),filterResults));
			return D;
		case Java:
			final DynamicRunner runner=DynamicFactory.getFactory().load(targetScript);
			final StringBuilder results=new StringBuilder();
			runner.parameter.output=new OutputImpl(line->results.append(line),false);
			runner.parameter.statistics=new StatisticsImpl(line->results.append(line),doc,null,false);
			if (runner.getStatus()!=DynamicStatus.OK) {
				logOutput(String.format("  "+Language.tr("Optimizer.Error.ErrorExecutingScript")+":\n%s",DynamicFactory.getLongStatusText(runner)));
				return null;
			}
			runner.run();
			if (runner.getStatus()!=DynamicStatus.OK) {
				logOutput(String.format("  "+Language.tr("Optimizer.Error.ErrorExecutingScript")+":\n%s",DynamicFactory.getLongStatusText(runner)));
				return null;
			}
			D=NumberTools.getDouble(results.toString().trim());
			if (D==null) logOutput(String.format("  "+Language.tr("Optimizer.Error.DataCouldNotBeInterpretedAsNumber"),results.toString()));
			return D;
		default:
			return null;
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Textzeile in der Log-Ausgabe erscheinen soll.
	 * @param text	Als Log-Information auszugebende Textzeile
	 */
	protected final synchronized void logOutput(final String text) {
		if (logOutput!=null) logOutput.accept(text);
	}

	/**
	 * Ausgabe der Werte der Kontrollvariablen
	 * @param indent	Zeichenkette, die zum Einrücken der Zeilen verwendet werden soll
	 * @param controlValues	Werte der Kontrollvariablen
	 */
	protected final void outputControlVariables(final String indent, final double[] controlValues) {
		for (int i=0;i<controlValues.length;i++) {
			logOutput(indent+String.format(Language.tr("Optimizer.ControlVariableValue"),i+1,NumberTools.formatNumber(controlValues[i])));
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn die Optimierung abgeschlossen oder abgebrochen wurde.
	 * @param optimizationCompleted	Gibt an, ob die Optimierung erfolgreich abgeschlossen wurde (und nicht abgebrochen wurde).
	 */
	protected final synchronized void done(final boolean optimizationCompleted) {
		if (whenDone!=null) whenDone.accept(optimizationCompleted);
	}

	/**
	 * Startet die Optimierung
	 */
	public abstract void start();

	/**
	 * Bricht die Optimierung ab.
	 */
	public abstract void cancel();

	/**
	 * Fügt das Ergebnis eines Optimierungsschritts zur Liste der Ergebnisse hinzu
	 * @param value	Neues Ergebnis
	 * @see #getResults()
	 */
	protected final void addOptimizationRunResults(final double value) {
		optimizationRunResultsList.add(new OptimizationRunResults(value));
		if (whenStepDone!=null) whenStepDone.run();
	}

	/**
	 * Fügt die Ergebnisse eines Optimierungsschritts zur Liste der Ergebnisse hinzu
	 * @param values	Ergebniswerte des aktuellen Schritts
	 * @param usedForNextStep	Welche der zu diesen Ergebnissen gehörenden Modelle werden in der nächsten Runde als Elterngeneration verwendet?
	 * @see #getResults()
	 */
	protected final void addOptimizationRunResults(final double[] values, final boolean[] usedForNextStep) {
		optimizationRunResultsList.add(new OptimizationRunResults(values,usedForNextStep));
		if (whenStepDone!=null) whenStepDone.run();
	}

	/**
	 * Liefert die Liste der bisherigen Ergebnisse für die Anzeige im Ergebnisdiagramm
	 * @return	Liste der bisherigen Ergebnisse
	 */
	public final List<OptimizationRunResults> getResults() {
		return optimizationRunResultsList;
	}

	/**
	 * Liefert die Ergebniswerte eines einzelnen Optimierungsschritts
	 * @author Alexander Herzog
	 * @see OptimizerBase#getResults()
	 */
	public class OptimizationRunResults {
		/**
		 * Ergebnisse der aktuellen Runde
		 */
		public final double[] values;

		/**
		 * Welche der zu diesen Ergebnissen gehörenden Modelle werden in der nächsten Runde als Elterngeneration verwendet?
		 */
		public final boolean[] usedForNextStep;

		/**
		 * Konstruktor der Klasse
		 * @param value	Ergebnis der aktuellen Runde
		 */
		public OptimizationRunResults(final double value) {
			values=new double[]{value};
			usedForNextStep=new boolean[]{true};
		}

		/**
		 * Konstruktor der Klasse
		 * @param values	Ergebnisse der aktuellen Runde
		 * @param usedForNextStep	Welche der zu diesen Ergebnissen gehörenden Modelle werden in der nächsten Runde als Elterngeneration verwendet?
		 */
		public OptimizationRunResults(final double[] values, final boolean[] usedForNextStep) {
			this.values=values;
			this.usedForNextStep=usedForNextStep;
		}
	}
}
