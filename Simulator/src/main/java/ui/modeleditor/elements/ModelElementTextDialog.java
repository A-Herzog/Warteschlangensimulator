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
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import language.Language;
import mathtools.NumberTools;
import systemtools.LabeledAlphaButton;
import systemtools.LabeledColorChooserButton;
import systemtools.MsgBox;
import systemtools.OptionalColorChooserButton;
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
	/** Vorschaubereich */
	private ModelElementTextPreviewPanel preview;
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
	/** Drehwinkel */
	private JSpinner rotation;
	/** Auswahl der Textfarbe */
	private LabeledColorChooserButton colorChooser;
	/** Auswahl der Hintergrundfarbe */
	private OptionalColorChooserButton colorChooserBackground;
	/** Schieberegler zur Auswahl des Deckkraft der Hintergrundfarbe */
	private LabeledAlphaButton alpha;
	/** Auswahl der Schattenfarbe */
	private OptionalColorChooserButton colorChooserShadow;
	/** Schieberegler zur Auswahl des Deckkraft des Schattens */
	private LabeledAlphaButton shadowAlpha;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementText}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementTextDialog(final Component owner, final ModelElementText element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.Text.Dialog.Title"),element,"ModelElementText",readOnly,false);
		final Dimension size=getSize();
		size.height+=50;
		size.width+=250;
		setMinSizeRespectingScreensize(size.width,size.height);
		setSizeRespectingScreensize(size.width,size.height);
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

			/* Bereich in der Mitte */
			panel.add(subPanel=new JPanel(new GridLayout(2,1)),BorderLayout.CENTER);

			/* Eingabebereich */
			subPanel.add(subPanel2=new JPanel(new BorderLayout()));
			subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEADING)),BorderLayout.NORTH);
			line.add(label=new JLabel(Language.tr("Surface.Text.Dialog.Text")+":"));
			subPanel2.add(new ScriptEditorAreaBuilder.RScrollPane(textField=ScriptEditorAreaBuilder.getPlainTextField(5,50,text.getText(),readOnly,ScriptEditorAreaBuilder.TextAreaMode.TEXT_ELEMENT)),BorderLayout.CENTER);
			label.setLabelFor(textField);
			addUndoFeature(textField);
			if (text.isInterpretSymbols()) ScriptEditorAreaBuilder.setEntityAutoComplete(textField,true);
			textField.addKeyListener(new KeyAdapter() {
				@Override public void keyReleased(KeyEvent e) {updatePreview();}
			});

			/* Vorschaubereich */
			subPanel.add(subPanel2=new JPanel(new BorderLayout()));
			subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEADING)),BorderLayout.NORTH);
			line.add(label=new JLabel(Language.tr("Surface.Text.Dialog.Preview")+":"));
			subPanel2.add(new JScrollPane(preview=new ModelElementTextPreviewPanel()));
			label.setLabelFor(preview);

			/* Bereich unten */
			JPanel bottomPanel=new JPanel();
			panel.add(bottomPanel,BorderLayout.SOUTH);
			bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.PAGE_AXIS));

			/* Schriftart */
			data=getFontFamilyComboBoxPanel(Language.tr("Surface.Text.Dialog.FontFamily")+":",text.getFontFamily());
			fontFamilyComboBox=(JComboBox<FontCache.FontFamily>)data[1];
			fontFamilyComboBox.setEnabled(!readOnly);
			fontFamilyComboBox.addActionListener(e->updatePreview());
			bottomPanel.add((JPanel)data[0]);

			/* Schriftgröße */
			data=getInputPanel(Language.tr("Surface.Text.Dialog.FontSize")+":",""+text.getTextSize(),5);
			sizeField=(JTextField)data[1];
			sizeField.setEnabled(!readOnly);
			bottomPanel.add((JPanel)data[0]);
			sizeField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (readOnly) return;
					NumberTools.getNotNegativeInteger(sizeField,true);
					updatePreview();
				}
			});

			/* Fett/Kursiv */
			bottomPanel.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			subPanel.add(optionBold=new JCheckBox("<html><b>"+Language.tr("Surface.Text.Dialog.Bold")+"</b></html>",text.getTextBold()));
			optionBold.setEnabled(!readOnly);
			optionBold.addActionListener(e->updatePreview());
			subPanel.add(optionItalic=new JCheckBox("<html><i>"+Language.tr("Surface.Text.Dialog.Italic")+"</i></html>",text.getTextItalic()));
			optionItalic.setEnabled(!readOnly);
			optionItalic.addActionListener(e->updatePreview());

			/* Interpretation von Symbolen */
			subPanel.add(optionInterpretSymbols=new JCheckBox(Language.tr("Surface.Text.Dialog.FontSize.HTMLLaTeX"),text.isInterpretSymbols()));
			optionInterpretSymbols.setToolTipText(Language.tr("Surface.Text.Dialog.FontSize.HTMLLaTeX.Info"));
			optionInterpretSymbols.setEnabled(!readOnly);
			optionInterpretSymbols.addActionListener(e->{
				ScriptEditorAreaBuilder.setEntityAutoComplete(textField,optionInterpretSymbols.isSelected());
				updatePreview();
			});

			/* Interpretation von Markdown */
			subPanel.add(optionInterpretMarkdown=new JCheckBox(Language.tr("Surface.Text.Dialog.FontSize.Markdown"),text.isInterpretMarkdown()));
			optionInterpretMarkdown.setToolTipText(Language.tr("Surface.Text.Dialog.FontSize.Markdown.Info"));
			optionInterpretMarkdown.setEnabled(!readOnly);
			optionInterpretMarkdown.addActionListener(e->updatePreview());

			/* Interpretation von LaTeX-Formatierungen */
			subPanel.add(optionInterpretLaTeX=new JCheckBox(Language.tr("Surface.Text.Dialog.FontSize.LaTeX"),text.isInterpretLaTeX()));
			optionInterpretLaTeX.setToolTipText(Language.tr("Surface.Text.Dialog.FontSize.LaTeX.Info"));
			optionInterpretLaTeX.setEnabled(!readOnly);
			optionInterpretLaTeX.addActionListener(e->updatePreview());

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
			textAlign.addActionListener(e->updatePreview());

			subPanel.add(Box.createHorizontalStrut(10));

			/* Drehung */
			subPanel.add(label=new JLabel(Language.tr("Surface.Text.Dialog.RotationAngle")+":"));
			final SpinnerModel spinnerModel=new SpinnerNumberModel(Math.round(text.getRotation()),0,360,1);
			subPanel.add(rotation=new JSpinner(spinnerModel));
			label.setLabelFor(rotation);
			rotation.setEnabled(!readOnly);
			subPanel.add(new JLabel("°"));

			/* Zeile für Farben 1 */
			bottomPanel.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));

			/* Schriftfarbe */
			subPanel.add(colorChooser=new LabeledColorChooserButton(Language.tr("Surface.Text.Dialog.Color")+":",text.getColor()));
			colorChooser.addClickListener(e->updatePreview());
			colorChooser.setEnabled(!readOnly);

			/* Zeile für Farben 2 */
			bottomPanel.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));

			/* Hintergrundfarbe */
			subPanel.add(colorChooserBackground=new OptionalColorChooserButton(Language.tr("Surface.Text.Dialog.FillBackground")+":",text.getFillColor(),Color.BLUE));
			colorChooserBackground.addClickListener(e->updatePreview());
			colorChooserBackground.setEnabled(!readOnly);

			subPanel.add(Box.createHorizontalStrut(10));

			/* Deckkraft */
			subPanel.add(alpha=new LabeledAlphaButton(Language.tr("Surface.Text.Dialog.Alpha")+":",text.getFillAlpha()));
			alpha.addClickListener(e->{colorChooserBackground.setActive(true); updatePreview();});
			alpha.setEnabled(!readOnly);

			/* Zeile für Farben 3 */
			bottomPanel.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));

			/* Schattenfarbe */
			subPanel.add(colorChooserShadow=new OptionalColorChooserButton(Language.tr("Surface.Text.Dialog.ShadowColor")+":",text.getShadowColor(),Color.GRAY));
			colorChooserShadow.addClickListener(e->updatePreview());
			colorChooserShadow.setEnabled(!readOnly);

			subPanel.add(Box.createHorizontalStrut(10));

			/* Stärke des Schatten */
			subPanel.add(shadowAlpha=new LabeledAlphaButton(Language.tr("Surface.Text.Dialog.ShadowAlpha")+":",text.getShadowAlpha()));
			shadowAlpha.addClickListener(e->{colorChooserShadow.setActive(true); updatePreview();});
			shadowAlpha.setEnabled(!readOnly);
		}

		updatePreview();

		return panel;
	}

	/**
	 * Aktualisiert den Vorschaubereich.
	 * @see #preview
	 */
	private void updatePreview() {
		final Integer size=NumberTools.getNotNegativeInteger(sizeField,true);
		final ModelElementText.TextAlign align;
		switch (textAlign.getSelectedIndex()) {
		case 0: align=TextAlign.LEFT; break;
		case 1: align=TextAlign.CENTER; break;
		case 2: align=TextAlign.RIGHT; break;
		default: align=TextAlign.LEFT; break;
		}

		preview.set(
				optionInterpretMarkdown.isSelected(),
				optionInterpretLaTeX.isSelected(),
				optionInterpretSymbols.isSelected(),
				textField.getText(),
				colorChooser.getColor(),
				colorChooserBackground.getColor(),
				alpha.getAlpha(),
				colorChooserShadow.getColor(),
				shadowAlpha.getAlpha(),
				((size==null)?14:size),
				optionBold.isSelected(),
				optionItalic.isSelected(),
				(FontCache.FontFamily)fontFamilyComboBox.getSelectedItem(),
				align);
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

		/* Drehung */
		text.setRotation((Double)rotation.getValue());

		/* Schriftfarbe */
		text.setColor(colorChooser.getColor());

		/* Hintergrundfarbe */
		text.setFillColor(colorChooserBackground.getColor());

		/* Deckkraft */
		text.setFillAlpha(alpha.getAlpha());

		/* Schattenfarbe */
		text.setShadowColor(colorChooserShadow.getColor());

		/* Deckkraft des Schattens */
		text.setShadowAlpha(shadowAlpha.getAlpha());
	}
}
