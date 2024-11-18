/**
 * Copyright 2024 Alexander Herzog
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import mathtools.NumberTools;
import systemtools.LabeledColorChooserButton;
import systemtools.MsgBox;
import systemtools.OptionalColorChooserButton;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationTable}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationTable
 */
public class ModelElementAnimationTableDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=5445543704100994387L;

	/* Zellen */

	/** Eingabefeld für die Anzahl an Tabellenzeilen */
	private SpinnerModel rowCount;
	/** Eingabefeld für die Anzahl an Tabellenspalten */
	private SpinnerModel colCount;
	/** Tabellenmodell */
	private AnimationTableModel model;

	/* Layout */

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
	/** Option: Markdown interpretieren */
	private JCheckBox optionInterpretMarkdown;
	/** Option: LaTeX-Formatierungen interpretieren */
	private JCheckBox optionInterpretLaTeX;
	/** Auswahl der Farbe für äußeren Rahmen */
	private OptionalColorChooserButton colorBordersOuter;
	/** Auswahl der Farbe für die Rahmenlinien im Inneren */
	private OptionalColorChooserButton colorBordersInner;
	/** Auswahl der Farbe für den Text */
	private LabeledColorChooserButton colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationTable}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementAnimationTableDialog(final Component owner, final ModelElementAnimationTable element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.AnimationTable.Dialog.Title"),element,"ModelElementAnimationTable",readOnly);
	}

	@Override
	protected boolean hasNameField() {
		return false;
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
		pack();
		setMaxSizeRespectingScreensize(1280,1024);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationTable;
	}

	/**
	 * Erzeugt ein Zahlen-Eingabefeld.
	 * @param panel	Panel in das die Beschriftung und das Eingabefeld eingefügt werden sollen.
	 * @param labelText	Beschriftungstext
	 * @param value	Initialer Wert
	 * @return	Datenmodell des Eingabefeldes
	 */
	private SpinnerModel buildSpinner(final JPanel panel, final String labelText, final int value) {
		final JLabel label;
		panel.add(label=new JLabel(labelText+":"));
		final SpinnerModel model;
		final JSpinner spinner=new JSpinner(model=new SpinnerNumberModel(value,1,10,1));
		final JSpinner.NumberEditor editor=new JSpinner.NumberEditor(spinner);
		editor.getFormat().setGroupingUsed(false);
		editor.getTextField().setColumns(2);
		spinner.setEditor(editor);
		panel.add(spinner);
		label.setLabelFor(spinner);
		spinner.setValue(value);
		editor.setEnabled(!readOnly);
		spinner.setEnabled(!readOnly);
		return model;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		if (!(element instanceof ModelElementAnimationTable)) return null;
		final ModelElementAnimationTable table=(ModelElementAnimationTable)element;
		final List<List<ModelElementAnimationTable.Cell>> cells=table.getCells();

		final JTabbedPane tabs=new JTabbedPane();
		JPanel tab, line;
		Object[] data;

		/* Tab: "Zellen" */

		tabs.addTab(Language.tr("Surface.AnimationTable.Dialog.TabCells"),tab=new JPanel(new BorderLayout()));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		rowCount=buildSpinner(line,Language.tr("Surface.AnimationTable.Dialog.RowCount"),cells.size());
		rowCount.addChangeListener(e->tableSizeChanged());
		line.add(Box.createHorizontalStrut(10));
		colCount=buildSpinner(line,Language.tr("Surface.AnimationTable.Dialog.ColCount"),cells.get(0).size());
		colCount.addChangeListener(e->tableSizeChanged());

		data=AnimationTableModel.buildTable(cells,readOnly,element.getModel(),()->Help.topicModal(this,"ModelElementAnimationTable"));
		tab.add((JScrollPane)data[0],BorderLayout.CENTER);
		model=(AnimationTableModel)data[1];

		if (!readOnly) {
			tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
			line.add(new JLabel(Language.tr("Surface.AnimationTable.Dialog.CellEditHint")));
		}

		/* Tab: "Aussehen" */

		tabs.addTab(Language.tr("Surface.AnimationTable.Dialog.TabLayout"),tab=new JPanel(new BorderLayout()));
		final JPanel bottomPanel=new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.PAGE_AXIS));
		tab.add(bottomPanel,BorderLayout.NORTH);

		/* Schriftart */
		data=getFontFamilyComboBoxPanel(Language.tr("Surface.AnimationTable.Dialog.FontFamily")+":",table.getFontFamily());
		fontFamilyComboBox=(JComboBox<FontCache.FontFamily>)data[1];
		fontFamilyComboBox.setEnabled(!readOnly);
		bottomPanel.add((JPanel)data[0]);

		/* Schriftgröße */
		data=getInputPanel(Language.tr("Surface.AnimationTable.Dialog.FontSize")+":",""+table.getTextSize(),5);
		sizeField=(JTextField)data[1];
		sizeField.setEnabled(!readOnly);
		bottomPanel.add((JPanel)data[0]);
		sizeField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (readOnly) return;
				NumberTools.getNotNegativeInteger(sizeField,true);
			}
		});

		/* Fett/Kursiv */
		bottomPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionBold=new JCheckBox("<html><b>"+Language.tr("Surface.AnimationTable.Dialog.Bold")+"</b></html>",table.getTextBold()));
		optionBold.setEnabled(!readOnly);
		line.add(optionItalic=new JCheckBox("<html><i>"+Language.tr("Surface.AnimationTable.Dialog.Italic")+"</i></html>",table.getTextItalic()));
		optionItalic.setEnabled(!readOnly);

		/* Interpretation von Symbolen */
		line.add(optionInterpretSymbols=new JCheckBox(Language.tr("Surface.AnimationTable.Dialog.FontSize.HTMLLaTeX"),table.isInterpretSymbols()));
		optionInterpretSymbols.setToolTipText(Language.tr("Surface.AnimationTable.Dialog.FontSize.HTMLLaTeX.Info"));
		optionInterpretSymbols.setEnabled(!readOnly);

		/* Interpretation von Markdown */
		line.add(optionInterpretMarkdown=new JCheckBox(Language.tr("Surface.AnimationTable.Dialog.FontSize.Markdown"),table.isInterpretMarkdown()));
		optionInterpretMarkdown.setToolTipText(Language.tr("Surface.AnimationTable.Dialog.FontSize.Markdown.Info"));
		optionInterpretMarkdown.setEnabled(!readOnly);

		/* Interpretation von LaTeX-Formatierungen */
		line.add(optionInterpretLaTeX=new JCheckBox(Language.tr("Surface.AnimationTable.Dialog.FontSize.LaTeX"),table.isInterpretLaTeX()));
		optionInterpretLaTeX.setToolTipText(Language.tr("Surface.AnimationTable.Dialog.FontSize.LaTeX.Info"));
		optionInterpretLaTeX.setEnabled(!readOnly);

		/* Auswahl der Farbe für äußeren Rahmen */
		bottomPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorBordersOuter=new OptionalColorChooserButton(Language.tr("Surface.AnimationTable.Dialog.ColorBordersOuter")+":",table.getBordersOuter(),Color.BLACK));
		colorBordersOuter.setEnabled(!readOnly);

		/* Auswahl der Farbe für die Rahmenlinien im Inneren */
		bottomPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorBordersInner=new OptionalColorChooserButton(Language.tr("Surface.AnimationTable.Dialog.ColorBordersInner")+":",table.getBordersInner(),Color.BLACK));
		colorBordersInner.setEnabled(!readOnly);

		/* Schriftfarbe */
		bottomPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooser=new LabeledColorChooserButton(Language.tr("Surface.AnimationTable.Dialog.Color")+":",table.getColor()));
		colorChooser.setEnabled(!readOnly);

		/* Icons */

		tabs.setIconAt(0,Images.GENERAL_TABLE.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROPERTIES_APPEARANCE.getIcon());

		return tabs;
	}

	/**
	 * Reagiert auf Veränderungen der Tabellengröße über die beiden Eingabefelder.
	 */
	private void tableSizeChanged() {
		model.setSize((Integer)rowCount.getValue(),(Integer)colCount.getValue());
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
			MsgBox.error(this,Language.tr("Surface.AnimationTable.Dialog.FontSize.Error.Title"),Language.tr("Surface.AnimationTable.Dialog.FontSize.Error.Info"));
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

		if (!(element instanceof ModelElementAnimationTable)) return;
		final ModelElementAnimationTable table=(ModelElementAnimationTable)element;

		/* Zellen */

		table.setCells(model.getCells());

		/* Layout */

		/* Schriftart */
		table.setFontFamily((FontCache.FontFamily)fontFamilyComboBox.getSelectedItem());

		/* Schriftgröße */
		Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
		if (I!=null) table.setTextSize(I);

		/* Fett/Kursiv */
		table.setTextBold(optionBold.isSelected());
		table.setTextItalic(optionItalic.isSelected());

		/* Interpretation von Symbolen */
		table.setInterpretSymbols(optionInterpretSymbols.isSelected());
		table.setInterpretMarkdown(optionInterpretMarkdown.isSelected());
		table.setInterpretLaTeX(optionInterpretLaTeX.isSelected());

		/* Auswahl der Farbe für äußeren Rahmen */
		table.setBordersOuter(colorBordersOuter.getColor());

		/* Auswahl der Farbe für die Rahmenlinien im Inneren */
		table.setBordersInner(colorBordersInner.getColor());

		/* Schriftfarbe */
		table.setColor(colorChooser.getColor());
	}
}
