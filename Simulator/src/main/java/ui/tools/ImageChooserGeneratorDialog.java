/**
 * Copyright 2022 Alexander Herzog
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
package ui.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import systemtools.BaseDialog;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.elements.FontCache;
import ui.modeleditor.elements.ModelElementText;
import ui.modeleditor.elements.ModelElementTextRendererPlain;

/**
 * Zeigt einen Dialog zur Erzeugung von Schriftzug-Bildern an.
 * @author Alexander Herzog
 * @see ImageChooser
 */
public class ImageChooserGeneratorDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-1001011003815889946L;

	/** Hintergrundfarbe für das Vorschaufeld */
	private static final Color DRAW_PANEL_BACKGROUND_COLOR=new Color(245,245,245);

	/** Renderer-System zur Ausgabe des Textes */
	private final ModelElementTextRendererPlain renderer;

	/** Zeichenfläche */
	private final JPanel drawPanel;

	/** Eingabefeld für den anzuzeigenden Text */
	private final JTextField textInput;
	/** Auswahl der Schriftart */
	private final JComboBox<FontCache.FontFamily> fontFamilyComboBox;
	/** Option: Text fett darstellen */
	private final JCheckBox optionBold;
	/** Option: Text kursiv darstellen */
	private final JCheckBox optionItalic;
	/** Option: HTML- und LaTeX-Symbole interpretieren */
	private final JCheckBox optionInterpretSymbols;
	/** Breite des Ausgabebildes */
	private final SpinnerModel outputSizeSpinnerModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	@SuppressWarnings("unchecked")
	public ImageChooserGeneratorDialog(final Component owner) {
		super(owner,Language.tr("Dialog.Button.TextGenerator.Title"));

		renderer=new ModelElementTextRendererPlain();
		renderer.setBackgroundColor(null,0);

		final JPanel content=createGUI(800,600,null);
		content.setLayout(new BorderLayout());

		/* Zeichenfläche */
		content.add(drawPanel=new JPanel() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=-3844378604317809242L;

			@Override
			public void paint(final Graphics g) {
				g.drawRect(0,0,drawPanel.getWidth()-1,drawPanel.getHeight()-1);
				g.setColor(DRAW_PANEL_BACKGROUND_COLOR);
				g.fillRect(1,1,drawPanel.getWidth()-2,drawPanel.getHeight()-2);
				g.setColor(Color.BLACK);

				final boolean isBold=optionBold.isSelected();
				final boolean isItalic=optionItalic.isSelected();
				final String fontName=((FontCache.FontFamily)fontFamilyComboBox.getSelectedItem()).name;

				renderer.setText(textInput.getText().trim(),optionInterpretSymbols.isSelected());
				renderer.setStyle(10,isBold,isItalic,fontName,ModelElementText.TextAlign.LEFT);
				renderer.calc(g,1.0);
				final int fontSize=(int)Math.round(Math.min((drawPanel.getWidth()-3)/((double)renderer.getWidth()),(drawPanel.getHeight()-3)/((double)renderer.getHeight()))*10);
				renderer.setStyle(fontSize,isBold,isItalic,fontName,ModelElementText.TextAlign.LEFT);
				renderer.calc(g,1.0);
				renderer.draw(g,2,2,Color.BLACK);
			}

		},BorderLayout.CENTER);

		/* Einstellungen */
		final JPanel setupArea=new JPanel();
		setupArea.setLayout(new BoxLayout(setupArea,BoxLayout.PAGE_AXIS));
		content.add(setupArea,BorderLayout.SOUTH);

		Object[] data;
		JPanel line;
		JLabel label;

		/* Anzuzeigender Text */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Text.Dialog.Text")+":","abc");
		setupArea.add((JPanel)data[0]);
		textInput=(JTextField)data[1];
		textInput.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {drawPanel.repaint();}
			@Override public void keyReleased(KeyEvent e) {drawPanel.repaint();}
			@Override public void keyPressed(KeyEvent e) {drawPanel.repaint();}
		});

		/* Schriftart */
		data=ModelElementBaseDialog.getFontFamilyComboBoxPanel(Language.tr("Surface.Text.Dialog.FontFamily")+":",null);
		fontFamilyComboBox=(JComboBox<FontCache.FontFamily>)data[1];
		fontFamilyComboBox.addActionListener(e->drawPanel.repaint());
		setupArea.add((JPanel)data[0]);

		/* Fett/Kursiv */
		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionBold=new JCheckBox("<html><b>"+Language.tr("Surface.Text.Dialog.Bold")+"</b></html>",false));
		optionBold.addActionListener(e->drawPanel.repaint());
		line.add(optionItalic=new JCheckBox("<html><i>"+Language.tr("Surface.Text.Dialog.Italic")+"</i></html>",false));
		optionItalic.addActionListener(e->drawPanel.repaint());

		/* Interpretation von Symbolen */
		line.add(optionInterpretSymbols=new JCheckBox(Language.tr("Surface.Text.Dialog.FontSize.HTMLLaTeX"),true));
		optionInterpretSymbols.setToolTipText(Language.tr("Surface.Text.Dialog.FontSize.HTMLLaTeX.Info"));
		optionInterpretSymbols.addActionListener(e->drawPanel.repaint());

		/* Ausgabebildbreite */
		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Dialog.Button.TextGenerator.ImageSize")+":"));
		final JSpinner outputSizeSpinner=new JSpinner(outputSizeSpinnerModel=new SpinnerNumberModel(400,1,1000,1));
		final JSpinner.NumberEditor outputSizeEditor=new JSpinner.NumberEditor(outputSizeSpinner);
		outputSizeEditor.getFormat().setGroupingUsed(false);
		outputSizeEditor.getTextField().setColumns(4);
		outputSizeSpinner.setEditor(outputSizeEditor);
		line.add(outputSizeSpinner);
		label.setLabelFor(outputSizeSpinner);

		/* Dialog starten */
		setMinSizeRespectingScreensize(800,600);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Liefert das erzeugte Bild zurück.
	 * @return	Bild
	 */
	public BufferedImage getImage() {
		final int imageSize=(Integer)outputSizeSpinnerModel.getValue();
		Graphics g;

		final BufferedImage calculationImage=new BufferedImage(imageSize,imageSize,BufferedImage.TYPE_4BYTE_ABGR);
		g=calculationImage.getGraphics();

		final boolean isBold=optionBold.isSelected();
		final boolean isItalic=optionItalic.isSelected();
		final String fontName=((FontCache.FontFamily)fontFamilyComboBox.getSelectedItem()).name;

		renderer.setText(textInput.getText().trim(),optionInterpretSymbols.isSelected());
		renderer.setStyle(10,isBold,isItalic,fontName,ModelElementText.TextAlign.LEFT);
		renderer.calc(g,1.0);
		final int fontSize=(int)Math.round(Math.min(imageSize/((double)renderer.getWidth()),imageSize/((double)renderer.getHeight()))*10);
		renderer.setStyle(fontSize,isBold,isItalic,fontName,ModelElementText.TextAlign.LEFT);

		final BufferedImage resultImage=new BufferedImage(imageSize,imageSize,BufferedImage.TYPE_4BYTE_ABGR);
		g=resultImage.getGraphics();

		renderer.calc(g,1.0);
		renderer.draw(g,2,2,Color.BLACK);

		return resultImage;
	}
}
