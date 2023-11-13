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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.distribution.swing.JOpenURL;
import simulator.editmodel.EditModelProcessor;
import systemtools.BaseDialog;
import systemtools.SetupBase;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.UpdateSystem;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Zeigt einen Dialog mit Datenschutzeinstellungen an.
 * @author Alexander Herzog
 */
public class DataPrivacyDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-8968273387193313846L;

	/**
	 * Setup-Singleton
	 */
	private final SetupData setup;

	/** Autorenname für neue Modelle */
	private final JTextField defaultUserName;

	/** Autoren-E-Mail-Adresse für neue Modelle */
	private final JTextField defaultUserEMail;

	/** Zuletzt verwendete Dateien merken? */
	private final JCheckBox useLastFiles;

	/** Häufig verwendete Folgestationen merken? */
	private final JCheckBox useNextStationData;

	/** Modelle mit Skripten signieren */
	private final JCheckBox signModels;

	/** Aufruf von Internetadressen */
	private final JComboBox<String> allowToOpenLinks;

	/** Programm automatisch aktualisieren */
	private final JComboBox<String> autoUpdate;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public DataPrivacyDialog(final Component owner) {
		super(owner,Language.tr("DataPrivacy.Title"));
		setup=SetupData.getSetup();

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(this,"DataPrivacy"));
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		all.add(content,BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;
		Object[] data;
		JButton button;

		/*
		 * Bereich:
		 * Lokale Einstellungen
		 */
		addHeading(content,Language.tr("DataPrivacy.LocalSettings"));

		/* Autorenname für neue Modelle */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.Tabs.DefaultUserName.Name")+":","");
		content.add(line=(JPanel)data[0]);
		defaultUserName=(JTextField)data[1];
		line.add(button=new JButton(Language.tr("SettingsDialog.Tabs.DefaultUserName.Reset")),BorderLayout.EAST);
		button.setIcon(Images.MODELPROPERTIES_DESCRIPTION_SET_AUTHOR.getIcon());
		final String displayUserName=SetupBase.getDisplayUserName();
		button.setToolTipText(String.format(Language.tr("SettingsDialog.Tabs.DefaultUserName.Reset.Info"),displayUserName));
		button.addActionListener(e->defaultUserName.setText(displayUserName));
		defaultUserName.setText(setup.defaultUserName);

		/* Autoren-E-Mail-Adresse für neue Modelle */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.Tabs.DefaultUserName.EMail")+":","");
		content.add(line=(JPanel)data[0]);
		defaultUserEMail=(JTextField)data[1];
		defaultUserEMail.setText(setup.defaultUserEMail);

		/* Zuletzt verwendete Dateien merken? */
		addLine(content).add(useLastFiles=new JCheckBox(Language.tr("SettingsDialog.UseLastFiles")));
		useLastFiles.setSelected(setup.useLastFiles);

		/* Häufig verwendete Folgestationen merken? */
		addLine(content).add(useNextStationData=new JCheckBox(Language.tr("SettingsDialog.UseNextStationData")));
		useNextStationData.setSelected(setup.collectNextStationData);

		/* Modelle mit Skripten signieren */
		addLine(content).add(signModels=new JCheckBox(Language.tr("SettingsDialog.ModellSecurity.SignModels")));
		signModels.setSelected(setup.signModels);

		/*
		 * Bereich:
		 * Internet
		 */
		addHeading(content,Language.tr("DataPrivacy.Internet"));

		/* Aufruf von Internetadressen */
		line=addLine(content);
		line.add(label=new JLabel(Language.tr("SettingsDialog.ModellSecurity.AllowOpenLinks")+":"));
		line.add(allowToOpenLinks=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.ModellSecurity.AllowOpenLinks.Ask"),
				Language.tr("SettingsDialog.ModellSecurity.AllowOpenLinks.Prohibited")
		}));
		allowToOpenLinks.setRenderer(new IconListCellRenderer(new Images[]{
				Images.HELP,
				Images.GENERAL_LOCK_CLOSED
		}));
		label.setLabelFor(allowToOpenLinks);
		allowToOpenLinks.setSelectedIndex(setup.allowToOpenLinks?0:1);

		/* Programm automatisch aktualisieren */
		line=addLine(content);
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

		/* Link zur GitHub-Datenschutz-Seite */
		addLine(content).add(new JLabel("<html><body>"+Language.tr("DataPrivacy.GitHub.Info")+"</body></html>"));
		addLine(content).add(label=new JLabel("<html><body><a href=\""+Language.tr("DataPrivacy.GitHub.DataPrivavy.URL")+"\">"+Language.tr("DataPrivacy.GitHub.DataPrivavy")+"</a></body></html>"));
		label.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) JOpenURL.open(DataPrivacyDialog.this,Language.tr("DataPrivacy.GitHub.DataPrivavy.URL"));
			}
		});

		/* Dialog starten */
		setMinSizeRespectingScreensize(700,0);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Fügt eine Zeile zu dem Dialog hinzu.
	 * @param content	Panel in das die Zeile eingefügt werden soll
	 * @return	Neue Zeile (bereits in den Dialog eingefügt)
	 */
	protected final JPanel addLine(final JPanel content) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		return line;
	}

	/**
	 * Erstellt eine neue Überschrift
	 * @param content	Panel in das die Überschrift eingefügt werden soll
	 * @param title	Überschriftzeile
	 */
	protected void addHeading(final JPanel content, final String title) {
		if (content.getComponentCount()>0) content.add(Box.createVerticalStrut(15));
		addLine(content).add(new JLabel("<html><body><b>"+title+"</b></body></html>"));
	}

	@Override
	protected void storeData() {
		setup.defaultUserName=defaultUserName.getText().trim();
		setup.defaultUserEMail=defaultUserEMail.getText().trim();
		setup.useLastFiles=useLastFiles.isSelected();
		setup.collectNextStationData=useNextStationData.isSelected();
		setup.signModels=signModels.isSelected();
		setup.allowToOpenLinks=allowToOpenLinks.getSelectedIndex()==0;
		switch (autoUpdate.getSelectedIndex()) {
		case 0: setup.autoUpdate=SetupData.AutoUpdate.OFF; break;
		case 1: setup.autoUpdate=SetupData.AutoUpdate.SEARCH; break;
		case 2: setup.autoUpdate=SetupData.AutoUpdate.INSTALL; break;
		}

		if (!setup.useLastFiles) {
			setup.lastFiles=new String[0];
		}

		if (!setup.collectNextStationData) {
			EditModelProcessor.getInstance().reset();
		}
	}
}
