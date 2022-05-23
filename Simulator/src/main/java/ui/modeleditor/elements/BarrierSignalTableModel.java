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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.table.TableCellEditor;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;

/**
 * Tabellendarstellung der möglichen Signale zur Öffnung einer Schranke
 * innerhalb eines {@link ModelElementBarrier}-Dialogs
 * @author Alexander Herzog
 * @see ModelElementBarrier
 * @see ModelElementBarrierSignalOption
 */
public class BarrierSignalTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8386629305184893632L;

	/** Zugehörige Tabelle (um diese veranlassen zu können, sich zu aktualisieren) */
	private final JTableExt table;
	/** Darzustellende Daten */
	private final List<ModelElementBarrierSignalOption> options;
	/** Liste mit allen Signalnamen im System */
	private final String[] signals;
	/** Liste mit allen Kundentypnamen im System */
	private final String[] clientTypes;
	/** Hilfe-Callback */
	private final Runnable help;
	/** Nur-Lese-Status */
	private final boolean readOnly;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle (um diese veranlassen zu können, sich zu aktualisieren)
	 * @param options	Darzustellende Daten
	 * @param signals	Liste mit allen Signalnamen im System
	 * @param clientTypes	Liste mit allen Kundentypnamen im System
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 * @see ModelElementBarrierSignalOption
	 */
	public BarrierSignalTableModel(final JTableExt table, final List<ModelElementBarrierSignalOption> options, final String[] signals, final String[] clientTypes, final boolean readOnly, final Runnable help) {
		super();

		this.table=table;
		this.options=new ArrayList<>();
		for (ModelElementBarrierSignalOption option: options) this.options.add(option.clone());

		this.signals=signals;
		this.clientTypes=clientTypes;
		this.readOnly=readOnly;
		this.help=help;

		updateTable();
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
		return options.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * Liefert die Beschreibung für ein Freigabesignal
	 * @param option	Signal
	 * @return	Beschreibung
	 */
	private String getDescription(final ModelElementBarrierSignalOption option) {
		final StringBuilder result=new StringBuilder();

		if (option.getClientType()==null) result.append(Language.tr("Surface.Barrier.Dialog.Description.AllClientTypes")); else result.append(option.getClientType());
		if (option.getInitialClients()>0) {
			result.append(", "+Language.tr("Surface.Barrier.Dialog.Description.InitialRelease")+"="+option.getInitialClients());
		}
		result.append(", "+Language.tr("Surface.Barrier.Dialog.Description.PerSignal")+"=");
		if (option.getClientsPerSignal()>=0) {
			result.append(option.getClientsPerSignal());
		} else {
			result.append(Language.tr("Surface.Barrier.Dialog.Description.PerSignal.All"));
		}
		if (!option.isStoreSignals()) {
			result.append(", ");
			result.append(Language.tr("Surface.Barrier.Dialog.Description.DiscardSignals"));
		}

		return result.toString();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==options.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Surface.Barrier.Dialog.Add")},new Icon[]{Images.MODELEDITOR_ELEMENT_SIGNAL.getIcon()},new ActionListener[]{new EditButtonListener(0,-1)});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			return makeEditPanelSmallBorderIcon(
					Images.MODELEDITOR_ELEMENT_SIGNAL.getIcon(),
					options.get(rowIndex).getSignalName(),
					new Icon[]{Images.GENERAL_SETUP.getIcon(),Images.EDIT_DELETE.getIcon()},
					new String[]{
							Language.tr("Surface.Barrier.Dialog.Edit"),
							Language.tr("Surface.Barrier.Dialog.Delete")
					},
					new ActionListener[]{new EditButtonListener(0,rowIndex),new DeleteButtonListener(rowIndex)}
					);
		case 1:
			if (rowIndex==0 && rowIndex==options.size()-1) {
				/* kein hoch oder runter */
				return makePanel(getDescription(options.get(rowIndex)),null);
			}
			if (rowIndex==0) {
				/* kein Nach-Oben-Button */
				return makeEditPanelSmallBorder(
						getDescription(options.get(rowIndex)),
						new Icon[]{Images.ARROW_DOWN.getIcon()},
						new String[]{Language.tr("Surface.Barrier.Dialog.Down")},
						new ActionListener[]{new EditButtonListener(2,rowIndex)});
			}
			if (rowIndex==options.size()-1) {
				/* kein Nach-Unten-Button */
				return makeEditPanelSmallBorder(
						getDescription(options.get(rowIndex)),
						new Icon[]{Images.ARROW_UP.getIcon()},
						new String[]{Language.tr("Surface.Barrier.Dialog.Up")},
						new ActionListener[]{new EditButtonListener(1,rowIndex)});
			}
			return makeEditPanelSmallBorder(
					getDescription(options.get(rowIndex)),
					new Icon[]{Images.ARROW_UP.getIcon(),Images.ARROW_DOWN.getIcon()},
					new String[]{
							Language.tr("Surface.Barrier.Dialog.Up"),
							Language.tr("Surface.Barrier.Dialog.Down")
					},
					new ActionListener[]{new EditButtonListener(1,rowIndex),new EditButtonListener(2,rowIndex)});


		default: return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.Barrier.Dialog.ReleaseSignal");
		case 1: return Language.tr("Surface.Barrier.Dialog.ReleaseProperties");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Schreibt die Daten aus der Tabelle in die übergebene Liste zurück
	 * @param options	Liste in die die in der Tabelle befindlichen Freigabe-Optionen zurückgeschrieben werden sollen (die Liste wird dabei als erstes geleert)
	 */
	public void storeData(final List<ModelElementBarrierSignalOption> options) {
		options.clear();
		for (ModelElementBarrierSignalOption option: this.options) options.add(option.clone());
	}

	/**
	 * Reagiert auf Klicks auf die Bearbeiten und Verschieben-Schaltflächen
	 */
	private class EditButtonListener implements ActionListener {
		/** Auszuführender Befehl (0: Bearbeiten, 1: In der Liste nach oben schieben, 2: In der Liste nach unten schieben) */
		private final int col;
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param col	Auszuführender Befehl (0: Bearbeiten, 1: In der Liste nach oben schieben, 2: In der Liste nach unten schieben)
		 * @param row	Zeilennummer
		 */
		public EditButtonListener(final int col, final int row) {
			this.col=col;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			ModelElementBarrierSignalOption option;

			switch (col) {
			case 0:
				final BarrierSignalTableModelDialog dialog=new BarrierSignalTableModelDialog(table,help,(row>=0)?options.get(row):null,signals,clientTypes);
				dialog.setVisible(true);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row<0) {
						options.add(dialog.getOption());
					} else {
						options.set(row,dialog.getOption());
					}
					updateTable();
				}
				break;
			case 1:
				if (row>0) {
					option=options.get(row); options.set(row,options.get(row-1)); options.set(row-1,option);
					updateTable();
				}
				break;
			case 2:
				if (row<options.size()-1) {
					option=options.get(row); options.set(row,options.get(row+1)); options.set(row+1,option);
					updateTable();
				}
				break;
			}
		}
	}

	/**
	 * Reagiert auf Klicks auf die Löschen-Schaltflächen
	 */
	private class DeleteButtonListener implements ActionListener {
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param row	Zeilennummer
		 */
		public DeleteButtonListener(final int row) {
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;
			final String name=options.get(row).getSignalName();
			if ((e.getModifiers() & ActionEvent.SHIFT_MASK)==0) {
				if (!MsgBox.confirm(table,Language.tr("Surface.Barrier.Dialog.Delete.Confirm.Title"),String.format(Language.tr("Surface.Barrier.Dialog.Delete.Confirm.Info"),name),Language.tr("Surface.Barrier.Dialog.Delete.Confirm.YesInfo"),Language.tr("Surface.Barrier.Dialog.Delete.Confirm.NoInfo"))) return;
			}
			options.remove(row);
			updateTable();
		}
	}
}