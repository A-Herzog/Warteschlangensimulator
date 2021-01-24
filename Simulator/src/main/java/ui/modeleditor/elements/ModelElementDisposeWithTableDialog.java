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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.Table;
import mathtools.distribution.swing.CommonVariables;
import systemtools.MsgBox;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementDisposeWithTable}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementDisposeWithTable
 */
public class ModelElementDisposeWithTableDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=5470309978570993110L;

	/**
	 * Option: Simulation abbrechen, wenn an dieser Station ein Kunde eintrifft?
	 */
	private JCheckBox stoppSimulationOnClientArrival;

	/**
	 * Eingabefeld: Tabellendatei zum Speichern der Kunden
	 */
	private JTextField clientsOutputTable;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementDisposeWithTable}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementDisposeWithTableDialog(final Component owner, final ModelElementDisposeWithTable element, final boolean readOnly) {
		super(owner,Language.tr("Surface.DisposeWithTable.Dialog.Title"),element,"ModelElementDisposeWithTable",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(550,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationDisposeWithTable;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final ModelElementDisposeWithTable dispose=(ModelElementDisposeWithTable)element;

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		JPanel line;

		/*  Simulation abbrechen, wenn an dieser Station ein Kunde eintrifft? */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(stoppSimulationOnClientArrival=new JCheckBox(Language.tr("Surface.Dispose.Dialog.StoppSimulationOnClientArrival")));
		stoppSimulationOnClientArrival.setSelected(dispose.isStoppSimulationOnClientArrival());

		/* Tabellendatei zum Speichern der Kunden */
		final Object[] data=getInputPanel(Language.tr("Surface.Dispose.Dialog.Table")+":",dispose.getOutputFile());
		content.add(line=(JPanel)data[0]);
		clientsOutputTable=(JTextField)data[1];
		clientsOutputTable.setToolTipText(Language.tr("Surface.Dispose.Dialog.Table.Hint"));
		clientsOutputTable.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});
		final JButton button=new JButton("",Images.GENERAL_SELECT_TABLE_IN_FILE.getIcon());
		button.setToolTipText(Language.tr("Surface.Dispose.Dialog.Table.Select"));
		line.add(button,BorderLayout.EAST);
		button.addActionListener(e->selectTable());

		checkData(false);

		return content;
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Tabellendatei an.
	 * @see #clientsOutputTable
	 */
	private void selectTable() {
		final File oldFile=new File(clientsOutputTable.getText());
		final File initialDirectory=oldFile.getParentFile();

		JFileChooser fc;
		if (initialDirectory!=null) fc=new JFileChooser(initialDirectory.toString()); else {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		}
		fc.setDialogTitle(Language.tr("Surface.Output.Dialog.FileName.Select"));
		final FileFilter txt=new FileNameExtensionFilter(Table.FileTypeText+" (*.txt, *.tsv)","txt","tsv");
		final FileFilter csv=new FileNameExtensionFilter(Table.FileTypeCSV+" (*.csv)","csv");
		fc.addChoosableFileFilter(txt);
		fc.addChoosableFileFilter(csv);
		fc.setFileFilter(txt);

		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
			if (fc.getFileFilter()==csv) file=new File(file.getAbsoluteFile()+".csv");
		}

		clientsOutputTable.setText(file.toString());
		checkData(false);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		if (clientsOutputTable.getText().trim().isEmpty()) {
			clientsOutputTable.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.Dispose.Dialog.Table"),Language.tr("Surface.Dispose.Dialog.Table.ErrorEmpty"));
				return false;
			}
		} else {
			clientsOutputTable.setBackground(SystemColor.text);
		}


		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		super.storeData();
		if (element instanceof ModelElementDisposeWithTable) {
			final ModelElementDisposeWithTable dispose=(ModelElementDisposeWithTable)element;

			/*  Simulation abbrechen, wenn an dieser Station ein Kunde eintrifft? */
			dispose.setStoppSimulationOnClientArrival(stoppSimulationOnClientArrival.isSelected());

			/* Tabellendatei zum Speichern der Kunden */
			dispose.setOutputFile(clientsOutputTable.getText().trim());
		}
	}
}
