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
package systemtools.commandline;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Abstrakte Basisklasse für die Anzeige von Menüsystemen auf der Kommandozeile.
 * @see BaseCommandLineSystem
 * @see AbstractCommand
 * @author Alexander Herzog
 */
public abstract class AbstractMenuCommand extends AbstractCommand {
	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public AbstractMenuCommand(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String s=parameterCountCheck(0,additionalArguments); if (s!=null) return s;
		return null;
	}

	@Override
	public boolean isGUIProcessable() {
		return false;
	}

	/**
	 * Reagiert auf Nutzereingaben.
	 * @param input	Letzte Eingabe
	 * @return	Wird <code>true</code> zurückgeliefert, so wird das Menüsystem beendet
	 */
	protected abstract boolean processInput(final String input);

	/**
	 * Zeigt das Menü an.
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 * @return	Anzahl an ausgegebenen Zeilen
	 */
	protected abstract int showMenu(final PrintStream out);

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		style.setColor(null);
		try (var scanner=new Scanner(in)) {
			while (true) {
				final int outputLines=showMenu(out);
				if (outputLines<=0) break;
				if (processInput(scanner.nextLine())) return;
				style.moveCursorUp(outputLines+1);
				style.eraseAfterCursor();
			}
		}
	}
}
