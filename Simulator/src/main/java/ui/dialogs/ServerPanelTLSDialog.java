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

import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.distribution.swing.PlugableFileChooser;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.SetupData;
import ui.MainFrame;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.tools.ServerPanel;

/**
 * Zeigt den Dialog zum Bearbeiten der TLS-Einstellungen an.
 * @author Alexander Herzog
 * @see ServerPanel
 */
public class ServerPanelTLSDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1559764457199767696L;

	/**
	 * Setup-Objekt zum Laden und Speichern von Daten
	 */
	private final SetupData setup;

	/**
	 * Eingabefeld für die Key-Store-Datei
	 */
	private final JTextField	editFile;

	/**
	 * Eingabefeld für das Passwort für die Key-Store-Datei
	 */
	private final JTextField	editPassword;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public ServerPanelTLSDialog(final Component owner) {
		super(owner,Language.tr("SimulationServer.Setup.TLSInfo.Title"));

		final JPanel content=createGUI(()->Help.topicModal(this,"SimulationServer"));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		setup=SetupData.getSetup();
		Object[] data;
		JPanel line;
		JButton button;

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("SimulationServer.Setup.TLSInfo.DialogInfo")));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("SimulationServer.Setup.TLSInfo.KeyStoreFile")+":",setup.serverTLSKeyStoreFile,50);
		content.add(line=(JPanel)data[0]);
		editFile=(JTextField)data[1];
		line.add(button=new JButton(Images.GENERAL_SELECT_FILE.getIcon()));
		button.setToolTipText(Language.tr("SimulationServer.Setup.TLSInfo.KeyStoreFile.Hint"));
		button.addActionListener(e->selectFile());

		line.add(button=new JButton(Images.MODEL_GENERATOR.getIcon()));
		button.setToolTipText(Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore.Hint"));
		button.addActionListener(e->generateKeyStore());

		data=ModelElementBaseDialog.getInputPanel(Language.tr("SimulationServer.Setup.TLSInfo.KeyStorePassword")+":",setup.serverTLSKeyStorePassword,50);
		content.add(line=(JPanel)data[0]);
		editPassword=(JTextField)data[1];

		/* Dialog starten */
		setMinSizeRespectingScreensize(500,0);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Key-Store-Datei an.
	 * @see #editFile
	 */
	private void selectFile() {
		File oldFile=new File(editFile.getText());
		File initialDirectory=oldFile.getParentFile();

		final var fc=new PlugableFileChooser(initialDirectory,true);
		fc.setDialogTitle(Language.tr("SimulationServer.Setup.TLSInfo.KeyStoreFile"));

		final File file=fc.showSaveDialogFileWithExtension(this);
		if (file==null) return;
		editFile.setText(file.toString());
	}

	@Override
	protected void storeData() {
		setup.serverTLSKeyStoreFile=editFile.getText().trim();
		setup.serverTLSKeyStorePassword=editPassword.getText().trim();
		setup.saveSetup();
	}

	/**
	 * Erzeugt eine KeyStore-Datei mit dem angegebenen Namen und dem angegebenen Passwort.
	 * Die KeyStore-Datei enthält ein selbstsigniertes Zertifikat für den Webserver.
	 */
	private void generateKeyStore() {
		/* KeyStore-Datei bestimmen */
		if (editFile.getText().isBlank()) {
			MsgBox.error(this,Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore"),Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore.ErrorNoKeyStoreFile"));
			return;
		}
		final File keyStoreFile=new File(editFile.getText());
		if (keyStoreFile.exists()) {
			MsgBox.error(this,Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore"),String.format(Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore.ErrorKeyStoreFileExists"),keyStoreFile.toString()));
			return;
		}

		final String passwd=editPassword.getText().trim();
		if (passwd.length()<6) {
			MsgBox.error(this,Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore"),Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore.ErrorPasswordTooShort"));
			return;
		}
		if (passwd.chars().filter(i->(i<32 || i>127)).findFirst().isPresent()) {
			MsgBox.error(this,Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore"),Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore.ErrorPasswordASCII"));
			return;
		}

		/* KeyTool finden */
		final File keyToolFolder=new File(System.getProperty("java.home"),"bin");
		File keyToolFile=new File(keyToolFolder,"keytool.exe");
		if (!keyToolFile.isFile()) keyToolFile=new File(keyToolFolder,"keytool");
		if (!keyToolFile.isFile()) {
			MsgBox.error(this,Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore"),Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore.ErrorKeyToolNotFound"));
			return;
		}

		/* KeyTool-Befehl zusammensetzen */
		final List<String> commandLine=new ArrayList<>();
		commandLine.add(keyToolFile.toString());
		commandLine.add("-genkey");
		commandLine.add("-noprompt");
		commandLine.add("-keyalg");
		commandLine.add("RSA");
		commandLine.add("-alias");
		commandLine.add("selfsigned");
		commandLine.add("-keystore"); commandLine.add(keyStoreFile.toString());
		commandLine.add("-storepass"); commandLine.add(passwd);
		commandLine.add("-keysize"); commandLine.add("4096");
		commandLine.add("-ext"); commandLine.add("SAN=DNS:localhost,IP:127.0.0.1");
		commandLine.add("-dname"); commandLine.add("\"CN="+MainFrame.PROGRAM_NAME+", OU=, O=, L=, S=, C=\"");
		commandLine.add("-validity"); commandLine.add("365");

		/* KeyTool ausführen */
		try {
			final Process p=Runtime.getRuntime().exec(commandLine.toArray(String[]::new));
			if (p==null) {
				MsgBox.error(this,Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore"),Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore.ExecuteError"));
				return;
			}
			p.waitFor();
			/* try (InputStream stream=p.getInputStream()) {(new BufferedReader(new InputStreamReader(stream))).lines().forEach(System.out::println);	} */
		} catch (IOException | InterruptedException e) {
			MsgBox.error(this,Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore"),Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore.ExecuteError"));
			return;
		}

		MsgBox.info(this,Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore"),Language.tr("SimulationServer.Setup.TLSInfo.GenerateKeyStore.Success"));
	}
}
