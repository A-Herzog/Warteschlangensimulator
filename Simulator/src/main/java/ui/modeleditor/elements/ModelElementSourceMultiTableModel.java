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

import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Stellt ein Tabellenmodell zur Anzeige und Bearbeitung der Kunden-Teil-Quellen zur Verfügung.
 * @author Alexander Herzog
 * @see ModelElementSourceMultiDialog
 */
public class ModelElementSourceMultiTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -8059012715680468181L;

	/** Kann der Datensatz deaktiviert werden? */
	private final boolean hasActivation;
	/** Gibt an, ob die Quelle selbst Kunden erzeugen können soll oder ob sie jeweils von außen angestoßen wird */
	private final boolean hasOwnArrivals;
	/** Tabelle in der das Datenmodell zum Einsatz kommen soll */
	private final JTableExt table;
	/** Hilfe-Callback welches aufgerufen wird, wenn in einem der untergeordneten Dialoge auf die "Hilfe"-Schaltfläche geklickt wird. */
	private final Runnable help;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden) */
	private final EditModel model;
	/** Zeichenoberfläche */
	private final ModelSurface surface;
	/** Element, dessen Zuweisungen bearbeitet werden sollen (für den ExpressionBuilder und um die Variablenliste zusammenzustellen) */
	private final ModelElement element;
	/** Callback zum Erstellen der Schaltfläche zum Aufrufen der Zeitpläne */
	private final Function<Supplier<Boolean>,JButton> getSchedulesButton;
	/**
	 * Kundendaten
	 * @see #setClientData(ModelClientData)
	 */
	private ModelClientData clientData;

	/**
	 * Objekt das die verfügbaren Animations-Icons vorhält
	 */
	private final AnimationImageSource imageSource;

	/**
	 * Liste der Daten für die Teil-Kundenquellen
	 */
	private final List<ModelElementSourceRecord> records;

	/**
	 * Konstruktor der Klasse <code>ModelElementSourceMultiTableModel</code>
	 * @param table	Tabelle in der das Datenmodell zum Einsatz kommen soll
	 * @param records	Datensätze, die bearbeitet werden sollen
	 * @param hasOwnArrivals	Gibt an, ob die Quelle selbst Kunden erzeugen können soll oder ob sie jeweils von außen angestoßen wird
	 * @param hasActivation	Kann der Datensatz deaktiviert werden?
	 * @param element	Element, dessen Zuweisungen bearbeitet werden sollen (für den ExpressionBuilder und um die Variablenliste zusammenzustellen)
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Zeichenoberfläche
	 * @param readOnly	Gibt an, ob die Daten bearbeitet werden dürfen (<code>false</code>) oder nicht (<code>true</code>).
	 * @param help	Hilfe-Callback welches aufgerufen wird, wenn in einem der untergeordneten Dialoge auf die "Hilfe"-Schaltfläche geklickt wird.
	 * @param getSchedulesButton	Callback zum Erstellen der Schaltfläche zum Aufrufen der Zeitpläne
	 */
	public ModelElementSourceMultiTableModel(final JTableExt table, final List<ModelElementSourceRecord> records, final boolean hasOwnArrivals, final boolean hasActivation, final ModelElement element, final EditModel model, final ModelSurface surface, final boolean readOnly, final Runnable help, final Function<Supplier<Boolean>,JButton> getSchedulesButton) {
		super();
		this.help=help;
		this.getSchedulesButton=getSchedulesButton;
		this.table=table;
		this.readOnly=readOnly;
		this.model=model;
		this.surface=surface;
		this.element=element;
		this.hasOwnArrivals=hasOwnArrivals;
		this.hasActivation=hasActivation;

		imageSource=new AnimationImageSource();

		this.records=new ArrayList<>();
		for (ModelElementSourceRecord record: records) this.records.add(record.clone());

		updateTable();
	}

	/**
	 * Übergibt die Kundendaten an das Tabellen-Datenmodell
	 * @param clientData	Kundendaten
	 */
	public void setClientData(final ModelClientData clientData) {
		this.clientData=clientData;
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
		return records.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * Liefert eine Beschreibung zu einem Kunden-Teil-Quelle-Datensatz.
	 * @param record	Kunden-Teil-Quelle-Datensatz
	 * @return	Beschreibung zu dem Kunden-Teil-Quelle-Datensatz
	 */
	private String getInfoText(final ModelElementSourceRecord record) {
		final StringBuilder sb=new StringBuilder();

		if (!hasOwnArrivals && record.hasName()) {
			sb.append(Language.tr("Surface.MultiSourceTable.Info.ClientTypeName")+": "+record.getName());
		}

		if (hasOwnArrivals) switch (record.getNextMode()) {
		case NEXT_DISTRIBUTION:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.NextDistribution"));
			sb.append(" (");
			sb.append(DistributionTools.getDistributionName(record.getInterarrivalTimeDistribution()));
			sb.append(", ");
			sb.append(Language.tr("Surface.Source.Dialog.TimeBase"));
			sb.append(": ");
			sb.append(ModelSurface.getTimeBaseString(record.getTimeBase()));
			sb.append(")");
			break;
		case NEXT_EXPRESSION:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.NextExpression"));
			sb.append(" (");
			sb.append(record.getInterarrivalTimeExpression());
			sb.append(", ");
			sb.append(Language.tr("Surface.Source.Dialog.TimeBase"));
			sb.append(": ");
			sb.append(ModelSurface.getTimeBaseString(record.getTimeBase()));
			sb.append(")");
			break;
		case NEXT_SCHEDULE:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.NextSchedule"));
			sb.append(" (");
			sb.append(record.getInterarrivalTimeSchedule());
			sb.append(")");
			break;
		case NEXT_CONDITION:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.NextCondition"));
			sb.append(" (");
			sb.append(record.getArrivalCondition());
			sb.append(", "+Language.tr("Surface.MultiSourceTable.Info.NextCondition.MinDistance"));
			sb.append(": ");
			sb.append(record.getArrivalConditionMinDistance());
			sb.append(")");
			break;
		case NEXT_THRESHOLD:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.NextThreshold"));
			sb.append(": ");
			sb.append(record.getThresholdExpression());
			sb.append(" ");
			if (record.isThresholdDirectionUp()) Language.tr("Surface.MultiSourceTable.Info.NextThreshold.Up"); else Language.tr("Surface.MultiSourceTable.Info.NextThreshold.Down");
			sb.append(" ");
			sb.append(NumberTools.formatNumber(record.getThresholdValue()));
			break;
		case NEXT_SIGNAL:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.NextSignal"));
			sb.append(" (");
			sb.append(String.join(", ",record.getArrivalSignalNames().toArray(String[]::new)));
			sb.append(")");
			break;
		case NEXT_INTERVAL_EXPRESSIONS:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.IntervalExpressions"));
			break;
		case NEXT_INTERVAL_DISTRIBUTIONS:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.IntervalDistributions"));
			break;
		case NEXT_STREAM:
			sb.append(Language.tr("Surface.MultiSourceTable.Info.DataStream"));
			break;
		}

		if (record.getBatchSize()==null || !record.getBatchSize().trim().equals("1")) {
			sb.append(", ");
			sb.append(Language.tr("Surface.MultiSourceTable.Info.BatchMode"));
			if (record.getBatchSize()!=null) {
				sb.append(" (");
				sb.append(record.getBatchSize());
				sb.append(")");
			}
		}

		if (hasOwnArrivals && record.getMaxArrivalCount()>=0) {
			sb.append(", ");
			sb.append(Language.tr("Surface.MultiSourceTable.Info.ArrivalCount"));
			sb.append(": ");
			sb.append(record.getMaxArrivalCount());
			sb.append(" ");
		}

		if (hasOwnArrivals && record.getMaxArrivalClientCount()>=0) {
			sb.append(", ");
			sb.append(Language.tr("Surface.MultiSourceTable.Info.ArrivalClientCount"));
			sb.append(": ");
			sb.append(record.getMaxArrivalClientCount());
			sb.append(" ");
		}

		if (hasOwnArrivals && record.getArrivalStart()>0) {
			sb.append(", ");
			sb.append(Language.tr("Surface.MultiSourceTable.Info.ArrivalStart"));
			sb.append(": ");
			sb.append(NumberTools.formatNumber(record.getArrivalStart()));
			sb.append(" ");
		}

		return sb.toString();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==records.size()) {
			if (columnIndex>0) return "";
			return makeButtonPanel(new String[]{Language.tr("Surface.MultiSourceTable.Add")},new Icon[]{Images.EDIT_ADD.getIcon()},new ActionListener[]{new EditButtonListener(0,-1)});
		}

		final ModelElementSourceRecord record=records.get(rowIndex);

		switch (columnIndex) {
		case 0:
			String icon=clientData.getIcon(record.getName());
			if (icon==null || icon.trim().isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_CLIENT_ICON_NAME;
			final Image image=imageSource.get(icon,model.animationImages,16);
			if (hasActivation) {
				return makeEditPanelSmallBorderIcon(
						new ImageIcon(image),
						record.getName()+(record.isActive()?"":(" ("+Language.tr("Surface.MultiSourceTable.IsDeactivated")+")")),
						new Icon[]{record.isActive()?Images.GENERAL_OFF.getIcon():Images.GENERAL_ON.getIcon(),Images.GENERAL_SETUP.getIcon(),Images.EDIT_DELETE.getIcon()},
						new String[]{record.isActive()?Language.tr("Surface.MultiSourceTable.Deactivate"):Language.tr("Surface.MultiSourceTable.Activate"),Language.tr("Surface.MultiSourceTable.Edit"),Language.tr("Surface.MultiSourceTable.Delete")},
						new ActionListener[]{new EditButtonListener(record.isActive()?2:3,rowIndex),new EditButtonListener(0,rowIndex),new EditButtonListener(1,rowIndex)}
						);
			} else {
				return makeEditPanelSmallBorderIcon(
						new ImageIcon(image),
						record.getName(),
						new Icon[]{Images.GENERAL_SETUP.getIcon(),Images.EDIT_DELETE.getIcon()},
						new String[]{Language.tr("Surface.MultiSourceTable.Edit"),Language.tr("Surface.MultiSourceTable.Delete")},
						new ActionListener[]{new EditButtonListener(0,rowIndex),new EditButtonListener(1,rowIndex)}
						);
			}
		case 1:
			if (records.size()==1) {
				return makePanel(getInfoText(record),null);
			} else {
				final List<Icon> icons=new ArrayList<>();
				final List<String> hints=new ArrayList<>();
				final List<ActionListener> listener=new ArrayList<>();
				if (rowIndex>0) {
					icons.add(Images.ARROW_UP.getIcon());
					hints.add(Language.tr("Surface.MultiSourceTable.MoveUp"));
					listener.add(e->moveUp(rowIndex));
				}
				if (rowIndex<records.size()-1) {
					icons.add(Images.ARROW_DOWN.getIcon());
					hints.add(Language.tr("Surface.MultiSourceTable.MoveDown"));
					listener.add(e->moveDown(rowIndex));
				}
				return makeEditPanelSmallBorderIcon(null,getInfoText(record),icons.toArray(Icon[]::new),hints.toArray(String[]::new),listener.toArray(ActionListener[]::new));

			}
		default:
			return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.MultiSourceTable.ColumnClientType");
		case 1: return Language.tr("Surface.MultiSourceTable.ColumnData");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Verschiebt einen Eintrag um einen Platz nach oben.
	 * @param nr	Index des zu verschiebenden Eintrags
	 */
	private void moveUp(final int nr) {
		if (nr<=0) return;
		final ModelElementSourceRecord record=records.get(nr-1);
		records.set(nr-1,records.get(nr));
		records.set(nr,record);
		updateTable();
	}

	/**
	 * Verschiebt einen Eintrag um einen Platz nach unten.
	 * @param nr	Index des zu verschiebenden Eintrags
	 */
	private void moveDown(final int nr) {
		if (nr<0 || nr>=records.size()-1) return;
		final ModelElementSourceRecord record=records.get(nr+1);
		records.set(nr+1,records.get(nr));
		records.set(nr,record);
		updateTable();
	}

	/**
	 * Schreibt die Daten aus der Tabelle in das Element zurück
	 * @param element	Station, in die die Kundenankunftsdatensätze zurück geschrieben werden sollen
	 */
	public void storeData(final ModelElementSourceMulti element) {
		element.getRecords().clear();
		for (ModelElementSourceRecord record: records) {
			if (!element.getSurface().getClientTypes().contains(record.saveName)) {
				ModelElementSourceRecordPanel.renameClients(record.saveName,record.getName(),clientData,surface);
			}
			element.addRecord(record);
		}
	}

	/**
	 * Schreibt die Daten aus der Tabelle in das Element zurück
	 * @param element	Station, in die die Kundenankunftsdatensätze zurück geschrieben werden sollen
	 */
	public void storeData(final ModelElementSplit element) {
		element.getRecords().clear();
		for (ModelElementSourceRecord record: records) {
			if (!element.getSurface().getClientTypes().contains(record.saveName)) {
				ModelElementSourceRecordPanel.renameClients(record.saveName,record.getName(),clientData,surface);
			}
			element.addRecord(record);
		}
	}

	/**
	 * Liefert die Namen aller Kundentypen in dem Modell.
	 * @return	Namen aller Kundentypen in dem Modell
	 */
	public Set<String> getClientTypeNames() {
		return new HashSet<>(records.stream().map(record->record.getName()).collect(Collectors.toList()));
	}

	/**
	 * Ersetzt einzelne Einträge in dem Modell durch neue.
	 * @param clientTypes	Kundentypen, die hinzugefügt werden sollen oder ggf. vorhandene Typen mit demselben Namen ersetzen sollen
	 */
	public void replaceRecords(final List<ModelElementSourceRecord> clientTypes) {
		if (clientTypes==null) return;
		final Set<String> newNames=new HashSet<>(clientTypes.stream().map(clientType->clientType.getName()).collect(Collectors.toList()));

		int index=0;
		while (index<records.size()) {
			if (newNames.contains(records.get(index).getName())) {
				records.remove(index);
				continue;
			}
			index++;
		}

		records.addAll(clientTypes);

		updateTable();
	}

	/**
	 * Reagiert auf Klicks auf die Bearbeiten-Schaltflächen
	 */
	private class EditButtonListener implements ActionListener {
		/** Auszuführende Aktion (0: Neu/Bearbeiten; 1: Löschen) */
		private final int nr;
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param nr	Auszuführende Aktion (0: Neu/Bearbeiten; 1: Löschen)
		 * @param row	Zeilennummer
		 */
		public EditButtonListener(final int nr, final int row) {
			this.nr=nr;
			this.row=row;
		}

		/**
		 * Befehl: Kundentyp hinzufügen
		 */
		private void add() {
			final ModelElementSourceRecord record=new ModelElementSourceRecord(true,hasActivation,hasOwnArrivals);
			final ModelElementSourceMultiTableModelDialog dialog=new ModelElementSourceMultiTableModelDialog(table,record,element,model,surface,clientData,help,getSchedulesButton,hasActivation);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				records.add(record);
				updateTable();
			}
		}

		/**
		 * Befehl: Kundentyp bearbeiten
		 * @param row	0-basierter Index des Kundentyps
		 */
		private void edit(int row) {
			Point location=null;
			int activeTabIndex=-1;
			while (true) {
				final ModelElementSourceRecord record=records.get(row);
				final String oldName=record.getName();
				final ModelElementSourceMultiTableModelDialog dialog=new ModelElementSourceMultiTableModelDialog(table,record,element,model,surface,clientData,help,getSchedulesButton,hasActivation,row>0,row<records.size()-1,activeTabIndex);
				if (location!=null) dialog.setLocation(location);
				dialog.setVisible(true);
				location=dialog.getLocation();
				activeTabIndex=dialog.getActiveTabIndex();
				final String newName=record.getName();
				switch (dialog.getClosedBy()) {
				case BaseDialog.CLOSED_BY_OK:
					if (!newName.equals(oldName)) ModelElementSourceRecordPanel.renameClients(oldName,newName,clientData,element.getSurface());
					updateTable();
					return;
				case BaseDialog.CLOSED_BY_PREVIOUS:
					if (!newName.equals(oldName)) ModelElementSourceRecordPanel.renameClients(oldName,newName,clientData,element.getSurface());
					updateTable();
					row--;
					break;
				case BaseDialog.CLOSED_BY_NEXT:
					if (!newName.equals(oldName)) ModelElementSourceRecordPanel.renameClients(oldName,newName,clientData,element.getSurface());
					updateTable();
					row++;
					break;
				default:
					return;
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			switch (nr) {
			case 0: /* Add / Edit */
				if (row<0) add(); else edit(row);
				break;
			case 1: /* Delete */
				final String name=records.get(row).getName();
				if ((e.getModifiers() & ActionEvent.SHIFT_MASK)==0) {
					if (!MsgBox.confirm(table,Language.tr("Surface.MultiSourceTable.Delete.Confirmation.Title"),String.format(Language.tr("Surface.MultiSourceTable.Delete.Confirmation.Info"),name),Language.tr("Surface.MultiSourceTable.Delete.Confirmation.YesInfo"),Language.tr("Surface.MultiSourceTable.Delete.Confirmation.NoInfo"))) return;
				}
				records.remove(row);
				updateTable();
				break;
			case 2: /* Aus */
				records.get(row).setActive(false);
				updateTable();
				break;
			case 3: /* An */
				records.get(row).setActive(true);
				updateTable();
				break;
			}
		}
	}
}
