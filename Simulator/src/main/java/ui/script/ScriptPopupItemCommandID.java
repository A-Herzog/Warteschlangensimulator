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
 * Popupmenü-Eintrag zur Ausführung eines Befehls, der eine Stations-ID enthält
 * @author Alexander Herzog
 * @see ScriptPopupItemCommandModel
 */
public class ScriptPopupItemCommandID extends ScriptPopupItemCommandModel {
	/** Soll wenn möglich in der Liste eine Bedienstation oder Verzögerungsstation initial ausgewählt werden? */
	private final boolean preferProcessStations;
	/** Soll auch ein leerer Parameter zulässig sein? */
	private final boolean allowEmpty;
	/** Liste der Klassen der Elementtypen auf die die Auswahl eingeschränkt werden soll (wird hier <code>null</code> oder eine leere Liste übergeben, so erfolgt keine Einschränkung) */
	private final Class<?>[] stationTypes;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon für den Eintrag (kann <code>null</code> sein)
	 * @param command	Befehl, der in das Textfeld eingefügt werden soll (muss ein "%s" enthalten).
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param model	Editor-Modell dem die Daten entnommen werden sollen
	 * @param help	Hilfe-Runnable (für Dialog)
	 */
	public ScriptPopupItemCommandID(final String name, final String hint, final Icon icon, final String command, final Component owner, final EditModel model, final Runnable help) {
		super(name,hint,icon,command,owner,model,help);
		stationTypes=null;
		preferProcessStations=false;
		allowEmpty=false;
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon für den Eintrag (kann <code>null</code> sein)
	 * @param command	Befehl, der in das Textfeld eingefügt werden soll (muss ein "%s" enthalten).
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param model	Editor-Modell dem die Daten entnommen werden sollen
	 * @param help	Hilfe-Runnable (für Dialog)
	 * @param preferProcessStations	Soll wenn möglich in der Liste eine Bedienstation oder Verzögerungsstation initial ausgewählt werden?
	 */
	public ScriptPopupItemCommandID(final String name, final String hint, final Icon icon, final String command, final Component owner, final EditModel model, final Runnable help, final boolean preferProcessStations) {
		super(name,hint,icon,command,owner,model,help);
		stationTypes=null;
		this.preferProcessStations=preferProcessStations;
		allowEmpty=false;
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon für den Eintrag (kann <code>null</code> sein)
	 * @param command	Befehl, der in das Textfeld eingefügt werden soll (muss ein "%s" enthalten).
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param model	Editor-Modell dem die Daten entnommen werden sollen
	 * @param help	Hilfe-Runnable (für Dialog)
	 * @param preferProcessStations	Soll wenn möglich in der Liste eine Bedienstation oder Verzögerungsstation initial ausgewählt werden?
	 * @param allowEmpty	Soll auch ein leerer Parameter zulässig sein?
	 */
	public ScriptPopupItemCommandID(final String name, final String hint, final Icon icon, final String command, final Component owner, final EditModel model, final Runnable help, final boolean preferProcessStations, final boolean allowEmpty) {
		super(name,hint,icon,command,owner,model,help);
		stationTypes=null;
		this.preferProcessStations=preferProcessStations;
		this.allowEmpty=allowEmpty;
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon für den Eintrag (kann <code>null</code> sein)
	 * @param command	Befehl, der in das Textfeld eingefügt werden soll (muss ein "%s" enthalten).
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param model	Editor-Modell dem die Daten entnommen werden sollen
	 * @param help	Hilfe-Runnable (für Dialog)
	 * @param stationTypes	Liste der Klassen der Elementtypen auf die die Auswahl eingeschränkt werden soll (wird hier <code>null</code> oder eine leere Liste übergeben, so erfolgt keine Einschränkung)
	 */
	public ScriptPopupItemCommandID(final String name, final String hint, final Icon icon, final String command, final Component owner, final EditModel model, final Runnable help, final Class<?>[] stationTypes) {
		super(name,hint,icon,command,owner,model,help);
		this.stationTypes=stationTypes;
		preferProcessStations=false;
		allowEmpty=false;
	}

	@Override
	protected String getParameter(final Component owner, final EditModel model, final Runnable help) {
		final SelectIDDialog dialog=new SelectIDDialog(owner,model,help,stationTypes,preferProcessStations,allowEmpty);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
		final int id=dialog.getSelectedID();
		if (allowEmpty && id==-2) return "";
		if (id<0) return null;
		return ""+id;
	}
}
