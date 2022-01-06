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
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.TableCellEditor;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Tabellenmodelle zur Festlegung von Prioritäten für Kunden
 * @author Alexander Herzog
 * @see ModelElementProcessDialog
 * @see ModelElementTransportTransporterSource
 */
public class PriorityTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4369405484601947657L;

	/** Tabelle in der das Datenmodell zum Einsatz kommen soll */
	private final JTableExt table;
	/** Editor-Element (entweder vom Typ {@link ModelElementProcess} oder {@link ModelElementTransportTransporterSource}) */
	private final ModelElement element;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Liste aller Kundentypen in dem Modell */
	private final String[] clientTypes;
	/** Liste der zu den Kunden in {@link #clientTypes} gehörenden Prioritäten */
	private final String[] priorities;

	/**
	 * Konstruktor der Klasse
	 * @param table	Tabelle in der das Datenmodell zum Einsatz kommen soll
	 * @param element	Editor-Element (entweder vom Typ {@link ModelElementProcess} oder {@link ModelElementTransportTransporterSource})
	 * @param readOnly	Nur-Lese-Status
	 */
	public PriorityTableModel(final JTableExt table, final ModelElement element, final boolean readOnly) {
		super();
		this.table=table;
		this.readOnly=readOnly;
		this.element=element;

		clientTypes=element.getSurface().getClientTypes().toArray(new String[0]);
		priorities=new String[clientTypes.length];
		if (element instanceof ModelElementProcess) {
			for (int i=0;i<clientTypes.length;i++) priorities[i]=((ModelElementProcess)element).getPriority(clientTypes[i]);
		}
		if (element instanceof ModelElementTransportTransporterSource) {
			for (int i=0;i<clientTypes.length;i++) priorities[i]=((ModelElementTransportTransporterSource)element).getClientPriority(clientTypes[i]);
		}
		if (element instanceof ModelElementHold) {
			for (int i=0;i<clientTypes.length;i++) priorities[i]=((ModelElementHold)element).getPriority(clientTypes[i]);
		}
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
		return clientTypes.length;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex>=1 && !readOnly;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.Process.Dialog.Priority.ClientType");
		case 1: return Language.tr("Surface.Process.Dialog.Priority.Priority");
		case 2: return Language.tr("Surface.Process.Dialog.Priority.Edit");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Objekt das die verfügbaren Animations-Icons vorhält
	 */
	private final AnimationImageSource imageSource=new AnimationImageSource();

	/**
	 * Liefert das Icon für eine Bedienergruppe.
	 * @param name	Bedienergruppe
	 * @return	Icon für die Bedienergruppe
	 */
	private Icon getClientTypeIcon(final String name) {
		String icon=null;
		if (name!=null) icon=element.getModel().clientData.getIcon(name);
		if (icon==null || icon.trim().isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_CLIENT_ICON_NAME;
		return new ImageIcon(imageSource.get(icon,element.getModel().animationImages,16));
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		switch (columnIndex) {
		case 0: return makePanelIcon(clientTypes[rowIndex],getClientTypeIcon(clientTypes[rowIndex]));
		case 1: return priorities[rowIndex];
		case 2: return makeButtonPanel(new String[]{""},new String[]{Language.tr("Editor.DialogBase.ExpressionEditTooltip")},new Icon[]{Images.EXPRESSION_BUILDER.getIcon()},new ActionListener[] {e->editExpression(rowIndex)});
		default: return null;
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex!=1) return;
		if (!(aValue instanceof String)) return;
		priorities[rowIndex]=(String)aValue;
	}

	/**
	 * Öffnet den Dialog, um einen Prioritäts-Rechenausdruck zu bearbeiten
	 * @param rowIndex	Index bzw. Zeile des zu bearbeitenden Prioritäts-Rechenausdrucks
	 */
	private void editExpression(final int rowIndex) {
		if (readOnly) return;

		final EditModel model=element.getModel();
		final ModelSurface mainSurface=model.surface;
		final String[] variableNames=element.getSurface().getMainSurfaceVariableNames(model.getModelVariableNames(),true);
		final Map<Integer,String> stationIDs=ExpressionBuilder.getStationIDs(mainSurface);
		final Map<Integer,String> stationNameIDs=ExpressionBuilder.getStationNameIDs(mainSurface);
		final Map<String,String> initialVariableValues=model.getInitialVariablesWithValues();
		final ExpressionBuilder dialog=new ExpressionBuilder(table,priorities[rowIndex],false,variableNames,initialVariableValues,stationIDs,stationNameIDs,true,false,false);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			priorities[rowIndex]=dialog.getExpression();
			updateTable();
		}
	}

	/**
	 * Überprüft die Eingaben
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird im Fehlerfall eine Fehlermeldung ausgegeben
	 * @return	Gibt <code>true</code> zurück, wenn die Eingaben in Ordnung sind
	 */
	public boolean checkInput(final boolean showErrorMessage) {
		for (int i=0;i<clientTypes.length;i++) {
			final int error=ExpressionCalc.check(priorities[i],element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true));
			if (error>=0) {
				if (showErrorMessage) MsgBox.error(table,Language.tr("Surface.Process.Dialog.Priority.Error.Title"),String.format(Language.tr("Surface.Process.Dialog.Priority.Error.Info"),clientTypes[i],priorities[i]));
				return false;
			}
		}
		return true;
	}

	/**
	 * Schreibt die Einstellungen in das im Konstruktor übergebene Modell-Element zurück.
	 */
	public void storeData() {
		if (element instanceof ModelElementProcess) {
			for (int i=0;i<clientTypes.length;i++) ((ModelElementProcess)element).setPriority(clientTypes[i],priorities[i]);
		}
		if (element instanceof ModelElementTransportTransporterSource) {
			for (int i=0;i<clientTypes.length;i++) ((ModelElementTransportTransporterSource)element).getClientPriorities().put(clientTypes[i],priorities[i]);
		}
		if (element instanceof ModelElementHold) {
			for (int i=0;i<clientTypes.length;i++) ((ModelElementHold)element).setPriority(clientTypes[i],priorities[i]);
		}
	}
}