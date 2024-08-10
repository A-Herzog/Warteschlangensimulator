/**
 * Copyright 2021 Alexander Herzog
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
package ui.dialogs;

import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tools.SetupData;

/**
 * Abstrakte Basisklasse für die Dialogseiten in
 * {@link SetupDialog}
 * @author Alexander Herzog
 * @see SetupDialog
 */
public abstract class SetupDialogPage extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2443037570781259213L;

	/**
	 * Setup-Singleton
	 */
	protected final SetupData setup;

	/**
	 * Konstruktor der Klasse
	 */
	public SetupDialogPage() {
		setup=SetupData.getSetup();
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	}

	/**
	 * Fügt eine Zeile zu der Dialogseite hinzu.
	 * @return	Neue Zeile (bereits in die Dialogseite eingefügt)
	 */
	protected final JPanel addLine() {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(line);
		return line;
	}

	/**
	 * Fügt eine Zeile zu der Dialogseite hinzu.
	 * @param indent	Zusätzliche Einrückung
	 * @return	Neue Zeile (bereits in die Dialogseite eingefügt)
	 */
	protected final JPanel addLine(final int indent) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(line);
		line.add(Box.createHorizontalStrut(Math.max(0,indent)));
		return line;
	}

	/**
	 * Erstellt eine neue Überschrift
	 * @param title	Überschriftzeile
	 */
	protected void addHeading(final String title) {
		if (getComponentCount()>0) add(Box.createVerticalStrut(15));
		addLine().add(new JLabel("<html><body><b>"+title+"</b></body></html>"));
	}

	/**
	 * Lädt die Daten aus dem Setup-Objekt in die Dialogseite.
	 */
	public abstract void loadData();

	/**
	 * Überprüft die Eingaben.
	 * @return	Liefert <code>true</code>, wenn die Daten gültig sind.
	 */
	public boolean checkData() {
		return true;
	}

	/**
	 * Speichert die Daten der Dialogseite im Setup-Objekt.
	 */
	public void storeData() {
	}

	/**
	 * Setzt die Einstellungen auf dieser Dialogseite wieder auf die Standardwerte zurück.
	 */
	public void resetSettings() {
	}
}