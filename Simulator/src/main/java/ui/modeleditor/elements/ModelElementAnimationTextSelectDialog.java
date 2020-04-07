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

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
	private static final long serialVersionUID = 5107663089458560385L;

	private JTableExt expressionTable;
	private ExpressionTableModelText expressionTableModel;
	private JTextField defaultTextEdit;
	private JComboBox<FontCache.FontFamily> fontFamilyComboBox;
	private JTextField sizeField;
	private JCheckBox optionBold;
	private JCheckBox optionItalic;
	private SmallColorChooser colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationTextSelect}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationTextSelectDialog(final Component owner, final ModelElementAnimationTextSelect element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnimationTextSelect.Dialog.Title"),element,"ModelElementAnimationTextSelect",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationTextSelect;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JTabbedPane tabs=new JTabbedPane();

		Object[] data;
		JPanel tabOuter, tab, line;

		/* Tab "Ausdrücke" */
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
		sizeField.addActionListener((e)->NumberTools.getNotNegativeInteger(sizeField,true));

		/* Fett / Kursiv */
		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionBold=new JCheckBox("<html><b>"+Language.tr("Surface.AnimationText.Dialog.FontSize.Bold")+"</b></html>",false));
		optionBold.setEnabled(!readOnly);
		line.add(optionItalic=new JCheckBox("<html><i>"+Language.tr("Surface.AnimationText.Dialog.FontSize.Italic")+"</i></html>",false));
		optionItalic.setEnabled(!readOnly);

		/* Farbe */
		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("Surface.AnimationText.Dialog.FontColor")+":"));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooser=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooser.setEnabled(!readOnly);

		/* Icons für Tabs */
		tabs.setIconAt(0,Images.MODE_EXPRESSION.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROPERTIES_APPEARANCE.getIcon());

		/* Werte initialisieren */
		if (element instanceof ModelElementAnimationTextSelect) {
			defaultTextEdit.setText(((ModelElementAnimationTextSelect)element).getDefaultText());
			sizeField.setText(""+((ModelElementAnimationTextSelect)element).getTextSize());
			optionBold.setSelected(((ModelElementAnimationTextSelect)element).getTextBold());
			optionItalic.setSelected(((ModelElementAnimationTextSelect)element).getTextItalic());
			colorChooser.setColor(((ModelElementAnimationTextSelect)element).getColor());
		}

		return tabs;
	}

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

		if (element instanceof ModelElementAnimationTextSelect) {
			expressionTableModel.storeData((ModelElementAnimationTextSelect)element);
			((ModelElementAnimationTextSelect)element).setDefaultText(defaultTextEdit.getText());
			Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
			((ModelElementAnimationTextSelect)element).setFontFamily((FontCache.FontFamily)fontFamilyComboBox.getSelectedItem());
			if (I!=null) ((ModelElementAnimationTextSelect)element).setTextSize(I);
			((ModelElementAnimationTextSelect)element).setTextBold(optionBold.isSelected());
			((ModelElementAnimationTextSelect)element).setTextItalic(optionItalic.isSelected());
			((ModelElementAnimationTextSelect)element).setColor(colorChooser.getColor());
		}
	}
}