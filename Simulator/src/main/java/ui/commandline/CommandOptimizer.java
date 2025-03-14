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

import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import ui.optimizer.OptimizerBase;
import ui.optimizer.OptimizerCatalog;
import ui.optimizer.OptimizerSetup;
import xml.XMLTools;

/**
 * F�hrt eine Optimierung aus.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandOptimizer extends AbstractCommand {
	/** Modelldatei (Ausgangsmodell f�r die Optimierung) */
	private File modelFile;
	/** Optimierer-Konfigurationsdatei */
	private File setupFile;
	/** Ist die Optimierung abgeschlossen? */
	private boolean optimizationDone=false;
	/** Tats�chlier interner Optimierer */
	private OptimizerBase optimizer;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandOptimizer(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.Optimizer.Name"));
		for (String s: Language.trOther("CommandLine.Optimizer.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Optimizer.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Optimizer.Description.Long").split("\n");
	}

	/**
	 * Pr�ft, ob die �bergebene Datei eine Modelldatei ist
	 * @param file	Zu pr�fende Datei
	 * @return	Gibt <code>true</code> zur�ck, wenn es sich um eine Modelldatei handelt
	 */
	private final boolean isModelFile(final File file) {
		Element root=new XMLTools(file).load();
		if (root==null) return false;

		for (String test: new EditModel().getRootNodeNames()) if (root.getNodeName().equalsIgnoreCase(test)) return true;

		return false;
	}

	/**
	 * Pr�ft, ob die �bergebene Datei eine Optimierer-Konfigurationsdatei ist
	 * @param file	Zu pr�fende Datei
	 * @return	Gibt <code>true</code> zur�ck, wenn es sich um eine Optimierer-Konfigurationsdatei handelt
	 */
	private final boolean isSetupFile(final File file) {
		Element root=new XMLTools(file).load();
		if (root==null) return false;

		for (String test: new OptimizerSetup().getRootNodeNames())  if (root.getNodeName().equalsIgnoreCase(test)) return true;

		return false;
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(2,additionalArguments); if (s!=null) return s;

		modelFile=new File(additionalArguments[0]);
		setupFile=new File(additionalArguments[1]);

		if (!modelFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),modelFile);
		if (!setupFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.OptimizerInputDoesNotExist"),setupFile);
		if (!isModelFile(modelFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidModelFile"),modelFile);
		if (!isSetupFile(setupFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidOptimizerInput"),setupFile);

		return null;
	}

	/**
	 * Setzt {@link #optimizationDone} auf <code>true</code>, d.h.
	 * gibt an, dass die Optimierung abgeschlossen wurde.
	 * @see #run(AbstractCommand[], InputStream, PrintStream)
	 */
	private void setOptimizationDone() {
		optimizationDone=true;
	}

	@Override
	public void run(final AbstractCommand[] allCommands, InputStream in, final PrintStream out) {
		String error;

		final EditModel model=new EditModel();
		error=model.loadFromFile(modelFile);
		if (error!=null) {
			style.setErrorStyle();
			out.println(Language.tr("Optimizer.Error.CouldNotStart")+":\n"+error);
			style.setNormalStyle();
			return;
		}

		if (model.modelLoadData.willChangeModel()) out.println(Language.tr("ModelLoadData.IncompatibleWarning.Optimization"));

		final OptimizerSetup setup=new OptimizerSetup();
		error=setup.loadFromFile(setupFile);
		if (error!=null) {
			style.setErrorStyle();
			out.println(Language.tr("Optimizer.Error.CouldNotStart")+":\n"+error);
			style.setNormalStyle();
			return;
		}

		optimizer=new OptimizerCatalog(null).getOptimizer(setup.optimizerName);
		error=optimizer.check(model,modelFile.getParent(),setup,text->out.println(text),b->setOptimizationDone(),null);
		if (error!=null) {
			style.setErrorStyle();
			out.println(Language.tr("Optimizer.Error.CouldNotStart")+":\n"+error);
			style.setNormalStyle();
			return;
		}
		optimizer.start();

		CloseRequestSignal signal=new CloseRequestSignal(true,in);

		while (!optimizationDone) {
			try {Thread.sleep(25);} catch (InterruptedException e) {}
			if (signal.isQuit()) {optimizer.cancel(); break;}
		}
	}

	@Override
	public void setQuit() {
		optimizer.cancel();
	}
}
