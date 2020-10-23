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
import mathtools.NumberTools;
import systemtools.commandline.AbstractCommand;
import tools.SetupData;

/**
 * Stellt die Maximalanzahl an für die Simulation zu verwendenden Threads ein.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandSetMaxThreads extends AbstractCommand {
	/** Maximalzahl an Threads (-1 für unbegrenzt) */
	private int maxThreads=-1;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.SetMaxThreads.Name"));
		for (String s: Language.trOther("CommandLine.SetMaxThreads.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.SetMaxThreads.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.SetMaxThreads.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String s=parameterCountCheck(1,additionalArguments); if (s!=null) return s;

		final Integer I=NumberTools.getInteger(additionalArguments[0]);
		if (I==null) return String.format(Language.tr("CommandLine.SetMaxThreads.Error.Parameter"),additionalArguments[0]);
		maxThreads=I.intValue();

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		final SetupData setup=SetupData.getSetup();
		final boolean oldMultiCore=setup.useMultiCoreSimulation;
		final int oldMultiCoreMax=setup.useMultiCoreSimulationMaxCount;
		setup.useMultiCoreSimulation=true;
		setup.useMultiCoreSimulationMaxCount=(maxThreads<=0)?1024:maxThreads;

		if (!setup.saveSetup()) {
			out.println(Language.tr("CommandLine.SetMaxThreads.Error.CannotSaveSetup"));
			return;
		}

		out.println(String.format(Language.tr("CommandLine.SetMaxThreads.Info.MultiCore"),oldMultiCore?Language.tr("CommandLine.SetMaxThreads.Info.MultiCore.OldState.Yes"):Language.tr("CommandLine.SetMaxThreads.Info.MultiCore.OldState.No")));
		final String oldMax=(oldMultiCoreMax<=0 || oldMultiCoreMax>=1024)?Language.tr("CommandLine.SetMaxThreads.Info.MaxThreads.Unlimited"):(""+oldMultiCoreMax);
		final String newMax=(maxThreads<=0 || maxThreads>=1024)?Language.tr("CommandLine.SetMaxThreads.Info.MaxThreads.Unlimited"):(""+maxThreads);
		out.println(String.format(Language.tr("CommandLine.SetMaxThreads.Info.MaxThreads"),newMax,oldMax));
		out.println(String.format(Language.tr("CommandLine.SetMaxThreads.Info.CoreCount"),Runtime.getRuntime().availableProcessors()));
	}

	@Override
	public boolean isHidden() {
		return true;
	}
}
