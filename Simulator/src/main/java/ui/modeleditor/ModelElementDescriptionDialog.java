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
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import language.Language;
import systemtools.BaseDialog;
import ui.help.Help;
import ui.script.ScriptEditorAreaBuilder;

/**
 * Ermöglicht das Editieren einer Beschreibung für ein Modell-Element
 * @author Alexander Herzog
 * @see ModelElementBaseDialog
 */
public class ModelElementDescriptionDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2244792499807474032L;

	/** Eingabefeld für die Modellbeschreibung */
	private final RSyntaxTextArea descriptionEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param description	Bisherige Beschreibung
	 * @param readOnly	Read-Only-Status
	 * @param helpPage	Hilfeseite
	 */
	public ModelElementDescriptionDialog(final Component owner, final String description, final boolean readOnly, final String helpPage) {
		super(owner,Language.tr("Editor.DialogBase.Description.Dialog.Title"),readOnly);

		JLabel label;

		final JPanel content=createGUI(()->Help.topicModal(this,helpPage));
		content.setLayout(new BorderLayout());
		JPanel line;
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(label=new JLabel(Language.tr("Editor.DialogBase.Description.Dialog.Info")));
		content.add(new ScriptEditorAreaBuilder.RScrollPane(descriptionEdit=ScriptEditorAreaBuilder.getPlainTextField(description,readOnly,ScriptEditorAreaBuilder.TextAreaMode.ELEMENT_DESCRIPTION)),BorderLayout.CENTER);
		label.setLabelFor(descriptionEdit);

		setMinSizeRespectingScreensize(700,500);
		setSizeRespectingScreensize(700,500);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Liefert die neue, geänderte Beschreibung für das Element
	 * @return	Beschreibung für das Element
	 */
	public String getDescription() {
		return descriptionEdit.getText().trim();
	}
}
