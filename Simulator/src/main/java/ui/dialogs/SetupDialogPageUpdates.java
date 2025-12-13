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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.distribution.swing.JOpenURL;
import mathtools.distribution.swing.PlugableFileChooser;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.MainPanel;
import ui.UpdateSystem;
import ui.images.Images;

/**
 * Dialogseite "Updates" im Programmeinstellungen-Dialog
 * @author Alexander Herzog
 * @see SetupData
 */
public class SetupDialogPageUpdates extends SetupDialogPage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5737135952990395935L;

	/** Java-Version beim Programmstart prüfen */
	private final JCheckBox programStartJavaCheck;
	/** Programm automatisch aktualisieren */
	private final JComboBox<String> autoUpdate;
	/** Informationen zum Fortschritt der Aktualisierung */
	private final JLabel updateInfo;
	/** Schaltfläche um ein manuelles Update auszulösen */
	private final JButton manualUpdateButton;
	/** Schaltfläche zum Prüfen auf Updates */
	private final JButton updateCheckButton;

	/**
	 * Konstruktor der Klasse
	 */
	public SetupDialogPageUpdates() {
		JPanel line;
		JLabel label;
		JButton button;


		addHeading(Language.tr("SettingsDialog.Update.SectionJava"));

		/* Java-Version beim Programmstart prüfen */
		addLine().add(programStartJavaCheck=new JCheckBox(Language.tr("SettingsDialog.TestJavaVersionOnProgramStart")));

		/* Versionsnummer der verwendeten Java-Version */
		line=addLine();
		line.add(new JLabel(Language.tr("InfoDialog.JavaVersion")+": "+System.getProperty("java.version")+" ("+System.getProperty("java.vm.name")+")"));

		/* Update URL */
		final boolean isWindows=System.getProperty("os.name").toUpperCase().contains("WIN");
		if (isWindows) {
			line=addLine();
			line.add(new JLabel(Language.tr("SettingsDialog.JDK.Info")+": "));
			line.add(label=new JLabel("<html><body><a href=\"https://"+MainPanel.JDK_URL+"\">https://"+MainPanel.JDK_URL+"</a></body></html>"));
			label.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent e) {
					if (SwingUtilities.isLeftMouseButton(e)) JOpenURL.open(SetupDialogPageUpdates.this,"https://"+MainPanel.JDK_URL);
				}
			});
		}

		if (setup.updaterAvailable) {
			addHeading(Language.tr("SettingsDialog.Update.SectionSimulator"));

			/* Programm automatisch aktualisieren */
			line=addLine();
			line.add(label=new JLabel(Language.tr("SettingsDialog.AutoUpdate")+":"));
			final UpdateSystem updateSystem=UpdateSystem.getUpdateSystem();
			if (updateSystem.isAutomaticUpdatePossible()) {
				line.add(autoUpdate=new JComboBox<>(new String[] {
						Language.tr("SettingsDialog.AutoUpdate.Off"),
						Language.tr("SettingsDialog.AutoUpdate.Search"),
						Language.tr("SettingsDialog.AutoUpdate.Install")
				}));
				autoUpdate.setRenderer(new IconListCellRenderer(new Images[]{
						Images.GENERAL_OFF,
						Images.GENERAL_INFO,
						Images.GENERAL_ON,
				}));
			} else {
				line.add(autoUpdate=new JComboBox<>(new String[] {
						Language.tr("SettingsDialog.AutoUpdate.Off"),
						Language.tr("SettingsDialog.AutoUpdate.Search"),
				}));
				autoUpdate.setRenderer(new IconListCellRenderer(new Images[]{
						Images.GENERAL_OFF,
						Images.GENERAL_INFO
				}));
			}
			label.setLabelFor(autoUpdate);

			/* Informationen zum Fortschritt der Aktualisierung */
			addLine().add(updateInfo=new JLabel());

			/* Schaltfläche: Proxy-Einstellungen */
			line=addLine();
			button=new JButton(Language.tr("SettingsDialog.ProxySettings"));
			button.setIcon(Images.SETUP_PROXY.getIcon());
			button.addActionListener(e->showProxySettingsDialog());
			line.add(button);

			/* Schaltfläche um ein manuelles Update auszulösen */
			line.add(manualUpdateButton=new JButton(Language.tr("SettingsDialog.ManualUpdate")));
			manualUpdateButton.setIcon(Images.SETUP_PAGE_UPDATE.getIcon());
			manualUpdateButton.setVisible(false);
			manualUpdateButton.addActionListener(e->showManualUpdateMenu());

			/* Schaltfläche zum Prüfen auf Updates */
			line.add(updateCheckButton=new JButton(Language.tr("SettingsDialog.UpdateCheck")));
			updateCheckButton.setIcon(Images.SETUP_PAGE_UPDATE.getIcon());
			updateCheckButton.setVisible(false);
			updateCheckButton.addActionListener(e->runUpdateCheck());
		} else {
			autoUpdate=null;
			updateInfo=null;
			manualUpdateButton=null;
			updateCheckButton=null;
		}
	}

	/**
	 * Dialog zur Konfiguration der Proxy-Server-Einstellungen.
	 */
	private void showProxySettingsDialog() {
		new ProxyDialog(this);
	}

	/**
	 * Führt eine Update-Prüfung durch.<br>
	 * (Wird, wenn entsprechend konfiguriert, beim Aufruf des Dialogs ausgeführt oder kann über eine Schaltfläche manuell ausgelöst werden.)
	 * @see #updateCheckButton
	 */
	private void runUpdateCheck() {
		updateInfo.setText("<html><b>"+Language.tr("SettingsDialog.Tabs.Updates.ConnectingServer")+"</b></html>");
		updateCheckButton.setVisible(false);
		final UpdateSystem updateSystem=UpdateSystem.getUpdateSystem();
		new Thread(()->{
			updateSystem.checkUpdateNow(true);
			updateInfo.setText("<html><b>"+updateSystem.getInfoString()+"</b></html>");
			final Timer timer=new Timer("UpdateCheckProgress");
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (!updateSystem.isLoading()) {
						timer.cancel();
						if (updateSystem.isNewVersionAvailable()==UpdateSystem.NewVersionAvailableStatus.NEW_VERSION_AVAILABLE) {
							manualUpdateButton.setVisible(true);
						}
					}
					updateInfo.setText("<html><b>"+updateSystem.getInfoString()+"</b></html>");
				}
			},1000,1000);
		}).start();
	}

	/**
	 * Zeigt das Popup-Menü zur Auswahl der Optionen für ein
	 * manuelles Update des Simulators an.
	 * @see #manualUpdateButton
	 */
	private void showManualUpdateMenu() {
		final JPopupMenu menu=new JPopupMenu();
		JMenuItem item;

		menu.add(item=new JMenuItem(Language.tr("SettingsDialog.ManualUpdate.Homepage")));
		item.setIcon(Images.HELP_HOMEPAGE.getIcon());
		item.addActionListener(e->JOpenURL.open(this,"https://"+MainPanel.WEB_URL));
		menu.add(item=new JMenuItem(Language.tr("SettingsDialog.ManualUpdate.Download")));
		item.setIcon(Images.GENERAL_SAVE.getIcon());
		item.addActionListener(e->{
			final var fc=new PlugableFileChooser(true);
			fc.setDialogTitle(Language.tr("SettingsDialog.ManualUpdate.Download.Folder"));
			final File file=fc.showSelectDirectoryDialog(this);
			if (file==null) return;
			final UpdateSystem updateSystem=UpdateSystem.getUpdateSystem();
			updateSystem.downloadUpdateToFolder(file);
			updateInfo.setText("<html><b>"+updateSystem.getInfoString()+"</b></html>");
			manualUpdateButton.setVisible(false);
			final Timer timer=new Timer("DownloadProgressCheck");
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (!updateSystem.isLoading()) {
						timer.cancel();
						manualUpdateButton.setVisible(true);
					}
					updateInfo.setText("<html><b>"+updateSystem.getInfoString()+"</b></html>");
				}
			},1000,1000);
		});
		menu.show(manualUpdateButton,0,manualUpdateButton.getHeight());
	}

	@Override
	public void loadData() {
		programStartJavaCheck.setSelected(setup.testJavaVersion);

		if (setup.updaterAvailable) {
			final UpdateSystem updateSystem=UpdateSystem.getUpdateSystem();
			if (updateSystem.isAutomaticUpdatePossible()) {
				switch (setup.autoUpdate) {
				case OFF: autoUpdate.setSelectedIndex(0); break;
				case SEARCH: autoUpdate.setSelectedIndex(1); break;
				case INSTALL: autoUpdate.setSelectedIndex(2); break;
				default: autoUpdate.setSelectedIndex(2); break;
				}
			} else {
				switch (setup.autoUpdate) {
				case OFF: autoUpdate.setSelectedIndex(0); break;
				case SEARCH: autoUpdate.setSelectedIndex(1); break;
				default: autoUpdate.setSelectedIndex(1); break;
				}
			}
			updateInfo.setText("<html><b>"+updateSystem.getInfoString()+"</b></html>");
			if (setup.autoUpdate==SetupData.AutoUpdate.INSTALL && updateSystem.isAutomaticUpdatePossible()) {
				runUpdateCheck();
			} else {
				if (updateSystem.isNewVersionAvailable()==UpdateSystem.NewVersionAvailableStatus.NEW_VERSION_AVAILABLE) {
					updateCheckButton.setVisible(false);
					manualUpdateButton.setVisible(true);
				} else {
					updateCheckButton.setVisible(true);
				}
			}
		}
	}

	@Override
	public void storeData() {
		setup.testJavaVersion=programStartJavaCheck.isSelected();
		if (setup.updaterAvailable) {
			switch (autoUpdate.getSelectedIndex()) {
			case 0: setup.autoUpdate=SetupData.AutoUpdate.OFF; break;
			case 1: setup.autoUpdate=SetupData.AutoUpdate.SEARCH; break;
			case 2: setup.autoUpdate=SetupData.AutoUpdate.INSTALL; break;
			}
		}
	}

	@Override
	public void resetSettings() {
		programStartJavaCheck.setSelected(true);
		if (setup.updaterAvailable) {
			autoUpdate.setSelectedIndex(autoUpdate.getModel().getSize()-1);
		}
	}
}