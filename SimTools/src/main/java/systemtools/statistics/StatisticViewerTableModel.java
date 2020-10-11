package systemtools.statistics;

import java.io.Serializable;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import mathtools.Table;

/**
 * Tabellenmodell für eine {@link StatisticViewerTable}
 * @author Alexander Herzog
 * @see StatisticViewerTable
 */
public class StatisticViewerTableModel extends AbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=598876378208710137L;

	/** Option zur Speicherung der Daten in der Tabelle */
	private final List<List<String>> data;

	/** Weitere Option zur Speicherung der Daten in der Tabelle */
	private final Table table;

	/** Spaltenüberschriften */
	private final List<String> columnNames;

	/**
	 * Konstruktor der Klasse
	 * @param data	Daten in der Tabelle
	 * @param columnNames	Spaltennnamen
	 */
	public StatisticViewerTableModel(final List<List<String>> data, final List<String> columnNames) {
		this.data=data;
		this.table=null;
		this.columnNames=columnNames;
	}

	/**
	 * Konstruktor der Klasse
	 * @param table	Daten in der Tabelle
	 * @param columnNames	Spaltennnamen
	 */
	public StatisticViewerTableModel(final Table table, final List<String> columnNames) {
		this.data=null;
		this.table=table;
		this.columnNames=columnNames;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public int getRowCount() {
		if (table==null) {
			if (data==null || data.isEmpty()) return 0;
			return data.size();
		} else {
			return table.getSize(0);
		}
	}

	@Override
	public int getColumnCount() {
		if (columnNames==null) return 0;
		return columnNames.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex<0 || columnIndex<0 ) return "";

		if (table==null) {
			if (data==null || data.isEmpty()) return "";
			if (rowIndex>=data.size()) return "";
			final List<String> row=data.get(rowIndex);
			if (row==null || columnIndex>=row.size()) return "";
			return row.get(columnIndex);
		} else {
			return table.getValue(rowIndex,columnIndex);
		}
	}

	@Override
	public String getColumnName(int column) {
		if (columnNames==null) return "";
		if (column<0 || column>=columnNames.size()) return "";
		return columnNames.get(column);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}
}
