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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.Table;
import mathtools.distribution.swing.PlugableFileChooser;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorPanel;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementOutputJS}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementOutputJS
 */
public class ModelElementOutputJSDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2395379269131512946L;

	/**
	 * Checkbox: Ausgabe aktiv?
	 */
	private JCheckBox outputActive;

	/**
	 * Eingabefeld für den Dateinamen der Ausgabedatei
	 */
	private JTextField fileNameEdit;

	/**
	 * Soll eine möglicherweise bestehende Datei beim Start der Ausgabe überschrieben werden? (Ansonsten wird angehängt)
	 */
	private JCheckBox fileOverwrite;

	/**
	 * Skripteditor
	 */
	private ScriptEditorPanel editor;

	/**
	 * Checkbox: Überschriften-Skript verwenden?
	 */
	private JCheckBox useHeadingScript;

	/**
	 * Skripteditor für Überschriftenzeile
	 */
	private ScriptEditorPanel editorHeading;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementOutputJS}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementOutputJSDialog(final Component owner, final ModelElementOutputJS element, final boolean readOnly) {
		super(owner,Language.tr("Surface.OutputJS.Dialog.Title"),element,"ModelElementOutputJS",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationOutputJS;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		JPanel line;

		if (element instanceof ModelElementOutputJS) {
			final ModelElementOutputJS output=(ModelElementOutputJS)element;

			/* Konfigurationsbereich oben */
			final JPanel upperPanel=new JPanel();
			upperPanel.setLayout(new BoxLayout(upperPanel,BoxLayout.PAGE_AXIS));
			content.add(upperPanel,BorderLayout.NORTH);

			/* Aktiv? */
			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(outputActive=new JCheckBox("<html><body><b>"+Language.tr("Surface.OutputJS.Dialog.OutputActive")+"</b></body></html>",output.isOutputActive()));

			/* Eingabefeld: Dateiname */
			final Object[] data=getInputPanel(Language.tr("Surface.Output.Dialog.FileName")+":",output.getOutputFile());
			line=(JPanel)data[0];
			fileNameEdit=(JTextField)data[1];
			upperPanel.add(line);
			fileNameEdit.setEnabled(!readOnly);
			final JButton button=new JButton();
			button.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
			button.setToolTipText(Language.tr("Surface.Output.Dialog.FileName.Select"));
			button.addActionListener(e->selectFile());
			button.setEnabled(!readOnly);
			line.add(button,BorderLayout.EAST);

			/* Checkbox: Überschreiben? */
			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(fileOverwrite=new JCheckBox(Language.tr("Surface.Output.Dialog.Overwrite"),output.isOutputFileOverwrite()));
			fileOverwrite.setToolTipText(Language.tr("Surface.Output.Dialog.Overwrite.Info"));

			/* Tabs in der Mitte */
			final JTabbedPane tabs=new JTabbedPane();
			content.add(tabs,BorderLayout.CENTER);
			JPanel tab;

			/* Tab: Ausgabedaten */
			tabs.addTab(Language.tr("Surface.Output.Dialog.Tab.OutputData"),tab=new JPanel(new BorderLayout()));
			final String script=output.getScript();
			ScriptEditorPanel.ScriptMode mode;
			switch (output.getMode()) {
			case Javascript: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			case Java: mode=ScriptEditorPanel.ScriptMode.Java; break;
			default: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			}
			tab.add(editor=new ScriptEditorPanel(script,mode,readOnly,Language.tr("Surface.SetJS.Dialog.Script"),element.getModel(),helpRunnable,ScriptEditorPanel.featuresClientStationOutput),BorderLayout.CENTER);

			/* Tab: Überschriften */
			tabs.addTab(Language.tr("Surface.Output.Dialog.Tab.Headings"),tab=new JPanel(new BorderLayout()));
			tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
			line.add(useHeadingScript=new JCheckBox("<html><body><b>"+Language.tr("Surface.Output.Dialog.Tab.Headings.Active")+"</b></body></html>",output.isUseHeadingScript()));
			final String scriptHeading=output.getScriptHeading();
			ScriptEditorPanel.ScriptMode modeHeading;
			switch (output.getModeHeading()) {
			case Javascript: modeHeading=ScriptEditorPanel.ScriptMode.Javascript; break;
			case Java: modeHeading=ScriptEditorPanel.ScriptMode.Java; break;
			default: modeHeading=ScriptEditorPanel.ScriptMode.Javascript; break;
			}
			tab.add(editorHeading=new ScriptEditorPanel(scriptHeading,modeHeading,readOnly,Language.tr("Surface.SetJS.Dialog.ScriptHeading"),element.getModel(),helpRunnable,ScriptEditorPanel.featuresClientStationOutput),BorderLayout.CENTER);

			/* Icons auf den Tabs */
			tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_OUTPUT.getIcon());
			tabs.setIconAt(1,Images.GENERAL_FONT.getIcon());
		}

		return content;
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
		pack();
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	@Override
	protected boolean checkData() {
		if (!editor.checkData()) return false;
		if (useHeadingScript.isSelected()) {
			if (!editorHeading.checkData()) return false;
		}
		return true;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementOutputJS) {
			final ModelElementOutputJS output=(ModelElementOutputJS)element;

			output.setOutputActive(outputActive.isSelected());

			output.setOutputFile(fileNameEdit.getText());
			output.setOutputFileOverwrite(fileOverwrite.isSelected());

			output.setScript(editor.getScript());
			switch (editor.getMode()) {
			case Javascript: output.setMode(ModelElementOutputJS.ScriptMode.Javascript); break;
			case Java: output.setMode(ModelElementOutputJS.ScriptMode.Java); break;
			}

			output.setUseHeadingScript(useHeadingScript.isSelected());

			output.setScriptHeading(editorHeading.getScript());
			switch (editorHeading.getMode()) {
			case Javascript: output.setModeHeading(ModelElementOutputJS.ScriptMode.Javascript); break;
			case Java: output.setModeHeading(ModelElementOutputJS.ScriptMode.Java); break;
			}
		}
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Ausgabedatei an.
	 * @see #fileNameEdit
	 */
	private void selectFile() {
		final File initialDirectory=(!fileNameEdit.getText().isBlank())?(new File(fileNameEdit.getText())).getParentFile():null;

		final var fc=new PlugableFileChooser(initialDirectory,true);
		fc.setDialogTitle(Language.tr("Surface.Output.Dialog.FileName.Select"));
		fc.addChoosableFileFilter(Table.FileTypeText+" (*.txt, *.tsv)","txt","tsv");
		fc.setFileFilter("txt");

		final File file=fc.showSaveDialogFileWithExtension(ModelElementOutputJSDialog.this);
		if (file==null) return;

		fileNameEdit.setText(file.toString());
	}
}