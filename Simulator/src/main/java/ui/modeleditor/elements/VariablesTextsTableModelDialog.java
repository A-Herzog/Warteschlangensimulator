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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dieser Dialog ermöglicht das Bearbeiten einer einzelnen
 * Zuweisung aus einer {@link VariablesTextsTableModel}-Tabelle.
 * @author Alexander Herzog
 * @see VariablesTextsTableModel
 */
public class VariablesTextsTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4219560141889593262L;

	/** Eingabefeld für den Schlüssel */
	private final JTextField keyEdit;
	/** Eingabefeld für den Wert */
	private final JTextField valueEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param key	Bisheriger Schlüssel
	 * @param value	Bisheriger Text der an den Schlüssel zugewiesen werden soll
	 * @param help	Hilfe-Callback
	 */
	public VariablesTextsTableModelDialog(final Component owner, final String key, final String value, final Runnable help) {
		super(owner,Language.tr("Surface.AssignString.Table.Edit.Dialog.Title"));

		/* GUI erstellen */

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());
		final JPanel main=new JPanel();
		content.add(main,BorderLayout.CENTER);
		main.setLayout(new BoxLayout(main,BoxLayout.PAGE_AXIS));

		/* Eingabezeilen */

		Object[] data;

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AssignString.Table.Edit.Dialog.Key")+":",key);
		main.add((JPanel)data[0]);
		keyEdit=(JTextField)data[1];
		keyEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AssignString.Table.Edit.Dialog.Value")+":",value);
		main.add((JPanel)data[0]);
		valueEdit=(JTextField)data[1];
		valueEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Dialog starten */

		setMinSizeRespectingScreensize(600,0);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Callback
	 */
	public VariablesTextsTableModelDialog(final Component owner, final Runnable help) {
		this(owner,"","",help);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (keyEdit.getText().trim().isEmpty()) {
			keyEdit.setBackground(Color.RED);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.AssignString.Table.Edit.Dialog.Key.Error.Title"),Language.tr("Surface.AssignString.Table.Edit.Dialog.Key.Error.Info"));
			return false;
		} else {
			keyEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			return true;
		}
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Wurde der Dialog per "Ok" geschlossen, so kann über diese Methode der neue Zuweisungsschlüssel abgefragt werden.
	 * @return	Neuer Zuweisungsschlüssel
	 */
	public String getKey() {
		return keyEdit.getText();
	}

	/**
	 * Wurde der Dialog per "Ok" geschlossen, so kann über diese Methode der neue Text, der zugewiesen werden soll, abgefragt werden.
	 * @return	Neuer Text, der zugewiesen werden soll
	 */
	public String getValue() {
		return valueEdit.getText();
	}
}
