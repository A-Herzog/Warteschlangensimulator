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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;

/**
 * Eintrag in das {@link ScriptPopup}-Menü
 * @author Alexander Herzog
 * @see ScriptPopup
 */
public class ScriptPopupItem {
	/** Name des Eintrags (kann <code>null</code> sein) */
	private final String name;
	/** Tooltip des Eintrags (kann <code>null</code> sein) */
	private final String hint;
	/** Icon für den Eintrags (kann <code>null</code> sein) */
	private final Icon icon;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon für den Eintrags (kann <code>null</code> sein)
	 */
	public ScriptPopupItem(final String name, final String hint, final Icon icon) {
		this.name=name;
		this.hint=hint;
		this.icon=icon;
	}

	/**
	 * Erstellt aus den im Konstruktur übergebenen Daten ein {@link JMenuItem}
	 * @return	Popupmenü-Eintrag basierend auf den Daten, die dem Konstruktur übergeben wurden
	 */
	protected JMenuItem buildMenuItem() {
		final JMenuItem item=(name==null || name.trim().isEmpty())?new JMenuItem():new JMenuItem(name.trim());
		if (hint!=null && !hint.trim().isEmpty()) item.setToolTipText(hint.trim());
		if (icon!=null) item.setIcon(icon);
		return item;
	}

	/**
	 * Erstellt aus den im Konstruktur übergebenen Daten ein {@link JMenu} (ein Untermenü)
	 * @return	Popupmenü-Untermenü-Eintrag basierend auf den Daten, die dem Konstruktur übergeben wurden
	 */
	protected JMenu buildMenu() {
		final JMenu item=(name==null || name.trim().isEmpty())?new JMenu():new JMenu(name.trim());
		if (hint!=null && !hint.trim().isEmpty()) item.setToolTipText(hint.trim());
		if (icon!=null) item.setIcon(icon);
		return item;
	}

	/**
	 * Wird aufgerufen, wenn die Daten des Befehl in ein Textfeld eingefügt werden sollen
	 * @param textArea	Textfeld, in das der neue Befehl eingetragen werden soll
	 * @param update	Muss (wenn nicht <code>null</code>) aufgerufen werden, wenn es Änderungen an dem Textfeld gab
	 */
	public void insertIntoTextArea(final JTextArea textArea, final Runnable update) {
	}

	/**
	 * Fügt einen Text an der aktuellen Cursorposition in ein Textfeld ein und setzt den Cursor daran auf die Position nach dem neuen Text
	 * @param textArea	Textfeld, in das der neue Text eingetragen werden soll
	 * @param textToInsert	Einzufügender Text (wird automatisch um einen Zeilenumbruch am Ende ergänzt)
	 */
	protected static final void insertTextIntoTextArea(final JTextArea textArea, final String textToInsert) {
		final String oldText=textArea.getText();
		final int caretPosition=textArea.getCaretPosition();
		textArea.setText(oldText.substring(0,caretPosition)+textToInsert+"\n"+oldText.substring(caretPosition));
		textArea.setCaretPosition(caretPosition+textToInsert.length()+1);
		textArea.requestFocus();
	}
}
