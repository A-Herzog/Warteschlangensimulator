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
package ui.inputprocessor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.Table;
import mathtools.distribution.tools.FileDropper;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.dialogs.WaitDialog;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.elements.ModelElementSourceTable;

/**
 * In diesem Dialog können gewöhnliche Tabellen geladen werden
 * und in Tabellen umgewandelt werden, die von {@link ModelElementSourceTable}
 * gelesen werden können.
 * @author Alexander Herzog
 * @see ClientInputTableProcessor
 * @see ModelElementSourceTable
 */
public class ClientInputTableDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-505249988542045443L;

	/**
	 * Eingabefeld für die Eingabetabelle
	 */
	private final JTextField editInput;

	/**
	 * Eingabefeld für die Ausgabetabelle
	 */
	private final JTextField editOutput;

	/**
	 * Name der zuletzt verarbeiteten Tabelle
	 * (um nicht die komplette Verarbeitung bei jedem Cursor-Tastendruck auszulösen)
	 * @see #editInput
	 * @see #processInput()
	 */
	private String lastInputTable;

	/**
	 * System zur Verarbeitung der Eingabetabelle
	 * @see #processInput()
	 */
	private ClientInputTableProcessor processor;

	/**
	 * Liste mit den Konfigurationen für die Spalten
	 */
	private final JList<ClientInputTableProcessor.ColumnSetup> list;

	/**
	 * Datenmodell für {@link #list}
	 * @see #list
	 */
	private final DefaultListModel<ClientInputTableProcessor.ColumnSetup> listData;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public ClientInputTableDialog(final Component owner) {
		super(owner,Language.tr("BuildClientSourceTable.Title"));

		Object[] data;
		JPanel line;
		JButton button;

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(this,"ProcessClientTable"));
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.globalProcessClientTable);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		/* Konfigurationsbereich */
		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("BuildClientSourceTable.InputTable")+":","");
		setup.add(line=(JPanel)data[0]);
		editInput=(JTextField)data[1];
		editInput.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {processInput();}
			@Override public void keyReleased(KeyEvent e) {processInput();}
			@Override public void keyPressed(KeyEvent e) {processInput();	}
		});
		line.add(button=new JButton(Images.GENERAL_SELECT_TABLE_IN_FILE.getIcon()),BorderLayout.EAST);
		button.setToolTipText(Language.tr("BuildClientSourceTable.InputTable.Select"));
		button.addActionListener(e->selectInputTable());
		FileDropper.addFileDropper(line,editInput);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("BuildClientSourceTable.OutputTable")+":","");
		setup.add(line=(JPanel)data[0]);
		editOutput=(JTextField)data[1];
		line.add(button=new JButton(Images.GENERAL_SELECT_TABLE_IN_FILE.getIcon()),BorderLayout.EAST);
		button.setToolTipText(Language.tr("BuildClientSourceTable.OutputTable.Select"));
		button.addActionListener(e->selectOutputTable());
		FileDropper.addFileDropper(line,editOutput);

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("BuildClientSourceTable.ColumnsInfo")));

		/* Liste der Spaltenkonfigurationen */
		content.add(new JScrollPane(list=new JList<>()),BorderLayout.CENTER);
		list.setModel(listData=new DefaultListModel<>());
		list.setCellRenderer(new ColumnSetupRender());
		list.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) editColumnSetup();}
		});
		list.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {if (e.getKeyCode()==KeyEvent.VK_ENTER) editColumnSetup();}
		});

		/* Dialog starten */
		setSizeRespectingScreensize(600,800);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Zeigt den Dialog zur Auswahl der Eingabetabelle an.
	 * @see #editInput
	 */
	private void selectInputTable() {
		final File file=Table.showLoadDialog(this,Language.tr("BuildClientSourceTable.InputTable.Select"));
		if (file!=null) {
			editInput.setText(file.toString());
			processInput();
		}
	}

	/**
	 * Zeigt den Dialog zur Auswahl der Ausgabetabelle an.
	 * @see #editOutput
	 */
	private void selectOutputTable() {
		final File file=Table.showSaveDialog(this,Language.tr("BuildClientSourceTable.OutputTable.Select"));
		if (file!=null) {
			editOutput.setText(file.toString());
		}
	}

	/**
	 * Bearbeitet die Einstellungen zu einer einzelnen Spalte
	 */
	private void editColumnSetup() {
		if (list.getSelectedIndex()<0 || processor==null) return;

		final ClientInputTableProcessor.ColumnSetup column=processor.getColumns()[list.getSelectedIndex()];

		final ClientInputTableEditDialog dialog=new ClientInputTableEditDialog(this,column);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			list.updateUI();
		}
	}

	/**
	 * Lädt die Eingabetabelle und verarbeitet diese.
	 * @see #editInput
	 * @see #list
	 */
	private void processInput() {
		final String newInputTable=editInput.getText().trim();
		if (newInputTable.equals(lastInputTable)) return;
		lastInputTable=newInputTable;

		listData.clear();

		processor=new ClientInputTableProcessor();

		WaitDialog.workString(this,()->{
			if (!processor.loadTable(new File(newInputTable))) {processor=null; return null;}
			for (ClientInputTableProcessor.ColumnSetup columnSetup: processor.getColumns()) listData.addElement(columnSetup);
			return null;
		},WaitDialog.Mode.LOAD_DATA);
	}

	@Override
	protected boolean checkData() {
		final String inputTable=editInput.getText().trim();
		if (inputTable.isEmpty()) {
			MsgBox.error(this,Language.tr("BuildClientSourceTable.InputTable.Error.NoFile.Title"),Language.tr("BuildClientSourceTable.InputTable.Error.NoFile.Info"));
			return false;
		}

		if (!new File(inputTable).isFile()) {
			MsgBox.error(this,Language.tr("BuildClientSourceTable.InputTable.Error.DoesNotExist.Title"),String.format(Language.tr("BuildClientSourceTable.InputTable.Error.DoesNotExist.Info"),inputTable));
			return false;
		}

		if (processor==null) {
			MsgBox.error(this,Language.tr("BuildClientSourceTable.InputTable.Error.Process.Title"),String.format(Language.tr("BuildClientSourceTable.InputTable.Error.Process.Info"),inputTable));
			return false;
		}

		final String outputTable=editOutput.getText().trim();
		if (outputTable.isEmpty()) {
			MsgBox.error(this,Language.tr("BuildClientSourceTable.OutputTable.Error.NoFile.Title"),Language.tr("BuildClientSourceTable.OutputTable.Error.NoFile.Info"));
			return false;
		}

		final File outputFile=new File(outputTable);
		if (outputFile.exists()) {
			if (!MsgBox.confirmOverwrite(this,outputFile)) return false;
		}

		return true;
	}

	@Override
	protected void storeData() {
		final Table result=processor.process();
		if (result==null) {
			MsgBox.error(this,Language.tr("BuildClientSourceTable.InputTable.Error.Process.Title"),String.format(Language.tr("BuildClientSourceTable.InputTable.Error.Process.Info"),editInput.getText().trim()));
			return;
		}

		if (!WaitDialog.workBoolean(this,()->result.save(editOutput.getText()),WaitDialog.Mode.SAVE_DATA)) {
			MsgBox.error(this,Language.tr("BuildClientSourceTable.OutputTable.Error.Save.Title"),String.format(Language.tr("BuildClientSourceTable.OutputTable.Error.Save.Info"),editOutput.getText()));
			return;
		}
	}

	/**
	 * Renderer für die Liste der Spaltenkonfigurationen
	 * @see ClientInputTableDialog#list
	 */
	private static class ColumnSetupRender extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=1587292200169531942L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (value instanceof ClientInputTableProcessor.ColumnSetup) {
				final ClientInputTableProcessor.ColumnSetup setup=(ClientInputTableProcessor.ColumnSetup)value;
				final ColumnSetupRender columnSetupRender=(ColumnSetupRender)renderer;
				columnSetupRender.setText(setup.getHTMLInfo());
				columnSetupRender.setIcon(Images.GENERAL_TABLE.getIcon());
				columnSetupRender.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			}
			return renderer;
		}
	}
}
