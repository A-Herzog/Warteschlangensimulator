/**
 * Copyright 2024 Alexander Herzog
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

import language.Language;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;

/**
 * Gibt die Java-Properties aus.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandJavaProperties extends AbstractCommand {
	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandJavaProperties(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		return Language.trAll("CommandLine.JavaProperties.Name");
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.JavaProperties.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.JavaProperties.Description.Long").split("\n");
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		System.getProperties().entrySet().forEach(property->out.println(property.getKey()+"="+property.getValue()));
	}
}