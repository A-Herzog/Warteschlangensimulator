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

		list.add(new CommandSimulation());
		list.add(new CommandBenchmark());
		list.add(new CommandReport());
		list.add(new CommandReset());
		list.add(new CommandFilter());
		list.add(new CommandOptimizer());
		list.add(new CommandServer());
		list.add(new CommandServerLimited());
		list.add(new CommandServerWeb());
		list.add(new CommandServerWebFixed());
		list.add(new CommandServerMQTT());
		list.add(new CommandServerMQTTTest());
		list.add(new CommandVersion());
		list.add(new CommandSetMaxThreads());
		list.add(new CommandSetNUMA());
		list.add(new CommandSetDynamicThreadBalance());
		list.add(new CommandParameterCompare());
		list.add(new CommandParameterCompareTable());
		list.add(new CommandParameterCompareVariance());
		list.add(new CommandFolderSimulation());
		list.add(new CommandFolderFilter());
		list.add(new CommandBuildCatalogDescriptions());
		list.add(new CommandBuildCatalogImages());
		list.add(new CommandBuildCommandlineDescriptions());
		list.add(new CommandBuildExampleModelImages());
		list.add(new CommandGC());

		return list;
	}
}