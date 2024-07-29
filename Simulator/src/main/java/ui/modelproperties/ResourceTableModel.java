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
package ui.modelproperties;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelResources;
import ui.modeleditor.ModelSchedules;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSeize;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportSource;

/**
 * Diese Klasse h�lt das Tabellenmodell f�r die Anzeige von Bedienergruppen
 * im Modelleigenschaften-Dialog vor.
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 */
public class ResourceTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -382448545912017044L;

	/** Objekt das die verf�gbaren Animations-Icons vorh�lt */
	private AnimationImageSource imageSource;
	/** Liste mit den darzustellenden Bedienergruppen */
	private ModelResources resources;
	/** Liste mit den darzustellenden Bedienergruppen */
	private ModelSchedules schedules;
	/** Vollst�ndiges Editor-Modell (wird f�r den Expression-Builder ben�tigt) */
	private EditModel model;
	/** Haupt-Zeichenfl�che (wird ben�tigt um zu vermitteln, wo eine Bedienergruppe im Einsatz ist, und f�r den Expression-Builder) */
	private ModelSurface surface;
	/** Zu dem Tabellenmodell geh�renden Tabellenobjekt (wird sowohl zum Ausrichten von Dialogen als auch um ein Update der Tabelle anzusto�en ben�tigt) */
	private final JTableExt table;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Hilfe-Callback */
	private final Runnable help;

	/**
	 * Konstruktor der Klasse
	 * @param model	Vollst�ndiges Editor-Modell (wird f�r den Expression-Builder ben�tigt)
	 * @param surface	Haupt-Zeichenfl�che (wird ben�tigt um zu vermitteln, wo eine Bedienergruppe im Einsatz ist, und f�r den Expression-Builder)
	 * @param resources	Liste mit den darzustellenden Bedienergruppen
	 * @param schedules	Liste mit den verf�gbaren Zeitpl�nen (die in der Liste der m�gliche Schichtpl�ne zur Auswahl aufgelistet werden)
	 * @param table	Zu dem Tabellenmodell geh�renden Tabellenobjekt (wird sowohl zum Ausrichten von Dialogen als auch um ein Update der Tabelle anzusto�en ben�tigt)
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ResourceTableModel(final EditModel model, final ModelSurface surface, final ModelResources resources, final ModelSchedules schedules, final JTableExt table, final boolean readOnly, final Runnable help) {
		super();
		imageSource=new AnimationImageSource();
		this.table=table;
		this.readOnly=readOnly;
		this.help=help;
		setResources(model,surface,resources,schedules);
	}

	/**
	 * Stellt das zu bearbeitende Bedienergruppen-Objekt ein
	 * @param model	Vollst�ndiges Editor-Modell (wird f�r den Expression-Builder ben�tigt)
	 * @param surface	Haupt-Zeichenfl�che (wird ben�tigt um zu vermitteln, wo eine Bedienergruppe im Einsatz ist, und f�r den Expression-Builder)
	 * @param resources	Liste mit den darzustellenden Bedienergruppen
	 * @param schedules	Liste mit den verf�gbaren Zeitpl�nen (die in der Liste der m�gliche Schichtpl�ne zur Auswahl aufgelistet werden)
	 */
	public void setResources(final EditModel model, final ModelSurface surface, final ModelResources resources, final ModelSchedules schedules) {
		this.resources=resources;
		this.schedules=schedules;
		this.model=model;
		this.surface=surface;
		updateTable();
	}

	/**
	 * Weist das Datenmodell an, die Tabelle zu aktualisieren.
	 */
	public void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	/**
	 * Pr�ft, ob eine Bedienergruppe an einer bestimmten Station verwendet wird.
	 * @param name	Name der Bedienergruppe
	 * @param element	Zu pr�fende Station
	 * @return	Liefert <code>true</code>, wenn die Bedienergruppe an der Station verwendet wird
	 */
	private boolean resourceInUse(final String name, final ModelElementBox element) {
		if (element instanceof ModelElementProcess) {
			for (Map<String,Integer> map: ((ModelElementProcess)element).getNeededResources()) if (map.get(name)!=null) return true;
			return false;
		}

		if (element instanceof ModelElementSeize) {
			return (((ModelElementSeize)element).getNeededResources().get(name)!=null);
		}

		if (element instanceof ModelElementTransportSource) {
			return (((ModelElementTransportSource)element).getTransportResourceRecord().getResources().get(name)!=null);
		}

		return false;
	}

	/**
	 * Liefert die IDs der Stationen an denen eine Bedienergruppe verwendet wird
	 * @param name	Name der Bedienergruppe
	 * @return	Liste der IDs der Stationen an denen eine Bedienergruppe verwendet wird
	 */
	private List<Integer> resourceInUse(final String name) {
		final List<Integer> usingIDs=new ArrayList<>();

		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementBox) {
			if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) if (sub instanceof ModelElementBox) {
				if (resourceInUse(name,(ModelElementBox)sub)) usingIDs.add(sub.getId());
			}
			if (resourceInUse(name,(ModelElementBox)element)) usingIDs.add(element.getId());
		}

		return usingIDs;
	}

	@Override
	public int getRowCount() {
		return resources.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * Anzahl an Bedienern in einer bestimmten Bedienergruppe
	 * @param rowIndex	0-basierter Index der Bedienergruppe
	 * @return	Anzahl an Bedienern in der Gruppe (kann auch "unendlich" oder der Name eines Zeitplans sein)
	 * @see #getValueAt(int, int)
	 */
	private String getCount(final int rowIndex) {
		final ModelResource resource=resources.get(resources.getName(rowIndex));
		if (resource.getMode()==ModelResource.Mode.MODE_NUMBER) {
			final int value=resource.getCount();
			if (value<0) return Language.tr("Resources.Number.Infinite"); else return ""+value;
		}
		if (resource.getMode()==ModelResource.Mode.MODE_SCHEDULE) {
			return Language.tr("Resources.Number.bySchedule")+" "+resource.getSchedule();
		}
		return Language.tr("Dialog.Title.Error");
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==resources.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Resources.Group.Add")},new Icon[]{Images.MODELPROPERTIES_OPERATORS_ADD.getIcon()},new ActionListener[]{e->addClick()});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			String icon=resources.getResources()[rowIndex].getIcon();
			if (icon==null || icon.isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_OPERATOR_ICON_NAME;
			final Image image=imageSource.get(icon,model.animationImages,16);
			return makeEditPanelSmallBorderIcon(
					new ImageIcon(image),
					"<html><body>"+resources.getName(rowIndex)+" <span style=\"color: gray;\">"+String.format(Language.tr("Resources.Group.ID"),rowIndex+1)+"</span>",
					new Icon[]{Images.GENERAL_SETUP.getIcon(),Images.EDIT_DELETE.getIcon()},new String[]{Language.tr("Resources.Group.EditName"),Language.tr("Resources.Group.Delete")},
					new ActionListener[]{e->editClick(0,rowIndex),e->deleteClick(e.getModifiers(),rowIndex)}
					);
		case 1:
			return makeEditPanelSmallBorder(
					getCount(rowIndex),
					new Icon[]{Images.GENERAL_DECREASE.getIcon(),Images.GENERAL_INCREASE.getIcon(),Images.GENERAL_SETUP.getIcon()},
					new String[]{Language.tr("Resources.Number.Decrease"),Language.tr("Resources.Number.Increase"),Language.tr("Resources.Number.Edit")},
					new ActionListener[]{e->editClick(1,rowIndex),e->editClick(2,rowIndex),e->editClick(0,rowIndex)}
					);
		default: return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	/**
	 * Informiert eine Reihe von Stationen dar�ber, dass sich der Name einer Bedienergruppe ge�ndert hat.
	 * @param usingIDs	Liste der IDs der zu benachrichtigenden Stationen
	 * @param oldName	Alter Name der Bedienergruppe
	 * @param newName	Neuer Name der Bedienergruppe
	 */
	private void renameResouceInStations(final List<Integer> usingIDs, final String oldName, final String newName) {
		for (Integer id: usingIDs) {
			final ModelElement element=surface.getByIdIncludingSubModels(id);
			if (element instanceof ModelDataRenameListener) ((ModelDataRenameListener)element).objectRenamed(oldName,newName,ModelDataRenameListener.RenameType.RENAME_TYPE_RESOURCE);
		}
	}

	/**
	 * Erstellt einen Infotext, der darauf hinweist, dass eine Bedienergruppe an einer oder mehreren Stationen verwendet wird
	 * @param sb	Ausgabe-{@link StringBuilder}
	 * @param usingIDs	Liste der IDs an denen die Bedienergruppe verwendet wird
	 */
	private void getInUseInfoText(final StringBuilder sb, List<Integer> usingIDs) {
		if (usingIDs.size()==0) return;

		if (usingIDs.size()==1) {
			sb.append(Language.tr("Resources.Group.InUse.Single"));
		} else {
			sb.append(Language.tr("Resources.Group.InUse.Multiple"));
		}
		sb.append("\n");
		sb.append(usingIDs.get(0));
		for (int i=1;i<usingIDs.size();i++) {sb.append(", "); sb.append(usingIDs.get(i));}
		sb.append("\n\n");
	}

	/**
	 * Liefert einen Namensvorschlag f�r eine neue Bedienergruppe
	 * @return	Namensvorschlag f�r eine neue Bedienergruppe (Namensvorschlag existiert sich noch nicht als Bedienergruppe)
	 */
	private String getFreeName() {
		final String baseName=Language.tr("Resources.Group.DefaultName");
		String name=baseName;
		int nr=0;
		while (resources.getNoAutoAdd(name)!=null) {
			nr++;
			name=baseName+" "+nr;
		}
		return name;
	}

	/**
	 * Reagiert auf Klicks auf die Hinzuf�gen-Schaltfl�che.
	 */
	private void addClick() {
		if (readOnly) return;

		ModelResource resource=new ModelResource(getFreeName());
		ResourceTableModelDialog dialog=new ResourceTableModelDialog(table,help,resources.list(),resource,schedules.getScheduleNames(),model,surface,model.animationImages,false,false);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			resources.add(resource);
			updateTable();
		}
	}

	/**
	 * Zeigt den Bearbeitendialog f�r eine bestimmte Zeile an.
	 * @param row	Zeilenindex
	 * @return	Liefert -1 oder 1, wenn der Nutzer ausgew�hlt hat, dass der vorherige oder der n�chste Datensatz bearbeitet werden soll; sonst 0.
	 */
	private int showEditDialog(final int row) {
		final String name=resources.getName(row);
		final ModelResource resource=resources.get(name);
		final ResourceTableModelDialog dialog=new ResourceTableModelDialog(table,help,resources.list(),resource,schedules.getScheduleNames(),model,surface,model.animationImages,row>0,row<resources.size()-1);
		dialog.setVisible(true);
		final int closedBy=dialog.getClosedBy();
		if (closedBy==BaseDialog.CLOSED_BY_OK || closedBy==BaseDialog.CLOSED_BY_NEXT || closedBy==BaseDialog.CLOSED_BY_PREVIOUS) {
			final String newName=resource.getName();
			if (!newName.equals(name)) {
				final List<Integer> usingIDs=resourceInUse(name);
				final StringBuilder renameInfo=new StringBuilder();
				if (usingIDs.size()>0) {
					getInUseInfoText(renameInfo,usingIDs);
					renameInfo.append("<br>");
					renameInfo.append(Language.tr("Resources.Group.EditName.ChangeName.Info"));
					if (MsgBox.confirm(table,Language.tr("Resources.Group.EditName.ChangeName.Title"),"<html><body>"+renameInfo.toString(),Language.tr("Resources.Group.EditName.ChangeName.YesInfo"),Language.tr("Resources.Group.EditName.ChangeName.NoInfo"))) {
						renameResouceInStations(usingIDs,name,newName);
					}
				}
			}
			updateTable();
		}
		if (closedBy==BaseDialog.CLOSED_BY_NEXT) return 1;
		if (closedBy==BaseDialog.CLOSED_BY_PREVIOUS) return -1;
		return 0;
	}

	/**
	 * Reagiert auf Klicks auf die Bearbeiten und Verschieben-Schaltfl�chen.
	 * @param col	Spaltennummer
	 * @param row	Zeilennummer
	 */
	private void editClick(final int col, final int row) {
		if (readOnly) return;

		if (col==0) {
			int rowToEdit=row;
			while (true) {
				final int nextRowToEditDelta=showEditDialog(rowToEdit);
				if (nextRowToEditDelta==0) break;
				rowToEdit+=nextRowToEditDelta;
			}
			return;
		}

		final String name=resources.getName(row);
		final int count=(resources.get(name).getMode()==ModelResource.Mode.MODE_NUMBER)?(resources.get(name).getCount()):-1;
		switch (col) {
		case 1:
			if (count<=1) resources.get(name).setCount(1); else resources.get(name).setCount(count-1);
			break;
		case 2:
			if (count>=1) resources.get(name).setCount(count+1); else resources.get(name).setCount(1);
			break;
		}
		updateTable();
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Resources.Group.RowTitle.Name");
		case 1: return Language.tr("Resources.Group.RowTitle.Count");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Reagiert auf Klicks auf die L�schen-Schaltfl�chen.
	 * @param modifiers	Gedr�ckte Umschalt-Tasten (aus dem Ereignisdatensatz)
	 * @param row	Zeilennummer
	 */
	private void deleteClick(final int modifiers, final int row) {
		if (readOnly) return;
		final String name=resources.getName(row);
		final List<Integer> usingIDs=resourceInUse(name);
		final StringBuilder sb=new StringBuilder();
		getInUseInfoText(sb,usingIDs);
		sb.append("<br>");
		sb.append(String.format(Language.tr("Resources.Group.Delete.Confirmation"),name));
		if ((modifiers & ActionEvent.SHIFT_MASK)==0) {
			if (!MsgBox.confirm(table,Language.tr("Resources.Group.Delete"),"<html><body>"+sb.toString()+"</body></html>",Language.tr("Resources.Group.Delete.YesInfo"),Language.tr("Resources.Group.Delete.NoInfo"))) return;
		}
		resources.delete(name);
		updateTable();
	}

	/**
	 * Liefert alle momentan in dem Datenmodell enthaltenen Bedienergruppen-Datens�tze
	 * @return	Liste mit den Bedienergruppen-Datens�tze
	 */
	public ModelResources getResources() {
		return resources;
	}
}