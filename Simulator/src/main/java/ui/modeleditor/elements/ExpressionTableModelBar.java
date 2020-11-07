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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.table.TableCellEditor;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;

/**
 * Stellt ein Tabellenmodell zur Anzeige und Bearbeitung der Diagrammereihen in einem
 * <code>ModelElementAnimationBarChart</code>-Element zur Verfügung.
 * @author Alexander Herzog
 * @see ModelElementAnimationBarChart
 */
public class ExpressionTableModelBar extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 226251170994598281L;

	/**
	 * Darzustelltende Icons
	 * @author Alexander Herzog
	 */
	public enum IconMode {
		/** Balkendiagramme */
		BAR,
		/** Tortendiagramme */
		PIE
	}

	/** Vor den Tabelleneinträgen stilisierte Balken- oder Liniendiagramme darstellen? */
	private final IconMode iconMode;

	/** Tabelle in der das Datenmodell zum Einsatz kommen soll */
	private final JTableExt table;
	/** Hilfe-Callback welches aufgerufen wird, wenn in einem der untergeordneten Dialoge auf die "Hilfe"-Schaltfläche geklickt wird. */
	private final Runnable help;
	/** Nur-Lese-Status */
	private final boolean readOnly;

	/**
	 * Liste der globalen Variablen (zum Prüfen von Ausdrücken)
	 */
	private final String[] variableNames;

	/**
	 * Liste mit allen globalen Variablen und ihren Startwerten
	 */
	private final Map<String,String> initialVariableValues;

	/**
	 * Zuordnung von Stations-IDs zu Stationsbeschreibungen
	 */
	private final Map<Integer,String> stationIDs;

	/**
	 * Zuordnung von Stations-IDs zu Stationsnamen
	 */
	private final Map<Integer,String> stationNameIDs;

	/**
	 * In der Tabelle anzuzeigende Rechenausdrücke
	 */
	private final List<String> expression=new ArrayList<>();

	/**
	 * In der Tabelle anzuzeigende Farben
	 */
	private final List<Color> expressionColor=new ArrayList<>();

	/**
	 * Konstruktor der Klasse <code>ExpressionTableModel</code>
	 * @param table	Tabelle in der das Datenmodell zum Einsatz kommen soll
	 * @param element	Modell-Element aus dem die Daten geladen werden sollen
	 * @param readOnly	Gibt an, ob die Daten bearbeitet werden dürfen (<code>false</code>) oder nicht (<code>true</code>).
	 * @param help	Hilfe-Callback welches aufgerufen wird, wenn in einem der untergeordneten Dialoge auf die "Hilfe"-Schaltfläche geklickt wird.
	 */
	public ExpressionTableModelBar(final JTableExt table, final ModelElementAnimationBarChart element, final boolean readOnly, final Runnable help) {
		super();
		iconMode=IconMode.BAR;
		this.help=help;
		this.table=table;
		this.readOnly=readOnly;
		variableNames=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false);
		initialVariableValues=element.getModel().getInitialVariablesWithValues();
		stationIDs=ExpressionBuilder.getStationIDs(element.getSurface());
		stationNameIDs=ExpressionBuilder.getStationNameIDs(element.getSurface());

		final List<Object[]> data=element.getExpressionData();
		for (Object[] row: data) if (row.length==2) {
			if (!(row[0] instanceof String)) continue;
			if (!(row[1] instanceof Color)) continue;
			expression.add((String)row[0]);
			expressionColor.add((Color)row[1]);
		}

		updateTable();
	}

	/**
	 * Konstruktor der Klasse <code>ExpressionTableModel</code>
	 * @param table	Tabelle in der das Datenmodell zum Einsatz kommen soll
	 * @param element	Modell-Element aus dem die Daten geladen werden sollen
	 * @param readOnly	Gibt an, ob die Daten bearbeitet werden dürfen (<code>false</code>) oder nicht (<code>true</code>).
	 * @param help	Hilfe-Callback welches aufgerufen wird, wenn in einem der untergeordneten Dialoge auf die "Hilfe"-Schaltfläche geklickt wird.
	 */
	public ExpressionTableModelBar(final JTableExt table, final ModelElementAnimationPieChart element, final boolean readOnly, final Runnable help) {
		super();
		iconMode=IconMode.PIE;
		this.help=help;
		this.table=table;
		this.readOnly=readOnly;
		variableNames=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false);
		initialVariableValues=element.getModel().getInitialVariablesWithValues();
		stationIDs=ExpressionBuilder.getStationIDs(element.getSurface());
		stationNameIDs=ExpressionBuilder.getStationNameIDs(element.getSurface());

		final List<Object[]> data=element.getExpressionData();
		for (Object[] row: data) if (row.length==2) {
			if (!(row[0] instanceof String)) continue;
			if (!(row[1] instanceof Color)) continue;
			expression.add((String)row[0]);
			expressionColor.add((Color)row[1]);
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
		return expression.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==expression.size()) {
			if (columnIndex>0) return "";
			final Images image;
			final String addString;
			switch (iconMode) {
			case BAR:
				image=Images.MODELEDITOR_ELEMENT_ANIMATION_BAR_CHART_ADD;
				addString=Language.tr("Surface.ExpressionTableModel.Add");
				break;
			case PIE:
				image=Images.MODELEDITOR_ELEMENT_ANIMATION_PIE_CHART_ADD;
				addString=Language.tr("Surface.ExpressionTableModel.AddSegment");
				break;
			default:
				image=null;
				addString="";
			}
			return makeButtonPanel(new String[]{addString},new URL[]{(image==null)?null:image.getURL()},new ActionListener[]{new EditButtonListener(0,-1)});
		}

		switch (columnIndex) {
		case 0:
			final Images icon;
			switch (iconMode) {
			case BAR: icon=Images.MODELEDITOR_ELEMENT_ANIMATION_BAR_CHART; break;
			case PIE: icon=Images.MODELEDITOR_ELEMENT_ANIMATION_PIE_CHART; break;
			default: icon=null;
			}
			return makeEditPanelSmallBorder(
					(icon==null)?null:icon.getURL(),
							expression.get(rowIndex),
							new URL[]{Images.GENERAL_SETUP.getURL(),Images.EDIT_DELETE.getURL()},
							new String[]{Language.tr("Surface.ExpressionTableModel.Edit"),Language.tr("Surface.ExpressionTableModel.Delete")},
							new ActionListener[]{new EditButtonListener(0,rowIndex),new DeleteButtonListener(rowIndex)}
					);
		case 1:
			List<URL> icons=new ArrayList<>();
			List<String> hints=new ArrayList<>();
			List<ActionListener> actions=new ArrayList<>();
			icons.add(Images.GENERAL_SETUP.getURL());
			hints.add(Language.tr("Surface.ExpressionTableModel.SetupAppearance"));
			actions.add(new EditButtonListener(1,rowIndex));
			if (rowIndex>0) {
				icons.add(Images.ARROW_UP.getURL());
				hints.add(Language.tr("Surface.ExpressionTableModel.MoveUp"));
				actions.add(new EditButtonListener(2,rowIndex));
			}
			if (rowIndex<expression.size()-1) {
				icons.add(Images.ARROW_DOWN.getURL());
				hints.add(Language.tr("Surface.ExpressionTableModel.MoveDown"));
				actions.add(new EditButtonListener(3,rowIndex));
			}
			BufferedImage image=new BufferedImage(22,22,BufferedImage.TYPE_4BYTE_ABGR);
			Graphics g=image.getGraphics();
			g.setColor(expressionColor.get(rowIndex));
			g.fillRect(0,0,22,22);
			g.setColor(Color.BLACK);
			g.drawRect(0,0,21,21);
			return makeEditPanelSmallBorderIcon(
					new ImageIcon(image),
					"",
					icons.toArray(new URL[0]),
					hints.toArray(new String[0]),
					actions.toArray(new ActionListener[0])
					);
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
		case 0: return Language.tr("Surface.ExpressionTableModel.ColumnExpression");
		case 1: return Language.tr("Surface.ExpressionTableModel.ColumnAppearance");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Schreibt die geänderten Datenreihen in das <code>ModelElementAnimationBarChart</code>-Element zurück
	 * @param element	Objekt, in das die Daten zur Diagrammdarstellung geschrieben werden sollen
	 */
	public void storeData(ModelElementAnimationBarChart element) {
		final List<Object[]> data=new ArrayList<>();
		for (int i=0;i<expression.size();i++) {
			if (i>=expressionColor.size()) break;

			Object[] row=new Object[2];
			row[0]=expression.get(i);
			row[1]=expressionColor.get(i);

			data.add(row);
		}

		element.setExpressionData(data);
	}

	/**
	 * Schreibt die geänderten Datenreihen in das <code>ModelElementAnimationPieChart</code>-Element zurück
	 * @param element	Objekt, in das die Daten zur Diagrammdarstellung geschrieben werden sollen
	 */
	public void storeData(ModelElementAnimationPieChart element) {
		final List<Object[]> data=new ArrayList<>();
		for (int i=0;i<expression.size();i++) {
			if (i>=expressionColor.size()) break;

			Object[] row=new Object[2];
			row[0]=expression.get(i);
			row[1]=expressionColor.get(i);

			data.add(row);
		}

		element.setExpressionData(data);
	}

	/**
	 * Löscht alle bisherigen Diagrammreihen
	 */
	public void clear() {
		updateTable();
		expression.clear();
		expressionColor.clear();
	}

	/**
	 * Fügt eine neue Diagrammreihe zu der Tabelle hinzu
	 * @param command	Ausdruck für die Diagrammreihe
	 * @param color	Farbe für die Diagrammreihe
	 */
	public void add(final String command, final Color color) {
		updateTable();
		expression.add((command==null)?"":command);
		expressionColor.add((color==null)?Color.BLUE:color);
	}

	/**
	 * Reagiert auf Klicks auf die Bearbeiten und Verschieben-Schaltflächen
	 */
	private class EditButtonListener implements ActionListener {
		/** Auszuführender Befehl (0: Bearbeiten oder Hinzufügen, 1: Farbe ändern, 2: Nach oben verschieben, 3: Nach unten verschieben) */
		private final int nr;
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param nr	Auszuführender Befehl (0: Bearbeiten oder Hinzufügen, 1: Farbe ändern, 2: Nach oben verschieben, 3: Nach unten verschieben)
		 * @param row	Zeilennummer
		 */
		public EditButtonListener(final int nr, final int row) {
			this.nr=nr;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			String s;
			Color c;

			ExpressionTableModelDialog1 dialog1;
			ExpressionTableModelDialog2 dialog2;

			switch (nr) {
			case 0: /* Ausdruck bearbeiten (auch neuen Eintrag anlegen) */
				if (row<0) {
					dialog1=new ExpressionTableModelDialog1(table,"",variableNames,initialVariableValues,stationIDs,stationNameIDs,help,iconMode);
				} else {
					dialog1=new ExpressionTableModelDialog1(table,expression.get(row),variableNames,initialVariableValues,stationIDs,stationNameIDs,help,iconMode);
				}
				if (dialog1.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row<0) {
						expression.add(dialog1.getExpression());
						expressionColor.add(Color.BLUE);
					} else {
						expression.set(row,dialog1.getExpression());
					}
					updateTable();
				}
				break;
			case 1: /* Farbe */
				dialog2=new ExpressionTableModelDialog2(table,expressionColor.get(row),help);
				if (dialog2.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					expressionColor.set(row,dialog2.getColor());
					updateTable();
				}
				break;
			case 2: /* Nach oben */
				if (row>0) {
					s=expression.get(row); expression.set(row,expression.get(row-1)); expression.set(row-1,s);
					c=expressionColor.get(row); expressionColor.set(row,expressionColor.get(row-1)); expressionColor.set(row-1,c);
					updateTable();
				}
				break;
			case 3: /* Nach unten */
				if (row<expression.size()-1) {
					s=expression.get(row); expression.set(row,expression.get(row+1)); expression.set(row+1,s);
					c=expressionColor.get(row); expressionColor.set(row,expressionColor.get(row+1)); expressionColor.set(row+1,c);
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

			final String name=expression.get(row);
			if (!MsgBox.confirm(table,Language.tr("Surface.ExpressionTableModel.Delete.Confirmation.Title"),String.format(Language.tr("Surface.ExpressionTableModel.Delete.Confirmation.Info"),name),Language.tr("Surface.ExpressionTableModel.Delete.Confirmation.YesInfo"),Language.tr("Surface.ExpressionTableModel.Delete.Confirmation.NoInfo"))) return;
			expression.remove(row);
			expressionColor.remove(row);
			updateTable();
		}
	}
}