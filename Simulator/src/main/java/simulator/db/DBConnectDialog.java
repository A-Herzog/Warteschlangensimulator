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
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import language.Language;
import systemtools.BaseDialog;
import ui.help.Help;
import ui.images.Images;

/**
 * Dialog zur Prüfung der Verbindungseinstellungen zu einer Datenbank
 * @author Alexander Herzog
 */
public class DBConnectDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3330178091897338877L;

	/** Datenbankeinstellungs-Objekt */
	private final DBSettings settings;

	/** Konfigurationspanel */
	private final DBSettingsPanel settingsPanel;

	/** Ausgabebereich */
	private final JTextArea output;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public DBConnectDialog(final Component owner) {
		super(owner,Language.tr("Surface.Database.TestDialog.Title"),false);

		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(this,"DBTest"));
		content.setLayout(new BorderLayout());

		/* Konfiguration */

		content.add(settingsPanel=new DBSettingsPanel(settings=new DBSettings(),false),BorderLayout.NORTH);

		/* Abfrage */

		JPanel main, line;
		JButton button;

		content.add(main=new JPanel(new BorderLayout()));

		main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(button=new JButton(Language.tr("Surface.Database.TestDialog.TestButton")));
		button.setIcon(Images.EXTRAS_DATABASE_TEST.getIcon());
		button.addActionListener(e->runTest());

		/* Ausgabe */

		main.add(new JScrollPane(output=new JTextArea()),BorderLayout.CENTER);

		/* Vorbereiten und anzeigen */

		setMinSizeRespectingScreensize(600,400);
		setSizeRespectingScreensize(600,400);
		setResizable(true);
		setVisible(true);
	}

	/**
	 * Führt den Test mit den angegebenen Parametern durch.
	 */
	private void runTest() {
		output.setText("");

		settingsPanel.storeData();
		final StringBuilder sb=new StringBuilder();

		try (DBConnect connect=new DBConnect(settings,false)) {
			if (connect.getInitError()!=null) sb.append(connect.getInitError()); else {
				sb.append(Language.tr("Surface.Database.TestDialog.Info.Connection")+"\n");
				final String[] list=connect.listTables();
				if (list.length==0) {
					sb.append(Language.tr("Surface.Database.TestDialog.Info.NoTables")+"\n");
				} else {
					if (list.length==1) {
						sb.append(Language.tr("Surface.Database.TestDialog.Info.OneTable")+"\n");
						sb.append(list[0]);
					} else {
						sb.append(String.format(Language.tr("Surface.Database.TestDialog.Info.MultipleTables"),list.length)+"\n");
						sb.append(String.join("\n",list));
					}
				}
			}
		}
		output.setText(sb.toString());
	}
}
