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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import language.Language;
import scripting.java.SimDynamicSetup;
import systemtools.BaseDialog;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;

/**
 * In diesem Dialog können die Imports, die für Klassen
 * mit nutzerdefiniertem Java-Code verwendet werden sollen,
 * eingestellt werden.
 * @author Alexander Herzog
 */
public class JavaImportsDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=6041655299516004145L;

	/**
	 * Eingabefeld für die Imports
	 */
	private final JTextArea importsEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param initalJavaImports	Initial anzuzeigende Imports
	 */
	public JavaImportsDialog(final Component owner, final String initalJavaImports) {
		super(owner,Language.tr("JavaImports.Title"));

		/* GUI */
		addUserButton(Language.tr("JavaImports.Reset"),Images.EDIT_UNDO.getIcon());
		final JPanel all=createGUI(()->Help.topicModal(this,"JavaImports"));
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.globalJavaImports);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		/* Eingabefeld */
		String imports=SimDynamicSetup.defaultImports;
		if (initalJavaImports!=null && !initalJavaImports.trim().isEmpty()) imports=initalJavaImports;
		content.add(new JScrollPane(importsEdit=new JTextArea(imports)),BorderLayout.CENTER);

		/* Dialog starten */
		setResizable(true);
		setMinSizeRespectingScreensize(640,480);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		importsEdit.setText(SimDynamicSetup.defaultImports);
	}

	/**
	 * Liefert die in dem Dialog eingestellten Imports zurück.
	 * @return	vom Nutzer gewählte Imports
	 */
	public String getImports() {
		final String imports=importsEdit.getText().trim();
		if (imports.isEmpty() || imports.equals(SimDynamicSetup.defaultImports)) return "";
		return imports;
	}
}
