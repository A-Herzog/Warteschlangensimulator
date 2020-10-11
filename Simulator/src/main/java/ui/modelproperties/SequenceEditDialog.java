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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSequence;
import ui.modeleditor.ModelSequenceStep;

/**
 * Diese Klasse stellt einen Dialog zur Bearbeitung eines Fertigungsplans zur Verfügung.
 * @author Alexander Herzog
 * @see SequencesEditPanel
 * @see ModelSequence
 */
public class SequenceEditDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6625482419664030626L;

	private final ModelSequence sequence;
	private final List<ModelSequenceStep> steps;
	private final String[] names;
	private final int namesIndex;

	private final JTableExt table;
	private final JTextField nameEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param sequence	Zu bearbeitender Fertigungsplan
	 * @param list	Liste mit allen bisherigen Fertigungsplan (zur Erstellung der Liste der bereits vergebenen Namen)
	 * @param index	Aktueller Index des zu bearbeitenden Plans in der Liste (zur Prüfung auf doppelte Namen notwendig; darf -1 sein)
	 * @param destinations	Liste mit den Namen der Zielstationen
	 * @param help	Hilfe-Runnable
	 * @param model	Editor-Model (für den Expression-Builder-Dialog)
	 */
	public SequenceEditDialog(final Component owner, final ModelSequence sequence, final ModelSequence[] list, final int index, final String[] destinations, final Runnable help, final EditModel model) {
		super(owner,Language.tr("Editor.Dialog.Sequences.Edit.Title"));

		this.sequence=sequence;
		steps=new ArrayList<>();
		for (ModelSequenceStep step: sequence.getSteps()) steps.add(step.clone());
		names=new String[list.length];
		for (int i=0;i<list.length;i++) names[i]=list[i].getName();
		namesIndex=index;

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Sequences.Edit.Name")+":",sequence.getName());
		content.add((JPanel)data[0],BorderLayout.NORTH);
		nameEdit=(JTextField)data[1];
		nameEdit.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		content.add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
		table.setModel(new SequenceEditTableModel(table,steps,destinations,help,model));
		table.setIsPanelCellTable(1);
		table.setIsPanelCellTable(2);
		table.setIsPanelCellTable(4);
		table.getColumnModel().getColumn(0).setMinWidth(50);
		table.getColumnModel().getColumn(0).setMaxWidth(50);
		table.getColumnModel().getColumn(3).setMinWidth(100);
		table.getColumnModel().getColumn(3).setMaxWidth(100);
		table.getColumnModel().getColumn(4).setMinWidth(100);
		table.getColumnModel().getColumn(4).setMaxWidth(100);

		checkData(false);

		setMinSizeRespectingScreensize(600,500);
		setSizeRespectingScreensize(600,500);
		setResizable(true);
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
		boolean ok=true;

		if (name.isEmpty()) {
			ok=false;
			nameEdit.setBackground(Color.RED);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Editor.Dialog.Sequences.Edit.Name.ErrorTitle"),Language.tr("Editor.Dialog.Sequences.Edit.Name.ErrorInfoEmpty"));
				return false;
			}
		} else {
			boolean inList=false;
			for (int i=0;i<names.length;i++) if (i!=namesIndex && name.equalsIgnoreCase(names[i])) {inList=true; break;}
			if (inList) {
				ok=false;
				nameEdit.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Editor.Dialog.Sequences.Edit.Name.ErrorTitle"),String.format(Language.tr("Editor.Dialog.Sequences.Edit.Name.ErrorInfoInUse"),name));
					return false;
				}
			} else {
				nameEdit.setBackground(SystemColor.text);
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		sequence.setName(nameEdit.getText().trim());

		sequence.getSteps().clear();
		for (ModelSequenceStep step: steps) sequence.getSteps().add(step);
	}
}
