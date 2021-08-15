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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import tools.JTableExt;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementOutput}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementOutput
 */
public class ModelElementOutputDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1828468870634988424L;

	/**
	 * Eingabefeld für den Dateinamen der Ausgabedatei
	 */
	private JTextField fileNameEdit;

	/**
	 * Soll eine möglicherweise bestehende Datei beim Start der Ausgabe überschrieben werden? (Ansonsten wird angehängt)
	 */
	private JCheckBox fileOverwrite;

	/**
	 * Zeigt je nach Typ der gewählten Ausgabetabelle an,
	 * dass bei bestimmten Tabellentypen die Ausgabe am Ende
	 * enbloc erfolgt und daher nicht zeilenweise
	 * nachvollzogen werden kann.
	 */
	private JLabel info;

	/**
	 * Zahlen im lokalen Format (<code>false</code>) oder im System-Format (<code>true</code>) ausgeben?<br>
	 * (Ist <code>null</code>, wenn Dezimalpunkte bereits als lokales Format verwendet werden.)
	 */
	private JCheckBox systemFormat;

	/**
	 * Tabelle zur Konfiguration der auszugebenden Daten
	 */
	private OutputTableModel tableModel;

	/**
	 * Auswahlbox für die Art der Überschriftenausgabe
	 */
	private JComboBox<String> headingMode;

	/**
	 * Tabelle zur Konfiguration der auszugebenden Überschriften
	 */
	private OutputTableModel tableModelHeadings;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementOutput}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementOutputDialog(final Component owner, final ModelElementOutput element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Output.Dialog.Title"),element,"ModelElementOutput",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationOutput;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		if (element instanceof ModelElementOutput) {
			final ModelElementOutput output=(ModelElementOutput)element;

			/* Konfigurationsbereich oben */
			final JPanel upperPanel=new JPanel();
			upperPanel.setLayout(new BoxLayout(upperPanel,BoxLayout.PAGE_AXIS));
			content.add(upperPanel,BorderLayout.NORTH);

			/* Eingabefeld: Dateiname */
			Object[] data=getInputPanel(Language.tr("Surface.Output.Dialog.FileName")+":",output.getOutputFile());
			JPanel line=(JPanel)data[0];
			fileNameEdit=(JTextField)data[1];
			upperPanel.add(line);
			fileNameEdit.setEditable(!readOnly);
			fileNameEdit.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {updateInfo();}
				@Override public void keyReleased(KeyEvent e) {updateInfo();}
				@Override public void keyPressed(KeyEvent e) {updateInfo();}
			});
			final JButton button=new JButton();
			button.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
			button.setToolTipText(Language.tr("Surface.Output.Dialog.FileName.Select"));
			button.addActionListener(e->selectFile());
			button.setEnabled(!readOnly);
			line.add(button,BorderLayout.EAST);

			/* Infozeile zu Dateityp */
			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(info=new JLabel(Language.tr("Surface.Output.Dialog.TableInfo")));

			/* Checkbox: Überschreiben? */
			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(fileOverwrite=new JCheckBox(Language.tr("Surface.Output.Dialog.Overwrite"),output.isOutputFileOverwrite()));
			fileOverwrite.setToolTipText(Language.tr("Surface.Output.Dialog.Overwrite.Info"));

			/* Checkbox: System-Zahlenformat? */
			if (NumberTools.getDecimalSeparator()!='.') {
				upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
				line.add(systemFormat=new JCheckBox(Language.tr("Surface.Output.Dialog.SystemFormat")),output.isSystemFormat());
				systemFormat.setToolTipText(Language.tr("Surface.Output.Dialog.SystemFormat.Hint"));
			} else {
				systemFormat=null;
			}

			/* Tabs in der Mitte */
			final JTabbedPane tabs=new JTabbedPane();
			content.add(tabs,BorderLayout.CENTER);
			JPanel tab;
			JTableExt table;

			/* Tab: Ausgabedaten */
			tabs.addTab(Language.tr("Surface.Output.Dialog.Tab.OutputData"),tab=new JPanel(new BorderLayout()));
			tab.add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
			table.setModel(tableModel=new OutputTableModel(table,output.getModel(),output.getOutput(),output.getSurface().getMainSurfaceVariableNames(output.getModel().getModelVariableNames(),true),readOnly));

			/* Tab: Überschriften */
			tabs.addTab(Language.tr("Surface.Output.Dialog.Tab.Headings"),tab=new JPanel(new BorderLayout()));
			final JPanel lines=new JPanel();
			tab.add(lines,BorderLayout.NORTH);
			lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));
			data=getComboBoxPanel(Language.tr("Surface.Output.Dialog.Tab.Headings.Mode")+":",Arrays.asList(
					Language.tr("Surface.Output.Dialog.Tab.Headings.Off"),
					Language.tr("Surface.Output.Dialog.Tab.Headings.Auto"),
					Language.tr("Surface.Output.Dialog.Tab.Headings.UserDefined")));
			lines.add((JPanel)data[0]);
			lines.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(new JLabel(Language.tr("Surface.Output.Dialog.Tab.Headings.UserDefined.Info")+":"));
			headingMode=(JComboBox<String>)data[1];
			switch (output.getHeadingMode()) {
			case OFF: headingMode.setSelectedIndex(0); break;
			case AUTO: headingMode.setSelectedIndex(1); break;
			case USER_DEFINED: headingMode.setSelectedIndex(2); break;
			}
			tab.add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
			table.setModel(tableModelHeadings=new OutputTableModel(table,output.getModel(),output.getOutputHeading(),output.getSurface().getMainSurfaceVariableNames(output.getModel().getModelVariableNames(),true),readOnly));

			/* Icons auf den Tabs */
			tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_OUTPUT.getIcon());
			tabs.setIconAt(1,Images.GENERAL_FONT.getIcon());

			/* Dialog starten */
			updateInfo();
		}

		return content;
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		pack();
		setResizable(true);
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	/**
	 * Prüft, ob für die gewählte Ausgabedatei die Warnung, dass die Ausgabe nicht
	 * zeilenweise erfolgen kann, angezeigt werden muss.
	 * @see #fileNameEdit
	 * @see #info
	 */
	private void updateInfo() {
		final String nameLower=fileNameEdit.getText().toLowerCase();
		final boolean isZippedTable=nameLower.endsWith(".xls") || nameLower.endsWith(".xlsx") || nameLower.endsWith(".ods");
		final boolean isText=nameLower.endsWith(".txt") || nameLower.endsWith(".tsv");
		info.setVisible(isZippedTable);
		if (fileOverwrite!=null) {
			if (isZippedTable) fileOverwrite.setSelected(true);
			fileOverwrite.setEnabled(!readOnly && !isZippedTable);
		}
		if (systemFormat!=null) systemFormat.setEnabled(!readOnly && isText);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementOutput) {
			final ModelElementOutput output=(ModelElementOutput)element;

			output.setOutputFile(fileNameEdit.getText());
			output.setOutputFileOverwrite(fileOverwrite.isSelected());
			if (systemFormat!=null) output.setSystemFormat(systemFormat.isSelected());

			final List<ModelElementOutput.OutputRecord> outputList=output.getOutput();
			outputList.clear();
			outputList.addAll(tableModel.getOutput());

			switch (headingMode.getSelectedIndex()) {
			case 0: output.setHeadingMode(ModelElementOutput.HeadingMode.OFF); break;
			case 1: output.setHeadingMode(ModelElementOutput.HeadingMode.AUTO); break;
			case 2: output.setHeadingMode(ModelElementOutput.HeadingMode.USER_DEFINED); break;
			}

			final List<ModelElementOutput.OutputRecord> outputHeadingList=output.getOutputHeading();
			outputHeadingList.clear();
			outputHeadingList.addAll(tableModelHeadings.getOutput());
		}
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Ausgabedatei an.
	 * @see #fileNameEdit
	 */
	private void selectFile() {
		File oldFile=new File(fileNameEdit.getText());
		File initialDirectory=oldFile.getParentFile();

		final File file=Table.showSaveDialog(ModelElementOutputDialog.this,"Surface.Output.Dialog.FileName.Select",initialDirectory);
		if (file!=null) {
			fileNameEdit.setText(file.toString());
			updateInfo();
		}
	}
}
