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

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import language.Language;
import tools.JTableExt;

/**
 * Editor für die Batch-Raten in Form einer Tabelle
 * @author Alexander Herzog
 * @see ModelElementSourceBatchDialog
 * @see ModelElementSourceBatchEditor
 */
public class ModelElementSourceBatchEditor2 extends ModelElementSourceBatchEditorAbstract {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=7150716298828469987L;

	/**
	 * Anzuzeigende Tabelle
	 */
	private final JTableExt table;

	/**
	 * Datenmodell für {@link #table}
	 */
	private final ModelElementSourceBatchEditor2TableModel model;

	/**
	 * Konstruktor der Klasse
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementSourceBatchEditor2(final boolean readOnly) {
		super(readOnly);

		add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
		table.setModel(model=new ModelElementSourceBatchEditor2TableModel(table,()->fireChangeListeners()));

		table.getTableHeader().setReorderingAllowed(false);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				/* Doppelklick links */
				if (e.getClickCount()!=2) return;
				if (!SwingUtilities.isLeftMouseButton(e)) return;

				/* Spalte */
				final int colNr=table.columnAtPoint(e.getPoint());
				final int rowNr=table.rowAtPoint(e.getPoint());
				if (colNr<0 || rowNr<0) return;

				/* Bearbeiten-Aktion auslösen */
				if (colNr==0) model.editSize(rowNr);
			}
		});
		table.getColumnModel().getColumn(2).setMaxWidth(75);
		table.getColumnModel().getColumn(2).setMinWidth(75);
		table.setIsPanelCellTable(2);
		table.setEnabled(!readOnly);
	}

	@Override
	public boolean checkData(boolean showErrorMessage) {
		return model.checkData(showErrorMessage);
	}

	@Override
	public void setDistribution(double[] distribution) {
		model.setDistribution(distribution);
	}

	@Override
	public double[] getDistribution() {
		return model.getDistribution();
	}

	@Override
	public String getEditorName() {
		return Language.tr("Surface.Source.DialogBatchSize.EditorName.Table");
	}
}
