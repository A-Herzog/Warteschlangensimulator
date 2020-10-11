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
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;

/**
 * Stellt ein Tabellenmodell zur Anzeige und Bearbeitung der Diagrammereihen in einem
 * <code>ModelElementAnimationDiagram</code>-Element zur Verfügung.
 * @author Alexander Herzog
 * @see ModelElementAnimationLineDiagram
 */
public class ExpressionTableModelLine extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3888805430308717205L;

	private final JTableExt table;
	private final Runnable help;
	private final boolean readOnly;
	private final String[] variableNames;
	private final Map<String,String> initialVariableValues;
	private final Map<Integer,String> stationIDs;
	private final Map<Integer,String> stationNameIDs;

	private final List<String> expression=new ArrayList<>();
	private final List<Double> minValue=new ArrayList<>();
	private final List<Double> maxValue=new ArrayList<>();
	private final List<Color> expressionColor=new ArrayList<>();
	private final List<Integer> expressionWidth=new ArrayList<>();

	/**
	 * Konstruktor der Klasse <code>ExpressionTableModel</code>
	 * @param table	Tabelle in der das Datenmodell zum Einsatz kommen soll
	 * @param element	Modell-Element aus dem die Daten geladen werden sollen
	 * @param readOnly	Gibt an, ob die Daten bearbeitet werden dürfen (<code>false</code>) oder nicht (<code>true</code>).
	 * @param help	Hilfe-Callback welches aufgerufen wird, wenn in einem der untergeordneten Dialoge auf die "Hilfe"-Schaltfläche geklickt wird.
	 */
	public ExpressionTableModelLine(final JTableExt table, final ModelElementAnimationLineDiagram element, final boolean readOnly, final Runnable help) {
		super();
		this.help=help;
		this.table=table;
		this.readOnly=readOnly;
		variableNames=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false);
		initialVariableValues=element.getModel().getInitialVariablesWithValues();
		stationIDs=ExpressionBuilder.getStationIDs(element.getSurface());
		stationNameIDs=ExpressionBuilder.getStationNameIDs(element.getSurface());

		final List<Object[]> data=element.getExpressionData();
		for (Object[] row: data) if (row.length==5) {
			if (!(row[0] instanceof String)) continue;
			if (!(row[1] instanceof Double)) continue;
			if (!(row[2] instanceof Double)) continue;
			if (!(row[3] instanceof Color)) continue;
			if (!(row[4] instanceof Integer)) continue;
			expression.add((String)row[0]);
			minValue.add((Double)row[1]);
			maxValue.add((Double)row[2]);
			expressionColor.add((Color)row[3]);
			expressionWidth.add((Integer)row[4]);
		}

		updateTable();
	}

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
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==expression.size()) {
			if (columnIndex>0) return "";
			return makeButtonPanel(new String[]{Language.tr("Surface.ExpressionTableModel.Add")},new URL[]{Images.MODELEDITOR_ELEMENT_ANIMATION_DIAGRAM_ADD.getURL()},new ActionListener[]{new EditButtonListener(0,-1)});
		}

		switch (columnIndex) {
		case 0:
			return makeEditPanelSmallBorder(
					Images.MODELEDITOR_ELEMENT_ANIMATION_DIAGRAM.getURL(),
					expression.get(rowIndex),
					new URL[]{Images.GENERAL_SETUP.getURL(),Images.EDIT_DELETE.getURL()},
					new String[]{Language.tr("Surface.ExpressionTableModel.Edit"),Language.tr("Surface.ExpressionTableModel.Delete")},
					new ActionListener[]{new EditButtonListener(0,rowIndex),new DeleteButtonListener(rowIndex)}
					);
		case 1:
			return makeEditPanelSmallBorder(
					NumberTools.formatNumber(minValue.get(rowIndex))+"..."+NumberTools.formatNumber(maxValue.get(rowIndex)),
					Images.GENERAL_SETUP.getURL(),
					Language.tr("Surface.ExpressionTableModel.SetupArea"),
					new EditButtonListener(1,rowIndex)
					);
		case 2:
			List<URL> icons=new ArrayList<>();
			List<String> hints=new ArrayList<>();
			List<ActionListener> actions=new ArrayList<>();
			icons.add(Images.GENERAL_SETUP.getURL());
			hints.add(Language.tr("Surface.ExpressionTableModel.SetupAppearance"));
			actions.add(new EditButtonListener(2,rowIndex));
			if (rowIndex>0) {
				icons.add(Images.ARROW_UP.getURL());
				hints.add(Language.tr("Surface.ExpressionTableModel.MoveUp"));
				actions.add(new EditButtonListener(3,rowIndex));
			}
			if (rowIndex<expression.size()-1) {
				icons.add(Images.ARROW_DOWN.getURL());
				hints.add(Language.tr("Surface.ExpressionTableModel.MoveDown"));
				actions.add(new EditButtonListener(4,rowIndex));
			}
			BufferedImage image=new BufferedImage(22,22,BufferedImage.TYPE_4BYTE_ABGR);
			Graphics g=image.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0,0,22,22);
			g.setColor(Color.BLACK);
			g.drawRect(0,0,21,21);
			g.setColor(expressionColor.get(rowIndex));
			int w=expressionWidth.get(rowIndex);
			for (int i=Math.max(0,11-(w-1)/2);i<=Math.min(22,11+w/2);i++) g.drawLine(1,i,21,i);
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
		case 1: return Language.tr("Surface.ExpressionTableModel.ColumnValueRange");
		case 2: return Language.tr("Surface.ExpressionTableModel.ColumnAppearance");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Schreibt die geänderten Datenreihen in das <code>ModelElementAnimationDiagram</code>-Element zurück
	 * @param element	Objekt, in das die Daten zur Diagrammdarstellung geschrieben werden sollen
	 */
	public void storeData(ModelElementAnimationLineDiagram element) {
		final List<Object[]> data=new ArrayList<>();
		for (int i=0;i<expression.size();i++) {
			if (i>=minValue.size()) break;
			if (i>=maxValue.size()) break;
			if (i>=expressionColor.size()) break;
			if (i>=expressionWidth.size()) break;

			Object[] row=new Object[5];
			row[0]=expression.get(i);
			row[1]=minValue.get(i);
			row[2]=maxValue.get(i);
			row[3]=expressionColor.get(i);
			row[4]=expressionWidth.get(i);

			data.add(row);
		}

		element.setExpressionData(data);
	}

	private class EditButtonListener implements ActionListener {
		private final int nr;
		private final int row;

		public EditButtonListener(final int nr, final int row) {
			this.nr=nr;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			String s;
			Double D;
			Color c;
			Integer I;

			ExpressionTableModelDialog1 dialog1;
			ExpressionTableModelDialog2 dialog2;

			switch (nr) {
			case 0: /* Ausdruck bearbeiten (auch neuen Eintrag anlegen) */
				if (row<0) {
					dialog1=new ExpressionTableModelDialog1(table,"",0.0,10.0,variableNames,initialVariableValues,stationIDs,stationNameIDs,help,ExpressionTableModelBar.IconMode.BAR);
				} else {
					dialog1=new ExpressionTableModelDialog1(table,expression.get(row),minValue.get(row),maxValue.get(row),variableNames,initialVariableValues,stationIDs,stationNameIDs,help,ExpressionTableModelBar.IconMode.BAR);
				}
				if (dialog1.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row<0) {
						expression.add(dialog1.getExpression());
						minValue.add(dialog1.getMinValue());
						maxValue.add(dialog1.getMaxValue());
						expressionColor.add(Color.BLUE);
						expressionWidth.add(2);
					} else {
						expression.set(row,dialog1.getExpression());
						minValue.set(row,dialog1.getMinValue());
						maxValue.set(row,dialog1.getMaxValue());
					}
					updateTable();
				}
				break;
			case 1: /* Bereich */
				dialog1=new ExpressionTableModelDialog1(table,expression.get(row),minValue.get(row),maxValue.get(row),variableNames,initialVariableValues,stationIDs,stationNameIDs,help,ExpressionTableModelBar.IconMode.BAR);
				if (dialog1.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					expression.set(row,dialog1.getExpression());
					minValue.set(row,dialog1.getMinValue());
					maxValue.set(row,dialog1.getMaxValue());
					updateTable();
				}
				break;
			case 2: /* Linienfarbe und -breite */
				dialog2=new ExpressionTableModelDialog2(table,expressionColor.get(row),expressionWidth.get(row),help);
				if (dialog2.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					expressionColor.set(row,dialog2.getColor());
					expressionWidth.set(row,dialog2.getLineWidth());
					updateTable();
				}
				break;
			case 3: /* Nach oben */
				if (row>0) {
					s=expression.get(row); expression.set(row,expression.get(row-1)); expression.set(row-1,s);
					D=minValue.get(row); minValue.set(row,minValue.get(row-1)); minValue.set(row-1,D);
					D=maxValue.get(row); maxValue.set(row,maxValue.get(row-1)); maxValue.set(row-1,D);
					c=expressionColor.get(row); expressionColor.set(row,expressionColor.get(row-1)); expressionColor.set(row-1,c);
					I=expressionWidth.get(row); expressionWidth.set(row,expressionWidth.get(row-1)); expressionWidth.set(row-1,I);
					updateTable();
				}
				break;
			case 4: /* Nach unten */
				if (row<expression.size()-1) {
					s=expression.get(row); expression.set(row,expression.get(row+1)); expression.set(row+1,s);
					D=minValue.get(row); minValue.set(row,minValue.get(row+1)); minValue.set(row+1,D);
					D=maxValue.get(row); maxValue.set(row,maxValue.get(row+1)); maxValue.set(row+1,D);
					c=expressionColor.get(row); expressionColor.set(row,expressionColor.get(row+1)); expressionColor.set(row+1,c);
					I=expressionWidth.get(row); expressionWidth.set(row,expressionWidth.get(row+1)); expressionWidth.set(row+1,I);
					updateTable();
				}
				break;
			}
		}
	}

	private class DeleteButtonListener implements ActionListener {
		private final int row;

		public DeleteButtonListener(final int row) {
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			final String name=expression.get(row);
			if (!MsgBox.confirm(table,Language.tr("Surface.ExpressionTableModel.Delete.Confirmation.Title"),String.format(Language.tr("Surface.ExpressionTableModel.Delete.Confirmation.Info"),name),Language.tr("Surface.ExpressionTableModel.Delete.Confirmation.YesInfo"),Language.tr("Surface.ExpressionTableModel.Delete.Confirmation.NoInfo"))) return;
			expression.remove(row);
			minValue.remove(row);
			maxValue.remove(row);
			expressionColor.remove(row);
			expressionWidth.remove(row);
			updateTable();
		}
	}
}