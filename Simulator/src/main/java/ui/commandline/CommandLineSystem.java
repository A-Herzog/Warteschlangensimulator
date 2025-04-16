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
package ui.commandline;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import simulator.editmodel.EditModel;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.MainFrame;
import ui.MainPanel;

/**
 * System zur Ausführung von Kommandozeilen-Befehlen
 * @author Alexander Herzog
 */
public class CommandLineSystem extends BaseCommandLineSystem {

	/**
	 * Konstruktor der Klasse <code>CommandLineSystem</code>
	 * @param in	Ein {@link InputStream}-Objekt oder <code>null</code>, über das Zeichen von der Konsole gelesen werden können (<code>null</code>, wenn keine Konsole verfügbar ist)
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 */
	public CommandLineSystem(InputStream in, PrintStream out) {
		super(MainFrame.PROGRAM_NAME,EditModel.systemVersion,MainPanel.AUTHOR,in,out);
	}

	/**
	 * Konstruktor der Klasse <code>CommandLineSystem</code><br>
	 * Die Ausgabe der Befehle erfolgt auf <code>System.out</code>
	 */
	public CommandLineSystem() {
		this(System.in,System.out);
	}

	@Override
	protected List<AbstractCommand> getCommands() {
		List<AbstractCommand> list=super.getCommands();

		list.add(new CommandSimulation(this));
		list.add(new CommandSimulationTimeout(this));
		list.add(new CommandBenchmark(this));
		list.add(new CommandReport(this));
		list.add(new CommandReset(this));
		list.add(new CommandFilter(this));
		list.add(new CommandExport(this));
		list.add(new CommandProcessOutput(this));
		list.add(new CommandOptimizer(this));
		list.add(new CommandServer(this));
		list.add(new CommandServerLimited(this));
		list.add(new CommandServerWeb(this));
		list.add(new CommandServerWebFixed(this));
		list.add(new CommandServerMQTT(this));
		list.add(new CommandServerMQTTTest(this));
		list.add(new CommandServerMQTTFixed(this));
		list.add(new CommandServerSocket(this));
		list.add(new CommandVersion(this));
		list.add(new CommandSetMaxThreads(this));
		list.add(new CommandSetNUMA(this));
		list.add(new CommandSetDynamicThreadBalance(this));
		list.add(new CommandParameterCompare(this));
		list.add(new CommandParameterCompareTable(this));
		list.add(new CommandParameterCompareVariance(this));
		list.add(new CommandFolderSimulation(this));
		list.add(new CommandFolderFilter(this));
		list.add(new CommandBuildCatalogDescriptions(this));
		list.add(new CommandBuildCatalogImages(this));
		list.add(new CommandBuildCommandlineDescriptions(this));
		list.add(new CommandBuildExampleModelImages(this));
		list.add(new CommandBuildGlossaryText(this));
		list.add(new CommandGC(this));
		list.add(new CommandJavaProperties(this));
		list.add(new CommandCountSimTime(this));
		list.add(new CommandSetLanguage(this));
		list.add(new CommandSetNumberFormat(this));
		list.add(new CommandSetup(this));

		return list;
	}
}