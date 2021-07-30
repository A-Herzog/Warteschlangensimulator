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
import java.io.Serializable;

import javax.swing.JPanel;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.script.ScriptEditorPanel;

/**
 * Dieser Dialog ermöglicht es, ein in {@link AnimationExpression} konfiguriertes
 * Skript zu bearbeiten. Er wird über {@link AnimationExpressionPanel} aufgerufen.
 * @author Alexander Herzog
 * @see AnimationExpression
 * @see AnimationExpressionPanel
 */
public class AnimationExpressionDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=7344562003950600726L;

	/**
	 * Skript-Editor-Panel innerhalb des Dialogs
	 */
	private ScriptEditorPanel editor;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param mode	Initial zu verwendende Skriptsprache
	 * @param script	Initial anzuzeigendes Skript
	 * @param model	Zugehöriges Editormodell (welches für das Befehls-Popupmenü verwendet wird)
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Callback für Klicks auf die Hilfe-Schaltfläche
	 */
	public AnimationExpressionDialog(final Component owner, final AnimationExpression.ExpressionMode mode, final String script, final EditModel model, final boolean readOnly, final Runnable helpRunnable) {
		super(owner,Language.tr("AnimationExpression.Script.Edit"));

		/* Skripteditor */
		ScriptEditorPanel.ScriptMode editMode;
		switch (mode) {
		case Expression:
			editMode=ScriptEditorPanel.ScriptMode.Javascript;
			break;
		case Javascript:
			editMode=ScriptEditorPanel.ScriptMode.Javascript;
			break;
		case Java:
			editMode=ScriptEditorPanel.ScriptMode.Java;
			break;
		default:
			editMode=ScriptEditorPanel.ScriptMode.Javascript;
			break;
		}

		final JPanel content=createGUI(helpRunnable);
		content.setLayout(new BorderLayout());
		content.add(editor=new ScriptEditorPanel(script,editMode,readOnly,null,model,helpRunnable,ScriptEditorPanel.featuresPlainStationOutput),BorderLayout.CENTER);

		/* Dialog starten */
		setMinSizeRespectingScreensize(1024,768);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected boolean checkData() {
		return editor.checkData();
	}

	/**
	 * Liefert die eingestellte Skriptsprache.
	 * @return	Skriptsprache
	 */
	public AnimationExpression.ExpressionMode getMode() {
		switch (editor.getMode()) {
		case Javascript: return AnimationExpression.ExpressionMode.Javascript;
		case Java: return AnimationExpression.ExpressionMode.Java;
		default: return AnimationExpression.ExpressionMode.Javascript;
		}
	}

	/**
	 * Liefert das aktuelle Skript.
	 * @return	Skript
	 */
	public String getScript() {
		return editor.getScript();
	}
}
