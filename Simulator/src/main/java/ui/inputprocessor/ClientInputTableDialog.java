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
	 * @see #processInput(ui.inputprocessor.ClientInputTableProcessor.ColumnsSetup)
	 */
	private String lastInputTable;

	/**
	 * System zur Verarbeitung der Eingabetabelle
	 * @see #processInput(ui.inputprocessor.ClientInputTableProcessor.ColumnsSetup)
	 */
	private ClientInputTableProcessor processor;

	/**
	 * Liste mit den Konfigurationen für die Spalten
	 */
	private final JList<ClientInputTableProcessor.ColumnData> list;

	/**
	 * Datenmodell für {@link #list}
	 * @see #list
	 */
	private final DefaultListModel<ClientInputTableProcessor.ColumnData> listData;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public ClientInputTableDialog(final Component owner) {
		this(owner,null,null);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param loadTable	Name der initial zu verwendenden Tabellendatei (kann <code>null</code> sein)
	 * @param loadSetup	Initial zu ladende Einstellungen zur Verwendung der Tabellenspalten (kann <code>null</code> sein)
	 */
	public ClientInputTableDialog(final Component owner, final String loadTable, final ClientInputTableProcessor.ColumnsSetup loadSetup) {
		super(owner,(loadSetup==null)?Language.tr("BuildClientSourceTable.Title"):Language.tr("BuildClientSourceTable.TitleDirectImport"));

		Object[] data;
		JPanel line;
		JButton button;

		/* GUI */
		addUserButton(Language.tr("BuildClientSourceTable.SelectAll"),Images.EDIT_ADD.getIcon());
		addUserButton(Language.tr("BuildClientSourceTable.SelectNone"),Images.EDIT_DELETE.getIcon());
		getUserButton(0).setToolTipText(Language.tr("BuildClientSourceTable.SelectAll.Hint"));
		getUserButton(1).setToolTipText(Language.tr("BuildClientSourceTable.SelectNone.Hint"));
		final JPanel all=createGUI(()->Help.topicModal(this,(loadSetup==null)?"ProcessClientTable":"ModelElementSourceTable"));
		all.setLayout(new BorderLayout());
		if (loadSetup==null) {
			InfoPanel.addTopPanel(all,InfoPanel.globalProcessClientTable);
		} else {
			InfoPanel.addTopPanel(all,InfoPanel.globalProcessClientTableDirect);
		}
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		/* Konfigurationsbereich */
		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("BuildClientSourceTable.InputTable")+":",(loadTable==null)?"":loadTable);
		setup.add(line=(JPanel)data[0]);
		editInput=(JTextField)data[1];
		editInput.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {processInput(null);}
			@Override public void keyReleased(KeyEvent e) {processInput(null);}
			@Override public void keyPressed(KeyEvent e) {processInput(null);}
		});
		line.add(button=new JButton(Images.GENERAL_SELECT_TABLE_IN_FILE.getIcon()),BorderLayout.EAST);
		button.setToolTipText(Language.tr("BuildClientSourceTable.InputTable.Select"));
		button.addActionListener(e->selectInputTable());
		FileDropper.addFileDropper(line,editInput);

		if (loadSetup==null) {
			data=ModelElementBaseDialog.getInputPanel(Language.tr("BuildClientSourceTable.OutputTable")+":","");
			setup.add(line=(JPanel)data[0]);
			editOutput=(JTextField)data[1];
			line.add(button=new JButton(Images.GENERAL_SELECT_TABLE_IN_FILE.getIcon()),BorderLayout.EAST);
			button.setToolTipText(Language.tr("BuildClientSourceTable.OutputTable.Select"));
			button.addActionListener(e->selectOutputTable());
			FileDropper.addFileDropper(line,editOutput);
		} else {
			editOutput=null;
		}

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

		/* Setup laden */
		if (loadSetup!=null) processInput(loadSetup);

		/* Dialog starten */
		setSizeRespectingScreensize(600,800);
		setMinSizeRespectingScreensize(600,800);
		setResizable(true);
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
			processInput(null);
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

		final int index=list.getSelectedIndex();
		final ClientInputTableProcessor.ColumnData column=processor.getColumns()[index];

		final ClientInputTableProcessor.ColumnMode oldMode=column.mode;
		final ClientInputTableEditDialog dialog=new ClientInputTableEditDialog(this,column,editOutput!=null);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			if (column.mode!=oldMode) {
				if (column.mode==ClientInputTableProcessor.ColumnMode.ARRIVALS) {
					for (int i=0;i<processor.getColumns().length;i++) if (i!=index && processor.getColumns()[i].mode==ClientInputTableProcessor.ColumnMode.ARRIVALS) {
						ClientInputTableProcessor.ColumnData columnData=processor.getColumns()[i];
						columnData.mode=columnData.isNumeric?ClientInputTableProcessor.ColumnMode.NUMBER:ClientInputTableProcessor.ColumnMode.TEXT;
						/* Neuen Index vergeben? */
						if (columnData.index<0) {
							int testIndex=-1;
							boolean ok=false;
							do {
								testIndex++;
								ok=true;
								for (ClientInputTableProcessor.ColumnData columnData2: processor.getColumns()) if (columnData2.mode==ClientInputTableProcessor.ColumnMode.NUMBER && columnData2.index==testIndex) {ok=false; break;}
							} while (!ok);
							columnData.index=testIndex;
						}
					}
				}
				if (column.mode==ClientInputTableProcessor.ColumnMode.CLIENT_TYPES) {
					for (int i=0;i<processor.getColumns().length;i++) if (i!=index && processor.getColumns()[i].mode==ClientInputTableProcessor.ColumnMode.CLIENT_TYPES) {
						ClientInputTableProcessor.ColumnData columnData=processor.getColumns()[i];
						columnData.mode=columnData.isNumeric?ClientInputTableProcessor.ColumnMode.NUMBER:ClientInputTableProcessor.ColumnMode.TEXT;
						/* Neuen Index vergeben? */
						if (columnData.index<0) {
							int testIndex=-1;
							boolean ok=false;
							do {
								testIndex++;
								ok=true;
								for (ClientInputTableProcessor.ColumnData columnData2: processor.getColumns()) if (columnData2.mode==ClientInputTableProcessor.ColumnMode.NUMBER && columnData2.index==testIndex) {ok=false; break;}
							} while (!ok);
							columnData.index=testIndex;
						}
					}
				}
			}
			if (column.mode==ClientInputTableProcessor.ColumnMode.NUMBER) {

				/* Gibt es jetzt doppelte Indices? */
				final int usedIndex=column.index;
				ClientInputTableProcessor.ColumnData needToChange=null;
				for (ClientInputTableProcessor.ColumnData columnData: processor.getColumns()) if (columnData!=column) {
					if (columnData.mode==ClientInputTableProcessor.ColumnMode.NUMBER && columnData.index==usedIndex) {
						needToChange=columnData;
						break;
					}
				}
				if (needToChange!=null) {
					int testIndex=-1;
					boolean ok=false;
					do {
						testIndex++;
						ok=true;
						for (ClientInputTableProcessor.ColumnData columnData: processor.getColumns()) {
							if (columnData.mode==ClientInputTableProcessor.ColumnMode.NUMBER && columnData.index==testIndex) {
								ok=false;
								break;
							}
						}
					} while (!ok);
					needToChange.index=testIndex;
				}
			}
			list.updateUI();
		}
	}

	/**
	 * Lädt die Eingabetabelle und verarbeitet diese.
	 * @param loadSetup	Vorgabeeinstellungen für die Spalten (kann <code>null</code> sein)
	 * @see #editInput
	 * @see #list
	 */
	private void processInput(final ClientInputTableProcessor.ColumnsSetup loadSetup) {
		final String newInputTable=editInput.getText().trim();
		if (newInputTable.equals(lastInputTable)) return;
		lastInputTable=newInputTable;

		listData.clear();

		processor=new ClientInputTableProcessor();

		WaitDialog.workString(this,()->{
			if (!processor.loadTable(new File(newInputTable))) {processor=null; return null;}
			if (loadSetup!=null) processor.loadSetup(loadSetup);
			for (ClientInputTableProcessor.ColumnData columnSetup: processor.getColumns()) listData.addElement(columnSetup);
			return null;
		},WaitDialog.Mode.LOAD_DATA);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		for (ClientInputTableProcessor.ColumnData columnSetup: processor.getColumns()) {
			columnSetup.mode=(nr==0)?columnSetup.initialMode:ClientInputTableProcessor.ColumnMode.OFF;
		}
		list.updateUI();
	}

	@Override
	protected boolean checkData() {
		/* Eingabetabelle */
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

		/* Ausgabetabelle */
		final String outputTable;
		if (editOutput!=null) {
			outputTable=editOutput.getText().trim();
			if (outputTable.isEmpty()) {
				MsgBox.error(this,Language.tr("BuildClientSourceTable.OutputTable.Error.NoFile.Title"),Language.tr("BuildClientSourceTable.OutputTable.Error.NoFile.Info"));
				return false;
			}
		} else {
			outputTable=null;
		}

		/* Konfiguration */
		boolean hasArrivalsCol=false;
		boolean hasTypesCol=false;
		for (ClientInputTableProcessor.ColumnData columnSetup: processor.getColumns()) {
			if (columnSetup.mode==ClientInputTableProcessor.ColumnMode.ARRIVALS) hasArrivalsCol=true;
			if (columnSetup.mode==ClientInputTableProcessor.ColumnMode.CLIENT_TYPES) hasTypesCol=true;
			if (hasArrivalsCol && hasTypesCol) break;
		}
		if (!hasArrivalsCol) {
			MsgBox.error(this,Language.tr("BuildClientSourceTable.ProcessError.NoArrivalsCol.Title"),Language.tr("BuildClientSourceTable.ProcessError.NoArrivalsCol.Info"));
			return false;
		}
		if (!hasTypesCol) {
			MsgBox.error(this,Language.tr("BuildClientSourceTable.ProcessError.NoTypesCol.Title"),Language.tr("BuildClientSourceTable.ProcessError.NoTypesCol.Info"));
			return false;
		}

		/* Ausgabetabelle überschreiben? */
		if (outputTable!=null) {
			final File outputFile=new File(outputTable);
			if (outputFile.exists()) {
				if (!MsgBox.confirmOverwrite(this,outputFile)) return false;
			}
		}

		return true;
	}

	@Override
	protected void storeData() {
		if (editOutput==null) {
			/* Nur Konfiguration speichern; hier nichts zu tun */
		} else {
			/* Tabelle aufbereiten */
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
	}

	/**
	 * Liefert die Einstellungen zur Verwendung der Tabellenspalten zurück.
	 * @return	Einstellungen zur Verwendung der Tabellenspalten
	 */
	public ClientInputTableProcessor.ColumnsSetup getSetup() {
		return processor.getSetup();
	}

	/**
	 * Liefert den Namen der gewählten Tabelle.
	 * @return	Name der gewählten Tabelle
	 */
	public String getInputTable() {
		return editInput.getText().trim();
	}

	/**
	 * Renderer für die Liste der Spaltenkonfigurationen
	 * @see ClientInputTableDialog#list
	 */
	private static class ColumnSetupRender extends DefaultListCellRenderer {
		/**
		 * Konstruktor der Klasse
		 */
		public ColumnSetupRender() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=1587292200169531942L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (value instanceof ClientInputTableProcessor.ColumnData) {
				final ClientInputTableProcessor.ColumnData setup=(ClientInputTableProcessor.ColumnData)value;
				final ColumnSetupRender columnSetupRender=(ColumnSetupRender)renderer;
				columnSetupRender.setText(setup.getHTMLInfo());
				columnSetupRender.setIcon(setup.getIcon());
				columnSetupRender.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			}
			return renderer;
		}
	}
}
