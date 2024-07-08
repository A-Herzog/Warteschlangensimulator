/**
 * Copyright 2024 Alexander Herzog
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
package systemtools.statistics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import systemtools.BaseDialog;

/**
 * Zeigt einen Dialog zur Auswahl einer Tabellenspalte an.
 * @see StatisticViewerTable#showColSelectDialog()
 */
public class StatisticViewerTableColumnSelectDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1239312995180965486L;

	/**
	 * Liste zur Auswahl einer Spalte
	 */
	private final JList<String> list;

	/**
	 * Konstruktor der Klasse
	 * @param owner	‹bergeordnetes Element
	 * @param columns	Liste der anzuzeigenden Spaltennamen
	 */
	public StatisticViewerTableColumnSelectDialog(final Component owner, final List<String> columns) {
		super(owner,StatisticsBasePanel.contextSelectColumn);

		/* GUI */
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		/* Liste */
		final JScrollPane scrollPane=new JScrollPane(list=new JList<>());
		content.add(scrollPane,BorderLayout.CENTER);
		final DefaultListModel<String> model=new DefaultListModel<>();
		list.setModel(model);
		list.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2) close(BaseDialog.CLOSED_BY_OK);}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		/* Daten laden */
		model.addAll(columns);
		list.setSelectedIndex(0);

		/* Dialog starten */
		setResizable(true);
		setMinSizeRespectingScreensize(600,400);
		pack();
		setMaxSizeRespectingScreensize(600,800);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Liefert nach dem Schlieﬂen des Dialogs den Index der ausgew‰hlten Spalte.
	 * @return	0-basierter Index der ausgew‰hlten Spalte oder -1, wenn der Dialog abgebrochen wurde.
	 */
	public int getSelectedColumnIndex() {
		if (getClosedBy()!=BaseDialog.CLOSED_BY_OK) return -1;
		return list.getSelectedIndex();
	}
}
