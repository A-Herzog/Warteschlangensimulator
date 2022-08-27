/**
 * Copyright 2022 Alexander Herzog
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
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.distribution.swing.JOpenURL;
import systemtools.BaseDialog;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.script.HunspellDictionaries;
import ui.script.HunspellDictionaryRecord;
import ui.script.ScriptEditorAreaBuilder;
import ui.script.ScriptEditorAreaBuilder.TextAreaMode;
import ui.tools.FlatLaFHelper;

/**
 * Zeigt einen Dialog zur Konfiguration der Rechtschreibungprüfung-Wörterbücher an.
 * @author Alexander Herzog
 * @see SetupDialog
 */
public class SetupDialogSpellChecking extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-4320341304276857421L;

	/**
	 * Konfigurations-Singleton
	 */
	private final SetupData setup;

	/**
	 * Liste der Checkboxen für die verschiedenen Wörterbücher
	 */
	private final List<JCheckBox> localesCheckBoxes;

	/**
	 * Eingabefeld für die nutzerdefinierten zusätzlichen Wörter
	 */
	private final JTextArea userWords;

	/**
	 * Checkboxen für die Auswahl der zu prüfenden Elemente
	 * @see TextAreaMode
	 */
	private final List<JCheckBox> modes;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public SetupDialogSpellChecking(final Component owner) {
		super(owner,Language.tr("SettingsDialog.Tabs.ProgramStart.SpellChecking.Title"));
		setup=SetupData.getSetup();

		final HunspellDictionaries hunspellDictionaries=HunspellDictionaries.getInstance();

		/* GUI */
		final JPanel content=createGUI(()->Help.topicModal(this,"Setup"));
		content.setLayout(new BorderLayout());

		/* Tabs */
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tabOuter, tab;

		/* Tab "Wörterbücher" */
		tabs.addTab(Language.tr("SettingsDialog.Tabs.ProgramStart.SpellChecking.Dictionaries"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		localesCheckBoxes=new ArrayList<>();
		final Set<String> activeLoces=new HashSet<>(Arrays.asList(setup.spellCheckingLanguages.split(";")));
		for (String locale: hunspellDictionaries.getAvailableLocales()) {
			final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			tab.add(line);
			final JCheckBox checkBox=new JCheckBox(locale,activeLoces.contains(locale));
			line.add(checkBox);
			localesCheckBoxes.add(checkBox);
		}

		tab.add(Box.createVerticalStrut(20));

		final String linkColor;
		if (FlatLaFHelper.isDark()) {
			linkColor="#589DF6";
		} else {
			linkColor="blue";
		}

		JPanel line;
		JLabel label;

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel("<html><body><span style=\"color: "+linkColor+"; text-decoration: underline;\">"+Language.tr("SettingsDialog.Tabs.ProgramStart.SpellChecking.Dictionaries.Folder")+"</span></body></html>"));
		label.setToolTipText(Language.tr("SettingsDialog.Tabs.ProgramStart.SpellChecking.Dictionaries.Folder.Hint"));
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) commandOpenDictionariesFolder();
			}
		});

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel("<html><body><span style=\"color: "+linkColor+"; text-decoration: underline;\">"+Language.tr("SettingsDialog.Tabs.ProgramStart.SpellChecking.Dictionaries.Website")+"</span></body></html>"));
		label.setToolTipText(Language.tr("SettingsDialog.Tabs.ProgramStart.SpellChecking.Dictionaries.Website.Hint"));
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) commandOpenDictionariesWebsite();
			}
		});

		if (HunspellDictionaryRecord.isGlobalOff()) {
			tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			String info;
			if (SetupData.getSetup().allowSpellCheck) {
				info=Language.tr("SettingsDialog.Tabs.ProgramStart.SpellChecking.GlobalOffBySystem");
			} else {
				info=Language.tr("SettingsDialog.Tabs.ProgramStart.SpellChecking.GlobalOffByUser");
			}
			line.add(label=new JLabel(info));
		}

		/* Tab "Nutzerdefinierte Wörter" */
		tabs.addTab(Language.tr("SettingsDialog.Tabs.ProgramStart.SpellChecking.UserWords"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(new JScrollPane(userWords=new JTextArea()));
		userWords.setText(String.join("\n",HunspellDictionaries.getUserDictionaryWords().toArray(new String[0])));

		/* Tab "Zu prüfende Elemente" */
		tabs.addTab(Language.tr("SettingsDialog.Tabs.ProgramStart.SpellChecking.Mode"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));
		modes=new ArrayList<>();
		for (ScriptEditorAreaBuilder.TextAreaMode mode: ScriptEditorAreaBuilder.TextAreaMode.values()) {
			tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			final JCheckBox checkBox=new JCheckBox(mode.getLanguageName(),setup.spellCheckMode.contains(mode));
			line.add(checkBox);
			modes.add(checkBox);
		}

		/* Icons auf Tabs */
		tabs.setIconAt(0,Images.SPELL_CHECK_DICTIONARIES.getIcon());
		tabs.setIconAt(1,Images.SPELL_CHECK_USER_WORDS.getIcon());
		tabs.setIconAt(2,Images.SPELL_CHECK_ELEMENTS.getIcon());

		/* Dialog starten */
		pack();
		setMinSizeRespectingScreensize(640,480);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Öffnet den Dialog zum Ablegen von xpi- und oxt-Wörterbüchern im Dateimanager.
	 */
	private void commandOpenDictionariesFolder() {
		final File folder1=new File(SetupData.getSetupFolder(),"dictionaries");
		final File folder2=new File(new File(SetupData.getProgramFolder(),"build"),"Dictionaries");

		if (folder1.isDirectory()) {
			try {Desktop.getDesktop().open(folder1);} catch (IOException e) {}
		} else {
			if (folder2.isDirectory()) {
				try {Desktop.getDesktop().open(folder2);} catch (IOException e) {}
			}
		}
	}

	/**
	 * Öffnet die Webseite zum Herunterladen von weiteren Wörterbüchern im Browser.
	 */
	private void commandOpenDictionariesWebsite() {
		JOpenURL.open(this,"https://addons.mozilla.org/de/firefox/language-tools/");
	}

	@Override
	protected void storeData() {
		/* Tab "Wörterbücher" */
		setup.spellCheckingLanguages=String.join(";",localesCheckBoxes.stream().filter(checkBox->checkBox.isSelected()).map(checkBox->checkBox.getText()).toArray(String[]::new));

		/* Tab "Nutzerdefinierte Wörter" */
		HunspellDictionaries.setUserDictionaryWords(Arrays.asList(userWords.getText().split("\\n")));

		/* Tab "Zu prüfende Elemente" */
		setup.spellCheckMode.clear();
		int index=0;
		for (ScriptEditorAreaBuilder.TextAreaMode mode: ScriptEditorAreaBuilder.TextAreaMode.values()) {
			if (modes.get(index).isSelected()) setup.spellCheckMode.add(mode);
			index++;
		}

		setup.saveSetup();
	}
}
