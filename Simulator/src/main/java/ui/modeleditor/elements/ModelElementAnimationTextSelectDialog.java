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
import java.io.Serializable;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import tools.JTableExt;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationTextSelect}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationTextSelect
 */
public class ModelElementAnimationTextSelectDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5107663089458560385L;

	/** Tabelle zur Definition der möglichen Bedingungen und Ausgaben */
	private ExpressionTableModelText expressionTableModel;
	/** Eingabefeld für den Standardtext, der angezeigt werden soll, wenn keine der Bedingungen zutrifft */
	private JTextField defaultTextEdit;
	/** Auswahlfeld für die Schriftart */
	private JComboBox<FontCache.FontFamily> fontFamilyComboBox;
	/** Eingabefeld für die Schriftgröße */
	private JTextField sizeField;
	/** Option: Text in Fettdruck darstellen */
	private JCheckBox optionBold;
	/** Option: Text kursiv darstellen */
	private JCheckBox optionItalic;
	/** Option: HTML- und LaTeX-Symbole interpretieren */
	private JCheckBox optionInterpretSymbols;
	/** Auswahl der Farbe für den Text */
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
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationTextSelect}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementAnimationTextSelectDialog(final Component owner, final ModelElementAnimationTextSelect element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.AnimationTextSelect.Dialog.Title"),element,"ModelElementAnimationTextSelect",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
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
		return InfoPanel.stationAnimationTextSelect;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JTabbedPane tabs=new JTabbedPane();

		JPanel tabOuter, tab, subPanel, subPanel2, line;
		Object[] data;
		JLabel label;

		/* Tab "Ausdrücke" */
		final JTableExt expressionTable;
		tabs.add(Language.tr("Surface.AnimationTextSelect.Dialog.Tabs.Expression"),tab=new JPanel(new BorderLayout()));
		tab.add(new JScrollPane(expressionTable=new JTableExt()),BorderLayout.CENTER);
		if (element instanceof ModelElementAnimationTextSelect) {
			expressionTable.setModel(expressionTableModel=new ExpressionTableModelText(expressionTable,(ModelElementAnimationTextSelect)element,readOnly,helpRunnable));
		}
		expressionTable.getColumnModel().getColumn(0).setMaxWidth(200);
		expressionTable.getColumnModel().getColumn(0).setMinWidth(200);
		expressionTable.setIsPanelCellTable(0);
		expressionTable.setIsPanelCellTable(1);
		expressionTable.setEnabled(!readOnly);

		/* Standardtext */
		data=getInputPanel(Language.tr("Surface.AnimationTextSelect.Dialog.DefaultValue")+":","");
		defaultTextEdit=(JTextField)data[1];
		defaultTextEdit.setEditable(!readOnly);
		tab.add((JPanel)data[0],BorderLayout.SOUTH);

		/* Tab "Darstellung" */
		tabs.add(Language.tr("Surface.AnimationTextSelect.Dialog.Tabs.Appearance"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		/* Schriftart */
		data=getFontFamilyComboBoxPanel(Language.tr("Surface.AnimationTextSelect.Dialog.FontFamily")+":",((ModelElementAnimationTextSelect)element).getFontFamily());
		fontFamilyComboBox=(JComboBox<FontCache.FontFamily>)data[1];
		fontFamilyComboBox.setEnabled(!readOnly);
		tab.add((JPanel)data[0]);

		/* Schriftgröße */
		data=getInputPanel(Language.tr("Surface.AnimationText.Dialog.FontSize")+":","",5);
		sizeField=(JTextField)data[1];
		sizeField.setEditable(!readOnly);
		tab.add((JPanel)data[0]);
		sizeField.addActionListener(e->NumberTools.getNotNegativeInteger(sizeField,true));

		/* Fett / Kursiv */
		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionBold=new JCheckBox("<html><b>"+Language.tr("Surface.AnimationText.Dialog.FontSize.Bold")+"</b></html>",false));
		optionBold.setEnabled(!readOnly);
		line.add(optionItalic=new JCheckBox("<html><i>"+Language.tr("Surface.AnimationText.Dialog.FontSize.Italic")+"</i></html>",false));
		optionItalic.setEnabled(!readOnly);

		/* Interpretation von Symbolen */
		line.add(optionInterpretSymbols=new JCheckBox(Language.tr("Surface.AnimationText.Dialog.FontSize.HTMLLaTeX"),false));
		optionInterpretSymbols.setToolTipText(Language.tr("Surface.AnimationText.Dialog.FontSize.HTMLLaTeX.Info"));
		optionInterpretSymbols.setEnabled(!readOnly);

		/* Zeile für Farben */
		tab.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		/* Schriftfarbe */
		subPanel.add(subPanel2=new JPanel());
		subPanel2.setLayout(new BoxLayout(subPanel2,BoxLayout.PAGE_AXIS));

		subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnimationText.Dialog.Color")+":"));

		subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooser=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooser.setEnabled(!readOnly);
		label.setLabelFor(colorChooser);

		/* Hintergrundfarbe */
		subPanel.add(subPanel2=new JPanel());
		subPanel2.setLayout(new BoxLayout(subPanel2,BoxLayout.PAGE_AXIS));

		subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(background=new JCheckBox(Language.tr("Surface.AnimationText.Dialog.FillBackground")),BorderLayout.NORTH);
		background.setEnabled(!readOnly);

		subPanel2.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooserBackground=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooserBackground.setEnabled(!readOnly);
		colorChooserBackground.addClickListener(e->background.setSelected(true));

		/* Deckkraft */
		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		JLabel alphaLabel=new JLabel(Language.tr("Surface.AnimationText.Dialog.Alpha")+":");
		line.add(alphaLabel);
		line.add(alpha=new JSlider(0,100,100));
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

		/* Werte initialisieren */
		if (element instanceof ModelElementAnimationTextSelect) {
			final ModelElementAnimationTextSelect text=(ModelElementAnimationTextSelect)element;
			defaultTextEdit.setText(text.getDefaultText());
			sizeField.setText(""+text.getTextSize());
			optionBold.setSelected(text.getTextBold());
			optionItalic.setSelected(text.getTextItalic());
			optionInterpretSymbols.setSelected(text.isInterpretSymbols());
			colorChooser.setColor(text.getColor());
			background.setSelected(text.getFillColor()!=null);
			colorChooserBackground.setColor(text.getFillColor());
			alpha.setValue((int)Math.round(100*text.getFillAlpha()));
		}

		/* Icons für Tabs */
		tabs.setIconAt(0,Images.MODE_EXPRESSION.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROPERTIES_APPEARANCE.getIcon());

		return tabs;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;

		boolean ok=true;

		Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
		if (I==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationTextSelect.Dialog.FontSize.Error.Title"),Language.tr("Surface.AnimationTextSelect.Dialog.FontSize.Error.Info"));
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

		if (!(element instanceof ModelElementAnimationTextSelect)) return;
		final ModelElementAnimationTextSelect text=(ModelElementAnimationTextSelect)element;
		/* Bedingte Ausdrücke */
		expressionTableModel.storeData((ModelElementAnimationTextSelect)element);

		/* Standardtext */
		text.setDefaultText(defaultTextEdit.getText());

		/* Schriftart */
		text.setFontFamily((FontCache.FontFamily)fontFamilyComboBox.getSelectedItem());

		/* Schriftgröße */
		final Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
		if (I!=null) text.setTextSize(I);

		/* Fett/Kursiv */
		text.setTextBold(optionBold.isSelected());
		text.setTextItalic(optionItalic.isSelected());

		/* Interpretation von Symbolen */
		text.setInterpretSymbols(optionInterpretSymbols.isSelected());

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