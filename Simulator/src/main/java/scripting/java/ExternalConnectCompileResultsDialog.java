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
package scripting.java;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import language.Language;
import systemtools.BaseDialog;
import ui.help.Help;

/**
 * Zeigt die Ergebnisse der Kompilierung der Dateien im Plugins-Verzeichnis an.
 * @author Alexander Herzog
 * @see ExternalConnectDialog
 */
public class ExternalConnectCompileResultsDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2286992515191738384L;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param results	Anzuzeigende Ergebnisse
	 */
	public ExternalConnectCompileResultsDialog(final Component owner, final String results) {
		super(owner,Language.tr("ExternalConnect.Dialog.Compile.ResultsTitle"));

		/* GUI */
		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(this,"ExternalConnect"));
		content.	setLayout(new BorderLayout());

		/* Anzeige der Ergebnisse */
		final JTextArea text=new JTextArea(results);
		text.setEditable(false);
		content.add(new JScrollPane(text),BorderLayout.CENTER);

		/* Dialog starten */
		setSizeRespectingScreensize(1024,768);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}
}
