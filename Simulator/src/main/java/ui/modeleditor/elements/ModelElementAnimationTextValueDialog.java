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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationTextValue}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationTextValue
 */
public class ModelElementAnimationTextValueDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 6936633077601457873L;

	private JRadioButton optionExpression;
	private JRadioButton optionTime;
	private JTextField editExpression;
	private JSpinner digits;
	private JCheckBox optionPercent;
	private JComboBox<FontCache.FontFamily> fontFamilyComboBox;
	private JTextField sizeField;
	private JCheckBox optionBold;
	private JCheckBox optionItalic;
	private SmallColorChooser colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationTextValue}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationTextValueDialog(final Component owner, final ModelElementAnimationTextValue element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnimationText.Dialog.Title"),element,"ModelElementAnimationText",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,0);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationTextValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		JPanel line;
		Object[] data;

		final JPanel content=new JPanel();

		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Anzuzeigender Text */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionExpression=new JRadioButton(Language.tr("Surface.AnimationText.Dialog.Expression")+":"));
		optionExpression.setEnabled(!readOnly);
		line.add(editExpression=new JTextField());
		editExpression.setPreferredSize(new Dimension(200,editExpression.getPreferredSize().height));
		editExpression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionExpression.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionExpression.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionExpression.setSelected(true); checkData(false);}
		});
		editExpression.setEditable(!readOnly);
		line.add(getExpressionEditButton(this,editExpression,false,false,element.getModel(),element.getSurface()));

		line.add(optionPercent=new JCheckBox(Language.tr("Surface.AnimationText.Dialog.PercentValue")));
		optionPercent.setEnabled(!readOnly);

		JLabel label;
		line.add(label=new JLabel(Language.tr("Surface.AnimationText.Dialog.Digits")+":"));
		final SpinnerModel spinnerModel=new SpinnerNumberModel(1,0,15,1);
		line.add(digits=new JSpinner(spinnerModel));
		label.setLabelFor(digits);
		digits.setEnabled(!readOnly);
		digits.addChangeListener(e->checkData(false));
		digits.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionTime=new JRadioButton(Language.tr("Surface.AnimationText.Dialog.CurrentSimulationTime")));
		optionTime.setEnabled(!readOnly);

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionExpression);
		buttonGroup.add(optionTime);

		/* Schritftart */
		data=getFontFamilyComboBoxPanel(Language.tr("Surface.AnimationText.Dialog.FontFamily")+":",((ModelElementAnimationTextValue)element).getFontFamily());
		fontFamilyComboBox=(JComboBox<FontCache.FontFamily>)data[1];
		fontFamilyComboBox.setEnabled(!readOnly);
		content.add((JPanel)data[0]);

		/* Schriftgröße */
		data=getInputPanel(Language.tr("Surface.AnimationText.Dialog.FontSize")+":","",5);
		sizeField=(JTextField)data[1];
		sizeField.setEditable(!readOnly);
		content.add((JPanel)data[0]);
		sizeField.addActionListener((e)->NumberTools.getNotNegativeInteger(sizeField,true));

		/* Fett / Kursiv */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionBold=new JCheckBox("<html><b>"+Language.tr("Surface.AnimationText.Dialog.FontSize.Bold")+"</b></html>",false));
		optionBold.setEnabled(!readOnly);
		line.add(optionItalic=new JCheckBox("<html><i>"+Language.tr("Surface.AnimationText.Dialog.FontSize.Italic")+"</i></html>",false));
		optionItalic.setEnabled(!readOnly);

		/* Farbe */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("Surface.AnimationText.Dialog.FontColor")+":"));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooser=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooser.setEnabled(!readOnly);

		/* Werte initialisieren */
		if (element instanceof ModelElementAnimationTextValue) {
			switch (((ModelElementAnimationTextValue)element).getMode()) {
			case MODE_EXPRESSION_NUMBER:
				optionExpression.setSelected(true);
				editExpression.setText(((ModelElementAnimationTextValue)element).getExpression());
				optionPercent.setSelected(false);
				optionTime.setSelected(false);
				digits.setValue(((ModelElementAnimationTextValue)element).getDigits());
				break;
			case MODE_EXPRESSION_PERCENT:
				optionExpression.setSelected(true);
				editExpression.setText(((ModelElementAnimationTextValue)element).getExpression());
				optionPercent.setSelected(true);
				optionTime.setSelected(false);
				digits.setValue(((ModelElementAnimationTextValue)element).getDigits());
				break;
			case MODE_TIME:
				optionExpression.setSelected(false);
				editExpression.setText("123");
				optionPercent.setSelected(false);
				optionTime.setSelected(true);
				break;
			}

			sizeField.setText(""+((ModelElementAnimationTextValue)element).getTextSize());
			optionBold.setSelected(((ModelElementAnimationTextValue)element).getTextBold());
			optionItalic.setSelected(((ModelElementAnimationTextValue)element).getTextItalic());
			colorChooser.setColor(((ModelElementAnimationTextValue)element).getColor());
		}

		checkData(false);

		return content;
	}

	private boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;

		boolean ok=true;

		boolean expressionOk=true;
		int error=0;
		final String text=editExpression.getText().trim();
		if (text.isEmpty()) {
			expressionOk=false;
			editExpression.setBackground(Color.red);
		} else {
			error=ExpressionCalc.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
			if (error>=0) {
				expressionOk=false;
				editExpression.setBackground(Color.red);
			} else {
				editExpression.setBackground(SystemColor.text);
			}
		}

		if (optionExpression.isSelected()) {
			if (!expressionOk) {
				ok=false;
				if (showErrorMessages) {
					if (text.isEmpty()) {
						MsgBox.error(this,Language.tr("Surface.AnimationText.Dialog.Expression.Error.Title"),Language.tr("Surface.AnimationText.Dialog.Expression.Error.InfoMissing"));
					} else {
						MsgBox.error(this,Language.tr("Surface.AnimationText.Dialog.Expression.Error.Title"),String.format(Language.tr("Surface.AnimationText.Dialog.Expression.Error.InfoInvalid"),text,error+1));
					}
					return false;
				}
			}
		}

		Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
		if (I==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationText.Dialog.FontSize.Error.Title"),Language.tr("Surface.AnimationText.Dialog.FontSize.Error.Info"));
				return false;
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

		if (element instanceof ModelElementAnimationTextValue) {
			if (optionExpression.isSelected()) {
				((ModelElementAnimationTextValue)element).setDigits((Integer)digits.getValue());
				if (optionPercent.isSelected()) {
					((ModelElementAnimationTextValue)element).setMode(ModelElementAnimationTextValue.ModeExpression.MODE_EXPRESSION_PERCENT);
					((ModelElementAnimationTextValue)element).setExpression(editExpression.getText().trim());
				} else {
					((ModelElementAnimationTextValue)element).setMode(ModelElementAnimationTextValue.ModeExpression.MODE_EXPRESSION_NUMBER);
					((ModelElementAnimationTextValue)element).setExpression(editExpression.getText().trim());
				}
			} else {
				((ModelElementAnimationTextValue)element).setMode(ModelElementAnimationTextValue.ModeExpression.MODE_TIME);
			}

			((ModelElementAnimationTextValue)element).setFontFamily((FontCache.FontFamily)fontFamilyComboBox.getSelectedItem());
			Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
			if (I!=null) ((ModelElementAnimationTextValue)element).setTextSize(I);
			((ModelElementAnimationTextValue)element).setTextBold(optionBold.isSelected());
			((ModelElementAnimationTextValue)element).setTextItalic(optionItalic.isSelected());
			((ModelElementAnimationTextValue)element).setColor(colorChooser.getColor());
		}
	}
}
