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
package systemtools.commandline;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Zeigt die Hilfeübersicht über alle gültigen Kommandozeilenbefehle an.
 * @author Alexander Herzog
 * @version 1.0
 * @see AbstractCommand
 * @see BaseCommandLineSystem
 */
public final class CommandHelp extends AbstractCommand {
	/** Befehl zu dem die Hilfeseite angezeigt werden soll (<code>null</code> für eine Übersicht über alle verfügbaren Befehle) */
	private String commandName=null;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandHelp(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		final List<String> list=new ArrayList<>();
		list.add(BaseCommandLineSystem.commandHelpName);
		if (BaseCommandLineSystem.commandHelpNamesOtherLanguages!=null) list.addAll(Arrays.asList(BaseCommandLineSystem.commandHelpNamesOtherLanguages));
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return BaseCommandLineSystem.commandHelpHelpShort;
	}

	@Override
	public String[] getLongDescription() {
		return BaseCommandLineSystem.commandHelpHelpLong.split("\\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(0,1,additionalArguments); if (s!=null) return s;
		if (additionalArguments.length>0) commandName=additionalArguments[0];
		return null;
	}

	/**
	 * Zeigt die Daten zu einem bestimmten Befehl an.<br>
	 * Diese Methode kann sowohl bei der Auflistung aller Befehle
	 * als auch bei der Anzeige der Daten zu einem konkreten
	 * Kommandozeilenbefehl verwendet werden.
	 * @param command	Befehl zu dem die Daten ausgegeben werden sollen
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 * @see #listAll(AbstractCommand[], PrintStream)
	 * @see #listCommand(AbstractCommand[], PrintStream)
	 */
	private void showCommandInfo(AbstractCommand command, PrintStream out) {
		out.println(command.getName().toUpperCase());
		out.println("  "+command.getShortDescription());
		for (String line : command.getLongDescription()) out.println("  "+line);
	}

	/**
	 * Listet die Daten zu allen Kommandozeilenbefehlen auf.
	 * @param allCommands	Liste alle registrierten Befehle
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 * @see #run(AbstractCommand[], InputStream, PrintStream)
	 */
	private void listAll(AbstractCommand[] allCommands, PrintStream out) {
		for (String line: BaseCommandLineSystem.commandHelpInfo1.split("\\n")) out.println(line);

		String[] names=new String[allCommands.length];
		int maxLength=0;
		for (int i=0;i<allCommands.length;i++) {
			String s=allCommands[i].getName();
			maxLength=Math.max(maxLength,s.length());
			names[i]=s.toUpperCase();
		}
		for (int i=0;i<names.length;i++) while (names[i].length()<maxLength) names[i]+=" ";
		for (int i=0;i<names.length;i++) if (!allCommands[i].isHidden()) out.println(names[i]+" - "+allCommands[i].getShortDescription());

		for (String line: BaseCommandLineSystem.commandHelpInfo2.split("\\n")) out.println(line);

		for (AbstractCommand command : allCommands) if (!command.isHidden()) showCommandInfo(command,out);
	}

	/**
	 * Listet die Daten zu einem konkreten Kommandozeilenbefehlen auf.
	 * @param allCommands	Liste alle registrierten Befehle
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 * @see #commandName
	 * @see #run(AbstractCommand[], InputStream, PrintStream)
	 */
	private void listCommand(AbstractCommand[] allCommands, PrintStream out) {
		for (AbstractCommand command: allCommands) for (String name: command.getKeys()) if (name.equalsIgnoreCase(commandName)) {
			showCommandInfo(command,out);
			return;
		}
		out.println(String.format(BaseCommandLineSystem.commandHelpError,commandName));
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		if (commandName==null) listAll(allCommands,out); else listCommand(allCommands,out);
	}
}
