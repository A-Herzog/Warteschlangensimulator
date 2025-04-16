/**
 * Copyright 2025 Alexander Herzog
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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import systemtools.commandline.AbstractMenuCommand;
import systemtools.commandline.BaseCommandLineSystem;
import tools.SetupData;

/**
 * Zeigt auf der Kommandozeile ein Menü zur Konfiguration des Simulators an.
 */
public class CommandSetup extends AbstractMenuCommand {
	/**
	 * Konfigurationsdaten-Singleton
	 */
	private final SetupData setup;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandSetup(final BaseCommandLineSystem system) {
		super(system);
		setup=SetupData.getSetup();
	}

	@Override
	public String[] getKeys() {
		final List<String> list=new ArrayList<>();
		list.addAll(Arrays.asList(Language.trAll("CommandLine.Setup.Name")));
		for (String s: Language.trOther("CommandLine.Setup.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Setup.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Setup.Description.Long").split("\n");
	}

	@Override
	protected boolean processInput(final String input) {
		if (input==null) return true;
		if (input.equalsIgnoreCase("q")) return true;

		if (input.equalsIgnoreCase("l")) {
			final var languages=Language.getLanguages();
			int index=-1;
			for (int i=0;i<languages.length;i++) if (setup.language.equalsIgnoreCase(languages[i])) {index=i; break;}
			index++;
			if (index>=languages.length) index=0;
			setup.language=languages[index];
			Language.init(setup.language);
			setup.saveSetup();
			return false;
		}

		if (input.equalsIgnoreCase("n")) {
			final var formats=SetupData.NumberFormat.values();

			int index=-1;
			for (int i=0;i<formats.length;i++) if (setup.numberFormat==formats[i]) {index=i; break;}
			index++;
			if (index>=formats.length) index=0;
			setup.numberFormat=formats[index];
			setup.saveSetup();
			return false;
		}

		final Long L=NumberTools.getPositiveLong(input);
		if (L!=null) {
			setup.useMultiCoreSimulation=true;
			setup.useMultiCoreSimulationMaxCount=Math.min(L.intValue(),1024);
			setup.saveSetup();
			return false;
		}

		if (input.equalsIgnoreCase("u")) {
			setup.useMultiCoreSimulation=true;
			setup.useMultiCoreSimulationMaxCount=1024;
			setup.saveSetup();
			return false;
		}

		if (input.equalsIgnoreCase("a")) {
			setup.useNUMAMode=!setup.useNUMAMode;
			setup.saveSetup();
			return false;
		}


		if (input.equalsIgnoreCase("b")) {
			setup.useDynamicThreadBalance=!setup.useDynamicThreadBalance;
			setup.saveSetup();
			return false;
		}

		return false;
	}

	/**
	 * Generiert die im Menü auszugebenden Zeilen
	 * @param lines	Liste die die auszugebenden Zeilen aufnimmt
	 */
	private void addMenuLines(final List<String> lines) {
		lines.add(Language.tr("CommandLine.Setup.Language")+": "+setup.language+" ("+Language.tr("CommandLine.Setup.Change")+": L+"+Language.tr("CommandLine.Setup.Return")+")");

		lines.add(Language.tr("CommandLine.Setup.NumberFormat")+": "+setup.numberFormat.name+" ("+Language.tr("CommandLine.Setup.Change")+": N+"+Language.tr("CommandLine.Setup.Return")+")");

		if (!setup.useMultiCoreSimulation) {
			lines.add(Language.tr("CommandLine.Setup.MultiCoreSimulation")+": "+Language.tr("CommandLine.Setup.off")+" ("+Language.tr("CommandLine.Setup.Change")+": "+Language.tr("CommandLine.Setup.MaxThreads.Info"));
		} else {
			lines.add(Language.tr("CommandLine.Setup.MaxThreads")+": "+((setup.useMultiCoreSimulationMaxCount<0 || setup.useMultiCoreSimulationMaxCount>=1024)?Language.tr("CommandLine.Setup.MaxThreads.unlimited"):setup.useMultiCoreSimulationMaxCount)+" "+Language.tr("CommandLine.Setup.MaxThreads.Info"));
		}

		lines.add(Language.tr("CommandLine.Setup.NUMA")+": "+(setup.useNUMAMode?Language.tr("CommandLine.Setup.on"):Language.tr("CommandLine.Setup.off"))+" ("+Language.tr("CommandLine.Setup.Change")+": A+"+Language.tr("CommandLine.Setup.Return")+")");

		lines.add(Language.tr("CommandLine.Setup.LoadBalancer")+": "+(setup.useDynamicThreadBalance?Language.tr("CommandLine.Setup.on"):Language.tr("CommandLine.Setup.off"))+" ("+Language.tr("CommandLine.Setup.Change")+": B+"+Language.tr("CommandLine.Setup.Return")+")");

		lines.add(Language.tr("CommandLine.Setup.Quit")+": Q+"+Language.tr("CommandLine.Setup.Return"));
	}

	@Override
	protected int showMenu(final PrintStream out) {
		final List<String> lines=new ArrayList<>();
		addMenuLines(lines);
		lines.forEach(out::println);
		return lines.size();
	}
}
