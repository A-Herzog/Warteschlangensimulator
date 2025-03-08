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
 * Popupmen�-Eintrag zur Ausf�hrung eines Befehls, der eine Stations-ID enth�lt
 * @author Alexander Herzog
 * @see ScriptPopupItemCommandModel
 */
public class ScriptPopupItemCommandID extends ScriptPopupItemCommandModel {
	/** Soll wenn m�glich in der Liste eine Bedienstation oder Verz�gerungsstation initial ausgew�hlt werden? */
	private final boolean preferProcessStations;
	/** Soll auch ein leerer Parameter zul�ssig sein? */
	private final boolean allowEmpty;
	/** Soll es zul�ssig sein, Stationen �ber ihre Namen zu identifizieren? */
	private final boolean allowNames;
	/** Liste der Klassen der Elementtypen auf die die Auswahl eingeschr�nkt werden soll (wird hier <code>null</code> oder eine leere Liste �bergeben, so erfolgt keine Einschr�nkung) */
	private final Class<?>[] stationTypes;

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
	public ScriptPopupItemCommandID(final String name, final String hint, final Icon icon, final String command, final Component owner, final EditModel model, final Runnable help) {
		super(name,hint,icon,command,owner,model,help);
		stationTypes=null;
		preferProcessStations=false;
		allowEmpty=false;
		allowNames=false;
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon f�r den Eintrag (kann <code>null</code> sein)
	 * @param command	Befehl, der in das Textfeld eingef�gt werden soll (muss ein "%s" enthalten).
	 * @param owner	�bergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param model	Editor-Modell dem die Daten entnommen werden sollen
	 * @param help	Hilfe-Runnable (f�r Dialog)
	 * @param preferProcessStations	Soll wenn m�glich in der Liste eine Bedienstation oder Verz�gerungsstation initial ausgew�hlt werden?
	 */
	public ScriptPopupItemCommandID(final String name, final String hint, final Icon icon, final String command, final Component owner, final EditModel model, final Runnable help, final boolean preferProcessStations) {
		super(name,hint,icon,command,owner,model,help);
		stationTypes=null;
		this.preferProcessStations=preferProcessStations;
		allowEmpty=false;
		allowNames=false;
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon f�r den Eintrag (kann <code>null</code> sein)
	 * @param command	Befehl, der in das Textfeld eingef�gt werden soll (muss ein "%s" enthalten).
	 * @param owner	�bergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param model	Editor-Modell dem die Daten entnommen werden sollen
	 * @param help	Hilfe-Runnable (f�r Dialog)
	 * @param preferProcessStations	Soll wenn m�glich in der Liste eine Bedienstation oder Verz�gerungsstation initial ausgew�hlt werden?
	 * @param allowEmpty	Soll auch ein leerer Parameter zul�ssig sein?
	 */
	public ScriptPopupItemCommandID(final String name, final String hint, final Icon icon, final String command, final Component owner, final EditModel model, final Runnable help, final boolean preferProcessStations, final boolean allowEmpty) {
		super(name,hint,icon,command,owner,model,help);
		stationTypes=null;
		this.preferProcessStations=preferProcessStations;
		this.allowEmpty=allowEmpty;
		allowNames=false;
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon f�r den Eintrag (kann <code>null</code> sein)
	 * @param command	Befehl, der in das Textfeld eingef�gt werden soll (muss ein "%s" enthalten).
	 * @param owner	�bergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param model	Editor-Modell dem die Daten entnommen werden sollen
	 * @param help	Hilfe-Runnable (f�r Dialog)
	 * @param preferProcessStations	Soll wenn m�glich in der Liste eine Bedienstation oder Verz�gerungsstation initial ausgew�hlt werden?
	 * @param allowEmpty	Soll auch ein leerer Parameter zul�ssig sein?
	 * @param allowNames	Soll es zul�ssig sein, Stationen �ber ihre Namen zu identifizieren?
	 */
	public ScriptPopupItemCommandID(final String name, final String hint, final Icon icon, final String command, final Component owner, final EditModel model, final Runnable help, final boolean preferProcessStations, final boolean allowEmpty, final boolean allowNames) {
		super(name,hint,icon,command,owner,model,help);
		stationTypes=null;
		this.preferProcessStations=preferProcessStations;
		this.allowEmpty=allowEmpty;
		this.allowNames=allowNames;
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon f�r den Eintrag (kann <code>null</code> sein)
	 * @param command	Befehl, der in das Textfeld eingef�gt werden soll (muss ein "%s" enthalten).
	 * @param owner	�bergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param model	Editor-Modell dem die Daten entnommen werden sollen
	 * @param help	Hilfe-Runnable (f�r Dialog)
	 * @param stationTypes	Liste der Klassen der Elementtypen auf die die Auswahl eingeschr�nkt werden soll (wird hier <code>null</code> oder eine leere Liste �bergeben, so erfolgt keine Einschr�nkung)
	 */
	public ScriptPopupItemCommandID(final String name, final String hint, final Icon icon, final String command, final Component owner, final EditModel model, final Runnable help, final Class<?>[] stationTypes) {
		super(name,hint,icon,command,owner,model,help);
		this.stationTypes=stationTypes;
		preferProcessStations=false;
		allowEmpty=false;
		allowNames=false;
	}

	@Override
	protected String getParameter(final Component owner, final EditModel model, final Runnable help) {
		final SelectIDDialog dialog=new SelectIDDialog(owner,model,help,stationTypes,preferProcessStations,allowEmpty,allowNames);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
		final String name=dialog.getSelectedName();
		if (name!=null && !name.isBlank()) {
			return "\""+name+"\"";
		} else {
			final int id=dialog.getSelectedID();
			if (allowEmpty && id==-2) return "";
			if (id<0) return null;
			return ""+id;
		}
	}
}
