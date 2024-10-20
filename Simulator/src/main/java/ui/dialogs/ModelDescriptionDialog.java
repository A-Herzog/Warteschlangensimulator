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
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilderStyled;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilderTable;
import ui.modeleditor.descriptionbuilder.StyledTextBuilder;

/**
 * In diesem Dialog wird eine Beschreibung des Modells angezeigt und es wird angeboten,
 * die Beschreibung in verschiedenen Formaten zu speichern
 * @author Alexander Herzog
 * @see ModelDescriptionBuilder
 */
public class ModelDescriptionDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6362465944821402994L;

	/**
	 * Erzeugt eine Beschreibungen f�r das Modell und stellt diese in formatierter Form in verschiedenen Formaten zur Verf�gung.
	 */
	private final ModelDescriptionBuilderStyled descriptionBuilder;

	/**
	 * Kopfdaten f�r die Ausgabe der Beschreibung im HTML-Format.
	 * @see #saveToFile()
	 */
	private static final String HTML_HEADER=
			"<!DOCTYPE html>\n"+
					"<html>\n"+
					"<head>\n"+
					"  <meta charset=\"utf-8\">\n"+
					"  <meta name=\"author\" content=\"Alexander Herzog\">\n"+
					"  <style type=\"text/css\">\n"+
					"  body {font-family: Verdana, Lucida, sans-serif; background-color: #FFFFF3; margin: 2px;}\n"+
					"  table {border-collapse: collapse;}\n"+
					"  table td {border: 1px solid gray; padding: 2px 5px;}\n"+
					"  </style>\n"+
					"</head>\n"+
					"<body>\n\n";

	/**
	 * Fu�daten f�r die Ausgabe der Beschreibung im HTML-Format.
	 * @see #saveToFile()
	 */
	private static final String HTML_FOOTER="\n</body></html>";

	/**
	 * Editor-Modell zu dem die Beschreibung generiert werden soll
	 */
	private final EditModel model;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param model	Editor-Modell zu dem die Beschreibung generiert werden soll
	 */
	public ModelDescriptionDialog(final Component owner, final EditModel model) {
		super(owner,Language.tr("ModelDescription.Dialog.Title"));
		this.model=model;
		showCloseButton=true;

		/* GUI aufbauen */
		addUserButton(Language.tr("ModelDescription.Dialog.Copy"),Images.EDIT_COPY.getIcon());
		addUserButton(Language.tr("ModelDescription.Dialog.Save"),Images.GENERAL_SAVE.getIcon());
		final JPanel all=createGUI(()->Help.topicModal(this,"EditorDescriptionDialog"));
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.globalModelDescription);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		final JTextPane textPane=new JTextPane();
		textPane.setEditable(false);
		textPane.setBackground(new Color(0xFF,0xFF,0xF8));

		descriptionBuilder=new ModelDescriptionBuilderStyled(model);
		descriptionBuilder.run();
		descriptionBuilder.writeToTextPane(textPane);

		content.add(new JScrollPane(textPane),BorderLayout.CENTER);
		textPane.setSelectionStart(0);
		textPane.setSelectionEnd(0);

		/* Dialog vorbereiten */
		setMinSizeRespectingScreensize(700,900);
		setSizeRespectingScreensize(700,900);
		setResizable(true);
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Speichert einen Text in einer Datei und gibt im
	 * Fehlerfall eine Fehlermeldung aus.
	 * @param text	Zu speichernder Text
	 * @param file	Ausgabedatei
	 */
	private void saveTextToFile(final String text, final File file) {
		try {
			if (file.isFile()) {
				if (!file.delete()) {
					MsgBox.error(this,Language.tr("ModelDescription.Dialog.Save.Failed.Title"),String.format(Language.tr("ModelDescription.Dialog.Save.Failed.Info"),file.toString()));
					return;
				}
			}
			Files.write(file.toPath(),text.getBytes(StandardCharsets.UTF_8),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			MsgBox.error(this,Language.tr("ModelDescription.Dialog.Save.Failed.Title"),String.format(Language.tr("ModelDescription.Dialog.Save.Failed.Info"),file.toString()));
		}
	}

	/**
	 * Zeigt einen Dateiauswahldialog an und speichert dann,
	 * sofern die Auswahl nicht abgebrochen wurde, die Beschreibung
	 * in der angegebenen Datei.
	 * @see #saveTextToFile(String, File)
	 */
	private void saveToFile() {
		final File file=StyledTextBuilder.getSaveFile(this,Language.tr("ModelDescription.Dialog.Save.Title"));
		if (file==null) return;

		if (file.getName().toLowerCase().endsWith(".txt")) {
			saveTextToFile(descriptionBuilder.getText(),file);
		}

		if (file.getName().toLowerCase().endsWith(".rtf")) {
			saveTextToFile(descriptionBuilder.getRTFText(),file);
		}

		if (file.getName().toLowerCase().endsWith(".html")) {
			final ModelDescriptionBuilderTable descriptionBuilderTable=new ModelDescriptionBuilderTable(model);
			descriptionBuilderTable.run();
			saveTextToFile(HTML_HEADER+descriptionBuilderTable.getHTML()+HTML_FOOTER,file);
		}

		if (file.getName().toLowerCase().endsWith(".md")) {
			final ModelDescriptionBuilderTable descriptionBuilderTable=new ModelDescriptionBuilderTable(model);
			descriptionBuilderTable.run();
			saveTextToFile(descriptionBuilderTable.getMD(),file);
		}

		if (file.getName().toLowerCase().endsWith(".docx")) {
			if (!descriptionBuilder.saveDOCX(file,true)) {
				MsgBox.error(this,Language.tr("ModelDescription.Dialog.Save.Failed.Title"),String.format(Language.tr("ModelDescription.Dialog.Save.Failed.Info"),file.toString()));
			}
		}

		if (file.getName().toLowerCase().endsWith(".odt")) {
			if (!descriptionBuilder.saveODT(file)) {
				MsgBox.error(this,Language.tr("ModelDescription.Dialog.Save.Failed.Title"),String.format(Language.tr("ModelDescription.Dialog.Save.Failed.Info"),file.toString()));
			}
		}

		if (file.getName().toLowerCase().endsWith(".pdf")) {
			if (!descriptionBuilder.savePDF(this,file,true)) {
				MsgBox.error(this,Language.tr("ModelDescription.Dialog.Save.Failed.Title"),String.format(Language.tr("ModelDescription.Dialog.Save.Failed.Info"),file.toString()));
			}
		}
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		switch (nr) {
		case 0: descriptionBuilder.copyToClipboard(); break;
		case 1: saveToFile(); break;
		}
	}
}