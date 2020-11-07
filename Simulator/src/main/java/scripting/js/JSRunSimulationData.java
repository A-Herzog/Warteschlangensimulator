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

import java.util.List;

import language.Language;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Javascript-Interpreter zur Verarbeitung von Daten w�hrend der Simulation
 * @author Alexander Herzog
 */
public class JSRunSimulationData {
	/**
	 * Stellt das Javascript-"Simulation"-Objekt zur Verf�gung.
	 * @see #compile(String)
	 */
	private final JSCommandSystem simulatorCommand;

	/**
	 * Soll es ein JS-"Output"-Objekt geben?
	 */
	private final boolean hasOutput;

	/**
	 * Soll es ein JS-"Clients"-Objekt geben?
	 */
	private final boolean hasMultiClientData;

	/**
	 * Stellt das Javascript-"Output"-Objekt zur Verf�gung.
	 * @see #compile(String)
	 */
	private JSCommandOutput outputCommand;

	/**
	 * Stellt das Javascript-"Clients"-Objekt zur Verf�gung.
	 * @see #compile(String)
	 */
	private JSCommandClients clientsCommand;

	/**
	 * Scripting-System das die eigentliche Verarbeitung �bernimmt
	 */
	private JSEngine runner;

	/**
	 * War die letzte Skriptausf�hrung erfolgreich?
	 * @see #getLastSuccess()
	 */
	private boolean lastSuccess;

	/**
	 * Konstruktor der Klasse
	 * @param hasOutput	Soll es ein JS-"Output"-Objekt geben?
	 * @param hasMultiClientData	Soll es ein JS-"Clients"-Objekt geben?
	 */
	public JSRunSimulationData(final boolean hasOutput, final boolean hasMultiClientData) {
		simulatorCommand=new JSCommandSystem();
		this.hasOutput=hasOutput;
		this.hasMultiClientData=hasMultiClientData;
		lastSuccess=false;
	}

	/**
	 * L�dt das Skript in dieses Objekt
	 * @param script	Sp�ter auszuf�hrendes Skript
	 * @return	Gibt <code>true</code> zur�ck, wenn das Skript erfolgreich geladen werden konnte.
	 */
	public boolean compile(final String script) {
		final JSBuilder builder=new JSBuilder(2_000);
		builder.addBinding("Simulation",simulatorCommand);
		if (hasOutput) builder.addBinding("Output",outputCommand=new JSCommandOutput(builder.output,false));
		if (hasMultiClientData) builder.addBinding("Clients",clientsCommand=new JSCommandClients(builder.output));
		runner=builder.build();
		if (runner==null) return false;
		final boolean initOk=runner.initScript(script);
		if (!initOk) runner=null;
		return initOk;
	}

	/**
	 * F�hrt das zuvor geladene Skript aus.
	 * @return	R�ckgabe der Skriptausf�hrung
	 */
	public String runCompiled() {
		if (runner==null) return Language.tr("Statistics.Filter.EngineInitError.General");
		lastSuccess=runner.run();
		return runner.getResult();
	}

	/**
	 * Gibt an, ob die letzte Befehlsausf�hrung erfolgreich war
	 * @return	Gibt <code>true</code> zur�ck, wenn die Ausf�hrung erfolgreich war
	 */
	public final boolean getLastSuccess() {
		return lastSuccess;
	}

	/**
	 * Gibt an, ob es sich bei der Ausgabe in Summe einfach nur um eine Double-Zahl handelt.
	 * @return	Ist die Ausgabe nur eine Double-Zahl?
	 * @see #getOutputDouble()
	 */
	public boolean isOutputDouble() {
		if (outputCommand==null) return false;
		return outputCommand.isOutputDouble();
	}

	/**
	 * Wenn es sich bei der Ausgabe nur um eine Double-Zahl handelt, so liefert diese Methode die Zahl zur�ck.
	 * @return	Ausgabe als Double-Zahl
	 * @see #isOutputDouble()
	 */
	public double getOutputDouble() {
		if (outputCommand==null) return 0.0;
		return outputCommand.getOutputDouble();
	}

	/**
	 * Stellt eine Eingangsgr��e f�r die Abfrage durch das Javascript-Verkn�pfungs-Objekt ein
	 * @param value	Eingangsgr��e (z.B. aus einer Datei geladener Zahlenwert)
	 */
	public void setSimulationInputValue(final double value) {
		simulatorCommand.setSimulationInputValue(value);
	}

	/**
	 * Stellt die Simulationsdaten f�r die Abfrage durch das Javascript-Verkn�pfungs-Objekt ein
	 * @param simData	Simulationsdaten-Objekt (kann auch <code>null</code> sein)
	 */
	public void setSimulationDataNoClient(final SimulationData simData) {
		simulatorCommand.setSimulationData(simData,null);
	}

	/**
	 * Stellt die Simulationsdaten und den aktuellen Kunden f�r die Abfrage durch das Javascript-Verkn�pfungs-Objekt ein
	 * @param simData	Simulationsdaten-Objekt (kann auch <code>null</code> sein)
	 * @param client	Aktueller Kunde (kann auch <code>null</code> sein)
	 */
	public void setSimulationData(final SimulationData simData, final RunDataClient client) {
		simulatorCommand.setSimulationData(simData,client);
	}

	/**
	 * Stellt die Simulationsdaten und die Liste der wartenden Kunden f�r die Abfrage durch das Javascript-Verkn�pfungs-Objekt ein
	 * @param simData	Simulationsdaten-Objekt (kann auch <code>null</code> sein)
	 * @param clients	Liste der wartenden Kunden
	 */
	public void setSimulationData(final SimulationData simData, final List<RunDataClient> clients) {
		simulatorCommand.setSimulationData(simData,null);
		clientsCommand.setSimulationData(simData,clients);
	}

	/**
	 * Gibt an, welche der Kunden freigegeben werden sollen
	 * @return	Array mit Angaben dar�ber, welche Kunden freigegeben werden sollen
	 * @see JSRunSimulationData#setSimulationData(SimulationData, List)
	 */
	public boolean[] getSimulationDataClients() {
		return clientsCommand.getSimulationData();
	}
}
