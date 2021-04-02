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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSequence;

/**
 * Dialog zur Eingabe eines neuen Namens für eine Kopie eines Fertigungsplan-Objektes
 * @author Alexander Herzog
 * @see SequencesEditPanel
 */
public class SequenceCopyDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3306795145111165668L;

	/** Kopierter Fertigungsplan (in diesen wird beim Schließen des Dialogs der neue Name eingetragen) */
	private final ModelSequence sequence;
	/** Bereits verwente Namen für Fertigungspläne */
	private final String[] namesInUse;

	/** Eingabefeld für den Namen des Fertigungsplans */
	private final JTextField nameEdit;
	/** Zeigt ggf. Fehlermeldungen an, wenn der eingegebene Name für den Fertigungsplan ungültig ist */
	private final JLabel nameInfo;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param sequence	Kopierter Fertigungsplan (in diesen wird beim Schließen des Dialogs der neue Name eingetragen)
	 * @param list	Liste der bisherigen Fertigungspläne (die den neuen Plan noch nicht enthält)
	 * @param help	Hilfe-Runnable
	 */
	public SequenceCopyDialog(final Component owner, final ModelSequence sequence, final ModelSequence[] list, final Runnable help) {
		super(owner,Language.tr("Editor.Dialog.Sequences.Copy.Title"));

		this.sequence=sequence;
		namesInUse=new String[list.length];
		for (int i=0;i<list.length;i++) namesInUse[i]=list[i].getName();

		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Sequences.Copy.NewName")+":",sequence.getName());
		content.add((JPanel)data[0]);
		nameEdit=(JTextField)data[1];
		nameEdit.addKeyListener(new KeyAdapter(){
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		line.add(nameInfo=new JLabel());

		checkData(false);

		setMinSizeRespectingScreensize(500,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		final String name=nameEdit.getText().trim();

		if (name.isEmpty()) {
			nameEdit.setBackground(Color.RED);
			nameInfo.setText(Language.tr("Editor.Dialog.Sequences.Copy.NewName.ErrorInfoEmpty"));
			nameInfo.setVisible(true);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Editor.Dialog.Sequences.Copy.NewName.ErrorTitle"),Language.tr("Editor.Dialog.Sequences.Copy.NewName.ErrorInfoEmpty"));
			return false;
		} else {
			boolean inUse=false;
			for (String test: namesInUse) if (test.equalsIgnoreCase(name)) {inUse=true; break;}
			if (inUse) {
				nameEdit.setBackground(Color.RED);
				nameInfo.setText(String.format(Language.tr("Editor.Dialog.Sequences.Copy.NewName.ErrorInfoInUse"),name));
				nameInfo.setVisible(true);
				if (showErrorMessage) MsgBox.error(this,Language.tr("Editor.Dialog.Sequences.Copy.NewName.ErrorTitle"),String.format(Language.tr("Editor.Dialog.Sequences.Copy.NewName.ErrorInfoInUse"),name));
				return false;
			}
		}
		nameEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		nameInfo.setVisible(false);
		return true;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		sequence.setName(nameEdit.getText().trim());
	}
}
