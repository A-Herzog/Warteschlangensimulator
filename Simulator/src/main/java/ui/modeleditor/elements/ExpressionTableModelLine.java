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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
	 * In der Tabelle anzuzeigende Datenreihen
	 */
	private final List<ModelElementAnimationLineDiagram.Series> series=new ArrayList<>();

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

		element.getExpressionData().stream().map(graph->new ModelElementAnimationLineDiagram.Series(graph)).forEach(series::add);

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
		return series.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==series.size()) {
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
			final AnimationExpression ex=series.get(rowIndex).expression;
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
					NumberTools.formatNumber(series.get(rowIndex).minValue)+"..."+NumberTools.formatNumber(series.get(rowIndex).maxValue),
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
			if (rowIndex<series.size()-1) {
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
			g.setColor(series.get(rowIndex).color);
			int w=series.get(rowIndex).width;
			ModelElementAnimationLineDiagram.LineMode mode=series.get(rowIndex).lineMode;
			if (mode!=ModelElementAnimationLineDiagram.LineMode.POINTS) {
				/* Linie */
				if (mode.dash==null) {
					((Graphics2D)g).setStroke(new BasicStroke(w));
				} else {
					((Graphics2D)g).setStroke(new BasicStroke(w,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f,mode.dash,0.0f));
				}
				g.drawLine(1,11,21,11);
			} else {
				/* Punkt */
				final int radius=Math.min(8,w);
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
		element.setExpressionData(series);
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

			final ModelElementAnimationLineDiagram.Series  s;

			ExpressionTableModelDialog1 dialog1;
			ExpressionTableModelDialog2 dialog2;

			final List<AnimationExpression> allUsedExpressions=series.stream().map(graph->graph.expression).collect(Collectors.toList());

			switch (nr) {
			case 0: /* Ausdruck bearbeiten (auch neuen Eintrag anlegen) */
				if (row<0) {
					dialog1=new ExpressionTableModelDialog1(table,element,new AnimationExpression(),0.0,10.0,help,ExpressionTableModelBar.IconMode.BAR,allUsedExpressions);
				} else {
					dialog1=new ExpressionTableModelDialog1(table,element,series.get(row).expression,series.get(row).minValue,series.get(row).maxValue,help,ExpressionTableModelBar.IconMode.BAR,allUsedExpressions);
				}
				if (dialog1.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row<0) {
						series.add(new ModelElementAnimationLineDiagram.Series(dialog1.getExpression(),dialog1.getMinValue(),dialog1.getMaxValue(),Color.BLUE,2,ModelElementAnimationLineDiagram.LineMode.LINE));
					} else {
						series.get(row).expression=dialog1.getExpression();
						series.get(row).minValue=dialog1.getMinValue();
						series.get(row).maxValue=dialog1.getMaxValue();
					}
					updateTable();
				}
				break;
			case 1: /* Bereich */
				if (row<0) {
					if (series.size()>0) {
						final double min=series.stream().map(graph->graph.minValue).min(Double::compare).get();
						final double max=series.stream().map(graph->graph.maxValue).max(Double::compare).get();
						dialog1=new ExpressionTableModelDialog1(table,element,null,min,max,help,ExpressionTableModelBar.IconMode.BAR,allUsedExpressions);
						if (dialog1.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
							for (int i=0;i<series.size();i++) {
								series.get(i).minValue=dialog1.getMinValue();
								series.get(i).maxValue=dialog1.getMaxValue();
							}
							updateTable();
						}
					}
				} else {
					dialog1=new ExpressionTableModelDialog1(table,element,series.get(row).expression,series.get(row).minValue,series.get(row).maxValue,help,ExpressionTableModelBar.IconMode.BAR,allUsedExpressions);
					if (dialog1.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
						series.get(row).expression=dialog1.getExpression();
						series.get(row).minValue=dialog1.getMinValue();
						series.get(row).maxValue=dialog1.getMaxValue();
						updateTable();
					}
				}
				break;
			case 2: /* Linienfarbe und -breite */
				dialog2=new ExpressionTableModelDialog2(table,series.get(row).color,series.get(row).width,series.get(row).lineMode,help);
				if (dialog2.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					series.get(row).color=dialog2.getColor();
					series.get(row).width=dialog2.getLineWidth();
					series.get(row).lineMode=dialog2.getLineMode();
					updateTable();
				}
				break;
			case 3: /* Nach oben */
				if (row>0) {
					s=series.get(row); series.set(row,series.get(row-1)); series.set(row-1,s);
					updateTable();
				}
				break;
			case 4: /* Nach unten */
				if (row<series.size()-1) {
					s=series.get(row); series.set(row,series.get(row+1)); series.set(row+1,s);
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
				if (series.size()==0) return;
				if (!MsgBox.confirm(table,Language.tr("Surface.ExpressionTableModel.DeleteAll.Confirmation.Title"),Language.tr("Surface.ExpressionTableModel.DeleteAll.Confirmation.Info"),Language.tr("Surface.ExpressionTableModel.DeleteAll.Confirmation.YesInfo"),Language.tr("Surface.ExpressionTableModel.DeleteAll.Confirmation.NoInfo"))) return;
				series.clear();
				updateTable();
				return;
			}

			final AnimationExpression ex=series.get(row).expression;
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
			series.remove(row);
			updateTable();
		}
	}
}