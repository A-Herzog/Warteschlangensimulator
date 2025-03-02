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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.coreelements.ModelElement;

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

	/** Tabelle in der das Datenmodell zum Einsatz kommen soll */
	private final JTableExt table;
	/** Modell-Element dessen Ausdrücke konfiguriert werden sollen */
	private final ModelElement element;
	/** Hilfe-Callback welches aufgerufen wird, wenn in einem der untergeordneten Dialoge auf die "Hilfe"-Schaltfläche geklickt wird. */
	private final Runnable help;
	/** Nur-Lese-Status */
	private final boolean readOnly;

	/**
	 * In der Tabelle anzuzeigende Rechenausdrücke
	 */
	private final List<AnimationExpression> expression=new ArrayList<>();

	/**
	 * In der Tabelle anzuzeigende Minimalwerte
	 */
	private final List<Double> minValue=new ArrayList<>();

	/**
	 * In der Tabelle anzuzeigende Maximalwerte
	 */
	private final List<Double> maxValue=new ArrayList<>();

	/**
	 * In der Tabelle anzuzeigende Farben
	 */
	private final List<Color> expressionColor=new ArrayList<>();

	/**
	 * In der Tabelle anzuzeigende Linienbereiten
	 */
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
		this.element=element;
		this.readOnly=readOnly;

		final List<Object[]> data=element.getExpressionData();
		for (Object[] row: data) if (row.length==5) {
			if (!(row[0] instanceof AnimationExpression)) continue;
			if (!(row[1] instanceof Double)) continue;
			if (!(row[2] instanceof Double)) continue;
			if (!(row[3] instanceof Color)) continue;
			if (!(row[4] instanceof Integer)) continue;
			expression.add((AnimationExpression)row[0]);
			minValue.add((Double)row[1]);
			maxValue.add((Double)row[2]);
			expressionColor.add((Color)row[3]);
			expressionWidth.add((Integer)row[4]);
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
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==expression.size()) {
			switch (columnIndex) {
			case 0:
				return makeButtonPanel(
						new String[]{Language.tr("Surface.ExpressionTableModel.Add"),Language.tr("Surface.ExpressionTableModel.DeleteAll")},
						new Icon[]{Images.MODELEDITOR_ELEMENT_ANIMATION_DIAGRAM_ADD.getIcon(),Images.EDIT_DELETE.getIcon()},
						new ActionListener[]{new EditButtonListener(0,-1),new DeleteButtonListener(-1)});
			case 1:
				return makeButtonPanel(
						new String[] {Language.tr("Surface.ExpressionTableModel.SetRangeForAll")},
						new Icon[] {Images.AXIS_FULL.getIcon()},
						new ActionListener[]{new EditButtonListener(1,-1)});
			default:
				return "";
			}
		}

		switch (columnIndex) {
		case 0:
			final AnimationExpression ex=expression.get(rowIndex);
			final String info;
			switch (ex.getMode()) {
			case Expression: info=ex.getExpression(); break;
			case Javascript: info=Language.tr("Surface.AnimationBarStack.Dialog.ExpressionMode.Javascript"); break;
			case Java: info=Language.tr("Surface.AnimationBarStack.Dialog.ExpressionMode.Java"); break;
			default: info=ex.getExpression(); break;
			}
			return makeEditPanelSmallBorderIcon(
					Images.MODELEDITOR_ELEMENT_ANIMATION_DIAGRAM.getIcon(),
					info,
					new Icon[]{Images.GENERAL_SETUP.getIcon(),Images.EDIT_DELETE.getIcon()},
					new String[]{Language.tr("Surface.ExpressionTableModel.Edit"),Language.tr("Surface.ExpressionTableModel.Delete")},
					new ActionListener[]{new EditButtonListener(0,rowIndex),new DeleteButtonListener(rowIndex)}
					);
		case 1:
			return makeEditPanelSmallBorder(
					NumberTools.formatNumber(minValue.get(rowIndex))+"..."+NumberTools.formatNumber(maxValue.get(rowIndex)),
					Images.GENERAL_SETUP.getIcon(),
					Language.tr("Surface.ExpressionTableModel.SetupArea"),
					new EditButtonListener(1,rowIndex)
					);
		case 2:
			List<Icon> icons=new ArrayList<>();
			List<String> hints=new ArrayList<>();
			List<ActionListener> actions=new ArrayList<>();
			icons.add(Images.GENERAL_SETUP.getIcon());
			hints.add(Language.tr("Surface.ExpressionTableModel.SetupAppearance"));
			actions.add(new EditButtonListener(2,rowIndex));
			if (rowIndex>0) {
				icons.add(Images.ARROW_UP.getIcon());
				hints.add(Language.tr("Surface.ExpressionTableModel.MoveUp"));
				actions.add(new EditButtonListener(3,rowIndex));
			}
			if (rowIndex<expression.size()-1) {
				icons.add(Images.ARROW_DOWN.getIcon());
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
			if (w>=0) {
				/* Linie */
				for (int i=Math.max(0,11-(w-1)/2);i<=Math.min(22,11+w/2);i++) g.drawLine(1,i,21,i);
			} else {
				/* Punkt */
				final int radius=Math.min(8,-w);
				g.fillOval(11-radius,11-radius,2*radius,2*radius);
			}
			return makeEditPanelSmallBorderIcon(
					new ImageIcon(image),
					"",
					icons.toArray(Icon[]::new),
					hints.toArray(String[]::new),
					actions.toArray(ActionListener[]::new)
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

	/**
	 * Reagiert auf Klicks auf die Bearbeiten und Verschieben-Schaltflächen
	 */
	private class EditButtonListener implements ActionListener {
		/** Auszuführender Befehl (0: Bearbeiten oder Hinzufügen, 1: Bereich ändern, 2: Farbe ändern, 3: Nach oben verschieben, 4: Nach unten verschieben) */
		private final int nr;
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param nr	Auszuführender Befehl (0: Bearbeiten oder Hinzufügen, 1: Bereich ändern, 2: Farbe ändern, 3: Nach oben verschieben, 4: Nach unten verschieben)
		 * @param row	Zeilennummer
		 */
		public EditButtonListener(final int nr, final int row) {
			this.nr=nr;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			AnimationExpression s;
			Double D;
			Color c;
			Integer I;

			ExpressionTableModelDialog1 dialog1;
			ExpressionTableModelDialog2 dialog2;

			switch (nr) {
			case 0: /* Ausdruck bearbeiten (auch neuen Eintrag anlegen) */
				if (row<0) {
					dialog1=new ExpressionTableModelDialog1(table,element,new AnimationExpression(),0.0,10.0,help,ExpressionTableModelBar.IconMode.BAR,expression);
				} else {
					dialog1=new ExpressionTableModelDialog1(table,element,expression.get(row),minValue.get(row),maxValue.get(row),help,ExpressionTableModelBar.IconMode.BAR,expression);
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
				if (row<0) {
					if (expression.size()>0) {
						final double min=minValue.stream().min(Double::compare).get();
						final double max=maxValue.stream().max(Double::compare).get();
						dialog1=new ExpressionTableModelDialog1(table,element,null,min,max,help,ExpressionTableModelBar.IconMode.BAR,expression);
						if (dialog1.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
							for (int i=0;i<expression.size();i++) {
								minValue.set(i,dialog1.getMinValue());
								maxValue.set(i,dialog1.getMaxValue());
							}
							updateTable();
						}
					}
				} else {
					dialog1=new ExpressionTableModelDialog1(table,element,expression.get(row),minValue.get(row),maxValue.get(row),help,ExpressionTableModelBar.IconMode.BAR,expression);
					if (dialog1.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
						expression.set(row,dialog1.getExpression());
						minValue.set(row,dialog1.getMinValue());
						maxValue.set(row,dialog1.getMaxValue());
						updateTable();
					}
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

			if (row<0) {
				if (expression.size()==0) return;
				if (!MsgBox.confirm(table,Language.tr("Surface.ExpressionTableModel.DeleteAll.Confirmation.Title"),Language.tr("Surface.ExpressionTableModel.DeleteAll.Confirmation.Info"),Language.tr("Surface.ExpressionTableModel.DeleteAll.Confirmation.YesInfo"),Language.tr("Surface.ExpressionTableModel.DeleteAll.Confirmation.NoInfo"))) return;
				expression.clear();
				minValue.clear();
				maxValue.clear();
				expressionColor.clear();
				expressionWidth.clear();
				updateTable();
				return;
			}

			final AnimationExpression ex=expression.get(row);
			final String info;
			switch (ex.getMode()) {
			case Expression: info=ex.getExpression(); break;
			case Javascript: info=Language.tr("Surface.AnimationBarStack.Dialog.ExpressionMode.Javascript"); break;
			case Java: info=Language.tr("Surface.AnimationBarStack.Dialog.ExpressionMode.Java"); break;
			default: info=ex.getExpression(); break;
			}

			if ((e.getModifiers() & ActionEvent.SHIFT_MASK)==0) {
				if (!MsgBox.confirm(table,Language.tr("Surface.ExpressionTableModel.Delete.Confirmation.Title"),String.format(Language.tr("Surface.ExpressionTableModel.Delete.Confirmation.Info"),info),Language.tr("Surface.ExpressionTableModel.Delete.Confirmation.YesInfo"),Language.tr("Surface.ExpressionTableModel.Delete.Confirmation.NoInfo"))) return;
			}
			expression.remove(row);
			minValue.remove(row);
			maxValue.remove(row);
			expressionColor.remove(row);
			expressionWidth.remove(row);
			updateTable();
		}
	}
}