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
package gitconnect;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropper;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Zeigt einen Dialog zum Bearbeiten einer einzelnen Git-Konfiguration an.
 * @author Alexander Herzog
 * @see GitSetup
 * @see GitListDialog
 */
public class GitEditDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5658864138938275415L;

	/**
	 * Zu bearbeitende Git-Konfiguration
	 */
	private final GitSetup gitSetup;

	/**
	 * Eingabefeld für den lokalen Ordner, der als Git-Repository-Basis verwendet werden soll
	 * @see GitSetup#localFolder
	 */
	private final JTextField editLocalFolder;

	/**
	 * Informationstext zum lokalen Ordner.
	 * @see #editLocalFolder
	 */
	private final JLabel labelLocalFolder;

	/**
	 * Schaltfläche "Lokal initialisieren"
	 * @see #commandInitLocal()
	 */
	private final JButton buttonInitLocal;

	/**
	 * Schaltfläche "Status des Repositories"
	 * @see #commandStatus()
	 */
	private final JButton buttonStatus;

	/**
	 * Schaltfläche "Pull"
	 * @see #commandPull()
	 */
	private final JButton buttonPull;

	/**
	 * Schaltfläche "Push"
	 * @see #commandPush()
	 */
	private final JButton buttonPush;

	/**
	 * Schaltfläche "Zweige"
	 * @see #commandBranches()
	 */
	private final JButton buttonBranches;

	/**
	 * Auswahlbox: Git-Daten in entferntes Repository übertragen (Push)
	 * @see GitSetup#useServer
	 */
	private final JCheckBox checkboxUseServer;

	/**
	 * Eingabefeld für die Git-Server-URL
	 * @see GitSetup#serverURL
	 */
	private final JTextField editServerURL;

	/**
	 * Auswahlbox für die Authentifizierungsmethode
	 * @see GitSetup#serverAuth
	 */
	private final JComboBox<String> comboboxAuth;

	/**
	 * Bereich zur Anzeige der Authentifizierungseinstellungen
	 */
	private final JPanel authArea;

	/**
	 * Layout für {@link #authArea}
	 * @see #authArea
	 */
	private final CardLayout authAreaLayout;

	/**
	 * Eingabefeld für den Nutzernamen (bei Authentifizierung über Name und Passwort)
	 * @see GitSetup#authName
	 * @see GitSetup.ServerAuthMode#PASSWORD
	 */
	private final JTextField editAuthName;

	/**
	 * Eingabefeld für den Nutzernamen (bei Authentifizierung über Name und Passwort)
	 * @see GitSetup#authPassword
	 * @see GitSetup.ServerAuthMode#PASSWORD
	 */
	private final JTextField editAuthPassword;

	/**
	 * Eingabefeld für die Schlüsseldatei (bei Authentifizierung über einen private Key)
	 * @see GitSetup#authKey
	 * @see GitSetup.ServerAuthMode#KEY
	 */
	private final JTextField editAuthKey;

	/**
	 * Eingabefeld für das optionale Passwort zum Öffnen der Schlüsseldatei {@link #editAuthKey}
	 * @see #editAuthKey
	 * @see GitSetup#authKeyPassphrase
	 * @see GitSetup.ServerAuthMode#KEY
	 */
	private final JTextField editAuthKeyPassphrase;

	/**
	 * Informationstext zu der angegebenen Schlüsseldatei
	 * @see #editAuthKey
	 */
	private final JLabel labelAuthKey;

	/**
	 * Soll bei einem Server-Push die Datenübertragung
	 * erzwungen werden, auch wenn lokaler Branch und
	 * Branch auf dem Server keine gemeinsame Wurzel
	 * haben (Upstream)?
	 * @see GitSetup#forcePush
	 */
	private final JCheckBox checkboxForcePush;

	/**
	 * Auswahlbox: Soll beim Starten des Programms ein Server-Pull für dieses Repository durchgeführt werden?
	 * @see GitSetup#pullOnStart
	 */
	private final JCheckBox checkboxPullOnStart;

	/**
	 * Schaltfläche "Repository von Server clonen"
	 * @see #commandClone()
	 */
	private final JButton buttonClone;

	/**
	 * Auswahlbox: Sollen Modelle im Git-Repository gespeichert werden?
	 * @see GitSetup.GitSaveMode#MODELS
	 */
	private final JCheckBox checkboxSaveModels;

	/**
	 * Auswahlbox: Sollen Parameterreihen im Git-Repository gespeichert werden?
	 * @see GitSetup.GitSaveMode#PARAMETER_SERIES
	 */
	private final JCheckBox checkboxSaveParameterSeries;

	/**
	 * Auswahlbox: Sollen Optimierer-Einstellungen im Git-Repository gespeichert werden?
	 * @see GitSetup.GitSaveMode#OPTIMIZATION_SETUPS
	 */
	private final JCheckBox checkboxSaveOptimizationSetups;

	/**
	 * Auswahlbox: Sollen Statistikdateien im Git-Repository gespeichert werden?
	 * @see GitSetup.GitSaveMode#STATISTICS
	 */
	private final JCheckBox checkboxSaveStatistics;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param gitSetup	Zu bearbeitende Git-Konfiguration
	 */
	public GitEditDialog(final Component owner, final GitSetup gitSetup) {
		super(owner,Language.tr("Git.Edit.Title"));
		this.gitSetup=gitSetup;

		/* GUI */
		final JPanel content=createGUI(()->Help.topicModal(this,"Git"));
		content.setLayout(new BorderLayout());

		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tabOuter;
		JPanel tab;
		JPanel line, sub;
		Object[] obj;
		JButton button;
		JLabel label;

		/* Tab "Lokaler Ordner" */
		tabs.addTab(Language.tr("Git.List.Tab.LocalFolder"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		obj=ModelElementBaseDialog.getInputPanel(Language.tr("Git.List.Tab.LocalFolder.LocalFolder")+":",gitSetup.localFolder,50);
		tab.add(line=(JPanel)obj[0]);
		editLocalFolder=(JTextField)obj[1];
		editLocalFolder.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {commandTestLocalFolder();}
		});
		FileDropper.addDirectoryDropper(tabOuter,editLocalFolder);

		line.add(button=new JButton(Images.GENERAL_SELECT_FOLDER.getIcon()),BorderLayout.EAST);
		button.setToolTipText(Language.tr("Git.List.Tab.LocalFolder.LocalFolder.Select"));
		button.addActionListener(e->commandSelectLocalFolder());

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(labelLocalFolder=new JLabel());
		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(buttonInitLocal=new JButton(Language.tr("Git.List.Tab.Server.Button.InitLocal"),Images.GENERAL_SELECT_FOLDER.getIcon()));
		buttonInitLocal.setToolTipText(Language.tr("Git.List.Tab.Server.Button.InitLocal.Hint"));
		buttonInitLocal.addActionListener(e->commandInitLocal());
		line.add(buttonStatus=new JButton(Language.tr("Git.List.Tab.Server.Button.Status"),Images.GIT.getIcon()));
		buttonStatus.setToolTipText(Language.tr("Git.List.Tab.Server.Button.Status.Hint"));
		buttonStatus.addActionListener(e->commandStatus());
		line.add(Box.createHorizontalStrut(5));
		line.add(buttonPull=new JButton(Language.tr("Git.List.Tab.Server.Button.Pull"),Images.ARROW_DOWN.getIcon()));
		buttonPull.setToolTipText(Language.tr("Git.List.Tab.Server.Button.Pull.Hint"));
		buttonPull.addActionListener(e->commandPull());
		line.add(buttonPush=new JButton(Language.tr("Git.List.Tab.Server.Button.Push"),Images.ARROW_UP.getIcon()));
		buttonPush.setToolTipText(Language.tr("Git.List.Tab.Server.Button.Push.Hint"));
		buttonPush.addActionListener(e->commandPush());
		line.add(Box.createHorizontalStrut(5));
		line.add(buttonBranches=new JButton(Language.tr("Git.List.Tab.Server.Button.Branches"),Images.ARROW_SWITCH.getIcon()));
		buttonBranches.setToolTipText(Language.tr("Git.List.Tab.Server.Button.Branches.Hint"));
		buttonBranches.addActionListener(e->commandBranches());

		/* Tab "Server" */
		tabs.addTab(Language.tr("Git.List.Tab.Server"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(checkboxUseServer=new JCheckBox(Language.tr("Git.List.Tab.Server.UseServer"),gitSetup.useServer));
		checkboxUseServer.addActionListener(e->commandTestLocalFolder());

		obj=ModelElementBaseDialog.getInputPanel(Language.tr("Git.List.Tab.Server.ServerURL")+":",gitSetup.serverURL,50);
		tab.add(line=(JPanel)obj[0]);
		editServerURL=(JTextField)obj[1];
		editServerURL.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {checkboxUseServer.setSelected(true); commandTestLocalFolder();}
		});

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Git.List.Tab.Server.Auth")+":"));
		line.add(comboboxAuth=new JComboBox<>(new String[] {
				Language.tr("Git.List.Tab.Server.Auth.Off"),
				Language.tr("Git.List.Tab.Server.Auth.UserNamePassword"),
				Language.tr("Git.List.Tab.Server.Auth.Key")
		}));
		comboboxAuth.setRenderer(new IconListCellRenderer(new Icon[] {
				Images.AUTH_OFF.getIcon(),
				Images.AUTH_USER.getIcon(),
				Images.AUTH_KEY.getIcon()
		}));
		switch (gitSetup.serverAuth) {
		case NONE: comboboxAuth.setSelectedIndex(0); break;
		case PASSWORD: comboboxAuth.setSelectedIndex(1); break;
		case KEY: comboboxAuth.setSelectedIndex(2); break;
		default: comboboxAuth.setSelectedIndex(0); break;
		}
		comboboxAuth.addActionListener(e->commandSelectAuthType());
		label.setLabelFor(comboboxAuth);

		tab.add(authArea=new JPanel());
		authArea.setLayout(authAreaLayout=new CardLayout());

		authArea.add(sub=new JPanel(),Language.tr("Git.List.Tab.Server.Auth.Off"));

		authArea.add(sub=new JPanel(),Language.tr("Git.List.Tab.Server.Auth.UserNamePassword"));
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		obj=ModelElementBaseDialog.getInputPanel(Language.tr("Git.List.Tab.Server.Auth.UserNamePassword.Name")+":",gitSetup.authName,50);
		sub.add((JPanel)obj[0]);
		editAuthName=(JTextField)obj[1];

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		obj=ModelElementBaseDialog.getInputPanel(Language.tr("Git.List.Tab.Server.Auth.UserNamePassword.Password")+":",gitSetup.authPassword,50);
		sub.add((JPanel)obj[0]);
		editAuthPassword=(JTextField)obj[1];

		authArea.add(sub=new JPanel(),Language.tr("Git.List.Tab.Server.Auth.Key"));
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		obj=ModelElementBaseDialog.getInputPanel(Language.tr("Git.List.Tab.Server.Auth.Key.Key")+":",gitSetup.authKey,50);
		sub.add(line=(JPanel)obj[0]);
		editAuthKey=(JTextField)obj[1];
		editAuthKey.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {commandTestPrivateKey();}
		});
		line.add(button=new JButton(Images.GENERAL_SELECT_FILE.getIcon()),BorderLayout.EAST);
		button.setToolTipText(Language.tr("Git.List.Tab.Server.Auth.Key.SelectKey"));
		button.addActionListener(e->commandSelectKeyFile());

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		obj=ModelElementBaseDialog.getPlaceholderInputPanel(Language.tr("Git.List.Tab.Server.Auth.Key.Passphrase")+":",Language.tr("Git.List.Tab.Server.Auth.Key.Passphrase.Info"),gitSetup.authKeyPassphrase,50);
		sub.add(line=(JPanel)obj[0]);
		editAuthKeyPassphrase=(JTextField)obj[1];

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(labelAuthKey=new JLabel());
		FileDropper.addFileDropper(tabOuter,editAuthKey,()->{
			comboboxAuth.setSelectedIndex(2);
			commandSelectAuthType();
		});

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(checkboxForcePush=new JCheckBox(Language.tr("Git.List.Tab.Server.ForcePush"),gitSetup.forcePush));
		checkboxForcePush.setToolTipText(Language.tr("Git.List.Tab.Server.ForcePush.Hint"));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(checkboxPullOnStart=new JCheckBox(Language.tr("Git.List.Tab.Server.PullOnStart"),gitSetup.pullOnStart));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		line.add(button=new JButton(Language.tr("Git.List.Tab.Server.Button.Test"),Images.EXTRAS_SERVER.getIcon()));
		button.setToolTipText(Language.tr("Git.List.Tab.Server.Button.Test.Hint"));
		button.addActionListener(e->commandTestServerConnection());

		line.add(Box.createHorizontalStrut(5));

		line.add(buttonClone=new JButton(Language.tr("Git.List.Tab.Server.Button.Clone"),Images.EDIT_COPY.getIcon()));
		buttonClone.setToolTipText(Language.tr("Git.List.Tab.Server.Button.Clone.Hint"));
		buttonClone.addActionListener(e->commandClone());

		/* Tab "Daten speichern" */
		tabs.addTab(Language.tr("Git.List.Tab.StoreFiles"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("Git.List.Tab.StoreFiles.Info")));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(checkboxSaveModels=new JCheckBox(Language.tr("Git.List.Tab.StoreFiles.Models"),gitSetup.saveData.contains(GitSetup.GitSaveMode.MODELS)));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(checkboxSaveParameterSeries=new JCheckBox(Language.tr("Git.List.Tab.StoreFiles.ParameterSeries"),gitSetup.saveData.contains(GitSetup.GitSaveMode.PARAMETER_SERIES)));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(checkboxSaveOptimizationSetups=new JCheckBox(Language.tr("Git.List.Tab.StoreFiles.OptimizationSetups"),gitSetup.saveData.contains(GitSetup.GitSaveMode.OPTIMIZATION_SETUPS)));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(checkboxSaveStatistics=new JCheckBox(Language.tr("Git.List.Tab.StoreFiles.Statistics"),gitSetup.saveData.contains(GitSetup.GitSaveMode.STATISTICS)));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(button=new JButton(Language.tr("Git.List.Tab.StoreFiles.Button.All"),Images.EDIT_ADD.getIcon()));
		button.setToolTipText(Language.tr("Git.List.Tab.StoreFiles.Button.All.Hint"));
		button.addActionListener(e->{
			checkboxSaveModels.setSelected(true);
			checkboxSaveParameterSeries.setSelected(true);
			checkboxSaveOptimizationSetups.setSelected(true);
			checkboxSaveStatistics.setSelected(true);
		});
		line.add(Box.createHorizontalStrut(5));
		line.add(button=new JButton(Language.tr("Git.List.Tab.StoreFiles.Button.None"),Images.EDIT_DELETE.getIcon()));
		button.setToolTipText(Language.tr("Git.List.Tab.StoreFiles.Button.None.Hint"));
		button.addActionListener(e->{
			checkboxSaveModels.setSelected(false);
			checkboxSaveParameterSeries.setSelected(false);
			checkboxSaveOptimizationSetups.setSelected(false);
			checkboxSaveStatistics.setSelected(false);
		});

		/* GUI-Elemente gemäß geladenen Daten initialisieren */
		commandTestLocalFolder();
		commandTestPrivateKey();
		commandSelectAuthType();

		/* Icons auf den Tabs */
		tabs.setIconAt(0,Images.GENERAL_SELECT_FOLDER.getIcon());
		tabs.setIconAt(1,Images.EXTRAS_SERVER.getIcon());
		tabs.setIconAt(2,Images.MODEL.getIcon());

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Befehl: Ordner für das lokale Git-Verzeichnis wählen
	 */
	private void commandSelectLocalFolder() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		final String oldFolder=editLocalFolder.getText().trim();
		if (!oldFolder.isEmpty()) fc.setCurrentDirectory(new File(oldFolder));
		fc.setDialogTitle(Language.tr("Git.List.Tab.LocalFolder.LocalFolder.Select"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		editLocalFolder.setText(fc.getSelectedFile().toString());
		commandTestLocalFolder();
	}

	/**
	 * Prüft, ob das angegebene lokale Verzeichnis existiert und passt
	 * die Informationsanzeige entsprechend an.
	 * @see #editLocalFolder
	 * @see #labelLocalFolder
	 */
	private void commandTestLocalFolder() {
		final GitSetup setup=getNewSetup();

		/* Kein Verzeichnis angegeben */
		if (setup.localFolder.isBlank()) {
			labelLocalFolder.setIcon(Images.GENERAL_OFF.getIcon());
			labelLocalFolder.setText(Language.tr("Git.List.Tab.LocalFolder.LocalFolder.Info.NoFolderSelected"));
			buttonInitLocal.setVisible(false);
			buttonStatus.setVisible(false);
			buttonPull.setVisible(false);
			buttonPush.setVisible(false);
			buttonBranches.setVisible(false);
			buttonClone.setVisible(false);
			return;
		}

		/* Verzeichnis existiert nicht */
		if (!setup.isLocalFolderExists()) {
			labelLocalFolder.setIcon(Images.GENERAL_OFF.getIcon());
			labelLocalFolder.setText(Language.tr("Git.List.Tab.LocalFolder.LocalFolder.Info.FolderDoesNotExist"));
			buttonInitLocal.setVisible(false);
			buttonStatus.setVisible(false);
			buttonPull.setVisible(false);
			buttonPush.setVisible(false);
			buttonBranches.setVisible(false);
			buttonClone.setVisible(false);
			return;
		}

		/* Verzeichnis existiert, ist aber kein Git-Verzeichnis */
		if (!setup.isLocalFolderGit()) {
			labelLocalFolder.setIcon(Images.GENERAL_SELECT_FOLDER.getIcon());
			labelLocalFolder.setText(Language.tr("Git.List.Tab.LocalFolder.LocalFolder.Info.FolderNotGit"));
			buttonInitLocal.setVisible(true);
			buttonStatus.setVisible(false);
			buttonPull.setVisible(false);
			buttonPush.setVisible(false);
			buttonBranches.setVisible(false);
			buttonClone.setVisible(checkboxUseServer.isSelected());
			return;
		}

		/* Verzeichnis existiert und enthält Git-Daten */
		labelLocalFolder.setIcon(Images.GENERAL_ON.getIcon());
		labelLocalFolder.setText(Language.tr("Git.List.Tab.LocalFolder.LocalFolder.Info.FolderIsGit"));
		buttonInitLocal.setVisible(false);
		buttonStatus.setVisible(true);
		buttonPull.setVisible(checkboxUseServer.isSelected());
		buttonPush.setVisible(checkboxUseServer.isSelected());
		buttonBranches.setVisible(true);
		buttonClone.setVisible(false);
	}

	/**
	 * Befehl: Lokales Repository anlegen
	 */
	private void commandInitLocal() {
		final GitSetup setup=getNewSetup();

		if (!setup.isLocalFolderExists()) {
			MsgBox.error(this,Language.tr("Git.List.Tab.Server.Button.InitLocal"),Language.tr("Git.List.Tab.LocalFolder.LocalFolder.Info.FolderDoesNotExist"));
			return;
		}

		if (setup.isLocalFolderGit()) {
			MsgBox.error(this,Language.tr("Git.List.Tab.Server.Button.InitLocal"),Language.tr("Git.List.Tab.LocalFolder.LocalFolder.Info.FolderIsGit"));
			return;
		}

		if (!MsgBox.confirm(this,Language.tr("Git.List.Tab.Server.Button.InitLocal"),String.format(Language.tr("Git.List.Tab.Server.Button.InitLocal.Confirm"),setup.localFolder),Language.tr("Git.List.Tab.Server.Button.InitLocal.Confirm.InfoYes"),Language.tr("Git.List.Tab.Server.Button.InitLocal.Confirm.InfoNo"))) return;

		final String error=setup.initLocal();
		if (error==null) {
			MsgBox.info(this,Language.tr("Git.List.Tab.Server.Button.InitLocal"),String.format(Language.tr("Git.List.Tab.Server.Button.InitLocal.Success"),setup.localFolder));
		} else {
			MsgBox.error(this,Language.tr("Git.List.Tab.Server.Button.InitLocal"),String.format(Language.tr("Git.List.Tab.Server.Button.InitLocal.Error"),setup.localFolder,error));
		}

		commandTestLocalFolder();
	}

	/**
	 * Befehl: Status des lokalen Repositories anzeigen
	 */
	private void commandStatus() {
		final GitSetup setup=getNewSetup();

		if (!setup.isLocalFolderExists()) {
			MsgBox.error(this,Language.tr("Git.List.Tab.Server.Button.Status"),Language.tr("Git.List.Tab.LocalFolder.LocalFolder.Info.FolderDoesNotExist"));
			return;
		}

		if (!setup.isLocalFolderGit()) {
			MsgBox.error(this,Language.tr("Git.List.Tab.Server.Button.Status"),Language.tr("Git.List.Tab.LocalFolder.LocalFolder.Info.FolderNotGit"));
			return;
		}

		MsgBox.info(this,Language.tr("Git.List.Tab.Server.Button.Status"),"<html><body>"+setup.getRepositoryStatus().replace("\n","<br>")+"</body></html>");
	}

	/**
	 * Befehl: Pull vom Git-Server ausführen
	 */
	private void commandPull() {
		final GitSetup setup=getNewSetup();
		if (!setup.useServer) {
			MsgBox.error(this,Language.tr("Git.List.Pull"),Language.tr("Git.List.Pull.ErrorNoServer"));
			return;
		}

		final String error=(String)GitDialogProcessMonitor.run(this,Language.tr("Git.List.Pull"),progressMonitor->setup.doPull(progressMonitor));
		if (error==null) {
			MsgBox.info(this,Language.tr("Git.List.Pull"),String.format(Language.tr("Git.List.Pull.Success"),setup.serverURL,setup.localFolder));
		} else {
			MsgBox.error(this,Language.tr("Git.List.Pull"),String.format(Language.tr("Git.List.Pull.Error"),setup.serverURL,setup.localFolder,error));
		}
	}

	/**
	 * Befehl: Push vom Git-Server ausführen
	 */
	private void commandPush() {
		final GitSetup setup=getNewSetup();
		if (!setup.useServer) {
			MsgBox.error(this,Language.tr("Git.List.Push"),Language.tr("Git.List.Push.ErrorNoServer"));
			return;
		}

		if (!setup.hasCommitsToPush()) {
			MsgBox.info(this,Language.tr("Git.List.Push"),Language.tr("Git.List.Push.NoCommitsToPush"));
			return;
		}

		final String error=(String)GitDialogProcessMonitor.run(this,Language.tr("Git.List.Push"),progressMonitor->setup.doPushOnly(progressMonitor));
		if (error==null) {
			MsgBox.info(this,Language.tr("Git.List.Push"),String.format(Language.tr("Git.List.Push.Success"),setup.localFolder,setup.serverURL));
		} else {
			MsgBox.error(this,Language.tr("Git.List.Push"),String.format(Language.tr("Git.List.Push.Error"),setup.localFolder,setup.serverURL,error));
		}
	}

	/**
	 * Befehl: Branches auflisten
	 */
	private void commandBranches() {
		final GitSetup setup=getNewSetup();

		final String[] branches=setup.listBranches();
		if (branches==null || branches.length==0) {
			MsgBox.error(this,Language.tr("Git.List.Tab.Server.Button.Branches"),Language.tr("Git.List.Tab.Server.Button.Branches.NoBranches"));
			return;
		}
		new GitBranchesListDialog(this,setup);
	}

	/**
	 * Aktiviert das zu der Auswahl in {@link #comboboxAuth} passende Panel zur
	 * Eingabe der Authentifizierungsdaten.
	 * @see #comboboxAuth
	 * @see #authArea
	 * @see #authAreaLayout
	 */
	private void commandSelectAuthType() {
		authAreaLayout.show(authArea,(String)comboboxAuth.getSelectedItem());
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Schlüsseldatei an.
	 * @see #editAuthKey
	 */
	private void commandSelectKeyFile() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		if (!editAuthKey.getText().isBlank()) fc.setSelectedFile(new File(editAuthKey.getText()));
		fc.setDialogTitle(Language.tr("Git.List.Tab.Server.Auth.Key.SelectKey"));

		if (fc.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		editAuthKey.setText(fc.getSelectedFile().toString());

		commandTestPrivateKey();
	}

	/**
	 * Prüft, ob der in {@link #editAuthKey} angegebene Schlüssel
	 * für die Verwendung mit JGit passend ist.
	 * @see #editAuthKey
	 * @see #labelAuthKey
	 */
	private void commandTestPrivateKey() {
		final GitSetup setup=getNewSetup();

		switch (setup.getPrivateKeyStatus()) {
		case NO_KEY:
			labelAuthKey.setIcon(Images.GENERAL_OFF.getIcon());
			labelAuthKey.setText(Language.tr("Git.List.Tab.Server.Auth.Key.InfoNoKey"));
			break;
		case NO_FILE:
			labelAuthKey.setIcon(Images.GENERAL_OFF.getIcon());
			labelAuthKey.setText(Language.tr("Git.List.Tab.Server.Auth.Key.InfoKeyFilesDoesNotExist"));
			break;
		case WRONG_FORMAT:
			labelAuthKey.setIcon(Images.GENERAL_OFF.getIcon());
			labelAuthKey.setText(Language.tr("Git.List.Tab.Server.Auth.Key.InfoKeyWrongFormat"));
			break;
		case OK:
			labelAuthKey.setIcon(Images.GENERAL_ON.getIcon());
			labelAuthKey.setText(Language.tr("Git.List.Tab.Server.Auth.Key.InfoKeyOk"));
			break;
		default:
			labelAuthKey.setIcon(Images.GENERAL_OFF.getIcon());
			labelAuthKey.setText(Language.tr("Git.List.Tab.Server.Auth.Key.InfoNoKey"));
			break;
		}
	}

	/**
	 * Befehl: Verbindung zum Git-Server prüfen
	 */
	private void commandTestServerConnection() {
		final GitSetup setup=getNewSetup();

		if (setup.serverURL.isBlank()) {
			MsgBox.error(this,Language.tr("Git.List.Tab.Server.Button.Test"),Language.tr("Git.List.Tab.Server.Button.Test.Error.NoServerURL"));
			return;
		}

		final String error=setup.testConnection();
		if (error==null) {
			MsgBox.info(this,Language.tr("Git.List.Tab.Server.Button.Test"),String.format(Language.tr("Git.List.Tab.Server.Button.Test.Success"),setup.serverURL));
		} else {
			MsgBox.error(this,Language.tr("Git.List.Tab.Server.Button.Test"),String.format(Language.tr("Git.List.Tab.Server.Button.Test.Error"),setup.serverURL,error));
		}
	}

	/**
	 * Befehl: Entferntes Repository clonen
	 */
	private void commandClone() {
		final GitSetup setup=getNewSetup();

		if (setup.serverURL.isBlank()) {
			MsgBox.error(this,Language.tr("Git.List.Tab.Server.Button.Clone"),Language.tr("Git.List.Tab.Server.Button.Test.Error.NoServerURL"));
			return;
		}

		final String error=(String)GitDialogProcessMonitor.run(this,Language.tr("Git.List.Tab.Server.Button.Clone"),progressMonitor->setup.doClone(progressMonitor));
		if (error==null) {
			MsgBox.info(this,Language.tr("Git.List.Tab.Server.Button.Clone"),String.format(Language.tr("Git.List.Tab.Server.Button.Clone.Success"),setup.serverURL,setup.localFolder));
		} else {
			MsgBox.error(this,Language.tr("Git.List.Tab.Server.Button.Clone"),String.format(Language.tr("Git.List.Tab.Server.Button.Clone.Error"),setup.serverURL,setup.localFolder,error));
		}
		commandTestLocalFolder();
	}

	/**
	 * Erstellt auf Basis der Dialogeinstellungen eine neue Git-Konfiguration
	 * @return	Neue Git-Konfiguration
	 */
	private GitSetup getNewSetup() {
		final GitSetup setup=new GitSetup();

		/* Lokaler Ordner */
		setup.localFolder=editLocalFolder.getText().trim();

		/* Server */
		setup.useServer=checkboxUseServer.isSelected();
		setup.serverURL=editServerURL.getText().trim();
		switch (comboboxAuth.getSelectedIndex()) {
		case 0: setup.serverAuth=GitSetup.ServerAuthMode.NONE; break;
		case 1: setup.serverAuth=GitSetup.ServerAuthMode.PASSWORD; break;
		case 2: setup.serverAuth=GitSetup.ServerAuthMode.KEY; break;
		}
		setup.authName=editAuthName.getText();
		setup.authPassword=editAuthPassword.getText();
		setup.authKey=editAuthKey.getText();
		setup.authKeyPassphrase=editAuthKeyPassphrase.getText();
		setup.forcePush=checkboxForcePush.isSelected();
		setup.pullOnStart=checkboxPullOnStart.isSelected();

		/* Daten speichern */
		setup.saveData.clear();
		if (checkboxSaveModels.isSelected()) setup.saveData.add(GitSetup.GitSaveMode.MODELS);
		if (checkboxSaveParameterSeries.isSelected()) setup.saveData.add(GitSetup.GitSaveMode.PARAMETER_SERIES);
		if (checkboxSaveOptimizationSetups.isSelected()) setup.saveData.add(GitSetup.GitSaveMode.OPTIMIZATION_SETUPS);
		if (checkboxSaveStatistics.isSelected()) setup.saveData.add(GitSetup.GitSaveMode.STATISTICS);

		return setup;
	}

	@Override
	protected void storeData() {
		gitSetup.copyFrom(getNewSetup());
	}
}