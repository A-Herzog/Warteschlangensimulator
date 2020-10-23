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
package ui.modelproperties;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog zur Konfiguration des Namens eines Zeitplans
 * @author Alexander Herzog
 * @see SchedulesTableModel
 */
public class ScheduleTableModelNameDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7337581671808114921L;

	/** Liste der Namen aller Zeitpläne (inkl. der aktuellen) */
	private final String[] usedNames;
	/** Index dieses Zeitplans in {@link #usedNames} */
	private final int ownIndex;
	/** Eingabefeld für den Namen des Zeitplans */
	private final JTextField nameEdit;
	/** Evtl. sichtbare Fehlermeldung, wenn der eingegebene Name für den Zeitplan nicht gültig ist */
	private final JLabel errorLabel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilf-Callback
	 * @param usedNames	Liste der Namen aller Zeitpläne (inkl. der aktuellen)
	 * @param editName	Aktueller Name des Zeitplans
	 */
	public ScheduleTableModelNameDialog(final Component owner, final Runnable help, final String[] usedNames, final String editName) {
		super(owner,(editName==null)?Language.tr("Schedule.Dialog.TitleCreate"):Language.tr("Schedule.Dialog.TitleEdit"));
		int index=-1;
		for (int i=0;i<usedNames.length;i++) if (usedNames[i].equals(editName)) {index=i; break;}
		ownIndex=index;
		this.usedNames=usedNames;

		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Schedule.Dialog.Name")+":",(editName==null)?getNextFreeName():editName);
		content.add((JPanel)data[0]);
		nameEdit=(JTextField)data[1];
		nameEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		line.add(errorLabel=new JLabel(Language.tr("Schedule.Dialog.Name.InUse")));
		errorLabel.setVisible(false);

		checkData(false);

		setMinSizeRespectingScreensize(450,0);
		pack();
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Liefert einen Namensvorschlag für einen neuen Zeitplan
	 * @return	Namensvorschlag für einen neuen Zeitplan (Namensvorschlag existiert sich noch nicht als Bedienergruppe)
	 */
	private String getNextFreeName() {
		int nr=1;
		while (true) {
			final String testName=Language.tr("Schedule.Schedule")+" "+nr;
			boolean ok=true;
			for (int i=0;i<usedNames.length;i++) if (i!=ownIndex && usedNames[i].equalsIgnoreCase(testName)) {ok=false; break;}
			if (ok) return testName;
			nr++;
		}
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		final String name=nameEdit.getText().trim();

		if (name.isEmpty()) {
			errorLabel.setVisible(true);
			pack();
			nameEdit.setBackground(Color.RED);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Schedule.Dialog.NameMissing.Title"),Language.tr("Schedule.Dialog.NameMissing.Info"));
			return false;
		}

		for (int i=0;i<usedNames.length;i++) if (i!=ownIndex && usedNames[i].equalsIgnoreCase(name)) {
			errorLabel.setVisible(true);
			pack();
			nameEdit.setBackground(Color.RED);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Schedule.Dialog.Name.InUse.Title"),String.format(Language.tr("Schedule.Dialog.Name.InUse.Info"),name));
			return false;
		}

		errorLabel.setVisible(false);
		pack();
		nameEdit.setBackground(SystemColor.text);
		return true;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert den neuen Namen für den Zeitplan
	 * @return	Neuer Name für den Zeitplan
	 */
	public String getScheduleName() {
		return nameEdit.getText().trim();
	}
}
