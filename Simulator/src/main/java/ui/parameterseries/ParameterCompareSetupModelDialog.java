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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog zum Bearbeiten von eines Modells innerhalb
 * der Parameter-Vergleichs-Funktion
 * @author Alexander Herzog
 */
public class ParameterCompareSetupModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3223934203763679697L;

	/** Modell innerhalb der Parameter-Vergleichs-Funktion, das bearbeitet werden soll */
	private final ParameterCompareSetupModel model;

	private final JTextField nameEdit;
	private final ParameterCompareSetupModelTableModel tableModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Modell innerhalb der Parameter-Vergleichs-Funktion, das bearbeitet werden soll
	 * @param input	Liste der Eingabeparameter-Einstellungen
	 * @param help	Hilfe-Runnable
	 */
	public ParameterCompareSetupModelDialog(final Component owner, final ParameterCompareSetupModel model, final List<ParameterCompareSetupValueInput> input, final Runnable help) {
		super(owner,Language.tr("ParameterCompare.Settings.Model.Title"));
		this.model=model;

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		JPanel sub;

		content.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("ParameterCompare.Settings.Model.Name")+":",model.getName());
		sub.add((JPanel)data[0]);
		nameEdit=(JTextField)data[1];
		nameEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		final JTableExt table=new JTableExt();
		content.add(new JScrollPane(table),BorderLayout.CENTER);
		table.setModel(tableModel=new ParameterCompareSetupModelTableModel(table,model,input));
		tableModel.updateTable();
		table.getColumnModel().getColumn(1).setMinWidth(125);
		table.getColumnModel().getColumn(1).setMaxWidth(125);

		checkData(false);

		setMinSizeRespectingScreensize(650,500);
		pack();
		setResizable(true);
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (nameEdit.getText().trim().isEmpty()) {
			nameEdit.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("ParameterCompare.Settings.Model.Name.ErrorTitle"),Language.tr("ParameterCompare.Settings.Model.Name.ErrorInfo"));
				return false;
			}
		} else {
			nameEdit.setBackground(SystemColor.text);
		}

		final String error=tableModel.checkData();
		if (error!=null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("ParameterCompare.Settings.Model.Data.ValueError.Title"),error);
				return false;
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
		model.setName(nameEdit.getText().trim());
		tableModel.storeData(model);
	}
}