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
package ui.script;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import systemtools.BaseDialog;
import systemtools.JRegExWikipediaLinkLabel;
import systemtools.JSearchSettingsSync;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Zeigt einen Dialog zur Eingabe eines Suchbegriffs an.
 * @author Alexander Herzog
 * @see ScriptEditorPanel
 * @see ScriptEditorAreaBuilder.SearchSetup
 */
public class ScriptEditorPanelSearchDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2589358955681475312L;

	/** Eingabefeld "Suchbegriff" */
	private final JTextField editSearchString;

	/** Checkbox "Groß- und Kleinschreibung beachten" */
	private final JCheckBox optionMatchCase;

	/** Checkbox "Suchbegriff ist regulärer Ausdruck" */
	private final JCheckBox optionRegex;

	/** Checkbox "Vorwärts suchen" */
	private final JCheckBox optionForward;

	/** Checkbox "Nur ganze Wörter" */
	private final JCheckBox optionWholeWord;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param lastSetup	Vorheriges Such-Setup (kann <code>null</code> sein)
	 */
	public ScriptEditorPanelSearchDialog(final Component owner, ScriptEditorAreaBuilder.SearchSetup lastSetup) {
		super(owner,Language.tr("Surface.ScriptEditor.Search"));

		if (lastSetup==null) lastSetup=ScriptEditorAreaBuilder.SearchSetup.getDefaultSetup();

		/* GUI */
		final JPanel all=createGUI(null);
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		all.add(content,BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;

		/* Suchbegriff */
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.ScriptEditor.Search.SearchString")+":",lastSetup.text);
		content.add((JPanel)data[0]);
		editSearchString=(JTextField)data[1];

		/* Groß- und Kleinschreibung beachten */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionMatchCase=new JCheckBox(Language.tr("Surface.ScriptEditor.Search.MatchCase"),JSearchSettingsSync.getCaseSensitive()));

		/* Suchbegriff ist regulärer Ausdruck */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionRegex=new JCheckBox(Language.tr("Surface.ScriptEditor.Search.RegularExpression"),JSearchSettingsSync.getRegEx()));
		line.add(new JRegExWikipediaLinkLabel(this));

		/* Vorwärts suchen */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionForward=new JCheckBox(Language.tr("Surface.ScriptEditor.Search.Forward"),JSearchSettingsSync.getForward()));

		/* Nur ganze Wörter */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionWholeWord=new JCheckBox(Language.tr("Surface.ScriptEditor.Search.WholeWord"),JSearchSettingsSync.getFullMatchOnly()));

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Liefert ein neues Such-Setup basierend auf den Einstellungen im Dialog.
	 * @return	Neues Such-Setup
	 */
	public ScriptEditorAreaBuilder.SearchSetup getNewSearchSetup() {
		JSearchSettingsSync.setCaseSensitive(optionMatchCase.isSelected());
		JSearchSettingsSync.setRegEx(optionRegex.isSelected());
		JSearchSettingsSync.setForward(optionForward.isSelected());
		JSearchSettingsSync.setFullMatchOnly(optionWholeWord.isSelected());
		return new ScriptEditorAreaBuilder.SearchSetup(editSearchString.getText());
	}
}
