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
import java.io.File;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.Table;
import mathtools.distribution.swing.CommonVariables;
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
	 * Eingabefeld für den Dateinamen der Ausgabedatei
	 */
	private JTextField fileNameEdit;

	/**
	 * Skripteditor
	 */
	private ScriptEditorPanel editor;

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

		if (element instanceof ModelElementOutputJS) {
			final Object[] data=getInputPanel(Language.tr("Surface.Output.Dialog.FileName")+":",((ModelElementOutputJS)element).getOutputFile());
			final JPanel upperPanel=(JPanel)data[0];
			fileNameEdit=(JTextField)data[1];
			content.add(upperPanel,BorderLayout.NORTH);
			fileNameEdit.setEditable(!readOnly);

			JButton button=new JButton();
			button.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
			button.setToolTipText(Language.tr("Surface.Output.Dialog.FileName.Select"));
			button.addActionListener(e->selectFile());
			button.setEnabled(!readOnly);
			upperPanel.add(button,BorderLayout.EAST);

			final String script=((ModelElementOutputJS)element).getScript();
			ScriptEditorPanel.ScriptMode mode;
			switch (((ModelElementOutputJS)element).getMode()) {
			case Javascript: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			case Java: mode=ScriptEditorPanel.ScriptMode.Java; break;
			default: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			}
			content.add(editor=new ScriptEditorPanel(script,mode,readOnly,Language.tr("Surface.SetJS.Dialog.Script"),element.getModel(),helpRunnable,ScriptEditorPanel.featuresClientStationOutput),BorderLayout.CENTER);
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
	}

	@Override
	protected boolean checkData() {
		return editor.checkData();
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
			((ModelElementOutputJS)element).setOutputFile(fileNameEdit.getText());
			((ModelElementOutputJS)element).setScript(editor.getScript());
			switch (editor.getMode()) {
			case Javascript:
				((ModelElementOutputJS)element).setMode(ModelElementOutputJS.ScriptMode.Javascript);
				break;
			case Java:
				((ModelElementOutputJS)element).setMode(ModelElementOutputJS.ScriptMode.Java);
				break;
			}
		}
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Ausgabedatei an.
	 * @see #fileNameEdit
	 */
	private void selectFile() {
		File oldFile=new File(fileNameEdit.getText());
		File initialDirectory=oldFile.getParentFile();

		JFileChooser fc;
		if (initialDirectory!=null) fc=new JFileChooser(initialDirectory.toString()); else {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		}
		fc.setDialogTitle(Language.tr("Surface.Output.Dialog.FileName.Select"));
		FileFilter txt=new FileNameExtensionFilter(Table.FileTypeText+" (*.txt, *.tsv)","txt","tsv");
		fc.addChoosableFileFilter(txt);
		fc.setFileFilter(txt);

		if (fc.showSaveDialog(ModelElementOutputJSDialog.this)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
		}

		fileNameEdit.setText(file.toString());
	}
}