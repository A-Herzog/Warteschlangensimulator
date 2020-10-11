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
package start;

import java.io.File;
import java.io.PrintStream;

import org.w3c.dom.Document;

import language.Language;
import mathtools.NumberTools;
import simulator.AnySimulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import ui.commandline.AbstractSimulationCommand;

/**
 * Diese Klasse ermöglicht es, Simulationen von anderen Java-Klassen aus durchzuführen,
 * d.h. den Warteschlangensimulator in andere Java-Programme einzubetten.
 * @author Alexander Herzog
 */
public class Simulator {
	/**
	 * Zu ladende Datei, die das xml-Modell enthält
	 */

	private File editModelFile;
	/**
	 * Editor-Modell (welches aus {@link #editModelFile} geladen wurde)
	 * @see #editModelFile
	 */

	private EditModel editModel;
	/** Statistikergebnisse
	 * @see #getStatistics()
	 */
	private Statistics statistics;

	/**
	 * Konstruktor der Klasse
	 */
	public Simulator() {
		/* System initialisieren */
		Main.prepare();
	}

	/**
	 * Lädt ein Modell in die Klasse
	 * @param doc	Zu ladendes xml-Modell
	 * @return	Liefert im Erfolgsfall <code>null</code> und im Fehlerfall eine Meldung
	 */
	public String loadModel(final Document doc) {
		if (doc==null) return Language.tr("XML.Runner.NoDocument");
		editModel=new EditModel();
		final String error=editModel.loadFromXML(doc.getDocumentElement());
		if (error!=null) editModel=null;
		return error;
	}

	/**
	 * Lädt ein Modell in die Klasse
	 * @param file	Zu ladende Datei, die das xml-Modell enthält
	 * @return	Liefert im Erfolgsfall <code>null</code> und im Fehlerfall eine Meldung
	 */
	public String loadModel(final File file) {
		if (file==null) return Language.tr("XML.Runner.NoFile");
		editModel=new EditModel();
		final String error=editModel.loadFromFile(file);
		if (error!=null) editModel=null;
		editModelFile=file;
		return error;
	}

	/**
	 * Führt die eigentliche Simulation durch.<br>
	 * Voraussetzung dafür ist, dass zuvor über {@link #loadModel(Document)} oder
	 * über {@link #loadModel(File)} erfolgreich ein Modell geladen wurde.
	 * @param out	Über diesem Stream werden während der Simulation anzuzeigende Meldungen ausgegeben. (Darf <code>null</code> sein.)
	 * @return	Liefert im Erfolgsfall <code>null</code> und im Fehlerfall eine Meldung
	 */
	public String runSimulation(final PrintStream out) {
		if (editModel==null) return Language.tr("XML.Runner.NoModel");

		statistics=null;

		/* Vorbereiten und starten */
		File parentFolder=null;
		if (editModelFile!=null) parentFolder=editModelFile.getParentFile();
		final EditModel changedEditModel=editModel.modelLoadData.changeModel(editModel,parentFolder);
		if (changedEditModel!=null) {
			AbstractSimulationCommand.outputModelLoadDataWarnings(editModel.modelLoadData.getChangeWarnings(),out);
			editModel=changedEditModel;
		}
		final Object obj=AbstractSimulationCommand.prepare(Integer.MAX_VALUE,editModel,out);
		if (!(obj instanceof AnySimulator)) return (String)obj;
		final AnySimulator simulator=(AnySimulator)obj;

		/* Auf Ende der Simulation warten */
		AbstractSimulationCommand.waitForSimulationDone(simulator,false,out);

		/* Statistik zusammenstellen */
		statistics=simulator.getStatistic();
		if (statistics==null) {
			if (out!=null) {
				out.println(Language.tr("CommandLine.Simulation.NoResults"));
				return Language.tr("CommandLine.Simulation.NoResults");
			}
		} else {
			if (out!=null) out.println(String.format(Language.tr("CommandLine.Simulation.Done"),NumberTools.formatLong(statistics.simulationData.runTime)));
		}

		return null;
	}

	/**
	 * Liefert nach der erfolgreichen Ausführung von {@link #runSimulation(PrintStream)}
	 * die Ergebnisse der Simulation als xml-Dokument zurück.
	 * @return	xml-Dokument mit den Ergebnissen oder <code>null</code>, wenn keine Statistikergebnisse vorliegen
	 */
	public Document getStatistics() {
		if (statistics==null) return null;
		return statistics.saveToXMLDocument();
	}

	/**
	 * Speichert nach der erfolgreichen Ausführung von {@link #runSimulation(PrintStream)}
	 * die Ergebnisse der Simulation als xml-Datei.
	 * @param file	Datei in der die Ergebnisse gespeichert werden sollen.
	 * @return	Gibt an, ob das Speichern erfolgreich war.
	 */
	public boolean saveStatistics(final File file) {
		if (statistics==null) return false;
		return statistics.saveToFile(file);
	}
}
