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
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementMatch}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementMatch
 */
public class ModelElementMatchDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2581147688988297708L;

	/** Option: Kunden gemeinsam weiterleiten */
	private JRadioButton optionForward;
	/** Option: Temporären Batch bilden */
	private JRadioButton optionTemporary;
	/** Eingabefeld für den neuen Kundentyp für einen temporären Batch */
	private JTextField tempTypeField;
	/** Option: Permanenten Batch bilden */
	private JRadioButton optionNewType;
	/** Eingabefeld für den neuen Kundentyp für einen permanenten Batch */
	private JTextField newTypeField;

	/** Option: Kein Eigenschaftsabgleich */
	private JRadioButton optionPropertyNone;
	/** Option: Abgleich über Kundendatenfeld mit Index */
	private JRadioButton optionPropertyNumber;
	/** Option: Abgleich über Kundendatentextfeld mit Schlüssel  */
	private JRadioButton optionPropertyText;
	/** Eingabefeld für den Index im Fall {@link #optionPropertyNumber} */
	private JTextField optionPropertyNumberField;
	/** Eingabefeld für den Schlüssel im Fall {@link #optionPropertyText} */
	private JTextField optionPropertyTextField;

	/** Soll zusätzlich für eine Freigabe eine Bedingung geprüft werden? */
	private JCheckBox conditionEnabled;
	/** Eingabefeld für die Bedingung */
	private JTextField condition;

	/** Wie sollen die Zeiten der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden? */
	private JComboBox<?> transferTimes;
	/** Wie sollen die numerischen Datenfelder der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden? */
	private JComboBox<?> transferNumbers;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementMatch}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementMatchDialog(final Component owner, final ModelElementMatch element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Match.Dialog.Title"),element,"ModelElementMatch",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationMatch;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		ButtonGroup buttonGroup;
		Object[] data;

		/* Batch-Bildungsoptionen */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.Match.Dialog.OptionsBatching")+"</b></body></html>"));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionForward=new JRadioButton(Language.tr("Surface.Match.Dialog.OptionJustForward")));
		optionForward.setEnabled(!readOnly);
		optionForward.addActionListener(e->checkData(false));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionTemporary=new JRadioButton(Language.tr("Surface.Match.Dialog.SendTemporaryBatched")+":"));
		optionTemporary.setEnabled(!readOnly);
		optionTemporary.addActionListener(e->checkData(false));
		line.add(tempTypeField=new JTextField(25));
		ModelElementBaseDialog.addUndoFeature(tempTypeField);
		tempTypeField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionTemporary.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionTemporary.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionTemporary.setSelected(true); checkData(false);}
		});
		tempTypeField.setEditable(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionNewType=new JRadioButton(Language.tr("Surface.Match.Dialog.OptionBatch")+":"));
		optionNewType.setEnabled(!readOnly);
		optionNewType.addActionListener(e->checkData(false));
		line.add(newTypeField=new JTextField(25));
		ModelElementBaseDialog.addUndoFeature(newTypeField);
		newTypeField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionNewType.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionNewType.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionNewType.setSelected(true); checkData(false);}
		});
		newTypeField.setEditable(!readOnly);

		buttonGroup=new ButtonGroup();
		buttonGroup.add(optionForward);
		buttonGroup.add(optionTemporary);
		buttonGroup.add(optionNewType);

		/* Eigenschaftsabgleich */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.Match.Dialog.OptionsPropertyMatch")+"</b></body></html>"));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionPropertyNone=new JRadioButton(Language.tr("Surface.Match.Dialog.OptionProperty.Off")));
		optionPropertyNone.setEnabled(!readOnly);
		optionPropertyNone.addActionListener(e->checkData(false));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionPropertyNumber=new JRadioButton(Language.tr("Surface.Match.Dialog.OptionProperty.Number")+":"));
		optionPropertyNumber.setEnabled(!readOnly);
		optionPropertyNumber.addActionListener(e->checkData(false));
		line.add(optionPropertyNumberField=new JTextField(5));
		ModelElementBaseDialog.addUndoFeature(optionPropertyNumberField);
		optionPropertyNumberField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionPropertyNumber.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionPropertyNumber.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionPropertyNumber.setSelected(true); checkData(false);}
		});
		optionPropertyNumberField.setEditable(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionPropertyText=new JRadioButton(Language.tr("Surface.Match.Dialog.OptionProperty.Text")+":"));
		optionPropertyText.setEnabled(!readOnly);
		optionPropertyText.addActionListener(e->checkData(false));
		line.add(optionPropertyTextField=new JTextField(25));
		ModelElementBaseDialog.addUndoFeature(optionPropertyTextField);
		optionPropertyTextField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionPropertyText.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionPropertyText.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionPropertyText.setSelected(true); checkData(false);}
		});
		optionPropertyTextField.setEditable(!readOnly);

		buttonGroup=new ButtonGroup();
		buttonGroup.add(optionPropertyNone);
		buttonGroup.add(optionPropertyNumber);
		buttonGroup.add(optionPropertyText);

		/* Bedingung */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(conditionEnabled=new JCheckBox("<html><body><b>"+Language.tr("Surface.Match.Dialog.ConditionEnabled")+"</b></body></html>"));
		conditionEnabled.addActionListener(e->checkData(false));

		data=getInputPanel(Language.tr("Surface.Match.Dialog.Condition")+":","");
		line=(JPanel)data[0];
		condition=(JTextField)data[1];
		condition.setEditable(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {if (!condition.getText().trim().isEmpty()) conditionEnabled.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {if (!condition.getText().trim().isEmpty()) conditionEnabled.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {if (!condition.getText().trim().isEmpty()) conditionEnabled.setSelected(true); checkData(false);}
		});
		line.add(getExpressionEditButton(this,condition,true,true,element.getModel(),element.getSurface()),BorderLayout.EAST);
		content.add(line);

		/* Daten von Eingangskunden auf Batch-Kunden übertragen */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.Match.Dialog.TransferData")+"</b></body></html>"));
		data=ModelElementBaseDialog.getComboBoxPanel(Language.tr("Surface.Match.Dialog.TransferData.Times")+":",new String[] {
				Language.tr("Surface.Match.Dialog.TransferData.Mode.Off"),
				Language.tr("Surface.Match.Dialog.TransferData.Mode.Min"),
				Language.tr("Surface.Match.Dialog.TransferData.Mode.Max"),
				Language.tr("Surface.Match.Dialog.TransferData.Mode.Mean"),
				Language.tr("Surface.Match.Dialog.TransferData.Mode.Sum"),
				Language.tr("Surface.Match.Dialog.TransferData.Mode.Multiply")
		});
		content.add((JPanel)data[0]);
		transferTimes=(JComboBox<?>)data[1];
		transferTimes.setEnabled(!readOnly);

		data=ModelElementBaseDialog.getComboBoxPanel(Language.tr("Surface.Match.Dialog.TransferData.Numbers")+":",new String[] {
				Language.tr("Surface.Match.Dialog.TransferData.Mode.Off"),
				Language.tr("Surface.Match.Dialog.TransferData.Mode.Min"),
				Language.tr("Surface.Match.Dialog.TransferData.Mode.Max"),
				Language.tr("Surface.Match.Dialog.TransferData.Mode.Mean"),
				Language.tr("Surface.Match.Dialog.TransferData.Mode.Sum"),
				Language.tr("Surface.Match.Dialog.TransferData.Mode.Multiply")
		});
		content.add((JPanel)data[0]);
		transferNumbers=(JComboBox<?>)data[1];
		transferNumbers.setEnabled(!readOnly);

		/* Daten laden */

		final ModelElementMatch match=(ModelElementMatch)element;

		switch (match.getMatchMode()) {
		case MATCH_MODE_COLLECT:
			optionForward.setSelected(true);
			break;
		case MATCH_MODE_TEMPORARY:
			optionTemporary.setSelected(true);
			tempTypeField.setText(match.getNewClientType());
			break;
		case MATCH_MODE_PERMANENT:
			optionNewType.setSelected(true);
			newTypeField.setText(match.getNewClientType());
			break;
		}

		switch (match.getMatchPropertyMode()) {
		case NONE:
			optionPropertyNone.setSelected(true);
			break;
		case NUMBER:
			optionPropertyNumber.setSelected(true);
			optionPropertyNumberField.setText(""+match.getMatchPropertyNumber());
			break;
		case TEXT:
			optionPropertyText.setSelected(true);
			optionPropertyTextField.setText(match.getMatchPropertyString());
			break;
		}

		conditionEnabled.setSelected(!match.getCondition().trim().isEmpty());
		condition.setText(match.getCondition());

		switch (match.getTransferTimes()) {
		case OFF: transferTimes.setSelectedIndex(0); break;
		case MIN: transferTimes.setSelectedIndex(1); break;
		case MAX: transferTimes.setSelectedIndex(2); break;
		case MEAN: transferTimes.setSelectedIndex(3); break;
		case SUM: transferTimes.setSelectedIndex(4); break;
		case MULTIPLY: transferTimes.setSelectedIndex(5); break;
		default: transferTimes.setSelectedIndex(0); break;
		}

		switch (match.getTransferNumbers()) {
		case OFF: transferNumbers.setSelectedIndex(0); break;
		case MIN: transferNumbers.setSelectedIndex(1); break;
		case MAX: transferNumbers.setSelectedIndex(2); break;
		case MEAN: transferNumbers.setSelectedIndex(3); break;
		case SUM: transferNumbers.setSelectedIndex(4); break;
		case MULTIPLY: transferNumbers.setSelectedIndex(5); break;
		default: transferNumbers.setSelectedIndex(0); break;
		}

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (optionNewType.isSelected() && newTypeField.getText().isEmpty()) {
			newTypeField.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Match.Dialog.OptionBatch.Error.Title"),Language.tr("Surface.Match.Dialog.OptionBatch.Error.Info"));
				return false;
			}
		} else {
			newTypeField.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (optionTemporary.isSelected() && tempTypeField.getText().isEmpty()) {
			tempTypeField.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Match.Dialog.SendTemporaryBatched.Error.Title"),Language.tr("Surface.Match.Dialog.SendTemporaryBatched.Error.Info"));
				return false;
			}
		} else {
			tempTypeField.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (optionPropertyNumber.isSelected()) {
			final Integer I=NumberTools.getNotNegativeInteger(optionPropertyNumberField,true);
			if (I==null) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Match.Dialog.OptionProperty.Number.Error.Title"),Language.tr("Surface.Match.Dialog.OptionProperty.Number.Error.Info"));
					return false;
				}
			}
		}

		if (optionPropertyText.isSelected() && optionPropertyTextField.getText().isEmpty()) {
			optionPropertyTextField.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Match.Dialog.OptionProperty.Text.Error.Title"),Language.tr("Surface.Match.Dialog.OptionProperty.Text.Error.Info"));
				return false;
			}
		} else {
			optionPropertyTextField.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (conditionEnabled.isSelected()) {
			final String text=condition.getText();
			if (text.trim().isEmpty()) {
				condition.setBackground(NumberTools.getTextFieldDefaultBackground());
			} else {
				final int error=ExpressionMultiEval.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true),element.getModel().userFunctions);
				if (error>=0) {
					ok=false;
					condition.setBackground(Color.RED);
					if (showErrorMessage) {
						MsgBox.error(this,Language.tr("Surface.Match.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.Match.Dialog.Condition.Error.Info"),text,error+1));
						return false;
					}
				} else {
					condition.setBackground(NumberTools.getTextFieldDefaultBackground());
				}
			}
		} else {
			condition.setBackground(NumberTools.getTextFieldDefaultBackground());
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
		final ModelElementMatch match=(ModelElementMatch)element;

		if (optionForward.isSelected()) match.setMatchMode(ModelElementMatch.MatchMode.MATCH_MODE_COLLECT);
		if (optionTemporary.isSelected()) {
			match.setMatchMode(ModelElementMatch.MatchMode.MATCH_MODE_TEMPORARY);
			match.setNewClientType(tempTypeField.getText());
		}
		if (optionNewType.isSelected()) {
			match.setMatchMode(ModelElementMatch.MatchMode.MATCH_MODE_PERMANENT);
			match.setNewClientType(newTypeField.getText());
		}

		if (optionPropertyNone.isSelected()) match.setMatchPropertyMode(ModelElementMatch.MatchPropertyMode.NONE);
		if (optionPropertyNumber.isSelected()) {
			match.setMatchPropertyMode(ModelElementMatch.MatchPropertyMode.NUMBER);
			match.setMatchPropertyNumber(NumberTools.getNotNegativeInteger(optionPropertyNumberField,true));
		}
		if (optionPropertyText.isSelected()) {
			match.setMatchPropertyMode(ModelElementMatch.MatchPropertyMode.TEXT);
			match.setMatchPropertyString(optionPropertyTextField.getText());
		}

		if (conditionEnabled.isSelected()) {
			match.setCondition(condition.getText());
		} else {
			match.setCondition("");
		}

		switch (transferTimes.getSelectedIndex()) {
		case 0: match.setTransferTimes(BatchRecord.DataTransferMode.OFF); break;
		case 1: match.setTransferTimes(BatchRecord.DataTransferMode.MIN); break;
		case 2: match.setTransferTimes(BatchRecord.DataTransferMode.MAX); break;
		case 3: match.setTransferTimes(BatchRecord.DataTransferMode.MEAN); break;
		case 4: match.setTransferTimes(BatchRecord.DataTransferMode.SUM); break;
		case 5: match.setTransferTimes(BatchRecord.DataTransferMode.MULTIPLY); break;
		}
		switch (transferNumbers.getSelectedIndex()) {
		case 0: match.setTransferNumbers(BatchRecord.DataTransferMode.OFF); break;
		case 1: match.setTransferNumbers(BatchRecord.DataTransferMode.MIN); break;
		case 2: match.setTransferNumbers(BatchRecord.DataTransferMode.MAX); break;
		case 3: match.setTransferNumbers(BatchRecord.DataTransferMode.MEAN); break;
		case 4: match.setTransferNumbers(BatchRecord.DataTransferMode.SUM); break;
		case 5: match.setTransferNumbers(BatchRecord.DataTransferMode.MULTIPLY); break;
		}
	}
}
