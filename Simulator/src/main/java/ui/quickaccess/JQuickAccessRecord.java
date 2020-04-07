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
package ui.quickaccess;

import java.util.function.Consumer;

import javax.swing.Icon;

/**
 * Eintrag in der Schnellwahlliste
 * @author Alexander Herzog
 */
public class JQuickAccessRecord {
	/** Gruppenname in der der Eintrag angezeigt werden soll */
	public final String category;

	/** Anzuzeigender Tooltip (darf <code>null</code> sein) */
	public final String tooltip;

	/** Text */
	public final String text;

	/** Anzuzeigender Text */
	public final String textDisplay;

	/** Optional anzuzeigendes Icon (kann <code>null</code> sein) */
	public final Icon icon;

	/** Callback, das beim Anklicken des Eintrags ausgeführt werden soll */
	public final Consumer<JQuickAccessRecord> action;

	/** Zusätzliche Daten für die Action zur Identifikation, was angeklickt wurde */
	public final Object data;

	/**
	 * Konstruktor der Klasse
	 * @param category	Gruppenname in der der Eintrag angezeigt werden soll
	 * @param text	Text
	 * @param textDisplay	Anzuzeigender Text
	 * @param tooltip	Tooltip für den Eintrag
	 * @param icon	Optional anzuzeigendes Icon (kann <code>null</code> sein)
	 * @param action	Callback, das beim Anklicken des Eintrags ausgeführt werden soll
	 * @param data	Zusätzliche Daten für die Action zur Identifikation, was angeklickt wurde
	 */
	public JQuickAccessRecord(final String category, final String text, final String textDisplay, final String tooltip, final Icon icon, final Consumer<JQuickAccessRecord> action, final Object data) {
		this.category=category;
		this.text=text;
		this.textDisplay=textDisplay;
		this.tooltip=tooltip;
		this.icon=icon;
		this.action=action;
		this.data=data;
	}

	/**
	 * Konstruktor der Klasse
	 * @param category	Gruppenname in der der Eintrag angezeigt werden soll
	 * @param text	Text
	 * @param textDisplay	Anzuzeigender Text
	 * @param tooltip	Tooltip für den Eintrag
	 * @param icon	Optional anzuzeigendes Icon (kann <code>null</code> sein)
	 * @param action	Callback, das beim Anklicken des Eintrags ausgeführt werden soll
	 */
	public JQuickAccessRecord(final String category, final String text, final String textDisplay, final String tooltip, final Icon icon, final Consumer<JQuickAccessRecord> action) {
		this(category,text,textDisplay,tooltip,icon,action,null);
	}

	/**
	 *Konstruktor der Klasse
	 * @param category	Gruppenname in der der Eintrag angezeigt werden soll
	 * @param text	Text
	 * @param textDisplay	Anzuzeigender Text
	 * @param tooltip	Tooltip für den Eintrag
	 * @param action	Callback, das beim Anklicken des Eintrags ausgeführt werden soll
	 * @param data	Zusätzliche Daten für die Action zur Identifikation, was angeklickt wurde
	 */
	public JQuickAccessRecord(final String category, final String text, final String textDisplay, final String tooltip, final Consumer<JQuickAccessRecord> action, final Object data) {
		this(category,text,textDisplay,tooltip,(Icon)null,action,data);
	}

	/**
	 *Konstruktor der Klasse
	 * @param category	Gruppenname in der der Eintrag angezeigt werden soll
	 * @param text	Text
	 * @param textDisplay	Anzuzeigender Text
	 * @param tooltip	Tooltip für den Eintrag
	 * @param action	Callback, das beim Anklicken des Eintrags ausgeführt werden soll
	 */
	public JQuickAccessRecord(final String category, final String text, final String textDisplay, final String tooltip, final Consumer<JQuickAccessRecord> action) {
		this(category,text,textDisplay,tooltip,(Icon)null,action,null);
	}

	/**
	 * Führt die hinterlegte Aktion aus.
	 */
	void runAction() {
		if (action!=null) action.accept(this);
	}
}