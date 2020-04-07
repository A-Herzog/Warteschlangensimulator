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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorPanel;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationTextValueJS}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationTextValueJS
 */
public final class ModelElementAnimationTextValueJSDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = -3914193192263655248L;

	private ScriptEditorPanel editor;
	private JComboBox<FontCache.FontFamily> fontFamilyComboBox;
	private JTextField sizeField;
	private JCheckBox optionBold;
	private JCheckBox optionItalic;
	private SmallColorChooser colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationTextValueJS}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationTextValueJSDialog(final Component owner, final ModelElementAnimationTextValueJS element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnimationTextJS.Dialog.Title"),element,"ModelElementAnimationTextJS",readOnly);
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
		return InfoPanel.stationAnimationTextValueJS;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tabOuter, tab, line;
		Object[] data;

		/* Skript */
		tabs.addTab(Language.tr("Surface.AnimationTextJS.Dialog.Tab.Script"),tabOuter=new JPanel(new BorderLayout()));

		final String script=((ModelElementAnimationTextValueJS)element).getScript();
		ScriptEditorPanel.ScriptMode mode;
		switch (((ModelElementAnimationTextValueJS)element).getMode()) {
		case Javascript: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
		case Java: mode=ScriptEditorPanel.ScriptMode.Java; break;
		default: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
		}
		tabOuter.add(editor=new ScriptEditorPanel(script,mode,readOnly,Language.tr("Surface.AnimationTextJS.Dialog.Script"),element.getModel(),helpRunnable,ScriptEditorPanel.featuresPlainStationOutput),BorderLayout.CENTER);

		/* Darstellung */
		tabs.addTab(Language.tr("Surface.AnimationTextJS.Dialog.Tab.FontAndColor"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel());
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		/* Schriftart */
		data=getFontFamilyComboBoxPanel(Language.tr("Surface.AnimationTextJS.Dialog.FontFamily")+":",((ModelElementAnimationTextValueJS)element).getFontFamily());
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

		/* Werte initialisieren */
		if (element instanceof ModelElementAnimationTextValueJS) {
			sizeField.setText(""+((ModelElementAnimationTextValueJS)element).getTextSize());
			optionBold.setSelected(((ModelElementAnimationTextValueJS)element).getTextBold());
			optionItalic.setSelected(((ModelElementAnimationTextValueJS)element).getTextItalic());
			colorChooser.setColor(((ModelElementAnimationTextValueJS)element).getColor());
		}

		/* Icons für Tabs */
		tabs.setIconAt(0,Images.SCRIPTRUNNER.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROPERTIES_TEXT.getIcon());

		checkData(false);

		return content;
	}

	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
		if (I==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.AnimationText.Dialog.FontSize.Error.Title"),Language.tr("Surface.AnimationText.Dialog.FontSize.Error.Info"));
				return false;
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementAnimationTextValueJS) {
			((ModelElementAnimationTextValueJS)element).setScript(editor.getScript());
			switch (editor.getMode()) {
			case Javascript:
				((ModelElementAnimationTextValueJS)element).setMode(ModelElementAnimationTextValueJS.ScriptMode.Javascript);
				break;
			case Java:
				((ModelElementAnimationTextValueJS)element).setMode(ModelElementAnimationTextValueJS.ScriptMode.Java);
				break;
			}

			Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
			((ModelElementAnimationTextValueJS)element).setFontFamily((FontCache.FontFamily)fontFamilyComboBox.getSelectedItem());
			if (I!=null) ((ModelElementAnimationTextValueJS)element).setTextSize(I);
			((ModelElementAnimationTextValueJS)element).setTextBold(optionBold.isSelected());
			((ModelElementAnimationTextValueJS)element).setTextItalic(optionItalic.isSelected());
			((ModelElementAnimationTextValueJS)element).setColor(colorChooser.getColor());
		}
	}
}
