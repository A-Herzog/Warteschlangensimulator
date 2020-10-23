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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.TableCellEditor;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelResources;
import ui.modeleditor.ModelSurfaceAnimatorBase;

/**
 * Stellt ein Tabellenmodell zur Anzeige und Bearbeitung der für z.B. eine Bedienstation notwendigen Ressourcen zur Verfügung.
 * @author Alexander Herzog
 * @see ModelResources
 */
public class ResourceTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 9171941501499316592L;

	/** Tabelle in der das Datenmodell zum Einsatz kommen soll */
	private final JTableExt table;
	/** Hilfe-Callback welches aufgerufen wird, wenn in einem der untergeordneten Dialoge auf die "Hilfe"-Schaltfläche geklickt wird. */
	private final Runnable help;
	/** Gesamtes Modell (um Icons für die Listenansicht auszulesen) */
	private final EditModel model;
	/** Liste der verfügbaren Ressourcen (kann evtl. geändert werden, d.h. es muss das Original übergeben werden) */
	private final ModelResources resources;
	/** Liste der benötigten Ressourcen (zur Anzeige und zum Bearbeiten) */
	private final Map<String,Integer> map;
	/** Nur-Lese-Status */
	private final boolean readOnly;

	private final List<String> usedNames;

	private final List<ActionListener> tableChangeListener;

	/**
	 * Konstruktor der Klasse <code>ResourceTableModel</code>
	 * @param table	Tabelle in der das Datenmodell zum Einsatz kommen soll
	 * @param map Liste der benötigten Ressourcen (zur Anzeige und zum Bearbeiten)
	 * @param model	Gesamtes Modell (um Icons für die Listenansicht auszulesen)
	 * @param resources Liste der verfügbaren Ressourcen (kann evtl. geändert werden, d.h. es muss das Original übergeben werden)
	 * @param readOnly	Gibt an, ob die Daten bearbeitet werden dürfen (<code>false</code>) oder nicht (<code>true</code>).
	 * @param help	Hilfe-Callback welches aufgerufen wird, wenn in einem der untergeordneten Dialoge auf die "Hilfe"-Schaltfläche geklickt wird.
	 */
	public ResourceTableModel(final JTableExt table, final Map<String,Integer> map, final EditModel model, final ModelResources resources, final boolean readOnly, final Runnable help) {
		super();
		tableChangeListener=new ArrayList<>();
		this.help=help;
		this.table=table;
		this.model=model;
		this.resources=resources;
		this.map=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.map.putAll(map);
		this.readOnly=readOnly;
		usedNames=new ArrayList<>();
		updateTable();
	}

	/**
	 * Aktualisiert die Tabellendarstellung
	 */
	private void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
		for (ActionListener listener: tableChangeListener) listener.actionPerformed(new ActionEvent(this,0,"changed"));

		usedNames.clear();
		for(Map.Entry<String,Integer> entry: map.entrySet()) usedNames.add(entry.getKey());
	}

	/**
	 * Fügt einen Listener hinzu, der aktiviert wird, wenn sich die Tabelleninhalte ändern
	 * @param listener	Neuer Listener, der bei Tabellenänderungen benachrichtigt werden soll
	 */
	public void addTableChangeListener(final ActionListener listener) {
		if (tableChangeListener.indexOf(listener)<0) tableChangeListener.add(listener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die aktiviert werden, wenn sich die Tabelleninhalte ändern
	 * @param listener	Listener der nicht mehr bei Tabellenänderungen benachrichtigt werden soll
	 */
	public void removeTableChangeListener(final ActionListener listener) {
		if (tableChangeListener.indexOf(listener)<0) tableChangeListener.remove(listener);
	}

	/**
	 * Liefert die Anzahl der in der Liste verwendeten verschiedenen Ressourcengruppen
	 * @return	Anzahl der in der Liste verwendeten verschiedenen Ressourcengruppen
	 */
	public int size() {
		return map.size();
	}

	@Override
	public int getRowCount() {
		return map.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	private String getCount(final String name) {
		final ModelResource resource=resources.getNoAutoAdd(name);
		if (resource==null) return Language.tr("Surface.Resource.Group.DoesNotExist");
		if (resource.getMode()==ModelResource.Mode.MODE_NUMBER) {
			final int i=resource.getCount();
			if (i>0) return String.format(Language.tr("Surface.Resource.Group.Number"),i);
			return Language.tr("Surface.Resource.Group.Infinite");
		}
		if (resource.getMode()==ModelResource.Mode.MODE_SCHEDULE) {
			return Language.tr("Surface.Resource.Group.Schedule")+": "+resource.getSchedule();
		}

		return Language.tr("Surface.Resource.Group.Error");
	}

	/**
	 * Objekt das die verfügbaren Animations-Icons vorhält
	 */
	private final AnimationImageSource imageSource=new AnimationImageSource();

	private Icon getIconForResource(final String resourceName) {
		final ModelResource resource=resources.getNoAutoAdd(resourceName);
		String icon=null;
		if (resource!=null) icon=resource.getIcon();
		if (icon==null || icon.trim().isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_OPERATOR_ICON_NAME;
		return new ImageIcon(imageSource.get(icon,model.animationImages,16));
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==map.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Surface.Resource.Add")},new URL[]{Images.MODELPROPERTIES_OPERATORS_ADD.getURL()},new ActionListener[]{new EditButtonListener(0,-1)});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0: return makeEditPanelSmallBorderIcon(
				getIconForResource(usedNames.get(rowIndex)),

				usedNames.get(rowIndex)+" ("+getCount(usedNames.get(rowIndex))+")",
				new URL[]{Images.GENERAL_SETUP.getURL(),Images.EDIT_DELETE.getURL()},
				new String[]{
						Language.tr("Surface.Resource.EditName"),
						Language.tr("Surface.Resource.Delete")
				},
				new ActionListener[]{new EditButtonListener(0,rowIndex),new DeleteButtonListener(rowIndex)}
				);
		case 1: return makeEditPanelSmallBorder(
				""+map.get(usedNames.get(rowIndex)),
				new URL[]{Images.GENERAL_DECREASE.getURL(),Images.GENERAL_INCREASE.getURL(),Images.GENERAL_SETUP.getURL()},
				new String[]{
						Language.tr("Surface.Resource.NumberDecrease"),
						Language.tr("Surface.Resource.NumberIncrease"),
						Language.tr("Surface.Resource.EditNumber")
				},
				new ActionListener[]{new EditButtonListener(1,rowIndex),new EditButtonListener(2,rowIndex),new EditButtonListener(3,rowIndex)}
				);
		default: return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	private class EditButtonListener implements ActionListener {
		private final int col;
		/** Zeilennummer */
		private final int row;

		public EditButtonListener(final int col, final int row) {
			this.col=col;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			String name;
			int max;
			int used;

			switch (col) {
			case 0:
				ResourceTableModelDialog1 dialog1=new ResourceTableModelDialog1(table,help,map,row,resources,model,true);
				dialog1.setVisible(true);
				if (dialog1.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row<0) {
						map.put(dialog1.getName(),1);
					} else {
						final String oldName=usedNames.get(row);
						final int value=map.get(oldName);
						map.remove(oldName);
						map.put(dialog1.getName(),value);
					}
					updateTable();
				}
				break;
			case 1:
				name=usedNames.get(row);
				used=map.get(name);
				if (used>1) {
					map.put(name,used-1);
					updateTable();
				}
				break;
			case 2:
				name=usedNames.get(row);
				used=map.get(name);
				if (resources.get(name).getMode()==ModelResource.Mode.MODE_NUMBER) max=resources.get(name).getCount(); else max=-1;
				if (used<max || max<0) {
					map.put(name,used+1);
					updateTable();
				}
				break;
			case 3:
				name=usedNames.get(row);
				if (resources.get(name).getMode()==ModelResource.Mode.MODE_NUMBER) max=resources.get(name).getCount(); else max=-1;
				used=map.get(name);
				ResourceTableModelDialog2 dialog2=new ResourceTableModelDialog2(table,help,name,max,used);
				dialog2.setVisible(true);
				if (dialog2.getClosedBy()==BaseDialog.CLOSED_BY_OK) {map.put(name,dialog2.getCount()); updateTable();}
				break;
			}
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.Resource.Header.Type");
		case 1: return Language.tr("Surface.Resource.Header.NeededNumber");
		default: return super.getColumnName(column);
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

			final String name=usedNames.get(row);
			if (!MsgBox.confirm(table,Language.tr("Surface.Resource.Delete.Confirm.Title"),String.format(Language.tr("Surface.Resource.Delete.Confirm.Info"),name),Language.tr("Surface.Resource.Delete.Confirm.YesInfo"),Language.tr("Surface.Resource.Delete.Confirm.NoInfo"))) return;
			map.remove(name);
			updateTable();
		}
	}

	/**
	 * Liefert die geänderte Liste der benötigten Ressourcen zurück
	 * @param map	Liste der benötigten Ressourcen
	 */
	public void storeData(final Map<String,Integer> map) {
		map.clear();
		map.putAll(this.map);
	}

	/**
	 * Zeigt einen Dialog zum Anlegen und Hinzufügen einer neuen Bedienergruppe an.
	 */
	public void addNewGroup() {
		final ResourceTableModelDialog1 dialog1=new ResourceTableModelDialog1(table,help,map,-1,resources,model,false);
		dialog1.setVisible(true);
		if (dialog1.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			map.put(dialog1.getName(),1);
			updateTable();
		}
	}

	/**
	 * Fügt eine bestehende Bedienergruppe zu der Liste der notwendigen Bediener hinzu.
	 * @param name	Name der bestehenden Bedienergruppe
	 */
	public void addExistingGroup(final String name) {
		map.put(name,1);
		updateTable();
	}
}
