/**
 * Copyright 2023 Alexander Herzog
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
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementLink}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementLink
 */
public class ModelElementLinkDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-7644009227213521850L;
	/** Eingabefeld für den anzuzeigenden Text */
	private JTextField editText;
	/** Eingabefeld für den Link */
	private JTextField editLink;
	/** Auswahl der Schriftart */
	private JComboBox<?> fontFamilyComboBox;
	/** Eingabefeld für die Schriftgröße */
	private JTextField sizeField;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementText}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementLinkDialog(final Component owner, final ModelElementLink element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.Link.Dialog.Title"),element,"ModelElementLink",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(550,0);
		pack();
	}

	@Override
	protected boolean hasNameField() {
		return false;
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationLink;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel outer=new JPanel(new BorderLayout());
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		outer.add(content,BorderLayout.NORTH);

		Object[] data;

		if (element instanceof ModelElementLink) {
			final ModelElementLink link=(ModelElementLink)element;

			/* Text */
			data=getInputPanel(Language.tr("Surface.Link.Dialog.Text")+":",link.getText());
			content.add((JPanel)data[0]);
			editText=(JTextField)data[1];
			editText.setEnabled(!readOnly);

			/* Link */
			data=getInputPanel(Language.tr("Surface.Link.Dialog.Link")+":",link.getLink());
			content.add((JPanel)data[0]);
			editLink=(JTextField)data[1];
			editLink.setEnabled(!readOnly);

			/* Schriftart */
			data=getFontFamilyComboBoxPanel(Language.tr("Surface.Link.Dialog.FontFamily")+":",link.getFontFamily());
			fontFamilyComboBox=(JComboBox<?>)data[1];
			fontFamilyComboBox.setEnabled(!readOnly);
			content.add((JPanel)data[0]);

			/* Schriftgröße */
			data=getInputPanel(Language.tr("Surface.Link.Dialog.FontSize")+":",""+link.getTextSize(),5);
			sizeField=(JTextField)data[1];
			sizeField.setEditable(!readOnly);
			content.add((JPanel)data[0]);
			sizeField.addActionListener(e->checkData(false));
		}

		return outer;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		final Integer I=NumberTools.getNotNegativeInteger(sizeField,true);
		if (I==null) {
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.Link.Dialog.FontSize.ErrorTitle"),String.format(Language.tr("Surface.Link.Dialog.FontSize.ErrorInfo"),sizeField.getText()));
				return false;
			}
			ok=false;
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
		if (element instanceof ModelElementLink) {
			final ModelElementLink link=(ModelElementLink)element;
			link.setText(editText.getText().trim());
			link.setLink(editLink.getText().trim());
			link.setFontFamily((FontCache.FontFamily)fontFamilyComboBox.getSelectedItem());
			link.setTextSize(NumberTools.getNotNegativeInteger(sizeField,true));
		}
	}
}
