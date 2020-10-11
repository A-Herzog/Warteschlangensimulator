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
package ui.modeleditor.coreelements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;

import language.Language;
import mathtools.Table;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.elements.ModelElementAnimationBarChart;
import ui.modeleditor.elements.ModelElementAnimationLineDiagram;
import ui.modeleditor.elements.ModelElementAnimationRecord;

/**
 * Zeigt aktuelle Simulationstabellendaten w�hrend der Animation zu einer bestimmten Station an
 * @author Alexander Herzog
 * @see ModelElementAnimationRecord
 * @see ModelElementAnimationBarChart
 * @see ModelElementAnimationLineDiagram
 */
public class ModelElementAnimationTableDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7508979174535787490L;

	/** Timer f�r automatische Aktualisierungen */
	private Timer timer=null;
	/** Datenmodell f�r die Ausgabetabelle */
	private final TableTableModel tableModel;
	/** Schaltfl�che zum Umschalten zwischen automatischer und manueller Aktualisierung */
	private final JButton buttonAutoUpdate;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationTableDialog</code>
	 * @param owner	�bergeordnetes Fenster
	 * @param title	Anzuzeigender Titel
	 * @param info	Anzuzeigende Tabelle im Content-Bereich
	 */
	public ModelElementAnimationTableDialog(final Component owner, final String title, final Supplier<Table> info) {
		super(owner,title);

		timer=null;

		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(ModelElementAnimationTableDialog.this.owner,"AnimationStatistics"));
		content.setLayout(new BorderLayout());

		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);
		addButton(toolbar,Language.tr("Dialog.Button.Copy"),Images.EDIT_COPY.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.CopyHint"),e->commandCopy());
		addButton(toolbar,Language.tr("Dialog.Button.Save"),Images.GENERAL_SAVE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveHint"),e->commandSave());
		toolbar.addSeparator();
		addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Update"),Images.ANIMATION_DATA_UPDATE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateHint"),e->commandUpdate());
		buttonAutoUpdate=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdate"),Images.ANIMATION_DATA_UPDATE_AUTO.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdateHint"),e->commandAutoUpdate());

		tableModel=new TableTableModel(info);
		content.add(new JScrollPane(new JTable(tableModel)),BorderLayout.CENTER);

		setMinSizeRespectingScreensize(400,300);
		setSizeRespectingScreensize(800,600);
		setLocationRelativeTo(this.owner);
		setResizable(true);
		setVisible(true);
	}

	private JButton addButton(final JToolBar toolbar, final String name, final Icon icon, final String hint, final ActionListener listener) {
		final JButton button=new JButton(name);
		if (icon!=null) button.setIcon(icon);
		if (hint!=null && !hint.trim().isEmpty()) button.setToolTipText(hint);
		button.addActionListener(listener);
		toolbar.add(button);
		return button;
	}

	private void commandCopy() {
		buttonAutoUpdate.setSelected(false);
		if (timer!=null) timer.cancel();

		copyTable(tableModel.table);
	}

	private void commandSave() {
		buttonAutoUpdate.setSelected(false);
		if (timer!=null) timer.cancel();

		saveTable(this,tableModel.table);
	}

	private static void copyTable(final Table table) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(table.toStringTabs()),null);
	}

	private static void saveTable(final Component owner, final Table table) {
		final File file=Table.showSaveDialog(owner,Language.tr("FileType.Save.Table"));
		if (file==null) return;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		if (file.isFile()) {
			if (!file.delete()) {
				MsgBox.error(owner,Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Title"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Info"),file.toString()));
				return;
			}
		}

		if (!table.save(file)) {
			MsgBox.error(owner,Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Title"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Info"),file.toString()));
		}
	}

	private void commandUpdate() {
		tableModel.updateData();
	}

	private void commandAutoUpdate() {
		buttonAutoUpdate.setSelected(!buttonAutoUpdate.isSelected());

		if (buttonAutoUpdate.isSelected()) {
			timer=new Timer();
			timer.schedule(new TimerTask() {
				@Override public synchronized void run() {if (buttonAutoUpdate.isSelected()) commandUpdate();}
			},100,250);
		} else {
			if (timer!=null) {timer.cancel(); timer=null;}
		}
	}

	@Override
	protected boolean closeButtonOK() {
		if (timer!=null) {timer.cancel(); timer=null;}
		return true;
	}

	/**
	 * Datenmodell f�r die Ausgabetabelle
	 * @see ModelElementAnimationTableDialog#tableModel
	 */
	private static class TableTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -8304016520194169458L;

		private final Supplier<Table> tableGetter;
		public Table table;

		public TableTableModel(final Supplier<Table> tableGetter) {
			this.tableGetter=tableGetter;
			updateData();
		}

		public void updateData() {
			table=tableGetter.get();
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return table.getSize(0)-1;
		}

		@Override
		public int getColumnCount() {
			return table.getSize(1);
		}

		@Override
		public String getColumnName(int column) {
			return table.getValue(0,column);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return table.getValue(rowIndex+1,columnIndex);
		}
	}

	/**
	 * F�gt Men�punkte zum Kopieren und Speichern der Daten aus einer Tabelle in ein Kontextmen�
	 * ein, so dass die Funktionen f�r eine Tabelle auch ohne das �ffnen dieses Dialogs zur Verf�gung stehen.
	 * @param owner	�bergeordnetes Element (zum Ausrichten von Speichern-Dialogen)
	 * @param menu	Popupmen� in das die Eintr�ge angeh�ngt werden sollen
	 * @param table	Tabelle deren Daten gespeichert oder kopiert werden sollen
	 */
	public static void buildPopupMenuItem(final Component owner, final JPopupMenu menu, final Table table) {
		JMenuItem item;

		menu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Copy"),Images.EDIT_COPY.getIcon()));
		item.addActionListener(e->copyTable(table));

		menu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Save"),Images.GENERAL_SAVE.getIcon()));
		item.addActionListener(e->saveTable(owner,table));
	}
}
