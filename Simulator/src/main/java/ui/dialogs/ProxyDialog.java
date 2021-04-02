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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.SetupData;
import ui.help.Help;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Ermöglicht es, die Proxy-Einstellungen anzupassen.
 * @author Alexander Herzog
 */
public class ProxyDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6839899857734552923L;

	/** Auswahl: Keinen Proxy verwenden */
	private final JRadioButton proxyModeOff;
	/** Auswahl: Proxy verwenden */
	private final JRadioButton proxyModeOn;
	/** Name des Proxy-Servers */
	private final JTextField proxyHost;
	/** Port des Proxy-Servers */
	private final JTextField proxyPort;
	/** Muss sich der Client gegenüber dem Proxy-Servers authentifizieren? */
	private final JCheckBox useAuthentification;
	/** Name des Clienten */
	private final JTextField proxyUser;
	/** Passwort für den Clienten */
	private final JTextField proxyPassword;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public ProxyDialog(final Component owner) {
		super(owner,Language.tr("ProxySettings.Title"));

		/* Dialog erstellen */

		final JPanel content=createGUI(600,200,()->Help.topicModal(this,"Proxy"));
		content.setLayout(new BorderLayout());
		final JPanel main=new JPanel();
		main.setLayout(new BoxLayout(main,BoxLayout.PAGE_AXIS));
		content.add(main,BorderLayout.NORTH);

		final SetupData setup=SetupData.getSetup();

		JPanel line;
		Object[] data;

		/* Dialogfelder anlegen */

		main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(proxyModeOff=new JRadioButton(Language.tr("ProxySettings.ProxyOff"),!setup.useProxy));

		main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(proxyModeOn=new JRadioButton(Language.tr("ProxySettings.ProxyOn"),setup.useProxy));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ProxySettings.ProxyHost")+":",setup.proxyHost);
		main.add((JPanel)data[0]);
		proxyHost=(JTextField)data[1];

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ProxySettings.ProxyPort")+":",""+setup.proxyPort);
		main.add((JPanel)data[0]);
		proxyPort=(JTextField)data[1];

		main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useAuthentification=new JCheckBox(Language.tr("ProxySettings.UseAuthentification"),!setup.proxyUser.trim().isEmpty() || !setup.proxyPassword.trim().isEmpty()));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ProxySettings.ProxyUser")+":",""+setup.proxyUser);
		main.add((JPanel)data[0]);
		proxyUser=(JTextField)data[1];

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ProxySettings.ProxyPassword")+":",""+setup.proxyPassword);
		main.add((JPanel)data[0]);
		proxyPassword=(JTextField)data[1];

		/* Dialog-Aktionen einrichten */

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(proxyModeOff);
		buttonGroup.add(proxyModeOn);

		proxyModeOff.addActionListener(e->checkData(false));
		proxyModeOn.addActionListener(e->checkData(false));
		proxyHost.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {proxyModeOn.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {proxyModeOn.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {proxyModeOn.setSelected(true); checkData(false);}
		});
		proxyPort.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {proxyModeOn.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {proxyModeOn.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {proxyModeOn.setSelected(true); checkData(false);}
		});
		proxyUser.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {useAuthentification.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {useAuthentification.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {useAuthentification.setSelected(true); checkData(false);}
		});
		proxyPassword.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {useAuthentification.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {useAuthentification.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {useAuthentification.setSelected(true); checkData(false);}
		});

		/* Dialog starten */

		setMinSizeRespectingScreensize(600,0);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		if (proxyModeOff.isSelected()) {
			proxyHost.setBackground(NumberTools.getTextFieldDefaultBackground());
			proxyPort.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			if (proxyHost.getText().trim().isEmpty()) {
				ok=false;
				proxyHost.setBackground(Color.RED);
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("ProxySettings.ProxyHost.NoHostTitle"),Language.tr("ProxySettings.ProxyHost.NoHostInfo"));
					return false;
				}
			} else {
				proxyHost.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
			final Long L=NumberTools.getPositiveLong(proxyPort,true);
			if (L==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("ProxySettings.ProxyPort.ErrorTitle"),String.format(Language.tr("ProxySettings.ProxyPort.ErrorInfo"),proxyPort.getText().trim()));
					return false;
				}
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		final SetupData setup=SetupData.getSetup();

		setup.useProxy=proxyModeOn.isSelected();
		setup.proxyHost=proxyHost.getText().trim();
		final Long L=NumberTools.getLong(proxyPort,true);
		setup.proxyPort=(L==null)?8080:L.intValue();
		if (useAuthentification.isSelected()) {
			setup.proxyUser=proxyUser.getText().trim();
			setup.proxyPassword=proxyPassword.getText().trim();
		} else {
			setup.proxyUser="";
			setup.proxyPassword="";
		}

		setup.saveSetup();
	}

}
