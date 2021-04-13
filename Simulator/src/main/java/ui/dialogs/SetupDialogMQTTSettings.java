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
import tools.SetupData;
import ui.help.Help;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Zeigt einen Dialog zur Konfiguration der MQTT-Klienten-Parameter an
 * (insbesondere im Hinblick auf die MQTT-Benachrichtigungen zum Simulationsende).
 * @author Alexander Herzog
 * @see SetupDialog
 */
public class SetupDialogMQTTSettings extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-8728272958157389182L;

	/**
	 * Konfigurations-Singleton
	 */
	private final SetupData setup;

	/**
	 * Eingabefeld für die Broker-Adresse
	 */
	private final JTextField editBroker;

	/**
	 * Auswahlfeld: Soll das Zertifikat des MQTT-Brokers validiert werden?
	 */
	private final JCheckBox checkCertificate;

	/**
	 * Eingabefeld für den optionalen Nutzername
	 */
	private final JTextField editName;

	/**
	 * Eingabefeld für das optionale Passwort
	 */
	private final JTextField editPassword;

	/**
	 * Eingabefeld für das MQTT-Thema über das Benachrichtigungen gesendet werden sollen
	 */
	private final JTextField editTopic;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public SetupDialogMQTTSettings(final Component owner) {
		super(owner,Language.tr("SettingsDialog.NotifyMQTT.Settings.Title"));
		setup=SetupData.getSetup();

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(this,"Setup"));
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		all.add(content,BorderLayout.CENTER);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		Object[] data;
		JPanel line;

		/* Broker */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SimulationServer.Setup.MQTTBroker")+":",setup.mqttBroker,50);
		content.add((JPanel)data[0]);
		editBroker=(JTextField)data[1];

		/* Zertifikat prüfen? */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(checkCertificate=new JCheckBox(Language.tr("SimulationServer.Setup.MQTTBrokerVerify"),setup.mqttVerifyCertificates));
		checkCertificate.setToolTipText(Language.tr("SimulationServer.Setup.MQTTBrokerVerify.Hint"));

		/* Nutzername */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SimulationServer.Setup.AuthName")+":",setup.serverAuthName,50);
		content.add((JPanel)data[0]);
		editName=(JTextField)data[1];

		/* Passwort */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SimulationServer.Setup.AuthPassword")+":",setup.serverAuthPassword,50);
		content.add((JPanel)data[0]);
		editPassword=(JTextField)data[1];

		/* Topic */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SimulationServer.Setup.MQTTNotifyTopic")+":",setup.notifyMQTTTopic,50);
		content.add((JPanel)data[0]);
		editTopic=(JTextField)data[1];

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected void storeData() {
		setup.mqttBroker=editBroker.getText().trim();
		setup.mqttVerifyCertificates=checkCertificate.isSelected();
		setup.serverAuthName=editName.getText().trim();
		setup.serverAuthPassword=editPassword.getText().trim();
		setup.notifyMQTTTopic=editTopic.getText().trim();
	}
}
