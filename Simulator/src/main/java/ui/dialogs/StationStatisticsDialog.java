package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Zeigt einen Dialog an, in dem eingestellt werden kann, für welche Stationen
 * eine Statistikaufzeichnung erfolgen soll.
 * @author Alexander Herzog
 * @see ModelElementPosition#isStationStatisticsActive()
 * @see ModelElementPosition#setStationStatisticsActive(boolean)
 */
public class StationStatisticsDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-3653892607306372543L;

	/** Liste der Stationen */
	private final List<Record> listRecords;

	/** Datenmodell für die Liste der Stationen */
	private final DefaultListModel<JCheckBox> listModel;

	/** Tabellendaten für die Intervalllängen */
	private IntervalLengthMode tableModel;

	/** Fußzeilen-Schaltfläche "Alle wählen" */
	private final JButton buttonSelectAll;
	/** Fußzeilen-Schaltfläche "Alle abwählen" */
	private final JButton buttonSelectNone;
	/** Fußzeilen-Schaltfläche "Für alle einstellen" */
	private final JButton buttonSetValue;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Zu bearbeitendes Modell
	 */
	public StationStatisticsDialog(final Component owner, final EditModel model) {
		super(owner,Language.tr("StationStatistics.Title"));

		JPanel tab, line;

		/* Liste der Stationen erstellen */
		listRecords=getElements(null,model.surface);

		/* GUI erstellen */
		addUserButton(Language.tr("StationStatistics.SelectAll"),Images.EDIT_ADD.getIcon());
		addUserButton(Language.tr("StationStatistics.SelectNone"),Images.EDIT_DELETE.getIcon());
		addUserButton(Language.tr("StationStatistics.SetValue"),Images.GENERAL_EDIT.getIcon());
		buttonSelectAll=getUserButton(0);
		buttonSelectNone=getUserButton(1);
		buttonSetValue=getUserButton(2);
		final JPanel content=createGUI(()->Help.topicModal(this,"StationStatisticsDialog"));
		content.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(content,InfoPanel.globalStationStatistics);

		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		/* Tab: "Statistikerfassung" */
		tabs.addTab(Language.tr("StationStatistics.TabStationStatistic"),tab=new JPanel(new BorderLayout()));
		tabs.addChangeListener(e->{
			buttonSelectAll.setEnabled(tabs.getSelectedIndex()==0);
			buttonSelectNone.setEnabled(tabs.getSelectedIndex()==0);
			buttonSetValue.setEnabled(tabs.getSelectedIndex()==1);
		});
		buttonSetValue.setEnabled(false);

		final JList<JCheckBox> list=new JList<>();
		tab.add(new JScrollPane(list),BorderLayout.CENTER);
		list.setCellRenderer(new JCheckBoxCellRenderer());
		list.setModel(listModel=new DefaultListModel<>());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index=list.locationToIndex(e.getPoint());
				if (index<0) return;
				final JCheckBox checkbox=list.getModel().getElementAt(index);
				checkbox.setSelected(!checkbox.isSelected());
				repaint();
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (Record record: listRecords) listModel.addElement(new JCheckBox(record.name,record.element.isStationStatisticsActive()));

		/* Tab: "Intervalllänge für maximalen Durchsatz" */
		tabs.addTab(Language.tr("StationStatistics.TabMaxThroughputIntervalLength"),tab=new JPanel(new BorderLayout()));
		final JTableExt table=new JTableExt();
		tab.add(new JScrollPane(table),BorderLayout.CENTER);
		table.setModel(tableModel=new IntervalLengthMode(table,listRecords));
		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		line.add(new JLabel(Language.tr("StationStatistics.MaxThroughputIntervalLength.Info")));

		/* Icons auf den Tabs */
		tabs.setIconAt(0,Images.SIMULATION_STATISTICS_STATISTICS.getIcon());
		tabs.setIconAt(1,Images.SIMULATION_STATISTICS_MAXTHROUGHPUT.getIcon());

		/* Dialog starten */
		setMinSizeRespectingScreensize(775,500);
		setSizeRespectingScreensize(775,500);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Liefert eine Liste der Stationen
	 * @param parent	Übergeordnetes Element oder <code>null</code>, wenn es um die Hauptebene geht
	 * @param surface	Zeichenfläche
	 * @return	Liste der Stationen
	 * @see Record
	 */
	private List<Record> getElements(final ModelElementSub parent, final ModelSurface surface) {
		final List<Record> list=new ArrayList<>();
		for (ModelElement element: surface.getElements()) {
			if (!(element instanceof ModelElementBox)) continue;
			final ModelElementBox box=(ModelElementBox)element;
			if (!box.inputConnected()) continue;

			if (box instanceof ModelElementSub) {
				final ModelElementSub sub=(ModelElementSub)box;
				list.addAll(getElements(sub,sub.getSubSurface()));
			} else {
				list.add(new Record(parent,box));
			}
		}
		return list;
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		switch (nr) {
		case 0:
			for (int i=0;i<listModel.getSize();i++) listModel.getElementAt(i).setSelected(true);
			repaint();
			break;
		case 1:
			for (int i=0;i<listModel.getSize();i++) listModel.getElementAt(i).setSelected(false);
			repaint();
			break;
		case 2:
			tableModel.setForAll();
			break;
		}
	}

	@Override
	protected boolean checkData() {
		final String error=tableModel.checkData();
		if (error!=null) {
			MsgBox.error(this,Language.tr("StationStatistics.MaxThroughputIntervalLength.ErrorTitle"),error);
			return false;
		}
		return true;
	}

	@Override
	public void storeData() {
		/* Tab: "Statistikerfassung" */
		for (int i=0;i<listModel.getSize();i++) {
			listRecords.get(i).element.setStationStatisticsActive(listModel.getElementAt(i).isSelected());
		}

		/* Tab: "Intervalllänge für maximalen Durchsatz" */
		tableModel.storeData();
	}

	/**
	 * Datensatz für eine Station
	 */
	private static class Record {
		/** Name der Station */
		public final String name;
		/** Modellelement der Station */
		public final ModelElementBox element;

		/**
		 * Konstruktor der Klasse
		 * @param parent	Übergeordnetes Element (oder <code>null</code>, wenn sich die Station auf der Hauptebene befindet)
		 * @param element	Modellelement der Station
		 */
		public Record(final ModelElementSub parent, final ModelElementBox element) {
			this.element=element;
			if (parent==null) {
				name=buildName(element);
			} else {
				name=buildName(parent)+" - "+buildName(element);
			}
		}

		/**
		 * Erstellt den Namen für die Station.
		 * @param element	Stationselement
		 * @return	Name der Station
		 */
		private static String buildName(final ModelElementBox element) {
			final StringBuilder name=new StringBuilder();
			name.append(element.getTypeName());
			name.append(String.format(" (id=%d)",element.getId()));
			if (!element.getName().isEmpty()) name.append(String.format(" \"%s\"",element.getName()));
			return name.toString();
		}
	}

	/**
	 * Renderer für die Elemente von {@link StationStatisticsDialog#listRecords}
	 * @see StationStatisticsDialog#listRecords
	 */
	private static class JCheckBoxCellRenderer implements ListCellRenderer<JCheckBox> {
		/**
		 * Konstruktor der Klasse
		 */
		public JCheckBoxCellRenderer() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value, int index, boolean isSelected, boolean cellHasFocus) {
			value.setForeground(list.getForeground());
			value.setBackground(list.getBackground());
			return value;
		}
	}

	/**
	 * Tabellenmodell zur Einstellung der Maximaldurchsatz Intervalllängen für die Stationen
	 */
	private static class IntervalLengthMode extends JTableExtAbstractTableModel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-6667177706908398310L;

		/**
		 * Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
		 */
		private final JTableExt table;

		/**
		 * Lister aller Stationen
		 */
		private final ModelElementBox[] stations;

		/**
		 * Liste der Namen der Stationen
		 * @see #stations
		 */
		private final String[] names;

		/**
		 * Liste der aktuellen Intervalllängen für die Stationen
		 * @see #stations
		 */
		private final String[] values;

		/**
		 * Konstruktor der Klasse
		 * @param table	Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
		 * @param listRecords	Liste der Stationen
		 */
		public IntervalLengthMode(final JTableExt table, final List<Record> listRecords) {
			this.table=table;
			stations=listRecords.stream().map(record->record.element).toArray(ModelElementBox[]::new);
			names=listRecords.stream().map(record->record.name).toArray(String[]::new);
			values=Stream.of(stations).map(station->station.getMaxThroughputIntervalSeconds()).map(i->""+i).toArray(String[]::new);
		}

		/**
		 * Aktualisiert die Tabellendarstellung
		 */
		private void updateTable() {
			fireTableDataChanged();
			TableCellEditor cellEditor=table.getCellEditor();
			if (cellEditor!=null) cellEditor.stopCellEditing();
		}

		@Override
		public int getRowCount() {
			return names.length;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0: return Language.tr("StationStatistics.MaxThroughputIntervalLength.ColNames");
			case 1: return Language.tr("StationStatistics.MaxThroughputIntervalLength.ColIntervalSeconds");
			default: return super.getColumnName(column);
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex==0) return names[rowIndex]; else return values[rowIndex];
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (!(aValue instanceof String) || columnIndex!=1) return;
			final String newValue=((String)aValue).trim();
			if (newValue.equals(values[rowIndex])) return;
			values[rowIndex]=newValue;
			checkData();
		}

		@Override
		public final boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex==1;
		}

		/**
		 * Prüft alle Eingaben.
		 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Meldung, die den Fehler beschreibt
		 */
		public String checkData() {
			updateTable();
			for (int i=0;i<stations.length;i++) {
				final Integer I=NumberTools.getInteger(values[i]);
				if (I==null) return String.format(Language.tr("StationStatistics.MaxThroughputIntervalLength.Error"),names[i],values[i]);
			}
			return null;
		}

		/**
		 * Schreibt die Daten in die Stationselemente zurück.
		 */
		public void storeData() {
			updateTable();
			for (int i=0;i<stations.length;i++) {
				final Integer I=NumberTools.getInteger(values[i]);
				if (I!=null) stations[i].setMaxThroughputIntervalSeconds(I);
			}
		}

		/**
		 * Zeigt einen Dialog zum Einstellen eines einheitlichen Intervalllängenwertes für alle Einträge an.
		 */
		public void setForAll() {
			String newValue=JOptionPane.showInputDialog(table,Language.tr("StationStatistics.MaxThroughputIntervalLength.SetForAllTitle"),"3600");
			if (newValue==null || newValue.isBlank()) return;
			newValue=newValue.trim();

			updateTable();
			for (int i=0;i<values.length;i++) values[i]=newValue;
			updateTable();
		}
	}
}