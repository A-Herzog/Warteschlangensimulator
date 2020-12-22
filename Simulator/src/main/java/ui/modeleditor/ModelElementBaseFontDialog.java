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
package ui.modeleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import systemtools.BaseDialog;
import ui.images.Images;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.FontCache;

/**
 * Dialog, der die Auswahl von Schriftarten für ein {@link ModelElementBox}-Element ermöglicht.<br>
 * Dieser Dialog wird von {@link ModelElementBaseDialog} verwendet, wenn dieser mit einem Objekt, dessen Typ
 * sich von {@link ModelElementBox} ableitet, instanziert wird.
 * @author Alexander Herzog
 * @see ModelElementBaseDialog
 */
public class ModelElementBaseFontDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8529275665071650491L;

	/** Auswahl der Schriftart (groß) */
	private final JComboBox<FontCache.FontFamily> fontFamilyComboBoxLarge;
	/** Eingabefeld für die Schriftgröße (groß) */
	private final SpinnerModel sizeLarge;
	/** Option: Text fett darstellen (groß) */
	private final JCheckBox optionBoldLarge;
	/** Option: Text kursiv darstellen (groß) */
	private final JCheckBox optionItalicLarge;

	/** Auswahl der Schriftart (klein) */
	private final JComboBox<FontCache.FontFamily> fontFamilyComboBoxSmall;
	/** Eingabefeld für die Schriftgröße (klein) */
	private final SpinnerModel sizeSmall;
	/** Option: Text fett darstellen (klein) */
	private final JCheckBox optionBoldSmall;
	/** Option: Text kursiv darstellen (klein) */
	private final JCheckBox optionItalicSmall;

	/**
	 * Konstruktor der Klasse {@link ModelElementBaseColorDialog}
	 * @param owner	Übergeordnetes Element
	 * @param help	Runnable, das aufgerufen wird, wenn der Nutzer auf die Hilfe-Schaltfläche klickt
	 * @param fontLarge	Große Schriftart für die Elementbox
	 * @param fontSmall	Kleine Schriftart für die Elementbox
	 */
	@SuppressWarnings("unchecked")
	public ModelElementBaseFontDialog(final Component owner, final Runnable help, final Font fontLarge, final Font fontSmall) {
		super(owner,Language.tr("Editor.FontSelect.Title"));

		addUserButton(Language.tr("Editor.FontSelect.ButtonUndo"),Images.EDIT_UNDO.getURL());
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tabOuter;
		JPanel tab;
		Object[] data;
		JPanel line;

		/* Tab "Große Schrift" */
		tabs.addTab(Language.tr("Editor.FontSelect.Tabs.Large"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		data=ModelElementBaseDialog.getFontFamilyComboBoxPanel(Language.tr("Surface.Text.Dialog.FontFamily")+":",FontCache.getFontCache().getFamilyFromName(fontLarge.getName()));
		fontFamilyComboBoxLarge=(JComboBox<FontCache.FontFamily>)data[1];
		tab.add((JPanel)data[0]);

		data=addFontSizeSpinner(Language.tr("Surface.Text.Dialog.FontSize"),fontLarge.getSize());
		sizeLarge=(SpinnerModel)data[1];
		tab.add((JPanel)data[0]);

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionBoldLarge=new JCheckBox("<html><b>"+Language.tr("Surface.Text.Dialog.Bold")+"</b></html>",fontLarge.isBold()));
		line.add(optionItalicLarge=new JCheckBox("<html><i>"+Language.tr("Surface.Text.Dialog.Italic")+"</i></html>",fontLarge.isItalic()));

		/* Tab "Kleine Schrift" */
		tabs.addTab(Language.tr("Editor.FontSelect.Tabs.Small"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		data=ModelElementBaseDialog.getFontFamilyComboBoxPanel(Language.tr("Surface.Text.Dialog.FontFamily")+":",FontCache.getFontCache().getFamilyFromName(fontSmall.getName()));
		fontFamilyComboBoxSmall=(JComboBox<FontCache.FontFamily>)data[1];
		tab.add((JPanel)data[0]);

		data=addFontSizeSpinner(Language.tr("Surface.Text.Dialog.FontSize"),fontSmall.getSize());
		sizeSmall=(SpinnerModel)data[1];
		tab.add((JPanel)data[0]);

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionBoldSmall=new JCheckBox("<html><b>"+Language.tr("Surface.Text.Dialog.Bold")+"</b></html>",fontSmall.isBold()));
		line.add(optionItalicSmall=new JCheckBox("<html><i>"+Language.tr("Surface.Text.Dialog.Italic")+"</i></html>",fontSmall.isItalic()));

		/* Icons */
		tabs.setIconAt(0,Images.GENERAL_FONT.getIcon());
		tabs.setIconAt(1,Images.GENERAL_FONT.getIcon());

		/* Start */
		pack();
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Erzeugt ein Spinner-Eingabefeld für eine Schriftgröße
	 * @param name	Beschriftung des Feldes
	 * @param initialValue	Anfänglich anzuzeigender Wert
	 * @return	2-elementiges Array: Panel das das Eingabefeld enthält und Spinner-Modell
	 */
	private Object[] addFontSizeSpinner(final String name, final int initialValue) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel label=new JLabel(name+":");
		line.add(label);

		final SpinnerNumberModel spinnerModel;
		final JSpinner serverPortSpinner=new JSpinner(spinnerModel=new SpinnerNumberModel(initialValue,1,112,1));
		final JSpinner.NumberEditor editor=new JSpinner.NumberEditor(serverPortSpinner);
		editor.getFormat().setGroupingUsed(false);
		editor.getTextField().setColumns(5);
		serverPortSpinner.setEditor(editor);
		line.add(serverPortSpinner);
		label.setLabelFor(serverPortSpinner);

		return new Object[] {line,spinnerModel};
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		FontCache.FontFamily family;
		ComboBoxModel<FontCache.FontFamily> model;
		int index;

		/* Große Schrift */

		family=FontCache.getFontCache().getFamilyFromName(ModelElementBox.DEFAULT_FONT_LARGE.getName());
		model=fontFamilyComboBoxLarge.getModel();

		index=-1;
		for (int i=0;i<model.getSize();i++) if (model.getElementAt(i)==family) {index=i; break;}
		if (index<0 && model.getSize()>0) index=0;
		fontFamilyComboBoxLarge.setSelectedIndex(index);

		sizeLarge.setValue(ModelElementBox.DEFAULT_FONT_LARGE.getSize());

		optionBoldLarge.setSelected(ModelElementBox.DEFAULT_FONT_LARGE.isBold());
		optionItalicLarge.setSelected(ModelElementBox.DEFAULT_FONT_LARGE.isItalic());

		/* Kleine Schrift */

		family=FontCache.getFontCache().getFamilyFromName(ModelElementBox.DEFAULT_FONT_SMALL.getName());
		model=fontFamilyComboBoxSmall.getModel();

		index=-1;
		for (int i=0;i<model.getSize();i++) if (model.getElementAt(i)==family) {index=i; break;}
		if (index<0 && model.getSize()>0) index=0;
		fontFamilyComboBoxSmall.setSelectedIndex(index);

		sizeSmall.setValue(ModelElementBox.DEFAULT_FONT_SMALL.getSize());

		optionBoldSmall.setSelected(ModelElementBox.DEFAULT_FONT_SMALL.isBold());
		optionItalicSmall.setSelected(ModelElementBox.DEFAULT_FONT_SMALL.isItalic());
	}

	/**
	 * Liefert die große Schriftart für die Elementbox.
	 * @return	Große Schriftart für die Elementbox
	 */
	public Font getFontLarge() {
		final String family=((FontCache.FontFamily)fontFamilyComboBoxLarge.getSelectedItem()).name;
		int style=Font.PLAIN;
		if (optionBoldLarge.isSelected()) style+=Font.BOLD;
		if (optionItalicLarge.isSelected()) style+=Font.ITALIC;
		return new Font(family,style,((Integer)sizeLarge.getValue()).intValue());
	}

	/**
	 * Liefert die kleine Schriftart für die Elementbox.
	 * @return	Kleine Schriftart für die Elementbox
	 */
	public Font getFontSmall() {
		final String family=((FontCache.FontFamily)fontFamilyComboBoxSmall.getSelectedItem()).name;
		int style=Font.PLAIN;
		if (optionBoldSmall.isSelected()) style+=Font.BOLD;
		if (optionItalicSmall.isSelected()) style+=Font.ITALIC;
		return new Font(family,style,((Integer)sizeSmall.getValue()).intValue());
	}
}
