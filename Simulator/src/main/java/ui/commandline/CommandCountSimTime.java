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

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.statistics.Statistics;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.parameterseries.ParameterCompareSetup;
import xml.XMLTools;

/**
 * Summiert die benötigte Rechenzeit für alle Ergebnisse in einem Verzeichnis auf.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandCountSimTime extends AbstractCommand {
	/**
	 * Zu verarbeitendes Verzeichnis
	 */
	private File folder;

	/**
	 * Wird von {@link #setQuit()} auf <code>true</code> gesetzt, wenn die Simulation
	 * abgebrochen werden soll.
	 */
	private boolean isQuit;

	/**
	 * Benötigte Rechenzeit (in MS) im Single-Core-Modus
	 */
	private long singleCoreMS;

	/**
	 * Benötigte Rechenzeit (in MS) im Multi-Core-Modus
	 */
	private long multiCoreMS;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandCountSimTime(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.addAll(Arrays.asList(Language.trAll("CommandLine.CountSimTime.Name")));
		for (String s: Language.trOther("CommandLine.CountSimTime.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.CountSimTime.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.CountSimTime.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String s=parameterCountCheck(1,additionalArguments); if (s!=null) return s;

		folder=new File(additionalArguments[0]);
		if (!folder.isDirectory()) return String.format(Language.tr("CommandLine.FolderSimulation.ParameterIsNotFolder"),folder.toString());

		return null;
	}

	/**
	 * Verarbeitet ein Statistikobjekt.
	 * @param statistics	Statistikobjekt (kann <code>null</code> sein)
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 */
	private void processStatistics(final Statistics statistics, final PrintStream out) {
		if (statistics==null) return;

		final boolean isMultiCore=(statistics.simulationData.runThreads>1);
		final long simulationMS=statistics.simulationData.runTime;

		if (isMultiCore) multiCoreMS+=simulationMS; else singleCoreMS+=simulationMS;

		out.println(String.format(Language.tr("CommandLine.CountSimTime.PartialInfo"),NumberTools.formatNumber(simulationMS/1000.0),(isMultiCore?Language.tr("CommandLine.CountSimTime.PartialInfo.MultiCoreYes"):Language.tr("CommandLine.CountSimTime.PartialInfo.MultiCoreNo"))));
	}

	/**
	 * Verarbeitet eine Datei.
	 * @param file	Zu verarbeitende Datei
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 * @return	Liefert <code>true</code>, wenn die Datei erfolgreich verarbeitet werden konnte
	 */
	private boolean processFile(final File file, final PrintStream out) {
		if (file.isDirectory()) return false;

		/* Info ausgeben */
		style.setColor(Color.GREEN);
		out.println(file.getName()+":");
		style.setColor(null);

		try {
			/* Datei laden */
			final XMLTools xml=new XMLTools(file);
			final Element root=xml.load();
			if (root==null) {
				style.setErrorStyle();
				out.println(Language.tr("CommandLine.FolderSimulation.CannotProcessFile"));
				style.setNormalStyle();
				return false;
			}

			/* Statistik */
			final Statistics statistics=new Statistics();
			if (statistics.loadFromFile(file)==null) {
				processStatistics(statistics,out);
				return true;
			}

			/* Parameterreihe */
			final ParameterCompareSetup setup=new ParameterCompareSetup(null);
			if (setup.loadFromFile(file)==null) {
				for (var model: setup.getModels()) processStatistics(model.getStatistics(),out);
				return true;
			}

			style.setErrorStyle();
			out.println(Language.tr("CommandLine.FolderSimulation.CannotProcessFile"));
			style.setNormalStyle();
			return false;
		} finally {
			out.println("");
		}
	}


	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		final String[] files=folder.list();
		if (files==null) {
			style.setErrorStyle();
			out.println(String.format(Language.tr("CommandLine.FolderSimulation.NoFilesInFolder"),folder.toString()));
			style.setNormalStyle();
			return;
		}

		Arrays.sort(files);

		int count=0;
		for (String file: files) {
			if (processFile(new File(folder,file),out)) count++;
			if (isQuit) break;
		}

		style.setBold(true);
		out.println(String.format(Language.tr("CommandLine.CountSimTime.ResultInfo"),count,NumberTools.formatNumber(singleCoreMS/1000.0),NumberTools.formatNumber(multiCoreMS/1000.0)));
		style.setBold(false);
	}

	@Override
	public void setQuit() {
		isQuit=true;
	}

	@Override
	public boolean isHidden() {
		return true;
	}
}
