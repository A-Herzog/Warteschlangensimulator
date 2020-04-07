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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import simulator.statistics.Statistics;
import tools.JTableExt;

/**
 * Tabelle zur Darstellung der Parameter-Vergleichs-Modelle.
 * @author Alexander Herzog
 */
public class ParameterCompareTable extends JPanel {
	private static final long serialVersionUID = 3242958130381580291L;

	private final JTableExt table;
	private final ParameterCompareTableModel tableModel;

	/**
	 * Konstruktor der Klasse
	 * @param setup	Parameter-Vergleichs-Einstellungen
	 * @param help	Hilfe-Runnable
	 * @param loadToEditor	Wird aufgerufen, wenn der Nutzer die Funktion zum Laden eines Modells aus den Ergebnissen in den Editor gewählt hat.
	 * @param compareResults	Wird aufgerufen, wenn der Nutzer den Button zum Vergleichen der Statistikergebnisse verschiedener Modell anklickt
	 * @param setupInput	Wird aufgerufen, wenn der Nutzer auf die Spaltenüberschrift einer Eingabeparameter-Spalte doppelt klickt.
	 * @param setupOutput	Wird aufgerufen, wenn der Nutzer auf die Spaltenüberschrift einer Ausgabeparameter-Spalte doppelt klickt.
	 */
	public ParameterCompareTable(final ParameterCompareSetup setup, final Runnable help, final Consumer<Statistics> loadToEditor, final Runnable compareResults, final Runnable setupInput, final Runnable setupOutput) {
		super();

		setLayout(new BorderLayout());
		JScrollPane scroll=new JScrollPane(table=new JTableExt());
		add(scroll,BorderLayout.CENTER);

		scroll.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -2516207553359526401L;
			@Override public Dimension getPreferredSize() {
				final Dimension d=super.getPreferredSize();
				d.height=(int)Math.round(d.height*1.75);
				return d;
			}
		});

		table.setModel(tableModel=new ParameterCompareTableModel(table,setup,help,()->{updateTable(); table.setIsPanelCellTable();},loadToEditor,compareResults));
		table.setIsPanelCellTable();
		table.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount()!=2) return;
				if (!SwingUtilities.isLeftMouseButton(e)) return;
				final int colNr=table.columnAtPoint(e.getPoint());
				if (colNr<0) return;

				if (colNr==0) return; /* Modellnamen-Spalte */
				if (colNr<=setup.getInput().size()) {
					/* Eingabeparameter */
					if (setupInput!=null) setupInput.run();
					return;
				}
				if (colNr<tableModel.getColumnCount()-1) {
					/* Ausgabeparameter */
					if (setupOutput!=null) setupOutput.run();
					return;
				}
				/* sonst: Steuerungsspalte */
			}
		});
	}

	/**
	 * Aktualisiert die Tabelle, nach dem Änderungen an den Daten vorgenommen wurden.
	 * @param row	Zu aktualisierende Zeile
	 */
	public void updateTableContentOnly(final int row) {
		tableModel.updateTableContentOnly(row);
	}

	/**
	 * Stellt die Anzahl an anzuzeigenden Nachkommastellen ein (wirkt sich nicht auf Kopieren/Speichern der Tabelle).
	 * @param digits	Anzahl an Nachkommastellen (mögliche Werte sind 1, 3 und 9 für Maximalanzahl)
	 */
	public void setDisplayDigits(final int digits) {
		tableModel.setDisplayDigits(digits);
	}

	/**
	 * Aktualisiert die Tabelle, nach dem Änderungen an den Einstellungen vorgenommen wurden.
	 */
	public void updateTable() {
		tableModel.updateTable();
		table.setIsPanelCellTable();

		final int count=table.getColumnModel().getColumnCount();

		final int LAST_COL_WIDTH=175;

		for (int i=0;i<count;i++) {
			final TableColumn column=table.getColumnModel().getColumn(i);
			column.setMaxWidth((i==count-1)?LAST_COL_WIDTH:Integer.MAX_VALUE);
			column.setMinWidth((i==count-1)?LAST_COL_WIDTH:0);
		}
	}

	/**
	 * Macht die Tabelle bearbeitbar oder sperrt diese
	 * @param enabled	Gibt an, ob die Tabelle bearbeitet werden können soll
	 */
	public void setTableEnabled(final boolean enabled) {
		table.setEnabled(enabled);
	}
}