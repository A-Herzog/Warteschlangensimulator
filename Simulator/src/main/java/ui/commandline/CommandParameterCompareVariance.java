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
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.parameterseries.ParameterCompareSetup;

/**
 * Erstellt eine Parameterreihen-Konfigurationsdatei zur Varianzanalyse.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandParameterCompareVariance extends AbstractCommand {
	/** Eingabedatei für das Parameterreihen-Setup */
	private File inFile;
	/** Ausgabedatei für das Parameterreihen-Setup (inkl. Ergebnissen) */
	private File outFile;
	/** Anzahl der Wiederholungen */
	private int repeatCount;
	/** Ankünfte pro Wiederholung */
	private long arrivals;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandParameterCompareVariance(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		final List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.ParameterCompareVariance.Name"));
		for (String s: Language.trOther("CommandLine.ParameterCompareVariance.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ParameterCompareVariance.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.ParameterCompareVariance.Description.Long").split("\n");
	}

	@Override
	public String prepare(final String[] additionalArguments, final InputStream in, final PrintStream out) {
		final String s=parameterCountCheck(3,4,additionalArguments); if (s!=null) return s;

		/* Eingabe/Ausgabe */
		inFile=new File(additionalArguments[0]);
		outFile=new File(additionalArguments[1]);
		if (!inFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),inFile.toString());
		if (outFile.exists()) return String.format(Language.tr("CommandLine.Error.File.OutputAlreadyExist"),outFile.toString());

		/* Anzahl an Modellen */
		Long L=NumberTools.getPositiveLong(additionalArguments[2]);
		if (L==0) return String.format(Language.tr("CommandLine.ParameterCompareVariance.NumberOfModelsError"),additionalArguments[2]);
		repeatCount=L.intValue();

		/* Optional: Änderung Anzahl Ankünfte */
		if (additionalArguments.length==4) {
			L=NumberTools.getPositiveLong(additionalArguments[3]);
			if (L==0) return String.format(Language.tr("CommandLine.ParameterCompareVariance.ArrivalsError"),additionalArguments[3]);
			arrivals=L;
		} else {
			arrivals=-1;
		}

		return null;
	}


	@Override
	public void run(final AbstractCommand[] allCommands, final InputStream in, final PrintStream out) {
		/* Modell laden */
		final EditModel editModel=new EditModel();
		final String loadError=editModel.loadFromFile(inFile);
		if (loadError!=null) {
			style.setErrorStyle();
			out.println(BaseCommandLineSystem.errorBig+": "+loadError);
			style.setNormalStyle();
			return;
		}
		if (arrivals>0) {
			editModel.useClientCount=true;
			editModel.clientCount=arrivals;
		}

		/* Parameterreihe erstellen */
		final ParameterCompareSetup setup=new ParameterCompareSetup(null);
		setup.setEditModel(editModel);
		setup.setupVarianceAnalysis(repeatCount);

		/* Speichern */
		if (setup.saveToFile(outFile)) {
			out.println(String.format(Language.tr("CommandLine.ParameterCompareVariance.SaveOk"),outFile.toString()));
		} else {
			style.setErrorStyle();
			out.println(BaseCommandLineSystem.errorBig+": "+String.format(Language.tr("CommandLine.ParameterCompareVariance.SaveErrorInfo"),outFile.toString()));
			style.setNormalStyle();
		}
	}
}
