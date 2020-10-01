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
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementText}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementText
 */
public class ModelElementTextDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = -2715231750705476641L;

	private JTextArea textField;
	private JComboBox<FontCache.FontFamily> fontFamilyComboBox;
	private JTextField sizeField;
	private JCheckBox optionBold;
	private JCheckBox optionItalic;
	private SmallColorChooser colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementText}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTextDialog(final Component owner, final ModelElementText element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Text.Dialog.Title"),element,"ModelElementText",readOnly,false);
		setMinimumSize(getSize());
		setResizable(true);
		setVisible(true);
	}

	@Override
	protected boolean hasNameField() {
		return false;
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationText;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JPanel panel=new JPanel(new BorderLayout());

		JLabel label;
		JPanel subPanel;
		Object[] data;

		if (element instanceof ModelElementText) {
			final ModelElementText text=(ModelElementText)element;

			panel.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
			subPanel.add(label=new JLabel(Language.tr("Surface.Text.Dialog.Text")+":"));

			panel.add(new JScrollPane(textField=new JTextArea(5,50)),BorderLayout.CENTER);
			textField.setEditable(!readOnly);
			textField.setText(text.getText());
			label.setLabelFor(textField);
			addUndoFeature(textField);

			JPanel bottomPanel=new JPanel();
			panel.add(bottomPanel,BorderLayout.SOUTH);
			bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.PAGE_AXIS));

			data=getFontFamilyComboBoxPanel(Language.tr("Surface.Text.Dialog.FontFamily")+":",text.getFontFamily());
			fontFamilyComboBox=(JComboBox<FontCache.FontFamily>)data[1];
			fontFamilyComboBox.setEnabled(!readOnly);
			bottomPanel.add((JPanel)data[0]);

			data=getInputPanel(Language.tr("Surface.Text.Dialog.FontSize")+":",""+text.getTextSize(),5);
			sizeField=(JTextField)data[1];
			sizeField.setEditable(!readOnly);
			bottomPanel.add((JPanel)data[0]);
			sizeField.addActionListener(e->{
				if (readOnly) return;
				NumberTools.getNotNegativeInteger(sizeField,true);
			});

			bottomPanel.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			subPanel.add(optionBold=new JCheckBox("<html><b>"+Language.tr("Surface.Text.Dialog.Bold")+"</b></html>",text.getTextBold()));
			optionBold.setEnabled(!readOnly);
			subPanel.add(optionItalic=new JCheckBox("<html><i>"+Language.tr("Surface.Text.Dialog.Italic")+"</i></html>",text.getTextItalic()));
			optionItalic.setEnabled(!readOnly);

			bottomPanel.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			subPanel.add(label=new JLabel(Language.tr("Surface.Text.Dialog.Color")+":"));

			bottomPanel.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			subPanel.add(colorChooser=new SmallColorChooser(text.getColor()),BorderLayout.CENTER);
			colorChooser.setEnabled(!readOnly);
			label.setLabelFor(colorChooser);
		}

		return panel;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		if (readOnly) return false;
		Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
		if (I==null) {
			MsgBox.error(this,Language.tr("Surface.Text.Dialog.FontSize.Error.Title"),Language.tr("Surface.Text.Dialog.FontSize.Error.Info"));
			return false;
		}

		return true;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementText) {
			final ModelElementText text=(ModelElementText)element;
			text.setText(textField.getText().trim());
			text.setFontFamily((FontCache.FontFamily)fontFamilyComboBox.getSelectedItem());
			Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
			if (I!=null) text.setTextSize(I);
			text.setTextBold(optionBold.isSelected());
			text.setTextItalic(optionItalic.isSelected());
			text.setColor(colorChooser.getColor());
		}
	}
}
