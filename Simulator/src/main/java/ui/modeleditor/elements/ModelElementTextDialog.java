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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.elements.ModelElementText.TextAlign;
import ui.script.ScriptEditorAreaBuilder;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementText}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementText
 */
public class ModelElementTextDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2715231750705476641L;

	/** Eingabefeld für den anzuzeigenden Text */
	private RSyntaxTextArea textField;
	/** Auswahl der Schriftart */
	private JComboBox<FontCache.FontFamily> fontFamilyComboBox;
	/** Eingabefeld für die Schriftgröße */
	private JTextField sizeField;
	/** Option: Text fett darstellen */
	private JCheckBox optionBold;
	/** Option: Text kursiv darstellen */
	private JCheckBox optionItalic;
	/** Option: HTML- und LaTeX-Symbole interpretieren */
	private JCheckBox optionInterpretSymbols;
	/** Option: Markdown interpretieren */
	private JCheckBox optionInterpretMarkdown;
	/** Option: LaTeX-Formatierungen interpretieren */
	private JCheckBox optionInterpretLaTeX;
	/** Ausrichtung */
	private JComboBox<String> textAlign;
	/** Auswahl der Textfarbe */
	private SmallColorChooser colorChooser;
	/** Option: Hintergrundfarbe verwenden? */
	private JCheckBox background;
	/** Auswahl der Hintergrundfarbe */
	private SmallColorChooser colorChooserBackground;
	/** Schieberegler zur Auswahl des Deckkraft der Hintergrundfarbe */
	private JSlider alpha;

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
		JPanel subPanel, subPanel2, line;
		Object[] data;

		if (element instanceof ModelElementText) {
			final ModelElementText text=(ModelElementText)element;

			/* Text */
			panel.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
			subPanel.add(label=new JLabel(Language.tr("Surface.Text.Dialog.Text")+":"));

			panel.add(new ScriptEditorAreaBuilder.RScrollPane(textField=ScriptEditorAreaBuilder.getPlainTextField(5,50,text.getText(),readOnly,ScriptEditorAreaBuilder.TextAreaMode.TEXT_ELEMENT)),BorderLayout.CENTER);
			label.setLabelFor(textField);
			addUndoFeature(textField);
			if (text.isInterpretSymbols()) ScriptEditorAreaBuilder.setEntityAutoComplete(textField,true);

			/* Bereich unten */
			JPanel bottomPanel=new JPanel();
			panel.add(bottomPanel,BorderLayout.SOUTH);
			bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.PAGE_AXIS));

			/* Schriftart */
			data=getFontFamilyComboBoxPanel(Language.tr("Surface.Text.Dialog.FontFamily")+":",text.getFontFamily());
			fontFamilyComboBox=(JComboBox<FontCache.FontFamily>)data[1];
			fontFamilyComboBox.setEnabled(!readOnly);
			bottomPanel.add((JPanel)data[0]);

			/* Schriftgröße */
			data=getInputPanel(Language.tr("Surface.Text.Dialog.FontSize")+":",""+text.getTextSize(),5);
			sizeField=(JTextField)data[1];
			sizeField.setEditable(!readOnly);
			bottomPanel.add((JPanel)data[0]);
			sizeField.addActionListener(e->{
				if (readOnly) return;
				NumberTools.getNotNegativeInteger(sizeField,true);
			});

			/* Fett/Kursiv */
			bottomPanel.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			subPanel.add(optionBold=new JCheckBox("<html><b>"+Language.tr("Surface.Text.Dialog.Bold")+"</b></html>",text.getTextBold()));
			optionBold.setEnabled(!readOnly);
			subPanel.add(optionItalic=new JCheckBox("<html><i>"+Language.tr("Surface.Text.Dialog.Italic")+"</i></html>",text.getTextItalic()));
			optionItalic.setEnabled(!readOnly);

			/* Interpretation von Symbolen */
			subPanel.add(optionInterpretSymbols=new JCheckBox(Language.tr("Surface.Text.Dialog.FontSize.HTMLLaTeX"),text.isInterpretSymbols()));
			optionInterpretSymbols.setToolTipText(Language.tr("Surface.Text.Dialog.FontSize.HTMLLaTeX.Info"));
			optionInterpretSymbols.setEnabled(!readOnly);
			optionInterpretSymbols.addActionListener(e->{
				ScriptEditorAreaBuilder.setEntityAutoComplete(textField,optionInterpretSymbols.isSelected());
			});

			/* Interpretation von Markdown */
			subPanel.add(optionInterpretMarkdown=new JCheckBox(Language.tr("Surface.Text.Dialog.FontSize.Markdown"),text.isInterpretMarkdown()));
			optionInterpretMarkdown.setToolTipText(Language.tr("Surface.Text.Dialog.FontSize.Markdown.Info"));
			optionInterpretMarkdown.setEnabled(!readOnly);

			/* Interpretation von LaTeX-Formatierungen */
			subPanel.add(optionInterpretLaTeX=new JCheckBox(Language.tr("Surface.Text.Dialog.FontSize.LaTeX"),text.isInterpretLaTeX()));
			optionInterpretLaTeX.setToolTipText(Language.tr("Surface.Text.Dialog.FontSize.LaTeX.Info"));
			optionInterpretLaTeX.setEnabled(!readOnly);

			/* Ausrichtung */
			bottomPanel.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			subPanel.add(label=new JLabel(Language.tr("Surface.Text.Dialog.Align")+":"));
			subPanel.add(textAlign=new JComboBox<>(new String[] {
					Language.tr("Surface.Text.Dialog.Align.Left"),
					Language.tr("Surface.Text.Dialog.Align.Center"),
					Language.tr("Surface.Text.Dialog.Align.Right"),
			}));
			textAlign.setRenderer(new IconListCellRenderer(new Images[]{
					Images.TEXT_ALIGN_LEFT,
					Images.TEXT_ALIGN_CENTER,
					Images.TEXT_ALIGN_RIGHT
			}));
			label.setLabelFor(textAlign);
			switch (text.getTextAlign()) {
			case LEFT: textAlign.setSelectedIndex(0); break;
			case CENTER: textAlign.setSelectedIndex(1); break;
			case RIGHT: textAlign.setSelectedIndex(2); break;
			default: textAlign.setSelectedIndex(0); break;
			}
			textAlign.setEnabled(!readOnly);

			/* Zeile für Farben */
			bottomPanel.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));

			/* Schriftfarbe */
			subPanel.add(subPanel2=new JPanel());
			subPanel2.setLayout(new BoxLayout(subPanel2,BoxLayout.PAGE_AXIS));

			subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(label=new JLabel(Language.tr("Surface.Text.Dialog.Color")+":"));

			subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(colorChooser=new SmallColorChooser(text.getColor()),BorderLayout.CENTER);
			colorChooser.setEnabled(!readOnly);
			label.setLabelFor(colorChooser);

			/* Hintergrundfarbe */
			subPanel.add(subPanel2=new JPanel());
			subPanel2.setLayout(new BoxLayout(subPanel2,BoxLayout.PAGE_AXIS));

			subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(background=new JCheckBox(Language.tr("Surface.Text.Dialog.FillBackground")),BorderLayout.NORTH);
			background.setSelected(text.getFillColor()!=null);
			background.setEnabled(!readOnly);

			subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(colorChooserBackground=new SmallColorChooser(text.getFillColor()),BorderLayout.CENTER);
			colorChooserBackground.setEnabled(!readOnly);
			colorChooserBackground.addClickListener(e->background.setSelected(true));

			/* Deckkraft */
			bottomPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
			JLabel alphaLabel=new JLabel(Language.tr("Surface.Text.Dialog.Alpha")+":");
			line.add(alphaLabel);
			line.add(alpha=new JSlider(0,100,(int)Math.round(100*text.getFillAlpha())));
			alphaLabel.setLabelFor(alpha);
			alpha.setEnabled(!readOnly);
			alpha.setMinorTickSpacing(1);
			alpha.setMajorTickSpacing(10);
			Hashtable<Integer,JComponent> labels=new Hashtable<>();
			for (int i=0;i<=10;i++) labels.put(i*10,new JLabel(NumberTools.formatPercent(i/10.0)));
			alpha.setLabelTable(labels);
			alpha.setPaintTicks(true);
			alpha.setPaintLabels(true);
			alpha.setPreferredSize(new Dimension(400,alpha.getPreferredSize().height));
			alpha.addChangeListener(e->background.setSelected(true));
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

		if (!(element instanceof ModelElementText)) return;
		final ModelElementText text=(ModelElementText)element;

		/* Text */
		text.setText(textField.getText().trim());

		/* Schriftart */
		text.setFontFamily((FontCache.FontFamily)fontFamilyComboBox.getSelectedItem());

		/* Schriftgröße */
		Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
		if (I!=null) text.setTextSize(I);

		/* Fett/Kursiv */
		text.setTextBold(optionBold.isSelected());
		text.setTextItalic(optionItalic.isSelected());

		/* Interpretation von Symbolen */
		text.setInterpretSymbols(optionInterpretSymbols.isSelected());
		text.setInterpretMarkdown(optionInterpretMarkdown.isSelected());
		text.setInterpretLaTeX(optionInterpretLaTeX.isSelected());

		/* Ausrichtung */
		switch (textAlign.getSelectedIndex()) {
		case 0: text.setTextAlign(TextAlign.LEFT); break;
		case 1: text.setTextAlign(TextAlign.CENTER); break;
		case 2: text.setTextAlign(TextAlign.RIGHT); break;
		}

		/* Schriftfarbe */
		text.setColor(colorChooser.getColor());

		/* Hintergrundfarbe */
		if (background.isSelected()) {
			text.setFillColor(colorChooserBackground.getColor());
		} else {
			text.setFillColor(null);
		}

		/* Deckkraft */
		text.setFillAlpha(alpha.getValue()/100.0);
	}
}
