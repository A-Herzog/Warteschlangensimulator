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

import java.io.Serializable;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import language.Language;
import simulator.editmodel.EditModelCertificateStore;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.images.Images;

/**
 * Dialogseite "Sicherheit" im Programmeinstellungen-Dialog
 * @author Alexander Herzog
 * @see SetupData
 */
public class SetupDialogPageSecurity extends SetupDialogPage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-6078682517244209962L;

	/* Bereich: Modelle */

	/** Verhalten beim Landen von Modellen mit Sicherheitsproblemen */
	private final JComboBox<String> modelSecurity;
	/** Modelle mit Skripten signieren */
	private final JCheckBox signModels;
	/** Externe Programme via Skript startbar? */
	private final JCheckBox modelSecurityExternal;

	/* Bereich: Internet */

	/** Aufruf von Internetadressen */
	private final JComboBox<String> allowToOpenLinks;

	/**
	 * Konstruktor der Klasse
	 */
	public SetupDialogPageSecurity() {
		JPanel line;
		JLabel label;
		JButton button;
		JToolBar toolbar;

		/*
		 * Bereich:
		 * Modelle
		 */
		addHeading(Language.tr("SettingsDialog.ModellSecurity.ModelsSecurity"));

		/* Verhalten beim Landen von Modellen mit Sicherheitsproblemen */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.ModellSecurity")+":"));
		line.add(modelSecurity=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.ModellSecurity.AllowAll"),
				Language.tr("SettingsDialog.ModellSecurity.Ask"),
				Language.tr("SettingsDialog.ModellSecurity.Strict")
		}));
		modelSecurity.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_LOCK_OPEN,
				Images.HELP,
				Images.GENERAL_LOCK_CLOSED
		}));
		label.setLabelFor(modelSecurity);

		/* Modelle mit Skripten signieren */
		addLine().add(signModels=new JCheckBox(Language.tr("SettingsDialog.ModellSecurity.SignModels")));

		/* Gespeicherte Zertifikate löschen? */
		line.add(toolbar=new JToolBar());
		toolbar.setFloatable(false);
		toolbar.add(button=new JButton());
		button.setToolTipText(Language.tr("SettingsDialog.ModellSecurity.Hint"));
		button.setIcon(Images.HELP.getIcon());
		button.addActionListener(e->MsgBox.info(this,Language.tr("SettingsDialog.ModellSecurity"),Language.tr("SettingsDialog.ModellSecurity.Info")));
		if (EditModelCertificateStore.certificateFileExists()) {
			final EditModelCertificateStore certStore=new EditModelCertificateStore();
			if (certStore.getTrustedPublicKeys().size()>0) {
				toolbar.add(Box.createHorizontalStrut(10));
				final JButton clearCertificatesButton=new JButton(Language.tr("SettingsDialog.ModellSecurity.ClearTrustedUserList"),Images.GENERAL_TRASH.getIcon());
				clearCertificatesButton.setToolTipText(Language.tr("SettingsDialog.ModellSecurity.ClearTrustedUserList.Info"));
				toolbar.add(clearCertificatesButton);
				clearCertificatesButton.addActionListener(e->{
					certStore.clearTrustedPublicKeys();
					clearCertificatesButton.setEnabled(false);
				});
			}
		}

		/* Externe Programme via Skript startbar? */
		line=addLine();
		line.add(modelSecurityExternal=new JCheckBox(Language.tr("SettingsDialog.ModellSecurityExternal")));
		line.add(toolbar=new JToolBar());
		toolbar.setFloatable(false);
		toolbar.add(button=new JButton());
		button.setToolTipText(Language.tr("SettingsDialog.ModellSecurityExternal.Hint"));
		button.setIcon(Images.HELP.getIcon());
		button.addActionListener(e->MsgBox.info(this,Language.tr("SettingsDialog.ModellSecurityExternal"),Language.tr("SettingsDialog.ModellSecurityExternal.Info")));

		/*
		 * Bereich:
		 * Internet
		 */
		addHeading(Language.tr("SettingsDialog.ModellSecurity.Internet"));

		/* Aufruf von Internetadressen */
		line=addLine();
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
	}

	@Override
	public String getTitle() {
		return Language.tr("SettingsDialog.Tabs.Security");
	}

	@Override
	public Icon getIcon() {
		return Images.SETUP_PAGE_SECURITY.getIcon();
	}

	@Override
	public void loadData() {
		switch (setup.modelSecurity) {
		case ALLOWALL: modelSecurity.setSelectedIndex(0); break;
		case ASK: modelSecurity.setSelectedIndex(1); break;
		case STRICT: modelSecurity.setSelectedIndex(2); break;
		}

		signModels.setSelected(setup.signModels);

		modelSecurityExternal.setSelected(setup.modelSecurityAllowExecuteExternal);

		allowToOpenLinks.setSelectedIndex(setup.allowToOpenLinks?0:1);
	}

	@Override
	public void storeData() {
		switch (modelSecurity.getSelectedIndex()) {
		case 0: setup.modelSecurity=SetupData.ModelSecurity.ALLOWALL; break;
		case 1: setup.modelSecurity=SetupData.ModelSecurity.ASK; break;
		case 2: setup.modelSecurity=SetupData.ModelSecurity.STRICT; break;
		}

		setup.signModels=signModels.isSelected();

		setup.modelSecurityAllowExecuteExternal=modelSecurityExternal.isSelected();

		setup.allowToOpenLinks=allowToOpenLinks.getSelectedIndex()==0;
	}

	@Override
	public void resetSettings() {
		modelSecurity.setSelectedIndex(1);
		signModels.setSelected(true);
		modelSecurityExternal.setSelected(false);
	}
}