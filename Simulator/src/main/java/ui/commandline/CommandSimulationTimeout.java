/**
 * Copyright 2021 Alexander Herzog
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

import java.util.ArrayList;
import java.util.List;

import language.Language;
import systemtools.commandline.AbstractCommand;

/**
 * Führt eine einzelne Simulation aus und bricht diese ggf. ab,
 * wenn sie zu lange dauert.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see AbstractSimulationCommand
 * @see CommandLineSystem
 */
public class CommandSimulationTimeout extends CommandSimulation {
	/**
	 * Konstruktor der Klasse
	 */
	public CommandSimulationTimeout() {
		super(true);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.SimulationTimeout.Name"));
		for (String s: Language.trOther("CommandLine.SimulationTimeout.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.SimulationTimeout.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.SimulationTimeout.Description.Long").split("\n");
	}
}
