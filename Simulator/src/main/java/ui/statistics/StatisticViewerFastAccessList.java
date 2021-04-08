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
package ui.statistics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.SetupData;
import ui.images.Images;
import ui.script.ScriptTools;
import ui.statistics.ListPopup.ScriptHelperRecord;

/**
 * Ermöglicht die Filterung der Ergebnisse mit Hilfe von einfachen Listeneinträgen.
 * @author Alexander Herzog
 * @see StatisticViewerFastAccessBase
 * @see StatisticViewerFastAccess
 */
public class StatisticViewerFastAccessList extends StatisticViewerFastAccessBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7570159030451235613L;

	/** Referenz auf das Setup-Singleton */
	private final SetupData setup;

	/** Datenmodell für die Liste der Ausgabe-Anweisungen {@link #list} */
	private final DefaultListModel<FilterListRecord> listModel;
	/** Liste der Ausgabe-Anweisungen */
	private final JList<FilterListRecord> list;

	/** Zuletzt gespeichertes Skript */
	private String lastSavedFilterText;

	/** Schaltfläche "Eintrag hinzufügen" */
	private JButton toolbarAdd;
	/** Schaltfläche "Eintrag bearbeiten" */
	private JButton toolbarEdit;
	/** Schaltfläche "Eintrag löschen" */
	private JButton toolbarDelete;
	/** Schaltfläche "Eintrag in der Liste nach oben verschieben" */
	private JButton toolbarUp;
	/** Schaltfläche "Eintrag in der Liste nach unten verschieben" */
	private JButton toolbarDown;

	/**
	 * Konstruktor der Klasse
	 * @param helpFastAccess	Hilfe für Schnellzugriff-Seite
	 * @param helpFastAccessModal	Hilfe für Schnellzugriff-Dialog
	 * @param statistics	Statistik-Objekt, dem die Daten entnommen werden sollen
	 * @param resultsChanged	Runnable das aufgerufen wird, wenn sich die Ergebnisse verändert haben
	 */
	public StatisticViewerFastAccessList(final Runnable helpFastAccess, final Runnable helpFastAccessModal, final Statistics statistics, final Runnable resultsChanged) {
		super(helpFastAccess,helpFastAccessModal,statistics,resultsChanged,false);
		setup=SetupData.getSetup();
		lastSavedFilterText="";

		/* Filtertext */
		add(new JScrollPane(list=new JList<>(listModel=new DefaultListModel<>())),BorderLayout.CENTER);
		list.setCellRenderer(new FilterListRenderer());
		list.addListSelectionListener(e->selectionChanged());
		list.addMouseListener(new MouseAdapter() {
			@Override public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) commandEdit();
				if (SwingUtilities.isRightMouseButton(e)) commandPopup(e);
			}
		});
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_INSERT && !e.isControlDown() && !e.isShiftDown()) {commandAdd(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_ENTER && !e.isControlDown() && !e.isShiftDown()) {if (list.getSelectedIndex()<0) return; commandEdit(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_DELETE && !e.isControlDown() && !e.isShiftDown()) {if (list.getSelectedIndex()<0) return; commandDelete(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_UP && e.isControlDown() && !e.isShiftDown()) {commandUp(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_DOWN && e.isControlDown() && !e.isShiftDown()) {commandDown(); e.consume(); return;}
			}
		});

		/* Laden */
		loadTextToList(setup.filterList);
	}

	@Override
	protected Icon getIcon() {
		return Images.SCRIPT_MODE_LIST.getIcon();
	}

	@Override
	protected void addXML(final String selector) {
		final FilterListRecord record=new FilterListRecord();
		record.mode=FilterListRecord.Mode.XML;
		record.text=selector;
		listModel.addElement(record);
		process(false);
	}

	@Override
	protected void addCustomToolbarButtons(final JToolBar toolbar) {
		toolbar.addSeparator();

		toolbar.add(toolbarAdd=new JButton(Language.tr("Statistic.FastAccess.FilterList.Add")));
		toolbarAdd.setToolTipText(Language.tr("Statistic.FastAccess.FilterList.Add.Hint"));
		toolbarAdd.setIcon(Images.EDIT_ADD.getIcon());
		toolbarAdd.addActionListener(e->commandAdd());

		toolbar.add(toolbarEdit=new JButton(Language.tr("Statistic.FastAccess.FilterList.Edit")));
		toolbarEdit.setToolTipText(Language.tr("Statistic.FastAccess.FilterList.Edit.Hint"));
		toolbarEdit.setIcon(Images.GENERAL_SETUP.getIcon());
		toolbarEdit.addActionListener(e->commandEdit());

		toolbar.add(toolbarDelete=new JButton(Language.tr("Statistic.FastAccess.FilterList.Delete")));
		toolbarDelete.setToolTipText(Language.tr("Statistic.FastAccess.FilterList.Delete.Hint"));
		toolbarDelete.setIcon(Images.EDIT_DELETE.getIcon());
		toolbarDelete.addActionListener(e->commandDelete());

		toolbar.add(toolbarUp=new JButton(Language.tr("Statistic.FastAccess.FilterList.Up")));
		toolbarUp.setToolTipText(Language.tr("Statistic.FastAccess.FilterList.Up.Hint"));
		toolbarUp.setIcon(Images.ARROW_UP.getIcon());
		toolbarUp.addActionListener(e->commandUp());

		toolbar.add(toolbarDown=new JButton(Language.tr("Statistic.FastAccess.FilterList.Down")));
		toolbarDown.setToolTipText(Language.tr("Statistic.FastAccess.FilterList.Down.Hint"));
		toolbarDown.setIcon(Images.ARROW_DOWN.getIcon());
		toolbarDown.addActionListener(e->commandDown());
	}

	/**
	 * Aktualisiert den aktiv/deaktiviert Status
	 * der Schaltflächen in der Symbolleiste,
	 * wenn ein neuer Eintrag in der Liste
	 * selektiert wurde.
	 */
	private void selectionChanged() {
		final int count=listModel.size();
		final int index=list.getSelectedIndex();

		toolbarEdit.setEnabled(index>=0);
		toolbarDelete.setEnabled(index>=0);
		toolbarUp.setEnabled(index>0);
		toolbarDown.setEnabled(index>=0 && index<count-1);
	}

	/**
	 * Führt die Verarbeitung der definierten Ausgabereglen aus
	 * @param forceProcess	Verarbeitung erzwingen (<code>true</code>) auch wenn sich der Regelsatz seit der letzten Ausführung nicht verändert hat?
	 */
	public void process(final boolean forceProcess) {
		final FilterListFormat format=new FilterListFormat();
		final StringBuilder sb=new StringBuilder();
		for (int i=0;i<listModel.size();i++) {
			sb.append(listModel.getElementAt(i).process(statistics,format));
		}
		setResults(sb.toString());

		final String newFilterList=saveListToText();
		if (!newFilterList.equals(setup.filterList)) {
			setup.filterList=newFilterList;
			setup.saveSetup();
		}
	}

	/**
	 * Fügt einen Eintrag zu der Liste hinzu.
	 * @param helperRecord	Ausgewählter Menüpunkt
	 */
	private void addHelperRecord(final ScriptHelperRecord helperRecord) {
		final FilterListRecord record=new FilterListRecord();
		record.mode=FilterListRecord.Mode.XML;
		record.text=helperRecord.xml;
		listModel.addElement(record);
		list.setSelectedIndex(listModel.size()-1);
		list.setSelectedIndex(listModel.size()-1);
		process(false);
	}

	/**
	 * Fügt einen Eintrag zu der Liste hinzu.
	 * @param mode	Auszuführender Befehl
	 */
	private void addSpecialRecord(final FilterListRecord.Mode mode) {
		final FilterListRecord record=new FilterListRecord();
		record.mode=mode;
		listModel.addElement(record);
		list.setSelectedIndex(listModel.size()-1);
		list.setSelectedIndex(listModel.size()-1);
		process(false);
	}

	/**
	 * Zeigt den Dialog zum Hinzufügen von Listeneinträgen an.
	 * @param initialMode	Initial zu aktivierender Modus
	 * @see StatisticViewerFastAccessListDialog
	 */
	private void showAddDialog(final FilterListRecord.Mode initialMode) {
		FilterListRecord record=null;
		if (initialMode!=null) {
			record=new FilterListRecord();
			record.mode=initialMode;
		}

		final StatisticViewerFastAccessListDialog dialog=new StatisticViewerFastAccessListDialog(this,statistics,record,helpFastAccessModal,true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			listModel.addElement(dialog.getRecord());
			list.setSelectedIndex(listModel.size()-1);
			list.setSelectedIndex(listModel.size()-1);
			process(false);
		}
	}

	/**
	 * Befehl: Eintrag hinzufügen
	 * @see #toolbarAdd
	 */
	private void commandAdd() {
		ListPopup helper=new ListPopup(toolbarAdd,null);
		final JPopupMenu popupMenu=new JPopupMenu();

		helper.addSelectXML(popupMenu,statistics,record->addHelperRecord(record),helpFastAccessModal);
		popupMenu.add(ListPopup.getMenuItem(Language.tr("Statistic.FastAccess.Template.Expression"),Language.tr("Statistic.FastAccess.Template.Expression.Tooltip"),Images.SCRIPT_RECORD_EXPRESSION.getIcon(),e->showAddDialog(FilterListRecord.Mode.Expression)));
		popupMenu.addSeparator();
		popupMenu.add(ListPopup.getMenuItem(Language.tr("Statistic.FastAccess.Template.Tab"),Language.tr("Statistic.FastAccess.Template.Tab.Tooltip"),Images.SCRIPT_RECORD_TEXT.getIcon(),e->addSpecialRecord(FilterListRecord.Mode.Tabulator)));
		popupMenu.add(ListPopup.getMenuItem(Language.tr("Statistic.FastAccess.Template.NewLine"),Language.tr("Statistic.FastAccess.Template.NewLine.Tooltip"),Images.SCRIPT_RECORD_TEXT.getIcon(),e->addSpecialRecord(FilterListRecord.Mode.NewLine)));
		popupMenu.add(ListPopup.getMenuItem(Language.tr("Statistic.FastAccess.Template.Text"),Language.tr("Statistic.FastAccess.Template.Text.Tooltip"),Images.SCRIPT_RECORD_TEXT.getIcon(),e->showAddDialog(FilterListRecord.Mode.Text)));
		JMenu menu;
		popupMenu.add(menu=ListPopup.getSubMenu(Language.tr("Statistic.FastAccess.Template.Format"),Language.tr("Statistic.FastAccess.Template.Format.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon()));
		menu.add(ListPopup.getMenuItem(Language.tr("Statistic.FastAccess.Template.Format.System"),Language.tr("Statistic.FastAccess.Template.Format.System.Hint"),null,e->addSpecialRecord(FilterListRecord.Mode.FormatSystem)));
		menu.add(ListPopup.getMenuItem(Language.tr("Statistic.FastAccess.Template.Format.Local"),Language.tr("Statistic.FastAccess.Template.Format.Local.Hint"),null,e->addSpecialRecord(FilterListRecord.Mode.FormatLocal)));
		menu.addSeparator();
		menu.add(ListPopup.getMenuItem(Language.tr("Statistic.FastAccess.Template.Format.Fraction"),Language.tr("Statistic.FastAccess.Template.Format.Fraction.Hint"),null,e->addSpecialRecord(FilterListRecord.Mode.FormatFraction)));
		menu.add(ListPopup.getMenuItem(Language.tr("Statistic.FastAccess.Template.Format.Percent"),Language.tr("Statistic.FastAccess.Template.Format.Percent.Hint"),null,e->addSpecialRecord(FilterListRecord.Mode.FormatPercent)));
		menu.addSeparator();
		menu.add(ListPopup.getMenuItem(Language.tr("Statistic.FastAccess.Template.Format.Time"),Language.tr("Statistic.FastAccess.Template.Format.Time.Hint"),null,e->addSpecialRecord(FilterListRecord.Mode.FormatTime)));
		menu.add(ListPopup.getMenuItem(Language.tr("Statistic.FastAccess.Template.Format.Number"),Language.tr("Statistic.FastAccess.Template.Format.Number.Hint"),null,e->addSpecialRecord(FilterListRecord.Mode.FormatNumber)));
		popupMenu.addSeparator();

		helper.popupCustom(popupMenu,statistics,record->addHelperRecord(record),record->true);

		popupMenu.show(toolbarAdd,0,toolbarAdd.getHeight());
	}

	/**
	 * Befehl: Eintrag bearbeiten
	 * @see #toolbarEdit
	 */
	private void commandEdit() {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		final StatisticViewerFastAccessListDialog dialog=new StatisticViewerFastAccessListDialog(this,statistics,listModel.getElementAt(index),helpFastAccessModal,false);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			listModel.setElementAt(dialog.getRecord(),index);
			process(false);
		}
	}

	/**
	 * Befehl: Eintrag löschen
	 * @see #toolbarDelete
	 */
	private void commandDelete() {
		final int index=list.getSelectedIndex();
		if (index<0) return;
		listModel.removeElementAt(index);
		if (index>0) list.setSelectedIndex(index-1); else {
			if (listModel.size()>0) list.setSelectedIndex(0);
		}
		process(false);
	}

	/**
	 * Vertauscht zwei Einträge in der Liste
	 * @param index1	Index des ersten Eintrags
	 * @param index2	Index des zweiten Eintrags
	 */
	private void commandSwap(final int index1, final int index2) {
		final FilterListRecord record1=listModel.getElementAt(index1);
		final FilterListRecord record2=listModel.getElementAt(index2);
		listModel.setElementAt(record2,index1);
		listModel.setElementAt(record1,index2);
		process(false);
	}

	/**
	 * Befehl: Eintrag in der Liste nach oben verschieben
	 * @see #toolbarUp
	 */
	private void commandUp() {
		final int index=list.getSelectedIndex();
		if (index<1) return;
		commandSwap(index,index-1);
		list.setSelectedIndex(index-1);
	}

	/**
	 * Befehl: Eintrag in der Liste nach unten verschieben
	 * @see #toolbarDown
	 */
	private void commandDown() {
		final int index=list.getSelectedIndex();
		if (index<0 || index>=listModel.size()-1) return;
		commandSwap(index,index+1);
		list.setSelectedIndex(index+1);
	}

	/**
	 * Darf die Liste verworfen werden (ggf. Nutzer fragen)?
	 * @return	Liefert <code>true</code>, wenn die Liste verworfen werden darf
	 */
	private boolean discardOk() {
		final String text=saveListToText();
		if (text.trim().equals(lastSavedFilterText.trim())) return true;

		switch (MsgBox.confirmSave(getParent(),Language.tr("Filter.Save.Title"),Language.tr("Filter.Save.Info"))) {
		case JOptionPane.YES_OPTION: return saveToFile();
		case JOptionPane.NO_OPTION: return true;
		case JOptionPane.CANCEL_OPTION: return false;
		default: return false;
		}
	}

	@Override
	protected void commandNew() {
		if (!discardOk()) return;
		listModel.clear();
		lastSavedFilterText="";
		process(false);
	}

	/**
	 * Lädt die Listendarstellung aus einem Text
	 * @param text	Zuvor gespeicherte Listendarstellung
	 */
	private void loadTextToList(final String text) {
		final FilterList filterList=new FilterList();
		filterList.load(text);
		listModel.clear();
		for (FilterListRecord record: filterList.getList()) listModel.addElement(record);
		process(false);
		selectionChanged();
	}

	/**
	 * Liefert den Speicher-Text der Listendarstellung.
	 * @return	Speicher-Text der Listendarstellung
	 */
	private String saveListToText() {
		return FilterList.save(listModel.elements());
	}

	/**
	 * Lädt ein neues Skript.
	 * @return	Liefert <code>true</code>, wenn ein Skript geladen wurde
	 */
	private boolean loadIntern() {
		if (!discardOk()) return false;
		final String fileName=ScriptTools.selectTextFile(getParent(),null,null);
		if (fileName==null) return false;
		final File file=new File(fileName);

		try {
			final String text=String.join("\n",Files.lines(file.toPath()).toArray(String[]::new));
			loadTextToList(text);
			lastSavedFilterText=text;
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	protected void commandLoad() {
		loadIntern();
	}

	/**
	 * Speichert das aktuelle Skript in einer Datei.
	 * @return	Liefert <code>true</code>, wenn das Skript gespeichert werden konnte.
	 */
	private boolean saveToFile() {
		final String fileName=ScriptTools.selectTextSaveFile(getParent(),null,null);
		if (fileName==null) return false;
		final File file=new File(fileName);
		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(getParent(),file)) return false;
		}

		final String text=saveListToText();
		try {
			Files.write(file.toPath(),text.getBytes(),StandardOpenOption.CREATE);
		} catch (IOException e) {
			return false;
		}

		lastSavedFilterText=text;

		return true;
	}

	@Override
	protected void commandSave() {
		saveToFile();
	}

	@Override
	protected void commandTools(JButton sender) {
	}

	/**
	 * Erstellt auf Basis der Daten einer Schaltfläche einen Menüpunkt.
	 * @param button	Ausgangs-Schaltfläche
	 * @param popup	Popupmenü dem der neue Menüpunkt hinzugefügt werden soll
	 * @param keyStroke	Optionaler Hotkey für den Menüpunkt (kann <code>null</code> sein)
	 */
	private void buttonToPopup(final JButton button, final JPopupMenu popup, final KeyStroke keyStroke) {
		final JMenuItem item=new JMenuItem(button.getText());
		if (button.getIcon()!=null) item.setIcon(button.getIcon());
		item.setToolTipText(button.getToolTipText());
		item.setEnabled(button.isEnabled());
		for (ActionListener listener: button.getActionListeners()) item.addActionListener(listener);
		if (keyStroke!=null) item.setAccelerator(keyStroke);
		popup.add(item);
	}

	/**
	 * Zeigt das Popupmenü zu einem Eintrag in
	 * {@link StatisticViewerFastAccessList#list} an.
	 * @param event	Auslösendes Maus-Ereignis (zur Ausrichtung des Popupmenüs)
	 */
	private void commandPopup(final MouseEvent event) {
		final JPopupMenu popup=new JPopupMenu();

		buttonToPopup(toolbarAdd,popup,KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,0));
		popup.addSeparator();
		buttonToPopup(toolbarEdit,popup,KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));
		buttonToPopup(toolbarDelete,popup,KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
		popup.addSeparator();
		buttonToPopup(toolbarUp,popup,KeyStroke.getKeyStroke(KeyEvent.VK_UP,InputEvent.CTRL_DOWN_MASK));
		buttonToPopup(toolbarDown,popup,KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.CTRL_DOWN_MASK));

		popup.show(list,event.getX(),event.getY());
	}

	/**
	 * Renderer für die Einträge in
	 * {@link StatisticViewerFastAccessList#list}
	 * @see StatisticViewerFastAccessList#list
	 */
	private static class FilterListRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -1704779702001655809L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			final Component result=super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
			if ((result instanceof JLabel) && (value instanceof FilterListRecord)) {
				((FilterListRecord)value).writeToJLabel((JLabel)result);
			}

			return result;
		}
	}
}