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
	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.addAll(Arrays.asList(BaseCommandLineSystem.commandInteractiveName));
		for (String s: BaseCommandLineSystem.commandInteractiveNamesOtherLanguages) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
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
