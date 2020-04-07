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

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportDestination;
import ui.modeleditor.elements.ModelElementTransportParking;
import ui.modeleditor.elements.ModelElementTransportTransporterSource;

/**
 * Tabellenmodelle zur Festlegung der Entfernungen der Stationen
 * aus Sicht eines Transporters
 * @author Alexander Herzog
 * @see TransporterTableModelDialog
 */
public class TransporterDistancesTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -8770320675401142234L;

	private final Component owner;
	private final JTable table;
	private final ModelTransporter transporter;
	private final List<ModelElementBox> stationElements;
	private final List<String> stations;
	private final List<String> stationsLong;
	private final String[][] distances;
	private final boolean readOnly;
	private final Runnable help;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element (zur Platzierung von Dialogen)
	 * @param table	Zu diesem Tabellenmodell gehörige Tabelle
	 * @param transporter	Transporter-Objekt aus dem die Anzahl-Werte ausgelesen werden sollen und in das sie später ggf. zurückgeschrieben werden sollen
	 * @param surface	Zeichenfläche (zur Ermittlung der Namen der Stationen)
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Runnable
	 */
	public TransporterDistancesTableModel(final Component owner, final JTable table, final ModelTransporter transporter, final ModelSurface surface, final boolean readOnly, final Runnable help) {
		super();
		this.owner=owner;
		this.table=table;
		this.transporter=transporter;
		this.readOnly=readOnly;
		this.help=help;

		/* Liste der Stationen zusammenstellen */
		stations=new ArrayList<>();
		stationsLong=new ArrayList<>();
		stationElements=new ArrayList<>();

		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element).getSubSurface().getElements()) {
				addStation(element2,element);
			}
			addStation(element,null);
		}

		distances=new String[this.stations.size()][this.stations.size()];
		for (int i=0;i<distances.length;i++) {
			distances[i]=new String[this.stations.size()];
			Arrays.fill(distances[i],"0");
		}

		/* Daten aus Transporter-Objekt auslesen */
		for (Map.Entry<String,Map<String,Double>> entryA: transporter.getDistances().entrySet()) {
			final String stationA=entryA.getKey();
			for (Map.Entry<String,Double> entryB: entryA.getValue().entrySet()) {
				final String stationB=entryB.getKey();
				final double distance=entryB.getValue().doubleValue();
				final int indexA=stations.indexOf(stationA);
				final int indexB=stations.indexOf(stationB);
				if (indexA>=0 && indexB>=0) distances[indexA][indexB]=NumberTools.formatNumber(distance);
			}
		}
	}

	private void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	private void addStation(final ModelElement element, final ModelElement parent) {
		if ((element instanceof ModelElementTransportTransporterSource) || (element instanceof ModelElementTransportParking) || (element instanceof ModelElementTransportDestination)) {
			final String name=element.getName();
			if (!name.isEmpty()) {
				stations.add(name);
				if (parent!=null) {
					String parentName=parent.getName().trim();
					if (parentName.isEmpty() && parent instanceof ModelElementBox) parentName=((ModelElementBox)parent).getTypeName();
					stationsLong.add(name+" (id="+element.getId()+") in "+parentName+" (id="+parent.getId()+")");
				} else {
					stationsLong.add(name+" (id="+element.getId()+")");
				}
				stationElements.add((ModelElementBox)element);
			}
		}
	}

	@Override
	public int getRowCount() {
		return distances.length;
	}

	@Override
	public int getColumnCount() {
		return 1+distances.length;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex>0 && !readOnly && rowIndex!=columnIndex-1;
	}

	@Override
	public String getColumnName(int column) {
		if (column==0) return Language.tr("Transporters.Group.Edit.Dialog.Distances.Label");
		return stationsLong.get(column-1);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex==0) return stationsLong.get(rowIndex);
		if (rowIndex==columnIndex-1) return "-";
		return distances[rowIndex][columnIndex-1];
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex==0) return;
		distances[rowIndex][columnIndex-1]=(String)aValue;
	}

	/**
	 * Überprüft die Eingaben
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird im Fehlerfall eine Fehlermeldung ausgegeben
	 * @return	Gibt <code>true</code> zurück, wenn die Eingaben in Ordnung sind
	 */
	public boolean checkInput(final boolean showErrorMessage) {
		for (int i=0;i<distances.length;i++) for (int j=0;j<distances[i].length;j++) {
			if (i==j) continue;
			final Double D=NumberTools.getNotNegativeDouble(distances[i][j]);
			if (D==null) {
				if (showErrorMessage) MsgBox.error(owner,Language.tr("Transporters.Group.Edit.Dialog.Distances.ErrorTitle"),String.format(Language.tr("Transporters.Group.Edit.Dialog.Distances.ErrorInfo"),stationsLong.get(i),stationsLong.get(j),distances[i][j]));
				return false;
			}
		}
		return true;
	}

	/**
	 * Schreibt die Einstellungen in das im Konstruktor übergebene Transporter-Objekt zurück.
	 */
	public void storeData() {
		transporter.getDistances().clear();
		for (int i=0;i<distances.length;i++) for (int j=0;j<distances[i].length;j++) {
			if (i==j) continue;
			final Double D=NumberTools.getNotNegativeDouble(distances[i][j]);
			if (D!=null) transporter.setDistance(stations.get(i),stations.get(j),D.doubleValue());
		}
	}

	/**
	 * Kopiert die rechte obere in die linke untere Hälfte der Tabelle
	 */
	public void fillDown() {
		if (distances.length<2 || readOnly) return;
		for (int i=0;i<distances.length-1;i++) for (int j=i+1;j<distances.length;j++) distances[j][i]=distances[i][j];
		updateTable();
	}

	/**
	 * Kopiert die linke untere in die rechts obere Hälfte der Tabelle
	 */
	public void fillUp() {
		if (distances.length<2 || readOnly) return;
		for (int i=0;i<distances.length-1;i++) for (int j=i+1;j<distances.length;j++) distances[i][j]=distances[j][i];
		updateTable();
	}

	private ModelElementSub getParentStation(final ModelSurface surface, final ModelElementBox child) {
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementSub) {
			for (ModelElement element2: ((ModelElementSub)element).getSubSurface().getElements()) if (element2==child) return (ModelElementSub)element;
		}
		return null; /* Nicht gefunden */
	}

	private int getModelDistance(final ModelSurface surface, ModelElementBox station1, ModelElementBox station2) {
		if (station1.getSurface()!=station2.getSurface()) {
			/* Elemente sind auf verschiedenen Modell-Ebenen, beide auf Hauptebene projizieren */
			if (station1.getSurface().getParentSurface()!=null) station1=getParentStation(surface,station1);
			if (station2.getSurface().getParentSurface()!=null) station2=getParentStation(surface,station2);
		}

		if (station1==null || station2==null) return 0;

		final Point p1=station1.getMiddlePosition(true);
		final Point p2=station2.getMiddlePosition(true);
		final int x=p1.x-p2.x;
		final int y=p1.y-p2.y;

		return (int)Math.round(Math.sqrt(x*x+y*y));
	}

	private int[][] getModelDistances(final ModelSurface surface) {
		final int[][] modelDistances=new int[stationElements.size()][];

		for (int i=0;i<stationElements.size();i++) {
			modelDistances[i]=new int[stationElements.size()];
			for (int j=0;j<stationElements.size();j++) {
				if (i==j) modelDistances[i][j]=0; else {
					if (j<i) modelDistances[i][j]=modelDistances[j][i]; else modelDistances[i][j]=getModelDistance(surface,stationElements.get(i),stationElements.get(j));
				}
			}
		}
		return modelDistances;
	}

	private void fillByModel(final ModelSurface surface, final double minDistance, final double maxDistance) {
		/* Abstände im Modell bestimmen */
		final int[][] modelDistances=getModelDistances(surface);

		/* Minimum/Maximum für Skalierung berechnen */
		double min=Double.MAX_VALUE;
		double max=0;
		for (int i=0;i<modelDistances.length-1;i++) for (int j=i+1;j<modelDistances.length;j++) if (i!=j) {
			double d=modelDistances[i][j];
			if (d>max) max=d;
			if (d<min) min=d;
		}

		/* Transporter-Abstände berechnen */
		for (int i=0;i<distances.length;i++) for (int j=0;j<distances.length;j++) if (i!=j) {
			double d=modelDistances[i][j];
			if (max==min) {
				d=(maxDistance-minDistance)/2; /* Nur ein Abstand. */
			} else {
				d=(d-min)/(max-min)*(maxDistance-minDistance)+minDistance;
			}
			if (maxDistance-minDistance>=20) {
				distances[i][j]=""+Math.round(d);
			} else {
				distances[i][j]=NumberTools.formatNumber(d);
			}
		}

		updateTable();
	}

	/**
	 * Belegt die Entfernungentabelle initial mit Abständen basierend auf den Abständen der Stationen auf der Zeichenfläche
	 * @param surface	Zeichenfläche aus der die Abstände der Stationen ausgelesen werden sollen
	 */
	public void fillByModel(final ModelSurface surface) {
		final TransporterDistancesTableModelScaleDialog dialog=new TransporterDistancesTableModelScaleDialog(owner,help);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			fillByModel(surface,dialog.getMinDistance(),dialog.getMaxDistance());
		}
	}

	/**
	 * Kopiert die Daten als Tabelle in die Zwischenablage
	 */
	public void copyToClipboard() {
		final StringBuilder sb=new StringBuilder();

		final String[] line=new String[getColumnCount()];

		for (int i=0;i<line.length;i++) {
			if (i>0) sb.append('\t');
			sb.append(getColumnName(i));
		}
		sb.append('\n');

		for (int i=0;i<getRowCount();i++) {
			for (int j=0;j<line.length;j++) {
				if (j>0) sb.append('\t');
				sb.append(getValueAt(i,j).toString());
			}
			sb.append('\n');
		}

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()),null);
	}

	/**
	 * Versucht die Daten aus der Zwischenablage in das Tabellenobjekt zu kopieren
	 * @return	Gibt an, ob die Daten geladen werden konnten
	 */
	public boolean pasteFromClipboard() {
		/* Text aus Zwischenablage laden */
		String data=null;
		try {
			data=(String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException | UnsupportedFlavorException | IOException e) {return false;}
		if (data==null || data.isEmpty()) return false;

		/* Tabelle aufbauen und prüfen */
		final Table table=new Table();
		table.load(data);
		if (table.getSize(0)!=table.getSize(1)) {
			MsgBox.error(owner,Language.tr("Transporters.Group.Edit.Dialog.Distances.Paste.ErrorTitle"),Language.tr("Transporters.Group.Edit.Dialog.Distances.Paste.ErrorTableNotSquare"));
			return false;
		}
		if (table.getSize(0)!=distances.length && table.getSize(0)!=distances.length+1) {
			MsgBox.error(owner,Language.tr("Transporters.Group.Edit.Dialog.Distances.Paste.ErrorTitle"),String.format(Language.tr("Transporters.Group.Edit.Dialog.Distances.Paste.ErrorTableSize"),distances.length,distances.length,distances.length+1,distances.length+1));
			return false;
		}

		/* Daten in TableModel laden */
		final boolean hasHeadings=(table.getSize(0)==distances.length+1);

		for (int i=0;i<distances.length;i++) for (int j=0;j<distances.length;j++) if (i!=j) {
			distances[i][j]=table.getValue(hasHeadings?(i+1):i,hasHeadings?(j+1):j);
		}

		updateTable();
		return true;
	}
}