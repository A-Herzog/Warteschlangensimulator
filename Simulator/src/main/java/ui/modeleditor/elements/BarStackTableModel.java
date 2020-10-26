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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;

/**
 * Diese Tabelle wird in {@link ModelElementAnimationBarStackDialog} verwendet,
 * um die verschiedenen Teilbalken zu konfigurieren.
 * @author Alexander Herzog
 * @see ModelElementAnimationBarStackDialog
 * @see ModelElementAnimationBarStack
 */
public class BarStackTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6108339972603381841L;

	/** Tabelle, in der das Modell verwendet werden soll */
	private final JTableExt table;
	/** Modell-Element dessen Ausdrücke und Farben konfiguriert werden sollen */
	private final ModelElementAnimationBarStack element;
	/** Hilfe-Callback */
	private final Runnable help;
	/** Nur-Lese-Status */
	private final boolean readOnly;

	/** Liste der Rechenausdrücke für die Balkensegmente */
	private final List<String> expressions=new ArrayList<>();
	/** Liste der Farben für die Balkensegmente */
	private final List<Color> colors=new ArrayList<>();

	/**
	 * Konstruktor der Klasse
	 * @param table	Tabelle, in der das Modell verwendet werden soll
	 * @param element	Modell-Element dessen Ausdrücke und Farben konfiguriert werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public BarStackTableModel(final JTableExt table, final ModelElementAnimationBarStack element, final boolean readOnly, final Runnable help) {
		super();

		this.table=table;
		this.element=element;
		this.readOnly=readOnly;
		this.help=help;

		expressions.addAll(element.getExpressions());
		colors.addAll(element.getBarColors());
		while (colors.size()<expressions.size()) colors.add(Color.BLACK);
		while (colors.size()>expressions.size()) colors.remove(colors.size()-1);

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
		return expressions.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==expressions.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Surface.AnimationBarStack.Dialog.Add")},new URL[]{Images.MODELEDITOR_ELEMENT_ANIMATION_BAR_STACK.getURL()},new ActionListener[]{new EditButtonListener(0,-1)});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			final List<URL> icons=new ArrayList<>();
			final List<String> names=new ArrayList<>();
			final List<ActionListener> events=new ArrayList<>();
			icons.add(Images.GENERAL_SETUP.getURL());
			names.add(Language.tr("Surface.AnimationBarStack.Dialog.Edit"));
			events.add(new EditButtonListener(0,rowIndex));
			icons.add(Images.EDIT_DELETE.getURL());
			names.add(Language.tr("Surface.AnimationBarStack.Dialog.Delete"));
			events.add(new DeleteButtonListener(rowIndex));
			if (rowIndex>0) {
				icons.add(Images.ARROW_UP.getURL());
				names.add(Language.tr("Surface.AnimationBarStack.Dialog.Up"));
				events.add(new EditButtonListener(1,rowIndex));
			}
			if (rowIndex<expressions.size()-1) {
				icons.add(Images.ARROW_DOWN.getURL());
				names.add(Language.tr("Surface.AnimationBarStack.Dialog.Down"));
				events.add(new EditButtonListener(2,rowIndex));
			}
			return makeEditPanelSmallBorder(
					Images.MODELEDITOR_ELEMENT_ANIMATION_BAR_STACK.getURL(),
					expressions.get(rowIndex),
					icons.toArray(new URL[0]),
					names.toArray(new String[0]),
					events.toArray(new ActionListener[0])
					);
		case 1:
			final JPanel colorPanel=new JPanel();
			colorPanel.setBackground(colors.get(rowIndex));
			return colorPanel;
		default: return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.AnimationBarStack.Dialog.Column.Expression");
		case 1: return Language.tr("Surface.AnimationBarStack.Dialog.Column.Color");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Schreibt die Daten zurück in das im Konstruktor übergebene
	 * {@link ModelElementAnimationBarStack}-Objekt
	 */
	public void storeData() {
		element.getExpressions().clear();
		element.getExpressions().addAll(expressions);

		element.getBarColors().clear();
		element.getBarColors().addAll(colors);
	}

	/**
	 * Reagiert auf Klicks auf die Bearbeiten und Verschieben-Schaltflächen
	 */
	private class EditButtonListener implements ActionListener {
		/** Auszuführender Befehl (0: Bearbeiten, 1: In der Liste nach oben schieben, 2: In der Liste nach unten schieben) */
		private final int col;
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param col	Auszuführender Befehl (0: Bearbeiten, 1: In der Liste nach oben schieben, 2: In der Liste nach unten schieben)
		 * @param row	Zeilennummer
		 */
		public EditButtonListener(final int col, final int row) {
			this.col=col;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			String expression;
			Color color;

			switch (col) {
			case 0:
				final BarStackTableModelDialog dialog=new BarStackTableModelDialog(table,help,(row>=0)?expressions.get(row):null,(row>=0)?colors.get(row):null,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false),element.getModel(),element.getSurface());
				dialog.setVisible(true);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row<0) {
						expressions.add(dialog.getExpression());
						colors.add(dialog.getColor());
					} else {
						expressions.set(row,dialog.getExpression());
						colors.add(row,dialog.getColor());
					}
					updateTable();
				}
				break;
			case 1:
				if (row>0) {
					expression=expressions.get(row); expressions.set(row,expressions.get(row-1)); expressions.set(row-1,expression);
					color=colors.get(row); colors.set(row,colors.get(row-1)); colors.set(row-1,color);
					updateTable();
				}
				break;
			case 2:
				if (row<expressions.size()-1) {
					expression=expressions.get(row); expressions.set(row,expressions.get(row+1)); expressions.set(row+1,expression);
					color=colors.get(row); colors.set(row,colors.get(row+1)); colors.set(row+1,color);
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
			final String name=expressions.get(row);
			if (!MsgBox.confirm(table,Language.tr("Surface.AnimationBarStack.Dialog.Delete.Confirm.Title"),String.format(Language.tr("Surface.AnimationBarStack.Dialog.Delete.Confirm.Info"),name),Language.tr("Surface.AnimationBarStack.Dialog.Delete.Confirm.YesInfo"),Language.tr("Surface.AnimationBarStack.Dialog.Delete.Confirm.NoInfo"))) return;
			expressions.remove(row);
			colors.remove(row);
			updateTable();
		}
	}
}