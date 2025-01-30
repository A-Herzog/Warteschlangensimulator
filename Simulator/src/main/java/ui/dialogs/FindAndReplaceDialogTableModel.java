/**
 * Copyright 2021 Alexander Herzog
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
package ui.dialogs;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ElementRendererTools;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementSub;
import ui.tools.FlatLaFHelper;

/**
 * Zeigt die Suchtreffer aus {@link FindAndReplaceDialog}
 * in einer Tabelle an.
 * @author Alexander Herzog
 * @see FindAndReplaceDialog
 * @see FullTextSearch
 */
public class FindAndReplaceDialogTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-4104623395257213584L;

	/**
	 * Tabelle in der das Datenmodell zum Einsatz kommen soll
	 */
	private final JTableExt table;

	/**
	 * Gesamtes Editor-Modell (wird verwendet um die Stationsbeschreibungen zu generieren)
	 */
	private final EditModel model;

	/**
	 * Liste der Suchtreffer
	 */
	private List<FullTextSearch.SearchMatch> results;

	/**
	 * Auswahlstatus der Suchtreffer aus {@link #results}
	 * @see #results
	 */
	private List<JCheckBox> selected;

	/**
	 * Darstellung der Stationen
	 */
	private List<JPanel> stationIcons;

	/**
	 * Konstruktor der Klasse
	 * @param table	Tabelle in der das Datenmodell zum Einsatz kommen soll
	 * @param model	Gesamtes Editor-Modell (wird verwendet um die Stationsbeschreibungen zu generieren)
	 */
	public FindAndReplaceDialogTableModel(final JTableExt table, final EditModel model) {
		this.table=table;
		this.model=model;
		setData(null);
	}

	/**
	 * Stellt eine neue Liste mit Suchtreffern ein.
	 * @param results	Liste der Suchtreffer (kann <code>null</code> sein, wenn keine Daten angezeigt werden sollen)
	 */
	public void setData(final List<FullTextSearch.SearchMatch> results) {
		if (results==null) {
			this.results=new ArrayList<>();
			selected=new ArrayList<>();
			stationIcons=new ArrayList<>();
		} else {
			this.results=results;
			selected=new ArrayList<>(results.size());
			stationIcons=new ArrayList<>(results.size());
			for (FullTextSearch.SearchMatch result: results) {
				final JCheckBox checkBox=new JCheckBox("",result.canReplace());
				checkBox.setEnabled(result.canReplace());
				if (FlatLaFHelper.isActive()) {
					checkBox.setBackground(UIManager.getColor("Table.background"));
				} else {
					checkBox.setOpaque(false);
				}
				checkBox.setBackground(Color.WHITE);
				selected.add(checkBox);
				stationIcons.add(null);
			}
		}

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
		return results.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex==0;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return "";
		case 1: return Language.tr("FindAndReplace.Table.Station");
		case 2: return Language.tr("FindAndReplace.Table.Match");
		default: return "";
		}
	}

	/**
	 * Generiert ein Panel für die Station.
	 * @param element	Station (darf <code>null</code> sein)
	 * @return	Panel zur Darstellung der Station
	 */
	private JPanel generateStationPanel(final ModelElement element) {
		if (element==null) {
			return ElementRendererTools.getIconRenderer(Images.MODEL.getIcon(),Language.tr("FindAndReplace.Table.Model"),null);
		}

		if (element instanceof ModelElementBox) {
			return ElementRendererTools.getElementRenderer((ModelElementBox)element,1.0,true,false,ElementRendererTools.GradientStyle.OFF);
		} else {
			return ElementRendererTools.getIconRenderer(Images.MODEL_ADD_STATION.getIcon(),element.getContextMenuElementName(),null);
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return selected.get(rowIndex);
		case 1:
			if (stationIcons.get(rowIndex)==null) stationIcons.set(rowIndex,generateStationPanel(results.get(rowIndex).station));
			return stationIcons.get(rowIndex);
		case 2:
			return results.get(rowIndex).getHTML(model);
		default:
			return null;
		}
	}

	/**
	 * Markiert alle oder keine der Suchtreffer.
	 * @param select	Suchtreffer markieren?
	 */
	public void select(final boolean select) {
		for (JCheckBox checkBox: selected) {
			if (!checkBox.isEnabled()) continue;
			checkBox.setSelected(select);
		}

		updateTable();
	}

	/**
	 * Führt die Textersetzung in allen gewählten Treffern aus.
	 * @param replaceText	Neuer Text
	 * @return	Liefert <code>true</code> wenn mindestens eine Ersetzung ausgeführt wurde.
	 */
	public boolean doReplace(final String replaceText) {
		boolean dataReplaced=false;
		for (int i=0;i<results.size();i++) if (selected.get(i).isSelected()) {
			if (results.get(i).replace(replaceText)) dataReplaced=true;
		}
		if (dataReplaced) setData(null);
		return dataReplaced;
	}

	/**
	 * Liefert die ID der Station in einer bestimmten Tabellenzeile
	 * @param row	Tabellenzeile
	 * @return	ID der Station in der Tabellenzeile (kann -1 sein, wenn sich in der Zeile keine Station befindet oder die Zeilenangabe ungültig ist)
	 */
	public int getRowElementID(final int row) {
		if (row<0 || row>=results.size()) return -1;

		final ModelElement station=results.get(row).station;
		if (station==null) return -1;
		final int selectedID=station.getId();

		for (ModelElement element: model.surface.getElements()) {
			if (element.getId()==selectedID) return selectedID;
			if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) {
				if (sub.getId()==selectedID) return element.getId();
			}
		}

		return -1;
	}
}
