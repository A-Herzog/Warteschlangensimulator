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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.table.TableCellEditor;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ScaledImageCache;

/**
 * Diese Tabelle h�lt die verschiedenen M�glichkeiten, welches Bild w�hrend der
 * Animation in einem {@link ModelElementAnimationImage}-Element angezeigt werden
 * soll, vor.
 * @author Alexander Herzog
 * @see ModelElementAnimationImage
 */
public class ModelElementAnimationImageTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6468064552683722803L;

	/** Zugeh�rige Tabelle (damit die Tabelle angewiesen werden kann, sich neu zu zeichnen, wenn die Daten ver�ndert wurden) */
	private final JTableExt table;
	/** Hilfe-Callback */
	private final Runnable help;
	/** {@link ModelElementAnimationImage}-Element aus dem die Daten f�r die Tabelle ausgelesen und in das sie auch wieder zur�ckgeschrieben werden sollen */
	private final ModelElementAnimationImage element;
	/** Bedingungen zur Auswahl der Bilder */
	private final List<String> expression;
	/** Anzuzeigende Bilder */
	private final List<BufferedImage> images;
	/** Nur-Lese-Status */
	private final boolean readOnly;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugeh�rige Tabelle (damit die Tabelle angewiesen werden kann, sich neu zu zeichnen, wenn die Daten ver�ndert wurden)
	 * @param element	{@link ModelElementAnimationImage}-Element aus dem die Daten f�r die Tabelle ausgelesen und in das sie auch wieder zur�ckgeschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelElementAnimationImageTableModel(final JTableExt table, final ModelElementAnimationImage element, final boolean readOnly, final Runnable help) {
		super();

		this.table=table;
		this.help=help;
		this.element=element;
		this.readOnly=readOnly;

		expression=new ArrayList<>();
		images=new ArrayList<>();
		for (Object[] data: element.getExpressionData()) {
			expression.add((String)data[0]);
			images.add((BufferedImage)data[1]);
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
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Surface.AnimationImage.Dialog.Images.Add")},new Icon[]{Images.MODELEDITOR_ELEMENT_ANIMATION_IMAGE.getIcon()},new ActionListener[]{new EditButtonListener(0,-1)});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			final String exp=(expression.get(rowIndex)==null)?Language.tr("Surface.AnimationImage.Dialog.Images.DefaultImage"):expression.get(rowIndex);
			final List<Icon> icons=new ArrayList<>();
			icons.add(Images.GENERAL_SETUP.getIcon());
			final List<String> infos=new ArrayList<>();
			infos.add(Language.tr("Surface.AnimationImage.Dialog.Images.Edit"));
			final List<ActionListener> actions=new ArrayList<>();
			actions.add(new EditButtonListener(0,rowIndex));
			if (rowIndex<expression.size()-1) {
				icons.add(Images.EDIT_DELETE.getIcon());
				infos.add(Language.tr("Surface.AnimationImage.Dialog.Images.Delete"));
				actions.add(new DeleteButtonListener(rowIndex));
			}
			if (rowIndex>0 && rowIndex<expression.size()-1) {
				icons.add(Images.ARROW_UP.getIcon());
				infos.add(Language.tr("Surface.AnimationImage.Dialog.Images.Up"));
				actions.add(new EditButtonListener(1,rowIndex));
			}
			if (rowIndex<expression.size()-2) {
				icons.add(Images.ARROW_DOWN.getIcon());
				infos.add(Language.tr("Surface.AnimationImage.Dialog.Images.Down"));
				actions.add(new EditButtonListener(2,rowIndex));
			}
			return makeEditPanelSmallBorderIcon(Images.MODELEDITOR_ELEMENT_ANIMATION_IMAGE.getIcon(),exp,icons.toArray(Icon[]::new),infos.toArray(String[]::new),actions.toArray(ActionListener[]::new));
		case 1:
			return makePanel(ScaledImageCache.getScaledImageCache().getScaledImage(images.get(rowIndex),50,50));
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
		case 0: return Language.tr("Surface.AnimationImage.Dialog.Images.Condition");
		case 1: return Language.tr("Surface.AnimationImage.Dialog.Images.Image");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Schreibt die Tabellendaten in das im Konstruktor angegebene
	 * {@link ModelElementAnimationImage}-Objekt zur�ck.
	 */
	public void storeData() {
		final List<Object[]> data=new ArrayList<>();
		for (int i=0;i<expression.size();i++) {
			final Object[] obj=new Object[2];
			obj[0]=expression.get(i);
			obj[1]=images.get(i);
			data.add(obj);
		}
		element.setExpressionData(data);
	}

	/**
	 * Reagiert auf Klicks auf die Bearbeiten und Verschieben-Schaltfl�chen
	 */
	private class EditButtonListener implements ActionListener {
		/** Auszuf�hrender Befehl (0: Bearbeiten, 1: In der Liste nach oben schieben, 2: In der Liste nach unten schieben) */
		private final int col;
		/** Zeilennummer */
		private final int row;

		/**
		 * Konstruktor der Klasse
		 * @param col	Auszuf�hrender Befehl (0: Bearbeiten, 1: In der Liste nach oben schieben, 2: In der Liste nach unten schieben)
		 * @param row	Zeilennummer
		 */
		public EditButtonListener(final int col, final int row) {
			this.col=col;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			String s;
			BufferedImage img;

			switch (col) {
			case 0:
				final String exp=(row<0)?"":expression.get(row);
				final BufferedImage image=(row<0)?null:images.get(row);
				final ModelElementAnimationImageTableModelDialog dialog=new ModelElementAnimationImageTableModelDialog(table,help,exp,image,element.getModel(),element.getSurface());
				dialog.setVisible(true);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row<0) {
						final int nr=expression.size()-1;
						expression.add(nr,dialog.getExpression());
						images.add(nr,dialog.getImage());
					} else {
						expression.set(row,dialog.getExpression());
						images.set(row,dialog.getImage());
					}
					updateTable();
				}
				break;
			case 1:
				if (row>0 && row<expression.size()-1) {
					s=expression.get(row); expression.set(row,expression.get(row-1)); expression.set(row-1,s);
					img=images.get(row); images.set(row,images.get(row-1)); images.set(row-1,img);
					updateTable();
				}
				break;
			case 2:
				if (row<expression.size()-1) {
					s=expression.get(row); expression.set(row,expression.get(row+1)); expression.set(row+1,s);
					img=images.get(row); images.set(row,images.get(row+1)); images.set(row+1,img);
					updateTable();
				}
				break;
			}
		}
	}

	/**
	 * Reagiert auf Klicks auf die L�schen-Schaltfl�chen
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
			final String exp=expression.get(row);
			if ((e.getModifiers() & ActionEvent.SHIFT_MASK)==0) {
				if (!MsgBox.confirm(table,Language.tr("Surface.AnimationImage.Dialog.Images.Delete.Confirm.Title"),String.format(Language.tr("Surface.AnimationImage.Dialog.Images.Delete.Confirm.Info"),exp),Language.tr("Surface.AnimationImage.Dialog.Images.Delete.Confirm.YesInfo"),Language.tr("Surface.AnimationImage.Dialog.Images.Delete.Confirm.NoInfo"))) return;
			}
			expression.remove(row);
			images.remove(row);
			updateTable();
		}
	}
}