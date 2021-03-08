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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import language.Language;
import systemtools.BaseDialog;
import ui.script.ScriptEditorAreaBuilder;

/**
 * Zeigt ein Skript aus einer Station an
 * @author Alexander Herzog
 * @see ModelSecurityCheckDialog
 */
public class ModelSecurityCheckScriptDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -383820623125365540L;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes visuelles Element (zur Ausrichtung des Dialogs)
	 * @param stationName	Name der Station
	 * @param scriptType	Skriptsprache (für passendes Syntax-Highlighting)
	 * @param script	Anzuzeigendes Skript
	 * @param help	Hilfe-Runnable
	 */
	public ModelSecurityCheckScriptDialog(final Component owner, final String stationName, final ModelSecurityCheckDialog.CriticalType scriptType, final String script, final Runnable help) {
		super(owner,Language.tr("ModelSecurityCheck.ScriptViewer")+" - "+stationName);

		showCloseButton=true;
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		final RSyntaxTextArea editor=new RSyntaxTextArea();
		ScriptEditorAreaBuilder.setupFontSize(editor);
		editor.setText(script);
		editor.setEditable(false);

		switch (scriptType) {
		case SCRIPT_JAVASCRIPT: editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT); break;
		case SCRIPT_JAVA: editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); break;
		default: /* Sollte nicht auftreten. Wenn doch, kein Highlighting. */ break;
		}
		content.add(new JScrollPane(editor),BorderLayout.CENTER);

		/* Dialog starten */
		setMinSizeRespectingScreensize(400,550);
		setResizable(true);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}
}
