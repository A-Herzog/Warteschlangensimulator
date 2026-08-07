/**
 * Copyright 2026 Alexander Herzog
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionMultiEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Bearbeitendialog für ein einzelnes Teilsignal.
 * @see ModelElementSignalMultiTableModel
 * @see ModelElementSignalMulti
 */
public class ModelElementSignalMultiTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-7496398495110474520L;

	/**
	 * Zu bearbeitender Signaldatensatz
	 */
	private final ModelElementSignalMultiRecord record;

	/**
	 * Editor-Modell (zur Anzeige des Expression-Builders)
	 */
	private final EditModel model;

	/**
	 * Eingabefeld für den Namen des Signals
	 */
	private JTextField signalName;

	/**
	 * Eingabefeld für die Verzögerung bei der Signalauslösung
	 */
	private JTextField signalDelay;

	/**
	 * Checkbox: Soll die Bedingung verwendet werden?
	 */
	private JCheckBox useCondition;

	/**
	 * Eingabefeld für die optionale Bedingung
	 */
	private JTextField condition;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param record	Zu bearbeitender Signaldatensatz
	 * @param model	Editor-Modell (zur Anzeige des Expression-Builders)
	 */
	public ModelElementSignalMultiTableModelDialog(final Component owner, final ModelElementSignalMultiRecord record, final EditModel model) {
		super(owner,Language.tr("Surface.MultiSignal.DialogRecord.Title"));

		this.record=record;
		this.model=model;

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(this,"ModelElementSignalMulti"));
		all.setLayout(new BorderLayout());

		final JPanel content=new JPanel();
		all.add(content,BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		Object[] data;

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.MultiSignal.Dialog.Name")+":",record.getName(),30);
		content.add(line=(JPanel)data[0]);
		signalName=(JTextField)data[1];
		signalName.setEnabled(!readOnly);
		signalName.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Signal.Dialog.DelayedExecution")+":",NumberTools.formatNumberMax(record.getSignalDelay()),7);
		content.add(line=(JPanel)data[0]);
		signalDelay=(JTextField)data[1];
		signalDelay.setEnabled(!readOnly);
		signalDelay.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(new JLabel(" ("+Language.tr("Statistic.Seconds")+")"));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useCondition=new JCheckBox(Language.tr("Surface.Signal.Dialog.Condition.UseCondition")+":",!record.getCondition().isEmpty()));
		useCondition.addActionListener(e->checkData(false));
		useCondition.setEnabled(!readOnly);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Signal.Dialog.AdditionalCondition")+":",record.getCondition());
		content.add(line=(JPanel)data[0]);
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,(JTextField)data[1],true,true,model,model.surface),BorderLayout.EAST);
		condition=(JTextField)data[1];
		condition.setEnabled(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {useCondition.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {useCondition.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {useCondition.setSelected(true); checkData(false);}
		});

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (signalName.getText().isBlank()) {
			signalName.setBackground(Color.RED);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.MultiSignal.Dialog.Name.ErrorTitle"),Language.tr("Surface.MultiSignal.Dialog.Name.ErrorInfo"));
				return false;
			}
		} else {
			signalName.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		final Double D=NumberTools.getNotNegativeDouble(signalDelay,true);
		if (D==null) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Signal.Dialog.DelayedExecution.ErrorTitle"),Language.tr("Surface.Signal.Dialog.DelayedExecution.ErrorInfo"));
				return false;
			}
			ok=false;
		}

		final String conditionString=condition.getText().trim();
		if (!useCondition.isSelected() || conditionString.isEmpty()) {
			condition.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			final int error=ExpressionMultiEval.check(conditionString,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
			if (error>=0) {
				condition.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Assign.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.Assign.Dialog.Condition.Error.Info"),condition,error+1));
					return false;
				}
				ok=false;
			} else {
				condition.setBackground(NumberTools.getTextFieldDefaultBackground());
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
		record.setName(signalName.getText().trim());
		record.setSignalDelay(NumberTools.getNotNegativeDouble(signalDelay,true));
		record.setCondition(useCondition.isSelected()?condition.getText().trim():"");
	}
}
