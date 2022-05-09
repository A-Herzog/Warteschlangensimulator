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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.ModelTransporters;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportParking;
import ui.modeleditor.elements.ModelElementTransportTransporterSource;

/**
 * Diese Klasse hält das Tabellenmodell für die Anzeige von Transportergruppen
 * im Modelleigenschaften-Dialog vor.
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 */
public class TransporterTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7363232010136439807L;

	/** Objekt das die verfügbaren Animations-Icons vorhält */
	private AnimationImageSource imageSource;
	/** Liste der Transportergruppen die in der Tabelle angezeigt werden sollen */
	private final List<ModelTransporter> transporters;
	/** Vollständiges Editor-Modell (wird für den Expression-Builder benötigt) */
	private final EditModel model;
	/** Zeichenfläche (für die Bestimmung der Abstände) */
	private final ModelSurface surface;
	/** Zu dem Tabellenmodell gehörenden Tabellenobjekt (wird sowohl zum Ausrichten von Dialogen als auch um ein Update der Tabelle anzustoßen benötigt) */
	private final JTableExt table;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Hilfe-Callback */
	private final Runnable help;

	/**
	 * Konstruktor der Klasse
	 * @param model	Vollständiges Editor-Modell (wird für den Expression-Builder benötigt)
	 * @param surface	Zeichenfläche (für die Bestimmung der Abstände)
	 * @param transporters	Liste der Transportergruppen die in der Tabelle angezeigt werden sollen
	 * @param table	Zu dem Tabellenmodell gehörenden Tabellenobjekt (wird sowohl zum Ausrichten von Dialogen als auch um ein Update der Tabelle anzustoßen benötigt)
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public TransporterTableModel(final EditModel model, final ModelSurface surface, final ModelTransporters transporters, final JTableExt table, final boolean readOnly, final Runnable help) {
		super();
		imageSource=new AnimationImageSource();
		this.model=model;
		this.surface=surface;
		this.transporters=transporters.getTransporters().stream().map(transporter->transporter.clone()).collect(Collectors.toList());
		this.table=table;
		this.readOnly=readOnly;
		this.help=help;
		updateTable();
	}

	/**
	 * Prüft, ob eine Transportergruppe an einer bestimmten Station verwendet wird.
	 * @param name	Name der Transportergruppe
	 * @param element	Station bei der geprüft werden soll, ob an dieser die Transportergruppe verwendet wird
	 * @return	Liefert <code>true</code>, wenn die Transportergruppe an der Station verwendet wird
	 * @see #transporterInUse(String)
	 */
	private boolean transporterInUse(final String name, final ModelElementBox element) {
		if (element instanceof ModelElementTransportParking) {
			if (((ModelElementTransportParking)element).getTransporterType().equalsIgnoreCase(name)) return true;
		}

		if (element instanceof ModelElementTransportTransporterSource) {
			if (((ModelElementTransportTransporterSource)element).getTransporterType().equalsIgnoreCase(name)) return true;
		}

		return false;
	}

	/**
	 * Liefert eine Liste der Stations-IDs die eine bestimmte Transportergruppe verwenden.
	 * @param name	Name der Transportergruppe
	 * @return	Liste der Stations-IDs an denen die Transportergruppe verwendet wird
	 */
	private List<Integer> transporterInUse(final String name) {
		final List<Integer> usingIDs=new ArrayList<>();

		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementBox) {
			if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) if (sub instanceof ModelElementBox) {
				if (transporterInUse(name,(ModelElementBox)sub)) usingIDs.add(sub.getId());
			}
			if (transporterInUse(name,(ModelElementBox)element)) usingIDs.add(element.getId());
		}

		return usingIDs;
	}

	/**
	 * Weist das Datenmodell an, die Tabelle zu aktualisieren.
	 */
	public void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	@Override
	public int getRowCount() {
		return transporters.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==transporters.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Transporters.Group.Add")},new Icon[]{Images.MODELPROPERTIES_TRANSPORTERS_ADD.getIcon()},new ActionListener[]{new AddButtonListener()});
			case 1: return "";
			case 2: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			String icon=transporters.get(rowIndex).getEastLoadedIcon();
			if (icon==null || icon.isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_TRANSPORTER_EAST_ICON_NAME;
			final Image image=imageSource.get(icon,model.animationImages,16);
			return makeEditPanelSmallBorderIcon(
					new ImageIcon(image),
					"<html><body>"+transporters.get(rowIndex).getName()+" <span style=\"color: gray;\">"+String.format(Language.tr("Transporters.Group.ID"),rowIndex+1)+"</span>",
					new Icon[]{Images.GENERAL_SETUP.getIcon(),Images.EDIT_DELETE.getIcon()},new String[]{Language.tr("Transporters.Group.Edit"),Language.tr("Transporters.Group.Delete")},
					new ActionListener[]{new EditButtonListener(rowIndex),new DeleteButtonListener(rowIndex)}
					);
		case 1:
			return makeEditPanelSmallBorder(""+transporters.get(rowIndex).getCountAll(),new URL[0],new String[0],new ActionListener[0]);
		case 2:
			return makeEditPanelSmallBorder(""+transporters.get(rowIndex).getCapacity(),new URL[0],new String[0],new ActionListener[0]);
		default: return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	/**
	 * Informiert eine Reihe von Stationen darüber, dass sich der Name einer Transportergruppe geändert hat.
	 * @param usingIDs	Liste der IDs der zu benachrichtigenden Stationen
	 * @param oldName	Alter Name der Transportergruppe
	 * @param newName	Neuer Name der Transportergruppe
	 */
	private void renameTransporterInStations(final List<Integer> usingIDs, final String oldName, final String newName) {
		for (Integer id: usingIDs) {
			final ModelElement element=surface.getByIdIncludingSubModels(id);
			if (element instanceof ModelDataRenameListener) ((ModelDataRenameListener)element).objectRenamed(oldName,newName,ModelDataRenameListener.RenameType.RENAME_TYPE_TRANSPORTER);
		}
	}

	/**
	 * Erstellt einen Infotext, der darauf hinweist, dass eine Transportergruppe an einer oder mehreren Stationen verwendet wird
	 * @param sb	Ausgabe-{@link StringBuilder}
	 * @param usingIDs	Liste der IDs an denen die Transportergruppe verwendet wird
	 */
	private void getInUseInfoText(final StringBuilder sb, List<Integer> usingIDs) {
		if (usingIDs.size()==0) return;

		if (usingIDs.size()==1) {
			sb.append(Language.tr("Transporters.Group.InUse.Single"));
		} else {
			sb.append(Language.tr("Transporters.Group.InUse.Multiple"));
		}
		sb.append("\n");
		sb.append(usingIDs.get(0));
		for (int i=1;i<usingIDs.size();i++) {sb.append(", "); sb.append(usingIDs.get(i));}
		sb.append("\n\n");
	}


	/**
	 * Liefert einen Namensvorschlag für eine neue Transportergruppe
	 * @return	Namensvorschlag für eine neue Transportergruppe (Namensvorschlag existiert sich noch nicht als Transportergruppe)
	 */
	private String getFreeName() {
		final String baseName=Language.tr("Transporters.Group.DefaultName");
		String name=baseName;
		int nr=0;

		boolean ok=false;
		while (!ok) {
			ok=true;
			nr++;
			name=baseName+" "+nr;
			for (ModelTransporter transporter: transporters) if (transporter.getName().equalsIgnoreCase(name)) {ok=false; break;}
		}

		return name;
	}

	/**
	 * Reagiert auf Klicks auf die Hinzufügen-Schaltfläche.
	 */
	private class AddButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public AddButtonListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			final ModelTransporter transporter=new ModelTransporter(getFreeName());
			final String[] names=transporters.stream().map(t->t.getName()).toArray(String[]::new);

			final TransporterTableModelDialog dialog=new TransporterTableModelDialog(table,help,names,transporter,model,surface,model.animationImages);
			dialog.setVisible(true);
			if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
			transporters.add(transporter);
			updateTable();
		}
	}

	/**
	 * Reagiert auf Klicks auf die Bearbeiten-Schaltfläche.
	 */
	private class EditButtonListener implements ActionListener {
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param row	Zeilennummer
		 */
		public EditButtonListener(final int row) {
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			final ModelTransporter transporter=transporters.get(row);
			final String oldName=transporter.getName();
			final String[] names=transporters.stream().map(t->t.getName()).toArray(String[]::new);

			final TransporterTableModelDialog dialog=new TransporterTableModelDialog(table,help,names,transporter,model,surface,model.animationImages);
			dialog.setVisible(true);
			if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

			final String newName=transporter.getName();
			if (!newName.equals(oldName)) {
				final List<Integer> usingIDs=transporterInUse(oldName);
				final StringBuilder sb=new StringBuilder();
				if (usingIDs.size()>0) {
					getInUseInfoText(sb,usingIDs);
					sb.append("<br>");
					sb.append(Language.tr("Transporter.Group.Edit.ChangeName.Info"));
					if (MsgBox.confirm(table,Language.tr("Transporter.Group.Edit.ChangeName.Title"),"<html><body>"+sb.toString(),Language.tr("Transporter.Group.Edit.ChangeName.YesInfo"),Language.tr("Transporter.Group.Edit.ChangeName.NoInfo"))) {
						renameTransporterInStations(usingIDs,oldName,newName);
					}
				}
			}
			updateTable();
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Transporters.Group.RowTitle.Name");
		case 1: return Language.tr("Transporters.Group.RowTitle.Count");
		case 2: return Language.tr("Transporters.Group.RowTitle.Capacity");
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
			final String name=transporters.get(row).getName();

			final List<Integer> usingIDs=transporterInUse(name);
			final StringBuilder sb=new StringBuilder();
			getInUseInfoText(sb,usingIDs);
			sb.append("<br>");
			sb.append(String.format(Language.tr("Transporters.Group.Delete.Confirmation"),name));
			if ((e.getModifiers() & ActionEvent.SHIFT_MASK)==0) {
				if (!MsgBox.confirm(table,Language.tr("Transporters.Group.Delete"),"<html><body>"+sb.toString()+"</body></html>",Language.tr("Transporters.Group.Delete.YesInfo"),Language.tr("Transporters.Group.Delete.NoInfo"))) return;
			}
			transporters.remove(row);

			updateTable();
		}
	}

	/**
	 * Liefert alle momentan in dem Datenmodell enthaltenen Transportergruppen-Datensätze
	 * @return	Liste mit den Transportergruppen-Datensätze
	 */
	public ModelTransporters getTransporters() {
		final ModelTransporters transporters=new ModelTransporters();
		transporters.getTransporters().addAll(this.transporters);
		return transporters;
	}
}