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
package ui.modelproperties;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JTextField;

import simulator.editmodel.EditModel;

/**
 * Abstrakte Basisklasse für die Dialogseiten in
 * {@link ModelPropertiesDialog}
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 */
public abstract class ModelPropertiesDialogPage {
	/**
	 * Dialog in dem sich diese Seite befindet
	 */
	protected final ModelPropertiesDialog dialog;

	/**
	 * Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 */
	protected final EditModel model;

	/**
	 * Nur-Lese-Status
	 */
	protected final boolean readOnly;

	/**
	 * Hilfe-Callback
	 */
	protected final Runnable help;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPage(final ModelPropertiesDialog dialog, final EditModel model, final boolean readOnly, final Runnable help) {
		this.dialog=dialog;
		this.model=model;
		this.readOnly=readOnly;
		this.help=help;
	}

	/**
	 * Erzeugt und initialisiert die Dialogseite.
	 * @param content	Übergeordnetes Element in das die Dialogelemente eingefügt werden sollen.
	 */
	public abstract void build(final JPanel content);

	/**
	 * Überprüft die Eingaben.
	 * @return	Liefert <code>true</code>, wenn die Daten gültig sind.
	 */
	public boolean checkData() {
		return true;
	}

	/**
	 * Speichert die Daten der Dialogseite.
	 */
	public void storeData() {
	}

	/**
	 * Fügt eine Callback zur Reaktion auf Tastendrücke zu einem Eingabefeld hinzu.
	 * @param field	Eingabefeld
	 * @param action	Aktion die bei Tastendrücken ausgelöst werden soll
	 */
	protected final void addKeyListener(final JTextField field, final Runnable action) {
		field.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {action.run();}
			@Override public void keyPressed(KeyEvent e) {action.run();}
			@Override public void keyReleased(KeyEvent e) {action.run();}
		});
	}
}
