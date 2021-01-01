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

import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;

/**
 * Datenmodell für die Tabelle in {@link ModelElementSourceBatchEditor2}
 * @author Alexander Herzog
 * @see ModelElementSourceBatchEditor2
 */
public class ModelElementSourceBatchEditor2TableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-3320042641922923165L;

	/**
	 * Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
	 */
	private final JTableExt table;

	/**
	 * Runnable das ausgelöst wird, wenn die Daten verändert wurden
	 */
	private final Runnable updateTrigger;

	/**
	 * Batch-Größen
	 */
	private final List<Integer> sizes;

	/**
	 * Zu {@link #sizes} zugehörige Raten
	 */
	private final List<String> rates;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle (um das Update der Tabelle veranlassen zu können, wenn sich die Daten verändert haben)
	 * @param updateTrigger	Runnable das ausgelöst werden soll, wenn die Daten verändert wurden
	 */
	public ModelElementSourceBatchEditor2TableModel(final JTableExt table, final Runnable updateTrigger) {
		this.table=table;
		this.updateTrigger=updateTrigger;
		sizes=new ArrayList<>();
		sizes.add(1);
		rates=new ArrayList<>();
		rates.add("1");
	}

	/**
	 * Aktualisiert die Tabellendarstellung
	 */
	private void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	public boolean checkData(boolean showErrorMessage) {
		boolean ok=true;
		double sum=0.0;

		for (int i=0;i<sizes.size();i++) {
			final Double D=NumberTools.getDouble(rates.get(i));
			if (D==null || D<0) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(table,Language.tr("Surface.Source.DialogBatchSize.Table.ErrorTitle"),String.format(Language.tr("Surface.Source.DialogBatchSize.Table.ErrorInfo"),sizes.get(i),rates.get(i)));
					return false;
				}
			} else {
				sum+=D;
			}
		}

		if (ok && sum==0.0) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(table,Language.tr("Surface.Source.DialogBatchSize.Rate.ErrorSum.Title"),Language.tr("Surface.Source.DialogBatchSize.Rate.ErrorSum.Info"));
				return false;
			}
		}

		return ok;
	}

	/**
	 * Stellt neue Batch-Raten ein.
	 * @param distribution	Batch-Raten
	 */
	public void setDistribution(double[] distribution) {
		if (distribution==null || distribution.length==0) return;

		sizes.clear();
		rates.clear();
		for (int i=0;i<distribution.length;i++) if (distribution[i]>0) {
			sizes.add(i+1);
			rates.add(NumberTools.formatNumber(distribution[i],3));
		}
		if (sizes.size()==0) {
			sizes.add(1);
			rates.add("1");
		}

		updateTable();
	}

	/**
	 * Liefert die aktuellen Batch-Raten.
	 * @return	Batch-Raten
	 */
	public double[] getDistribution() {
		int max=-1;
		for (int i=0;i<sizes.size();i++) {
			final Double D=NumberTools.getDouble(rates.get(i));
			if (D!=null && D>=0) max=Math.max(max,sizes.get(i));
		}

		if (max<=0) return null;

		final double[] result=new double[max];
		for (int i=0;i<sizes.size();i++) {
			final Double D=NumberTools.getDouble(rates.get(i));
			if (D!=null && D>=0) {
				result[sizes.get(i)-1]=D;
			}
		}

		return result;
	}

	@Override
	public int getRowCount() {
		return sizes.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.Source.DialogBatchSize.TableColumn.Size");
		case 1: return Language.tr("Surface.Source.DialogBatchSize.TableColumn.Rate");
		case 2: return "";
		default: return super.getColumnName(column);
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex<sizes.size()) {
			/* Normale Zeile */
			switch (columnIndex) {
			case 0:
				return sizes.get(rowIndex);
			case 1:
				return rates.get(rowIndex);
			case 2:
				final List<String> title=new ArrayList<>();
				final List<String> tooltip=new ArrayList<>();
				final List<Icon> icons=new ArrayList<>();
				final List<ActionListener> listener=new ArrayList<>();
				title.add("");
				tooltip.add(Language.tr("Surface.Source.DialogBatchSize.Table.Edit"));
				icons.add(Images.GENERAL_SETUP.getIcon());
				listener.add(e->editSize(rowIndex));
				if (sizes.size()>1) {
					title.add("");
					tooltip.add(Language.tr("Surface.Source.DialogBatchSize.Table.Delete"));
					icons.add(Images.EDIT_DELETE.getIcon());
					listener.add(e->deleteSize(rowIndex));
				}
				return makeButtonPanel(title.toArray(new String[0]),tooltip.toArray(new String[0]),icons.toArray(new Icon[0]),listener.toArray(new ActionListener[0]));
			default:
				return "";
			}

		} else {
			/* Letzte Zeile */
			if (columnIndex==2) {
				return makeButtonPanel(
						new String[] {""},
						new String[] {Language.tr("Surface.Source.DialogBatchSize.Table.Add")},
						new Icon[] {Images.EDIT_ADD.getIcon()},
						new ActionListener[] {e->addSize()});
			} else {
				return "";
			}
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex!=1 || rowIndex<0 || rowIndex>=sizes.size()) return;
		if (!(aValue instanceof String)) return;
		final String newRate=(String)aValue;
		if (rates.get(rowIndex).equals(newRate)) return;
		rates.set(rowIndex,newRate);
		updateTrigger.run();
	}

	/**
	 * Sortiert die Batch-Raten-Zuordnung nach der Veränderung eines
	 * Batch-Größeneintrags neu nach aufsteigenden Batch-Größen.
	 */
	private void sort() {
		final int[] sortedSizes=sizes.stream().mapToInt(Integer::intValue).sorted().toArray();

		final List<Integer> oldSizes=new ArrayList<>(sizes);
		final List<String> oldRates=new ArrayList<>(rates);

		sizes.clear();
		rates.clear();

		for (int i=0;i<sortedSizes.length;i++) {
			final int index=oldSizes.indexOf(sortedSizes[i]);
			sizes.add(oldSizes.get(index));
			rates.add(oldRates.get(index));
		}
	}

	/**
	 * Fügt einen Eintrag zu der Tabelle hinzu.
	 */
	private void addSize() {
		int nextSize=1;
		while (sizes.contains(nextSize)) nextSize++;

		while (true) {
			final String input=JOptionPane.showInputDialog(table,Language.tr("Surface.Source.DialogBatchSize.TableColumn.Size"),""+nextSize);
			if (input==null) return;
			final Long L=NumberTools.getPositiveLong(input);
			if (L==null) {
				MsgBox.error(table,Language.tr("Surface.Source.DialogBatchSize.TableColumn.Size.ErrorTitle"),String.format(Language.tr("Surface.Source.DialogBatchSize.TableColumn.Size.ErrorInfo"),input));
				continue;
			}
			final int size=L.intValue();
			if (sizes.contains(size)) {
				MsgBox.error(table,Language.tr("Surface.Source.DialogBatchSize.TableColumn.Size.ContainsErrorTitle"),String.format(Language.tr("Surface.Source.DialogBatchSize.TableColumn.Size.ContainsErrorInfo"),input));
				continue;
			}
			sizes.add(size);
			rates.add("1");
			break;
		}

		sort();
		updateTable();
		updateTrigger.run();
	}

	/**
	 * Bearbeitet die Batch-Größe für einen Eintrag
	 * @param index	Zeile deren Batch-Größe verändert werden soll
	 */
	public void editSize(final int index) {
		if (index<0 || index>=sizes.size()) return;

		int oldSize=sizes.get(index);
		while (true) {
			final String input=JOptionPane.showInputDialog(table,Language.tr("Surface.Source.DialogBatchSize.TableColumn.Size"),""+oldSize);
			if (input==null) return;
			final Long L=NumberTools.getPositiveLong(input);
			if (L==null) {
				MsgBox.error(table,Language.tr("Surface.Source.DialogBatchSize.TableColumn.Size.ErrorTitle"),String.format(Language.tr("Surface.Source.DialogBatchSize.TableColumn.Size.ErrorInfo"),input));
				continue;
			}
			final int size=L.intValue();
			if (size==oldSize) return; /* Nichts geändert. */
			if (sizes.contains(size)) {
				MsgBox.error(table,Language.tr("Surface.Source.DialogBatchSize.TableColumn.Size.ContainsErrorTitle"),String.format(Language.tr("Surface.Source.DialogBatchSize.TableColumn.Size.ContainsErrorInfo"),input));
				continue;
			}

			sizes.set(index,size);
			break;
		}

		sort();
		updateTable();
		updateTrigger.run();
	}

	/**
	 * Löscht einen Batch-Größen-Datensatz
	 * @param index	Zu löschende Zeile
	 */
	private void deleteSize(final int index) {
		if (index<0 || index>=sizes.size()) return;

		sizes.remove(index);
		rates.remove(index);
		updateTable();
		updateTrigger.run();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex==2) || ((columnIndex==1) && rowIndex<sizes.size());
	}
}
