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
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import systemtools.commandline.AbstractCommand;
import tools.SetupData;

/**
 * Stellt ein, ob die dynamische Thread-Lastverteilung aktiviert oder deaktiviert werden soll.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandSetDynamicThreadBalance extends AbstractCommand {
	private boolean useDynamicThreadBalance=false;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.addAll(Arrays.asList(Language.trAll("CommandLine.SetDynamicThreadBalance.Name")));
		for (String s: Language.trOther("CommandLine.SetDynamicThreadBalance.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.SetDynamicThreadBalance.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.SetDynamicThreadBalance.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String s=parameterCountCheck(1,additionalArguments); if (s!=null) return s;

		final Integer I=NumberTools.getInteger(additionalArguments[0]);
		if (I==null) return String.format(Language.tr("CommandLine.SetDynamicThreadBalance.Error.Parameter"),additionalArguments[0]);
		useDynamicThreadBalance=I.intValue()!=0;

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		final SetupData setup=SetupData.getSetup();
		final boolean oldUseDynamicThreadBalance=setup.useDynamicThreadBalance;
		setup.useDynamicThreadBalance=useDynamicThreadBalance;

		if (!setup.saveSetup()) {
			out.println(Language.tr("CommandLine.SetDynamicThreadBalance.Error.CannotSaveSetup"));
			return;
		}

		final String oldDynamicThreadBalance=oldUseDynamicThreadBalance?Language.tr("CommandLine.SetDynamicThreadBalance.Info.DynamicThreadBalance.On"):Language.tr("CommandLine.SetDynamicThreadBalance.Info.DynamicThreadBalance.Off");
		final String newDynamicThreadBalance=setup.useDynamicThreadBalance?Language.tr("CommandLine.SetDynamicThreadBalance.Info.DynamicThreadBalance.On"):Language.tr("CommandLine.SetDynamicThreadBalance.Info.DynamicThreadBalance.Off");
		out.println(String.format(Language.tr("CommandLine.SetDynamicThreadBalance.Info.DynamicThreadBalance"),newDynamicThreadBalance,oldDynamicThreadBalance));
	}

	@Override
	public boolean isHidden() {
		return true;
	}
}
