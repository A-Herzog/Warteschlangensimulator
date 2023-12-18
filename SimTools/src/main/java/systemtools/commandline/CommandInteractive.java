package systemtools.commandline;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Interaktiver Modus
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see BaseCommandLineSystem
 */
public class CommandInteractive extends AbstractCommand {

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandInteractive(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>(Arrays.asList(BaseCommandLineSystem.commandInteractiveName));
		for (String s: BaseCommandLineSystem.commandInteractiveNamesOtherLanguages) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return BaseCommandLineSystem.commandHelpInteractiveShort;
	}

	@Override
	public String[] getLongDescription() {
		return BaseCommandLineSystem.commandHelpInteractiveLong.split("\n");
	}

	@Override
	public void run(final AbstractCommand[] allCommands, final InputStream in, final PrintStream out) {
		if (system==null) return;

		out.println(BaseCommandLineSystem.commandHelpInteractiveStart);
		system.runInteractive(BaseCommandLineSystem.commandHelpInteractiveReady);
		out.println(BaseCommandLineSystem.commandHelpInteractiveStop);
	}

	@Override
	public boolean isGUIProcessable() {
		return false;
	}
}
