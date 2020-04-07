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
package ui.modeleditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.Table;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.parameterseries.ParameterCompareSetupValueInput;
import ui.parameterseries.ParameterCompareSetupValueInputPanel;

/**
 * Ermöglicht das Bearbeiten eines einzelnen Externe-Daten-Lade-Datensatzes ({@link ModelLoadDataRecord}).
 * @author Alexander Herzog
 * @see ModelLoadDataDialog
 * @see ModelLoadDataRecord
 */
public class ModelLoadDataRecordDialog extends BaseDialog {
	private static final long serialVersionUID=-2465183329338223046L;

	private final ParameterCompareSetupValueInput change;

	private final JTextField cell;
	private final ParameterCompareSetupValueInputPanel editor;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param record	Zu bearbeitender Datensatz (kann auch <code>null</code> sein)
	 * @param model	Editormodell (zum Auslesen der verfügbaren Daten für Parameter)
	 */
	public ModelLoadDataRecordDialog(final Component owner, final ModelLoadDataRecord record, final EditModel model) {
		super(owner,Language.tr("ModelLoadData.EditDialog.RecordTitle"));

		change=new ParameterCompareSetupValueInput();
		if (record!=null) change.copyDataFrom(record.getChange());

		final JPanel main=createGUI(()->Help.topicModal(this,"ModelLoadData"));
		main.setLayout(new BorderLayout());

		/* Tabellenzelle */

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		main.add(line,BorderLayout.NORTH);

		final JLabel label=new JLabel(Language.tr("ModelLoadData.EditDialog.Cell")+":");
		line.add(label);
		final String cellName;
		if (record==null) {
			cellName="A1";
		} else {
			cellName=record.getCell();
		}
		line.add(cell=new JTextField(cellName,7));
		label.setLabelFor(cell);

		/* Daten */

		main.add(editor=new ParameterCompareSetupValueInputPanel(getOwner(),change,model,()->Help.topicModal(this,"ModelLoadData"),false));

		/* Dialog starten */

		setMinSizeRespectingScreensize(650,0);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (Table.cellIDToNumbers(cell.getText().trim())==null) {
			ok=false;
			cell.setBackground(Color.RED);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("ModelLoadData.EditDialog.Cell.ErrorTitle"),String.format(Language.tr("ModelLoadData.EditDialog.Cell.ErrorInfo"),cell.getText().trim()));
				return false;
			}
		} else {
			cell.setBackground(SystemColor.text);
		}

		if (!editor.checkData(showErrorMessage)) ok=false;

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert den neuen Datensatz auf Basis der Dialogeinstellungen.
	 * @return	Liefert, wenn der Dialog mit "Ok" geschlossen wurde, den neuen Datensatz auf Basis der Dialogeinstellungen; sonst <code>null</code>.
	 */
	public ModelLoadDataRecord getRecord() {
		if (getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;

		final ModelLoadDataRecord record=new ModelLoadDataRecord();

		record.setCell(cell.getText().trim());
		editor.storeData();
		record.getChange().copyDataFrom(change);

		return record;
	}

}
