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
package ui.modeleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.MultiTable;
import net.dde.DDEConnect;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.elements.DDEEditPanelDialog;

/**
 * Dieser Dialog ermöglicht das Bearbeiten der externen Daten
 * in einem {@link ModelLoadData}-Objekt.
 * @author Alexander Herzog
 * @see ModelLoadData
 * @see EditModel#modelLoadData
 */
public class ModelLoadDataDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-8751526127040969002L;

	/** Editormodell (zum Auslesen der verfügbaren Daten für Parameter) */
	private final EditModel model;

	/** Datenquelle aktiv? */
	private final JCheckBox active;
	/** Modus: Datei oder DDE-Verbindung */
	private final JComboBox<String> mode;
	/** Tabellendatei */
	private final JTextField workbook;
	/** Schaltfläche zur Auswahl der Tabellendatei ({@link #workbook}) */
	private final JButton workbookButton;
	/** Tabellenblatt innerhalb der Tabellendatei */
	private final JTextField sheet;

	/** "Eintrag bearbeiten"-Schaltfläche */
	private final JButton buttonEdit;
	/** "Eintrag löschen"-Schaltfläche */
	private final JButton buttonDelete;
	/** "Eintrag nach oben verschieben"-Schaltfläche */
	private final JButton buttonMoveUp;
	/** "Eintrag nach unten verschieben"-Schaltfläche */
	private final JButton buttonMoveDown;

	/** Interne Liste der Einträge */
	private final List<ModelLoadDataRecord> listData;
	/** Datenmodell zur Verbindung von {@link #listData} und {@link #list} */
	private final DefaultListModel<String> listModel;
	/** Darstellung der Einträge aus {@link #listData} */
	private final JList<String> list;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param loadData	In den Dialog zu ladende Daten; der Inhalt des Objektes wird nicht verändert
	 * @param model	Editormodell (zum Auslesen der verfügbaren Daten für Parameter)
	 */
	public ModelLoadDataDialog(final Component owner, final ModelLoadData loadData, final EditModel model) {
		super(owner,Language.tr("ModelLoadData.EditDialog.Title"));
		this.model=model;

		JPanel main=createGUI(()->Help.topicModal(this,"ModelLoadData"));
		main=InfoPanel.addTopPanelAndGetNewContent(main,InfoPanel.globalModelLoadData);

		main.setLayout(new BorderLayout());

		JPanel line;
		JLabel label;

		/* Einstellungen oben */

		final JPanel setup=new JPanel();
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
		main.add(setup,BorderLayout.NORTH);

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(active=new JCheckBox("<html><body><b>"+Language.tr("ModelLoadData.EditDialog.Active")+"</b></body></html>"));

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("ModelLoadData.EditDialog.Mode")+": "));
		line.add(mode=new JComboBox<>(new String[]{
				Language.tr("ModelLoadData.EditDialog.Mode.File"),
				Language.tr("ModelLoadData.EditDialog.Mode.DDE")
		}));
		label.setLabelFor(mode);
		mode.setRenderer(new IconListCellRenderer(new Images[] {
				Images.GENERAL_SELECT_TABLE_IN_FILE,
				Images.GENERAL_APPLICATION
		}));
		mode.addActionListener(e->modeChanged());

		setup.add(line=new JPanel(new BorderLayout()));
		line.add(label=new JLabel(Language.tr("ModelLoadData.EditDialog.Workbook")+": "),BorderLayout.WEST);
		line.add(workbook=new JTextField(),BorderLayout.CENTER);
		ModelElementBaseDialog.addUndoFeature(workbook);
		label.setLabelFor(workbook);
		line.add(workbookButton=new JButton(),BorderLayout.EAST);
		workbookButton.addActionListener(e->selectWorkbook());

		setup.add(line=new JPanel(new BorderLayout()));
		line.add(label=new JLabel(Language.tr("ModelLoadData.EditDialog.Table")+": "),BorderLayout.WEST);
		line.add(sheet=new JTextField(),BorderLayout.CENTER);
		ModelElementBaseDialog.addUndoFeature(sheet);
		label.setLabelFor(sheet);
		sheet.setToolTipText(Language.tr("ModelLoadData.EditDialog.Table.Hint"));

		/* Toolbar über Liste */

		final JPanel center=new JPanel(new BorderLayout());
		main.add(center,BorderLayout.CENTER);

		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		center.add(toolbar,BorderLayout.NORTH);
		toolbar.setFloatable(false);

		JButton button;
		toolbar.add(button=new JButton(Language.tr("ModelLoadData.EditDialog.Add")));
		button.setToolTipText(Language.tr("ModelLoadData.EditDialog.Add.Hint"));
		button.addActionListener(e->commandAdd());
		button.setIcon(Images.EDIT_ADD.getIcon());

		toolbar.add(buttonEdit=new JButton(Language.tr("ModelLoadData.EditDialog.Edit")));
		buttonEdit.setToolTipText(Language.tr("ModelLoadData.EditDialog.Edit.Hint"));
		buttonEdit.addActionListener(e->commandEdit());
		buttonEdit.setIcon(Images.GENERAL_EDIT.getIcon());

		toolbar.add(buttonDelete=new JButton(Language.tr("ModelLoadData.EditDialog.Delete")));
		buttonDelete.setToolTipText(Language.tr("ModelLoadData.EditDialog.Delete.Hint"));
		buttonDelete.addActionListener(e->commandDelete((e.getModifiers() & ActionEvent.SHIFT_MASK)!=0));
		buttonDelete.setIcon(Images.EDIT_DELETE.getIcon());

		toolbar.addSeparator();

		toolbar.add(buttonMoveUp=new JButton(Language.tr("ModelLoadData.EditDialog.MoveUp")));
		buttonMoveUp.setToolTipText(Language.tr("ModelLoadData.EditDialog.MoveUp.Hint"));
		buttonMoveUp.addActionListener(e->commandMoveUp());
		buttonMoveUp.setIcon(Images.ARROW_UP.getIcon());

		toolbar.add(buttonMoveDown=new JButton(Language.tr("ModelLoadData.EditDialog.MoveDown")));
		buttonMoveDown.setToolTipText(Language.tr("ModelLoadData.EditDialog.MoveDown.Hint"));
		buttonMoveDown.addActionListener(e->commandMoveDown());
		buttonMoveDown.setIcon(Images.ARROW_DOWN.getIcon());

		/* Liste */

		list=new JList<>(listModel=new DefaultListModel<>());
		center.add(new JScrollPane(list),BorderLayout.CENTER);
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_INSERT && e.getModifiersEx()==0) {
					commandAdd();
					e.consume();
					return;
				}
				if (e.getKeyCode()==KeyEvent.VK_ENTER && e.getModifiersEx()==InputEvent.SHIFT_DOWN_MASK) {
					commandEdit();
					e.consume();
					return;
				}
				if (e.getKeyCode()==KeyEvent.VK_DELETE) {
					commandDelete(e.getModifiersEx()==InputEvent.SHIFT_DOWN_MASK);
					e.consume();
					return;
				}
				if (e.getKeyCode()==KeyEvent.VK_UP && e.getModifiersEx()==InputEvent.CTRL_DOWN_MASK) {
					commandMoveUp();
					e.consume();
					return;
				}
				if (e.getKeyCode()==KeyEvent.VK_DOWN && e.getModifiersEx()==InputEvent.CTRL_DOWN_MASK) {
					commandMoveDown();
					e.consume();
					return;
				}
			}
		});
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e) && e.getClickCount()==1) {
					showContextMenu(e);
					e.consume();
					return;
				}
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
					commandEdit();
					e.consume();
					return;
				}
			}
		});
		list.addListSelectionListener(e->selectionChanged());

		/* Daten laden */

		active.setSelected(loadData.isActive());

		switch (loadData.getMode()) {
		case FILE: mode.setSelectedIndex(0); break;
		case DDE: mode.setSelectedIndex(1); break;
		default: mode.setSelectedIndex(0); break;
		}
		modeChanged();
		workbook.setText(loadData.getWorkbook());
		sheet.setText(loadData.getTable());
		listData=loadData.getList().stream().map(rec->rec.clone()).collect(Collectors.toList());
		reloadList(-1);

		/* Dialog starten */

		setMinSizeRespectingScreensize(700,600);
		setSizeRespectingScreensize(700,600);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}


	/**
	 * Aktualisiert die Darstellung von {@link #workbookButton}
	 * wenn der Modus in {@link #mode} geändert wurde.
	 * @see #workbookButton
	 * @see #mode
	 */
	private void modeChanged() {
		switch (mode.getSelectedIndex()) {
		case 0:
			workbookButton.setIcon(Images.GENERAL_SELECT_TABLE_IN_FILE.getIcon());
			workbookButton.setToolTipText(Language.tr("ModelLoadData.EditDialog.Workbook.SelectFile"));
			break;
		case 1:
			workbookButton.setIcon(Images.GENERAL_APPLICATION.getIcon());
			workbookButton.setToolTipText(Language.tr("ModelLoadData.EditDialog.Workbook.SelectDDE"));
			break;
		}
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Tabellendatei bzw. einer DDE-Arbeitsmappe an.
	 * @see #workbookButton
	 */
	private void selectWorkbook() {
		switch (mode.getSelectedIndex()) {
		case 0:
			final File file=new File(workbook.getText());
			final File newFile=MultiTable.showLoadDialog(this,Language.tr("ModelLoadData.EditDialog.Workbook.SelectFile"),file.getParentFile());
			if (newFile!=null) workbook.setText(newFile.toString());
			break;
		case 1:
			if (!DDEConnect.available()) {
				MsgBox.error(this,Language.tr("ModelLoadData.EditDialog.DDEError.Title"),Language.tr("ModelLoadData.EditDialog.DDEError.Info"));
				return;
			}
			final DDEConnect connect=new DDEConnect();
			final DDEEditPanelDialog dialog=new DDEEditPanelDialog(this,connect.listTables(),workbook.getText(),sheet.getText(),()->Help.topicModal(this,"ModelLoadData"));
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				workbook.setText(dialog.getSelectedWorkbook());
				sheet.setText(dialog.getSelectedTable());
			}
		}
	}

	/**
	 * Lädt die Listendarstellung neu
	 * @param selectIndex	Index des Eintrags der nach dem erneuten Laden der Liste selektiert werden soll (Werte &lt;0 führen zur Auswahl des ersten Eintrags, sofern es einen solchen gibt)
	 * @see #list
	 */
	private void reloadList(int selectIndex) {
		if (selectIndex<0) selectIndex=list.getSelectedIndex();

		listModel.clear();
		for (ModelLoadDataRecord record: listData) listModel.addElement(record.getListText());
		list.setModel(listModel);

		if (selectIndex>=0 && selectIndex<listData.size()) {
			list.setSelectedIndex(selectIndex);
		} else {
			if (listData.size()>0) list.setSelectedIndex(0);
		}

		selectionChanged();
	}

	/**
	 * Befehl: Eintrag hinzufügen
	 */
	private void commandAdd() {
		final ModelLoadDataRecordDialog dialog=new ModelLoadDataRecordDialog(this,null,model);
		final ModelLoadDataRecord record=dialog.getRecord();
		if (record!=null) {
			listData.add(record);
			reloadList(listData.size()-1);
		}
	}

	/**
	 * Befehl: Eintrag bearbeiten
	 * @see #buttonEdit
	 */
	private void commandEdit() {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		final ModelLoadDataRecordDialog dialog=new ModelLoadDataRecordDialog(this,listData.get(index),model);
		final ModelLoadDataRecord record=dialog.getRecord();
		if (record!=null) {
			listData.set(index,record);
			reloadList(index);
		}
	}

	/**
	 * Befehl: Eintrag löschen
	 *  @param isShiftDown	Ist die Umschalttaste gedrückt? (Wenn ja, löschen ohne Nachfrage.)
	 * @see #buttonDelete
	 */
	private void commandDelete(final boolean isShiftDown) {
		final int index=list.getSelectedIndex();
		if (index<0) return;
		if (!isShiftDown) {
			if (!MsgBox.confirm(this,Language.tr("ModelLoadData.EditDialog.Delete.Confirm.Title"),Language.tr("ModelLoadData.EditDialog.Delete.Confirm.Info"),Language.tr("ModelLoadData.EditDialog.Delete.Confirm.InfoYes"),Language.tr("ModelLoadData.EditDialog.Delete.Confirm.InfoNo"))) return;
		}
		listData.remove(index);
		reloadList(Math.max(0,index-1));
	}

	/**
	 * Befehl: Eintrag in der Liste nach oben verschieben
	 * @see #buttonMoveUp
	 */
	private void commandMoveUp() {
		final int index=list.getSelectedIndex();
		if (index<1) return;

		final ModelLoadDataRecord temp=listData.get(index);
		listData.set(index,listData.get(index-1));
		listData.set(index-1,temp);

		reloadList(index-1);
	}

	/**
	 * Befehl: Eintrag in der Liste nach unten verschieben
	 * @see #buttonMoveDown
	 */
	private void commandMoveDown() {
		final int index=list.getSelectedIndex();
		if (index<0 || index>=listData.size()-1) return;

		final ModelLoadDataRecord temp=listData.get(index);
		listData.set(index,listData.get(index+1));
		listData.set(index+1,temp);

		reloadList(index+1);
	}

	/**
	 * Zeigt das Kontextmenü zu einem Listeneintrag an.
	 * @param e	Auslösendes Mausereignis
	 */
	private void showContextMenu(final MouseEvent e) {
		final int index=list.getSelectedIndex();

		final JPopupMenu menu=new JPopupMenu();

		JMenuItem item;

		menu.add(item=new JCheckBoxMenuItem(Language.tr("ModelLoadData.EditDialog.Add")));
		item.setToolTipText(Language.tr("ModelLoadData.EditDialog.Add.Hint"));
		item.setIcon(Images.EDIT_ADD.getIcon());
		item.addActionListener(ev->commandAdd());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,0));

		menu.add(item=new JCheckBoxMenuItem(Language.tr("ModelLoadData.EditDialog.Edit")));
		item.setToolTipText(Language.tr("ModelLoadData.EditDialog.Edit.Hint"));
		item.setIcon(Images.GENERAL_EDIT.getIcon());
		item.addActionListener(ev->commandEdit());
		item.setEnabled(index>=0);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,InputEvent.SHIFT_DOWN_MASK));

		menu.add(item=new JCheckBoxMenuItem(Language.tr("ModelLoadData.EditDialog.Delete")));
		item.setToolTipText(Language.tr("ModelLoadData.EditDialog.Delete.Hint"));
		item.setIcon(Images.EDIT_DELETE.getIcon());
		item.addActionListener(ev->commandDelete((ev.getModifiers() & ActionEvent.SHIFT_MASK)!=0));
		item.setEnabled(index>=0);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));

		menu.addSeparator();

		menu.add(item=new JCheckBoxMenuItem(Language.tr("ModelLoadData.EditDialog.MoveUp")));
		item.setToolTipText(Language.tr("ModelLoadData.EditDialog.MoveUp.Hint"));
		item.setIcon(Images.ARROW_UP.getIcon());
		item.addActionListener(ev->commandMoveUp());
		item.setEnabled(index>=1);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP,InputEvent.CTRL_DOWN_MASK));

		menu.add(item=new JCheckBoxMenuItem(Language.tr("ModelLoadData.EditDialog.MoveDown")));
		item.setToolTipText(Language.tr("ModelLoadData.EditDialog.MoveUp.Hint"));
		item.setIcon(Images.ARROW_DOWN.getIcon());
		item.addActionListener(ev->commandMoveDown());
		item.setEnabled(index>0 && index<listData.size()-1);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.CTRL_DOWN_MASK));

		menu.show((Component)e.getSource(),e.getX(),e.getY());
	}

	/**
	 * Aktiviert oder deaktiviert einzelne Schaltflächen,
	 * wenn sich die Listenauswahl verändert hat.
	 */
	private void selectionChanged() {
		buttonEdit.setEnabled(list.getSelectedIndex()>=0);
		buttonDelete.setEnabled(list.getSelectedIndex()>=0);
		buttonMoveUp.setEnabled(list.getSelectedIndex()>0);
		buttonMoveDown.setEnabled(list.getSelectedIndex()>=0 && list.getSelectedIndex()<listData.size()-1);
	}

	/**
	 * Wurde der Dialog per "Ok" geschlossen, so wird über diese
	 * Methode ein neues Objekt, in dem die neuen Daten stehen,
	 * zurückgeliefert.
	 * @return	Im Dialog eingestellte Daten in einem neuen Objekt oder <code>null</code>, wenn der Dialog nicht per "Ok" geschlossen wurde.
	 */
	public ModelLoadData getLoadData() {
		if (getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
		final ModelLoadData results=new ModelLoadData();

		results.setActive(active.isSelected());

		switch (mode.getSelectedIndex()) {
		case 0: results.setMode(ModelLoadData.Mode.FILE); break;
		case 1: results.setMode(ModelLoadData.Mode.DDE); break;
		}
		results.setWorkbook(workbook.getText());
		results.setTable(sheet.getText());

		results.getList().clear();
		results.getList().addAll(listData);

		return results;
	}
}
