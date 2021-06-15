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
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import language.Language;
import systemtools.BaseDialog;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;

/**
 * Zeigt den Setup-Dialog unter Verwendung der Daten aus <code>SetupData</code> an.
 * Alle Verarbeitung erfolgt direkt in dem Dialog. Der Dialog muss vom Aufrufer nur per Konstruktor
 * erstellt werden; das Daten Laden, Prüfen und Speichern übernimmt der Dialog selbst.
 * @see SetupData
 * @author Alexander Herzog
 */
public final class SetupDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8167759839522880144L;

	/**
	 * Seiten des Dialog
	 */
	public enum Page {
		/** Benutzeroberfläche */
		UI(0,()->Language.tr("SettingsDialog.Tabs.GUI")),
		/** Leistung */
		PERFORMANCE(1,()->Language.tr("SettingsDialog.Tabs.Performance")),
		/** Animation */
		ANIMATION(2,()->Language.tr("SettingsDialog.Tabs.Animation")),
		/** Statistik */
		STATISTICS(3,()->Language.tr("SettingsDialog.Tabs.Statistics")),
		/** Dateiformate */
		FILE_FORMATS(4,()->Language.tr("SettingsDialog.Tabs.Exporting")),
		/** Sicherheit */
		SECURITY(5,()->Language.tr("SettingsDialog.Tabs.Security")),
		/** Updates */
		UPDATES(6,()->Language.tr("SettingsDialog.Tabs.Updates"));

		/** Zugehöriger Dialogseiten-Index */
		private final int index;

		/** Name der Dialogseite in der aktuellen Sprache */
		private final Supplier<String> nameGetter;

		/**
		 * Liefert den Namen der Dialogseite
		 * @return	Name der Dialogseite
		 */
		public String getName() {
			return nameGetter.get();
		}

		/**
		 * Konstruktor des Enums.
		 * @param index Zu der Seite gehöriger Dialogseiten-Index
		 * @param nameGetter	Callback das den Namen der Dialogseite liefert
		 */
		Page(final int index, final Supplier<String> nameGetter) {
			this.index=index;
			this.nameGetter=nameGetter;
		}
	}

	/** Registerreiter für die Seiten */
	private final JTabbedPane tabs;

	/** Dialogseiten */
	private final List<SetupDialogPage> pages;

	/**
	 * Konstruktor der Klasse
	 * @param owner Übergeordnetes Element
	 * @param showPage Initial anzuzeigende Seite (darf <code>null</code> sein)
	 */
	public SetupDialog(final Component owner, final Page showPage) {
		super(owner,Language.tr("SettingsDialog.Title"));

		/* Schaltfläche "Standardwerte" */

		addUserButton(Language.tr("SettingsDialog.Default"),Language.tr("SettingsDialog.Default.Hint"),Images.EDIT_UNDO.getIcon());
		JPanel main=createGUI(()->Help.topicModal(SetupDialog.this.owner,"Setup"));
		main.setLayout(new BorderLayout());
		main.add(tabs=new JTabbedPane(),BorderLayout.CENTER);

		/* Dialogseiten anlegen */

		pages=new ArrayList<>();
		pages.add(new SetupDialogPageUI());
		pages.add(new SetupDialogPagePerformance());
		pages.add(new SetupDialogPageAnimation());
		pages.add(new SetupDialogPageStatistics());
		pages.add(new SetupDialogPageFileFormats());
		pages.add(new SetupDialogPageSecurity());
		pages.add(new SetupDialogPageUpdates());

		for (int i=0;i<pages.size();i++) {
			final SetupDialogPage page=pages.get(i);
			final JPanel tabOuter=new JPanel(new FlowLayout(FlowLayout.LEFT));
			tabs.addTab(page.getTitle(),tabOuter);
			tabOuter.add(page);
			tabs.setIconAt(i,page.getIcon());
			page.loadData();
		}

		/* Daten in den Dialog laden */

		for (SetupDialogPage page: pages) page.loadData();

		/* Dialog anzeigen */

		if (showPage!=null) tabs.setSelectedIndex(showPage.index);
		setMinSizeRespectingScreensize(750,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	@Override
	protected boolean checkData() {
		for (SetupDialogPage page: pages) if (!page.checkData()) return false;
		return true;
	}

	@Override
	protected void storeData() {
		for (SetupDialogPage page: pages) page.storeData();
		SetupData.getSetup().saveSetupWithWarning(this);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		pages.get(tabs.getSelectedIndex()).resetSettings();
	}
}