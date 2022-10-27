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
import java.awt.GraphicsEnvironment;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;

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
		UI(SetupDialogPageUI.class,()->Language.tr("SettingsDialog.Tabs.GUI"),Images.SETUP_PAGE_APPLICATION),
		/** Leistung */
		PERFORMANCE(SetupDialogPagePerformance.class,()->Language.tr("SettingsDialog.Tabs.Performance"),Images.SETUP_PAGE_PERFORMANCE),
		/** Animation */
		ANIMATION(SetupDialogPageAnimation.class,()->Language.tr("SettingsDialog.Tabs.Animation"),Images.SETUP_PAGE_ANIMATION),
		/** Statistik */
		STATISTICS(SetupDialogPageStatistics.class,()->Language.tr("SettingsDialog.Tabs.Statistics"),Images.SETUP_PAGE_STATISTICS),
		/** Dateiformate */
		FILE_FORMATS(SetupDialogPageFileFormats.class,()->Language.tr("SettingsDialog.Tabs.Exporting"),Images.SETUP_PAGE_FILE_FORMATS),
		/** Sicherheit */
		SECURITY(SetupDialogPageSecurity.class,()->Language.tr("SettingsDialog.Tabs.Security"),Images.SETUP_PAGE_SECURITY),
		/** Updates */
		UPDATES(SetupDialogPageUpdates.class,()->Language.tr("SettingsDialog.Tabs.Updates"),Images.SETUP_PAGE_UPDATE);

		/**
		 * Klasse der Dialogseite
		 */
		private final Class<? extends SetupDialogPage> setupDialogPageClass;

		/**
		 * Name der Dialogseite in der aktuellen Sprache
		 */
		private final Supplier<String> nameGetter;

		/**
		 * Icon für den Tab über der Dialogseite
		 */
		private final Images image;

		/**
		 * Liefert den Namen der Dialogseite.
		 * @return	Name der Dialogseite
		 */
		public String getName() {
			return nameGetter.get();
		}

		/**
		 * Liefert das Icon für den Tab über der Dialogseite.
		 * @return	Icon für den Tab über der Dialogseite
		 */
		public Icon getIcon() {
			return image.getIcon();
		}

		/**
		 * Liefert eine neue Instanz der Dialogseite.
		 * @return	Neue Instanz der Dialogseite
		 */
		public SetupDialogPage getPageInstance()  {
			try {
				return setupDialogPageClass.getDeclaredConstructor().newInstance();
			} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e) {
				return null;
			}
		}

		/**
		 * Konstruktor des Enums
		 * @param setupDialogPageClass	Klasse der Dialogseite
		 * @param nameGetter	Callback das den Namen der Dialogseite liefert
		 * @param image	Icon für den Tab über der Dialogseite
		 */
		Page(final Class<? extends SetupDialogPage> setupDialogPageClass, final Supplier<String> nameGetter, final Images image) {
			this.setupDialogPageClass=setupDialogPageClass;
			this.nameGetter=nameGetter;
			this.image=image;
		}
	}

	/**
	 * Registerreiter für die Seiten
	 */
	private final JTabbedPane tabs;

	/**
	 * Dialogseiten
	 */
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
		for (Page page: Page.values()) {
			final SetupDialogPage tab=page.getPageInstance();
			pages.add(tab);

			final JPanel tabOuter=new JPanel(new FlowLayout(FlowLayout.LEFT));
			tabOuter.add(tab);
			final JScrollPane tabOuter2=new JScrollPane(tabOuter,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			tabOuter2.setBorder(BorderFactory.createEmptyBorder());
			tabs.addTab(page.getName(),tabOuter2);

			tabs.setIconAt(pages.size()-1,page.getIcon());
			tab.loadData();
		}

		/* Daten in den Dialog laden */

		for (SetupDialogPage page: pages) page.loadData();

		/* Dialog anzeigen */

		if (showPage!=null) for (int i=0;i<Page.values().length;i++) if (Page.values()[i]==showPage) {
			tabs.setSelectedIndex(i);
			break;
		}
		setMinSizeRespectingScreensize(750,0);
		pack();
		final int maxAllowedHeight=(int)Math.round(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight());
		if (getHeight()>maxAllowedHeight) setSize(getWidth(),maxAllowedHeight);
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