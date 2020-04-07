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
package ui.script;

import javax.swing.Icon;
import javax.swing.JTextArea;

/**
 * Popupmenü-Eintrag zur Ausführung eines einfachen Befehls
 * @author Alexander Herzog
 * @see ScriptPopup
 * @see ScriptPopupItem
 */
public class ScriptPopupItemCommand extends ScriptPopupItem {
	private final String command;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon für den Eintrag (kann <code>null</code> sein)
	 * @param command	Befehl, der in das Textfeld eingefügt werden soll.
	 */
	public ScriptPopupItemCommand(final String name, final String hint, final Icon icon, final String command) {
		super(name,hint,icon);
		this.command=command;
	}

	@Override
	public void insertIntoTextArea(final JTextArea textArea, final Runnable update) {
		if (textArea==null) return;

		insertTextIntoTextArea(textArea,command);

		if (update!=null) update.run();
	}
}
