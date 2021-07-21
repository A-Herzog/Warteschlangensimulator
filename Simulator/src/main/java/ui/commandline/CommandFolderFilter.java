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

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Element;

import language.Language;
import mathtools.Table;
import simulator.statistics.Statistics;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import xml.XMLTools;

/**
 * Wendet einen Filter auf alle Statistikdaten in einem Verzeichnis an.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandFolderFilter extends AbstractCommand {
	/**
	 * Zu verarbeitendes Verzeichnis
	 */
	private File folder;

	/**
	 * Filterskript
	 */
	private File filterFile;

	/**
	 * Ausgabedatei
	 */
	private File filterResultFile;

	/**
	 * Konstruktor der Klasse
	 */
	public CommandFolderFilter() {
		super();
	}

	/**
	 * Konstruktor der Klasse
	 * @param folder	Zu verarbeitendes Verzeichnis
	 * @param filterFile	Filterskript
	 * @param filterResultFile	Ausgabedatei
	 */
	public CommandFolderFilter(final File folder, final File filterFile, final File filterResultFile) {
		this.folder=folder;
		this.filterFile=filterFile;
		this.filterResultFile=filterResultFile;
	}

	@Override
	public String[] getKeys() {
		final List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.FolderFilter.Name"));
		for (String s: Language.trOther("CommandLine.FolderFilter.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.FolderFilter.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.FolderFilter.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String s=parameterCountCheck(3,additionalArguments); if (s!=null) return s;

		folder=new File(additionalArguments[0]);
		if (!folder.isDirectory()) return String.format(Language.tr("CommandLine.FolderFilter.ParameterIsNotFolder"),folder.toString());

		filterFile=new File(additionalArguments[1]);
		if (!filterFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.ConfigDoesNotExist"),filterFile);

		filterResultFile=new File(additionalArguments[2]);
		if (filterResultFile.isDirectory()) return String.format(Language.tr("CommandLine.Error.File.OutputFileIsFolder"),filterResultFile);

		return null;
	}

	/**
	 * Prüft, ob eine Datei geladen/verarbeitet werden kann
	 * und führt ggf. die Simulation durch.
	 * @param file	Zu prüfende Datei
	 * @param commands	Auszuführender Filter
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 */
	private void processFile(final File file, final String commands, final PrintStream out) {
		if (file.isDirectory()) return;
		out.println(file.getName()+":");

		try {
			/* Datei laden */
			final XMLTools xml=new XMLTools(file);
			final Element root=xml.load();
			if (root==null) {
				out.println(Language.tr("CommandLine.FolderFilter.CannotProcessFile"));
				return;
			}

			/* Als Statistikdaten verarbeiten */
			final Statistics statistics=new Statistics();
			if (statistics.loadFromXML(root)!=null) {
				out.println(Language.tr("CommandLine.FolderFilter.NoStatisticFile"));
				return;
			}
			statistics.loadedStatistics=file;

			CommandFilter.runFilter(statistics,commands,filterResultFile,out);

		} finally {
			out.println("");
		}
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		final String[] files=folder.list();
		if (files==null) {
			out.println(String.format(Language.tr("CommandLine.FolderFilter.NoFilesInFolder"),folder.toString()));
			return;
		}

		Arrays.sort(files);

		final String commands=Table.loadTextFromFile(filterFile);
		if (commands==null) {
			out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.FolderFilter.ErrorLoadingFilterConfiguration"));
			return;
		}

		for (String file: files) {
			processFile(new File(folder,file),commands, out);
		}
	}
}