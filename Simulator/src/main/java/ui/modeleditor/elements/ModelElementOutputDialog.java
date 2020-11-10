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
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
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
	 * Zeigt je nach Typ der gewählten Ausgabetabelle an,
	 * dass bei bestimmten Tabellentypen die Ausgabe am Ende
	 * enbloc erfolgt und daher nicht zeilenweise
	 * nachvollzogen werden kann.
	 */
	private JLabel info;

	/**
	 * Tabelle zur Konfiguration der auszugebenden Daten
	 */
	private OutputTableModel tableModel;

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
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		if (element instanceof ModelElementOutput) {
			final JPanel upperPanel=new JPanel();
			upperPanel.setLayout(new BoxLayout(upperPanel,BoxLayout.PAGE_AXIS));
			content.add(upperPanel,BorderLayout.NORTH);
			final Object[] data=getInputPanel(Language.tr("Surface.Output.Dialog.FileName")+":",((ModelElementOutput)element).getOutputFile());
			JPanel line=(JPanel)data[0];
			fileNameEdit=(JTextField)data[1];
			upperPanel.add(line);
			fileNameEdit.setEditable(!readOnly);
			fileNameEdit.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {updateInfo();}
				@Override public void keyReleased(KeyEvent e) {updateInfo();}
				@Override public void keyPressed(KeyEvent e) {updateInfo();}
			});

			JButton button=new JButton();
			button.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
			button.setToolTipText(Language.tr("Surface.Output.Dialog.FileName.Select"));
			button.addActionListener(e->selectFile());
			button.setEnabled(!readOnly);
			line.add(button,BorderLayout.EAST);

			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(info=new JLabel(Language.tr("Surface.Output.Dialog.TableInfo")));

			final JTableExt table=new JTableExt();
			table.setModel(tableModel=new OutputTableModel(table,element.getModel(),((ModelElementOutput)element).getModes(),((ModelElementOutput)element).getData(),element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true),readOnly));
			table.setIsPanelCellTable(0);
			table.setIsPanelCellTable(1);
			table.getColumnModel().getColumn(1).setMaxWidth(300);
			table.getColumnModel().getColumn(1).setMinWidth(300);
			table.setEnabled(!readOnly);
			content.add(new JScrollPane(table),BorderLayout.CENTER);

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
	}

	/**
	 * Prüft, ob für die gewählte Ausgabedatei die Warnung, dass die Ausgabe nicht
	 * zeilenweise erfolgen kann, angezeigt werden muss.
	 * @see #fileNameEdit
	 * @see #info
	 */
	private void updateInfo() {
		final String nameLower=fileNameEdit.getText().toLowerCase();
		info.setVisible(nameLower.endsWith(".xls") || nameLower.endsWith(".xlsx"));
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
			((ModelElementOutput)element).setOutputFile(fileNameEdit.getText());

			final List<ModelElementOutput.OutputMode> modes=((ModelElementOutput)element).getModes();
			modes.clear();
			modes.addAll(tableModel.getModes());
			List<String> data=((ModelElementOutput)element).getData();
			data.clear();
			data.addAll(tableModel.getData());
			while (data.size()<modes.size()) data.add("");
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
