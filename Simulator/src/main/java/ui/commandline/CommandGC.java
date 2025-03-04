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
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import language.Language;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.modeleditor.ModelElementCatalogDescriptionBuilder;

/**
 * Gibt aus, welcher Garbage Collector verwendet wird.
 * @author Alexander Herzog
 * @see ModelElementCatalogDescriptionBuilder
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandGC extends AbstractCommand {
	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandGC(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		return Language.trAll("CommandLine.GC.Name");
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.GC.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.GC.Description.Long").split("\n");
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		try {
			final List<GarbageCollectorMXBean> gcMxBeans=ManagementFactory.getGarbageCollectorMXBeans();

			for (GarbageCollectorMXBean gcMxBean: gcMxBeans) {
				out.println(gcMxBean.getName());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean isHidden() {
		return true;
	}
}