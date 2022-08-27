/**
 * Copyright 2021 Alexander Herzog
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
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorAreaBuilder;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementNote}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementNote
 */
public class ModelElementNoteDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2715231750705476641L;

	/**
	 * Eingabefeld für die Notiz
	 */
	private RSyntaxTextArea edit;

	/**
	 * Datenmodell für das Icon Auswahlfeld
	 * @see #iconChooser
	 */
	private DefaultComboBoxModel<JLabel> iconChooserList;

	/**
	 * Icon Auswahlfeld
	 */
	private JComboBox<JLabel> iconChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementNote}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementNoteDialog(final Component owner, final ModelElementNote element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Note.Dialog.Title"),element,"ModelElementNote",readOnly,false);
		setMinimumSize(getSize());
		setResizable(true);
		setVisible(true);
	}

	@Override
	protected boolean hasNameField() {
		return false;
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,450);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationNote;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());
		JPanel sub;
		JLabel label;

		final ModelElementNote note=(ModelElementNote)element;

		/* Infozeile */
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.Note.Dialog.Note")+":"));

		/* Eingabebereich */
		content.add(new ScriptEditorAreaBuilder.RScrollPane(edit=ScriptEditorAreaBuilder.getPlainTextField(note.getNote(),readOnly,ScriptEditorAreaBuilder.TextAreaMode.NOTE_ELEMENT)),BorderLayout.CENTER);
		label.setLabelFor(edit);

		/* Icon-Auswahl */
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		sub.add(label=new JLabel(Language.tr("Surface.Note.Dialog.Icon")+":"));

		sub.add(iconChooser=new JComboBox<>());
		final AnimationImageSource imageSource=new AnimationImageSource();
		iconChooserList=imageSource.getIconsComboBox(element.getModel().animationImages);
		iconChooser.setModel(iconChooserList);
		iconChooser.setRenderer(new AnimationImageSource.IconComboBoxCellRenderer());
		iconChooser.setEnabled(!readOnly);
		label.setLabelFor(iconChooser);

		/* Icon-Combobox mit Vorgabe belegen */
		final String icon=note.getIcon();
		int index=0;
		for (int i=0;i<iconChooserList.getSize();i++) {
			String name=iconChooserList.getElementAt(i).getText();
			String value=AnimationImageSource.ICONS.getOrDefault(name,name);
			if (icon.equalsIgnoreCase(value)) {index=i; break;}
		}
		iconChooser.setSelectedIndex(index);

		return content;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementNote note=(ModelElementNote)element;

		note.setNote(edit.getText());

		String name=iconChooserList.getElementAt(iconChooser.getSelectedIndex()).getText();
		note.setIcon(AnimationImageSource.ICONS.getOrDefault(name,name));
	}
}
