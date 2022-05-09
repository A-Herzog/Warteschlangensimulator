package ui.commandline;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import simulator.examples.EditModelExamples;
import systemtools.commandline.AbstractCommand;

/**
 * Erstellt Bilder die die Beispielmodelle darstellen.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandBuildExampleModelImages extends AbstractCommand {
	/** Sprache */
	private String language;

	/**
	 * Konstruktor der Klasse
	 */
	public CommandBuildExampleModelImages() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.addAll(Arrays.asList(Language.trAll("CommandLine.BuildExampleModelImages .Name")));
		for (String s: Language.trOther("CommandLine.BuildExampleModelImages .Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.BuildExampleModelImages .Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.BuildExampleModelImages .Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String error=parameterCountCheck(1,additionalArguments);
		if (error!=null) return error;

		language=additionalArguments[0];

		return null;
	}


	@Override
	public void run(final AbstractCommand[] allCommands, final InputStream in, final PrintStream out) {
		if (!Language.isSupportedLanguage(language)) {
			out.println(String.format(Language.tr("CommandLine.BuildExampleModelImages .Error.Language"),language));
			return;
		}

		EditModelExamples.saveImages(language.toLowerCase(),new File(System.getProperty("user.home")+"\\Desktop"),out);
	}

	@Override
	public boolean isHidden() {
		return true;
	}
}
