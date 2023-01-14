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
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAssignString}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAssignString
 */
public class ModelElementAssignStringDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3177924513955998727L;

	/**
	 * Tabellenmodell zum Bearbeiten der Textzuweisungen
	 */
	private VariablesTextsTableModel model;

	/**
	 * Checkbox: Soll die Bedingung verwendet werden?
	 */
	private JCheckBox useCondition;

	/**
	 * Eingabefeld für die Bedingung zur Auslösung der Aktion
	 */
	private JTextField condition;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAssignString}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAssignStringDialog(final Component owner, final ModelElementAssignString element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AssignString.Dialog.Title"),element,"ModelElementAssignString",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setSizeRespectingScreensize(800,600);
		pack();
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAssignString;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementAssignString assign=(ModelElementAssignString)element;
		final EditModel editModel=assign.getModel();

		final JPanel content=new JPanel(new BorderLayout());

		JPanel line;
		Object[] data;

		/* Zuweisungen */
		data=VariablesTextsTableModel.buildTable(assign.getRecord(),readOnly,helpRunnable);
		content.add((JScrollPane)data[0],BorderLayout.CENTER);
		model=(VariablesTextsTableModel)data[1];

		/* Optionale Bedingung */
		final JPanel bottomArea=new JPanel();
		bottomArea.setLayout(new BoxLayout(bottomArea,BoxLayout.PAGE_AXIS));
		content.add(bottomArea,BorderLayout.SOUTH);

		bottomArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useCondition=new JCheckBox(Language.tr("Surface.AssignString.Dialog.Condition.UseCondition")+":",!assign.getCondition().isEmpty()));
		useCondition.addActionListener(e->checkData(false));
		useCondition.setEnabled(!readOnly);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AssignString.Dialog.Condition.Condition")+":",assign.getCondition());
		bottomArea.add(line=(JPanel)data[0]);
		condition=(JTextField)data[1];
		condition.setEnabled(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,condition,true,true,editModel,editModel.surface),BorderLayout.EAST);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		final EditModel model=element.getModel();

		boolean ok=true;

		final String conditionString=condition.getText().trim();
		if (!useCondition.isSelected() || conditionString.isEmpty()) {
			condition.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			final int error=ExpressionMultiEval.check(conditionString,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
			if (error>=0) {
				condition.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.AssignString.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.AssignString.Dialog.Condition.Error.Info"),condition,error+1));
					return false;
				}
				ok=false;
			} else {
				condition.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		model.storeData();

		if (useCondition.isSelected()) ((ModelElementAssignString)element).setCondition(condition.getText().trim()); else ((ModelElementAssignString)element).setCondition("");
	}
}