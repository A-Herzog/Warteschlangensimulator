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
import java.util.ArrayList;
import java.util.List;

import language.Language;
import simulator.editmodel.EditModelCertificateStore;
import simulator.editmodel.EditModelProcessor;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import tools.SetupData;
import ui.UpdateSystem;

/**
 * Setzt die Konfiguration auf die Standardwerte zur�ck.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandReset extends AbstractCommand {
	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandReset(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.ResetConfig.Name"));
		for (String s: Language.trOther("CommandLine.ResetConfig.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ResetConfig.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.ResetConfig.Description.Long").split("\n");
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		SetupData.resetSetup();
		UpdateSystem.reset();

		final EditModelCertificateStore certStore=new EditModelCertificateStore();
		certStore.clearTrustedPublicKeys();

		EditModelProcessor.getInstance().reset();
	}

	@Override
	public boolean isHidden() {
		return true;
	}
}
