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
package simulator.db;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.Table;
import mathtools.distribution.swing.CommonVariables;
import simulator.db.DBConntectSetupTemplates.DBType;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dieses Panel ermöglicht es, Einstellungen zur Verbindung
 * zu einer Datenbank herzustellen.
 * @author Alexander Herzog
 * @see DBSettings
 */
public class DBSettingsPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1050768140573186364L;

	/** Einstellungenobjekt (darf nicht <code>null</code> sein) */
	private final DBSettings settings;

	/** Ausgangs-Einstellungenobjekt */
	private DBSettings lastSettings;

	/** Nur-Lese-Status */
	private final boolean readOnly;

	/** Datenbank-Typ */
	private final JComboBox<String> comboType;

	/** Konfiguration für die Datenbankverbindung */
	private final JTextField editConfig;

	/** Schaltfläche zur Auswahl einer Datei für die Datenbank (zum Eintragen in das Konfigurations-Eingabefeld) */
	private final JButton buttonConfig;

	/** Anzeige der Verbindungseigenschaften */
	private final JLabel labelConfig;

	/** Nutzername für die Datenbank */
	private final JTextField editUser;

	/** Passwort für die Datenbank */
	private final JTextField editPassword;

	/** Listener, die bei Änderungen zu benachrichtigen sind */
	private final List<Runnable> changeListeners;

	/**
	 * Konstruktor der Klasse
	 * @param settings	Einstellungenobjekt (darf nicht <code>null</code> sein)
	 * @param readOnly	Nur-Lese-Status
	 */
	public DBSettingsPanel(final DBSettings settings, final boolean readOnly) {
		super();
		this.settings=settings;
		this.readOnly=readOnly;
		changeListeners=new ArrayList<>();

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		JPanel line;
		JLabel label;
		Object[] data;

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Database.Type")+":"));
		final String[] typeNames=DBConnectSetups.getNames();
		line.add(comboType=new JComboBox<>(typeNames));
		final Icon[] iconList=new Icon[typeNames.length];
		Arrays.fill(iconList,Images.SIMULATION_CHECK_DATABASE.getIcon());
		comboType.setRenderer(new IconListCellRenderer(iconList));
		label.setLabelFor(comboType);
		comboType.setEnabled(!readOnly);
		comboType.setSelectedIndex(DBConnectSetups.index(settings.getType()));
		comboType.addActionListener(e->comboChanged());

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Database.Config")+":",settings.getConfig());
		add(line=(JPanel)data[0]);
		editConfig=(JTextField)data[1];
		editConfig.setEditable(!readOnly);
		editConfig.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {fireChangedNotify();}
			@Override public void keyReleased(KeyEvent e) {fireChangedNotify();}
			@Override public void keyPressed(KeyEvent e) {fireChangedNotify();}
		});
		line.add(buttonConfig=new JButton(),BorderLayout.EAST);
		buttonConfig.setEnabled(!readOnly);
		buttonConfig.addActionListener(e->configButtonClick());

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(labelConfig=new JLabel(""));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Database.User")+":",settings.getUser());
		add((JPanel)data[0]);
		editUser=(JTextField)data[1];
		editUser.setEditable(!readOnly);
		editUser.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {fireChangedNotify();}
			@Override public void keyReleased(KeyEvent e) {fireChangedNotify();}
			@Override public void keyPressed(KeyEvent e) {fireChangedNotify();}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Database.Password")+":",settings.getPassword());
		add((JPanel)data[0]);
		editPassword=(JTextField)data[1];
		editPassword.setEditable(!readOnly);
		editPassword.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {fireChangedNotify();}
			@Override public void keyReleased(KeyEvent e) {fireChangedNotify();}
			@Override public void keyPressed(KeyEvent e) {fireChangedNotify();}
		});

		lastSettings=storeToCopy();
		comboChanged();
	}

	/**
	 * Reagiert darauf, wenn ein neuer Datenbanktyp gewählt wurde.
	 * @see #comboType
	 */
	private void comboChanged() {
		final DBConnectSetup currentType=DBConnectSetups.byIndex(comboType.getSelectedIndex());
		final DBType type=DBConntectSetupTemplates.getByName(currentType.name);

		editUser.setEnabled(!readOnly && type!=DBConntectSetupTemplates.DBType.SQLITE_FILE);
		editPassword.setEnabled(!readOnly && type!=DBConntectSetupTemplates.DBType.SQLITE_FILE);

		buttonConfig.setVisible(currentType.selectSource!=DBConnectSetup.SelectSource.NONE);
		if (currentType.selectSource.isFile) {
			buttonConfig.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
			buttonConfig.setToolTipText(Language.tr("Surface.Database.Config.ButtonHintFile"));
		}
		if (currentType.selectSource.isFolder) {
			buttonConfig.setIcon(Images.GENERAL_SELECT_FOLDER.getIcon());
			buttonConfig.setToolTipText(Language.tr("Surface.Database.Config.ButtonHintFolder"));
		}

		if (type==null) {
			labelConfig.setText(Language.tr("Surface.Database.Config.InfoUser"));
		} else {
			switch (type) {
			case SQLITE_FILE:
				labelConfig.setText(Language.tr("Surface.Database.Config.InfoSQLite"));
				buttonConfig.setToolTipText(Language.tr("Surface.Database.Config.ButtonHintSQLite"));
				break;
			case HSQLDB_LOCAL:
				labelConfig.setText(Language.tr("Surface.Database.Config.InfoHSQLDBLocal"));
				buttonConfig.setToolTipText(Language.tr("Surface.Database.Config.ButtonHintHSQLDBLocal"));
				break;
			case HSQLDB_SERVER:
				labelConfig.setText(Language.tr("Surface.Database.Config.InfoHSQLDBServer"));
				break;
			case POSTGRESQL_SERVER:
				labelConfig.setText(Language.tr("Surface.Database.Config.InfoPostgreSQL"));
				break;
			case MARIADB_SERVER:
				labelConfig.setText(Language.tr("Surface.Database.Config.InfoMariaDB"));
				break;
			case FIREBIRD_SERVER:
				labelConfig.setText(Language.tr("Surface.Database.Config.InfoFirebird"));
				break;
			case ACCESS:
				labelConfig.setText(Language.tr("Surface.Database.Config.InfoAccess"));
				buttonConfig.setToolTipText(Language.tr("Surface.Database.Config.ButtonHintAccess"));
				break;
			case DERBY:
				labelConfig.setText(Language.tr("Surface.Database.Config.InfoDerby"));
				buttonConfig.setToolTipText(Language.tr("Surface.Database.Config.ButtonHintDerby"));
				break;
			}
		}

		fireChangedNotify();
	}

	/**
	 * Reagiert auf einen Klick auf die Schaltfläche zur
	 * Konfiguration der Datenbankeinstellungen.
	 * @see #buttonConfig
	 */
	private void configButtonClick() {
		final DBConnectSetup currentType=DBConnectSetups.byIndex(comboType.getSelectedIndex());

		if (currentType.selectSource==DBConnectSetup.SelectSource.FILE_SQLITE) {
			final JFileChooser fc;
			if (!editConfig.getText().trim().isEmpty()) fc=new JFileChooser(editConfig.getText()); else {
				fc=new JFileChooser();
				CommonVariables.initialDirectoryToJFileChooser(fc);
			}
			fc.setDialogTitle(Language.tr("Surface.Database.Config.ButtonHintSQLite"));
			FileFilter sqlite=new FileNameExtensionFilter(Table.FileTypeSQLite+" (*.sqlite3, *.sqlite, *.db, *.db3, *.s3db)","sqlite3","sqlite","db","db3","s3db");
			fc.addChoosableFileFilter(sqlite);
			fc.setFileFilter(sqlite);
			if (fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			File file=fc.getSelectedFile();
			if (file.getName().indexOf('.')<0) {
				if (fc.getFileFilter()==sqlite) file=new File(file.getAbsoluteFile()+".sqlite3");
			}
			editConfig.setText(file.toString());
			fireChangedNotify();
		}

		if (currentType.selectSource==DBConnectSetup.SelectSource.FILE_GENERAL) {
			final JFileChooser fc;
			if (!editConfig.getText().trim().isEmpty()) fc=new JFileChooser(editConfig.getText()); else {
				fc=new JFileChooser();
				CommonVariables.initialDirectoryToJFileChooser(fc);
			}
			fc.setDialogTitle(Language.tr("Surface.Database.Config.ButtonHintFile"));
			if (fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			File file=fc.getSelectedFile();
			editConfig.setText(file.toString());
			fireChangedNotify();
		}

		if (currentType.selectSource==DBConnectSetup.SelectSource.FOLDER) {
			final JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			fc.setDialogTitle(Language.tr("Surface.Database.Config.ButtonHintHSQLDBLocal"));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			final File file=fc.getSelectedFile();
			editConfig.setText(file.toString());
			fireChangedNotify();
		}

		if (currentType.selectSource==DBConnectSetup.SelectSource.FILE_ACCESS) {
			final JFileChooser fc;
			if (!editConfig.getText().trim().isEmpty()) fc=new JFileChooser(editConfig.getText()); else {
				fc=new JFileChooser();
				CommonVariables.initialDirectoryToJFileChooser(fc);
			}
			fc.setDialogTitle(Language.tr("Surface.Database.Config.ButtonHintAccess"));
			FileFilter access=new FileNameExtensionFilter(Language.tr("Surface.Database.FileTypeAccess")+" (*.accdb)","accdb");
			fc.addChoosableFileFilter(access);
			fc.setFileFilter(access);
			if (fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			File file=fc.getSelectedFile();
			if (file.getName().indexOf('.')<0) {
				if (fc.getFileFilter()==access) file=new File(file.getAbsoluteFile()+".accdb");
			}
			editConfig.setText(file.toString());
			fireChangedNotify();
		}
	}

	/**
	 * Schreibt die Einstellungen aus der GUI in ein {@link DBSettings}-Objekt zurück.
	 * @param settings	{@link DBSettings}-Objekt in das die Einstellungen aus der GUI geschrieben werden sollen.
	 * @see #storeData()
	 */
	private void storeData(final DBSettings settings) {
		settings.setType(DBConnectSetups.byIndex(comboType.getSelectedIndex()).name);
		settings.setConfig(editConfig.getText().trim());
		settings.setUser(editUser.getText().trim());
		settings.setPassword(editPassword.getText().trim());
	}

	/**
	 * Schreibt die Einstellungen aus der GUI in das im Konstruktor übergebene
	 * Einstellungenobjekt zurück.
	 */
	public void storeData() {
		if (readOnly) return;

		storeData(settings);
	}

	/**
	 * Schreibt die Einstellungen aus der GUI in ein neu erstelltes Einstellungenobjekt
	 * @return	Neues Einstellungenobjekt
	 */
	public DBSettings storeToCopy() {
		final DBSettings settings=new DBSettings();
		storeData(settings);
		return settings;
	}

	/**
	 * Fügt einen Listener zu der Liste der Listener, die bei einer Änderung in der GUI benachrichtigt werden, hinzu.
	 * @param listener	Zusätzlicher zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener zu der Liste hinzugefügt wurde. War er bereits in der Liste, liefert die Funktion <code>false</code>.
	 * @see DBSettingsPanel#removeChangeListener(Runnable)
	 */
	public boolean addChangeListener(final Runnable listener) {
		if (changeListeners.contains(listener)) return false;
		changeListeners.add(listener);
		return true;
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die bei einer Änderung in der GUI benachrichtigt werden sollen.
	 * @param listener	Nicht mehr zu benachrichtigender Listener
	 * @return	Liefert <code>true</code> zurück, wenn sich der Listener vorher in der Liste befand.
	 * @see DBSettingsPanel#addChangeListener(Runnable)
	 */
	public boolean removeChangeListener(final Runnable listener) {
		return changeListeners.remove(listener);
	}

	/**
	 * Benachrichtigt die {@link #changeListeners}, dass es Änderungen an den
	 * Einstellungen in der GUI gab.
	 * @see #changeListeners
	 */
	private synchronized void fireChangedNotify() {
		final DBSettings newSettings=storeToCopy();
		if (lastSettings!=null && lastSettings.equalsDBSettings(newSettings)) return;
		lastSettings=new DBSettings(newSettings);

		for (Runnable listener: changeListeners) listener.run();
	}
}