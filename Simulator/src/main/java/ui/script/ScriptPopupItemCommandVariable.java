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

import simulator.editmodel.EditModel;
import systemtools.BaseDialog;

/**
 * Popupmen�-Eintrag zur Ausf�hrung eines Befehls, der einen Variablennamen enth�lt
 * @author Alexander Herzog
 * @see ScriptPopupItemCommandModel
 */
public class ScriptPopupItemCommandVariable extends ScriptPopupItemCommandModel {
	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon f�r den Eintrag (kann <code>null</code> sein)
	 * @param command	Befehl, der in das Textfeld eingef�gt werden soll (muss ein "%s" enthalten).
	 * @param owner	�bergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param model	Editor-Modell dem die Daten entnommen werden sollen
	 * @param help	Hilfe-Runnable (f�r Dialog)
	 */
	public ScriptPopupItemCommandVariable(final String name, final String hint, final Icon icon, final String command, final Component owner, final EditModel model, final Runnable help) {
		super(name,hint,icon,command,owner,model,help);
	}

	@Override
	protected String getParameter(final Component owner, final EditModel model, final Runnable help) {
		final SelectVariableDialog dialog=new SelectVariableDialog(owner,model,help);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
		return dialog.getVariableName();
	}
}
