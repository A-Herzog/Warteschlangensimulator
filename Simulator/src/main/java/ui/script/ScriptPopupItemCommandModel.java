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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTextArea;

import simulator.editmodel.EditModel;

/**
 * Popupmen�-Eintrag zur Ausf�hrung eines Befehls, der auf Modelldaten zur�ckgreift
 * @author Alexander Herzog
 * @see ScriptPopup
 * @see ScriptPopupItem
 */
public abstract class ScriptPopupItemCommandModel extends ScriptPopupItem {
	/** Befehl, der in das Textfeld eingef�gt werden soll (muss ein "%s" enthalten). */
	private final String command;
	/** �bergeordnetes Element (zur Ausrichtung von optionalen Dialogen) */
	private final Component owner;
	/** Editor-Modell dem die Daten entnommen werden sollen */
	private final EditModel model;
	/** Hilfe-Runnable (f�r Dialog) */
	private final Runnable help;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon f�r den Eintrag (kann <code>null</code> sein)
	 * @param command	Befehl, der in das Textfeld eingef�gt werden soll (muss ein "%s" enthalten).
	 * @param owner	�bergeordnetes Element (zur Ausrichtung von optionalen Dialogen)
	 * @param model	Editor-Modell dem die Daten entnommen werden sollen
	 * @param help	Hilfe-Runnable (f�r Dialog)
	 */
	public ScriptPopupItemCommandModel(final String name, final String hint, final Icon icon, final String command, final Component owner, final EditModel model, final Runnable help) {
		super(name,hint,icon);
		this.command=command;
		this.owner=owner;
		this.model=model;
		this.help=help;
	}

	/**
	 * Der Befehl, der im Konstruktor angegeben wurde, muss einen "%s" Platzhalter enthalten.
	 * Diese Methode liefert den Inhalt f�r den Platzhalter.
	 * @param owner	�bergeordnetes Element
	 * @param model	Editor-Modell dem die Daten entnommen werden sollen
	 * @param help	Hilfe-Runnable
	 * @return	Inhalt f�r den Platzhalter oder <code>null</code> oder ein leerer String, wenn keine Ausgabe erfolgen soll
	 */
	protected abstract String getParameter(final Component owner, final EditModel model, final Runnable help);

	@Override
	public void insertIntoTextArea(final JTextArea textArea, final Runnable update) {
		if (textArea==null) return;

		final String parameter=getParameter(owner,model,help);
		if (parameter==null) return;

		insertTextIntoTextArea(textArea,String.format(command,parameter));

		if (update!=null) update.run();
	}
}
