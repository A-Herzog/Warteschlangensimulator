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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import systemtools.statistics.StatisticViewerBarChart;
import systemtools.statistics.StatisticViewerJFreeChart;
import systemtools.statistics.StatisticViewerLineChart;
import systemtools.statistics.StatisticViewerPieChart;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.elements.ModelElementAnimationBarChart;
import ui.modeleditor.elements.ModelElementAnimationLineDiagram;
import ui.modeleditor.elements.ModelElementAnimationPieChart;
import ui.modeleditor.elements.ModelElementAnimationRecord;

/**
 * Zeigt aktuelle Simulationstabellendaten während der Animation zu einer bestimmten Station an
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

	/** Dialogtitel */
	private final String title;
	/** Timer für automatische Aktualisierungen */
	private Timer timer=null;
	/** Registerreiter (kann <code>null</code> sein, wenn nur eine Tabelle vorhanden ist) */
	private final JTabbedPane tabs;
	/** Panel in dem Diagram-Tab */
	private final JPanel diagramTab;
	/** Datenmodell für die Ausgabetabelle */
	private final TableTableModel tableModel;
	/** Diagrammanzeige */
	private StatisticViewerJFreeChart chart;
	/** Element dem die Daten entnommen wurden (kann <code>null</code> sein) */
	private final ModelElementPosition element;
	/** Schaltfläche um bei den Diagrammen den Standardzoomfaktor wiederherzustellen */
	private final JButton buttonUnzoom;
	/** Schaltfläche zum Umschalten zwischen automatischer und manueller Aktualisierung */
	private final JButton buttonAutoUpdate;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationTableDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param title	Anzuzeigender Titel
	 * @param info	Anzuzeigende Tabelle im Content-Bereich
	 * @param element	Element dem die Daten entnommen wurden (kann <code>null</code> sein)
	 */
	public ModelElementAnimationTableDialog(final Component owner, final String title, final Supplier<Table> info, final ModelElementPosition element) {
		super(owner,title);

		/* Daten in Element übernehmen */
		this.title=title;
		if ((element instanceof ModelElementAnimationBarChart) || (element instanceof ModelElementAnimationLineDiagram) || (element instanceof ModelElementAnimationPieChart)) {
			this.element=element;
		} else {
			this.element=null;
		}
		timer=null;

		/* GUI vorbereiten */
		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(ModelElementAnimationTableDialog.this.owner,"AnimationStatistics"));
		content.setLayout(new BorderLayout());

		/* Toolbar */
		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);
		addButton(toolbar,Language.tr("Dialog.Button.Copy"),Images.EDIT_COPY.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.CopyHint"),e->commandCopy());
		addButton(toolbar,Language.tr("Dialog.Button.Save"),Images.GENERAL_SAVE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveHint"),e->commandSave());
		if (this.element!=null) {
			buttonUnzoom=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Unzoom"),Images.ZOOM.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.UnzoomHint"),e->commandUnzoom());
			buttonUnzoom.setVisible(false);
		} else {
			buttonUnzoom=null;
		}

		toolbar.addSeparator();
		addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Update"),Images.ANIMATION_DATA_UPDATE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateHint"),e->commandUpdate());
		buttonAutoUpdate=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdate"),Images.ANIMATION_DATA_UPDATE_AUTO.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdateHint"),e->commandAutoUpdate());

		if (this.element==null) {
			/* Nur Tabelle */
			tabs=null;
			diagramTab=null;
			tableModel=new TableTableModel(info);
			content.add(new JScrollPane(new JTable(tableModel)),BorderLayout.CENTER);
		} else {
			/* Tabelle und Diagramm */
			tabs=new JTabbedPane();
			content.add(tabs,BorderLayout.CENTER);
			JPanel tab;
			tabs.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.Table"),tab=new JPanel(new BorderLayout()));
			tableModel=new TableTableModel(info);
			tab.add(new JScrollPane(new JTable(tableModel)),BorderLayout.CENTER);
			tabs.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.Diagram"),tab=new JPanel(new BorderLayout()));
			if (this.element instanceof ModelElementAnimationBarChart) chart=new ElementDataBarChart((ModelElementAnimationBarChart)this.element);
			if (this.element instanceof ModelElementAnimationLineDiagram) chart=new ElementDataLineChart((ModelElementAnimationLineDiagram)this.element);
			if (this.element instanceof ModelElementAnimationPieChart) chart=new ElementDataPieChart((ModelElementAnimationPieChart)this.element);
			diagramTab=tab;
			SwingUtilities.invokeLater(()->{
				final Container c=chart.getViewer(true);
				if (c!=null) diagramTab.add(c,BorderLayout.CENTER);
			});
			tabs.setIconAt(0,Images.GENERAL_TABLE.getIcon());
			tabs.setIconAt(1,new ImageIcon(element.getAddElementIcon()));
			tabs.addChangeListener(e->buttonUnzoom.setVisible(tabs.getSelectedIndex()==1));
		}

		/* Dialog starten */
		setMinSizeRespectingScreensize(400,300);
		setSizeRespectingScreensize(800,600);
		setLocationRelativeTo(this.owner);
		setResizable(true);
		setVisible(true);
	}

	/**
	 * Erstellt eine neue Schaltfläche und fügt sie zur Symbolleiste hinzu.
	 * @param toolbar	Symbolleiste auf der die neue Schaltfläche eingefügt werden soll
	 * @param name	Beschriftung der Schaltfläche
	 * @param hint	Tooltip für die Schaltfläche (darf <code>null</code> sein)
	 * @param icon	Optionales Icon für die Schaltfläche (darf <code>null</code> sein)
	 * @param listener	Aktion die beim Anklicken der Schaltfläche ausgeführt werden soll
	 * @return	Neue Schaltfläche (ist bereits in die Symbolleiste eingefügt)
	 */
	private JButton addButton(final JToolBar toolbar, final String name, final Icon icon, final String hint, final ActionListener listener) {
		final JButton button=new JButton(name);
		if (icon!=null) button.setIcon(icon);
		if (hint!=null && !hint.trim().isEmpty()) button.setToolTipText(hint);
		button.addActionListener(listener);
		toolbar.add(button);
		return button;
	}

	/**
	 * Befehl: Angezeigte Daten in die Zwischenablage kopieren
	 */
	private void commandCopy() {
		buttonAutoUpdate.setSelected(false);
		if (timer!=null) timer.cancel();

		if (tabs==null || tabs.getSelectedIndex()==0) {
			copyTable(tableModel.table);
		} else {
			if (chart!=null) chart.copyToClipboard(Toolkit.getDefaultToolkit().getSystemClipboard());
		}
	}

	/**
	 * Befehl: Angezeigte Daten speichern
	 */
	private void commandSave() {
		buttonAutoUpdate.setSelected(false);
		if (timer!=null) timer.cancel();

		if (tabs==null || tabs.getSelectedIndex()==0) {
			saveTable(this,tableModel.table);
		} else {
			if (chart!=null) chart.save(this);
		}
	}

	/**
	 * Befehl: Standardzoomfaktor wiederherstellen
	 */
	private void commandUnzoom() {
		if (chart!=null) chart.unZoom();
	}

	/**
	 * Kopiert die Daten einer Tabelle in die Zwischenablage.
	 * @param table	Zu kopierende Tabelle
	 * @see #commandCopy()
	 */
	private static void copyTable(final Table table) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(table.toStringTabs()),null);
	}

	/**
	 * Speichert die Daten einer Tabelle in einer Datei.
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Dialogs zur Abfrage des Dateinamens)
	 * @param table	Zu speichernde Tabelle
	 * @see #commandSave()
	 */
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

	/**
	 * Befehl: Anzeige aktualisieren
	 */
	private void commandUpdate() {
		tableModel.updateData();

		if (chart!=null) {
			((ElementDataDiagram)chart).updateData();
			final Container c=chart.getViewer(true);
			if (c!=null) {
				diagramTab.removeAll();
				diagramTab.add(c,BorderLayout.CENTER);
				diagramTab.doLayout();
			}
		}
	}

	/**
	 * Befehl: Anzeige automatisch aktualisieren (an/aus)
	 */
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
	 * Datenmodell für die Ausgabetabelle
	 * @see ModelElementAnimationTableDialog#tableModel
	 */
	private static class TableTableModel extends AbstractTableModel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -8304016520194169458L;

		/**
		 * Getter für die anzuzeigende Tabelle im Content-Bereich
		 */
		private final Supplier<Table> tableGetter;

		/**
		 * Anzuzeigende Tabelle im Content-Bereich
		 */
		public Table table;

		/**
		 * Konstruktor der Klasse
		 * @param tableGetter	Getter für die anzuzeigende Tabelle im Content-Bereich
		 */
		public TableTableModel(final Supplier<Table> tableGetter) {
			this.tableGetter=tableGetter;
			updateData();
		}

		/**
		 * Aktualisiert {@link #table} mit Hilfe von {@link #tableGetter}.
		 * @see #table
		 * @see #tableGetter
		 */
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
	 * Fügt Menüpunkte zum Kopieren und Speichern der Daten aus einer Tabelle in ein Kontextmenü
	 * ein, so dass die Funktionen für eine Tabelle auch ohne das Öffnen dieses Dialogs zur Verfügung stehen.
	 * @param owner	Übergeordnetes Element (zum Ausrichten von Speichern-Dialogen)
	 * @param menu	Popupmenü in das die Einträge angehängt werden sollen
	 * @param table	Tabelle deren Daten gespeichert oder kopiert werden sollen
	 */
	public static void buildPopupMenuItem(final Component owner, final JPopupMenu menu, final Table table) {
		JMenuItem item;

		menu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Copy"),Images.EDIT_COPY.getIcon()));
		item.addActionListener(e->copyTable(table));

		menu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Save"),Images.GENERAL_SAVE.getIcon()));
		item.addActionListener(e->saveTable(SwingUtilities.windowForComponent(owner),table));
	}

	/**
	 * Gemeinsames Interface für die Diagramme in diesem Dialog
	 */
	private interface ElementDataDiagram {
		/**
		 * Aktualisierung der Diagrammdaten
		 */
		void updateData();
	}

	/**
	 * Balkendiagramm zur Anzeige in diesem Dialog
	 */
	private class ElementDataBarChart extends StatisticViewerBarChart implements ElementDataDiagram {
		/**
		 * Element dem die Farbinformationen entnommen werden sollen
		 */
		private final ModelElementAnimationBarChart element;

		/**
		 * Konstruktor der Klasse
		 * @param element	Element dem die Farbinformationen entnommen werden sollen
		 */
		public ElementDataBarChart(final ModelElementAnimationBarChart element) {
			this.element=element;
			updateData();
		}

		@Override
		public void updateData() {
			final Table table=tableModel.table;
			if (table.getSize(0)==0 || table.getSize(1)<2) return;

			initBarChart(title);
			setupBarChart(title,table.getValue(0,0),table.getValue(0,1),false);

			for (int i=1;i<table.getSize(0);i++) {
				final Double D=NumberTools.getDouble(table.getValue(i,1));
				final String name=table.getValue(i,0);
				data.addValue(D,name,name);
				plot.getRendererForDataset(data).setSeriesPaint(i-1,(Color)element.getExpressionData().get(i-1)[1]);
			}

			initTooltips();
			setOutlineColor(Color.BLACK);
		}
	}

	/**
	 * Liniendiagramm zur Anzeige in diesem Dialog
	 */
	private class ElementDataLineChart extends StatisticViewerLineChart implements ElementDataDiagram {
		/**
		 * Element dem die Farbinformationen entnommen werden sollen
		 */
		private final ModelElementAnimationLineDiagram element;

		/**
		 * Konstruktor der Klasse
		 * @param element	Element dem die Farbinformationen entnommen werden sollen
		 */
		public ElementDataLineChart(final ModelElementAnimationLineDiagram element) {
			this.element=element;
			updateData();
		}

		@Override
		public void updateData() {
			final Table table=tableModel.table;
			if (table.getSize(0)==0 || table.getSize(1)<2) return;

			initLineChart(title);
			setupChartValue(title,table.getValue(0,0),Language.tr("Statistics.Value"));

			final Table table2=table.transpose();
			for (int i=1;i<table2.getSize(0);i++) {
				final List<String> line=table2.getLine(i);
				final String name=line.get(0);
				line.remove(0);
				final double[] data=line.stream().mapToDouble(s->NumberTools.getDouble(s)).toArray();
				addSeries(name,(Color)element.getExpressionData().get(i-1)[3],data);
			}

			smartZoom(1);
		}
	}

	/**
	 * Tortendiagramm zur Anzeige in diesem Dialog
	 */
	private class ElementDataPieChart extends StatisticViewerPieChart implements ElementDataDiagram {
		/**
		 * Element dem die Farbinformationen entnommen werden sollen
		 */
		private final ModelElementAnimationPieChart element;

		/**
		 * Konstruktor der Klasse
		 * @param element	Element dem die Farbinformationen entnommen werden sollen
		 */
		public ElementDataPieChart(final ModelElementAnimationPieChart element) {
			this.element=element;
			updateData();
		}

		@Override
		public void updateData() {
			final Table table=tableModel.table;
			if (table.getSize(0)==0 || table.getSize(1)<2) return;

			initPieChart(title);

			for (int i=1;i<table.getSize(0);i++) {
				final Double D=NumberTools.getDouble(table.getValue(i,1));
				final String name=table.getValue(i,0);
				addPieSegment(name,D,(Color)element.getExpressionData().get(i-1)[1]);
			}
		}
	}
}