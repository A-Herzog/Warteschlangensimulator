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
package systemtools;

import java.awt.Component;
import java.io.File;

/**
 * Dieses Interface dient zur Abstraktion verschiedener Backends zur Anzeige von Meldungen gegenüber
 * der statischen Klasse <code>MsgBox</code>.<br>
 * Über <code>MsgBox.setBackend()</code> kann eine Instanz eines Backends eingestellt werden, welches
 * zur Darstellung der Dialoge, die <code>MsgBox</code> über seine statischen Methoden bietet,
 * verwendet werden soll.
 * @author Alexander Herzog
 * @see MsgBox
 * @version 1.1
 */
public interface MsgBoxBackend {
	/**
	 * Zeigt einen Informationsdialog an.
	 * @param parentComponent	Übergeordnetes Element
	 * @param title	Titel der Meldung
	 * @param message	Anzuzeigende Meldung
	 */
	public void info(Component parentComponent, String title, String message);

	/**
	 * Zeigt einen Warnungdialog an.
	 * @param parentComponent	Übergeordnetes Element
	 * @param title	Titel der Meldung
	 * @param message	Anzuzeigende Meldung
	 */
	public void warning(Component parentComponent, String title, String message);

	/**
	 * Zeigt eine Fehlermeldung an.
	 * @param parentComponent	Übergeordnetes Element
	 * @param title	Titel der Meldung
	 * @param message	Anzuzeigende Meldung
	 */
	public void error(Component parentComponent, String title, String message);

	/**
	 * Zeigt einen Ja/Nein/Abbrechen-Dialog an.
	 * @param parentComponent	Übergeordnetes Element
	 * @param title	Titel der Anfrage
	 * @param message	Anzuzeigende Meldung
	 * @param infoYes	Infotext für die "Ja"-Option (muss nicht von allen Backends verwendet werden)
	 * @param infoNo	Infotext für die "Nein"-Option (muss nicht von allen Backends verwendet werden)
	 * @param infoCancel	Infotext für die "Abbrechen"-Option (muss nicht von allen Backends verwendet werden)
	 * @return	Gibt eine der Ja/Nein/Abbrechen-Konstanten aus <code>JOptionPane</code> zurück.
	 */
	public int confirm(Component parentComponent, String title, String message, String infoYes, String infoNo, String infoCancel);

	/**
	 * Zeigt einen "Sollen die nicht gespeicherten Daten jetzt gespeichert werden"-Dialog an; verwendet dafür
	 * die Ja/Nein/Abbrechen-Schaltflächen.
	 * @param parentComponent	Übergeordnetes Element
	 * @param title	Titel der Anfrage
	 * @param message	Anzuzeigende Meldung
	 * @return Gibt eine der Ja/Nein/Abbrechen-Konstanten aus <code>JOptionPane</code> zurück.
	 */
	public int confirmSave(Component parentComponent, String title, String message);

	/**
	 * Zeigt einen Ja/Nein-Dialog an.
	 * @param parentComponent	Übergeordnetes Element
	 * @param title	Titel der Anfrage
	 * @param message	Anzuzeigende Meldung
	 * @param infoYes	Infotext für die "Ja"-Option (muss nicht von allen Backends verwendet werden)
	 * @param infoNo	Infotext für die "Nein"-Option (muss nicht von allen Backends verwendet werden)
	 * @return	Gibt <code>true</code> zurück, wenn der Dialog mit "Ja" geschlossen wurde.
	 */
	public boolean confirm(Component parentComponent, String title, String message, String infoYes, String infoNo);

	/**
	 * Zeigt eine Abfrage an, ob die angegebene Datei überschrieben werden soll
	 * @param parentComponent	Übergeordnetes Element
	 * @param file	Name der Datei, für die die Abfrage durchgeführt werden soll
	 * @return	Gibt <code>true</code> zurück, wenn der Nutzer dem Überschreiben zugestimmt hat.
	 */
	public boolean confirmOverwrite(Component parentComponent, File file);

	/**
	 * Zeigt einen Auswahldialog mit verschiedenen Optionen an.
	 * @param parentComponent	Übergeordnetes Element
	 * @param title	Titel der Anfrage
	 * @param message	Anzuzeigende Meldung
	 * @param options	Texte zu den Optionen
	 * @param info	Erklärungen zu den Texten zu den Optionen
	 * @return	Gewählte Option (0-basierend); -1 für Abbruch
	 */
	public int options(Component parentComponent, String title, String message, String[] options, String[] info);
}
