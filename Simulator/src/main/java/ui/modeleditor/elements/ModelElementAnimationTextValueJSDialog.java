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
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.LabeledAlphaButton;
import systemtools.LabeledColorChooserButton;
import systemtools.MsgBox;
import systemtools.OptionalColorChooserButton;
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
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3914193192263655248L;

	/** Editor für das Skript */
	private ScriptEditorPanel editor;
	/** Auswahlbox der Schriftart */
	private JComboBox<FontCache.FontFamily> fontFamilyComboBox;
	/** Eingabefeld für die Schriftgröße */
	private JTextField sizeField;
	/** Option: Schrift in Fettdruck anzeigen */
	private JCheckBox optionBold;
	/** Option: Schrift kursiv anzeigen */
	private JCheckBox optionItalic;
	/** Auswahl der Textfarbe */
	private LabeledColorChooserButton colorChooser;
	/** Auswahl der Hintergrundfarbe */
	private OptionalColorChooserButton colorChooserBackground;
	/** Schieberegler zur Auswahl des Deckkraft der Hintergrundfarbe */
	private LabeledAlphaButton alpha;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationTextValueJS}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementAnimationTextValueJSDialog(final Component owner, final ModelElementAnimationTextValueJS element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.AnimationTextJS.Dialog.Title"),element,"ModelElementAnimationTextJS",readOnly);
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
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
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
		sizeField.addActionListener(e->NumberTools.getNotNegativeInteger(sizeField,true));

		/* Fett / Kursiv */
		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionBold=new JCheckBox("<html><b>"+Language.tr("Surface.AnimationText.Dialog.FontSize.Bold")+"</b></html>",false));
		optionBold.setEnabled(!readOnly);
		line.add(optionItalic=new JCheckBox("<html><i>"+Language.tr("Surface.AnimationText.Dialog.FontSize.Italic")+"</i></html>",false));
		optionItalic.setEnabled(!readOnly);

		/* Zeile für Farben */
		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		/* Schriftfarbe */
		line.add(colorChooser=new LabeledColorChooserButton(Language.tr("Surface.AnimationText.Dialog.Color")+":",Color.BLACK));
		colorChooser.setEnabled(!readOnly);

		line.add(Box.createHorizontalStrut(10));

		/* Hintergrundfarbe */
		line.add(colorChooserBackground=new OptionalColorChooserButton(Language.tr("Surface.AnimationText.Dialog.FillBackground"),null,Color.BLACK));
		colorChooserBackground.setEnabled(!readOnly);

		line.add(Box.createHorizontalStrut(10));

		/* Deckkraft */
		line.add(alpha=new LabeledAlphaButton(Language.tr("Surface.AnimationText.Dialog.Alpha")+":",0));
		alpha.setEnabled(!readOnly);

		/* Werte initialisieren */
		if (element instanceof ModelElementAnimationTextValueJS) {
			final ModelElementAnimationTextValueJS text=(ModelElementAnimationTextValueJS)element;
			sizeField.setText(""+text.getTextSize());
			optionBold.setSelected(text.getTextBold());
			optionItalic.setSelected(text.getTextItalic());
			colorChooser.setColor(text.getColor());
			colorChooserBackground.setColor(text.getFillColor());
			alpha.setAlpha(text.getFillAlpha());
		}

		alpha.addClickListener(e->colorChooserBackground.setActive(true));

		/* Icons für Tabs */
		tabs.setIconAt(0,Images.SCRIPTRUNNER.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROPERTIES_TEXT.getIcon());

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (showErrorMessage) {
			ok=editor.checkData();
			if (!ok) return false;
		}

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

		if (!(element instanceof ModelElementAnimationTextValueJS)) return;
		final ModelElementAnimationTextValueJS text=(ModelElementAnimationTextValueJS)element;

		/* Skript */
		text.setScript(editor.getScript());
		switch (editor.getMode()) {
		case Javascript:
			text.setMode(ModelElementAnimationTextValueJS.ScriptMode.Javascript);
			break;
		case Java:
			text.setMode(ModelElementAnimationTextValueJS.ScriptMode.Java);
			break;
		}

		/* Schriftart */
		text.setFontFamily((FontCache.FontFamily)fontFamilyComboBox.getSelectedItem());

		/* Schriftgröße */
		final Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
		if (I!=null) text.setTextSize(I);

		/* Fett/Kursiv */
		text.setTextBold(optionBold.isSelected());
		text.setTextItalic(optionItalic.isSelected());

		/* Schriftfarbe */
		text.setColor(colorChooser.getColor());

		/* Hintergrundfarbe */
		text.setFillColor(colorChooserBackground.getColor());

		/* Deckkraft */
		text.setFillAlpha(alpha.getAlpha());
	}
}
